/**
  * iSocial Project
  * http://isocial.missouri.edu
  *
  * Copyright (c) 2011, University of Missouri iSocial Project, All 
  * Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * The iSocial project designates this particular file as
  * subject to the "Classpath" exception as provided by the iSocial
  * project in the License file that accompanied this code.
  */

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
package org.jdesktop.wonderland.modules.mediaboard.common;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLConstants;

import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author jbarratt
 */
public class WhiteboardUtils {

    public static final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
    public static final DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
    public static final SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
    
    
    public static synchronized Document newDocument() {
        return impl.createDocument(svgNS, "svg", null);
    }

    public static synchronized Document openDocument(String uri) {
        Document doc = null;

        try {
            doc = factory.createDocument(uri);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return doc;
    }

    public static synchronized String documentToXMLString(Document doc) {
        String docString = null;

        try {
            Writer svgOut = new StringWriter();
            DOMUtilities.writeDocument(doc, svgOut);
            docString = svgOut.toString();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return docString;
    }

    public static synchronized Document xmlStringToDocument(String xmlString) {
        Document result = null;

        try {
            byte[] bArray = xmlString.getBytes("UTF-8");
            InputStream inStream = new ByteArrayInputStream(bArray);
            result = factory.createDocument(null, inStream);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return result;
    }

    public static synchronized String elementToXMLString(Element e) {
        return DOMUtilities.getXML(e);
    }

    public static synchronized Element xmlStringToElement(String xmlString) {

        Map<String, String> prefixes = new HashMap<String, String>();
        prefixes.put(XMLConstants.XMLNS_PREFIX,
                svgNS);
        prefixes.put(XMLConstants.XMLNS_PREFIX + ':' + XMLConstants.XLINK_PREFIX,
                XLinkSupport.XLINK_NAMESPACE_URI);

        String wrapperElementName = SVGConstants.SVG_SVG_TAG;

        StringBuffer wrapperElementOpen = new StringBuffer("<" + wrapperElementName);

        // Copy the prefixes from the prefixes map to the wrapper element
        wrapperElementOpen.append(" ");
        Set keySet = prefixes.keySet();
        Iterator iter = keySet.iterator();
        while (iter.hasNext()) {
            String currentKey = (String) iter.next();
            String currentValue = (String) prefixes.get(currentKey);
            wrapperElementOpen.append(currentKey);
            wrapperElementOpen.append("=\"");
            wrapperElementOpen.append(currentValue);
            wrapperElementOpen.append("\" ");
        }

        wrapperElementOpen.append(">");

        StringBuffer wrapperElementClose = new StringBuffer("</" + wrapperElementName + ">");

        String wrappedXMLString = wrapperElementOpen + xmlString + wrapperElementClose;

        Document tempDoc = xmlStringToDocument(wrappedXMLString);

        return (Element) tempDoc.getDocumentElement().getFirstChild();
    }

    public static synchronized String constructRGBString(Color c) {
        String rgbString = "rgb(" +
                c.getRed() + "," +
                c.getGreen() + "," +
                c.getBlue() + ")";
        return rgbString;
    }

    public static synchronized Rectangle constructRectObject(Point start, Point end) {
        int width = Math.abs(start.x - end.x);
        int height = Math.abs(start.y - end.y);
        int minX = Math.min(start.x, end.x);
        int minY = Math.min(start.y, end.y);

        return new Rectangle(minX, minY, width, height);
    }

    public static synchronized Shape getElementShape(Element e, Rectangle2D elementBounds) {

        String tagName = e.getTagName();
        Shape s = null;

        if (tagName.equals("line")) {
            double x1 = Double.parseDouble(e.getAttributeNS(null, "x1")),
                    y1 = Double.parseDouble(e.getAttributeNS(null, "y1")),
                    x2 = Double.parseDouble(e.getAttributeNS(null, "x2")),
                    y2 = Double.parseDouble(e.getAttributeNS(null, "y2"));

            s = new Line2D.Double(x1, y1, x2, y2);
        } else if (tagName.equals("rect")) {
            double x = Double.parseDouble(e.getAttributeNS(null, "x")),
                    y = Double.parseDouble(e.getAttributeNS(null, "y")),
                    w = Double.parseDouble(e.getAttributeNS(null, "width")),
                    h = Double.parseDouble(e.getAttributeNS(null, "height"));

            s = new Rectangle2D.Double(x, y, w, h);
        } else if (tagName.equals("ellipse")) {
            double x = Double.parseDouble(e.getAttributeNS(null, "cx")) - Double.parseDouble(e.getAttributeNS(null, "rx")),
                    y = Double.parseDouble(e.getAttributeNS(null, "cy")) - Double.parseDouble(e.getAttributeNS(null, "ry")),
                    w = 2 * Double.parseDouble(e.getAttributeNS(null, "rx")),
                    h = 2 * Double.parseDouble(e.getAttributeNS(null, "ry"));

            s = new Ellipse2D.Double(x, y, w, h);
        } else if (tagName.equals("text")) {
            double x = elementBounds.getMinX(),
                    y = elementBounds.getMinY(),
                    w = elementBounds.getWidth(),
                    h = elementBounds.getHeight();
            s = new Rectangle2D.Double(x, y, w, h);
        } else if (tagName.equals("image")) {
            double x = Double.parseDouble(e.getAttributeNS(null, "x")),
                    y = Double.parseDouble(e.getAttributeNS(null, "y")),
                    w = Double.parseDouble(e.getAttributeNS(null, "width")),
                    h = Double.parseDouble(e.getAttributeNS(null, "height"));

            s = new Rectangle2D.Double(x, y, w, h);
        }

        return s;
    }
}
