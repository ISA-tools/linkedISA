package org.isatools.linkedISA.converter;

import java.io.File;

/**
 * Created by agbeltran on 19/06/2014.
 *
 * Given a folder with many studies, it converts all the studies.
 *
 */
public class ISAtab2OWLFolderConverter {

    private ISAtab2OWLConverter isatab2OWLConverter = null;

    public ISAtab2OWLFolderConverter(ISAtab2OWLConverter isa2owl){
        isatab2OWLConverter = isa2owl;
    }

    public void convert(String inputFolder, String uriPrefix, String outputFolder) throws Exception {

        File inputFolderFile = new File(inputFolder);

        if (!inputFolderFile.isDirectory()) {
            return;
        }

        File[] inputFiles = inputFolderFile.listFiles();

        for(File inputFile: inputFiles){
            System.out.println("============ Converting "+inputFile);

            String path = inputFile.getAbsolutePath();
            String name = path.substring(path.lastIndexOf('/')+1);
            if (!name.equals("sdata20149-isa1"))
                continue;
            isatab2OWLConverter.convert(path, uriPrefix + name);

            //System.out.println("Minted IRIs");
            //System.out.println(isatab2OWLConverter.getMintedIRIs());

            isatab2OWLConverter.saveOntology(outputFolder + name + ".rdf");
        }
    }


}
