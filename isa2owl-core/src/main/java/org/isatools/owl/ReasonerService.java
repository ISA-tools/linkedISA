package org.isatools.owl;


import org.apache.log4j.Logger;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

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

    //private OWLReasonerFactory reasonerFactory = null;
    private OWLReasoner reasoner = null;

    public ReasonerService(OWLOntology ontology){
        reasoner = PelletReasonerFactory.getInstance().createReasoner( ontology );
    }

    public boolean isConsistent(){
        return reasoner.isConsistent();
    }

    public boolean isSuperClass(OWLClass subclass, OWLClassExpression superclass, boolean direct){
        NodeSet<OWLClass> superclasses = reasoner.getSuperClasses(subclass, direct);
        return superclasses.containsEntity(subclass);
    }

}
