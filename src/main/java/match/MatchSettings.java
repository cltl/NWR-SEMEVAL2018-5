package match;

/**
 * Created by piek on 15/11/2017.
 */
public class MatchSettings {

    private boolean matchAny;
    private boolean matchEnActor;
    private boolean matchNeActor;
    private boolean matchDbpActor;
    private boolean matchAnyPlace;
    private boolean matchDbpPlace;
    private boolean day;
    private Integer tripleMatchThreshold;
    private Integer editDistanceThreshold;

    public MatchSettings() {
        init();
    }


    void init () {
        this.matchAny = false;
        this.matchEnActor = false;
        this.matchNeActor = false;
        this.matchDbpActor = false;
        this.matchAnyPlace = false;
        this.matchDbpPlace = false;
        this.day = true;
        this.tripleMatchThreshold = 5;
        this.editDistanceThreshold = 1;
    }

    public void setLoose () {
            this.matchEnActor = true;
            this.matchNeActor = true;
            this.matchDbpActor = true;
            this.matchAnyPlace = true;
            this.matchDbpPlace = true;
            this.day = true;
            this.tripleMatchThreshold = 2;
            this.editDistanceThreshold = 3;
    }

    public void parseArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--dbp")) {
                matchDbpActor = true;
            }
            else if (arg.equals("--entity")) {
                matchEnActor = true;
            }
            else if (arg.equals("--non-entity")) {
                matchNeActor = true;
            }
            else if (arg.equals("--dbp-place")) {
                matchDbpPlace = true;
            }
            else if (arg.equals("--place")) {
                matchAnyPlace = true;
            }
            else if (arg.equals("--any")) {
                matchAny = true;
            }
            else if (arg.equals("--day")) {
                day = true;
            }
            else if (arg.equals("--n-triples") && args.length>(i+1)) {
                tripleMatchThreshold = Integer.parseInt(args[i+1]);
            }
            else if (arg.equals("--levenshtein") && args.length>(i+1)) {
                editDistanceThreshold = Integer.parseInt(args[i+1]);
            }
        }
    }

    public boolean isMatchAny() {
        return matchAny;
    }

    public void setMatchAny(boolean matchAny) {
        this.matchAny = matchAny;
    }

    public void setMatchDbpActor(boolean match) {
        this.matchDbpActor = match;
    }
    public void setMatchDbpPlace(boolean match) {
        this.matchDbpPlace = match;
    }
    public void setMatchEnActor(boolean matchEnActor) {this.matchEnActor = matchEnActor; }
    public void setMatchNeActor(boolean matchNeActor) {this.matchNeActor = matchNeActor; }
    public void setMatchAnyPlace(boolean matchAnyPlace) { this.matchAnyPlace = matchAnyPlace;  }
    public boolean isMatchEnActor() {return matchEnActor; }
    public boolean isMatchNeActor() {return matchNeActor; }
    public boolean isMatchDbpActor() {return matchDbpActor; }
    public boolean isMatchDbpPlace() {return matchDbpPlace;}
    public boolean isMatchAnyPlace() {  return matchAnyPlace; }

    public boolean isDay() {
        return day;
    }

    public void setDay(boolean day) {
        this.day = day;
    }

    public Integer getEditDistanceThreshold() {
        return editDistanceThreshold;
    }

    public void setEditDistanceThreshold(Integer editDistanceThreshold) {
        this.editDistanceThreshold = editDistanceThreshold;
    }

    public Integer getTripleMatchThreshold() {
        return tripleMatchThreshold;
    }

    public void setTripleMatchThreshold(Integer tripleMatchThreshold) {
        this.tripleMatchThreshold = tripleMatchThreshold;
    }

    public String getSettings() {
        String str ="Settings:\n";
        str += ".matchAny = "+ matchAny +"\n";
        str += ".matchEnActor = "+ matchEnActor+"\n";
        str += ".matchNeActor = "+ matchNeActor+"\n";
        str += ".matchDbpActor = "+ matchDbpActor+"\n";
        str += ".matchAnyPlace = "+ matchAnyPlace+"\n";
        str += ".matchDbpPlace = "+ matchDbpPlace+"\n";
        str += ".day = "+ day+"\n";
        str += ".tripleMatchThreshold = "+ tripleMatchThreshold+"\n";
        str += ".editDistanceThreshold = "+ editDistanceThreshold+"\n";
        return str;
    }
}
