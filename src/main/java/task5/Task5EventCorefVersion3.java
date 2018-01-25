package task5;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.tdb.TDBFactory;
import conll.ConllOutputFromSem;
import match.DocumentIdentity;
import match.MatchSettings;
import match.TemporalReasoning;
import objects.EventTypes;
import objects.Space;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.json.simple.JSONObject;
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
public class Task5EventCorefVersion3 {
    static int nJoined = 0;
    static ArrayList<String> allEventKeys = new ArrayList<>();

    static String trialParameters = "--trig-files /Users/piek/Desktop/SemEval2018/trial_data_final/NAFDONE.ALL " +
            "--conll-file /Users/piek/Desktop/SemEval2018/trial_data_final/input/s3/docs.conll " +
            "--eckg /Users/piek/Desktop/SemEval2018/trial_data_final/eckg-weekend " +
            "--event-file /Users/piek/Desktop/SemEval2018/scripts/trial_vocabulary " +
            "--cities /Users/piek/Desktop/SemEval2018/scripts/cities.rel " +
            "--states /Users/piek/Desktop/SemEval2018/scripts/states.rel " +
            "--threshold 1 --period weekend";

    static String testParameters = "--trig-files /Users/piek/Desktop/SemEval2018/test_data/NAFOUT " +
            "--conll-file /Users/piek/Desktop/SemEval2018/test_data/input/s3/docs.conll " +
            "--eckg /Users/piek/Desktop/SemEval2018/trial_data_final/eckg-weekend " +
            "--event-file /Users/piek/Desktop/SemEval2018/scripts/trial_vocabulary " +
            "--cities /Users/piek/Desktop/SemEval2018/scripts/cities.rel " +
            "--states /Users/piek/Desktop/SemEval2018/scripts/states.rel " +
            "--threshold 1 --period weekend";

