package org.jelly.html;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Given a dir, list all files recursive, and process each of them.
 *  commandline parameter: -s path/to/input/dir -d path/to/dest/dir
 *
 *  Test:
 *  java -jar htmlprocessor.jar  -i ../../ -l ../../ -s D:\Develop\AndroidSDK\docs\guide -d D:\epub
 */
public class DirProcessor {
    private static DirProcessor mThis;
    private String srcDirectory;
    private String dstDirectory;
    private String[] remainOptions;
    private String imagePattern;
    private String linkPattern;

    private synchronized static DirProcessor instance() {
        if (mThis == null) {
            mThis = new DirProcessor();
        }
        return mThis;
    }

    public static void main(String[] args) throws Exception
    {
        instance().processArgs(args);
    }

    private boolean processArgs(String[] args) throws ParseException, IOException {
        Options options = new Options();
        options.addOption("h", false, "Lists short help");
        options.addOption("s", true, "The source directory to find input files.");
        options.addOption("d", true, "The destination directory to save output files.");
        options.addOption("i", true, "Set the image path pattern that should reprocess."); // copy image, and modify src
        options.addOption("l", true, "Set the link path pattern that should remove."); // remove useless links

        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(options, args);

        if(cmd.hasOption("h")) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("HtmlProcess: make html file to be suitable for epub.\n" +
                    "    java -jar htmlprocessor.jar [options] -s input_dir -d output_dir\n" +
                    "eg: java -jar htmlprocessor.jar -i ../../ -l ../../ -s D:\\AndroidSDK\\docs -d D:\\epub\n", options);
            return false;
        }

        srcDirectory = cmd.getOptionValue("s");
        dstDirectory = cmd.getOptionValue("d");
        imagePattern = cmd.getOptionValue("i");
        linkPattern = cmd.getOptionValue("l");

        remainOptions = cmd.getArgs();

        processDir();

        return true;
    }

    private void processDir() throws IOException, ParseException {
        // list the files
        Collection<File> listFiles = FileUtils.listFiles(new File(srcDirectory), new String[]{"html", "htm"}, true);
        for (File file : listFiles) {
            processFile(file.getAbsolutePath());
        }
    }

    private void processFile(String file) throws IOException, ParseException {
        String[] options = Arrays.copyOf(remainOptions, remainOptions.length + 7);  // copy and add one more item
        options[options.length - 1] = file;
        options[options.length - 2] = FilenameUtils.concat(dstDirectory, file.replace(srcDirectory, ".")); // If second para is /a/b.html, can not concat.
        options[options.length - 3] = "-o";
        options[options.length - 4] = imagePattern;
        options[options.length - 5] = "-i";
        options[options.length - 6] = linkPattern;
        options[options.length - 7] = "-l";
        FileUtils.copyFile(new File(file), new File(options[options.length - 2]));  // First copy the fist to dest path, this command can make dir if nessery.
        HtmlProcessor processor = new HtmlProcessor();
        processor.process(options);
    }
}
