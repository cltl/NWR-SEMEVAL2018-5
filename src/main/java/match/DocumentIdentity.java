package match;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import objects.EventTypes;
import objects.Participants;
import objects.Sem;
import vu.cltl.triple.TrigUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static match.MatchUtil.matchingStatements;
import static match.MatchUtil.matchingStatementsByPrefLabel;
import static util.Util.getObjectPrefLabelString;

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
    static public HashMap<String, ArrayList<String>> getIncidentEventMapFromDocuments1 (HashMap<String, ArrayList<String>> documentEventIndex,
                                                                                     HashMap<String, ArrayList<Statement>> eckgMap,
                                                                                     HashMap<String, ArrayList<Statement>> seckgMap,
                                                                                     MatchSettings matchSettings) {
        HashMap<String, ArrayList<String>> indicentEventIndex = new HashMap<>();
        int nJoinedIncidentFiles = 0;
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
                        if (    (mutualLocations.size() >= matchSettings.getTripleMatchThreshold() )
                                &&
                                (mutualEntityParticipants.size() >= matchSettings.getTripleMatchThreshold() )
                            )
                        {   nJoinedIncidentFiles++;
                            mergedFiles.add(entry2.getKey());
                            processedFiles.add(entry2.getKey());
                        }
                        else if (entityparticipants1.size()==0){
                            ArrayList<String> nonentityparticipants2 = getNonEntityParticipants(fileEvents2, eckgMap, seckgMap);
                            //System.out.println("nonentityparticipants2.toString() = " + nonentityparticipants2.toString());
                            ArrayList<String> mutualNonEntityParticipants = new ArrayList<>(nonentityparticipants1);
                            mutualNonEntityParticipants.retainAll(nonentityparticipants2);
                            if (   (mutualLocations.size() >= matchSettings.getTripleMatchThreshold() )
                                    &&
                                    (mutualNonEntityParticipants.size() >= matchSettings.getTripleMatchThreshold())
                                )
                            {
                                nJoinedIncidentFiles++;
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
               // System.out.println("already merged entry1 = " + entry1);
            }
        }
        System.out.println("nJoinedIncidentFiles = " + nJoinedIncidentFiles);
        return indicentEventIndex;
    }

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
    static public HashMap<String, ArrayList<String>> getIncidentEventMapFromDocuments2 (HashMap<String, ArrayList<String>> documentEventIndex,
                                                                                     HashMap<String, ArrayList<Statement>> eckgMap,
                                                                                     HashMap<String, ArrayList<Statement>> seckgMap,
                                                                                     MatchSettings matchSettings) {
        HashMap<String, ArrayList<String>> indicentEventIndex = new HashMap<>();
        int nJoinedIncidentFiles = 0;
        ArrayList<String> processedFiles = new ArrayList<>();
        for (Map.Entry<String, ArrayList<String>> entry1 : documentEventIndex.entrySet()) {
            if (!processedFiles.contains(entry1.getKey())) {
                processedFiles.add(entry1.getKey());
                ArrayList<String> mergedFiles = new ArrayList<>();
                mergedFiles.add(entry1.getKey());
                //System.out.println("Key : " + entry1.getKey());
                ArrayList<String> fileEvents1 = documentEventIndex.get(entry1.getKey());
                ArrayList<Statement> directStatements1 = getStatements(fileEvents1, eckgMap);
                for (Map.Entry<String, ArrayList<String>> entry2 : documentEventIndex.entrySet()) {
                    if (!processedFiles.contains(entry2.getKey())) {
                        ArrayList<String> fileEvents2 = documentEventIndex.get(entry2.getKey());
                        ArrayList<Statement> directStatements2 = getStatements(fileEvents2, eckgMap);
                        ArrayList<Statement> matchingStatements = matchingStatements(directStatements1, directStatements2, matchSettings);
                        ArrayList<Statement> matchingPrefStatements = matchingStatementsByPrefLabel(seckgMap, directStatements1, directStatements2, matchSettings);
                        int matches = 0;
                        matches += matchingStatements.size();
                        matches += matchingPrefStatements.size();
                        if (matches >= matchSettings.getTripleMatchThreshold())  {
                            nJoinedIncidentFiles++;
                            mergedFiles.add(entry2.getKey());
                            processedFiles.add(entry2.getKey());
                        }
                    }
                }
                //System.out.println("processedFiles = " + processedFiles.toString());
                indicentEventIndex.put(entry1.getKey(), mergedFiles);
            }
            else {
               // System.out.println("already merged entry1 = " + entry1);
            }
        }
        System.out.println("nJoinedIncidentFiles = " + nJoinedIncidentFiles);
        return indicentEventIndex;
    }

    static ArrayList<Statement> getStatements (ArrayList<String> uris, HashMap<String, ArrayList<Statement>> map) {
        ArrayList<Statement> statements = new ArrayList<>();
        for (int i = 0; i < uris.size(); i++) {
            String uri = uris.get(i);
            if (map.containsKey(uri)) {
                ArrayList<Statement> uriStatements = map.get(uri);
                statements.addAll(uriStatements);
            }
        }
        return statements;
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

        ArrayList<String> eventArrayList = new ArrayList<>(); //// just for counting
        HashMap<String, ArrayList<Statement>> mergedIncidentEventMap = new HashMap<>();
        for (Map.Entry<String, ArrayList<String>> entry : indicentEventIndex.entrySet()) {
            String incidentKey = entry.getKey()+"#incident";
            ArrayList<Statement> incidentStatements = new ArrayList<>();
            ArrayList<String> indidentFiles = entry.getValue();
            /// we collect all events across all incidentFiles
            ArrayList<String> events = new ArrayList<>();
            for (int i = 0; i < indidentFiles.size(); i++) {
                String incidentFile = indidentFiles.get(i);
                events.addAll(documentEventIndex.get(incidentFile));
            }
            /// we get all locations and all participants
            ArrayList<Statement> allLocations = getLocationStatements(events, eckgMap, seckgMap);
            /// do we also need all time anchors???
            /// what do we do with conflicting locations and time anchors?

            //// we also get all participants from the complete file
            ArrayList<Statement> allParticipants = new ArrayList<>();
            ArrayList<Statement> allParticipantCandidates = getParticipantStatements(events, eckgMap, seckgMap);
            for (int k = 0; k < allParticipantCandidates.size(); k++) {
                Statement statement = allParticipantCandidates.get(k);
                //// we check if this is not a location
                if (!TrigUtil.hasStatement(allLocations, statement)) {
                    allParticipants.add(statement);
                }
            }

            ArrayList<Statement> killList = new ArrayList<>();
            ArrayList<Statement> hitList = new ArrayList<>();
            ArrayList<Statement> injureList = new ArrayList<>();
            /// we first collect all statements per event type (HIT, DEAD, INJURE) with their participants
            /// we assign these later to the ones without participants
            for (int j = 0; j < events.size(); j++) {
                String event = events.get(j);
                ArrayList<Statement> eventStatements = eckgMap.get(event);
                String eventType = EventTypes.getEventType(eventStatements);
                ArrayList<Statement> participants = new ArrayList<>();
                ArrayList<Statement> participantCandidates = Participants.getEntityParticipantStatements(seckgMap, eventStatements);
                for (int k = 0; k < participantCandidates.size(); k++) {
                    Statement statement = participantCandidates.get(k);
                    //// we check if this is not a location
                    if (!TrigUtil.hasStatement(allLocations, statement)) {
                        participants.add(statement);
                    }
                }
                participants.addAll(Participants.getNonEntityParticipantStatements(seckgMap, eventStatements));
                if (participants.size()>0) {
                   if (eventType.equals(EventTypes.DEAD)) {
                       killList.addAll(participants);
                   }
                   else if (eventType.equals(EventTypes.INJURED)) {
                      injureList.addAll(participants);
                   }
                   else if (eventType.equals(EventTypes.HIT)) {
                      hitList.addAll(participants);
                   }
                }
            }

            //// now we iterate again over the events and try to differentiate
            for (int j = 0; j < events.size(); j++) {
                String event = events.get(j);
                ArrayList<Statement> eventStatements = eckgMap.get(event);
                String eventType = EventTypes.getEventType(eventStatements);
                if (eventType.isEmpty()) {
                   // System.out.println("empty event:" + event);
                    continue;
                }
                //// Instance level match
                if (eventType.equals(EventTypes.INCIDENT) ||
                        eventType.equals((EventTypes.SHOOT))) {
                    eventArrayList.add(incidentKey);
                    incidentStatements.addAll(aggregateStatements(incidentKey, eventType, eventStatements, allParticipants, allLocations ));
                }
                else {

                    /// Subtype level match
                    String participantKey = incidentKey + "#"+eventType+"#";

                    /// We get the local participants for this event
                    ArrayList<Statement> participants = new ArrayList<>();
                    ArrayList<Statement> participantCandidates = Participants.getEntityParticipantStatements(seckgMap, eventStatements);
                    for (int k = 0; k < participantCandidates.size(); k++) {
                        Statement statement = participantCandidates.get(k);
                        //// we check if this is not a location
                       if (!TrigUtil.hasStatement(allLocations, statement)) {
                          participants.add(statement);
                       }
                    }
                    participants.addAll(Participants.getNonEntityParticipantStatements(seckgMap, eventStatements));

                    if (participants.size()==0) {

                        //// this is the generic instance for this type without participant information UNK
                        //participantKey += "UNK";
                        if (eventType.equals(EventTypes.DEAD)) {
                           participants = killList;
                        }
                        else if (eventType.equals(EventTypes.INJURED)) {
                           participants = injureList;
                        }
                        else if (eventType.equals(EventTypes.HIT)) {
                           participants = hitList;
                        }
                    }

                    /// if we have local participants, we use these to identify the event mention

                    participantKey += getObjectPrefLabelString(participants, seckgMap);
                    eventArrayList.add(participantKey);
                  //  System.out.println("participantKey = " + participantKey);
                    incidentStatements.addAll(aggregateStatements(participantKey, eventType, eventStatements, participants, allLocations ));

                }
            }
            mergedIncidentEventMap.put(incidentKey, incidentStatements);
        }
        Collections.sort(eventArrayList);
        System.out.println("eventArrayList = " + eventArrayList.size());
       // System.out.println(eventArrayList.toString());
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

    static ArrayList<Statement> aggregateStatements (String incidentKey, String eventType, ArrayList<Statement> statements, ArrayList<Statement> participants, ArrayList<Statement> locations) {
        Dataset ds = TDBFactory.createDataset();
        Model instanceModel =  ds.getNamedModel("instances");
        ArrayList<Statement> aggregation = new ArrayList<>();
        Resource aggregatedEvent = instanceModel.createResource(incidentKey);
        Resource eventTypeResource = instanceModel.createResource("http://www.newsreader-project.eu/ontologies/"+eventType);
        Statement typeStatement = instanceModel.createStatement(aggregatedEvent, RDF.type, eventTypeResource);
        aggregation.add(typeStatement);
        for (int i = 0; i < participants.size(); i++) {
            Statement participant = participants.get(i);
            Statement aggregatedStatement = instanceModel.createStatement(aggregatedEvent, Sem.hasActor, participant.getObject());
            aggregation.add(aggregatedStatement);
        }
        for (int i = 0; i < locations.size(); i++) {
            Statement location = locations.get(i);
            Statement aggregatedStatement = instanceModel.createStatement(aggregatedEvent, Sem.hasPlace, location.getObject());
            aggregation.add(aggregatedStatement);
        }
        for (int k = 0; k < statements.size(); k++) {
            Statement statement = statements.get(k);
            if (statement.getPredicate().getLocalName().equalsIgnoreCase("denotedBy") ||
                    statement.getPredicate().getLocalName().equalsIgnoreCase("label") ||
                    statement.getPredicate().getLocalName().equalsIgnoreCase("hasTime") ||
            statement.getPredicate().getLocalName().equalsIgnoreCase("prefLabel")) {
                Statement aggregatedStatement = instanceModel.createStatement(aggregatedEvent, statement.getPredicate(), statement.getObject());
                aggregation.add(aggregatedStatement);
            }
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

    static ArrayList<Statement> getEntityParticipantStatements (ArrayList<String> events,
                                              HashMap<String, ArrayList<Statement>> eckgMap,
                                              HashMap<String, ArrayList<Statement>> seckgMap) {
        ArrayList<Statement> participants = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            String event =  events.get(i);
            ArrayList<Statement> statements = eckgMap.get(event);
            participants.addAll(Participants.getEntityParticipantStatements(seckgMap, statements));
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

    static ArrayList<Statement> getNonEntityParticipantStatements (ArrayList<String> events,
                                              HashMap<String, ArrayList<Statement>> eckgMap,
                                              HashMap<String, ArrayList<Statement>> seckgMap) {
        ArrayList<Statement> participants = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            String event =  events.get(i);
            ArrayList<Statement> statements = eckgMap.get(event);
            participants.addAll(Participants.getNonEntityParticipantStatements(seckgMap, statements));
        }
        return participants;
    }


    static ArrayList<Statement> getParticipantStatements (ArrayList<String> events,
                                              HashMap<String, ArrayList<Statement>> eckgMap,
                                              HashMap<String, ArrayList<Statement>> seckgMap)  {
           ArrayList<Statement> participants = new ArrayList<>();
           for (int i = 0; i < events.size(); i++) {
               String event =  events.get(i);
               ArrayList<Statement> statements = eckgMap.get(event);
               participants.addAll(Participants.getParticipantStatements(seckgMap, statements));
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

    static ArrayList<Statement> getLocationStatements (ArrayList<String> events,
                                           HashMap<String, ArrayList<Statement>> eckgMap,
                                           HashMap<String, ArrayList<Statement>> seckgMap)  {
        ArrayList<Statement> participants = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            String event =  events.get(i);
            ArrayList<Statement> statements = eckgMap.get(event);
            participants.addAll(SpatialReasoning.getLocationStatements(seckgMap, statements));
        }
        return participants;
    }



}
