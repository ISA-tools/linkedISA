package org.isatools.isa2owl.converter;

import org.apache.log4j.Logger;
import org.isatools.isa2owl.mapping.ISASyntax2OWLMapping;
import org.isatools.owl.reasoner.ReasonerService;
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


    private static final Logger log = Logger.getLogger(ISA2OWL.class);

    public static OWLOntology ontology = null;
    public static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    public static OWLDataFactory factory = manager.getOWLDataFactory();
    public static IRI ontoIRI = null;
    public static ReasonerService reasonerService = null;

    //TODO move this to the mapping file so that the conversion is independent of particular resources, but the dependency is kept in the mapping
    //TODO check if this is possible given how 'Characteristics' are converted

    public static final String STUDY_DESIGN_SUFFIX = " study design";
    public static final String STUDY_DESIGN_EXECUTION_SUFFIX = " study design execution";
    public static final String TITLE_SUFFIX = " title";
    public static final String DESCRIPTION_SUFFIX = " description";
    public static final String STUDY_PROTOCOL_SUFFIX = " protocol";
    public static final String STUDY_PROTOCOL_NAME_SUFFIX = " protocol name";
    public static final String STUDY_PUBLIC_RELEASE_DATE_SUFFIX = " public release date";

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
        log.debug("addOWLClass(owlClassIRI=" + owlClassIRI + " individual=" + individual + ")");
        if (owlClassIRI==null || owlClassIRI.equals("") || individual==null || individual.equals(""))
            return null;

        OWLClass owlClass = factory.getOWLClass(owlClassIRI);
        OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(owlClass, individual);
        manager.addAxiom(ontology,classAssertion);
        return owlClass;

    }

    public static void addComment(String comment, IRI iri){
        OWLAnnotation annotation = ISA2OWL.factory.getOWLAnnotation(ISA2OWL.factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()), ISA2OWL.factory.getOWLLiteral(comment));
        OWLAnnotationAssertionAxiom annotationAssertionAxiom = ISA2OWL.factory.getOWLAnnotationAssertionAxiom(iri, annotation);
        ISA2OWL.manager.addAxiom(ISA2OWL.ontology, annotationAssertionAxiom);
    }


    /**
     *
     * @param type an IRI with the type for the individual
     * @param name the name for the individual
     * @return
     */
    public static OWLNamedIndividual createIndividual(IRI type, String name){
        OWLNamedIndividual individual = factory.getOWLNamedIndividual(IRIGenerator.getIRI(ISA2OWL.ontoIRI));

        OWLAnnotation annotation =
                ISA2OWL.factory.getOWLAnnotation(ISA2OWL.factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()),
                        ISA2OWL.factory.getOWLLiteral(name));
        OWLAnnotationAssertionAxiom annotationAssertionAxiom = ISA2OWL.factory.getOWLAnnotationAssertionAxiom(individual.getIRI(), annotation);
        ISA2OWL.manager.addAxiom(ISA2OWL.ontology, annotationAssertionAxiom);

        OWLClass owlClass = ISA2OWL.addOWLClassAssertion(type, individual);
        return individual;
    }


    /**
     *
     * Creates an individual - it relies on the more complete method
     *
     * @param typeMappingLabel a String to be used to find the type of the individual from the mapping file
     * @param individualLabel a String to be used as the label for the individual
     * @return
     */
    public static OWLNamedIndividual createIndividual(String typeMappingLabel, String individualLabel){
        return createIndividual(typeMappingLabel,individualLabel, null, null);

    }

    /**
     *
     * Creates an individual - it relies on the more complete method
     *
     * @param typeMappingLabel
     * @param individualLabel
     * @param map
     * @return
     */
    public static OWLNamedIndividual createIndividual(String typeMappingLabel, String individualLabel, Map<String, OWLNamedIndividual> map){
        return createIndividual(typeMappingLabel, individualLabel, null, map);
    }

    /**
     * Creates an individual - it relies on the more complete method
     *
     * @param typeMappingLabel
     * @param individualLabel
     * @param comment
     * @param map
     * @return
     */
    public static OWLNamedIndividual createIndividual(String typeMappingLabel, String individualLabel, String comment, Map<String, OWLNamedIndividual> map){
        return  createIndividual(typeMappingLabel, individualLabel, comment, null, map);
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
        return createIndividual(typeMappingLabel,individualLabel, comment, null);

    }


    /**
     * It creates an OWLNamedIndividual given its type (given a string, the typeIdIndividualMap is used)
     * and its label (which should not be null, or the individual retrieved will be null)
     *
     *
     * @param typeMappingLabel this is a string indicating the type for the individual to be retrieved from the mapping
     * @param individualLabel this is the label to be used for the individual
     * @param comment this is a string that will be added as an annotation for the individual
     * @param individualIRI this is the IRI for the individual, if null, an IRI will be generated using IRIGenerator
     * @param parameterMap this is a map with <String, individual> given as parameter
     * @return
     */
    public static OWLNamedIndividual createIndividual(String typeMappingLabel,
                                                      String individualLabel,
                                                      String comment,
                                                      IRI individualIRI,
                                                      Map<String, OWLNamedIndividual> parameterMap){

        //avoid empty individuals
        if (individualLabel.equals(""))
            return null;

        if (typeIdIndividualMap == null){
            typeIdIndividualMap = new HashMap<String, Map<String, OWLNamedIndividual>>();
        }
        Map<String, OWLNamedIndividual> map = typeIdIndividualMap.get(typeMappingLabel);

        //if it wasn't created, create it now
        Set<IRI> owlClassIRIs = mapping.getTypeMapping(typeMappingLabel);

        OWLNamedIndividual individual = null;

        if (owlClassIRIs==null){
            log.debug("No IRIs for type " + typeMappingLabel);
            return null;
        }

        for(IRI owlClassIRI: owlClassIRIs){

            if (owlClassIRI==null){
                log.debug("No IRI for type " + typeMappingLabel);
                return null;
            }

            if (individual ==null)
                individual = ISA2OWL.factory.getOWLNamedIndividual( (individualIRI==null)? IRIGenerator.getIRI(ISA2OWL.ontoIRI) : individualIRI);

            //label
            OWLAnnotation annotation = ISA2OWL.factory.getOWLAnnotation(
                ISA2OWL.factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()),
                ISA2OWL.factory.getOWLLiteral(individualLabel));
            OWLAnnotationAssertionAxiom annotationAssertionAxiom = ISA2OWL.factory.getOWLAnnotationAssertionAxiom(individual.getIRI(), annotation);
            ISA2OWL.manager.addAxiom(ISA2OWL.ontology, annotationAssertionAxiom);

            //comment
            if (comment!=null && !comment.equals("")) {
                OWLAnnotation commentAnnotation = ISA2OWL.factory.getOWLAnnotation(ISA2OWL.factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()), ISA2OWL.factory.getOWLLiteral(comment));
                OWLAnnotationAssertionAxiom commentAnnotationAssertionAxiom = ISA2OWL.factory.getOWLAnnotationAssertionAxiom(individual.getIRI(), commentAnnotation);
                ISA2OWL.manager.addAxiom(ISA2OWL.ontology, commentAnnotationAssertionAxiom);
            }

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

            if (parameterMap!=null){
                parameterMap.put(typeMappingLabel, individual);
            }
        }//for

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
            List<Pair<IRI, String>> predicateObjects = propertyMappings.get(subjectString);
            OWLNamedIndividual subject = typeIndividualM.get(subjectString);


            for(Pair<IRI,String> predicateObject: predicateObjects){

                IRI predicate = predicateObject.getFirst();

                OWLObjectProperty property = ISA2OWL.factory.getOWLObjectProperty(predicate);

                String objectString = predicateObject.getSecond();

                OWLNamedIndividual object = typeIndividualM.get(objectString);

                if (subject==null || object==null || property==null){

                    log.debug("At least one of subject/predicate/object is null...");

                }else{
                    OWLObjectPropertyAssertionAxiom axiom = ISA2OWL.factory.getOWLObjectPropertyAssertionAxiom(property, subject, object);
                    ISA2OWL.manager.addAxiom(ISA2OWL.ontology, axiom);
                }
            }//for
        }
    }

    public static void convertPropertiesMultipleIndividuals(Map<String, List<Pair<IRI, String>>> propertyMappings, Map<String, Set<OWLNamedIndividual>> typeIndividualM){
        for(String subjectString: propertyMappings.keySet()){
            List<Pair<IRI, String>> predicateObjects = propertyMappings.get(subjectString);
            Set<OWLNamedIndividual> subjectSet = typeIndividualM.get(subjectString);


            for(Pair<IRI,String> predicateObject: predicateObjects){

                IRI predicate = predicateObject.getFirst();

                OWLObjectProperty property = ISA2OWL.factory.getOWLObjectProperty(predicate);

                String objectString = predicateObject.getSecond();

                Set<OWLNamedIndividual> objectSet = typeIndividualM.get(objectString);

                if (subjectSet==null || objectSet==null || property==null){

                    log.debug("At least one of subject/predicate/object is null...");

                }else{

                    for(OWLNamedIndividual subject: subjectSet){
                        for(OWLNamedIndividual object:objectSet){
                            System.out.println("subject = "+subject + " object="+object);

                            OWLObjectPropertyAssertionAxiom axiom = ISA2OWL.factory.getOWLObjectPropertyAssertionAxiom(property, subject, object);
                            ISA2OWL.manager.addAxiom(ISA2OWL.ontology, axiom);
                        }
                    }
                }
            }//for
        }
    }

    public static void addObjectPropertyAssertionAxiom(OWLObjectProperty property, OWLNamedIndividual subject, OWLNamedIndividual object){
        OWLObjectPropertyAssertionAxiom axiom = ISA2OWL.factory.getOWLObjectPropertyAssertionAxiom(property, subject, object);
        ISA2OWL.manager.addAxiom(ISA2OWL.ontology, axiom);
    }

    public static void findOntologyTermAndAddClassAssertion(String termSourceRef, String termAccession, OWLNamedIndividual individual){
        log.debug("============findOntologyTermAndAddClassAssertion termSourceRef="+termSourceRef + " termAccession="+termAccession + " individual="+individual);

        if (termSourceRef==null || termAccession==null || termSourceRef.equals("") || termAccession.equals(""))
            return;

        String purl = OntologyLookup.findOntologyPURL(termSourceRef, termAccession);

        log.debug("purl="+purl);

        if (purl!=null && !purl.equals(""))
            ISA2OWL.addOWLClassAssertion(IRI.create(purl), individual);
    }


}