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
package org.jdesktop.wonderland.modules.isocial.tokensheet.common;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.isocial.common.model.ResultDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.SheetDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * Token result corresponding to TokenSheet
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 * @author Ryan Babiuch
 * @author Kaustubh
 */
@ISocialModel
@XmlRootElement(name = "token-result")
public class TokenResult extends ResultDetails {

    //@XmlTransient
    private Student studentResult;
    
    private ResultType type;

    public TokenResult() {
        studentResult = new Student();
    }

    public void setStudentResult(Student studentResult) {
        this.studentResult = studentResult;
    }

    //@XmlElement
    public Student getStudentResult() {
        return studentResult;
    }

    public ResultType getType() {
        return type;
    }

    public void setType(ResultType type) {
        this.type = type;
    }

    @Override
    public List<String> getResultValues(List<String> list, SheetDetails sd) {
        List<String> values = new ArrayList<String>();
        values.add(String.valueOf(studentResult.getTokensValue()));
        values.add(String.valueOf(studentResult.getPassesValue()));
        values.add(String.valueOf(studentResult.getStrikesValue()));
        return values;
    }
}
