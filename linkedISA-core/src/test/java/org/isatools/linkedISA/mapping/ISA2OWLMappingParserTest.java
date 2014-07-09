package org.isatools.linkedISA.mapping;

import org.isatools.graph.model.ISAMaterialAttribute;
import org.isatools.graph.model.ISAMaterialNode;
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

        ISASyntax2OWLMapping mapping = parser.getMapping();

        System.out.println("propertyIRI="+mapping.getPropertyIRI("Source", "Characteristics"));

        System.out.println("propertyIRI="+mapping.getPropertyIRISubjectRegexObject("Source*", "Characteristics"));
        System.out.println("propertyIRI="+mapping.getPropertyIRISubjectRegexObject("Extract*", "Characteristics"));

        System.out.println("two regexps = " + mapping.getPropertyIRISubjectRegexObjectRegex(ISAMaterialNode.REGEXP, ISAMaterialAttribute.REGEXP));

	}

    @Test
    public void testLoadTwoMappings() throws Exception {
        URL isa_obi_mapping_url = getClass().getClassLoader().getResource(ISASyntax2OWLMappingFiles.ISA_OBI_MAPPING_FILENAME);
        parser.parseCSVMappingFile(isa_obi_mapping_url.toURI().getRawPath().toString());

        URL isa_isa_mapping_url = getClass().getClassLoader().getResource(ISASyntax2OWLMappingFiles.ISA_ISA_MAPPING_FILENAME);
        parser.parseCSVMappingFile(isa_isa_mapping_url.toURI().getRawPath().toString());

        ISASyntax2OWLMapping mapping = parser.getMapping();

        System.out.println("mapping="+mapping);
    }

    @Test
    public void testReadISA_SIOMappingFile() throws Exception{
        //TODO add assertions
        URL fileURL = getClass().getClassLoader().getResource(ISASyntax2OWLMappingFiles.ISA_SIO_MAPPING_FILENAME);
        parser.parseCSVMappingFile(fileURL.toURI().getRawPath().toString());
        System.out.println(parser.getMapping());
    }

    @Test
    public void testReadISA_ISAMappingFile() throws Exception{
        URL fileURL = getClass().getClassLoader().getResource(ISASyntax2OWLMappingFiles.ISA_ISA_MAPPING_FILENAME);
        parser.parseCSVMappingFile(fileURL.toURI().getRawPath().toString());
        System.out.println(parser.getMapping());
    }

}
