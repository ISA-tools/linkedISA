package org.isatools.isa2owl.converter;


import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Set;
import org.isatools.isacreator.configuration.Ontology;
import org.isatools.isacreator.ontologymanager.BioPortal4Client;
import org.isatools.isacreator.ontologymanager.OntologyManager;
import org.isatools.isacreator.ontologymanager.OntologySourceRefObject;
import org.isatools.isacreator.ontologymanager.common.OntologyTerm;

import java.util.Collection;
import java.util.Set;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 10/04/2013
 * Time: 11:36
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class OntologyLookup {


    private static final Logger log = Logger.getLogger(OntologyLookup.class);

    //this list will be populated only once with a query to bioportal
    private static Collection<Ontology> allOntologies = null;
    private static OntologyLookupCache cache = new OntologyLookupCache();

    private static void getAllOntologies(BioPortal4Client client) {
        allOntologies = client.getAllOntologies();
    }

    private static String getOntologyVersion(String ontologyAbbreviation){
        log.debug("getOntologyVersion(" + ontologyAbbreviation + ")");

        log.debug("allOntologies="+allOntologies);

        for(org.isatools.isacreator.configuration.Ontology ontology: allOntologies ){

            if (ontology.getOntologyAbbreviation().equals(ontologyAbbreviation)){
                String version = ontology.getOntologyVersion();
                log.debug("version="+version);
                return version;
            }

        }
        return null;
    }

    public static String findOntologyPURL(String termSourceRef, String termAccession){

        log.debug("findOntologyPURL(termSourceRef=" + termSourceRef + ", termAccession=" + termAccession + ")");

        String purl = cache.getPurl(termSourceRef, termAccession);
        if (purl!=null) {
            log.debug("IN CACHE!!! "+ termSourceRef +" " + termAccession + " " + purl);
            return purl;
        }

        if ((termSourceRef==null) || (termSourceRef.equals("")) || (termAccession==null) || (termAccession.equals("")))
            return "";

        Set<OntologySourceRefObject> ontologiesUsed = OntologyManager.getOntologiesUsed();

        OntologySourceRefObject ontologySourceRefObject = null;
        for(OntologySourceRefObject ontologyRef: ontologiesUsed){
            if (termSourceRef!=null && termSourceRef.equals(ontologyRef.getSourceName())){
                ontologySourceRefObject = ontologyRef;
                break;
            }
        }

        //searching term in bioportal
        if (ontologySourceRefObject!=null){

            System.out.println("Found ontology "+ontologySourceRefObject);
            BioPortal4Client client = new BioPortal4Client();

            if (allOntologies==null){
               allOntologies = client.getAllOntologies();
            }

            String ontologyVersion = getOntologyVersion(ontologySourceRefObject.getSourceName());

            OntologyTerm term = null;


            if (termAccession!=null)

                term = client.getTerm(termAccession, ontologyVersion);

            log.debug("term====>"+term);
            if (term!=null) {
                purl = term.getOntologyTermURI();
                if (purl!=null)
                    cache.addSourceTermPurlMapping(termSourceRef, termAccession, purl);
                return purl;
            }//term not null

        } //ontologySourceRefObject not null

        cache.addSourceTermPurlMapping(termSourceRef, termAccession, "");
        return null;
    }


}
