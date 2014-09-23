/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */

package com.wonderbuilders.modules.animation.client;

import com.wonderbuilders.modules.animation.common.EZScriptAnimationControl;
import com.wonderbuilders.modules.animation.common.FrameRange;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JViewport;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;

/**
 * Parent panel for multiple frame range panels.
 *
 * @author Vladimir Djurovic
 */
class AdvancedContainerPanel extends JPanel {
    
    /**
     * Properties sheet editor.
     */
    private CellPropertiesEditor editor;

    /**
     * Creates new instance.
     * 
     * @param ranges set of frame ranges to include in this panel
     */
    AdvancedContainerPanel(SortedSet<FrameRange> ranges){
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        if(ranges != null){
            Iterator<FrameRange> it = ranges.iterator();
            while (it.hasNext()){
                add(new ManualAnimationPanel(true, it.next()));
            }
        }
        
    }

    public AdvancedContainerPanel(List<EZScriptAnimationControl> data) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        if(data != null){
            for(EZScriptAnimationControl control : data){
                add(new EZScriptAnimationPanel(control));
            }
        }
    }
    
    

    /**
     * Set property sheet editor.
     * 
     * @param editor editor
     */
    public void setEditor(CellPropertiesEditor editor) {
        this.editor = editor;
    }
    
    
    /**
     * Adds new manual frame range panel to this container.
     * 
     * @param panel panel to add
     */
    void addChildPanel(ManualAnimationPanel panel){
        add(panel);
        validate();
        ((JViewport)getParent()).setView(this);
    }
    
    /**
     * Add new EZScript control panel.
     * 
     * @param panel  panel to add
     */
    void addChildPanel(EZScriptAnimationPanel panel){
        add(panel);
        validate();
        ((JViewport)getParent()).setView(this);
    }
    
    /**
     * Removes specified manual frame range panel from this container.
     * 
     * @param panel panel to remove
     */
    void removeChildPanel(ManualAnimationPanel panel){
        remove(panel);
        validate();
        ((JViewport)getParent()).setView(this);
        markDirty();
    }
    
    /**
     * Removes specified EZScript animation panel from this container.
     * 
     * @param panel panel to remove
     */
    void removeChildPanel(EZScriptAnimationPanel panel){
        remove(panel);
        validate();
        ((JViewport)getParent()).setView(this);
        markDirty();
    }
    
    /**
     * Verifies that all input data are correct.
     * 
     * @return <code>true</code> if input is correct, <code>false</code> otherwise
     */
    boolean verifyInput(){
        boolean state = true;
        Component[] comps = getComponents();
        for(Component comp : comps){
            if(comp instanceof ManualAnimationPanel){
                ManualAnimationPanel map = (ManualAnimationPanel)comp;
                state &= map.verifyInput();
                if(!state){
                    break;
                }
            }
        }
        return state;
    }
    
    /**
     * Returns entered data as a {@link FrameRange} objects set.
     * 
     * @param maxFrames maximum frame number allowed
     * @return  set of frame ranges
     */
    public SortedSet<FrameRange> createFrameRangeData(int maxFrames){
        SortedSet<FrameRange> set = new TreeSet<FrameRange>();
        Component[] comps = getComponents();
        for(Component comp : comps){
            if(comp instanceof ManualAnimationPanel){
                ManualAnimationPanel map = (ManualAnimationPanel)comp;
                FrameRange fr = map.getFrameRange();
                fr.setMaxFrame(maxFrames);
                set.add(fr);
            }
        }
        return set;
    }
    
    /**
     * Returns user entered data as a {@link EZScriptAnimationControl} objects list.
     * 
     * @return list of objects
     */
    public List<EZScriptAnimationControl> createEZScriptControls(){
        List<EZScriptAnimationControl> list = new ArrayList<EZScriptAnimationControl>();
        Component[] comps = getComponents();
        for(Component comp : comps){
            if(comp instanceof EZScriptAnimationPanel){
                EZScriptAnimationPanel panel = (EZScriptAnimationPanel)comp;
                EZScriptAnimationControl control = panel.getControlData();
                list.add(control);
            }
        }
        return list;
    }
    
    /**
     * Mark panel as dirty.
     */
    public void markDirty(){
        editor.setPanelDirty(AnimationComponentProperties.class, true);
    }
}
