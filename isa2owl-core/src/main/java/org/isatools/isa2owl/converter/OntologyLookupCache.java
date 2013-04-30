package org.isatools.isa2owl.converter;

import org.isatools.isacreator.ontologymanager.OntologySourceRefObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 08/04/2013
 * Time: 16:36
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class OntologyLookupCache {

    private Map<OntologySourceRefObject, String> ontologySourceAccession = null;

    private Map<String, Map<String, String>> sourceTermPurlMap = null;

    public OntologyLookupCache(){
        sourceTermPurlMap = new HashMap<String, Map<String, String>>();
    }

    public void addSourceTermPurlMapping(String source, String term, String purl){
        Map<String, String> termPurlMap = sourceTermPurlMap.get(source);

        if (termPurlMap==null){
            termPurlMap = new HashMap<String, String>();
        }
        termPurlMap.put(term, purl);
        sourceTermPurlMap.put(source, termPurlMap);
    }

    public String getPurl(String source, String term){
        Map<String, String> termPurlMap = sourceTermPurlMap.get(source);

        if (termPurlMap!=null){
            return termPurlMap.get(term);
        }
        return null;
    }

}
