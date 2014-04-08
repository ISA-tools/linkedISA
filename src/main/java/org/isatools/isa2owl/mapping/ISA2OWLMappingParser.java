 
package org.isatools.isa2owl.mapping;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

//import org.isatools.isacreator.model.*;

/**
 * Mapping from ISA format to OWL.
 * 
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 *
 */
public class ISA2OWLMappingParser {

    private static final Logger log = Logger.getLogger(ISA2OWLMappingParser.class);
	
	
	private ISASyntax2OWLMapping mapping = null;
		
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
		mapping = new ISASyntax2OWLMapping();
	}
	
	
	public ISASyntax2OWLMapping getMapping(){
		return mapping;
	}
	
	
	public void parseCSVMappingFile(String csvFilename) {//throws FileNotFoundException{
		
		try {
		CSVReader csvReader = new CSVReader(new FileReader(csvFilename));
		
	    String[] nextLine = null;
	    MappingFileField currentField = null;
	    
	    	while ((nextLine = csvReader.readNext()) != null) {
	    		//System.out.println("nextLine="+nextLine[0]);

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
                    default:
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
        log.debug("*********adding ontology " + line[0] + "," + line[2]);
		mapping.addOntology(line[0],line[2]);
		
	}
	
	private void parseAuthor(String[] line){
		
	}

    private void parseMapping(String[] line){

        if (!line[0].startsWith("{")){

            mapping.addTypeMapping(line[0], line[2]);

            //parsing property mappings
            int i = 4;
            while(i<(line.length-2) && !line[i].equals("") && !line[i+2].equals("")){
                mapping.addPropertyMapping(line[0], line[i], line[i+2]);
                i = i+4;
            }

        }else{

            String element = line[0];
            element = element.substring(1,element.length()-1);
            String[] types = element.split("\\|");

            for(int i=0; i<types.length; i++){
                mapping.addTypeMapping(types[i],line[2]);
            }

            //parsing property mappings
            int i = 4;
            while(i<(line.length-2) && !line[i].equals("") && !line[i+2].equals("")){

                String object = line[i+2];

                if (!object.startsWith("{")){

                    for(int j=0; j<types.length; j++)
                        mapping.addPropertyMapping(types[j], line[i],object);

                }else{
                    object = object.substring(1, object.length()-1);
                    String[] objects = object.split(ISASyntax2OWLMapping.SEPARATOR_REGEXPR);

                    for(int j=0; j<types.length; j++){
                        mapping.addPropertyMapping(types[j], line[i], objects[j]);
                    }
                }
                i = i+4;
            }
        }
    }
	
	private void parsePatterns(String[] line){

	}
	
}
