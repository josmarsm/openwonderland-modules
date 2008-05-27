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
package com.sun.labs.miw.common;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author jkaplan
 */
public class AlbumInfoParser extends DefaultHandler {

    private static final Logger logger =
            Logger.getLogger(AlbumInfoParser.class.getName());
    
    private int albumId;
    
    private MIWAlbum currentAlbum;
    private boolean inAlbum;
    
    private MIWTrack currentTrack;
    private boolean inTrack;
    
    private int albumCount;
    private int trackCount;
    private long parseTime;
    private StringBuffer accumulator;

    public Collection<MIWAlbum> albums;
    
    public AlbumInfoParser() {
    }
    
    public Collection<MIWAlbum> parse(URL albumInfoURL) 
            throws ParserConfigurationException, SAXException, IOException
    {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
            
        SAXParser parser = spf.newSAXParser();
    
        long startTime = System.currentTimeMillis();
        parser.parse(albumInfoURL.openStream(), this);
        parseTime = System.currentTimeMillis() - startTime;
        
        logger.info("Parsed " + getAlbumCount() + " albums in " + 
                    parseTime + " ms.");
        
        return albums;
    }
    
    public Collection<MIWAlbum> getAlbums() {
        return albums;
    }
    
    public int getAlbumCount() {
        return albumCount;
    }
    
    public int getTrackCount() {
        return trackCount;
    }

    public long getParseTime() {
        return parseTime;
    }
    
    @Override
    public void startDocument() throws SAXException {
        albumCount = 0;
        trackCount = 0;
        albums = new ArrayList<MIWAlbum>();
    }
  
    @Override
    public void startElement(String uri, String localName, 
                             String qName, Attributes attrs) 
            throws SAXException 
    {
        if (qName.equals("Album")) {
            inAlbum = true;
            currentAlbum = new MIWAlbum(String.valueOf(albumId++));
            albumCount++;
        } else if (qName.equals("Track")) {
            inTrack = true;
            currentTrack = new MIWTrack();
            currentTrack.setAlbum(currentAlbum.getName());
            currentTrack.setArtist(currentAlbum.getArtist());
            trackCount++;
        }
        
        accumulator = new StringBuffer();
    }

    @Override
    public void characters(char[] chars, int start, int length) 
            throws SAXException 
    {
        accumulator.append(chars, start, length);
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) 
            throws SAXException 
    {
        if (qName.equals("Album")) {
            albums.add(currentAlbum);
            inAlbum = false;
        } else if (qName.equals("Track")) {
            currentAlbum.getTracks().add(currentTrack);
            inTrack = false;
        } else if (qName.equals("albumname")) {
            currentAlbum.setName(accumulator.toString());
        } else if (qName.equals("artist") && !inTrack) {
            currentAlbum.setArtist(accumulator.toString());
        } else if (qName.equals("cover_small")) {
            try {
                currentAlbum.setArtURL(new URL(accumulator.toString()));
            } catch (MalformedURLException mue) {
                logger.log(Level.WARNING, "Bad url: " + accumulator.toString() +
                           " in " + currentAlbum.getAlbumId(), mue);
            }
        } else if (qName.equals("trackname")) {
            currentTrack.setName(accumulator.toString());
        } else if (qName.equals("tracknum")) {
            currentTrack.setTrackId(currentAlbum.getAlbumId() + "-" + 
                                    accumulator.toString());
        } else if (qName.equals("url")) {
            try {
                currentTrack.setAudioURL(new URL(accumulator.toString()));
            } catch (MalformedURLException mue) {
                logger.log(Level.WARNING, "Bad url: " + accumulator.toString() +
                           " in " + currentTrack.getTrackId());
            }
        } else if (qName.equals("seconds")) {
            currentTrack.setLength(Integer.parseInt(accumulator.toString()));
        }
        
        accumulator = new StringBuffer();
    }
 
    @Override
    public void endDocument() throws SAXException {
    }
    
    public static void main(String[] args) {
        
        AlbumInfoParser aip = new AlbumInfoParser();
        
        try {
            URL fileURL = new URL("file:/Users/jkaplan/Desktop/album_info.xml");
            aip.parse(fileURL);
            
            Iterator<MIWAlbum> all = aip.getAlbums().iterator();
            MIWAlbum first = all.next();
            System.out.println("First album");
            System.out.println(first.fullString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
