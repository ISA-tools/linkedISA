package org.isatools.isa2owl.converter;

import org.semanticweb.owlapi.model.IRI;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 25/10/2012
 * Time: 15:56
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class IRIGenerator {


    private static Map<String, Integer> identifiers = new HashMap<String, Integer>();

    public IRIGenerator(){
    }

    public static IRI getIRI(IRI baseIRI, String type){
         //return IRI.create(baseIRI+UUID.randomUUID().toString());

        if (type.contains("http"))
            type = type.substring(type.lastIndexOf("/")+1, type.length());
        String new_type = type.toLowerCase().replace(' ', '_');

        int new_id;
        Integer previous = identifiers.get(new_type);
        if (previous == null) {
            identifiers.put(new_type, new Integer(1));
            new_id = 1;
        } else {
            new_id = previous.intValue()+1;
            identifiers.put(new_type, new Integer(new_id));
        }

        System.out.println("IDENTIFIER ===> "+ baseIRI+ "/" + new_type + "/"+new_id);
        return IRI.create(baseIRI+ "/" + new_type + "/"+new_id);
    }

}
