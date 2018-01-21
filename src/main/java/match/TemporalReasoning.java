package match;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import objects.Time;
import org.apache.jena.riot.RDFDataMgr;
import question.Questiondata;
import vu.cltl.triple.objects.TrigTripleData;
import vu.cltl.triple.TrigUtil;

import java.io.File;
import java.util.*;

public class TemporalReasoning {

    static public HashMap<String, ArrayList<File>> getTemporalContainersWithTrigFiles (ArrayList<File> trigFiles) {
        HashMap<String, ArrayList<File>> containers = new HashMap<>();
        for (int i = 0; i < trigFiles.size(); i++) {
            File trigFile = trigFiles.get(i);
            String documentCreationTime = getDocumentCreationTime(trigFile);
            //System.out.println("documentCreationTime = " + documentCreationTime);
            if (containers.containsKey(documentCreationTime)) {
                ArrayList<File> files= containers.get(documentCreationTime);
                files.add(trigFile);
                containers.put(documentCreationTime, files);
            }
            else {
                ArrayList<File> files= new ArrayList<>();
                files.add(trigFile);
                containers.put(documentCreationTime, files);
            }
        }
        return containers;
    }


    /**
     *
     * @param trigFile
     * @return
     */
    /*
        <http://www.newsreader-project.eu/data/semeval2018-5/0a3e49ad0467c6545e36d754cc08d312#tmx0>
            a                time:Instant ;
            time:inDateTime  <http://www.newsreader-project.eu/time/20170112> .
     */
    static public String getDocumentCreationTime(File trigFile) {
        String dct = "NODCT";
        Dataset dataset = RDFDataMgr.loadDataset(trigFile.getAbsolutePath());
        Model namedModel = dataset.getNamedModel(TrigTripleData.instanceGraph);
        StmtIterator siter = namedModel.listStatements();
        while (siter.hasNext()) {
            Statement s = siter.nextStatement();
            String subject = s.getSubject().getURI();
            if (subject.endsWith("#tmx0")) {
               if (s.getPredicate().getLocalName().equals("inDateTime")) {
                   if (s.getObject().isURIResource()) {
                       dct = s.getObject().asResource().getURI();
                       dct = dct.substring(dct.lastIndexOf("/")+1);
                       //System.out.println("dct = " + dct);
                       break;
                   }
               }
            }
        }
        dataset.close();
        return dct;
    }

