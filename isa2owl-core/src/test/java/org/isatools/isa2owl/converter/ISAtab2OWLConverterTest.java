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
    private String path = null;
    private ISAtab2OWLConverter isatab2owl = null;
    private ISA2OWLMappingParser parser = null;
    private ISASyntax2OWLMapping mapping = null;
    private String iri = null;
	
	@Before
    public void setUp() throws Exception {

	    baseDir = System.getProperty("user.dir");
		System.out.println("baseDir="+baseDir);
    	configDir = baseDir + "/isa2owl-core/src/test/resources/configurations/isaconfig-default_v2011-02-18/";
    	System.out.println("configDir="+configDir);
        path = "/isa2owl-core/src/test/resources/ISAtab-Datasets/";

        System.out.println("Parsing the mapping...");

        parser = new ISA2OWLMappingParser();
        URL fileURL = getClass().getClassLoader().getResource(ISA2OWLMappingParserTest.ISA_OBO_MAPPING_FILENAME);
        parser.parseCSVMappingFile(fileURL.toURI().getRawPath().toString());
        mapping = parser.getMapping();

        System.out.println("MAPPING-----");
        System.out.println(mapping);

		isatab2owl = new ISAtab2OWLConverter(configDir, mapping);

        iri = "http://isa-tools.org/isa/isa.owl";


    }

    @After
    public void tearDown() {
    }

    //@Test
    public void testConvertBII_I_1() {
        isatabParentDir = baseDir + path+ "BII-I-1";
        System.out.println("isatabParentDir="+isatabParentDir);

        System.out.println("Converting the ISA-tab dataset into OWL");

        assert(isatab2owl.convert(isatabParentDir, iri));

        isatab2owl.saveOntology("/Users/agbeltran/workspace-private/isa2owl/BII-I-1.owl");

    }


    @Test
    public void testConvertMTBLS6() {
        isatabParentDir = baseDir + path+  "MTBLS6";
        System.out.println("isatabParentDir="+isatabParentDir);

        System.out.println("Converting the ISA-tab dataset into OWL");

        assert(isatab2owl.convert(isatabParentDir, iri));

        isatab2owl.saveOntology("/Users/agbeltran/workspace-private/isa2owl/MTBLS6.owl");

    }


    @Test
    public void testConvertFaahKO() {
        isatabParentDir = baseDir + path +"faahKO";
        System.out.println("isatabParentDir="+isatabParentDir);

        System.out.println("Converting the ISA-tab dataset into OWL");

        assert(isatab2owl.convert(isatabParentDir, iri));

        isatab2owl.saveOntology("/Users/agbeltran/workspace-private/isa2owl/faahko.owl");

    }

    //@Test
    public void testConvertT12by2strainsex() {
        isatabParentDir = baseDir + "/src/test/resources/ISAtab-Datasets/T1-2x2-strain-sex";
        System.out.println("isatabParentDir="+isatabParentDir);

        System.out.println("Converting the ISA-tab dataset into OWL");

        assert(isatab2owl.convert(isatabParentDir, iri));

        isatab2owl.saveOntology("/Users/agbeltran/workspace-private/isa2owl/T1.owl");

    }

    //@Test
    public void testConvertT3() {
        isatabParentDir = baseDir + "/src/test/resources/ISAtab-Datasets/T3";
        System.out.println("isatabParentDir="+isatabParentDir);

        System.out.println("Converting the ISA-tab dataset into OWL");

        assert(isatab2owl.convert(isatabParentDir, iri));

        isatab2owl.saveOntology("/Users/agbeltran/workspace-private/isa2owl/T3.owl");

    }

    @Test
    public void testConvertT4() {
        isatabParentDir = baseDir + "/src/test/resources/ISAtab-Datasets/T4";
        System.out.println("isatabParentDir="+isatabParentDir);

        System.out.println("Converting the ISA-tab dataset into OWL");

        assert(isatab2owl.convert(isatabParentDir, iri));

        isatab2owl.saveOntology("/Users/agbeltran/workspace-private/isa2owl/T4.owl");
    }

    @Test
    public void testConvertGWAS() {
        isatabParentDir = baseDir + "/src/test/resources/ISAtab-Datasets/GWAS-E-GEOD-11948-corrected-with-publication";
        System.out.println("isatabParentDir="+isatabParentDir);

        System.out.println("Converting the ISA-tab dataset into OWL");

        assert(isatab2owl.convert(isatabParentDir, iri));

        isatab2owl.saveOntology("/Users/agbeltran/workspace-private/isa2owl/GWAS.owl");
    }

    @Test
    public void testConvertEGEOD() {
        isatabParentDir = baseDir + "/src/test/resources/ISAtab-Datasets/E-GEOD-25835-MPBRCA1";
        System.out.println("isatabParentDir="+isatabParentDir);

        System.out.println("Converting the ISA-tab dataset into OWL");

        assert(isatab2owl.convert(isatabParentDir, iri));

        isatab2owl.saveOntology("/Users/agbeltran/workspace-private/isa2owl/E-GEOD-25835-MPBRCA1.owl");
    }


}