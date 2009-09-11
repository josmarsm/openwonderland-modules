/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.rockwellcollins.clickablelink.client;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.rockwellcollins.clickablelink.common.ClickableLinkComponentClientState;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

/**
 * ClickableLinkComponents allow you to set a URL and open them with a single
 * click from inside the world
 * 
 * @author Ben (shavnir)
 * 
 */
public class ClickableLinkComponent extends CellComponent {
	/** The listener for the mouse clicks */
	private MouseEventListener listener = null;

	/** The URL to travel to */
	private String url;

	/** The constructor */
	public ClickableLinkComponent(Cell cell) {
		super(cell);

	}

	/**
	 * This is a bit hackish but we wanted the functionality that certain URL
	 * patterns could result in certain browsers being opened.  Currently any exceptions
	 * must be hardcoded but these will be configurable later.
	 */
	private Map<String, String> exceptionList;

	/**
	 * Sets the local properties from a given ClientState.
	 */
	@Override
	public void setClientState(CellComponentClientState clientState) {
		super.setClientState(clientState);
		if (clientState instanceof ClickableLinkComponentClientState) {
			this.url = ((ClickableLinkComponentClientState) clientState)
					.getLinkURL();
		} else {
			Logger logger = Logger.getLogger(ClickableLinkComponent.class
					.getName());
			logger
					.severe("ClickableLinkComponent got the wrong type of ClientState."
							+ clientState.getClass().getName());
			url = "http://www.google.com";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setStatus(CellStatus status, boolean increasing) {

		super.setStatus(status, increasing);

		switch (status) {
		case DISK:
			//Cleanup aisle 3
			if (listener != null) {
				CellRendererJME renderer = (CellRendererJME) cell
						.getCellRenderer(RendererType.RENDERER_JME);
				Entity entity = renderer.getEntity();
				listener.removeFromEntity(entity);
				listener = null;
				url = null;
				exceptionList = null;
			}
			break;

		case RENDERING:
			if (listener == null) {
				//Build the exception list
				exceptionList = new HashMap<String, String>();

				exceptionList.put("ieOnlySite.com", "iexplore");
				
				try {
					//Attach a click listener
					CellRendererJME renderer = (CellRendererJME) cell
							.getCellRenderer(RendererType.RENDERER_JME);
					Entity entity = renderer.getEntity();
					listener = new MouseEventListener();
					listener.addToEntity(entity);
				} catch (NullPointerException npe) {
					// do be doo~
				}
			}
			break;

		default:
			break;

		}
	}

	/**
	 * This is where a lot of cool stuff happens, the event listener dealing
	 * with mouse clicks
	 * @author bmjohnst
	 *
	 */
	class MouseEventListener extends EventClassListener {
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class[] eventClassesToConsume() {
			return new Class[] { MouseButtonEvent3D.class };
		}

		/**
		 * This method is where the click turns into an opened browser.  
		 * Incredible!
		 */
		@Override
		public void commitEvent(Event event) {
			MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
			//Make sure its a click!
			if (mbe.isClicked() == false || mbe.getButton() != ButtonId.BUTTON1) {
				return;
			}

			BrowserLauncher bl;
			try {
				//Slight fix because a lot of people like to put "www.google.com" instead of "http://www.google.com"
				if (!url.startsWith("http://")) {
					url = "http://" + url;
				}
				//Initialize the launcher
				bl = new BrowserLauncher();
				
				//This code checks against the map and looks for matches.  It does a simple .contains so it could
				//be fooled so be a bit careful with what you set your strings to.  If it doesn't match any key
				// in the exception list it will open with the default browser
				boolean patternMatch = false;
				for (String pattern : exceptionList.keySet()) {
					if (url.toLowerCase().contains(pattern) && !patternMatch) {
						patternMatch = true;
						bl.openURLinBrowser(exceptionList.get(pattern), url);
					}
				}
				if (!patternMatch) {
					bl.openURLinBrowser(url);
				}

			} catch (BrowserLaunchingInitializingException e) {
				e.printStackTrace();
			} catch (UnsupportedOperatingSystemException e) {
				e.printStackTrace();
			}

		}

	}

}
