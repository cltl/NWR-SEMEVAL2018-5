package answer;

import question.CoNLLdata;
import util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by piek on 11/11/2017.
 */
public class ConllAnswerFromSem {

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


    static public void main(String[] args) {
        String inputfolder = "";
        inputfolder = "/Code/vu/newsreader/NWR-SEMEVAL2018-5/examples/s3/CONLL/";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--folder") && args.length>(i+1)) {
                inputfolder = args[i+1];
            }
        }
        ArrayList<File> files = util.Util.makeRecursiveFileList(new File(inputfolder), ".conll");
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            System.out.println("file.getName() = " + file.getName());
        }
    }

    static public void resultForCoNLLFile(File coNLLfile, HashMap<String,String> tokenEventMap) {
        try {
            FileInputStream fis = new FileInputStream(coNLLfile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            OutputStream fos = new FileOutputStream(coNLLfile+".result");
            String inputLine = "";
            String str = "";
            while (in.ready() && (inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("#begin document")) {
                    //#begin document (a212420b8d7c079bd385ff4dba9fea86);
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
                            String tag = "-";
                            String tokenId = coNLLdata.getUniqueTokenString();
                            if (tokenEventMap.containsKey(tokenId)) {
                                String eventId = tokenEventMap.get(tokenId);
                                tag = Util.getNumericId(eventId);
                            }
                            str += coNLLdata.toConll(tag);
                        }
                    }
                    else {}
                }
            }
            fos.write(str.getBytes());
            fos.flush();
            fos.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
