package org.isatools.owl;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.util.Set;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 24/01/2013
 * Time: 15:08
 *
 *
 * Class to extract modules from an ontology, given a signature
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class ModuleExtractor {

    private OWLOntologyManager ontologyManager = null;
    private OWLDataFactory owlDataFactory = null;

    private IRI sourceOntologyIRI = null;
    private IRI sourceOntologyPhysicalIRI = null;

    private OWLOntology moduleOntology = null;
    private OWLOntology sourceOntology = null;


    /**
     * Constructor for ModuleExtractor.
     * Initialises sourceOntology given its URI.
     *
     * @param iri the IRI of the domain ontology
     */
    public ModuleExtractor(IRI iri) {

        this.sourceOntologyIRI = iri;
        ontologyManager = OWLManager.createOWLOntologyManager();
        owlDataFactory = ontologyManager.getOWLDataFactory();

        try{
            sourceOntology = ontologyManager.loadOntology(sourceOntologyIRI);
            if (sourceOntology == null){
                throw new IllegalArgumentException("The domain ontology could not be loaded.");
            }
        }catch(OWLOntologyCreationException oocex){

        }

    }

    /**
     *
     * @param moduleOntologyURI
     * @param signature
     * @return
     */
    private OWLOntology extractModule(
            IRI moduleOntologyURI,
            Set<OWLEntity> signature)  {

        SyntacticLocalityModuleExtractor moduleExtractor =
                new SyntacticLocalityModuleExtractor(ontologyManager, sourceOntology, ModuleType.STAR);
        try{

            moduleOntology = moduleExtractor.extractAsOntology(signature, moduleOntologyURI);

        }catch(OWLOntologyChangeException oocex){
            System.err.println("Error when extracting ontology " + moduleOntologyURI + ", reason "+ oocex);
        }catch(OWLOntologyCreationException oocex){
            System.err.println("Error when extracting ontology " + moduleOntologyURI + ", reason "+ oocex);
        }
        return moduleOntology;
    }


}
