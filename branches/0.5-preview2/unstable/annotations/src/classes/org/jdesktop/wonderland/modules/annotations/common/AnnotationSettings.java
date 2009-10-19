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

package org.jdesktop.wonderland.modules.annotations.common;

import java.io.Serializable;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Stores graphical configuration information for an Annotation. Referenced
 * by the AnnotationEntity and AnnotationNode generated to represent this
 * anno in-world.
 * @author mabonner
 */
public class AnnotationSettings implements Serializable{
  // Default Colors
  public static Color DEFAULT_BACKGROUND_COLOR = Color.DARK_GRAY;
  public static Color DEFAULT_FONT_COLOR = Color.WHITE;
  public static Color DEFAULT_SHADOW_COLOR = Color.BLACK;
  // Default alpha
  public static int DEFAULT_ALPHA = 200;
  
  private Color bgColor = DEFAULT_BACKGROUND_COLOR;
  private Color fontColor = DEFAULT_FONT_COLOR;
  private Color shadowColor = DEFAULT_SHADOW_COLOR;

  /** base font */
  private Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
  /** derived from base font in the setFont method */
  private Font authorFont = null;
  /** derived from base font in the setFont method */
  private Font subjectFont = null;

  private float authorFontResolution = 40f;
  private float subjectFontResolution = 40f;

  public AnnotationSettings(Color bgc, Color fc, Color sc, Font f){
    bgColor = bgc;
    fontColor = fc;
    shadowColor = sc;
    setFont(f);
  }

  public AnnotationSettings(Color bgc, Color fc, Color sc){
    bgColor = bgc;
    fontColor = fc;
    shadowColor = sc;
    setFont(null);
  }

  /**
   * copy constructor
   * @param ac
   */
  public AnnotationSettings(AnnotationSettings ac){
    bgColor = ac.getBackgroundColor();
    fontColor = ac.getFontColor();
    shadowColor = ac.getShadowColor();
    setFont(ac.getFont());
  }

  public AnnotationSettings(){
    setFont(null);
  }

  public void setBackgroundColor(Color c){
    bgColor = c;
  }

  public void setFontColor(Color c){
    fontColor = c;
  }

  public void setFont(Font font) {
    if(font == null){
      font = Font.decode("Sans PLAIN 12");
    }
    this.font = font;
    authorFont = new Font(font.getName(),Font.BOLD,font.getSize());
    BufferedImage tmp0 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = (Graphics2D) tmp0.getGraphics();
    authorFont = authorFont.deriveFont(authorFontResolution);
    subjectFont = authorFont.deriveFont(subjectFontResolution);
    subjectFont = subjectFont.deriveFont(Font.ITALIC);   
  }

  public void setFontResolution(float fontResolution) {
      this.authorFontResolution = fontResolution;
      this.subjectFontResolution = fontResolution * 0.8f;
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

  public Font getAuthorFont(){
    return authorFont;
  }

  public Font getSubjectFont(){
    return subjectFont;
  }

  public Color getShadowColor() {
    return shadowColor;
  }

  public void setShadowColor(Color shadowColor) {
    this.shadowColor = shadowColor;
  }



  
}
