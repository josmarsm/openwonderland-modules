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
package org.jdesktop.wonderland.modules.pdf.client;

import java.io.InputStream;
import java.io.OutputStream;
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
 * This class represents a PDF document that has been "deployed" to the system.
 * It contains meta-information about the document (e.g. number pages, width
 * and height) for a PDF document.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name = "deployed-pdf")
public class DeployedPDF {

    // The error logger
    private static final Logger LOGGER =
            Logger.getLogger(DeployedPDF.class.getName());

    // The JAXB serialization context
    private static JAXBContext jaxbContext = null;
    static {
        try {
            jaxbContext = JAXBContext.newInstance(DeployedPDF.class);
        } catch (javax.xml.bind.JAXBException excp) {
            LOGGER.log(Level.WARNING, "Unable to create JAXB Context", excp);
        }
    }

    // The name of the PDF file. Note this is just the name of the file and
    // should not contain any path information.
    @XmlElement(name = "pdf-file-name")
    private String pdfFileName = null;

    // The number of slides
    @XmlElement(name = "number-of-slides")
    private int numberOfSlides = 0;

    // The maximum width (in pixels) of the slides
    @XmlElement(name = "maximum-slide-width")
    private int maximumSlideWidth = 0;

    // The maximum height (in pixels) of the slides
    @XmlElement(name = "maximum-slide-height")
    private int maximumSlideHeight = 0;

    /**
     * Default constructor, needed for JAXB
     */
    public DeployedPDF() {
    }

    @XmlTransient
    public int getNumberOfSlides() {
        return numberOfSlides;
    }

    public void setNumberOfSlides(int numberOfSlides) {
        this.numberOfSlides = numberOfSlides;
    }

    @XmlTransient
    public String getPdfFileName() {
        return pdfFileName;
    }

    public void setPdfFileName(String pdfFileName) {
        this.pdfFileName = pdfFileName;
    }

    @XmlTransient
    public int getMaximumSlideHeight() {
        return maximumSlideHeight;
    }

    public void setMaximumSlideHeight(int slideHeight) {
        this.maximumSlideHeight = slideHeight;
    }

    @XmlTransient
    public int getMaximumSlideWidth() {
        return maximumSlideWidth;
    }

    public void setMaximumSlideWidth(int slideWidth) {
        this.maximumSlideWidth = slideWidth;
    }

    /**
     * Serializes the instance of this class to an output stream.
     * <p>
     * @param os The output stream to write to
     * @throw JAXBException Upon error writing the XML
     */
    public void encode(OutputStream os) throws JAXBException {
        /* Write out to the stream */
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        marshaller.marshal(this, os);
    }

    /**
     * Deserializes an XML stream into an instance of this class.
     *
     * @param in The input stream to read from
     * @return An instance of this class
     * @throws JAXBException Upon error reading the XML
     */
    public static DeployedPDF decode(InputStream in) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (DeployedPDF)unmarshaller.unmarshal(in);
    }
}
