package org.isatools.graph.parser;

import org.apache.log4j.Logger;
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
    public void parserTest2(){
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

            log.info("STUDY SAMPLE GRAPH...");
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
                parser.getGraph().outputGraph();

                System.out.println("GROUPS=" + parser.getGroups());
                System.out.println("Material attributes..."+parser.extractMaterialAttributes());
            }

        }

    }

}
