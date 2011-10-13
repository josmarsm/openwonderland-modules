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

package org.jdesktop.wonderland.modules.sitting.client;

import com.jme.math.Vector3f;
import imi.objects.ChairObject;
import imi.objects.ObjectCollectionBase;
import imi.objects.SpatialObject;
import imi.scene.PSphere;
import imi.scene.polygonmodel.PPolygonModelInstance;

/**
 *
 * @author morrisford
 */
public class SittingChair  implements ChairObject
    {
    private boolean chairOccupied = false;
    private SpatialObject chairOwner = null;
    private Vector3f chairPosition;
    private Vector3f chairHeading;

    public SittingChair(Vector3f position, Vector3f heading)
        {
        chairPosition = position;
        chairHeading = heading;
        }

    public float getDesiredDistanceFromOtherTargets()
        {
        throw new UnsupportedOperationException("Not supported yet.");
        }

    public Vector3f getTargetPositionRef()
        {
        return chairPosition;
        }

    public Vector3f getTargetForwardVector()
        {
        return chairHeading;
        }

    public SpatialObject getOwner()
        {
        return chairOwner;
        }

    public void setOwner(SpatialObject owner)
        {
        chairOwner = owner;
        }

    public boolean isOccupied()
        {
        return false;
        }

    public boolean isOccupied(boolean arg0)
        {
        return false;
        }

    public void setOccupied(boolean occupied)
        {
        chairOccupied = occupied;
        }

    public void destroy()
        {
        throw new UnsupportedOperationException("Not supported yet.");
        }

    public PPolygonModelInstance getModelInst()
        {
        throw new UnsupportedOperationException("Not supported yet.");
        }

    public PSphere getBoundingSphere()
        {
        throw new UnsupportedOperationException("Not supported yet.");
        }

    public PSphere getNearestObstacleSphere(Vector3f arg0)
        {
        throw new UnsupportedOperationException("Not supported yet.");
        }

    public void setObjectCollection(ObjectCollectionBase arg0)
        {
        throw new UnsupportedOperationException("Not supported yet.");
        }

    public Vector3f getPositionRef()
        {
        throw new UnsupportedOperationException("Not supported yet.");
        }

    public Vector3f getRightVector()
        {
        throw new UnsupportedOperationException("Not supported yet.");
        }

    public Vector3f getForwardVector()
        {
        throw new UnsupportedOperationException("Not supported yet.");
        }
    }
