package org.isatools.isa2owl.converter;

import org.apache.log4j.Logger;

import org.isatools.isacreator.model.*;

import org.isatools.graph.model.MaterialNode;

import org.isatools.isa2owl.mapping.ISASyntax2OWLMapping;
import org.isatools.isacreator.io.importisa.ISAtabFilesImporter;
import org.isatools.isacreator.io.importisa.ISAtabImporter;
import org.isatools.isacreator.ontologymanager.OntologyManager;
import org.isatools.isacreator.ontologymanager.OntologySourceRefObject;

import org.isatools.owl.OWLUtil;
import org.isatools.owl.ReasonerService;
import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import java.io.File;
import java.net.URISyntaxException;
import java.util.*;

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
        System.out.println("importer="+importer);
    }


    /**
     *
     * @param parentDir
     */
    public boolean convert(String parentDir, String iri ){
        System.out.println("2 Converting ISA-TAB dataset " + parentDir);

        ISA2OWL.setIRI(iri);


        try{
            //TODO add AutoIRIMapper
            //adding mapper for local ontologies
            //manager.addIRIMapper(new SimpleIRIMapper(IRI.create(ISAtab2OWLConverter.BFO_IRI), IRI.create(getClass().getClassLoader().getResource("owl/ruttenberg-bfo2.owl"))));
            ISA2OWL.manager.addIRIMapper(new SimpleIRIMapper(IRI.create(ISAtab2OWLConverter.OBI_IRI), IRI.create(getClass().getClassLoader().getResource("owl/extended-obi.owl"))));

            ISA2OWL.ontology = ISA2OWL.manager.createOntology(ISA2OWL.ontoIRI);
            ISA2OWL.reasonerService = new ReasonerService(ISA2OWL.ontology);

            //only import extended-obi.owl
            OWLImportsDeclaration importDecl = ISA2OWL.factory.getOWLImportsDeclaration(IRI.create("http://purl.obolibrary.org/obo/extended-obi.owl"));
            ISA2OWL.manager.applyChange(new AddImport(ISA2OWL.ontology, importDecl));

        }catch(URISyntaxException e){
            e.printStackTrace();
        }catch(OWLOntologyCreationException e){
            e.printStackTrace();
        }


        if (!readInISAFiles(parentDir)){
            System.out.println(importer.getMessagesAsString());
        }

        Investigation investigation = importer.getInvestigation();
        //processSourceOntologies();

        System.out.println("investigation=" + investigation);
        log.debug("investigation=" + investigation);

        convertInvestigation(investigation);

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


        return true;
    }


    /**
     * TODO imports from mapping vs imports from ISAtab dataset
     *
     */
    private void processSourceOntologies(){
        System.out.println("PROCESS SOURCE ONTOLOGY");

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
            System.out.println("sourceFile="+sourceFile);

            if (sourceFile==null || sourceFile.equals("") || sourceFile.contains("obi"))
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
        return importer.importFile(parentDir);
    }

    /**
     * Saves resulting ontology
     * @param filename
     */
    public void saveOntology(String filename){
        File file = new File(filename);
        OWLUtil.saveRDFXML(ISA2OWL.ontology, IRI.create(file.toURI()));
        OWLUtil.systemOutputMOWLSyntax(ISA2OWL.ontology);
    }

    private void convertInvestigation(Investigation investigation){

        ISA2OWL.createIndividual(Investigation.INVESTIGATION_SUBMISSION_DATE_KEY, investigation.getSubmissionDate());

        ISA2OWL.createIndividual(Investigation.INVESTIGATION_PUBLIC_RELEASE_KEY, investigation.getPublicReleaseDate());

        //TODO add the rest of the elements for the investigation

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
        OWLNamedIndividual studyTitleIndividual = ISA2OWL.createIndividual(Study.STUDY_TITLE, study.getStudyId()+ISA2OWL.STUDY_TITLE_SUFFIX, study.getStudyTitle());
        if (studyTitleIndividual!=null){
            OWLDataProperty hasMeasurementValue = ISA2OWL.factory.getOWLDataProperty(ISA2OWL.ISA_HAS_VALUE);
            OWLLiteral titleLiteral = ISA2OWL.factory.getOWLLiteral(study.getStudyTitle(), OWL2Datatype.XSD_STRING);
            OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom = ISA2OWL.factory.getOWLDataPropertyAssertionAxiom(hasMeasurementValue, studyTitleIndividual, titleLiteral);
            ISA2OWL.manager.addAxiom(ISA2OWL.ontology, dataPropertyAssertionAxiom);
        }

        //Study description
        OWLNamedIndividual studyDescriptionIndividual = ISA2OWL.createIndividual(Study.STUDY_DESC, study.getStudyId()+ISA2OWL.STUDY_DESCRIPTION_SUFFIX, study.getStudyDesc());
        if (studyDescriptionIndividual!=null){
            OWLDataProperty hasMeasurementValue = ISA2OWL.factory.getOWLDataProperty(ISA2OWL.ISA_HAS_VALUE);
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
            OWLDataProperty hasMeasurementValue = ISA2OWL.factory.getOWLDataProperty(ISA2OWL.ISA_HAS_VALUE);
            OWLLiteral publicReleaseDateLiteral = ISA2OWL.factory.getOWLLiteral(study.getPublicReleaseDate(), OWL2Datatype.XSD_STRING);
            OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom = ISA2OWL.factory.getOWLDataPropertyAssertionAxiom(hasMeasurementValue, publicReleaseDateIndividual, publicReleaseDateLiteral);
            ISA2OWL.manager.addAxiom(ISA2OWL.ontology, dataPropertyAssertionAxiom);
        }

        //Publications
        List<Publication> publicationList = study.getPublications();
        convertPublications(publicationList);

        //Study design
        List<StudyDesign> studyDesignList = study.getStudyDesigns();
        for(StudyDesign studyDesign: studyDesignList){
            convertStudyDesign(studyIndividual, studyDesign);
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
        assay2OWLConverter.convert(study.getStudySample(), Assay2OWLConverter.AssayTableType.STUDY, null, protocolIndividualMap,true);

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

        System.out.println("... end of conversion for Study " + study.getStudyId() + ".");

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
                    && factor.getFactorTypeTermSource()!=null && !factor.getFactorTypeTermSource().equals("")){

                ISA2OWL.findOntologyTermAndAddClassAssertion(factor.getFactorTypeTermSource(), factor.getFactorTypeTermAccession(), factorIndividual);

            }//factors attributes not null
        }

    }


    private void convertStudyDesign(OWLNamedIndividual studyIndividual, StudyDesign studyDesign){

        //Study Design Type
        //define a StudyDesignExecution per StudyDesign and associate with study (Study has_part StudyDesignExecution
        OWLNamedIndividual studyDesignIndividual = ISA2OWL.createIndividual(StudyDesign.STUDY_DESIGN_TYPE, studyDesign.getStudyDesignType());

        //use term source and term accession to declare a more specific type for the factor
        if (studyDesign.getStudyDesignTypeTermAcc()!=null && !studyDesign.getStudyDesignTypeTermAcc().equals("")
                && studyDesign.getStudyDesignTypeTermSourceRef()!=null && !studyDesign.getStudyDesignTypeTermSourceRef().equals("")){

            ISA2OWL.findOntologyTermAndAddClassAssertion(studyDesign.getStudyDesignTypeTermSourceRef(), studyDesign.getStudyDesignTypeTermAcc(), studyDesignIndividual);
        }

        OWLNamedIndividual studyDesignExecutionIndividual = ISA2OWL.createIndividual(studyDesign.getStudyDesignType()+ISA2OWL.STUDY_DESIGN_EXECUTION_SUFFIX, ISA2OWL.OBI_STUDY_DESIGN_EXECUTION);

        OWLObjectProperty executes = ISA2OWL.factory.getOWLObjectProperty(ISA2OWL.ISA_EXECUTES);
        OWLObjectPropertyAssertionAxiom axiom1 = ISA2OWL.factory.getOWLObjectPropertyAssertionAxiom(executes,studyDesignExecutionIndividual, studyDesignIndividual);
        ISA2OWL.manager.addAxiom(ISA2OWL.ontology, axiom1);


        OWLObjectProperty isPartOf = ISA2OWL.factory.getOWLObjectProperty(ISA2OWL.BFO_IS_PART_OF);
        OWLObjectPropertyAssertionAxiom axiom2 = ISA2OWL.factory.getOWLObjectPropertyAssertionAxiom(isPartOf,studyDesignExecutionIndividual, studyIndividual);
        ISA2OWL.manager.addAxiom(ISA2OWL.ontology, axiom2);


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
            individual = ISA2OWL.createIndividual(ExtendedISASyntax.STUDY_PROTOCOL, protocol.getProtocolName()+ISA2OWL.STUDY_PROTOCOL_SUFFIX, protocolIndividuals);
            protocolIndividualMap.put(protocol.getProtocolName(),individual);

            //Study Protocol Name
            ISA2OWL.createIndividual(Protocol.PROTOCOL_NAME, protocol.getProtocolName()+ISA2OWL.STUDY_PROTOCOL_NAME_SUFFIX, protocolIndividuals);

            //Study Protocol Type
            ISA2OWL.createIndividual(Protocol.PROTOCOL_TYPE, protocol.getProtocolType(), protocolIndividuals);

            //use term source and term accession to declare a more specific type for the protocol
            if (protocol.getProtocolTypeTermAccession()!=null && !protocol.getProtocolTypeTermAccession().equals("")
                    && protocol.getProtocolTypeTermSourceRef()!=null && !protocol.getProtocolTypeTermSourceRef().equals("")){

                ISA2OWL.findOntologyTermAndAddClassAssertion(protocol.getProtocolTypeTermSourceRef(), protocol.getProtocolTypeTermAccession(), individual);

            }//factors attributes not null

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
            //OWLNamedIndividual studyAssayIndividual = ISA2OWL.createIndividual(ExtendedISASyntax.STUDY_ASSAY, assay.getIdentifier());

            //Study Assay Measurement Type
            ISA2OWL.createIndividual(Assay.MEASUREMENT_ENDPOINT, assay.getMeasurementEndpoint());

            //Study Assay Technology Type
            ISA2OWL.createIndividual(Assay.TECHNOLOGY_TYPE, assay.getTechnologyType());

            //Study Assay File
            OWLNamedIndividual studyAssayFile = ISA2OWL.createIndividual(ExtendedISASyntax.STUDY_ASSAY_FILE, assay.getAssayReference());

            //Study Assay File Name
            ISA2OWL.createIndividual(Assay.ASSAY_REFERENCE, assay.getAssayReference());

            Assay2OWLConverter assayConverter = new Assay2OWLConverter();
            assayConverter.convert(assay, Assay2OWLConverter.AssayTableType.ASSAY, studyAssayFile, protocolIndividualMap, false);
        }

    }


}
