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
package org.jdesktop.wonderland.modules.isocial.weblib;

/**
 * Factory to create instances of ISocialDAO
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class ISocialDAOFactory {

    public static ISocialDAO getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public static ISocialDAO getReadOnlyInstance() {
        return new ReadOnlyDAOWrapper(SingletonHolder.INSTANCE);
    }

    private static final class SingletonHolder {

        private static final ISocialDAO INSTANCE;

        static {
            // create the data access object and security policy objects
            String daoProvider = System.getProperty(ISocialDAO.PROVIDER_PROP,
                    ISocialDAO.DEFAULT_PROVIDER);

            String policyProvider = System.getProperty(ISocialSecurityPolicy.PROVIDER_PROP,
                    ISocialSecurityPolicy.DEFAULT_PROVIDER);

            try {
                // create the singleton instance
                Class<ISocialDAO> providerClass = (Class<ISocialDAO>) Class.forName(daoProvider);
                INSTANCE = providerClass.newInstance();

                // initialize the security policy
                Class<ISocialSecurityPolicy> policyClass = (Class<ISocialSecurityPolicy>) Class.forName(policyProvider);
                ISocialSecurityPolicy policy = policyClass.newInstance();

                // initialize the instance
                INSTANCE.setSecurityPolicy(policy);
                INSTANCE.initialize();
            } catch (ClassNotFoundException cnfe) {
                throw new RuntimeException(cnfe);
            } catch (InstantiationException ie) {
                throw new RuntimeException(ie);
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae);
            }
        }
    }
}
