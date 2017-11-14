package match;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import objects.EventTypes;
import org.apache.jena.riot.RDFDataMgr;
import vu.cltl.triple.TrigTripleData;
import vu.cltl.triple.TrigUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by piek on 12/11/2017.
 */
public class TrigReader {

    static public TrigTripleData readTripleFromTrigFiles (ArrayList<File> trigFiles) {
        Dataset ds = ds = TDBFactory.createDataset();
        Model instanceModel =  ds.getNamedModel("instances");
        TrigTripleData trigTripleData = new TrigTripleData();
        Dataset dataset = TDBFactory.createDataset();

        for (int i = 0; i < trigFiles.size(); i++) {
            // if (i==200) break;
            File file = trigFiles.get(i);
            Resource eventId = null;
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
                            Resource subject = s.getSubject();
                            if (subject.getURI().indexOf("#ev")>-1) {
                                //// we lump all events from the same documents together
                                if (eventId==null) {
                                    eventId = subject; /// we initialize with the first event subject id
                                } else {
                                  //  System.out.println("subject.getURI() = " + subject.getLocalName());
                                  //  System.out.println("eventId.getLocalName() = " + eventId.getLocalName());
                                    subject = eventId;
                                }
                            }
                            Statement statement = instanceModel.createStatement(subject, s.getPredicate(), s.getObject());
                            if (TrigUtil.isGafTriple(statement) || EventTypes.eventTypeMatch(statement)) {
                                if (trigTripleData.tripleMapInstances.containsKey(subject.getURI())) {
                                    ArrayList<Statement> triples = trigTripleData.tripleMapInstances.get(subject.getURI());
                                    triples.add(statement);
                                    trigTripleData.tripleMapInstances.put(subject.getURI(), triples);
                                } else {
                                    ArrayList<Statement> triples = new ArrayList<Statement>();
                                    triples.add(statement);
                                    trigTripleData.tripleMapInstances.put(subject.getURI(), triples);
                                }
                            }
                        }
                    }
                    else if (name.equals(TrigTripleData.graspGraph)) {
                       ///skip
                    } else { ///// all other graphs are named graphs with SEM relations
                        Model namedModel = dataset.getNamedModel(name);
                        StmtIterator siter = namedModel.listStatements();
                        while (siter.hasNext()) {
                            Statement s = siter.nextStatement();
                            Resource subject = s.getSubject();
                            if (subject.getURI().indexOf("#ev")>-1) {
                                //// we lump all events from the same documents together
                                if (eventId==null) {
                                    eventId = subject;
                                } else {
                                    subject = eventId;
                                }
                            }
                            Statement statement = instanceModel.createStatement(subject, s.getPredicate(), s.getObject());
                            if (EventIdentity.entityParticipant(statement)) {
                                if (trigTripleData.tripleMapOthers.containsKey(subject.getURI())) {
                                    ArrayList<Statement> triples = trigTripleData.tripleMapOthers.get(subject.getURI());
                                    triples.add(statement);
                                    trigTripleData.tripleMapOthers.put(subject.getURI(), triples);
                                } else {
                                    ArrayList<Statement> triples = new ArrayList<Statement>();
                                    triples.add(statement);
                                    trigTripleData.tripleMapOthers.put(subject.getURI(), triples);
                                }
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
