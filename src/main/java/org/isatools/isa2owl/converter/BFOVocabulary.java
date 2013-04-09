package org.isatools.isa2owl.converter;

import org.semanticweb.owlapi.model.IRI;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 21/02/2013
 * Time: 10:33
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public enum BFOVocabulary {

//    public static final IRI BFO_INDEPENDENT_CONTINUANT_IRI = IRI.create("http://purl.obolibrary.org/obo/BFO_0000004");
//    public static final String BFO_DEPENDENT_CONTINUANT_IRI = "http://purl.obolibrary.org/obo/BFO_0000005";
//
//    public static final IRI BFO_REALIZES = IRI.create("http://purl.obolibrary.org/obo/BFO_0000055");
//    public static final IRI BFO_CONCRETIZES = IRI.create("http://purl.obolibrary.org/obo/BFO_0000059");

//public static final IRI BFO_IS_PART_OF = IRI.create("http://purl.obolibrary.org/obo/BFO_0000050");


    HAS_QUALITY(Namespaces.BFO, "0000086");

    final IRI iri;

    final Namespaces namespace;

    final String shortName;

    BFOVocabulary(Namespaces namespace, String shortName) {
        this.namespace = namespace;
        this.shortName = shortName;
        this.iri = org.semanticweb.owlapi.model.IRI.create(namespace.toString() + shortName);
    }
}
