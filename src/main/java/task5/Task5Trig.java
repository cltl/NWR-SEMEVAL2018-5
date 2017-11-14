package task5;

import com.hp.hpl.jena.rdf.model.Statement;
import match.EventIdentity;
import match.TrigReader;
import objects.EventTypes;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import question.Questiondata;
import util.Util;
import vu.cltl.triple.TrigTripleData;
import vu.cltl.triple.TrigUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 10/11/2017.
 */
public class Task5Trig {

    static ArrayList<String> allEventKeys = new ArrayList<>();

    static public void main(String[] args) {
        Integer tripleMatchThreshold = 5;
        String pathToQuestionFile = "/Users/piek/Desktop/SemEval2018/trial_data/input/s3/questions.json";
        String pathToTrigFiles = "/Users/piek/Desktop/SemEval2018/trial_data/nwr/data";
        String pathToConllFiles = "/Users/piek/Desktop/SemEval2018/trial_data/input/s3/CONLL";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--question") && args.length>(i+1)) {
                pathToQuestionFile = args[i+1];
            }
            else if (arg.equals("--triple-threshold") && args.length>(i+1)) {
                tripleMatchThreshold = Integer.parseInt(args[i+1]);
            }
            else if (arg.equals("--trig-files") && args.length>(i+1)) {
                pathToTrigFiles = args[i+1];
            }
            else if (arg.equals("--conll-files") && args.length>(i+1)) {
                pathToConllFiles = args[i+1];
            }
        }

        ArrayList<File> conllFiles = Util.makeRecursiveFileList(new File(pathToConllFiles), ".conll");

        /// process all trig files and build the knowledge graphs
        ArrayList<File> trigFiles = Util.makeRecursiveFileList(new File(pathToTrigFiles), ".trig");
        vu.cltl.triple.TrigTripleData trigTripleData = TrigReader.readTripleFromTrigFiles(trigFiles);
        ArrayList<String> domainEvents = EventTypes.getEventSubjectUris(trigTripleData.tripleMapInstances);
        HashMap<String, ArrayList<Statement>> eckgMap = TrigUtil.getPrimaryKnowledgeGraphHashMap(domainEvents,trigTripleData);
        HashMap<String, ArrayList<Statement>> seckgMap = TrigUtil.getSecondaryKnowledgeGraphHashMap(domainEvents,trigTripleData);
        System.out.println("eckgMap = " + eckgMap.size());
        System.out.println("domainEvents = " + domainEvents.size());
        eckgMap = EventIdentity.lookForSimilarEvents(domainEvents, eckgMap, seckgMap,tripleMatchThreshold);
        System.out.println("eckgMap after merge = " + eckgMap.size());
        try {
            OutputStream fos = new FileOutputStream(pathToQuestionFile+".eckg");
          //  TrigUtil.printKnowledgeGraph(fos, eckgMap, seckgMap);
            TrigUtil.printKnowledgeGraph(fos, eckgMap);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //// process the question json
        //// match each question constraint with the KG and create json answer and the conll output files for event coreference
        try {
            HashMap<String, String> tokenEventIdMap = new HashMap<>();
            JSONObject jsonObject = Util.readJsonFile(pathToQuestionFile);
            JSONObject answerObjectArray = new JSONObject();
            /// iterate over json array with the questions
            for (Object key : jsonObject.keySet()) {
                String questionId = (String) key;
               // System.out.println("questionId = " + questionId);
                Object question = jsonObject.get(questionId);
                Questiondata questiondata = new Questiondata(questionId, (JSONObject) question);
                ArrayList<String> eventKeys = new ArrayList<>(); // for adding events that matter
                //// we use keySet from the eckgMap because events could have been merged.
                Set keySet = eckgMap.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String eventKey = keys.next();
                    ArrayList<Statement> secondaryStatements = seckgMap.get(eventKey);
                    if (matchTimeConstraint(secondaryStatements, questiondata)) {
                        if (!eventKeys.contains(eventKey)) eventKeys.add(eventKey);
                    }
                }

                //// add event tokenids to augment the CoNLL files with mentions
                getTokenEventMap(tokenEventIdMap, eventKeys, eckgMap);


                JSONObject answerObject = new JSONObject();
                answerObject.put("numerical_anwer", eventKeys.size());
                JSONObject docObject = new JSONObject();
                for (int i = 0; i < eventKeys.size(); i++) {
                    String eventKey = eventKeys.get(i);
                    Integer intId = Util.getEventId(eventKey, allEventKeys);
                    ArrayList<Statement> directStatements = eckgMap.get(eventKey);
                    ArrayList<String> fileNames = getFilesFromStatements(directStatements);
                    docObject.put(intId.toString(), fileNames);
                }
                answerObject.put ("answer_docs",docObject);
                answerObjectArray.put(questionId, answerObject);

                /**
                 *
                 *
                 "3-59876" : {
                 "answer_docs" : {
                 "3" : [ "4f7fc8d1692d6bb2f5e450a23e90a034" ],
                 "4" : [ "4f7fc8d1692d6bb2f5e450a23e90a034" ],
                 "5" : [ "4f7fc8d1692d6bb2f5e450a23e90a034" ]
                 },
                 "numerical_anwer" : 3
                 },
                 *

                 "3-58117": {
                 "answer_docs": {
                 "750833": [
                 "f2b694085da3d4c7a47b9daf84203fc2"
                 ]
                 },
                 "numerical_answer": 2,
                 "part_info": {
                 "750833": {
                 "num_injured": 0,
                 "num_killed": 2
                 }
                 }
                 },
                 */
            }
            try (FileWriter file = new FileWriter(pathToQuestionFile+".answer.json")) {
                ObjectMapper mapper = new ObjectMapper();
                file.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(answerObjectArray));
                file.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }


            System.out.println("tokenEventIdMap = " + tokenEventIdMap.size());
            File conllFolder = new File (pathToConllFiles);
            File conllParentFile = conllFolder.getParentFile();
            File conllResultFolder = new File(conllParentFile.getAbsolutePath()+"/"+conllFolder.getName()+"RESULT");
            if (!conllResultFolder.exists()) conllResultFolder.mkdir();
            /// process the CONLL files
            for (int i = 0; i < conllFiles.size(); i++) {
                File conllFile =conllFiles.get(i);
                System.out.println("conllFile = " + conllFile);
              //  ConllAnswerFromSem.resultForCoNLLFile(conllResultFolder, conllFile, allEventKeys, tokenEventIdMap);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    static  void getTokenEventMap (HashMap<String, String> tokenEventMap, ArrayList<String> eventKeys, HashMap<String, ArrayList<Statement>> kGraph) {

        for (int i = 0; i < eventKeys.size(); i++) {
            String eventKey = eventKeys.get(i);
            if (!allEventKeys.contains(eventKey)) allEventKeys.add(eventKey);
            if (kGraph.containsKey(eventKey)) {
                ArrayList<Statement> directStatements = kGraph.get(eventKey);
                for (int j = 0; j < directStatements.size(); j++) {
                    Statement statement = directStatements.get(j);
                    if (statement.getPredicate().getLocalName().equals("denotedBy")) {
                        String mention = statement.getObject().toString();
                        String fileName = getFileNameFromMention(mention);
                        ArrayList<String> tokenList = getTokenIdsFromMention(mention);
                        for (int t = 0; t < tokenList.size(); t++) {
                            String tokenId = fileName+tokenList.get(t).substring(1);
                            tokenEventMap.put(tokenId, eventKey);
                        }
                    }
                }
            }
        }
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
