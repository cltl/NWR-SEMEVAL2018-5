package util;

import com.hp.hpl.jena.rdf.model.Statement;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.*;

/**
 * Created by piek on 09/11/2017.
 */
public class Util {


    static public ArrayList<Statement> filterBySentences (ArrayList<Statement> statements, int sentences) {
        ArrayList<Statement> filteredStatements = new ArrayList<>();
        ArrayList<String> firstSentencesSubjects = new ArrayList<>();
        for (int i = 0; i < statements.size(); i++) {
           // gaf:denotedBy    <http://www.newsreader-project.eu/data/semeval2018-5/0a3e49ad0467c6545e36d754cc08d312#char=78,86&word=w17&term=t17&sentence=1&paragraph=1> ;
           Statement statement = statements.get(i);
            for (int j = 0; j <= sentences; j++) {
                if (statement.getPredicate().getLocalName().equals("denotedBy")) {
                    String filter = "&sentence="+j+"&";
                    if (statement.getObject().toString().indexOf(filter)>-1 ) {
                       if (!firstSentencesSubjects.contains(statement.getSubject().getURI())) {
                           firstSentencesSubjects.add(statement.getSubject().getURI());
                       }
                    }
               }
            }
        }
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (firstSentencesSubjects.contains(statement.getSubject().getURI())) {
                continue;
            }
            else {
                filteredStatements.add(statement);
            }
        }
        return filteredStatements;
    }

    static public boolean hasFile (ArrayList<File> files, File file) {
        for (int i = 0; i < files.size(); i++) {
            File file1 = files.get(i);
            if (file1.getName().equals(file.getName())) {
                 return true;
             }
        }
        return false;
    }

        static public String getPrefLabel (String uri, HashMap<String, ArrayList<Statement>> map) {
            String prefLabel = "";
            ArrayList<Statement> statements = map.get(uri);
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                if (statement.getPredicate().getLocalName().equals("preLabel")) {
                   prefLabel = statement.getObject().asLiteral().toString().replace(" ", "_");
                }
            }
            if (prefLabel.isEmpty()) {
                int idx = uri.lastIndexOf("/");
                if (idx>-1) {
                    prefLabel = uri.substring(idx+1);
                }
                else prefLabel = uri;
            }
            return prefLabel;
        }

        static public ArrayList<String> getSortedObjectPrefLabels (ArrayList<Statement> statements, HashMap<String, ArrayList<Statement>> map) {
            ArrayList<String> prefLabels = new ArrayList<>();
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                String prefLabel = getPrefLabel(statement.getObject().toString(), map);
                if (!prefLabels.contains(prefLabel)) {
                    prefLabels.add(prefLabel);
                }
            }
            Collections.sort(prefLabels);
            return prefLabels;
        }
        static public String getObjectPrefLabelString (ArrayList<Statement> statements, HashMap<String, ArrayList<Statement>> map) {
            String prefs = "";
            ArrayList<String> prefLabels = getSortedObjectPrefLabels(statements, map);
            for (int i = 0; i < prefLabels.size(); i++) {
                String label = prefLabels.get(i);
                if (i>0) prefs+="+";
                prefs += label;
            }
            return prefs;
        }

    //<http://..../02e278ddb2d52a796d111d5a1258b0ee#char=20,25&word=w3&term=t3&sentence=1&paragraph=1>
        // gaf:denotedBy
        //     <http://www.newsreader-project.eu/data/semeval2018-5/4f78f01eadd1fc9e9d4795f1888e18fb#
        // char=297,308&word=w2003004,w2003005,w2003006&
        // term=t59,t60,t61&sentence=3&paragraph=2> ;

        static public ArrayList<String> getTokenIdsFromMention (String mention) {
            ArrayList<String> ids = new ArrayList<>();
            String [] fields = mention.split("&");
            if (fields.length>1) {
                String wordIdString = fields[1].substring(5);
                String[] subfields = wordIdString.split(",");
                for (int i = 0; i < subfields.length; i++) {
                    String subfield = subfields[i];
                    ids.add(subfield);
                }
            }
            return ids;
        }

        static public ArrayList<String> getFilesFromStatements (ArrayList<Statement> directStatements) {
             ArrayList<String> fileNames = new ArrayList<>();
             for (int j = 0; j < directStatements.size(); j++) {
                 Statement statement = directStatements.get(j);
                 if (statement.getPredicate().getLocalName().equals("denotedBy")) {
                     String mention = statement.getObject().toString();
                     String fileName = getFileNameFromMention(mention);
                     if (!fileNames.contains(fileName)) fileNames.add(fileName);

                 }
             }
             return fileNames;
        }

        static public String getFileNameFromMention (String mention) {
            String fileName = "";
            int idx_s = mention.lastIndexOf("/");
            int idx_e = mention.lastIndexOf("#");
            fileName = mention.substring(idx_s+1, idx_e);
            return fileName;
        }

        public static  HashMap<String, String>  getTokenEventMap(HashMap<String, ArrayList<Statement>> kGraph, ArrayList<String> allEventKeys) {
            HashMap<String, String> tokenEventMap = new HashMap<>();
            Set keySet = kGraph.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String incidentKey = keys.next();
               // System.out.println("eventKey = " + eventKey);
                ArrayList<Statement> directStatements = kGraph.get(incidentKey);
                for (int j = 0; j < directStatements.size(); j++) {
                    Statement statement = directStatements.get(j);
                    String eventId = statement.getSubject().getURI();
                    if (!allEventKeys.contains(eventId)) allEventKeys.add(eventId);
                    if (statement.getPredicate().getLocalName().equals("denotedBy")) {
                        String mention = statement.getObject().toString();
                        //System.out.println("mention = " + mention);
                        String fileName = getFileNameFromMention(mention);
                        ArrayList<String> tokenList = getTokenIdsFromMention(mention);
                        for (int t = 0; t < tokenList.size(); t++) {
                            String tokenId = fileName+":"+tokenList.get(t);
                           // System.out.println("tokenId = " + tokenId);
                            tokenEventMap.put(tokenId, eventId);
                        }
                    }
                }
            }
            System.out.println("tokenEventMap.size() = " + tokenEventMap.size());
            return tokenEventMap;
        }

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

    static public HashMap<String, String> ReadFileToStringHashMap(String fileName) {
        HashMap<String, String> map = new HashMap<>();
        if (new File (fileName).exists() ) {
            try {
                FileInputStream fis = new FileInputStream(fileName);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    //System.out.println(inputLine);
                    if (inputLine.trim().length()>0) {
                        String[] fields = inputLine.trim().split("\t");
                        if (fields.length==2) {
                            String word = fields[0];
                            String type = fields[1];
                            map.put(word, type);
                        }
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return map;
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
