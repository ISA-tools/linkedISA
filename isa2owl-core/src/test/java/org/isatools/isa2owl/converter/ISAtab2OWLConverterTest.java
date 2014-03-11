package org.isatools.isa2owl.converter;

import org.isatools.isa2owl.mapping.ISA2OWLMappingParser;
import org.isatools.isa2owl.mapping.ISASyntax2OWLMapping;
import org.isatools.isa2owl.mapping.ISASyntax2OWLMappingFiles;
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

    	configDir = //getClass().getResource("/configurations/isaconfig-default_v2013-02-13").getFile();
                getClass().getResource("/configurations/isaconfig-default_v2014-01-16").getFile();
    	System.out.println("configDir="+configDir);
        path = "/ISAtab-Datasets/";

        //System.out.println("Parsing the mapping...");

        parser = new ISA2OWLMappingParser();
        URL isa_obi_mapping_url = getClass().getClassLoader().getResource(ISASyntax2OWLMappingFiles.ISA_OBI_MAPPING_FILENAME);
        //System.out.println("isa_obi_mapping_url="+isa_obi_mapping_url);
        parser.parseCSVMappingFile(isa_obi_mapping_url.toURI().getRawPath().toString());

        URL isa_isa_mapping_url = getClass().getClassLoader().getResource(ISASyntax2OWLMappingFiles.ISA_ISA_MAPPING_FILENAME);
        //System.out.println("isa_isa_mapping_url="+isa_isa_mapping_url);
        parser.parseCSVMappingFile(isa_isa_mapping_url.toURI().getRawPath().toString());

        URL isa_prov_o_mapping_url = getClass().getClassLoader().getResource(ISASyntax2OWLMappingFiles.ISA_PROV_O_MAPPING_FILENAME);
        //System.out.println("isa_isa_mapping_url="+isa_isa_mapping_url);
        parser.parseCSVMappingFile(isa_prov_o_mapping_url.toURI().getRawPath().toString());

        mapping = parser.getMapping();
        //System.out.println("MAPPING-----");
        //System.out.println(mapping);

		isatab2owl = new ISAtab2OWLConverter(configDir, mapping);
    }

    @After
    public void tearDown() {
    }

    //@Test
    public void consecutiveConversions() {
        iri = "http://isa-tools.org/isa/BII-I-1.owl#";
        isatabParentDir = getClass().getResource(path+"BII-I-1").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        assert(isatab2owl.convert(isatabParentDir, iri));
        isatab2owl.saveOntology(savePath+"BII-I-1.owl");

        iri = "http://isa-tools.org/isa/MTBLS6.owl";
        isatabParentDir = getClass().getResource( path+  "MTBLS6").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        assert(isatab2owl.convert(isatabParentDir, iri));
        isatab2owl.saveOntology(savePath+"MTBLS6.owl");
    }

    @Test
    public void testConvertBII_I_1() throws Exception {
        iri = "http://purl.org/isatab/BII-I-1.owl";
        isatabParentDir = getClass().getResource(path+"BII-I-1").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        isatab2owl.convert(isatabParentDir, iri);
        isatab2owl.saveOntology(savePath+"BII-I-1.owl");

        isatab2owl.saveInferredOntology(savePath+"BII-I-1-inferred.owl");
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
    public void testConvertArmstrong() throws Exception {
        iri = "http://isa-tools.org/isa/ARMSTRONG-3.owl";
        isatabParentDir = getClass().getResource(path+"ARMSTRONG-3").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        isatab2owl.convert(isatabParentDir, iri);
        isatab2owl.saveOntology(savePath+"ARMSTRONG-3.owl");

        isatab2owl.saveInferredOntology(savePath+"ARMSTRONG-3-inferred.owl");
    }

    @Test
    public void testConvertBII_S_3() throws Exception {
        iri = "http://purl.org/isatab";
        isatabParentDir = getClass().getResource(path+"BII-S-3").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        isatab2owl.convert(isatabParentDir, iri);
        isatab2owl.saveOntology(savePath+"BII-S-3.owl");

        isatab2owl.saveInferredOntology(savePath+"BII-S-3-inferred.owl");
    }


    @Test
    public void testConvertMTBLS6() {
        iri = "http://isa-tools.org/isa/MTBLS6.owl";
        isatabParentDir = getClass().getResource( path+  "MTBLS6").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        assert(isatab2owl.convert(isatabParentDir, iri));
        isatab2owl.saveOntology(savePath+"MTBLS6.owl");
    }


    @Test
    public void testConvertFaahKO() {
        iri = "http://purl.org/isatab/faahko.owl";
        isatabParentDir = getClass().getResource( path +"faahKO").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        isatab2owl.convert(isatabParentDir, iri);
        isatab2owl.saveOntology(savePath+"faahko.owl");
    }

   @Test
    public void testConvertT12by2strainsex() {
       iri = "http://isa-tools.org/isa/T1.owl";
        isatabParentDir = getClass().getResource( path + "T1-2x2-strain-sex").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        isatab2owl.convert(isatabParentDir, iri);
        isatab2owl.saveOntology(savePath+"T1.owl");
    }

    @Test
    public void testConvertT3() {
        iri = "http://isa-tools.org/isa/T3.owl";
        isatabParentDir = getClass().getResource( path + "T3").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);

        System.out.println("Converting the ISA-tab dataset into OWL");

        assert(isatab2owl.convert(isatabParentDir, iri));

        isatab2owl.saveOntology(savePath+"T3.owl");

    }

    @Test
    public void testConvertT4() {
        iri = "http://isa-tools.org/isa/T4.owl";
        isatabParentDir = getClass().getResource(path + "T4").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        assert(isatab2owl.convert(isatabParentDir, iri));
        isatab2owl.saveOntology(savePath+"T4.owl");
    }

    @Test
    public void testConvertGWAS() {
        iri = "http://isa-tools.org/isa/GWAS.owl";
        isatabParentDir = getClass().getResource( path +"GWAS-E-GEOD-11948-corrected-with-publication").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        isatab2owl.convert(isatabParentDir, iri);
        isatab2owl.saveOntology(savePath+"GWAS.owl");
    }

    @Test
    public void testConvertEGEOD() {
        iri = "http://isa-tools.org/isa/E-GEOD-25835-MPBRCA1.owl";
        isatabParentDir = getClass().getResource( path + "E-GEOD-25835-MPBRCA1").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        isatab2owl.convert(isatabParentDir, iri);
        isatab2owl.saveOntology(savePath+"E-GEOD-25835-MPBRCA1.owl");
    }

    @Test
    public void testConvertMTBLS2() {
        iri = "http://isa-tools.org/isa/MTBLS2.owl";
        isatabParentDir = getClass().getResource( path + "MTBLS2").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        isatab2owl.convert(isatabParentDir, iri);
        isatab2owl.saveOntology(savePath+"MTBLS2.owl");
    }

    @Test
    public void testConvertHeck_ISAtab() {
        iri = "http://isa-tools.org/isa/Heck.owl";
        isatabParentDir = getClass().getResource( path + "Heck_ISA-tab-July10").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        isatab2owl.convert(isatabParentDir, iri);
        isatab2owl.saveOntology(savePath+"Heck.owl");
    }

    @Test
    public void testConvertSOAPdenovo2(){
        iri = "http://w3id.org/isa/soapdenovo2";
        isatabParentDir = getClass().getResource( path + "BGI-SOAPdenovo2").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        isatab2owl.convert(isatabParentDir, iri);
        isatab2owl.saveOntology(savePath+"soapdenovo2.rdf");
    }


    @Test
    public void testConvertISATABTest1(){
        iri = "http://w3id.org/isa/isatab-test1";
        isatabParentDir = getClass().getResource( path + "isatab-test1").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        isatab2owl.convert(isatabParentDir, iri);
        isatab2owl.saveOntology(savePath+"isatab-test1.rdf");

        System.out.println();
        System.out.println();
        isatab2owl.getMintedIRIs();

    }

    @Test
    public void testConvertISATABTest2(){
        iri = "http://w3id.org/isa/isatab-test2";
        isatabParentDir = getClass().getResource( path + "isatab-test2").getFile();
        System.out.println("isatabParentDir="+isatabParentDir);
        System.out.println("Converting the ISA-tab dataset into OWL");
        isatab2owl.convert(isatabParentDir, iri);
        isatab2owl.saveOntology(savePath+"isatab-test1.rdf");

        System.out.println();
        System.out.println();
        isatab2owl.getMintedIRIs();

    }


}
