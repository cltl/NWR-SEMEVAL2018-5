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

                }
                mergedEvents.put(key1, directStatements1);
               // System.out.println("mergedEvents = " + mergedEvents.size());
            }
        }
        return mergedEvents;
    }



    static ArrayList<Statement> matchingStatements (ArrayList<Statement>statements1, ArrayList<Statement> statements2, MatchSettings matchSettings) {
        ArrayList<Statement> matchingStatements = new ArrayList<>();
        for (int i = 0; i < statements1.size(); i++) {
            Statement statement1 = statements1.get(i);
            if (identityStatement(statement1, matchSettings)) {
                for (int j = 0; j < statements2.size(); j++) {
                    Statement statement2 = statements2.get(j);
                    if (identityStatement(statement2, matchSettings)) {
                        if (statement1.getObject().toString().equals(statement2.getObject().toString())) {
                            TrigUtil.addNewStatement(matchingStatements, statement1);
                        }
                    }
                }
            }
        }
        return matchingStatements;
    }

    static ArrayList<Statement> matchingStatementsByPrefLabel (HashMap<String, ArrayList<Statement>> seckgMap,
                                                    ArrayList<Statement>statements1,
                                                    ArrayList<Statement> statements2,
                                                               MatchSettings matchSettings) {
        ArrayList<Statement> matchingStatements = new ArrayList<>();
        for (int i = 0; i < statements1.size(); i++) {
            Statement statement1 = statements1.get(i);
            if (identityStatement(statement1, matchSettings)) {
                for (int j = 0; j < statements2.size(); j++) {
                    Statement statement2 = statements2.get(j);
                    if (identityStatement(statement2, matchSettings)) {
                        if (matchPreferredLabel(seckgMap, statement1.getSubject().getURI(), statement2.getSubject().getURI())) {
                            TrigUtil.addNewStatement(matchingStatements, statement1);
                        }
                    }
                }
            }
        }
        return matchingStatements;
    }

    static boolean matchPreferredLabel (HashMap<String, ArrayList<Statement>> seckgMap, String uri1, String uri2) {
        String prefLabel1 = "";
        String prefLabel2 = "";
        if (seckgMap.containsKey(uri1)) {
            ArrayList<Statement> statements = seckgMap.get(uri1);
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                if (statement.getPredicate().getLocalName().equals("prefLabel")) {
                    prefLabel1 = statement.getObject().asLiteral().toString();
                }
            }
        }
        if (seckgMap.containsKey(uri2)) {
            ArrayList<Statement> statements = seckgMap.get(uri2);
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                if (statement.getPredicate().getLocalName().equals("prefLabel")) {
                    prefLabel2 = statement.getObject().asLiteral().toString();
                }
            }
        }
        if (prefLabel1.equalsIgnoreCase(prefLabel2) && !prefLabel1.isEmpty()) {
            return true;
        }
        else {
            return false;
        }
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

    static boolean identityStatement (Statement statement, MatchSettings matchSettings) {
        if (matchSettings.isMatchAny()) return true;
        if (matchSettings.isMatchDbpActor()) return dbpParticipant(statement);
        if (matchSettings.isMatchEnActor()) return entityParticipant(statement);
        if (matchSettings.isMatchNeActor()) return nonentityParticipant(statement);
        if (matchSettings.isMatchDbpPlace()) return dbpPlace(statement);
        if (matchSettings.isMatchAnyPlace()) return anyPlace(statement);
        return false;
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

    static boolean nonentityParticipant (Statement statement) {
        if ((statement.getPredicate().getLocalName().equals("A0"))
                        ||
                (statement.getPredicate().getLocalName().equals("A1"))
         ) {

            if ((statement.getObject().toString().indexOf("non-entities")>-1)) {
                return true;
            }
            else if ((statement.getObject().toString().indexOf("nonentities")>-1)) {
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
            //System.out.println("statement.getObject().toString() = " + statement.getObject().toString());
            if ((statement.getObject().toString().indexOf("dbpedia")>-1)) {
                return true;
            }
        }
        return false;
    }

    static boolean dbpPlace (Statement statement) {
        if (statement.getPredicate().getLocalName().equals("hasPlace") ||
                    statement.getPredicate().getLocalName().equals("atPlace-location") ||
                    statement.getPredicate().getLocalName().equals("AM-LOC")
                 )
        {
            if ((statement.getObject().toString().indexOf("dbpedia")>-1)) {
                return true;
            }
        }
        return false;
    }
    static boolean anyPlace (Statement statement) {
        if (statement.getPredicate().getLocalName().equals("hasPlace") ||
            statement.getPredicate().getLocalName().equals("atPlace-location") ||
            statement.getPredicate().getLocalName().equals("AM-LOC") 
         )
        { return true;  }
        else
        { return false; }
    }

}
