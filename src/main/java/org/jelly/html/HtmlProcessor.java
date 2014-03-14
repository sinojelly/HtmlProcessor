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


// Test:  java -jar htmlprocessor.jar D:\Develop\AndroidSDK\docs\guide\components\activities.html output.html
public class HtmlProcessor 
{
    private static HtmlProcessor mThis;

    private String inputFilePath;
    private String outputFilePath;
    private String inputDirPath;
    private String outputDirPath;
    private Document doc;

    private synchronized static HtmlProcessor instance() {
        if (mThis == null) {
            mThis = new HtmlProcessor();
        }
        return mThis;
    }

    public static void main(String[] args) throws Exception
    {
        if (args.length < 2) {
            System.out.println("Usage: java -jar htmlprocessor.jar path/to/input/file path/to/output/file");
            return;
        }
        instance().processArgs(args);
        instance().redirectStdout2File();
        instance().openDocument();
        instance().processTitle();
        instance().processContent();
    }

    private void processArgs(String[] args) {
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
        Elements content = doc.select("div#jd-content");
        if (content.size() != 1) {
            System.out.println(String.format("ERROR: [FILE:%s]select div#jd-content size = %d." , inputFilePath, content.size()));
        }
        content = processImage(content);
        System.out.println(content.outerHtml());
    }

    private Elements processImage(Elements content) {
        Elements images = content.select("img");
        for (Element img : images) {
            String image_src = img.attr("src");
            img.attr("src", processImageSrc(image_src));
        }
        return content;
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
        return imageDstPath;
    }

    private void processTitle() {
        Elements title = doc.select("h1[itemprop=name]");
        if (title.size() != 1) {
            System.out.println(String.format("ERROR: [FILE:%s]select h1[itemprop=name] size = %d." , inputFilePath, title.size()));
        }
        System.out.println(title.outerHtml());
    }

    private void openDocument() throws IOException {
        File input = new File(inputFilePath);
        doc = Jsoup.parse(input, "UTF-8", "http://example.com/");
    }

    private void redirectStdout2File() throws FileNotFoundException {
        // standard output redirect to file
        PrintStream ps=new PrintStream(new FileOutputStream(outputFilePath));
        System.setOut(ps);
    }
}
