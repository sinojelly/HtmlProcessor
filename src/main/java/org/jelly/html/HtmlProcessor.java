package org.jelly.html;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.cli.*;


// Test:
// java -jar htmlprocessor.jar -i ../ -l ../ -o D:\epub\output.html D:\Develop\AndroidSDK\docs\guide\components\activities.html
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
    private String destDirectory;

    private boolean useOriginalFile = false;
    private String relativePath2DstDirectory;

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

        openDocument();
        constructHtml();
        outputDocument();
    }

    private void outputDocument() throws FileNotFoundException {
        if (useOriginalFile) return;   // the original file has been copied in DirProcessor.
        //redirectStdout2File();
        //System.out.println(outputDoc.outerHtml());
        //System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        try {
            FileOutputStream fos = new FileOutputStream(outputFilePath);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            osw.write(outputDoc.outerHtml());
            osw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void constructHtml() {
        String html = "<html><head></head>"   // <title>First parse</title>
                + "<body></body></html>";
        outputDoc = Jsoup.parse(html);
        outputDoc.select("head").append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">"); // Fix some words can not show problem.
        processTitle();
        processContent();
        processImage();
        processPrettyPrint();
        processInsertGoogleStyle();
        processTrimUselessLinks();
    }

    private void processTrimUselessLinks() {
        if (linkPattern == null) return;
        Elements elements = outputDoc.select("a[href^=" + linkPattern + "]");
        elements.unwrap(); // remove the tag(<a href=..\..\*>), but keep text
    }

    private void processInsertGoogleStyle() {
        String html = String.format("<link href=\"%s\" rel=\"stylesheet\" type=\"text/css\">", relativePath2DstDirectory + "google/css/default.css");
        outputDoc.select("head").append(html);
    }

    private boolean processArgs(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("h", false, "Lists short help");   // -h  to list help
        options.addOption("i", true, "Set the image path pattern that should reprocess."); // copy image, and modify src
        options.addOption("l", true, "Set the link path pattern that should remove."); // remove useless links
        options.addOption("o", true, "Set the output file path.");
        options.addOption("d", true, "Set the dest dir.");

        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(options, args);

        if(cmd.hasOption("h")) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("HtmlProcess: make html file to be suitable for epub.\n" +
                    "    java -jar htmlprocessor.jar options input_file\n" +
                    "eg: java -jar htmlprocessor.jar -i ../ -l ../ -o D:\\epub\\output.html D:\\AndroidSDK\\docs\\activities.html\n", options);
            return false;
        }

        imagePattern = cmd.getOptionValue("i");
        linkPattern = cmd.getOptionValue("l");
        outputFilePath = cmd.getOptionValue("o");
        destDirectory = cmd.getOptionValue("d");
        inputFilePath = cmd.getArgs()[0];  // the left is:  inputFilePath
        relativePath2DstDirectory = getRelativePath(outputFilePath, destDirectory);

        System.out.println("input: " + inputFilePath);
        System.out.println("output: " + outputFilePath);

        File inputFile = new File(inputFilePath);
        inputDirPath = inputFile.getParent();

        File outputFile = new File(FilenameUtils.getFullPath(outputFilePath));
        outputDirPath = outputFile.getAbsolutePath();
        return true;
    }


    /*
        String test = instance().getRelativePath("D:\\a\\b\\c.txt", "D:\\a");
        System.out.println(test.equals("../") + " Result: " + test);

        test = instance().getRelativePath("D:\\a\\b\\c.txt", "D:\\a\\e");
        System.out.println(test.equals("../e/") + " Result: " + test);

        test = instance().getRelativePath("D:/a/b/c.txt", "D:/a/e");
        System.out.println(test.equals("../e/") + " Result: " + test);
     */
    private String getRelativePath(String filePath, String directory) {
        final String SEPARATOR = "/";
        filePath = filePath.replaceAll("\\\\", "/");
        directory = directory.replaceAll("\\\\", "/");
        String fileDir = filePath.substring(0, filePath.lastIndexOf(SEPARATOR));  // remove file name
        String[] fileArray = fileDir.split(SEPARATOR);
        String[] dirArray = directory.split(SEPARATOR);
        StringBuffer result = new StringBuffer("");
        int i;
        for (i = 0; i < fileArray.length && i < dirArray.length; i++) {
            if (fileArray[i].equals(dirArray[i])) {
                continue;
            }
            break;
        }
        int j = i;
        for (;i < fileArray.length; i++) {
            result.append(".."+SEPARATOR);
        }
        if (j >= dirArray.length) {
            return result.toString();
        }
        for (; j < dirArray.length; j++) {
            result.append(dirArray[j] + SEPARATOR);
        }
        return result.toString();
    }

    private void processContent() {
        Elements content = inputDoc.select("div#jd-content");
        if (content.size() != 1) {
            System.out.println(String.format("Warning: [FILE:%s]select div#jd-content size = %d. use original file." , inputFilePath, content.size()));
            useOriginalFile = true;
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
        outputDoc.select("head").append(String.format("<script type=\"text/javascript\" src=\"%s\"></script>", relativePath2DstDirectory + "google-code-prettify/prettify.js"));
        outputDoc.select("head").append(String.format("<link href=\"%s\" type=\"text/css\" rel=\"stylesheet\" />", relativePath2DstDirectory + "google-code-prettify/prettify.css"));
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
        if (image_src.startsWith("/")) {
            image_src = "." + image_src;
        }
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
            System.out.println(String.format("Warning: [FILE:%s]select h1[itemprop=name] size = %d. use original file." , inputFilePath, title.size()));
            useOriginalFile = true;
        }
        outputDoc.select("body").prepend(title.outerHtml());
        outputDoc.select("head").prepend("<title>" + title.text() + "</title>");
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
