package question;

import eu.kyotoproject.kaf.KafWordForm;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by piek on 02/03/16.
 */
public class CoNLLdata {
    /*
    1_10ecb	0	1	Perennial	-
1_10ecb	0	2	party	-
1_10ecb	0	3	girl	-
1_10ecb	0	4	Tara	-
1_10ecb	0	5	Reid	-
1_10ecb	0	6	checked	(132016236402809085484)
1_10ecb	0	7	herself	-
1_10ecb	0	8	into	(132016236402809085484)

     */

    private String fileName;
    private String sentence;
    private String tokenId;
    private String dunit;
    private String word;
    private String tag;


    void init () {
        this.fileName = "";
        this.sentence = "";
        this.tag = "";
        this.dunit = "";
        this.tokenId = "";
        this.word = "";
    }

    public CoNLLdata() {
        init();
    }


    public CoNLLdata(String line) {
        // e54a480756b852ed2f0596e130652b64.b20.21	NEWLINE	BODY	-

        //object.toString() = {"sentence":".t1","dunit":"TITLE","filename":"daadc88e95b4066652550d977d0bf96f","tokenId":".10","tag":"-","word":"that"}

        init();
        String[] fields = line.split("\t");
        if (fields.length==4) {
            String str = fields[0];
            int idx_1 = str.indexOf(".");
            int idx_2 = str.lastIndexOf(".");
          //  System.out.println("str = " + str);
            this.fileName = str.substring(0, idx_1);
            if (idx_2>-1 && idx_2>idx_1) {
                this.sentence = str.substring(idx_1 + 2, idx_2);
                this.tokenId = str.substring(idx_2 + 1);
            }
            else {
//
            }
            this.word = fields[1];
            this.dunit = fields[2];
            this.tag = fields[3];
        }
        else {
            System.out.println("line = " + line);
        }
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

    public void setDunit(String dunit) {
        this.dunit = dunit;
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

    public KafWordForm toKafWordForm (int n) {
        KafWordForm kafWordForm = new KafWordForm();
        kafWordForm.setWid(Integer.toString(n));
        kafWordForm.setWf(this.word);
        kafWordForm.setSent(this.sentence);
        kafWordForm.setPara(this.dunit);
        return kafWordForm;
    }
}
