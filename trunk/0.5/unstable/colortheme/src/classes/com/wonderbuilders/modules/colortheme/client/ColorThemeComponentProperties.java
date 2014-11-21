/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.colortheme.client;

import com.jme.image.Texture;
import com.jme.scene.Spatial;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import com.wonderbuilders.modules.colortheme.common.ColorTheme;
import java.util.ResourceBundle;
import javax.swing.JPanel;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import com.wonderbuilders.modules.colortheme.common.ColorThemeComponentServerState;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderComponent.AttachPointNode;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode.Type;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 * The property sheet for the color theme component.
 */
@PropertiesFactory(ColorThemeComponentServerState.class)
public class ColorThemeComponentProperties extends JPanel
        implements PropertiesFactorySPI {
    // The I18N resource bundle

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "com/wonderbuilders/modules/colortheme/client/resources/Bundle");
    
    private static final Logger LOGGER = Logger.getLogger(ColorThemeComponentProperties.class.getName());
    
    // The editor window
    private CellPropertiesEditor editor = null;
    /** Currently selected theme in a list */
    private ColorTheme currentTheme;
    /** Name of the color theme currently in use by this cell. */
    private String themeInUse;
    /** A theme previously in use. */
    private String previousTheme;
    /** Set of all themes in a list. */
    private Set<ColorTheme> allThemes;
    /** Set of themes that should be deleted (renamed or removed themes). */
    private Set<String> themesToDelete;
    /** Maps current texture image in object to alternative. Map key is original texture location,
     *  and value is alternative image.
     */
    private Map<String,String> textureMapping;
    /** Name of the original color theme used when dialog is opened. This should save 
     *  the state if a preview is being used while dialog is open. If no theme is applied 
     * originally,  this should be <code>null</code>.
     */
    private String originalTheme;
    /** Indicates that preview has been applied while dialog is opened. */
    private boolean previewApplied = false;
    /** 
     * Contains original textures used to revert object to original state. Map keys are locations of newly 
     * applied textures, while values are original textures. Keys are changed each time new texture is applied.
     * This field is used for preview mode.
     */
    private Map<String,String> originalTextures;

    public ColorThemeComponentProperties() {
        // Initialize the GUI
        themeInUse = null;
        allThemes = new TreeSet<ColorTheme>();
        themesToDelete = new HashSet<String>();
        textureMapping = new HashMap<String, String>();
        originalTextures = new HashMap<String, String>();
        initComponents();
    }

    /**
     * @inheritDoc()
     */
    public String getDisplayName() {
        return BUNDLE.getString("Color_Theme_Component");
    }

    /**
     * @inheritDoc()
     */
    public JPanel getPropertiesJPanel() {
        return this;
    }

    /**
     * @inheritDoc()
     */
    public void setCellPropertiesEditor(CellPropertiesEditor editor) {
        this.editor = editor;
        texturesList.setCellRenderer(new TextureListCellRenderer(editor.getCell().getCellCache().getSession().getSessionManager().getServerNameAndPort()));
    }

    /**
     * @inheritDoc()
     */
    public void open() {
        // read initial values for each field
        CellServerState state = editor.getCellServerState();
        CellComponentServerState compState = state.getComponentServerState(
                ColorThemeComponentServerState.class);

        if (state != null) {
            ColorThemeComponentServerState cccss =
                    (ColorThemeComponentServerState) compState;
            themeInUse = cccss.getCurrentColorTheme();
            // load themes from server
            SharedMapCli map = findMap();
            allThemes.clear();
            // add theme 'None' if does not exist
            if(!map.containsKey(ColorTheme.NONE_THEME_NAME)){
                map.put(ColorTheme.NONE_THEME_NAME, ColorTheme.createNeutralColorTheme());
            }

            for (SharedData sd : map.values()) {
                allThemes.add((ColorTheme) sd);
            }
            textureMapping = cccss.getTextureMapping();
        }
        // find all textures present in  model
        originalTextures.clear();
        reloadTextureList();
        // save original data for preview
        originalTheme = themeInUse;
        previewApplied = false;

        // update UI
//        restore();
         // restore all values to original
        reloadThemeList();
        // clear state fields
        currentTheme = (ColorTheme) themesList.getSelectedValue();
        themesToDelete.clear();

        // set up texture info
        textureCountLabel.setText(Integer.toString(texturesList.getModel().getSize()));
        fileNameLabel.setVisible(false);
        sizeLabel.setVisible(false);
        resolutionLabel.setVisible(false);
        texturesList.setSelectedIndex(-1);
    }

    /**
     * @inheritDoc()
     */
    public void close() {
        
    }

    /**
     * @inheritDoc()
     */
    public void apply() {

        CellServerState state = editor.getCellServerState();
        CellComponentServerState compState = state.getComponentServerState(
                ColorThemeComponentServerState.class);

        // set color theme in use, if any
        if (state != null) {
            ((ColorThemeComponentServerState) compState).setCurrentColorTheme(themeInUse);
            ((ColorThemeComponentServerState) compState).setPreviousTheme(previousTheme);
            ((ColorThemeComponentServerState) compState).setTextureMapping(textureMapping);
        }
        // update any stored original values
        SharedMapCli map = findMap();
        // remove deleted or renamed  themes
        for (String s : themesToDelete) {
            map.remove(s);
        }
        // add themes in a list
        for (ColorTheme ct : allThemes) {
            map.put(ct.getThemeName(), ct);
        }

        // include this server state when the server is updated
        editor.addToUpdateList(compState);

        // update the UI
        checkDirty();
    }
    
    

    /**
     * @inheritDoc()
     */
    public void restore() {
       
        // clear any remaining texture mapping
        textureMapping.clear();
        
        // remove any preview data, if applied
        if(previewApplied){
            themeInUse = originalTheme;
            reloadThemeList();
            ((ColorThemeComponent)editor.getCell().getComponent(ColorThemeComponent.class)).previewColor(originalTheme, null,originalTextures);
             SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    try{
                        Thread.sleep(500);
                    } catch(InterruptedException iex){
                        LOGGER.log(Level.SEVERE, "Interrupted while waiting for renderer to finish: {0}", iex.getMessage());
                    }
                    reloadTextureList();
                }
            });
        }

        // mark the panel as no longer dirty
        checkDirty();
    }

    /**
     * Check if the component has been modified
     */
    protected void checkDirty() {
        boolean dirty = false;

        // update the panel
        editor.setPanelDirty(ColorThemeComponentProperties.class, dirty);
    }

    /**
     * Reloads theme list items from {@code #allThemes}.
     */
    private void reloadThemeList() {
        DefaultListModel model = new DefaultListModel();
        // index of theme currently in use
        int themeInUseIndex = -1;
        for (ColorTheme th : allThemes) {
            model.addElement(th);
            if (th.getThemeName().equals(themeInUse)) {
                themeInUseIndex = model.getSize() - 1;
            }
        }
        themesList.setModel(model);
        themesList.setSelectedIndex(themeInUseIndex);
    }
    
    /**
     * Reloads list of textures found in model.
     */
    private void reloadTextureList(){
        CellRendererJME renderer = (CellRendererJME) editor.getCell().getCellRenderer(RendererType.RENDERER_JME);
        RenderComponent rc = renderer.getEntity().getComponent(RenderComponent.class);
        Spatial sceneRoot = rc.getSceneRoot();
        if (sceneRoot instanceof AttachPointNode) {
            sceneRoot = ((AttachPointNode) sceneRoot).getChild(0);
        }

        final DefaultListModel textureListModel = new DefaultListModel();
        TreeScan.findNode(sceneRoot, new ProcessNodeInterface() {

            public boolean processNode(Spatial node) {
                if (node instanceof AttachPointNode) {
                    return false;
                }
                TextureState ts = (TextureState) node.getRenderState(StateType.Texture);
                if (ts != null) {
                    Texture tx = ts.getTexture();
                    if (tx != null) {
                        // do not add the same texture twice
                        if (!textureListModel.contains(tx)) {
                            textureListModel.addElement(tx);
                        }
                        // save original texture data
                        if (!previewApplied) {
                            originalTextures.put(tx.getImageLocation(), tx.getImageLocation());
                        }
                    }


                }

                return true;
            }
        });
        texturesList.setModel(textureListModel);
    }

    /**
     * Finds shared state map for this component.
     * 
     * @return  shared state map
     */
    private SharedMapCli findMap() {
        SharedStateComponent ssc = editor.getCell().getCellCache().getEnvironmentCell().getComponent(SharedStateComponent.class);
        return ssc.get(ColorThemeComponentConstants.COLOR_THEME_SHARED_MAP);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        themesList = new javax.swing.JList();
        DefaultListModel themeListModel = new DefaultListModel();
        ColorTheme theme = new ColorTheme();
        theme.setThemeName(ColorTheme.NONE_THEME_NAME);
        themeListModel.addElement(theme);
        themesList.setModel(themeListModel);
        useThemeButton = new javax.swing.JButton();
        editThemeButton = new javax.swing.JButton();
        duplicateThemeButton = new javax.swing.JButton();
        addThemeButton = new javax.swing.JButton();
        removeThemeButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        texturesList = new javax.swing.JList();
        textureCountTitleLabel = new javax.swing.JLabel();
        textureCountLabel = new javax.swing.JLabel();
        fileNameLabel = new javax.swing.JLabel();
        sizeLabel = new javax.swing.JLabel();
        resolutionLabel = new javax.swing.JLabel();
        noteLabel = new javax.swing.JLabel();
        textureTextField = new javax.swing.JTextField();
        useTextureButton = new javax.swing.JButton();
        browseTextureButton = new javax.swing.JButton();
        resetTextureButton = new javax.swing.JButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/wonderbuilders/modules/colortheme/client/resources/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("ColorThemeComponentProperties.jLabel1.text")); // NOI18N

        themesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        themesList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                themesListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(themesList);

        useThemeButton.setText(bundle.getString("ColorThemeComponentProperties.useThemeButton.text")); // NOI18N
        useThemeButton.setEnabled(false);
        useThemeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useThemeButtonActionPerformed(evt);
            }
        });

        editThemeButton.setText(bundle.getString("ColorThemeComponentProperties.editThemeButton.text")); // NOI18N
        editThemeButton.setEnabled(false);
        editThemeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editThemeButtonActionPerformed(evt);
            }
        });

        duplicateThemeButton.setText(bundle.getString("ColorThemeComponentProperties.duplicateThemeButton.text")); // NOI18N
        duplicateThemeButton.setEnabled(false);
        duplicateThemeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateThemeButtonActionPerformed(evt);
            }
        });

        addThemeButton.setText("+");
        addThemeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addThemeButtonActionPerformed(evt);
            }
        });

        removeThemeButton.setText("-");
        removeThemeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeThemeButtonActionPerformed(evt);
            }
        });

        texturesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        texturesList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                texturesListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(texturesList);

        textureCountTitleLabel.setText(bundle.getString("ColorThemeComponentProperties.textureCountTitleLabel.text")); // NOI18N

        textureCountLabel.setText("0"); // NOI18N

        fileNameLabel.setText("filename"); // NOI18N
        fileNameLabel.setVisible(false);

        sizeLabel.setText("size"); // NOI18N
        sizeLabel.setVisible(false);

        resolutionLabel.setText("resolution");
        resolutionLabel.setVisible(false);

        noteLabel.setFont(new java.awt.Font("Dialog", 0, 12));
        noteLabel.setText(bundle.getString("ColorThemeComponentProperties.noteLabel.text")); // NOI18N

        textureTextField.setText(bundle.getString("ColorThemeComponentProperties.textureTextField.text")); // NOI18N

        useTextureButton.setText(bundle.getString("ColorThemeComponentProperties.useTextureButton.text")); // NOI18N
        useTextureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useTextureButtonActionPerformed(evt);
            }
        });

        browseTextureButton.setText(bundle.getString("ColorThemeComponentProperties.browseTextureButton.text")); // NOI18N
        browseTextureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseTextureButtonActionPerformed(evt);
            }
        });

        resetTextureButton.setText(bundle.getString("ColorThemeComponentProperties.resetTextureButton.text")); // NOI18N
        resetTextureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetTextureButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 278, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .add(useTextureButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(browseTextureButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(resetTextureButton)))
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, duplicateThemeButton, 0, 0, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, editThemeButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, useThemeButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(addThemeButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(removeThemeButton))
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(jLabel1))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(resolutionLabel)
                            .add(sizeLabel)
                            .add(layout.createSequentialGroup()
                                .add(textureCountTitleLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(textureCountLabel))
                            .add(fileNameLabel)
                            .add(noteLabel)))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(textureTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 428, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(27, 27, 27)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(layout.createSequentialGroup()
                        .add(useThemeButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(editThemeButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(duplicateThemeButton))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addThemeButton)
                    .add(removeThemeButton))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(textureCountTitleLabel)
                            .add(textureCountLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(fileNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sizeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(resolutionLabel)
                        .add(18, 18, 18)
                        .add(noteLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 141, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(textureTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(7, 7, 7)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(useTextureButton)
                    .add(browseTextureButton)
                    .add(resetTextureButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

   
    private void editThemeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editThemeButtonActionPerformed
        ColorThemeEditorPanel panel = new ColorThemeEditorPanel(currentTheme);
        int status = JOptionPane.showConfirmDialog(this, panel, "Edit Color Theme", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (status == JOptionPane.OK_OPTION) {
            
            Map<String, String> map = panel.getColorMap();
            currentTheme.setColorMap(map);
            // if theme is renamed, add it to 'delete' list
            if (!currentTheme.getThemeName().equals(panel.getThemeName())) {
                themesToDelete.add(currentTheme.getThemeName());
                currentTheme.setThemeName(panel.getThemeName());
            }
            
            reloadThemeList();
            editor.setPanelDirty(ColorThemeComponentProperties.class, true);
            // shared state is updated immediatelly!!!!!!
              Map<String, ColorTheme> tmpMap = new HashMap<String, ColorTheme>();
            for(ColorTheme ct : allThemes){
                tmpMap.put(ct.getThemeName(), ct);
            }
                editor.getCell().getComponent(ColorThemeComponent.class).previewColor(ColorTheme.NONE_THEME_NAME,tmpMap, textureMapping);
                editor.getCell().getComponent(ColorThemeComponent.class).previewColor(themeInUse,tmpMap, textureMapping);
            }
    }//GEN-LAST:event_editThemeButtonActionPerformed

    private void addThemeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addThemeButtonActionPerformed
        ColorTheme newTheme = new ColorTheme();
        // iterate through themes set and check if theme starting with "Untitled Theme" already exists
        // append new index number of so
        int count = 0;
        for (ColorTheme th : allThemes) {
            if (th.getThemeName().startsWith(ColorTheme.DEFAULT_THEME_NAME)) {
                // theme name starts with "Untitled Theme", increment counter
                count++;
            }
        }
        if (count > 0) {
            newTheme.setThemeName(ColorTheme.DEFAULT_THEME_NAME + " " + count);
        }
        allThemes.add(newTheme);
        reloadThemeList();
        editor.setPanelDirty(ColorThemeComponentProperties.class, true);
    }//GEN-LAST:event_addThemeButtonActionPerformed

    /**
     * Selection listener for theme list selection.
     * 
     * @param evt  selection event
     */
    private void themesListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_themesListValueChanged
        if (!evt.getValueIsAdjusting()) {
            currentTheme = (ColorTheme) themesList.getSelectedValue();
            if (currentTheme != null) {
                useThemeButton.setEnabled(true);
                editThemeButton.setEnabled(true);
                duplicateThemeButton.setEnabled(true);
            } else {
                useThemeButton.setEnabled(false);
                editThemeButton.setEnabled(false);
                duplicateThemeButton.setEnabled(false);
            }
        }
    }//GEN-LAST:event_themesListValueChanged

    /**
     * Handles action events for Duplicate button.
     * 
     * @param evt event
     */
    private void duplicateThemeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duplicateThemeButtonActionPerformed
        ColorTheme newTheme = new ColorTheme((ColorTheme) themesList.getSelectedValue());
        allThemes.add(newTheme);
        reloadThemeList();
        editor.setPanelDirty(ColorThemeComponentProperties.class, true);
    }//GEN-LAST:event_duplicateThemeButtonActionPerformed

    /**
     * Handles action events for Remove button.
     * @param evt event.
     */
    private void removeThemeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeThemeButtonActionPerformed
        // add theme to remove list
        ColorTheme selection = (ColorTheme) themesList.getSelectedValue();
        // show confirmation dialog
        int status = JOptionPane.showConfirmDialog(this, 
                                MessageFormat.format(BUNDLE.getString(ColorThemeComponentConstants.KEY_DELETE_THEME_MESSAGE),selection.getThemeName()), 
                                BUNDLE.getString(ColorThemeComponentConstants.KEY_DELETE_THEME_TITLE), JOptionPane.OK_CANCEL_OPTION);
        if(status == JOptionPane.CANCEL_OPTION){
            return;
        }
        themesToDelete.add(selection.getThemeName());
        allThemes.remove(selection);
        reloadThemeList();
        editor.setPanelDirty(ColorThemeComponentProperties.class, true);
    }//GEN-LAST:event_removeThemeButtonActionPerformed

    private void useThemeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useThemeButtonActionPerformed
        previousTheme = themeInUse;
        themeInUse = ((ColorTheme) themesList.getSelectedValue()).getThemeName();
        ColorThemeComponent comp = editor.getCell().getComponent(ColorThemeComponent.class);
        // put all themes into a map. This will make sure that any new themes added after dialog is opened
        // are passed to renderer
        Map<String, ColorTheme> tmpMap = new HashMap<String, ColorTheme>();
        for(ColorTheme ct : allThemes){
            tmpMap.put(ct.getThemeName(), ct);
        }
        comp.previewColor(themeInUse,tmpMap, textureMapping);
        previewApplied = true;
        // reload textures, if there's been a change
        // we need a delay so new texture can be rendered to model
        if(!textureMapping.isEmpty()){
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    try{
                        Thread.sleep(500);
                    } catch(InterruptedException iex){
                        LOGGER.log(Level.SEVERE, "Interrupted while waiting for renderer to finish: {0}", iex.getMessage());
                    }
                    reloadTextureList();
                }
            });
            
        }

        editor.setPanelDirty(ColorThemeComponentProperties.class, true);
    }//GEN-LAST:event_useThemeButtonActionPerformed

    /**
     * Performs texture replacement. Texture can be either local (on user's machine) or
     * remote (on server)
     * 
     * @param local <code>true</code> if texture is on user's machine, <code>false</code> otherwise
     */
    private void doReplaceTexture(boolean local){
        // if local, browse local file system
        String serverAndPort = editor.getCell().getCellCache().getSession().getSessionManager().getServerNameAndPort();
        if (local) {
            JFileChooser chooser = new JFileChooser();
            TextureImagePreviewPanel preview = new TextureImagePreviewPanel();
            chooser.setAccessory(preview);
            chooser.addPropertyChangeListener(preview);
            // add filters for image files
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("Image files (.jpg, .png, .gif, .tiff)", 
                        "jpg", "jpeg", "JPG", "JPEG", "png", "PNG", "gif", "GIF", "tiff", "TIFF"));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setMultiSelectionEnabled(false);
            int status = chooser.showOpenDialog(this);
            if (status == JFileChooser.APPROVE_OPTION) {
                try {
                    String content = uploadTexture(chooser.getSelectedFile());
                    textureTextField.setText(content);
                    // remove key equal to this value. This is used to avoid accidental repeating of textures
                    // after preview/apply cycle
                    textureMapping.remove(AssetUtils.getAssetURL(textureTextField.getText(), serverAndPort).toString());
                    textureMapping.put(((Texture)texturesList.getSelectedValue()).getImageLocation(), textureTextField.getText());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, 
                                MessageFormat.format(BUNDLE.getString("colortheme.texture.upload.error.ex"), ex.getMessage()), 
                                BUNDLE.getString("colortheme.texture.upload.error.ex.title"), JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            textureMapping.put(((Texture)texturesList.getSelectedValue()).getImageLocation(), textureTextField.getText());
        }
        // keep track of original textures
        // needs to be converted to proper URL for loading
        String val = originalTextures.get(((Texture)texturesList.getSelectedValue()).getImageLocation());
        if(val != null){
            originalTextures.remove(((Texture)texturesList.getSelectedValue()).getImageLocation());
            try{
                originalTextures.put(AssetUtils.getAssetURL(textureTextField.getText(), serverAndPort).toString(), val);
            } catch(MalformedURLException ex){
                // put original URL if error occurs
                originalTextures.put(textureTextField.getText(), val);
                LOGGER.log(Level.SEVERE, "Unable to convert URL: {0}", ex.getMessage());
            }
            
        }
        
        editor.setPanelDirty(ColorThemeComponentProperties.class, true);
        useThemeButtonActionPerformed(null);
    }
    
    private void texturesListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_texturesListValueChanged
        // populate texture info
        if (!evt.getValueIsAdjusting()) {
            Texture tx = (Texture) texturesList.getSelectedValue();
            if (tx != null) {
                fileNameLabel.setText(tx.getImageLocation().substring(tx.getImageLocation().lastIndexOf('/') + 1));
                fileNameLabel.setVisible(true);
                resolutionLabel.setText(tx.getImage().getWidth() + " x " + tx.getImage().getHeight());
                resolutionLabel.setVisible(true);
                useTextureButton.setEnabled(true);
                browseTextureButton.setEnabled(true);
                textureTextField.setText(tx.getImageLocation());
            } else {

                fileNameLabel.setVisible(false);
                resolutionLabel.setVisible(false);
                useTextureButton.setEnabled(false);
                browseTextureButton.setEnabled(false);
            }

        }
    }//GEN-LAST:event_texturesListValueChanged

    private void useTextureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useTextureButtonActionPerformed
        Texture tx = (Texture)texturesList.getSelectedValue();
        if(tx != null && !tx.getImageLocation().equals(textureTextField.getText())){
            doReplaceTexture(false);
        }
    }//GEN-LAST:event_useTextureButtonActionPerformed

    private void browseTextureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseTextureButtonActionPerformed
        doReplaceTexture(true);
    }//GEN-LAST:event_browseTextureButtonActionPerformed

    private void resetTextureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetTextureButtonActionPerformed
        ((ColorThemeComponent)editor.getCell().getComponent(ColorThemeComponent.class)).previewColor(themeInUse, null,originalTextures);
        originalTextures.clear();
        textureMapping.clear();
       SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    try{
                        Thread.sleep(500);
                    } catch(InterruptedException iex){
                        LOGGER.log(Level.SEVERE, "Interrupted while waiting for renderer to finish: {0}", iex.getMessage());
                    }
                    reloadTextureList();
                }
            });
    }//GEN-LAST:event_resetTextureButtonActionPerformed

    /**
     * Upload texture file from local disk to server.
     * 
     * @param file texture file to upload
     * @return  texture URL on server
     */
    private String uploadTexture(File file) throws ContentRepositoryException, IOException {
        WonderlandSession session = editor.getCell().getCellCache().getSession();
        ContentRepositoryRegistry repoReg = ContentRepositoryRegistry.getInstance();
        ContentRepository repo = repoReg.getRepository(session.getSessionManager());

        ContentCollection root = repo.getUserRoot();
        ContentResource node = (ContentResource) root.getChild(file.getName());
        if (node == null) {
            node = (ContentResource) root.createChild(file.getName(), Type.RESOURCE);
        }

        node.put(file);
        return "wlcontent:/" + node.getPath();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addThemeButton;
    private javax.swing.JButton browseTextureButton;
    private javax.swing.JButton duplicateThemeButton;
    private javax.swing.JButton editThemeButton;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel noteLabel;
    private javax.swing.JButton removeThemeButton;
    private javax.swing.JButton resetTextureButton;
    private javax.swing.JLabel resolutionLabel;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JLabel textureCountLabel;
    private javax.swing.JLabel textureCountTitleLabel;
    private javax.swing.JTextField textureTextField;
    private javax.swing.JList texturesList;
    private javax.swing.JList themesList;
    private javax.swing.JButton useTextureButton;
    private javax.swing.JButton useThemeButton;
    // End of variables declaration//GEN-END:variables
}
