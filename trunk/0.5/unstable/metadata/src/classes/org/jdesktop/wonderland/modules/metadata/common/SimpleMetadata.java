/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.common;

/**
 *
 * @author Matt
 */
public class SimpleMetadata extends Metadata{
    public SimpleMetadata(String author, String creationDate){
        super(author, creationDate);
        put("Text", new MetadataValue(""));
    }
    
    @Override
    public String simpleName(){
        return "Simple Metadata";
    }

}
