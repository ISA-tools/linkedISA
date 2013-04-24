package org.isatools.isa2owl.converter;

import org.semanticweb.owlapi.model.IRI;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 21/02/2013
 * Time: 10:44
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public enum OBIVocabulary {

    ORGANISM(Namespaces.OBI, "0100026"),
    HAS_SPECIFIED_INPUT(Namespaces.OBI, "0000293"),
    HAS_SPECIFIED_OUTPUT(Namespaces.OBI, "0000299");

    final IRI iri;

    final Namespaces namespace;

    final String shortName;

    OBIVocabulary(Namespaces namespace, String shortName) {
        this.namespace = namespace;
        this.shortName = shortName;
        this.iri = org.semanticweb.owlapi.model.IRI.create(namespace.toString() + shortName);
    }
}
