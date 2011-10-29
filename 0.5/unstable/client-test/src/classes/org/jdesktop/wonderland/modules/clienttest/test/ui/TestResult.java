/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.clienttest.test.ui;

/**
 *
 * @author jkaplan
 */
public enum TestResult {
    NOT_RUN     ("bullet_grey.png"), 
    IN_PROGRESS ("bullet_grey-animated.gif"), 
    PASS        ("bullet_green.png"), 
    WARNING     ("bullet_yellow.png"),
    FAIL        ("bullet_red.png");
    
    private final String icon;
    
    TestResult(String icon) {
        this.icon = icon;
    }
    
    public String getIcon() {
        return icon;
    }
}
