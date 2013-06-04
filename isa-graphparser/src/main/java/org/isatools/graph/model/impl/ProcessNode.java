package org.isatools.graph.model.impl;

import org.isatools.graph.model.ISAProcessNode;

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
public class ProcessNode extends NodeWithComments implements ISAProcessNode {

    public static final String REGEXP = "Protocol REF";

    private List<Node> inputNodes = null;
    private List<Node> outputNodes = null;
    private List<ProcessParameter> parameters = null;

    public ProcessNode(int index, String name) {
        super(index, name);
        inputNodes = new ArrayList<Node>();
        outputNodes = new ArrayList<Node>();
        parameters = new ArrayList<ProcessParameter>();

    }

    @Override
    public void addInputNode(Node inputNode) {
        inputNodes.add(inputNode);
    }

    @Override
    public void addOutputNode(Node outputNode) {
        outputNodes.add(outputNode);
    }

    @Override
    public void addParameter(ProcessParameter p){
        parameters.add(p);
    }


    @Override
    public List<Node> getInputNodes() {
        return inputNodes;
    }

    @Override
    public List<Node> getOutputNodes() {
        return outputNodes;
    }

    @Override
    public List<ProcessParameter> getParameters(){
        return parameters;
    }


    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("ProcessNode: "+getName()+"\n");

        if (getParameters() !=null){
            for (ProcessParameter parameter : getParameters()) {
                buffer.append("\t parameter: " + parameter.getName()+"\n");
            }
        }

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

        if (getComments() != null) {
            for (Node commentNode : getComments()) {
                buffer.append("\t comment: " + commentNode.getName()+"\n");
            }
        }
        return buffer.toString();
    }
}
