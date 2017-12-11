package objects;

public class Time {
    Integer year;
    Integer month;
    Integer day;

    public Time() {
        this.year = 0;
        this.month = 0;
        this.day = 0;
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
        str += year.toString()+month.toString();
        return str;
    }
}
