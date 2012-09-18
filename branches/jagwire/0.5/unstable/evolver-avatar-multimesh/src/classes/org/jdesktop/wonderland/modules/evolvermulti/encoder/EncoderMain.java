/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.evolvermulti.encoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import nu.xom.Document;
import nu.xom.converters.DOMConverter;
import nux.xom.binary.BinaryXMLCodec;
import org.collada.colladaschema.COLLADA;
import org.jdesktop.wonderland.modules.evolvermulti.client.evolver.MultimeshEvolverAvatarInfo;
import org.jdesktop.wonderland.modules.evolvermulti.client.evolver.config.MultimeshConfigElement;
import org.jdesktop.wonderland.modules.evolvermulti.client.evolver.config.MultimeshModelConfigElement;
import org.jdesktop.wonderland.modules.evolvermulti.client.evolver.config.MultimeshShapesConfigElement;

/**
 * Encode avatar files into binary
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class EncoderMain {

    private static final Logger logger = Logger.getLogger(EncoderMain.class.getName());
    /** Context for the unmarshaller to operate within **/
    private static JAXBContext jaxbContext = null;

    static {
        // Attempt to create the context and unmarshaller for JAXB
        try {
            jaxbContext = JAXBContext.newInstance("org.collada.colladaschema");
        } catch (JAXBException ex) {
            logger.log(Level.SEVERE, "Problem initializing JAXB Context and / or unmarshaller!", ex);
        }
    }

    public void encode(InputStream input, OutputStream output) throws IOException {
        try {
            COLLADA collada = parse(input);
            Document document = convert(collada);
            write(document, output);
        } catch (JAXBException je) {
            IOException ioe = new IOException("Error parsing collada");
            ioe.initCause(je);
            throw ioe;
        } catch (ParserConfigurationException pce) {
            IOException ioe = new IOException("Error converting collada");
            ioe.initCause(pce);
            throw ioe;
        }
    }

    protected COLLADA parse(InputStream in)
            throws JAXBException, IOException
    {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (COLLADA) unmarshaller.unmarshal(in);
    }

    protected Document convert(COLLADA collada)
            throws JAXBException, ParserConfigurationException, IOException
    {
        // create a new document
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document result = dbf.newDocumentBuilder().newDocument();

        // write the data out to it
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(collada, result);

        // convert to XOM
        return DOMConverter.convert(result);
    }

    protected void write(Document document, OutputStream output)
            throws IOException
    {
        BinaryXMLCodec codec = new BinaryXMLCodec();
        codec.serialize(document, 1, output);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            usage();
            System.exit(-1);
        }

        File input = new File(args[0]);
        File output = new File(args[1]);

        try {
            if (input.getName().endsWith(".dae")) {
                encodeDae(input, output);
            } else if (input.getName().endsWith(".evm")) {
                encodeEvm(input, output);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private static void encodeEvm(File input, File output) throws IOException {
        EncoderMain encoder = new EncoderMain();
        byte[] buffer = new byte[64 * 1024];
        
        System.out.println("Reading input multimesh avatar file " + input.getPath());
        ZipInputStream zin = new ZipInputStream(new FileInputStream(input)) {
            @Override
            public void close() throws IOException {
                // ignore close requests
            }
        };
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(output));
        
        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null) {
            if (entry.getName().endsWith(".dae")) {
                System.out.println("Converting " + entry.getName());
                entry = new ZipEntry(entry.getName().replace(".dae", ".dbn"));
               
                zout.putNextEntry(entry);
                encoder.encode(zin, zout);
                zout.closeEntry();
            } else if (entry.getName().equalsIgnoreCase("evolver.xml")) {
                System.out.println("Rewriting " + entry.getName());
                
                // rewrite the config file
                entry = new ZipEntry(entry.getName());
                zout.putNextEntry(entry);

                try {
                    // read the existing config
                    MultimeshEvolverAvatarInfo info =
                            MultimeshEvolverAvatarInfo.decode(zin);
                    for (MultimeshConfigElement mce : info.getConfigElements()) {
                        if (mce instanceof MultimeshModelConfigElement) {
                            MultimeshModelConfigElement mmce = 
                                    (MultimeshModelConfigElement) mce;
                            
                            if (mmce.getModel().endsWith(".dae")) {
                                mmce.setModel(mmce.getModel().replace(".dae", ".dbn"));
                            }
                        }
                    }

                    // write the updated config to the stream
                    info.encode(zout);
                    zout.closeEntry();
                } catch (JAXBException je) {
                    throw new IOException(je);
                }

            } else {
                zout.putNextEntry(new ZipEntry(entry.getName()));
                int read;
                while ((read = zin.read(buffer)) > 0) {
                    zout.write(buffer, 0, read);
                }
                zout.closeEntry();
            }
        }

        zout.close();
        System.out.println("Wrote output to " + output.getPath());
    }
    
    private static void encodeDae(File input, File output) throws IOException {
        System.out.println("Reading input collada file " + input.getPath());
        new EncoderMain().encode(new FileInputStream(input),
                                 new FileOutputStream(output));
        System.out.println("Wrote output to " + output.getPath());
    }


    
    protected static void usage() {
        System.out.println("Usage: EncoderMain inputfile outputfile");
    }
}