    static public HashMap<String, ArrayList<String>> getTemporalContainers (
            HashMap<String, ArrayList<Statement>> eckgMap,
            HashMap<String, ArrayList<Statement>> seckgMap,
            MatchSettings matchSettings) {
        boolean DEBUG = false;
        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
        Set keySet = eckgMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
           String eventKey = keys.next();
           String timeString = "";
            /*if (eventKey.endsWith("e1e2f54c3fb0c19ac3f9c4c3857269d5#ev1")) {
                System.out.println("eventKey = " + eventKey);
                DEBUG = true;
            }
            else {
                DEBUG = false;
            }*/
           if (seckgMap.containsKey(eventKey)) {
               ArrayList<Statement> secondaryStatements = seckgMap.get(eventKey);
               if (DEBUG) {
                   System.out.println("secondaryStatements = " + secondaryStatements.size());
               }
               ArrayList<Time> dominantDates = getDominantTimeStrings(secondaryStatements, DEBUG);
               if (dominantDates.size()>0) {
                   if (matchSettings.isDay()) {
                       timeString = dominantDates.get(0).toYearMonthDayString();
                   }
                   else {
                       timeString = dominantDates.get(0).toYearMonthString();
                   }
               }
               else {
                   if (DEBUG) {
                       System.out.println("no dominantDates for:" + eventKey);
                   }
               }
           }
           else {
               if (DEBUG) {
                   System.out.println("no secundary information for:" + eventKey);
               }
           }
        if (DEBUG) {
               System.out.println("e1e2f54c3fb0c19ac3f9c4c3857269d5#ev10 timeString:" + timeString);
           }
           if (timeString.isEmpty()) {
               timeString = "NOTIME";
           }
           if (map.containsKey(timeString)) {
               ArrayList<String> events = map.get(timeString);
               events.add(eventKey);
               map.put(timeString, events);
           }
           else {
               ArrayList<String> events = new ArrayList<>();
               events.add(eventKey);
               map.put(timeString, events);
           }
        }
        return map;
    }

    static ArrayList<Time> getDominantTimeStrings (ArrayList<Statement> statements, boolean DEBUG) {
            //nwr:tmx1	inDateTime	20170131
            //nwr:tmx1	inDateTime	201701
            //nwr:tmx1	inDateTime	2017
            //"day": "14/01/2017"
            //"month": "01/2017"
            //"year": "2017"
        /**
         *     <http://www.newsreader-project.eu/time/20170131>
                     a              time:DateTimeDescription ;
                     time:day       "---31"^^<http://www.w3.org/2001/XMLSchema#gDay> ;
                     time:month     "--01"^^<http://www.w3.org/2001/XMLSchema#gMonth> ;
                     time:unitType  time:unitDay ;
                     time:year      "2017"^^<http://www.w3.org/2001/XMLSchema#gYear> .

         <http://www.newsreader-project.eu/data/wikinews/07494133b6fc9cc255b79dbff1eb3623#tmx1>
                 a                time:Instant ;
                 rdfs:label       "Tuesday night" ;
                 gaf:denotedBy    <http://www.newsreader-project.eu/data/wikinews/07494133b6fc9cc255b79dbff1eb3623#char=167,180> ;
                 time:inDateTime  <http://www.newsreader-project.eu/time/20170131> .

         */
        HashMap<Integer, Integer> years = new HashMap<>();
        HashMap<Integer, Integer> months = new HashMap<>();
        ArrayList<Time> timeArrayList = new ArrayList<Time>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (DEBUG) {
                System.out.println("statement.getPredicate().getLocalName() = " + statement.getPredicate().getLocalName());
            }
            if (    statement.getPredicate().getLocalName().equals("inDateTime") ||
                    statement.getPredicate().getLocalName().equals("hasBeginning") ||
                    statement.getPredicate().getLocalName().equals("hasEnd")
                    ) {
                String timeString = TrigUtil.getPrettyNSValue(statement.getObject().toString());
                Time time = new Time();
               // System.out.println("timeString = " + timeString);
                if (timeString.length() == 8) {
                    time.setDay(Integer.parseInt(timeString.substring(6, 8)));
                    time.setMonth(Integer.parseInt(timeString.substring(4, 6)));
                    time.setYear(Integer.parseInt(timeString.substring(0, 4)));
                } else if (timeString.length() == 6) {
                    time.setMonth(Integer.parseInt(timeString.substring(4, 6)));
                    time.setYear(Integer.parseInt(timeString.substring(0, 4)));
                } else if (timeString.length() == 4) {

                    time.setYear(Integer.parseInt(timeString.substring(0, 4)));
                } else {
                    /// we have a problem
                }
                if (time.getYear() != 0) {
                    timeArrayList.add(time);
                    if (years.containsKey(time.getYear())) {
                        Integer cnt = years.get(time.getYear());
                        cnt++;
                        years.put(time.getYear(), cnt);
                    } else {
                        years.put(time.getYear(), 1);
                    }
                }
            }
        }
        Integer domYear = getMostFrequent(years);
        for (int i = 0; i < timeArrayList.size(); i++) {
            Time time = timeArrayList.get(i);
            if (time.getYear().equals(domYear)) {
                if (time.getMonth() != 0) {
                   if (months.containsKey(time.getMonth())) {
                       Integer cnt = months.get(time.getMonth());
                       cnt++;
                       months.put(time.getMonth(), cnt);
                   } else {
                       months.put(time.getMonth(), 1);
                   }
               }
            }
        }
        Integer domMonth = getMostFrequent(months);
        ArrayList<Time> dominantTimeArrayList = new ArrayList<Time>();
        for (int i = 0; i < timeArrayList.size(); i++) {
           Time time = timeArrayList.get(i);
           if (time.getYear().equals(domYear) && time.getMonth().equals(domMonth)) {
               dominantTimeArrayList.add(time);
           }
        }/*
        sortHashMap(years);
        sortHashMap(months);
        sortHashMap(days);*/

        return timeArrayList;
    }


    static Integer getMostFrequent (HashMap<Integer, Integer> map ) {
        Integer label = 0;
        Integer max = 0;
        Object[] a = map.entrySet().toArray();
        for (Object e : a) {
            if (((Map.Entry<Integer, Integer>) e).getValue() > max) {
                max = ((Map.Entry<Integer, Integer>) e).getValue();
                label = ((Map.Entry<Integer, Integer>) e).getKey();
            }
        }
        return label;
    }

    static void sortHashMap (HashMap<Integer, Integer> map ) {

        Object[] a = map.entrySet().toArray();
        Arrays.sort(a, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Map.Entry<String, Integer>) o2).getValue()
                        .compareTo(((Map.Entry<String, Integer>) o1).getValue());
            }
        });
        for (Object e : a) {
            System.out.println(((Map.Entry<String, Integer>) e).getKey() + " : "
                    + ((Map.Entry<String, Integer>) e).getValue());
        }
    }


    static boolean matchTimeConstraint (ArrayList<Statement> statements, Questiondata questiondata) {
            //nwr:tmx1	inDateTime	20170131
            //nwr:tmx1	inDateTime	201701
            //nwr:tmx1	inDateTime	2017
            //"day": "14/01/2017"
            //"month": "01/2017"
            //"year": "2017"
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getPredicate().getLocalName().equals("inDateTime")) {
                //System.out.println("TrigUtil.getPrettyNSValue(statement.getObject().toString() = " + TrigUtil.getPrettyNSValue(statement.getObject().toString()));
                if (TrigUtil.getPrettyNSValue(statement.getObject().toString()).equals(questiondata.getYear())) {
                    return true;
                } else if (TrigUtil.getPrettyNSValue(statement.getObject().toString()).equals(questiondata.getNormalisedDay())) {
                    return true;
                } else if (TrigUtil.getPrettyNSValue(statement.getObject().toString()).equals(questiondata.getNormalisedDay())) {
                    return true;
                }
            }
        }
        return false;
    }

}
