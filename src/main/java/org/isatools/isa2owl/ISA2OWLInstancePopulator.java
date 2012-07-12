package org.isatools.isa2owl;

import org.apache.log4j.Logger;
import org.isatools.isacreator.model.*;
import org.isatools.isacreator.io.importisa.ISAtabImporter;

import java.util.Map;



/**
 * It populates an ISA ontology with instances coming from ISATab files.
 * 
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 *
 */
public class ISA2OWLInstancePopulator {
	
	private static final Logger log = Logger.getLogger(ISA2OWLInstancePopulator.class.getName());
	
	private ISAtabImporter importer = null;
	private String configDir = null;
	
	
	/**
	 * Constructor
	 * 
	 * @param cDir directory where the ISA configuration file can be found
	 */
	public ISA2OWLInstancePopulator(String cDir){
		configDir = cDir;
		importer = new ISAtabImporter(configDir);
		System.out.println("importer="+importer);
	}
	
	
	private void readInISAFiles(String parentDir){
		importer.importFile(parentDir);
	}
	
	/**
	 * 
	 * @param parentDir
	 */
	public void populateOntology(String parentDir){
		System.out.println("parentDir="+parentDir);
		readInISAFiles(parentDir);
		Investigation investigation = importer.getInvestigation();
        System.out.println("investigation="+investigation);
		Map<String,Study> studies = investigation.getStudies();
        System.out.println("number of studies="+studies.keySet().size());
		for(String key: studies.keySet()){
			populateStudy(studies.get(key));
		}
		
	}
	
	private void populateStudy(Study study){
        System.out.println("study id"+study.getStudyId());
		System.out.println("study desc="+study.getStudyDesc());
		
	}

}
