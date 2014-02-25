package org.isatools.graph.model;

import java.util.Map;
import java.util.Set;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 24/02/2014
 * Time: 10:30
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public interface ISAGroup {

    public String getName();

    public String getCriterion();

    public Map<String, String> getGroupDefinition();

    public Set<String> getGroupMembers();

    public void addGroupDefinition(String k, String v);
}
