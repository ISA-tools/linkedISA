package org.isatools.isa2owl.converter;

import org.semanticweb.owlapi.model.IRI;

import java.util.UUID;

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

    // http://purl.org/isatab/investigation/BII-I-1
    public static IRI getInvestigationIRI(IRI baseIRI, String investigationId){
        //return IRI.create(baseIRI+"/investigation/"+investigationId);
        return getIRI(baseIRI);
    }

    // http://purl.org/isatab/study/1234
    public static IRI getStudyIRI(IRI baseIRI, String studyId){
        //return IRI.create(baseIRI+"/study/"+studyId);
        return getIRI(baseIRI);
    }

    // http://purl.org/isatab/study/123/assay/id/1234
    public static IRI getAssayIRI(IRI baseIRI, String studyId, String assayId){
        //return IRI.create(baseIRI+"/study/"+studyId+"/assay/id/"+assayId);
        return getIRI(baseIRI);
    }

    // http://purl.org/isatab/study/123/sample/1234
    public static IRI getSampleIRI(IRI baseIRI, String studyId, String sampleId){
        //return IRI.create(baseIRI+"/study/"+studyId+"/sample/"+sampleId);
        return getIRI(baseIRI);
    }

    // http://purl.org/isatab/study/123/source/1234
    public static IRI getSourceIRI(IRI baseIRI, String investigationID, String studyId){
        //return IRI.create(baseIRI+"/"+investigationID);
        return getIRI(baseIRI);
    }

    // http://purl.org/isatab/study/123/quality/1234
    public static IRI getQualityIRI(IRI baseIRI){
        //return IRI.create(baseIRI+"/quality/"+ Random.randomSequence(3).toString());
        return getIRI(baseIRI);
    }
}
