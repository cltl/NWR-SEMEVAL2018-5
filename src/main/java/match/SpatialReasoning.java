package match;

import com.hp.hpl.jena.rdf.model.Statement;
import objects.Space;
import vu.cltl.triple.TrigUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class SpatialReasoning {


    /**
         * Restricted version where the relation has to hasActor or hasPlace (which is almost everything)
         * @param statements
         * @param seckgMap
         * @return
         */
        static public ArrayList<String> getLocations (HashMap<String, ArrayList<Statement>> seckgMap, ArrayList<Statement> statements) {
            ArrayList<String> locationObjects = new ArrayList<>();
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                if (Space.locationURIs.contains(statement.getObject().toString())) {
                    if (!locationObjects.contains(statement.getObject().toString())) {
                        locationObjects.add(statement.getObject().toString());
                    }
                }
                else if (seckgMap.containsKey(statement.getObject().toString())) {
                    ArrayList<Statement> statementArrayList = seckgMap.get(statement.getObject().toString());
                    for (int j = 0; j < statementArrayList.size(); j++) {
                        Statement objectStatement = statementArrayList.get(j);
                        if (objectStatement.getPredicate().getLocalName().equals("type")) {
                            if (Space.locationURIs.contains(objectStatement.getObject().toString())) {
                                if (!locationObjects.contains(objectStatement.getObject().toString())) {
                                    locationObjects.add(objectStatement.getObject().toString());
                                }
                            }
                        }
                    }
                }
            }
            return locationObjects;
        }

        static public ArrayList<Statement> getLocationStatements (HashMap<String, ArrayList<Statement>> seckgMap, ArrayList<Statement> statements) {
            ArrayList<Statement> locationObjects = new ArrayList<>();
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                if (Space.locationURIs.contains(statement.getObject().toString())) {
                    if (!locationObjects.contains(statement.getObject().toString())) {
                        locationObjects.add(statement);
                    }
                }
                else if (seckgMap.containsKey(statement.getObject().toString())) {
                    ArrayList<Statement> statementArrayList = seckgMap.get(statement.getObject().toString());
                    for (int j = 0; j < statementArrayList.size(); j++) {
                        Statement objectStatement = statementArrayList.get(j);
                        if (objectStatement.getPredicate().getLocalName().equals("type")) {
                            if (Space.locationURIs.contains(objectStatement.getObject().toString())) {
                                if (!locationObjects.contains(objectStatement.getObject().toString())) {
                                    locationObjects.add(objectStatement);
                                }
                            }
                        }
                    }
                }
            }
            return locationObjects;
        }


    static public  ArrayList<String> spaceRelatedCityLexicon (String uri, ArrayList<String> targetUris) {
        ArrayList<String> matches = new ArrayList<>();
       // System.out.println("targetUris = " + targetUris);
        String sourceUri = uri;
        if (!Space.stateMap.containsKey(sourceUri))  {
            sourceUri = "<"+uri+">";
        }
        if (Space.cityMap.containsKey(sourceUri))  {
            System.out.println("sourceUri = " + sourceUri);
            ArrayList<String> objects = Space.cityMap.get(sourceUri);
            for (int i = 0; i < objects.size(); i++) {
                String object = objects.get(i);
                if (targetUris.contains(object)) {
                    System.out.println("object = " + object);
                    if (!matches.contains(object)) matches.add(object);
                }
            }
        }
        return matches;
    }

    static public  ArrayList<String> spaceRelatedStateLexicon (String uri, ArrayList<String> targetUris) {
        ArrayList<String> matches = new ArrayList<>();
        String sourceUri = uri;
        if (!Space.stateMap.containsKey(sourceUri))  {
            sourceUri = "<"+uri+">";
        }
        if (Space.stateMap.containsKey(sourceUri))  {
           // System.out.println("sourceUri = " + sourceUri);
            ArrayList<String> objects = Space.stateMap.get(sourceUri);
            for (int i = 0; i < objects.size(); i++) {
                String object = objects.get(i);
                if (targetUris.contains(object)) {
                    if (!matches.contains(object)) matches.add(object);
                }
            }
        }
        return matches;
    }

    static public HashMap<String, ArrayList<String>> getSpatialContainers (
            HashMap<String, ArrayList<Statement>> eckgMap,
            HashMap<String, ArrayList<Statement>> seckgMap ) {
        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
        Set keySet = eckgMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
           String eventKey = keys.next();
           String timeString = "NOSPACE";
           if (seckgMap.containsKey(eventKey)) {
               ArrayList<Statement> secondaryStatements = seckgMap.get(eventKey);
               timeString = getSpaceString(secondaryStatements);
           }
           if (map.containsKey(timeString)) {
               ArrayList<String> events = map.get(timeString);
               events.add(eventKey);
            }
        }
        return map;
    }


    static String getSpaceString (ArrayList<Statement> statements) {
        /**
         **/
        String spaceString = "";
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getPredicate().getLocalName().equals("hasPlace")) {
                spaceString += TrigUtil.getPrettyNSValue(statement.getObject().toString());
            }
            else if (statement.getPredicate().getLocalName().equals("atPlace-location")) {
                spaceString += TrigUtil.getPrettyNSValue(statement.getObject().toString());
            }
            else if (statement.getPredicate().getLocalName().equals("AM-LOC")) {
                spaceString += TrigUtil.getPrettyNSValue(statement.getObject().toString());
            }

        }
        return spaceString;
    }

}
