package question;

/**
 * Created by piek on 09/11/2017.
 * We read all CoNLL files from a directory and translate these to NAF files
 * For each subtask there is a CONLL folder with separate CONLL files
 * 1-84890.conll
 * 1-87552.conll
 *
 * The initial number indicates the subtask 1,2,3
 * The number after "-" is the file identifier
 *
 * The same file can be listed for multiple tasks!!!!!
 */


import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafWordForm;

import java.io.*;
import java.util.ArrayList;

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
public class ConllNafConversion {

    static public void main(String[] args) {
        String inputfolder = "";
        inputfolder = "/Code/vu/newsreader/nwr-semeval2018-5-bu/examples/s3/CONLL/";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--folder") && args.length>(i+1)) {
                inputfolder = args[i+1];
            }
        }
        ArrayList<File> files = util.Util.makeRecursiveFileList(new File(inputfolder), ".conll");
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            test(new File(inputfolder), file);
        }
    }


    static void test(File parentFolder, File conllfile) {
        readCoNLLFile(parentFolder, conllfile);
    }


    /*static void readCoNLLFile (File coNLLfile, File kafFile) {
        KafSaxParser kafSaxParser = new KafSaxParser();
        kafSaxParser.parseFile(kafFile);
        // System.out.println("kafFile.getName() = " + kafFile.getName());
        HashMap<String, CoNLLdata> conllDataMap = new HashMap<String,CoNLLdata>();
        try {
            FileInputStream fis = new FileInputStream(coNLLfile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine = "";
            while (in.ready() && (inputLine = in.readLine()) != null) {
                String[] fields = inputLine.split("\t");
                if (fields.length == 5) {
                    String fileId = fields[0];
                    // System.out.println("fileId = " + fileId);
                    if (kafFile.getName().startsWith(fileId) &&
                            !kafFile.getName().startsWith(fileId+"plus")) {
                        String sentenceId = fields[1];
                        String tokenId = "w"+fields[2];
                        String token = fields[3];
                        String tag = fields[4];
                        if (!tag.equals("-")) {
                            //  System.out.println("tokenId = " + tokenId);
                            KafTerm term = kafSaxParser.getTermForWordId(tokenId);
                            if (term != null) {
                                CoNLLdata coNLLdata = new CoNLLdata(fileId, sentenceId, tokenId, token, tag);
                                conllDataMap.put(term.getTid(), coNLLdata);
                            }
                            else {
                                //   System.out.println("no term for tokenId = " + tokenId);
                            }
                        }
                    }
                }
            }
            in.close();
            System.out.println("conllDataMap = " + conllDataMap.size());
            for (int i = 0; i < kafSaxParser.kafEventArrayList.size(); i++) {
                KafEvent kafEvent = kafSaxParser.kafEventArrayList.get(i);
                ArrayList<String> spanIds = kafEvent.getSpanIds();
                boolean match = false;
                for (int j = 0; j < spanIds.size(); j++) {
                    String s = spanIds.get(j);
                    if (conllDataMap.containsKey(s)) {
                        matchedEvents.add(s);
                        kafEvent.setStatus("true");
                        System.out.println("true = " + s);
                    }
                }
                if (!match) {
                    kafEvent.setStatus("false");
                }
            }
            Set keySet = conllDataMap.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                if (!matchedEvents.contains(key)) {
                    KafEvent kafEvent = new KafEvent();
                    int n = kafSaxParser.kafEntityArrayList.size();
                    kafEvent.setId("p"+n);
                    kafEvent.setStatus("new");
                    System.out.println("new = " + key);
                    CorefTarget corefTarget = new CorefTarget();
                    corefTarget.setId(key);
                    kafEvent.addSpan(corefTarget);
                    kafSaxParser.kafEventArrayList.add(kafEvent);
                }
            }
            //  kafSaxParser.writeNafToStream(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/

    static void readCoNLLFile(File parentFolder, File coNLLfile) {
        /*
       #begin document (a212420b8d7c079bd385ff4dba9fea86);
a212420b8d7c079bd385ff4dba9fea86.DCT	2017-01-14	DCT	-
a212420b8d7c079bd385ff4dba9fea86.t1.0	Reno	TITLE	-
a212420b8d7c079bd385ff4dba9fea86.t1.1	Man	TITLE	-
a212420b8d7c079bd385ff4dba9fea86.t1.2	Arrested	TITLE	-
a212420b8d7c079bd385ff4dba9fea86.t1.3	For	TITLE	-
a212420b8d7c079bd385ff4dba9fea86.t1.4	Accidental	TITLE	-
a212420b8d7c079bd385ff4dba9fea86.t1.5	Fatal	TITLE	-
e54a480756b852ed2f0596e130652b64.b20.19	members	BODY	-
e54a480756b852ed2f0596e130652b64.b20.20	.	BODY	-
e54a480756b852ed2f0596e130652b64.b20.21	NEWLINE	BODY	-
#end document
#begin document (ea781ee5a57a46b285d834708fee8c0d);
         */


        try {
            FileInputStream fis = new FileInputStream(coNLLfile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine = "";
            String fileName = "";
            String rawText = "";
            KafSaxParser kafSaxParser = new KafSaxParser();
            while (in.ready() && (inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("#begin document")) {
                    //#begin document (a212420b8d7c079bd385ff4dba9fea86);
                    System.out.println("inputLine = " + inputLine);
                    kafSaxParser.init();
                    rawText = "";
                    fileName = inputLine.substring(inputLine.indexOf("(")+1, inputLine.lastIndexOf(")"));
                    kafSaxParser.getKafMetaData().setUrl(fileName);
                }
                else if (inputLine.startsWith("#end document")) {
                    kafSaxParser.rawText = rawText.trim();
                    String outputPath = parentFolder.getAbsolutePath()+"/"+fileName+".naf";
                    OutputStream fos = new FileOutputStream(outputPath);
                    kafSaxParser.writeNafToStream(fos);
                    fos.close();
                }
                else {
                    String[] fields = inputLine.split("\t");
                    if (fields.length==4) {
                        CoNLLdata coNLLdata = new CoNLLdata(inputLine);
                        rawText += " "+coNLLdata.getWord();
                        int n = kafSaxParser.getKafWordFormList().size()+1;
                        KafWordForm kafWordForm = coNLLdata.toKafWordForm(n);
                        kafSaxParser.kafWordFormList.add(kafWordForm);
                    }
                    else {
                        System.out.println("inputLine = " + inputLine);
                    }
                }

            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


/*    static void readCoNLLFile(String coNLLfile, String incident, int tokenColumn) {


        HashMap<String, CoNLLdata> conllDataMap = new HashMap<String, CoNLLdata>();
        try {
            FileInputStream fis = new FileInputStream(coNLLfile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine = "";
            HashMap<String, String> fileContentMap = new HashMap<String, String>();
            while (in.ready() && (inputLine = in.readLine()) != null) {
                String[] fields = inputLine.split("\t");
                if (fields.length > tokenColumn) {
                    String fileName = fields[0];
                    String token = fields[tokenColumn];
                    if (token.equals("NEWLINE")) token = "\n";
                    if (fileContentMap.containsKey(fileName)) {
                        String content = fileContentMap.get(fileName);
                        content += " " + token;
                        fileContentMap.put(fileName, content);
                    } else {
                        fileContentMap.put(fileName, token);
                    }
                }
            }
            in.close();
            Set keySet = fileContentMap.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String file = keys.next();
                String content = fileContentMap.get(file);
                String filePath = incident + "---" + file + ".txt";
                OutputStream fos = new FileOutputStream(filePath);
                fos.write(content.getBytes());
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}