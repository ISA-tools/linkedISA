package org.isatools.isa2owl;

import org.apache.log4j.Logger;
import org.isatools.isacreator.model.*;
import org.isatools.isacreator.io.importisa.ISAtabImporter;
import org.isatools.isacreator.io.importisa.ISAtabFilesImporter;

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
		importer = new ISAtabFilesImporter(configDir);
		System.out.println("importer="+importer);
	}
	
	
	private boolean readInISAFiles(String parentDir){
		return importer.importFile(parentDir);
	}
	
	/**
	 * 
	 * @param parentDir
	 */
	public boolean populateOntology(String parentDir){
        System.out.println("In populateOntology....");
		System.out.println("parentDir="+parentDir);
		if (!readInISAFiles(parentDir)){
            System.out.println(importer.getMessagesAsString());
        }
		Investigation investigation = importer.getInvestigation();
        System.out.println("investigation="+investigation);
		Map<String,Study> studies = investigation.getStudies();
        System.out.println("number of studies="+studies.keySet().size());
		for(String key: studies.keySet()){
			populateStudy(studies.get(key));
		}

        return true;
	}
	
	private void populateStudy(Study study){
        System.out.println("study id"+study.getStudyId());
		System.out.println("study desc="+study.getStudyDesc());
		
	}

}
