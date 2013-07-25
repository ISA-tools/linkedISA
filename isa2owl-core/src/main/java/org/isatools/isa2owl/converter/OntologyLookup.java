package org.isatools.isa2owl.converter;

import org.apache.log4j.Logger;
import org.isatools.isacreator.ontologymanager.BioPortalClient;
import org.isatools.isacreator.ontologymanager.OntologyManager;
import org.isatools.isacreator.ontologymanager.OntologySourceRefObject;
import org.isatools.isacreator.ontologymanager.common.OntologyTerm;

import java.util.List;

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
    private static List<org.isatools.isacreator.configuration.Ontology> allOntologies = null;
    private static OntologyLookupCache cache = new OntologyLookupCache();

    private static void getAllOntologies(BioPortalClient client) {
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

        if ((termSourceRef==null) || (termSourceRef=="") || (termAccession==null) || (termAccession==""))
            return "";

        List<OntologySourceRefObject> ontologiesUsed = OntologyManager.getOntologiesUsed();

        OntologySourceRefObject ontologySourceRefObject = null;
        for(OntologySourceRefObject ontologyRef: ontologiesUsed){
            if (termSourceRef!=null && termSourceRef.equals(ontologyRef.getSourceName())){
                ontologySourceRefObject = ontologyRef;
                break;
            }
        }

        //searching term in bioportal
        if (ontologySourceRefObject!=null){

            log.debug("Found ontology "+ontologySourceRefObject);
            BioPortalClient client = new BioPortalClient();

            if (allOntologies==null){
                getAllOntologies(client);
            }

            String ontologyVersion = getOntologyVersion(ontologySourceRefObject.getSourceName());

            OntologyTerm term = null;

            if (ontologyVersion!=null && termAccession!=null)
                term = client.getTermInformation(termAccession, ontologyVersion);

            log.debug("term====>"+term);
            if (term!=null) {
                purl = term.getOntologyPurl();
                if (purl!=null)
                    cache.addSourceTermPurlMapping(termSourceRef, termAccession, purl);
                return purl;
            }//term not null

        } //ontologySourceRefObject not null

        return null;
    }


}
