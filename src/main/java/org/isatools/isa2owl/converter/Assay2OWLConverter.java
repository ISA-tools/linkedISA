package org.isatools.isa2owl.converter;

import org.isatools.graph.model.*;
import org.isatools.graph.parser.GraphParser;
import org.isatools.isacreator.model.Assay;
import org.isatools.isacreator.model.GeneralFieldTypes;
import org.isatools.isacreator.model.Protocol;
import org.isatools.isacreator.ontologymanager.OntologyManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 07/11/2012
 * Time: 16:11
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class Assay2OWLConverter {

    private GraphParser graphParser = null;
    private Object[][] data = null;

    //a matrix will all the individuals for the data (these are MaterialNodes or ProcessNodes individuals
    private OWLNamedIndividual[][] individualMatrix = null;

    private Map<MaterialNode, Map<String,OWLNamedIndividual>> materialNodeIndividualMap = new HashMap<MaterialNode, Map<String,OWLNamedIndividual>>();
    private Map<Integer, OWLNamedIndividual> processIndividualMap = new HashMap<Integer, OWLNamedIndividual>();


    public Assay2OWLConverter(){

    }

    public void convert(Assay assay, OWLNamedIndividual assayIndividual, Map<String, OWLNamedIndividual> protocolIndividualMap){

        System.out.println("CONVERTING ASSAY");

        data = assay.getAssayDataMatrix();

        //TODO remove
        for(int i=0; i<data.length; i++){
            for (int j=0; j< data[i].length; j++){
                System.out.println("data["+i+","+j+"]="+data[i][j]);
            }
        }

        individualMatrix = new OWLNamedIndividual[data.length][data[0].length];

        graphParser = new GraphParser(assay.getAssayDataMatrix());
        graphParser.parse();
        Graph graph = graphParser.getGraph();

        //print graph
        System.out.println("ASSAY GRAPH...");
        graph.outputGraph();

        OWLNamedIndividual individual = null;

        //Material Nodes
        List<Node> materialNodes = graph.getNodes(NodeType.MATERIAL_NODE);

        for(Node node: materialNodes){

            Map<String, OWLNamedIndividual> materialNodeIndividuals = new HashMap<String,OWLNamedIndividual>();

            MaterialNode materialNode = (MaterialNode) node;
            int col = materialNode.getIndex();

            System.out.println("CONVERT MATERIAL NODE whose index is "+ col);
            System.out.println(materialNode.getMaterialNodeType());

            for(int row=1; row < data.length; row++){

                System.out.println("data[row]["+col+"]="+(data[row][col]).toString());

                String dataValue = (String) data[row][col];

                if (dataValue.equals(""))
                    continue;

                //Material Node
                individual = ISA2OWL.createIndividual(materialNode.getMaterialNodeType(), dataValue, materialNode.getMaterialNodeType());
                individualMatrix[row][col] = individual;
                materialNodeIndividuals.put(materialNode.getMaterialNodeType(), individual);

                //Material Node Annotation
                System.out.println("=====Material Node Annotation=====");
                String purl = OntologyManager.getOntologyTermPurl(dataValue);
                if (purl!=null && !purl.equals("")){
                   System.out.println("If there is a PURL, use it!");
                }else{

                    String source = OntologyManager.getOntologyTermSource(dataValue);
                    String accession = OntologyManager.getOntologyTermAccession(dataValue);
                    ISA2OWL.findOntologyTermAndAddClassAssertion(source, accession, individual);

                }

                //Material Node Name
                individual = ISA2OWL.createIndividual(materialNode.getName(),dataValue);
                materialNodeIndividuals.put(materialNode.getName(), individual);



                //material node attributes
                //materialNode.getMaterialAttributes();

                System.out.println("Material Node Individuals="+materialNodeIndividuals);

                Map<String, Map<IRI,String>> materialNodeMapping = ISA2OWL.mapping.getMaterialNodePropertyMappings();
                ISA2OWL.convertProperties(materialNodeMapping,materialNodeIndividuals);
            }
        }

        for(int i=0; i<data.length; i++){
            for (int j=0; j< data[i].length; j++){
                System.out.println("individualMatrix["+i+","+j+"]="+individualMatrix[i][j]);
            }
        }


        //Process Nodes
        List<Node> processNodes = graph.getNodes(NodeType.PROCESS_NODE);

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
                    processIndividual = ISA2OWL.createIndividual(GeneralFieldTypes.PROTOCOL_REF.toString(),processName);
                    processIndividualMap.put(processCol, processIndividual);
                }

                protocolREFIndividuals.put(GeneralFieldTypes.PROTOCOL_REF.toString(), processIndividual);

                //inputs & outputs
                List<Node> inputs = processNode.getInputNodes();
                for(Node input: inputs){
                    int inputCol = input.getIndex();
                    System.out.println("inputCol="+inputCol);
                    //for(int row=1; row < data.length; row++){

                    if (!data[processRow][inputCol].toString().equals("")){
                        protocolREFIndividuals.put(ExtendedISASyntax.PROTOCOL_REF_INPUT, individualMatrix[processRow][inputCol]);
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
                        protocolREFIndividuals.put(ExtendedISASyntax.PROTOCOL_REF_OUTPUT, individualMatrix[processRow][outputCol]);
                        System.out.println("OUTPUT = "+individualMatrix[processRow][outputCol]);
                     }

                    //}//for row

                }//for inputs

                System.out.println("PROTOCOL REF INDIVIDUALS ="+protocolREFIndividuals);

                Map<String, Map<IRI,String>> protocolREFmapping = ISA2OWL.mapping.getProtocolREFMappings();
                ISA2OWL.convertProperties(protocolREFmapping, protocolREFIndividuals);
            }//processRow

        }//processNode


    }

}
