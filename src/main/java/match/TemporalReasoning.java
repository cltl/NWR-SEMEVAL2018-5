package match;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import objects.Time;
import org.apache.jena.riot.RDFDataMgr;
import question.Questiondata;
import util.Util;
import vu.cltl.triple.objects.TrigTripleData;
import vu.cltl.triple.TrigUtil;

import java.io.File;
import java.util.*;

public class TemporalReasoning {

    static public void main (String[] args) {
       String dateString = "20170127";
        System.out.println("dateString = " + dateString);
       Time time = new Time();
       time.parseDateString(dateString);
       ArrayList<Time> week = time.getNextWeek();
        for (int i = 0; i < week.size(); i++) {
            Time time1 = week.get(i);
            System.out.println("time1.toYearMonthDayString() = " + time1.toYearMonthDayString());
        }
    }
    static public boolean matchNextWeekConstraint (String dayString, String eckgFileName) {
         Time time = new Time();
         time.parseDateString(dayString);
         ArrayList<Time> week = time.getNextWeek();
         for (int i = 0; i < week.size(); i++) {
            Time time1 = week.get(i);
            if (eckgFileName.equals(time1.toYearMonthDayString())) return true;
         }
         return false;
    }

    static public boolean matchWeekendConstraint (String dayString, String eckgFileName) {
         String timeString = dayString;
         Time time = new Time();
         time.parseDateString(dayString);
         Time nextMonday = time.getNextMonday();
         Time nextFriday = time.getNextFriday();
         if (nextMonday.before(nextFriday)) {
            timeString = nextMonday.toYearMonthDayString();
         }
         else {
            timeString = nextFriday.toYearMonthDayString();
         }
         if (eckgFileName.startsWith(timeString)) {
             return true;
         }
         return false;
    }

    static public HashMap<String, ArrayList<File>> getDocumentCreationTimeContainersWithTrigFiles (ArrayList<File> trigFiles) {
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
     * We extract the document-creation-time from the TriG file. If there is none, we take the first within document time
     * @param trigFile
     * @return
     */
    /*
        <http://www.newsreader-project.eu/data/semeval2018-5/0a3e49ad0467c6545e36d754cc08d312#tmx0>
            a                time:Instant ;
            time:inDateTime  <http://www.newsreader-project.eu/time/20170112> .
     */
    static public String getDocumentCreationTime(File trigFile) {
        Dataset dataset = RDFDataMgr.loadDataset(trigFile.getAbsolutePath());
        Time time = getDocumentCreationTime(dataset);
        dataset.close();
        return time.toYearMonthDayString();
    }

    static public Time getDocumentCreationTime(Dataset dataset) {
        Time dcTime = new Time();
        String dct = "";
        String otherTime = "";
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
            else if (otherTime.isEmpty()){
                if (s.getPredicate().getLocalName().equals("inDateTime")) {
                   if (s.getObject().isURIResource()) {
                       otherTime = s.getObject().asResource().getURI();
                       otherTime = otherTime.substring(dct.lastIndexOf("/")+1);
                   }
               }
            }
        }
        if (dct.isEmpty() && !otherTime.isEmpty())  {
                dct = otherTime;
        }
        dcTime.parseDateString(dct);
        dataset.close();
        return dcTime;
    }

    static ArrayList<Statement> getTimeStatements (Dataset dataset) {
        ArrayList<Statement> timeStatements = new ArrayList<>();
        Model namedModel = dataset.getNamedModel(TrigTripleData.instanceGraph);
        StmtIterator siter = namedModel.listStatements();
        while (siter.hasNext()) {
            Statement statement = siter.nextStatement();
            if (    statement.getPredicate().getLocalName().equals("inDateTime") ||
                    statement.getPredicate().getLocalName().equals("hasBeginning") ||
                    statement.getPredicate().getLocalName().equals("hasEnd")
                ) {
                timeStatements.add(statement);
            }
        }
        return timeStatements;
    }


