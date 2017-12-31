package objects;

import com.hp.hpl.jena.rdf.model.Statement;

import java.util.ArrayList;
import java.util.HashMap;

public class Participants {





    static public ArrayList<String> getEntityParticipants (HashMap<String, ArrayList<Statement>> seckgMap,
                                                           ArrayList<Statement> statements) {
            ArrayList<String> participants = new ArrayList<>();
            for (int j = 0; j < statements.size(); j++) {
                Statement statement = statements.get(j);
                String participantUri = "";
                if (statement.getPredicate().getLocalName().equalsIgnoreCase("a0")) {
                   // participantUri = statement.getObject().asResource().getURI();
                }
                else if (statement.getPredicate().getLocalName().equalsIgnoreCase("a1")) {
                    participantUri = statement.getObject().asResource().getURI();
                }
                if (!participantUri.isEmpty()) {
                    /// check for HUMAN
                    if (isHumanEntity(participantUri, seckgMap)) {
                        if (!participants.contains(participantUri)) {
                            participants.add(participantUri);
                        }
                    }
                }

            }
            return participants;
    }

    static public ArrayList<String> getNonEntityParticipants (HashMap<String, ArrayList<Statement>> instanceStatements, ArrayList<Statement> statements) {
        ArrayList<String> participants = new ArrayList<>();
        for (int j = 0; j < statements.size(); j++) {
            Statement statement = statements.get(j);
            String participantUri = "";
            if (statement.getPredicate().getLocalName().equalsIgnoreCase("a1")) {
                participantUri = statement.getObject().asResource().getURI();
                /// check for HUMAN
                if (isHumanNonEntity(participantUri, instanceStatements)) {
                    if (!participants.contains(participantUri)) {
                        participants.add(participantUri);
                    }
                }
            }
        }
        return participants;
    }

    static boolean isHumanEntity (String participantUri, HashMap<String, ArrayList<Statement>> instanceStatements) {
        boolean HUMAN = false;
        boolean PLACE = false;
       // System.out.println("participantUri = " + participantUri);
        if (instanceStatements.containsKey(participantUri)) {
            ArrayList<Statement> participantStatements = instanceStatements.get(participantUri);
            for (int j = 0; j < participantStatements.size(); j++) {
                Statement participantStatement = participantStatements.get(j);
                if (participantStatement.getPredicate().getLocalName().equals("type")) {
                    //System.out.println("participantStatement.toString() = " + participantStatement.toString());
                    //System.out.println("participantStatement.getObject().asResource().getLocalName() = " + participantStatement.getObject().asResource().getLocalName());
                    if (participantStatement.getObject().asResource().getLocalName().equals("PER")) {
                        HUMAN = true;
                    }
                    else if (participantStatement.getObject().asResource().getLocalName().equals("LOC")) {
                        PLACE = true;
                    }
                }
            }
        }
        if (HUMAN && !PLACE) return true;
        else return false;
    }

    static boolean isHumanNonEntity (String participantUri, HashMap<String, ArrayList<Statement>> instanceStatements) {
        boolean HUMAN = false;
        if (instanceStatements.containsKey(participantUri)) {
            ArrayList<Statement> participantStatements = instanceStatements.get(participantUri);
            for (int j = 0; j < participantStatements.size(); j++) {
                Statement participantStatement = participantStatements.get(j);
                if (participantStatement.getPredicate().getLocalName().equals("prefLabel") ||
                        participantStatement.getPredicate().getLocalName().equals("label")
                    ) {
                    String label = participantStatement.getObject().asLiteral().getLexicalForm();
                    if (ParticipantTypes.isHumanLabel(label)) {
                        HUMAN = true;
                        //System.out.println("label = " + label);
                        break;
                    }
                }
            }
        }
        return HUMAN;
    }

    static ArrayList<String> getHumanNonEntity (String participantUri, HashMap<String, ArrayList<Statement>> instanceStatements) {
        ArrayList<String> humanNonEntities = new ArrayList<>();
        if (instanceStatements.containsKey(participantUri)) {
            ArrayList<Statement> participantStatements = instanceStatements.get(participantUri);
            for (int j = 0; j < participantStatements.size(); j++) {
                Statement participantStatement = participantStatements.get(j);
                if (participantStatement.getPredicate().getLocalName().equals("prefLabel") ||
                        participantStatement.getPredicate().getLocalName().equals("label")
                    ) {
                    String label = participantStatement.getObject().asLiteral().getLexicalForm();
                    String humanNonEntityLabel = ParticipantTypes.getHumanLabel(label);
                    if (!humanNonEntityLabel.isEmpty()) {
                        String uri = participantStatement.getObject().asLiteral().getLexicalForm();
                        int idx = participantStatement.getObject().asLiteral().getLexicalForm().lastIndexOf("/");
                        if (idx>-1) {
                            uri = uri.substring(0, idx)+humanNonEntityLabel;
                        }
                        else {
                            ////
                        }
                        humanNonEntities.add(uri);
                    }
                }
            }
        }
        return humanNonEntities;
    }
}
