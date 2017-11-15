package match;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.tdb.TDBFactory;
import vu.cltl.triple.TrigUtil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by piek on 12/11/2017.
 */
public class EventIdentity {


    static public HashMap<String, ArrayList<Statement>> lookForSimilarEvents (ArrayList<String> domainEvents,
            HashMap<String, ArrayList<Statement>> eckgMap,
            HashMap<String, ArrayList<Statement>> seckgMap,
            MatchSettings matchSettings) {
        Dataset ds = ds = TDBFactory.createDataset();
        Model instanceModel =  ds.getNamedModel("instances");
        HashMap<String, ArrayList<Statement>> mergedEvents = new HashMap<>();
        ArrayList<String> skipEvents = new ArrayList<>();
        for (int i = 0; i < domainEvents.size(); i++) {
            String key1 = domainEvents.get(i);
            //boolean merge = false;
            if (!skipEvents.contains(key1)) {
                ArrayList<Statement> directStatements1 = eckgMap.get(key1);
                for (int j = i+1; j < domainEvents.size(); j++) {
                    String key2 = domainEvents.get(j);
                    if (!skipEvents.contains(key2)) {
                        ArrayList<Statement> directStatements2 = eckgMap.get(key2);
                        ArrayList<Statement> matchingStatements = matchingStatements(directStatements1, directStatements2);
                        if (matchingStatements.size() >= matchSettings.getTripleMatchThreshold()) {
                            ////identify
                            //merge = true;
                            //System.out.println("matchingStatements.size() = " + matchingStatements.size());
                            for (int m = 0; m < matchingStatements.size(); m++) {
                                Statement statement = matchingStatements.get(m);
                                System.out.println(statement.getPredicate().getLocalName()+":" + TrigUtil.getValue(statement.getObject().toString()));
                            }
                            skipEvents.add(key2);
                            System.out.println("skipEvents = " + skipEvents.size());
                            for (int k = 0; k < directStatements2.size(); k++) {
                                Statement statement2 = directStatements2.get(k);
                                Statement statement = instanceModel.createStatement(directStatements1.get(0).getSubject(), statement2.getPredicate(), statement2.getObject());
                                directStatements1.add(statement);
                            }
                        }
                    }

                }
                mergedEvents.put(key1, directStatements1);
                System.out.println("mergedEvents = " + mergedEvents.size());
            }
        }
        return mergedEvents;
    }

    static public HashMap<String, ArrayList<Statement>> lookForSimilarEvents (
            HashMap<String, ArrayList<Statement>> eckgMap,
            HashMap<String, ArrayList<Statement>> seckgMap,
            MatchSettings matchSettings) {

        HashMap<String, ArrayList<Statement>> mergedEvents = new HashMap<>();
        ArrayList<String> skipEvents = new ArrayList<>();
        for (HashMap.Entry<String, ArrayList<Statement>> entry : eckgMap.entrySet()) {
            String key1 = entry.getKey();
            if (!skipEvents.contains(key1)) {
                ArrayList<Statement> directStatements1 = entry.getValue();
                for (HashMap.Entry<String, ArrayList<Statement>> entry2 : eckgMap.entrySet()) {
                    String key2 = entry2.getKey();
                    if (!key1.equals(key2) && !skipEvents.contains(key2)) {
                        ArrayList<Statement> directStatements2 = entry2.getValue();
                        ArrayList<Statement> matchingStatements = matchingStatements(directStatements1, directStatements2);
                        if (matchingStatements.size() > matchSettings.getTripleMatchThreshold()) {
                            ////identify
                            System.out.println("matchingStatements.size() = " + matchingStatements.size());
                            for (int i = 0; i < matchingStatements.size(); i++) {
                                Statement statement = matchingStatements.get(i);
                                System.out.println(statement.getPredicate().getLocalName()+":" + TrigUtil.getValue(statement.getObject().toString()));
                            }
                            skipEvents.add(key2);
                            System.out.println("skipEvents = " + skipEvents.size());
                            ArrayList<Statement> newStatements = getNewStatements(directStatements1, directStatements2);
                            directStatements1.addAll(newStatements);
                            System.out.println("directStatements1.size() = " + directStatements1.size());
                            /*for (int i = 0; i < directStatements2.size(); i++) {
                                Statement statement = directStatements2.get(i);
                                if (statement.getPredicate().getLocalName().equals("denotedBy")) {
                                    directStatements1.add(statement);
                                    System.out.println("statement.toString() = " + statement.toString());
                                }
                            }*/
                        }
                    }

                }
                mergedEvents.put(key1, directStatements1);
                System.out.println("mergedEvents = " + mergedEvents.size());
            }
        }
        return mergedEvents;
    }

    static boolean matchStatements (Statement stat1, Statement stat2, MatchSettings matchSettings) {
        if (stat1.getObject().toString().equals(stat2.getObject().toString())) {
            if (!matchSettings.matchSemPlace() && (stat1.getPredicate().getLocalName().equals("hasPlace"))) {
                    return false;
            }
            else if (!matchSettings.matchDbpActor() && stat1.getObject().toString().indexOf("dbpedia")>-1) {
                return false;
            }
            else {
                return true;
            }
        }
        return false;
    }

    static ArrayList<Statement> matchingStatements (ArrayList<Statement>statements1, ArrayList<Statement> statements2) {
        ArrayList<Statement> matchingStatements = new ArrayList<>();
        for (int i = 0; i < statements1.size(); i++) {
            Statement statement1 = statements1.get(i);
            if (entityParticipant(statement1)) {
                for (int j = 0; j < statements2.size(); j++) {
                    Statement statement2 = statements2.get(j);
                    if (entityParticipant(statement2)) {
                        if (statement1.getObject().toString().equals(statement2.getObject().toString())) {
                            TrigUtil.addNewStatement(matchingStatements, statement1);
                        }
                    }
                }
            }
        }
        return matchingStatements;
    }

    static ArrayList<Statement> getNewStatements (ArrayList<Statement>statements2, ArrayList<Statement> statements1) {
        ArrayList<Statement> newStatements = new ArrayList<>();
        for (int i = 0; i < statements1.size(); i++) {
            Statement statement1 = statements1.get(i);
            boolean match = false;
            for (int j = 0; j < statements2.size(); j++) {
                Statement statement2 = statements2.get(j);
                if (statement1.toString().equals(statement2.toString())) {
                    match = true;
                    break;
                }
            }
            if (!match) newStatements.add(statement1);
        }
        return newStatements;
    }

    static boolean entityParticipant (Statement statement) {
        if ((statement.getPredicate().getLocalName().equals("A0"))
                        ||
                (statement.getPredicate().getLocalName().equals("A1"))
         ) {

            if ((statement.getObject().toString().indexOf("/entities/")>-1)) {
                return true;
            }
        }
        return false;
    }

    static boolean dbpParticipant (Statement statement) {
        if (
                (statement.getPredicate().getLocalName().equals("A0"))
                        ||
                (statement.getPredicate().getLocalName().equals("A1"))
         ) {
            if ((statement.getObject().toString().indexOf("//dbpedia.")>-1)) {
                return true;
            }
        }
        return false;
    }

    static boolean dbpPlace (Statement statement) {
        if ((statement.getPredicate().getLocalName().equals("hasPlace"))
         ) {
            if ((statement.getObject().toString().indexOf("//dbpedia.")>-1)) {
                return true;
            }
        }
        return false;
    }

}
