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
package org.jdesktop.wonderland.modules.evolvermulti.client.evolver;

import imi.loaders.Collada;
import java.io.IOException;
import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import nu.xom.Document;
import nu.xom.NodeFactory;
import nu.xom.converters.DOMConverter;
import nux.xom.binary.BinaryParsingException;
import nux.xom.binary.BinaryXMLCodec;
import org.collada.colladaschema.COLLADA;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * Plugin to load the binary avatar parser
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@Plugin
public class MultimeshEvolverAvatarClientPlugin extends BaseClientPlugin {

    @Override
    public void initialize(ServerSessionManager sessionManager) {
        super.initialize(sessionManager);

        Collada.addParser("dbn", new BinaryColladaParser());
    }

    /**
     * Parse binary collada files
     */
    public class BinaryColladaParser implements Collada.ColladaParser {

        public COLLADA parse(JAXBContext context, URL colladaURL)
                throws JAXBException, IOException
        {
            // read the document
            Document doc = read(colladaURL);

            // convert to COLLADA
            return convert(context, doc);
        }

        public Document read(URL colladaURL) throws IOException {
            BinaryXMLCodec codec = new BinaryXMLCodec();

            try {
                return codec.deserialize(colladaURL.openStream(), new NodeFactory());
            } catch (BinaryParsingException bpe) {
                IOException ioe = new IOException("Error reading binary stream");
                ioe.initCause(bpe);
                throw ioe;
            }
        }

        public COLLADA convert(JAXBContext context, Document doc)
                throws IOException, JAXBException
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            org.w3c.dom.DOMImplementation dom;

            try {
                dom = dbf.newDocumentBuilder().getDOMImplementation();
            } catch (ParserConfigurationException pce) {
                IOException ioe = new IOException("Error getting DOM");
                ioe.initCause(pce);
                throw ioe;
            }

            org.w3c.dom.Document converted = DOMConverter.convert(doc, dom);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (COLLADA) unmarshaller.unmarshal(converted);
        }
    }
}
