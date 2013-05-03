package org.isatools.graph.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 03/05/2013
 * Time: 11:38
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class NodeWithComments extends Node {

    private List<CommentNode> comments;

    public NodeWithComments(int index, String name) {
        super(index, name);
        comments = new ArrayList<CommentNode>();
    }

    public void addComment(CommentNode c){
        comments.add(c);
    }

    public List<CommentNode> getComments(){
        return comments;
    }


}
