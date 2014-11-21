/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.appframe.common;


import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerStateFactory;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * Server state for app frame
 */
@ServerState
@XmlRootElement(name="app-frame")
public class AppFrameServerState extends CellServerState {    
 
    @XmlElement(name="border-Color")
    private String borderColor = null;
    
    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.appframe.server.AppFrameMO";
    }
    
    @XmlTransient 
    public String getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * override encode method of CellServerState to use CDATA for storing whole state of AppFrame contents
     */
    @Override
    public void encode(Writer w, Marshaller marshaller) {

        try {
            /*
            * If the marshaller is null, use the one associated with the system
            * classloader
            */
            if (marshaller == null) {
                marshaller = CellServerStateFactory.getMarshaller(null);
            }
            //prevent from escaping characters
            marshaller.setProperty("com.sun.xml.bind.characterEscapeHandler"
                    , new AppFrameServerState.JaxbCharacterEscapeHandler());
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            
            //call encode method of CellServerState
            super.encode(w, marshaller);
        } catch (Exception ex) {
            Logger.getLogger(AppFrameServerState.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    //Handler to prevent escaping characters in xml
    public static class JaxbCharacterEscapeHandler implements com.sun.xml.bind.marshaller.CharacterEscapeHandler {
        public void escape(char[] buf, int start, int len, boolean isAttValue,
                        Writer out) throws IOException {
            for (int i = start; i < start + len; i++) {
                    char ch = buf[i];
                    out.write(ch);
            }
        }
    }
    
    @Override
    public void encode(Writer w) {
        encode(w, null);
    }
   
}

