/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.poster.client;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.awt.Cursor;
import java.awt.Window;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.jdesktop.wonderland.client.help.WebBrowserLauncher;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.MainFrame.PlacemarkType;
import org.jdesktop.wonderland.modules.placemarks.api.client.PlacemarkRegistry;
import org.jdesktop.wonderland.modules.placemarks.api.client.PlacemarkRegistryFactory;
import org.jdesktop.wonderland.modules.placemarks.api.common.Placemark;

/**
 * Handle hyperlinks in a poster
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class PosterHyperlinkListener implements HyperlinkListener {
    private static final Logger LOGGER =
            Logger.getLogger(PosterHyperlinkListener.class.getName());
    
    private final JEditorPane panel;
    private boolean cursorSet;
    
    public PosterHyperlinkListener(final JEditorPane panel) {
        this.panel = panel;
    }
    
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
            setCursor(true);
            return;
        } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
            setCursor(false);
            return;
        } else if (e.getDescription() == null) {
            return;
        }

        try {
            handleURI(new URI(e.getDescription()));
        } catch (URISyntaxException use) {
            LOGGER.log(Level.WARNING, "Error parsing " + e.getDescription(), 
                       use);
        }
    }    
    
    protected void setCursor(boolean link) {
        if (this.cursorSet == link) {
            return;
        }
        
        this.cursorSet = link;
    
        Window w = SwingUtilities.windowForComponent(panel);
        if (link) {
            w.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            w.setCursor(Cursor.getDefaultCursor());
        }
    }
    
    protected void handleURI(URI u) {
        String scheme = u.getScheme();
        if (scheme != null && !scheme.equalsIgnoreCase("wlserver")) {
            handleWebURI(u);
            return;
        }
        
        // start with the starting location
        String server = u.getHost();
        Vector3f loc = new Vector3f();
        Quaternion look = new Quaternion();
        
        Map<String, String> parsed = parseQuery(u.getRawQuery());
        
        // see if there is a placemark specified
        String placemark = parsed.get("placemark");
        if (placemark != null) {
        
            PlacemarkRegistry pr = PlacemarkRegistryFactory.getInstance();
            Set<Placemark> placemarks = 
                    pr.getAllPlacemarks(PlacemarkType.SYSTEM);
            for (Placemark p : placemarks) {
                if (p.getName().equals(placemark)) {
                    loc.setX(p.getX());
                    loc.setY(p.getY());
                    loc.setZ(p.getZ());
                    look.fromAngles(0, p.getAngle() * FastMath.DEG_TO_RAD, 0);
                    server = p.getUrl();
                    break;
                }
            }
        }
        
        // search for individual x, y and z properties
        String x = parsed.get("x");
        String y = parsed.get("y");
        String z = parsed.get("z");

        // look for a single combined location property
        String locStr = parsed.get("location");
        if (locStr != null) {
            String[] comp = locStr.split("\\s+");
            if (comp.length == 3) {
                x = comp[0];
                y = comp[1];
                z = comp[2];
            }
        }
        
        // set x, y and z
        if (x != null) {
            loc.setX(Float.parseFloat(x));
        }
        if (y != null) {
            loc.setY(Float.parseFloat(y));
        }
        if (z != null) {
            loc.setZ(Float.parseFloat(z));
        }
        
        // set the angle from a look property
        String angle = parsed.get("look");
        if (angle != null) {
            float angDeg = Float.parseFloat(angle);
            look.fromAngles(0, angDeg * FastMath.DEG_TO_RAD, 0);
        }
        
        try {
            ClientContextJME.getClientMain().gotoLocation(server, loc, look);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Error connecting to " + server, ex);
        }
    }
    
    private Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> out = new LinkedHashMap<String, String>();
        if (rawQuery == null) {
            return out;
        }
        
        for (String pair : rawQuery.split("&")) {
            String[] split = pair.split("=");
            if (split.length == 1) {
                out.put(split[0], null);
            } else if (split.length >= 2) {
                try {
                    out.put(split[0], URLDecoder.decode(split[1], "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    LOGGER.log(Level.WARNING, "Unable to decode " + split[1], ex);
                }
            }
        }
        
        return out;
    }
    
    protected void handleWebURI(URI u) {
        try {
            WebBrowserLauncher.openURL(u.toASCIIString());
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Unable to open browser for " + 
                       u, ex);
        }
    }
}
