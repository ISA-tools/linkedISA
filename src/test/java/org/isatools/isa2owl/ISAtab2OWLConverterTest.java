package org.isatools.isa2owl;

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
    private ISAtab2OWLConverter populator = null;
    private ISA2OWLMappingParser parser = null;
    private ISASyntax2OWLMapping mapping = null;
	
	@Before
    public void setUp() throws Exception {
        //System.out.println("defined properties = "+System.getProperties());
	    baseDir = System.getProperty("user.dir");
		System.out.println("baseDir="+baseDir);
    	configDir = baseDir + "/src/test/resources/configurations/isaconfig-default_v2011-02-18/";
    	System.out.println("configDir="+configDir);

        parser = new ISA2OWLMappingParser();
        URL fileURL = getClass().getClassLoader().getResource(ISA2OWLMappingParserTest.ISA_OBO_MAPPING_FILENAME);
        parser.parseCSVMappingFile(fileURL.toURI().getRawPath().toString());
        mapping = parser.getMapping();

		populator = new ISAtab2OWLConverter(configDir, mapping);


    }

    @After
    public void tearDown() {
    }

    @Test
    public void testPopulate() {
        isatabParentDir = baseDir + "/src/test/resources/ISAtab-Datasets/BII-I-1";
        System.out.println("isatabParentDir="+isatabParentDir);
        assert(populator.populateOntology(isatabParentDir));

    }
	
}
