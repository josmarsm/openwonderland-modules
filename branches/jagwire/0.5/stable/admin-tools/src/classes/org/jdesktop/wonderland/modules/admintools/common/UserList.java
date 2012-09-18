/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.admintools.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB-serializable list of users
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@XmlRootElement(name="user-list")
public class UserList implements Serializable {
    private List<User> users = new ArrayList<User>();
    
    public UserList() {
    }
    
    @XmlElement
    public List<User> getUsers() {
        return users;
    }
    
    public void setUsers(List<User> users) {
        this.users = users;
    }

    @XmlRootElement(name="user")
    public static class User implements Serializable {
        public String name;
        public String id;
        public Date when;

        public User() {
        }

        public User(String id, String name, Date when) {
            this.name = name;
            this.id = id;
            this.when = when;
        }
    }
}
