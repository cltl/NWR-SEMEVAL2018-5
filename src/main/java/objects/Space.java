package objects;

import java.util.ArrayList;

public class Space {

    //http://dbpedia.org/page/Flint,_Michigan
    //dbo:isPartOf
    //dbr:Michigan
    //dbr:Genesee_County,_Michigan
    /*
    dbp:north
    dbr:Bay_City,_Michigan
    dbr:Mackinaw_City
    dbr:Saginaw
    dbp:northwest
    dbr:Flushing,_Michigan
    dbr:Mount_Pleasant,_Michigan
    dbr:Traverse_City

    dbp:south
    dbr:Fenton,_Michigan
    dbr:Toledo,_Ohio
    dbr:Ann_Arbor
    dbp:southeast
    dbr:Detroit
    dbr:Pontiac,_Michigan
    dbr:Grand_Blanc
    dbp:southwest
    dbr:Chicago
    dbr:Benton_Harbor
    dbr:Kalamazoo

    dbp:west
    dbr:Grand_Rapids
    dbr:Lansing
    dbr:Swartz_Creek
    


is dbp:north of
dbr:Ann_Arbor,_Michigan
dbr:Mundy_Township,_Michigan
dbr:Tourism_in_metropolitan_Detroit


is dbp:northeast of
dbr:Lansing,_Michigan
dbr:Flint_Township,_Michigan

    geo:lat
    43.009998 (xsd:float)
    geo:long
    -83.690002 (xsd:float)

     */
   // String q = "select distinct ?object where {<http://dbpedia.org/resource/Flint,_Michigan> <http://dbpedia.org/property/north> ?object} LIMIT 100";

    /*
    select distinct ?obj where
    {
    { <http://dbpedia.org/resource/Flint,_Michigan> dbp:north ?obj}
    UNION
    { <http://dbpedia.org/resource/Flint,_Michigan> dbp:south ?obj}
    UNION
    { <http://dbpedia.org/resource/Flint,_Michigan> dbp:west ?obj}
    }
     */
    static public  ArrayList<String> spaceRelated (String sourceUri, ArrayList<String> targetUris) {
        ArrayList<String> matches = new ArrayList<>();
        String north = "<http://dbpedia.org/property/north>";
       String northeast = "<http://dbpedia.org/property/northeast>";
       String northwest = "<http://dbpedia.org/property/northwest>";
       String south = "<http://dbpedia.org/property/south>";
       String southeast = "<http://dbpedia.org/property/southeast>";
       String southwest = "<http://dbpedia.org/property/southwest>";
       String west = "<http://dbpedia.org/property/west>";
       String east = "<http://dbpedia.org/property/east>";
       String city = "<http://dbpedia.org/property/city>";
       String state = "<http://dbpedia.org/property/state>";
       String q = "select distinct ?object where {";
              q+= "{"+sourceUri+" "+north + " ?object} UNION ";
              q+= "{"+sourceUri+" "+northeast + " ?object} UNION ";
              q+= "{"+sourceUri+" "+northwest + " ?object} UNION ";
              q+= "{"+sourceUri+" "+south + " ?object} UNION ";
              q+= "{"+sourceUri+" "+southeast + " ?object} UNION ";
              q+= "{"+sourceUri+" "+southwest + " ?object} UNION ";
              q+= "{"+sourceUri+" "+west + " ?object} UNION ";
              q+= "{"+sourceUri+" "+east + " ?object} UNION ";
              q+= "{"+sourceUri+" "+city + " ?object} UNION ";
              q+= "{"+sourceUri+" "+state + " ?object} UNION ";
              q+= "{"+"?object"+" "+north + " "+sourceUri+"} UNION ";
              q+= "{"+"?object"+" "+northeast + " "+sourceUri+"} UNION ";
              q+= "{"+"?object"+" "+northwest + " "+sourceUri+"} UNION ";
              q+= "{"+"?object"+" "+south + " "+sourceUri+"} UNION ";
              q+= "{"+"?object"+" "+southeast + " "+sourceUri+"} UNION ";
              q+= "{"+"?object"+" "+southwest + " "+sourceUri+"} UNION ";
              q+= "{"+"?object"+" "+west + " "+sourceUri+"} UNION ";
              q+= "{"+"?object"+" "+east + " "+sourceUri+"} UNION ";
              q+= "{"+"?object"+" "+city + " "+sourceUri+"} UNION ";
              q+= "{"+"?object"+" "+state + " "+sourceUri+"} ";
              q+="}";
       // System.out.println("q = " + q);
       ArrayList<String> objects = SparqlGenerator.readTriplesFromEndPoint(q);
      // System.out.println("objects.toString() = " + objects.toString());
       for (int i = 0; i < objects.size(); i++) {
            String object = objects.get(i);
            if (targetUris.contains(object)) {
                if (!matches.contains(object)) matches.add(object);
            }
        }
        return matches;
    }

    
    static public void main (String [] args) {
        String sourceUri = "<http://dbpedia.org/resource/Flint,_Michigan>";
        ArrayList<String> targets = new ArrayList<>();
        spaceRelated(sourceUri, targets);
    }


}
