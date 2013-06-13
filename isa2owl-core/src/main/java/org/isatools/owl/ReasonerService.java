package org.isatools.owl;


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


    public ReasonerService(String logicalIRIString, String physicalIRIString){
        try{
            manager = OWLManager.createOWLOntologyManager();
            IRI ontoIRI = IRI.create(logicalIRIString);
            IRI physicalIRI = IRI.create(physicalIRIString);
            manager.addIRIMapper(new SimpleIRIMapper(ontoIRI, physicalIRI));
            System.out.println("ontoIRI="+ontoIRI);
            System.out.println("physicalIRI="+physicalIRI);
            manager.loadOntology(ontoIRI);
        }catch(OWLOntologyCreationException ex){
            System.out.println("Exception thrown! "+ ex);
            ex.printStackTrace();
        }
    }

    public ReasonerService(String ontologyIRIString){
        try{
            manager = OWLManager.createOWLOntologyManager();
            IRI ontologyIRI = IRI.create(ontologyIRIString);
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontologyIRI);
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
       // Now we want to query the reasoner for all descendants of vegetarian.
       // Vegetarians are defined in the ontology to be animals that don't eat
       // animals or parts of animals.
       OWLDataFactory fac = manager.getOWLDataFactory();
       // Get a reference to the vegetarian class so that we can as the
       // reasoner about it. The full IRI of this class happens to be:
       // <http://owl.man.ac.uk/2005/07/sssw/people#vegetarian>
       OWLClass owlClass = fac.getOWLClass(classIRI);
       // Now use the reasoner to obtain the subclasses of vegetarian. We can
       // ask for the direct subclasses of vegetarian or all of the (proper)
       // subclasses of vegetarian. In this case we just want the direct ones
       // (which we specify by the "true" flag).
       NodeSet<OWLClass> subClses = reasoner.getSubClasses(owlClass, true);
       // The reasoner returns a NodeSet, which represents a set of Nodes. Each
       // node in the set represents a subclass of vegetarian pizza. A node of
       // classes contains classes, where each class in the node is equivalent.
       // For example, if we asked for the subclasses of some class A and got
       // back a NodeSet containing two nodes {B, C} and {D}, then A would have
       // two proper subclasses. One of these subclasses would be equivalent to
       // the class D, and the other would be the class that is equivalent to
       // class B and class C. In this case, we don't particularly care about
       // the equivalences, so we will flatten this set of sets and print the
       // result
       Set<OWLClass> classes = subClses.getFlattened();
       System.out.println("Subclasses of "+classIRI+" : ");
       for (OWLClass cls : classes) {
           System.out.println("    " + cls);
       }
       System.out.println("\n");

   }

    public static void main(String[] args) {

        //String ontoIRIString = "http://owl.cs.manchester.ac.uk/repository/download?ontology=file:/Users/seanb/Desktop/Cercedilla2005/hands-on/people.owl&format=RDF/XML";
        String ontoIRIString = "http://purl.obolibrary.org/obo/extended-obi.owl";
        String physicalIRIString = "/Users/agbeltran/workspace-private/doe-stato/extended-obi.owl";

        ReasonerService reasonerService = new ReasonerService(ontoIRIString, physicalIRIString);
        reasonerService.isConsistent();
        reasonerService.getUnsatisfiableClasses();

    }

}
