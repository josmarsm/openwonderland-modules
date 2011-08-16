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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class fetches the data from Google Warehouse
 * using the SwingWorker class.
 */

public class LoadDataWorker extends SwingWorker<List<WHModel>, WHModel> {

    MainPanel mp;
    DefaultListModel model;
    String url;

    public LoadDataWorker(String url, DefaultListModel model, MainPanel mpd) {
        this.model = model;
        this.url = url;
        this.mp = mpd;

        PropertyChangeListener listener =
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent event) {
                        if ("progress".equals(event.getPropertyName())) {
                            mp.getPBar().setValue((Integer) event.getNewValue());
                        }
                    }
                };
        this.addPropertyChangeListener(listener);
    }

    @Override
    protected List<WHModel> doInBackground() throws Exception {
        String id = "", title = "", desc = "", author = "", thumbnail = "", rating = "";
        ImageIcon img = null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(url);
        doc.getDocumentElement().normalize();
        NodeList nodeLst = doc.getElementsByTagName("entry");

        List<WHModel> results = new ArrayList<WHModel>();
        WHModel data = null;
        for (int s = 0; s < nodeLst.getLength(); s++) {
            Node fstNode = nodeLst.item(s);
            if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

                Element fstElmnt = (Element) fstNode;

                NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("title");
                Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
                NodeList fstNm = fstNmElmnt.getChildNodes();
                title = fstNm.item(0).getNodeValue();

                NodeList lstNmElmntLst = fstElmnt.getElementsByTagName("id");
                Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
                NodeList lstNm = lstNmElmnt.getChildNodes();
                id = lstNm.item(0).getNodeValue();
                id = id.substring(id.length() - 32);

                lstNmElmntLst = fstElmnt.getElementsByTagName("summary");
                lstNmElmnt = (Element) lstNmElmntLst.item(0);
                lstNm = lstNmElmnt.getChildNodes();
                desc = lstNm.item(0).getNodeValue();


                lstNmElmntLst = fstElmnt.getElementsByTagName("name");
                lstNmElmnt = (Element) lstNmElmntLst.item(0);
                lstNm = lstNmElmnt.getChildNodes();
                author = lstNm.item(0).getNodeValue();

                lstNmElmntLst = fstElmnt.getElementsByTagName("media:thumbnail");
                lstNmElmnt = (Element) lstNmElmntLst.item(1);
                thumbnail = lstNmElmnt.getAttribute("url");

                lstNmElmntLst = fstElmnt.getElementsByTagName("gd:rating");
                lstNmElmnt = (Element) lstNmElmntLst.item(0);
                rating = lstNmElmnt.getAttribute("average");

                lstNmElmntLst = fstElmnt.getElementsByTagName("media:content");
                Map<String, String> content = new TreeMap<String, String>();

                for (int i = 0; i < lstNmElmntLst.getLength(); i++) {
                    Element e = (Element) lstNmElmntLst.item(i);
                    String extension = e.getAttribute("type");
                    extension = extension.substring(extension.length() - 3);
                    String url = e.getAttribute("url");
                    content.put(extension, url);
                }

                try {
                    img = new ImageIcon(new URL(thumbnail));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                data = new WHModel(author, desc, id, img, title, content, rating);

                publish(data);
                setProgress((s + 1) * 100 / nodeLst.getLength());
                results.add(data);
            }
        }
        return results;
    }

    @Override
    protected void process(List<WHModel> list) {
        for (WHModel m : list) {
            if (isCancelled()) {
                break;
            }
            model.addElement(m);
        }
    }

    @Override
    protected void done() {
        mp.getPBar().setValue(0);
    }
}
