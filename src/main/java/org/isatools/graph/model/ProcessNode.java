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
 *         Time: 10:13
 */
public class ProcessNode extends Node {

    public static final String REGEXP = "Protocol REF";

    private List<Node> inputNodes = new ArrayList<Node>();
    private List<Node> outputNodes = new ArrayList<Node>();

    public ProcessNode(int index, String name) {
        super(index, name);
    }

    public ProcessNode(int index, String name, Node inputNode, Node outputNode) {
        super(index, name);
        inputNodes.add(inputNode);
        outputNodes.add(outputNode);
    }

    public void addInputNode(Node inputNode) {
        inputNodes.add(inputNode);
    }

    public void addOutputNode(Node outputNode) {
        outputNodes.add(outputNode);
    }

    public List<Node> getInputNodes() {
        return inputNodes;
    }

    public List<Node> getOutputNodes() {
        return outputNodes;
    }
}
