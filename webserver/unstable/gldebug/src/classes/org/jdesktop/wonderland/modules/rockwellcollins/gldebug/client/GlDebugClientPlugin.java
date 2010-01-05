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
package org.jdesktop.wonderland.modules.rockwellcollins.gldebug.client;

import com.jme.renderer.jogl.JOGLContextCapabilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.WorldManager;

/**
 * Client-size plugin for getting GL debug information
 * @author Ryan (mymegabyte)
 */
@Plugin
public class GlDebugClientPlugin extends BaseClientPlugin {

    private JMenuItem glDebugMI = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ServerSessionManager loginInfo) {
        glDebugMI = new JMenuItem("OpenGL Debug");
        glDebugMI.setToolTipText("View OpenGL debug information");
        glDebugMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {              
                    RenderManager rm = WorldManager.getDefaultWorldManager().getRenderManager();
                    JOGLContextCapabilities caps = rm.getContextCaps();
                    String capabilities = getCapabilityString(caps);
                    showCapabilityDialog(capabilities);
            }
        });

        super.initialize(loginInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void activate() {
        // activate
        JmeClientMain.getFrame().addToToolsMenu(glDebugMI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void deactivate() {
        // deactivate
        JmeClientMain.getFrame().removeFromToolsMenu(glDebugMI);
    }

    public String getCapabilityString(JOGLContextCapabilities cap) {
        StringBuffer sb = new StringBuffer();
        sb.append("GL_ARB_fragment_program..." + cap.GL_ARB_fragment_program + "\n");
        sb.append("GL_ARB_fragment_shader..." + cap.GL_ARB_fragment_shader + "\n");
        sb.append("GL_ARB_imaging..." + cap.GL_ARB_imaging + "\n");
        sb.append("GL_ARB_multitexture..." + cap.GL_ARB_multitexture + "\n");
        sb.append("GL_ARB_shader_objects..." + cap.GL_ARB_shader_objects + "\n");
        sb.append("GL_ARB_shading_language_100..." + cap.GL_ARB_shading_language_100 + "\n");
        sb.append("GL_ARB_texture_border_clamp..." + cap.GL_ARB_texture_border_clamp + "\n");
        sb.append("GL_ARB_texture_cube_map..." + cap.GL_ARB_texture_cube_map + "\n");
        sb.append("GL_ARB_texture_env_combine..." + cap.GL_ARB_texture_env_combine + "\n");
        sb.append("GL_ARB_texture_env_dot3..." + cap.GL_ARB_texture_env_dot3 + "\n");
        sb.append("GL_ARB_texture_mirrored_repeat..." + cap.GL_ARB_texture_mirrored_repeat + "\n");
        sb.append("GL_ARB_texture_non_power_of_two..." + cap.GL_ARB_texture_non_power_of_two + "\n");
        sb.append("GL_ARB_texture_rectangle..." + cap.GL_ARB_texture_rectangle + "\n");
        sb.append("GL_ARB_vertex_buffer_object..." + cap.GL_ARB_vertex_buffer_object + "\n");
        sb.append("GL_ARB_vertex_program..." + cap.GL_ARB_vertex_program + "\n");
        sb.append("GL_ARB_vertex_shader..." + cap.GL_ARB_vertex_shader + "\n");
        sb.append("GL_EXT_blend_equation_separate..." + cap.GL_EXT_blend_equation_separate + "\n");
        sb.append("GL_EXT_blend_func_separate..." + cap.GL_EXT_blend_func_separate + "\n");
        sb.append("GL_EXT_blend_minmax..." + cap.GL_EXT_blend_minmax + "\n");
        sb.append("GL_EXT_blend_subtract..." + cap.GL_EXT_blend_subtract + "\n");
        sb.append("GL_EXT_compiled_vertex_array..." + cap.GL_EXT_compiled_vertex_array + "\n");
        sb.append("GL_EXT_fog_coord..." + cap.GL_EXT_fog_coord + "\n");
        sb.append("GL_EXT_stencil_two_side..." + cap.GL_EXT_stencil_two_side + "\n");
        sb.append("GL_EXT_stencil_wrap..." + cap.GL_EXT_stencil_wrap + "\n");
        sb.append("GL_EXT_texture_3d..." + cap.GL_EXT_texture_3d + "\n");
        sb.append("GL_EXT_texture_compression_s3tc..." + cap.GL_EXT_texture_compression_s3tc + "\n");
        sb.append("GL_EXT_texture_filter_anisotropic..." + cap.GL_EXT_texture_filter_anisotropic + "\n");
        sb.append("GL_EXT_texture_mirror_clamp..." + cap.GL_EXT_texture_mirror_clamp + "\n");
        sb.append("GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS_ARB..." + cap.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS_ARB + "\n");
        sb.append("GL_MAX_FRAGMENT_UNIFORM_COMPONENTS_ARB..." + cap.GL_MAX_FRAGMENT_UNIFORM_COMPONENTS_ARB + "\n");
        sb.append("GL_MAX_TEXTURE_COORDS_ARB..." + cap.GL_MAX_TEXTURE_COORDS_ARB + "\n");
        sb.append("GL_MAX_TEXTURE_IMAGE_UNITS_ARB..." + cap.GL_MAX_TEXTURE_IMAGE_UNITS_ARB + "\n");
        sb.append("GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT..." + cap.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT + "\n");
        sb.append("GL_MAX_TEXTURE_UNITS..." + cap.GL_MAX_TEXTURE_UNITS + "\n");
        sb.append("GL_MAX_VARYING_FLOATS_ARB..." + cap.GL_MAX_VARYING_FLOATS_ARB + "\n");
        sb.append("GL_MAX_VERTEX_ATTRIBS_ARB..." + cap.GL_MAX_VERTEX_ATTRIBS_ARB + "\n");
        sb.append("GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS_ARB..." + cap.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS_ARB + "\n");
        sb.append("GL_MAX_VERTEX_UNIFORM_COMPONENTS_ARB..." + cap.GL_MAX_VERTEX_UNIFORM_COMPONENTS_ARB + "\n");
        sb.append("GL_SGIS_generate_mipmap..." + cap.GL_SGIS_generate_mipmap + "\n");
        sb.append("GL_SHADING_LANGUAGE_VERSION_ARB..." + cap.GL_SHADING_LANGUAGE_VERSION_ARB + "\n");
        sb.append("GL_VERSION_1_2..." + cap.GL_VERSION_1_2 + "\n");
        sb.append("GL_VERSION_2_0..." + cap.GL_VERSION_2_0 + "\n");
        sb.append("GL_VERSION_2_1..." + cap.GL_VERSION_2_1 + "\n");
        sb.append("GL_VERSION_3_0..." + cap.GL_VERSION_3_0 + "\n");

        return sb.toString();
    }

    public void showCapabilityDialog(String cap) {
        JDialog dialog = new JDialog(JmeClientMain.getFrame().getFrame());
        JTextArea jta = new JTextArea();
        jta.setLineWrap(false);
        jta.setColumns(40);
        jta.setRows(30);
        jta.setText(cap);
        JScrollPane jsp = new JScrollPane(jta);
        dialog.add(jsp);
        dialog.pack();
        dialog.setLocationRelativeTo(JmeClientMain.getFrame().getFrame());
        dialog.setResizable(true);
        dialog.setVisible(true);

    }
}