    static public HashMap<String, ArrayList<File>> getTemporalContainersWithTrigFiles (
            ArrayList<File> trigFiles) {
        HashMap<String, ArrayList<File>> containers = new HashMap<String, ArrayList<File>>();
        for (int i = 0; i < trigFiles.size(); i++) {
            File trigFile = trigFiles.get(i);
           // System.out.println("trigFile = " + trigFile.getName());
            Dataset dataset = RDFDataMgr.loadDataset(trigFile.getAbsolutePath());
            ArrayList<Statement> timeStatements = getTimeStatements(dataset);
            ArrayList<Time> dominantTimes = getDominantTimes(timeStatements, false);
            if (dominantTimes.size()>0) {
                ///// there can be multiple dates, if  So files are copied to multiple buckets
                for (int j = 0; j < dominantTimes.size(); j++) {
                    Time time = dominantTimes.get(j);
                    String timeString = time.toYearMonthDayString();
                    if (containers.containsKey(timeString)) {
                        ArrayList<File> files = containers.get(timeString);
                        files.add(trigFile);
                        containers.put(timeString, files);
                    } else {
                        ArrayList<File> files = new ArrayList<>();
                        files.add(trigFile);
                        containers.put(timeString, files);
                    }
                }
            }
            else {
                String documentCreationTime = getDocumentCreationTime(dataset).toYearMonthDayString();
                //System.out.println("documentCreationTime = " + documentCreationTime);
                if (containers.containsKey(documentCreationTime)) {
                    ArrayList<File> files = containers.get(documentCreationTime);
                    files.add(trigFile);
                    containers.put(documentCreationTime, files);
                } else {
                    ArrayList<File> files = new ArrayList<>();
                    files.add(trigFile);
                    containers.put(documentCreationTime, files);
                }
            }
        }
        return containers;
    }

    static public HashMap<String, ArrayList<File>> getTemporalWeekContainersWithTrigFiles (
            ArrayList<File> trigFiles) {
        HashMap<String, ArrayList<File>> containers = new HashMap<String, ArrayList<File>>();
        for (int i = 0; i < trigFiles.size(); i++) {
            File trigFile = trigFiles.get(i);
           // System.out.println("trigFile = " + trigFile.getName());
            Dataset dataset = RDFDataMgr.loadDataset(trigFile.getAbsolutePath());
            ArrayList<Statement> timeStatements = getTimeStatements(dataset);
            ArrayList<Time> dominantTimes = getDominantTimes(timeStatements, false);
            if (dominantTimes.size()>0) {
                ///// there can be multiple dates, if  So files are copied to multiple buckets
                for (int j = 0; j < dominantTimes.size(); j++) {
                    Time time = dominantTimes.get(j);
                    String timeString = time.toYearMonthWeekString();
                    if (containers.containsKey(timeString)) {
                        ArrayList<File> files = containers.get(timeString);
                        files.add(trigFile);
                        containers.put(timeString, files);
                    } else {
                        ArrayList<File> files = new ArrayList<>();
                        files.add(trigFile);
                        containers.put(timeString, files);
                    }
                }
            }
            else {
                String documentCreationTime = getDocumentCreationTime(dataset).toYearMonthWeekString();
                //System.out.println("documentCreationTime = " + documentCreationTime);
                if (containers.containsKey(documentCreationTime)) {
                    ArrayList<File> files = containers.get(documentCreationTime);
                    files.add(trigFile);
                    containers.put(documentCreationTime, files);
                } else {
                    ArrayList<File> files = new ArrayList<>();
                    files.add(trigFile);
                    containers.put(documentCreationTime, files);
                }
            }
        }
        return containers;
    }

