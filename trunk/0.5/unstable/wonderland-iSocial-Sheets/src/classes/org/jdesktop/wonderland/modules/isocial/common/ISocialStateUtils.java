/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
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
package org.jdesktop.wonderland.modules.isocial.common;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.common.login.CredentialManager;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * Utilities for getting and storing ISocial state
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class ISocialStateUtils {
    private static final Logger LOGGER =
            Logger.getLogger(ISocialStateUtils.class.getName());
    private static final String RESOURCE_BASE =
            "isocial-sheets/isocial-sheets/resources/";
    
    /**
     * Create a the JAXBContext for marshalling and unmarshalling ISocial
     * objects
     * @param classloader the classloader to look for objects in
     * @return the instantiated context
     * @throws RuntimeException if there is an error creating the context
     */
    public static JAXBContext createContext(ScannedClassLoader scl) {
        // find all isocial model classes
        Set<String> modelClassNames = scl.getClasses(ISocialModel.class);
        Set<Class> modelClasses = new LinkedHashSet<Class>();

        for (String className : modelClassNames) {
            try {
                Class modelClass = scl.loadClass(className);
                modelClasses.add(modelClass);
            } catch (ClassNotFoundException cnfe) {
                LOGGER.log(Level.WARNING, "Error loading class", cnfe);
            }
        }

        // create the context
        try {
            return JAXBContext.newInstance(modelClasses.toArray(new Class[0]));
        } catch (JAXBException je) {
            throw new RuntimeException("Error initializing", je);
        }
    }
    
    /**
     * Get an object from a URL.
     * @param baseURL the base url for the server
     * @param objectURL the relative object URL in the resource
     * @param cm the credential manager for securing connections
     * @param context the JAXB context for unmarshalling
     * @return the object unmarshalled from the given URL
     */
    public static Object getObject(String baseURL, String objectURL,
                                   CredentialManager cm, JAXBContext context) 
            throws IOException 
    {
        URL conn = new URL(baseURL + RESOURCE_BASE + objectURL);

        HttpURLConnection uc = (HttpURLConnection) conn.openConnection();
        uc.setRequestProperty("Accept", "application/xml");
        
        cm.secureURLConnection(uc);

        try {
            return context.createUnmarshaller().unmarshal(uc.getInputStream());
        } catch (JAXBException je) {
            throw new IOException(je);
        }
    }

    /**
     * Write an object to a URL.
     * @param baseURL the base url for the server
     * @param objectURL the relative object URL in the resource
     * @param object the object to write
     * @param cm the credential manager for securing connections
     * @param context the JAXB context for unmarshalling
     * @return the object unmarshalled from the given URL
     */
    public static Object writeObject(String baseURL, String objectURL,  
                                 Object object, CredentialManager cm,
                                 JAXBContext context)
            throws IOException
    {
        URL conn = new URL(baseURL + RESOURCE_BASE + objectURL);

        HttpURLConnection uc = (HttpURLConnection) conn.openConnection();
        uc.setRequestMethod("POST");
        uc.setDoInput(true);
        uc.setDoOutput(true);

        uc.setRequestProperty("Content-Type", "application/xml");
        uc.setRequestProperty("Accept", "application/xml");

        cm.secureURLConnection(uc);

        // write the data
        try {
            context.createMarshaller().marshal(object, uc.getOutputStream());
        } catch (JAXBException je) {
            throw new IOException(je);
        }

        // read the response
        int res = uc.getResponseCode();
        if (res != HttpURLConnection.HTTP_OK && 
            res != HttpURLConnection.HTTP_CREATED)
        {
            throw new IOException("Response " + res + ":" + uc.getResponseMessage());
        }

        // try to unmarshall the result
        try {
            return context.createUnmarshaller().unmarshal(uc.getInputStream());
        } catch (JAXBException je) {
            throw new IOException(je);
        }
    }
    
    /**
     * Delete an object from a URL.
     * @param baseURL the base url for the server
     * @param objectURL the relative object URL in the resource
     * @param cm the credential manager for securing connections
     * @return the object unmarshalled from the given URL
     */
    public static void deleteObject(String baseURL, String objectURL, 
                                CredentialManager cm) 
            throws IOException 
    {
        URL conn = new URL(baseURL + RESOURCE_BASE + objectURL);

        HttpURLConnection uc = (HttpURLConnection) conn.openConnection();
        uc.setRequestMethod("DELETE");
        
        cm.secureURLConnection(uc);
        
        int res = uc.getResponseCode();
        if (res != HttpURLConnection.HTTP_OK &&
            res != HttpURLConnection.HTTP_GONE) {
            throw new IOException("Response " + res + ":" + uc.getResponseMessage());
        }
    }
    
}
