package org.isatools.linkedISA.mapping;

import org.apache.log4j.Logger;
import org.isatools.graph.model.MaterialNode;
import org.isatools.linkedISA.converter.ExtendedISASyntax;
import org.isatools.isacreator.model.GeneralFieldTypes;
import org.semanticweb.owlapi.model.IRI;

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

    public static String SEPARATOR_REGEXPR = "\\|";
    public static String CHAIN_PROPERTY = "chain";
	
	Map<String,IRI> sourceOntoIRIs = null;
	Map<String, IRI> typeMappings = null;

    //property mappings
	Map<String, Map<IRI, String>> propertyMappings = null;
    Map<String,Map<IRI, String>> contactPropertyMappings = null;
    Map<String,Map<IRI, String>> protocolPropertyMappings = null;
    Map<String,Map<IRI, String>> protocolREFPropertyMappings = null;
    Map<String,Map<IRI, String>> materialNodePropertyMappings = null;

	Map<String, String> patternMappings = null;
	

	public ISASyntax2OWLMapping(){
		init();
	}
	
	private void init(){
		sourceOntoIRIs = new HashMap<String,IRI>();
		typeMappings = new HashMap<String, IRI>();
		propertyMappings = new HashMap<String, Map<IRI,String>>();
        contactPropertyMappings = new HashMap<String, Map<IRI,String>>();
        protocolPropertyMappings = new HashMap<String, Map<IRI,String>>();
        protocolREFPropertyMappings = new HashMap<String, Map<IRI,String>>();
        materialNodePropertyMappings = new HashMap<String, Map<IRI,String>>();
		
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
        return contactPropertyMappings;
    }

    public Map<String,Map<IRI, String>> getProtocolMappings(){
        return protocolPropertyMappings;
    }

    public Map<String,Map<IRI, String>> getProtocolREFMappings(){
        return protocolREFPropertyMappings;
    }

    public Map<String,Map<IRI, String>> getMaterialNodePropertyMappings(){
        return materialNodePropertyMappings;
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
            contactPropertyMappings.put(subject, predobj);
        }

        if (subject.startsWith(ExtendedISASyntax.STUDY_PROTOCOL)){
            protocolPropertyMappings.put(subject, predobj);
        }

        if (subject.startsWith(GeneralFieldTypes.PROTOCOL_REF.toString())){
            protocolREFPropertyMappings.put(subject, predobj);
        }

        if (subject.matches(MaterialNode.REGEXP)){
            materialNodePropertyMappings.put(subject, predobj);
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
        builder.append("\nCONTACT PROPERTY MAPPINGS=\n");
        builder.append(this.mapToString(contactPropertyMappings));
        builder.append("\nPROTOCOL PROPERTY MAPPINGS=\n");
        builder.append(this.mapToString(protocolPropertyMappings));
        builder.append("\nPROTOCOL REF PROPERTY MAPPINGS=\n");
        builder.append(this.mapToString(protocolREFPropertyMappings));
        builder.append("\nMATERIAL NODE PROPERTY MAPPINGS=\n");
        builder.append(this.mapToString(materialNodePropertyMappings));
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
