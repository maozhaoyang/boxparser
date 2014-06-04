package com.boxparser.test;

import java.io.IOException;
import java.io.InputStream;

import com.boxparser.parse.HtmlParser;
import com.boxparser.test.tool.IOUtils;

public class Main {

    public static void main(String[] args) throws IOException {

        final String encoding = "GBK";
        final String baseUri = "";
        InputStream file = Thread.currentThread().getContextClassLoader().getResourceAsStream("merge8.html");
        String input = IOUtils.toString(file, encoding);
        String html = HtmlParser.parseHTML(input);
        System.out.println(html);

    }
}
