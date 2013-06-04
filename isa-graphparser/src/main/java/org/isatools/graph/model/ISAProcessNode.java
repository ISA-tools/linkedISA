package org.isatools.graph.model;

import org.isatools.graph.model.impl.Node;
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

    public void addInputNode(Node inputNode);

    public void addOutputNode(Node outputNode);

    public void addParameter(ProcessParameter p);

    public List<Node> getInputNodes();

    public List<Node> getOutputNodes();

    public List<ProcessParameter> getParameters();
}
