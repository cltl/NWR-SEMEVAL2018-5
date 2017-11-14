package match;

import com.hp.hpl.jena.rdf.model.Statement;
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
            Integer tripleMatchThreshold) {

        HashMap<String, ArrayList<Statement>> mergedEvents = new HashMap<>();
        ArrayList<String> skipEvents = new ArrayList<>();
        for (int i = 0; i < domainEvents.size(); i++) {
            String key1 = domainEvents.get(i);
            if (!skipEvents.contains(key1)) {
                ArrayList<Statement> directStatements1 = eckgMap.get(key1);
                for (int j = i+1; j < domainEvents.size(); j++) {
                    String key2 = domainEvents.get(j);
                    if (!skipEvents.contains(key2)) {
                        ArrayList<Statement> directStatements2 = eckgMap.get(key2);
                        ArrayList<Statement> matchingStatements = matchingStatements(directStatements1, directStatements2);
                        if (matchingStatements.size() >= tripleMatchThreshold) {
                            ////identify
                            System.out.println("matchingStatements.size() = " + matchingStatements.size());
                            for (int m = 0; m < matchingStatements.size(); m++) {
                                Statement statement = matchingStatements.get(m);
                                System.out.println(statement.getPredicate().getLocalName()+":" + TrigUtil.getValue(statement.getObject().toString()));
                            }
                            skipEvents.add(key2);
                            System.out.println("skipEvents = " + skipEvents.size());
                            TrigUtil.addNewStatements(directStatements1, directStatements2);
                            System.out.println("directStatements1.size() = " + directStatements1.size());
/*                            for (int d = 0; d < directStatements2.size(); d++) {
                                Statement statement = directStatements2.get(d);
                                if (statement.getPredicate().getLocalName().equals("denotedBy")) {
                                    directStatements1.add(statement);
                                  //  System.out.println("statement.toString() = " + statement.toString());
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

    static public HashMap<String, ArrayList<Statement>> lookForSimilarEvents (
            HashMap<String, ArrayList<Statement>> eckgMap,
            HashMap<String, ArrayList<Statement>> seckgMap,
            Integer tripleMatchThreshold) {

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
                        if (matchingStatements.size() > tripleMatchThreshold) {
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
        if (
               /* (statement.getPredicate().getLocalName().equals("hasActor"))
                        ||*/
                (statement.getPredicate().getLocalName().equals("hasPlace"))
                        ||
                (statement.getPredicate().getLocalName().equals("A1"))
                        ||
                (statement.getPredicate().getLocalName().equals("AM-LOC"))
         ) {
            if ((statement.getObject().toString().indexOf("/entities/")>-1) ||
                    (statement.getObject().toString().indexOf("//dbpedia.")>-1)) {
                return true;
            }
        }
        return false;
    }

}
