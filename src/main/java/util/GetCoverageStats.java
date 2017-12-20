package util;

import conll.CoNLLdata;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class GetCoverageStats {

    static public void main (String[] args) {
        String keyFile = "/Users/piek/Desktop/SemEval2018/trial_data_final/dev_data/s3/docs.conll";
        String responseFile = "/Users/piek/Desktop/SemEval2018/trial_data_final/input/s3/docs.conll.result";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--key") && args.length>(i+1)) {
                keyFile = args[i+1];
            }
            else if (arg.equals("--response") && args.length>(i+1)) {
                responseFile = args[i+1];
            }
        }
        ArrayList<CoNLLdata> keyAnnotations = readCoNLLFile(new File(keyFile));
        ArrayList<CoNLLdata> systemAnnotations = readCoNLLFile(new File(responseFile));
        compareAnnotations(keyAnnotations, systemAnnotations);
    }

    static void compareAnnotations (ArrayList<CoNLLdata> key, ArrayList<CoNLLdata> system) {
        HashMap<String, Integer> missedMap = new HashMap<>();
        HashMap<String, Integer> inventedMap = new HashMap<>();
        HashMap<String, Integer> matchedMap = new HashMap<>();
        for (int i = 0; i < key.size(); i++) {
            CoNLLdata coNLLdataKey = key.get(i);
            boolean match = false;
            for (int j = 0; j < system.size(); j++) {
                CoNLLdata coNLLdataSystem = system.get(j);
                String idKey = coNLLdataKey.getFileName()+coNLLdataKey.getTokenId();
                String idSystem = coNLLdataSystem.getFileName()+coNLLdataSystem.getTokenId();
                if (idKey.equals(idSystem)) {
                    /// we have a match
                    match = true;
                    if (matchedMap.containsKey(coNLLdataKey.getWord())) {
                        Integer cnt = matchedMap.get(coNLLdataKey.getWord());
                        cnt++;
                        matchedMap.put(coNLLdataKey.getWord(), cnt);
                    }
                    else {
                        matchedMap.put(coNLLdataKey.getWord(), 1);
                    }
                    break;
                }
            }
            if (!match) {
                if (missedMap.containsKey(coNLLdataKey.getWord())) {
                    Integer cnt = missedMap.get(coNLLdataKey.getWord());
                    cnt++;
                    missedMap.put(coNLLdataKey.getWord(), cnt);
                }
                else {
                    missedMap.put(coNLLdataKey.getWord(), 1);
                }
            }
        }
        for (int i = 0; i < system.size(); i++) {
            CoNLLdata coNLLdataSystem = system.get(i);
            boolean match = false;
            for (int j = 0; j < key.size(); j++) {
                CoNLLdata coNLLdataKey = system.get(j);
                String idKey = coNLLdataKey.getFileName()+coNLLdataKey.getTokenId();
                String idSystem = coNLLdataSystem.getFileName()+coNLLdataSystem.getTokenId();
                if (idKey.equals(idSystem)) {                    /// we have a match
                    match = true;
                    break;

                }
            }
            if (!match) {
                if (inventedMap.containsKey(coNLLdataSystem.getWord())) {
                    Integer cnt = inventedMap.get(coNLLdataSystem.getWord());
                    cnt++;
                    inventedMap.put(coNLLdataSystem.getWord(), cnt);
                }
                else {
                    inventedMap.put(coNLLdataSystem.getWord(), 1);
                }
            }
        }
        System.out.println("Matched = " + matchedMap.size());
        System.out.println("Missed = " + missedMap.size());
        System.out.println("Invented = " + inventedMap.size());
        System.out.println("MATCHED");

        Set keySet = matchedMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String w = keys.next();
            Integer cnt = matchedMap.get(w);
            System.out.println(w+"\t" + cnt);
        }
        System.out.println("MISSED:");
        keySet = missedMap.keySet();
        keys = keySet.iterator();
        while (keys.hasNext()) {
            String w = keys.next();
            Integer cnt = missedMap.get(w);
            System.out.println(w+"\t" + cnt);
        }
        System.out.println("INVENTED:");
        keySet = inventedMap.keySet();
        keys = keySet.iterator();
        while (keys.hasNext()) {
            String w = keys.next();
            Integer cnt = inventedMap.get(w);
            System.out.println(w+"\t" + cnt);
        }
    }

    static ArrayList<CoNLLdata> readCoNLLFile(File coNLLfile) {
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

        ArrayList<CoNLLdata> annotations = new ArrayList<>();
        try {
            OutputStream fos = new FileOutputStream(coNLLfile.getAbsolutePath()+".anno");
            FileInputStream fis = new FileInputStream(coNLLfile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine = "";
            String fileName = "";
            while (in.ready() && (inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("#begin document")) {
                    //#begin document (a212420b8d7c079bd385ff4dba9fea86);
                }
                else if (inputLine.startsWith("#end document")) {
                }
                else {
                    String[] fields = inputLine.split("\t");
                    if (fields.length==4) {
                        if (!fields[3].equals("-")) {
                            CoNLLdata coNLLdata = new CoNLLdata(inputLine);
                            annotations.add(coNLLdata);
                            inputLine+= "\n";
                            fos.write(inputLine.getBytes());
                        }
                    }
                    else {
                        System.out.println("inputLine = " + inputLine);
                    }
                }

            }
            in.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return annotations;
    }


}
