package org.isatools.isa2owl.mapping;

import org.apache.log4j.Logger;
import org.isatools.isa2owl.converter.ExtendedISASyntax;
import org.semanticweb.owlapi.model.*;


import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates ISA to OWL mapping information. All data validation is done in this class.
 * 
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 *
 */
public class ISASyntax2OWLMapping {

    private static final Logger log = Logger.getLogger(ISASyntax2OWLMapping.class);
	
	Map<String,IRI> sourceOntoIRIs = null;
	Map<String, IRI> typeMappings = null;

    //property mappings
	Map<String, Map<IRI, String>> propertyMappings = null;
    Map<String,Map<IRI, String>> contactMappings = null;
    Map<String,Map<IRI, String>> protocolMappings = null;

	Map<String, String> patternMappings = null;
	

	public ISASyntax2OWLMapping(){
		init();
	}
	
	private void init(){
		sourceOntoIRIs = new HashMap<String,IRI>();
		typeMappings = new HashMap<String, IRI>();
		propertyMappings = new HashMap<String, Map<IRI,String>>();
        contactMappings = new HashMap<String, Map<IRI,String>>();
        protocolMappings = new HashMap<String, Map<IRI,String>>();
		
	}

	/**
	 * 
	 * @param iri ontology IRI 
	 */
	public void addOntology(String name,String iri){
		if (!iri.equals(""))
			sourceOntoIRIs.put(name,IRI.create(iri));
	}

	/**
	 * 
	 * @return list of source ontologies
	 */
	public Map<String,IRI> getSourceOntoIRIs(){
		return sourceOntoIRIs;
	}
	
	public IRI getOntoIRI(String ontoID){
		return sourceOntoIRIs.get(ontoID);
	}

    public IRI getTypeMapping(String label){
        return typeMappings.get(label);
    }

	public void addTypeMapping(String label, String type){
		typeMappings.put(label, IRI.create(type));
	}


    public Map<String, Map<IRI,String>> getPropertyMappings(){
        return propertyMappings;
    }

    public Map<IRI,String> getPropertyMappings(String subject){
        return propertyMappings.get(subject);
    }

    public Map<String,Map<IRI, String>> getContactMappings(){
        return contactMappings;
    }

    public Map<String,Map<IRI, String>> getProtocolMappings(){
        return protocolMappings;
    }
	
	public void addPropertyMapping(String subject, String predicate, String object){
		Map<IRI,String> predobj = propertyMappings.get(subject);
		if (predobj==null)
			predobj = new HashMap<IRI,String>();
		if (!predicate.equals("") && !object.equals("")){
			predobj.put(IRI.create(predicate), object);
		}
		propertyMappings.put(subject, predobj);

        if (subject.startsWith(ExtendedISASyntax.STUDY_PERSON)){
            contactMappings.put(subject, predobj);
        }

        if (subject.startsWith(ExtendedISASyntax.STUDY_PROTOCOL)){
            protocolMappings.put(subject, predobj);
        }

	}



    @Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("MAPPING OBJECT(");
		builder.append("ONTOLOGIES=");
        builder.append(this.mapToString(sourceOntoIRIs));
        builder.append("\nTYPE MAPPINGS=\n");
        builder.append(this.mapToString(typeMappings));
		builder.append("\nPROPERTY MAPPINGS=\n");
		builder.append(this.mapToString(propertyMappings));
        builder.append("\nCONTACT MAPPINGS=\n");
        builder.append(this.mapToString(contactMappings));
        builder.append("\nPROTOCOL MAPPINGS=\n");
        builder.append(this.mapToString(protocolMappings));
		builder.append("\nPATTERNS");
		
		return builder.toString();
	}
	
	private <A,B> String mapToString(Map<A, B> map){
		if (map==null)
			return "";
		StringBuilder builder = new StringBuilder();
		for(A key: map.keySet()){
			builder.append(key+ "," + map.get(key)+"\n");
		}
		return builder.toString();
	}
	
}
