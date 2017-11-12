package match;

import com.hp.hpl.jena.rdf.model.Statement;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by piek on 12/11/2017.
 */
public class EventIdentity {


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
                    if (!key1.equals(key2)) {
                        ArrayList<Statement> directStatements2 = entry2.getValue();
                        if (matchingStatements(directStatements1, directStatements2) > tripleMatchThreshold) {
                            ////identify
                            skipEvents.add(key2);
                            directStatements1.addAll(directStatements2);
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
                //System.out.println("mergedEvents = " + mergedEvents.size());
            }
        }
        return mergedEvents;
    }

    static int matchingStatements (ArrayList<Statement>statements1, ArrayList<Statement> statements2) {
        int count = 0;
        for (int i = 0; i < statements1.size(); i++) {
            Statement statement1 = statements1.get(i);
            if (statement1.getPredicate().getLocalName().equals("hasActor")) {
                for (int j = 0; j < statements2.size(); j++) {
                    Statement statement2 = statements2.get(j);
                    if (statement2.getPredicate().getLocalName().equals("hasActor")) {
                        if (statement1.getObject().toString().equals(statement2.getObject().toString())) {
                            count++;
                           // System.out.println("statement1 = " + statement1.getObject().toString());
                        }
                    }
                }
            }
        }
        return count;
    }
}
