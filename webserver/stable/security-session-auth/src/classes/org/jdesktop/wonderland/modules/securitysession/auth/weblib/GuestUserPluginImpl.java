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
package org.jdesktop.wonderland.modules.securitysession.auth.weblib;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.naming.directory.BasicAttribute;
import org.jdesktop.wonderland.modules.securitysession.auth.weblib.AuthSessionManagerImpl.TokenGenerator;
import org.jdesktop.wonderland.modules.securitysession.weblib.UserRecord;

/**
 * User plugin to support guest login.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class GuestUserPluginImpl implements UserPlugin {
    /** a map of username to credentials */
    private final Map<String, Object[]> credentialMap =
            Collections.synchronizedMap(new HashMap<String, Object[]>());

    public void configure(Properties props) {
        // nothing to do
    }

    public PasswordResult credentialsMatch(String userId, Object... credentials) {
        // as long as we get anything for username, we accept the user. Record
        // the credentials for subsequent calls to getUserRecord()
        credentialMap.put(userId, credentials);
        return PasswordResult.MATCH;
    }

    public UserRecord getUserRecord(String userId, TokenGenerator generator) {
        // find the user in our map
        Object[] credentials = credentialMap.get(userId);
        if (credentials == null) {
            return null;
        }

        String fullname = null;
        String email = null;

        if (credentials.length > 0 && credentials[0] instanceof String) {
            fullname = (String) credentials[0];
        }
        if (credentials.length > 1 && credentials[1] instanceof String) {
            email = (String) credentials[1];
        }

        // create the userrecord
        UserRecord rec = new UserRecord(userId, generator.generateToken(userId));

        // set values in the record
        rec.getAttributes().put(new BasicAttribute("uid", userId));
        rec.getAttributes().put(new BasicAttribute("cn", fullname));
        rec.getAttributes().put(new BasicAttribute("mail", email));

        return rec;
    }
}
