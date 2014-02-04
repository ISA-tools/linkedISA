package org.isatools.isa2owl.repository;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 04/02/2014
 * Time: 15:21
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * This class is inspired by Jeen Broekstra
 * http://rivuli-development.com/further-reading/sesame-cookbook/loading-large-file-in-sesame-native/
 */
public class ChunkCommitter implements RDFHandler {

    private final long chunkSize;
    private final RDFInserter inserter;
    private final RepositoryConnection conn;
    private final URI context;
    private final ValueFactory factory;

    private long count = 0L;

    public ChunkCommitter(RepositoryConnection conn, URI context, long chunkSize) {
        this.chunkSize = chunkSize;
        this.context = context;
        this.conn = conn;
        this.factory = conn.getValueFactory();
        inserter = new RDFInserter(conn);
    }

    public long getStatementCount() {
        return count;
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        inserter.startRDF();
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        inserter.endRDF();
    }

    @Override
    public void handleNamespace(String prefix, String uri)
            throws RDFHandlerException {
        inserter.handleNamespace(prefix, uri);
    }

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        if(context !=null) {
            st = factory.createStatement(st.getSubject(), st.getPredicate(), st.getObject(), context);
        }
        inserter.handleStatement(st);
        count++;
        // do an intermittent commit whenever the number of triples
        // has reached a multiple of the chunk size
        if (count % chunkSize == 0) {
            try {
                conn.commit();
                System.out.print(".");
            } catch (RepositoryException e) {
                throw new RDFHandlerException(e);
            }
        }
    }

    @Override
    public void handleComment(String comment) throws RDFHandlerException {
        inserter.handleComment(comment);
    }
}