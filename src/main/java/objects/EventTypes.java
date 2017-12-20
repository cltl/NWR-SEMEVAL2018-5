package objects;

import com.hp.hpl.jena.rdf.model.Statement;
import util.Util;
import vu.cltl.triple.TrigUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 10/11/2017.
 */
public class EventTypes {

    static final String DEAD = "DEAD";
    static final String INJURED = "INJURED";
    static final String SHOOT = "SHOOT";
    static final String HIT = "HIT";
    static final String INCIDENT = "INCIDENT";

    static final String [] killWords = {"dead", "death", "murder", "murderer", "kill", "killed", "killing", "killer", "homicide", "fatal", "suicide", "life", "losing", "breath", "loss", "die", "", "died", "dies", "grieving", "autopsies", "autopsy"};
    static final String [] incidentWords = {"it", "incident", "crime", "this", "accident", "drama"};
    static final String [] shootWords = {"gunshot", "gunman", "gun", "shooting", "shooter", "bodies", "weapon", "handling", "turning", "turned", "turn"};
    static final String [] hitWords = {"hit", "strike", "stroke"};
    static final String [] injureWords = {"injure", "injury", "injuries", "wounded", "wounds", "wound", "stable", "condition", "surgery", "critical", "hospitalized"};

  /*  static final String[] types = {"eso:Attacking", "eso:BeingInExistence", "eso:Damaging", "eso:Destroying", "eso:Injuring",
            "eso:Killing", "fn:Attack", "fn:Catastrophe", "fn:Cause_harm", "fn:Cause_impact", "fn:Cause_to_end",
            "fn:Death", "fn:Destroying", "fn:Existence", "fn:Experience_bodily_harm", "fn:Firing", "fn:Hit_target",
            "fn:Killing", "fn:Recovery", "fn:Resurrection", "fn:Shoot_projectiles", "fn:Use_firearm"};
*/
    static final String[] incidentTypes = {"eso:Attacking", "eso:Destroying", "fn:Attack", "fn:Catastrophe", "fn:Cause_harm","fn:Destroying"};

    static final String[] killTypes = {"eso:BeingInExistence",  "eso:Destroying",
            "eso:Killing", "fn:Cause_to_end",
            "fn:Death", "fn:Destroying", "fn:Existence",
            "fn:Killing"};

    static final String[] injuredTypes = { "eso:Damaging", "eso:Injuring",
             "fn:Cause_harm", "fn:Cause_impact",
            "fn:Experience_bodily_harm", "fn:Hit_target",
            "fn:Recovery", "fn:Resurrection"};

    static final String[] hitTypes = { "fn:Cause_impact","fn:Hit_target"};

    static final String[] shootTypes = { "fn:Shoot_projectiles", "fn:Use_firearm","fn:Firing"};

    public static boolean isType(String type) {
       // System.out.println("type = " + type);
        if (isKill(type)) return true;
        if (isHit(type)) return true;
        if (isInjury(type)) return true;
        if (isIncident(type)) return true;
        if (isShoot(type)) return true;
        return false;
    }

    public static boolean isWord(String word) {
       // System.out.println("type = " + type);
        if (isInjuryWord(word))  return true;
        if (isIncidentWord(word))  return true;
        if (isKillWord(word))  return true;
        if (isShootWord(word))  return true;
        if (isHitWord(word))  return true;
        return false;
    }

    public static boolean isKill(String type) {
        for (int i = 0; i < killTypes.length; i++) {
            String s = killTypes[i];
            if (s.equals(type)) return true;
        }
        return false;
    }

    public static boolean isIncident(String type) {
        for (int i = 0; i < incidentTypes.length; i++) {
            String s = incidentTypes[i];
            if (s.equals(type)) return true;
        }
        return false;
    }

    public static boolean isShoot(String type) {
        for (int i = 0; i < shootTypes.length; i++) {
            String s = shootTypes[i];
            if (s.equals(type)) return true;
        }
        return false;
    }

    public static boolean isInjury(String type) {
        for (int i = 0; i < injuredTypes.length; i++) {
            String s = injuredTypes[i];
            if (s.equals(type)) return true;
        }
        return false;
    }

    public static boolean isHit(String type) {
        for (int i = 0; i < hitTypes.length; i++) {
            String s = hitTypes[i];
            if (s.equals(type)) return true;
        }
        return false;
    }

