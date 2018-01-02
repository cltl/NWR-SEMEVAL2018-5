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
public class Task5EventCorefVersion2 {

    static ArrayList<String> allEventKeys = new ArrayList<>();

    static String testParameters = "--trig-files /Users/piek/Desktop/SemEval2018/trial_data_final/NAFDONE.ALL " +
            "--conll-file /Users/piek/Desktop/SemEval2018/trial_data_final/input/s3/docs.conll " +
            "--event-file /Users/piek/Desktop/SemEval2018/scripts/trial_vocabulary " +
            "--cities /Users/piek/Desktop/SemEval2018/scripts/cities.rel " +
            "--states /Users/piek/Desktop/SemEval2018/scripts/states.rel";

    static public void main(String[] args) {
        String pathToTrigFiles = "";
        String pathToConllFile = "";
        String eventFile = "";
        String cityLex = "";
        String stateLex = "";
        MatchSettings matchSettings = new MatchSettings();
        if (args.length==0) args = testParameters.split(" ");
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--trig-files") && args.length>(i+1)) {
                pathToTrigFiles = args[i+1];
            }
            else if (arg.equals("--conll-file") && args.length>(i+1)) {
                pathToConllFile = args[i+1];
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
        }
        matchSettings.parseArguments(args);
        matchSettings.setLoose();
       // matchSettings.setMatchAny(true);
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
        File eckgFolder = new File (taskFileFolder.getAbsolutePath()+"/"+"eckg");
        if (!eckgFolder.exists()) eckgFolder.mkdir();


        /// our first approach is event driven. Since we can expect one incident per source document, we probably can better
        /// use the document as a starting point:
        // 1. group documents per temporal container
        // 2. compare documents for incident identity
        // 3. lump incident level events and shoot events across all identical documents
        // 4. link hit, kill and injure events to participants within and across all documents

        /// We could create temporal containers from the questions or from the document creation time.

        /// STEP 1
        /// process all trig files and build temporal containers using the document creation time
        File trigFolder = new File (pathToTrigFiles);
        ArrayList<File> trigFiles = Util.makeRecursiveFileList(trigFolder, ".coref.trig");
        System.out.println("trigFiles.size() = " + trigFiles.size());
        HashMap<String,ArrayList<File>> temporalContainers = TemporalReasoning.getTemporalContainersWithTrigFiles(trigFiles);
        System.out.println("temporalContainers.size() = " + temporalContainers.size());
        Set containerSet = temporalContainers.keySet();
        Iterator<String> containerKeys = containerSet.iterator();
        HashMap<String, ArrayList<Statement>> finalEvents = new HashMap<>();

        while (containerKeys.hasNext()) {
            String containerKey = containerKeys.next();

           // HashMap<String, ArrayList<Statement>> eckgMap = new HashMap<>();
            HashMap<String, ArrayList<Statement>> seckgMap = new HashMap<>();
            ArrayList<File> timeTrigFiles = temporalContainers.get(containerKey);
            System.out.println("containerKey = " + containerKey+": "+timeTrigFiles.size()+" source files");

            HashMap<String, ArrayList<Statement>> containerIncidents = getContainerEvents(timeTrigFiles, seckgMap, eventVocabulary, matchSettings);

            finalEvents.putAll(containerIncidents);
            //// Dump the ECKGs
            try {
              OutputStream fos3 = new FileOutputStream(eckgFolder.getAbsoluteFile()+"/"+containerKey+".trig");
              Dataset dataset = TDBFactory.createDataset();
              TrigUtil.prefixDefaultModels(dataset);
              TrigUtil.addStatementsToJenaData(dataset, containerIncidents);
              TrigUtil.addStatementsToJenaData(dataset, seckgMap);
              RDFDataMgr.write(fos3, dataset.getDefaultModel(), RDFFormat.TRIG_PRETTY);
              fos3.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
        }

        /// STEP 5 CREATE THE CONLL FILE
        createSystemConllFile(finalEvents, pathToConllFile);
    }


    static HashMap<String, ArrayList<Statement>> getContainerEvents (ArrayList<File> containerTrigFiles,
                                                                     HashMap<String, ArrayList<Statement>> seckgMap,
                                                                     HashMap<String, String> eventVocabulary,
                                                                     MatchSettings matchSettings) {
        /// now we need to deal with all the events in these trigfiles
          /// get locations
          /// get participants
          /// compare trigfiles for location and participant match
          /// if so merge events mentions accordingly but make sure hit, kill and injury are participant sensitive

          /// we get all triples from these trigfiles that share the same temporal container

          HashMap<String, ArrayList<Statement>> containerIncidents = new HashMap<>();
          HashMap<String, ArrayList<Statement>> eckgMap = new HashMap<>();
          HashMap<String, ArrayList<String>> documentEventIndex = new HashMap<>();

          for (int i = 0; i < containerTrigFiles.size(); i++) {
              File timeTrigFile = containerTrigFiles.get(i);
              vu.cltl.triple.TrigTripleData trigTripleData = vu.cltl.triple.TrigTripleReader.readTripleFromTrigFile(timeTrigFile);

              /// from the complete graph we extract all events that match the domain constraints
              ArrayList<String> domainEvents = EventTypes.getDomainEventSubjectUris(trigTripleData.tripleMapInstances, eventVocabulary);
              documentEventIndex.put(timeTrigFile.getName(), domainEvents);

              TrigUtil.addPrimaryKnowledgeGraphHashMap(domainEvents, eckgMap, trigTripleData);
              TrigUtil.addSecondaryKnowledgeGraphHashMap(domainEvents, seckgMap, trigTripleData);
              //System.out.println("domainEvents = " + domainEvents.size());
          }

          System.out.println("eckgMap = " + eckgMap.size());
          System.out.println("seckgMap = " + seckgMap.size());

          /// we need to build some similarity function that compares the events across the trigfiles with the same DCT

          HashMap<String, ArrayList<String>> indicentEventIndex =
                  DocumentIdentity.getIncidentEventMapFromDocuments1(documentEventIndex, eckgMap, seckgMap, matchSettings );

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
