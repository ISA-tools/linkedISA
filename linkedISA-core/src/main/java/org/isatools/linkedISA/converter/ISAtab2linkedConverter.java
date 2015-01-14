package org.isatools.linkedISA.converter;

import org.apache.log4j.Logger;
import org.isatools.graph.model.impl.MaterialNode;
import org.isatools.linkedISA.mapping.ISASyntax2LinkedMapping;
import org.isatools.isacreator.io.importisa.ISAtabFilesImporter;
import org.isatools.isacreator.io.importisa.ISAtabImporter;
import org.isatools.isacreator.model.*;
import org.isatools.isacreator.ontologymanager.OntologyManager;
import org.isatools.isacreator.ontologymanager.OntologySourceRefObject;
import org.isatools.owl.DCAT;
import org.isatools.owl.ISA;
import org.isatools.owl.OBI;
import org.isatools.owl.OWLUtil;
import org.isatools.owl.reasoner.ReasonerService;
import org.isatools.syntax.ExtendedISASyntax;
import org.isatools.util.Pair;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import java.io.File;
import java.util.*;

/**
 * It converts an ISAtab dataset into RDF based on a given ISA2OWL mapping
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 *
 */
public class ISAtab2LinkedConverter {

    private static final Logger log = Logger.getLogger(ISAtab2LinkedConverter.class);

    private ISAtabImporter importer = null;
    private String configDir = null;

    private Map<Publication, OWLNamedIndividual> publicationIndividualMap = null;
    private Map<Contact, OWLNamedIndividual> contactIndividualMap = null;
    private Map<String, OWLNamedIndividual> protocolIndividualMap = null;
    private Map<String, OWLNamedIndividual> sampleIndividualMap = null;
    private Map<String, OWLNamedIndividual> measurementTechnologyIndividuals = new HashMap<String, OWLNamedIndividual>();
    private Map<String, OWLNamedIndividual> affiliationIndividualMap = null;
    private Map<String, OWLNamedIndividual> factorIndividualMap = null;

    private String COMMENT_EXPERIMENTAL_METADATA_LICENCE = "Comment[Experimental Metadata Licence]";
    private String COMMENT_STUDY_GRANT_NUMBER = "Comment[Study Grant Number]";
    private String COMMENT_STUDY_FUNDING_AGENCY = "Comment[Study Funding Agency]";


    /**
     * Constructor
     *
     * @param cDir directory where the ISA configuration file can be found
     */
    public ISAtab2LinkedConverter(String cDir, ISASyntax2LinkedMapping m){
        configDir = cDir;
        log.debug("configDir="+configDir);
        LinkedISA.mapping = m;
        importer = new ISAtabFilesImporter(configDir);
    }


    /**
     * Retrieves all the IRIs minted when creating the ISA-OWL representation
     *
     * @return
     */
    public Map<String, OWLNamedIndividual> getMintedIRIs(){

        for(String id: LinkedISA.idIndividualMap.keySet()){
            System.out.println( id + "\t" + LinkedISA.idIndividualMap.get(id));
        }
       return null;
    }

