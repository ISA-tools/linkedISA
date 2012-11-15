package org.isatools.graph.model;

import org.isatools.isa2owl.converter.ExtendedISASyntax;
import org.isatools.isacreator.model.GeneralFieldTypes;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 06/11/2012
 * Time: 16:11
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class MaterialNode extends Node {

    public static final String REGEXP = "(Source.*)|(Sample.*)|(Extract.*)|(Labeled Extract.*)";

    private List<MaterialAttribute> materialAttributes;

    public MaterialNode(int index, String name) {
        super(index, name);
        materialAttributes = new ArrayList<MaterialAttribute>();
    }

    public void addMaterialAttribute(MaterialAttribute attribute) {
        materialAttributes.add(attribute);
    }

    public List<MaterialAttribute> getMaterialAttributes() {
        return materialAttributes;
    }

    public String getMaterialNodeType(){

        if (getName().equals(GeneralFieldTypes.SAMPLE_NAME.toString()))
            return ExtendedISASyntax.SAMPLE;

        if (getName().equals(GeneralFieldTypes.SOURCE_NAME.toString()))
            return ExtendedISASyntax.SOURCE;

        if (getName().equals(GeneralFieldTypes.EXTRACT_NAME.toString()))
            return ExtendedISASyntax.EXTRACT;

        if (getName().equals(GeneralFieldTypes.LABELED_EXTRACT_NAME.toString()))
            return ExtendedISASyntax.LABELED_EXTRACT;

        return null;

    }

}