    public static boolean isInjuryWord(String word) {
        for (int i = 0; i < injureWords.length; i++) {
            String s = injureWords[i];
            if (s.equalsIgnoreCase(word)) return true;
        }
        return false;
    }
    public static boolean isHitWord(String word) {
        for (int i = 0; i < hitWords.length; i++) {
            String s = hitWords[i];
            if (s.equalsIgnoreCase(word)) return true;
        }
        return false;
    }

    public static boolean isShootWord(String word) {
        for (int i = 0; i < shootWords.length; i++) {
            String s = shootWords[i];
            if (s.equalsIgnoreCase(word)) return true;
        }
        return false;
    }

    public static boolean isIncidentWord(String word) {
        for (int i = 0; i < incidentWords.length; i++) {
            String s = incidentWords[i];
            if (s.equalsIgnoreCase(word)) return true;
        }
        return false;
    }

    public static boolean isKillWord(String word) {
        for (int i = 0; i < killWords.length; i++) {
            String s = killWords[i];
            if (s.equalsIgnoreCase(word)) return true;
        }
        return false;
    }

    public static ArrayList<String> getDomainEventSubjectUris(HashMap<String, ArrayList<Statement>> tripleMap, ArrayList<String> eventVocabulary) {
        Set keySet = tripleMap.keySet();
        ArrayList<String> eventUris = new ArrayList<>();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String tripleKey = keys.next();
            if (Util.isEventKey(tripleKey)) {
                ArrayList<Statement> statements = tripleMap.get(tripleKey);
                if (eventTypeMatch(statements)) {
                    eventUris.add(tripleKey);
                } else if (eventWordMatch(statements)) {
                    eventUris.add(tripleKey);
                } else if (eventWordMatch(statements, eventVocabulary)) {
                    eventUris.add(tripleKey);
                }
            }
        }
        return eventUris;
    }


    public static String getEventType(String subjectUri, ArrayList<Statement> statements) {
            String type = getType(statements);
            return type;
    }


    public static String getType (ArrayList<Statement> statements) {
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getPredicate().getLocalName().equals("type")) {
                String objValue = TrigUtil.getPrettyNSValue(statement.getObject().toString());
                if (isShoot(objValue)) return SHOOT;
                if (isKill(objValue)) return DEAD;
                if (isInjury(objValue)) return INJURED;
                if (isIncident(objValue)) return INCIDENT;
                if (isHit(objValue)) return HIT;
            }
        }
        return "";
    }

    /**
     * KS util
     * @param statements
     * @return
     */
    public static boolean eventTypeMatch (ArrayList<Statement> statements) {
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getPredicate().getLocalName().equals("type")) {
                String objValue = TrigUtil.getPrettyNSValue(statement.getObject().toString());
                if (EventTypes.isType(objValue))  {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean eventWordMatch (ArrayList<Statement> statements) {
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getPredicate().getLocalName().equals("prefLabel")) {
                String objValue = TrigUtil.getPrettyNSValue(statement.getObject().toString());
                if (EventTypes.isWord(objValue))  {
                    return true;
                }
            }
            if (statement.getPredicate().getLocalName().equals("label")) {
                String objValue = TrigUtil.getPrettyNSValue(statement.getObject().toString());
                if (EventTypes.isWord(objValue))  {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean eventWordMatch (ArrayList<Statement> statements, ArrayList<String> eventVocabulary) {
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getPredicate().getLocalName().equals("prefLabel")) {
                String objValue = TrigUtil.getPrettyNSValue(statement.getObject().toString());
                if (eventVocabulary.contains(objValue))  {
                    return true;
                }
            }
            if (statement.getPredicate().getLocalName().equals("label")) {
                String objValue = TrigUtil.getPrettyNSValue(statement.getObject().toString());
                if (eventVocabulary.contains(objValue))  {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean eventKillMatch (ArrayList<Statement> statements) {
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getPredicate().getLocalName().equals("type")) {
                String objValue = TrigUtil.getPrettyNSValue(statement.getObject().toString());
                if (EventTypes.isKill(objValue))  {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean eventInjuryMatch (ArrayList<Statement> statements) {
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getPredicate().getLocalName().equals("type")) {
                String objValue = TrigUtil.getPrettyNSValue(statement.getObject().toString());
                if (EventTypes.isInjury(objValue))  {
                    return true;
                }
            }
        }
        return false;
    }


}
