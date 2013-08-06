package org.isatools.graph.model;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 25/07/2013
 * Time: 01:40
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public interface ISASampleNode extends ISAMaterialNode {

    public static final String REGEXP = "(Sample.*)";

    public Iterable<ISAFactorValue> getFactorValues();

    public void addFactorValue(ISAFactorValue factorValue);
}
