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
package org.jdesktop.wonderland.modules.cmu.common.events;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A collection of event/response pairs for a particular cell.
 * @author kevin
 */
public class EventResponseList extends AbstractList<EventResponsePair>
        implements Serializable {

    private final List<EventResponsePair> events = new ArrayList<EventResponsePair>();

    /**
     * Write this collection to the given file.
     * @param file File to write
     * @throws FileNotFoundException If the specified file does not exist
     */
    public void writeToFile(File file) throws FileNotFoundException {
        FileOutputStream outStream = new FileOutputStream(file);
        XMLEncoder encoder = new XMLEncoder(outStream);
        encoder.writeObject(this);
    }

    /**
     * Read an EventResponseCollection from a file.
     * @param file The file to read
     * @return A newly created EventResponseCollection from the file
     * @throws FileNotFoundException If the specified file does not exist
     */
    public static EventResponseList readFromStream(InputStream inStream) {
        XMLDecoder decoder = new XMLDecoder(inStream);
        return (EventResponseList) decoder.readObject();
    }

    /**
     * Get an iterator for the event/response pairs in this collection.
     * @return Iterator for the event/response pairs in this collection
     */
    @Override
    public Iterator<EventResponsePair> iterator() {
        return events.iterator();
    }

    /**
     * Get the number of event/response pairs in this collection.
     * @return Size of this collection
     */
    @Override
    public int size() {
        return events.size();
    }

    /**
     * Add an event/response pair to this collection.
     * @param erp The pair to add
     * @return true, since this will change the collection
     */
    @Override
    public boolean add(EventResponsePair erp) {
        return this.events.add(erp);
    }

    /**
     * Get the specified list element.
     * @param n The index of the list element to retrieve
     * @return The desired list element
     */
    @Override
    public EventResponsePair get(int n) {
        return this.events.get(n);
    }
}
