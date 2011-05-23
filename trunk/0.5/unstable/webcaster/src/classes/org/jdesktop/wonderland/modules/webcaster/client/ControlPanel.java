/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
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

package org.jdesktop.wonderland.modules.webcaster.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author Christian O'Connell
 */
public class ControlPanel extends JPanel
{
    private WebcasterCell cell;
    
    private JButton recordButton;
    private JPanel previewPanel;
    private JLabel streamBox;
    private JTextField dirField;

    public ControlPanel(WebcasterCell cell)
    {
        super(new GridBagLayout());
        this.cell = cell;
        initComponents();
        dirField.setText("wonderland");
        previewPanel.add(cell.getCaptureComponent(), BorderLayout.CENTER);
    }

    public String getStreamName(){
        return dirField.getText();
    }

    private void record(ActionEvent evt)
    {
        streamBox.setEnabled(cell.getRecording());
        dirField.setEnabled(cell.getRecording());
        
        recordButton.setText(!cell.getRecording()?"Stop Capture":"Begin Capture");
        cell.setRecording(!cell.getRecording());
    }

    @Override
    public Dimension getPreferredSize(){
        return new Dimension(640, 400);
    }

    private void initComponents()
    {
        GridBagConstraints c = new GridBagConstraints();

        previewPanel = new JPanel(new BorderLayout());
        previewPanel.setPreferredSize(new Dimension(640, 360));
        previewPanel.setMaximumSize(new Dimension(640, 360));
        previewPanel.setMinimumSize(new Dimension(640, 360));
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.BOTH;
        this.add(previewPanel, c);

        streamBox = new JLabel();
        streamBox.setText("Stream Name:");
        streamBox.setHorizontalTextPosition(JLabel.RIGHT);
        c.weightx = 0.5;
        c.weighty = 0.2;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.EAST;
        this.add(streamBox, c);

        dirField = new JTextField();
        dirField.setPreferredSize(new Dimension(420, 30));
        dirField.setMaximumSize(new Dimension(420, 30));
        dirField.setMinimumSize(new Dimension(420, 30));
        c.gridx = 1;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(dirField, c);

        recordButton = new JButton();
        recordButton.setText("Begin Capture");
        recordButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                record(evt);
            }
        });
        c.gridx = 2;
        c.gridy = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        this.add(recordButton, c);
    }
}
