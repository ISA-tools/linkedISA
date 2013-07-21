package org.isatools.owl;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 01/07/2013
 * Time: 14:30
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class Upload {


    public void upload1(String[] args){
        File file = new File("BII-I-1.owl");
        System.out.println("absolute path="+file.getAbsolutePath());
        String baseURI = "http://isa-tools.org/isa/BII-I-1.owl";

        Repository myRepository = new SailRepository(new MemoryStore());

        try {
            myRepository.initialize();
            RepositoryConnection con = myRepository.getConnection();
            try {
                con.add(file, baseURI, RDFFormat.RDFXML);
                URL url = new URL("http://bii.oerc.ox.ac.uk:8181/openrdf-sesame/home/overview.view");
                con.add(url, url.toString(), RDFFormat.RDFXML);
            }
            finally {
                con.close();
            }
        }
        catch(RepositoryException e){
            System.out.println("Repository exception...");
            e.printStackTrace();

        }catch (OpenRDFException e) {
            System.out.println("OpenRDFException...");
            e.printStackTrace();
        }
        catch (IOException e) {
            System.out.println("IOException...");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        try{
            RepositoryManager repositoryManager = new RemoteRepositoryManager("http://bii.oerc.ox.ac.uk:8181/openrdf-sesame/home/overview.view");
            repositoryManager.initialize();

            System.out.println("url="+repositoryManager.getLocation());
            Collection<Repository> repositoryCollection = repositoryManager.getAllRepositories();

            for(Repository repository: repositoryCollection){
                System.out.println("repo="+repository);
            }

        }catch(RepositoryException e){
            System.out.println("Repository exception...");
            e.printStackTrace();
        }catch(MalformedURLException e){
            System.out.println("MalformedURLException exception...");
            e.printStackTrace();
        }catch(RepositoryConfigException e){
            System.out.println("RepositoryConfigException exception...");
            e.printStackTrace();
        }
    }

}