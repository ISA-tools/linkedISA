package org.isatools.graph.model;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 10/10/2011
 *         Time: 10:06
 */
public abstract class Node {

    private int index;
    private String name;
    private NodeType type;

    public Node(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public NodeType getType(){
        return type;
    }

    public void setType(NodeType t){
        type = t;
    }

}