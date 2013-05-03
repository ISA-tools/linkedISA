package org.isatools.graph.model;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 03/05/2013
 * Time: 11:28
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class CommentNode extends Node {

    public static final String REGEXP = "(Comment.*)";

    public CommentNode(int index, String name) {
        super(index, name);
    }


}
