package org.isatools.graph.model.impl;

import org.isatools.graph.model.ISAGraph;

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
public class Graph implements ISAGraph {

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
                if (n instanceof ProcessNode && !(n instanceof AssayNode)) {
                    nodes.add(n);
                }
            } else if (nodeType == NodeType.DATA_NODE) {
                if (n instanceof DataNode) {
                    nodes.add(n);
                }
            }  else if (nodeType == NodeType.ASSAY_NODE) {
                if (n instanceof AssayNode) {
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
     * Prints out the org.isatools.graph
     */
    public void outputGraph() {
        for (Node n : getNodes()) {
            System.out.println(n);
        }
    }


}