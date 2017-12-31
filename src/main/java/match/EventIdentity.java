package match;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.tdb.TDBFactory;

import java.util.ArrayList;
import java.util.HashMap;

import static match.MatchUtil.matchingStatements;
import static match.MatchUtil.matchingStatementsByPrefLabel;
import static objects.EventTypes.getEventType;

/**
 * Created by piek on 12/11/2017.
 */
public class EventIdentity {


    static public HashMap<String, ArrayList<Statement>> lookForSimilarEvents (ArrayList<String> domainEvents,
            HashMap<String, ArrayList<Statement>> eckgMap,
            HashMap<String, ArrayList<Statement>> seckgMap,
            MatchSettings matchSettings) {
        Dataset ds = TDBFactory.createDataset();
        Model instanceModel =  ds.getNamedModel("instances");
        HashMap<String, ArrayList<Statement>> mergedEvents = new HashMap<>();
        ArrayList<String> skipEvents = new ArrayList<>();
        for (int i = 0; i < domainEvents.size(); i++) {
            String key1 = domainEvents.get(i);
            //boolean merge = false;
            if (!skipEvents.contains(key1)) {
                ArrayList<Statement> directStatements1 = eckgMap.get(key1);
                String key1Type = getEventType(key1, directStatements1 );
                for (int j = i+1; j < domainEvents.size(); j++) {
                    String key2 = domainEvents.get(j);
                    if (!skipEvents.contains(key2)) {
                        ArrayList<Statement> directStatements2 = eckgMap.get(key2);
                        String key2Type = getEventType(key2, directStatements2 );
                        if (!key1Type.isEmpty() && key1Type.equals(key2Type)) {
                            ArrayList<Statement> matchingStatements = matchingStatements(directStatements1, directStatements2, matchSettings);
                            ArrayList<Statement> matchingPrefStatements = matchingStatementsByPrefLabel(seckgMap, directStatements1, directStatements2, matchSettings);
                            int matches = 0;
                            matches += matchingStatements.size();
                            matches += matchingPrefStatements.size();
                            if (matches >= matchSettings.getTripleMatchThreshold()
                                    ) {
                                ////identify
                                //merge = true;
                                //System.out.println("matchingStatements.size() = " + matchingStatements.size());
                                for (int m = 0; m < matchingStatements.size(); m++) {
                                    Statement statement = matchingStatements.get(m);
                                    // System.out.println(statement.getPredicate().getLocalName()+":" + TrigUtil.getValue(statement.getObject().toString()));
                                }
                                skipEvents.add(key2);
                                //System.out.println("skipEvents = " + skipEvents.size());
                                for (int k = 0; k < directStatements2.size(); k++) {
                                    Statement statement2 = directStatements2.get(k);
                                    Statement statement = instanceModel.createStatement(directStatements1.get(0).getSubject(), statement2.getPredicate(), statement2.getObject());
                                    directStatements1.add(statement);
                                }
                            }
                        }
                        else {
                            /// Event type mismatch so we skip this one....
                        }
                    }
                }
                mergedEvents.put(key1, directStatements1);
               // System.out.println("mergedEvents = " + mergedEvents.size());
            }
        }
        return mergedEvents;
    }





}
