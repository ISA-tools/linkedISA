package org.isatools.graph.model.impl;

import org.isatools.graph.model.ISANode;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 10/10/2011
 *         Time: 10:06
 */
public abstract class Node implements ISANode {

    private int index;
    private String name;
    private NodeType type;

    public Node(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public Node(int index, String name, NodeType type) {
        this.index = index;
        this.name = name;
        this.type = type;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NodeType getType(){
        return type;
    }

    @Override
    public void setType(NodeType t){
        type = t;
    }

}