package org.isatools.graph.model.impl;

import org.isatools.graph.model.ISANode;
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

    private List<ISANode> inputNodes = null;
    private List<ISANode> outputNodes = null;
    private List<ProcessParameter> parameters = null;

    public ProcessNode(int index, String name) {
        super(index, name);
        inputNodes = new ArrayList<ISANode>();
        outputNodes = new ArrayList<ISANode>();
        parameters = new ArrayList<ProcessParameter>();

    }

    @Override
    public void addInputNode(ISANode inputNode) {
        inputNodes.add((Node)inputNode);
    }

    @Override
    public void addOutputNode(ISANode outputNode) {
        outputNodes.add((Node)outputNode);
    }

    @Override
    public void addParameter(ProcessParameter p){
        parameters.add(p);
    }


    @Override
    public List<ISANode> getInputNodes() {
        return inputNodes;
    }

    @Override
    public List<ISANode> getOutputNodes() {
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
            for (ISANode inputNode : getInputNodes()) {
                buffer.append("\t input: " + inputNode.getName()+"\n");
            }
        }
        if (getOutputNodes() != null) {
            for (ISANode outputNode : getOutputNodes()) {
                buffer.append("\t output: " + outputNode.getName()+"\n");
            }
        }

        if (getComments() != null) {
            for (ISANode commentNode : getComments()) {
                buffer.append("\t comment: " + commentNode.getName()+"\n");
            }
        }
        return buffer.toString();
    }
}
