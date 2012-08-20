/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.globals.builder.cell;

import com.jme.renderer.ColorRGBA;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.Renderer;
import org.jdesktop.wonderland.modules.ezscript.client.cell.AnotherMovableComponent;
import org.jdesktop.wonderland.modules.ezscript.client.cell.SmallCell;
import org.jdesktop.wonderland.modules.ezscript.common.cell.BlockClientState;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;

/**
 *
 * @author Ryan
 */
public class BlockCell extends SmallCell {

    @UsesCellComponent
    ChannelComponent channelComponent;
    
    @UsesCellComponent
    MovableComponent movable;
    
    @UsesCellComponent
    AnotherMovableComponent anotherMovable;
    
    @UsesCellComponent
    SharedStateComponent sharedState;
    
    
    @Renderer
    private BlockBrush renderer;
    
    private ColorRGBA material;
    private String textureURL;

    public BlockCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    @Override
    protected void rendering(boolean increasing) {
    }

    @Override
    protected void active(boolean rendering) {
    }

    @Override
    protected void inactive(boolean rendering) {
    }

    @Override
    protected void disk(boolean rendering) {
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {

        //get our class so we can search for our field annotated with @Renderer
        Class clazz = this.getClass();

        
        //for every field in our class
        for (Field field : clazz.getDeclaredFields()) {
            //if the current field is annotated...
            if (field.getAnnotation(Renderer.class) != null) {
                try {
                    //get the class of the field
                    Class rendererClass = field.getType();

                    //create a constructor from the class of the current field
                    Constructor constructor = rendererClass.getConstructor(BlockCell.class);
                    
                    
                    //create an instance from the constructor we just created
                    renderer = (BlockBrush)constructor.newInstance(this);

                    //return
                    return renderer;


                } catch (InstantiationException ex) {
                    Logger.getLogger(BlockCell.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(BlockCell.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(BlockCell.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(BlockCell.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(BlockCell.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(BlockCell.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return null;
    }

    @Override
    public void setClientState(CellClientState clientState) {
        this.material = ((BlockClientState)clientState).getMaterial();
        this.textureURL = ((BlockClientState)clientState).getTextureURL();
    }
    
    @Override
    public CellRenderer getRenderer() {
        return renderer;
    }
    
    protected ColorRGBA getMaterial() {
        return this.material;
    }

    protected String getTextureURL() {
        return this.textureURL;
    }
}
