package org.isatools.owl;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;


/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 05/02/2013
 * Time: 10:07
 *
 * Parametric singleton for an OWLOntology.
 * It is used to ensure that the ontology is loaded only once.
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class OWLOntologyParametricSingleton {

    private static final Logger log = Logger.getLogger(OWLOntologyParametricSingleton.class);

    private OWLOntologyManager owlManager = null;
    private OWLOntology ontology = null;

    private IRI ontologyIRI = null, ontologyPhysicalIRI = null;

    private static Set<OWLOntologyParametricSingleton>
            instanceSet = new HashSet<OWLOntologyParametricSingleton>();

    /**
     * Constructor with no parameters
     */
    public OWLOntologyParametricSingleton(){
        owlManager = OWLManager.createOWLOntologyManager();
    }

    private IRI getOntologyIRI(){
        return ontologyIRI;
    }

    private IRI getOntologyPhysicalIRI(){
        return ontologyPhysicalIRI;
    }

    private OWLOntology getOntology(){
        return ontology;
    }

    private void setOntologyIRI(IRI iri){
        ontologyIRI = iri;
    }

    private void setOntologyPhysicalIRI(IRI pIri){
        ontologyPhysicalIRI = pIri;
    }

    /**
     * Load the ontology and record the time taken.
     */
    private void setOntology(){
        try {
            if (ontologyPhysicalIRI==null){
                ontology = owlManager.loadOntology(ontologyIRI);
            }else{
                //stopwatch.start();
                // Due to OWLAPI 3 deprecation, changed from
                // ontology = owlManager.loadOntologyFromPhysicalIRI(ontologyPhysicalIRI)
                ontology = owlManager.loadOntology(ontologyPhysicalIRI);
            }
        } catch (OWLOntologyCreationException oocex) {
            oocex.printStackTrace();
        }
    }

    /**
     * See if the ontology has been loaded. If it has, return it.
     * @param iri
     * @return
     */
    private static OWLOntologyParametricSingleton instanceExists(IRI iri){
        for (Iterator<OWLOntologyParametricSingleton> it = instanceSet.iterator(); it.hasNext(); ){
            OWLOntologyParametricSingleton inst = it.next();
            if (inst.getOntologyIRI().equals(iri)){
                return inst;
            }
        }
        return null;
    }

    /**
     * The singleton getInstance() by IRI
     * @param iri
     * @return
     */
    private static OWLOntologyParametricSingleton getInstance(IRI iri){
        log.debug("getInstance("+iri+")");
        OWLOntologyParametricSingleton instance = instanceExists(iri);
        log.debug("instance="+instance);
        if (instance==null){
            log.info("instance was null, so creating the instance and adding it to the set");
            instance = new OWLOntologyParametricSingleton();
            instance.setOntologyIRI(iri);
            instance.setOntology();
            instanceSet.add(instance);
        }
        return instance;
    }

    /**
     * Specifies both logical and physical IRIs
     * @param iri
     * @param pIri
     * @return
     */
    private static OWLOntologyParametricSingleton getInstance(IRI iri, IRI pIri){
        log.debug("getInstance("+iri+","+pIri+")");
        //TODO add another method instanceExists considering the physical IRI?
        OWLOntologyParametricSingleton instance = instanceExists(iri);
        System.out.println("instance="+instance);
        if (instance==null){
            log.info("instance was null, so creating the instance and adding it to the set");
            instance = new OWLOntologyParametricSingleton();
            instance.setOntologyIRI(iri);
            instance.setOntologyPhysicalIRI(pIri);
            instance.setOntology();
            instanceSet.add(instance);
        }
        return instance;
    }

    /**
     * Retrieves the ontology (after loading from its logical IRI).
     * @param iri the ontology logical IRI
     * @return the OWLOntology
     */
    public static OWLOntology getOntologyInstance(IRI iri){
        return getInstance(iri).getOntology();
    }

    /**
     * Retrieves the ontology (after loading from its physical URI)/
     *
     * @param iri the ontology logical URI
     * @param pIri the ontology physical URI
     * @return the OWLOntology
     */
    public static OWLOntology getOntologyInstance(IRI iri, IRI pIri){
        return getInstance(iri, pIri).getOntology();
    }
}
