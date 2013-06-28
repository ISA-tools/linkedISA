package org.isatools.owl.reasoner;


import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import java.util.Set;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 24/01/2013
 * Time: 13:04
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class ReasonerService {

    private static Logger log = Logger.getLogger(ReasonerService.class);
    private OWLReasonerFactory reasonerFactory = null;
    private OWLReasoner reasoner = null;
    private OWLOntologyManager manager = null;
    private OWLOntology ontology = null;


    public ReasonerService(String logicalIRIString, String physicalIRIString){
        try{
            manager = OWLManager.createOWLOntologyManager();
            IRI ontoIRI = IRI.create(logicalIRIString);
            IRI physicalIRI = IRI.create(physicalIRIString);
            manager.addIRIMapper(new SimpleIRIMapper(ontoIRI, physicalIRI));
            System.out.println("ontoIRI="+ontoIRI);
            System.out.println("physicalIRI="+physicalIRI);
            ontology = manager.loadOntology(ontoIRI);
            initReasoner(ontology);
        }catch(OWLOntologyCreationException ex){
            System.out.println("Exception thrown! "+ ex);
            ex.printStackTrace();
        }
    }

    public ReasonerService(String ontologyIRIString){
        try{
            manager = OWLManager.createOWLOntologyManager();
            IRI ontologyIRI = IRI.create(ontologyIRIString);
            ontology = manager.loadOntologyFromOntologyDocument(ontologyIRI);
            initReasoner(ontology);
        }catch(OWLOntologyCreationException ex){
            ex.printStackTrace();
        }
    }

    public ReasonerService(OWLOntology ontology){
        initReasoner(ontology);
    }

    private void initReasoner(OWLOntology ontology){
        //reasoner = PelletReasonerFactory.getInstance().createReasoner( ontology );
        ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
        // Specify the progress monitor via a configuration. We could also
        // specify other setup parameters in the configuration, and different
        // reasoners may accept their own defined parameters this way.
        OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);

        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        // Create a reasoner that will reason over our ontology and its imports
        // closure. Pass in the configuration.
        reasoner = reasonerFactory.createReasoner(ontology, config);
        // Ask the reasoner to do all the necessary work now
        reasoner.precomputeInferences();
    }

    /**
     * Determine if the ontology is consistent
     *
     * @return
     */
    public boolean isConsistent(){
        return reasoner.isConsistent();
    }

    public Set<OWLClass> getUnsatisfiableClasses(){

        Node<OWLClass> bottomNode = reasoner.getUnsatisfiableClasses();
        // This node contains owl:Nothing and all the classes that are
        // equivalent to owl:Nothing - i.e. the unsatisfiable classes. We just
        // want to print out the unsatisfiable classes excluding owl:Nothing,
        // and we can used a convenience method on the node to get these
        Set<OWLClass> unsatisfiable = bottomNode.getEntitiesMinusBottom();
        if (!unsatisfiable.isEmpty()) {
            System.out.println("The following classes are unsatisfiable: ");
            for (OWLClass cls : unsatisfiable) {
                System.out.println("    " + cls);
            }
        } else {
            System.out.println("There are no unsatisfiable classes");
        }
        System.out.println("\n");

        return unsatisfiable;
    }

    public boolean isSuperClass(OWLClass subclass, OWLClassExpression superclass, boolean direct){
        NodeSet<OWLClass> superclasses = reasoner.getSuperClasses(subclass, direct);
        return superclasses.containsEntity(subclass);
    }


   public void getDescendants(IRI classIRI){
       OWLDataFactory factory = manager.getOWLDataFactory();
       OWLClass owlClass = factory.getOWLClass(classIRI);
       NodeSet<OWLClass> subClses = reasoner.getSubClasses(owlClass, true);
       Set<OWLClass> classes = subClses.getFlattened();
       System.out.println("Subclasses of "+classIRI+" : ");
       for (OWLClass cls : classes) {
           System.out.print("    " + cls);
           System.out.println("    " + cls.getAnnotations(ontology, factory.getOWLAnnotationProperty(IRI.create("http://www.w3.org/2000/01/rdf-schema#label")))+"\n");
       }
       System.out.println("\n");

   }

    public static void main(String[] args) {

        //String ontoIRIString = "http://owl.cs.manchester.ac.uk/repository/download?ontology=file:/Users/seanb/Desktop/Cercedilla2005/hands-on/people.owl&format=RDF/XML";
        String ontoIRIString = "http://purl.obolibrary.org/obo/extended-obi.owl";
        String physicalIRIString = "file:/Users/agbeltran/workspace-private/doe-stato/extended-obi.owl";

        ReasonerService reasonerService = new ReasonerService(ontoIRIString, physicalIRIString);
        reasonerService.isConsistent();
        reasonerService.getUnsatisfiableClasses();

        //reasonerService.getDescendants(IRI.create("http://isa-tools.org/owl/ISA_10000258"));
        reasonerService.getDescendants(IRI.create("http://isa-tools.org/isa/ISA_0000105"));

    }

}
