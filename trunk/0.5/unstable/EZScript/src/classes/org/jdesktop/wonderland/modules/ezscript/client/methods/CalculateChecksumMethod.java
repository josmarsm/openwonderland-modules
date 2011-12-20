/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 * HIGHLY Adapted from: http://www.javablogging.com/sha1-and-md5-checksums-in-java/
 * 
 * @author JagWire
 */
@ReturnableScriptMethod
public class CalculateChecksumMethod implements ReturnableScriptMethodSPI {

    String input = "";
    String checksum = null;
    String algorithmID = "SHA1";
    private static final Logger logger = Logger.getLogger(CalculateChecksumMethod.class.getName());
    public String getDescription() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "Calculates a checksum for a given string, based on a given algorithm.\n"
                + "-- usage: CalculateChecksum(\"SHA1\", \"The quick brown fox jumps over the lazy dog\");\n"
                + "-- usage: CalculateChecksum(\"MD5\", \"Now is the time for all good men...\");";
        
    }

    public String getFunctionName() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "CalculateChecksum";
    }

    public String getCategory() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "utilities";
    }

    public void setArguments(Object[] args) {
//        throw new UnsupportedOperationException("Not supported yet.");
        algorithmID = (String)args[0];
        input = (String)args[1];
    }

    public Object returns() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return new String(checksum);
        
    }

    public void run() {
        try {
            MessageDigest algorithm = MessageDigest.getInstance(algorithmID);
            DigestInputStream dis = new DigestInputStream(new ByteArrayInputStream(input.getBytes()),
                                                          algorithm);
            while(dis.read() != -1);
            
            byte[] hash = algorithm.digest();
            checksum = byteArray2Hex(hash);
            
        } catch (NoSuchAlgorithmException ex) {
           logger.warning("Algorithm: "+algorithmID+" doesn't seem to exist! String returned will be null.");
           
        } catch (IOException ioe) {
//           logger.warning("IO")
            ioe.printStackTrace();
        } 
                                                        
    }
    
    /**
     * Convert a byte array into a readable Hex string.
     * 
     * @param hash
     * @return 
     */
    private String byteArray2Hex(byte[] hash) {
        //I know it's slow, I'll see what I can do to find a faster one
        //when I have more time.
        Formatter formatter = new Formatter();
        for(byte b: hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
    
}
