package task5;

import com.hp.hpl.jena.rdf.model.Statement;
import match.SpatialReasoning;
import match.TrigReader;
import objects.Participants;
import objects.Space;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import question.Questiondata;
import util.Util;
import vu.cltl.triple.objects.TrigTripleData;
import vu.cltl.triple.TrigUtil;

import java.io.*;
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

    static public boolean LOGGING = false;
    static OutputStream locationlog = null;
    static String testParameters = "--question /Users/piek/Desktop/SemEval2018/trial_data_final/input/s1/questions.json " +
            "--eckg-files /Users/piek/Desktop/SemEval2018/trial_data_final/input/s3/eckg --subtask s1" +
            " --cities /Users/piek/Desktop/SemEval2018/scripts/cities.rel --states /Users/piek/Desktop/SemEval2018/scripts/states.rel";
    static String subtask = "s1"; // s2, s3
    static public void main(String[] args) {

        String pathToQuestionFile = "";
        String pathToEckgFiles = "";
        String taskFolder = "";
        if (args.length==0) args = testParameters.split(" ");
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--question") && args.length>(i+1)) {
                pathToQuestionFile = args[i+1];
            }
            else if (arg.equals("--eckg-files") && args.length>(i+1)) {
                pathToEckgFiles = args[i+1];
            }
            else if (arg.equals("--subtask") && args.length>(i+1)) {
                subtask = args[i+1];
            }
            else if (arg.equals("--task") && args.length>(i+1)) {
                taskFolder = args[i+1];
            }
            else if (arg.equals("--cities") && args.length>(i+1)) {
                String cityLex = args[i+1];
                Space.initCities(new File (cityLex));
            }
            else if (arg.equals("--states") && args.length>(i+1)) {
                String stateLex = args[i+1];
                Space.initStates(new File (stateLex));
            }
            else if (arg.equals("--log")) {
                LOGGING = true;
            }
        }
        if (LOGGING) {
            try {
                locationlog = new FileOutputStream("locationlog");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        ArrayList<File> eckgFiles = Util.makeRecursiveFileList(new File(pathToEckgFiles), ".trig");
        if (!taskFolder.isEmpty()) {
            subtask = "s1";
            pathToQuestionFile = taskFolder+"/"+subtask+"/"+"questions.json";
            performSubtask(eckgFiles, pathToQuestionFile, subtask);
            subtask = "s2";
            pathToQuestionFile = taskFolder+"/"+subtask+"/"+"questions.json";
            performSubtask(eckgFiles, pathToQuestionFile, subtask);
            subtask = "s3";
            pathToQuestionFile = taskFolder+"/"+subtask+"/"+"questions.json";
            performSubtask(eckgFiles, pathToQuestionFile, subtask);
        }
        else {
            performSubtask(eckgFiles, pathToQuestionFile, subtask);
        }
        if (LOGGING) {
            try {
                locationlog.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void performSubtask (ArrayList<File> eckgFiles, String pathToQuestionFile, String subtask) {
        //// process the question json
               //// match each question constraint with the KG and create json answer and the conll output files for event coreference
           try {
               File parentFolder = new File(pathToQuestionFile).getParentFile();
               int count = 0;
               JSONObject jsonObject = Util.readJsonFile(pathToQuestionFile);
               JSONObject answerObjectArray = new JSONObject();
               /// iterate over json array with the questions
               for (Object key : jsonObject.keySet()) {
                   count++;  /// just to break for testing
                   String questionId = (String) key;
                   System.out.println("questionId = " + questionId);
                   Object question = jsonObject.get(questionId);
                   Questiondata questiondata = new Questiondata(questionId, (JSONObject) question);
                   String dataString = questiondata.getYear()+questiondata.getNormalisedMonth()+ questiondata.getNormalisedDay();
                   System.out.println("dataString = " + dataString);
                   System.out.println("state = " + questiondata.getState());
                   System.out.println("city = " + questiondata.getCity());
                   System.out.println("first = " + questiondata.getParticipant_first());
                   System.out.println("last = " + questiondata.getParticipant_last());
                   ArrayList<File> myTrigFiles = new ArrayList<>();
                   for (int i = 0; i < eckgFiles.size(); i++) {
                       File eckGFile = eckgFiles.get(i);
                       if (eckGFile.getName().startsWith(dataString)) {
                           /// by using startWith,
                           // we ensure that constraints for just the year or month
                           // still match with
                           myTrigFiles.add(eckGFile);
                       }
                       else { ///////
                       }
                   }
                   System.out.println("myTrigFiles.size() = " + myTrigFiles.size());
                   vu.cltl.triple.objects.TrigTripleData trigTripleData = TrigReader.simpleRdfReader(myTrigFiles);
                   ArrayList<String> eventKeys = getQuestionDataEventKeys(trigTripleData, questiondata);
                   System.out.println("Nr. of incidents = " + eventKeys.size());

                   JSONObject answerObject = new JSONObject();
                   if (subtask.equals("s1")) {
                       // for subtask1 we should not add the numerical answer!!! It is always 1
                       ArrayList<String> fileNames = getFileNames(trigTripleData, eventKeys);
                       System.out.println("fileNames.size() = " + fileNames.size());
                       answerObject.put("answer_docs", fileNames);

                   }
                   else if (subtask.equals("s2")) {
                       ArrayList<String> fileNames = getFileNames(trigTripleData, eventKeys);
                       System.out.println("fileNames.size() = " + fileNames.size());
                       answerObject.put("answer_docs", fileNames);
                       answerObject.put("numerical_answer", eventKeys.size());
                   }
                   else if (subtask.equals("s3")) {
                       ///// we need to count the participants killed or injured
                       ArrayList<String> participants = getEntityParticipants(trigTripleData, questiondata, eventKeys);
                       System.out.println("Entity Victims = " + participants.size());
                       if (participants.size()==0) {
                          participants = getNonEntityParticipants(trigTripleData, questiondata, eventKeys);
                          System.out.println("Non Entity Victims = " + participants.size());
                       }
                       //System.out.println("participants.toString() = " + participants.toString());
                       answerObject.put("numerical_answer", participants.size());
                       if (participants.size()>0) {
                           ArrayList<String> fileNames = getFileNames(trigTripleData, eventKeys);
                           System.out.println("fileNames.size() = " + fileNames.size());
                           answerObject.put("answer_docs", fileNames);
                       }
                   }
                   answerObjectArray.put(questionId, answerObject);
               }
               try (FileWriter file = new FileWriter(parentFolder+"/"+"answers.json")) {
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

    static ArrayList<String> getFileNames (TrigTripleData trigTripleData, ArrayList<String> eventKeys) {
        ArrayList<String> fileNames = new ArrayList<>();
        for (int i = 0; i < eventKeys.size(); i++) {
            String eventKey = eventKeys.get(i);
            ArrayList<Statement> directStatements = trigTripleData.tripleMapInstances.get(eventKey);
            ArrayList<String> files = getFilesFromStatements(directStatements);
            for (int j = 0; j < files.size(); j++) {
                String file = files.get(j);
                if (!fileNames.contains(file)) fileNames.add(file);
            }
            if (fileNames.size()>1) {
             //   System.out.println("fileNames.toString() = " + fileNames.toString());
            }
        }
        return fileNames;
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
            if (checkType(questiondata, statements) && 
                    checkLocation(questiondata, statements, trigTripleData) &&
                    checkParticipantName(questiondata, statements, trigTripleData)) {
                if (!keys.contains(key)) {
                    keys.add(key);
                }
            }
        }
        return keys;
    }

    static boolean checkParticipantName (Questiondata questiondata, ArrayList<Statement> statements, TrigTripleData trigTripleData) {
            if (questiondata.getParticipant_first().isEmpty() && questiondata.getParticipant_last().isEmpty()) {
                return true;
            }
            boolean FIRST = false;
            boolean LAST = false;
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                String participantUri = "";
                if (statement.getPredicate().getLocalName().equalsIgnoreCase("a0")) {
                   // participantUri = statement.getObject().asResource().getURI();
                }
                else if (statement.getPredicate().getLocalName().equalsIgnoreCase("a1")) {
                    participantUri = statement.getObject().asResource().getURI();
                }
                if (!participantUri.isEmpty()) {
                    if (trigTripleData.tripleMapInstances.containsKey(participantUri)) {
                        ArrayList<Statement> participantStatements = trigTripleData.tripleMapInstances.get(participantUri);
                        for (int j = 0; j < participantStatements.size(); j++) {
                            Statement participantStatement = participantStatements.get(j);
                            if (    participantStatement.getPredicate().getLocalName().equals("label") ||
                                    participantStatement.getPredicate().getLocalName().equals("prefLabel")
                                ) {
                                String label = participantStatement.getObject().asLiteral().getLexicalForm();
                                if (!questiondata.getParticipant_first().isEmpty() &&
                                        label.toLowerCase().startsWith(questiondata.getParticipant_first().toLowerCase())) {
                                    //System.out.println("label = " + label);
                                    FIRST = true;
                                }
                                if (!questiondata.getParticipant_last().isEmpty() &&
                                        label.toLowerCase().endsWith(questiondata.getParticipant_last().toLowerCase())) {
                                    //System.out.println("label = " + label);
                                    LAST = true;
                                }
                            }
                        }
                    }
                }
            }
            if (!questiondata.getParticipant_first().isEmpty() && !FIRST) {
                return false;
            }
            if (!questiondata.getParticipant_last().isEmpty() && !LAST) {
                return false;
            }
            return true;
        }


    static ArrayList<String> getEntityParticipants (TrigTripleData trigTripleData, Questiondata questiondata, ArrayList<String> events) {
        ArrayList<String> participants = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            String key = events.get(i);
            ArrayList<Statement> statements = trigTripleData.tripleMapInstances.get(key);
            if (checkType(questiondata, statements) && checkLocation(questiondata, statements, trigTripleData)) {
                participants = Participants.getEntityParticipants(trigTripleData.tripleMapOthers, statements);
            }
        }
        //System.out.println("participants = " + participants.toString());
        return participants;
    }

    static ArrayList<String> getNonEntityParticipants (TrigTripleData trigTripleData, Questiondata questiondata, ArrayList<String> events) {
        ArrayList<String> participants = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            String key = events.get(i);
            ArrayList<Statement> statements = trigTripleData.tripleMapInstances.get(key);
            if (checkType(questiondata, statements) && checkLocation(questiondata, statements, trigTripleData)) {
                participants = Participants.getNonEntityParticipants(trigTripleData.tripleMapOthers, statements);
            }
        }
        //System.out.println("participants = " + participants.toString());
        return participants;
    }

    /**
     * Restricted version where the relation has to hasActor or hasPlace (which is almost everything)
     * @param questiondata
     * @param statements
     * @param trigTripleData
     * @return
     */
    static boolean checkLocation (Questiondata questiondata, ArrayList<Statement> statements, TrigTripleData trigTripleData) {
        if (questiondata.getCity().isEmpty() && questiondata.getState().isEmpty()) {
            return true;
        }
        ArrayList<String> dbpediaTargetObjects = new ArrayList<>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getPredicate().getLocalName().equals("hasActor") ||
                statement.getPredicate().getLocalName().equals("hasPlace")) {
                if (statement.getObject().toString().equals(questiondata.getCity()) ||
                        statement.getObject().toString().equals(questiondata.getState())) {
                    System.out.println("LOCATION MATCH:"+statement.getObject().toString());
                    return true;
                }
                else {
                    /// we add this as a possible target for dbp location sparql
                    if (statement.getObject().toString().indexOf("dbpedia")>-1 ) {
                      if (!dbpediaTargetObjects.contains(statement.getObject().toString())) {
                          dbpediaTargetObjects.add(statement.getObject().toString());
                      }
                    }
                    if (trigTripleData.tripleMapInstances.containsKey(statement.getObject().toString())) {
                        ArrayList<Statement> statementArrayList = trigTripleData.tripleMapInstances.get(statement.getObject().toString());
                        for (int j = 0; j < statementArrayList.size(); j++) {
                            Statement objectStatement = statementArrayList.get(j);
                            if (objectStatement.getPredicate().getLocalName().equals("type")) {
                                /////    <http://dbpedia.org/resource/Colton,_California>
                                /////       a  <http://dbpedia.org/resource/Colton,_California> ,
                                // nwrontology:MISC , nwrontology:LOC ,
                                // <http://dbpedia.org/resource/Colton,_Staffordshire> ,
                                // <http://dbpedia.org/resource/Colton,_Washington> ,
                                // <http://dbpedia.org/resource/Colton,_New_York> ,
                                // <http://dbpedia.org/resource/Colton,_Leeds> ,
                                // <http://dbpedia.org/resource/Colton,_Utah> ,
                                // <http://dbpedia.org/resource/Electoral_district_of_Colton> ,
                                // <http://dbpedia.org/resource/Colton,_Cumbria> , nwrontology:ORG ;
                                if (objectStatement.getObject().toString().equals(questiondata.getCity()) ||
                                        objectStatement.getObject().toString().equals(questiondata.getState())) {
                                    System.out.println("LOCATION MATCH:"+statement.getObject().toString());
                                                    return true;
                                }
                                else {
                                    /// we add this one as well as a possible target for dbp location sparql
                                    if (objectStatement.getObject().toString().indexOf("dbpedia")>-1 ) {
                                      if (!dbpediaTargetObjects.contains(objectStatement.getObject().toString())) {
                                          dbpediaTargetObjects.add(objectStatement.getObject().toString());
                                      }
                                    }
                                }
                            }
                        }
                    }
                }

                ///// At this point we have no match,

                if (LOGGING) {
                    if (statement.getPredicate().getLocalName().equals("hasPlace") && statement.getObject().toString().indexOf("dbpedia") > -1) {
                        String str = "LOCATION MISMATCH:\n";
                        str += "questiondata State = " + questiondata.getState() + "\n";
                        str += "questiondata City = " + questiondata.getCity() + "\n";
                        str += "statement.getObject().toString() = " + statement.getObject().toString() + "\n";
                        try {
                            locationlog.write(str.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        //System.out.println("dbpediaTargetObjects.size() = " + dbpediaTargetObjects.size());
        if (dbpediaTargetObjects.size()>0) {
            ArrayList<String> matchingLocations = new ArrayList<>();
           if (!questiondata.getCity().isEmpty()) {
               matchingLocations = SpatialReasoning.spaceRelatedCityLexicon("<"+questiondata.getCity()+">", dbpediaTargetObjects);
           }
           if (!questiondata.getState().isEmpty()) {
               matchingLocations.addAll(SpatialReasoning.spaceRelatedStateLexicon("<"+questiondata.getState()+">", dbpediaTargetObjects));
           }
           if (matchingLocations.size()>0) {
               System.out.println("INDIRECT LOCATION MATCH:"+matchingLocations.toString());
               return true;
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
