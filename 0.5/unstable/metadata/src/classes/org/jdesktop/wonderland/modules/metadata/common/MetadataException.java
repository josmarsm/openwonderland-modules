/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.common;

/**
 *
 * @author Matt
 */
public class MetadataException extends Exception {

    /**
     * Creates a new instance of <code>MetadataException</code> without detail message.
     */
    public MetadataException() {
    }


    /**
     * Constructs an instance of <code>MetadataException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public MetadataException(String msg) {
        super(msg);
    }
}
