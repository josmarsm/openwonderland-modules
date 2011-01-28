/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.genericnpc.client.ezscripting;

import com.jme.math.Vector3f;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.behavior.CharacterBehaviorManager;
import imi.character.behavior.GoTo;
import imi.character.statemachine.GameContext;
import java.util.concurrent.Semaphore;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.genericnpc.client.cell.NpcCell;
import org.jdesktop.wonderland.modules.genericnpc.client.cell.NpcControls;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class MoveNpcToMethod implements ScriptMethodSPI {

    private NpcCell cell;
    private AvatarImiJME renderer;
    float x, y, z;
    private Semaphore lock;
    public String getFunctionName() {
        return "MoveNPC";
    }

    public void setArguments(Object[] os) {
        if(os[0] instanceof NpcCell) {
            cell = (NpcCell)os[0];
            renderer = (AvatarImiJME)cell.getCellRenderer(RendererType.RENDERER_JME);
        }
        float x = ((Double)os[1]).floatValue();
        float y = ((Double)os[2]).floatValue();
        float z = ((Double)os[3]).floatValue();
    }

    public void run() {
        move(x, y, z);

    }


    protected void move(float x, float y, float z) {
        GameContext context = renderer.getAvatarCharacter().getContext();
        CharacterBehaviorManager helm = context.getBehaviorManager();
        helm.clearTasks();
        helm.setEnable(true);
        helm.addTaskToTop(new BlockingGoTo(new Vector3f(x, y, z), context));
        try {
            lock.acquire();
        } catch(InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("MoveNPC finished...");
        }

    }


    class BlockingGoTo extends GoTo {
        private boolean bDone = false;

        

        public BlockingGoTo(Vector3f goalPosition, GameContext context) {
            super(goalPosition, context);
            lock = new Semaphore(0);
        }

        public BlockingGoTo(Vector3f goalPosition, Vector3f directionAtGoal, GameContext context) {
            super(goalPosition, directionAtGoal, context);
        }

        @Override
        public void update(float deltaTime) {
            super.update(deltaTime);
            if(bDone) {
                lock.release();
            }
        }
    }
}
