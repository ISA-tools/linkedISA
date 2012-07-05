package org.isatools.isa2owl;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;

import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import java.util.List;
import java.util.Map;

import java.io.File;

/**
 * Given an ISA2OWLMapping, it converts ISA to OWL (TBox).
 * 
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 *
 */
public class ISA2OWLConverter {
	
	//mapping file
	private ISA2OWLMapping mapping = null;
	
	private OWLOntology ontology = null;
	private OWLOntologyManager manager = null;
	private OWLDataFactory factory = null;
	private IRI ontoIRI = null;
	
	private List<OWLOntology> sourceOntos = null;
	
	
	/**
	 * Constructor
	 */
	public ISA2OWLConverter(ISA2OWLMapping m) throws Exception{
		mapping = m;
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		ontoIRI = IRI.create("http://www.isa-tools.org/owl/isa.owl");
		
		//TODO add AutoIRIMapper
		//adding mapper for local ontologies
		 manager.addIRIMapper(new SimpleIRIMapper(IRI.create("http://purl.obolibrary.org/bfo.owl"), IRI.create(getClass().getClassLoader().getResource("owl/ruttenberg-bfo2.owl"))));
		 manager.addIRIMapper(new SimpleIRIMapper(IRI.create("http://purl.obolibrary.org/obo/obi.owl"), IRI.create(getClass().getClassLoader().getResource("owl/obi.owl"))));
		 
		
	}
	
	/**
	 * Given an ISA2OWLMapping, it generates an OWL ontology for ISA
	 * @return
	 */
	public OWLOntology convert() throws Exception{
		try{
			ontology = manager.createOntology(ontoIRI);
			System.out.println("Newly created"+ontology);
			
			processSourceOntologies();
			processMappings();	
			
			File file = new File("/Users/agbeltran/workspace/isa2owl/src/test/resources/isa.owl");
			manager.saveOntology(ontology, IRI.create(file.toURI()));
			
			// We can also dump an ontology to System.out by specifying a different OWLOntologyOutputTarget
			// Note that we can write an ontology to a stream in a similar way using the StreamOutputTarget class
			OWLOntologyDocumentTarget documentTarget = new SystemOutDocumentTarget();
			// Try another format - The Manchester OWL Syntax
			ManchesterOWLSyntaxOntologyFormat manSyntaxFormat = new ManchesterOWLSyntaxOntologyFormat();
			OWLXMLOntologyFormat format = new OWLXMLOntologyFormat();
			if(format.isPrefixOWLOntologyFormat()) {
				manSyntaxFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat());
			}
			
			
			//manager.saveOntology(ontology, format, ontoIRI);//, new SystemOutDocumentTarget());
			manager.saveOntology(ontology, manSyntaxFormat, new SystemOutDocumentTarget());
			
			
			return ontology;
		}catch(OWLOntologyCreationException ocex){
			ocex.printStackTrace();
		}
		return null;
	
	}
	
	
	/**
	 * Private method to process the source ontologies.
	 */
	private void processSourceOntologies(){ 	
		Map<String,IRI> sourceOntoIRIs = mapping.getSourceOntoIRIs();
		OWLOntology onto = null;
			
		//TODO check imports from ontologies where the import chain implies that ontologies are duplicated
		for(IRI iri: sourceOntoIRIs.values()){
			try{
				System.out.println("iri="+iri);
				onto = manager.loadOntology(iri);
				OWLImportsDeclaration importDecl = factory.getOWLImportsDeclaration(iri);
				manager.applyChange(new AddImport(ontology, importDecl));				
				
			}catch(OWLOntologyCreationException oocrex){
				//oocrex.printStackTrace();		
			}
		}
		
	}
	
	/**
	 * Private method to process the different types of mappings.
	 */
	private void processMappings(){
		
		//EquivalentClass Mappings
		Map<String,IRI> equivClassMappings = mapping.getEquivClassMappings();
		
		for(String key: equivClassMappings.keySet()){
			OWLClass keyClass = factory.getOWLClass(IRI.create(ontoIRI+"/"+key.replaceAll(" ", "_")));
			
			System.out.println("key="+key);
			IRI type = equivClassMappings.get(key);
			System.out.println("type="+type);
			if (type!=null && !type.equals("")){
			
				OWLClass valueClass = factory.getOWLClass(type);
				OWLEquivalentClassesAxiom equivClassAxiom = 
					factory.getOWLEquivalentClassesAxiom(keyClass, valueClass);
				manager.addAxiom(ontology, equivClassAxiom);
			}
		
		}//for
		
		//SubClass Mappings
		Map<String,IRI> subClassMappings = mapping.getSubClassMappings();
		
		for(String key: subClassMappings.keySet()){
			OWLClass keyClass = factory.getOWLClass(IRI.create(ontoIRI+"/"+key.replaceAll(" ", "_")));
			
			System.out.println("key="+key);
			IRI type = subClassMappings.get(key);
			System.out.println("type="+type);
			if (type!=null && !type.equals("")){
			
				OWLClass valueClass = factory.getOWLClass(type);
				OWLSubClassOfAxiom subClassAxiom = 
					factory.getOWLSubClassOfAxiom(keyClass, valueClass);
				manager.addAxiom(ontology, subClassAxiom);
			}
		
		}//for
		
		
		//SubClass Mappings
		Map<String,Map<IRI,IRI>> propertyMappings = mapping.getPropertyMappings();

		System.out.println("Dealing with property mappings...");
		for(String key: propertyMappings.keySet()){
			OWLClass keyClass = factory.getOWLClass(IRI.create(ontoIRI+"/"+key.replaceAll(" ", "_")));
					
			System.out.println("key="+key);
			Map<IRI,IRI> predObjMap = propertyMappings.get(key);
			for(IRI propIRI: predObjMap.keySet()){
				OWLObjectProperty prop = factory.getOWLObjectProperty(propIRI);
				OWLClass objClass = factory.getOWLClass(predObjMap.get(propIRI));
				
				OWLObjectSomeValuesFrom objSomeValuesFrom = factory.getOWLObjectSomeValuesFrom(prop, objClass);
				OWLSubClassOfAxiom subClassAxiom = factory.getOWLSubClassOfAxiom(keyClass, objSomeValuesFrom);
				manager.addAxiom(ontology, subClassAxiom);
			}
				
		}//for
		
	}//processMappings

}
