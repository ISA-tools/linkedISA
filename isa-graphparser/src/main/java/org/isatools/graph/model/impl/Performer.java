package org.isatools.graph.model.impl;


import org.isatools.graph.model.ISANode;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 18/12/2013
 * Time: 17:34
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class Performer extends Node implements ISANode {

    public static final String REGEXP = "(Performer.*)";

    public Performer(int index, String name) {
        super(index, name);
    }

    @Override
    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("Performer: "+getName());
        return buffer.toString();
    }

}
