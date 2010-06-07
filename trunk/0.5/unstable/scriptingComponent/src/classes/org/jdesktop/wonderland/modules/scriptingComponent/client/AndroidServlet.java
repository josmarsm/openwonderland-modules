/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.scriptingComponent.client;

import imi.character.avatar.AvatarContext.TriggerNames;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;

/**
 *
 * @author morrisford
 */

    public class AndroidServlet extends HttpServlet
        {
        public static final int ANDROID_SERVLET_PASS_DATA = 0;
        public static final int ANDROID_SERVLET_ACT_SCRIPT = 1;
        public static final int ANDROID_SERVLET_ACT_DIRECTLY = 77;

        private int androidCode = 0;
        private int androidErrorCode = 0;
        private int androidMode = 0;

        private WlAvatarCharacter myAvatar = null;

        public AndroidServlet(int Code, int ErrorCode, int Mode)
            {
            androidCode = Code;
            androidErrorCode = ErrorCode;
            androidMode = Mode;
            }

    @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
            {
            doGet(request, response);
            }

    @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
            {
            response.setContentType("text/plain");
            PrintWriter out = response.getWriter();

            out.println(request.getParameter("command"));
            String command = request.getParameter("command");
            switch(this.androidMode)
                {
                case ANDROID_SERVLET_ACT_SCRIPT:
                    {
                    if(command.equals("forward"))
                        ClientContext.getInputManager().postEvent(new IntercellEvent("F", androidCode));
                    if(command.equals("back"))
                        ClientContext.getInputManager().postEvent(new IntercellEvent("B", androidCode));
                    if(command.equals("right"))
                        ClientContext.getInputManager().postEvent(new IntercellEvent("R", androidCode));
                    if(command.equals("left"))
                        ClientContext.getInputManager().postEvent(new IntercellEvent("L", androidCode));
                    break;
                    }
                case ANDROID_SERVLET_ACT_DIRECTLY:
                    {
                    System.out.println("get - " + command);
                    if(command.equals("forward"))
                        {
                        getMyAvatar();
                        moveAvatarForward();
                        mySleep(500);
                        stopAvatarForward();
                        }
                    if(command.equals("back"))
                        {
                        getMyAvatar();
                        moveAvatarBack();
                        mySleep(500);
                        stopAvatarBack();
                        }
                    if(command.equals("right"))
                        {
                        getMyAvatar();
                        moveAvatarRight();
                        mySleep(500);
                        stopAvatarRight();
                        }
                    if(command.equals("left"))
                        {
                        getMyAvatar();
                        moveAvatarLeft();
                        mySleep(500);
                        stopAvatarLeft();
                        }
                    break;
                    }
                case ANDROID_SERVLET_PASS_DATA:
                    {
                    ClientContext.getInputManager().postEvent(new IntercellEvent(command, androidCode));
                    break;
                    }
                }
            out.flush();
            }
    
        private void getMyAvatar()
            {
            Cell avatarCell = ClientContextJME.getViewManager().getPrimaryViewCell();
            CellRenderer rend = avatarCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
            myAvatar = ((AvatarImiJME)rend).getAvatarCharacter();
            }

        private void moveAvatarForward()
            {
            if(myAvatar != null)
                {
                myAvatar.triggerActionStart(TriggerNames.Move_Forward);
                }
            }

        private void stopAvatarForward()
            {
            if(myAvatar != null)
                {
                myAvatar.triggerActionStop(TriggerNames.Move_Forward);
                }
            }

        private void moveAvatarBack()
            {
            if(myAvatar != null)
                {
                myAvatar.triggerActionStart(TriggerNames.Move_Back);
                }
            }

        private void stopAvatarBack()
            {
            if(myAvatar != null)
                {
                myAvatar.triggerActionStop(TriggerNames.Move_Back);
                }
            }

        private void moveAvatarRight()
            {
            if(myAvatar != null)
                {
                myAvatar.triggerActionStart(TriggerNames.Move_Right);
                }
            }

        private void stopAvatarRight()
            {
            if(myAvatar != null)
                {
                myAvatar.triggerActionStop(TriggerNames.Move_Right);
                }
            }

        private void moveAvatarLeft()
            {
            if(myAvatar != null)
                {
                myAvatar.triggerActionStart(TriggerNames.Move_Left);
                }
            }

        private void stopAvatarLeft()
            {
            if(myAvatar != null)
                {
                myAvatar.triggerActionStop(TriggerNames.Move_Left);
                }
            }

        private void mySleep(int milliseconds)
            {
            try
                {
                Thread.sleep(milliseconds);
                }
            catch(Exception e)
                {
                System.out.println("ScriptingComponent : AndroidServlet : Sleep exception in mySleep(int) method");
                }
            }
        }


