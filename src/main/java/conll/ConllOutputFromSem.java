package conll;

import util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by piek on 11/11/2017.
 */
public class ConllOutputFromSem {

    /**
     * #begin document (a212420b8d7c079bd385ff4dba9fea86);
     a212420b8d7c079bd385ff4dba9fea86.DCT	2017-01-14	DCT	-
     a212420b8d7c079bd385ff4dba9fea86.t1.0	Reno	TITLE	-
     a212420b8d7c079bd385ff4dba9fea86.t1.1	Man	TITLE	-
     a212420b8d7c079bd385ff4dba9fea86.t1.2	Arrested	TITLE	-
     a212420b8d7c079bd385ff4dba9fea86.t1.3	For	TITLE	-
     a212420b8d7c079bd385ff4dba9fea86.t1.4	Accidental	TITLE	-
     a212420b8d7c079bd385ff4dba9fea86.t1.5	Fatal	TITLE	-
     a212420b8d7c079bd385ff4dba9fea86.t1.6	Shooting	TITLE	-
     a212420b8d7c079bd385ff4dba9fea86.t1.7	-	TITLE	-
     a212420b8d7c079bd385ff4dba9fea86.t1.8	KTVN	TITLE	-
     a212420b8d7c079bd385ff4dba9fea86.t1.9	Channel	TITLE	-
     a212420b8d7c079bd385ff4dba9fea86.t1.10	2	TITLE	-
     a212420b8d7c079bd385ff4dba9fea86.t1.11	-	TITLE	-
     a212420b8d7c079bd385ff4dba9fea86.t1.12	Reno	TITLE	-
     a212420b8d7c079bd385ff4dba9fea86.t1.13	Tahoe	TITLE	-
     a212420b8d7c079bd385ff4dba9fea86.t1.14	Sparks	TITLE	-
     a212420b8d7c079bd385ff4dba9fea86.t1.15	News	TITLE	-
     a212420b8d7c079bd385ff4dba9fea86.t1.16	,	TITLE	-
     a212420b8d7c079bd385ff4dba9fea86.t1.17	Weather	TITLE	-
     a212420b8d7c079bd385ff4dba9fea86.t1.18	,	TITLE	-
     a212420b8d7c079bd385ff4dba9fea86.t1.19	Video	TITLE	-
     a212420b8d7c079bd385ff4dba9fea86.b1.0	Reno	BODY	-
     a212420b8d7c079bd385ff4dba9fea86.b1.1	Police	BODY	-
     a212420b8d7c079bd385ff4dba9fea86.b1.2	have	BODY	-
     #end document
     */



    static public void resultForCoNLLFile(File resultFolder, File coNLLfile, ArrayList<String> allEventKeys, HashMap<String, String> tokenEventMap) {
        try {
            FileInputStream fis = new FileInputStream(coNLLfile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            File resultFile = new File (resultFolder.getAbsolutePath()+"/"+coNLLfile.getName());
            OutputStream fos = new FileOutputStream(resultFile);
            String inputLine = "";
            String fileName = "";


            /// we use a tokencounter to create token identifiers.
            /// this is delicate as these tokens need to match across the NAF files and the CoNLL file
            /// make sure we use the same counting when we convert conll to naf in ConllNafConversion
            /// We skip DCT and NEWLINE and only count the real tokens
            /// For every new file, we reset the counter to zero

            int tokenCounter = 0;
            while (in.ready() && (inputLine = in.readLine()) != null) {
                String str = "";
                if (inputLine.startsWith("#begin document")) {
                    //#begin document (a212420b8d7c079bd385ff4dba9fea86);
                    fileName = inputLine.substring(inputLine.indexOf("(")+1, inputLine.lastIndexOf(")"));
                    tokenCounter = 0;
                    str += inputLine+"\n";
                }
                else if (inputLine.startsWith("#end document")) {
                    str += inputLine+"\n";
                }
                else {
                    String[] fields = inputLine.split("\t");
                    if (fields.length==4) {
                        CoNLLdata coNLLdata = new CoNLLdata(inputLine);
                        if (coNLLdata.getDunit().equals("DCT")) {
                            //a212420b8d7c079bd385ff4dba9fea86.DCT	2017-01-14	DCT	-
                            str += inputLine+"\n";
                        }
                        else if (coNLLdata.getWord().equals("NEWLINE")) {
                            str += inputLine+"\n";
                        }
                        else {
                            tokenCounter++;
                            String tag = "-";
                            String tokenId = "";
                            ///the format needs to be identical to Task5EventCoref:getTokenEventMap
                            tokenId = fileName+":w"+tokenCounter;
                            if (tokenEventMap.containsKey(tokenId)) {
                                String eventId = tokenEventMap.get(tokenId);
                                Integer intId = Util.getEventId(eventId, allEventKeys);
                                tag= "("+intId.toString()+")";
                            }
                            str += coNLLdata.toConll(tag);
                        }
                    }
                    else {}
                }
                fos.write(str.getBytes());
            }
            fos.flush();
            fos.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static public void resultForCoNLLFile(File coNLLfile, ArrayList<String> allEventKeys, HashMap<String, String> tokenEventMap) {
        try {
            FileInputStream fis = new FileInputStream(coNLLfile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            File resultFile = new File (coNLLfile.getAbsolutePath()+".result");
            OutputStream fos = new FileOutputStream(resultFile);
            String inputLine = "";
            String fileName = "";

            /// we use a tokencounter to create token identifiers.
            /// this is delicate as these tokens need to match across the NAF files and the CoNLL file
            /// make sure we use the same counting when we convert conll to naf in ConllNafConversion
            /// We skip DCT and NEWLINE and only count the real tokens
            /// For every new file, we reset the counter to zero

            int tokenCount = 0;
            while (in.ready() && (inputLine = in.readLine()) != null) {
                String str = "";
                if (inputLine.startsWith("#begin document")) {
                    //#begin document (a212420b8d7c079bd385ff4dba9fea86);
                    str += inputLine+"\n";
                    fileName = inputLine.substring(inputLine.indexOf("(")+1, inputLine.lastIndexOf(")"));
                    tokenCount = 0;
                }
                else if (inputLine.startsWith("#end document")) {
                    str += inputLine+"\n";
                }
                else {
                    String[] fields = inputLine.split("\t");
                    if (fields.length==4) {
                        CoNLLdata coNLLdata = new CoNLLdata(inputLine);
                        if (coNLLdata.getDunit().equals("DCT")) {
                            //a212420b8d7c079bd385ff4dba9fea86.DCT	2017-01-14	DCT	-
                            str += inputLine+"\n";
                        }
                        else if (coNLLdata.getWord().equals("NEWLINE")) {
                            str += inputLine+"\n";
                        }
                        else {
                            String tag = "-";
                            tokenCount++;
                            ///the format needs to be identical to Task5EventCoref:getTokenEventMap
                            String tokenId = fileName+":w"+tokenCount;
                            if (tokenEventMap.containsKey(tokenId)) {
                                //tokenId = 00a4747ab229a2ea49288743a55ab22b:w776  filename+NAF token identifier
                                String eventId = tokenEventMap.get(tokenId);
                                Integer intId = Util.getEventId(eventId, allEventKeys);
                                tag= "("+intId.toString()+")";
                            }
                            else {
                               ///////
                            }
                            str += coNLLdata.toConll(tag);
                        }
                    }
                    else {}
                }
                fos.write(str.getBytes());
            }
            fos.flush();
            fos.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
