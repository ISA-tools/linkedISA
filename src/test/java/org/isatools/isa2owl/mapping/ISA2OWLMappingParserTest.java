package org.isatools.isa2owl.mapping;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

/**
 * Test class for ISA2OWLMappingParser.
 * 
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 *
 */
public class ISA2OWLMappingParserTest {
	
	private ISA2OWLMappingParser parser = null;
	public static final String ISA_OBO_MAPPING_FILENAME = "mappings/ISA-OBO-mapping.csv";
    public static final String ISA_SIO_MAPPING_FILENAME = "mappings/ISA-SIO-mapping.csv";
	
	@Before
    public void setUp() {
        parser = new ISA2OWLMappingParser();
    }

    @After
    public void tearDown() {
    }
	
		
	@Test
	public void testReadISA_OBIMappingFile() throws Exception{
		//TODO add assertions
		URL fileURL = getClass().getClassLoader().getResource(ISA2OWLMappingParserTest.ISA_OBO_MAPPING_FILENAME);
		parser.parseCSVMappingFile(fileURL.toURI().getRawPath().toString());
		System.out.println(parser.getMapping());
	}

    @Test
    public void testReadISA_SIOMappingFile() throws Exception{
        //TODO add assertions
        URL fileURL = getClass().getClassLoader().getResource(ISA2OWLMappingParserTest.ISA_SIO_MAPPING_FILENAME);
        parser.parseCSVMappingFile(fileURL.toURI().getRawPath().toString());
        System.out.println(parser.getMapping());
    }

}
