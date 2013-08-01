package org.isatools.isa2owl.converter;

import org.apache.log4j.Logger;
import org.isatools.graph.model.impl.MaterialNode;
import org.isatools.isa2owl.mapping.ISASyntax2OWLMapping;
import org.isatools.isacreator.io.importisa.ISAtabFilesImporter;
import org.isatools.isacreator.io.importisa.ISAtabImporter;
import org.isatools.isacreator.model.*;
import org.isatools.isacreator.ontologymanager.OntologyManager;
import org.isatools.isacreator.ontologymanager.OntologySourceRefObject;
import org.isatools.owl.ExtendedOBIVocabulary;
import org.isatools.owl.OWLUtil;
import org.isatools.owl.reasoner.ReasonerService;
import org.isatools.syntax.ExtendedISASyntax;
import org.isatools.util.Pair;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * It converts an ISAtab dataset into RDF based on a given ISA2OWL mapping
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
    public static String OBI_IRI = "http://purl.obolibrary.org/obo/isa-obi-module.owl";

    private Map<Publication, OWLNamedIndividual> publicationIndividualMap = null;
    private Map<Contact, OWLNamedIndividual> contactIndividualMap = null;
    private Map<String, OWLNamedIndividual> protocolIndividualMap = null;
    private Map<String, OWLNamedIndividual> sampleIndividualMap = null;
    private Map<String, OWLNamedIndividual> measurementTechnologyIndividuals = new HashMap<String, OWLNamedIndividual>();
    private Map<String, OWLNamedIndividual> affiliationIndividualMap = null;

    /**
     * Constructor
     *
     * @param cDir directory where the ISA configuration file can be found
     */
    public ISAtab2OWLConverter(String cDir, ISASyntax2OWLMapping m){
        configDir = cDir;
        log.debug("configDir="+configDir);
        ISA2OWL.mapping = m;
        importer = new ISAtabFilesImporter(configDir);
    }


    /**
     *
     * Method to convert the ISA-TAB data set into RDF/OWL.
     *
     * @param parentDir
     */
    public boolean convert(String parentDir, String iri){
        log.info("Converting ISA-TAB dataset " + parentDir + " into RDF/OWL");

        ISA2OWL.setIRI(iri);

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

        if (!readInISAFiles(parentDir)){
            log.debug(importer.getMessagesAsString());
        }

        //initialise the map of individuals
        ISA2OWL.typeIndividualMap = new HashMap<String, Set<OWLNamedIndividual>>();
        publicationIndividualMap = new HashMap<Publication, OWLNamedIndividual>();
        contactIndividualMap = new HashMap<Contact, OWLNamedIndividual>();
        protocolIndividualMap = new HashMap<String, OWLNamedIndividual>();
        affiliationIndividualMap = new HashMap<String, OWLNamedIndividual>();

        Investigation investigation = importer.getInvestigation();
        //processSourceOntologies();

        log.debug("investigation=" + investigation);
        log.debug("Ontology selection history--->" + OntologyManager.getOntologySelectionHistory());

        convertInvestigation(investigation);

        Map<String,Study> studies = investigation.getStudies();

        log.debug("number of studies=" + studies.keySet().size());


        //convert each study
        for(String key: studies.keySet()){
            convertStudy(studies.get(key));
            //reset the map of type/individuals
            //ISA2OWL.typeIndividualMap = new HashMap<String, Set<OWLNamedIndividual>>();

            //remove from the map of type/individuals, anything that is not related to the Investigation
            HashMap newMap = new HashMap<String, Set<OWLNamedIndividual>>();
            Set<String> keys = ISA2OWL.typeIndividualMap.keySet();
            for(String mapkey: keys){
                if (mapkey.startsWith(ExtendedISASyntax.INVESTIGATION))
                    newMap.put(mapkey,ISA2OWL.typeIndividualMap.get(mapkey));
            }
            ISA2OWL.typeIndividualMap = newMap;
        }

        return true;
    }



    /**
     * TODO imports from mapping vs imports from ISAtab dataset
     *
     */
    private void processSourceOntologies(){
        log.debug("PROCESS SOURCE ONTOLOGY");

        //ontologies from the mapping
        //Map<String,IRI> sourceOntoIRIs = ISA2OWL.mapping.getSourceOntoIRIs();

        //ontologies from the ISAtab dataset
        List<OntologySourceRefObject> sourceRefObjects = OntologyManager.getOntologiesUsed();


        //TODO check imports from ontologies where the import chain implies that ontologies are duplicated
        //for(IRI iri: sourceOntoIRIs.values()){
        for(OntologySourceRefObject sourceRefObject: sourceRefObjects) {
            //try{
            //System.out.println("iri="+iri);
            //onto = manager.loadOntology(iri);
            String sourceFile = sourceRefObject.getSourceFile();
            log.debug("sourceFile="+sourceFile);

            if (sourceFile==null || sourceFile.equals("") || sourceFile.contains("obi") || !sourceFile.contains("http"))
                continue;


            OWLImportsDeclaration importDecl = ISA2OWL.factory.getOWLImportsDeclaration(IRI.create(sourceFile));
            ISA2OWL.manager.applyChange(new AddImport(ISA2OWL.ontology, importDecl));

            //}catch(OWLOntologyCreationException oocrex){
            //oocrex.printStackTrace();
            //}
        }

    }


    /***
     * Parsers ISA-tab files
     *
     * @param parentDir
     * @return
     */
    private boolean readInISAFiles(String parentDir){
        //TODO check parser errors!
        return importer.importFile(parentDir);
    }

    /**
     * Saves resulting ontology
     * @param filename
     */
    public void saveOntology(String filename){
        File file = new File(filename);
        OWLUtil.saveRDFXML(ISA2OWL.ontology, IRI.create(file.toURI()));
        //OWLUtil.systemOutputMOWLSyntax(ISA2OWL.ontology);
    }

    public void saveInferredOntology(String filename) throws Exception{
        ReasonerService reasoner = new ReasonerService(ISA2OWL.manager,ISA2OWL.ontology);
        reasoner.saveInferredOntology(filename);
    }

    private void convertInvestigation(Investigation investigation){

        //Investigation
        OWLNamedIndividual investigationIndividual = ISA2OWL.createIndividual(ExtendedISASyntax.INVESTIGATION, investigation.getInvestigationId());

        //Investigation identifier
        ISA2OWL.createIndividual(Investigation.INVESTIGATION_ID_KEY,  investigation.getInvestigationId());

        //Investigation title
        OWLNamedIndividual invTitleIndividual = ISA2OWL.createIndividual(Investigation.INVESTIGATION_TITLE_KEY, investigation.getInvestigationId()+ISA2OWL.TITLE_SUFFIX, investigation.getInvestigationTitle());
        if (invTitleIndividual!=null){
            OWLDataProperty hasMeasurementValue = ISA2OWL.factory.getOWLDataProperty(ExtendedOBIVocabulary.HAS_VALUE.iri);
            OWLLiteral titleLiteral = ISA2OWL.factory.getOWLLiteral(investigation.getInvestigationTitle(), OWL2Datatype.XSD_STRING);
            OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom = ISA2OWL.factory.getOWLDataPropertyAssertionAxiom(hasMeasurementValue, invTitleIndividual, titleLiteral);
            ISA2OWL.manager.addAxiom(ISA2OWL.ontology, dataPropertyAssertionAxiom);
        }

        //Investigation description
        OWLNamedIndividual investigationDescriptionIndividual = ISA2OWL.createIndividual(Investigation.INVESTIGATION_DESCRIPTION_KEY, investigation.getInvestigationId()+ISA2OWL.DESCRIPTION_SUFFIX, investigation.getInvestigationDescription());
        if (investigationDescriptionIndividual!=null){
            OWLDataProperty hasMeasurementValue = ISA2OWL.factory.getOWLDataProperty(ExtendedOBIVocabulary.HAS_VALUE.iri);
            OWLLiteral descriptionLiteral = ISA2OWL.factory.getOWLLiteral(investigation.getInvestigationDescription(), OWL2Datatype.XSD_STRING);
            OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom = ISA2OWL.factory.getOWLDataPropertyAssertionAxiom(hasMeasurementValue, investigationDescriptionIndividual, descriptionLiteral);
            ISA2OWL.manager.addAxiom(ISA2OWL.ontology, dataPropertyAssertionAxiom);
        }


        ISA2OWL.createIndividual(Investigation.INVESTIGATION_SUBMISSION_DATE_KEY, investigation.getSubmissionDate());

        OWLNamedIndividual publicReleaseDateIndividual = ISA2OWL.createIndividual(Investigation.INVESTIGATION_PUBLIC_RELEASE_KEY, investigation.getInvestigationId()+ISA2OWL.STUDY_PUBLIC_RELEASE_DATE_SUFFIX);

        if (publicReleaseDateIndividual!=null){
            OWLDataProperty hasMeasurementValue = ISA2OWL.factory.getOWLDataProperty(ExtendedOBIVocabulary.HAS_VALUE.iri);
            OWLLiteral publicReleaseDateLiteral = ISA2OWL.factory.getOWLLiteral(investigation.getPublicReleaseDate(), OWL2Datatype.XSD_STRING);
            OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom = ISA2OWL.factory.getOWLDataPropertyAssertionAxiom(hasMeasurementValue, publicReleaseDateIndividual, publicReleaseDateLiteral);
            ISA2OWL.manager.addAxiom(ISA2OWL.ontology, dataPropertyAssertionAxiom);
        }


        //Publications
        List<Publication> publicationList = investigation.getPublications();
        convertPublications(publicationList, investigationIndividual);

        //Investigation Person
        List<Contact> contactList = investigation.getContacts();
        convertContacts(contactList,investigationIndividual);


    }

    /**
     * It converts each of the ISA Study elements into OWL
     *
     * @param study
     */
    private void convertStudy(Study study){
        log.info("Converting study " + study.getStudyId() + "...");

        //Study
        OWLNamedIndividual studyIndividual = ISA2OWL.createIndividual(ExtendedISASyntax.STUDY, study.getStudyId());

        //Study identifier
        ISA2OWL.createIndividual(Study.STUDY_ID, study.getStudyId());

        //Study title
        OWLNamedIndividual studyTitleIndividual = ISA2OWL.createIndividual(Study.STUDY_TITLE, study.getStudyId()+ISA2OWL.TITLE_SUFFIX, study.getStudyTitle());
        if (studyTitleIndividual!=null){
            OWLDataProperty hasMeasurementValue = ISA2OWL.factory.getOWLDataProperty(ExtendedOBIVocabulary.HAS_VALUE.iri);
            OWLLiteral titleLiteral = ISA2OWL.factory.getOWLLiteral(study.getStudyTitle(), OWL2Datatype.XSD_STRING);
            OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom = ISA2OWL.factory.getOWLDataPropertyAssertionAxiom(hasMeasurementValue, studyTitleIndividual, titleLiteral);
            ISA2OWL.manager.addAxiom(ISA2OWL.ontology, dataPropertyAssertionAxiom);
        }

        //Study description
        OWLNamedIndividual studyDescriptionIndividual = ISA2OWL.createIndividual(Study.STUDY_DESC, study.getStudyId()+ISA2OWL.DESCRIPTION_SUFFIX, study.getStudyDesc());
        if (studyDescriptionIndividual!=null){
            OWLDataProperty hasMeasurementValue = ISA2OWL.factory.getOWLDataProperty(ExtendedOBIVocabulary.HAS_VALUE.iri);
            OWLLiteral descriptionLiteral = ISA2OWL.factory.getOWLLiteral(study.getStudyDesc(), OWL2Datatype.XSD_STRING);
            OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom = ISA2OWL.factory.getOWLDataPropertyAssertionAxiom(hasMeasurementValue, studyDescriptionIndividual, descriptionLiteral);
            ISA2OWL.manager.addAxiom(ISA2OWL.ontology, dataPropertyAssertionAxiom);
        }

        //Study File
        ISA2OWL.createIndividual(ExtendedISASyntax.STUDY_FILE, study.getStudySampleFileIdentifier());

        //Study file name
        ISA2OWL.createIndividual(Study.STUDY_FILE_NAME, study.getStudySampleFileIdentifier());

        //Study submission date
        ISA2OWL.createIndividual(Study.STUDY_DATE_OF_SUBMISSION, study.getDateOfSubmission());

        OWLNamedIndividual publicReleaseDateIndividual = ISA2OWL.createIndividual(Study.STUDY_DATE_OF_PUBLIC_RELEASE, study.getStudyId()+ISA2OWL.STUDY_PUBLIC_RELEASE_DATE_SUFFIX);

        if (publicReleaseDateIndividual!=null){
            OWLDataProperty hasMeasurementValue = ISA2OWL.factory.getOWLDataProperty(ExtendedOBIVocabulary.HAS_VALUE.iri);
            OWLLiteral publicReleaseDateLiteral = ISA2OWL.factory.getOWLLiteral(study.getPublicReleaseDate(), OWL2Datatype.XSD_STRING);
            OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom = ISA2OWL.factory.getOWLDataPropertyAssertionAxiom(hasMeasurementValue, publicReleaseDateIndividual, publicReleaseDateLiteral);
            ISA2OWL.manager.addAxiom(ISA2OWL.ontology, dataPropertyAssertionAxiom);
        }

        //Publications
        List<Publication> publicationList = study.getPublications();
        convertPublications(publicationList, studyIndividual);

        //Study design
        List<StudyDesign> studyDesignList = study.getStudyDesigns();
        OWLNamedIndividual studyDesignIndividual = studyDesignIndividual = convertStudyDesign(studyIndividual, study, studyDesignList);

        //Study Person
        List<Contact> contactList = study.getContacts();
        convertContacts(contactList,studyIndividual);

        //Study Factor
        List<Factor> factorList = study.getFactors();
        convertFactors(factorList);

        //Study Protocol
        List<Protocol> protocolList = study.getProtocols();
        convertProtocols(protocolList);

        Assay2OWLConverter assay2OWLConverter = new Assay2OWLConverter();
        sampleIndividualMap = assay2OWLConverter.convert(study.getStudySample(), Assay2OWLConverter.AssayTableType.STUDY, null,
                protocolList, protocolIndividualMap,studyDesignIndividual, studyIndividual, true, null);

        log.debug("ASSAYS..." + study.getAssays());

        //Study Assays
        Map<String, Assay> assayMap = study.getAssays();
        convertAssays(assayMap, protocolList, studyIndividual);

        //dealing with all property mappings
        Map<String, List<Pair<IRI, String>>> propertyMappings = ISA2OWL.mapping.getPropertyMappings();
        for(String subjectString: propertyMappings.keySet()){

            //skip Study Person properties as they are dealt with in the Contact mappings
            if (subjectString.startsWith(ExtendedISASyntax.STUDY_PERSON) ||  subjectString.startsWith(ExtendedISASyntax.INVESTIGATION_PERSON) ||
                    subjectString.startsWith(ExtendedISASyntax.STUDY_PROTOCOL) ||
                    subjectString.startsWith(GeneralFieldTypes.PROTOCOL_REF.toString()) ||
                    subjectString.matches(MaterialNode.REGEXP) ||
                    subjectString.startsWith(ExtendedISASyntax.STUDY_ASSAY) ||
                    subjectString.startsWith(ExtendedISASyntax.INVESTIGATION_PUBLICATION) ||
                    subjectString.startsWith(ExtendedISASyntax.STUDY_PUBLICATION) ||
                    subjectString.startsWith(ExtendedISASyntax.PUBLICATION) ||
                    subjectString.startsWith(InvestigationPublication.PUBMED_ID))
                continue;

            List<Pair<IRI, String>> predicateObjects = propertyMappings.get(subjectString);
            Set<OWLNamedIndividual> subjects = ISA2OWL.typeIndividualMap.get(subjectString);

            if (subjects==null)
                continue;

            for(OWLIndividual subject: subjects){

                for(Pair<IRI,String> predicateObject: predicateObjects){

                    IRI predicate = predicateObject.getFirst();

                    OWLObjectProperty property = ISA2OWL.factory.getOWLObjectProperty(predicate);

                    String objectString = predicateObject.getSecond();


                    Set<OWLNamedIndividual> objects = ISA2OWL.typeIndividualMap.get(objectString);

                    if (objects==null)
                        continue;

                    for(OWLNamedIndividual object: objects){

                        if (subject==null || object==null || property==null){
                            log.debug("At least one is null...");
                        }else{
                            OWLObjectPropertyAssertionAxiom axiom = ISA2OWL.factory.getOWLObjectPropertyAssertionAxiom(property, subject, object);
                            ISA2OWL.manager.addAxiom(ISA2OWL.ontology, axiom);
                        }
                    }//for
                } //for
            } //for
        }

        log.info("... end of conversion for Study " + study.getStudyId() + ".");

    }


    /**
     *
     * @param publicationList
     * @param individual the study or investigation individual
     */
    private void convertPublications(List<Publication> publicationList, OWLNamedIndividual individual){

        Map<String,List<Pair<IRI, String>>> publicationMappings = ISA2OWL.mapping.getPublicationPropertyMappings();
        Map<String, OWLNamedIndividual> publicationIndividuals = null;

        for(Publication pub: publicationList){

            boolean investigation = (pub instanceof InvestigationPublication);

            OWLNamedIndividual publicationIndividual = publicationIndividualMap.get(pub);

            publicationIndividuals = new HashMap<String, OWLNamedIndividual>();

            if (publicationIndividual==null) {

                String pubmedID = pub.getPubmedId();
                OWLNamedIndividual pubInd = ISA2OWL.createIndividual(ExtendedISASyntax.PUBLICATION, pubmedID, pubmedID, ExternalRDFLinkages.getPubMedIRI(pubmedID), publicationIndividuals);
                publicationIndividualMap.put(pub,pubInd);

                //Study PubMed ID
                ISA2OWL.createIndividual(investigation ? InvestigationPublication.PUBMED_ID : StudyPublication.PUBMED_ID, pub.getPubmedId(), publicationIndividuals);

                //Study Publication DOI
                ISA2OWL.createIndividual(investigation ? InvestigationPublication.PUBLICATION_DOI: StudyPublication.PUBLICATION_DOI, pub.getPublicationDOI(), publicationIndividuals);

                //Study Publication Author List
                ISA2OWL.createIndividual(investigation ? InvestigationPublication.PUBLICATION_AUTHOR_LIST: StudyPublication.PUBLICATION_AUTHOR_LIST, pub.getPublicationAuthorList(), publicationIndividuals);

                 //Study Publication Title
                ISA2OWL.createIndividual(investigation ? InvestigationPublication.PUBLICATION_TITLE: StudyPublication.PUBLICATION_TITLE, pub.getPublicationTitle(), publicationIndividuals);
            } else {
                publicationIndividuals.put(ExtendedISASyntax.PUBLICATION, publicationIndividual);
            }

            if (investigation)
                publicationIndividuals.put(ExtendedISASyntax.INVESTIGATION, individual);
            else
                publicationIndividuals.put(ExtendedISASyntax.STUDY, individual);

            ISA2OWL.convertProperties(publicationMappings, publicationIndividuals);

        }

    }

    /**
     * Converts contact information into OWL
     *
     * @param contactsList
     * @param individual this is either an Investigation individual or a Study individual
     */
    private void convertContacts(List<Contact> contactsList, OWLNamedIndividual individual){

        //process properties for the contactIndividuals
        Map<String,List<Pair<IRI, String>>> contactMappings = ISA2OWL.mapping.getContactMappings();

        Map<String, OWLNamedIndividual> contactIndividuals = null;

        for(Contact contact: contactsList){

            OWLNamedIndividual contactIndividual = contactIndividualMap.get(contact);
            boolean investigation = contact instanceof InvestigationContact;
            contactIndividuals = new HashMap<String, OWLNamedIndividual>();

            if (contactIndividual==null){

                //Study Person
                contactIndividual = ISA2OWL.createIndividual(investigation ? ExtendedISASyntax.INVESTIGATION_PERSON : ExtendedISASyntax.STUDY_PERSON, contact.getIdentifier(), contactIndividuals);
                contactIndividualMap.put(contact, contactIndividual);

                //Study Person Last Name
                ISA2OWL.createIndividual(investigation ? InvestigationContact.CONTACT_LAST_NAME: StudyContact.CONTACT_LAST_NAME, contact.getLastName(), contactIndividuals);

                //Study Person First Name
                ISA2OWL.createIndividual(investigation ? InvestigationContact.CONTACT_FIRST_NAME: StudyContact.CONTACT_FIRST_NAME, contact.getFirstName(), contactIndividuals);

                //Study Person Mid Initials
                ISA2OWL.createIndividual(investigation ? InvestigationContact.CONTACT_MID_INITIAL : StudyContact.CONTACT_MID_INITIAL, contact.getMidInitial(), contactIndividuals);

                //Study Person Email
                ISA2OWL.createIndividual(investigation ? InvestigationContact.CONTACT_EMAIL :StudyContact.CONTACT_EMAIL, contact.getEmail(), contactIndividuals);

                //Study Person Phone
                ISA2OWL.createIndividual(investigation ? InvestigationContact.CONTACT_PHONE :StudyContact.CONTACT_PHONE, contact.getPhone(), contactIndividuals);

                //Study Person Fax
                ISA2OWL.createIndividual(investigation ? InvestigationContact.CONTACT_FAX :StudyContact.CONTACT_FAX, contact.getFax(), contactIndividuals);

                //Study Person Address
                ISA2OWL.createIndividual(investigation ? InvestigationContact.CONTACT_ADDRESS :StudyContact.CONTACT_ADDRESS, contact.getAddress(), contactIndividuals);

                //Study Person Affiliation
                String affiliation = contact.getAffiliation();
                OWLNamedIndividual affiliationIndividual = affiliationIndividualMap.get(affiliation);
                if (affiliationIndividual!=null) {
                   contactIndividuals.put(affiliation, affiliationIndividual);
                }else {
                   affiliationIndividual = ISA2OWL.createIndividual(investigation ? InvestigationContact.CONTACT_AFFILIATION :StudyContact.CONTACT_AFFILIATION, contact.getAffiliation(), contactIndividuals);
                }
                affiliationIndividualMap.put(affiliation, affiliationIndividual);

                //Investigation/Study Person Roles
                ISA2OWL.createIndividual(investigation ? InvestigationContact.CONTACT_ROLE :StudyContact.CONTACT_ROLE, contact.getRole(), contactIndividuals);
            } else {
                contactIndividuals.put(investigation ? ExtendedISASyntax.INVESTIGATION_PERSON : ExtendedISASyntax.STUDY_PERSON, contactIndividual);
            }


            if (investigation)
                contactIndividuals.put(ExtendedISASyntax.INVESTIGATION, individual);
            else
                contactIndividuals.put(ExtendedISASyntax.STUDY, individual);

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

            log.debug("FACTOR NAME ="+factor.getFactorName());
            log.debug("FACTOR TYPE ="+factor.getFactorType());
            log.debug("FACTOR TYPE ACCESSION NUMBER="+factor.getFactorTypeTermAccession());
            log.debug("FACTOR TYPE TERM SOURCE"+factor.getFactorTypeTermSource());

            //use term source and term accession to declare a more specific type for the factor
            if (factor.getFactorTypeTermAccession()!=null && !factor.getFactorTypeTermAccession().equals("")
                    && factor.getFactorTypeTermSource()!=null && !factor.getFactorTypeTermSource().equals("")){

                ISA2OWL.findOntologyTermAndAddClassAssertion(factor.getFactorTypeTermSource(), factor.getFactorTypeTermAccession(), factorIndividual);

            }//factors attributes not null
        }

    }

    /**
     *
     * Method to convert the study design
     *
     * @param studyIndividual a named individual corresponding to the study
     * @param study the Study from the ISA model
     * @param studyDesigns a list of StudyDesign objects
     * @return a named individual corresponding to the study design
     */
    private OWLNamedIndividual convertStudyDesign(OWLNamedIndividual studyIndividual, Study study, List<StudyDesign> studyDesigns){

        //Study Design Type
        //define a StudyDesignExecution per StudyDesign and associate with study (Study has_part StudyDesignExecution
        OWLNamedIndividual studyDesignIndividual = ISA2OWL.createIndividual(StudyDesign.STUDY_DESIGN_TYPE, study.getStudyId()+ISA2OWL.STUDY_DESIGN_SUFFIX);

        for(StudyDesign studyDesign: studyDesigns){

            ISA2OWL.addComment(studyDesign.getStudyDesignType(), studyDesignIndividual.getIRI());

            //use term source and term accession to declare a more specific type for the factor
            if (studyDesign.getStudyDesignTypeTermAcc()!=null && !studyDesign.getStudyDesignTypeTermAcc().equals("")
                && studyDesign.getStudyDesignTypeTermSourceRef()!=null && !studyDesign.getStudyDesignTypeTermSourceRef().equals("")){

                ISA2OWL.findOntologyTermAndAddClassAssertion(studyDesign.getStudyDesignTypeTermSourceRef(), studyDesign.getStudyDesignTypeTermAcc(), studyDesignIndividual);
            }
        }

        OWLNamedIndividual studyDesignExecutionIndividual = ISA2OWL.createIndividual(ExtendedISASyntax.STUDY_DESIGN_EXECUTION, study.getStudyId()+ISA2OWL.STUDY_DESIGN_EXECUTION_SUFFIX);

        return studyDesignIndividual;

    }


    /**
     *
     * Converts the protocols.
     *
     * @param protocolList a list with Protocol objects
     */
    private void convertProtocols(List<Protocol> protocolList){

        Map<String, OWLNamedIndividual> protocolIndividuals = null;
        Map<String,List<Pair<IRI, String>>> protocolMappings = ISA2OWL.mapping.getProtocolMappings();
        OWLNamedIndividual individual = null;

        for(Protocol protocol: protocolList){
            protocolIndividuals = new HashMap<String, OWLNamedIndividual>();

            //Study Protocol
            individual = ISA2OWL.createIndividual(ExtendedISASyntax.STUDY_PROTOCOL, protocol.getProtocolName()+ISA2OWL.STUDY_PROTOCOL_SUFFIX, protocolIndividuals);
            protocolIndividualMap.put(protocol.getProtocolName(),individual);
            ISA2OWL.addComment(protocol.getProtocolType(), individual.getIRI());

            //Study Protocol Name
            ISA2OWL.createIndividual(Protocol.PROTOCOL_NAME, protocol.getProtocolName()+ISA2OWL.STUDY_PROTOCOL_NAME_SUFFIX, protocolIndividuals);

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
     * Converts the assays.
     *
     * @param assayMap
     */
    private void convertAssays(Map<String, Assay> assayMap, List<Protocol> protocolList, OWLNamedIndividual studyIndividual){

        Map<String, OWLNamedIndividual> assayIndividualsForProperties;

        for(String assayRef: assayMap.keySet()){
            log.debug("AssayRef="+assayRef);
            Assay assay = assayMap.get(assayRef);

            assayIndividualsForProperties = new HashMap<String, OWLNamedIndividual>();
            assayIndividualsForProperties.put(ExtendedISASyntax.STUDY, studyIndividual);

            //Study Assay
            //OWLNamedIndividual studyAssayIndividual = ISA2OWL.createIndividual(ExtendedISASyntax.STUDY_ASSAY, assay.getIdentifier());

            OWLNamedIndividual measurementIndividual = measurementTechnologyIndividuals.get(assay.getMeasurementEndpoint());

            if (measurementIndividual==null){

                //Study Assay Measurement Type
                measurementIndividual = ISA2OWL.createIndividual(Assay.MEASUREMENT_ENDPOINT, assay.getMeasurementEndpoint(), assayIndividualsForProperties);
                measurementTechnologyIndividuals.put(assay.getMeasurementEndpoint(),measurementIndividual);
                ISA2OWL.findOntologyTermAndAddClassAssertion(assay.getMeasurementEndpointTermSourceRef(),
                                                            assay.getMeasurementEndpointTermAccession(),
                                                            measurementIndividual);

            } else {
                assayIndividualsForProperties.put(Assay.MEASUREMENT_ENDPOINT, measurementIndividual);
            }

            OWLNamedIndividual technologyIndividual = measurementTechnologyIndividuals.get(assay.getTechnologyType());

            if (technologyIndividual==null){
                 //Study Assay Technology Type
                technologyIndividual = ISA2OWL.createIndividual(Assay.TECHNOLOGY_TYPE, assay.getTechnologyType(), assayIndividualsForProperties);
                measurementTechnologyIndividuals.put(assay.getTechnologyType(),technologyIndividual);
                ISA2OWL.findOntologyTermAndAddClassAssertion(assay.getTechnologyTypeTermSourceRef(),
                                                             assay.getTechnologyTypeTermAccession(),
                                                             technologyIndividual);
            } else {
                assayIndividualsForProperties.put(Assay.TECHNOLOGY_TYPE, technologyIndividual);
            }

            //Study Assay File
            OWLNamedIndividual studyAssayFile = ISA2OWL.createIndividual(ExtendedISASyntax.STUDY_ASSAY_FILE, assay.getAssayReference(), assayIndividualsForProperties);

            //Study Assay File Name
            ISA2OWL.createIndividual(Assay.ASSAY_REFERENCE, assay.getAssayReference(), assayIndividualsForProperties);

            Assay2OWLConverter assayConverter = new Assay2OWLConverter();
            assayConverter.convert(assay, Assay2OWLConverter.AssayTableType.ASSAY, sampleIndividualMap,
                    protocolList, protocolIndividualMap, null, studyIndividual, false,
                    assayIndividualsForProperties);
        }

    }


}
