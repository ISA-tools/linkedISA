package org.isatools.isa2owl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntology;

import java.net.URL;

/**
 * Test class for the ISA2OWLConverter.
 * 
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 *
 */
public class ISA2OWLConverterTest {
	
	
	private ISA2OWLMappingParser parser = null;
	private ISA2OWLMapping mapping = null;
	private ISA2OWLConverter converter = null;
	
	
	@Before
    public void setUp() throws Exception {
		parser = new ISA2OWLMappingParser();
		URL fileURL = getClass().getClassLoader().getResource(ISA2OWLMappingParserTest.ISA_BFO_OWL_MAPPING_FILENAME);
		parser.parseCSVMappingFile(fileURL.toURI().getRawPath().toString());
		mapping = parser.getMapping();
		System.out.println(mapping.toString());
        converter = new ISA2OWLConverter(mapping);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConvert() throws Exception{  	
    	OWLOntology onto = converter.convert();
    	System.out.println(onto);
    }

}
