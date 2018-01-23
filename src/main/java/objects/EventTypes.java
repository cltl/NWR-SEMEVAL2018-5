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


    /*
     142 s1/questions.json:        "event_type": "fire_burning",
 551 s1/questions.json:        "event_type": "injuring",
  13 s1/questions.json:        "event_type": "job_firing",
 326 s1/questions.json:        "event_type": "killing",
  79 s2/questions.json:        "event_type": "fire_burning",
 543 s2/questions.json:        "event_type": "injuring",
   4 s2/questions.json:        "event_type": "job_firing",
 371 s2/questions.json:        "event_type": "killing",
1502 s3/questions.json:        "event_type": "injuring",
  26 s3/questions.json:        "event_type": "job_firing",
 928 s3/questions.json:        "event_type": "killing",
     */

    static public final String DEAD = "DEAD";
    static public final String INJURED = "INJURED";
    static public final String SHOOT = "SHOOT";
    static public final String HIT = "HIT";
    static public final String INCIDENT = "INCIDENT";
    static public final String BURN = "BURN";
    static public final String DISMISS = "DISMISS";

    static final String[] incidentTypes = {"eso:Attacking", "eso:Destroying", "fn:Attack",
            "fn:Catastrophe", "fn:Cause_harm","fn:Destroying", "fn:Catastrophe"};

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
    static final String[] burnTypes = { "fn:Absorb_heat", "fn:Apply_heat","fn:Setting_fire", "fn:Fire_burning", "fn:Fire_going_out"};
    static final String[] dismissTypes = {"fn:Quitting_a_place","fn:Quitting", "fn:Get_a_job","fn:Hiring","fn:Employing",
            "fn:Being_employed", "fn:Earnings_and_losses", "fn:Change_of_leadership", "fn:Personal_relationship", "fn:Working_a_post"};

    static ArrayList<String> killWords = new ArrayList<>();
    static ArrayList<String> incidentWords = new ArrayList<>();
    static ArrayList<String> shootWords = new ArrayList<>();
    static ArrayList<String> hitWords = new ArrayList<>();
    static ArrayList<String> injureWords = new ArrayList<>();
    static ArrayList<String> burnWords = new ArrayList<>();
    static ArrayList<String> dismissWords = new ArrayList<>();


    /**
     * We need to initialise the vocabulary of words with their event type
     * @param wordMap
     */
  static public void initVocabulary(HashMap<String, String> wordMap) {
      Set keyset = wordMap.keySet();
      Iterator<String> keys = keyset.iterator();
      while (keys.hasNext()) {
          String word = keys.next();
          String type = wordMap.get(word);
          if (type.equalsIgnoreCase(DEAD)) {
             killWords.add(word);
          }
          else if (type.equalsIgnoreCase(INCIDENT)) {
             incidentWords.add(word);
          }
          else if (type.equalsIgnoreCase(INJURED)) {
             injureWords.add(word);
          }
          else if (type.equalsIgnoreCase(SHOOT)) {
             shootWords.add(word);
          }
          else if (type.equalsIgnoreCase(HIT)) {
             hitWords.add(word);
          }
          else if (type.equalsIgnoreCase(BURN)) {
             burnWords.add(word);
          }
          else if (type.equalsIgnoreCase(DISMISS)) {
             dismissWords.add(word);
          }
      }
  }


    public static boolean isType(String type) {
       // System.out.println("type = " + type);
        if (isKill(type)) return true;
        if (isHit(type)) return true;
        if (isInjury(type)) return true;
        if (isIncident(type)) return true;
        if (isShoot(type)) return true;
        if (isBurn(type)) return true;
        if (isDismiss(type)) return true;
        return false;
    }

    public static boolean isWord(String word) {
       // System.out.println("type = " + type);
        if (isInjuryWord(word))  return true;
        if (isIncidentWord(word))  return true;
        if (isKillWord(word))  return true;
        if (isShootWord(word))  return true;
        if (isHitWord(word))  return true;
        if (isBurnWord(word))  return true;
        if (isDismissWord(word))  return true;
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

    public static boolean isBurn(String type) {
        for (int i = 0; i < burnTypes.length; i++) {
            String s = burnTypes[i];
            if (s.equals(type)) return true;
        }
        return false;
    }

    public static boolean isDismiss(String type) {
        for (int i = 0; i < dismissTypes.length; i++) {
            String s = dismissTypes[i];
            if (s.equals(type)) return true;
        }
        return false;
    }

    public static boolean isInjuryWord(String word) {
        for (int i = 0; i < injureWords.size(); i++) {
            String s = injureWords.get(i);
            if (s.equalsIgnoreCase(word)) return true;
        }
        return false;
    }
    public static boolean isHitWord(String word) {
        for (int i = 0; i < hitWords.size(); i++) {
            String s = hitWords.get(i);
            if (s.equalsIgnoreCase(word)) return true;
        }
        return false;
    }

    public static boolean isShootWord(String word) {
        for (int i = 0; i < shootWords.size(); i++) {
            String s = shootWords.get(i);
            if (s.equalsIgnoreCase(word)) return true;
        }
        return false;
    }

    public static boolean isIncidentWord(String word) {
        for (int i = 0; i < incidentWords.size(); i++) {
            String s = incidentWords.get(i);
            if (s.equalsIgnoreCase(word)) return true;
        }
        return false;
    }

    public static boolean isKillWord(String word) {
        for (int i = 0; i < killWords.size(); i++) {
            String s = killWords.get(i);
            if (s.equalsIgnoreCase(word)) return true;
        }
        return false;
    }

    public static boolean isBurnWord(String word) {
        for (int i = 0; i < burnWords.size(); i++) {
            String s = burnWords.get(i);
            if (s.equalsIgnoreCase(word)) return true;
        }
        return false;
    }

    public static boolean isDismissWord(String word) {
        for (int i = 0; i < dismissWords.size(); i++) {
            String s = dismissWords.get(i);
            if (s.equalsIgnoreCase(word)) return true;
        }
        return false;
    }

    public static ArrayList<String> getDomainEventSubjectUris(HashMap<String, ArrayList<Statement>> tripleMap) {
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
                } else if (eventWordMatch(statements)) {
                    eventUris.add(tripleKey);
                }
            }
        }
        return eventUris;
    }


    public static String getEventType(String subjectUri, HashMap<String, ArrayList<Statement>> eckgMap) {
            ArrayList<Statement> statements  = eckgMap.get(subjectUri);
            String type = getType(statements);
            return type;
    }

    public static String getEventType(ArrayList<Statement> statements) {
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
                if (isBurn(objValue)) return BURN;
                if (isDismiss(objValue)) return DISMISS;
            }
            else if (statement.getPredicate().getLocalName().equals("prefLabel")) {
                String objValue = statement.getObject().toString();
                //System.out.println("objValue = " + objValue);
                if (isShootWord(objValue)) return SHOOT;
                if (isKillWord(objValue)) return DEAD;
                if (isInjuryWord(objValue)) return INJURED;
                if (isIncidentWord(objValue)) return INCIDENT;
                if (isHitWord(objValue)) return HIT;
                if (isBurnWord(objValue)) return BURN;
                if (isDismissWord(objValue)) return DISMISS;
            }
        }
        return "";
    }

    public static ArrayList<String> getTaskSubTypes (ArrayList<Statement> statements) {
       ArrayList<String> types = new ArrayList<>();
       for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getPredicate().getLocalName().equals("type")) {
                String objValue = TrigUtil.getPrettyNSValue(statement.getObject().toString());
                if (isShoot(objValue)) types.add(SHOOT);
                else if (isHit(objValue)) types.add(HIT);
                else if (isKill(objValue)) types.add(DEAD);
                else if (isInjury(objValue)) types.add(INJURED);
                else if (isBurn(objValue)) types.add(BURN);
                else if (isDismiss(objValue)) types.add(DISMISS);
            }
            else if (statement.getPredicate().getLocalName().equals("prefLabel")) {
                String objValue = statement.getObject().toString();
                //System.out.println("objValue = " + objValue);
                if (isShootWord(objValue)) types.add(SHOOT);
                else if (isHitWord(objValue)) types.add(HIT);
                else if (isKillWord(objValue)) types.add(DEAD);
                else if (isInjuryWord(objValue)) types.add(INJURED);
                else if (isBurnWord(objValue)) types.add(BURN);
                else if (isDismissWord(objValue)) types.add(DISMISS);
            }
        }
        return types;
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
               // System.out.println("objValue = " + objValue);
                if (objValue.equals(EventTypes.DEAD))  {
                    return true;
                }
                else if (EventTypes.isKill(objValue))  {
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
                if (objValue.equals(EventTypes.INJURED))  {
                    return true;
                }
                if (EventTypes.isInjury(objValue))  {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getDominantEventTypeFromDataset (HashMap<String, ArrayList<Statement>> tripleMap) {
        int nShoot = 0;
        int nBurn = 0;
        int nDismiss = 0;
        Set keySet = tripleMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String tripleKey = keys.next();
            if (Util.isEventKey(tripleKey)) {
                ArrayList<Statement> statements = tripleMap.get(tripleKey);
                ArrayList<String> taskSubTypes = getTaskSubTypes(statements);
                for (int i = 0; i < taskSubTypes.size(); i++) {
                    String subType = taskSubTypes.get(i);
                    if (subType.equals(SHOOT)) {
                        nShoot++;
                    }
                    else if (subType.equals(BURN)) {
                        nBurn++;
                    }
                    else if (subType.equals(DISMISS)) {
                        nDismiss++;
                    }
                    else if (subType.equals(DEAD)) {
                        //// in case of dead dismiss is punished
                        nDismiss--;
                    }
                    else if (subType.equals(INJURED)) {
                        //// in case of injured dismiss is punished
                        nDismiss--;
                    }
                }
            }
        }
        String eventType = "";
        if (nBurn>nShoot && nBurn>=nDismiss) eventType = BURN;
        if (nDismiss>nShoot && nDismiss>=nBurn) eventType = DISMISS;
        if (nShoot>=nBurn && nShoot>=nDismiss) eventType = SHOOT;
        return eventType;
    }

}
