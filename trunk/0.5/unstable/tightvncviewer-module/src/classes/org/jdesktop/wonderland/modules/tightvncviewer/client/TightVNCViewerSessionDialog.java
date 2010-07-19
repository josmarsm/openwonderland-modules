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
package org.jdesktop.wonderland.modules.tightvncviewer.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 * VNC session dialog
 *
 * @author nsimpson
 */
public class TightVNCViewerSessionDialog extends javax.swing.JPanel {

    private static final Logger logger = Logger.getLogger(TightVNCViewerSessionDialog.class.getName());

    public enum VNCEncoding {

        TIGHT
    }

    public TightVNCViewerSessionDialog() {
        initComponents();
        showConnectionOptions(true);
    }

    public void addActionListener(ActionListener listener) {
        okButton.addActionListener(listener);
        cancelButton.addActionListener(listener);
    }

    public void setServer(final String server) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                serverTextField.setText(server);
            }
        });
    }

    public String getServer() {
        return serverTextField.getText();
    }

    public void setPort(final int port) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                portTextField.setText(String.valueOf(port));
            }
        });
    }

    public int getPort() {
        return Integer.valueOf(portTextField.getText());
    }

    public void setUser(final String user) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                userTextField.setText(user);
            }
        });
    }

    public String getUser() {
        return userTextField.getText();
    }

    public void setPassword(final String password) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                passwordTextField.setText(password);
            }
        });
    }

    public String getPassword() {
        return new String(passwordTextField.getPassword());
    }

    public void setCompression(final boolean compressed) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                compressionCheckBox.setSelected(compressed);
            }
        });
    }

    public boolean getCompression() {
        return compressionCheckBox.isSelected();
    }

    public void setBandwidthOption(final VNCEncoding option) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                switch (option) {
                    case TIGHT:
                        bandwidthComboBox.setSelectedIndex(0);
                        break;
                    default:
                        bandwidthComboBox.setSelectedIndex(0);
                        break;
                }
            }
        });
    }

    public VNCEncoding getVNCEncoding() {
        VNCEncoding option = VNCEncoding.TIGHT;
        switch (bandwidthComboBox.getSelectedIndex()) {
            case 0:
                option = VNCEncoding.TIGHT;
                break;
            default:
                option = VNCEncoding.TIGHT;
                break;
        }

        return option;
    }

    public void setColorDepth(final int depth) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                for (int i = 0; i < colorDepthComboBox.getItemCount(); i++) {
                    String ci = (String) colorDepthComboBox.getItemAt(i);
                    if ((ci.equals("Default") && (depth == 0))
                            || (ci.equals(String.valueOf(depth)))) {
                        colorDepthComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
        });
    }

    public int getColorDepth() {
        String depthStr = (String) colorDepthComboBox.getSelectedItem();
        int depth = 0;

        if (!depthStr.equals("Default")) {
            depth = Integer.valueOf(depthStr);
        }

        return depth;
    }

    public void showConnectionOptions(final boolean show) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                connectionOptionsPanel.setVisible(show);
                if (show == true) {
                    connectionOptionsButton.setText("Hide Connection Options");
                } else {
                    connectionOptionsButton.setText("Show Connection Options");
                }
            }
        });
    }

    public boolean isShowingConnectionOptions() {
        return connectionOptionsPanel.isVisible();
    }

    public void displaySettings() {
        logger.fine("Remote Desktop properties:" + "\n"
                + "system:   " + getServer() + "\n"
                + "port:     " + getPort() + "\n"
                + "user:     " + getUser() + "\n"
                + "options:  " + isShowingConnectionOptions() + "\n"
                + "  encoding:   " + getVNCEncoding() + "\n"
                + "  compression: " + getCompression() + "\n"
                + "  color depth: " + getColorDepth());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        connectLabel = new javax.swing.JLabel();
        serverLabel = new javax.swing.JLabel();
        serverTextField = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        portTextField = new javax.swing.JTextField();
        userLabel = new javax.swing.JLabel();
        userTextField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordTextField = new javax.swing.JPasswordField();
        connectionOptionsButton = new javax.swing.JButton();
        connectionOptionsPanel = new javax.swing.JPanel();
        encodingLabel = new javax.swing.JLabel();
        bandwidthComboBox = new javax.swing.JComboBox();
        compressionCheckBox = new javax.swing.JCheckBox();
        colorDepthLabel = new javax.swing.JLabel();
        colorDepthComboBox = new javax.swing.JComboBox();
        bitsPerPixelLabel = new javax.swing.JLabel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();

        connectLabel.setFont(connectLabel.getFont().deriveFont(connectLabel.getFont().getStyle() | java.awt.Font.BOLD));
        connectLabel.setText("Connect to VNC server:");

        serverLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        serverLabel.setText("VNC server:");

        serverTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverTextFieldActionPerformed(evt);
            }
        });

        portLabel.setText("Port:");

        portTextField.setText("5900");

        userLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        userLabel.setText("User name:");
        userLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        passwordLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        passwordLabel.setText("Password:");
        passwordLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        passwordTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordTextFieldActionPerformed(evt);
            }
        });

        connectionOptionsButton.setText("Show Connection Options");
        connectionOptionsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectionOptionsButtonActionPerformed(evt);
            }
        });

        connectionOptionsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        encodingLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        encodingLabel.setText("Encoding:");
        encodingLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        bandwidthComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tight" }));

        compressionCheckBox.setText("Enable compression");
        compressionCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compressionCheckBoxActionPerformed(evt);
            }
        });

        colorDepthLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        colorDepthLabel.setText("Color depth:");
        colorDepthLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        colorDepthComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default", "8", "16", "24" }));

        bitsPerPixelLabel.setText("bits per pixel");

        org.jdesktop.layout.GroupLayout connectionOptionsPanelLayout = new org.jdesktop.layout.GroupLayout(connectionOptionsPanel);
        connectionOptionsPanel.setLayout(connectionOptionsPanelLayout);
        connectionOptionsPanelLayout.setHorizontalGroup(
            connectionOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, connectionOptionsPanelLayout.createSequentialGroup()
                .add(20, 20, 20)
                .add(connectionOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(connectionOptionsPanelLayout.createSequentialGroup()
                        .add(encodingLabel)
                        .add(21, 21, 21))
                    .add(connectionOptionsPanelLayout.createSequentialGroup()
                        .add(colorDepthLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(connectionOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, bandwidthComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, colorDepthComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 7, Short.MAX_VALUE)
                .add(connectionOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(bitsPerPixelLabel)
                    .add(compressionCheckBox))
                .addContainerGap())
        );

        connectionOptionsPanelLayout.linkSize(new java.awt.Component[] {bandwidthComboBox, colorDepthComboBox}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        connectionOptionsPanelLayout.setVerticalGroup(
            connectionOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(connectionOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(connectionOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(encodingLabel)
                    .add(bandwidthComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(compressionCheckBox))
                .add(9, 9, 9)
                .add(connectionOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(colorDepthLabel)
                    .add(colorDepthComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(bitsPerPixelLabel))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(serverLabel)
                                    .add(userLabel)
                                    .add(passwordLabel))
                                .add(10, 10, 10)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(connectionOptionsButton)
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                            .add(passwordTextField)
                                            .add(userTextField)
                                            .add(serverTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 134, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(portLabel)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(portTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(layout.createSequentialGroup()
                                    .add(cancelButton)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(okButton))
                                .add(connectionOptionsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(connectLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(connectLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(serverTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(portLabel)
                    .add(portTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(serverLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(userLabel)
                    .add(userTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passwordLabel)
                    .add(passwordTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(connectionOptionsButton)
                .add(6, 6, 6)
                .add(connectionOptionsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(okButton)
                    .add(cancelButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (evt.getID() == ActionEvent.ACTION_PERFORMED) {
            displaySettings();
        }
}//GEN-LAST:event_okButtonActionPerformed

    private void compressionCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compressionCheckBoxActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_compressionCheckBoxActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        if (evt.getID() == ActionEvent.ACTION_PERFORMED) {
            this.setVisible(false);
        }
}//GEN-LAST:event_cancelButtonActionPerformed

    private void connectionOptionsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectionOptionsButtonActionPerformed
        if (evt.getID() == ActionEvent.ACTION_PERFORMED) {
            //showConnectionOptions(!isShowingConnectionOptions());
        }
}//GEN-LAST:event_connectionOptionsButtonActionPerformed

    private void serverTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverTextFieldActionPerformed
        okButton.doClick();
    }//GEN-LAST:event_serverTextFieldActionPerformed

    private void passwordTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordTextFieldActionPerformed
        okButton.doClick();
    }//GEN-LAST:event_passwordTextFieldActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox bandwidthComboBox;
    private javax.swing.JLabel bitsPerPixelLabel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox colorDepthComboBox;
    private javax.swing.JLabel colorDepthLabel;
    private javax.swing.JCheckBox compressionCheckBox;
    private javax.swing.JLabel connectLabel;
    private javax.swing.JButton connectionOptionsButton;
    private javax.swing.JPanel connectionOptionsPanel;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JPasswordField passwordTextField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField portTextField;
    private javax.swing.JLabel serverLabel;
    private javax.swing.JTextField serverTextField;
    private javax.swing.JLabel userLabel;
    private javax.swing.JTextField userTextField;
    // End of variables declaration//GEN-END:variables
}
