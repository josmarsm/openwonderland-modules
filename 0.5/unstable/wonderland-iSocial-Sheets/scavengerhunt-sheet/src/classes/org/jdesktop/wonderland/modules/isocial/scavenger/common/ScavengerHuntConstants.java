/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.common;

/**
 * Defines constant values used throughout the module.
 *
 * @author Vladimir Djurovic
 */
public class ScavengerHuntConstants {
    /** Path to resource bundle with component configuration data. */
    public static final String BUNDLE_PATH = "org/jdesktop/wonderland/modules/isocial/scavenger/client/resources/Bundle";
    public static final String STRINGS_PATH = "org/jdesktop/wonderland/modules/isocial/scavenger/client/resources/strings";
    public static final String HINT_1_ICON_PATH = "/org/jdesktop/wonderland/modules/isocial/scavenger/client/resources/icons/question-white.png";
    public static final String HINT_2_ICON_PATH = "/org/jdesktop/wonderland/modules/isocial/scavenger/client/resources/icons/question-blue.png";
    public static final String HINT_3_ICON_PATH = "/org/jdesktop/wonderland/modules/isocial/scavenger/client/resources/icons/question-red.png";
    public static final String USER_ICON_PATH = "/org/jdesktop/wonderland/modules/isocial/scavenger/client/resources/icons/user.png";
    public static final String ORDER_ICON_PATH = "/org/jdesktop/wonderland/modules/isocial/scavenger/client/resources/icons/Order.png";
    public static final String ARROW_UP_ICON_PATH = "/org/jdesktop/wonderland/modules/isocial/scavenger/client/resources/icons/arrow_up.png";
    public static final String ARROW_DOWN_ICON_PATH = "/org/jdesktop/wonderland/modules/isocial/scavenger/client/resources/icons/arrow_down.png";
    
    /** Property denoting display name of Scavenger Hunt component. */
    public static final String PROP_SCAVENGER_HUNT_NAME = "Scavenger_Hunt_Component";
    
    /** Property denoting SCavenger Hunt component description. */
    public static final String PROP_SCAVENGER_HUNT_DESC = "Scavenger_Hunt_Component_Description";
    
    /** Property denoting name of Question component */
    public static final String PROP_QUESTION_NAME = "Question_Component";
    
    /** Property denoting description of Question component. */ 
    public static final String PROP_QUESTION_DESC = "Question_Component_Description";
    
    /** Key for storing ordered list of items in shared state. */
    public static final String ORDER_LIST_NAME = "ItemOrderList";
    
    public static final String FIND_METHOD_KEY_NAME = "Global_Find_Method";
    
     /** Interval for polling for shared map creation (in ms) */
    public static final int MAP_POLL_INTERVAL = 2000;
    
    public static final String KEY_SNAPSHOT_WARN = "WorldSheetManager.snapshot.warning";
    public static final String KEY_SNAPSHOT_WARN_TITLE = "WorldSheetManager.snapshot.warning.title";
    public static final String KEY_SNAPSHOT_ERROR = "WorldSheetManager.snapshot.update.error";
    public static final String KEY_SHEET_ERROR = "WorldSheetManager.sheet.update.error";
    
    // Question component related
    /** Specifies custom configured question. */
    public static final int QUESTION_SRC_CUSTOM = 0;
    
    /** Specifies that question is configured in sheet. */
    public static final int QUESTION_SRC_SHEET = 1;
    
    /** Horizontal offset of answer dialog relative to left edge of main window. */
    public static final int DLG_OFFSET_X = 20;
    
    /** Vertical offset of answer dialog relative to top edge of main window. */
    public static final int DLG_OFFSET_Y = 100;
}
