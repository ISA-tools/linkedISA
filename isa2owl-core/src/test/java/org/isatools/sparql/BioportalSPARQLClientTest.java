package org.isatools.sparql;

import org.junit.After;
import org.junit.Before;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 07/02/2013
 * Time: 14:31
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class BioportalSPARQLClientTest {


    private BioportalSPARQLClient client = null;

    @Before
    public void setUp()  {
         client = new BioportalSPARQLClient();
    }

    @After
    public void tearDown() {
    }

    //@Test
    public void getVersionIDtest() throws Exception {
       ResultSet rs = client.getVersionIDs();
       client.printResults(rs);
    }

    //@Test
    public void getOntoAcronymsTest() throws Exception {
        ResultSet results = client.getOntoAcronyms();

        for ( ; results.hasNext() ; ) {
            QuerySolution soln = results.nextSolution() ;
            RDFNode ontUri = soln.get("ont") ;
            Literal name = soln.getLiteral("name") ;
            Literal acr = soln.getLiteral("acr") ;
            System.out.println(ontUri + " ---- " + name + " ---- " + acr);
        }
    }

}
