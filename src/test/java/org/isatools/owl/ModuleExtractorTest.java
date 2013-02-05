package org.isatools.owl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 05/02/2013
 * Time: 09:36
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class ModuleExtractorTest {

    private IRI sourceOntologyIRI = null;
    private IRI sourceOntologyPhysicalIRI = null;
    private ModuleExtractor moduleExtractor = null;

    @Before
    public void setUp() {
       sourceOntologyIRI = IRI.create("http://purl.obolibrary.org/obo/extended-obi.owl");
       sourceOntologyPhysicalIRI = IRI.create("file:/Users/agbeltran/workspace-private/isa2owl/src/main/resources/owl/extended-obi.owl");
       moduleExtractor = new ModuleExtractor(sourceOntologyIRI, sourceOntologyPhysicalIRI);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testModuleExtractor() {
       Set<String> signatureSet = new HashSet<String>();
       signatureSet.add("http://isa-tools.org/isa/ISA0000002");

       OWLOntology module = moduleExtractor.extractModule(IRI.create("http://test.owl"),signatureSet);
    }

}
