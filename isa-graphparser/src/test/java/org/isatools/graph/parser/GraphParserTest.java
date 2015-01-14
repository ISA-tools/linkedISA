package org.isatools.graph.parser;

import org.apache.log4j.Logger;
import org.isatools.graph.model.impl.Graph;
import org.isatools.graph.model.impl.NodeType;
import org.isatools.isacreator.io.importisa.ISAtabFilesImporter;
import org.isatools.isacreator.model.Assay;
import org.isatools.isacreator.model.Investigation;
import org.isatools.isacreator.model.Study;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 07/11/2012
 * Time: 14:55
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class GraphParserTest {

    private static final Logger log = Logger.getLogger(GraphParserTest.class);

    private String configDir = null;

    @Before
    public void setUp(){
        configDir = getClass().getResource("/configurations/isaconfig-default_v2011-02-18/").getFile();
        System.out.println("configDir="+configDir);
    }

    @Test
    public void parseTest1(){
        String isatabParentDir = getClass().getResource("/ISAtab-Datasets/GWAS-E-GEOD-11948-corrected-with-publication").getFile();
        graphParser(isatabParentDir);
    }

    @Test
    public void parserMTBLS6Test(){
        String isatabParentDir =   getClass().getResource("/ISAtab-Datasets/MTBLS6").getFile();
        graphParser(isatabParentDir);
    }


    @Test
    public void parserTest3(){
        String isatabParentDir =   getClass().getResource("/ISAtab-Datasets/faahKO").getFile();
        graphParser(isatabParentDir);
    }


    @Test
    public void parserTest4(){
        String isatabParentDir =   getClass().getResource("/ISAtab-Datasets/MTBLS2").getFile();
        graphParser(isatabParentDir);
    }

//    @Test
//    public void parserBII_I_1Test(){
//        String isatabParentDir =   getClass().getResource("/ISAtab-Datasets/BII-I-1").getFile();
//        graphParser(isatabParentDir);
//    }

    @Test
    public void parserEGEODTest(){
        String isatabParentDir =   getClass().getResource("/ISAtab-Datasets/E-GEOD-25835-MPBRCA1").getFile();
        graphParser(isatabParentDir);
    }

    @Test
    public void parserBII_S_3Test(){
        String isatabParentDir =   getClass().getResource("/ISAtab-Datasets/BII-S-3").getFile();
        graphParser(isatabParentDir);
    }

//    @Test
//    public void parserBGISoapdenovo2Test(){
//        String isatabParentDir =   getClass().getResource("/ISAtab-Datasets/BGI-SOAPdenovo2").getFile();
//        graphParser(isatabParentDir);
//    }

//    @Test
//    public void parserBGISoapdenovo2CompactTest(){
//        String isatabParentDir =   getClass().getResource("/ISAtab-Datasets/soapdenovo2-compact").getFile();
//        graphParser(isatabParentDir);
//    }

    @Test
    public void parserCompositeTest(){
        String isatabParentDir =   getClass().getResource("/ISAtab-Datasets/ISA-composite-test").getFile();
        graphParser(isatabParentDir);
    }

    @Test
    public void parserISATABTest1(){
        String isatabParentDir =   getClass().getResource("/ISAtab-Datasets/isatab-test1").getFile();
        graphParser(isatabParentDir);
    }

    @Test
    public void parserISATABTest2(){
        String isatabParentDir =   getClass().getResource("/ISAtab-Datasets/isatab-test2").getFile();
        graphParser(isatabParentDir);
    }

    @Test
    public void parserISATABTest3(){
        String isatabParentDir =   getClass().getResource("/ISAtab-Datasets/isatab-test3").getFile();
        graphParser(isatabParentDir);
    }

    @Test
    public void parserISATABTest4(){
        String isatabParentDir =   getClass().getResource("/ISAtab-Datasets/isatab-test4").getFile();
        graphParser(isatabParentDir);
    }


    @Test
    public void parserSCC(){
        String isatabParentDir =  "/Users/agbeltran/work-datasets/StemCellCommons/isa_7068_695637/";
        graphParser(isatabParentDir);
    }


    @Test
    public void parserSCC2(){
        String isatabParentDir =  "/Users/agbeltran/work-datasets/scc/isa_11620_879353/";
        graphParser(isatabParentDir);
    }

    private void graphParser(String isatabParentDir){
        //Import ISAtab dataset
        System.out.println("configDir="+configDir);

        ISAtabFilesImporter importer = new ISAtabFilesImporter(configDir);

        System.out.println("isatabParentDir="+isatabParentDir);

        importer.importFile(isatabParentDir);

//        for(ErrorMessage error: importer.getMessages().get(0).getMessages()){
//          System.out.println(error.getMessage());
//        }

        Investigation investigation = importer.getInvestigation();

        System.out.println("investigation="+investigation);

        Map<String, Study> studies = investigation.getStudies();
        for(String studyId: studies.keySet()){

            System.out.println("Study id:"+studyId);

            Study study = studies.get(studyId);

            Object[][] data = study.getStudySampleDataMatrix();

            GraphParser parser = new GraphParser(data);
            parser.parse();

            System.out.println("STUDY SAMPLE GRAPH...");
            parser.getGraph().outputGraph();

            System.out.println("GROUPS=" + parser.getGroups());

            log.info("Material attributes..."+parser.extractMaterialAttributes());

            Map<String, Assay> assayMap = study.getAssays();

            for(String assayId: assayMap.keySet()){

                System.out.println("Assay id="+assayId);
                Assay assay = assayMap.get(assayId);

                data = assay.getAssayDataMatrix();

                parser = new GraphParser(data);
                parser.parse();

                System.out.println("ASSAY GRAPH...");
                Graph graph = parser.getGraph();
                graph.outputGraph();


                System.out.println("GROUPS=" + parser.getGroups());
                System.out.println("Material attributes..."+parser.extractMaterialAttributes());


                System.out.println("ASSAY NODES");
                System.out.println(parser.getGraph().getNodes(NodeType.ASSAY_NODE));

                System.out.println("PROCESS NODES");
                System.out.println(parser.getGraph().getNodes(NodeType.PROCESS_NODE));

                System.out.println("PROTOCOL EXECUTION NODES");
                System.out.println(parser.getGraph().getNodes(NodeType.PROTOCOL_EXECUTION_NODE));

            }

        }

    }

}
