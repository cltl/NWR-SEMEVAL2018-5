package objects;

import com.hp.hpl.jena.rdf.model.Statement;
import vu.cltl.triple.TrigTripleData;

import java.util.ArrayList;

public class Participants {





    static public ArrayList<String> getEntityParticipants (TrigTripleData trigTripleData, ArrayList<Statement> statements) {
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
                    if (isHumanEntity(participantUri, trigTripleData)) {
                        if (!participants.contains(participantUri)) {
                            participants.add(participantUri);
                        }
                    }
                }
            }
            return participants;
    }

    static public ArrayList<String> getNonEntityParticipants (TrigTripleData trigTripleData, ArrayList<Statement> statements) {
        ArrayList<String> participants = new ArrayList<>();
        for (int j = 0; j < statements.size(); j++) {
            Statement statement = statements.get(j);
            String participantUri = "";
            if (statement.getPredicate().getLocalName().equalsIgnoreCase("a1")) {
                participantUri = statement.getObject().asResource().getURI();
                /// check for HUMAN
                if (isHumanNonEntity(participantUri, trigTripleData)) {
                    if (!participants.contains(participantUri)) {
                        participants.add(participantUri);
                    }
                }
            }
        }
        return participants;
    }

    static boolean isHumanEntity (String participantUri, TrigTripleData trigTripleData) {
        boolean HUMAN = false;
        boolean PLACE = false;
        if (trigTripleData.tripleMapInstances.containsKey(participantUri)) {
            ArrayList<Statement> participantStatements = trigTripleData.tripleMapInstances.get(participantUri);
            for (int j = 0; j < participantStatements.size(); j++) {
                Statement participantStatement = participantStatements.get(j);
                if (participantStatement.getPredicate().getLocalName().equals("type")) {
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

    static boolean isHumanNonEntity (String participantUri, TrigTripleData trigTripleData) {
        boolean HUMAN = false;
        if (trigTripleData.tripleMapInstances.containsKey(participantUri)) {
            ArrayList<Statement> participantStatements = trigTripleData.tripleMapInstances.get(participantUri);
            for (int j = 0; j < participantStatements.size(); j++) {
                Statement participantStatement = participantStatements.get(j);
                if (participantStatement.getPredicate().getLocalName().equals("prefLabel") ||
                        participantStatement.getPredicate().getLocalName().equals("label")
                    ) {
                    String label = participantStatement.getObject().asLiteral().getLexicalForm();
                    if (ParticipantTypes.isHumanLabel(label)) {
                        HUMAN = true;
                        System.out.println("label = " + label);
                        break;
                    }
                    /*
                    if (!label.toLowerCase().equals(label)) {
                        System.out.println("label = " + label);
                        HUMAN = true;
                        break;
                    }*/
                }
            }
        }
        return HUMAN;
    }
}
