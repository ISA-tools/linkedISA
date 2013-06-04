package org.isatools.graph.model.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 19/02/2013
 * Time: 15:01
 *
 * AssayNode occurs for some of the ProcessNodes, when input is MaterialNode
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class AssayNode extends ProcessNode {

    public static final String REGEXP = "(.*Assay Name)";

    private List<ProcessNode> processNodes = null;

    public AssayNode(int index, String name) {
        super(index, name);
        processNodes = new ArrayList<ProcessNode>();
    }

    public void addAssociatedProcessNode(ProcessNode pn){
        processNodes.add(pn);
    }

    public List<ProcessNode> getAssociatedProcessNodes(){
        return processNodes;
    }

    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("AssayNode: "+getName()+"\n");
        if (getInputNodes() != null) {
            for (Node inputNode : getInputNodes()) {
                buffer.append("\t input: " + inputNode.getName()+"\n");
            }
        }
        if (getOutputNodes() != null) {
            for (Node outputNode : getOutputNodes()) {
                buffer.append("\t output: " + outputNode.getName()+"\n");
            }
        }

        if (getAssociatedProcessNodes() != null){
            for (Node associatedProcessNode : getAssociatedProcessNodes()) {
                buffer.append("\t associated process node: " + associatedProcessNode.getName()+"\n");
            }
        }
        return buffer.toString();

    }
}
