package objects;

public class ParticipantTypes {

    static final String[] humanWords={"man","woman","women", "men", "boy", "boys", "girl", "girls",
            "teen", "teens", "child", "children",
            "son", "daughter", "father", "mother", "victim", "victims", "body", "bodies", "injured", "dead", "people", "person", "persons"};

    static public boolean isHumanLabel (String label) {
        for (int i = 0; i < humanWords.length; i++) {
            String humanWord = humanWords[i];
            if (label.equalsIgnoreCase(humanWord)) {
                return true;
            }
            else if (label.toLowerCase().startsWith(humanWord+" ")) {
                return true;
            }
            else if (label.toLowerCase().endsWith(" "+humanWord)) {
                return true;
            }
        }
        return false;
    }
}
