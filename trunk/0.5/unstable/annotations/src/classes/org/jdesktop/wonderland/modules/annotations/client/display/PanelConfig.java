/*
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

package org.jdesktop.wonderland.modules.annotations.client.display;

import java.awt.Color;
import java.awt.Font;

/**
 * Stores graphical config information for an AnnotationPane
 * @author mabonner
 */
public class PanelConfig {
  // Default Colors
  public static Color DEFAULT_BACKGROUND_COLOR = Color.DARK_GRAY;
  public static Color DEFAULT_FONT_COLOR = Color.WHITE;
  public static Color DEFAULT_SHADOW_COLOR = Color.BLACK;
  // Default alpha
  public static int DEFAULT_ALPHA = 200;
  
  private Color bgColor = DEFAULT_BACKGROUND_COLOR;
  private Color fontColor = DEFAULT_FONT_COLOR;
  private Color shadowColor = DEFAULT_SHADOW_COLOR;

  private Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

  public PanelConfig(Color bgc, Color fc, Color sc, Font f){
    bgColor = bgc;
    fontColor = fc;
    shadowColor = sc;
    font = f;
  }

  public PanelConfig(Color bgc, Color fc, Color sc){
    bgColor = bgc;
    fontColor = fc;
    shadowColor = sc;

  }

  public PanelConfig(){
    
  }

  public void setBackgroundColor(Color c){
    bgColor = c;
  }

  public void setFontColor(Color c){
    fontColor = c;
  }

  public void setFont(Font f){
    font = f;
  }

  public Color getBackgroundColor(){
    return bgColor;
  }

  public Color getFontColor(){
    return fontColor;
  }

  public Font getFont(){
    return font;
  }

  public Color getShadowColor() {
    return shadowColor;
  }

  public void setShadowColor(Color shadowColor) {
    this.shadowColor = shadowColor;
  }
  
}
