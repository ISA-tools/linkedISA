package org.isatools.graph.model;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 10/10/2011
 *         Time: 10:13
 */
public class DataNode extends NodeWithComments {

    public static final String CONTAINS = "File";

    public DataNode(int index, String name) {
        super(index, name);
    }

    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("DataNode: "+getName()+"\n");
        if (getComments() != null) {
            for (Node commentNode : getComments()) {
                buffer.append("\t comment: " + commentNode.getName()+"\n");
            }
        }
        return buffer.toString();
    }
}