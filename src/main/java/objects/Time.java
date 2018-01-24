package objects;

import java.util.ArrayList;
import java.util.Calendar;

public class Time {
    Integer year;
    Integer month;
    Integer day;
    Integer week;
    Integer weekend;

    public Time() {
        this.year = 0;
        this.month = 0;
        this.day = 0;
        this.week = 0;
        this.weekend= 0;
    }
    public Time(Time aTime) {
        this.year = aTime.year;
        this.month = aTime.month;
        this.day = aTime.day;
        this.week = aTime.week;
        this.weekend= aTime.weekend;
    }

    public void parseDateString (String timeString) {
        if (timeString.length() == 8) {
           this.setDay(Integer.parseInt(timeString.substring(6, 8)));
           this.setMonth(Integer.parseInt(timeString.substring(4, 6)));
           this.setYear(Integer.parseInt(timeString.substring(0, 4)));
           this.setWeek();
       } else if (timeString.length() == 6) {
           this.setMonth(Integer.parseInt(timeString.substring(4, 6)));
           this.setYear(Integer.parseInt(timeString.substring(0, 4)));
       } else if (timeString.length() == 4) {
           this.setYear(Integer.parseInt(timeString.substring(0, 4)));
       } else {
            //System.out.println("error timeString = " + timeString);
           /// we have a problem
       }
      //  System.out.println("this.toYearMonthDayString() = " + this.toYearMonthDayString());
    }
    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getWeek() {
        return week;
    }

    public Integer getWeekend() {
        return weekend;
    }
    public void addDay () {
        this.day++;
    }
    public void setWeekend(Integer weekend) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        //calendar.get(Calendar.)
        this.week = Calendar.WEEK_OF_MONTH;
    }

    public void setWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        this.week = Calendar.WEEK_OF_MONTH;
    }

    public String toYearMonthString () {
        String str = "";
        str += year.toString();
        if (month<10) {
            str += "0"+month.toString();
        }
        else {
            str += month.toString();
        }
        return str;
    }

    public String toYearMonthDayString () {
        String str = "";
        str += year.toString();
        if (month<10) {
            str += "0"+month.toString();
        }
        else {
            str += month.toString();
        }
        if (day<10) {
            str += "0"+day.toString();
        }
        else {
            str += day.toString();
        }
        return str;
    }

    /// We replace the day identifier by a week identifier
    public String toYearMonthWeekString () {
            String str = "";
           str += year.toString();
           if (month<10) {
               str += "0"+month.toString();
           }
           else {
               str += month.toString();
           }
           str += "0"+week.toString();
           return str;
    }

    static public void addToTimeList (ArrayList<Time> list, Time time) {
        boolean has = false;
        for (int i = 0; i < list.size(); i++) {
            Time listTime = list.get(i);
            if (listTime.toYearMonthDayString().equals(time.toYearMonthDayString())) {
               has = true; break;
            }
        }
        if (!has) list.add(time);
    }

    public ArrayList<Time> getPreviousWeek () {
        ArrayList<Time> week = new ArrayList<>();
        Time t1 = this.slideTime(-1);
        Time t2 = this.slideTime(-2);
        Time t3 = this.slideTime(-3);
        Time t4 = this.slideTime(-4);
        Time t5 = this.slideTime(-5);
        Time t6 = this.slideTime(-6);
        week.add(this);
        week.add(t1);
        week.add(t2);
        week.add(t3);
        week.add(t4);
        week.add(t5);
        week.add(t6);
        return week;
    }

    public ArrayList<Time> getNextWeek () {
        ArrayList<Time> week = new ArrayList<>();
        Time t1 = this.slideTime(1);
        Time t2 = this.slideTime(2);
        Time t3 = this.slideTime(3);
        Time t4 = this.slideTime(4);
        Time t5 = this.slideTime(5);
        Time t6 = this.slideTime(6);
        week.add(this);
        week.add(t1);
        week.add(t2);
        week.add(t3);
        week.add(t4);
        week.add(t5);
        week.add(t6);
        return week;
    }

    public Time slideTime (int days) {
        Time yesterday = new Time();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        calendar.add(Calendar.DATE, days);
        yesterday.day = (new Integer (calendar.get(Calendar.DAY_OF_MONTH)));
        yesterday.month = (new Integer (calendar.get(Calendar.MONTH)));
        yesterday.year = (new Integer (calendar.get(Calendar.YEAR)));
        yesterday.setWeek();
        return yesterday;
    }



    public void setNow () {
         Calendar now = Calendar.getInstance();
         this.day = (new Integer (now.get(Calendar.DAY_OF_MONTH)));
         this.month = (new Integer (now.get(Calendar.MONTH)+1));
         this.year = (new Integer (now.get(Calendar.YEAR)));
         this.setWeek();
    }


    public Time getNextMonday () {
        Time friday = new Time(this);
        Calendar calendar = Calendar.getInstance();
        calendar.set(friday.year, friday.month-1, friday.day);
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DATE, 1);
        }
       
        friday.setCalendar(calendar);
        return friday;
    }

    public Time getNextFriday () {
        Time friday = new Time(this);
        Calendar calendar = Calendar.getInstance();
        calendar.set(friday.year, friday.month-1, friday.day);
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
            calendar.add(Calendar.DATE, 1);
        }
        friday.setCalendar(calendar);
        return friday;
    }

    public boolean before (Time aTime) {
        Integer date = Integer.parseInt(this.toYearMonthDayString());
        Integer aDate = Integer.parseInt(aTime.toYearMonthDayString());
        if (date<=aDate) return true;
        else return false;
    }

    public void setCalendar (Calendar calendar) {
        this.year=(new Integer (calendar.get(Calendar.YEAR)));
        this.month=(new Integer (calendar.get(Calendar.MONTH)+1));
        this.day=(new Integer (calendar.get(Calendar.DAY_OF_MONTH)));
    }

    static public void main (String[] args) {
        Time now = new Time();
        now.setNow();
        //now.setDay(20);
        System.out.println("now.toYearMonthDayString() = " + now.toYearMonthDayString());
        System.out.println("now.toYearMonthWeekString() = " + now.toYearMonthWeekString());
        System.out.println("friday = " + now.getNextFriday().toYearMonthDayString());
        System.out.println("monday = " + now.getNextMonday().toYearMonthDayString());

    }
}
