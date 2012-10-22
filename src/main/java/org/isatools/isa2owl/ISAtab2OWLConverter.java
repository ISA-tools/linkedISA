package org.isatools.isa2owl;

import org.apache.log4j.Logger;
import org.isatools.isacreator.io.importisa.ISAtabFilesImporter;
import org.isatools.isacreator.io.importisa.ISAtabImporter;
import org.isatools.isacreator.model.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.util.Map;


/**
 * It populates an ISA ontology with instances coming from ISATab files.
 * 
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 *
 */
public class ISAtab2OWLConverter {
	
	private static final Logger log = Logger.getLogger(ISAtab2OWLConverter.class);
	
	private ISAtabImporter importer = null;
	private String configDir = null;
    private ISASyntax2OWLMapping mapping = null;

    private OWLOntology ontology = null;
    private OWLOntologyManager manager = null;
    private OWLDataFactory factory = null;
    private IRI ontoIRI = null;
	
	
	/**
	 * Constructor
	 * 
	 * @param cDir directory where the ISA configuration file can be found
	 */
	public ISAtab2OWLConverter(String cDir, ISASyntax2OWLMapping mapping){
		configDir = cDir;
        log.debug("configDir="+configDir);
		importer = new ISAtabFilesImporter(configDir);
		System.out.println("importer="+importer);
        manager = OWLManager.createOWLOntologyManager();
        factory = manager.getOWLDataFactory();

	}
	
	
	private boolean readInISAFiles(String parentDir){
		return importer.importFile(parentDir);
	}
	
	/**
	 * 
	 * @param parentDir
	 */
	public boolean populateOntology(String parentDir){
        log.debug("In populateOntology....");
		log.debug("parentDir=" + parentDir);
		if (!readInISAFiles(parentDir)){
            System.out.println(importer.getMessagesAsString());
        }
		Investigation investigation = importer.getInvestigation();
        System.out.println("investigation=" + investigation);
        log.debug("investigation=" + investigation);
		Map<String,Study> studies = investigation.getStudies();
        System.out.println("number of studies=" + studies.keySet().size());
		for(String key: studies.keySet()){
			populateStudy(studies.get(key));
		}

        return true;
	}
	
	private void populateStudy(Study study){
        System.out.println("study id"+study.getStudyId());
		System.out.println("study desc="+study.getStudyDesc());


        //Study
        IRI type = mapping.getTypeMapping("Study");
        factory.getOWLNamedIndividual(ontoIRI.create(study.getStudyId()));



        System.out.println("ASSAYS..." + study.getAssays());
		
	}

    private void populateAssay(Assay assay){

    }

}
