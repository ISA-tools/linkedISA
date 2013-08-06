package org.isatools.graph.model.impl;

import org.isatools.graph.model.ISAFactorValue;
import org.isatools.graph.model.ISANode;
import org.isatools.graph.model.ISASampleNode;
import org.isatools.syntax.ExtendedISASyntax;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 25/07/2013
 * Time: 01:42
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class SampleNode extends MaterialNode implements ISASampleNode {

    private List<ISAFactorValue> factorValues = null;

    public SampleNode(int index, String name) {
        super(index, name);
        factorValues = new ArrayList<ISAFactorValue>();
    }

    @Override
    public List<ISAFactorValue> getFactorValues() {
        return factorValues;
    }

    @Override
    public void addFactorValue(ISAFactorValue factorValue) {
       factorValues.add(factorValue);
    }

    @Override
    public String getMaterialNodeType() {
        return ExtendedISASyntax.SAMPLE;
    }

    @Override
    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("SampleNode: "+getName()+"\n");
        for (ISANode mp : getMaterialAttributes()) {
            buffer.append("\t attribute: " + mp.getName()+"\n");
        }

        for (ISANode fv : getFactorValues()) {
            buffer.append("\t factor value: " + fv.getName()+"\n");
        }

        if (getComments() != null) {
            for (Node commentNode : getComments()) {
                buffer.append("\t comment: " + commentNode.getName()+"\n");
            }
        }
        return buffer.toString();
    }
}
