/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */

package org.jdesktop.wonderland.modules.rockwellcollins.stickynote.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import com.sun.sgs.app.util.ScalableHashMap;
import com.sun.sgs.app.util.ScalableHashSet;
import java.io.Serializable;
import java.util.Set;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.server.cell.MirroredStickyNoteCellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author Ryan Babiuch
 */
public class MirroredStickyRegistry implements ManagedObject, Serializable {
    private static final String KEY = MirroredStickyRegistry.class.getName();
    //private final ManagedReference<Set<MirroredStickyNoteCellMO>> listenersRef;
    private final ManagedReference<ScalableHashMap<String, Set<MirroredStickyNoteCellMO>>> listenersRef;
            //new ScalableHashSet<MirroredStickyNoteCellMO>();

    private MirroredStickyRegistry() {
       ScalableHashMap<String, Set<MirroredStickyNoteCellMO>> listeners = new ScalableHashMap();
       // Set<MirroredStickyNoteCellMO> listeners = new ScalableHashSet();
        listenersRef = AppContext.getDataManager().createReference(listeners);
    }
    public void addListener(String group, MirroredStickyNoteCellMO listener) {
        if(listenersRef.get().containsKey(group)) {
            listenersRef.get().get(group).add(listener);
            System.out.println("Listener added.");
        }
        else {
            Set s = new ScalableHashSet();
            s.add(listener);
            listenersRef.get().put(group, s);
            System.out.println("New group created and listener added.");
        }
    }

    public void removeListener(String group, MirroredStickyNoteCellMO listener) {
        if(listenersRef.get().containsKey(group)) {
            if(listenersRef.get().get(group).contains(listener)) {
                listenersRef.get().get(group).remove(listener);
                System.out.println("Listener removed!");
            }
        }
    }

    public void notifyChange(WonderlandClientSender sender, String text, CellMessage message, String group) {
        for(MirroredStickyNoteCellMO listener : listenersRef.get().get(group)) {
            listener.setText(sender, text, message, group);
        }
    }

    public static MirroredStickyRegistry getInstance() {
        DataManager dm = AppContext.getDataManager();

        try {
            return (MirroredStickyRegistry)dm.getBinding(KEY);
        } catch(NameNotBoundException nnbe) {
            MirroredStickyRegistry reg = new MirroredStickyRegistry();
            dm.setBinding(KEY, reg);
            return reg;
        }
    }

}
