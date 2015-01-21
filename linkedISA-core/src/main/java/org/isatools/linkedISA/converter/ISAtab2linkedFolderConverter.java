package org.isatools.linkedISA.converter;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by agbeltran on 19/06/2014.
 *
 * Given a folder with many studies, it converts all the studies.
 *
 */
public class ISAtab2LinkedFolderConverter {

    private ISAtab2LinkedConverter isatab2OWLConverter = null;

    public ISAtab2LinkedFolderConverter(ISAtab2LinkedConverter isa2owl){
        isatab2OWLConverter = isa2owl;
    }



    public void convert(String inputFolder, String uriPrefix, String outputFolder) throws Exception{
        convert(inputFolder, uriPrefix, outputFolder, null);
    }


    public void convert(String inputFolder, String uriPrefix, String outputFolder, String stringPattern) throws Exception {

        File inputFolderFile = new File(inputFolder);

        if (!inputFolderFile.isDirectory()) {
            return;
        }

        File[] inputFiles = inputFolderFile.listFiles();

        Pattern pattern = null;

        if (stringPattern!=null){
            pattern = Pattern.compile(stringPattern);
        }

        for(File inputFile: inputFiles){

            Matcher matcher = null;

            if (stringPattern!=null){
                matcher = pattern.matcher(inputFile.getName());
                if (!matcher.matches())
                    continue;
            }

            System.out.println("============ Converting "+inputFile);

            String path = inputFile.getAbsolutePath();
            String name = path.substring(path.lastIndexOf('/')+1);
            isatab2OWLConverter.convert(path, uriPrefix + name);
            isatab2OWLConverter.saveOntology(outputFolder + name + ".rdf");
        }
    }


}