    static public void main(String[] args) {
        String pathToTrigFiles = "";
        String pathToConllFile = "";
        String pathToEckgFolder = "";
        String eventFile = "";
        String cityLex = "";
        String stateLex = "";
        Integer matchThreshold = 0;
        String period = "weekend"; ///day, week, weekend, dct (document-creation-day)
        MatchSettings matchSettings = new MatchSettings();
        if (args.length==0) args = trialParameters.split(" ");
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--trig-files") && args.length>(i+1)) {
                pathToTrigFiles = args[i+1];
            }
            else if (arg.equals("--conll-file") && args.length>(i+1)) {
                pathToConllFile = args[i+1];
            }
            else if (arg.equals("--eckg") && args.length>(i+1)) {
                pathToEckgFolder = args[i+1];
            }
            else if (arg.equals("--event-file") && args.length>(i+1)) {
                eventFile = args[i+1];
            }
            else if (arg.equals("--cities") && args.length>(i+1)) {
                cityLex = args[i+1];
                Space.initCities(new File (cityLex));
            }
            else if (arg.equals("--states") && args.length>(i+1)) {
                stateLex = args[i+1];
                Space.initStates(new File (stateLex));
            }
            else if (arg.equals("--period") && args.length>(i+1)) {
                period = args[i+1];
            }
            else if (arg.equals("--threshold") && args.length>(i+1)) {
                try {
                    matchThreshold = Integer.parseInt(args[i+1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        matchSettings.parseArguments(args);
        matchSettings.setLoose();
        if (matchThreshold>0) matchSettings.setTripleMatchThreshold(matchThreshold);
        System.out.println(matchSettings.getSettings());
        System.out.println("pathToConllFile = " + pathToConllFile);
        System.out.println("pathToTrigFiles = " + pathToTrigFiles);
        System.out.println("eventFile = " + eventFile);
        /// We first read the event type vocabulary file.
        HashMap<String, String> eventVocabulary = Util.ReadFileToStringHashMap(eventFile);
        EventTypes.initVocabulary(eventVocabulary);
        System.out.println("eventVocabulary.size() = " + eventVocabulary.size());
        System.out.println("Space.locationURIs.toString() = " + Space.locationURIs.size());

        File taskFileFolder = new File (pathToTrigFiles).getParentFile();
        File eckgFolder = new File (pathToEckgFolder);
        if (!eckgFolder.exists()) eckgFolder.mkdir();
        if (!eckgFolder.exists()) {
            System.out.println("Cannot find folder for ECKGs = "+eckgFolder.getAbsolutePath());
            return;
        }
        /// our first approach is event driven. Since we expect one incident per source document, we probably can better
        /// use the document as a starting point:
        // 1. group documents per temporal container
        // 2. group documents per container for dominant event type
        // 2. compare documents for incident identity within time-type container
        // 3. lump incident level events and sub events across all identical documents
        // 4. link subevents events to participants within and across all documents
        // 5. Dump <date>.eckgs to <date>.RDF-TRiG within the type containers: SHOOT, BURN, DISMISS
        // 6. Create the CoNLL files

        /// STEP 1
        /// process all trig files and build temporal containers using the document creation time
        File trigFolder = new File (pathToTrigFiles);
        ArrayList<File> trigFiles = Util.makeRecursiveFileList(trigFolder, ".trig");
        System.out.println("trigFiles.size() = " + trigFiles.size());
        HashMap<String,ArrayList<File>> temporalContainers = null;
        if (period.equalsIgnoreCase("week")) {
            temporalContainers = TemporalReasoning.getTemporalWeekContainersWithTrigFiles(trigFiles);
        }
        else if (period.equalsIgnoreCase("day")) {
            temporalContainers = TemporalReasoning.getTemporalContainersWithTrigFiles(trigFiles);
        }
        else if (period.equalsIgnoreCase("dct")) {
            temporalContainers = TemporalReasoning.getDocumentCreationTimeContainersWithTrigFiles(trigFiles);
        }
        else if (period.equalsIgnoreCase("weekend")) {
            temporalContainers = TemporalReasoning.getTemporalWeekendContainersWithTrigFiles(trigFiles);
        }
        System.out.println("temporalContainers.size() = " + temporalContainers.size());
        Set containerSet = temporalContainers.keySet();
        Iterator<String> containerKeys = containerSet.iterator();
        HashMap<String, ArrayList<Statement>> finalEvents = new HashMap<>();

        while (containerKeys.hasNext()) {
            String containerKey = containerKeys.next();
            ArrayList<File> timeTrigFiles = temporalContainers.get(containerKey);
            System.out.println();
            System.out.println("###########");
            System.out.println("containerKey = " + containerKey+": "+timeTrigFiles.size()+" source files");

        /// STEP 2
        /// group documents for dominant event type

            //// Within each temporal container, we split according to dominant event type
            HashMap<String, ArrayList<File>> eventTypeFileIndex = getContainerEventsPerType(timeTrigFiles);
            System.out.println("eventTypeFileIndex.size() = " + eventTypeFileIndex.size());
            Set typeSet = eventTypeFileIndex.keySet();
            Iterator<String> typeSetKeys = typeSet.iterator();
            while (typeSetKeys.hasNext()) {
                String taskEventSubType = typeSetKeys.next();
                ArrayList<File> typedFiles = eventTypeFileIndex.get(taskEventSubType);
                HashMap<String, ArrayList<Statement>> seckgMap = new HashMap<>();
                System.out.println("taskEventSubType = " + taskEventSubType+": "+typedFiles.size()+" source files");

        /// STEP 3 and 4
        /// establish identity according to matchsettings and merge incidents and subevents

                //// This HashMap has all aggregated statements that presumably belong to the same incident
                HashMap<String, ArrayList<Statement>> containerIncidents = getIncidentsAndSubEventThroughIDAP(typedFiles, seckgMap, matchSettings);
                System.out.println("incidents and subevents, containerIncidents.size() = " + containerIncidents.size());
                /// we add the unique incidents to the totals for creating the CoNLL result later on
                finalEvents.putAll(containerIncidents);

        /// STEP5
        /// dump the ECKGs to the event type containers in a RDF-TRiG file with the dominant incident time
                try {
                    File typeFolder = new File(eckgFolder.getAbsolutePath()+"/"+taskEventSubType);
                    if (!typeFolder.exists()) typeFolder.mkdir();
                    if (typeFolder.exists()) {
                        OutputStream fos3 = new FileOutputStream(typeFolder.getAbsoluteFile() + "/" + containerKey + ".trig");
                        Dataset dataset = TDBFactory.createDataset();
                        TrigUtil.prefixDefaultModels(dataset);
                        TrigUtil.addStatementsToJenaData(dataset, containerIncidents);
                        TrigUtil.addStatementsToJenaData(dataset, seckgMap);
                        RDFDataMgr.write(fos3, dataset.getDefaultModel(), RDFFormat.TRIG_PRETTY);
                        fos3.close();
                    }
                } catch (IOException e) {
                  e.printStackTrace();
                }
            }
        }

        System.out.println("nJoined = " + nJoined);
        /// STEP 6 CREATE THE CONLL FILE
        createSystemConllFile(finalEvents, pathToConllFile);
    }


    /**
     * For each document we choose the dominant task Event Type
     * @param containerTrigFiles
     * @return
     */
    static HashMap<String, ArrayList<File>> getContainerEventsPerType (ArrayList<File> containerTrigFiles) {
          HashMap<String, ArrayList<File>> eventTypeDocumentIndex = new HashMap<>();
          for (int i = 0; i < containerTrigFiles.size(); i++) {
              File timeTrigFile = containerTrigFiles.get(i);
              vu.cltl.triple.objects.TrigTripleData trigTripleData = vu.cltl.triple.read.TrigTripleReader.readTripleFromTrigFile(timeTrigFile);
              String taskSubType = EventTypes.getDominantEventTypeFromDataset(trigTripleData.tripleMapInstances);
              if (eventTypeDocumentIndex.containsKey(taskSubType)) {
                  ArrayList<File> files = eventTypeDocumentIndex.get(taskSubType);
                  if (!Util.hasFile(files, timeTrigFile)) {
                      files.add(timeTrigFile);
                      eventTypeDocumentIndex.put(taskSubType, files);
                  }
              }
              else {
                  ArrayList<File> files = new ArrayList<>();
                  files.add(timeTrigFile);
                  eventTypeDocumentIndex.put(taskSubType,files);
              }
          }
          return eventTypeDocumentIndex;
    }

    static HashMap<String, ArrayList<Statement>> getIncidentsAndSubEventThroughIDAP (ArrayList<File> containerTrigFiles,
                                                                     HashMap<String, ArrayList<Statement>> seckgMap,
                                                                     MatchSettings matchSettings) {
          /// now we need to deal with all the events in these trigfiles
          /// get locations
          /// get participants
          /// compare trigfiles for location and participant match
          /// if so merge events mentions accordingly but make sure hit, kill and injury are participant sensitive
          /// we also have to deal with BURN and DISMISS

          /// we get all triples from these trigfiles that share the same temporal container

          HashMap<String, ArrayList<Statement>> containerIncidents = new HashMap<>();
          HashMap<String, ArrayList<Statement>> eckgMap = new HashMap<>();
          HashMap<String, ArrayList<String>> documentEventIndex = new HashMap<>();

          for (int i = 0; i < containerTrigFiles.size(); i++) {
              File timeTrigFile = containerTrigFiles.get(i);
              vu.cltl.triple.objects.TrigTripleData trigTripleData = vu.cltl.triple.read.TrigTripleReader.readTripleFromTrigFile(timeTrigFile);

              /// from the complete graph we extract all events that match the domain constraints
              /// this is important to ignore all irrelevant event mentions and make the software more efficient
              ArrayList<String> domainEvents = EventTypes.getDomainEventSubjectUris(trigTripleData.tripleMapInstances);
              documentEventIndex.put(timeTrigFile.getName(), domainEvents);

              /// we build the KGs only relevant for the events to save space
              TrigUtil.addPrimaryKnowledgeGraphHashMap(domainEvents, eckgMap, trigTripleData);
              TrigUtil.addSecondaryKnowledgeGraphHashMap(domainEvents, seckgMap, trigTripleData);
              //System.out.println("domainEvents = " + domainEvents.size());
          }

          System.out.println("eckgMap = " + eckgMap.size());
          System.out.println("seckgMap = " + seckgMap.size());

          /// we need some similarity function that compares the events across trigfiles with same incident time and same event type
          HashMap<String, ArrayList<String>> indicentEventIndex =
                  DocumentIdentity.getIncidentTrigFileMapFromDocuments1(documentEventIndex, eckgMap, seckgMap, matchSettings, nJoined);
          System.out.println("Nr of incidents, indicentEventIndex.size() = " + indicentEventIndex.size());

          containerIncidents =
                  DocumentIdentity.getIndicentEventsWithStatements(
                              documentEventIndex,
                              indicentEventIndex,
                              eckgMap,
                              seckgMap);

          return containerIncidents;
    }

     static void createSystemConllFile (
                        HashMap<String, ArrayList<Statement>> eckgMap,
                        String pathToConllFile
                        ) {
           HashMap<String, String> tokenEventIdMap =  Util.getTokenEventMap(eckgMap, allEventKeys);
           File conllFile = new File (pathToConllFile);
           File conllResultFolder = conllFile.getParentFile();
           if (!conllResultFolder.exists()) conllResultFolder.mkdir();
           ConllOutputFromSem.resultForCoNLLFile(conllFile, allEventKeys, tokenEventIdMap);
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
