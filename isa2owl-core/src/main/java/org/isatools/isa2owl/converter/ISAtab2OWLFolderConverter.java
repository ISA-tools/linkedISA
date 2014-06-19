package org.isatools.isa2owl.converter;

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
            String path = inputFile.getAbsolutePath();
            String name = path.substring(path.lastIndexOf('/')+1);
            isatab2OWLConverter.convert(path, uriPrefix + name);
            isatab2OWLConverter.saveInferredOntology(outputFolder + name + ".rdf");
        }
    }


}
