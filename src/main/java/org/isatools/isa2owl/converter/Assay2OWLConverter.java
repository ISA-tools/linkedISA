package org.isatools.isa2owl.converter;

import org.isatools.graph.model.Graph;
import org.isatools.graph.model.MaterialNode;
import org.isatools.graph.model.Node;
import org.isatools.graph.model.NodeType;
import org.isatools.graph.parser.GraphParser;
import org.isatools.isacreator.model.Assay;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.util.List;

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

    public Assay2OWLConverter(){

    }

    public void convert(Assay assay, OWLNamedIndividual assayIndividual){

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

        //Material Nodes
        List<Node> materialNodes = graph.getNodes(NodeType.MATERIAL_NODE);

        for(Node node: materialNodes){
            MaterialNode materialNode = (MaterialNode) node;
            int col = materialNode.getIndex();


            System.out.println("CONVERT MATERIAL NODE whose index is "+ col);

            System.out.println(materialNode.getMaterialNodeType());

            System.out.println(materialNode.getMaterialNodeType());


            for(int row=1; row < data.length-1; row++){

                System.out.println("data[i][j]="+(data[row][col]).toString());

                if (data[row][col].toString().equals(""))
                    continue;

                //Material Node
                ISA2OWL.createIndividual(materialNode.getMaterialNodeType(),(data[row][col]).toString(), materialNode.getMaterialNodeType());

                //Material Node Name
                ISA2OWL.createIndividual(materialNode.getName(),(data[row][col]).toString());
            }
        }


    }
}
