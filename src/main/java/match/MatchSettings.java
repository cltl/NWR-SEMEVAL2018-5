package match;

/**
 * Created by piek on 15/11/2017.
 */
public class MatchSettings {

    private boolean matchDbpActor;
    private boolean matchSemPlace;
    private Integer tripleMatchThreshold;

    public MatchSettings() {
        init();
    }

    void init () {
        this.matchDbpActor = false;
        this.matchSemPlace = false;
        this.tripleMatchThreshold = 5;
    }

    public boolean matchDbpActor() {
        return matchDbpActor;
    }

    public void setMatchDbpActor(boolean match) {
        this.matchDbpActor = match;
    }
    public boolean matchSemPlace() {
            return matchSemPlace;
    }

    public void setMatchSemPlace(boolean match) {
        this.matchSemPlace = match;
    }

    public Integer getTripleMatchThreshold() {
        return tripleMatchThreshold;
    }

    public void setTripleMatchThreshold(Integer tripleMatchThreshold) {
        this.tripleMatchThreshold = tripleMatchThreshold;
    }
}
