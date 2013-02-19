package org.isatools.graph.model;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 19/02/2013
 * Time: 15:01
 *
 * AssayNode occurs for some of the ProcessNodes, when input is MaterialNode
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class AssayNode extends Node {

    public static final String REGEXP = "(.*Assay Name)";

    private ProcessNode processNode = null;

    public AssayNode(int index, String name) {
        super(index, name);
    }

    public void addAssociatedProcessNode(ProcessNode pn){
        processNode = pn;
    }
}
