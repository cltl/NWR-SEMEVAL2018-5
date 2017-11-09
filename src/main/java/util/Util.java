package util;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by piek on 09/11/2017.
 */
public class Util {


    static public ArrayList<File> makeRecursiveFileList(File inputFile, String filter) {
        ArrayList<File> acceptedFileList = new ArrayList<File>();
        File[] theFileList = null;
        if ((inputFile.canRead())) {
            theFileList = inputFile.listFiles();
            for (int i = 0; i < theFileList.length; i++) {
                File newFile = theFileList[i];
                if (newFile.isDirectory()) {
                    ArrayList<File> nextFileList = makeRecursiveFileList(newFile, filter);
                    acceptedFileList.addAll(nextFileList);
                } else if (newFile.getName().endsWith(filter)){
                    acceptedFileList.add(newFile);
                }
            }
        } else {
            System.out.println("Cannot access file:" + inputFile + "#");
            if (!inputFile.exists()) {
                System.out.println("File/folder does not exist!");
            }
        }
        return acceptedFileList;
    }

}
