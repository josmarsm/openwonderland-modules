/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

package org.jdesktop.wonderland.modules.eventrecorder.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 /**
 * Represents a changes file, Using this, the
 * web services knows which changes file to write to or read from.
 *
 * @author Bernard Horan
 */
@XmlRootElement(name="changes-file")
public class ChangesFile {
    private File file;
    @XmlElement(name="timestamp")
    private long timeOfLastChange;
    final private static String ENCODING = "ISO-8859-1";
    private PrintWriter changesWriter;

    /* The XML marshaller and unmarshaller for later use */
    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;

    /* Create the XML marshaller and unmarshaller once for all ChangesFiles */
    static {
        try {
            JAXBContext jc = JAXBContext.newInstance(ChangesFile.class);
            ChangesFile.unmarshaller = jc.createUnmarshaller();
            ChangesFile.marshaller = jc.createMarshaller();
            ChangesFile.marshaller.setProperty("jaxb.formatted.output", true);
        } catch (javax.xml.bind.JAXBException excp) {
            Logger.getLogger(ChangesFile.class.getName()).log(Level.WARNING,
                    "[WFS] Unable to create JAXBContext", excp);
        }
    }

    /**
     * Default constructor
     */
    public ChangesFile() {

    }

    /**
     * Gets an instance of WFSRecording for the given directory.  If there
     * is no WFS changesFile in the directory, it will be created.
     *
     * @param file -- is assumed not to exist
     * @param timestamp 
     * @throws IOException if there is an error writing to the file
     */
    public ChangesFile(File file, long timestamp) throws IOException {
        setFile(file);
        timeOfLastChange = timestamp;
    }

    /**
     * Delete the underlying file
     */
    public void delete() {
        file.delete();
    }

    /**
     * Set the underlying file, open a printwriter and write out the XML header info
     * @param file the underlying file, whose pathname should end in changes.xml
     * @throws java.io.FileNotFoundException
     */
    private void setFile(File file) throws FileNotFoundException {
        this.file = file;
        changesWriter = new PrintWriter(new FileOutputStream(file), true);
        changesWriter.println("<?xml version=\"1.0\" encoding=\"" + ENCODING + "\"?>");
        changesWriter.println("<Wonderland_Recorder>");
        changesWriter.println("<Wonderland_Changes>");
    }

    /**
     * Write out the footer for the printWriter and then close the writer
     * (and indirectly the underlying file)
     */
    public void closeFile() {
        changesWriter.println("</Wonderland_Changes>");
        changesWriter.println("</Wonderland_Recorder>");
        changesWriter.close();
    }

    /**
     * Get the name of this recording
     * @return name the name of this recording
     */
    @XmlTransient
    public String getName() {
        return file.getName();
    }

    /**
     * Takes a reader for the XML stream and returns an instance of this class
     * <p>
     * @param r The reader of the XML stream
     * @return
     * @throws JAXBException Upon error reading the XML stream
     * @throw ClassCastException If the input file does not map to this class
     */
    public static ChangesFile decode(Reader r) throws JAXBException {
        return (ChangesFile)ChangesFile.unmarshaller.unmarshal(r);
    }

    /**
     * Writes the XML representation of this class to a writer.
     * <p>
     * @param w The output writer to write to
     * @throws JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        ChangesFile.marshaller.marshal(this, w);
    }

    @Override
    public String toString() {
        return this.getName();
    }



}
