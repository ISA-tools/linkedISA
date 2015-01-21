package org.isatools.graph.parser;

import org.isatools.graph.model.*;
import org.isatools.graph.model.impl.*;
import org.isatools.graph.model.impl.Date;

import java.util.*;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 07/11/2012
 * Time: 10:41
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 *
 * This class was named SpreadsheetAnalysis in Eamonn's project.
 *
 * The Graph Parser identifies the different types of nodes and attributes (material nodes, process nodes, material attributes).
 *
 */
public class GraphParser {

    private Object[][] assayTable;

    private Graph graph;
    private Map<String, StudyGroup> groups;

    /**
     * Constructor
     *
     * @param assayTable matrix of objects - either an study sample table or an assay table
     */
    public GraphParser(Object[][] assayTable) {
        this.assayTable = assayTable;
        groups = new HashMap<String, StudyGroup>();
    }

    public void parse() {
        extractGroups();
        createGraph();
    }

    private void createGraph() {
        graph = new Graph();
        String[] columns = Arrays.copyOf(assayTable[0], assayTable[0].length, String[].class);

        int index = 0;

        ProcessNode lastProcess = null;
        NodeWithComments lastMaterialOrData = null;
        NodeWithComments lastSample = null;
        ISAFactorValue lastFactorValue = null;
        ProtocolExecutionNode lastProtocolExecutionNode = null;
        List<ProtocolExecutionNode> protocolExecutionNodes = new ArrayList<ProtocolExecutionNode>();

        for (String column : columns) {

            if (column.matches(Date.REGEXP)) {
                Date date = new Date(index, column);
                if (lastProtocolExecutionNode!=null){
                    lastProtocolExecutionNode.addDate(date);
                }

            }else if (column.matches(Performer.REGEXP)) {
                Performer performer = new Performer(index, column);
                if (lastProtocolExecutionNode!=null){
                    lastProtocolExecutionNode.addPerformer(performer);
                }

            } else if (column.matches(ProtocolExecutionNode.REGEXP)){
                ProtocolExecutionNode protocolExecutionNode = new ProtocolExecutionNode(index, column);
                protocolExecutionNodes.add(protocolExecutionNode);
                lastProtocolExecutionNode = protocolExecutionNode;

                if (lastMaterialOrData != null) {
                    protocolExecutionNode.setInputNode(lastMaterialOrData);
                }

                graph.addNode(protocolExecutionNode);

            }else if (column.matches(ProcessNode.REGEXP)) {
                ProcessNode processNode = new ProcessNode(index, column);

                graph.addNode(processNode);
                if (lastMaterialOrData != null) {
                    //but it could be a DataNode rather than a material node... do I need a new object?
                    //processNode.setInputNode(
                    //       new MaterialNode(lastMaterialOrData.getIndex(), lastMaterialOrData.getName()));
                    processNode.setInputNode(lastMaterialOrData);
                }
                lastProcess = processNode;
                if (lastProcess!=null) {
                    lastProcess.addProtocolExecutionNodes(protocolExecutionNodes);
                    protocolExecutionNodes =  new ArrayList<ProtocolExecutionNode>();
                }

            }  else if (column.contains(ISADataNode.CONTAINS) && !column.contains("Comment")) {
                NodeWithComments dataNode = new DataNode(index, column);
                graph.addNode(dataNode);
                lastMaterialOrData = dataNode;

                if (lastProcess != null) {
                    lastProcess.setOutputNode(dataNode);
                    lastProcess = null;
                } else if (lastProtocolExecutionNode !=null){
                    lastProtocolExecutionNode.setOutputNode(dataNode);
                    lastProtocolExecutionNode = null;
                }

                if (lastMaterialOrData !=null && lastProcess==null && lastProtocolExecutionNode!=null){
                    lastProtocolExecutionNode.setOutputNode(dataNode);
                    protocolExecutionNodes =  new ArrayList<ProtocolExecutionNode>();
                }

            } else if (column.matches(ISAMaterialAttribute.REGEXP)) {

                ISAMaterialAttribute materialAttribute = new MaterialAttribute(index, column);
                if (lastMaterialOrData != null && lastMaterialOrData instanceof MaterialNode) {
                    ((MaterialNode) graph.getNode(lastMaterialOrData.getIndex())).addMaterialAttribute(materialAttribute);
                }

            } else if (column.matches(MaterialNode.REGEXP)) {

                NodeWithComments materialNode = null;
                if (column.matches(ISASampleNode.REGEXP)) {
                    materialNode = new SampleNode(index, column);
                    lastSample = materialNode;
                } else {
                    materialNode = new MaterialNode(index, column);

                }
                //if there is a previous material node
                //and no process node, add a dummy process node
                if (lastMaterialOrData !=null && lastProcess==null && lastProtocolExecutionNode!=null){
                    lastProtocolExecutionNode.setOutputNode(materialNode);
                    protocolExecutionNodes =  new ArrayList<ProtocolExecutionNode>();
                }

                graph.addNode(materialNode);
                lastMaterialOrData = materialNode;
                if (lastProcess != null) {
                    lastProcess.setOutputNode(materialNode);
                    lastProcess = null;
                }

            } else if (column.matches(ISAFactorValue.REGEXP)) {

                ISAFactorValue factorValue = new FactorValue(index, column);
                lastFactorValue = factorValue;
                if (lastSample != null && lastSample instanceof SampleNode) {
                    ((SampleNode) graph.getNode(lastSample.getIndex())).addFactorValue(factorValue);
                }

            }else if (column.matches(ISAUnit.REGEXP)) {

                ISAUnit unit = new Unit(index, column);
                if (lastFactorValue!=null){
                    lastFactorValue.setUnit(unit);
                }


            } else if (column.matches(ProcessParameter.REGEXP)){

                ProcessParameter parameter = new ProcessParameter(index, column);

                if (lastProtocolExecutionNode!=null){
                    ((ProcessNode)graph.getNode(lastProtocolExecutionNode.getIndex())).addParameter(parameter);
                }

            } else if (column.matches(CommentNode.REGEXP)){

                CommentNode commentNode = new CommentNode(index, column);

                if (lastProcess != null && lastMaterialOrData!=null && lastProtocolExecutionNode!=null) {

                    //lastProcess greater index
                    if (lastProcess.getIndex() > lastMaterialOrData.getIndex() && lastProcess.getIndex() > lastProtocolExecutionNode.getIndex()){
                        lastProcess.addComment(commentNode);
                    }else if (lastMaterialOrData.getIndex() > lastProcess.getIndex() && lastMaterialOrData.getIndex() > lastProtocolExecutionNode.getIndex()){
                        lastMaterialOrData.addComment(commentNode);
                    }else if (lastProtocolExecutionNode.getIndex() > lastProcess.getIndex() && lastProtocolExecutionNode.getIndex() > lastMaterialOrData.getIndex()){
                        lastProtocolExecutionNode.addComment(commentNode);
                    }

                }  else { //one of them is null

                    if (lastProcess!=null && lastMaterialOrData!=null){

                        if (lastProcess.getIndex()>lastMaterialOrData.getIndex()){
                            lastProcess.addComment(commentNode);
                        } else {
                            lastMaterialOrData.addComment(commentNode);
                        }

                    } else if (lastProcess!=null && lastProtocolExecutionNode!=null){

                        if (lastProcess.getIndex()>lastProtocolExecutionNode.getIndex()){
                            lastProcess.addComment(commentNode);
                        } else{
                            lastProtocolExecutionNode.addComment(commentNode);
                        }

                    } else if (lastMaterialOrData!=null && lastProtocolExecutionNode!=null){

                        if (lastMaterialOrData.getIndex() > lastProtocolExecutionNode.getIndex()){
                            lastMaterialOrData.addComment(commentNode);
                        } else {
                            lastProtocolExecutionNode.addComment(commentNode);
                        }

                    } else {

                        //only one is not null

                        if (lastMaterialOrData!=null){
                            lastMaterialOrData.addComment(commentNode);
                        } else if (lastProcess!=null){
                            lastProcess.addComment(commentNode);
                        } else if (lastProtocolExecutionNode!=null){
                            lastProtocolExecutionNode.addComment(commentNode);
                        }



                    }


                }
            }
            index++;
        }
    }

