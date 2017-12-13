package match;

import com.hp.hpl.jena.rdf.model.Statement;
import objects.Time;
import vu.cltl.triple.TrigUtil;

import java.util.*;

public class TemporalReasoning {

    static final int minYear = 2000;
    static final int maxYear = 2017;

    static public HashMap<String, ArrayList<String>> getTemporalContainers (
            HashMap<String, ArrayList<Statement>> eckgMap,
            HashMap<String, ArrayList<Statement>> seckgMap,
            MatchSettings matchSettings) {
        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
        Set keySet = eckgMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
           String eventKey = keys.next();
           String timeString = "";
           if (seckgMap.containsKey(eventKey)) {
               ArrayList<Statement> secondaryStatements = seckgMap.get(eventKey);
               ArrayList<Time> dominantDates = getDominantTimeStrings(secondaryStatements);
               if (dominantDates.size()>0) {
                   if (matchSettings.isDay()) {
                       timeString = dominantDates.get(0).toYearMonthDayString();
                   }
                   else {
                       timeString = dominantDates.get(0).toYearMonthString();
                   }
               }
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

    static ArrayList<Time> getDominantTimeStrings (ArrayList<Statement> statements) {
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
            if (statement.getPredicate().getLocalName().equals("inDateTime")) {
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
                    if (time.getYear() >= minYear && time.getYear() <= maxYear) {
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


}
