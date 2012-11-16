package org.isatools.isa2owl.converter;

import org.isatools.isa2owl.mapping.ISASyntax2OWLMapping;
import org.isatools.isacreator.ontologymanager.BioPortalClient;
import org.isatools.isacreator.ontologymanager.OntologyManager;
import org.isatools.isacreator.ontologymanager.OntologySourceRefObject;
import org.isatools.isacreator.ontologymanager.common.OntologyTerm;
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
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class ISA2OWL {

    public static  OWLOntology ontology = null;
    public static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    public static OWLDataFactory factory = manager.getOWLDataFactory();
    public static IRI ontoIRI = null;

    //<type, id, individual>
    public static Map<String, Map<String,OWLNamedIndividual>> typeIdIndividualMap = null;
    //<type, individual>
    public static Map<String, Set<OWLNamedIndividual>> typeIndividualMap = null;
    public static ISASyntax2OWLMapping mapping = null;

    //this list will be populated only once with a query to bioportal
    private static List<org.isatools.isacreator.configuration.Ontology> allOntologies = null;

    public static void setIRI(String iri){
        ontoIRI = IRI.create(iri);
    }

    public static OWLClass addOWLClassAssertion(IRI owlClassIRI, OWLNamedIndividual individual) {

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
     * @param typeMappingLabel a label indicating the type of the individual (as defined in the mapping file)
     * @param individualLabel a label identifying the individual to be created
     * @param comment a comment to annotate the individual
     * @return
     */
    public static OWLNamedIndividual createIndividual(String typeMappingLabel, String individualLabel, String comment){

        OWLNamedIndividual individual = createIndividual(typeMappingLabel,individualLabel);
        if (comment!=null && comment.equals("")) {
            OWLAnnotation annotation = ISA2OWL.factory.getOWLAnnotation(ISA2OWL.factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()), ISA2OWL.factory.getOWLLiteral(comment));
            OWLAnnotationAssertionAxiom annotationAssertionAxiom = ISA2OWL.factory.getOWLAnnotationAssertionAxiom(individual.getIRI(), annotation);
            ISA2OWL.manager.addAxiom(ISA2OWL.ontology, annotationAssertionAxiom);
        }
        return individual;

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

        //System.out.println("CREATE INDIVIDUAL-> "+individualLabel + " rdf:type " + owlClass + "("+ typeMappingLabel +")");

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

    public static void convertProperties(Map<String, Map<IRI, String>> propertyMappings, Map<String, OWLNamedIndividual> typeIndividualM){

        for(String subjectString: propertyMappings.keySet()){
            System.out.println("subjectString="+subjectString);

            Map<IRI, String> predicateObjects = propertyMappings.get(subjectString);
            OWLNamedIndividual subject = typeIndividualM.get(subjectString);


            for(IRI predicate: predicateObjects.keySet()){
                OWLObjectProperty property = ISA2OWL.factory.getOWLObjectProperty(predicate);

                String objectString = predicateObjects.get(predicate);

//                System.out.println("objectString="+objectString);
                OWLNamedIndividual object = typeIndividualM.get(objectString);

//                System.out.println("property="+property);
//                System.out.println("subject="+subject);
//                System.out.println("object="+object);

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
//
//        System.out.println("Find ontology term...");
//
//        System.out.println("termAccession="+termAccession);
//        System.out.println("termSourceRef="+termSourceRef);

        List<OntologySourceRefObject> ontologiesUsed = OntologyManager.getOntologiesUsed();
//        System.out.println("ONTOLOGIES USED = "+ontologiesUsed);

        OntologySourceRefObject ontologySourceRefObject = null;
        for(OntologySourceRefObject ontologyRef: ontologiesUsed){
            if (termSourceRef!=null && termSourceRef.equals(ontologyRef.getSourceName())){
                ontologySourceRefObject = ontologyRef;
                break;
            }
        }

        //searching term in bioportal
        if (ontologySourceRefObject!=null){

            System.out.println("Found ontology "+ontologySourceRefObject);
            BioPortalClient client = new BioPortalClient();

            if (allOntologies==null){
                getAllOntologies(client);
            }

            String ontologyVersion = getOntologyVersion(ontologySourceRefObject.getSourceName());
            termAccession = completeTermAccession(termAccession, ontologySourceRefObject.getSourceName());

            OntologyTerm term = null;

            if (termAccession!=null)
                term = client.getTermInformation(termAccession, ontologyVersion);

            System.out.println("term====>"+term);
            if (term!=null) {
                String purl = term.getOntologyPurl();

                ISA2OWL.addOWLClassAssertion(IRI.create(purl), individual);

            }//term not null

        } //ontologySourceRefObject not null

    }

    private static void getAllOntologies(BioPortalClient client) {
        allOntologies = client.getAllOntologies();
//        System.out.println("ALL ONTOLOGIES="+allOntologies);
    }

    private static String completeTermAccession(String termAccession, String ontologyAbbreviation){

        if (ontologyAbbreviation.equals("OBI"))
            return "obo:OBI_"+termAccession;
        return null;

    }

    private static String getOntologyVersion(String ontologyAbbreviation){
        for(org.isatools.isacreator.configuration.Ontology ontology: allOntologies ){

            if (ontology.getOntologyAbbreviation().equals(ontologyAbbreviation)){
                return ontology.getOntologyVersion();
            }

        }
        return null;
    }
}