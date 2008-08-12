/**
 * Project Looking Glass
 *
 * $RCSfile: AvatarConfigurator.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.21 $
 * $Date: 2008/01/30 15:55:08 $
 * $State: Exp $
 */
package dsclient;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Color;
import java.net.URL;
import java.io.File;
import java.util.Arrays;

import org.jdesktop.j3d.utils.loaders.rtg.ArticulatedSkeleton;
import org.jdesktop.j3d.utils.loaders.rtg.ArticulatedSkeletonSwappableBodyPart;
import org.jdesktop.j3d.utils.loaders.rtg.ModelFactory;
import org.jdesktop.j3d.utils.loaders.rtg.RtgBaseSkeleton;
import org.jdesktop.lg3d.wonderland.config.common.AvatarModelConfig;
//import org.jdesktop.lg3d.wonderland.config.common.WonderlandConfig;
import org.jdesktop.lg3d.wonderland.darkstar.client.ChannelController;
import org.jdesktop.lg3d.wonderland.darkstar.common.AvatarInfo;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.AvatarSetupMessage;
import org.jdesktop.lg3d.wonderland.scenemanager.AnimatedModelController;
import org.jdesktop.lg3d.wonderland.scenemanager.AssetManager;
import org.jdesktop.lg3d.wonderland.scenemanager.avatar.SimpleAvatar;

/**
 *
 * @author  kkg
 */
public class AvatarConfigurator {

  private static Logger logger = Logger.getLogger("wonderland.AvatarConfigurator");
  

  static {
    logger.setLevel(Level.INFO);
  }
  //private static boolean enableGosling = Boolean.getBoolean("wonderland.avatar.gosling.enable");
  private static AvatarConfigurator avatarConfigurator = null;
  private AvatarConfig avatarConfigs[];

  public synchronized static AvatarConfigurator getDefault(String baseURL) {
    if (avatarConfigurator == null) {
      avatarConfigurator = new AvatarConfigurator(baseURL);
    }

    return avatarConfigurator;
  }

  public static AvatarInfo getDefaultAvatarInfo(String baseURL) {
//    AvatarInfo avatarInfo = AvatarModelConfig.getDefault().getAvatarInfo();
//    
//
//    // If we have a valid avatar with no body parts, generate a random avatar.
//    if (avatarInfo.getBodyParts() == null) {
//      logger.log(Level.WARNING,"Valid avatar with no body parts");
      AvatarConfigurator ac = getDefault(baseURL);
      int gender = 2; //hardcode the web interface here
      AvatarInfo avatarInfo = ac.avatarConfigs[0].getAvatarInfo();

      
      // save the default
      /*
       AvatarModelConfig config = AvatarModelConfig.getDefault();
      config.setAvatarInfo(avatarInfo);
      config.writeUserConfig();
      */
//    }

    logger.info("Loading default avatar : " + avatarInfo);
    return avatarInfo;
  }
 


  class AvatarPartConfig implements Comparable<AvatarPartConfig> {

    String partName;
    String[] partOptions;
    Color color;
    boolean useColors;
    //DefaultComboBoxModel partOptionsModel;
    int selectedIndex = 0;

    AvatarPartConfig(String partName, boolean useColors, String[] partOptions) {
      this.partName = partName;

    }



    String getOption() {
      return partOptions[selectedIndex];
    }

    String getColor() {
      return String.format("#%06x", (color.getRGB() & 0xffffff));
    }

    void setColor(String partOption, String color) {
      if (partOptions[selectedIndex].equals(partOption)) {
        // XXX: Color.decode does not decode 0xff??????  properly!!
        // this.color = Color.decode(color);

        int colorValue = (int) (Long.decode(color) & 0xffffff);
        this.color = new Color(colorValue);
      }
    }

    void setSelected(String partName, String partOption) {
      if ((partName == null) || partName.equals(this.partName)) {

        // find the index of the given option in the list of
        // options
        int index = -1;
        for (int i = 0; i < partOptions.length; i++) {
          if (partOptions[i].equals(partOption)) {
            index = i;
            break;
          }
        }
        if (index >= 0) {
          selectedIndex = index;
        }
      }
    }

    public int compareTo(AvatarPartConfig o) {
      return partName.compareTo(o.partName);
    }
  }

  /** Class to create an avatar configuration.
   *
   */
  class AvatarConfig {

    AvatarInfo.ModelType gender;
    String[] modelFileNames;
    ArticulatedSkeleton skeleton = null;
    int bodyPartCount = 0;
    AvatarPartConfig[] bodyPartConfig;
    AvatarPartConfig[] partShaderConfig;
    String base;

    AvatarConfig(AvatarInfo.ModelType gender, String baseURL) {
      this.gender = gender;
      base = baseURL;     
    }

    AvatarInfo getAvatarInfo() {
      //return new AvatarInfo(gender, buildBodyPartsList(), buildSharedMaterialsList(), buildMaterialColorsList());
      return new AvatarInfo(gender, new String[]{""},new String[]{""},new String[]{""});
    }
  }

  /** Creates new form AvatarConfigurator */
  private AvatarConfigurator(String baseURL) {

    avatarConfigs = new AvatarConfig[]{
                    new AvatarConfig(AvatarInfo.ModelType.WEB, baseURL)
                  };
  }
}
