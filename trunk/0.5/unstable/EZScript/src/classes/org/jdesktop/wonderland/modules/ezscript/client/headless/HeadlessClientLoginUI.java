/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.headless;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.client.login.LoginUI;
import org.jdesktop.wonderland.client.login.ServerSessionManager.EitherLoginControl;
import org.jdesktop.wonderland.client.login.ServerSessionManager.NoAuthLoginControl;
import org.jdesktop.wonderland.client.login.ServerSessionManager.UserPasswordLoginControl;

/**
 *
 * @author ryan
 */
public class HeadlessClientLoginUI implements LoginUI {

        private String username;
        private Properties props;
        private static final Logger logger = Logger.getLogger(HeadlessClientLoginUI.class.getName());

        public HeadlessClientLoginUI(String username, Properties props) {
            this.username = username;
            this.props = props;
        }

        public void requestLogin(NoAuthLoginControl control) {
            String fullname = props.getProperty("fullname");
            if (fullname == null) {
                fullname = username;
            }

            try {
                control.authenticate(username, fullname);
            } catch (LoginFailureException lfe) {
                logger.log(Level.WARNING, "Login failed", lfe);
                control.cancel();
            }
        }

        public void requestLogin(UserPasswordLoginControl control) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void requestLogin(EitherLoginControl control) {
            requestLogin(control.getNoAuthLogin());
        }
    }
