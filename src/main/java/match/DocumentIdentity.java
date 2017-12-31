package match;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.tdb.TDBFactory;
import objects.EventTypes;
import objects.Participants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static util.MD5Checksum.getMD5ChecksumFromString;

public class DocumentIdentity {



    /**
     * We get all particpants and locations from all events for a file and compare these with the others
     * @param documentEventIndex
     * @param eckgMap
     * @param seckgMap
     * @param matchSettings
     * @return
     */
    // @TODO We need to build some chaining here
    // @TODO We need to deal with empty location and participant data
    static public HashMap<String, ArrayList<String>> getIncidentEventMapFromDocuments (HashMap<String, ArrayList<String>> documentEventIndex,
                                                                                     HashMap<String, ArrayList<Statement>> eckgMap,
                                                                                     HashMap<String, ArrayList<Statement>> seckgMap,
                                                                                     MatchSettings matchSettings) {
        HashMap<String, ArrayList<String>> indicentEventIndex = new HashMap<>();
        ArrayList<String> processedFiles = new ArrayList<>();
        for (Map.Entry<String, ArrayList<String>> entry1 : documentEventIndex.entrySet()) {
            if (!processedFiles.contains(entry1.getKey())) {
                processedFiles.add(entry1.getKey());
                ArrayList<String> mergedFiles = new ArrayList<>();
                mergedFiles.add(entry1.getKey());
                //System.out.println("Key : " + entry1.getKey());
                ArrayList<String> fileEvents1 = documentEventIndex.get(entry1.getKey());
                ArrayList<String> entityparticipants1 = getEntityParticipants(fileEvents1, eckgMap, seckgMap);
                //System.out.println("entityparticipants1.toString() = " + entityparticipants1.toString());
                ArrayList<String> nonentityparticipants1 = getNonEntityParticipants(fileEvents1, eckgMap, seckgMap);
                //System.out.println("nonentityparticipants1.toString() = " + nonentityparticipants1.toString());
                ArrayList<String> locations1 = getLocations(fileEvents1, eckgMap, seckgMap);
                //System.out.println("locations1.size() = " + locations1.toString());
                for (Map.Entry<String, ArrayList<String>> entry2 : documentEventIndex.entrySet()) {
                    if (!processedFiles.contains(entry2.getKey())) {
                        ArrayList<String> fileEvents2 = documentEventIndex.get(entry2.getKey());
                        ArrayList<String> entityparticipants2 = getEntityParticipants(fileEvents2, eckgMap, seckgMap);
                        //System.out.println("entityparticipants2.toString() = " + entityparticipants2.toString());
                        ArrayList<String> locations2 = getLocations(fileEvents2, eckgMap, seckgMap);
                        //System.out.println("locations2.size() = " + locations2.toString());
                        ArrayList<String> mutualEntityParticipants = new ArrayList<>(entityparticipants1);
                        mutualEntityParticipants.retainAll(entityparticipants2);
                        ArrayList<String> mutualLocations = new ArrayList<>(locations1);
                        mutualLocations.retainAll(locations2);
                        /// We require both a participant and location match for the whole file
                        if (    mutualLocations.size() >= matchSettings.getTripleMatchThreshold()
                                &&
                                mutualEntityParticipants.size() >= matchSettings.getTripleMatchThreshold()
                            )
                        {
                            System.out.println("merging entry2.getKey() = " + entry2.getKey());
                            mergedFiles.add(entry2.getKey());
                            processedFiles.add(entry2.getKey());
                        }
                        else if (entityparticipants1.size()==0){
                            ArrayList<String> nonentityparticipants2 = getNonEntityParticipants(fileEvents2, eckgMap, seckgMap);
                            //System.out.println("nonentityparticipants2.toString() = " + nonentityparticipants2.toString());
                            ArrayList<String> mutualNonEntityParticipants = new ArrayList<>(nonentityparticipants1);
                            mutualNonEntityParticipants.retainAll(nonentityparticipants2);
                            if (    mutualLocations.size() >= matchSettings.getTripleMatchThreshold()
                                    &&
                                    mutualNonEntityParticipants.size() >= matchSettings.getTripleMatchThreshold()
                                )
                            {
                                System.out.println("merging entry2.getKey() = " + entry2.getKey());
                                mergedFiles.add(entry2.getKey());
                                processedFiles.add(entry2.getKey());
                            }
                        }
                    }
                }

                //System.out.println("processedFiles = " + processedFiles.toString());
                indicentEventIndex.put(entry1.getKey(), mergedFiles);
            }
            else {
                System.out.println("already merged entry1 = " + entry1);
            }
        }
        return indicentEventIndex;
    }


