package org.isatools.owl.reasoner;

import java.util.Set;

import org.isatools.owl.OWLOntologyParametricSingleton;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

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
        sourceOntology = OWLOntologyParametricSingleton.getOntologyInstance(sourceOntologyIRI);
        if (sourceOntology == null){
                throw new IllegalArgumentException("The source ontology could not be loaded.");
        }

    }

    public ModuleExtractor(IRI sourceOntoIRI, IRI sourceOntoPhysIRI){
        sourceOntologyIRI = sourceOntoIRI;
        sourceOntologyPhysicalIRI = sourceOntoPhysIRI;
        ontologyManager = OWLManager.createOWLOntologyManager();
        owlDataFactory = ontologyManager.getOWLDataFactory();
        sourceOntology = OWLOntologyParametricSingleton.getOntologyInstance(sourceOntologyIRI, sourceOntologyPhysicalIRI);
        System.out.println("sourceOntology="+sourceOntology);
        if (sourceOntology == null){
            throw new IllegalArgumentException("The source ontology could not be loaded.");
        }
    }


    public OWLOntology extractModule(
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
