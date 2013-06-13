package org.isatools.isa2owl.converter;

import org.semanticweb.owlapi.model.IRI;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 11/06/2013
 * Time: 18:35
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class ExternalRDFLinkages {

    public static IRI getPubMedIRI(String pubmedID){
        return IRI.create("http://bio2rdf.org/pubmed:"+pubmedID);
    }

}
