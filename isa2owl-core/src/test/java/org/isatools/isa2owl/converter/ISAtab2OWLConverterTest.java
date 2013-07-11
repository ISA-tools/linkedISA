package org.isatools.isa2owl.converter;

import java.net.URL;

import org.isatools.isa2owl.mapping.ISA2OWLMappingParser;
import org.isatools.isa2owl.mapping.ISASyntax2OWLMapping;
import org.isatools.isa2owl.mapping.ISASyntax2OWLMappingFiles;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Test class for ISAtab2OWLConverter
 * 
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 *
 */
public class ISAtab2OWLConverterTest {

	private String configDir = null;
	private String isatabParentDir = null;
    private String path = "/ISAtab-Datasets/";
    private String savePath=ISAtab2OWLConverterTest.class.getClass().getResource(path).getFile();
    private ISAtab2OWLConverter isatab2owl = null;
    private ISA2OWLMappingParser parser = null;
    private ISASyntax2OWLMapping mapping = null;
    private String iri = null;
	
	@Before
    public void setUp() throws Exception {
		iri = "http://isa-tools.org/isa/faahko.owl";

    	configDir = getClass().getResource("/configurations/isaconfig-default_v2013-02-13").getFile();
    	System.out.println("configDir="+configDir);
        path = "/ISAtab-Datasets/";

        System.out.println("Parsing the mapping...");

        parser = new ISA2OWLMappingParser();
        URL fileURL = getClass().getClassLoader().getResource(ISASyntax2OWLMappingFiles.ISA_OBI_MAPPING_FILENAME);
        parser.parseCSVMappingFile(fileURL.toURI().getRawPath().toString());
        mapping = parser.getMapping();

        System.out.println("MAPPING-----");
        System.out.println(mapping);

		isatab2owl = new ISAtab2OWLConverter(configDir, mapping);
    }

    @After
    public void tearDown() {
    }

    //@Test
    public void consecutiveConversions() {
        isatabParentDir = getClass().getResource(path+"BII-I-1").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        assert(isatab2owl.convert(isatabParentDir, iri));
        isatab2owl.saveOntology(savePath+"BII-I-1.owl");

        isatabParentDir = getClass().getResource( path+  "MTBLS6").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        assert(isatab2owl.convert(isatabParentDir, iri));
        isatab2owl.saveOntology(savePath+"MTBLS6.owl");
    }


    @Test
    public void testConvertBII_S_9() {
        iri = "http://isa-tools.org/isa/BII-S-9.owl";
        isatabParentDir = getClass().getResource(path+"BII-S-9").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
//        assert(isatab2owl.convert(isatabParentDir, iri));
        isatab2owl.convert(isatabParentDir, iri);
        isatab2owl.saveOntology(savePath+"BII-S-9.owl");
    }


    @Test
    public void testConvertBII_I_1() {
        iri = "http://isa-tools.org/isa/BII-I-1.owl";
        isatabParentDir = getClass().getResource(path+"BII-I-1").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        isatab2owl.convert(isatabParentDir, iri);
        isatab2owl.saveOntology(savePath+"BII-I-1.owl");
    }


    @Test
    public void testConvertMTBLS6() {
        isatabParentDir = getClass().getResource( path+  "MTBLS6").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        assert(isatab2owl.convert(isatabParentDir, iri));
        isatab2owl.saveOntology(savePath+"MTBLS6.owl");
    }


    @Test
    public void testConvertFaahKO() {
        iri = "http://isa-tools.org/isa/faahko.owl";
        isatabParentDir = getClass().getResource( path +"faahKO").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        isatab2owl.convert(isatabParentDir, iri);
        isatab2owl.saveOntology(savePath+"faahko.owl");
    }

   @Test
    public void testConvertT12by2strainsex() {
        isatabParentDir = getClass().getResource( path + "T1-2x2-strain-sex").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        isatab2owl.convert(isatabParentDir, iri);
        isatab2owl.saveOntology(savePath+"T1.owl");
    }

    @Test
    public void testConvertT3() {
        isatabParentDir = getClass().getResource( path + "T3").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);

        System.out.println("Converting the ISA-tab dataset into OWL");

        assert(isatab2owl.convert(isatabParentDir, iri));

        isatab2owl.saveOntology(savePath+"T3.owl");

    }

    @Test
    public void testConvertT4() {
        isatabParentDir = getClass().getResource(path + "T4").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        assert(isatab2owl.convert(isatabParentDir, iri));
        isatab2owl.saveOntology(savePath+"T4.owl");
    }

    @Test
    public void testConvertGWAS() {
        isatabParentDir = getClass().getResource( path +"GWAS-E-GEOD-11948-corrected-with-publication").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        isatab2owl.convert(isatabParentDir, iri);
        isatab2owl.saveOntology(savePath+"GWAS.owl");
    }

    @Test
    public void testConvertEGEOD() {
        isatabParentDir = getClass().getResource( path + "E-GEOD-25835-MPBRCA1").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        isatab2owl.convert(isatabParentDir, iri);
        isatab2owl.saveOntology(savePath+"E-GEOD-25835-MPBRCA1.owl");
    }

}
