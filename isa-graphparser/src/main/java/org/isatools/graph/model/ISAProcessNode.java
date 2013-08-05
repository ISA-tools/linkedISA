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
public interface ISAProcessNode {

    public static final String REGEXP = "((?i)Protocol REF)|(Data Normalization.*)|(Data Transformation.*)";

    public void addInputNode(ISANode inputNode);

    public void addOutputNode(ISANode outputNode);

    public void addParameter(ProcessParameter p);

    public List<ISANode> getInputNodes();

    public List<ISANode> getOutputNodes();

    public List<ProcessParameter> getParameters();
}
