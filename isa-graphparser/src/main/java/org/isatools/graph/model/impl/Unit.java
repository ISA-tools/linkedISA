package org.isatools.graph.model.impl;

import org.isatools.graph.model.ISAUnit;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 06/08/2013
 * Time: 17:42
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class Unit extends Node implements ISAUnit {


    public Unit(int index, String name) {
        super(index, name);
    }

    @Override
    public String toString(){
        return getName();
    }

}
