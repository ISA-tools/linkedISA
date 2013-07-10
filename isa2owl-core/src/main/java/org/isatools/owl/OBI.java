package org.isatools.owl;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 21/02/2013
 * Time: 10:44
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class OBI {

    public static final String NAMESPACE = "http://purl.obolibrary.org/obo/OBI_";
    public static final String SHORTNAME = "OBI";

    public static final String ORGANISM = NAMESPACE + "0100026";
    public static final String HAS_SPECIFIED_INPUT = NAMESPACE + "0000293";
    public static final String HAS_SPECIFIED_OUTPUT = NAMESPACE+"0000299";
    public static final String INVESTIGATION = NAMESPACE + "0000066";
    public static final String INVESTIGATION_DESCRIPTION = NAMESPACE+ "0001615";
    public static final String INVESTIGATION_TITLE = NAMESPACE+ "0001622";
    public static final String ANALYTE_ASSAY = NAMESPACE + "0000443";


    OBI() {
    }

}
