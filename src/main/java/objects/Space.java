package objects;

import vu.wntools.util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Space {
    static public HashMap<String, ArrayList<String>> cityMap = new HashMap();
    static public HashMap<String, ArrayList<String>> stateMap = new HashMap();
    static public ArrayList<String> locationURIs = new ArrayList<>();




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

    static public void initCities (File input) {
        initMap(input, cityMap);
    }
    static public void initStates (File input) {
        initMap(input, stateMap);
    }

    static public void initMap(File inputFile, HashMap<String, ArrayList<String>> map) {
        if (inputFile.exists() ) {
            try {
                FileInputStream fis = new FileInputStream(inputFile);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    //System.out.println(inputLine);
                    if (inputLine.trim().length()>0) {
                        String[] fields = inputLine.trim().split("\t");
                        if (fields.length==2) {
                            String uri = fields[0];
                            if (uri.startsWith("<")) {
                                uri = uri.substring(1);
                            }
                            if (uri.endsWith(">")) {
                                uri = uri.substring(0, uri.length()-1);
                            }
                            String []targets = fields[1].split(", ");
                            ArrayList<String> targetList = new ArrayList<>();
                            for (int i = 0; i < targets.length; i++) {
                                String target = targets[i].trim();
                                if (target.startsWith("[")) {
                                    target = target.substring(1);
                                }
                                if (target.endsWith("]")) {
                                    target = target.substring(0, target.length()-1);
                                   // System.out.println("target = " + target);
                                }
                                if (target.startsWith("http://")) {
                                    targetList.add(target);
                                }
                            }
                            locationURIs.add(uri);
                            locationURIs.addAll(targetList);
                            map.put(uri, targetList);
                        }
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    static public  ArrayList<String> spaceRelatedSparql (String sourceUri, ArrayList<String> targetUris) {
        ArrayList<String> matches = new ArrayList<>();
       String q = makeQuery(sourceUri);
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
        try {
            String file = "/Users/piek/Desktop/SemEval2018/scripts/states";
            OutputStream fos = new FileOutputStream(file+".rel");
            ArrayList<String> sources =  Util.readFileToArrayList(file);
            for (int i = 0; i < sources.size(); i++) {
                String uri = "<"+sources.get(i)+">";
                String q = makeQuery(uri);
                ArrayList<String> objects = SparqlGenerator.readTriplesFromEndPoint(q);
                String str = uri+"\t"+objects.toString()+"\n";
                fos.write(str.getBytes());
               // System.out.println(str);
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public String makeQuery(String sourceUri) {
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
                     return q;
    }
}
