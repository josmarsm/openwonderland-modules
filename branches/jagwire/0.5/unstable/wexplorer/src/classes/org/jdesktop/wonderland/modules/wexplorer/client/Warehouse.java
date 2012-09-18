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

/**
 * @author Alexios Akrimpai (akrim777@gmail.com)
 */

package org.jdesktop.wonderland.modules.wexplorer.client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.jdesktop.wonderland.client.jme.dnd.FileListDataFlavorHandler;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Alex
 */
public class Warehouse {
    // Singelton for this class

    public static final Warehouse instance = new Warehouse();
    private Collection<WHModel> whObjects;
    private DefaultListModel remoteModels, localModels, lastSearches, downloadModels;
    private String localDir;
    private WHModel selectedModel;
    private int resultsPerPage = 12;

    // make sure we dont initialize this class from outside
    private Warehouse() {
        whObjects = new ArrayList<WHModel>();
        remoteModels = new DefaultListModel();
        lastSearches = new DefaultListModel();
        localModels = new DefaultListModel();
        downloadModels = new DefaultListModel();
    }

    public DefaultListModel getLastSearches() {
        return lastSearches;
    }

    public DefaultListModel getDownloadModels() {
        return downloadModels;
    }

    public void addLastSearch(String search) {
        if (!lastSearches.contains(search)) {
            lastSearches.addElement(search);
        }
    }

    public void navigate(final String keyword, int page, MainPanel mp) {
        remoteModels.clear();
        String url = "http://sketchup.google.com/3dwarehouse/data/entities?q="
                + keyword + "&styp=m&btnG=Search&start-index=" + (resultsPerPage * (page - 1) + 1)
                + "&max-results=" + resultsPerPage;

        LoadDataWorker worker = new LoadDataWorker(url, remoteModels, mp);
        worker.execute();
    }

    public DefaultListModel getModels() {
        return remoteModels;
    }

    public DefaultListModel getLocalModels() {
        return localModels;
    }

    public int getResultsPerPage() {
        return resultsPerPage;
    }

    public void setResultsPerPage(int n) {
        resultsPerPage = n;
    }

    public String getLocalDir() {
        return localDir;
    }

    public void setLocalDir(String dir) {
        localDir = dir;
    }

    public static String cutString(String text) {
        int n = 30;
        int count = n;
        String temp = "";
        String[] words = text.split(" ");
        for (String word : words) {
            if ((word.length() + temp.length()) > count) {
                temp += "<br>";
                count += n;
            }
            temp += word + " ";
        }
        return "<html>" + temp + "</html>";
    }

    public void setSelectedModel(String id, DefaultListModel models, MainPanel panel) {
        for (int i = 0; i < models.size(); i++) {
            WHModel o = (WHModel) models.get(i);
            if (o.getId().equals(id)) {
                selectedModel = o;
                changeDetailsPanelInfo(panel);
                return;
            }
        }
    }

    private void changeDetailsPanelInfo(MainPanel panel) {
        if (getSelectedModel() != null) {
            panel.setTitle(getSelectedModel().getTitle());
            panel.setAuthor(getSelectedModel().getAuthor());
            panel.setRating(getSelectedModel().getRating());
            panel.setFormats(getSelectedModel().getFormats());
//            System.out.println("File: " + selectedModel.getFile());
            if (selectedModel.getFile() != null) {
                panel.setImportButtons(true);
            } else {
                panel.setImportButtons(false);
            }
            changeDownloadList(panel);
        } else {
            System.out.println("Fix me by clearing the details");
        }
    }

    private void changeDownloadList(MainPanel panel) {
        downloadModels.clear();
        panel.getDownloadButton().setEnabled(false);
        for (String s : getSelectedModel().getSources().keySet()) {
            if (s.equals("kmz")) {
                downloadModels.addElement(".dae");
                panel.getDownloadButton().setEnabled(true);
            }
            if (s.equals("skp")) {
                downloadModels.addElement(".skp (currently not supported)");
            }
        }
    }

    public WHModel getSelectedModel() {
        return selectedModel;
    }

    public void downloadModel(final String url, final String path, final MainPanel mp) {
        SwingWorker worker = new SwingWorker<Integer, Void>() {

            @Override
            protected Integer doInBackground() {
                mp.getDownloadButton().setEnabled(false);
                BufferedInputStream in = null;
                FileOutputStream fout = null;
                try {
                    String filePath = path + File.separator + getSelectedModel().getTitle() + ".kmz";
                    URL fileUrl = new URL(url);
                    new File(path).mkdir();
                    new File(filePath).createNewFile();

                    int count;

                    URLConnection conexion = fileUrl.openConnection();
                    conexion.connect();
                    // this will be useful so that you can show a typical 0-100% progress bar
                    int lenghtOfFile = conexion.getContentLength();

                    // downlod the file
                    InputStream input = new BufferedInputStream(fileUrl.openStream());
                    OutputStream output = new FileOutputStream(filePath);

                    byte data[] = new byte[1024];

                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        // publishing the progress....
                        setProgress((int) (total * 100 / lenghtOfFile));
                        output.write(data, 0, count);
                    }

                    output.flush();
                    output.close();

                    getSelectedModel().setFile(filePath);
                    createMetaInfo(path);
                    mp.setImportButtons(true);
                } catch (Exception ed) {
                    ed.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                mp.getDownloadPBar().setValue(0);
                mp.getDownloadButton().setEnabled(true);
            }
        };

        PropertyChangeListener listener =
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent event) {
                        if ("progress".equals(event.getPropertyName())) {
                            mp.getDownloadPBar().setValue((Integer) event.getNewValue());
                        }
                    }
                };
        worker.addPropertyChangeListener(listener);
        worker.execute();
    }

    private void createMetaInfo(String path) throws IOException {
        path = path + File.separator + "meta";
        new File(path).mkdir();
        FileOutputStream fos = new FileOutputStream(path + File.separator + "data.obj");
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(getSelectedModel());
        out.close();
    }

    public void importModel(String path) {
        List<File> file = new ArrayList<File>();
        file.add(new File(path));
        FileListDataFlavorHandler.launchCellFromFileList(file);
    }

    public void navigateLocal() {
        String path = getLocalDir();
        if (path != null) {
            File directory = new File(path);
            FileInputStream fis = null;
            ObjectInputStream in = null;

            localModels.clear();

            for (String dir : directory.list()) {
                String filePath = path + File.separator + dir + File.separator + "meta" + File.separator + "data.obj";
                File file = new File(filePath);
                if (file.exists()) { //the right file size
                    try {
                        fis = new FileInputStream(filePath);
                        in = new ObjectInputStream(fis);
                        localModels.addElement((WHModel) in.readObject());
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    } catch (ClassNotFoundException cnf) {
                        cnf.printStackTrace();
                    }

                }
            }
        }
    }
}
