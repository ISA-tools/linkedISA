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

    public Assay2OWLConverter(){

    }

    public void convert(Assay assay, OWLNamedIndividual assayIndividual){

        graphParser = new GraphParser(assay.getAssayDataMatrix());

        graphParser.parse();

        Graph graph = graphParser.getGraph();

        graph.outputGraph();

        //Material Nodes
        List<Node> materialNodes = graph.getNodes(NodeType.MATERIAL_NODE);

        for(Node node: materialNodes){
            MaterialNode materialNode = (MaterialNode) node;

            System.out.println("CONVERT MATERIAL NODE");
            System.out.println(materialNode.getMaterialNodeType());
            System.out.println(materialNode.getMaterialNodeName());
            //Material Node
            ISA2OWL.createIndividual(materialNode.getMaterialNodeType(), materialNode.getMaterialNodeName());

            //Material Node Name
            ISA2OWL.createIndividual(ExtendedISASyntax.MATERIAL_NODE, materialNode.getName());
        }


    }
}
