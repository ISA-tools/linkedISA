package org.isatools.isa2owl.repository;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 04/02/2014
 * Time: 16:15
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class Parameters {

    // Command line parameters
    public static String PARAM_CONFIG = "config";
    public static String PARAM_URL = "url";
    public static String PARAM_REPOSITORY = "repository";
    public static String PARAM_USERNAME = "username";
    public static String PARAM_PASSWORD = "password";

    // Query and miscellaneous parameters
    public static String PARAM_QUERYFILE = "queryfile";
    public static String PARAM_SHOWRESULTS = "showresults";
    public static String PARAM_SHOWSTATS = "showstats";
    public static String PARAM_UPDATES = "updates";

    // Export parameters
    public static String PARAM_EXPORT_FILE = "exportfile";
    public static String PARAM_EXPORT_FORMAT = "exportformat";
    public static String PARAM_EXPORT_TYPE = "exporttype";

    // Loading parameters
    public static String PARAM_PRELOAD = "preload";
    public static String PARAM_CONTEXT = "context";
    public static String PARAM_VERIFY = "verify";
    public static String PARAM_STOP_ON_ERROR = "stoponerror";
    public static String PARAM_PRESERVE_BNODES = "preservebnodes";
    public static String PARAM_DATATYPE_HANDLING = "datatypehandling";
    public static String PARAM_CHUNK_SIZE = "chunksize";

    // The storage for the command line parameters
    private static Map<String, String> parameters = new HashMap<String, String>();


    public static String get(String key){
        return parameters.get(key);
    }

    /**
     * The parse method that accepts an array of name-value pairs.
     *
     * @param nameValuePairs
     *            An array of name-value pairs, where each string is of the form:
     *            "<name>'separator'<value>"
     * @param separator
     *            The character that separates the name from the value
     * @param overWrite
     *            true if the parsed values should overwrite existing value
     */
    public static void parseNameValuePairs(String[] nameValuePairs, char separator, boolean overWrite) {
        for (String pair : nameValuePairs) {
            int pos = pair.indexOf(separator);
            if (pos < 0)
                throw new IllegalArgumentException("Invalid name-value pair '" + pair
                        + "', expected <name>" + separator + "<value>");
            String name = pair.substring(0, pos).toLowerCase();
            String value = pair.substring(pos + 1);
            if (overWrite)
                setValue(name, value);
            else
                setDefaultValue(name, value);
        }
    }

    /**
     * Associate the given value with the given parameter name.
     *
     * @param name
     *            The name of the parameter.
     * @param value
     *            The value of the parameter.
     */
    public static void setValue(String name, String value) {
        parameters.put(name.trim().toLowerCase(), value);
    }

    /**
     * Set a default value, i.e. set this parameter to have the given value ONLY if it has not already
     * been set.
     *
     * @param name
     *            The name of the parameter.
     * @param value
     *            The value of the parameter.
     */
    public static void setDefaultValue(String name, String value) {
        if (getValue(name) == null)
            setValue(name, value);
    }

    /**
     * Get the value associated with a parameter.
     *
     * @param name
     *            The name of the parameter.
     * @return The value associated with the parameter.
     */
    public static String getValue(String name) {
        return parameters.get(name);
    }

}
