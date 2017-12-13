package task5;

import com.hp.hpl.jena.rdf.model.Statement;
import match.TrigReader;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import question.Questiondata;
import util.Util;
import vu.cltl.triple.TrigTripleData;
import vu.cltl.triple.TrigUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import static objects.EventTypes.eventInjuryMatch;
import static objects.EventTypes.eventKillMatch;

/**
 * Created by piek on 10/11/2017.
 * New version that assumes there is no specific set of documents associated with each query in the json file.
 * This means that all documents associated with the CoNLL file needs to be considered for event detection and cross-document event coreference.
 * We therefore separated the event coreference from answering the questions. The event coreference considers all the documents and all events within the documents
 *
 */
public class Task5Counting {

    static ArrayList<String> allEventKeys = new ArrayList<>();

    static public void main(String[] args) {
        String pathToQuestionFile = "/Users/piek/Desktop/SemEval2018/trial_data_final/s3/questions.json";
        String pathToEckgFiles = "/Users/piek/Desktop/SemEval2018/trial_data_final/s3/eckg";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--question") && args.length>(i+1)) {
                pathToQuestionFile = args[i+1];
            }
            else if (arg.equals("--eckg-files") && args.length>(i+1)) {
                pathToEckgFiles = args[i+1];
            }
        }

        ArrayList<File> eckgFiles = Util.makeRecursiveFileList(new File(pathToEckgFiles), ".trig");
        //// process the question json
        //// match each question constraint with the KG and create json answer and the conll output files for event coreference
        try {
            JSONObject jsonObject = Util.readJsonFile(pathToQuestionFile);
            JSONObject answerObjectArray = new JSONObject();
            /// iterate over json array with the questions
            for (Object key : jsonObject.keySet()) {
                String questionId = (String) key;
                System.out.println("questionId = " + questionId);
                Object question = jsonObject.get(questionId);
                Questiondata questiondata = new Questiondata(questionId, (JSONObject) question);
                String dataString = questiondata.getYear()+questiondata.getNormalisedMonth()+ questiondata.getNormalisedDay();
                //System.out.println("dataString = " + dataString);
                ArrayList<File> myTrigFiles = new ArrayList<>();
                for (int i = 0; i < eckgFiles.size(); i++) {
                    File eckGFile = eckgFiles.get(i);
                    if (eckGFile.getName().startsWith(dataString)) {
                        myTrigFiles.add(eckGFile);
                    }
                    else {
                      //  System.out.println("ignoring:"+eckGFile.getName());
                    }
                }
               // System.out.println("myTrigFiles.size() = " + myTrigFiles.size());
                vu.cltl.triple.TrigTripleData trigTripleData = TrigReader.simpleRdfReader(myTrigFiles);
              //  ArrayList<String> eventKeys = getAllEventKeys(trigTripleData);
                ArrayList<String> eventKeys = getQuestionDataEventKeys(trigTripleData, questiondata);
                System.out.println("eventKeys.size() = " + eventKeys.size());
                JSONObject answerObject = new JSONObject();
                answerObject.put("numerical_anwer", eventKeys.size());
                JSONObject docObject = new JSONObject();
                for (int i = 0; i < eventKeys.size(); i++) {
                    String eventKey = eventKeys.get(i);
                    Integer intId = Util.getEventId(eventKey, eventKeys);
                    ArrayList<Statement> directStatements = trigTripleData.tripleMapInstances.get(eventKey);
                    ArrayList<String> fileNames = getFilesFromStatements(directStatements);
                    docObject.put(intId.toString(), fileNames);
                    if (fileNames.size()>1) {
                        System.out.println("fileNames.toString() = " + fileNames.toString());
                        System.out.println("docObject = " + docObject.toString());
                    }
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
                //break;
            }
            try (FileWriter file = new FileWriter(pathToQuestionFile+".answer.json")) {
                ObjectMapper mapper = new ObjectMapper();
                file.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(answerObjectArray));
                file.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static ArrayList<String> getAllEventKeys (TrigTripleData trigTripleData) {
        ArrayList<String> keys = new ArrayList<>();
        Set keySet = trigTripleData.tripleMapInstances.keySet();
        Iterator<String> keySetKeys = keySet.iterator();
        while (keySetKeys.hasNext()) {
            String key = keySetKeys.next();
            ArrayList<Statement> statements = trigTripleData.tripleMapInstances.get(key);
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                if (statement.getPredicate().getLocalName().equals("type"))
                   // System.out.println("statement.getObject().toString() = " + statement.getObject().toString());
                if (statement.getObject().toString().endsWith("sem/Event")) {
                    if (!keys.contains(statement.getSubject().getURI())) {
                        keys.add(statement.getSubject().getURI());
                    }
                    break;
                }
            }
        }
        return keys;
    }

    static ArrayList<String> getQuestionDataEventKeys (TrigTripleData trigTripleData, Questiondata questiondata) {
        ArrayList<String> keys = new ArrayList<>();
        Set keySet = trigTripleData.tripleMapInstances.keySet();
        Iterator<String> keySetKeys = keySet.iterator();
        while (keySetKeys.hasNext()) {
            String key = keySetKeys.next();
            ArrayList<Statement> statements = trigTripleData.tripleMapInstances.get(key);
            if (checkType(questiondata, statements) && checkLocation(questiondata, statements)) {
                if (!keys.contains(key)) {
                    keys.add(key);
                }
            }
        }
        return keys;
    }

    static boolean checkLocation (Questiondata questiondata, ArrayList<Statement> statements) {
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getPredicate().getLocalName().equals("hasActor") ||
                statement.getPredicate().getLocalName().equals("hasPlace")) {
                if (statement.getObject().toString().equals(questiondata.getCity()) ||
                        statement.getObject().toString().equals(questiondata.getState())) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean checkType (Questiondata questiondata, ArrayList<Statement> statements) {
        if (questiondata.getEvent_type().equals("killing") && eventKillMatch(statements)) {
            return true;
        }
        if (questiondata.getEvent_type().equals("injuring") && eventInjuryMatch(statements)) {
            return true;
        }
        return false;
    }

    static ArrayList<String> getFilesFromStatements (ArrayList<Statement> directStatements) {
         ArrayList<String> fileNames = new ArrayList<>();
         for (int j = 0; j < directStatements.size(); j++) {
             Statement statement = directStatements.get(j);
             if (statement.getPredicate().getLocalName().equals("denotedBy")) {
                 String mention = statement.getObject().toString();
                 String fileName = getFileNameFromMention(mention);
                 if (!fileNames.contains(fileName)) {
                    // System.out.println("fileName = " + fileName);
                     fileNames.add(fileName);
                 }
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
