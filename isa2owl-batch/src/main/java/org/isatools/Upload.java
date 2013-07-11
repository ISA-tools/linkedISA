package org.isatools;

import java.io.File;



/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 27/06/2013
 * Time: 14:57
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class Upload {


    public void upload(){

        File file = new File("/path/to/example.rdf");
        String baseURI = "http://example.org/example/local";

   //    Repository myRepository = new SailRepository(new MemoryStore());
//        myRepository.initialize();
//
//        try {
//            RepositoryConnection con = myRepository.getConnection();
//            try {
//                con.add(file, baseURI, RDFFormat.RDFXML);
//
//                URL url = new URL("http://example.org/example/remote");
//                con.add(url, url.toString(), RDFFormat.RDFXML);
//            }
//            finally {
//                con.close();
//            }
//        }
//        catch (OpenRDFException e) {
//            // handle exception
//        }
//        catch (java.io.IOEXception e) {
//            // handle io exception
//        }

    }

}
