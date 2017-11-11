package task5;

import answer.ConllAnswerFromSem;
import com.hp.hpl.jena.rdf.model.Statement;
import objects.EventTypes;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import question.Questiondata;
import util.Util;
import vu.cltl.storyteller.objects.TrigTripleData;
import vu.cltl.storyteller.trig.TrigTripleReader;
import vu.cltl.storyteller.trig.TrigUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 10/11/2017.
 */
public class Task5Trig {



    static public void main(String[] args) {
        String pathToQuestionFile = "/Users/piek/Desktop/SemEval2018/trial_data/input/s3/questions.json";
        String pathToTrigFiles = "/Users/piek/Desktop/SemEval2018/trial_data/nwr/data";
        String pathToConllFiles = "/Users/piek/Desktop/SemEval2018/trial_data/input/s3/CONLL";
        ArrayList<File> conllFiles = Util.makeRecursiveFileList(new File(pathToConllFiles), ".conll");

        /// process all trig files and build the knowledge graphs
        ArrayList<File> trigFiles = Util.makeRecursiveFileList(new File(pathToTrigFiles), ".trig");
        TrigTripleData trigTripleData = TrigTripleReader.readTripleFromTrigFiles(trigFiles);
        ArrayList<String> domainEvents = EventTypes.getEventSubjectUris(trigTripleData.tripleMapInstances);
        HashMap<String, ArrayList<Statement>> eckgMap = TrigUtil.getPrimaryKnowledgeGraphHashMap(domainEvents,trigTripleData);
        HashMap<String, ArrayList<Statement>> seckgMap = TrigUtil.getSecondaryKnowledgeGraphHashMap(domainEvents,trigTripleData);

/*        try {
            OutputStream fos = new FileOutputStream(pathToQuestionFile+".eckg");
            TrigUtil.printKnowledgeGraph(fos, eckgMap, seckgMap);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/


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
                ArrayList<String> eventKeys = new ArrayList<>();
                Set keySet = seckgMap.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String eventKey = keys.next();
                    ArrayList<Statement> secondaryStatements = seckgMap.get(eventKey);
                    if (matchTimeConstraint(secondaryStatements, questiondata)) {
                        if (!eventKeys.contains(eventKey)) eventKeys.add(eventKey);
                    }
                }
                JSONObject answerObject = new JSONObject();
                answerObject.put("numerical_anwer", eventKeys.size());
                for (int i = 0; i < eventKeys.size(); i++) {
                    String eventKey = eventKeys.get(i);
                    JSONObject documentObject = new JSONObject();
                    String eventId = Util.getNumericId(eventKey);
                    ArrayList<Statement> directStatements = eckgMap.get(eventKey);
                    ArrayList<String> fileNames = getFilesFromStatements(directStatements);
                    for (int j = 0; j < fileNames.size(); j++) {
                        String s = fileNames.get(j);
                        documentObject.put(eventId, s);
                    }
                    answerObject.put("answer_docs", documentObject);
                }
                answerObjectArray.put(questionId, answerObject);
                /**
                 *
                 "2-6871": {
                 "answer_docs": {
                 "799973": [
                 "3193b95f6cf7cc55977eecee652a1c11",
                 "d946a60729e647458a12be6bf1e938c2"
                 ],
                 "803822": [
                 "ebc6cc8eac3bd2fe52fa20c9f893e0ae",
                 "b80d73dbddcfb3d939aed0486022ecc1",
                 "3468a5abcade68507b6ecd0bbd9ed7cf",
                 "8b3b34bf4feb61c23f7392eafbb517a4",
                 "c3ae54561642c62433537ddb9f3e7fb8",
                 "a5795e3d5be396f0815785ce7a4948ac"
                 ]
                 },
                 "numerical_answer": 2
                 },
                 */
                //// add event tokenids to augment the CoNLL files with mentions
                getTokenEventMap(tokenEventIdMap, eventKeys, eckgMap);
            }

            try (FileWriter file = new FileWriter(pathToQuestionFile+".answer.json")) {


                ObjectMapper mapper = new ObjectMapper();
                file.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(answerObjectArray));
                file.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }


            /// process the CONLL files
            for (int i = 0; i < conllFiles.size(); i++) {
                File conllFile =conllFiles.get(i);
                ConllAnswerFromSem.resultForCoNLLFile(conllFile, tokenEventIdMap);
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
            if (kGraph.containsKey(eventKey)) {
                ArrayList<Statement> directStatements = kGraph.get(eventKey);
                for (int j = 0; j < directStatements.size(); j++) {
                    Statement statement = directStatements.get(j);
                    if (statement.getPredicate().getLocalName().equals("denotedBy")) {
                        String mention = statement.getObject().toString();
                        ArrayList<String> tokenList = getTokenIdsFromMention(mention);
                        for (int t = 0; t < tokenList.size(); t++) {
                            String tokenId = tokenList.get(t);
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
