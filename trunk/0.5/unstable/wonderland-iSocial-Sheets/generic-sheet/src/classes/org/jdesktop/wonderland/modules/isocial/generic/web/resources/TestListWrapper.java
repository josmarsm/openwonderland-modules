/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.isocial.generic.web.resources;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.isocial.generic.common.GenericAnswer;

/**
 *
 * @author ryan
 */
@XmlRootElement(name="test-list-wrapper")
public class TestListWrapper {

    private List<GenericAnswer> answers;

    public TestListWrapper() { }

    public void setAnswers(List<GenericAnswer> answers) {
        this.answers = answers;
    }

    public List<GenericAnswer> getAnswers() {
        return answers;
    }

    public String toString() {
        return "answers: "+answers;
    }

}
