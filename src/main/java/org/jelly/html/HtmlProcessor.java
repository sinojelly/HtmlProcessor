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


// Test:  java -jar htmlprocessor.jar D:\Develop\AndroidSDK\docs\guide\components\activities.html output.html
public class HtmlProcessor 
{
    private static HtmlProcessor mThis;

    private String inputFilePath;
    private String outputFilePath;
    private String inputDirPath;
    private String outputDirPath;
    private Document inputDoc;
    private Document outputDoc;

    private synchronized static HtmlProcessor instance() {
        if (mThis == null) {
            mThis = new HtmlProcessor();
        }
        return mThis;
    }

    public static void main(String[] args) throws Exception
    {
//        if (args.length < 2) {
//            System.out.println("Usage: java -jar htmlprocessor.jar path/to/input/file path/to/output/file");
//            return;
//        }
//        instance().processArgs(args);
        String[] argsDebug = new String[]{"D:\\Develop\\AndroidSDK\\docs\\guide\\components\\activities.html", "output4.html"};
        instance().processArgs(argsDebug);
        instance().redirectStdout2File();
        instance().openDocument();
        instance().constructHtml();

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
        Elements elements = outputDoc.select("a[href^=../../]");
        elements.unwrap(); // remove the tag(<a href=..\..\*>), but keep text
    }

    private void processInsertGoogleStyle() {
        String html = "<link href=\"google/css/default.css\" rel=\"stylesheet\" type=\"text/css\">";
        outputDoc.select("head").append(html);
    }

    private void processArgs(String[] args) throws ParseException {
        /*
        Options options = new Options();
        options.addOption("h", false, "Lists short help");   // -h  to list help
        options.addOption("o", true, "Set the output file path.");
        options.addOption("i", true, "Set the input file path.");
        options.addOption("c", true, "Set the image path should copy."); // copy image, and modify src
        options.addOption("r", true, "Set the link path should remove."); // remove useless links

        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(options, args);

        if(cmd.hasOption("h")) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("HtmlProcess: make html file to be approperate for epub.", options);
            return;
        }

        inputFilePath = cmd.getOptionValue("i");
        outputFilePath = cmd.getOptionValue("o");
        inputFilePath = cmd.getOptionValue("c");
        inputFilePath = cmd.getOptionValue("r");

        if(protocol == null) {
            // 设置默认的 HTTP 传输协议
        } else {
            // 设置用户自定义的 HTTP 传输协议
        }*/
        inputFilePath = args[0];
        outputFilePath = args[1];
        System.out.println("input: " + inputFilePath);
        System.out.println("output: " + outputFilePath);

        File inputFile = new File(inputFilePath);
        inputDirPath = inputFile.getParent();

        File outputFile = new File(FilenameUtils.getFullPath(outputFilePath));
        outputDirPath = outputFile.getAbsolutePath();
    }

    private void processContent() {
        Elements content = inputDoc.select("div#jd-content");
        if (content.size() != 1) {
            System.out.println(String.format("ERROR: [FILE:%s]select div#jd-content size = %d." , inputFilePath, content.size()));
        }
        outputDoc.select("body").append(content.outerHtml());
        //System.out.println(content.outerHtml());
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
//        String style = "<style type=\"text/css\"> \n" +
//                ".pln{color:#000}@media screen{.str{color:#080}.kwd{color:#008}.com{color:#800}.typ{color:#606}.lit{color:#066}.pun,.opn,.clo{color:#660}.tag{color:#008}.atn{color:#606}.atv{color:#080}.dec,.var{color:#606}.fun{color:red}}@media print,projection{.str{color:#060}.kwd{color:#006;font-weight:bold}.com{color:#600;font-style:italic}.typ{color:#404;font-weight:bold}.lit{color:#044}.pun,.opn,.clo{color:#440}.tag{color:#006;font-weight:bold}.atn{color:#404}.atv{color:#060}}pre.prettyprint{padding:2px;border:1px solid #888}ol.linenums{margin-top:0;margin-bottom:0}li.L0,li.L1,li.L2,li.L3,li.L5,li.L6,li.L7,li.L8{list-style-type:none}li.L1,li.L3,li.L5,li.L7,li.L9{background:#eee}"
//                +"</style>";
//        outputDoc.select("head").append(style);
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
        String imageSrcPath = FilenameUtils.concat(inputDirPath, image_src);
        String dstPath = image_src.replaceAll("\\.\\./", "");
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
        //System.out.println(title.outerHtml());
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
