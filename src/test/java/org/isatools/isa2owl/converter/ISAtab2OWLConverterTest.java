package org.isatools.isa2owl.converter;

import org.isatools.isa2owl.mapping.ISA2OWLMappingParserTest;
import org.isatools.isa2owl.mapping.ISA2OWLMappingParser;
import org.isatools.isa2owl.mapping.ISASyntax2OWLMapping;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;


/**
 * Test class for ISAtab2OWLConverter
 * 
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 *
 */
public class ISAtab2OWLConverterTest {

    private String baseDir = null;
	private String configDir = null;
	private String isatabParentDir = null;
    private ISAtab2OWLConverter isatab2owl = null;
    private ISA2OWLMappingParser parser = null;
    private ISASyntax2OWLMapping mapping = null;
	
	@Before
    public void setUp() throws Exception {
        //System.out.println("defined properties = "+System.getProperties());
	    baseDir = System.getProperty("user.dir");
		System.out.println("baseDir="+baseDir);
    	configDir = baseDir + "/src/test/resources/configurations/isaconfig-default_v2011-02-18/";
    	System.out.println("configDir="+configDir);

        System.out.println("Parsing the mapping...");

        parser = new ISA2OWLMappingParser();
        URL fileURL = getClass().getClassLoader().getResource(ISA2OWLMappingParserTest.ISA_OBO_MAPPING_FILENAME);
        parser.parseCSVMappingFile(fileURL.toURI().getRawPath().toString());
        mapping = parser.getMapping();

        System.out.println("MAPPING-----");
        System.out.println(mapping);

		isatab2owl = new ISAtab2OWLConverter(configDir, mapping, "http://isa-tools.org/owl/" );


    }

    @After
    public void tearDown() {
    }

    //@Test
    public void testConvertBII_I_1() {
        isatabParentDir = baseDir + "/src/test/resources/ISAtab-Datasets/BII-I-1";
        System.out.println("isatabParentDir="+isatabParentDir);

        System.out.println("Converting the ISA-tab dataset into OWL");

        assert(isatab2owl.convert(isatabParentDir));

    }


    //@Test
    public void testConvertMTBLS6() {
        isatabParentDir = baseDir + "/src/test/resources/ISAtab-Datasets/MTBLS6";
        System.out.println("isatabParentDir="+isatabParentDir);

        System.out.println("Converting the ISA-tab dataset into OWL");

        assert(isatab2owl.convert(isatabParentDir));

    }

   // @Test
    public void testConvertFaahKO() {
        isatabParentDir = baseDir + "/src/test/resources/ISAtab-Datasets/faahKO";
        System.out.println("isatabParentDir="+isatabParentDir);

        System.out.println("Converting the ISA-tab dataset into OWL");

        assert(isatab2owl.convert(isatabParentDir));

    }

    @Test
    public void testConvertFaahKO_curated() {
        isatabParentDir = baseDir + "/src/test/resources/ISAtab-Datasets/faah_archive_curated";
        System.out.println("isatabParentDir="+isatabParentDir);

        System.out.println("Converting the ISA-tab dataset into OWL");

        assert(isatab2owl.convert(isatabParentDir));

    }
}
