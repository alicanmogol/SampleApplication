package com.fererlab.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * acm | 2/27/13
 */
public class FileContentHandler {

    private String contentPath;

    public String getContent(String folderName, String fileName) {
        String xslContent = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
                "<xsl:output method=\"html\" encoding=\"utf-8\" indent=\"yes\" />\n" +
                "<xsl:template match=\"/\">\n" +
                "</xsl:template>\n" +
                "</xsl:stylesheet>";
        String xslFilePath = getContentPath() + folderName + "/" + fileName;
        try {
            xslContent = new Scanner(new File(xslFilePath)).useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return xslContent;
    }

    private String getContentPath() {
        if (contentPath == null) {
            contentPath = getClass().getResource(".").getPath().substring(
                    0,
                    getClass().getResource(".").getPath().length() -
                            (1 + (getClass().getPackage().getName().replace('.', '/')).length())
            ) + "_/";
        }
        return contentPath;
    }

}
