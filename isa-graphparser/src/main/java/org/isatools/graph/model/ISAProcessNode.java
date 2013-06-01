package org.isatools.graph.model;

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

    void addInputNode(Node inputNode);

    void addOutputNode(Node outputNode);

    void addParameter(ProcessParameter p);

    List<Node> getInputNodes();

    List<Node> getOutputNodes();

    List<ProcessParameter> getParameters();
}
