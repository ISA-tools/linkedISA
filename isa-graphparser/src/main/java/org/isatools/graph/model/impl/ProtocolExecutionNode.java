package org.isatools.graph.model.impl;

import org.isatools.graph.model.ISANode;

import java.util.ArrayList;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 18/12/2013
 * Time: 17:59
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class ProtocolExecutionNode extends ProcessNode {

    public static final String REGEXP = "((?i)Protocol REF)";

    protected Performer performer = null;
    protected Date date = null;

    public ProtocolExecutionNode(int index, String name) {
        super(index, name);
        setType(NodeType.PROTOCOL_EXECUTION_NODE);
        parameters = new ArrayList<ProcessParameter>();
    }

    public void addPerformer(Performer p){
        performer = p;
    }

    public void addDate(Date d){
        date = d;
    }

    public Performer getPerformer(){
        return performer;
    }

    public Date getDate(){
        return date;
    }

    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("ProtocolExecutionNode: "+getName()+"\n");

        if (getPerformer()!=null){
            buffer.append("\t performer: " + getPerformer()+"\n");
        }

        if (getDate()!=null){
            buffer.append("\t date: " + getDate()+"\n");
        }

        if (getParameters() !=null){
            for (ProcessParameter parameter : getParameters()) {
                buffer.append("\t parameter: " + parameter.getName()+"\n");
            }
        }

        if (inputNode != null) {
                buffer.append("\t input: " + inputNode.getType() + " " + inputNode.getName()+"\n");
        }
        if (outputNode != null) {
                buffer.append("\t output: " + outputNode.getType() + " " + outputNode.getName()+"\n");
        }

        if (getComments() != null) {
            for (ISANode commentNode : getComments()) {
                buffer.append("\t comment: " + commentNode.getName()+"\n");
            }
        }
        return buffer.toString();
    }

}
