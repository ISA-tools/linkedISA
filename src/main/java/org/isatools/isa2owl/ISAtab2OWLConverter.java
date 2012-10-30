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
    private ISASyntax2OWLMapping mapping = null;

    private OWLOntology ontology = null;
    private OWLOntologyManager manager = null;
    private OWLDataFactory factory = null;
    private IRI ontoIRI = null;

    //ontologies IRIs
    public static String BFO_IRI = "http://purl.obolibrary.org/bfo.owl";
    public static String OBI_IRI = "http://purl.obolibrary.org/obo/extended-obi.owl";

    //<type, individual>
    private Map<String, Set<OWLNamedIndividual>> typeIndividualMap = null;
    //<type, id, individual>
    private Map<String, Map<String,OWLNamedIndividual>> typeIdIndividualMap = null;

    private Map<Publication, OWLNamedIndividual> publicationIndividualMap = null;
    private Map<Contact, OWLNamedIndividual> contactIndividualMap = null;



    /**
	 * Constructor
	 * 
	 * @param cDir directory where the ISA configuration file can be found
	 */
	public ISAtab2OWLConverter(String cDir, ISASyntax2OWLMapping m, String iri){
		configDir = cDir;
        log.debug("configDir="+configDir);
        mapping = m;
        ontoIRI = IRI.create(iri);
		importer = new ISAtabFilesImporter(configDir);
		System.out.println("importer="+importer);
        manager = OWLManager.createOWLOntologyManager();
        factory = manager.getOWLDataFactory();

        try{
        //TODO add AutoIRIMapper
        //adding mapper for local ontologies
        //manager.addIRIMapper(new SimpleIRIMapper(IRI.create(ISAtab2OWLConverter.BFO_IRI), IRI.create(getClass().getClassLoader().getResource("owl/ruttenberg-bfo2.owl"))));
        manager.addIRIMapper(new SimpleIRIMapper(IRI.create(ISAtab2OWLConverter.OBI_IRI), IRI.create(getClass().getClassLoader().getResource("owl/extended-obi.owl"))));


        ontology = manager.createOntology(ontoIRI);

        //only import extended-obi.owl
        //OWLImportsDeclaration importDecl = factory.getOWLImportsDeclaration(IRI.create("http://purl.obolibrary.org/obo/extended-obi.owl"));
        //manager.applyChange(new AddImport(ontology, importDecl));


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
        typeIndividualMap = new HashMap<String, Set<OWLNamedIndividual>>();
        typeIdIndividualMap = new HashMap<String, Map<String, OWLNamedIndividual>>();
        publicationIndividualMap = new HashMap<Publication, OWLNamedIndividual>();
        contactIndividualMap = new HashMap<Contact, OWLNamedIndividual>();

        //convert each study
		for(String key: studies.keySet()){

			convertStudy(studies.get(key));

            //reset the map of type/individuals
            typeIndividualMap = new HashMap<String, Set<OWLNamedIndividual>>();
		}

        //save the ontology
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

    /**
     * It converts each of the ISA Study elements into OWL
     *
     * @param study
     */
	private void convertStudy(Study study){
        log.info("Converting study " + study.getStudyId() + "...");

        //Study
        createIndividual(ExtendedISASyntax.STUDY,study.getStudyId());

        //Study identifier
        this.createIndividual(Study.STUDY_ID, study.getStudyId());

        //Study title
        this.createIndividual(Study.STUDY_TITLE, study.getStudyTitle());

        //Study description
        this.createIndividual(Study.STUDY_DESC, study.getStudyDesc());

        //Study File
        this.createIndividual(ExtendedISASyntax.STUDY_FILE, study.getStudySampleFileIdentifier());

        //Study file name
        this.createIndividual(Study.STUDY_FILE_NAME, study.getStudySampleFileIdentifier());

        //Publications
        List<Publication> publicationList = study.getPublications();
        convertPublications(publicationList);

        //Study Person
        List<Contact> contactList = study.getContacts();
        convertContacts(contactList);

        //Study Factor
        List<Factor> factorList = study.getFactors();
        convertFactors(factorList);

        //Study Protocol
        List<Protocol> protocolList = study.getProtocols();
        convertProtocols(protocolList);

        //Study Assays
        Map<String, Assay> assayMap = study.getAssays();
        convertAssays(assayMap);

       //dealing with all property mappings
       Map<String, Map<IRI, String>> propertyMappings = mapping.getPropertyMappings();
       for(String subjectString: propertyMappings.keySet()){
           System.out.println("subjectString="+subjectString);

           //skip Study Person properties as they are dealt with in the Contact mappings
           if (subjectString.startsWith(ExtendedISASyntax.STUDY_PERSON))
               continue;

           Map<IRI, String> predicateObjects = propertyMappings.get(subjectString);
           Set<OWLNamedIndividual> subjects = typeIndividualMap.get(subjectString);

           if (subjects==null)
               continue;

           for(OWLIndividual subject: subjects){

            for(IRI predicate: predicateObjects.keySet()){
               OWLObjectProperty property = factory.getOWLObjectProperty(predicate);

               String objectString = predicateObjects.get(predicate);

               System.out.println("objectString="+objectString);
               Set<OWLNamedIndividual> objects = typeIndividualMap.get(objectString);

               if (objects==null)
                   continue;

               for(OWLNamedIndividual object: objects){
                   System.out.println("property="+property);
                   System.out.println("subject="+subject);
                   System.out.println("object="+object);

                   if (subject==null || object==null || property==null){

                       System.err.println("At least one is null...");

                }else{
                    OWLObjectPropertyAssertionAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(property, subject, object);
                    manager.addAxiom(ontology, axiom);
                }
               }//for
            } //for
           } //for
        }

        System.out.println("ASSAYS..." + study.getAssays());

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
            OWLNamedIndividual pubInd = createIndividual(ExtendedISASyntax.PUBLICATION, publication.getPubmedId());
            publicationIndividualMap.put(pub,pubInd);

            //Study PubMed ID
            createIndividual(StudyPublication.PUBMED_ID, publication.getPubmedId());

            //Study Publication DOI
            createIndividual(StudyPublication.PUBLICATION_DOI, publication.getPublicationDOI());

            //Study Publication Author List
            createIndividual(StudyPublication.PUBLICATION_AUTHOR_LIST, publication.getPublicationAuthorList());

            //Study Publication Title
            createIndividual(StudyPublication.PUBLICATION_TITLE, publication.getPublicationTitle());
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
        Map<String,Map<IRI, String>> contactMappings = mapping.getContactMappings();
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
            ind = createIndividual(ExtendedISASyntax.STUDY_PERSON, contact.getIdentifier(), contactIndividuals);
            contactIndividualMap.put(contact0, ind);

            //Study Person Last Name
            createIndividual(StudyContact.CONTACT_LAST_NAME, contact.getLastName(), contactIndividuals);

            //Study Person First Name
            createIndividual(StudyContact.CONTACT_FIRST_NAME, contact.getFirstName(), contactIndividuals);

            //Study Person Mid Initials
            createIndividual(StudyContact.CONTACT_MID_INITIAL, contact.getMidInitial(), contactIndividuals);

            //Study Person Email
            createIndividual(StudyContact.CONTACT_EMAIL, contact.getEmail(), contactIndividuals);

            //Study Person Phone
            createIndividual(StudyContact.CONTACT_PHONE, contact.getPhone(), contactIndividuals);

            //Study Person Fax
            createIndividual(StudyContact.CONTACT_FAX, contact.getFax(), contactIndividuals);

            //Study Person Address
            createIndividual(StudyContact.CONTACT_ADDRESS, contact.getAddress(), contactIndividuals);

            //Study Person Affiliation
            createIndividual(StudyContact.CONTACT_AFFILIATION, contact.getAffiliation(), contactIndividuals);

            System.out.println("ROLE-> "+contact.getRole());
            //Study Person Roles
            createIndividual(StudyContact.CONTACT_ROLE, contact.getRole(), contactIndividuals);

            System.out.println("contactIndividuals="+contactIndividuals);

            convertProperties(contactMappings, contactIndividuals);

        }
    }

    private void convertProperties(Map<String, Map<IRI, String>> propertyMappings, Map<String, OWLNamedIndividual> typeIndividualM){

        for(String subjectString: propertyMappings.keySet()){
            System.out.println("subjectString="+subjectString);

            Map<IRI, String> predicateObjects = propertyMappings.get(subjectString);
            OWLNamedIndividual subject = typeIndividualM.get(subjectString);


                for(IRI predicate: predicateObjects.keySet()){
                    OWLObjectProperty property = factory.getOWLObjectProperty(predicate);

                    String objectString = predicateObjects.get(predicate);

                    System.out.println("objectString="+objectString);
                    OWLNamedIndividual object = typeIndividualM.get(objectString);

                        System.out.println("property="+property);
                        System.out.println("subject="+subject);
                        System.out.println("object="+object);

                        if (subject==null || object==null || property==null){

                            System.err.println("At least one is null...");

                        }else{
                            OWLObjectPropertyAssertionAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(property, subject, object);
                            manager.addAxiom(ontology, axiom);
                        }
                    }//for
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
            createIndividual(ExtendedISASyntax.STUDY_FACTOR, factor.getFactorName());

            //Study Factor Name
            createIndividual(Factor.FACTOR_NAME, factor.getFactorName());

            //if there is a type that is an ontology term, use it as the type for the factor
            System.out.println("FACTOR TYPE ="+factor.getFactorType());

            System.out.println("FACTOR TYPE ACCESSION NUMBER="+factor.getFactorTypeTermAccession());

            System.out.println("FACTOR TYPE TERM SOURCE"+factor.getFactorTypeTermSource());

            //if (factor.getFactorType()){

            //}

        }

    }

    /**
     *
     *
     * @param protocolList
     */
    private void convertProtocols(List<Protocol> protocolList){

    }

    /**
     *
     *
     * @param assayMap
     */
    private void convertAssays(Map<String, Assay> assayMap){

    }

    private OWLNamedIndividual createIndividual(String typeMappingLabel, String individualLabel, Map<String, OWLNamedIndividual> map){
        OWLNamedIndividual individual = createIndividual(typeMappingLabel, individualLabel);
        map.put(typeMappingLabel, individual);
        return individual;
    }

    /**
     *
     *
     * @param typeMappingLabel
     * @param individualLabel
     */
    private OWLNamedIndividual createIndividual(String typeMappingLabel, String individualLabel){

        //avoid empty individuals
        if (individualLabel.equals(""))
            return null;

        Map<String, OWLNamedIndividual> map = typeIdIndividualMap.get(typeMappingLabel);

        /*
        if (!allowDuplicates){
            //check if individual was already created

            if (map!=null){
                OWLNamedIndividual ind = map.get(individualLabel);
                if (ind != null) {
                    Set<OWLNamedIndividual> list = typeIndividualMap.get(typeMappingLabel);
                    if (list ==null){
                        list = new HashSet<OWLNamedIndividual>();
                    }
                    list.add(ind);
                    typeIndividualMap.put(typeMappingLabel, list);
                    return ind;
                }
            }
        }
        */

        //if it wasn't created, create it now
        IRI owlClassIRI = mapping.getTypeMapping(typeMappingLabel);
        if (owlClassIRI==null){
            System.err.println("No IRI for type " + typeMappingLabel);
            return null;
        }

        OWLNamedIndividual individual = factory.getOWLNamedIndividual(IRIGenerator.getIRI(ontoIRI));
        OWLAnnotation annotation = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()),factory.getOWLLiteral(individualLabel));
        OWLAnnotationAssertionAxiom annotationAssertionAxiom = factory.getOWLAnnotationAssertionAxiom(individual.getIRI(), annotation);
        manager.addAxiom(ontology, annotationAssertionAxiom);

        OWLClass owlClass = factory.getOWLClass(owlClassIRI);
        OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(owlClass, individual);
        manager.addAxiom(ontology,classAssertion);

        System.out.println("HERE-> "+individualLabel + " rdf:type " + owlClass );

        Set<OWLNamedIndividual> list = typeIndividualMap.get(typeMappingLabel);
        if (list ==null){
            list = new HashSet<OWLNamedIndividual>();
        }
        list.add(individual);
        typeIndividualMap.put(typeMappingLabel, list);

        if (map==null){
            map = new HashMap<String, OWLNamedIndividual>();
        }
        map.put(individualLabel, individual);
        typeIdIndividualMap.put(typeMappingLabel,map);

        return individual;
    }

}
