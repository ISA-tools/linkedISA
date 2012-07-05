package org.isatools.isa2owl;

import java.net.URL;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 * Test class for ISA2OWLMappingParser.
 * 
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 *
 */
public class ISA2OWLMappingParserTest {
	
	private ISA2OWLMappingParser parser = null;
	public static final String ISA_BFO_OWL_MAPPING_FILENAME = "ISA-BFO-OWLmapping.csv";
	
	@Before
    public void setUp() {
        parser = new ISA2OWLMappingParser();
    }

    @After
    public void tearDown() {
    }
	
		
	@Test
	public void testReadCSVMappingFile() throws Exception{
		//TODO add assertions
		URL fileURL = getClass().getClassLoader().getResource(ISA2OWLMappingParserTest.ISA_BFO_OWL_MAPPING_FILENAME);
		parser.parseCSVMappingFile(fileURL.toURI().getRawPath().toString());
		System.out.println(parser.getMapping());
	}

}
