package org.isatools.isa2owl.converter;

import org.semanticweb.owlapi.model.IRI;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 21/02/2013
 * Time: 10:34
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public enum IAOVocabulary {

    HAS_MEASUREMENT_VALUE(Namespaces.IAO, "0000004"),
    DENOTES(Namespaces.IAO, "0000219");

    final IRI iri;

    final Namespaces namespace;

    final String shortName;

    IAOVocabulary(Namespaces namespace, String shortName) {
        this.namespace = namespace;
        this.shortName = shortName;
        this.iri = org.semanticweb.owlapi.model.IRI.create(namespace.toString() + shortName);
    }
}
