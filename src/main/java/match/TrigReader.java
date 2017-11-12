package match;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import org.apache.jena.riot.RDFDataMgr;
import vu.cltl.storyteller.objects.TrigTripleData;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by piek on 12/11/2017.
 */
public class TrigReader {

    static public vu.cltl.storyteller.objects.TrigTripleData readTripleFromTrigFiles (ArrayList<File> trigFiles) {
        TrigTripleData trigTripleData = new TrigTripleData();
        Dataset dataset = TDBFactory.createDataset();

        for (int i = 0; i < trigFiles.size(); i++) {
            // if (i==200) break;
            File file = trigFiles.get(i);
            String eventId = "";
            //System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
            try {
                dataset = RDFDataMgr.loadDataset(file.getAbsolutePath());
                Iterator<String> it = dataset.listNames();
                while (it.hasNext()) {
                    String name = it.next();
                    // System.out.println("name = " + name);
                    if (name.equals(TrigTripleData.provenanceGraph)) {
                       /// skip
                    } else if (name.equals(TrigTripleData.instanceGraph)) {
                        Model namedModel = dataset.getNamedModel(name);
                        StmtIterator siter = namedModel.listStatements();
                        while (siter.hasNext()) {
                            Statement s = siter.nextStatement();
                            String subject = s.getSubject().getURI();
                            if (subject.indexOf("#ev")>-1) {
                                //// we lump all events from the same documents together
                                if (eventId.isEmpty()) {
                                    eventId = subject;
                                } else {
                                    subject = eventId;
                                }
                            }
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
                    }
                    else if (name.equals(TrigTripleData.graspGraph)) {
                       ///skip
                    } else {
                        Model namedModel = dataset.getNamedModel(name);
                        StmtIterator siter = namedModel.listStatements();
                        while (siter.hasNext()) {
                            Statement s = siter.nextStatement();
                            String subject = s.getSubject().getURI();
                            if (subject.indexOf("#ev")>-1) {
                                //// we lump all events from the same documents together
                                if (eventId.isEmpty()) {
                                    eventId = subject;
                                } else {
                                    subject = eventId;
                                }
                            }
                            if (trigTripleData.tripleMapOthers.containsKey(subject)) {
                                ArrayList<Statement> triples = trigTripleData.tripleMapOthers.get(subject);
                                triples.add(s);
                                trigTripleData.tripleMapOthers.put(subject, triples);
                            } else {

                                ArrayList<Statement> triples = new ArrayList<Statement>();
                                triples.add(s);
                                trigTripleData.tripleMapOthers.put(subject, triples);
                            }
                        }
                    }
                }
                dataset.close();
                dataset = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        System.out.println("trigTripleData instances = " + trigTripleData.tripleMapInstances.size());
        System.out.println("trigTripleData others = " + trigTripleData.tripleMapOthers.size());
        System.out.println("trigTripleData grasp = " + trigTripleData.tripleMapGrasp.size());
        return trigTripleData;
    }

}
