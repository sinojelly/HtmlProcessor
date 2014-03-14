package org.jelly.html;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

// Test:  java -jar htmlprocessor.jar D:\Develop\AndroidSDK\docs\guide\components\activities.html output.html
public class HtmlProcessor 
{

    public static void main(String[] args) throws Exception
    {
        if (args.length < 2) {
            System.out.println("Usage: java -jar htmlprocessor.jar path/to/input/file path/to/output/file");
            return;
        }
        String inputFilePath = args[0];
        String outputFilePath = args[1];
        System.out.println("input: " + inputFilePath);
        System.out.println("output: " + outputFilePath);

        // standard output redirect to file
        PrintStream ps=new PrintStream(new FileOutputStream(outputFilePath));
        System.setOut(ps);

        File input = new File(inputFilePath);
        Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");

        Elements title = doc.select("h1[itemprop=name]");
        if (title.size() != 1) {
            System.out.println(String.format("ERROR: [FILE:%s]select h1[itemprop=name] size = %d." , inputFilePath, title.size()));
        }
        System.out.println(title.outerHtml());
        Elements content = doc.select("div#jd-content");
        if (content.size() != 1) {
            System.out.println(String.format("ERROR: [FILE:%s]select div#jd-content size = %d." , inputFilePath, content.size()));
        }
        System.out.println(content.outerHtml());
    }
}
