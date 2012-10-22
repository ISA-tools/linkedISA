package org.isatools.isa2owl;

import org.apache.log4j.Logger;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.isatools.isacreator.io.importisa.ISAtabFilesImporter;
import org.isatools.isacreator.io.importisa.ISAtabImporter;
import org.isatools.isacreator.model.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.*;

import java.io.File;
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
	public ISAtab2OWLConverter(String cDir, ISASyntax2OWLMapping m){
		configDir = cDir;
        log.debug("configDir="+configDir);
        mapping = m;
		importer = new ISAtabFilesImporter(configDir);
		System.out.println("importer="+importer);
        manager = OWLManager.createOWLOntologyManager();
        factory = manager.getOWLDataFactory();
        try{
        ontology = manager.createOntology(ontoIRI);
        }catch(OWLOntologyCreationException e){
            e.printStackTrace();
        }


	}

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

        processSourceOntologies();

		Investigation investigation = importer.getInvestigation();
        System.out.println("investigation=" + investigation);
        log.debug("investigation=" + investigation);
		Map<String,Study> studies = investigation.getStudies();
        System.out.println("number of studies=" + studies.keySet().size());
		for(String key: studies.keySet()){
			populateStudy(studies.get(key));
		}

        try{
        File file = new File("/Users/agbeltran/workspace-private/isa2owl/isatab-example.owl");
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
        //save ontology
        manager.saveOntology(ontology, manSyntaxFormat, new SystemOutDocumentTarget());
        }catch(OWLOntologyStorageException e){
        e.printStackTrace();
        }

        return true;
	}
	
	private void populateStudy(Study study){
        System.out.println("study id"+study.getStudyId());
		System.out.println("study desc="+study.getStudyDesc());


        //Study
        IRI study_class_iri = mapping.getTypeMapping("Study");
        OWLNamedIndividual study_individual = factory.getOWLNamedIndividual(ontoIRI.create(study.getStudyId()));
        OWLClass study_class = factory.getOWLClass(study_class_iri);
        OWLClassAssertionAxiom study_class_assertion = factory.getOWLClassAssertionAxiom(study_class, study_individual);
        manager.addAxiom(ontology,study_class_assertion);

        //Study identifier
        IRI study_identifier_class_iri = mapping.getTypeMapping(Study.STUDY_ID);
        OWLNamedIndividual study_identifier_individual = factory.getOWLNamedIndividual(ontoIRI.create(study.getStudyId()+"_identifier"));
        OWLClass study_identifier_class = factory.getOWLClass(study_identifier_class_iri);
        OWLClassAssertionAxiom study_identifier_class_assertion = factory.getOWLClassAssertionAxiom(study_identifier_class, study_identifier_individual);
        manager.addAxiom(ontology,study_identifier_class_assertion);

        //properties for Study identifier
        //TODO

        //Study title
        IRI study_title_iri = mapping.getTypeMapping(Study.STUDY_TITLE);
        OWLNamedIndividual study_title_individual = factory.getOWLNamedIndividual(ontoIRI.create(study.getStudyId()+"_title"));
        OWLClass study_title_class = factory.getOWLClass(study_title_iri);
        OWLClassAssertionAxiom study_title_class_assertion = factory.getOWLClassAssertionAxiom(study_title_class, study_title_individual);
        manager.addAxiom(ontology,study_title_class_assertion);

        //TODO add properties for title

        //Study description
        IRI study_description_iri = mapping.getTypeMapping(Study.STUDY_DESC);
        OWLNamedIndividual study_description_individual = factory.getOWLNamedIndividual(ontoIRI.create(study.getStudyId()+"_description"));
        OWLClass study_description_class = factory.getOWLClass(study_description_iri);
        OWLClassAssertionAxiom study_description_class_assertion = factory.getOWLClassAssertionAxiom(study_description_class, study_description_individual);
        manager.addAxiom(ontology,study_description_class_assertion);



        System.out.println("ASSAYS..." + study.getAssays());
		
	}

    private void populateAssay(Assay assay){

    }

}
