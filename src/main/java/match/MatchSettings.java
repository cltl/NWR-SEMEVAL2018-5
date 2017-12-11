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
    private Integer tripleMatchThreshold;

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
        this.tripleMatchThreshold = 5;
    }

    public void alltrue () {
            this.matchEnActor = true;
            this.matchNeActor = true;
            this.matchDbpActor = true;
            this.matchAnyPlace = true;
            this.matchDbpPlace = true;
            this.tripleMatchThreshold = 1;
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


    public Integer getTripleMatchThreshold() {
        return tripleMatchThreshold;
    }

    public void setTripleMatchThreshold(Integer tripleMatchThreshold) {
        this.tripleMatchThreshold = tripleMatchThreshold;
    }
}