    /**
     * We now have a map from Incident to Events that belong to that incident
     * We check the event type
     * If it is INCIDENT or SHOOT, all events get the same instance ID
     * If it is one of the other types, we differentiate by the participants
     *
     * @param documentEventIndex
     * @param indicentEventIndex
     * @param eckgMap
     * @param seckgMap
     * @return
     */
    static public HashMap<String, ArrayList<Statement>> getIndicentEventsWithStatements (HashMap<String, ArrayList<String>> documentEventIndex,
                                                                                  HashMap<String, ArrayList<String>> indicentEventIndex,
                                                                                  HashMap<String, ArrayList<Statement>> eckgMap,
                                                                                  HashMap<String, ArrayList<Statement>> seckgMap) {

        HashMap<String, ArrayList<Statement>> mergedIncidentEventMap = new HashMap<>();
        for (Map.Entry<String, ArrayList<String>> entry : indicentEventIndex.entrySet()) {
            String incidentKey = entry.getKey()+"#incident";
            ArrayList<Statement> incidentStatements = new ArrayList<>();
            ArrayList<String> indidentFiles = entry.getValue();
            for (int i = 0; i < indidentFiles.size(); i++) {
                String incidentFile = indidentFiles.get(i);
                ArrayList<String> events = documentEventIndex.get(incidentFile);
                for (int j = 0; j < events.size(); j++) {
                    String event = events.get(j);
                    ArrayList<Statement> eventStatements = eckgMap.get(event);
                    String eventType = EventTypes.getEventType(event, eventStatements);

                    //// Instance level match
                    if (eventType.equals(EventTypes.INCIDENT) ||
                            eventType.equals((EventTypes.SHOOT))) {
                        incidentStatements.addAll(aggregateStatements(incidentKey, eventStatements ));
                    }
                    else {
                        /// Subtype level match
                        String participantKey = incidentKey + "#"+eventType+"#";
                        ArrayList<String> participants = Participants.getEntityParticipants(seckgMap, eventStatements);
                        if (participants.size()==0) {
                            participants  =Participants.getNonEntityParticipants(seckgMap, eventStatements);
                        }
                        if (participants.size()>0) {
                            Collections.sort(participants);
                           // System.out.println("participants.toString() = " + participants.toString());
                            try {
                                participantKey += getMD5ChecksumFromString(participants.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            //// this is the generic instance for this type without participant information UNK
                            participantKey += "UNK";
                        }
                        //System.out.println("participantKey = " + participantKey);
                        incidentStatements.addAll(aggregateStatements(participantKey, eventStatements ));
                    }
                }

            }
            mergedIncidentEventMap.put(incidentKey, incidentStatements);
        }
        return mergedIncidentEventMap;
    }


    static ArrayList<Statement> aggregateStatements (String incidentKey, ArrayList<Statement> statements) {
        Dataset ds = TDBFactory.createDataset();
        Model instanceModel =  ds.getNamedModel("instances");
        ArrayList<Statement> aggregation = new ArrayList<>();
        Resource aggregatedEvent = instanceModel.createResource(incidentKey);
        for (int k = 0; k < statements.size(); k++) {
            Statement statement = statements.get(k);
            Statement aggregatedStatement = instanceModel.createStatement(aggregatedEvent, statement.getPredicate(), statement.getObject());
            aggregation.add(aggregatedStatement);
        }
        return aggregation;
    }

    static ArrayList<String> getEntityParticipants (ArrayList<String> events,
                                              HashMap<String, ArrayList<Statement>> eckgMap,
                                              HashMap<String, ArrayList<Statement>> seckgMap) {
        ArrayList<String> participants = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            String event =  events.get(i);
            ArrayList<Statement> statements = eckgMap.get(event);
            participants.addAll(Participants.getEntityParticipants(seckgMap, statements));
        }
        return participants;
    }

    static ArrayList<String> getNonEntityParticipants (ArrayList<String> events,
                                              HashMap<String, ArrayList<Statement>> eckgMap,
                                              HashMap<String, ArrayList<Statement>> seckgMap) {
        ArrayList<String> participants = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            String event =  events.get(i);
            ArrayList<Statement> statements = eckgMap.get(event);
            participants.addAll(Participants.getNonEntityParticipants(seckgMap, statements));
        }
        return participants;
    }

    static ArrayList<String> getLocations (ArrayList<String> events,
                                           HashMap<String, ArrayList<Statement>> eckgMap,
                                           HashMap<String, ArrayList<Statement>> seckgMap)  {
        ArrayList<String> participants = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            String event =  events.get(i);
            ArrayList<Statement> statements = eckgMap.get(event);
            participants.addAll(SpatialReasoning.getLocations(seckgMap, statements));
        }
        return participants;
    }



}
