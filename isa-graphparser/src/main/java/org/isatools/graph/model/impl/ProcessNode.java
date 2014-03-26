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

    public static final String REGEXP = "(.*Assay Name)|(Normalization.*)|(Data Transformation.*)";

    //the process nodes that are related to this assay
    private List<ProtocolExecutionNode> protocolExecutionNodes = null;


    protected ISANode inputNode = null;
    protected ISANode outputNode = null;
    protected List<ProcessParameter> parameters = null;

    public ProcessNode(int index, String name) {
        super(index, name);
        setType(NodeType.PROCESS_NODE);
        parameters = new ArrayList<ProcessParameter>();
        protocolExecutionNodes = new ArrayList<ProtocolExecutionNode>();
    }

    public List<ProtocolExecutionNode> getAssociatedProcessNodes(){
        return protocolExecutionNodes;
    }

    public void addProtocolExecutionNodes(List<ProtocolExecutionNode> list){
        protocolExecutionNodes.addAll(list);
    }


    @Override
    public void setInputNode(ISANode in) {
        inputNode = in;
    }

    @Override
    public void setOutputNode(ISANode on) {
        outputNode = on;
    }

    @Override
    public void addParameter(ProcessParameter p){
        parameters.add(p);
    }


    @Override
    public ISANode getInputNode() {
        return inputNode;
    }

    @Override
    public ISANode getOutputNode() {
        return outputNode;
    }

    @Override
    public List<ProcessParameter> getParameters(){
        return parameters;
    }

    public String toShortString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append(getName());
        if (getParameters() !=null && !getParameters().isEmpty()){
            buffer.append("[");
            for (ProcessParameter parameter : getParameters()) {
                buffer.append("" + parameter.getName()+",");
            }
            buffer.append("]");
        }
        buffer.append(":");
        if (inputNode != null) {
            buffer.append("\t input: " + inputNode.getName()+"\n");
        }
        buffer.append("->");
        if (outputNode != null) {
                buffer.append(" " + outputNode.getType() +" " + outputNode.getName()+"");
        }
       return buffer.toString();
    }

    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("ProcessNode: "+getName()+"\n");

        if (getParameters() !=null){
            for (ProcessParameter parameter : getParameters()) {
                buffer.append("\t parameter: " + parameter.getName()+"\n");
            }
        }

        if (inputNode != null) {
                buffer.append(" " + inputNode.getType() +" "+ inputNode.getName()+"");
        }
        if (outputNode != null) {
                buffer.append("\t output: " + outputNode.getName()+"\n");
        }

        if (getAssociatedProcessNodes() !=null) {
            for(ProtocolExecutionNode pen : getAssociatedProcessNodes()){
                buffer.append("\t associated protocol execution: " + pen.getName()+"\n");
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
