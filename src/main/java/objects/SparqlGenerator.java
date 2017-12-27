package objects;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import java.util.ArrayList;

public class SparqlGenerator {

    static String serviceEndpoint = "http://dbpedia.org/sparql/";
    /*
    dbo	http://dbpedia.org/ontology/
    dbp	http://dbpedia.org/property/
     */

    static public void main (String[] args) {
       String q = "select distinct ?object where {<http://dbpedia.org/resource/Flint,_Michigan> <http://dbpedia.org/property/north> ?object} LIMIT 100";
       ArrayList<String> objects = readTriplesFromEndPoint(q);
       System.out.println("objects.toString() = " + objects.toString());
    }

    public static ArrayList<String> readTriplesFromEndPoint(String sparqlQuery){
        ArrayList<String> triples = new ArrayList<String>();
        try {
            QueryExecution x = QueryExecutionFactory.sparqlService(serviceEndpoint, sparqlQuery);
            ResultSet resultset = x.execSelect();
            while (resultset.hasNext()) {
                QuerySolution solution = resultset.nextSolution();
               // System.out.println("solution.toString() = " + solution.toString());
                RDFNode obj = solution.get("object");
                triples.add(obj.toString());
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return triples;
    }


    static public String getPrefixes() {
        String sparqlQuery = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/> \n" +
                "PREFIX owltime: <http://www.w3.org/TR/owl-time#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX dbp: <http://dbpedia.org/property/> \n" +
                "PREFIX dbo: <http://dbpedia.org/ontology/> \n" +
                "PREFIX dbr: <http://dbpedia.org/resource/> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
        return sparqlQuery;
    }

}
