package org.isatools.sparql;

import java.util.Iterator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 07/02/2013
 * Time: 14:15
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class BioportalSPARQLClient {

    public static final String SPARQL_URL = "http://sparql.bioontology.org/sparql";
    private static final String API_KEY = "fd88ee35-6995-475d-b15a-85f1b9dd7a42";

    private static final String PREFIX_META = "PREFIX meta: <http://bioportal.bioontology.org/metadata/def/> ";
    private static final String PREFIX_OMV = "PREFIX omv: <http://omv.ontoware.org/2005/05/ontology#> ";

    public BioportalSPARQLClient(){
    }

    public ResultSet executeQuery(String queryString) throws Exception {
        Query query = QueryFactory.create(queryString) ;

        System.out.println("query="+query);

        QueryEngineHTTP qexec = QueryExecutionFactory.createServiceRequest(SPARQL_URL, query);
        qexec.addParam("apikey", API_KEY );

        System.out.println("qexec=" + qexec.toString());

        ResultSet results = qexec.execSelect() ;
        return results;
    }

    public void printResults(ResultSet results){

        for ( ; results.hasNext() ; ) {
            QuerySolution soln = results.nextSolution() ;
            Iterator<String> iterator = soln.varNames();
            while (iterator.hasNext()){
                String varName = iterator.next();
                System.out.print(varName + ":\t");
                RDFNode node = soln.get(varName);
                System.out.println(node);
            }
            System.out.println();
        }
    }

    public ResultSet getVersionIDs() throws Exception {
        String query = PREFIX_META +
                    "SELECT DISTINCT ?version ?org.isatools.graph " +
                    "WHERE { " +
                    " ?version meta:hasDataGraph ?org.isatools.graph " +
                    "} ";

        return executeQuery(query);
    }

    public ResultSet getOntoAcronyms() throws Exception {
        String query = PREFIX_OMV +
                "SELECT ?ont ?name ?acr " +
                "WHERE { ?ont a omv:Ontology; " +
                "omv:acronym ?acr; " +
                "omv:name ?name . " +
                "}";
        return executeQuery(query);
    }

}
