package org.isatools.linkedISA.dataset.descriptor;

import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 03/05/2013
 * Time: 17:28
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public interface ISADatasetDescriptionGenerator {

    public OWLOntology generateDescription();

}
