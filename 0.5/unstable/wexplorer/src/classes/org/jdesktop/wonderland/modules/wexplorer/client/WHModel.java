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

import java.io.File;
import javax.swing.*;
import java.io.Serializable;
import java.util.Map;

/**
 * @author Alex
 */
public class WHModel implements Serializable {
    private String id, title, desc, author, rating;
    private File file;  //contains ref only if the model is downloaded
    private Map<String, String> sources;
    private ImageIcon thumbnail;

    public WHModel(String author, String desc, String id, 
            ImageIcon thumbnail, String title,
            Map<String, String> sources, String raiting) {
        this.author = author;
        this.desc = desc;
        this.id = id;
        this.thumbnail = thumbnail;
        this.title = title;
        this.sources = sources;
        this.rating = raiting;
    }

    public void setFile(String path) {
        file = new File(path);
    }

    public File getFile() {
        return file;
    }

    public String getRating() {
        return rating;
    }

    public String getFormats() {
        String text="";
        for (String s : sources.keySet()) {
            text += " ." + s ;
        }
        return text;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSources(Map<String, String> sources) {
        this.sources = sources;
    }

    public void setThumbnail(ImageIcon thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDesc() {
        return desc;
    }

    public String getId() {
        return id;
    }

    public Map<String, String> getSources() {
        return sources;
    }

    public ImageIcon getThumbnail() {
        return thumbnail;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }

}
