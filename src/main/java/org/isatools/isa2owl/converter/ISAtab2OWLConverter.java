package org.isatools.isa2owl.converter;

import org.apache.log4j.Logger;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.isatools.graph.model.MaterialNode;
import org.isatools.isa2owl.mapping.ISASyntax2OWLMapping;
import org.isatools.isacreator.io.importisa.ISAtabFilesImporter;
import org.isatools.isacreator.io.importisa.ISAtabImporter;
import org.isatools.isacreator.model.*;
import org.isatools.isacreator.ontologymanager.BioPortalClient;
import org.isatools.isacreator.ontologymanager.OntologyManager;
import org.isatools.isacreator.ontologymanager.OntologySourceRefObject;
import org.isatools.isacreator.ontologymanager.common.OntologyTerm;
import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.io.File;
import java.net.URISyntaxException;
import java.util.*;


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

    //ontologies IRIs
    public static String BFO_IRI = "http://purl.obolibrary.org/bfo.owl";
    public static String OBI_IRI = "http://purl.obolibrary.org/obo/extended-obi.owl";

    private Map<Publication, OWLNamedIndividual> publicationIndividualMap = null;
    private Map<Contact, OWLNamedIndividual> contactIndividualMap = null;
    private Map<String, OWLNamedIndividual> protocolIndividualMap = null;



    /**
	 * Constructor
	 * 
	 * @param cDir directory where the ISA configuration file can be found
	 */
	public ISAtab2OWLConverter(String cDir, ISASyntax2OWLMapping m, String iri){
		configDir = cDir;
        log.debug("configDir="+configDir);
        ISA2OWL.mapping = m;
        ISA2OWL.setIRI(iri);
		importer = new ISAtabFilesImporter(configDir);
		System.out.println("importer="+importer);

        try{
        //TODO add AutoIRIMapper
        //adding mapper for local ontologies
        //manager.addIRIMapper(new SimpleIRIMapper(IRI.create(ISAtab2OWLConverter.BFO_IRI), IRI.create(getClass().getClassLoader().getResource("owl/ruttenberg-bfo2.owl"))));
        ISA2OWL.manager.addIRIMapper(new SimpleIRIMapper(IRI.create(ISAtab2OWLConverter.OBI_IRI), IRI.create(getClass().getClassLoader().getResource("owl/extended-obi.owl"))));

        ISA2OWL.ontology = ISA2OWL.manager.createOntology(ISA2OWL.ontoIRI);

        //only import extended-obi.owl
        OWLImportsDeclaration importDecl = ISA2OWL.factory.getOWLImportsDeclaration(IRI.create("http://purl.obolibrary.org/obo/extended-obi.owl"));
        ISA2OWL.manager.applyChange(new AddImport(ISA2OWL.ontology, importDecl));


        }catch(URISyntaxException e){
            e.printStackTrace();
        }catch(OWLOntologyCreationException e){
            e.printStackTrace();
        }


	}

    /*
    private void processSourceOntologies(){
        Map<String,IRI> sourceOntoIRIs = mapping.getSourceOntoIRIs();
        OWLOntology onto = null;

        //TODO check imports from ontologies where the import chain implies that ontologies are duplicated
        for(IRI iri: sourceOntoIRIs.values()){
            //try{
                System.out.println("iri="+iri);
                //onto = manager.loadOntology(iri);
                OWLImportsDeclaration importDecl = factory.getOWLImportsDeclaration(iri);
                manager.applyChange(new AddImport(ontology, importDecl));

            //}catch(OWLOntologyCreationException oocrex){
                //oocrex.printStackTrace();
            //}
        }

    }
    */
	
	
	private boolean readInISAFiles(String parentDir){
		return importer.importFile(parentDir);
	}
	
	/**
	 * 
	 * @param parentDir
	 */
	public boolean convert(String parentDir){
        log.debug("In populateOntology....");
		log.debug("parentDir=" + parentDir);
		if (!readInISAFiles(parentDir)){
            System.out.println(importer.getMessagesAsString());
        }

        //processSourceOntologies();

		Investigation investigation = importer.getInvestigation();

        System.out.println("investigation=" + investigation);
        log.debug("investigation=" + investigation);

		Map<String,Study> studies = investigation.getStudies();

        System.out.println("number of studies=" + studies.keySet().size());

        //initialise the map of individuals
        ISA2OWL.typeIndividualMap = new HashMap<String, Set<OWLNamedIndividual>>();
        ISA2OWL.typeIdIndividualMap = new HashMap<String, Map<String, OWLNamedIndividual>>();
        publicationIndividualMap = new HashMap<Publication, OWLNamedIndividual>();
        contactIndividualMap = new HashMap<Contact, OWLNamedIndividual>();
        protocolIndividualMap = new HashMap<String, OWLNamedIndividual>();

        //convert each study
		for(String key: studies.keySet()){

			convertStudy(studies.get(key));

            //reset the map of type/individuals
            ISA2OWL.typeIndividualMap = new HashMap<String, Set<OWLNamedIndividual>>();
		}

        //save the ontology
        try{
        File file = new File("/Users/agbeltran/workspace-private/isa2owl/isatab-example.owl");
        ISA2OWL.manager.saveOntology(ISA2OWL.ontology, IRI.create(file.toURI()));

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
        ISA2OWL.manager.saveOntology(ISA2OWL.ontology, manSyntaxFormat, new SystemOutDocumentTarget());
        }catch(OWLOntologyStorageException e){
        e.printStackTrace();
        }

        return true;
	}


    /**
     * It converts each of the ISA Study elements into OWL
     *
     * @param study
     */
    private void convertStudy(Study study){
        log.info("Converting study " + study.getStudyId() + "...");

        //Study
        ISA2OWL.createIndividual(ExtendedISASyntax.STUDY, study.getStudyId());

        //Study identifier
        ISA2OWL.createIndividual(Study.STUDY_ID, study.getStudyId());

        //Study title
        ISA2OWL.createIndividual(Study.STUDY_TITLE, study.getStudyTitle());

        //Study description
        ISA2OWL.createIndividual(Study.STUDY_DESC, study.getStudyDesc());

        //Study File
        ISA2OWL.createIndividual(ExtendedISASyntax.STUDY_FILE, study.getStudySampleFileIdentifier());

        //Study file name
        ISA2OWL.createIndividual(Study.STUDY_FILE_NAME, study.getStudySampleFileIdentifier());

        //Publications
        List<Publication> publicationList = study.getPublications();
        convertPublications(publicationList);

        //Study design
        List<StudyDesign> studyDesignList = study.getStudyDesigns();
        for(StudyDesign studyDesign: studyDesignList){
            convertStudyDesign(studyDesign);
        }

        //Study Person
        List<Contact> contactList = study.getContacts();
        convertContacts(contactList);

        //Study Factor
        List<Factor> factorList = study.getFactors();
        convertFactors(factorList);

        //Study Protocol
        List<Protocol> protocolList = study.getProtocols();
        convertProtocols(protocolList);

        Assay2OWLConverter assay2OWLConverter = new Assay2OWLConverter();
        assay2OWLConverter.convert(study.getStudySample(), null, protocolIndividualMap);

        System.out.println("ASSAYS..." + study.getAssays());

        //Study Assays
        Map<String, Assay> assayMap = study.getAssays();
        convertAssays(assayMap);

        //dealing with all property mappings
        Map<String, Map<IRI, String>> propertyMappings = ISA2OWL.mapping.getPropertyMappings();
        for(String subjectString: propertyMappings.keySet()){
            System.out.println("subjectString="+subjectString);

            //skip Study Person properties as they are dealt with in the Contact mappings
            if (subjectString.startsWith(ExtendedISASyntax.STUDY_PERSON) ||
                    subjectString.startsWith(ExtendedISASyntax.STUDY_PROTOCOL) ||
                    subjectString.startsWith(GeneralFieldTypes.PROTOCOL_REF.toString()) ||
                    subjectString.matches(MaterialNode.REGEXP))
                continue;

            Map<IRI, String> predicateObjects = propertyMappings.get(subjectString);
            Set<OWLNamedIndividual> subjects = ISA2OWL.typeIndividualMap.get(subjectString);

            if (subjects==null)
                continue;

            for(OWLIndividual subject: subjects){

                for(IRI predicate: predicateObjects.keySet()){
                    OWLObjectProperty property = ISA2OWL.factory.getOWLObjectProperty(predicate);

                    String objectString = predicateObjects.get(predicate);

                    System.out.println("objectString="+objectString);
                    Set<OWLNamedIndividual> objects = ISA2OWL.typeIndividualMap.get(objectString);

                    if (objects==null)
                        continue;

                    for(OWLNamedIndividual object: objects){
                        System.out.println("property="+property);
                        System.out.println("subject="+subject);
                        System.out.println("object="+object);

                        if (subject==null || object==null || property==null){

                            System.err.println("At least one is null...");

                        }else{
                            OWLObjectPropertyAssertionAxiom axiom = ISA2OWL.factory.getOWLObjectPropertyAssertionAxiom(property, subject, object);
                            ISA2OWL.manager.addAxiom(ISA2OWL.ontology, axiom);
                        }
                    }//for
                } //for
            } //for
        }

        log.info("... end of conversion for Study "+study.getStudyId()+".");

    }


    /***
     *
     *
     * @param publicationList
     */
    private void convertPublications(List<Publication> publicationList){

        for(Publication pub: publicationList){

            OWLNamedIndividual individual = publicationIndividualMap.get(pub);

            for(Publication p: publicationIndividualMap.keySet()){
                System.out.println("Publication... equal? "+p.equals(pub));
            }

            if (individual!=null)
                continue;

            StudyPublication publication = (StudyPublication) pub;

            //Publication
            OWLNamedIndividual pubInd = ISA2OWL.createIndividual(ExtendedISASyntax.PUBLICATION, publication.getPubmedId());
            publicationIndividualMap.put(pub,pubInd);

            //Study PubMed ID
            ISA2OWL.createIndividual(StudyPublication.PUBMED_ID, publication.getPubmedId());

            //Study Publication DOI
            ISA2OWL.createIndividual(StudyPublication.PUBLICATION_DOI, publication.getPublicationDOI());

            //Study Publication Author List
            ISA2OWL.createIndividual(StudyPublication.PUBLICATION_AUTHOR_LIST, publication.getPublicationAuthorList());

            //Study Publication Title
            ISA2OWL.createIndividual(StudyPublication.PUBLICATION_TITLE, publication.getPublicationTitle());
        }

    }

    /**
     * Converts contact information into OWL
     *
     * @param contactsList
     */
    private void convertContacts(List<Contact> contactsList){

        System.out.println("Contact List->"+ contactsList);
        //process properties for the contactIndividuals
        Map<String,Map<IRI, String>> contactMappings = ISA2OWL.mapping.getContactMappings();
        System.out.println("contactMappings ="+contactMappings);

        Map<String, OWLNamedIndividual> contactIndividuals = null;

        for(Contact contact0: contactsList){

            for(Contact c: contactIndividualMap.keySet()){
                System.out.println("Contact is equal to previous one? -> "+c.equals(contact0));
            }

            OWLNamedIndividual ind = contactIndividualMap.get(contact0);
            if (ind!=null)
                continue;

            contactIndividuals = new HashMap<String, OWLNamedIndividual>();
            StudyContact contact = (StudyContact) contact0;

            //Study Person
            ind = ISA2OWL.createIndividual(ExtendedISASyntax.STUDY_PERSON, contact.getIdentifier(), contactIndividuals);
            contactIndividualMap.put(contact0, ind);

            //Study Person Last Name
            ISA2OWL.createIndividual(StudyContact.CONTACT_LAST_NAME, contact.getLastName(), contactIndividuals);

            //Study Person First Name
            ISA2OWL.createIndividual(StudyContact.CONTACT_FIRST_NAME, contact.getFirstName(), contactIndividuals);

            //Study Person Mid Initials
            ISA2OWL.createIndividual(StudyContact.CONTACT_MID_INITIAL, contact.getMidInitial(), contactIndividuals);

            //Study Person Email
            ISA2OWL.createIndividual(StudyContact.CONTACT_EMAIL, contact.getEmail(), contactIndividuals);

            //Study Person Phone
            ISA2OWL.createIndividual(StudyContact.CONTACT_PHONE, contact.getPhone(), contactIndividuals);

            //Study Person Fax
            ISA2OWL.createIndividual(StudyContact.CONTACT_FAX, contact.getFax(), contactIndividuals);

            //Study Person Address
            ISA2OWL.createIndividual(StudyContact.CONTACT_ADDRESS, contact.getAddress(), contactIndividuals);

            //Study Person Affiliation
            ISA2OWL.createIndividual(StudyContact.CONTACT_AFFILIATION, contact.getAffiliation(), contactIndividuals);

            System.out.println("ROLE-> "+contact.getRole());
            //Study Person Roles
            ISA2OWL.createIndividual(StudyContact.CONTACT_ROLE, contact.getRole(), contactIndividuals);

            System.out.println("contactIndividuals="+contactIndividuals);

            ISA2OWL.convertProperties(contactMappings, contactIndividuals);

        }
    }



    /**
     *
     *
     * @param factorList
     */
    private void convertFactors(List<Factor> factorList){

        for(Factor factor: factorList){

            //Study Factor
            OWLNamedIndividual factorIndividual = ISA2OWL.createIndividual(ExtendedISASyntax.STUDY_FACTOR, factor.getFactorName());

            //Study Factor Name
            ISA2OWL.createIndividual(Factor.FACTOR_NAME, factor.getFactorName());

            System.out.println("FACTOR NAME ="+factor.getFactorName());
            System.out.println("FACTOR TYPE ="+factor.getFactorType());
            System.out.println("FACTOR TYPE ACCESSION NUMBER="+factor.getFactorTypeTermAccession());
            System.out.println("FACTOR TYPE TERM SOURCE"+factor.getFactorTypeTermSource());


            //use term source and term accession to declare a more specific type for the factor
            if (factor.getFactorTypeTermAccession()!=null && !factor.getFactorTypeTermAccession().equals("")
                    && factor.getFactorTypeTermSource()!=null && factor.getFactorTypeTermSource().equals("")){

                ISA2OWL.findOntologyTermAndAddClassAssertion(factor.getFactorTypeTermSource(), factor.getFactorTypeTermAccession(), factorIndividual);



        }//factors attributes not null
        }

    }


    private void convertStudyDesign(StudyDesign studyDesign){

        //Study Design Type
        OWLNamedIndividual studyDesignIndividual = ISA2OWL.createIndividual(StudyDesign.STUDY_DESIGN_TYPE, studyDesign.getStudyDesignType());

        //use term source and term accession to declare a more specific type for the factor
        if (studyDesign.getStudyDesignTypeTermAcc()!=null && !studyDesign.getStudyDesignTypeTermAcc().equals("")
                && studyDesign.getStudyDesignTypeTermSourceRef()!=null && !studyDesign.getStudyDesignTypeTermSourceRef().equals("")){

            ISA2OWL.findOntologyTermAndAddClassAssertion(studyDesign.getStudyDesignTypeTermSourceRef(), studyDesign.getStudyDesignTypeTermAcc(), studyDesignIndividual);


        }
    }


    /**
     *
     *
     * @param protocolList
     */
    private void convertProtocols(List<Protocol> protocolList){

        Map<String, OWLNamedIndividual> protocolIndividuals = null;
        Map<String,Map<IRI, String>> protocolMappings = ISA2OWL.mapping.getProtocolMappings();
        OWLNamedIndividual individual = null;

        for(Protocol protocol: protocolList){
            protocolIndividuals = new HashMap<String, OWLNamedIndividual>();

            //Study Protocol
            individual = ISA2OWL.createIndividual(ExtendedISASyntax.STUDY_PROTOCOL, protocol.getProtocolName(), protocolIndividuals);
            protocolIndividualMap.put(protocol.getProtocolName(),individual);

            //Study Protocol Name
            ISA2OWL.createIndividual(Protocol.PROTOCOL_NAME, protocol.getProtocolName(), protocolIndividuals);

            //Study Protocol Type
            ISA2OWL.createIndividual(Protocol.PROTOCOL_TYPE, protocol.getProtocolType(), protocolIndividuals);

            //Study Protocol Description
            ISA2OWL.createIndividual(Protocol.PROTOCOL_DESCRIPTION, protocol.getProtocolDescription(), protocolIndividuals);

            //Study Protocol URI
            ISA2OWL.createIndividual(Protocol.PROTOCOL_URI, protocol.getProtocolURL(), protocolIndividuals);

            //Study Protocol Version
            ISA2OWL.createIndividual(Protocol.PROTOCOL_VERSION, protocol.getProtocolVersion(), protocolIndividuals);

            String[] parameterNames = protocol.getProtocolParameterNames();
            String[] termAccessions = protocol.getProtocolParameterNameAccessions();
            String[] termSources = protocol.getProtocolParameterNameSources();

            boolean annotated = false;
            if (parameterNames.length==termAccessions.length && parameterNames.length==termSources.length)
                annotated = true;

            int i = 0;
            for(String parameterName: parameterNames){

                //Study Protocol Parameter
                OWLNamedIndividual parameterNameIndividual = ISA2OWL.createIndividual(ExtendedISASyntax.STUDY_PROTOCOL_PARAMETER, parameterName, protocolIndividuals);

                ISA2OWL.createIndividual(Protocol.PROTOCOL_PARAMETER_NAME, parameterName, protocolIndividuals);

                if (annotated)
                    ISA2OWL.findOntologyTermAndAddClassAssertion(termSources[i], termAccessions[i], parameterNameIndividual);
                i++;
            }
            ISA2OWL.convertProperties(protocolMappings, protocolIndividuals);
        }

    }

    /**
     *
     *
     * @param assayMap
     */
    private void convertAssays(Map<String, Assay> assayMap){

        for(String assayRef: assayMap.keySet()){
            Assay assay = assayMap.get(assayRef);

            //Study Assay
            OWLNamedIndividual studyAssayIndividual = ISA2OWL.createIndividual(ExtendedISASyntax.STUDY_ASSAY, assay.getIdentifier());

            //Study Assay Measurement Type
            ISA2OWL.createIndividual(Assay.MEASUREMENT_ENDPOINT, assay.getMeasurementEndpoint());

            //Study Assay Technology Type
            ISA2OWL.createIndividual(Assay.TECHNOLOGY_TYPE, assay.getTechnologyType());

            //Study Assay File
            ISA2OWL.createIndividual(ExtendedISASyntax.STUDY_ASSAY_FILE, assay.getAssayReference());

            //Study Assay File Name
            ISA2OWL.createIndividual(Assay.ASSAY_REFERENCE, assay.getAssayReference());

            Assay2OWLConverter assayConverter = new Assay2OWLConverter();
            assayConverter.convert(assay, studyAssayIndividual, protocolIndividualMap);
        }

    }


}