    static public HashMap<String, ArrayList<File>> getTemporalWeekendContainersWithTrigFiles (
            ArrayList<File> trigFiles) {
        HashMap<String, ArrayList<File>> containers = new HashMap<String, ArrayList<File>>();
        for (int i = 0; i < trigFiles.size(); i++) {
            File trigFile = trigFiles.get(i);
           // System.out.println("trigFile = " + trigFile.getName());
            Dataset dataset = RDFDataMgr.loadDataset(trigFile.getAbsolutePath());
            ArrayList<Statement> timeStatements = getTimeStatements(dataset);
            ArrayList<Time> dominantTimes = getDominantTimes(timeStatements, false);
            if (dominantTimes.size()>0) {
                ///// there can be multiple dates, if  So files are copied to multiple buckets
                for (int j = 0; j < dominantTimes.size(); j++) {
                    Time time = dominantTimes.get(j);
                    String timeString = time.toYearMonthString();
                    Time nextMonday = time.getNextMonday();
                    Time nextFriday = time.getNextFriday();
                    if (nextMonday.before(nextFriday)) {
                        timeString = nextMonday.toYearMonthDayString();
                    }
                    else {
                        timeString = nextFriday.toYearMonthDayString();
                    }
                    if (containers.containsKey(timeString)) {
                        ArrayList<File> files = containers.get(timeString);
                        if (!Util.hasFile(files, trigFile)) {
                            files.add(trigFile);
                            containers.put(timeString, files);
                        }
                    } else {
                        ArrayList<File> files = new ArrayList<>();
                        files.add(trigFile);
                        containers.put(timeString, files);
                    }
                }
            }
            else {
                Time documentCreationTime = getDocumentCreationTime(dataset);
                //System.out.println("documentCreationTime = " + documentCreationTime);
                String timeString = documentCreationTime.toYearMonthString();

                Time nextMonday = documentCreationTime.getNextMonday();
                Time nextFriday = documentCreationTime.getNextFriday();
                if (nextMonday.before(nextFriday)) {
                    timeString = nextMonday.toYearMonthDayString();
                }
                else {
                    timeString = nextFriday.toYearMonthDayString();
                }
                if (containers.containsKey(timeString)) {
                    ArrayList<File> files = containers.get(timeString);
                    files.add(trigFile);
                    containers.put(timeString, files);
                } else {
                    ArrayList<File> files = new ArrayList<>();
                    files.add(trigFile);
                    containers.put(timeString, files);
                }
            }
        }
        return containers;
    }

