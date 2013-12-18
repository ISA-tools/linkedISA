package org.isatools.graph.model.impl;

import org.isatools.graph.model.ISANode;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 18/12/2013
 * Time: 17:49
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class Date extends Node implements ISANode {

    public static final String REGEXP = "(Date.*)";

    public Date(int index, String name) {
        super(index, name);

    }

    @Override
    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("Date: "+getName());
        return buffer.toString();
    }

}
