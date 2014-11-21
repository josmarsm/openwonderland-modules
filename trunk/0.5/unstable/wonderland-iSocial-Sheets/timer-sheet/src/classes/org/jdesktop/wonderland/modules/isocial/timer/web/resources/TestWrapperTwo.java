/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.isocial.timer.web.resources;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ryan
 */
@XmlRootElement(name="TestWrapperTwo")
public class TestWrapperTwo {
    private List<TestListWrapper> wrappers;

    public TestWrapperTwo() { }

    public void setWrappers(List<TestListWrapper> wrappers) {
        this.wrappers = wrappers;
    }

    public List<TestListWrapper> getWrappers() {
        return wrappers;
    }

    @Override
    public String toString() {
        return "wrappers: "+wrappers;
    }

}
