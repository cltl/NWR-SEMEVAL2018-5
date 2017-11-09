package question;

/**
 * Created by piek on 09/11/2017.
 */
public class Questiondata {
    /**
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
     */

    String id;
    String event_type;
    String location;
    String city;
    String time;
    String subtask;
    String verbose_question;

    public Questiondata() {
        init();
    }

    void init() {
         id="";
         event_type="";
         location="";
         city="";
         time="";
         subtask="";
         verbose_question="";
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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
}
