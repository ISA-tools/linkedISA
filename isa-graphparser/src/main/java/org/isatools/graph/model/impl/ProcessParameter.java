package org.isatools.graph.model.impl;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 30/04/2013
 * Time: 16:45
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class ProcessParameter extends Node {

    public static final String REGEXP = "(Parameter Value.*)";

    public ProcessParameter(int index, String name) {
        super(index, name);
    }

    public String toString(){
        return getName();
    }


}
