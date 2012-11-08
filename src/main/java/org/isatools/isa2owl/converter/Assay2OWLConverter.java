package org.isatools.isa2owl.converter;

import org.isatools.graph.model.*;
import org.isatools.graph.parser.GraphParser;
import org.isatools.isacreator.model.Assay;
import org.isatools.isacreator.model.GeneralFieldTypes;
import org.isatools.isacreator.model.Protocol;
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
    private Map<Integer, OWLNamedIndividual> materialNodeIndexIndividualMap = new HashMap<Integer, OWLNamedIndividual>();
    private Map<Node, Map<String,OWLNamedIndividual>> nodeIndividualMap = new HashMap<Node, Map<String,OWLNamedIndividual>>();
    private Map<Integer, OWLNamedIndividual> processIndividualMap = new HashMap<Integer, OWLNamedIndividual>();


    public Assay2OWLConverter(){

    }

    public void convert(Assay assay, OWLNamedIndividual assayIndividual, Map<String, OWLNamedIndividual> protocolIndividualMap){

        data = assay.getAssayDataMatrix();

        for(int i=0; i<data.length; i++){
            for (int j=0; j< data[i].length; j++){
                System.out.println("data["+i+","+j+"]="+data[i][j]);
            }
        }


        System.out.println("data.length="+data.length);

        System.out.println("data[0].length="+data[0].length);

        graphParser = new GraphParser(assay.getAssayDataMatrix());
        graphParser.parse();
        Graph graph = graphParser.getGraph();

        //print graph
        graph.outputGraph();

        OWLNamedIndividual individual = null;

        //Material Nodes
        List<Node> materialNodes = graph.getNodes(NodeType.MATERIAL_NODE);

        for(Node node: materialNodes){

            MaterialNode materialNode = (MaterialNode) node;
            int col = materialNode.getIndex();


            System.out.println("CONVERT MATERIAL NODE whose index is "+ col);
            System.out.println(materialNode.getMaterialNodeType());


            for(int row=1; row < data.length-1; row++){

                //System.out.println("data[i][j]="+(data[row][col]).toString());

                if (data[row][col].toString().equals(""))
                    continue;

                //Material Node
                individual = ISA2OWL.createIndividual(materialNode.getMaterialNodeType(), (data[row][col]).toString(), materialNode.getMaterialNodeType());
                addIndividualToMap(materialNode, materialNode.getMaterialNodeType(), individual);

                //Material Node Name
                individual = ISA2OWL.createIndividual(materialNode.getName(),(data[row][col]).toString());
                addIndividualToMap(materialNode, materialNode.getName(), individual);

                //material node attributes
                //materialNode.getMaterialAttributes();
            }
        }

        //Process Nodes
        List<Node> processNodes = graph.getNodes(NodeType.PROCESS_NODE);

        for(Node node: processNodes){
            ProcessNode processNode = (ProcessNode) node;
            int processCol = processNode.getIndex();

            for(int processRow=1; processRow < data.length; processRow ++){

                String processName = (data[processRow][processCol]).toString();
                OWLNamedIndividual protocolIndividual = protocolIndividualMap.get(processName);

                if (protocolIndividual==null)
                    System.err.println("Protocol "+processName+" must already exist");

                OWLNamedIndividual processIndividual = processIndividualMap.get(processCol);

                //material processing as the execution of the protocol
                if (processIndividual==null){

                    processIndividual = ISA2OWL.createIndividual(GeneralFieldTypes.PROTOCOL_REF.toString(),processName);
                    processIndividualMap.put(processCol, processIndividual);
                }

                //inputs
                List<Node> inputs = processNode.getInputNodes();
                for(Node input: inputs){
                    Map<String,OWLNamedIndividual> typeIndividualMap = nodeIndividualMap.get(input);

                    int col = input.getIndex();

                    for(int row=1; row < data.length-1; row++){

                        if (data[row][col].toString().equals(""))
                            continue;




                    }//for row


                }//for inputs

            }

        }


    }

    private void addIndividualToMap(Node node, String type, OWLNamedIndividual individual){

        Map stringIndividualMap = nodeIndividualMap.get(node);
        if (stringIndividualMap==null)
            stringIndividualMap = new HashMap<String, OWLNamedIndividual>();
        stringIndividualMap.put(type, individual);
        nodeIndividualMap.put(node, stringIndividualMap);

    }
}
