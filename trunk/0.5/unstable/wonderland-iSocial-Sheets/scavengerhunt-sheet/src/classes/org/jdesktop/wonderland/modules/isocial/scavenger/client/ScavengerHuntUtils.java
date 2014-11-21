/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.client;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.cell.utils.CellPlacementUtils;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.jme.ViewProperties;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;

/** Contains various utility methods used in this component.
 *
 * @author Vladimir Djurovic
 */
public class ScavengerHuntUtils {
    
    /** Current wonderland SESSION */
    private static final WonderlandSession SESSION;
    
     /** Format string for displaying time without hours (e.g. 0:00) */
    private static  final String FMT_NO_HR = "%d:%02d";
    
    /** Format string for displaying time with hours included (e.g. 0:00:00) */
    private static final String FMT_HR = "%d:%02d:%02d";
    
    static {
        SESSION = ISocialManager.INSTANCE.getSession().getPrimarySession();
        ContentRepositoryRegistry repoReg = ContentRepositoryRegistry.getInstance();
    }
    
    /**
     * Moves avatar next to the cell with specified ID.
     * 
     * @param itemCellId  cell ID
     */
    public static void moveAvatarToItem(String itemCellId) {
        
        Cell itemCell = ClientContextJME.getCellCache(SESSION).getCell(new CellID(Long.parseLong(itemCellId)));
        CellTransform gotoLocation = ScavengerHuntUtils.getGotoLocation(itemCell);
        try {
            ClientContextJME.getClientMain().gotoLocation(null, gotoLocation.getTranslation(null), gotoLocation.getRotation(null));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Retrieves cell trasformation of the given cell.
     * 
     * @param cell cell
     * @return  cell transformation
     */
    public static CellTransform getGotoLocation(Cell cell) {
        // get the bounds of the cell we are going to
        BoundingVolume bv = cell.getWorldBounds();
        if (isLarge(bv)) {
            // if the cell is big, go to the center rather than very
            // far away
            return cell.getWorldTransform();
        }
        
        // use the view properties to calculate the idea distance away
        ViewProperties vp = ViewManager.getViewManager().getViewProperties();
        float fov = vp.getFieldOfView();
        float min = vp.getFrontClip();
        float max = 30f;
        float distance = CellPlacementUtils.getDistance(bv, fov, min, max);
        
        // calculate the look vector to this cell -- we only care about the y axis
        // rotation
        Quaternion rotation = cell.getWorldTransform().getRotation(null);
        Vector3f lookVec = CellPlacementUtils.getLookDirection(rotation, null);

        // translate into a quaternion using lookAt
        Quaternion look = new Quaternion();
        look.lookAt(lookVec.negate(), Vector3f.UNIT_Y);

        // find the origin by translating the look vector
        Vector3f origin = lookVec.mult(distance);
        origin.addLocal(cell.getWorldTransform().getTranslation(null));
        return new CellTransform(look, origin);
    }
    
    /**
     * Check if cell is large or not.
     * 
     * @param bounds cell bounds
     * @return  <code>true</code> if cell is large, <code>false</code> otherwise
     */
    private static boolean isLarge(BoundingVolume bounds) {
        if (bounds instanceof BoundingBox) {
            BoundingBox box = (BoundingBox) bounds;
            return (box.xExtent > 20 || box.zExtent > 20);
        } else if (bounds instanceof BoundingSphere) {
            BoundingSphere sphere = (BoundingSphere) bounds;
            return (sphere.radius > 20);
        } else {
            // unknown bounds type
            return true;
        }
    }
    
    /**
     * Returns a path to item snapshot file.
     * 
     *
     */
    public static String getSnapshotPath(String itemCellId, String sheetId, String username) {
        StringBuilder sb = new StringBuilder("wlcontent://");
        sb.append("users/");
        sb.append(username);
        sb.append("/").append(createImageContentName(itemCellId, sheetId));

        try{
            return AssetUtils.getAssetURL(sb.toString(), SESSION.getSessionManager().getServerNameAndPort()).toString();
        } catch(MalformedURLException ex){
            throw new RuntimeException(ex);
        }
        
    }
    
    /**
     * Returns HTTP URL of item snapshot. This URL can be accessed from browser. Image 
     * might not necessarily exist in content repository.
     * 
     * @param itemCellId cell ID of item cell
     * @param sheetId sheet ID
     * @return HTTP URL
     */
    public static String getSnapshotHttpUrl(String itemCellId, String sheetId){
        StringBuilder sb = new StringBuilder("http://");
        sb.append(SESSION.getSessionManager().getServerNameAndPort());
        sb.append("/webdav/content/users/");
        sb.append(SESSION.getUserID().getUsername());
        sb.append("/").append(createImageContentName(itemCellId, sheetId));
        
        return sb.toString();
    }
    
    /**
     * Create a content node name for snapshot image of found item. Name is in form
     * 
     * <pre>
     *      sc_hunt_<i>sheetId</i>_<i>cellId</i>.png
     * </pre>
     *    
     * @param itemCellId cell ID of item cell
     * @param sheetId sheet ID
     * @return name of content node
     */
    public static String createImageContentName(String itemCellId, String sheetId){
        return "sc_hunt_" + sheetId + "_" + itemCellId + ".png";
    }
    
    /**
     * Formats given time in hh:ss or HH:mm:ss format, depending on whether time is
     * longer then hour or not.
     * 
     * @param time time in miliseconds
     * @return formatted string
     */
    public static String formatTime(long time){
        String result = null;
        long hr = TimeUnit.MILLISECONDS.toHours(time);
        long min = TimeUnit.MILLISECONDS.toMinutes(time - TimeUnit.HOURS.toMillis(hr));
        long sec = TimeUnit.MILLISECONDS.toSeconds(time - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        if(hr == 0){
            result = String.format(FMT_NO_HR, min, sec);
        } else {
            result = String.format(FMT_HR, hr,min, sec);
        }
        return result;
    }
}
