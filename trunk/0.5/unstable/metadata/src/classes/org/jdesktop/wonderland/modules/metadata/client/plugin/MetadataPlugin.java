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
package org.jdesktop.wonderland.modules.metadata.client.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.metadata.client.MetadataClientUtils;
import org.jdesktop.wonderland.modules.metadata.client.MetadataConnection;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSearchFilters;

/**
 * Client plugin for metadata module.
 *
 * Provides GUI access to search interface in the form of an extra window.
 *
 * In the future, in a client connected to multiple sessions at the same time,
 * items like this global search will have to make it clear what session/server
 * they are searching.
 *
 * This implementation searches only the primary session/server.
 *
 * @author mabonner
 */
@Plugin
public class MetadataPlugin extends BaseClientPlugin
{
    private static Logger logger = Logger.getLogger(MetadataPlugin.class.getName());
//    private static ArrayList<Class> metaTypes = new ArrayList<Class>();

    /* The single instance of the search dialog */
    private WeakReference<MetadataSearchForm> searchFormRef = null;

    /* The single instance of the results dialog */
    private WeakReference<MetadataSearchResultsForm> resultsFormRef = null;

    /* The menu item to add to the menu */
    private JMenuItem searchMI;

    /**
     * used to set up metadata connection
     */
    private SessionLifecycleListener sessionListener = null;

    @Override
    public void initialize(final ServerSessionManager loginInfo) {
      logger.log(Level.INFO, "[META PLUGIN] initialize");
      // Create the metadata search menu The menu will be added when our
      // server becomes primary.
      // also create
      searchMI = new JMenuItem("Search Metadata");
      searchMI.setEnabled(false);
      searchMI.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
              MetadataSearchForm searchFrame;
              if (searchFormRef == null || searchFormRef.get() == null) {
                  searchFrame = new MetadataSearchForm(new searchButtonListener());
                  searchFormRef = new WeakReference(searchFrame);
              }
              else {
                  searchFrame = searchFormRef.get();
              }

              if (searchFrame.isVisible() == false) {
                  searchFrame.setVisible(true);
              }
          }
      });

      // set up listener, which in turn will set up the metadata connection for this server
      sessionListener = new SessionLifecycleListener() {
          public void sessionCreated(WonderlandSession session) {
            // Do nothing for now
          }

          public void primarySession(WonderlandSession session) {
            // set client utils class loader to appropriate scl
            MetadataClientUtils.setScannedClassLoader(loginInfo.getClassloader());
            setPrimarySession(session);
          }

          public void activate() {

          }
      };
      loginInfo.addLifecycleListener(sessionListener);

      // Add the Palette menu and the Cell submenu and dialog that lets users
      // create new cells.
//        moduleMI = new JMenuItem("Module Art Palette");
//        moduleMI.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                ModulePalette modulePaletteFrame;
//                if (modulePaletteFrameRef == null || modulePaletteFrameRef.get() == null) {
//                    modulePaletteFrame = new ModulePalette();
//                    modulePaletteFrameRef = new WeakReference(modulePaletteFrame);
//                }
//                else {
//                    modulePaletteFrame = modulePaletteFrameRef.get();
//                }
//
//                if (modulePaletteFrame.isVisible() == false) {
//                    modulePaletteFrame.setVisible(true);
//                }
//            }
//        });

      super.initialize(loginInfo);
    }

//    public static final ArrayList<Class> getMetadataTypes(){
//        return metaTypes;
//    }

    /**
     * Notification that our server is now the the primary server
     */
    @Override
    protected void activate() {
      logger.log(Level.INFO, "[META PLUGIN] activated");
      // add menu item
      JmeClientMain.getFrame().addToWindowMenu(searchMI, -1);
    }

    @Override
    protected void deactivate() {
      // deactivate
      JmeClientMain.getFrame().removeFromInsertMenu(searchMI);
    }

  

  /**
   * Sets the primary session, when it is made the primary session. This
   * turns on everything: set up connection, set up menu item
   */
    private void setPrimarySession(WonderlandSession session) {
        // Create a new custom connection to receive text chats. Register a
        // listener that handles new text messages. Will display them in the
        // window.
        MetadataConnection conn = MetadataConnection.getInstance();

        // Open the text chat connection. If unsuccessful, then log an error
        // and return.
        try {
            conn.connect(session);
        } catch (ConnectionFailureException excp) {
            logger.log(Level.WARNING, "Unable to establish a connection to " +
                    "the metadata connection.", excp);
            return;
        }

        // enable menu item
        searchMI.setEnabled(true);
    }

    // context-menu level functionality begins here in palette plugin..
    // what to add besides tagging?
    // perhaps 'search for cells like this'

    class searchButtonListener implements ActionListener{
      /**
       * Implementation of action listener. Act on search button presses from
       * the search results
       * @param e
       */
      public void actionPerformed(ActionEvent e) {
        // search button was clicked
        logger.log(Level.INFO, "[META PLUGIN] search button listener");

        // get current filters from form
        MetadataSearchForm searchFrame = searchFormRef.get();
        MetadataSearchFilters filters = searchFrame.getFilters();

        // search via connection.. this will block until the search returns
        // from the server. Other objects can still execute searches.
        logger.log(Level.INFO, "[META PLUGIN] searching for " + filters.filterCount() + " filters");
        HashMap<CellID, Set<Integer>> results = MetadataConnection.getInstance().search(filters);

        logger.log(Level.INFO, "[META PLUGIN] got " + results.size() + " results");

        MetadataSearchResultsForm form;
        if (resultsFormRef == null || resultsFormRef.get() == null) {
            form = new MetadataSearchResultsForm();
            resultsFormRef = new WeakReference(form);
        }
        else {
            form = resultsFormRef.get();
        }

        form.setResults(results, getSessionManager());
        if (form.isVisible() == false) {
            form.setVisible(true);
        }
      }
  }

}
