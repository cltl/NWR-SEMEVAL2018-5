package match;

import com.hp.hpl.jena.rdf.model.Statement;
import vu.cltl.triple.TrigUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class MatchUtil {

    static public ArrayList<Statement> matchingStatements (ArrayList<Statement>statements1, ArrayList<Statement> statements2, MatchSettings matchSettings) {
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

    static public ArrayList<Statement> matchingStatementsByPrefLabel (HashMap<String, ArrayList<Statement>> seckgMap,
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
                        if (matchPreferredLabel(seckgMap, statement1.getSubject().getURI(), statement2.getSubject().getURI(), matchSettings.getEditDistanceThreshold())) {
                            TrigUtil.addNewStatement(matchingStatements, statement1);
                        }
                    }
                }
            }
        }
        return matchingStatements;
    }

    static public boolean matchPreferredLabel (HashMap<String, ArrayList<Statement>> seckgMap, String uri1, String uri2, int maxDistance) {
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
        else if (distance(prefLabel1.toLowerCase(), prefLabel2.toLowerCase())<maxDistance) {
            return true;
        }
        else {
            return false;
        }
    }

    static public boolean identityStatement (Statement statement, MatchSettings matchSettings) {
        if (matchSettings.isMatchAny()) return true;
        if (matchSettings.isMatchDbpActor()) return dbpParticipant(statement);
        if (matchSettings.isMatchEnActor()) return entityParticipant(statement);
        if (matchSettings.isMatchNeActor()) return nonentityParticipant(statement);
        if (matchSettings.isMatchDbpPlace()) return dbpPlace(statement);
        if (matchSettings.isMatchAnyPlace()) return anyPlace(statement);
        return false;
    }

    static public boolean entityParticipant (Statement statement) {
        if ((statement.getPredicate().getLocalName().equalsIgnoreCase("a0"))
                        ||
                (statement.getPredicate().getLocalName().equalsIgnoreCase("a1"))
         ) {

            if ((statement.getObject().toString().indexOf("/entities/")>-1)) {
                return true;
            }
        }
        return false;
    }

    static public boolean nonentityParticipant (Statement statement) {
        if ((statement.getPredicate().getLocalName().equalsIgnoreCase("a0"))
                        ||
                (statement.getPredicate().getLocalName().equalsIgnoreCase("a1"))
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

    static public boolean dbpParticipant (Statement statement) {
        if (
                (statement.getPredicate().getLocalName().equalsIgnoreCase("a0"))
                        ||
                (statement.getPredicate().getLocalName().equalsIgnoreCase("a1"))
         ) {
            //System.out.println("statement.getObject().toString() = " + statement.getObject().toString());
            if ((statement.getObject().toString().indexOf("dbpedia")>-1)) {
                return true;
            }
        }
        return false;
    }

    static public boolean dbpPlace (Statement statement) {
        if (statement.getPredicate().getLocalName().equalsIgnoreCase("hasPlace") ||
                    statement.getPredicate().getLocalName().equalsIgnoreCase("atPlace-location") ||
                    statement.getPredicate().getLocalName().equalsIgnoreCase("AM-LOC")
                 )
        {
            if ((statement.getObject().toString().indexOf("dbpedia")>-1)) {
                return true;
            }
        }
        return false;
    }
    static public boolean anyPlace (Statement statement) {
        if (statement.getPredicate().getLocalName().equalsIgnoreCase("hasPlace") ||
            statement.getPredicate().getLocalName().equalsIgnoreCase("atPlace-location") ||
            statement.getPredicate().getLocalName().equalsIgnoreCase("AM-LOC")
         )
        { return true;  }
        else
        { return false; }
    }

    public static int distance(String s1, String s2){
         int edits[][]=new int[s1.length()+1][s2.length()+1];
         for(int i=0;i<=s1.length();i++)
             edits[i][0]=i;
         for(int j=1;j<=s2.length();j++)
             edits[0][j]=j;
         for(int i=1;i<=s1.length();i++){
             for(int j=1;j<=s2.length();j++){
                 int u=(s1.charAt(i-1)==s2.charAt(j-1)?0:1);
                 edits[i][j]=Math.min(
                                 edits[i-1][j]+1,
                                 Math.min(
                                    edits[i][j-1]+1,
                                    edits[i-1][j-1]+u
                                 )
                             );
             }
         }
         return edits[s1.length()][s2.length()];
    }
}