    /**
     *
     * Method to convert the ISA-TAB data set into RDF/OWL.
     *
     * @param parentDir
     */
    public boolean convert(String parentDir, String iri){
        log.info("Converting ISA-TAB dataset " + parentDir + " into RDF/OWL");

        LinkedISA.setIRI(iri);

        try{
            LinkedISA.ontology = LinkedISA.manager.createOntology(LinkedISA.ontoIRI);

            OWLImportsDeclaration importDecl = LinkedISA.factory.getOWLImportsDeclaration(IRI.create(OBI.IRI));
            LinkedISA.manager.applyChange(new AddImport(LinkedISA.ontology, importDecl));

            OWLImportsDeclaration isaImportDecl = LinkedISA.factory.getOWLImportsDeclaration(IRI.create(ISA.IRI));
            LinkedISA.manager.applyChange(new AddImport(LinkedISA.ontology, isaImportDecl));

        }catch(OWLOntologyCreationException e){
            e.printStackTrace();
        }

        if (!readInISAFiles(parentDir)){
            log.debug(importer.getMessagesAsString());
        }

        //initialise the map of individuals
        LinkedISA.typeIndividualMap = new HashMap<String, Set<OWLNamedIndividual>>();
        publicationIndividualMap = new HashMap<Publication, OWLNamedIndividual>();
        contactIndividualMap = new HashMap<Contact, OWLNamedIndividual>();
        protocolIndividualMap = new HashMap<String, OWLNamedIndividual>();
        affiliationIndividualMap = new HashMap<String, OWLNamedIndividual>();

        Investigation investigation = importer.getInvestigation();

        OWLNamedIndividual isatabDistributionIndividual = null;
        OWLNamedIndividual isaowlDistributionIndividual = null;
        OWLNamedIndividual investigationFileIndividual = null;

        if (investigation==null)
            return false;

        if (investigation.getInvestigationId()!=null && !investigation.getInvestigationId().equals("")){

            //create 'ISA dataset' individual
            OWLNamedIndividual isaDatasetIndividual = LinkedISA.createIndividual(IRI.create(ISA.ISA_DATASET), investigation.getInvestigationId());
            //create 'ISA dataset' individual
            isatabDistributionIndividual = LinkedISA.createIndividual(IRI.create(ISA.ISATAB_DISTRIBUTION), investigation.getInvestigationId());
            LinkedISA.createObjectPropertyAssertion(DCAT.DISTRIBUTION_PROPERTY, isaDatasetIndividual, isatabDistributionIndividual);

            isaowlDistributionIndividual = LinkedISA.createIndividual(IRI.create(ISA.ISAOWL_DISTRIBUTION), investigation.getInvestigationId());
            LinkedISA.createObjectPropertyAssertion(DCAT.DISTRIBUTION_PROPERTY, isaDatasetIndividual, isaowlDistributionIndividual);

            //ISAtab_distribution has_part investigation_file
            investigationFileIndividual = LinkedISA.createIndividual(IRI.create(ISA.INVESTIGATION_FILE), "i_investigation.txt investigation file");
            LinkedISA.createObjectPropertyAssertion(ISA.HAS_PART, isatabDistributionIndividual, investigationFileIndividual);
        }

        convertInvestigation(investigation, isatabDistributionIndividual, investigationFileIndividual);

        Map<String,Study> studies = investigation.getStudies();

        log.debug("number of studies=" + studies.keySet().size());


        //convert each study
        for(String key: studies.keySet()){

            Study study = studies.get(key);


            if (isatabDistributionIndividual == null){
                //create 'ISA dataset' individual
                OWLNamedIndividual isaDatasetIndividual = LinkedISA.createIndividual(IRI.create(ISA.ISA_DATASET), study.getStudySampleFileIdentifier());
                //create 'ISA dataset' individual
                isatabDistributionIndividual = LinkedISA.createIndividual(IRI.create(ISA.ISATAB_DISTRIBUTION), study.getStudySampleFileIdentifier());
                LinkedISA.createObjectPropertyAssertion(DCAT.DISTRIBUTION_PROPERTY, isaDatasetIndividual, isatabDistributionIndividual);

                isaowlDistributionIndividual = LinkedISA.createIndividual(IRI.create(ISA.ISAOWL_DISTRIBUTION), study.getStudySampleFileIdentifier());
                LinkedISA.createObjectPropertyAssertion(DCAT.DISTRIBUTION_PROPERTY, isaDatasetIndividual, isaowlDistributionIndividual);

                //ISAtab_distribution has_part investigation_file
                investigationFileIndividual = LinkedISA.createIndividual(IRI.create(ISA.INVESTIGATION_FILE), "i_investigation.txt");
                LinkedISA.createObjectPropertyAssertion(ISA.HAS_PART, isatabDistributionIndividual, investigationFileIndividual);
            }


            if (isaowlDistributionIndividual!=null){

                //add comment about generation with ISA2OL
                LinkedISA.addComment("Created with ISA2OWL converter", isaowlDistributionIndividual.getIRI());


                //add comments about mappings used
                Set<String> mappingFiles = LinkedISA.mapping.getMappingFiles();
                for(String mappingFile: mappingFiles){
                    LinkedISA.addComment("Using mapping file " + mappingFile, isaowlDistributionIndividual.getIRI());
                }

            }

            convertStudy(study, investigationFileIndividual, isatabDistributionIndividual);

            //remove from the map of type/individuals, anything that is not related to the Investigation
            HashMap newMap = new HashMap<String, Set<OWLNamedIndividual>>();
            Set<String> keys = LinkedISA.typeIndividualMap.keySet();
            for(String mapkey: keys){
                if (mapkey.startsWith(ExtendedISASyntax.INVESTIGATION))
                    newMap.put(mapkey, LinkedISA.typeIndividualMap.get(mapkey));
            }
            LinkedISA.typeIndividualMap = newMap;
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
        Set<OntologySourceRefObject> sourceRefObjects = OntologyManager.getOntologiesUsed();


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


            OWLImportsDeclaration importDecl = LinkedISA.factory.getOWLImportsDeclaration(IRI.create(sourceFile));
            LinkedISA.manager.applyChange(new AddImport(LinkedISA.ontology, importDecl));

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
        OWLUtil.saveRDFXML(LinkedISA.ontology, IRI.create(file.toURI()));
        //OWLUtil.systemOutputMOWLSyntax(ISA2OWL.ontology);
    }

    public void saveInferredOntology(String filename) throws Exception{
        ReasonerService reasoner = new ReasonerService(LinkedISA.manager, LinkedISA.ontology);
        reasoner.saveInferredOntology(filename);
    }

    /***
     * Converts the invetigation file.
     *
     * @param investigation
     */
    private void convertInvestigation(Investigation investigation, OWLNamedIndividual isatabDistributionIndividual, OWLNamedIndividual investigationFileIndividual){

        //Investigation
        OWLNamedIndividual investigationIndividual = null;

        if (investigation==null)
            return;

        if (!investigation.getInvestigationId().equals("")){

            //Investigation
            investigationIndividual = LinkedISA.createIndividual(ExtendedISASyntax.INVESTIGATION, investigation.getInvestigationId());
            LinkedISA.createObjectPropertyAssertion(ISA.HAS_PART, isatabDistributionIndividual, investigationFileIndividual);

            //investigationFile describes investigation
            LinkedISA.createObjectPropertyAssertion(ISA.DESCRIBES, investigationFileIndividual, investigationIndividual);
        }

        //Investigation identifier
        LinkedISA.createIndividual(Investigation.INVESTIGATION_ID_KEY, investigation.getInvestigationId());

        //Investigation title
        OWLNamedIndividual invTitleIndividual = LinkedISA.createIndividual(Investigation.INVESTIGATION_TITLE_KEY, investigation.getInvestigationId() + LinkedISA.TITLE_SUFFIX, investigation.getInvestigationTitle());
        if (invTitleIndividual!=null){
            OWLDataProperty hasMeasurementValue = LinkedISA.factory.getOWLDataProperty(IRI.create(ISA.HAS_VALUE));
            OWLLiteral titleLiteral = LinkedISA.factory.getOWLLiteral(investigation.getInvestigationTitle(), OWL2Datatype.XSD_STRING);
            OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom = LinkedISA.factory.getOWLDataPropertyAssertionAxiom(hasMeasurementValue, invTitleIndividual, titleLiteral);
            LinkedISA.manager.addAxiom(LinkedISA.ontology, dataPropertyAssertionAxiom);
        }

        //Investigation description
        OWLNamedIndividual investigationDescriptionIndividual = LinkedISA.createIndividual(Investigation.INVESTIGATION_DESCRIPTION_KEY, investigation.getInvestigationId() + LinkedISA.DESCRIPTION_SUFFIX, investigation.getInvestigationDescription());
        if (investigationDescriptionIndividual!=null){
            OWLDataProperty hasMeasurementValue = LinkedISA.factory.getOWLDataProperty(IRI.create(ISA.HAS_VALUE));
            OWLLiteral descriptionLiteral = LinkedISA.factory.getOWLLiteral(investigation.getInvestigationDescription(), OWL2Datatype.XSD_STRING);
            OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom = LinkedISA.factory.getOWLDataPropertyAssertionAxiom(hasMeasurementValue, investigationDescriptionIndividual, descriptionLiteral);
            LinkedISA.manager.addAxiom(LinkedISA.ontology, dataPropertyAssertionAxiom);
        }


        LinkedISA.createIndividual(Investigation.INVESTIGATION_SUBMISSION_DATE_KEY, investigation.getSubmissionDate());

        OWLNamedIndividual publicReleaseDateIndividual = LinkedISA.createIndividual(Investigation.INVESTIGATION_PUBLIC_RELEASE_KEY, investigation.getInvestigationId() + LinkedISA.STUDY_PUBLIC_RELEASE_DATE_SUFFIX);

        if (publicReleaseDateIndividual!=null){
            OWLDataProperty hasMeasurementValue = LinkedISA.factory.getOWLDataProperty(IRI.create(ISA.HAS_VALUE));
            OWLLiteral publicReleaseDateLiteral = LinkedISA.factory.getOWLLiteral(investigation.getPublicReleaseDate(), OWL2Datatype.XSD_STRING);
            OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom = LinkedISA.factory.getOWLDataPropertyAssertionAxiom(hasMeasurementValue, publicReleaseDateIndividual, publicReleaseDateLiteral);
            LinkedISA.manager.addAxiom(LinkedISA.ontology, dataPropertyAssertionAxiom);
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
    private void convertStudy(Study study, OWLNamedIndividual investigationFileIndividual, OWLNamedIndividual isatabDistributionIndividual){
        log.info("Converting study " + study.getStudyId() + "...");

        //Study
        OWLNamedIndividual studyIndividual = null;

        if (study.getStudyId().startsWith("10.1038")){
            studyIndividual = LinkedISA.createIndividual(ExtendedISASyntax.STUDY, study.getStudyId(), "", IRI.create("http://dx.doi.org/"+study.getStudyId()), null);

        } else {
            studyIndividual = LinkedISA.createIndividual(ExtendedISASyntax.STUDY, study.getStudyId());
        }

        String license = study.getComment("Comment[Experimental Metadata Licence]");
        String study_grant = study.getComment("Comment[Study Grant Number]");
        String funding_agency = study.getComment("Comment[Study Funding Agency]");

        if (!license.equals(""))
            LinkedISA.addComment("Experimental Metadata Licence: " + license, studyIndividual.getIRI());

        if (!study_grant.equals(""))
            LinkedISA.addComment("Study Grant Number: " + study_grant, studyIndividual.getIRI());

        if (!funding_agency.equals(""))
            LinkedISA.addComment("Study Funding Agency: " + funding_agency, studyIndividual.getIRI());


        //Study identifier
        LinkedISA.createIndividual(Study.STUDY_ID, study.getStudyId());

        //Study title
        OWLNamedIndividual studyTitleIndividual = LinkedISA.createIndividual(Study.STUDY_TITLE, study.getStudyId() + LinkedISA.TITLE_SUFFIX, study.getStudyTitle());
        if (studyTitleIndividual!=null){
            OWLDataProperty hasMeasurementValue = LinkedISA.factory.getOWLDataProperty(IRI.create(ISA.HAS_VALUE));
            OWLLiteral titleLiteral = LinkedISA.factory.getOWLLiteral(study.getStudyTitle(), OWL2Datatype.XSD_STRING);
            OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom = LinkedISA.factory.getOWLDataPropertyAssertionAxiom(hasMeasurementValue, studyTitleIndividual, titleLiteral);
            LinkedISA.manager.addAxiom(LinkedISA.ontology, dataPropertyAssertionAxiom);
        }

        //Study description
        OWLNamedIndividual studyDescriptionIndividual = LinkedISA.createIndividual(Study.STUDY_DESC, study.getStudyId() + LinkedISA.DESCRIPTION_SUFFIX, study.getStudyDesc());
        if (studyDescriptionIndividual!=null){
            OWLDataProperty hasMeasurementValue = LinkedISA.factory.getOWLDataProperty(IRI.create(ISA.HAS_VALUE));
            OWLLiteral descriptionLiteral = LinkedISA.factory.getOWLLiteral(study.getStudyDesc(), OWL2Datatype.XSD_STRING);
            OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom = LinkedISA.factory.getOWLDataPropertyAssertionAxiom(hasMeasurementValue, studyDescriptionIndividual, descriptionLiteral);
            LinkedISA.manager.addAxiom(LinkedISA.ontology, dataPropertyAssertionAxiom);
        }

        //Study File
        OWLNamedIndividual studyFileIndividual = LinkedISA.createIndividual(ExtendedISASyntax.STUDY_FILE, study.getStudySampleFileIdentifier());
        if (investigationFileIndividual!=null) {
            LinkedISA.createObjectPropertyAssertion(ISA.HAS_PART, isatabDistributionIndividual, studyFileIndividual);
            LinkedISA.createObjectPropertyAssertion(ISA.POINTS_TO, investigationFileIndividual, studyFileIndividual);
        }

        //Study file name
        LinkedISA.createIndividual(Study.STUDY_FILE_NAME, study.getStudySampleFileIdentifier());

        //Study submission date
        LinkedISA.createIndividual(Study.STUDY_DATE_OF_SUBMISSION, study.getDateOfSubmission());

        OWLNamedIndividual publicReleaseDateIndividual = LinkedISA.createIndividual(Study.STUDY_DATE_OF_PUBLIC_RELEASE, study.getStudyId() + LinkedISA.STUDY_PUBLIC_RELEASE_DATE_SUFFIX);

        if (publicReleaseDateIndividual!=null){
            OWLDataProperty hasMeasurementValue = LinkedISA.factory.getOWLDataProperty(IRI.create(ISA.HAS_VALUE));
            OWLLiteral publicReleaseDateLiteral = LinkedISA.factory.getOWLLiteral(study.getPublicReleaseDate(), OWL2Datatype.XSD_STRING);
            OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom = LinkedISA.factory.getOWLDataPropertyAssertionAxiom(hasMeasurementValue, publicReleaseDateIndividual, publicReleaseDateLiteral);
            LinkedISA.manager.addAxiom(LinkedISA.ontology, dataPropertyAssertionAxiom);
        }

        //Publications
        List<Publication> publicationList = study.getPublications();
        convertPublications(publicationList, studyIndividual);

        //Study design
        List<StudyDesign> studyDesignList = study.getStudyDesigns();
        OWLNamedIndividual studyDesignIndividual = convertStudyDesign(studyIndividual, study, studyDesignList);

        //Study Person
        List<Contact> contactList = study.getContacts();
        convertContacts(contactList,studyIndividual);

        //Study Factor
        List<Factor> factorList = study.getFactors();
        convertFactors(factorList, studyDesignIndividual);

        //Study Protocol
        List<Protocol> protocolList = study.getProtocols();
        convertProtocols(protocolList);

        Assay2LinkedConverter assay2OWLConverter = new Assay2LinkedConverter();
        sampleIndividualMap = assay2OWLConverter.convert(study.getStudySample(), Assay2LinkedConverter.AssayTableType.STUDY, null,
                protocolList, protocolIndividualMap,studyDesignIndividual, studyIndividual, true, null, null, factorIndividualMap);

        log.debug("ASSAYS..." + study.getAssays());

        //Study Assays
        Map<String, Assay> assayMap = study.getAssays();
        convertAssays(assayMap, protocolList, studyIndividual, studyDesignIndividual, studyFileIndividual, isatabDistributionIndividual, investigationFileIndividual);

        //dealing with all property mappings, except those already treated in specific methods
        Map<String, List<Pair<IRI, String>>> propertyMappings = LinkedISA.mapping.getOtherPropertyMappings();
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
                    subjectString.startsWith(InvestigationPublication.PUBMED_ID) ||
                    subjectString.startsWith(GeneralFieldTypes.PARAMETER_VALUE.name))
                continue;

            List<Pair<IRI, String>> predicateObjects = propertyMappings.get(subjectString);
            Set<OWLNamedIndividual> subjects = LinkedISA.typeIndividualMap.get(subjectString);

            if (subjects==null)
                continue;

            for(OWLIndividual subject: subjects){

                for(Pair<IRI,String> predicateObject: predicateObjects){

                    IRI predicate = predicateObject.getFirst();

                    OWLObjectProperty property = LinkedISA.factory.getOWLObjectProperty(predicate);

                    String objectString = predicateObject.getSecond();


                    Set<OWLNamedIndividual> objects = LinkedISA.typeIndividualMap.get(objectString);

                    if (objects==null)
                        continue;

                    for(OWLNamedIndividual object: objects){

                        if (subject==null || object==null || property==null){
                            log.debug("At least one is null...");
                        }else{
                            OWLObjectPropertyAssertionAxiom axiom = LinkedISA.factory.getOWLObjectPropertyAssertionAxiom(property, subject, object);
                            LinkedISA.manager.addAxiom(LinkedISA.ontology, axiom);
                        }
                    }//for
                } //for
            } //for
        }

        log.info("... end of conversion for Study " + study.getStudyId() + ".");

    }


    /**
     * Converts the publications section.
     *
     *
     * @param publicationList
     * @param individual the study or investigation individual
     */
    private void convertPublications(List<Publication> publicationList, OWLNamedIndividual individual){

        Map<String,List<Pair<IRI, String>>> publicationMappings = LinkedISA.mapping.getPublicationPropertyMappings();
        Map<String, OWLNamedIndividual> publicationIndividuals = null;

        for(Publication pub: publicationList){

            boolean investigation = (pub instanceof InvestigationPublication);

            OWLNamedIndividual publicationIndividual = publicationIndividualMap.get(pub);

            publicationIndividuals = new HashMap<String, OWLNamedIndividual>();

            if (publicationIndividual==null) {

                String pubmedID = pub.getPubmedId();
                if (pubmedID != null && !pubmedID.equals("")){
                   // publicationIndividual = ISA2OWL.createIndividual(ExtendedISASyntax.PUBLICATION, pubmedID, pubmedID, ExternalRDFLinkages.getPubMedIRI(pubmedID), publicationIndividuals);
                    publicationIndividual = LinkedISA.createIndividual(ExtendedISASyntax.PUBLICATION, pubmedID, pubmedID, null, publicationIndividuals);
                } else {
                    //TODO
                }

                publicationIndividualMap.put(pub,publicationIndividual);

                //Study PubMed ID
                LinkedISA.createIndividual(investigation ? InvestigationPublication.PUBMED_ID : StudyPublication.PUBMED_ID, pub.getPubmedId(), publicationIndividuals);

                //Study Publication DOI
                LinkedISA.createIndividual(investigation ? InvestigationPublication.PUBLICATION_DOI : StudyPublication.PUBLICATION_DOI, pub.getPublicationDOI(), publicationIndividuals);

                //Study Publication Author List
                LinkedISA.createIndividual(investigation ? InvestigationPublication.PUBLICATION_AUTHOR_LIST : StudyPublication.PUBLICATION_AUTHOR_LIST, pub.getPublicationAuthorList(), publicationIndividuals);

                 //Study Publication Title
                LinkedISA.createIndividual(investigation ? InvestigationPublication.PUBLICATION_TITLE : StudyPublication.PUBLICATION_TITLE, pub.getPublicationTitle(), publicationIndividuals);
            } else {
                publicationIndividuals.put(ExtendedISASyntax.PUBLICATION, publicationIndividual);
            }

            if (investigation)
                publicationIndividuals.put(ExtendedISASyntax.INVESTIGATION, individual);
            else
                publicationIndividuals.put(ExtendedISASyntax.STUDY, individual);

            LinkedISA.convertProperties(publicationMappings, publicationIndividuals);

        }

    }

    /**
     * Converts contact information into RDF
     *
     * @param contactsList
     * @param individual this is either an Investigation individual or a Study individual
     */
    private void convertContacts(List<Contact> contactsList, OWLNamedIndividual individual){

        //process properties for the contactIndividuals
        Map<String,List<Pair<IRI, String>>> contactMappings = LinkedISA.mapping.getContactMappings();

        Map<String, OWLNamedIndividual> contactIndividuals = null;

        for(Contact contact: contactsList){

            OWLNamedIndividual contactIndividual = contactIndividualMap.get(contact);
            boolean investigation = contact instanceof InvestigationContact;
            contactIndividuals = new HashMap<String, OWLNamedIndividual>();

            if (contactIndividual==null){

                String studyPersonREF = contact.getComment("Comment[Study Person REF]");

                //Study Person
                if (studyPersonREF!=null && !studyPersonREF.equals("")) {

                    contactIndividual = LinkedISA.createIndividual(investigation ? ExtendedISASyntax.INVESTIGATION_PERSON : ExtendedISASyntax.STUDY_PERSON,
                            contact.getIdentifier(),
                            studyPersonREF,
                            null,
                            IRI.create(studyPersonREF));

                    contactIndividuals.put(investigation ? ExtendedISASyntax.INVESTIGATION_PERSON : ExtendedISASyntax.STUDY_PERSON, contactIndividual);

                } else {
                    contactIndividual = LinkedISA.createIndividual(investigation ? ExtendedISASyntax.INVESTIGATION_PERSON : ExtendedISASyntax.STUDY_PERSON, contact.getIdentifier(), contactIndividuals);
                }
                contactIndividualMap.put(contact, contactIndividual);

                //Study Person Last Name
                LinkedISA.createIndividual(investigation ? InvestigationContact.CONTACT_LAST_NAME : StudyContact.CONTACT_LAST_NAME, contact.getLastName(), contactIndividuals);

                //Study Person First Name
                LinkedISA.createIndividual(investigation ? InvestigationContact.CONTACT_FIRST_NAME : StudyContact.CONTACT_FIRST_NAME, contact.getFirstName(), contactIndividuals);

                //Study Person Mid Initials
                LinkedISA.createIndividual(investigation ? InvestigationContact.CONTACT_MID_INITIAL : StudyContact.CONTACT_MID_INITIAL, contact.getMidInitial(), contactIndividuals);

                //Study Person Email
                LinkedISA.createIndividual(investigation ? InvestigationContact.CONTACT_EMAIL : StudyContact.CONTACT_EMAIL, contact.getEmail(), contactIndividuals);

                //Study Person Phone
                LinkedISA.createIndividual(investigation ? InvestigationContact.CONTACT_PHONE : StudyContact.CONTACT_PHONE, contact.getPhone(), contactIndividuals);

                //Study Person Fax
                LinkedISA.createIndividual(investigation ? InvestigationContact.CONTACT_FAX : StudyContact.CONTACT_FAX, contact.getFax(), contactIndividuals);

                //Study Person Address
                LinkedISA.createIndividual(investigation ? InvestigationContact.CONTACT_ADDRESS : StudyContact.CONTACT_ADDRESS, contact.getAddress(), contactIndividuals);

                //Study Person Affiliation
                String affiliation = contact.getAffiliation();
                OWLNamedIndividual affiliationIndividual = affiliationIndividualMap.get(affiliation);
                if (affiliationIndividual!=null) {
                   contactIndividuals.put(affiliation, affiliationIndividual);
                }else {
                   affiliationIndividual = LinkedISA.createIndividual(investigation ? InvestigationContact.CONTACT_AFFILIATION : StudyContact.CONTACT_AFFILIATION, contact.getAffiliation(), contactIndividuals);
                }
                affiliationIndividualMap.put(affiliation, affiliationIndividual);

                //Investigation/Study Person Roles
                LinkedISA.createIndividual(investigation ? InvestigationContact.CONTACT_ROLE : StudyContact.CONTACT_ROLE, contact.getRole(), contactIndividuals);
            } else {
                contactIndividuals.put(investigation ? ExtendedISASyntax.INVESTIGATION_PERSON : ExtendedISASyntax.STUDY_PERSON, contactIndividual);
            }


            if (investigation)
                contactIndividuals.put(ExtendedISASyntax.INVESTIGATION, individual);
            else
                contactIndividuals.put(ExtendedISASyntax.STUDY, individual);

            LinkedISA.convertProperties(contactMappings, contactIndividuals);

        }
    }


    /**
     * This is the declarative definition of the factors. The factor values are in the study and assay tables.
     *
     * @param factorList
     */
    private void convertFactors(List<Factor> factorList, OWLNamedIndividual studyDesignIndividual){

        factorIndividualMap = new HashMap<String, OWLNamedIndividual>();
        Map<String, Set<OWLNamedIndividual>> factorIndividualsForProperties = new HashMap<String, Set<OWLNamedIndividual>>();
        factorIndividualsForProperties.put(StudyDesign.STUDY_DESIGN_TYPE, Collections.singleton(studyDesignIndividual));
        //map with <factor name> and <url or literal>

        Set<OWLNamedIndividual> factors = new HashSet<OWLNamedIndividual>();

        for(Factor factor: factorList){

            //Study Factor
            OWLNamedIndividual factorIndividual = LinkedISA.createIndividual(ExtendedISASyntax.STUDY_FACTOR, factor.getFactorName());
            factors.add(factorIndividual);
            factorIndividualMap.put(factor.getFactorName(), factorIndividual);

            //Study Factor Name
            OWLNamedIndividual factorNameIndividual = LinkedISA.createIndividual(Factor.FACTOR_NAME, factor.getFactorName());

            //use term source and term accession to declare a more specific type for the factor
            if (factor.getFactorTypeTermAccession()!=null && !factor.getFactorTypeTermAccession().equals("")
                    && factor.getFactorTypeTermSource()!=null && !factor.getFactorTypeTermSource().equals("")){

                if (factor.getFactorTypeTermAccession().startsWith("http"))
                    LinkedISA.addOWLClassAssertion(IRI.create(factor.getFactorTypeTermSource()), factorIndividual);
                    
                    LinkedISA.findOntologyTermAndAddClassAssertion(factor.getFactorTypeTermSource(), factor.getFactorTypeTermAccession(), factorIndividual);

            }//factors attributes not null


        }
        factorIndividualsForProperties.put(ExtendedISASyntax.STUDY_FACTOR, factors);

        Map<String,List<Pair<IRI, String>>> factorPropertyMappings = LinkedISA.mapping.getFactorPropertyMappings();
        LinkedISA.convertPropertiesMultipleIndividuals(factorPropertyMappings, factorIndividualsForProperties);


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
        OWLNamedIndividual studyDesignIndividual = LinkedISA.createIndividual(StudyDesign.STUDY_DESIGN_TYPE, study.getStudyId() + LinkedISA.STUDY_DESIGN_SUFFIX);

        for(StudyDesign studyDesign: studyDesigns){

            LinkedISA.addComment(studyDesign.getStudyDesignType(), studyDesignIndividual.getIRI());

            //use term source and term accession to declare a more specific type for the factor
            if (studyDesign.getStudyDesignTypeTermAcc()!=null && !studyDesign.getStudyDesignTypeTermAcc().equals("")
                && studyDesign.getStudyDesignTypeTermSourceRef()!=null && !studyDesign.getStudyDesignTypeTermSourceRef().equals("")){

                LinkedISA.findOntologyTermAndAddClassAssertion(studyDesign.getStudyDesignTypeTermSourceRef(), studyDesign.getStudyDesignTypeTermAcc(), studyDesignIndividual);
            }
        }

        OWLNamedIndividual studyDesignExecutionIndividual = LinkedISA.createIndividual(ExtendedISASyntax.STUDY_DESIGN_EXECUTION, study.getStudyId() + LinkedISA.STUDY_DESIGN_EXECUTION_SUFFIX);

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
        Map<String,List<Pair<IRI, String>>> protocolMappings = LinkedISA.mapping.getProtocolMappings();
        OWLNamedIndividual individual = null;

        for(Protocol protocol: protocolList){
            protocolIndividuals = new HashMap<String, OWLNamedIndividual>();

            //Study Protocol
            individual = LinkedISA.createIndividual(ExtendedISASyntax.STUDY_PROTOCOL, protocol.getProtocolName() + LinkedISA.STUDY_PROTOCOL_SUFFIX, protocolIndividuals);
            protocolIndividualMap.put(protocol.getProtocolName(),individual);
            LinkedISA.addComment(protocol.getProtocolType(), individual.getIRI());

            //Study Protocol Name
            LinkedISA.createIndividual(Protocol.PROTOCOL_NAME, protocol.getProtocolName() + LinkedISA.STUDY_PROTOCOL_NAME_SUFFIX, protocolIndividuals);

            //Study Protocol Description
            LinkedISA.createIndividual(Protocol.PROTOCOL_DESCRIPTION, protocol.getProtocolDescription(), protocolIndividuals);

            //Study Protocol URI
            LinkedISA.createIndividual(Protocol.PROTOCOL_URI, protocol.getProtocolURL(), protocolIndividuals);

            //Study Protocol Version
            LinkedISA.createIndividual(Protocol.PROTOCOL_VERSION, protocol.getProtocolVersion(), protocolIndividuals);

            //Study Protocol Parameters
            String[] parameterNames = protocol.getProtocolParameterNames();
            String[] termAccessions = protocol.getProtocolParameterNameAccessions();
            String[] termSources = protocol.getProtocolParameterNameSources();

            boolean annotated = false;
            if (parameterNames.length==termAccessions.length && parameterNames.length==termSources.length)
                annotated = true;

            int i = 0;
            for(String parameterName: parameterNames){

                //Study Protocol Parameter
                OWLNamedIndividual parameterNameIndividual = LinkedISA.createIndividual(ExtendedISASyntax.STUDY_PROTOCOL_PARAMETER, parameterName, protocolIndividuals);

                LinkedISA.createIndividual(Protocol.PROTOCOL_PARAMETER_NAME, parameterName, protocolIndividuals);

                if (annotated)
                    LinkedISA.findOntologyTermAndAddClassAssertion(termSources[i], termAccessions[i], parameterNameIndividual);
                i++;
            }
            LinkedISA.convertProperties(protocolMappings, protocolIndividuals);
        }

    }

    /**
     *
     * Converts the assays.
     *
     * @param assayMap
     */
    private void convertAssays(Map<String, Assay> assayMap,
                               List<Protocol> protocolList,
                               OWLNamedIndividual studyIndividual,
                               OWLNamedIndividual studyDesignIndividual,
                               OWLNamedIndividual studyFileIndividual,
                               OWLNamedIndividual isatabDistributionIndividual,
                               OWLNamedIndividual investigationFileIndividual){

        Map<String, Set<OWLNamedIndividual>> assayIndividualsForProperties;

        for(String assayRef: assayMap.keySet()){
            log.debug("AssayRef="+assayRef);
            Assay assay = assayMap.get(assayRef);

            assayIndividualsForProperties = new HashMap<String, Set<OWLNamedIndividual>>();
            assayIndividualsForProperties.put(ExtendedISASyntax.STUDY, Collections.singleton(studyIndividual));

            OWLNamedIndividual measurementIndividual = measurementTechnologyIndividuals.get(assay.getMeasurementEndpoint());

            if (measurementIndividual==null){

                //Study Assay Measurement Type
                measurementIndividual = LinkedISA.createIndividual(Assay.MEASUREMENT_ENDPOINT, assay.getMeasurementEndpoint(), null, assayIndividualsForProperties, null);
                measurementTechnologyIndividuals.put(assay.getMeasurementEndpoint(),measurementIndividual);
                LinkedISA.findOntologyTermAndAddClassAssertion(assay.getMeasurementEndpointTermSourceRef(),
                        assay.getMeasurementEndpointTermAccession(),
                        measurementIndividual);

            } else {
                assayIndividualsForProperties.put(Assay.MEASUREMENT_ENDPOINT, Collections.singleton(measurementIndividual));
            }

            OWLNamedIndividual technologyIndividual = measurementTechnologyIndividuals.get(assay.getTechnologyType());

            if (technologyIndividual==null){
                 //Study Assay Technology Type
                technologyIndividual = LinkedISA.createIndividual(Assay.TECHNOLOGY_TYPE, assay.getTechnologyType(), null, assayIndividualsForProperties, null);
                measurementTechnologyIndividuals.put(assay.getTechnologyType(),technologyIndividual);
                LinkedISA.findOntologyTermAndAddClassAssertion(assay.getTechnologyTypeTermSourceRef(),
                        assay.getTechnologyTypeTermAccession(),
                        technologyIndividual);
            } else {
                assayIndividualsForProperties.put(Assay.TECHNOLOGY_TYPE, Collections.singleton(technologyIndividual));
            }

            //Study Assay File
            OWLNamedIndividual studyAssayFileIndividual = LinkedISA.createIndividual(ExtendedISASyntax.STUDY_ASSAY_FILE, assay.getAssayReference(), null, assayIndividualsForProperties, null);

            //Study Assay File Name
            LinkedISA.createIndividual(Assay.ASSAY_REFERENCE, assay.getAssayReference() + " filename ", null, assayIndividualsForProperties, null);

            //ISAtab_distribution has_part assay_file
            LinkedISA.createObjectPropertyAssertion(ISA.HAS_PART, isatabDistributionIndividual, studyAssayFileIndividual);
            LinkedISA.createObjectPropertyAssertion(ISA.POINTS_TO, investigationFileIndividual, studyAssayFileIndividual);
            LinkedISA.createObjectPropertyAssertion(ISA.POINTS_TO, studyFileIndividual, studyAssayFileIndividual);
            LinkedISA.createObjectPropertyAssertion(ISA.DESCRIBES, studyAssayFileIndividual, studyIndividual);

            //ISAowl_distribution

            Assay2LinkedConverter assayConverter = new Assay2LinkedConverter();
            assayConverter.convert(assay, Assay2LinkedConverter.AssayTableType.ASSAY, sampleIndividualMap,
                    protocolList, protocolIndividualMap, studyDesignIndividual, studyIndividual, LinkedISA.groupsAtStudyLevel ? false : true,
                    assayIndividualsForProperties, studyAssayFileIndividual, factorIndividualMap);
        }

    }


}
