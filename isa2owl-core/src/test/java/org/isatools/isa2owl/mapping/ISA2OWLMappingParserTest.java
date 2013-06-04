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
		URL fileURL = getClass().getClassLoader().getResource(ISASyntax2OWLMappingFiles.ISA_OBI_MAPPING_FILENAME);
		parser.parseCSVMappingFile(fileURL.toURI().getRawPath().toString());
		System.out.println(parser.getMapping());

        System.out.println("propertyIRI="+parser.getMapping().getPropertyIRI("Source","Characteristics"));

        System.out.println("propertyIRI="+parser.getMapping().getPropertyIRIRegex("Source*","Characteristics"));
        System.out.println("propertyIRI="+parser.getMapping().getPropertyIRIRegex("Extract*","Characteristics"));
	}

    @Test
    public void testReadISA_SIOMappingFile() throws Exception{
        //TODO add assertions
        URL fileURL = getClass().getClassLoader().getResource(ISASyntax2OWLMappingFiles.ISA_SIO_MAPPING_FILENAME);
        parser.parseCSVMappingFile(fileURL.toURI().getRawPath().toString());
        System.out.println(parser.getMapping());
    }

}
