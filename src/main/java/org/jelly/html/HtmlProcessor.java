package org.jelly.html;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.cli.*;


// Test:
// java -jar htmlprocessor.jar -i ../../ -l ../../ -o D:\epub\output.html D:\Develop\AndroidSDK\docs\guide\components\activities.html
public class HtmlProcessor 
{
    private static HtmlProcessor mThis;

    private String inputFilePath;
    private String outputFilePath;
    private String inputDirPath;
    private String outputDirPath;
    private Document inputDoc;
    private Document outputDoc;
    private String imagePattern;
    private String linkPattern;

    HtmlProcessor() {
    }

//    private synchronized static HtmlProcessor instance() {
//        if (mThis == null) {
//            mThis = new HtmlProcessor();
//        }
//        return mThis;
//    }
//
//    private static void main(String[] args) throws Exception
//    {
//        instance().process(args);
//    }

    void process(String[] args) throws ParseException, IOException {
        if (!processArgs(args)) {
            return;
        }
//        String[] argsDebug = new String[]{"D:\\Develop\\AndroidSDK\\docs\\guide\\components\\activities.html", "output4.html"};
//        instance().processArgs(argsDebug);
        redirectStdout2File();
        openDocument();
        constructHtml();
    }

    private void constructHtml() {
        String html = "<html><head></head>"   // <title>First parse</title>
                + "<body></body></html>";
        outputDoc = Jsoup.parse(html);
        processTitle();
        processContent();
        processImage();
        processPrettyPrint();
        processInsertGoogleStyle();
        processTrimUselessLinks();
        System.out.println(outputDoc.outerHtml());
    }

    private void processTrimUselessLinks() {
        if (linkPattern == null) return;
        Elements elements = outputDoc.select("a[href^=" + linkPattern + "]");
        elements.unwrap(); // remove the tag(<a href=..\..\*>), but keep text
    }

    private void processInsertGoogleStyle() {
        String html = "<link href=\"google/css/default.css\" rel=\"stylesheet\" type=\"text/css\">";
        outputDoc.select("head").append(html);
    }

    private boolean processArgs(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("h", false, "Lists short help");   // -h  to list help
        options.addOption("i", true, "Set the image path pattern that should reprocess."); // copy image, and modify src
        options.addOption("l", true, "Set the link path pattern that should remove."); // remove useless links
        options.addOption("o", true, "Set the output file path.");

        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(options, args);

        if(cmd.hasOption("h")) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("HtmlProcess: make html file to be suitable for epub.\n" +
                    "    java -jar htmlprocessor.jar options input_file\n" +
                    "eg: java -jar htmlprocessor.jar -i ../../ -l ../../ -o D:\\epub\\output.html D:\\AndroidSDK\\docs\\activities.html\n", options);
            return false;
        }

        imagePattern = cmd.getOptionValue("i");
        linkPattern = cmd.getOptionValue("l");
        outputFilePath = cmd.getOptionValue("o");
        inputFilePath = cmd.getArgs()[0];  // the left is:  inputFilePath

        System.out.println("input: " + inputFilePath);
        System.out.println("output: " + outputFilePath);

        File inputFile = new File(inputFilePath);
        inputDirPath = inputFile.getParent();

        File outputFile = new File(FilenameUtils.getFullPath(outputFilePath));
        outputDirPath = outputFile.getAbsolutePath();
        return true;
    }

    private void processContent() {
        Elements content = inputDoc.select("div#jd-content");
        if (content.size() != 1) {
            System.out.println(String.format("ERROR: [FILE:%s]select div#jd-content size = %d." , inputFilePath, content.size()));
        }
        outputDoc.select("body").append(content.outerHtml());
    }

    private void processPrettyPrint() {
        prettyInsertHead();
        prettyInsertOnload();
        prettyInsertPre();
    }

    private void prettyInsertPre() {
        outputDoc.select("pre").addClass("prettyprint");
    }

    private void prettyInsertOnload() {
        outputDoc.select("body").attr("onload", "prettyPrint()");
    }

    private void prettyInsertHead() {
        outputDoc.select("head").append("<script type=\"text/javascript\" src=\"google-code-prettify/prettify.js\"></script>");
        outputDoc.select("head").append("<link href=\"google-code-prettify/prettify.css\" type=\"text/css\" rel=\"stylesheet\" />");
    }

    private void processImage() {
        Elements images = outputDoc.select("img");
        for (Element img : images) {
            String image_src = img.attr("src");
            img.attr("src", processImageSrc(image_src));
        }
    }

    //  inputPath
    // calc image path
    // copy image to outputPath/images
    // return new image_src
    private String processImageSrc(String image_src) {
        if (imagePattern == null) return image_src;
        String imageSrcPath = FilenameUtils.concat(inputDirPath, image_src);
        String dstPath = image_src.replaceAll(imagePattern, "");
//        System.err.println("outputDirPath = " + outputDirPath);
//        System.err.println("dstPath = " + dstPath);
        String imageDstPath = FilenameUtils.concat(outputDirPath, dstPath);
//        System.err.println("imageSrcPath = " + imageSrcPath);
//        System.err.println("imageDstPath = " + imageDstPath);
        try {
            FileUtils.copyFile(new File(imageSrcPath), new File(imageDstPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dstPath;
    }

    private void processTitle() {
        Elements title = inputDoc.select("h1[itemprop=name]");
        if (title.size() != 1) {
            System.out.println(String.format("ERROR: [FILE:%s]select h1[itemprop=name] size = %d." , inputFilePath, title.size()));
        }
        outputDoc.select("body").prepend(title.outerHtml());
    }

    private void openDocument() throws IOException {
        File input = new File(inputFilePath);
        inputDoc = Jsoup.parse(input, "UTF-8", "http://example.com/");
    }

    private void redirectStdout2File() throws FileNotFoundException {
        // standard output redirect to file
        PrintStream ps=new PrintStream(new FileOutputStream(outputFilePath));
        System.setOut(ps);
    }
}
