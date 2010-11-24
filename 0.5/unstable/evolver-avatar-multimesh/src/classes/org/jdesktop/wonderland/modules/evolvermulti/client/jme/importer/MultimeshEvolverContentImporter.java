/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
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

/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.evolvermulti.client.jme.importer;

import java.awt.Component;
import java.io.File;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.content.spi.ContentImporterSPI;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.avatarbase.client.registry.AvatarRegistry;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.evolvermulti.client.evolver.MultimeshEvolverAvatar;
import org.jdesktop.wonderland.modules.evolvermulti.client.evolver.MultimeshEvolverAvatarConfigManager;
import org.jdesktop.wonderland.modules.evolvermulti.client.evolver.MultimeshEvolverAvatarInfo;

/**
 * A content importer handler for Multimesh Evolver Avatar (.evm) files
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class MultimeshEvolverContentImporter implements ContentImporterSPI {

    private static final Logger LOGGER = Logger.getLogger(
            MultimeshEvolverContentImporter.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/evolvermulti/client/evolver/"
            + "resources/Bundle");
    private ServerSessionManager loginInfo = null;

    /** Constructor
     * @param loginInfo the login information
     */
    public MultimeshEvolverContentImporter(ServerSessionManager loginInfo) {
        this.loginInfo = loginInfo;
    }

    /**
     * @inheritDoc()
     */
    public String[] getExtensions() {
        return new String[] { "evm" };
    }

    /**
     * @inheritDoc()
     */
    public String importFile(File file, String extension) {
        return importFile(file, extension, false);
    }

    /**
     * @inheritDoc()
     */
    public String importFile(File file, String extension, boolean createCell) {
        JFrame frame = JmeClientMain.getFrame().getFrame();
        String fname = file.getName();

        LOGGER.warning("Importing Evolver avatar file " + fname);

        // We first need to check whether the file is a .zip file. If not, then
        // it is not properly formatted. Display a dialog indicating such.
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
        } catch (java.lang.Exception excp) {
            LOGGER.log(Level.WARNING, "Dropped file is not a .zip", excp);
            showUploadFailedMessage(frame, fname);
            return null;
        }

        // Next find the essential avatar information (e.g. name, gender) by
        // parsing the evolver.xml file to get an EvolverAvatarInfo class
        MultimeshEvolverAvatarInfo avatarInfo = getAvatarInfo(zipFile);
        if (avatarInfo == null) {
            LOGGER.warning("Dropped file does not contain evolver.xml");
            showUploadFailedMessage(frame, fname);
            return null;
        }

        // Fetch the avatar name. If there is no name, then return an error
        String avatarName = avatarInfo.getAvatarName();
        if (avatarName == null || avatarName.equals("") == true) {
            LOGGER.warning("No avatar name found in evolver.xml");
            showUploadFailedMessage(frame, fname);
            return null;
        }
        LOGGER.warning("Found avatar with named " + avatarName);

        // Otherwise, check to see if the name already exists in the user's
        // content repository. If so, then ask whether the user wishes to
        // overwrite the existing avatar.
        if (isAvatarExists(avatarName) == true) {
            String message = BUNDLE.getString("Replace_Avatar_Message");
            message = MessageFormat.format(message, avatarName);
            int result = JOptionPane.showConfirmDialog(frame, message,
                    BUNDLE.getString("Replace_Avatar_Title"),
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION) {
                return null;
            }
        }

        LOGGER.warning("Adding avatar named " + avatarName + " into the system.");

        // Display a dialog showing a wait message while we import. We need
        // to do this in the SwingWorker thread so it gets drawn
        String message = BUNDLE.getString("Wait_Upload");
        message = MessageFormat.format(message, avatarName);
        JOptionPane waitMsg = new JOptionPane(message);
        final JDialog dialog = waitMsg.createDialog(
                frame, BUNDLE.getString("Uploading_Avatar"));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dialog.setVisible(true);
            }
        });
        
        // Go ahead and create the avatar in the system. This will take care
        // of uploading all of the files and properly register the avatar in
        // the system.
        try {
            MultimeshEvolverAvatarConfigManager m =
                    MultimeshEvolverAvatarConfigManager.getEvolverAvatarConfigManager();
            MultimeshEvolverAvatar avatar = m.createAvatar(zipFile, avatarInfo);

            LOGGER.warning("Setting avatar named " + avatarName + " in-use.");

            // Finally, tell the avatar system to use this avatar as the default.
            AvatarRegistry registry = AvatarRegistry.getAvatarRegistry();
            registry.setAvatarInUse(avatar, false);
        } finally {
            // Close down the dialog indicating success
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    dialog.setVisible(false);
                }
            });
        }
        
        // We do not wish to create a Cell in this case, so always return null,
        // even upon success.
        return null;
    }

    /**
     * Searches the given zip file for a .dae and returns its name. If one
     * does not exist, then return null
     */
    private String getAvatarName(ZipFile zipFile) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements() == true) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.endsWith(".dae") == true) {
                return name.substring(0, name.length() - 4);
            }
        }
        return null;
    }

    /**
     * Searches the given zip file for a file named evolver.xml and returns an
     * object which represents its information. If no file exists, returns
     * null
     */
    private MultimeshEvolverAvatarInfo getAvatarInfo(ZipFile zipFile) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements() == true) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.equals("evolver.xml") == true) {
                try {
                    InputStream is = zipFile.getInputStream(entry);
                    return MultimeshEvolverAvatarInfo.decode(is);
                } catch (java.lang.Exception excp) {
                    LOGGER.log(Level.WARNING, "Unable to find evolver.xml", excp);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Returns true if the given avatar name already exists within the user's
     * content repository
     */
    private boolean isAvatarExists(String avatarName) {
        ContentCollection userRoot = getUserRoot();
        String path = "/evolver-multi/" + avatarName;
        try {
            boolean exists = (userRoot.getChild(path) != null);
            return exists;
        } catch (ContentRepositoryException excp) {
            LOGGER.log(Level.WARNING, "Error while try to find " + path +
                    " in content repository", excp);
            return false;
        }
    }

    /**
     * Returns the content repository root for the current user, or null upon
     * error.
     */
    private ContentCollection getUserRoot() {
        ContentRepositoryRegistry registry = ContentRepositoryRegistry.getInstance();
        ContentRepository repo = registry.getRepository(loginInfo);
        try {
            return repo.getUserRoot();
        } catch (ContentRepositoryException excp) {
            LOGGER.log(Level.WARNING, "Unable to find repository root", excp);
            return null;
        }
    }

    private void showUploadFailedMessage(Component parent, String fileName) {
        String message = BUNDLE.getString("Invalid_Evolver_Avatar_File");
        message = MessageFormat.format(message, fileName);
        JOptionPane.showMessageDialog(parent, message,
                BUNDLE.getString("Upload_Failed"),
                JOptionPane.ERROR_MESSAGE);
    }
}
