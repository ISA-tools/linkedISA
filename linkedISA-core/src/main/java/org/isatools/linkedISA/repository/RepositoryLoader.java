package org.isatools.linkedISA.repository;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;

import java.io.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 04/02/2014
 * Time: 13:44
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class RepositoryLoader {

    private static final Logger log = Logger.getLogger(RepositoryLoader.class);
    private RepositoryManager repositoryManager = null;

    // From repository.getConnection() - the connection through which we will
    // use the repository
    private RepositoryConnection repositoryConnection;

    // A list of datatype handling strategies
    private static final RDFParser.DatatypeHandling allDatatypeHandling[] = new RDFParser.DatatypeHandling[] {
            RDFParser.DatatypeHandling.IGNORE, RDFParser.DatatypeHandling.NORMALIZE, RDFParser.DatatypeHandling.VERIFY
    };

    public RepositoryLoader(){
        try {
            repositoryManager =
                new RemoteRepositoryManager( "http://127.0.0.1:8080/openrdf-sesame" );
            repositoryManager.initialize();
        }catch(RepositoryException ex){
            ex.printStackTrace();
            System.err.println("Could not initialize the repository manager");
        }
    }

    /**
     * Parses and loads all files specified in PARAM_PRELOAD
     */
    public void loadFiles() throws Exception {
        System.out.println("===== Load Files (from the '" + Parameters.PARAM_PRELOAD + "' parameter) ==========");

        final AtomicLong statementsLoaded = new AtomicLong();

        // Load all the files from the pre-load folder
        String preload_param = Parameters.get(Parameters.PARAM_PRELOAD);

        InputStream preload = RepositoryLoader.class.getClass().getResourceAsStream(preload_param);
        System.out.println("preload="+preload);

        if (preload == null)
            System.out.println("No pre-load directory/filename provided.");
        else {
            FileWalker.Handler handler = new FileWalker.Handler() {

                @Override
                public void file(File file) throws Exception {
                    statementsLoaded.addAndGet( loadFileChunked(file) );
                }

                @Override
                public void directory(File directory) throws Exception {
                    System.out.println("Loading files from: " + directory.getAbsolutePath());
                }
            };

            FileWalker walker = new FileWalker();
            walker.setHandler(handler);
            walker.walk(new File(preload.toString()));
        }

        System.out.println("TOTAL: " + statementsLoaded.get() + " statements loaded");
    }


    private long loadFileChunked(File file) throws RepositoryException, IOException {

        //log("Loading file '" + file.getName());
        System.out.print("Loading " + file.getName() + " ");

        RDFFormat format = RDFFormat.forFileName(file.getName());

        if(format == null) {
            System.out.println();
            System.out.println("Unknown RDF format for file: " + file);
            return 0;
        }

        URI dumyBaseUrl = new URIImpl(file.toURI().toString());

        URI context = null;
        if(!format.equals(RDFFormat.NQUADS) && !format.equals(RDFFormat.TRIG) && ! format.equals(RDFFormat.TRIX)) {
            String contextParam = null;//parameters.get(PARAM_CONTEXT);

            if (contextParam == null) {
                context = new URIImpl(file.toURI().toString());
            } else {
                if (contextParam.length() > 0) {
                    context = new URIImpl(contextParam);
                }
            }
        }

        InputStream reader = null;
        try {
            if(file.getName().endsWith("gz")) {
                reader = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file), 256 * 1024));
            }
            else {
                reader = new BufferedInputStream(new FileInputStream(file), 256 * 1024);
            }

            boolean verifyData = true;//isTrue(PARAM_VERIFY);
            boolean stopAtFirstError = true;//isTrue(PARAM_STOP_ON_ERROR);
            boolean preserveBnodeIds = true;//isTrue(PARAM_PRESERVE_BNODES);
            RDFParser.DatatypeHandling datatypeHandling = stringToDatatypeHandling( Parameters.get(Parameters.PARAM_DATATYPE_HANDLING));
            long chunkSize = Long.parseLong(Parameters.get(Parameters.PARAM_CHUNK_SIZE));

            ParserConfig config = new ParserConfig(verifyData, stopAtFirstError, preserveBnodeIds, datatypeHandling);

            // set the parser configuration for our connection
            repositoryConnection.setParserConfig(config);
            RDFParser parser = Rio.createParser(format);

            // add our own custom RDFHandler to the parser. This handler takes care of adding
            // triples to our repository and doing intermittent commits
            ChunkCommitter handler = new ChunkCommitter(repositoryConnection, context, chunkSize);
            parser.setRDFHandler(handler);
            parser.parse(reader, context == null ? dumyBaseUrl.toString() : context.toString());
            repositoryConnection.commit();
            long statementsLoaded = handler.getStatementCount();
            System.out.println( " " + statementsLoaded + " statements");
            return statementsLoaded;
        } catch (Exception e) {
            repositoryConnection.rollback();
            System.out.println();
            log.debug("Failed to load '" + file.getName() + "' (" + format.getName() + ")." + e);
            e.printStackTrace();
            return 0;
        } finally {
            if (reader != null)
                reader.close();
        }
    }


    private static RDFParser.DatatypeHandling stringToDatatypeHandling(String strHandling) {
        for (RDFParser.DatatypeHandling handling : allDatatypeHandling) {
            if (handling.name().equalsIgnoreCase(strHandling))
                return handling;
        }
        throw new IllegalArgumentException("Datatype handling strategy for parsing '" + strHandling + "' is not recognised");
    }

    public static void main(String[] args) throws Exception {
        RepositoryLoader loader = new RepositoryLoader();

        Parameters.setDefaultValue(Parameters.PARAM_PRELOAD, "toload");

        loader.loadFiles();

    }
}
