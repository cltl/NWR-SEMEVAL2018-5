package util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by piek on 09/11/2017.
 */
public class Util {


    static public Integer getEventId (String eventId, ArrayList<String> allEventKeys) {
        Integer intId = new Integer(allEventKeys.indexOf(eventId));
        intId++;   //// add one so we do not get a zero ID
        return intId;
    }

    static public String getNumericId (String id) {
        //1_10ecbplus.xml.naf.fix.xml#ev27
        final String number="12334567890";
        String numString = "";
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            if (number.indexOf(c)>-1) {
                numString+= c;
            }
        }
        return numString;
    }


    public static String makeSparqlQueryInit () {
        String sparqQueryInit = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/> \n" +
                "PREFIX eso: <http://www.newsreader-project.eu/domain-ontology#> \n" +
                "PREFIX fn: <http://www.newsreader-project.eu/ontologies/framenet/> \n" +
                "PREFIX ili: <http://globalwordnet.org/ili/> \n" +
                "PREFIX prov:  <http://www.w3.org/ns/prov#>\n" +
                "PREFIX nwrauthor: <http://www.newsreader-project.eu/provenance/author/> \n" +
                "PREFIX nwrcite: <http://www.newsreader-project.eu/data/non-entities/> \n" +
                "PREFIX grasp: <http://groundedannotationframework.org/grasp#>\n" +
                "PREFIX gaf:   <http://groundedannotationframework.org/gaf#>\n" +
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "PREFIX dbp: <http://dbpedia.org/ontology/> \n" +
                "PREFIX dbpedia: <http://dbpedia.org/resource/> \n" +
                "PREFIX dbpedianl: <http://nl.dbpedia.org/resource/> \n" +
                "PREFIX owltime: <http://www.w3.org/TR/owl-time#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT ?event ?relation ?object ?indatetime ?begintime ?endtime  \n" +
                //"SELECT ?event ?relation ?object \n" +
                "WHERE {\n" +
                "{SELECT distinct ?event WHERE { \n" +
                "?event rdf:type " + "sem:Event" + " .\n";
        return sparqQueryInit;
    }

    public static String makeSparqlQueryEndWithLimit (int limit) {
        String sparqQueryEnd =
                "\n} LIMIT "+limit+" }\n" +
                        "?event ?relation ?object . \n" +
                        "OPTIONAL { ?object rdf:type owltime:Instant ; owltime:inDateTime ?indatetime } \n" +
                        "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasBeginning ?begintime } \n" +
                        "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasEnd ?endtime } \n" +
                        "} ORDER BY ?event" ;
        return sparqQueryEnd;
    }
    public static String makeSparqlQueryEnd () {
        String sparqQueryEnd =
                "\n} }\n" +
                        "?event ?relation ?object . \n" +
                        "OPTIONAL { ?object rdf:type owltime:Instant ; owltime:inDateTime ?indatetime } \n" +
                        "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasBeginning ?begintime } \n" +
                        "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasEnd ?endtime } \n" +
                        "} ORDER BY ?event" ;
        return sparqQueryEnd;
    }


    static public JSONObject readJsonFile (String filePath) {
        JSONObject jsonObject  = null;
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(filePath));
            jsonObject = (JSONObject) obj;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    static public boolean isEventKey (String eventKey) {
        int idx_s = eventKey.indexOf("#");
        int idx_e = eventKey.lastIndexOf("#");
        if (idx_s == idx_e && idx_s>-1) {
            return true;
        }
        else if (eventKey.indexOf("/non-entities/")>-1) {
            return true;
        }
        else {
            return false;
        }
    }

    static public ArrayList<String> ReadFileToStringArrayList(String fileName) {
        ArrayList<String> list = new ArrayList<String>();
        if (new File (fileName).exists() ) {
            try {
                FileInputStream fis = new FileInputStream(fileName);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    //System.out.println(inputLine);
                    if (inputLine.trim().length()>0) {
                        list.add(inputLine);
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
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
