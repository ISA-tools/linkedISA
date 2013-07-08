package org.isatools.owl;

import java.io.File;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 05/02/2013
 * Time: 12:26
 *
 * Utility methods for OWL ontologies.
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class OWLUtil {

    /**
     * Save the ontology given as parameter.
     * @param onto
     */
    public static void saveRDFXML(OWLOntology onto){
        OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
        try {
            owlOntologyManager.saveOntology(onto, new RDFXMLOntologyFormat());
        } catch (OWLOntologyStorageException oosex) {
            oosex.printStackTrace();
        }
    }

    /**
     * Save the ontology given as parameter in the IRI given as parameter.
     * @param onto
     * @param iri
     */
    public static void saveRDFXML(OWLOntology onto, IRI iri){
        OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
        try {
            owlOntologyManager.saveOntology(onto, new RDFXMLOntologyFormat(), iri);
        } catch (OWLOntologyStorageException oosex) {
            oosex.printStackTrace();
        }
    }

    /**
     *
     * @param onto
     * @param filename
     * @return
     */
    public static File saveRDFXMLAsFile(OWLOntology onto, String filename) {
        File file;
        OWLOntologyManager owlManager = OWLManager.createOWLOntologyManager();
        try{
            file = new File(filename);
            owlManager.saveOntology(onto, new RDFXMLOntologyFormat(), IRI.create(file.toURI()));
            return file;
        }catch(OWLOntologyStorageException oosex){
            System.err.println("Error when saving ontology "+onto+", reason "+oosex);
            return null;
        }
    }

    /**
     *
     * @param onto
     * @param filename
     * @return
     */
    public static File saveMOWLSyntaxAsFile(OWLOntology onto, String filename) {
        File file;
        OWLOntologyManager owlManager = OWLManager.createOWLOntologyManager();
        try{
            file = new File(filename);
            owlManager.saveOntology(onto, new ManchesterOWLSyntaxOntologyFormat(), IRI.create(file.toURI()));
            return file;
        }catch(OWLOntologyStorageException oosex){
            System.err.println("Error when saving ontology "+onto+", reason "+oosex);
            return null;
        }
    }

    public static void systemOutputMOWLSyntax(OWLOntology onto) {
        OWLOntologyManager owlManager = OWLManager.createOWLOntologyManager();
        try{

            OWLOntologyDocumentTarget documentTarget = new SystemOutDocumentTarget();
            ManchesterOWLSyntaxOntologyFormat manSyntaxFormat = new ManchesterOWLSyntaxOntologyFormat();
            OWLXMLOntologyFormat format = new OWLXMLOntologyFormat();
            if(format.isPrefixOWLOntologyFormat()) {
                manSyntaxFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat());
            }
            owlManager.saveOntology(onto, manSyntaxFormat, documentTarget);

        }catch(OWLOntologyStorageException e){
            e.printStackTrace();
        }
    }
}
