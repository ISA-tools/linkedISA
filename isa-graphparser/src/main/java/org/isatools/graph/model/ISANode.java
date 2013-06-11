package org.isatools.graph.model;

import org.isatools.graph.model.impl.NodeType;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 04/06/2013
 * Time: 14:17
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public interface ISANode {

    public int getIndex();

    public String getName();

    public NodeType getType();

    public void setType(NodeType t);
}
