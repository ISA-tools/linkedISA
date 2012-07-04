package org.isatools.isa2owl;

import java.util.Map;
import java.util.HashMap;

import org.semanticweb.owlapi.model.*;

/**
 * Encapsulates ISA to OWL mapping information.
 * 
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 *
 */
public class ISA2OWLMapping {
	
	Map<String,IRI> sourceOntoIRIs = null;
	Map<String, String> defMappings = null;
	Map<String, IRI> equivclassMappings = null;
	Map<String, IRI> subclassMappings = null;
	Map<String, String> propertyMappings = null;
	Map<String, String> patternMappings = null;
	

	public ISA2OWLMapping(){
		init();
	}
	
	private void init(){
		sourceOntoIRIs = new HashMap<String,IRI>();
		defMappings = new HashMap<String, String>();
		equivclassMappings = new HashMap<String, IRI>();
		subclassMappings = new HashMap<String, IRI>();
	}

	/**
	 * 
	 * @param iri ontology IRI 
	 */
	public void addOntology(String name,IRI iri){
		sourceOntoIRIs.put(name,iri);
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
	
	public void addSubClassMapping(String label, IRI type){
		subclassMappings.put(label, type);
	}

	public void addEquivClassMapping(String label, IRI type){
		equivclassMappings.put(label, type);
	}

	public void addDefMapping(String label, String definition){
		defMappings.put(label, definition);
	}
	
	/**
	 * 
	 * @return
	 */
	public Map<String,IRI> getSubClassMappings(){
		return subclassMappings;
	}
	
	public Map<String, String> getPropertyMappings(){
		return propertyMappings;
	}
	
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("MAPPING OBJECT(");
		builder.append("ONTOLOGIES=");
		builder.append("\nDEFINITION MAPPINGS=");
		builder.append(this.mapToString(defMappings));
		builder.append("\nTYPE MAPPINGS=");
		builder.append(this.mapToString(subclassMappings));
		builder.append("\nPROPERTY MAPPINGS=");
		builder.append(this.mapToString(propertyMappings));
		builder.append("\nPATTERNS");
		
		return builder.toString();
	}
	
	private <A,B> String mapToString(Map<A, B> map){
		if (map==null)
			return "";
		StringBuilder builder = new StringBuilder();
		for(A key: map.keySet()){
			builder.append(key+ "," + map.get(key));
		}
		return builder.toString();
	}
	
}
