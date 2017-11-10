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
    String verbose_question;

    public Questiondata() {
        init();
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

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
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
}
