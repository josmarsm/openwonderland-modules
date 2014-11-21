/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */

/**
 * WonderBuilders, Inc.
 *
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * WonderBuilders, Inc. designates this particular file as subject to the
 * "Classpath" exception as provided WonderBuilders, Inc. in the License file
 * that accompanied this code.
 */
package org.jdesktop.wonderland.modules.standardsheet.common;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.jdesktop.wonderland.modules.standardsheet.common.XmlMapAdapter.KeyValuePair;

/**
 * Adapt a map into an array of KeyValuePairs for XML serialization
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class XmlMapAdapter extends XmlAdapter<KeyValuePair[], Map<String, String>> {

    @Override
    public Map<String, String> unmarshal(KeyValuePair[] vt) throws Exception {
        Map<String, String> out = new LinkedHashMap<String, String>();
        for (KeyValuePair kvp : vt) {
            out.put(kvp.key, kvp.value);
        }
        return out;
    }

    @Override
    public KeyValuePair[] marshal(Map<String, String> bt) throws Exception {
        KeyValuePair[] out = new KeyValuePair[bt.size()];
        int count = 0;
        for (Map.Entry<String, String> e : bt.entrySet()) {
            out[count++] = new KeyValuePair(e.getKey(), e.getValue());
        }
        return out;
    }
    
    public static class KeyValuePair {
        @XmlElement
        public String key;
        @XmlElement
        public String value;

        public KeyValuePair() {
        }

        public KeyValuePair(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
