package conll;

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
        String inputFile = "";
        inputFile = "/Users/piek/Desktop/SemEval2018/trial_data_final/s3/docs.conll";
        inputfolder = "";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--folder") && args.length>(i+1)) {
                inputfolder = args[i+1];
            }
            else if (arg.equals("--file") && args.length>(i+1)) {
                inputFile = args[i+1];
            }
        }
        if (inputFile.isEmpty() && !inputfolder.isEmpty()) {
            ArrayList<File> files = util.Util.makeRecursiveFileList(new File(inputfolder), ".conll");
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                //System.out.println("file.getName() = " + file.getName());
                readConll(new File(inputfolder), file);
            }
        }
        else if (!inputFile.isEmpty()) {
            File conllFile = new File(inputFile);
            File parentFolder = conllFile.getParentFile();
            readCoNLLFile(parentFolder, conllFile);
        }
    }


    static void readConll(File parentFolder, File conllfile) {
        readCoNLLFile(parentFolder, conllfile);
    }



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

            /// we use a tokencounter to create token identifiers.
            /// this is delicate as these tokens need to match across the NAF files and the CoNLL file
            /// make sure we use the same counting when we convert conll to naf in ConllOutputFromSem
            /// We skip DCT and NEWLINE and only count the real tokens
            /// For every new file, we reset the counter to zero

            int tokenCount = 0;
            ArrayList<String> paragraphSentenceIdList = new ArrayList<>();
            KafSaxParser kafSaxParser = new KafSaxParser();
            while (in.ready() && (inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("#begin document")) {
                    //#begin document (a212420b8d7c079bd385ff4dba9fea86);
                    kafSaxParser.init();
                    rawText = "";
                    fileName = inputLine.substring(inputLine.indexOf("(")+1, inputLine.lastIndexOf(")"));
                    kafSaxParser.getKafMetaData().setUrl(fileName);
                    kafSaxParser.getKafMetaData().setLanguage("en");
                    tokenCount = 0;
                    paragraphSentenceIdList = new ArrayList<>();
                }
                else if (inputLine.startsWith("#end document")) {
                        kafSaxParser.rawText = rawText.trim();
                        String outputPath = parentFolder.getAbsolutePath() + "/" + fileName + ".naf";
                        OutputStream fos = new FileOutputStream(outputPath);
                        kafSaxParser.writeNafToStream(fos);
                        fos.close();
                }
                else {
                    String[] fields = inputLine.split("\t");
                    if (fields.length==4) {
                        CoNLLdata coNLLdata = new CoNLLdata(inputLine);
                        if (coNLLdata.getDunit().equals("DCT")) {
                            /**
                             * http://www.w3.org/TR/xmlschema-2/#isoformats.
                             * In summary, the date is specified follow- ing the form “YYYY-MM-DDThh:mm:ss” (all fields required).
                             * To specify a time zone, you can either enter a dateTime in UTC time by adding a ”Z” behind the time (“2002-05-30T09:00:00Z”)
                             * or you can specify an offset from the UTC time by adding a positive or negative time behind the time (“2002-05- 30T09:00:00+06:00”).
                             * <fileDesc creationtime="2017-01-13"/>
                             * <fileDesc creationtime="2017-01-13T00:00:00"/>
                             */
                            kafSaxParser.getKafMetaData().setCreationtime(coNLLdata.getWord());
                        }
                        else if (coNLLdata.getWord().equals("NEWLINE")) {
                            rawText+="\n";
                        }
                        else {
                            if (!coNLLdata.getWord().isEmpty()) {
                                tokenCount++;
                                //// Sentence ids are not unique in Conll but need to be unqiue and sequetial in NAF
                                //// We create a list with unique ids by combining with the Dunit (TITLE, BODY)
                                String paragraphSentenceId = coNLLdata.getDunit() + "." + coNLLdata.getSentence();
                                if (!paragraphSentenceIdList.contains(paragraphSentenceId))
                                    paragraphSentenceIdList.add(paragraphSentenceId);
                                KafWordForm kafWordForm = coNLLdata.toKafWordForm(tokenCount, paragraphSentenceIdList.size());
                                //KafWordForm kafWordForm = coNLLdata.toKafWordForm();
                                kafWordForm.setCharOffset(Integer.toString(rawText.length()));
                                kafWordForm.setCharLength(Integer.toString(coNLLdata.getWord().length()));
                                rawText += " " + coNLLdata.getWord();
                                kafSaxParser.kafWordFormList.add(kafWordForm);
                            }
                            else {
                                System.out.println("EMPTY WORD inputLine = " + inputLine);
                            }
                        }
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


}