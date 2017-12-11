package match;

import com.hp.hpl.jena.rdf.model.Statement;
import vu.cltl.triple.TrigUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class SpatialReasoning {



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
