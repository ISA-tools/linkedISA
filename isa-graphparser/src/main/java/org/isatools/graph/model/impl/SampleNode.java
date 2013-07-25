package org.isatools.graph.model.impl;

import org.isatools.graph.model.ISAFactorValue;
import org.isatools.graph.model.ISAMaterialAttribute;
import org.isatools.graph.model.ISASampleNode;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 25/07/2013
 * Time: 01:42
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class SampleNode extends NodeWithComments implements ISASampleNode {

    public SampleNode(int index, String name) {
        super(index, name);
    }

    @Override
    public Iterable<ISAFactorValue> getFactorValues() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addMaterialAttribute(ISAMaterialAttribute attribute) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Iterable<ISAMaterialAttribute> getMaterialAttributes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getMaterialNodeType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
