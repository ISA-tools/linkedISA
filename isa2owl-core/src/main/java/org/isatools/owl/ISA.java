package org.isatools.owl;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 10/07/2013
 * Time: 11:19
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class ISA {

    public static final String IRI = "http://purl.org/isaterms";
    public static final String NAMESPACE = "http://purl.org/isaterms/";
    public static final String SHORTNAME = "ISA";

    public static final String INVESTIGATION = NAMESPACE + "investigation";//"00000058";
    public static final String FACTOR_VALUE = NAMESPACE + "factor_value";//"00000029";
    public static final String HAS_FACTOR_VALUE = NAMESPACE  + "has_factor_value";//+ "00000093";
    public static final String HAS_VALUE = NAMESPACE + "has_value";//+ "00000089";

    public static final String ISA_DATASET = NAMESPACE + "ISA_dataset";
    public static final String ISATAB_DISTRIBUTION = NAMESPACE + "ISAtab_distribution";
    public static final String ISAOWL_DISTRIBUTION = NAMESPACE + "ISAowl_distribution";
    public static final String HAS_PART = NAMESPACE + "has_part";
    public static final String INVESTIGATION_FILE = NAMESPACE + "investigation_file";
    public static final String STUDY_FILE = NAMESPACE + "study_file";
    public static final String ASSAY_FILE = NAMESPACE + "assay_file";

    public static final String POINTS_TO = NAMESPACE + "points_to";
    public static final String DESCRIBES = NAMESPACE + "describes";

    public static final String EXECUTES = NAMESPACE + "executes";
    public static final String HAS_MEMBER = NAMESPACE + "has_member";
}
