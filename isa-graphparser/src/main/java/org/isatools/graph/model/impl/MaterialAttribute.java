package org.isatools.graph.model.impl;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 *         <p/>
 *         Date: 11/10/2011
 *         Time: 13:22
 */
public class MaterialAttribute extends Node {

    public static final String REGEXP = "(Characteristic.*)";

    public MaterialAttribute(int index, String name) {
        super(index, name);
    }

    public String toString(){
        return getName();
    }

}