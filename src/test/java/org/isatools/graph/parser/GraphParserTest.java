package org.isatools.graph.parser;

import org.apache.log4j.Logger;
import org.isatools.errorreporter.model.ErrorMessage;
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
    private String baseDir = null;


    @Before
    public void setUp(){
        baseDir = System.getProperty("user.dir");
        System.out.println("baseDir="+baseDir);
        configDir = baseDir + "/src/test/resources/configurations/isaconfig-default_v2011-02-18/";

    }

    @Test
    public void parserTest(){

        //Import ISAtab dataset
        ISAtabFilesImporter importer = new ISAtabFilesImporter(configDir);

        //String isatabParentDir = baseDir + "/src/test/resources/ISAtab-Datasets/MTBLS6";
        String isatabParentDir = baseDir + "/src/test/resources/ISAtab-Datasets/faahKO";
        //String isatabParentDir = baseDir + "/src/test/resources/ISAtab-Datasets/faah_archive_curated";
        System.out.println("isatabParentDir="+isatabParentDir);

        importer.importFile(isatabParentDir);

//        for(ErrorMessage error: importer.getMessages().get(0).getMessages()){
//          System.out.println(error.getMessage());
//        }

        Investigation investigation = importer.getInvestigation();

        log.info("investigation="+investigation);

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

                log.info("Assay id="+assayId);
                Assay assay = assayMap.get(assayId);

                data = assay.getAssayDataMatrix();

                parser = new GraphParser(data);
                parser.parse();

                log.info("ASSAY GRAPH...");
                parser.getGraph().outputGraph();

                log.info("GROUPS=" + parser.getGroups());
                log.info("Material attributes..."+parser.extractMaterialAttributes());
            }

        }

    }

}
