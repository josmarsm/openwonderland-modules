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
    private Semaphore lock;// = new Semaphore(0);
    public String getFunctionName() {
        return "MoveNPC";
    }

    public void setArguments(Object[] os) {
        if(os[0] instanceof NpcCell) {
            cell = (NpcCell)os[0];
            renderer = (AvatarImiJME)cell.getCellRenderer(RendererType.RENDERER_JME);
        }
         x = ((Double)os[1]).floatValue();
         y = ((Double)os[2]).floatValue();
         z = ((Double)os[3]).floatValue();
        System.out.println("X: " + x + "\n"
                          +"Y: " + y + "\n"
                          +"Z: " + z + "\n");
        lock = new Semaphore(0);

    }

    public void run() {
        move(x, y, z);

    }

    protected void move(float x, float y, float z) {
        //lock = new Semaphore(0);
        GameContext context = renderer.getAvatarCharacter().getContext();
        CharacterBehaviorManager helm = context.getBehaviorManager();
        helm.clearTasks();
        helm.setEnable(true);
        helm.addTaskToTop(new BlockingGoTo(new Vector3f(x, y, z), context));
        try {
            //System.out.println("[EZScript Debug] Available lock permits before acquire: "+ lock.availablePermits());
            lock.acquire(); //--
            //System.out.println("[EZScript Debug] Available lock permits after acquire: " +lock.availablePermits());
        } catch(InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("MoveNPC finished...");
        }

    }

    public String getDescription() {
        return "usage: MoveNPC(cell, x, y, z)\n\n"
                +"- make the specified NPC walk to x, y, z\n"
                +"- animation blocks on the executing thread.";
    }

    public String getCategory() {
        return "NPCs";
    }


    class BlockingGoTo extends GoTo {                

        public BlockingGoTo(Vector3f goalPosition, GameContext context) {
            super(goalPosition, context);
            
        }

        public BlockingGoTo(Vector3f goalPosition, Vector3f directionAtGoal, GameContext context) {
            super(goalPosition, directionAtGoal, context);
        }

        @Override
        public void update(float deltaTime) {
            super.update(deltaTime);
            if(!verify()) {
                //System.out.println("[EZScript Debug] Available lock permits before release: "+lock.availablePermits());
                lock.release(); //++
                //System.out.println("[EZScript Debug] Available lock permits after release: "+lock.availablePermits());
                //System.out.println("[EZScript] releasing lock in BlockingGoTo");
            }
        }
    }
}
