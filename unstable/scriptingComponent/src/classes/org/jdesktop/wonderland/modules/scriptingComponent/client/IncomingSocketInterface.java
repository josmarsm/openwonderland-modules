package org.jdesktop.wonderland.modules.scriptingComponent.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jdesktop.wonderland.client.ClientContext;

public class IncomingSocketInterface
    {   
    private             int port = 0;
    private             int code = 0;
    private             int errorCode = 0;
    
    List socketList = Collections.synchronizedList(new ArrayList());
    
    public IncomingSocketInterface(int port, int code, int errorCode)
        {
        this.port = port;
        this.code = code;
        this.errorCode = errorCode;
        }
    
    public void doIt()
        {
        new myThread().start(); 
        }
    
    public void sendSocketMessage(String message)
        {
        Socket socket;
        Iterator it = socketList.iterator();
        
        while(it.hasNext())
            {
            socket = (Socket)it.next();
            try
                {
                DataOutputStream dataout = new DataOutputStream(socket.getOutputStream());
                String size = String.format("%05d", message.length() + 5);
                System.out.println("IncomingSocketInterface - Sending message = " + message + " Size = " + size);
                dataout.writeBytes(size);
                dataout.writeBytes(message);
                dataout.flush();
                }
            catch(Exception ex)
                {
                System.out.println("CommCell - Exception sending message" + ex);
                ex.printStackTrace();
                socketList.remove(it);
                }
            }
        }
    
    class myThread extends Thread
        {
//        int port = 9999;
        int i = 1;
        ServerSocket server_socket = null;
        Socket socket = null;
        boolean keepRunning = false;
        
@Override
        public void run()
            {
            try
                {
                server_socket = new ServerSocket(port);
                port = server_socket.getLocalPort();
                System.out.println("CommCell : Listening for connections on port " + port);
                keepRunning = true;
                }
            catch(Exception ex)
                {
                keepRunning = false;
                }
            while ( keepRunning )
                {
                try
                    {
                    socket = server_socket.accept();
                    System.out.println("CommCell : Accepted connection from " + socket.getInetAddress());
                    }
                catch(Exception exx)
                    {
                    System.out.println("CommCell : Caught exception on accept : " + exx);
                    exx.printStackTrace();
                    }
                System.out.println("CommCell : Before run " + " socket : " + socket);
                try
                    {
                    socket.setSoTimeout(1000*30);

                    System.out.println("CommCell received a connect");
                    new ThreadedWLHandler( socket, i ).start();
                    }
                catch(Exception exx)
                    {
                    System.out.println("CommCell : Caught exception starting thread : " + exx);
                    exx.printStackTrace();
                    }
                i++;
                }

            }
        
        }
    
    class ThreadedWLHandler extends Thread
        {
        Socket              socket;
        int                 counter;
        OutputStream        os;
        InputStream         is;
        DataInputStream     dis;
        DataOutputStream    dataout;
        byte[]              size = new byte[5];
        boolean keepRunning = false;
        int                 avail = 0;
        
    // constructor
        ThreadedWLHandler( Socket i, int c ) throws ClassNotFoundException
            {
            socket = i;
            counter = c;
            socketList.add(socket);
            System.out.println(counter + "I Control - Enter ThreadedWLHandler");
            }

        @Override
        public void run()
            {
            try
                {
                socket.setSoTimeout(1000*300);

                os = socket.getOutputStream();
                is = socket.getInputStream();
                dis = new DataInputStream(is);
                dataout = new DataOutputStream(os);
                }
            catch(Exception ex)
                {
                System.out.println("Control - Exception initializing socket and streams - " + ex);
                }
            keepRunning = true;
            while(keepRunning)
                {
                try
                    {
                    dis.readFully(size);
                    String s = new String(size);
                    Integer sizeInt = Integer.valueOf(s).intValue();
                    System.out.println("->" + counter + " T : Control - Read size of transaction = " + s + " int = " + sizeInt);
                    if(sizeInt > 0)
                        {
                        byte[] buffer = new byte[sizeInt - 5];
                        dis.readFully(buffer, 0, sizeInt - 5);

                        String temp = new String(buffer);
                        System.out.println("->" + counter + " T : Control - Finished read - transaction ->" + temp);
                        
                        System.out.println("->: Finished read - transaction ->" + size + " : " + temp);
                        ClientContext.getInputManager().postEvent(new IntercellEvent(temp, code));
                        }
                    else
                        {
                        sleep(2000);
                        avail = is.available();
                        System.out.println("Skipping " + avail + " bytes");
                        if(avail > 0)
                            dis.skipBytes(avail);
                        }
//                    os.write(size);
//                    os.write(buffer);
//                    os.flush();
                    }
                catch ( EOFException eof )
                    {
                    System.out.println("->" + counter + " T : Control EOFException while handling receipt of : " + eof);
                    keepRunning = false;
                    }
                catch ( SocketTimeoutException ste )
                    {
                    System.out.println("->" + counter + " T : Control SocketTimeoutException waiting for input : " + ste);
                    }
                catch ( SocketException se )
                    {
                    System.out.println("->" + counter + " T : Control SocketException waiting for input : " + se);
                    keepRunning = false;
                    }
                catch ( IOException ioe )
                    {
                    System.out.println("->" + counter + " T : Control IOException waiting for input : " + ioe);
                    keepRunning = false;
                    }
                catch ( NumberFormatException nfe )
                    {
                    System.out.println("->" + counter + " T : Control NumberFormatException waiting for input : " + nfe);
                    }
                catch ( InterruptedException ie )
                    {
                    System.out.println("->" + counter + " T : Control InterruptedException waiting for sleep : " + ie);
                    }
                if(keepRunning)
                    {
                    try
                        {
                        System.out.println("Control - Into after exception processing");
                        sleep(2000);
                        avail = is.available();
                        System.out.println("Skipping " + avail + " bytes");
                         if(avail > 0)
                            dis.skipBytes(avail);
                        }
                    catch(InterruptedException ie)
                        {
                        System.out.println("-> " + counter + " T : Control - InterruptedException waiting for sleep in recovery : " + ie);
                        keepRunning = false;
                        }
                    catch(IOException ioe)
                        {
                        System.out.println("-> " + counter + " T : Control - IOException waiting in recovery : " + ioe);
                        keepRunning = false;
                        }
                    }
                }
            socketList.remove(socket);
            }
        }
    }

