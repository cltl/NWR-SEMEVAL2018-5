package conll;

import eu.kyotoproject.kaf.KafWordForm;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by piek on 02/03/16.
 */
public class CoNLLdata {

    private String fileName;
    private String sentence;
    private String tokenId;
    private String dunit;
    private String word;
    private String tag;
    private String conllLineWithoutTag;


    void init () {
        this.fileName = "";
        this.sentence = "";
        this.tag = "";
        this.dunit = "";
        this.tokenId = "";
        this.word = "";
        this.conllLineWithoutTag = "";
    }

    public CoNLLdata() {
        init();
    }


    public CoNLLdata(String line) {
        // e54a480756b852ed2f0596e130652b64.b20.21	NEWLINE	BODY	-
        init();
        String[] fields = line.split("\t");
        if (fields.length==4) {
            String str = fields[0];
            int idx_1 = str.indexOf(".");
            int idx_2 = str.lastIndexOf(".");
            this.fileName = str.substring(0, idx_1);
            if (idx_2>-1 && idx_2>idx_1) {
                this.sentence = str.substring(idx_1 + 2, idx_2);
                this.tokenId = str.substring(idx_2+1);
            }
            else {}
            this.word = fields[1];
            this.dunit = fields[2];
            this.tag = fields[3];
            for (int i = 0; i < fields.length-1; i++) {
                String field = fields[i];
                this.conllLineWithoutTag += field+"\t";
            }
        }
        else {}
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDunit() {
        return dunit;
    }

    public Integer getDunitInteger () {
        Integer integer = new Integer(0);
        if (dunit.equals("DCT")) {
            integer = 0;
        }
        else if (dunit.equals("TITLE")) {
            integer = 1;
        }
        else {
            integer = 2;
        }
        return  integer;
    }

    public void setDunit(String dunit) {
        this.dunit = dunit;
    }

    public String toConll (String tag) {
        return conllLineWithoutTag+tag+"\n";
    }

    public JSONObject toJsonObject () throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("filename", this.fileName);
        jsonObject.put("sentence", this.sentence);
        jsonObject.put("tokenId", this.tokenId);
        jsonObject.put("word", this.word);
        jsonObject.put("dunit", this.dunit);
        jsonObject.put("tag", this.tag);
        return jsonObject;
    }

    public KafWordForm toKafWordForm (int tokenC, int sentenceC) {
        KafWordForm kafWordForm = new KafWordForm();
        kafWordForm.setWid("w"+Integer.toString(tokenC));
        kafWordForm.setWf(this.word);
        kafWordForm.setSent(this.sentence);
        kafWordForm.setSent(new Integer(sentenceC).toString());
        /// paragraph must be a number
        kafWordForm.setPara(this.getDunitInteger().toString());
        return kafWordForm;
    }

    String replaceAlphaByNumeric (String str) {
        final String intString = "abcdefghijklmnopqrstuvwxyz";
        String convertedString = "";
        for (int i = 0; i < str.length(); i++) {
           char c = str.charAt(i);
           int p = intString.indexOf(c);
           if (p==-1) {
              // System.out.println("Error converting = " + str);
               convertedString+= c;
           }
           else {
               convertedString+=p+1;
           }
        }
        return convertedString;
    }
}
