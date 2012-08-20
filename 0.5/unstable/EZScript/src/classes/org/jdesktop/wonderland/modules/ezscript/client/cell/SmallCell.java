/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.cell;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.ezscript.client.EZScriptComponent;
import org.jdesktop.wonderland.modules.ezscript.common.cell.iCellClientState;

/**
 *
 * @author Ryan
 */
public class SmallCell extends Cell {
    
    
    @UsesCellComponent
    EZScriptComponent scriptComponent;
    
    @UsesCellComponent
    ChannelComponent channelComponent;
    
    private String rendererClassName = null;
    
    private SmallCell delegate = null;
    
    private CellRenderer renderer = null;
    
    public SmallCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
    
    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        
        switch(status) {
            case RENDERING:
                rendering(increasing);
                break;
            case ACTIVE:
                active(increasing);
                break;
            case INACTIVE:
                inactive(increasing);
            case DISK:
                disk(increasing);
                       
        }
    }
    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if(rendererType == RendererType.RENDERER_JME) {
            //instantiate such a renderer
            if(rendererClassName == null) {
                logger.warning("RENDERER CLASS NAME IS NULL!");
                return super.createCellRenderer(rendererType);
            }
            
            
            renderer = instantiateRenderer(rendererClassName);
            
            if(renderer == null) {
                logger.warning("RENDERER FROM INSTANTIATION IS NULL!");
                return super.createCellRenderer(rendererType);
            }
            
            logger.warning("RETURNING NEW RENDERER!");
            return renderer;
        } else {
            logger.warning("RENDERER TYPE IS NOT JME!");
            return super.createCellRenderer(rendererType);
        }
    }
    
    @Override
    public void setClientState(CellClientState configData) {
        super.setClientState(configData);
        
        rendererClassName = ((iCellClientState)configData).getRendererClassName();
    }

    protected void rendering(boolean increasing) {
        if(delegate != null) {
            delegate.rendering(increasing);
        }
    }

    protected  void active(boolean increasing) {
        if(delegate != null) {
            delegate.active(increasing);
        }
    }

    protected  void inactive(boolean increasing) {
        if(delegate != null) {
            delegate.inactive(increasing);
        }
    }

    protected  void disk(boolean increasing) {
        if(delegate != null) {
            delegate.disk(increasing);
        }
    }

    private CellRenderer instantiateRenderer(String rendererClassName) {
        
        CellRenderer _renderer = null;
        
        
        try {
            ScannedClassLoader loader =
                    LoginManager.getPrimary().getClassloader();
        
            logger.warning("LOOKING FOR CLASS: "+rendererClassName);
            Class clazz = Class.forName(rendererClassName, true, loader);
            
            if(clazz == null) {
                logger.warning("COULD NOT FIND CLASS!");
            } else {
                logger.warning("FOUND CLASS: "+clazz.getName());
            }
            
            logger.warning("GETTING CONSTRUCTOR!");
            Constructor constructor = clazz.getConstructor(Cell.class);
            
            logger.warning("CREATING A NEW INSTANCE OF: "+rendererClassName);
            _renderer = (CellRenderer) constructor.newInstance(this);
            
            if(_renderer == null) {
                logger.warning("RENDERER CAME OUT NULL!");
            }
            
        } catch (InstantiationException ex) {
            Logger.getLogger(SmallCell.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(SmallCell.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(SmallCell.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(SmallCell.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(SmallCell.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(SmallCell.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SmallCell.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return _renderer;
        }
    }
    
    public CellRenderer getRenderer() {
        return renderer;
    }

    public <T extends SmallCell> void setDelegate(T vci) {
        delegate = vci;
        
        switch(this.getStatus()) {
            case INACTIVE:
                delegate.inactive(true);
            case DISK:
                delegate.disk(true);
            case ACTIVE:
                delegate.active(true);
            case RENDERING:
                delegate.rendering(true);
                break;
        }
    }

}
