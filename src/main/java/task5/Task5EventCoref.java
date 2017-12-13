package task5;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.tdb.TDBFactory;
import conll.ConllOutputFromSem;
import match.EventIdentity;
import match.MatchSettings;
import match.TemporalReasoning;
import objects.EventTypes;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.json.simple.JSONObject;
import question.Questiondata;
import util.Util;
import vu.cltl.triple.TrigUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 10/11/2017.
 * New version that assumes there is no specific set of documents associated with each query in the json file.
 * This means that all documents associated with the CoNLL file needs to be considered for event detection and cross-document event coreference.
 * We therefore separated the event coreference from answering the questions. The event coreference considers all the documents and all events within the documents
 * This function only carries out cross-document from the NWR output and create the event-centric knowledge graphs.
 * It stores the ECKG to disk and annotates the CoNLL file with event mentions and coreference relations.
 * We are now dealing with one big CoNLL file instead of many separate files.
 */
public class Task5EventCoref {

    static ArrayList<String> allEventKeys = new ArrayList<>();

    static public void main(String[] args) {
        String pathToTrigFiles = "/Users/piek/Desktop/SemEval2018/trial_data_final/NAFOUT";
        String pathToConllFile = "/Users/piek/Desktop/SemEval2018/trial_data_final/s3/docs.conll";
        MatchSettings matchSettings = new MatchSettings();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--trig-files") && args.length>(i+1)) {
                pathToTrigFiles = args[i+1];
            }
            else if (arg.equals("--conll-file") && args.length>(i+1)) {
                pathToConllFile = args[i+1];
            }
        }
        matchSettings.parseArguments(args);
        matchSettings.setLoose();
       // matchSettings.setMatchAny(true);
        System.out.println(matchSettings.getSettings());
        System.out.println("pathToConllFile = " + pathToConllFile);
        System.out.println("pathToTrigFiles = " + pathToTrigFiles);
        File conllFileFolder = new File (pathToConllFile).getParentFile();
        File eckgFolder = new File (conllFileFolder.getAbsolutePath()+"/"+"eckg");
        if (!eckgFolder.exists()) eckgFolder.mkdir();
        File trigFolder = new File (pathToTrigFiles);
        /// STEP 1
        /// process all trig files and build the knowledge graphs
        ArrayList<File> trigFiles = Util.makeRecursiveFileList(trigFolder, ".trig");
        System.out.println("trigFiles.size() = " + trigFiles.size());
       // vu.cltl.triple.TrigTripleData trigTripleData = TrigReader.readTripleFromTrigFiles(trigFiles);
        vu.cltl.triple.TrigTripleData trigTripleData = vu.cltl.triple.TrigTripleReader.readTripleFromTrigFiles(trigFiles);

        /// STEP 2
        /// from the complete graph we extract all events that match the domain constraints
        ArrayList<String> domainEvents = EventTypes.getEventSubjectUris(trigTripleData.tripleMapInstances);
        HashMap<String, ArrayList<Statement>> eckgMap = TrigUtil.getPrimaryKnowledgeGraphHashMap(domainEvents,trigTripleData);
        HashMap<String, ArrayList<Statement>> seckgMap = TrigUtil.getSecondaryKnowledgeGraphHashMap(domainEvents,trigTripleData);
        System.out.println("eckgMap = " + eckgMap.size());
        System.out.println("seckgMap = " + seckgMap.size());
        System.out.println("domainEvents = " + domainEvents.size());


        /// STEP 3 DEFINE TEMPORAL AND SPATIAL CONTAINERS

        HashMap<String, ArrayList<String>> temporalContainers = TemporalReasoning.getTemporalContainers(eckgMap, seckgMap, matchSettings);


        /// STEP 4 CROSSDOC EVENT COREF WITHIN CONTAINERS
          /// This will carry out cross-document event coreference for all events that belong to the same domain
        System.out.println("temporalContainers.size() = " + temporalContainers.size());
        Set containerSet = temporalContainers.keySet();
        Iterator<String> containerKeys = containerSet.iterator();
        HashMap<String, ArrayList<Statement>> finalEvents = new HashMap<>();
        while (containerKeys.hasNext()) {
            String containerKey = containerKeys.next();
            System.out.print("containerKey = " + containerKey+":");
            ArrayList<String> eventIds = temporalContainers.get(containerKey);
            HashMap<String, ArrayList<Statement>> containerEvents = EventIdentity.lookForSimilarEvents(
                                eventIds,
                                eckgMap,
                                seckgMap,
                                matchSettings);
            if (eventIds.size()==containerEvents.size()) {
                //System.out.println("NO MERGE");
                System.out.println();
            }
            else {
                System.out.println("MERGED = " + (eventIds.size()-containerEvents.size()));
            }
            finalEvents.putAll(containerEvents);
                       //// Dump the ECKGs
           try {
/*               OutputStream fos1 = new FileOutputStream(eckgFolder.getAbsoluteFile()+"/"+containerKey+".csv");
               OutputStream fos2 = new FileOutputStream(eckgFolder.getAbsoluteFile()+"/"+containerKey+".eckg");
               TrigUtil.printCountedKnowledgeGraph(fos2, containerEvents);
               TrigUtil.printCountedKnowledgeGraphCsv(fos1, containerEvents);
               fos1.close();
               fos2.close();*/

               OutputStream fos3 = new FileOutputStream(eckgFolder.getAbsoluteFile()+"/"+containerKey+".trig");
               Dataset dataset = TDBFactory.createDataset();
               TrigUtil.prefixDefaultModels(dataset);
               ArrayList<String> containerEventKeys = new ArrayList<>(containerEvents.keySet());
               HashMap<String, ArrayList<Statement>> seckgContainerEvent = TrigUtil.getSecondaryKnowledgeGraphHashMap( containerEventKeys,trigTripleData);
               TrigUtil.addStatementsToJenaData(dataset, containerEvents);
               TrigUtil.addStatementsToJenaData(dataset, seckgContainerEvent);
               RDFDataMgr.write(fos3, dataset.getDefaultModel(), RDFFormat.TRIG_PRETTY);
               fos3.close();

           } catch (IOException e) {
               e.printStackTrace();
           }
        }
        /// STEP 5 CREATE THE CONLL FILE
        createSystemConllFile(finalEvents, pathToConllFile);
    }


     static void createSystemConllFile (
                        HashMap<String, ArrayList<Statement>> eckgMap,
                        String pathToConllFile
                        ) {
           HashMap<String, String> tokenEventIdMap =  getTokenEventMap(eckgMap);
           File conllFile = new File (pathToConllFile);
           File conllResultFolder = conllFile.getParentFile();
           if (!conllResultFolder.exists()) conllResultFolder.mkdir();
           ConllOutputFromSem.resultForCoNLLFile(conllFile, allEventKeys, tokenEventIdMap);
     }

    //<http://..../02e278ddb2d52a796d111d5a1258b0ee#char=20,25&word=w3&term=t3&sentence=1&paragraph=1>
    // gaf:denotedBy
    //     <http://www.newsreader-project.eu/data/semeval2018-5/4f78f01eadd1fc9e9d4795f1888e18fb#
    // char=297,308&word=w2003004,w2003005,w2003006&
    // term=t59,t60,t61&sentence=3&paragraph=2> ;

    static ArrayList<String> getTokenIdsFromMention (String mention) {
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

     static ArrayList<String> getFilesFromStatements (ArrayList<Statement> directStatements) {
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

    static String getFileNameFromMention (String mention) {
        String fileName = "";
        int idx_s = mention.lastIndexOf("/");
        int idx_e = mention.lastIndexOf("#");
        fileName = mention.substring(idx_s+1, idx_e);
        return fileName;
    }

    static  HashMap<String, String>  getTokenEventMap ( HashMap<String, ArrayList<Statement>> kGraph) {
        HashMap<String, String> tokenEventMap = new HashMap<>();
        Set keySet = kGraph.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String eventKey = keys.next();
           // System.out.println("eventKey = " + eventKey);
            if (!allEventKeys.contains(eventKey)) allEventKeys.add(eventKey);
            ArrayList<Statement> directStatements = kGraph.get(eventKey);
            for (int j = 0; j < directStatements.size(); j++) {
                Statement statement = directStatements.get(j);
                if (statement.getPredicate().getLocalName().equals("denotedBy")) {
                    String mention = statement.getObject().toString();
                    //System.out.println("mention = " + mention);
                    String fileName = getFileNameFromMention(mention);
                    ArrayList<String> tokenList = getTokenIdsFromMention(mention);
                    for (int t = 0; t < tokenList.size(); t++) {
                        String tokenId = fileName+":"+tokenList.get(t);
                       // System.out.println("tokenId = " + tokenId);
                        tokenEventMap.put(tokenId, eventKey);
                    }
                }
            }
        }
        System.out.println("tokenEventMap.size() = " + tokenEventMap.size());
        return tokenEventMap;
    }

    static boolean matchTimeConstraint (ArrayList<Statement> statements, Questiondata questiondata) {
            //nwr:tmx1	inDateTime	20170131
            //nwr:tmx1	inDateTime	201701
            //nwr:tmx1	inDateTime	2017
            //"day": "14/01/2017"
            //"month": "01/2017"
            //"year": "2017"
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getPredicate().getLocalName().equals("inDateTime")) {
                //System.out.println("TrigUtil.getPrettyNSValue(statement.getObject().toString() = " + TrigUtil.getPrettyNSValue(statement.getObject().toString()));
                if (TrigUtil.getPrettyNSValue(statement.getObject().toString()).equals(questiondata.getYear())) {
                    return true;
                } else if (TrigUtil.getPrettyNSValue(statement.getObject().toString()).equals(questiondata.getNormalisedDay())) {
                    return true;
                } else if (TrigUtil.getPrettyNSValue(statement.getObject().toString()).equals(questiondata.getNormalisedDay())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @param jsonObj
     */
    public static void printJsonObject(JSONObject jsonObj) {
        for (Object key : jsonObj.keySet()) {
            //based on you key types
            String keyStr = (String)key;
            Object keyvalue = jsonObj.get(keyStr);

            //Print key and value
            System.out.println("key: "+ keyStr + " value: " + keyvalue);

            //for nested objects iteration if required
            if (keyvalue instanceof JSONObject)
                printJsonObject((JSONObject)keyvalue);
        }
    }


}
