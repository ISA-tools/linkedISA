package org.isatools.isa2owl.converter;

import org.isatools.isa2owl.mapping.ISASyntax2OWLMapping;
import org.isatools.owl.ReasonerService;
import org.isatools.util.Pair;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.util.*;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 07/11/2012
 * Time: 17:11
 *
 * Class with static fields and methods used in the conversion from ISA-tab to OWL.
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class ISA2OWL {

    public static OWLOntology ontology = null;
    public static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    public static OWLDataFactory factory = manager.getOWLDataFactory();
    public static IRI ontoIRI = null;
    public static ReasonerService reasonerService = null;

    //TODO move this to the mapping file so that the conversion is independent of particular resources, but the dependency is kept in the mapping
    //TODO check if this is possible given how 'Characteristics' are converted

    public static final String STUDY_DESIGN_SUFFIX = " study design";
    public static final String STUDY_DESIGN_EXECUTION_SUFFIX = " study design execution";
    public static final String STUDY_TITLE_SUFFIX = " title";
    public static final String STUDY_DESCRIPTION_SUFFIX = " description";
    public static final String STUDY_PROTOCOL_SUFFIX = " protocol";
    public static final String STUDY_PROTOCOL_NAME_SUFFIX = " protocol name";
    public static final String STUDY_PUBLIC_RELEASE_DATE_SUFFIX = " public release date";


    //PATO
    public static final String PATO_SIZE_IRI = "http://purl.obolibrary.org/obo/PATO_0000117";

    //IAO IRIs
//    public static final IRI IAO_HAS_MEASUREMENT_VALUE_IRI = IRI.create("http://purl.obolibrary.org/obo/IAO_0000004");
//    public static final IRI IAO_DENOTES_IRI = IRI.create("http://purl.obolibrary.org/obo/IAO_0000219");

    //public static final IRI OBI_STUDY_DESIGN_EXECUTION = IRI.create("http://purl.obolibrary.org/obo/OBI_0000471");


    //<type, id, individual>
    public static Map<String, Map<String,OWLNamedIndividual>> typeIdIndividualMap = null;
    //<type, individual>
    public static Map<String, Set<OWLNamedIndividual>> typeIndividualMap = null;

    public static Map<String, OWLNamedIndividual> idIndividualMap = new HashMap<String, OWLNamedIndividual>();

    public static ISASyntax2OWLMapping mapping = null;

    public static void setIRI(String iri){
        ontoIRI = IRI.create(iri);
    }

    public static OWLClass getOWLClass(IRI owlClassIRI){
        return factory.getOWLClass(owlClassIRI);
    }


    public static OWLClass addOWLClassAssertion(IRI owlClassIRI, OWLNamedIndividual individual) {
        System.out.println("addOWLClass(owlClassIRI="+owlClassIRI+" individual="+individual+")");
        if (owlClassIRI==null || owlClassIRI.equals("") || individual==null || individual.equals(""))
            return null;

        OWLClass owlClass = factory.getOWLClass(owlClassIRI);
        OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(owlClass, individual);
        manager.addAxiom(ontology,classAssertion);
        return owlClass;

    }

    public static OWLNamedIndividual createIndividual(String typeMappingLabel, String individualLabel, Map<String, OWLNamedIndividual> map){
        return createIndividual(typeMappingLabel, individualLabel, null, map);
    }

    public static OWLNamedIndividual createIndividual(String typeMappingLabel, String individualLabel, String comment, Map<String, OWLNamedIndividual> map){

        OWLNamedIndividual individual = createIndividual(typeMappingLabel, individualLabel, comment);
        map.put(typeMappingLabel, individual);
        return individual;

    }

    /**
     *
     * It creates an OWLNamedIndividual given its type, its label and a comment.
     *
     * @param typeMappingLabel a label indicating the type of the individual (as defined in the mapping file)
     * @param individualLabel a label identifying the individual to be created
     * @param comment a comment to annotate the individual
     * @return
     */
    public static OWLNamedIndividual createIndividual(String typeMappingLabel, String individualLabel, String comment){

        OWLNamedIndividual individual = createIndividual(typeMappingLabel,individualLabel);
        if (comment!=null && !comment.equals("")) {
            OWLAnnotation annotation = ISA2OWL.factory.getOWLAnnotation(ISA2OWL.factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()), ISA2OWL.factory.getOWLLiteral(comment));
            OWLAnnotationAssertionAxiom annotationAssertionAxiom = ISA2OWL.factory.getOWLAnnotationAssertionAxiom(individual.getIRI(), annotation);
            ISA2OWL.manager.addAxiom(ISA2OWL.ontology, annotationAssertionAxiom);
        }
        return individual;

    }

    public static void addComment(String comment, IRI iri){
        OWLAnnotation annotation = ISA2OWL.factory.getOWLAnnotation(ISA2OWL.factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()), ISA2OWL.factory.getOWLLiteral(comment));
        OWLAnnotationAssertionAxiom annotationAssertionAxiom = ISA2OWL.factory.getOWLAnnotationAssertionAxiom(iri, annotation);
        ISA2OWL.manager.addAxiom(ISA2OWL.ontology, annotationAssertionAxiom);
    }

    public static OWLNamedIndividual createIndividual(String name, IRI type){
        OWLNamedIndividual individual = factory.getOWLNamedIndividual(IRIGenerator.getIRI(ISA2OWL.ontoIRI));

        OWLAnnotation annotation = ISA2OWL.factory.getOWLAnnotation(ISA2OWL.factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), ISA2OWL.factory.getOWLLiteral(name));
        OWLAnnotationAssertionAxiom annotationAssertionAxiom = ISA2OWL.factory.getOWLAnnotationAssertionAxiom(individual.getIRI(), annotation);
        ISA2OWL.manager.addAxiom(ISA2OWL.ontology, annotationAssertionAxiom);

        OWLClass owlClass = ISA2OWL.addOWLClassAssertion(type, individual);
        return individual;
    }

    /**
     *
     * It creates an OWLNamedIndividual given its type (given a string, the typeIdIndividualMap is used) and its label (which should not be null, or the individual retrieved will be null)
     *
     * @param typeMappingLabel
     * @param individualLabel
     */
    public static OWLNamedIndividual createIndividual(String typeMappingLabel, String individualLabel){

        //avoid empty individuals
        if (individualLabel.equals(""))
            return null;

        if (typeIdIndividualMap == null){
            typeIdIndividualMap = new HashMap<String, Map<String, OWLNamedIndividual>>();
        }
        Map<String, OWLNamedIndividual> map = typeIdIndividualMap.get(typeMappingLabel);

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

        Set<OWLNamedIndividual> list = typeIndividualMap.get(typeMappingLabel);
        if (list ==null){
            list = new HashSet<OWLNamedIndividual>();
        }
        list.add(individual);
        typeIndividualMap.put(typeMappingLabel, list);

        idIndividualMap.put(individualLabel, individual);

        if (map==null){
            map = new HashMap<String, OWLNamedIndividual>();
        }
        map.put(individualLabel, individual);
        typeIdIndividualMap.put(typeMappingLabel,map);

        return individual;
    }

    /***
     *
     * Given the property mappings and the relevant individuals, it builds the OWL constructs.
     *
     * @param propertyMappings
     * @param typeIndividualM
     */
    public static void convertProperties(Map<String, List<Pair<IRI, String>>> propertyMappings, Map<String, OWLNamedIndividual> typeIndividualM){

        for(String subjectString: propertyMappings.keySet()){
            System.out.println("subjectString="+subjectString);

            List<Pair<IRI, String>> predicateObjects = propertyMappings.get(subjectString);
            OWLNamedIndividual subject = typeIndividualM.get(subjectString);


            for(Pair<IRI,String> predicateObject: predicateObjects){

                IRI predicate = predicateObject.getFirst();

                OWLObjectProperty property = ISA2OWL.factory.getOWLObjectProperty(predicate);

                String objectString = predicateObject.getSecond();

                OWLNamedIndividual object = typeIndividualM.get(objectString);

                if (subject==null || object==null || property==null){

                    System.err.println("At least one is null...");

                }else{
                    OWLObjectPropertyAssertionAxiom axiom = ISA2OWL.factory.getOWLObjectPropertyAssertionAxiom(property, subject, object);
                    ISA2OWL.manager.addAxiom(ISA2OWL.ontology, axiom);
                }
            }//for
        }
    }



    public static void findOntologyTermAndAddClassAssertion(String termSourceRef, String termAccession, OWLNamedIndividual individual){
        System.out.println("============findOntologyTermAndAddClassAssertion termSourceRef="+termSourceRef + " termAccession="+termAccession + " individual="+individual);

        if (termSourceRef==null || termAccession==null)
            return;

        String purl = OntologyLookup.findOntologyPURL(termSourceRef, termAccession);

        System.out.println("purl="+purl);

        if (purl!=null && !purl.equals(""))
            ISA2OWL.addOWLClassAssertion(IRI.create(purl), individual);
    }


}