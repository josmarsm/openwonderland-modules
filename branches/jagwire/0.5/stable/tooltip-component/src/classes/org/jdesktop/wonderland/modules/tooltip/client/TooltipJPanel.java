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
package org.jdesktop.wonderland.modules.tooltip.client;

/**
 * A JPanel that displays a text message, used to put into a HUDComponent.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class TooltipJPanel extends javax.swing.JPanel {

    /** Creates new form TooltipJPanel */
    public TooltipJPanel() {
        initComponents();
    }

    /**
     * Sets the text of the tooltip panel.
     *
     * @param The new tooltip text
     */
    public void setText(String text) {
        tooltipLabel.setText(text);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tooltipLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setLayout(new java.awt.GridLayout());

        tooltipLabel.setBackground(new java.awt.Color(255, 255, 255));
        tooltipLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        tooltipLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        tooltipLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        add(tooltipLabel);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel tooltipLabel;
    // End of variables declaration//GEN-END:variables
}
