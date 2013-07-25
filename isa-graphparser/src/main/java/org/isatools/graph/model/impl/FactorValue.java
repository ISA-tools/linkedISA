package org.isatools.graph.model.impl;

import org.isatools.graph.model.ISAFactorValue;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 25/07/2013
 * Time: 01:30
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class FactorValue extends Node implements ISAFactorValue {

    public FactorValue(int index, String name) {
        super(index, name);
    }

    @Override
    public String toString(){
        return getName();
    }
}
