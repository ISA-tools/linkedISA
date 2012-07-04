package org.isatools.isa2owl;

import org.isatools.isacreator.model.*;
import org.isatools.isacreator.io.importisa.ISAtabImporter;


/**
 * It populates an ISA ontology with instances coming from ISATab files.
 * 
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 *
 */
public class ISA2OWLInstancePopulator {
	
	private ISAtabImporter importer = null;
	private String configDir = null;
	
	public ISA2OWLInstancePopulator(String cDir){
		configDir = cDir;
		importer = new ISAtabImporter(configDir);
	}
	
	
	public void readInISAFiles(String parentDir){
		importer.importFile(parentDir);
	}
	
	public void populateOntology(){
		Investigation investigation = importer.getInvestigation();
		
		
		
	}

}
