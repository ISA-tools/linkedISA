 
package org.isatools.isa2owl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;

//import org.isatools.isacreator.model.*;

import org.semanticweb.owlapi.model.IRI;

/**
 * Mapping from ISA format to OWL.
 * 
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 *
 */
public class ISA2OWLMappingParser {
	
	
	private ISA2OWLMapping mapping = null;
		
	private static enum MappingFileField {
		ONTOLOGIES,
		AUTHORS,
		MAPPINGS,
		PATTERNS;
		
		public static MappingFileField getField(String sField){
			for (MappingFileField f : MappingFileField.values()) {
		        if (f.name().equals(sField)) {
		            return f;
		        }
		    }
		    return null;
		}
		
		public static boolean isMappingField(String sField) {
		    for (MappingFileField f : MappingFileField.values()) {
		        if (f.name().equals(sField)) {
		            return true;
		        }
		    }
		    return false;
		}
	} 
	
	
	public ISA2OWLMappingParser(){
		mapping = new ISA2OWLMapping();
	}
	
	
	public ISA2OWLMapping getMapping(){
		return mapping;
	}
	
	
	public void parseCSVMappingFile(String csvFilename) {//throws FileNotFoundException{
		
		try {
		CSVReader csvReader = new CSVReader(new FileReader(csvFilename));
		
	    String[] nextLine = null;
	    MappingFileField currentField = null;
	    
	    	while ((nextLine = csvReader.readNext()) != null) {
	    		
	    		if (nextLine!=null && MappingFileField.isMappingField(nextLine[0])){
	    			currentField = MappingFileField.getField(nextLine[0]);
	    			System.out.println("currentField="+currentField);
	    			
	    			
	    		}else{
	    			if (nextLine[0].equals(""))
	    				continue;
	    			switch(currentField){
	    			case ONTOLOGIES:
	    				parseOntology(nextLine);
	    				break;
	    			case AUTHORS:
	    				parseAuthor(nextLine);
	    				break;
	    			case MAPPINGS:
	    				parseMapping(nextLine);
	    				break;
	    			case PATTERNS:	
	    				parsePatterns(nextLine);
	    				break;
	    			}
	    			
	    		}
	    			
	        }
	    	
	    	csvReader.close();
	    	
	    }catch(FileNotFoundException fnfex){
	    	fnfex.printStackTrace();
		}catch (IOException ioex) {
			ioex.printStackTrace();
	    }

		
	}
	
	private void parseOntology(String[] line){
		//ONTOLOGY LINE FORMAT
		//name	version	IRI
		mapping.addOntology(line[0],line[2]);
		
	}
	
	private void parseAuthor(String[] line){
		
	}
	
	private void parseMapping(String[] line){

        //MAPPING LINE FORMAT
        //label; definition; uri of equiv class; uri of superclass; string; uri of property; string; uri of object; string; ...
		mapping.addDefMapping(line[0], line[1]);
		mapping.addEquivClassMapping(line[0], line[2]);
		mapping.addSubClassMapping(line[0], line[3]);
		
		int i = 5;
		while(i<(line.length-2) && !line[i].equals("") && !line[i+2].equals("")){
			mapping.addPropertyMapping(line[0], line[i], line[i+2]);
			System.out.println("Property mapping="+line[i]+","+line[i+2]);
			i = i+4;
		} 
		
	}
	
	private void parsePatterns(String[] line){
		
	}
	
}
