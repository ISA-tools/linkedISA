package org.isatools.isa2owl.converter;

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

    //this list will be populated only once with a query to bioportal
    private static List<org.isatools.isacreator.configuration.Ontology> allOntologies = null;

    private static void getAllOntologies(BioPortalClient client) {
        allOntologies = client.getAllOntologies();
    }

    private static String getOntologyVersion(String ontologyAbbreviation){
        System.out.println("getOntologyVersion("+ontologyAbbreviation+")");

        System.out.println("allOntologies="+allOntologies);

        for(org.isatools.isacreator.configuration.Ontology ontology: allOntologies ){

            if (ontology.getOntologyAbbreviation().equals(ontologyAbbreviation)){
                String version = ontology.getOntologyVersion();
                System.out.println("version="+version);
                return version;
            }

        }
        return null;
    }

    public static String findOntologyPURL(String termSourceRef, String termAccession){

        System.out.println("findOntologyPURL(termSourceRef="+termSourceRef+", termAccession="+termAccession+")");

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

            System.out.println("Found ontology "+ontologySourceRefObject);
            BioPortalClient client = new BioPortalClient();

            if (allOntologies==null){
                getAllOntologies(client);
            }

            String ontologyVersion = getOntologyVersion(ontologySourceRefObject.getSourceName());

            OntologyTerm term = null;

            if (termAccession!=null)
                term = client.getTermInformation(termAccession, ontologyVersion);

            System.out.println("term====>"+term);
            if (term!=null) {
                String purl = term.getOntologyPurl();

                return purl;

            }//term not null

        } //ontologySourceRefObject not null

        return null;
    }


}
