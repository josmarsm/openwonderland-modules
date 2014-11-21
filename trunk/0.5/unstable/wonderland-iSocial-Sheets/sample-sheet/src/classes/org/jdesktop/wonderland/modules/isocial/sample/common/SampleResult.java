/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.isocial.sample.common;

import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.isocial.common.model.ResultDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.SheetDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * Sample result corresponding to SampleSheet
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@ISocialModel
@XmlRootElement(name="sample-result")
public class SampleResult extends ResultDetails {
    private String answer;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public List<String> getResultValues(List<String> list, SheetDetails sd) {
        return Collections.singletonList(getAnswer());
    }
}
