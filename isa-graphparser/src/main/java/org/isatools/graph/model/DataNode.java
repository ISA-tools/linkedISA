package org.isatools.graph.model;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 10/10/2011
 *         Time: 10:13
 */
public class DataNode extends Node {

    public static final String CONTAINS = "File";
    public static final String REGEXP = "(Comment.*)";

    public DataNode(int index, String name) {
        super(index, name);
    }

    public String toString(){
        return "DataNode: "+getName()+"\n";
    }
}