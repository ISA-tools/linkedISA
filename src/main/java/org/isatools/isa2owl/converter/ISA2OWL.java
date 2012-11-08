package org.isatools.isa2owl.converter;

import org.isatools.isa2owl.mapping.ISASyntax2OWLMapping;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 07/11/2012
 * Time: 17:11
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class ISA2OWL {

//    private static ISA2OWL instance = new ISA2OWL();
//
//    public static ISA2OWL getInstance() {
//        return instance;
//    }

    public static  OWLOntology ontology = null;
    public static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    public static OWLDataFactory factory = manager.getOWLDataFactory();
    public static IRI ontoIRI = null;

    //<type, id, individual>
    public static Map<String, Map<String,OWLNamedIndividual>> typeIdIndividualMap = null;
    //<type, individual>
    public static Map<String, Set<OWLNamedIndividual>> typeIndividualMap = null;
    public static ISASyntax2OWLMapping mapping = null;


    public static void setIRI(String iri){
        ontoIRI = IRI.create(iri);
    }

    public static OWLClass addOWLClassAssertion(IRI owlClassIRI, OWLNamedIndividual individual) {
        OWLClass owlClass = factory.getOWLClass(owlClassIRI);
        OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(owlClass, individual);
        manager.addAxiom(ontology,classAssertion);
        return owlClass;
    }

    /**
     *
     *
     * @param typeMappingLabel
     * @param individualLabel
     */
    public static OWLNamedIndividual createIndividual(String typeMappingLabel, String individualLabel){

        //avoid empty individuals
        if (individualLabel.equals(""))
            return null;

        Map<String, OWLNamedIndividual> map = typeIdIndividualMap.get(typeMappingLabel);

        /*
        if (!allowDuplicates){
            //check if individual was already created

            if (map!=null){
                OWLNamedIndividual ind = map.get(individualLabel);
                if (ind != null) {
                    Set<OWLNamedIndividual> list = typeIndividualMap.get(typeMappingLabel);
                    if (list ==null){
                        list = new HashSet<OWLNamedIndividual>();
                    }
                    list.add(ind);
                    typeIndividualMap.put(typeMappingLabel, list);
                    return ind;
                }
            }
        }
        */

        //if it wasn't created, create it now
        IRI owlClassIRI = mapping.getTypeMapping(typeMappingLabel);
        if (owlClassIRI==null){
            System.err.println("No IRI for type " + typeMappingLabel);
            return null;
        }

        OWLNamedIndividual individual = ISA2OWL.factory.getOWLNamedIndividual(IRIGenerator.getIRI(ISA2OWL.ontoIRI));
        OWLAnnotation annotation = ISA2OWL.factory.getOWLAnnotation(ISA2OWL.factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), ISA2OWL.factory.getOWLLiteral(individualLabel));
        OWLAnnotationAssertionAxiom annotationAssertionAxiom = ISA2OWL.factory.getOWLAnnotationAssertionAxiom(individual.getIRI(), annotation);
        ISA2OWL.manager.addAxiom(ISA2OWL.ontology, annotationAssertionAxiom);

        OWLClass owlClass = ISA2OWL.addOWLClassAssertion(owlClassIRI, individual);

        System.out.println("HERE-> "+individualLabel + " rdf:type " + owlClass );

        Set<OWLNamedIndividual> list = typeIndividualMap.get(typeMappingLabel);
        if (list ==null){
            list = new HashSet<OWLNamedIndividual>();
        }
        list.add(individual);
        typeIndividualMap.put(typeMappingLabel, list);

        if (map==null){
            map = new HashMap<String, OWLNamedIndividual>();
        }
        map.put(individualLabel, individual);
        typeIdIndividualMap.put(typeMappingLabel,map);

        return individual;
    }

    public static OWLNamedIndividual createIndividual(String typeMappingLabel, String individualLabel, Map<String, OWLNamedIndividual> map){
        OWLNamedIndividual individual = createIndividual(typeMappingLabel, individualLabel);
        map.put(typeMappingLabel, individual);
        return individual;
    }
}