    /**
     * Duplicates trig files for previous & next week
     * @param trigFiles
     * @return
     */
    static public HashMap<String, ArrayList<File>> getLooseTemporalContainersWithTrigFiles (
            ArrayList<File> trigFiles) {
        HashMap<String, ArrayList<File>> containers = new HashMap<String, ArrayList<File>>();
        for (int i = 0; i < trigFiles.size(); i++) {
            File trigFile = trigFiles.get(i);
            Dataset dataset = RDFDataMgr.loadDataset(trigFile.getAbsolutePath());
            ArrayList<Statement> timeStatements = getTimeStatements(dataset);
            ArrayList<Time> dominantTimes = getDominantTimes(timeStatements, false);
            if (dominantTimes.size()==0) {
                Time documentCreationTime = getDocumentCreationTime(dataset);
                dominantTimes.add(documentCreationTime);
            }
            ArrayList<Time> allWeeks = new ArrayList<>();
            for (int j = 0; j < dominantTimes.size(); j++) {
                Time time = dominantTimes.get(j);
                ArrayList<Time> week = time.getPreviousWeek();
                for (int k = 0; k < week.size(); k++) {
                    Time time1 = week.get(k);
                    Time.addToTimeList(allWeeks, time1);
                }
                week = time.getNextWeek();
                for (int k = 0; k < week.size(); k++) {
                    Time time1 = week.get(k);
                    Time.addToTimeList(allWeeks, time1);
                }
            }
            ///// there can be multiple dates, if  So files are copied to multiple buckets
            for (int j = 0; j < allWeeks.size(); j++) {
                Time time =  allWeeks.get(j);
                String timeString = time.toYearMonthDayString();
                if (containers.containsKey(timeString)) {
                    ArrayList<File> files = containers.get(timeString);
                    files.add(trigFile);
                    containers.put(timeString, files);
                } else {
                    ArrayList<File> files = new ArrayList<>();
                    files.add(trigFile);
                    containers.put(timeString, files);
                }
            }

        }
        return containers;
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
           if (seckgMap.containsKey(eventKey)) {
               ArrayList<Statement> secondaryStatements = seckgMap.get(eventKey);
               if (DEBUG) {
                   System.out.println("secondaryStatements = " + secondaryStatements.size());
               }
               ArrayList<Time> dominantDates = getDominantTimes(secondaryStatements, DEBUG);
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


    static ArrayList<Time> getDominantTimes (ArrayList<Statement> allstatements, boolean DEBUG) {
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

         0a3e49ad0467c6545e36d754cc08d312.naf
         SPD Chief : Brothers killed were lured to Grant Ave , shot during robbery ; 3 arrested Syracuse Police Chief Frank Fowler says the two brothers who were fatally shot on Grant Avenue on Tuesday had been lured down there to be robbed .
          During a news conference Thursday morning Chief Fowler announced the arrests of three suspects , all of whom are facing murder and attempted robbery charges .
          The suspects have been named as 17-year - old Sheahonnie Davis , 19-year - old Toriano Newson and 23-year - old Curtis Paige III.
          Newson is accused of firing the shots that killed 17-year - old Daniel Diaz - Marrero and 19-year - old Ismael Diaz - Marrero , and has been charged with two counts of first - degree murder and first - degree attempted robbery . Davis and Paige have been charged with two counts of second - degree murder and first - degree attempted robbery . All three suspects are being held at the Onondaga County Justice Center .
          Court documents indicate the victims were lured to Grant Avenue to meet with Davis through text and Facebook messages . When they arrived the two other suspects , Newson and Paige , approached them attempted to rob them . During the attempted robbery , Newson shot both Daniel and Ismael , according to Chief Fowler . Court documents say several shots struck Daniel Diaz - Marrero in the chest " at close range " and a single shot struck his brother Ismael in the chest .
          The felony complaints filed against Newson and Paige charge that the trio had lured the victims to Grant Avenue with the intent to forcibly steal marijuana from them .
          The incident was no accident , according to Chief Fowler . He says all three suspects knew each other - Davis and Newson had a previous relationship and both were acquainted with Paige - and had plotted to commit the robbery ahead of time . Chief Fowler added that there does n't appear to be any particular reason why they targeted the Diaz - Marrero brothers .
          Chief Fowler says the weapon used has not been recovered as of yet , but emphasized that the investigation is still ongoing . He also made a plea to any witnesses who may have witnessed the crime to contact police at 315 - 442 - 5222 .
          Syracuse Police were called to the 300 block of Grant Avenue to reports of a shooting at about 5:04 p.m. Tuesday . When they arrived , Officers found two adult males suffering from gunshot wounds .
          One victim was pronounced dead at the scene , the other was taken to Upstate University Hospital in critical condition where he succumbed to his injuries .
          In a post on Facebook , Le Moyne President Linda M. LeMura , Ph.D. , said Ismael was a sophomore business student a Le Moyne . A remembrance was held Wednesday afternoon in the Grewen Auditorium .
          RELATED | Le Moyne College remembers life of student killed in Syracuse 's first homicide of 2017
          Syracuse City Schools Spokesperson Michael Henesey says Daniel was a senior at Fowler High School . The district 's crisis response team will be at the school to help grieving students and staff . His older brother Ismael graduated from Henninger High School in 2015 .</raw>
         */
        HashMap<Integer, Integer> years = new HashMap<>();
        HashMap<Integer, Integer> months = new HashMap<>();
        ArrayList<Time> timeArrayList = new ArrayList<Time>();
        ArrayList<Statement> statements = Util.filterBySentences(allstatements, 2);
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
                //System.out.println("timeString = " + timeString);
                time.parseDateString(timeString);
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
                else {
                  //  System.out.println("time.toYearMonthDayString() = " + time.toYearMonthDayString());
                }
            }
        }
        Integer domYear = getMostFrequent(years);
        //System.out.println("domYear = " + domYear);
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
        }
/*        sortHashMap(years);
        sortHashMap(months);*/
        return dominantTimeArrayList;
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
