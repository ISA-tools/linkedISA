package org.isatools.owl.reasoner;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;

import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;



/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 08/02/2013
 * Time: 10:59
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class Explanations {

    //private ReasonerFactory reasonerFactory = null;

    public Explanations(){

    }


    public void explain(){

        /*
        reasonerFactory = new PelletReasonerFactory();
        ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
        OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
        reasoner = reasonerFactory.createReasoner(ontology, config);

        //define an OWLClass with the Query
        //TODO simplify - this was already defined in the validator
        OWLClass query = factory.getOWLClass(IRI.create(ontologyIRI+"#Query"));
        OWLEquivalentClassesAxiom ax =
                factory.getOWLEquivalentClassesAxiom(query, description);
        manager.addAxiom(ontology, ax);

        log.debug("equivalent axiom="+ax);

        //classify the ontology
        //Set<OWLOntology> importsClosure = manager.getImportsClosure(ontology);
        //reasoner.loadOntologies(importsClosure);
        //reasoner.classify();
        //reasoner.prepareReasoner();
        log.debug("after classifying");

        DefaultExplanationGenerator egen = new DefaultExplanationGenerator(manager,
                reasonerFactory, ontology, null);

        //find rewriterResults for the query
        Node<OWLClass> queryEquivClasses =
                //OWLReasonerAdapter.flattenSetOfSets(reasoner.getSubClasses(query));
                reasoner.getEquivalentClasses(query);

        log.debug("classes that are equivalent to the query");
        //TODO remove for - only for debugging purposes
        for(OWLClass r:queryEquivClasses){
            log.debug("equivClass="+r);//r.getURI().getScheme()+"://"+r.getURI().getHost()+r.getURI().getPath());
        }


        OWLSubClassOfAxiom classAxiom = null;
        //OWLEquivalentClassesAxiom classAxiom = null;
        Set<Set<OWLAxiom>> exps = null;

        //For each subclass
        for(OWLClass clazz: queryEquivClasses){

            log.debug("FOR EACH CLASS #####################################################");
            log.debug("clazz="+clazz.getIRI());


            //if the class belongs to the domain ontology, do not consider it
            String string_uri =
                    clazz.getIRI().getScheme()+"://"+clazz.getIRI().toURI().getHost()+clazz.getIRI().toURI().getPath();
            if (string_uri.equals(domainOntologyIRI.toString())){
                log.debug("the class is from the domain ontology, continue");
                continue;
            }

            classAxiom = //factory.getOWLEquivalentClassesAxiom(clazz,query);
                    factory.getOWLSubClassOfAxiom(clazz, query);
            log.debug("classAx="+classAxiom+ " clazz="+clazz.getIRI()+" query="+query);
            //log.debug("getting all explanations");

            //REWRITTEN DATA RESTRICTION MAP
            log.debug("dataRestrictionMap="+dataRestrictionMap);

            Map<OWLClassExpression, OWLClassExpression> rewrittenDataRestMap =
                    rewriteDataRestrictionMap(dataRestrictionMap, reasoner, egen);

            log.debug("rewrittenDataRestMap="+rewrittenDataRestMap);


            //ALL THE EXPLANATIONS
            exps = egen.getExplanations(classAxiom);
            log.debug("explanations="+exps);
            log.debug("explanations number="+exps.size());

            Set<OWLAxiom> filteredExplanationSet = null;

            rewrittenDesc = new HashSet<List<OWLClassExpression>>();

             */
        }

}
