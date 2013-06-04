package org.isatools.owl;

import org.junit.After;
import org.junit.Before;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

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
       sourceOntologyIRI = IRI.create("http://purl.obolibrary.org/obo/obi.owl");
       sourceOntologyPhysicalIRI = IRI.create("file:/Users/agbeltran/workspace-private/isa2owl/isa2owl-core/src/main/resources/owl/obi/trunk/src/ontology/branches/obi.owl");
       moduleExtractor = new ModuleExtractor(sourceOntologyIRI, sourceOntologyPhysicalIRI);
    }

    @After
    public void tearDown() {

    }

    //@Test
    public void testModuleExtractor() {

       OWLOntology extended_obi_ontology = OWLOntologyParametricSingleton.getOntologyInstance(IRI.create("http://purl.obolibrary.org/obo/extended-obi.owl"),
               IRI.create("file:/Users/agbeltran/workspace-private/isa2owl/src/main/resources/owl/extended-obi.owl"));

       Set<OWLEntity> signature = extended_obi_ontology.getSignature();

       OWLOntology module = moduleExtractor.extractModule(IRI.create("http://http://purl.obolibrary.org/obo/isa-obi-module.owl"),signature);

       OWLUtil.systemOutputMOWLSyntax(module);
       OWLUtil.saveRDFXMLAsFile(module, "/Users/agbeltran/workspace-private/isa2owl/src/main/resources/owl/isa-obi-module.owl");
    }

}
