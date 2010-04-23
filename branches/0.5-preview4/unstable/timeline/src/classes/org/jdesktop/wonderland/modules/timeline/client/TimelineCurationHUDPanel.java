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
package org.jdesktop.wonderland.modules.timeline.client;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.JmeClientMain;

/**
 * A panel for curating a Timeline
 * @author nsimpson
 */
public class TimelineCurationHUDPanel extends javax.swing.JPanel {

    private final TimelineCell cell;
    private PropertyChangeSupport listeners;

    public TimelineCurationHUDPanel(TimelineCell cell) {
        initComponents();

        this.cell = cell;
    }

    /**
     * Adds a bound property listener to the dialog
     * @param listener a listener for dialog events
     */
    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listeners == null) {
            listeners = new PropertyChangeSupport(this);
        }
        listeners.addPropertyChangeListener(listener);
    }

    /**
     * Removes a bound property listener from the dialog
     * @param listener the listener to remove
     */
    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listeners != null) {
            listeners.removePropertyChangeListener(listener);
        }
    }

    /**
     * Get the date from this panel
     * @return the date
     */
    public Date getDate() {
        try {
            DateFormat df = new SimpleDateFormat("MMM dd, yyyy");
            return df.parse(eventDateTextField.getText().trim());
        } catch (ParseException pe) {
            pe.printStackTrace();
            return null;
        }
    }

    /**
     * Get the text
     * @return the text, or null if there is no text
     */
    public String getText() {
        String text = textTextArea.getText();
        if (text == null || text.trim().length() == 0) {
            return null;
        }

        return text.trim();
    }

    public void setText(String text) {
        textTextArea.setText(text);
    }

    /**
     * Get the file to upload
     * @return the file name, or null if there is no file
     */
    public String getFile() {
        String file = artworkTextField.getText();
        if (file == null || file.trim().length() == 0) {
            return null;
        }

        return file.trim();
    }

    public void setFile(String file) {
        artworkTextField.setText(file);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        curationLabel = new javax.swing.JLabel();
        addEventLabel = new javax.swing.JLabel();
        eventDateTextField = new javax.swing.JTextField();
        textLabel = new javax.swing.JLabel();
        textScrollPane = new javax.swing.JScrollPane();
        textTextArea = new javax.swing.JTextArea();
        artworkLabel = new javax.swing.JLabel();
        artworkTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        artworkHelpLabel = new javax.swing.JLabel();
        addButton = new javax.swing.JButton();
        doneButton = new javax.swing.JButton();

        curationLabel.setFont(curationLabel.getFont().deriveFont(curationLabel.getFont().getStyle() | java.awt.Font.BOLD));
        curationLabel.setText("Timeline Curation");

        addEventLabel.setText("Add an event at:");

        eventDateTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        eventDateTextField.setText("Aug 13, 2009");

        textLabel.setText("Text:");

        textTextArea.setColumns(20);
        textTextArea.setRows(5);
        textScrollPane.setViewportView(textTextArea);

        artworkLabel.setText("Media:");

        browseButton.setText("Browse...");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        artworkHelpLabel.setText("e.g., .jpg, .png, .kmz, .au");

        addButton.setText("Add");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        doneButton.setText("Done");
        doneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doneButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(textScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(curationLabel)
                            .add(layout.createSequentialGroup()
                                .add(6, 6, 6)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(artworkLabel)
                                    .add(layout.createSequentialGroup()
                                        .add(addEventLabel)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(eventDateTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 107, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(textLabel))))
                        .add(29, 29, 29))
                    .add(layout.createSequentialGroup()
                        .add(addButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(doneButton))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(6, 6, 6)
                                .add(artworkHelpLabel))
                            .add(layout.createSequentialGroup()
                                .add(artworkTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 175, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(browseButton)))
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(curationLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addEventLabel)
                    .add(eventDateTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(textLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(textScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(artworkLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(artworkTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(artworkHelpLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(doneButton)
                    .add(addButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        System.out.println("Add button action performed");
        listeners.firePropertyChange("add", new String(""), null);
    }//GEN-LAST:event_addButtonActionPerformed

    private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneButtonActionPerformed
        listeners.firePropertyChange("done", new String(""), null);
    }//GEN-LAST:event_doneButtonActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        final Set<String> extensions = ManualObjectCreator.getExtensions();

        JFileChooser jfc = new JFileChooser("Choose a file");
        jfc.setFileFilter(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return true;
                }

                for (String extension : extensions) {
                    if (pathname.getName().endsWith(extension)) {
                        return true;
                    }
                }

                return false;
            }

            @Override
            public String getDescription() {
                return "Manual creation files";
            }
        });

        int res = jfc.showOpenDialog(JmeClientMain.getFrame().getFrame());
        if (res == JFileChooser.APPROVE_OPTION) {
            artworkTextField.setText(jfc.getSelectedFile().getPath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JLabel addEventLabel;
    private javax.swing.JLabel artworkHelpLabel;
    private javax.swing.JLabel artworkLabel;
    private javax.swing.JTextField artworkTextField;
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel curationLabel;
    private javax.swing.JButton doneButton;
    private javax.swing.JTextField eventDateTextField;
    private javax.swing.JLabel textLabel;
    private javax.swing.JScrollPane textScrollPane;
    private javax.swing.JTextArea textTextArea;
    // End of variables declaration//GEN-END:variables
}
