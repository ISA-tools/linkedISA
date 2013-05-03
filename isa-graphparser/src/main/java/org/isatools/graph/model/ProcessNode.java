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
public class ProcessNode extends NodeWithComments {

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

    public void addInputNode(Node inputNode) {
        inputNodes.add(inputNode);
    }

    public void addOutputNode(Node outputNode) {
        outputNodes.add(outputNode);
    }

    public void addParameter(ProcessParameter p){
        parameters.add(p);
    }


    public List<Node> getInputNodes() {
        return inputNodes;
    }

    public List<Node> getOutputNodes() {
        return outputNodes;
    }

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
