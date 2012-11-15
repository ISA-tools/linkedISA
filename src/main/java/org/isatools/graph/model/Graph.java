package org.isatools.graph.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 *         <p/>
 *         Date: 10/10/2011
 *         Time: 10:15
 */
public class Graph {

    private List<Node> nodeList;

    public Graph() {
        this.nodeList = new ArrayList<Node>();
    }

    public void addNode(Node node) {
        nodeList.add(node);
    }

    public List<Node> getNodes() {
        return nodeList;
    }

    public List<Node> getNodes(NodeType nodeType) {
        List<Node> nodes = new ArrayList<Node>();

        for (Node n : nodeList) {
            // there will be more materials in general, so to make this method quicker, it's good to check the most
            // expected element first.
            if (nodeType == NodeType.MATERIAL_NODE) {
                if (n instanceof MaterialNode) {
                    nodes.add(n);
                }
            } else if (nodeType == NodeType.PROCESS_NODE) {
                if (n instanceof ProcessNode) {
                    nodes.add(n);
                }
            } else if (nodeType == NodeType.DATA_NODE) {
                if (n instanceof DataNode) {
                    nodes.add(n);
                }
            }
        }

        return nodes;
    }

    public Node getNode(int index) {
        for (Node n : nodeList) {
            if (n.getIndex() == index) {
                return n;
            }
        }

        return null;
    }

    /**
     * Prints out the graph
     */
    public void outputGraph() {
        for (Node n : getNodes()) {
            if (n instanceof ProcessNode) {
                ProcessNode node = (ProcessNode) n;
                System.out.println("ProcessNode: "+n.getName());

                if (node.getInputNodes() != null) {
                    for (Node inputNode : node.getInputNodes()) {
                        System.out.println("\t input: " + inputNode.getName());
                    }
                }
                if (node.getOutputNodes() != null) {
                    for (Node outputNode : node.getOutputNodes()) {
                        System.out.println("\t output: " + outputNode.getName());
                    }
                }
            } else if (n instanceof MaterialNode) {

                System.out.println("MaterialNode: "+n.getName());

                MaterialNode node = (MaterialNode) n;

                for (Node mp : node.getMaterialAttributes()) {
                    System.out.println("\t attribute: " + mp.getName());
                }
            } else {
                System.out.println("DataNode: "+n.getName());
            }
        }
    }


}