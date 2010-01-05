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
package org.jdesktop.wonderland.modules.proximitytest.common;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * Cell server state for proximity test
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@XmlRootElement(name="sample-cell")
@ServerState
public class ProximityCellServerState extends CellServerState {
    private List<BoundingVolume> clientBounds;
    private List<BoundingVolume> serverBounds;

    /** Default constructor */
    public ProximityCellServerState() {
    }

    @XmlJavaTypeAdapter(BoundsTypeAdapter.class)
    public List<BoundingVolume> getClientBounds() {
        return clientBounds;
    }

    public void setClientBounds(List<BoundingVolume> clientBounds) {
        this.clientBounds = clientBounds;
    }

    @XmlJavaTypeAdapter(BoundsTypeAdapter.class)
    public List<BoundingVolume> getServerBounds() {
        return serverBounds;
    }

    public void setServerBounds(List<BoundingVolume> serverBounds) {
        this.serverBounds = serverBounds;
    }

    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.proximitytest.server.ProximityCellMO";
    }

    @Override
    public String toString() {
        return "[ProximityCell]";
    }

    class BoundsTypeAdapter extends XmlAdapter<BoundsHolder[], List<BoundingVolume>> {
        @Override
        public List<BoundingVolume> unmarshal(BoundsHolder[] v) throws Exception {
            List<BoundingVolume> out = new ArrayList<BoundingVolume>(v.length);
            for (BoundsHolder bh : v) {
                out.add(bh.getBounds());
            }
            return out;
        }

        @Override
        public BoundsHolder[] marshal(List<BoundingVolume> v) throws Exception {
            List<BoundsHolder> out = new ArrayList<BoundsHolder>(v.size());
            for (BoundingVolume bv : v) {
                if (bv instanceof BoundingBox) {
                    out.add(new BoxBoundsHolder((BoundingBox) bv));
                } else if (bv instanceof BoundingSphere) {
                    out.add(new SphereBoundsHolder((BoundingSphere) bv));
                }
            }

            return out.toArray(new BoundsHolder[out.size()]);
        }
    }

    private abstract static class BoundsHolder {
        public BoundsHolder() {}

        protected abstract BoundingVolume getBounds();
    }

    @XmlRootElement(name="box-bounds")
    private static class BoxBoundsHolder extends BoundsHolder {
        private float x;
        private float y;
        private float z;

        public BoxBoundsHolder() {
        }

        public BoxBoundsHolder(BoundingBox box) {
            this.x = box.xExtent;
            this.y = box.yExtent;
            this.z = box.zExtent;
        }

        @XmlTransient
        protected BoundingBox getBounds() {
            return new BoundingBox(new Vector3f(), x, y, z);
        }

        @XmlElement
        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        @XmlElement
        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        @XmlElement
        public float getZ() {
            return z;
        }

        public void setZ(float z) {
            this.z = z;
        }
    }

    @XmlRootElement(name="sphere-bounds")
    private static class SphereBoundsHolder extends BoundsHolder {
        private float r;

        public SphereBoundsHolder() {
        }

        public SphereBoundsHolder(BoundingSphere sphere) {
            this.r = sphere.radius;
        }

        @XmlTransient
        protected BoundingSphere getBounds() {
            return new BoundingSphere(r, new Vector3f());
        }

        @XmlElement
        public float getR() {
            return r;
        }

        public void setR(float r) {
            this.r = r;
        }
    }
}
