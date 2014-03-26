package org.isatools.graph.model;


import org.isatools.graph.model.impl.ProcessParameter;

import java.util.List;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 31/05/2013
 * Time: 14:23
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public interface ISAProcessNode extends ISANode {

    public void setInputNode(ISANode inputNode);

    public void setOutputNode(ISANode outputNode);

    public void addParameter(ProcessParameter p);

    public ISANode getInputNode();

    public ISANode getOutputNode();

    public List<ProcessParameter> getParameters();
}
