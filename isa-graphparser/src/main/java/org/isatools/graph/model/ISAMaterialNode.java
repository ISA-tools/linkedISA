package org.isatools.graph.model;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 04/06/2013
 * Time: 17:35
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public interface ISAMaterialNode extends ISANode {

    public static final String REGEXP = "(Source.*)|(Sample.*)|(Extract.*)|(Labeled Extract.*)";

    public void addMaterialAttribute(ISAMaterialAttribute attribute);

    public Iterable<ISAMaterialAttribute> getMaterialAttributes();

    public String getMaterialNodeType();

    public String toString();
}
