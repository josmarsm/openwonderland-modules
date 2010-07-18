package org.jdesktop.wonderland.modules.scriptingComponent.client;

import java.io.*;
import java.net.*;
import org.jdesktop.wonderland.client.ClientContext;

class SocketInterface
    {
    private             String ip = new String();
    private             int port = 0;
    private             int code = 0;
    private             int errorCode = 0;
    OutputStream        os;
    DataInputStream     is;
    DataOutputStream	dataout;
    Socket              theSocket;

    public SocketInterface(String ip, int port, int code, int errorCode)
        {
        this.port = port;
        this.ip = ip;
        this.code = code;
        this.errorCode = errorCode;
        }

    public void quit()
        {
        try
            {
            os.close();
            is.close();
            theSocket.close();
            theSocket = null;
            os = null;
            is = null;
            }
      	catch ( Exception e )
      	    {
            System.out.println("SocketInterface : in doIt : Error creating socket" + e );
            e.printStackTrace();
      	    }
        }

    public void doIt() 
    	{
        theSocket = null;
      	try 
      	    {  
            theSocket = new Socket(ip, port);
            theSocket.setSoTimeout(1000*30);
            os = theSocket.getOutputStream();
            is = new DataInputStream(theSocket.getInputStream());
            dataout = new DataOutputStream(os);
            new SocketInterfaceHandler(theSocket, code, errorCode).start();
      	    }
      	catch ( Exception e )
      	    {  
            System.out.println("SocketInterface : in doIt : Error creating socket" + e );
            e.printStackTrace();
      	    }
        }
    
    public void sendBuffer(String buffer)
        {
        try
            {
            String requestSize = String.format("%05d", buffer.length() + 5);
            dataout.writeBytes(requestSize);
            dataout.writeBytes(buffer);
            dataout.flush();
            }
        catch(Exception ex)
            {
            System.out.println("CommCell - Exception sending message" + ex);
            ex.printStackTrace();
            }
        }
    }

class SocketInterfaceHandler extends Thread
    {  
    Socket              socket;
    OutputStream        os;
    DataInputStream     is;
    DataOutputStream	dataout;
    int                 code = 0;
    int                 errorCode = 0;
    private             int avail;
    
    // constructor 
    SocketInterfaceHandler(Socket i, int code, int errorCode) throws ClassNotFoundException
        { 
        this.socket = i;
        this.code = code;
        this.errorCode = errorCode;
        System.out.println("Enter SocketInterfaceHandler constructor");
        }

    @Override
    public void run()
        {
        String	the_size = null;
        boolean more = true;
        byte[]		size = new byte[5];
        try
            {
            os = socket.getOutputStream();
            is = new DataInputStream(socket.getInputStream());
            dataout = new DataOutputStream(os);
            }
        catch(Exception e)
            {
            System.out.println("Excpetion in making streams in run" + e);
            e.printStackTrace();
            }
        while(more)
            {
            try
                {
                is.readFully(size);
                the_size = new String(size);
                System.out.println("-> : Read size of transaction in string = " + the_size);

                byte[] buffer = new byte[Integer.valueOf(the_size).intValue() - 5];
                is.readFully(buffer, 0, Integer.valueOf(the_size).intValue() - 5);

                String temp = new String(buffer);
                System.out.println("->: Finished read - transaction ->" + size + " : " + temp);
                ClientContext.getInputManager().postEvent(new IntercellEvent(temp, code));
                }
            catch ( EOFException eof )
                {
                System.out.println("-> Control EOFException while handling receipt of : " + eof);
                more = false;
                }
            catch ( SocketTimeoutException ste )
                {
                System.out.println("-> Control SocketTimeoutException waiting for input : " + ste);
                }
            catch ( SocketException se )
                {
                System.out.println("-> Control SocketException waiting for input : " + se);
                more = false;
                }
            catch ( IOException ioe )
                {
                System.out.println("-> Control IOException waiting for input : " + ioe);
                more = false;
                }
            catch ( NumberFormatException nfe )
                {
                System.out.println("-> Control NumberFormatException waiting for input : " + nfe);
                }
            if(more)
                {
                try
                    {
                    System.out.println("Control - Into after exception processing");
                    sleep(2000);
                    avail = is.available();
                    System.out.println("Skipping " + avail + " bytes");
                    if(avail > 0)
                        is.skipBytes(avail);
                    }
                catch(InterruptedException ie)
                    {
                    System.out.println("-> InterruptedException waiting for sleep in recovery : " + ie);
                    more = false;
                    }
                catch(IOException ioe)
                    {
                    System.out.println("-> Control - IOException waiting in recovery : " + ioe);
                    more = false;
                    }
                }
            else
                {
                ClientContext.getInputManager().postEvent(new IntercellEvent("Socket interface has failed", errorCode));
                System.out.println("-> Control - Socket interfcae has failed : ");
                }
            }
        return;
        }
    }

