/*
 * Copyright 2007 Sun Microsystems, Inc.
 *
 * This file is part of jVoiceBridge.
 *
 * jVoiceBridge is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License version 2 as 
 * published by the Free Software Foundation and distributed hereunder 
 * to you.
 *
 * jVoiceBridge is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied this 
 */
package org.jdesktop.wonderland.modules.timeline.server;

import org.jdesktop.wonderland.modules.timeline.common.TimelineSegmentTreatmentMessage;

import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.ProximityComponentMO;

import com.sun.sgs.app.AppContext;

import com.sun.sgs.app.ManagedReference;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingCapsule;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.bounding.OrientedBoundingBox;

import com.jme.math.Vector3f;

import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
import com.sun.mpk20.voicelib.app.Treatment;
import com.sun.mpk20.voicelib.app.TreatmentGroup;
import com.sun.mpk20.voicelib.app.TreatmentSetup;
import com.sun.mpk20.voicelib.app.Spatializer;
import com.sun.mpk20.voicelib.app.VoiceManager;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

/*
 * XXX This code is here temporarily until there's a message receiver to process a TimlineSegmentTreatmentMessage.
 */
public class SegmentTreatment {

    private static String serverURL;

    static {
        serverURL = System.getProperty("wonderland.web.server.url");
    }

    public SegmentTreatment() {
	TimelineSegmentTreatmentMessage msg = null;

        VoiceManager vm = AppContext.getManager(VoiceManager.class);

        TreatmentGroup group = vm.createTreatmentGroup(msg.getSegmentID());
	
        TreatmentSetup setup = new TreatmentSetup();

	TrapozoidleSpatializer spatializer = new TrapozoidleSpatializer();

	setup.spatializer = spatializer;

        spatializer.setAttenuator(msg.getAttenuator());

        String treatment = msg.getTreatment();

        String treatmentId = msg.getSegmentID();

	if (treatment.startsWith("wls://")) {
            /*
             * We need to create a URL from wls:<module>/path
             */
            treatment = treatment.substring(6);  // skip past wls://

            int ix = treatment.indexOf("/");

            if (ix < 0) {
                System.out.println("Bad treatment:  " + treatment);
                return;
            }

            String moduleName = treatment.substring(0, ix);

            String path = treatment.substring(ix + 1);

            System.out.println("Module:  " + moduleName + " treatment " + treatment);

            URL url;

            try {
                url = new URL(new URL(serverURL),
                    "webdav/content/modules/installed/" + moduleName + "/audio/" + path);

                treatment = url.toString();
                System.out.println("Treatment: " + treatment);
            } catch (MalformedURLException e) {
                System.out.println("bad url:  " + e.getMessage());
		return;
            }
        }

        setup.treatment = treatment;
	//setup.managedListenerRef = 
	//    AppContext.getDataManager().createReference((ManagedCallStatusListener) this);

        if (setup.treatment == null || setup.treatment.length() == 0) {
            System.out.println("Invalid treatment '" + setup.treatment + "'");
	    return;
        }

        Vector3f location = null; //cellRef.get().getLocalTransform(null).getTranslation(null);

        setup.x = location.getX();
        setup.y = location.getY();
        setup.z = location.getZ();

        System.out.println("Starting treatment " + setup.treatment + " at (" + setup.x 
	    + ":" + setup.y + ":" + setup.z + ")");

        try {
	    Treatment t = vm.createTreatment(treatmentId, setup);
            group.addTreatment(t);

	    //addProximityListener(t);
        } catch (IOException e) {
            System.out.println("Unable to create treatment " + setup.treatment + e.getMessage());
            return;
        }
    }

    private void addProximityListener(Treatment treatment) {
        // Fetch the proximity component, we will need this below. If it does
        // not exist (it should), then log an error
        ProximityComponentMO component = null; //cellRef.get().getComponent(ProximityComponentMO.class);

        if (component == null) {
            System.out.println("The AudioTreatment Component does not have a " +
                    "Proximity Component for Cell ID " ); //+ cellRef.get().getCellID());
            return;
        }

        // We are making this component live, add a listener to the proximity component.
	BoundingVolume[] bounds = new BoundingVolume[1];

	//float cellRadius = getCellRadius();

        bounds[0] = new BoundingSphere(0f, new Vector3f());

        //AudioTreatmentProximityListener proximityListener = 
	//    new AudioTreatmentProximityListener(cellRef.get(), treatment);

        //component.addProximityListener(proximityListener, bounds);
    }

}
