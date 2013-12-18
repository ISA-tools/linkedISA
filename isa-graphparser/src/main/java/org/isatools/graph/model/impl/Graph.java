package org.isatools.graph.model.impl;

import org.isatools.graph.model.ISAGraph;
import org.isatools.graph.model.ISANode;

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

    private List<ISANode> nodeList;

    public Graph() {
        this.nodeList = new ArrayList<ISANode>();
    }

    public void addNode(ISANode node) {
        nodeList.add(node);
    }

    public List<ISANode> getNodes() {
        return nodeList;
    }

    public List<ISANode> getNodes(NodeType nodeType) {
        List<ISANode> nodes = new ArrayList<ISANode>();

        for (ISANode n : nodeList) {
            // there will be more materials in general, so to make this method quicker, it's good to check the most
            // expected element first.
            if (nodeType == NodeType.MATERIAL_NODE) {
                if (n instanceof MaterialNode) {
                    nodes.add(n);
                }
            } else if (nodeType == NodeType.PROCESS_NODE) {
                if (n instanceof ProcessNode && !(n instanceof AssayNode) && !(n instanceof ProtocolExecutionNode)) {
                    nodes.add(n);
                }
            } else if (nodeType == NodeType.DATA_NODE) {
                if (n instanceof DataNode) {
                    nodes.add(n);
                }
            } else if (nodeType == NodeType.ASSAY_NODE) {
                if (n instanceof AssayNode) {
                    nodes.add(n);
                }
            } else if (nodeType == NodeType.PROTOCOL_EXECUTION_NODE){
                if (n instanceof ProtocolExecutionNode) {
                    nodes.add(n);
                }
            }
        }

        return nodes;
    }

    public ISANode getNode(int index) {
        for (ISANode n : nodeList) {
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
        for (ISANode n : getNodes()) {
            System.out.println(n);
        }
    }


}