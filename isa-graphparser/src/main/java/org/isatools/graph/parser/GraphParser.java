package org.isatools.graph.parser;


import org.isatools.graph.model.*;
import org.isatools.graph.model.impl.*;

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
    private Map<String, Set<String>> groups;

    public GraphParser(Object[][] assayTable) {
        this.assayTable = assayTable;
        groups = new HashMap<String, Set<String>>();
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
        AssayNode lastAssayNode = null;
        ISAFactorValue lastFactorValue = null;

        for (String column : columns) {

            if (column.matches(ProcessNode.REGEXP)) {
                ProcessNode processNode = new ProcessNode(index, column);
                graph.addNode(processNode);
                if (lastMaterialOrData != null) {
                    processNode.addInputNode(
                            new MaterialNode(lastMaterialOrData.getIndex(), lastMaterialOrData.getName()));
                }
                lastProcess = processNode;
                if (lastAssayNode!=null)
                    lastAssayNode.addAssociatedProcessNode(processNode);

            } else if (column.matches(AssayNode.REGEXP)){

                AssayNode assayNode = new AssayNode(index, column);
                if (lastMaterialOrData != null) {
                    assayNode.addInputNode(
                            new MaterialNode(lastMaterialOrData.getIndex(), lastMaterialOrData.getName()));
                }

                if (lastProcess!=null)
                    assayNode.addAssociatedProcessNode(lastProcess);

                graph.addNode(assayNode);
                lastAssayNode = assayNode;


            }else if (column.contains(ISADataNode.CONTAINS)){ //&& !column.matches(DataNode.REGEXP)) {
                NodeWithComments dataNode = new DataNode(index, column);
                graph.addNode(dataNode);
                lastMaterialOrData = dataNode;

                if (lastProcess != null) {
                    lastProcess.addOutputNode(dataNode);
                    lastProcess = null;
                }
                if (lastAssayNode !=null){
                    lastAssayNode.addOutputNode(dataNode);
                    lastAssayNode = null;
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
                graph.addNode(materialNode);
                lastMaterialOrData = materialNode;
                if (lastProcess != null) {
                    lastProcess.addOutputNode(materialNode);
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

                if (lastProcess!=null){
                    ((ProcessNode)graph.getNode(lastProcess.getIndex())).addParameter(parameter);
                }

            } else if (column.matches(CommentNode.REGEXP)){

                CommentNode commentNode = new CommentNode(index, column);
                if (lastProcess != null) {
                    lastProcess.addComment(commentNode);
                }
                if (lastAssayNode !=null){
                    lastAssayNode.addComment(commentNode);
                }
                if (lastMaterialOrData != null){
                    lastMaterialOrData.addComment(commentNode);
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
        groups = getDataGroupsWithTypeByColumn(assayTable, "Factor", false, true);
    }

    public Map<String, Set<String>> getGroups() {
        return groups;
    }

    public Graph getGraph() {
        return graph;
    }


    /***
     *
     *
     *
     * @param fileContents a matrix of Objects, which is the contents of the spreadsheet
     * @param group a string indicating the column to consider to form the groups
     * @param exactMatch true or false indicating whether the string match is exact or not
     * @param includeColumnNames true or false indicating
     * @return
     */
    private Map<String, Set<String>> getDataGroupsWithTypeByColumn(Object[][] fileContents,
                                                                   String group,
                                                                   boolean exactMatch,
                                                                   boolean includeColumnNames) {

        //map for resulting groups
        Map<String, Set<String>> groups = new HashMap<String, Set<String>>();

        String[] columnNames = Arrays.copyOf(fileContents[0],fileContents[0].length, String[].class);

        boolean allowedUnit = false;
        for (int row = 1; row < fileContents.length; row++) {
            String groupVal = "";
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
                    groupVal += " " + (includeColumnNames ? column : "") + " " + fileContents[row][col];
                    allowedUnit = true;
                } else allowedUnit = column.contains("Term Source REF") || column.contains("Term Accession Number");
            }
            if (!groupVal.equals("")) {
                groupVal = groupVal.trim();

                if (!groups.containsKey(groupVal)) {
                    groups.put(groupVal, new HashSet<String>());
                }

                groups.get(groupVal).add(getColValAtRow(fileContents, columnNames, "Sample Name", row));
            }
        }

        return groups;
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


