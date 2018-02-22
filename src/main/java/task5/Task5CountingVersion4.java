package task5;

import com.hp.hpl.jena.rdf.model.Statement;
import match.SpatialReasoning;
import match.TemporalReasoning;
import match.TrigReader;
import objects.EventTypes;
import objects.Participants;
import objects.Space;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import question.Questiondata;
import util.Util;
import vu.cltl.triple.TrigUtil;
import vu.cltl.triple.objects.TrigTripleData;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import static objects.EventTypes.*;

/**
 * Created by piek on 10/11/2017.
 * New version that assumes there is no specific set of documents associated with each query in the json file.
 * This means that all documents associated with the CoNLL file needs to be considered for event detection and cross-document event coreference.
 * We therefore separated the event coreference from answering the questions. The event coreference considers all the documents and all events within the documents
 *
 */
public class Task5CountingVersion4 {

    /**
     * S1
      142         "event_type": "fire_burning",
      551         "event_type": "injuring",
       13         "event_type": "job_firing",
      326         "event_type": "killing",

       S2
       79         "event_type": "fire_burning",
      543         "event_type": "injuring",
        4         "event_type": "job_firing",
      371         "event_type": "killing",

       S3
     1502         "event_type": "injuring",
       26         "event_type": "job_firing",
      928         "event_type": "killing",

     */

