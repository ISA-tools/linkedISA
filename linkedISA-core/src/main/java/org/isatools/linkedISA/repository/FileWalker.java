package org.isatools.linkedISA.repository;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 04/02/2014
 * Time: 15:09
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Utility for a depth first traversal of a file-system starting from a given node (file or directory).
 */
public class FileWalker {

    /**
     * The call back interface for traversal.
     */
    public interface Handler {
        /**
         * Called to notify that a normal file has been encountered.
         *
         * @param file
         *            The file encountered.
         */
        void file(File file) throws Exception;

        /**
         * Called to notify that a directory has been encountered.
         *
         * @param directory
         *            The directory encountered.
         */
        void directory(File directory) throws Exception;
    }

    /**
     * Set the notification handler.
     *
     * @param handler
     *            The object that receives notifications of encountered nodes.
     */
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    /**
     * Start the walk at the given location, which can be a file, for a very short walk, or a directory
     * which will be traversed recursively.
     *
     * @param node
     *            The starting point for the walk.
     */
    public void walk(File node) throws Exception {
        if (node.isDirectory()) {
            handler.directory(node);
            File[] children = node.listFiles();
            Arrays.sort(children, new Comparator<File>() {

                @Override
                public int compare(File lhs, File rhs) {
                    return lhs.getName().compareTo(rhs.getName());
                }

            });
            for (File child : children) {
                walk(child);
            }
        } else {
            handler.file(node);
        }
    }

    private Handler handler;
}