    /**
     * @return Returns a Map of the processes present, and a count of how many times that process was used.
     */
    public Map<String, Integer> extractProcesses() {
        return extractNodes(NodeType.PROCESS_NODE);
    }

    /**
     * @return Returns a Map of the data files present, and a count of how many times that data was used.
     */
    public Map<String, Integer> extractDataNodes() {
        return extractNodes(NodeType.DATA_NODE);
    }

    /**
     *
     * @return Map with structure: < attribute name , < attribute value , row count >>
     */
    public Map<String, Map<String, Integer>> extractMaterialAttributes() {
        Map<String, Map<String, Integer>> resultNodes
                = new HashMap<String, Map<String, Integer>>();

        List<ISANode> node = graph.getNodes(NodeType.MATERIAL_NODE);

        for (ISANode nodeOfInterest : node) {
            // extract the values!
            MaterialNode materialNode = (MaterialNode) nodeOfInterest;

            for (ISANode property : materialNode.getMaterialAttributes()) {

                for (int rowIndex = 1; rowIndex < assayTable.length; rowIndex++) {

                    if (property.getIndex() < assayTable[rowIndex].length) {
                        String[] row = Arrays.copyOf(assayTable[rowIndex], assayTable[rowIndex].length, String[].class);

                        String value = row[property.getIndex()];
                        if (value != null && !value.equals("")) {

                            if (!resultNodes.containsKey(property.getName())) {
                                resultNodes.put(property.getName(), new HashMap<String, Integer>());
                            }

                            if (!resultNodes.get(property.getName()).containsKey(value)) {
                                resultNodes.get(property.getName()).put(value, 1);
                            } else {
                                int newCount = resultNodes.get(property.getName()).get(value) + 1;
                                resultNodes.get(property.getName()).put(value, newCount);
                            }
                        }
                    }
                }
            }
        }
        return resultNodes;
    }

