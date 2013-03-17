package com.fererlab.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.Scanner;

/**
 * acm | 2/27/13
 */
public class FileContentHandler {

    private String contentPath;
    private String filePath;
    private String fileExtension = "";

    public byte[] getContent(String folderName, String fileName) {
        byte[] bytes = new byte[0];
        filePath = getContentPath() + folderName + "/" + fileName;
        fileExtension = filePath.substring(filePath.lastIndexOf(".") + 1).trim();
        try {
            RandomAccessFile f = new RandomAccessFile(filePath, "rw");
            bytes = new byte[(int) f.length()];
            f.read(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileExtension() {
        return fileExtension;
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
