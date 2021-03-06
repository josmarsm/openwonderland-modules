/**
 * Project Wonderland
 *
 * $URL$
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Rev$
 * $Date$
 * $Author$
 */
package com.sun.labs.miw.client.cell;

import com.sun.labs.miw.common.MIWTrack;
import com.sun.labs.miw.common.MIWAlbum;
import com.sun.labs.miw.common.PlaylistAction;
import com.sun.labs.miw.common.PlaylistCellMessage;
import java.awt.Image;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author  Jeff Moguillansky
 */
public class AlbumViewer extends javax.swing.JFrame {
    
    /** Creates new form AlbumViewer */
    public AlbumViewer() {
        initComponents();
        setVisible(UI.showWindows);
    }
    public void setAlbum(MIWAlbum album) {
        if (album == null) return;
        if (this.album != null) {
            Album prevAlbum = UI.albums.get(this.album.getName());
            prevAlbum.setActive(false);
        }
        this.album = album;
        Album albumNode = UI.albums.get(album.getName());
        albumNode.setActive(true);
        URL art = UI.albums.get(album.getName()).getURL();
        Image image = new ImageIcon(art).getImage();
        image = Util.scaleImage(image,AlbumView.getWidth(), AlbumView.getHeight());
        AlbumView.setIcon(new ImageIcon(image));
        
        List<MIWTrack> tracks = album.getTracks();
       int numTracks = tracks.size();
       String[][] data = new String[numTracks][5];
       String albumName = album.getName();
       for (int j = 0; j<numTracks; j++) {
           MIWTrack t = tracks.get(j);
           data[j][0] = j+"";
           data[j][1] = ""+UI.playlist.indexOf(t);
           data[j][2] = t.getName();
           data[j][3] = t.getTrackId();
           data[j][4] = ""+t.getLength();
       }
       TracksTable.setModel(new DefaultTableModel(data,new String[] {"","Queue","Name","ID","Length"}));;
       this.setVisible(true);
    }
   void refresh() {
       setAlbum(album);
   }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Previous = new javax.swing.JToggleButton();
        Play = new javax.swing.JToggleButton();
        Next = new javax.swing.JToggleButton();
        Time = new javax.swing.JSlider();
        TimeLabel = new javax.swing.JTextField();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        AlbumView = new javax.swing.JLabel();
        TracksPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TracksTable = new javax.swing.JTable();
        AddToPlaylist = new javax.swing.JButton();

        setTitle("Album Viewer");

        Previous.setText("Previous");

        Play.setText("Play");

        Next.setText("Next");

        TimeLabel.setFont(new java.awt.Font("Dialog", 0, 14));
        TimeLabel.setText("0 : 00");

        AlbumView.setMaximumSize(new java.awt.Dimension(512, 512));
        AlbumView.setMinimumSize(new java.awt.Dimension(512, 512));
        AlbumView.setPreferredSize(new java.awt.Dimension(512, 512));
        jTabbedPane1.addTab("Album", AlbumView);

        TracksTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(TracksTable);

        AddToPlaylist.setText("Add to Playlist");
        AddToPlaylist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddToPlaylistActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout TracksPanelLayout = new org.jdesktop.layout.GroupLayout(TracksPanel);
        TracksPanel.setLayout(TracksPanelLayout);
        TracksPanelLayout.setHorizontalGroup(
            TracksPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
            .add(TracksPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(AddToPlaylist)
                .addContainerGap(376, Short.MAX_VALUE))
        );
        TracksPanelLayout.setVerticalGroup(
            TracksPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(TracksPanelLayout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 275, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(AddToPlaylist)
                .addContainerGap(206, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Tracks", TracksPanel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jTabbedPane1)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(Previous)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(Play)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(Next))
                    .add(layout.createSequentialGroup()
                        .add(Time, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(TimeLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(Previous)
                    .add(Play)
                    .add(Next))
                .add(1, 1, 1)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, TimeLabel)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, Time, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTabbedPane1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void AddToPlaylistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddToPlaylistActionPerformed
        if (album == null) return;
        int rows[] = TracksTable.getSelectedRows();
        List<MIWTrack> tracks = new Vector<MIWTrack>();
        for (int row : rows) {
            int i = Integer.valueOf((String)TracksTable.getValueAt(row,0));
            int playIndex =  Integer.valueOf((String)TracksTable.getValueAt(row,1));
            if (playIndex != -1) continue;
            tracks.add(album.getTracks().get(i));
        }
        if (tracks.isEmpty()) return;
        UI.albumCloudCell.requestAddToPlaylist(tracks,PlaylistAction.APPEND_FRONT);
    }//GEN-LAST:event_AddToPlaylistActionPerformed
    private MIWAlbum album;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddToPlaylist;
    private javax.swing.JLabel AlbumView;
    private javax.swing.JToggleButton Next;
    private javax.swing.JToggleButton Play;
    private javax.swing.JToggleButton Previous;
    private javax.swing.JSlider Time;
    private javax.swing.JTextField TimeLabel;
    private javax.swing.JPanel TracksPanel;
    private javax.swing.JTable TracksTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables
    
}
