package org.isatools.graph.model;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 06/11/2012
 * Time: 16:11
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class MaterialNode extends Node {

    private List<Node> materialProperties;

    public MaterialNode(int index, String name) {
        super(index, name);

        materialProperties = new ArrayList<Node>();
    }

    public void addMaterialProperty(Node property) {
        materialProperties.add(property);
    }

    public List<Node> getMaterialProperties() {
        return materialProperties;
    }
}