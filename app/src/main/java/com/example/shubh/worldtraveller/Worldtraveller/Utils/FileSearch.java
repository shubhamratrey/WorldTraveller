package com.example.shubh.worldtraveller.Worldtraveller.Utils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by User on 7/24/2017.
 */

public class FileSearch {

    /**
     * Search a directory and return a list of all **directories** contained inside
     *
     * @param directory
     * @return
     */
    public static ArrayList<String> getDirectoryPaths(String directory) {
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listfiles = file.listFiles();
        for (int i = 0; i < listfiles.length; i++) {
            if (listfiles[i].isDirectory()) {
                pathArray.add(listfiles[i].getAbsolutePath());
            }
        }
        return pathArray;
    }

    /**
     * Search a directory and return a list of all **files** contained inside
     *
     * @param directory
     * @return
     */
    /*public static ArrayList<String> getFilePaths(String directory) {


        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listfiles = file.listFiles();

        for (int i = 0; i < listfiles.length; i++) {
            String filepath = listfiles[i].getAbsolutePath();
            if (filepath.endsWith(".jpg")||filepath.endsWith(".jpeg")) {
                pathArray.add(filepath);
            }

        }
        return pathArray;
    }*/
    public static ArrayList<String> getFilePaths(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listfiles = file.listFiles();
        for(int i = 0; i < listfiles.length; i++){

            if(listfiles[i].isFile()){
                String s =listfiles[i].getAbsolutePath();
                //if(s.contains(".jpg") || s.contains(".png")|| s.contains(".JPEG"))
                    pathArray.add(s);
            }
        }
        return pathArray;
    }
}
