package org.jdesktop.wonderland.modules.scriptingComponent.client;

import org.jdesktop.wonderland.client.input.Event;

/**
 * Event on the receipt of an Intercell message
 * @author Morris Ford
 */
public class IntercellEvent extends Event {
    private String payload = null;
    private int code = 0;
    
    /** Default constructor */
    public IntercellEvent() {
    }
    
    /** Constructor, takes the payload string and the event code. */
    public IntercellEvent(String Payload, int Code) 
        {
        this.payload = Payload;
        this.code = Code;
        }

    public String getPayload()
        {
        return payload;
        }

    public int getCode()
        {
        return code;
        }
    /** 
     * {@inheritDoc}
     * <br>
     * If event is null, a new event of this class is created and returned.
     */
    @Override
    public Event clone (Event event) 
        {
        if (event == null) 
            {
            event = new IntercellEvent();
            }
        ((IntercellEvent) event).payload = payload;
        ((IntercellEvent) event).code = code;
        return super.clone(event);
    }
}
