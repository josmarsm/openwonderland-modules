/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.client.cache;

import java.util.EventListener;

/**
 *
 * @author Matt
 */
public interface CacheEventListener extends EventListener{
  public void cacheEventOccurred(CacheEvent e);
}
