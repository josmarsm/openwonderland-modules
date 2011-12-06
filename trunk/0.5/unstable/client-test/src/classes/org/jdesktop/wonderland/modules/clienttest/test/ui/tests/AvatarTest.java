/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
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

package org.jdesktop.wonderland.modules.clienttest.test.ui.tests;

import com.jme.math.Vector3f;
import imi.character.AvatarSystem;
import imi.character.Character;
import imi.character.CharacterInitializationInterface;
import imi.character.Manipulator;
import imi.character.avatar.Avatar;
import imi.repository.Repository;
import imi.scene.PMatrix;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WonderlandAvatarCache;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


/**
 * A World test application
 * 
 * @author Doug Twilleager
 */
public class AvatarTest extends BaseGraphicsTest {
    private static final Logger LOGGER =
            Logger.getLogger(ModelTest.class.getName());
    
  
    private final List<String> avatars = new ArrayList<String>();
    private final List<Avatar> loadedAvatars = new ArrayList<Avatar>();
    
    public AvatarTest() {
    }

    @Override
    public void initialize(JSONObject config) {
        super.initialize(config);
        
        if (config.containsKey("avatar")) {
            avatars.add((String) config.get("avatar"));
        } else if (config.containsKey("avatars")) {
            JSONArray jModels = (JSONArray) config.get("avatars");
            for (int i = 0; i < jModels.size(); i++) {
                avatars.add((String) jModels.get(i));
            }
        }
    }

    @Override
    protected List<Entity> doLoad() throws IOException {
        List<Entity> out = new ArrayList<Entity>();
        
        for (String avatar : avatars) {
            Avatar loaded = loadAvatar(avatar);
            
            loadedAvatars.add(loaded);
            out.add(loaded);
        }
        
        
        return out;
    }

    @Override
    protected void cleanupEntities() {
        super.cleanupEntities();
    
        for (Avatar loaded : loadedAvatars) {
            loaded.destroy();
        }
        loadedAvatars.clear();
    }
    
    protected Avatar loadAvatar(String avatarURL) 
            throws IOException 
    {
        WorldManager wm = getWorldManager();
        
        // Generate the base url for the avatar configuration. We must annotate
        // the base URL.
        URL baseURL = getAssetURL("wla://avatarbaseart/");
        File cacheDir = File.createTempFile("avatar", "tmp");
        cacheDir.delete();
        cacheDir.mkdir();

        wm.addUserData(Repository.class, new Repository(wm,
                new WonderlandAvatarCache(baseURL.toExternalForm(),
                                          cacheDir)));

        // Initialize the AvatarSystem after we set up caching
        AvatarSystem.initialize(wm);
        
        URL config = getAssetURL(avatarURL);

        CharacterInitializationInterface initializer = new CharacterInitializationInterface() {

            @Override
            public void initialize(Character character) {
                character.getSkeleton().resetAllJointsToBindPose();
            }
        };

        PMatrix mat = new PMatrix();
        mat.fromAngleAxis((float)Math.toRadians(180), Vector3f.UNIT_Y);
        mat.setTranslation(new Vector3f(0f, -.9f, 2.75f));
        Avatar avatar = new Avatar.AvatarBuilder(config, wm)
                             .baseURL(baseURL.toString())
                             .initializer(initializer)
                             .transform(mat)
                             .addEntity(false)
                             .build();
        
        Manipulator.playBodyAnimation(avatar, 1);
        wm.addEntity(avatar);
        
        return avatar;
    }
}