    /**
     *
     * @param type
     * @return a map whose keys are the values corresponding to nodes of type 'type' and the values are the number of them
     */
    private Map<String, Integer> extractNodes(NodeType type) {
        Map<String, Integer> resultNodes = new HashMap<String, Integer>();

        List<ISANode> node = graph.getNodes(type);

        for (ISANode nodeOfInterest : node) {
            // extract the values!
            for (int rowIndex = 1; rowIndex < assayTable.length; rowIndex++) {
                if (nodeOfInterest.getIndex() < assayTable[rowIndex].length) {

                    String[] row = Arrays.copyOf(assayTable[rowIndex], assayTable[rowIndex].length, String[].class);
                    String value = row[nodeOfInterest.getIndex()];
                    if (value != null && !value.equals("")) {
                        if (!resultNodes.containsKey(value)) {
                            resultNodes.put(value, 1);
                        } else {
                            int newCount = resultNodes.get(value) + 1;
                            resultNodes.put(value, newCount);
                        }

                    }
                }
            }
        }

        return resultNodes;
    }

    private void extractGroups() {
        groups = getDataGroupsWithTypeByColumn(assayTable, "Factor", false, false);
    }

    public Map<String, StudyGroup> getGroups() {
        return groups;
    }

    public org.isatools.graph.model.impl.Graph getGraph() {
        return graph;
    }


    /***
     *
     * Method to create the data groups.
     * This method determines the labels to be used for groups.
     *
     * @param fileContents a matrix of Objects, which is the contents of the spreadsheet
     * @param group a string indicating the column to consider to form the groups, e.g. Factor
     * @param exactMatch true or false indicating whether the string match is exact or not
     * @param includeColumnNames true or false indicating if the columns names are included in the result
     *                           (e.g if true the result will be 'Factor Value[<specific value>]', otherwise it will be '<specific value>' only)
     * @return Map<String, String>
     */
    private Map<String, StudyGroup> getDataGroupsWithTypeByColumn(Object[][] fileContents,
                                                                   String group,
                                                                   boolean exactMatch,
                                                                   boolean includeColumnNames) {

        //map for resulting groups
        //group name, study group object
        Map<String, StudyGroup> groups = new HashMap<String, StudyGroup>();


        String[] columnNames = Arrays.copyOf(fileContents[0],fileContents[0].length, String[].class);

        boolean allowedUnit = false;
        for (int row = 1; row < fileContents.length; row++) {
            Map<String, String> groupDefinition = new HashMap<String, String>();

            String groupVal = "";
            int elementsNumber = 0;
            for (int col = 0; col < columnNames.length; col++) {
                String column = columnNames[col];

                boolean match = false;

                if (exactMatch) {
                    if (column.equalsIgnoreCase(group)) {
                        match = true;
                    } else if (allowedUnit && column.equalsIgnoreCase("unit")) {
                        match = true;
                    }
                } else {
                    if (column.contains(group)) {
                        match = true;
                    } else if (allowedUnit && column.equalsIgnoreCase("unit")) {
                        match = true;
                    }
                }

                if (match) {
                    if (!fileContents[row][col].equals("")) {
                        groupVal += (elementsNumber>0? "|": "") + (includeColumnNames ? extractColumnType(column)+"=" : " ") + fileContents[row][col];
                        groupDefinition.put(extractColumnType(column), (String) fileContents[row][col]);
                        elementsNumber++;
                        allowedUnit = true;
                    }
                } else allowedUnit = column.contains("Term Source REF") || column.contains("Term Accession Number");
            }
            if (!groupVal.equals("")) {
                groupVal = groupVal.trim();


                if (!groups.containsKey(groupVal)) {
                    groups.put(groupVal, new StudyGroup(group, groupVal));
                }

                StudyGroup studyGroup = groups.get(groupVal);
                studyGroup.addGroupMember(getColValAtRow(fileContents, columnNames, "Sample Name", row));
                studyGroup.setGroupDefinition(groupDefinition);
            }
        }

        return groups;
    }

    private String extractColumnType(String column){
        if (column.indexOf('[')!=-1)
            return column.substring(column.indexOf('[')+1, column.indexOf(']'));
        else
            return column;
    }

    private String getColValAtRow(Object[][] fileContents, String[] columnNames, String colName, int rowNumber) {
        for (int col = 0; col < columnNames.length; col++) {

            if (columnNames[col].equalsIgnoreCase(colName)) {
                // safety precaution to finalise any cells. otherwise their value would be missed!
                return (String)fileContents[rowNumber][col];
            }
        }
        return "";
    }
}


