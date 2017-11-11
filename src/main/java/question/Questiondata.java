package question;

import org.json.simple.JSONObject;

/**
 * Created by piek on 09/11/2017.
 */
public class Questiondata {
    /**
     *
     * The keys of this JSON file are question IDs ("1-5109" in this example)
     each question contains one event type ("event_type")
     each question contains exactly two of the three event properties ("participant", "time", "location").
     each question contains a "subtask" field, which is always 1 for S1
     each question contains a field "verbose_question", which is the summary of the question in free text, for human readers.

     "3-58108": {
     "event_type": "killing",
     "location": {
     "city": "http://dbpedia.org/resource/Hayward,_California"
     },
     "subtask": 3,
     "time": {
     "day": "14/03/2017"
     },
     "verbose_question": "How many people were killed in 14/03/2017 (day) in ('California', 'Hayward') (city) ?"
     },
     "3-58795": {
     "event_type": "killing",
     "location": {
     "state": "http://dbpedia.org/resource/California"
     },
     "subtask": 3,
     "time": {
     "month": "01/2017"
     },
     "verbose_question": "How many people were killed in 01/2017 (month) in ('California',) (state) ?"
     },
     */

    String id;
    String event_type;
    String state;
    String city;
    String year;
    String month;
    String day;
    String subtask;
    String participant_first;
    String participant_last;
    String verbose_question;

    public Questiondata() {
        init();
    }


    public  void getValue(JSONObject jsonObj) {
        for (Object key : jsonObj.keySet()) {
            //based on you key types
            String keyStr = (String)key;
            Object keyvalue = jsonObj.get(keyStr);

            //Print key and value
           // System.out.println("key: "+ keyStr + " value: " + keyvalue);

            //for nested objects iteration if required
            if (keyvalue instanceof JSONObject)
                getValue((JSONObject)keyvalue);
            else {
                if (keyStr.equals("event_type")) event_type =  keyvalue.toString();
                if (keyStr.equals("state")) state =  keyvalue.toString();
                if (keyStr.equals("city")) city =  keyvalue.toString();
                if (keyStr.equals("year")) year =  keyvalue.toString();
                if (keyStr.equals("month")) month =  keyvalue.toString();
                if (keyStr.equals("day")) day =  keyvalue.toString();
                if (keyStr.equals("verbose_question")) verbose_question =  keyvalue.toString();
                if (keyStr.equals("subtask")) subtask =  keyvalue.toString();
                if (keyStr.equals("first")) participant_first =  keyvalue.toString();
                if (keyStr.equals("last")) participant_last =  keyvalue.toString();
            }
        }
    }

    public Questiondata(String id, JSONObject jsonObject) {
        init();
        this.id = id;
        getValue(jsonObject);
    }

    void init() {
         id="";
         event_type="";
         state="";
         city="";
         year="";
         month="";
         day="";
         subtask="";
         verbose_question="";
         participant_first="";
         participant_last="";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEvent_type() {
        return event_type;
    }

    public void setEvent_type(String event_type) {
        this.event_type = event_type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public String getNormalisedMonth() {
        return getNormalisedDate(month);
    }



    public String getNormalisedDate(String dateString) {
        String normalisedData = "";
        String [] fields = dateString.split("/");
        for (int i = 0; i < fields.length; i++) {
            normalisedData = fields[i]+normalisedData;

        }
        return normalisedData;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public String getNormalisedDay() {
        return getNormalisedDate(day);
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getSubtask() {
        return subtask;
    }

    public void setSubtask(String subtask) {
        this.subtask = subtask;
    }

    public String getVerbose_question() {
        return verbose_question;
    }

    public void setVerbose_question(String verbose_question) {
        this.verbose_question = verbose_question;
    }

    public String toString() {
        String str = "id = " + id+"\n";
        str += "\tevent_type = " + event_type+"\n";
        str += "\tstate = " + state+"\n";
        str += "\tcity = " + city+"\n";
        str += "\tyear = " + year+"\n";
        str += "\tmonth = " + month+"\n";
        str += "\tday = " + day+"\n";
        str += "\tsubtask = " + subtask+"\n";
        str += "\tparticipant_first = " + participant_first+"\n";
        str += "\tparticipant_last = " + participant_last+"\n";
        return str;
    }
}
