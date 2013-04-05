package org.isatools.isa2owl.converter;

import org.apache.log4j.Logger;
import org.isatools.graph.model.*;
import org.isatools.graph.parser.GraphParser;
import org.isatools.isacreator.model.Assay;
import org.isatools.isacreator.model.GeneralFieldTypes;
import org.isatools.isacreator.model.Protocol;
import org.isatools.isacreator.ontologymanager.OntologyManager;
import org.isatools.owl.ReasonerService;
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

    private GraphParser graphParser = null;
    private Object[][] data = null;

    //a matrix will all the individuals for the data (these are MaterialNodes or ProcessNodes individuals
    private OWLNamedIndividual[][] individualMatrix = null;

    private Map<MaterialNode, Map<String,OWLNamedIndividual>> materialNodeIndividualMap = new HashMap<MaterialNode, Map<String,OWLNamedIndividual>>();
    private Map<Integer, OWLNamedIndividual> processIndividualMap = new HashMap<Integer, OWLNamedIndividual>();


    public Assay2OWLConverter(){
        log.info("Assay2OWLConverter - constructor");
    }

    public void convert(Assay assay, AssayTableType assayTableType, List<Protocol> protocolList, Map<String, OWLNamedIndividual> protocolIndividualMap, OWLNamedIndividual studyDesignIndividual, boolean convertGroups){

        System.out.println("CONVERTING ASSAY");

        data = assay.getAssayDataMatrix();

        individualMatrix = new OWLNamedIndividual[data.length][data[0].length];

        graphParser = new GraphParser(assay.getAssayDataMatrix());
        graphParser.parse();
        Graph graph = graphParser.getGraph();

        convertMaterialNodes(graph);
        convertDataNodes(graph);

        //TODO remove after debugging
        for(int i=0; i<data.length; i++){
            for (int j=0; j< data[i].length; j++){
                System.out.println("individualMatrix["+i+","+j+"]="+individualMatrix[i][j]);
            }
        }

        convertAssayNodes(protocolIndividualMap, graph);
        convertProcessNodes(protocolList, protocolIndividualMap, graph, assayTableType);


        if (convertGroups){
            convertGroups(studyDesignIndividual);
        }
    }

    private void convertAssayNodes(Map<String, OWLNamedIndividual> protocolIndividualMap, Graph graph) {
        //assay individuals
        List<Node> assayNodes = graph.getNodes(NodeType.ASSAY_NODE);
        for(Node node: assayNodes){
            AssayNode assayNode = (AssayNode) node;

            int col = assayNode.getIndex();
            for(int row=1; row < data.length; row++){

                String dataValue = (String) data[row][col];
                if (dataValue.equals(""))
                    continue;

                OWLNamedIndividual assayIndividual = ISA2OWL.createIndividual(ExtendedISASyntax.STUDY_ASSAY, dataValue);

                //realizes o concretizes (executes) associated protocol
                //TODO fix this
                ProcessNode processNode = ((AssayNode)assayNode).getAssociatedProcessNodes().get(0);
                int protocolColumn = processNode.getIndex();
                String protocolName = (String)data[row][protocolColumn];

                OWLNamedIndividual protocolIndividual = protocolIndividualMap.get(protocolName);
                OWLObjectProperty executes = ISA2OWL.factory.getOWLObjectProperty(ExtendedOBIVocabulary.EXECUTES.iri);
                OWLObjectPropertyAssertionAxiom axiom1 = ISA2OWL.factory.getOWLObjectPropertyAssertionAxiom(executes,assayIndividual, protocolIndividual);
                ISA2OWL.manager.addAxiom(ISA2OWL.ontology, axiom1);

                //inputs & outputs
                //adding inputs and outputs to the assay
                OWLObjectProperty has_specified_input = ISA2OWL.factory.getOWLObjectProperty(OBIVocabulary.HAS_SPECIFIED_INPUT.iri);
                List<Node> inputs = assayNode.getInputNodes();
                for(Node input: inputs){
                    int inputCol = input.getIndex();
                    System.out.println("inputCol="+inputCol);

                    if (!data[row][inputCol].toString().equals("")){
                        OWLObjectPropertyAssertionAxiom axiom2 = ISA2OWL.factory.getOWLObjectPropertyAssertionAxiom(has_specified_input, assayIndividual, individualMatrix[row][inputCol]);
                        ISA2OWL.manager.addAxiom(ISA2OWL.ontology, axiom2);
                    }

                    //}//for row

                }//for inputs


                OWLObjectProperty has_specified_output = ISA2OWL.factory.getOWLObjectProperty(OBIVocabulary.HAS_SPECIFIED_OUTPUT.iri);
                List<Node> outputs = assayNode.getOutputNodes();
                for(Node output: outputs){
                    int outputCol = output.getIndex();
                    System.out.println("row="+row);
                    System.out.println("outputCol="+outputCol);
                    if (!data[row][outputCol].toString().equals("")){
                        OWLObjectPropertyAssertionAxiom axiom3 = ISA2OWL.factory.getOWLObjectPropertyAssertionAxiom(has_specified_output, assayIndividual, individualMatrix[row][outputCol]);
                        ISA2OWL.manager.addAxiom(ISA2OWL.ontology, axiom3);
                    }

                    //}//for row

                }//for inputs
            }
        }
    }

    /**
     *
     *
     * @param protocolIndividualMap
     * @param graph
     * @param assayTableType
     */
    private void convertProcessNodes(List<Protocol> protocolList, Map<String, OWLNamedIndividual> protocolIndividualMap, Graph graph, AssayTableType assayTableType) {
        //Process Nodes
        List<Node> processNodes = graph.getNodes(NodeType.PROCESS_NODE);

        Map<String, Protocol> protocolMap = new HashMap<String, Protocol>();

        for(Protocol protocol: protocolList){
            protocolMap.put(protocol.getProtocolName(), protocol);
        }

        System.out.println("protocolMap = "+protocolMap);

        for(Node node: processNodes){

            ProcessNode processNode = (ProcessNode) node;

            int processCol = processNode.getIndex();
            System.out.println("processNode.getName()="+node.getName()+" processCol="+processCol);

            //keeping all the individuals relevant for this processNode
            Map<String, OWLNamedIndividual> protocolREFIndividuals = new HashMap<String,OWLNamedIndividual>();


            for(int processRow=1; processRow < data.length; processRow ++){

                System.out.println("processRow="+processRow);

                String processName = (data[processRow][processCol]).toString();

                System.out.println("processName="+processName);
                Protocol protocol = protocolMap.get(processName);

                OWLNamedIndividual protocolIndividual = protocolIndividualMap.get(processName);

                if (protocolIndividual==null)
                    System.err.println("Protocol "+processName+" must already exist");

                //adding Study Protocol
                protocolREFIndividuals.put(ExtendedISASyntax.STUDY_PROTOCOL, protocolIndividual);

                OWLNamedIndividual processIndividual = processIndividualMap.get(processCol);

                System.out.println("processIndividual="+processIndividual);

                //material processing as the execution of the protocol
                if (processIndividual==null){
                    System.out.println("creating the process individual -"+ processName+" - this should happen only once");
                    processIndividual = ISA2OWL.createIndividual(assayTableType == AssayTableType.STUDY ? ExtendedISASyntax.STUDY_PROTOCOL_REF : ExtendedISASyntax.ASSAY_PROTOCOL_REF, processName);
                    processIndividualMap.put(processCol, processIndividual);
                }

                ISA2OWL.addComment(protocol.getProtocolType(), processIndividual.getIRI());

                OWLObjectProperty executes = ISA2OWL.factory.getOWLObjectProperty(ExtendedOBIVocabulary.EXECUTES.iri);
                OWLObjectPropertyAssertionAxiom axiom1 = ISA2OWL.factory.getOWLObjectPropertyAssertionAxiom(executes,processIndividual, protocolIndividual);
                ISA2OWL.manager.addAxiom(ISA2OWL.ontology, axiom1);

                protocolREFIndividuals.put(assayTableType == AssayTableType.STUDY ? ExtendedISASyntax.STUDY_PROTOCOL_REF : ExtendedISASyntax.ASSAY_PROTOCOL_REF, processIndividual);

                //Study Protocol Type
                //ISA2OWL.createIndividual(Protocol.PROTOCOL_TYPE, protocol.getProtocolType(), protocolIndividuals);

                //use term source and term accession to declare a more specific type for the process node
                if (protocol.getProtocolTypeTermAccession()!=null && !protocol.getProtocolTypeTermAccession().equals("")
                        && protocol.getProtocolTypeTermSourceRef()!=null && !protocol.getProtocolTypeTermSourceRef().equals("")){

                    ISA2OWL.findOntologyTermAndAddClassAssertion(protocol.getProtocolTypeTermSourceRef(), protocol.getProtocolTypeTermAccession(), processIndividual);

                }//process node attributes not null


                //inputs & outputs
                List<Node> inputs = processNode.getInputNodes();
                for(Node input: inputs){
                    int inputCol = input.getIndex();
                    System.out.println("inputCol="+inputCol);
                    //for(int row=1; row < data.length; row++){

                    if (!data[processRow][inputCol].toString().equals("")){
                        protocolREFIndividuals.put(assayTableType == AssayTableType.STUDY ? ExtendedISASyntax.STUDY_PROTOCOL_REF_INPUT: ExtendedISASyntax.ASSAY_PROTOCOL_REF_INPUT, individualMatrix[processRow][inputCol]);
                        System.out.println("INPUT = "+individualMatrix[processRow][inputCol]);
                    }

                    //}//for row

                }//for inputs

                List<Node> outputs = processNode.getOutputNodes();
                for(Node output: outputs){
                    int outputCol = output.getIndex();
                    System.out.println("outputCol="+outputCol);

                    //for(int row=1; row < data.length; row++){

                     if (!data[processRow][outputCol].toString().equals("")){
                        protocolREFIndividuals.put(assayTableType == AssayTableType.STUDY ? ExtendedISASyntax.STUDY_PROTOCOL_REF_OUTPUT: ExtendedISASyntax.ASSAY_PROTOCOL_REF_OUTPUT, individualMatrix[processRow][outputCol]);
                        System.out.println("OUTPUT = "+individualMatrix[processRow][outputCol]);
                     }

                    //}//for row

                }//for inputs

                System.out.println("PROTOCOL REF INDIVIDUALS ="+protocolREFIndividuals);

                Map<String, List<Pair<IRI,String>>> protocolREFmapping = ISA2OWL.mapping.getProtocolREFMappings();
                ISA2OWL.convertProperties(protocolREFmapping, protocolREFIndividuals);
            }//processRow

        }//processNode
    }

    private void convertDataNodes(Graph graph) {
        OWLNamedIndividual dataNodeIndividual = null;

        //Material Nodes
        List<Node> dataNodes = graph.getNodes(NodeType.DATA_NODE);

        for(Node node: dataNodes){
            DataNode dataNode = (DataNode) node;
            System.out.println("DATA NODE="+node);

            int col = dataNode.getIndex();

            for(int row=1; row < data.length; row++){

                String dataValue = (String) data[row][col];

                if (dataValue.equals(""))
                    continue;

                //Data Node
                dataNodeIndividual = ISA2OWL.createIndividual("Data File", dataValue, dataValue);
                System.out.println("data node individual="+dataNode.getName()+" "+ dataValue );
                individualMatrix[row][col] = dataNodeIndividual;
        }
    }
    }

    private void convertMaterialNodes(Graph graph) {
        OWLNamedIndividual materialNodeIndividual = null;

        //Material Nodes
        List<Node> materialNodes = graph.getNodes(NodeType.MATERIAL_NODE);

        for(Node node: materialNodes){

            Map<String, OWLNamedIndividual> materialNodeIndividuals = new HashMap<String,OWLNamedIndividual>();

            MaterialNode materialNode = (MaterialNode) node;
            System.out.println("MATERIAL NODE="+node);

            int col = materialNode.getIndex();


            for(int row=1; row < data.length; row++){

                String dataValue = (String) data[row][col];

                if (dataValue.equals(""))
                    continue;

                //Material Node

                materialNodeIndividual = ISA2OWL.createIndividual(materialNode.getMaterialNodeType(), dataValue, materialNode.getMaterialNodeType());
                System.out.println("material node individual="+materialNode.getMaterialNodeType()+" "+ dataValue );
                individualMatrix[row][col] = materialNodeIndividual;
                materialNodeIndividuals.put(materialNode.getMaterialNodeType(), materialNodeIndividual);

                //Material Node Annotation
                System.out.println("=====Material Node Annotation=====");
                String purl = OntologyManager.getOntologyTermPurl(dataValue);
                if (purl!=null && !purl.equals("")){

                   System.out.println("If there is a PURL, use it! "+purl);

                }else{

                    String source = OntologyManager.getOntologyTermSource(dataValue);
                    String accession = OntologyManager.getOntologyTermAccession(dataValue);
                    ISA2OWL.findOntologyTermAndAddClassAssertion(source, accession, materialNodeIndividual);

                }

                //Material Node Name
                OWLNamedIndividual materialNodeIndividualName = ISA2OWL.createIndividual(materialNode.getName(),dataValue);
                materialNodeIndividuals.put(materialNode.getName(), materialNodeIndividualName);

                //material node attributes
                List<MaterialAttribute> attributeList = materialNode.getMaterialAttributes();

                System.out.println("--------attributeList="+attributeList);

                /***** commenting this out for now



               for(MaterialAttribute attribute: attributeList){


                   String attributeDataValue = data[row][attribute.getIndex()].toString();
                   OWLNamedIndividual materialAttributeIndividual = ISA2OWL.createIndividual(GeneralFieldTypes.CHARACTERISTIC.toString(), attributeDataValue, attribute.getName());
                   individualMatrix[row][attribute.getIndex()] = materialAttributeIndividual;


                   String source = OntologyManager.getOntologyTermSource(attributeDataValue);
                   String accession = OntologyManager.getOntologyTermAccession(attributeDataValue);

                   System.out.println("MATERIAL ATTRIBUTE "+ attribute+ " data value="+attributeDataValue+ " source="+source+" accession="+accession);

                   String attributeName = attribute.getName();
                   String attributeType = null;

                   if (attributeName.contains("http://") || attributeName.contains("https://")){

                       attributeType = attributeName.substring(attributeName.indexOf("(")+1, attributeName.indexOf(")"));

//                        //if attributeType is an independent continuant, the individual is of that type
//                        boolean isIndependentContinuant = ISA2OWL.reasonerService.isSuperClass(ISA2OWL.getOWLClass(IRI.create(attributeType)), ISA2OWL.getOWLClass(ISA2OWL.BFO_INDEPENDENT_CONTINUANT_IRI), false);
//
//                        if (isIndependentContinuant)
//                            ISA2OWL.addOWLClassAssertion(IRI.create(attributeType), individual);

                   }else if (accession!=null && accession.startsWith("http://")){
                        ISA2OWL.addOWLClassAssertion(IRI.create(accession), materialAttributeIndividual);
                   }else{
                       ISA2OWL.findOntologyTermAndAddClassAssertion(source, accession, materialAttributeIndividual);
                   }

                   System.out.println("materialNodeIndividual="+materialNodeIndividual);
                   System.out.println("attributeName="+attributeName);
                   System.out.println("attributeType="+attributeType);

                   if (materialNodeIndividual!=null){

                   OWLObjectProperty hasQuality = ISA2OWL.factory.getOWLObjectProperty(ISA2OWL.BFO_HAS_QUALITY_IRI);

                   if (attributeType!=null)
                       materialAttributeIndividual = ISA2OWL.createIndividual(attributeName, IRI.create(attributeType));
                   else
                       materialAttributeIndividual = ISA2OWL.factory.getOWLNamedIndividual(IRIGenerator.getIRI(ISA2OWL.ontoIRI));
                   System.out.println("attributeIndividual="+materialAttributeIndividual);
                   OWLObjectPropertyAssertionAxiom axiom = ISA2OWL.factory.getOWLObjectPropertyAssertionAxiom(hasQuality, materialNodeIndividual, materialAttributeIndividual);
                   ISA2OWL.manager.addAxiom(ISA2OWL.ontology, axiom);
                   }

               }
                *******/

                System.out.println("Material Node Individuals="+materialNodeIndividuals);

                Map<String, List<Pair<IRI,String>>> materialNodeMapping = ISA2OWL.mapping.getMaterialNodePropertyMappings();
                ISA2OWL.convertProperties(materialNodeMapping,materialNodeIndividuals);
            }
        }
    }

    private void convertGroups(OWLNamedIndividual studyDesignIndividual){
        //Treatment groups
        Map<String, Set<String>> groups = graphParser.getGroups();

        for(String group : groups.keySet()){

            Set<String> elements = groups.get(group);

            OWLNamedIndividual groupIndividual = ISA2OWL.createIndividual(ExtendedISASyntax.STUDY_GROUP, group);

            //group membership
            for(String element: elements){
                System.out.println("element="+element);
                System.out.println("ISA2OWL.idIndividualMap="+ISA2OWL.idIndividualMap);
                OWLNamedIndividual memberIndividual = ISA2OWL.idIndividualMap.get(element);
                OWLObjectProperty hasMember = ISA2OWL.factory.getOWLObjectProperty(ExtendedOBIVocabulary.HAS_MEMBER.iri);
                OWLObjectPropertyAssertionAxiom axiom = ISA2OWL.factory.getOWLObjectPropertyAssertionAxiom(hasMember, groupIndividual, memberIndividual);
                ISA2OWL.manager.addAxiom(ISA2OWL.ontology, axiom);
            }

            //'study design' denotes 'study group population'
            OWLObjectProperty denotes = ISA2OWL.factory.getOWLObjectProperty(IAOVocabulary.DENOTES.iri);
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



}