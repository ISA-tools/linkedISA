package org.isatools.isa2owl.converter;

import java.util.UUID;

import org.semanticweb.owlapi.model.IRI;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 25/10/2012
 * Time: 15:56
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class IRIGenerator {


    public IRIGenerator(){
    }

    public static IRI getIRI(IRI baseIRI){
         return IRI.create(baseIRI+UUID.randomUUID().toString());
    }



}
