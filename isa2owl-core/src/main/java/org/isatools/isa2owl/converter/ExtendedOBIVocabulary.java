package org.isatools.isa2owl.converter;

import org.semanticweb.owlapi.model.IRI;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 25/02/2013
 * Time: 14:15
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public enum ExtendedOBIVocabulary {


    //Currently in ISA extension of OBI
//    public static IRI ISA_HAS_VALUE = IRI.create("http://isa-tools.org/isa/ISA_0000144");
//    public static final IRI ISA_HAS_MEMBER_IRI = IRI.create("http://isa-tools.org/isa/ISA0000018");
//    public static final IRI ISA_EXECUTES = IRI.create("http://isa-tools.org/isa/ISA0000001");

    HAS_VALUE(Namespaces.ISA, "0000144"),
    HAS_MEMBER(Namespaces.ISA, "0000018"),
    EXECUTES(Namespaces.ISA, "0000001");


    final IRI iri;

    final Namespaces namespace;

    final String shortName;

    ExtendedOBIVocabulary(Namespaces namespace, String shortName) {
        this.namespace = namespace;
        this.shortName = shortName;
        this.iri = org.semanticweb.owlapi.model.IRI.create(namespace.toString() + shortName);
    }
}
