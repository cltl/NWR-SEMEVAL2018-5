package match;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import org.apache.jena.riot.RDFDataMgr;
import vu.cltl.triple.TrigTripleData;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by piek on 12/11/2017.
 */
public class TrigReader {
    static public TrigTripleData simpleRdfReader (ArrayList<File> trigFiles) {
            TrigTripleData trigTripleData = new TrigTripleData();
            Dataset dataset = TDBFactory.createDataset();

            for (int i = 0; i < trigFiles.size(); i++) {
               // if (i==200) break;
                File file = trigFiles.get(i);
               // System.out.println("file.getName() = " + file.getName());
                try {
                    dataset = RDFDataMgr.loadDataset(file.getAbsolutePath());
                    Model namedModel = dataset.getDefaultModel();
                    StmtIterator siter = namedModel.listStatements();
                    while (siter.hasNext()) {
                        Statement s = siter.nextStatement();
                        String subject = s.getSubject().getURI();
                        if (trigTripleData.tripleMapInstances.containsKey(subject)) {
                            ArrayList<Statement> triples = trigTripleData.tripleMapInstances.get(subject);
                            triples.add(s);
                            trigTripleData.tripleMapInstances.put(subject, triples);
                        } else {

                            ArrayList<Statement> triples = new ArrayList<Statement>();
                            triples.add(s);
                            trigTripleData.tripleMapInstances.put(subject, triples);
                        }
                    }
                    dataset.close();
                    dataset = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            return trigTripleData;
        }

}
