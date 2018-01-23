package objects;

import java.util.ArrayList;
import java.util.Calendar;

public class Time {
    Integer year;
    Integer month;
    Integer day;

    public Time() {
        this.year = 0;
        this.month = 0;
        this.day = 0;
    }

    public void parseDateString (String timeString) {
        if (timeString.length() == 8) {
           this.setDay(Integer.parseInt(timeString.substring(6, 8)));
           this.setMonth(Integer.parseInt(timeString.substring(4, 6)));
           this.setYear(Integer.parseInt(timeString.substring(0, 4)));
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

    public ArrayList<Time> getWeek () {
        ArrayList<Time> week = new ArrayList<>();
        Time t1 = this.getYesterday();
        Time t2 = t1.getYesterday();
        Time t3 = t2.getYesterday();
        Time t4 = t3.getYesterday();
        Time t5 = t4.getYesterday();
        Time t6 = t5.getYesterday();
        week.add(this);
        week.add(t1);
        week.add(t2);
        week.add(t3);
        week.add(t4);
        week.add(t5);
        week.add(t6);
        return week;
    }
    public Time getYesterday () {
        Time yesterday = new Time();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        calendar.roll(Calendar.DATE, -1);
        yesterday.day = (new Integer (calendar.get(Calendar.DAY_OF_MONTH)));
        yesterday.month = (new Integer (calendar.get(Calendar.MONTH)+1));
        yesterday.year = (new Integer (calendar.get(Calendar.YEAR)));
        return yesterday;
    }

    public void setNow () {
        Calendar now = Calendar.getInstance();
         this.day = (new Integer (now.get(Calendar.DAY_OF_MONTH)));
         this.month = (new Integer (now.get(Calendar.MONTH)));
         this.year = (new Integer (now.get(Calendar.YEAR)));
    }
    
    static public void main (String[] args) {
        Time now = new Time();
        now.setNow();
        Time yesterday = now.getYesterday();
        System.out.println("now.toYearMonthDayString() = " + now.toYearMonthDayString());
        System.out.println("yesterday.toYearMonthDayString() = " + yesterday.toYearMonthDayString());
    }
}
