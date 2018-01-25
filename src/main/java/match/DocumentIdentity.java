package match;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import objects.EventTypes;
import objects.Participants;
import objects.Sem;
import vu.cltl.triple.TrigUtil;
import vu.cltl.triple.objects.ResourcesUri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static util.Util.getObjectPrefLabelString;

public class DocumentIdentity {
    /**
     * We get all particpants and locations from all events for a file and compare these with the others
     * This version compares locations, entity-participants and, if there are no entity-participants, non-entity-participants.
     *
     * If the number of both mutual locations and mutual entity-participants is above the triple threshold, we have a match
     * If there are no entity-participants in event 1, we also accept mutual locations and mutual nonentity-participants above threshold
     *
     * @param documentEventIndex
     * @param eckgMap
     * @param seckgMap
     * @param matchSettings
     * @return
     */
    // @TODO We need to build some chaining here
    // @TODO We need to deal with empty location and participant data
    static public HashMap<String, ArrayList<String>> getIncidentTrigFileMapFromDocuments1(HashMap<String, ArrayList<String>> documentEventIndex,
                                                                                          HashMap<String, ArrayList<Statement>> eckgMap,
                                                                                          HashMap<String, ArrayList<Statement>> seckgMap,
                                                                                          MatchSettings matchSettings, int nJoined) {
        HashMap<String, ArrayList<String>> indicentTrigFileIndex = new HashMap<>();
        int nJoinedIncidentFiles = 0;
        ArrayList<String> processedFiles = new ArrayList<>();
        /// documentEventIndex is the map of trigFiles with list of event uris from the domain
        for (Map.Entry<String, ArrayList<String>> trigFile1 : documentEventIndex.entrySet()) {
            if (!processedFiles.contains(trigFile1.getKey())) {
                processedFiles.add(trigFile1.getKey());
                ArrayList<String> mergedFiles = new ArrayList<>();
                mergedFiles.add(trigFile1.getKey());
                //System.out.println("Key : " + entry1.getKey());
                ArrayList<String> fileEvents1 = documentEventIndex.get(trigFile1.getKey());
                ArrayList<String> entityparticipants1 = getEntityParticipants(fileEvents1, eckgMap, seckgMap);
                //System.out.println("entityparticipants1.toString() = " + entityparticipants1.toString());
                ArrayList<String> nonentityparticipants1 = getNonEntityParticipants(fileEvents1, eckgMap, seckgMap);
                //System.out.println("nonentityparticipants1.toString() = " + nonentityparticipants1.toString());
                ArrayList<String> locations1 = getLocations(fileEvents1, eckgMap, seckgMap);
                //System.out.println("locations1.size() = " + locations1.toString());
                for (Map.Entry<String, ArrayList<String>> trigFile2 : documentEventIndex.entrySet()) {
                    if (!processedFiles.contains(trigFile2.getKey())) {
                        ArrayList<String> fileEvents2 = documentEventIndex.get(trigFile2.getKey());
                        ArrayList<String> entityparticipants2 = getEntityParticipants(fileEvents2, eckgMap, seckgMap);
                        ArrayList<String> mutualEntityParticipants = new ArrayList<>(entityparticipants1);
                        mutualEntityParticipants.retainAll(entityparticipants2);
                        ArrayList<String> locations2 = getLocations(fileEvents2, eckgMap, seckgMap);
                        ArrayList<String> mutualLocations = new ArrayList<>(locations1);
                        mutualLocations.retainAll(locations2);
                        //System.out.println("locations2.size() = " + locations2.toString());
                        /// We require both a participant and location match for the whole file
                        if (    (mutualLocations.size() >= matchSettings.getTripleMatchThreshold() )
                                &&
                                (mutualEntityParticipants.size() >= matchSettings.getTripleMatchThreshold() )
                            )
                        {   nJoinedIncidentFiles++;
                            mergedFiles.add(trigFile2.getKey());
                            processedFiles.add(trigFile2.getKey());
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
                                mergedFiles.add(trigFile2.getKey());
                                processedFiles.add(trigFile2.getKey());
                            }
                        }
                    }
                }
                //System.out.println("processedFiles = " + processedFiles.size());
                indicentTrigFileIndex.put(trigFile1.getKey(), mergedFiles);
            }
            else {
               // System.out.println("already merged entry1 = " + entry1);
            }
        }
        System.out.println("nJoinedIncidentFiles = " + nJoinedIncidentFiles);
        nJoined +=nJoinedIncidentFiles;
        return indicentTrigFileIndex;
    }

    /**
     * We get all particpants and locations from all events for a file and compare these with the others
     * This version does not differentiate the statements
     */
    // @TODO We need to build some chaining here
    // @TODO We need to deal with empty location and participant data
 /*   static public HashMap<String, ArrayList<String>> getIncidentEventMapFromDocuments2 (HashMap<String, ArrayList<String>> documentEventIndex,
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
*/

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
            ArrayList<Statement> burnList = new ArrayList<>();
            ArrayList<Statement> dismissList = new ArrayList<>();
            /// we first collect all statements per event type (HIT, DEAD, INJURE, BURN, DISMISS) with their participants
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
                   else if (eventType.equals(EventTypes.BURN)) {
                      burnList.addAll(participants);
                   }
                   else if (eventType.equals(EventTypes.DISMISS)) {
                      dismissList.addAll(participants);
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
                        eventType.equals(EventTypes.SHOOT) ||
                        eventType.equals(EventTypes.BURN) ||
                        eventType.equals(EventTypes.DISMISS)) {
                    eventArrayList.add(incidentKey);
                    Resource incident = ResourceFactory.createResource(incidentKey);
                    incidentStatements.addAll(aggregateStatements(incident, eventStatements, allParticipants, allLocations ));
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
                    Resource participant = ResourceFactory.createResource(participantKey);
                    incidentStatements.addAll(aggregateStatements(participant, eventStatements, participants, allLocations ));

                }
            }
            mergedIncidentEventMap.put(incidentKey, incidentStatements);
        }
        Collections.sort(eventArrayList);
        System.out.println("eventArrayList = " + eventArrayList.size());
       // System.out.println(eventArrayList.toString());
        return mergedIncidentEventMap;
    }

    /**
     * We now have a map from Incident to Events that belong to that incident
     * We check the event type
     * If it is INCIDENT or SHOOT, all events get the same instance ID
     * If it is one of the other types, we differentiate by the participants
     *
     * @param documentEventIndex
     * @param indicentTrigFileIndex
     * @param eckgMap
     * @param seckgMap
     * @return
     */
    static public HashMap<String, ArrayList<Statement>> getLoadedIndicentAndSubeventsEventsWithStatements (String taskEventSubType, HashMap<String, ArrayList<String>> documentEventIndex,
                                                                                  HashMap<String, ArrayList<String>> indicentTrigFileIndex,
                                                                                  HashMap<String, ArrayList<Statement>> eckgMap,
                                                                                  HashMap<String, ArrayList<Statement>> seckgMap) {


        Resource taskEventTypeResource =  ResourceFactory.createResource(ResourcesUri.nwrontology+taskEventSubType);
        /// indicentTrigFileIndex is the index from all incidents detected to all trig files that contain the events related to this incident
        /// from the trigFiles, we get the incident events that are all coreferential and all subevents for which we need to establish identity within the incident but across all trigfiles
        ArrayList<String> eventArrayList = new ArrayList<>(); //// just for counting
        HashMap<String, ArrayList<Statement>> mergedIncidentEventMap = new HashMap<>();
        for (Map.Entry<String, ArrayList<String>> entry : indicentTrigFileIndex.entrySet()) {
            /// Setting the basics
            String incidentKey = entry.getKey()+"#incident";
            ArrayList<Statement> incidentStatements = new ArrayList<>();

            Resource incidentEventResource = ResourceFactory.createResource(incidentKey);
            //// add the overall type to the incident
            Statement typeStatement = ResourceFactory.createStatement(incidentEventResource, RDF.type, taskEventTypeResource);
            incidentStatements.add(typeStatement);

            /// We obtain all trigfiles for this incident
            ArrayList<String> incidentTrigFiles = entry.getValue();
            /// we collect all events across all incident TrigFiles
            ArrayList<String> events = new ArrayList<>();
            for (int i = 0; i < incidentTrigFiles.size(); i++) {
                String incidentTrigFile = incidentTrigFiles.get(i);
                events.addAll(documentEventIndex.get(incidentTrigFile));
            }
            /// we get aggregate locations from these events
            ArrayList<Statement> allLocations = getLocationStatements(events, eckgMap, seckgMap);
            //// we also aggregate all participants from all events from all files, where we filter for locations
            ArrayList<Statement> allParticipants = new ArrayList<>();
            ArrayList<Statement> allParticipantCandidates = getParticipantStatements(events, eckgMap, seckgMap);
            for (int k = 0; k < allParticipantCandidates.size(); k++) {
                Statement statement = allParticipantCandidates.get(k);
                //// we check if this is not also a location
                if (!TrigUtil.hasStatement(allLocations, statement)) {
                    allParticipants.add(statement);
                }
            }

            ArrayList<Statement> killList = new ArrayList<>();
            ArrayList<Statement> hitList = new ArrayList<>();
            ArrayList<Statement> injureList = new ArrayList<>();
            /// we first collect all statements per event type (HIT, DEAD, INJURE, BURN, DISMISS) with their participants
            /// we assign these later to the ones without participants
            /// this is a generalisation for event mentions that are unspecified or vague with respect to their participants
            for (int j = 0; j < events.size(); j++) {
                String event = events.get(j);

                //// we get the local properties for an event (e.g. killing can be more specific as it is a subevent)
                ArrayList<Statement> eventStatements = eckgMap.get(event);
                String eventType = EventTypes.getEventType(eventStatements); /// this is the local event type
                ArrayList<Statement> subeventParticipants = new ArrayList<>();
                ArrayList<Statement> subeventParticipantCandidates = Participants.getEntityParticipantStatements(seckgMap, eventStatements);
                for (int k = 0; k < subeventParticipantCandidates.size(); k++) {
                    Statement statement = subeventParticipantCandidates.get(k);
                    //// we check if this is not a location
                    if (!TrigUtil.hasStatement(allLocations, statement)) {
                        subeventParticipants.add(statement);
                    }
                }
                subeventParticipants.addAll(Participants.getNonEntityParticipantStatements(seckgMap, eventStatements));
                if (subeventParticipants.size()>0) {
                   if (eventType.equals(EventTypes.DEAD)) {
                       killList.addAll(subeventParticipants);
                   }
                   else if (eventType.equals(EventTypes.INJURED)) {
                      injureList.addAll(subeventParticipants);
                   }
                   else if (eventType.equals(EventTypes.HIT)) {
                      hitList.addAll(subeventParticipants);
                   }
                }
            }

            //// now we iterate again over the events and try to differentiate subevents,
            //   while aggregating data for the incident
            //// events are typed according to the global type and subevent types
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
                        eventType.equals(EventTypes.SHOOT) ||
                        eventType.equals(EventTypes.BURN) ||
                        eventType.equals(EventTypes.DISMISS)) {
                    eventArrayList.add(incidentKey); // for reporting
                    //// all properties are aggregated
                    incidentStatements.addAll(aggregateStatements(incidentEventResource, eventStatements, allParticipants, allLocations ));
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
                    /// we add all other nonentity participants
                    participants.addAll(Participants.getNonEntityParticipantStatements(seckgMap, eventStatements));

                    ///  if there are no specific subEvent participants, we assume it is a generic or vague mention and simply assign all known
                    if (participants.size()==0) {
                        //// this is the generic and underspecified instance for this type
                        // without participant information (UNK)
                        // we assume that all subevent participants apply
                        if (eventType.equals(EventTypes.DEAD)) {
                           participants = killList;
                        }
                        else if (eventType.equals(EventTypes.INJURED)) {
                           participants = injureList;
                        }
                        else if (eventType.equals(EventTypes.HIT)) {
                           participants = hitList;
                        }
                        else {
                            //// we forget the list as they are assigned at the top level
                        }
                    }

                    /// if we have local participants, we differentiate the identity of the subevent mentions
                    participantKey += getObjectPrefLabelString(participants, seckgMap);
                    eventArrayList.add(participantKey); // for reporting
                    
                    Resource subEventResource =  ResourceFactory.createResource(participantKey);
                    Resource subEventTypeResource =  ResourceFactory.createResource(ResourcesUri.nwrontology+eventType);

                    /// we add all properties specific for the subevents
                    incidentStatements.addAll(aggregateStatements(subEventResource, eventStatements, participants, allLocations ));

                    /// we add the type info to the subevent
                    Statement subEventTypeStatement = ResourceFactory.createStatement(subEventResource, RDF.type, subEventTypeResource);
                    incidentStatements.add(subEventTypeStatement);

                    /// we  add subtype the type of subevent to the incident for easy reasoning later on
                    subEventTypeStatement = ResourceFactory.createStatement(incidentEventResource, Sem.hasSubType, subEventTypeResource);
                    incidentStatements.add(subEventTypeStatement);

                    /// we add the subevent itself as a meronym to the incident
                    Statement subEventStatement = ResourceFactory.createStatement(incidentEventResource, Sem.hasSubEvent, subEventResource);
                    incidentStatements.add(subEventStatement);

                }
            }
            mergedIncidentEventMap.put(incidentKey, incidentStatements);
        }
        /// reporting
            Collections.sort(eventArrayList);
            System.out.println("eventArrayList = " + eventArrayList.size());
            // System.out.println(eventArrayList.toString());
        return mergedIncidentEventMap;
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
    static public HashMap<String, ArrayList<Statement>> getIndicentEventsWithStatements (String taskSubType, HashMap<String, ArrayList<String>> documentEventIndex,
                                                                                  HashMap<String, ArrayList<String>> indicentEventIndex,
                                                                                  HashMap<String, ArrayList<Statement>> eckgMap,
                                                                                  HashMap<String, ArrayList<Statement>> seckgMap) {

        ArrayList<String> eventArrayList = new ArrayList<>(); //// just for counting
        HashMap<String, ArrayList<Statement>> mergedIncidentEventMap = new HashMap<>();
        for (Map.Entry<String, ArrayList<String>> entry : indicentEventIndex.entrySet()) {
            String incidentKey = entry.getKey()+"#incident"+"#"+taskSubType;
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
            ArrayList<Statement> burnList = new ArrayList<>();
            ArrayList<Statement> dismissList = new ArrayList<>();
            /// we first collect all statements per event type (HIT, DEAD, INJURE, BURN, DISMISS) with their participants
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
                   else if (eventType.equals(EventTypes.BURN)) {
                      burnList.addAll(participants);
                   }
                   else if (eventType.equals(EventTypes.DISMISS)) {
                      dismissList.addAll(participants);
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
                        eventType.equals(EventTypes.SHOOT) ||
                        eventType.equals(EventTypes.BURN) ||
                        eventType.equals(EventTypes.DISMISS)) {
                    eventArrayList.add(incidentKey);
                    Resource incident = ResourceFactory.createResource(incidentKey);
                    incidentStatements.addAll(aggregateStatements(incident, eventStatements, allParticipants, allLocations ));
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
                    Resource participant = ResourceFactory.createResource(participantKey);
                    incidentStatements.addAll(aggregateStatements(participant, eventStatements, participants, allLocations ));

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
        ArrayList<Statement> aggregation = new ArrayList<>();
        Resource aggregatedEvent = ResourceFactory.createResource(incidentKey);
        for (int k = 0; k < statements.size(); k++) {
            Statement statement = statements.get(k);
            Statement aggregatedStatement = ResourceFactory.createStatement(aggregatedEvent, statement.getPredicate(), statement.getObject());
            aggregation.add(aggregatedStatement);
        }
        return aggregation;
    }

    static Statement createTypeStatement (String incidentKey, Resource eventTypeResource) {
        Resource aggregatedEvent = ResourceFactory.createResource(incidentKey);
        Statement typeStatement = ResourceFactory.createStatement(aggregatedEvent, RDF.type, eventTypeResource);
        return typeStatement;
    }

    static ArrayList<Statement> aggregateStatements (Resource aggregatedEvent, ArrayList<Statement> statements, ArrayList<Statement> participants, ArrayList<Statement> locations) {
       ArrayList<Statement> aggregation = new ArrayList<>();
        for (int i = 0; i < participants.size(); i++) {
            Statement participant = participants.get(i);
            Statement aggregatedStatement = ResourceFactory.createStatement(aggregatedEvent, Sem.hasActor, participant.getObject());
            aggregation.add(aggregatedStatement);
        }
        for (int i = 0; i < locations.size(); i++) {
            Statement location = locations.get(i);
            Statement aggregatedStatement = ResourceFactory.createStatement(aggregatedEvent, Sem.hasPlace, location.getObject());
            aggregation.add(aggregatedStatement);
        }
        for (int k = 0; k < statements.size(); k++) {
            Statement statement = statements.get(k);
            if     (statement.getPredicate().getLocalName().equalsIgnoreCase("denotedBy") ||
                    statement.getPredicate().getLocalName().equalsIgnoreCase("label") ||
                   // statement.getPredicate().getLocalName().equalsIgnoreCase("type") ||
                    statement.getPredicate().getLocalName().equalsIgnoreCase("hasTime") ||
                    statement.getPredicate().getLocalName().equalsIgnoreCase("prefLabel")) {
                Statement aggregatedStatement = ResourceFactory.createStatement(aggregatedEvent, statement.getPredicate(), statement.getObject());
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
