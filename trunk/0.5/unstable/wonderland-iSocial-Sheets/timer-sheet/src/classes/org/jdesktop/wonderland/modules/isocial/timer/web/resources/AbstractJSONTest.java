/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.isocial.timer.web.resources;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ryan
 */
@XmlRootElement(name="AbstractJSONTest")
public abstract class AbstractJSONTest {

    private String label;

    public AbstractJSONTest() {
        
    }

    public String getLabel() {
        return label;
    }

    public abstract double getPI();

}
