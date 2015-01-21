package org.isatools.linkedISA.converter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.isatools.isacreator.model.Assay;
import org.isatools.isacreator.model.GeneralFieldTypes;
import org.isatools.isacreator.model.Protocol;
import org.isatools.isacreator.model.StudyDesign;

import org.isatools.graph.model.*;
import org.isatools.graph.model.impl.*;

import org.isatools.graph.parser.GraphParser;
import org.isatools.isacreator.ontologymanager.OntologyManager;
import org.isatools.owl.BFO;
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
 * Converts the assay representation into RDF/OWL, relying on the mapping files.
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class Assay2LinkedConverter {

    private static final Logger log = Logger.getLogger(Assay2LinkedConverter.class);

    public enum AssayTableType { STUDY, ASSAY}
    private GraphParser graphParser = null;
    private Object[][] data = null;
    //a matrix will all the individuals for the data (these are MaterialNodes or ProcessNodes individuals
    private OWLNamedIndividual[][] individualMatrix = null;

    private static boolean PROTOCOL_REF_ALWAYS_NEW = false;

    //the key is a concatenation of all the inputs and all the outputs, plus the process name, if all the inputs and outputs are the same, we don't create a new process individual
    private Map<String, OWLNamedIndividual> processParametersPerfomerDateProcessIndividualMap = new HashMap<String, OWLNamedIndividual>();
    private Map<String, OWLNamedIndividual> processInputProcessIndividualMap = new HashMap<String, OWLNamedIndividual>();
    private Map<String, OWLNamedIndividual> processOutputProcessIndividualMap = new HashMap<String, OWLNamedIndividual>();

    private Map<String, OWLNamedIndividual> materialAttributeIndividualMap = new HashMap<String, OWLNamedIndividual>();
    //<type, <name, individual>>
    private Map<String, Map<String, OWLNamedIndividual>> materialNodeIndividualMap = new HashMap<String, Map<String,OWLNamedIndividual>>();

    //factor value individuals identity is their own name, keep this map to create them only once
    private Map<String, OWLNamedIndividual> factorValueIndividuals = new HashMap<String, OWLNamedIndividual>();
    private Map<String, OWLNamedIndividual> dataNodesIndividuals = new HashMap<String, OWLNamedIndividual>();
    private Set<OWLNamedIndividual> assayFileSampleIndividualSet = null;
    private Map<String, OWLNamedIndividual> parameterNameIndividualMap = new HashMap<String, OWLNamedIndividual>();


    public Assay2LinkedConverter(){
        log.info("Assay2OWLConverter - constructor");
    }

    /***
     *
     * It converts the assay information to RDF
     *
     * @param assay the Assay object to be converted - it is the Assay table from ISAcreator
     * @param att the AssayTableType for the Assay object (either a STUDY or an ASSAY)
     * @param sampleIndividualMap a Map with <sample name, sample individual>
     * @param protocolList the list of Protocols
     * @param protocolIndividualMap a Map for the protocol individuals
     * @param studyDesignIndividual the individual for the study design
     * @param studyIndividual the individual corresponding to the study
     * @param convertGroups true or false, indicating if groups are created or not
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
                                                   Map<String, Set<OWLNamedIndividual>> assayIndividualsForProperties,
                                                   OWLNamedIndividual assayFileIndividual,
                                                   Map<String, OWLNamedIndividual> factorIndividualMap){
        log.debug("CONVERTING ASSAY ---> AssayTableType="+att);
        AssayTableType assayTableType = att;
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
        sampleIndividualMap = convertMaterialNodes(graph, sampleIndividualMap, studyIndividual, factorIndividualMap);

        if (assayTableType == AssayTableType.ASSAY){
            assayIndividualsForProperties.put(ExtendedISASyntax.SAMPLE, assayFileSampleIndividualSet);
        }
        convertDataNodes(graph);

        convertProtocolExecutionNodes(protocolList, protocolIndividualMap, graph, assayTableType);

        if (assayTableType == AssayTableType.ASSAY){

            //Assay Name *
            List<ISANode> assayNodes = graph.getNodes(NodeType.ASSAY_NODE);
            convertProcessNodes(assayNodes, protocolIndividualMap, graph, assayIndividualsForProperties, assayFileIndividual);

            //Data Transformation or Normalization Name
            List<ISANode> processNodes = graph.getNodes(NodeType.PROCESS_NODE);
            convertProcessNodes(processNodes, protocolIndividualMap, graph, assayIndividualsForProperties, assayFileIndividual);
        }

        if (convertGroups){
            LinkedISA.groupsAtStudyLevel = convertGroups(studyDesignIndividual,sampleIndividualMap);
        }
        return sampleIndividualMap;
    }

    /***
     *
     * Convert Assay, Data Transformation and Normalization Nodes
     *
     * For all these nodes, the identity method is the name of the node use in the ISA-TAB row.
     *
     * @param protocolIndividualMap a Map with protocol individuals
     * @param graph the Graph from the isa-graphparser
     * @param assayIndividualsForProperties a set of OWL individuals to be used for generating the properties
     */
    private void convertProcessNodes(List<ISANode> processNodes,
                                     Map<String,OWLNamedIndividual> protocolIndividualMap,
                                    Graph graph, Map<String,
                                    Set<OWLNamedIndividual>> assayIndividualsForProperties,
                                    OWLNamedIndividual assayFileIndividual) {

        //used to avoid repetitions of processNodeIndividuals
        Map<String, OWLNamedIndividual> processNodeIndividuals = new HashMap<String, OWLNamedIndividual>();

        for(ISANode node: processNodes){
            ProcessNode processNode = (ProcessNode) node;
            int processCol = processNode.getIndex();

            for(int processRow=1; processRow < data.length; processRow++){

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

                OWLNamedIndividual processIndividual = processNodeIndividuals.get(processNodeValue);
                String individualType = null;

                if (processIndividual==null){

                    if (processNode.getName().startsWith(ExtendedISASyntax.DATA_TRANSFORMATION.toString()))
                            individualType = ExtendedISASyntax.DATA_TRANSFORMATION;
                    else if (processNode.getName().startsWith(ExtendedISASyntax.NORMALIZATION_NAME.toString()))
                        individualType = ExtendedISASyntax.NORMALIZATION_NAME;
                    else
                        individualType = ExtendedISASyntax.STUDY_ASSAY;


                    processIndividual = LinkedISA.createIndividual(individualType, processNodeValue);
                 }//if individual is null.

                    processNodeIndividuals.put(processNodeValue, processIndividual);

                    //assay_file describes assay
                    if (individualType==ExtendedISASyntax.STUDY_ASSAY)
                        LinkedISA.createObjectPropertyAssertion(ISA.DESCRIBES, assayFileIndividual, processIndividual);

                    //inputs & outputs
                    //adding inputs and outputs to the assay
                    OWLObjectProperty has_specified_input = LinkedISA.factory.getOWLObjectProperty(IRI.create(OBI.HAS_SPECIFIED_INPUT));
                    ISANode input = processNode.getInputNode();
                    int inputCol = input.getIndex();

                    if (!data[processRow][inputCol].toString().equals("")){

                        if (individualMatrix[processRow][inputCol]==null){
                                System.out.println("individualMatrix[row][inputCol]==null!!!! " + individualMatrix[processRow][inputCol] == null + "  row=" + processRow + " inputCol=" + inputCol);
                        }else{
                                OWLNamedIndividual inputIndividual = individualMatrix[processRow][inputCol];
                                LinkedISA.addObjectPropertyAssertionAxiom(has_specified_input, processIndividual, inputIndividual);
                        }
                    }



                    OWLObjectProperty has_specified_output = LinkedISA.factory.getOWLObjectProperty(IRI.create(OBI.HAS_SPECIFIED_OUTPUT));
                    ISANode output = processNode.getOutputNode();
                    int outputCol = output.getIndex();
                    if (!data[processRow][outputCol].toString().equals("")){

                        if (individualMatrix[processRow][outputCol]!=null){
                            LinkedISA.addObjectPropertyAssertionAxiom(has_specified_output, processIndividual, individualMatrix[processRow][outputCol]);
                        }else{
                                System.out.println("individualMatrix[row][outputCol es null!!!! "+ individualMatrix[processRow][outputCol]+ "  row="+processRow+" outputCol="+outputCol);
                            }
                        } else {
                            System.out.println("the element value is empty");
                    }


                    addComments(processNode, processRow, processIndividual);

                    //realizes o concretizes (executes) associated protocol
                    List<ProtocolExecutionNode> associatedProcessNodes = processNode.getAssociatedProcessNodes();
                    OWLNamedIndividual lastProtocolExecutionIndividual = null;
                    for(ProtocolExecutionNode protocolExecutionNode: associatedProcessNodes){

                        int protocolExecutionColumn = protocolExecutionNode.getIndex();
                        String protocolExecutionName = (String)data[processRow][protocolExecutionColumn];

                        //executes
                        OWLNamedIndividual protocolIndividual = protocolIndividualMap.get(protocolExecutionName);
                        OWLObjectProperty executes = LinkedISA.factory.getOWLObjectProperty(IRI.create(ISA.EXECUTES));
                        LinkedISA.addObjectPropertyAssertionAxiom(executes, processIndividual, protocolIndividual);

                        OWLNamedIndividual protocolExecutionIndividual = individualMatrix[processRow][protocolExecutionColumn];

                        OWLObjectProperty isPrecededBy = LinkedISA.factory.getOWLObjectProperty(IRI.create(BFO.IS_PRECEDED_BY));
                        if (lastProtocolExecutionIndividual!=null)
                            LinkedISA.addObjectPropertyAssertionAxiom(isPrecededBy, protocolExecutionIndividual, lastProtocolExecutionIndividual);

                        lastProtocolExecutionIndividual = protocolExecutionIndividual;
                        OWLObjectProperty has_part = LinkedISA.factory.getOWLObjectProperty(IRI.create(BFO.HAS_PART));
                        LinkedISA.addObjectPropertyAssertionAxiom(has_part, processIndividual, protocolExecutionIndividual);

                        //RULE: if there is only one protocol REF associated with a 'data transformation' or 'normalization' node,
                        //the data transformation can take the same type as the protocol ref
                        if (associatedProcessNodes.size()==1){
                            System.out.println("processIndividual="+processIndividual);
                            System.out.println("protocolExecutionIndividual="+protocolExecutionIndividual);
                            //Set<IRI> protocolClassIRIs = ISA2OWL.individualTypeMap.get(protocolIndividual);

                            if (protocolExecutionIndividual==null)
                                System.out.println("this is null!!!!");

                            OWLSameIndividualAxiom axiom = LinkedISA.factory.getOWLSameIndividualAxiom(processIndividual, protocolExecutionIndividual);
                            LinkedISA.manager.addAxiom(LinkedISA.ontology,axiom);

                        }

                    }//for process node


                assayIndividualsForProperties.put(ExtendedISASyntax.STUDY_ASSAY, Collections.singleton(processIndividual));
                Map<String,List<Pair<IRI, String>>> assayPropertyMappings = LinkedISA.mapping.getAssayPropertyMappings();
                LinkedISA.convertPropertiesMultipleIndividuals(assayPropertyMappings, assayIndividualsForProperties);

            }//for
        }
    }


    /**
     *
     * Converts process nodes.
     *
     * ProcessNodes are either 'Data Transformation' or 'Normalization Name' columns
     *
     *
     * @param graph
     */
    /*
    private void convertProcessNodes(Graph graph) {
        //Process Nodes
        List<ISANode> processNodes = graph.getNodes(NodeType.PROCESS_NODE);

        for(ISANode node: processNodes){

            ProcessNode processNode = (ProcessNode) node;
            //keeping all the individuals relevant for this processNode
            Map<String, OWLNamedIndividual> processNodeIndividuals = new HashMap<String,OWLNamedIndividual>();

            int processCol = processNode.getIndex();

            for(int processRow=1; processRow < data.length; processRow ++){

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


                //build a string with concatenated inputs/outputs to identify different process individuals
                List<ISANode> inputs = processNode.getInputNodes();
                List<ISANode> outputs = processNode.getOutputNodes();
                String inputOutputString = getInputOutputMethodString(processRow, processNodeValue, inputs, outputs);

                OWLNamedIndividual processIndividual = processIndividualMap.get(inputOutputString);

                String individualType = processNode.getName().startsWith(ExtendedISASyntax.DATA_TRANSFORMATION.toString()) ?
                        ExtendedISASyntax.DATA_TRANSFORMATION : ExtendedISASyntax.NORMALIZATION_NAME;

                if (processIndividual==null){
                    processIndividual = ISA2OWL.createIndividual(individualType, processNodeValue);
                    if (processIndividual==null)
                        continue;
                    processIndividualMap.put(inputOutputString, processIndividual);
                }

                processNodeIndividuals.put(individualType, processIndividual);



                List<ProtocolExecutionNode> associatedNodes = processNode.getAssociatedProcessNodes();

                //processIndividual has_part protocolREFindividual
                for(ProtocolExecutionNode protocolExecutionNode: associatedNodes){
                    int penIndex = protocolExecutionNode.getIndex();
                    OWLNamedIndividual penIndividual = individualMatrix[processRow][penIndex];
                    if (penIndividual!=null)
                        ISA2OWL.addObjectPropertyAssertionAxiom(ISA2OWL.factory.getOWLObjectProperty(IRI.create(BFO.HAS_PART)), processIndividual, penIndividual);
                }

                //RULE: if there is only one protocol REF associated with a 'data transformation' or 'normalization' node,
                //the data transformation can take the same type as the protocol ref
                if (associatedNodes.size()==1){



                }

                //inputs & outputs
                OWLObjectProperty has_specified_input = ISA2OWL.factory.getOWLObjectProperty(IRI.create(OBI.HAS_SPECIFIED_INPUT));
                for(ISANode input: inputs){
                    int inputCol = input.getIndex();

                    if (!data[processRow][inputCol].toString().equals("")){

                        if (individualMatrix[processRow][inputCol]==null){
                            System.out.println("individualMatrix[row][inputCol]==null!!!! " + individualMatrix[processRow][inputCol] == null + "  row=" + processRow + " inputCol=" + inputCol);
                        }else{
                            processNodeIndividuals.put(individualType, individualMatrix[processRow][inputCol]);
                            OWLNamedIndividual inputIndividual = individualMatrix[processRow][inputCol];
                            if (inputIndividual != null )
                                ISA2OWL.addObjectPropertyAssertionAxiom(has_specified_input, processIndividual, inputIndividual);
                        }
                    }
                }//for inputs

                OWLObjectProperty has_specified_output = ISA2OWL.factory.getOWLObjectProperty(IRI.create(OBI.HAS_SPECIFIED_OUTPUT));
                for(ISANode output: outputs){
                    int outputCol = output.getIndex();
                    if (!data[processRow][outputCol].toString().equals("")){

                        if (individualMatrix[processRow][outputCol]!=null){
                            ISA2OWL.addObjectPropertyAssertionAxiom(has_specified_output, processIndividual, individualMatrix[processRow][outputCol]);
                        }else{
                            System.out.println("individualMatrix[row][outputCol es null!!!! "+ individualMatrix[processRow][outputCol]+ "  row="+processRow+" outputCol="+outputCol);
                        }

                    }

                }//for outputs

                addComments(processNode, processRow, processIndividual);

                //TODO revise these mappings conversion
                Map<String, List<Pair<IRI,String>>> protocolREFmapping = ISA2OWL.mapping.getProtocolREFMappings();
                ISA2OWL.convertProperties(protocolREFmapping, processNodeIndividuals);


            } //processRow
        }
    }
    */

    /**
     *
     * Converts ProtocolExecutionNodes. (Among the process nodes, only the ProtocolExecutions will have an associated declared protocol.)
     *
     * Uniqueness of ProtocolExecutionNodes: given by analysing inputs/outputs/parameters
     *
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

            for(int processRow=1; processRow < data.length; processRow ++){

                //keeping all the individuals relevant for this processNode
                Map<String, Set<OWLNamedIndividual>> protocolREFIndividuals = new HashMap<String,Set<OWLNamedIndividual>>();

                String protocolExecutionValue = null;
                if (processCol==-1){
                    protocolExecutionValue = processNode.toShortString();
                } else {
                    protocolExecutionValue = (data[processRow][processCol]).toString();
                }

                if (protocolExecutionValue.equals("")){
                    log.debug("ProtocolExecutionValue is empty!!!");
                    continue;
                } else {
                    log.debug("ProtocolExecutionValue = " + protocolExecutionValue);
                }
                Protocol protocol = protocolMap.get(protocolExecutionValue);

                OWLNamedIndividual protocolIndividual = protocolIndividualMap.get(protocolExecutionValue);

                if (protocolIndividual==null) {
                    System.err.println("Protocol "+protocolExecutionValue+" must already exist");
                } else {

                    addComments(processNode, processRow, protocolIndividual);

                    //adding Study Protocol
                    Set<OWLNamedIndividual> set = protocolREFIndividuals.get(ExtendedISASyntax.STUDY_PROTOCOL);
                    if (set==null)
                        set = new HashSet<OWLNamedIndividual>();
                    set.add(protocolIndividual);
                    protocolREFIndividuals.put(ExtendedISASyntax.STUDY_PROTOCOL, set);
                }


                //build a string with concatenated inputs/outputs/process/parameters to identify different process individuals
                String inputOutputString = getInputOutputMethodString(processRow, protocolExecutionValue, processNode);

                //if there are no inputs and outputs, do not create the processIndividual
                if (protocolExecutionValue.equals(inputOutputString))
                    continue;


                //input & output values
                ISANode input = processNode.getInputNode();
                int inputCol = input.getIndex();
                String inputValue = data[processRow][inputCol].toString();

                ISANode output =  processNode.getOutputNode();
                String outputValue = null;
                int outputCol = -1;
                if (output!=null){
                    outputCol = output.getIndex();
                    outputValue = data[processRow][outputCol].toString();
                } else {
                    outputValue = "";
                }


                //check parameters + performer + date + comments, if they are different, create different individuals
                String processParametersPerformerDateString = getProcessParametersPerformerDateString(processRow, processNode);

                OWLNamedIndividual processIndividual = null;
                //get all the process individuals with the same name, parameters, performer and date
                if (!PROTOCOL_REF_ALWAYS_NEW)
                    processIndividual = processParametersPerfomerDateProcessIndividualMap.get(processParametersPerformerDateString);

                if (processIndividual==null){
                        //create processIndividual
                        processIndividual = LinkedISA.createIndividual(assayTableType == AssayTableType.STUDY ?
                                ExtendedISASyntax.STUDY_PROTOCOL_REF : ExtendedISASyntax.ASSAY_PROTOCOL_REF, protocolExecutionValue);
                    if (!PROTOCOL_REF_ALWAYS_NEW)
                        processParametersPerfomerDateProcessIndividualMap.put(processParametersPerformerDateString,processIndividual);
                }

                individualMatrix[processRow][processCol] = processIndividual;

                //adding processIndividual to protocolREFIndividuals
                Set<OWLNamedIndividual> set = protocolREFIndividuals.get(assayTableType == AssayTableType.STUDY ? ExtendedISASyntax.STUDY_PROTOCOL_REF : ExtendedISASyntax.ASSAY_PROTOCOL_REF);
                if (set==null)
                    set = new HashSet<OWLNamedIndividual>();
                set.add(processIndividual);
                protocolREFIndividuals.put(assayTableType == AssayTableType.STUDY ?
                        ExtendedISASyntax.STUDY_PROTOCOL_REF : ExtendedISASyntax.ASSAY_PROTOCOL_REF, set);

                if (protocol!=null && protocol.getProtocolType()!=null){
                    LinkedISA.addComment(protocol.getProtocolType(), processIndividual.getIRI());
                } else{
                    System.out.println("Protocol type is null for protocol "+protocol);
                }

                if (protocolIndividual != null){

                    //TODO add protocolIndividual to protocolREFIndividuals

                    OWLObjectProperty executes = LinkedISA.factory.getOWLObjectProperty(IRI.create(ISA.EXECUTES));
                    OWLObjectPropertyAssertionAxiom axiom1 = LinkedISA.factory.getOWLObjectPropertyAssertionAxiom(executes,processIndividual, protocolIndividual);
                    LinkedISA.manager.addAxiom(LinkedISA.ontology, axiom1);

                    //use term source and term accession to declare a more specific type for the process node
                    if (protocol.getProtocolTypeTermAccession()!=null && !protocol.getProtocolTypeTermAccession().equals("")
                            && protocol.getProtocolTypeTermSourceRef()!=null && !protocol.getProtocolTypeTermSourceRef().equals("")){

                        LinkedISA.findOntologyTermAndAddClassAssertion(protocol.getProtocolTypeTermSourceRef(), protocol.getProtocolTypeTermAccession(), processIndividual);

                    }//process node attributes not null
                }

                //adding input
                if (!inputValue.equals("")){
                    set = protocolREFIndividuals.get(assayTableType == AssayTableType.STUDY ?
                            ExtendedISASyntax.STUDY_PROTOCOL_REF_INPUT: ExtendedISASyntax.ASSAY_PROTOCOL_REF_INPUT);

                    if (set==null)
                        set = new HashSet<OWLNamedIndividual>();

                    set.add(individualMatrix[processRow][inputCol]);

                    protocolREFIndividuals.put(assayTableType == AssayTableType.STUDY ?
                            ExtendedISASyntax.STUDY_PROTOCOL_REF_INPUT : ExtendedISASyntax.ASSAY_PROTOCOL_REF_INPUT, set);
                }

                if (!outputValue.equals("")){

                    set = protocolREFIndividuals.get(assayTableType == AssayTableType.STUDY ?
                            ExtendedISASyntax.STUDY_PROTOCOL_REF_OUTPUT: ExtendedISASyntax.ASSAY_PROTOCOL_REF_OUTPUT);

                    if (set==null)
                        set = new HashSet<OWLNamedIndividual>();

                    set.add(individualMatrix[processRow][outputCol]);

                    protocolREFIndividuals.put(assayTableType == AssayTableType.STUDY ?
                            ExtendedISASyntax.STUDY_PROTOCOL_REF_OUTPUT: ExtendedISASyntax.ASSAY_PROTOCOL_REF_OUTPUT, set);

                }

                //parameters
                System.out.println("=======>  processNode " +processIndividual.getIRI() + " processRow " + processRow);
                for(ProcessParameter parameter: processNode.getParameters()){
                    int parameterCol = parameter.getIndex();

                    String parameterLabel = data[processRow][parameterCol].toString();

                    if (!parameterLabel.equals("")){

                        OWLNamedIndividual parameterIndividual = null;

                        if (parameterNameIndividualMap.containsKey(parameterLabel)) {
                            parameterIndividual = parameterNameIndividualMap.get(parameterLabel);
                        } else {
                            parameterIndividual = LinkedISA.createIndividual(GeneralFieldTypes.PARAMETER_VALUE.name, parameterLabel);
                        }

                        individualMatrix[processRow][parameterCol] = parameterIndividual;
                        parameterNameIndividualMap.put(parameterLabel, parameterIndividual);

                        set = protocolREFIndividuals.get(GeneralFieldTypes.PARAMETER_VALUE.name);

                        if (set==null)
                            set = new HashSet<OWLNamedIndividual>();

                        set.add(parameterIndividual);

                        protocolREFIndividuals.put(GeneralFieldTypes.PARAMETER_VALUE.name, set);
                    }

                }

                if ( protocolREFIndividuals.get(GeneralFieldTypes.PARAMETER_VALUE.name) !=null)
                    for(OWLNamedIndividual param : protocolREFIndividuals.get(GeneralFieldTypes.PARAMETER_VALUE.name)){
                        System.out.println(" param "+ param.getIRI());
                    }


                Map<String, List<Pair<IRI,String>>> protocolREFmapping = LinkedISA.mapping.getProtocolREFMappings();
                LinkedISA.convertPropertiesMultipleIndividuals(protocolREFmapping, protocolREFIndividuals);


            }//processRow

        }//processNode
    }

    /**
     *
     * Converts the DATA_NODES from the graph (all the 'File' elements). The file name works as a key and only one individual is created for each file.
     *
     *
     * @param graph
     */
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
                dataNodeIndividual = dataNodesIndividuals.get(dataValue);
                if (dataNodeIndividual == null){
                    dataNodeIndividual = LinkedISA.createIndividual(dataNode.getName(), dataValue, dataValue);
                    dataNodesIndividuals.put(dataValue, dataNodeIndividual);
                }
                individualMatrix[row][col] = dataNodeIndividual;

                addComments(dataNode, row, dataNodeIndividual);
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
    private Map<String, OWLNamedIndividual> convertMaterialNodes(Graph graph,
                                                                 Map<String, OWLNamedIndividual> sampleIndividualMap,
                                                                 OWLNamedIndividual studyIndividual,
                                                                 Map<String, OWLNamedIndividual> factorIndividualMap) {
        OWLNamedIndividual materialNodeIndividual;

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
                    Map<String, OWLNamedIndividual> namedIndividualMap = materialNodeIndividualMap.get(materialNode.getName());
                    if (namedIndividualMap!=null) {
                        materialNodeIndividual = namedIndividualMap.get(dataValue);
                    } else {
                        materialNodeIndividual = null;
                        namedIndividualMap = new HashMap<String, OWLNamedIndividual>();
                    }

                    if (materialNodeIndividual==null) {
                        materialNodeIndividual = LinkedISA.createIndividual(materialNode.getMaterialNodeType(), dataValue + " " + materialNode.getMaterialNodeType(), materialNode.getMaterialNodeType());

                        namedIndividualMap.put(dataValue, materialNodeIndividual);
                        materialNodeIndividualMap.put(materialNode.getName(), namedIndividualMap);
                    }

                    addComments(materialNode, row, materialNodeIndividual);

                    if (materialNode.getMaterialNodeType() == ExtendedISASyntax.SAMPLE ){//&& sampleIndividualMapWasNull){
                        sampleIndividualMap.put(dataValue, materialNodeIndividual);
                    }

                    Set<OWLNamedIndividual> set2 = new HashSet();
                    set2.add(materialNodeIndividual);
                    materialNodeAndAttributesIndividuals.put(materialNode.getMaterialNodeType(), set2);

                    //Material Node Annotation
                    String purl = OntologyManager.getOntologyTermURI(dataValue);
                    if (purl!=null && !purl.equals("")){
                        log.debug("If there is a PURL, use it! "+purl);
                    }else{

                        String source = OntologyManager.getOntologyTermSource(dataValue);
                        String accession = OntologyManager.getOntologyTermAccession(dataValue);
                        LinkedISA.findOntologyTermAndAddClassAssertion(source, accession, materialNodeIndividual);

                    }

                    //Material Node Name
                    OWLNamedIndividual materialNodeIndividualName = LinkedISA.createIndividual(GeneralFieldTypes.SOURCE_NAME.toString(), dataValue);
                    Set<OWLNamedIndividual> set3 = new HashSet();
                    set3.add(materialNodeIndividualName);
                    materialNodeAndAttributesIndividuals.put(GeneralFieldTypes.SOURCE_NAME.toString(), set3);


                } else {   //createIndividualMaterialNode is false
                    materialNodeIndividual = sampleIndividualMap.get(dataValue);
                    assayFileSampleIndividualSet.add(materialNodeIndividual);

                }
                individualMatrix[row][col] = materialNodeIndividual;

                //adding factor values (so the factors may come from the study sample file or the assay file
                if (materialNode instanceof SampleNode){
                    List<ISAFactorValue> factorValues = ((SampleNode) materialNode).getFactorValues();
                    convertFactorValues(materialNodeIndividual, factorValues, row, factorIndividualMap);
                }

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
                    materialAttributeIndividual = LinkedISA.createIndividual(GeneralFieldTypes.CHARACTERISTIC.toString(), attributeDataValue, attributeTerm);

                materialAttributeIndividualMap.put(attributeDataValue, materialAttributeIndividual);

                individualMatrix[row][attribute.getIndex()] = materialAttributeIndividual;

                //the column is annotated with an ontology
                if (attributeSource!=null && attributeAccession!=null){
                    if (isOrganism(attributeSource, attributeAccession))  {
                        LinkedISA.findOntologyTermAndAddClassAssertion(attributeSource, attributeAccession, materialNodeIndividual);

                    } else {
                        LinkedISA.findOntologyTermAndAddClassAssertion(attributeSource, attributeAccession, materialAttributeIndividual);
                    }
                }

                //deal with the attribute
                String source = OntologyManager.getOntologyTermSource(attributeDataValue);
                String accession = OntologyManager.getOntologyTermAccession(attributeDataValue);

                //the attribute is annotated
                if (source!=null && accession!=null){

                    if (isOrganism(source, accession)){
                        LinkedISA.findOntologyTermAndAddClassAssertion(source, accession, materialNodeIndividual);
                    } else {
                        LinkedISA.findOntologyTermAndAddClassAssertion(source, accession, materialAttributeIndividual);
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
        Map<String, List<Pair<IRI,String>>> materialNodePropertyMapping = LinkedISA.mapping.getMaterialNodePropertyMappings();
        LinkedISA.convertPropertiesMultipleIndividuals(materialNodePropertyMapping, materialNodeAndAttributesIndividuals);
    }

    /**
     *
     * Method to create Study Groups.
     *
     * @param studyDesignIndividual the individual for 'Study Design Type' ISA element
     * @param sampleIndividualMap a Map with all the created sample individuals
     * @return true if groups were created, false otherwise
     */
    private boolean convertGroups(OWLNamedIndividual studyDesignIndividual, Map<String, OWLNamedIndividual> sampleIndividualMap){
        //Treatment groups
        Map<String, StudyGroup> groups = graphParser.getGroups();
        boolean groupsCreated = false;

        for(String group : groups.keySet()){

            Map<String, Set<OWLNamedIndividual>> individualsForProperties = new HashMap<String, Set<OWLNamedIndividual>>();
            individualsForProperties.put(StudyDesign.STUDY_DESIGN_TYPE, Collections.singleton(studyDesignIndividual));

            OWLNamedIndividual groupIndividual = LinkedISA.createIndividual(ExtendedISASyntax.STUDY_GROUP, group);
            individualsForProperties.put(ExtendedISASyntax.STUDY_GROUP, Collections.singleton(groupIndividual));

            StudyGroup studyGroup = groups.get(group);
            Set<String> elements = studyGroup.getGroupMembers();

            //group membership
            for(String element: elements){
                OWLNamedIndividual memberIndividual = sampleIndividualMap.get(element);

                if (memberIndividual==null)
                    continue;

                Set<OWLNamedIndividual> set = individualsForProperties.get(ExtendedISASyntax.SAMPLE);
                if (set==null)
                    set = new HashSet<OWLNamedIndividual>();
                set.add(memberIndividual);
                individualsForProperties.put(ExtendedISASyntax.SAMPLE, set);

            }

            Map<String, String> factorFactorValue = studyGroup.getGroupDefinition();
            for(String factor: factorFactorValue.keySet()) {
                String factorValue = factorFactorValue.get(factor);
                OWLNamedIndividual factorValueIndividual = factorValueIndividuals.get(factorValue);
                if (factorValueIndividual==null)
                    System.err.println("Theres is no individual for the factor value "+factorValue+"!");


                Set<OWLNamedIndividual> set = individualsForProperties.get(GeneralFieldTypes.FACTOR_VALUE.name);
                if (set==null)
                    set = new HashSet<OWLNamedIndividual>();
                set.add(factorValueIndividual);
                individualsForProperties.put(GeneralFieldTypes.FACTOR_VALUE.name, set);
            }

            groupsCreated = true;

            //convert properties per each attribute
            Map<String, List<Pair<IRI,String>>> materialNodePropertyMapping = LinkedISA.mapping.getGroupPropertyMappings();
            LinkedISA.convertPropertiesMultipleIndividuals(materialNodePropertyMapping, individualsForProperties);

        }
        return groupsCreated;
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
    private void convertFactorValues(OWLNamedIndividual materialNodeIndividual, List<ISAFactorValue> factorValues, int row, Map<String, OWLNamedIndividual> factorIndividualMap){

        for(ISAFactorValue factorValue: factorValues){

            Map<String, Set<OWLNamedIndividual>> factorIndividualsForProperties = new HashMap<String, Set<OWLNamedIndividual>>();
            factorIndividualsForProperties.put(ExtendedISASyntax.SAMPLE, Collections.singleton(materialNodeIndividual));

            //fvType is 'Factor Value[factor]'
            String fvType = factorValue.getName();
            String factorName = fvType.substring(fvType.indexOf('[')+1, fvType.indexOf(']'));
            OWLNamedIndividual factorIndividual = factorIndividualMap.get(factorName);
            factorIndividualsForProperties.put(ExtendedISASyntax.STUDY_FACTOR, Collections.singleton(factorIndividual));

            //the unit value associated with the factor
            ISAUnit fvUnit = factorValue.getUnit();
            int col = factorValue.getIndex();
            String unitData = null;

            //row information
            String factorValueData = data[row][factorValue.getIndex()].toString();
            if (fvUnit!=null)
                unitData = data[row][fvUnit.getIndex()].toString();

            String factorValueLabel = factorValueData+ (fvUnit!=null? unitData: "");

            OWLNamedIndividual factorValueIndividual = null;

            if (factorValueIndividuals.get(factorValueLabel)!=null)
                factorValueIndividual = factorValueIndividuals.get(factorValueLabel);
            else {
                factorValueIndividual = LinkedISA.createIndividual(GeneralFieldTypes.FACTOR_VALUE.name, factorValueLabel);
                factorValueIndividuals.put(factorValueLabel, factorValueIndividual);
            }

            //include individual for properties
            Set<OWLNamedIndividual> set = factorIndividualsForProperties.get(GeneralFieldTypes.FACTOR_VALUE.name);
            if (set==null){
                set = new HashSet<OWLNamedIndividual>();
            }
            set.add(factorValueIndividual);
            factorIndividualsForProperties.put(GeneralFieldTypes.FACTOR_VALUE.name, set);

            Map<String,List<Pair<IRI, String>>> factorPropertyMappings = LinkedISA.mapping.getFactorPropertyMappings();
            LinkedISA.convertPropertiesMultipleIndividuals(factorPropertyMappings, factorIndividualsForProperties);

        }

    }


    //Utility methods

    /**
     * Creates a string concatenating the inputs/outputs/process name for a ProcessNode
     * to be used as an identity method (to identify if two process nodes are different or not)
     *
     *
     * @param processRow
     * @param processNodeValue
     * @param processNode
     * @return
     */
    private String getInputOutputMethodString(int processRow, String processNodeValue, ProcessNode processNode) {
        StringBuffer buffer = new StringBuffer();

        buffer.append(processNodeValue);

        ISANode input = processNode.getInputNode();
        if (input!=null) {
            int inputCol = input.getIndex();
            if (!data[processRow][inputCol].toString().equals("")){
                buffer.append(data[processRow][inputCol].toString());
            }
        }

        ISANode output = processNode.getOutputNode();
        if (output!=null){
            int outputCol = output.getIndex();
            if (!data[processRow][outputCol].toString().equals("")){
                buffer.append(data[processRow][outputCol].toString());
            }
        }

        return buffer.toString();
    }



    /**
     *
     * Creates a string concatenating the inputs/outputs/process/parameters for ProtocolExecutionNodes
     * It is used as an identity method for ProtocolREFs.
     *
     * @param processRow integer indicating the table row where the process is defined

     * @return
     */
    private String getProcessParametersPerformerDateString(int processRow, ProtocolExecutionNode processNode){
        StringBuffer buffer = new StringBuffer();

        int processCol = processNode.getIndex();
        buffer.append(data[processRow][processCol].toString());

        List<ProcessParameter> parameters = processNode.getParameters();

        for(ProcessParameter parameter: parameters){
            int parameterCol = parameter.getIndex();
            if (!data[processRow][parameterCol].toString().equals("")){
                buffer.append(data[processRow][parameterCol].toString());
            }
        }

        //perfomer
        Performer performer = processNode.getPerformer();
        if (performer!=null) {
            int perfomerIndex = performer.getIndex();
            String perfomerString = data[processRow][perfomerIndex].toString();
            buffer.append(perfomerString);
        }

        //date
        org.isatools.graph.model.impl.Date date = processNode.getDate();
        if (date!=null){
            int dateIndex = date.getIndex();
            String dateString = data[processRow][dateIndex].toString();
            buffer.append(dateString);
        }

        return buffer.toString();
    }


    /**
     * It add the comments from the NodeWithComments to the corresponding individual for that node.
     *
     * @param nodeWithComments object of NodeWithComments type
     * @param row integer denoting the row where the data is
     * @param individual an OWL individual corresponding to the node
     */
    private void addComments(NodeWithComments nodeWithComments, int row, OWLNamedIndividual individual){
        for(CommentNode comment: nodeWithComments.getComments()){
            int comment_col = comment.getIndex();
            LinkedISA.addComment(comment.getName() + ":" + data[row][comment_col], individual.getIRI());
        }

    }


}