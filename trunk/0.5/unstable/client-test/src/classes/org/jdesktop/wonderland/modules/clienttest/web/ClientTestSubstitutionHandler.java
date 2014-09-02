/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.clienttest.web;

import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import org.jdesktop.wonderland.front.servlet.WonderlandSubstitutionHandler;
import org.jdesktop.wonderland.runner.DeploymentEntry;
import org.jdesktop.wonderland.runner.DeploymentManager;
import org.jdesktop.wonderland.runner.RunManager;
import org.jdesktop.wonderland.runner.Runner;

/**
 *
 * @author jkaplan
 */
public class ClientTestSubstitutionHandler extends WonderlandSubstitutionHandler {
    private static final String VOICE_BRIDGE_RUNNER = 
            "org.jdesktop.wonderland.modules.voicebridge.server.VoicebridgeRunner";
    
    @Override
    public String specializeJnlpTemplate(HttpServletRequest request, 
                                         String respath, String jnlpTemplate) 
    {
        String bridgeProp = "<property name=\"jnlp.voicebridge.info\" value=\"" +
                            getVoiceBridge() + "\"/>\n$$url.props\n";
        jnlpTemplate = substitute(jnlpTemplate, "$$url.props", bridgeProp.toString());
        jnlpTemplate = super.specializeJnlpTemplate(request, respath, jnlpTemplate);
                
        return jnlpTemplate;
    }
    
    protected String getVoiceBridge() {        
        String out = null;
        
        for (Runner r : RunManager.getInstance().getAll()) {
            if (r.getClass().getName().equals(VOICE_BRIDGE_RUNNER)) {
                Properties props = getRunnerProperties(r);
                
                String bridgeId =           null;
                String privateHostName =    props.getProperty("voicebridge.local.hostAddress");
                String privateControlPort = props.getProperty("voicebridge.control.port");
                String privateSipPort =     props.getProperty("voicebridge.sip.port");
                String publicHostName =     props.getProperty("voicebridge.server.public.address");
                String publicControlPort =  null;
                String publicSipPort =      props.getProperty("voicebridge.server.public.sip.port");
                
                if (publicHostName == null) {
                    publicHostName = privateHostName;
                }
                
                if (publicControlPort == null) {
                    publicControlPort = privateControlPort;
                }
                
                if (publicSipPort == null) {
                    publicSipPort = privateSipPort;
                }
                
                out = bridgeId + "::" + privateHostName
                                + ":" + privateControlPort 
                                + ":" + privateSipPort
                                + ":" + publicHostName
                                + ":" + publicControlPort
                                + ":" + publicSipPort; 
            }
        }
        
        return out;
    }
    
    protected Properties getRunnerProperties(Runner runner) {
        DeploymentEntry de = DeploymentManager.getInstance().getEntry(runner.getName());
        
        // add in the default properties to the runner.  These will be removed
        // before they are saved
        Properties props = runner.getDefaultProperties();
        props.putAll(de.getProperties());
        de.setProperties(props);
        
        return props;
    }
}
