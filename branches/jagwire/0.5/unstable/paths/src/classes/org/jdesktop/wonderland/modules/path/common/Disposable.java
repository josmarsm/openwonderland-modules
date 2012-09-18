package org.jdesktop.wonderland.modules.path.common;

/**
 * This interface is intended for use with objects who's internal state can be cleared up to aid garbage collection.
 * This can be such objects as those which form parent / child relationships or listeners or any other object which
 * may hold a reference to another which may benefit from having the reference cleared when that object is intended
 * to be disposed of.
 *
 * @author Carl Jokl
 */
public interface Disposable {

    /**
     * Dispose of the internal state of this object to free up memory and / or aid garbage collection.
     */
    public void dispose();
}
