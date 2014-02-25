package org.isatools.graph.model.impl;

import org.isatools.graph.model.ISAGroup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 24/02/2014
 * Time: 10:43
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class StudyGroup implements ISAGroup {

    //the column used to build the group, e.g. Factor
    private String groupCriterion = null;

    //the name of the group - includes the specific values
    private String groupName = null;

    //map with entity and value determining the group definition, e.g. Factor/Factor Value
    private Map<String, String> groupDefinition = null;

    //the members of the group
    private Set<String> groupMembers = null;

    public StudyGroup(String criterion){
        groupCriterion = criterion;
        groupDefinition = new HashMap<String, String>();
        groupMembers = new HashSet<String>();
    }

    @Override
    public void addGroupDefinition(String k, String v){
        groupDefinition.put(k, v);
    }

    public void addGroupValue(String v){
        groupMembers.add(v);
    }

    @Override
    public String getName() {
        return groupName;
    }

    @Override
    public String getCriterion() {
        return groupCriterion;
    }

    @Override
    public Map<String, String> getGroupDefinition() {
        return groupDefinition;
    }

    @Override
    public Set<String> getGroupMembers() {
        return null;
    }


}
