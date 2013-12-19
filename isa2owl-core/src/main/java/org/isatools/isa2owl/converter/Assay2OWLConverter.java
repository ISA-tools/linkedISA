package org.isatools.isa2owl.converter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.isatools.graph.model.ISAFactorValue;
import org.isatools.graph.model.ISAMaterialAttribute;
import org.isatools.graph.model.ISANode;
import org.isatools.graph.model.ISAUnit;
import org.isatools.graph.model.impl.*;
import org.isatools.graph.parser.GraphParser;
import org.isatools.isacreator.model.Assay;
import org.isatools.isacreator.model.GeneralFieldTypes;
import org.isatools.isacreator.model.Protocol;
import org.isatools.isacreator.ontologymanager.OntologyManager;
import org.isatools.owl.ExtendedOBIVocabulary;
import org.isatools.owl.IAO;
import org.isatools.owl.ISA;
import org.isatools.owl.OBI;
import org.isatools.syntax.ExtendedISASyntax;
import org.isatools.util.Pair;
import org.semanticweb.owlapi.model.*;

import java.util.*;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 07/11/2012
 * Time: 16:11
 *
 * Converts the assay representation into OWL.
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class Assay2OWLConverter {

    private static final Logger log = Logger.getLogger(Assay2OWLConverter.class);

    public enum AssayTableType { STUDY, ASSAY} ;

    //private fields
    private AssayTableType assayTableType;
    private GraphParser graphParser = null;
    private Object[][] data = null;
    //a matrix will all the individuals for the data (these are MaterialNodes or ProcessNodes individuals
    private OWLNamedIndividual[][] individualMatrix = null;
    //private Map<ISAMaterialNode, Map<String,OWLNamedIndividual>> materialNodeIndividualMap = new HashMap<ISAMaterialNode, Map<String,OWLNamedIndividual>>();

    private Map<String, OWLNamedIndividual> processIndividualMap = new HashMap<String, OWLNamedIndividual>();
    private Map<String, OWLNamedIndividual> materialAttributeIndividualMap = new HashMap<String, OWLNamedIndividual>();
    private Map<String, OWLNamedIndividual> factorValueIndividuals = new HashMap<String, OWLNamedIndividual>();
    private Set<OWLNamedIndividual> assayFileSampleIndividualSet = null;

    public Assay2OWLConverter(){
        log.info("Assay2OWLConverter - constructor");
    }

    /***
     *
     * It converts the assay information to RDF
     *
     * @param assay
     * @param att
     * @param sampleIndividualMap
     * @param protocolList
     * @param protocolIndividualMap
     * @param studyDesignIndividual
     * @param studyIndividual
     * @param convertGroups
     * @return
     */
    public Map<String, OWLNamedIndividual> convert(Assay assay,
                                                   AssayTableType att,
                                                   Map<String,OWLNamedIndividual> sampleIndividualMap,
                                                   List<Protocol> protocolList,
                                                   Map<String, OWLNamedIndividual> protocolIndividualMap,
                                                   OWLNamedIndividual studyDesignIndividual,
                                                   OWLNamedIndividual studyIndividual,
                                                   boolean convertGroups,
                                                   Map<String, Set<OWLNamedIndividual>> assayIndividualsForProperties){
        log.debug("CONVERTING ASSAY ---> AssayTableType="+att);
        assayTableType = att;
        data = assay.getAssayDataMatrix();
        individualMatrix = new OWLNamedIndividual[data.length][data[0].length];

        graphParser = new GraphParser(assay.getAssayDataMatrix());
        graphParser.parse();
        Graph graph = graphParser.getGraph();

        //if it is a study table
        if (assayTableType == AssayTableType.STUDY && sampleIndividualMap!=null){
            System.err.println("Converting STUDY table and sample individuals are not null - this cannot be possible");
            System.exit(-1);
        }

        //if it is an assay table, the sampleIndividualMap cannot be null
        if (assayTableType == AssayTableType.ASSAY && sampleIndividualMap==null){
            System.err.println("Converting ASSAY table and sample individuals are null - they should have been defined in the STUDY table");
            System.exit(-1);
        }

        //this method also sets the assayFileSampleIndividualSet variable, if it is an ASSAY
        sampleIndividualMap = convertMaterialNodes(graph, sampleIndividualMap, studyIndividual);

        if (assayTableType == AssayTableType.ASSAY){
            assayIndividualsForProperties.put(ExtendedISASyntax.SAMPLE, assayFileSampleIndividualSet);
        }
        convertDataNodes(graph);

        if (assayTableType == AssayTableType.ASSAY){
            convertAssayNodes(protocolIndividualMap, graph, assayIndividualsForProperties);
            convertProcessNodes(graph, assayTableType);
        }

        convertProtocolExecutionNodes(protocolList, protocolIndividualMap, graph, assayTableType);

        if (convertGroups){
            convertGroups(studyDesignIndividual,sampleIndividualMap);
        }
        return sampleIndividualMap;
    }

    /***
     *
     *
     *
     * @param protocolIndividualMap
     * @param graph
     * @param assayIndividualsForProperties
     */
    private void convertAssayNodes(Map<String, OWLNamedIndividual> protocolIndividualMap, Graph graph, Map<String, Set<OWLNamedIndividual>> assayIndividualsForProperties) {
        //assay individuals
        List<ISANode> assayNodes = graph.getNodes(NodeType.ASSAY_NODE);

        //used to avoid repetitions of assayIndividuals
        Map<String, OWLNamedIndividual> assayIndividuals = new HashMap<String, OWLNamedIndividual>();
        for(ISANode node: assayNodes){
            AssayNode assayNode = (AssayNode) node;

            int col = assayNode.getIndex();
            for(int row=1; row < data.length; row++){

                String dataValue = (String) data[row][col];
                if (dataValue.equals(""))
                    continue;


                OWLNamedIndividual assayIndividual = assayIndividuals.get(dataValue);
                if (assayIndividual==null){
                    assayIndividual = ISA2OWL.createIndividual(ExtendedISASyntax.STUDY_ASSAY, dataValue);
                    assayIndividuals.put(dataValue, assayIndividual);

                    //inputs & outputs
                    //adding inputs and outputs to the assay
                    OWLObjectProperty has_specified_input = ISA2OWL.factory.getOWLObjectProperty(IRI.create(OBI.HAS_SPECIFIED_INPUT));
                    List<ISANode> inputs = assayNode.getInputNodes();
                    for(ISANode input: inputs){
                        int inputCol = input.getIndex();

                        if (!data[row][inputCol].toString().equals("")){

                            if (individualMatrix[row][inputCol]==null){
                                System.out.println("individualMatrix[row][inputCol]==null!!!! " + individualMatrix[row][inputCol] == null + "  row=" + row + " inputCol=" + inputCol);
                            }else{
                                OWLNamedIndividual inputIndividual = individualMatrix[row][inputCol];
                                ISA2OWL.addObjectPropertyAssertionAxiom(has_specified_input, assayIndividual, inputIndividual);
                            }
                        }

                    }//for inputs

                    OWLObjectProperty has_specified_output = ISA2OWL.factory.getOWLObjectProperty(IRI.create(OBI.HAS_SPECIFIED_OUTPUT));
                    List<ISANode> outputs = assayNode.getOutputNodes();
                    for(ISANode output: outputs){
                        int outputCol = output.getIndex();
                        if (!data[row][outputCol].toString().equals("")){

                            if (individualMatrix[row][outputCol]!=null){
                                ISA2OWL.addObjectPropertyAssertionAxiom(has_specified_output, assayIndividual, individualMatrix[row][outputCol]);
                            }else{
                                System.out.println("individualMatrix[row][outputCol es null!!!! "+ individualMatrix[row][outputCol]+ "  row="+row+" outputCol="+outputCol);
                            }
                        } else {
                            System.out.println("the element value is empty");
                        }

                    }//for outputs

                    //add comments
                    for(CommentNode comment: assayNode.getComments()){
                        int comment_col = comment.getIndex();
                        ISA2OWL.addComment( comment.getName() + ":" + ((String)data[row][comment_col]), assayIndividual.getIRI());
                    }


                    //realizes o concretizes (executes) associated protocol
                    List<ProcessNode> associatedProcessNodes = assayNode.getAssociatedProcessNodes();
                    for(ProcessNode processNode: associatedProcessNodes){

                        int protocolColumn = processNode.getIndex();
                        String protocolName = (String)data[row][protocolColumn];

                        OWLNamedIndividual protocolIndividual = protocolIndividualMap.get(protocolName);
                        OWLObjectProperty executes = ISA2OWL.factory.getOWLObjectProperty(ExtendedOBIVocabulary.EXECUTES.iri);
                        ISA2OWL.addObjectPropertyAssertionAxiom(executes, assayIndividual, protocolIndividual);
                    }//for process node

                }//if individual is null.

                assayIndividualsForProperties.put(ExtendedISASyntax.STUDY_ASSAY, Collections.singleton(assayIndividual));
                Map<String,List<Pair<IRI, String>>> assayPropertyMappings = ISA2OWL.mapping.getAssayPropertyMappings();
                ISA2OWL.convertPropertiesMultipleIndividuals(assayPropertyMappings, assayIndividualsForProperties);

            }//for
        }
    }

    /**
     *
     * Converts process nodes.
     *
     * ProcessNodes are either Data Transformations or Data Normalizations columns
     *
     * @param graph
     * @param assayTableType
     */
    private void convertProcessNodes(Graph graph, AssayTableType assayTableType) {
        //Process Nodes
        List<ISANode> processNodes = graph.getNodes(NodeType.PROCESS_NODE);

        for(ISANode node: processNodes){

            ProcessNode processNode = (ProcessNode) node;
            //keeping all the individuals relevant for this processNode
            Map<String, OWLNamedIndividual> processNodeIndividuals = new HashMap<String,OWLNamedIndividual>();


            int processCol = processNode.getIndex();

            for(int processRow=1; processRow < data.length; processRow ++){

                log.debug("processRow="+processRow);
                String processNodeValue = null;
                if (processCol==-1){
                    processNodeValue = processNode.toShortString();
                } else {
                    processNodeValue = (data[processRow][processCol]).toString();
                }

                if (processNodeValue.equals("")){
                    log.debug("ProcessNodeValue is empty!!!");
                    continue;
                } else {
                    log.debug("ProcessNodeValue = " + processNodeValue);
                }

                OWLNamedIndividual processIndividual = processIndividualMap.get(processNodeValue);

                //material processing as the execution of the protocol
                if (processIndividual==null){

                    //System.out.println(processNode.getName());

                    //System.out.println( processNode.getName().startsWith(ExtendedISASyntax.DATA_TRANSFORMATION.toString()) );

                    processIndividual = ISA2OWL.createIndividual(
                            processNode.getName().startsWith(ExtendedISASyntax.DATA_TRANSFORMATION.toString()) ?
                                    ExtendedISASyntax.DATA_TRANSFORMATION : ExtendedISASyntax.NORMALIZATION_NAME , processNodeValue);
                    processIndividualMap.put(processNodeValue, processIndividual);
                }

                processNodeIndividuals.put(assayTableType == AssayTableType.STUDY ? ExtendedISASyntax.STUDY_PROTOCOL_REF : ExtendedISASyntax.ASSAY_PROTOCOL_REF, processIndividual);

                //inputs & outputs
                List<ISANode> inputs = processNode.getInputNodes();
                for(ISANode input: inputs){
                    int inputCol = input.getIndex();

                    if (!data[processRow][inputCol].toString().equals("")){
                        processNodeIndividuals.put(assayTableType == AssayTableType.STUDY ? ExtendedISASyntax.STUDY_PROTOCOL_REF_INPUT : ExtendedISASyntax.ASSAY_PROTOCOL_REF_INPUT, individualMatrix[processRow][inputCol]);
                    }

                }//for inputs

                List<ISANode> outputs = processNode.getOutputNodes();
                for(ISANode output: outputs){
                    int outputCol = output.getIndex();
                    if (!data[processRow][outputCol].toString().equals("")){
                        processNodeIndividuals.put(assayTableType == AssayTableType.STUDY ? ExtendedISASyntax.STUDY_PROTOCOL_REF_OUTPUT : ExtendedISASyntax.ASSAY_PROTOCOL_REF_OUTPUT, individualMatrix[processRow][outputCol]);
                    }

                }//for outputs

                //add comments
                for(CommentNode comment: processNode.getComments()){
                    int comment_col = comment.getIndex();
                    ISA2OWL.addComment( comment.getName() + ":" + ((String)data[processRow][comment_col]), processIndividual.getIRI() );
                }

                Map<String, List<Pair<IRI,String>>> protocolREFmapping = ISA2OWL.mapping.getProtocolREFMappings();
                ISA2OWL.convertProperties(protocolREFmapping, processNodeIndividuals);


            } //processRow
        }
    }

    /**
     *
     * Converts ProtocolExecutionNodes.
     *
     * Only the ProtocolExecutions will have an associated declared protocol.
     *
     * @param protocolIndividualMap
     * @param graph
     * @param assayTableType
     */
    private void convertProtocolExecutionNodes(List<Protocol> protocolList, Map<String, OWLNamedIndividual> protocolIndividualMap, Graph graph, AssayTableType assayTableType) {
        //Process Nodes
        List<ISANode> protocolExecutionNodes = graph.getNodes(NodeType.PROTOCOL_EXECUTION_NODE);

        Map<String, Protocol> protocolMap = new HashMap<String, Protocol>();

        for(Protocol protocol: protocolList){
            protocolMap.put(protocol.getProtocolName(), protocol);
        }

        for(ISANode node: protocolExecutionNodes){

            ProtocolExecutionNode processNode = (ProtocolExecutionNode) node;

            int processCol = processNode.getIndex();

            //keeping all the individuals relevant for this processNode
            Map<String, OWLNamedIndividual> protocolREFIndividuals = new HashMap<String,OWLNamedIndividual>();

            for(int processRow=1; processRow < data.length; processRow ++){

                String protocolExecutionValue = null;
                if (processCol==-1){
                    protocolExecutionValue = processNode.toShortString();
                } else {
                 protocolExecutionValue = (data[processRow][processCol]).toString();
                }

                if (protocolExecutionValue.equals("")){
                    log.debug("ProtocolExecutionValue is empty!!!");
                    continue;
                } //else {
                  //  System.out.println("ProtocolExecutionValue = "+protocolExecutionValue);
                //}
                Protocol protocol = protocolMap.get(protocolExecutionValue);

                OWLNamedIndividual protocolIndividual = protocolIndividualMap.get(protocolExecutionValue);

                if (protocolIndividual==null) {
                    System.err.println("Protocol "+protocolExecutionValue+" must already exist");
                } else {

                    //add comments
                    for(CommentNode comment: processNode.getComments()){
                        int comment_col = comment.getIndex();
                        ISA2OWL.addComment( comment.getName() + ":" + ((String)data[processRow][comment_col]), protocolIndividual.getIRI());
                    }


                    //adding Study Protocol
                    protocolREFIndividuals.put(ExtendedISASyntax.STUDY_PROTOCOL, protocolIndividual);
                }


                OWLNamedIndividual processIndividual = processIndividualMap.get(protocolExecutionValue);

                //material processing as the execution of the protocol
                if (processIndividual==null){
                    processIndividual = ISA2OWL.createIndividual(assayTableType == AssayTableType.STUDY ? ExtendedISASyntax.STUDY_PROTOCOL_REF : ExtendedISASyntax.ASSAY_PROTOCOL_REF, protocolExecutionValue);
                    processIndividualMap.put(protocolExecutionValue, processIndividual);
                }

                if (protocol!=null && protocol.getProtocolType()!=null){
                    ISA2OWL.addComment(protocol.getProtocolType(), processIndividual.getIRI());
                } else{
                    System.out.println("Protocol type is null for protocol "+protocol);
                }

                if (protocolIndividual != null){
                    OWLObjectProperty executes = ISA2OWL.factory.getOWLObjectProperty(ExtendedOBIVocabulary.EXECUTES.iri);
                    OWLObjectPropertyAssertionAxiom axiom1 = ISA2OWL.factory.getOWLObjectPropertyAssertionAxiom(executes,processIndividual, protocolIndividual);
                    ISA2OWL.manager.addAxiom(ISA2OWL.ontology, axiom1);

                    //use term source and term accession to declare a more specific type for the process node
                    if (protocol.getProtocolTypeTermAccession()!=null && !protocol.getProtocolTypeTermAccession().equals("")
                            && protocol.getProtocolTypeTermSourceRef()!=null && !protocol.getProtocolTypeTermSourceRef().equals("")){

                        ISA2OWL.findOntologyTermAndAddClassAssertion(protocol.getProtocolTypeTermSourceRef(), protocol.getProtocolTypeTermAccession(), processIndividual);

                    }//process node attributes not null
                }

                protocolREFIndividuals.put(assayTableType == AssayTableType.STUDY ? ExtendedISASyntax.STUDY_PROTOCOL_REF : ExtendedISASyntax.ASSAY_PROTOCOL_REF, processIndividual);

                //inputs & outputs
                List<ISANode> inputs = processNode.getInputNodes();
                for(ISANode input: inputs){
                    int inputCol = input.getIndex();

                    if (!data[processRow][inputCol].toString().equals("")){
                        protocolREFIndividuals.put(assayTableType == AssayTableType.STUDY ? ExtendedISASyntax.STUDY_PROTOCOL_REF_INPUT: ExtendedISASyntax.ASSAY_PROTOCOL_REF_INPUT, individualMatrix[processRow][inputCol]);
                    }

                }//for inputs

                List<ISANode> outputs = processNode.getOutputNodes();
                for(ISANode output: outputs){
                    int outputCol = output.getIndex();
                    if (!data[processRow][outputCol].toString().equals("")){
                        protocolREFIndividuals.put(assayTableType == AssayTableType.STUDY ? ExtendedISASyntax.STUDY_PROTOCOL_REF_OUTPUT: ExtendedISASyntax.ASSAY_PROTOCOL_REF_OUTPUT, individualMatrix[processRow][outputCol]);
                    }

                }//for outputs

                Map<String, List<Pair<IRI,String>>> protocolREFmapping = ISA2OWL.mapping.getProtocolREFMappings();
                ISA2OWL.convertProperties(protocolREFmapping, protocolREFIndividuals);
            }//processRow

        }//processNode
    }

    private void convertDataNodes(Graph graph) {
        OWLNamedIndividual dataNodeIndividual = null;

        //Data Nodes
        List<ISANode> dataNodes = graph.getNodes(NodeType.DATA_NODE);

        for(ISANode node: dataNodes){
            DataNode dataNode = (DataNode) node;

            int col = dataNode.getIndex();

            for(int row=1; row < data.length; row++){

                String dataValue = (String) data[row][col];

                if (dataValue.equals(""))
                    continue;

                //Data Node
                dataNodeIndividual = ISA2OWL.createIndividual(dataNode.getName(), dataValue, dataValue);
                individualMatrix[row][col] = dataNodeIndividual;
            }
        }
    }

    /***
     *
     * Creates the RDF for the material nodes (sources, samples, extracts, labeled extracts)
     *
     * @param graph the ISA-TAB files parsed as a org.isatools.graph
     * @param sampleIndividualMap a map with the individuals corresponding to samples, this is null for a STUDY table
     * @return
     */
    private Map<String, OWLNamedIndividual> convertMaterialNodes(Graph graph, Map<String, OWLNamedIndividual> sampleIndividualMap, OWLNamedIndividual studyIndividual) {
        OWLNamedIndividual materialNodeIndividual = null;

        boolean sampleIndividualMapWasNull = (sampleIndividualMap==null);


        if (sampleIndividualMapWasNull){
            sampleIndividualMap = new HashMap<String, OWLNamedIndividual>();
        } else {
            assayFileSampleIndividualSet = new HashSet<OWLNamedIndividual>();
        }

        //Material Nodes
        List<ISANode> materialNodes = graph.getNodes(NodeType.MATERIAL_NODE);

        for(ISANode node: materialNodes){

            MaterialNode materialNode = (MaterialNode) node;

            //if the material node is a sample, and the sampleIndividualMap is not null,
            // use the samples in the Map and only add Characteristics (but don't create new individuals)
            boolean createIndividualForMaterialNode = true;
            if (materialNode.getMaterialNodeType() == ExtendedISASyntax.SAMPLE && !sampleIndividualMapWasNull){
                createIndividualForMaterialNode = false;
            }

            int col = materialNode.getIndex();

            for(int row=1; row < data.length; row++){

                Map<String, Set<OWLNamedIndividual>> materialNodeAndAttributesIndividuals = new HashMap<String,Set<OWLNamedIndividual>>();
                Set<OWLNamedIndividual> set = new HashSet();
                set.add(studyIndividual);
                materialNodeAndAttributesIndividuals.put(ExtendedISASyntax.STUDY, set);

                String dataValue = (String) data[row][col];

                if (dataValue.equals(""))
                    continue;

                if ( createIndividualForMaterialNode ){

                    //Material Node
                    materialNodeIndividual = ISA2OWL.createIndividual(materialNode.getMaterialNodeType(), dataValue +" " +materialNode.getMaterialNodeType(), materialNode.getMaterialNodeType());

                    //add comments
                    for(CommentNode comment: materialNode.getComments()){
                        int comment_col = comment.getIndex();
                        ISA2OWL.addComment( comment.getName() +":"+((String)data[row][comment_col]), materialNodeIndividual.getIRI());
                    }


                    if (materialNode.getMaterialNodeType() == ExtendedISASyntax.SAMPLE ){//&& sampleIndividualMapWasNull){
                        sampleIndividualMap.put(dataValue, materialNodeIndividual);
                    }

                    Set<OWLNamedIndividual> set2 = new HashSet();
                    set2.add(materialNodeIndividual);
                    materialNodeAndAttributesIndividuals.put(materialNode.getMaterialNodeType(), set2);

                    //Material Node Annotation
                    String purl = OntologyManager.getOntologyTermPurl(dataValue);
                    if (purl!=null && !purl.equals("")){
                        log.debug("If there is a PURL, use it! "+purl);
                    }else{

                        String source = OntologyManager.getOntologyTermSource(dataValue);
                        String accession = OntologyManager.getOntologyTermAccession(dataValue);
                        ISA2OWL.findOntologyTermAndAddClassAssertion(source, accession, materialNodeIndividual);

                    }

                    //Material Node Name
                    OWLNamedIndividual materialNodeIndividualName = ISA2OWL.createIndividual(GeneralFieldTypes.SOURCE_NAME.toString(),dataValue);
                    Set<OWLNamedIndividual> set3 = new HashSet();
                    set3.add(materialNodeIndividualName);
                    materialNodeAndAttributesIndividuals.put(GeneralFieldTypes.SOURCE_NAME.toString(), set3);

                    //adding factor values only when creating individual (so the factors come from the study sample file
                    if (materialNode instanceof SampleNode){
                        List<ISAFactorValue> factorValues = ((SampleNode) materialNode).getFactorValues();
                        convertFactorValues(materialNodeIndividual, factorValues, row);
                    }


                } else {
                    materialNodeIndividual = sampleIndividualMap.get(dataValue);
                    assayFileSampleIndividualSet.add(materialNodeIndividual);

                }
                individualMatrix[row][col] = materialNodeIndividual;
                //material node attributes
                List<ISAMaterialAttribute> attributeList = materialNode.getMaterialAttributes();

                convertMaterialAttributes(materialNodeIndividual, row, materialNodeAndAttributesIndividuals, attributeList);

            } //for each row
        }


        return sampleIndividualMap;
    }

    /***
     * It converts the Characteristics (ISAMaterialAttributes) associated with an ISAMaterialNode
     *
     * @param materialNodeIndividual the individual representing the material node
     * @param row the row number in the assay table for the material node associated with these attributes
     * @param materialNodeAndAttributesIndividuals a map to store the material attributes associated with a material node
     * @param attributeList a list of ISAMaterialAttributes
     */
    private void convertMaterialAttributes(OWLNamedIndividual materialNodeIndividual, int row, Map<String, Set<OWLNamedIndividual>> materialNodeAndAttributesIndividuals, List<ISAMaterialAttribute> attributeList) {
        for(ISAMaterialAttribute attribute: attributeList){

            //column information
            String attributeString = attribute.getName();
            String attributeSource = null;
            String attributeTerm = null;
            String attributeAccession = null;

            if (attributeString.contains("-") && StringUtils.countMatches(attributeString, "-")== 2){
                attributeString = attributeString.substring(attributeString.indexOf("[")+1, attributeString.indexOf("]"));
                String[] parts =  attributeString.split("-");

                attributeSource = parts[0];
                attributeTerm = parts[1];
                attributeAccession = parts[2];

            }else{

                attributeTerm = attributeString.substring(attributeString.indexOf("[")+1, attributeString.indexOf("]"));
            }

            //row information
            String attributeDataValue = data[row][attribute.getIndex()].toString();

            if (attributeDataValue!=null && !attributeDataValue.equals("")){

                OWLNamedIndividual materialAttributeIndividual = materialAttributeIndividualMap.get(attributeDataValue);
                if (materialAttributeIndividual == null)
                    materialAttributeIndividual = ISA2OWL.createIndividual(GeneralFieldTypes.CHARACTERISTIC.toString(), attributeDataValue, attributeTerm);

                materialAttributeIndividualMap.put(attributeDataValue, materialAttributeIndividual);

                individualMatrix[row][attribute.getIndex()] = materialAttributeIndividual;

                //the column is annotated with an ontology
                if (attributeSource!=null && attributeAccession!=null){
                    if (isOrganism(attributeSource, attributeAccession))  {
                        ISA2OWL.findOntologyTermAndAddClassAssertion(attributeSource, attributeAccession, materialNodeIndividual);

                    } else {
                        ISA2OWL.findOntologyTermAndAddClassAssertion(attributeSource, attributeAccession, materialAttributeIndividual);
                    }
                }

                //deal with the attribute
                String source = OntologyManager.getOntologyTermSource(attributeDataValue);
                String accession = OntologyManager.getOntologyTermAccession(attributeDataValue);

                //the attribute is annotated
                if (source!=null && accession!=null){

                    if (isOrganism(source, accession)){
                        ISA2OWL.findOntologyTermAndAddClassAssertion(source, accession, materialNodeIndividual);
                    } else {
                        ISA2OWL.findOntologyTermAndAddClassAssertion(source, accession, materialAttributeIndividual);
                    }
                }

                Set<OWLNamedIndividual> materialAttributesSet = materialNodeAndAttributesIndividuals.get(GeneralFieldTypes.CHARACTERISTIC.toString());
                if (materialAttributesSet==null)
                    materialAttributesSet = new HashSet<OWLNamedIndividual>();

                materialAttributesSet.add(materialAttributeIndividual);
                materialNodeAndAttributesIndividuals.put(GeneralFieldTypes.CHARACTERISTIC.toString(), materialAttributesSet);

            }else{
                System.err.println("attributeDataValue is null or empty!");
            }

        } //for attribute

        //convert properties per each attribute
        Map<String, List<Pair<IRI,String>>> materialNodePropertyMapping = ISA2OWL.mapping.getMaterialNodePropertyMappings();
        ISA2OWL.convertPropertiesMultipleIndividuals(materialNodePropertyMapping, materialNodeAndAttributesIndividuals);
    }

    private void convertGroups(OWLNamedIndividual studyDesignIndividual, Map<String, OWLNamedIndividual> sampleIndividualMap){
        //Treatment groups
        Map<String, Set<String>> groups = graphParser.getGroups();

        for(String group : groups.keySet()){

            Set<String> elements = groups.get(group);

            OWLNamedIndividual groupIndividual = ISA2OWL.createIndividual(ExtendedISASyntax.STUDY_GROUP, group);

            //group membership
            for(String element: elements){
                OWLNamedIndividual memberIndividual = sampleIndividualMap.get(element);//ISA2OWL.idIndividualMap.get(element);
                OWLObjectProperty hasMember = ISA2OWL.factory.getOWLObjectProperty(ExtendedOBIVocabulary.HAS_MEMBER.iri);
                OWLObjectPropertyAssertionAxiom axiom = ISA2OWL.factory.getOWLObjectPropertyAssertionAxiom(hasMember, groupIndividual, memberIndividual);
                ISA2OWL.manager.addAxiom(ISA2OWL.ontology, axiom);
            }

            //'study design' denotes 'study group population'
            OWLObjectProperty denotes = ISA2OWL.factory.getOWLObjectProperty(IRI.create(IAO.DENOTES));
            OWLObjectPropertyAssertionAxiom axiom1 = ISA2OWL.factory.getOWLObjectPropertyAssertionAxiom(denotes,studyDesignIndividual, groupIndividual);
            ISA2OWL.manager.addAxiom(ISA2OWL.ontology, axiom1);

//            //group size
//            OWLObjectProperty hasQuality = ISA2OWL.factory.getOWLObjectProperty(ISA2OWL.BFO_HAS_QUALITY_IRI);
//            OWLClass size = ISA2OWL.factory.getOWLClass(IRI.create(ISA2OWL.PATO_SIZE_IRI));
//
//            OWLDataProperty hasMeasurementValue = ISA2OWL.factory.getOWLDataProperty(ISA2OWL.IAO_HAS_MEASUREMENT_VALUE_IRI);
//            OWLLiteral sizeValue = ISA2OWL.factory.getOWLLiteral(elements.size());
//            OWLDataHasValue hasMeasurementValueSizeValue = ISA2OWL.factory.getOWLDataHasValue(hasMeasurementValue, sizeValue);
//
//            OWLObjectIntersectionOf intersectionOf = ISA2OWL.factory.getOWLObjectIntersectionOf(size, hasMeasurementValueSizeValue);
//
//            OWLObjectSomeValuesFrom someSize = ISA2OWL.factory.getOWLObjectSomeValuesFrom(hasQuality,intersectionOf);
//
//            OWLClassAssertionAxiom classAssertionAxiom = ISA2OWL.factory.getOWLClassAssertionAxiom(someSize, groupIndividual);
//            ISA2OWL.manager.addAxiom(ISA2OWL.ontology, classAssertionAxiom);
        }
    }

    private boolean isOrganism(String source, String accession){

        if (source.equals("NCBITaxon") || source.equals("UBERON"))
            return true;

        if (accession.contains("NCBITaxon"))
            return true;

        return false;
    }

    /**
     *
     * Method to convert the factor values.
     *
     * @param materialNodeIndividual an OWLNamedIndividual corresponding to the material node for which the factor values correspond
     * @param factorValues a list of ISAFactorValue objects
     * @param row the row number, an integer, where the material node is described
     */
    private void convertFactorValues(OWLNamedIndividual materialNodeIndividual, List<ISAFactorValue> factorValues, int row){

        //OWLClass factorValueClass = ISA2OWL.factory.getOWLClass(IRI.create(ISA.FACTOR_VALUE));
        OWLDataProperty hasValue = ISA2OWL.factory.getOWLDataProperty(IRI.create(ISA.HAS_VALUE));
        OWLObjectProperty hasFactorValue =  ISA2OWL.factory.getOWLObjectProperty(IRI.create(ISA.HAS_FACTOR_VALUE));

        for(ISAFactorValue factorValue: factorValues){

            //the factor type (or factor name) is what it is found in between the square brackets in Factor Value[]
            String fvType = factorValue.getName();
            ISAUnit fvUnit = factorValue.getUnit();
            int col = factorValue.getIndex();
            String unitData = null;

            //row information
            String factorValueData = data[row][factorValue.getIndex()].toString();
            if (fvUnit!=null)
                unitData = data[row][fvUnit.getIndex()].toString();

//            if (fvUnit!=null)
//                System.out.println("fvUnit="+fvUnit.getName());
//            System.out.println("col="+col);
//            System.out.println("fvType="+fvType);
//            System.out.println("factorValueData="+factorValueData);
//            System.out.println("unitData="+unitData);

            String factorValueLabel = fvType+ factorValueData+ (fvUnit!=null? unitData: "");

            OWLNamedIndividual factorValueIndividual = null;

            if (factorValueIndividuals.get(factorValueLabel)!=null)
                factorValueIndividual = factorValueIndividuals.get(factorValueLabel);
            else {
                factorValueIndividual = ISA2OWL.createIndividual(IRI.create(ISA.FACTOR_VALUE), factorValueLabel);
                factorValueIndividuals.put(factorValueLabel, factorValueIndividual);
            }
            System.out.println("factor value individual="+factorValueIndividual);
            OWLLiteral factorValueLiteral = ISA2OWL.factory.getOWLLiteral(factorValueData);
            OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom = ISA2OWL.factory.getOWLDataPropertyAssertionAxiom(hasValue, factorValueIndividual, factorValueLiteral);
            ISA2OWL.manager.addAxiom(ISA2OWL.ontology, dataPropertyAssertionAxiom);

            OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = ISA2OWL.factory.getOWLObjectPropertyAssertionAxiom(hasFactorValue, materialNodeIndividual, factorValueIndividual);
            ISA2OWL.manager.addAxiom(ISA2OWL.ontology, objectPropertyAssertionAxiom);


        }

    }



}