package org.isatools.owl;



/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 21/02/2013
 * Time: 10:33
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class BFO {

    public static final String IRI = "http://purl.obolibrary.org/bfo.owl";;
    public static final String NAMESPACE = "http://purl.obolibrary.org/obo/BFO_";
    public static final String SHORTNAME = "BFO";

    public static final String QUALITY = NAMESPACE + "0000019";
    public static final String HAS_QUALITY = NAMESPACE + "0000086";

    public static final String IS_PART_OF = NAMESPACE + "0000050";
    public static final String HAS_PART = NAMESPACE + "0000051";
    public static final String MATERIAL_ENTITY = NAMESPACE + "0000040";

    public static final String REALIZES = NAMESPACE + "0000055";
    public static final String CONCRETIZES = NAMESPACE + "0000059";
    public static final String IS_BEARER_OF = NAMESPACE + "0000053";
    public static final String INDEPENDENT_CONTINUANT = NAMESPACE + "0000004";
    public static final String DEPENDENT_CONTINUANT = NAMESPACE + "0000005";
    public static final String IS_PRECEDED_BY = NAMESPACE + "0000062";


    BFO() {

    }
}