    static String typeMismatch = "";
    static String locMismatch = "";
    static String partMismatch = "";
    static public boolean LOGGING = false;
    static OutputStream locationlog = null;
    static String testParameters = "--question /Users/piek/Desktop/SemEval2018/trial_data_final/input/s1test/questions.json " +
            "--eckg-files /Users/piek/Desktop/SemEval2018/trial_data_final/eckg_4_day_dom_sentence --subtask s1 " +
            "--cities /Users/piek/Desktop/SemEval2018/scripts/cities.rel --states /Users/piek/Desktop/SemEval2018/scripts/states.rel " +
            "--period dct  --log";
    static String subtask = ""; // s1, s2, s3
    static String period = "weekend"; // day, week, dct (document-creation-day)
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
            else if (arg.equals("--period")&& args.length>(i+1)) {
                period = args[i+1];
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
                locationlog = new FileOutputStream(pathToQuestionFile+".log");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (!taskFolder.isEmpty()) {
            subtask = "s1";
            pathToQuestionFile = taskFolder+"/"+subtask+"/"+"questions.json";
            performSubtask(pathToEckgFiles, pathToQuestionFile, subtask);
            subtask = "s2";
            pathToQuestionFile = taskFolder+"/"+subtask+"/"+"questions.json";
            performSubtask(pathToEckgFiles, pathToQuestionFile, subtask);
            subtask = "s3";
            pathToQuestionFile = taskFolder+"/"+subtask+"/"+"questions.json";
            performSubtask(pathToEckgFiles, pathToQuestionFile, subtask);
        }
        else {
            performSubtask(pathToEckgFiles, pathToQuestionFile, subtask);
        }
        if (LOGGING) {
            try {
                locationlog.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void performSubtask (String eckgFolderPath,
                                String pathToQuestionFile,
                                String subtask) {
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
                   System.out.println("\nquestionId = " + questionId);
                   Object question = jsonObject.get(questionId);
                   Questiondata questiondata = new Questiondata(questionId, (JSONObject) question);
                   String dateString = questiondata.getYear()+questiondata.getNormalisedMonth()+ questiondata.getNormalisedDay();
                   String dayString = questiondata.getDay();
                   System.out.println("dateString = " + dateString);
                   System.out.println("incident type= " + questiondata.getEvent_type());
                   System.out.println("state = " + questiondata.getState());
                   System.out.println("city = " + questiondata.getCity());
                   System.out.println("first = " + questiondata.getParticipant_first());
                   System.out.println("last = " + questiondata.getParticipant_last());
                   /// Depending on the type of events from the query, we process a different set of trig files
                   /// Trig files are divided on the basis of the dominant type over different subfolders: SHOOT, BURN, DISMISS
                   /// Within each subfolder, a separate <date>.trig file is created
                   ArrayList<File> myTrigFiles = new ArrayList<>();
                   String eventType = EventTypes.SHOOT;
                   if (questiondata.getEvent_type().equals("fire_burning")) {
                       eventType = EventTypes.BURN;
                   }
                   else if (questiondata.getEvent_type().equals("job_firing")) {
                       eventType = EventTypes.DISMISS;
                   }
                   String logString = "\n"+questiondata.toString();
                   //// we only load the files for the requested event type
                   File eckgFolder = new File (eckgFolderPath+"/"+eventType);
                   ArrayList<File> eckgFiles = Util.makeRecursiveFileList(eckgFolder, ".trig");
                   logString+="###### ANSWER ########\n";
                   logString += "\tIncident type = "+eventType+"\n";
                   logString +="\tTotal nr. of trig files = "+eckgFiles.size()+"\n";
                   for (int i = 0; i < eckgFiles.size(); i++) {
                       File eckGFile = eckgFiles.get(i);
                       //// we check the date constraints
                       if (dateString.isEmpty()) {
                           /// there is no time constraint so we take all files
                           myTrigFiles.add(eckGFile);
                       }
                       else if (dayString.isEmpty()) {
                              /// the time constraint is a month or year
                              /// we take all trig files that start with year or year+month
                              /// we do not need to worry about day matches, weekends or week
                              if (eckGFile.getName().startsWith(dateString)) {
                                 myTrigFiles.add(eckGFile);
                              }
                       }
                       else if (period.equalsIgnoreCase("weekend")) {
                           if (TemporalReasoning.matchWeekendConstraint(dayString, eckGFile.getName())) {
                               /// we expand the question date (day) to the week after to capture all trig files based on document creation time
                               /// and are published after the incident date
                               myTrigFiles.add(eckGFile);
                           }
                       }
                       else if (period.equalsIgnoreCase("week")) {
                           if (TemporalReasoning.matchNextWeekConstraint(dayString, eckGFile.getName())) {
                               /// we expand the question date (day) to the week after to capture all trig files based on document creation time
                               /// and are published after the incident date
                               myTrigFiles.add(eckGFile);
                           }
                       }
                       else if (period.equalsIgnoreCase("day") || period.equalsIgnoreCase("dct")) {
                           if (eckGFile.getName().startsWith(dateString)) {
                               myTrigFiles.add(eckGFile);
                           }
                           else if (eckGFile.getName().endsWith("00")) {
                               String eckGshort = eckGFile.getName().substring(0, eckGFile.getName().length()-3);
                               System.out.println("eckGshort = " + eckGshort);
                               if (dateString.startsWith(eckGshort)) {
                                   myTrigFiles.add(eckGFile);
                               }
                           }
                       }
                       else {
                           System.out.println("unknown settings period = " + period);
                       }
                   }
                   logString +="\tTotal nr. of trig files with date match = "+myTrigFiles.size()+"\n";
                   if (LOGGING) locationlog.write(logString.getBytes());
                   System.out.println("myTrigFiles.size() = " + myTrigFiles.size());
                   /// We read all the trigfiles that pass the time and type constraints
                   TrigTripleData trigTripleData = TrigReader.simpleRdfReader(myTrigFiles);
                   ArrayList<String> eventKeys = getQuestionDataEventKeys(trigTripleData, questiondata);

                   ArrayList<String> incidents = getIncidents(eventKeys);
                   ArrayList<String> subevents = getSubEventsOfIncidents(eventKeys);
                   System.out.println("Nr. of matching incidents = " + incidents.size());
                   System.out.println("Nr. of matching subevents = " + subevents.size());
                   logString = "\tNr. of matching incidents = " + incidents.size()+"\n";
                   logString += "\tNr. of matching subevents = " + subevents.size()+"\n";
                   if (LOGGING) locationlog.write(logString.getBytes());

                   ArrayList<String> fileNames = getFileNames(trigTripleData, incidents);
                   System.out.println("fileNames.size() = " + fileNames.size());
                   logString = "\tTotal nr of souce file names = "+fileNames.size()+"\n";
                   if (LOGGING) locationlog.write(logString.getBytes());
                   JSONObject answerObject = new JSONObject();
                   if (subtask.equals("s1")) {
                       // for subtask1 we should not add the numerical answer!!! It is always 1
                       answerObject.put("answer_docs", fileNames);
                   }
                   else if (subtask.equals("s2")) {
                       answerObject.put("answer_docs", fileNames);
                       answerObject.put("numerical_answer", incidents.size());
                   }
                   else if (subtask.equals("s3")) {
                       ///// we need to count the participants killed or injured
                       int victimCount = countSemParticipants(trigTripleData, questiondata, subevents);
                       System.out.println("victimCount = " + victimCount);
                       logString ="\tvictimCount = " + victimCount+"\n";
                       if (LOGGING) locationlog.write(logString.getBytes());
                       answerObject.put("numerical_answer", victimCount);
                       if (victimCount>0) {
                           answerObject.put("answer_docs", fileNames);
                       }
                   }
                   if (subtask.equals("s1")) {
                        if (fileNames.size()>0) {
                            answerObjectArray.put(questionId, answerObject);
                        }
                   }
                   else {
                       answerObjectArray.put(questionId, answerObject);
                   }
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

    static ArrayList<String> getIncidents (ArrayList<String> eventKeys) {
        ArrayList<String>  incidents = new ArrayList<>();
        for (int i = 0; i < eventKeys.size(); i++) {
            String eventKey = eventKeys.get(i);
            if (eventKey.endsWith("#incident")) {
               if (!incidents.contains(eventKey)) incidents.add(eventKey);
            }
        }
        return incidents;
    }

    static ArrayList<String> getSubEventsOfIncidents (ArrayList<String> eventKeys) {
        ArrayList<String>  subevents = new ArrayList<>();
        for (int i = 0; i < eventKeys.size(); i++) {
            String eventKey = eventKeys.get(i);
            if (eventKey.indexOf("#incident#")>-1) {
               if (!subevents.contains(eventKey)) subevents.add(eventKey);
            }
        }
        return subevents;
    }

    static ArrayList<String> getFileNames (TrigTripleData trigTripleData,
                                           ArrayList<String> eventKeys) {
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

    /**
     * The TrigTripleData is derived from all trigfiles that passed the time and type constraints
     * We now check what are the events and which events pass the more specific constraints
     * @param trigTripleData
     * @param questiondata
     * @return
     */
    static ArrayList<String> getQuestionDataEventKeys (TrigTripleData trigTripleData,
                                                       Questiondata questiondata) {
        HashMap<String, Integer> typeMismatches = new HashMap<>();
        typeMismatch = "";
        partMismatch = "";
        locMismatch = "";
        int nTypeMismatch = 0;
        int nLocMismatch = 0;
        int nPartMismatch = 0;
        String logString = "";
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> eventKeys = new ArrayList<>();
        Set keySet = trigTripleData.tripleMapInstances.keySet();
        Iterator<String> keySetKeys = keySet.iterator();
        while (keySetKeys.hasNext()) {
            String key = keySetKeys.next();
            if (key.indexOf("#incident#")>-1 || key.endsWith("#incident")) {
                //System.out.println("key = " + key);
                if (!eventKeys.contains(key)) eventKeys.add(key);
            }
        }
        logString+= "Nr of incidents and subevents in trigfiles = "+eventKeys.size()+"\n";

        for (int i = 0; i < eventKeys.size(); i++) {
            String eventKey =  eventKeys.get(i);
            ArrayList<Statement> statements = trigTripleData.tripleMapInstances.get(eventKey);
            boolean MATCH = true;
            if (!checkSubeventType(questiondata, statements)) {
                //System.out.println("WRONG TYPE");
                ArrayList<String> types = getEventTypesAndSubevent(statements);
                //System.out.println(questiondata.getEvent_type()+" mismatching types.toString() = " + types.toString());
                for (int j = 0; j < types.size(); j++) {
                    String s = types.get(j);
                    if (typeMismatches.containsKey(s)) {
                        Integer cnt = typeMismatches.get(s);
                        cnt++;
                        typeMismatches.put(s, cnt);
                    }
                    else {
                        typeMismatches.put(s,1);
                    }
                }
                nTypeMismatch++;
                MATCH = false;
            }
            else {
              //  System.out.println("RIGHT TYPE");
            }
            if (MATCH) {
                ArrayList<String> matchingLocations = getMatchingLocations(questiondata, statements, trigTripleData);
                if (matchingLocations.isEmpty()) {
                    nLocMismatch++;
                    MATCH = false;
                } else {
                    if (!matchingLocations.contains("NOTREQUIRED")) {
                       if (LOGGING) logString += "\tLocation match = " + matchingLocations.toString() + "\n";
                    }
                }
                /*if (!checkLocation(questiondata, statements,trigTripleData)) {
                               // System.out.println("WRONG LOCATION");
                                MATCH = false;
                            }
                    else {
                          //  System.out.println("RIGHT LOCATION");
                }*/
            }

            if (MATCH) {
                ArrayList<String> matchingParticipants = getMatchingParticipantNames(questiondata, statements, trigTripleData);
                if (matchingParticipants.isEmpty()) {
                    nPartMismatch++;
                    MATCH = false;
                } else {
                    if (!matchingParticipants.contains("NOTREQUIRED")) {
                        logString += "\tParticipant match = " + matchingParticipants.toString() + "\n";
                    }
                }
                /*
                if (!checkParticipantName(questiondata, statements,trigTripleData)) {
                 //   System.out.println("WRONG NAME");
                    MATCH = false;
                }
                else {
                  //  System.out.println("RIGHT NAME");
                }*/
            }

            if (MATCH) {
                if (!keys.contains(eventKey)) {
                    keys.add(eventKey);
                }
            }
        }
        System.out.println("Nr. of matching incidents and subevents keys = " + keys.size());
        if (LOGGING) {
            logString += "\tNr. of matching incidents and subevents keys = " + keys.size() + "\n";
            if (keys.size() == 0) {
                logString += "\t" + "type mismatches = " + nTypeMismatch + "," + "location mismatches = " + nLocMismatch + "," + "particpant mismatches = " + nPartMismatch + "\n";
                Set keySetMisType = typeMismatches.keySet();
                Iterator<String> types = keySetMisType.iterator();
                while (types.hasNext()) {
                    String type = types.next();
                    Integer cnt = typeMismatches.get(type);
                    logString += type + ":" + cnt + ",";
                }
                logString += "\n";
                logString += locMismatch+"\n";
                logString += partMismatch+"\n";
            }
            try {
                locationlog.write(logString.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return keys;
    }

    static boolean checkParticipantName (Questiondata questiondata,
                                         ArrayList<Statement> statements,
                                         TrigTripleData trigTripleData) {
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
                else if (statement.getPredicate().getLocalName().equalsIgnoreCase("hasActor")) {
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

static ArrayList<String> getMatchingParticipantNames (Questiondata questiondata,
                                         ArrayList<Statement> statements,
                                         TrigTripleData trigTripleData) {
        ArrayList<String> participants = new ArrayList<>();
        ArrayList<String> misparticipants = new ArrayList<>();

            if (questiondata.getParticipant_first().isEmpty() && questiondata.getParticipant_last().isEmpty()) {
                participants.add("NOTREQUIRED");
            }
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                String participantUri = "";
                if (statement.getPredicate().getLocalName().equalsIgnoreCase("a0")) {
                   // participantUri = statement.getObject().asResource().getURI();
                }
                else if (statement.getPredicate().getLocalName().equalsIgnoreCase("a1")) {
                    participantUri = statement.getObject().asResource().getURI();
                }
                else if (statement.getPredicate().getLocalName().equalsIgnoreCase("hasActor")) {
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
                                    if (!participants.contains(label)) participants.add(label);
                                }
                                if (!questiondata.getParticipant_last().isEmpty() &&
                                        label.toLowerCase().endsWith(questiondata.getParticipant_last().toLowerCase())) {
                                    //System.out.println("label = " + label);
                                    if (!participants.contains(label)) participants.add(label);
                                }
                                if (participants.size()==0) {
                                    if (!misparticipants.contains(label)) misparticipants.add(label);
                                }
                            }
                        }
                    }
                }
            }
            if (LOGGING) {
                if (participants.size()==0 && misparticipants.size()>0) {
                        partMismatch += " mismatches = "+misparticipants.toString()+"\n";
                }
            }
            return participants;
    }


    static ArrayList<String> getSemParticipants (TrigTripleData trigTripleData,
                                                 Questiondata questiondata,
                                                 ArrayList<String> events) {
        ArrayList<String> participants = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            String key = events.get(i);
            ArrayList<Statement> statements = trigTripleData.tripleMapInstances.get(key);
            if (checkSubeventType(questiondata, statements)) {
                /// we have the right type of event
                participants.addAll(Participants.getSemParticipants(statements));
            }
        }
        //System.out.println("participants = " + participants.toString());
        return participants;
    }

    /**
     * We assume that each event has its own participants with different identities.
     * We need to estimate the number of victims for the event of the right type
     * If a boy was killed in one event it is not the same as a boy in another event
     * If we have a victim's name, we need to ignore all other names
     * If there is no participant, we count as one
     * @param trigTripleData
     * @param questiondata
     * @param events
     * @return
     */
    static int countSemParticipants (TrigTripleData trigTripleData,
                                                 Questiondata questiondata,
                                                 ArrayList<String> events) {
        int count = 0;
        for (int i = 0; i < events.size(); i++) {
            String key = events.get(i);
            ArrayList<Statement> statements = trigTripleData.tripleMapInstances.get(key);
            if (checkSubeventType(questiondata, statements)) {
                /// we have the right type of event
                ArrayList<String> participants = Participants.getSemParticipants(statements);
                //System.out.println("participants.toString() = " + participants.toString());
                int nDbpedia = 0;
                int nEntity = 0;
                int nNonEntity = 0;
                for (int j = 0; j < participants.size(); j++) {
                    String p = participants.get(j);
                    if (p.indexOf("dbpedia")>-1) nDbpedia++;
                    if (p.indexOf("/entities/")>-1) nEntity++;
                    if (p.indexOf("/non-entities/")>-1) nNonEntity++;
                }
                if (nDbpedia>0 || nEntity>0) {
                    count += nDbpedia+nEntity;
                }
                else if (nNonEntity>0) {
                    count += nNonEntity;
                }
                else {
                    count++;
                }

            }
        }
        //System.out.println("participants = " + participants.toString());
        return count;
    }

    static ArrayList<String> getEntityParticipants (TrigTripleData trigTripleData,
                                                    Questiondata questiondata,
                                                    ArrayList<String> events) {
        ArrayList<String> participants = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            String key = events.get(i);
            ArrayList<Statement> statements = trigTripleData.tripleMapInstances.get(key);
            if (checkSubeventType(questiondata, statements)) {
                participants.addAll(Participants.getEntityParticipants(trigTripleData.tripleMapOthers, statements));
            }
        }
        //System.out.println("participants = " + participants.toString());
        return participants;
    }

    static ArrayList<String> getNonEntityParticipants (TrigTripleData trigTripleData,
                                                       Questiondata questiondata,
                                                       ArrayList<String> events) {
        ArrayList<String> participants = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            String key = events.get(i);
            ArrayList<Statement> statements = trigTripleData.tripleMapInstances.get(key);
            if (checkSubeventType(questiondata, statements)) {
                participants.addAll(Participants.getNonEntityParticipants(trigTripleData.tripleMapOthers, statements));
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
    static boolean checkLocation (Questiondata questiondata,
                                  ArrayList<Statement> statements,
                                  TrigTripleData trigTripleData) {
        if (questiondata.getCity().isEmpty() && questiondata.getState().isEmpty()) {
            return true;
        }
        ArrayList<String> dbpediaTargetObjects = new ArrayList<>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getPredicate().getLocalName().equals("hasPlace")) {
                if (statement.getObject().toString().equals(questiondata.getCity()) ||
                        statement.getObject().toString().equals(questiondata.getState())) {
                   // System.out.println("DIRECT LOCATION MATCH");
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
                                //// we find various location URIs linked through the "type" relation
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
                                   // System.out.println("TYPE LOCATION MATCH");
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
            }
        }
        //System.out.println("dbpediaTargetObjects.size() = " + dbpediaTargetObjects.size());
        if (dbpediaTargetObjects.size()>0) {
            ArrayList<String> matchingLocations = new ArrayList<>();
           if (!questiondata.getCity().isEmpty()) {
               matchingLocations = SpatialReasoning.spaceRelatedCityLexicon(questiondata.getCity(), dbpediaTargetObjects);
           }
           if (!questiondata.getState().isEmpty()) {
               matchingLocations.addAll(SpatialReasoning.spaceRelatedStateLexicon(questiondata.getState(), dbpediaTargetObjects));
           }
           if (matchingLocations.size()>0) {
             //  System.out.println("INDIRECT LOCATION MATCH");
               return true;
           }
        }
        return false;
    }


    /**
     * Restricted version where the relation has to hasActor or hasPlace (which is almost everything)
     * @param questiondata
     * @param statements
     * @param trigTripleData
     * @return
     */
    static ArrayList<String> getMatchingLocations (Questiondata questiondata,
                                  ArrayList<Statement> statements,
                                  TrigTripleData trigTripleData) {
        ArrayList<String> locations = new ArrayList<>();
        ArrayList<String> mislocations = new ArrayList<>();
        if (questiondata.getCity().isEmpty() && questiondata.getState().isEmpty()) {
            locations.add("NOTREQUIRED");
        }
        ArrayList<String> dbpediaTargetObjects = new ArrayList<>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getPredicate().getLocalName().equals("hasPlace") ||
                    statement.getPredicate().getLocalName().equals("a0") ||
                    statement.getPredicate().getLocalName().equals("a1")) {
                boolean match = false;
                if (statement.getObject().toString().equals(questiondata.getCity()) ||
                        statement.getObject().toString().equals(questiondata.getState())) {
                   // System.out.println("DIRECT LOCATION MATCH");
                    match = true;
                    if (!locations.contains(statement.getObject().toString())) locations.add(statement.getObject().toString());
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
                                //// we find various location URIs linked through the "type" relation
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
                                   // System.out.println("TYPE LOCATION MATCH");
                                    match = true;
                                    if (!locations.contains(statement.getObject().toString())) locations.add(statement.getObject().toString());
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
                if (!match) {
                     if (!mislocations.contains(statement.getObject().toString())) mislocations.add(statement.getObject().toString());
                }
            }
        }
        //System.out.println("dbpediaTargetObjects.size() = " + dbpediaTargetObjects.size());
        if (dbpediaTargetObjects.size()>0) {
            ArrayList<String> matchingLocations = new ArrayList<>();
           if (!questiondata.getCity().isEmpty()) {
               matchingLocations = SpatialReasoning.spaceRelatedCityLexicon(questiondata.getCity(), dbpediaTargetObjects);
           }
           if (!questiondata.getState().isEmpty()) {
               matchingLocations.addAll(SpatialReasoning.spaceRelatedStateLexicon(questiondata.getState(), dbpediaTargetObjects));
           }
           if (matchingLocations.size()>0) {
             //  System.out.println("INDIRECT LOCATION MATCH");
               for (int i = 0; i < matchingLocations.size(); i++) {
                   String location =  matchingLocations.get(i);
                   if (!locations.contains(location)) locations.add(location);
               }
           }
        }
        if (locations.size()==0 && mislocations.size()>0) {
            ///// desperate attempt:
            String pattern = mislocations.toString();
            String questionLocationString = "";
            if (questiondata.getCity().isEmpty()) {
                questionLocationString = questiondata.getState();
            }
            else {
                questionLocationString = questiondata.getCity();
            }
            ArrayList<String> desparateLocations = new ArrayList<>();
            ArrayList<String> desparateFields = new ArrayList<>();
            int idx = questionLocationString.lastIndexOf("/");
            if (idx>-1) questionLocationString = questionLocationString.substring(idx+1);
            String [] fields = questionLocationString.split(",");
            for (int i = 0; i < fields.length; i++) {
                String field = fields[i];
                if (field.startsWith("_")) field = field.substring(1);
                if (field.endsWith("_")) {
                    field = field.substring(0, field.length() - 1);
                    System.out.println("field = " + field);
                }
                desparateFields.add(field);
            }
            for (int j = 0; j < mislocations.size(); j++) {
                String location = mislocations.get(j);
                for (int i = 0; i < desparateFields.size(); i++) {
                    String field = desparateFields.get(i);
                    if (location.equalsIgnoreCase(field)) {
                        if (!desparateLocations.contains(field)) {
                            desparateLocations.add(field);
                        }
                    }
                }
            }
            if (desparateLocations.isEmpty()) {
                for (int j = 0; j < mislocations.size(); j++) {
                    String location = mislocations.get(j);
                    for (int i = 0; i < desparateFields.size(); i++) {
                        String field = desparateFields.get(i);
                        if (location.indexOf(field)>-1) {
                            if (!desparateLocations.contains(field)) {
                                desparateLocations.add(field);
                            }
                        }
                    }
                }
            }
            if (desparateLocations.isEmpty()) {
                if (LOGGING) {
                    locMismatch += " mislocations = "+ mislocations.toString()+"\n";
                }
            }
            else {
                locations = desparateLocations;
            }
        }
        return locations;
    }

    static boolean checkSubeventType (Questiondata questiondata, ArrayList<Statement> statements) {
        if (questiondata.getEvent_type().equals("killing") && eventKillMatch(statements)) {
            return true;
        }
        if (questiondata.getEvent_type().equals("injuring") && eventInjuryMatch(statements)) {
            return true;
        }
        if (questiondata.getEvent_type().equals("job_firing") && eventDismissMatch(statements)) {
            return true;
        }
        if (questiondata.getEvent_type().equals("fire_burning") && eventBurningMatch(statements)) {
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
