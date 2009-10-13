/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ClientMonitorFrame.java
 *
 * Created on May 7, 2009, 5:05:50 PM
 */

package org.jdesktop.wonderland.modules.clientmonitor.client;

import com.jme.system.DisplaySystem;
import com.jme.util.geom.BufferUtils;
import com.jme.util.geom.BufferUtils.BufferStats;
import com.jmex.awt.jogl.JOGLAWTCanvas;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import net.quies.math.plot.ChartStyle;
import net.quies.math.plot.ChartType;
import net.quies.math.plot.Function;
import net.quies.math.plot.Graph;
import net.quies.math.plot.IndexScheme;
import net.quies.math.plot.InteractiveGraph;
import net.quies.math.plot.LinearTimeIndexScheme;
import net.quies.math.plot.XAxis;
import net.quies.math.plot.YAxis;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;

/**
 *
 * @author jkaplan
 */
public class ClientMonitorFrame extends javax.swing.JFrame {
    private Timer timer = new Timer();

    private int tick = 0;
    private Graph systemMemoryGraph;
    private Function totalSystemMemory;
    private Function availSystemMemory;
    private Function maxSystemMemory;

    private Graph directBuffersGraph;
    private Function totalBuffers;
    private Function doubleBuffers;
    private Function floatBuffers;
    private Function intBuffers;
    private Function shortBuffers;
    private Function byteBuffers;

    private Graph directBuffersMemoryGraph;
    private Function totalBuffersMemory;
    private Function doubleBuffersMemory;
    private Function floatBuffersMemory;
    private Function intBuffersMemory;
    private Function shortBuffersMemory;
    private Function byteBuffersMemory;

    /** Creates new form ClientMonitorFrame */
    public ClientMonitorFrame() {
        initComponents();
        initGeneralTab();
        initMemoryTab();
    }

    @Override
    public void dispose() {
        super.dispose();

        timer.cancel();
    }

    private void initMemoryTab() {
        ChartStyle blueStyle = new ChartStyle();
        blueStyle.setPaint(Color.BLUE);
        ChartStyle greenStyle = new ChartStyle();
        greenStyle.setPaint(Color.GREEN);
        ChartStyle redStyle = new ChartStyle();
        redStyle.setPaint(Color.RED);
        ChartStyle yellowStyle = new ChartStyle();
        yellowStyle.setPaint(Color.YELLOW);
        ChartStyle magentaStyle = new ChartStyle();
        magentaStyle.setPaint(Color.MAGENTA);
        ChartStyle cyanStyle = new ChartStyle();
        cyanStyle.setPaint(Color.CYAN);

        XAxis timeX = new XAxis("Time (seconds)");
        NumberFormat memoryYFormat = new DecimalFormat("#'MB'");
        YAxis memoryY = new YAxis("Memory", new LinearTimeIndexScheme(), memoryYFormat);
        systemMemoryGraph = new InteractiveGraph(timeX, memoryY);

        totalSystemMemory = new Function("Total memory");
        systemMemoryGraph.addFunction(totalSystemMemory, blueStyle);

        availSystemMemory = new Function("Available memory");
        systemMemoryGraph.addFunction(availSystemMemory, greenStyle);

        maxSystemMemory = new Function("Maximum memory");
        systemMemoryGraph.addFunction(maxSystemMemory, redStyle);
        systemMemoryPanel.add(systemMemoryGraph, BorderLayout.CENTER);

        NumberFormat buffersYFormat = NumberFormat.getIntegerInstance();
        YAxis buffersY = new YAxis("Buffers", new LinearTimeIndexScheme(), buffersYFormat);
        directBuffersGraph = new InteractiveGraph(timeX, buffersY);

        totalBuffers = new Function("Total Buffers");
        directBuffersGraph.addFunction(totalBuffers, redStyle);

        doubleBuffers = new Function("Double Buffers");
        directBuffersGraph.addFunction(doubleBuffers, blueStyle);

        floatBuffers = new Function("Float Buffers");
        directBuffersGraph.addFunction(floatBuffers, greenStyle);

        intBuffers = new Function("Int Buffers");
        directBuffersGraph.addFunction(intBuffers, yellowStyle);

        shortBuffers = new Function("Short Buffers");
        directBuffersGraph.addFunction(shortBuffers, magentaStyle);

        byteBuffers = new Function("Byte Buffers");
        directBuffersGraph.addFunction(byteBuffers, cyanStyle);
        directMemoryPanel.add(directBuffersGraph, BorderLayout.CENTER);

        directBuffersMemoryGraph = new InteractiveGraph(timeX, memoryY);

        totalBuffersMemory = new Function("Total Buffers");
        directBuffersMemoryGraph.addFunction(totalBuffersMemory, redStyle);

        doubleBuffersMemory = new Function("Double Buffers");
        directBuffersMemoryGraph.addFunction(doubleBuffersMemory, blueStyle);

        floatBuffersMemory = new Function("Float Buffers");
        directBuffersMemoryGraph.addFunction(floatBuffersMemory, greenStyle);

        intBuffersMemory = new Function("Int Buffers");
        directBuffersMemoryGraph.addFunction(intBuffersMemory, yellowStyle);

        shortBuffersMemory = new Function("Short Buffers");
        directBuffersMemoryGraph.addFunction(shortBuffersMemory, magentaStyle);

        byteBuffersMemory = new Function("Byte Buffers");
        directBuffersMemoryGraph.addFunction(byteBuffersMemory, cyanStyle);
        directBufferMemoryPanel.add(directBuffersMemoryGraph, BorderLayout.CENTER);

        timer.schedule(new GetMemoryTask(), 0, 1000);
    }

    private void initGeneralTab() {
        DisplaySystem ds = DisplaySystem.getDisplaySystem("JOGL");
        displayAdpaterLabel.setText(ds.getAdapter());
        displayVendorLabel.setText(ds.getDisplayVendor());
        displayVersionLabel.setText(ds.getDriverVersion());
        displayRendererLabel.setText(ds.getDisplayRenderer());
        displayAPIVersionLabel.setText(ds.getDisplayAPIVersion());
        
        timer.schedule(new GetWorldInfoTask(), 0, 5000);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        generalPanel = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        displayAPIVersionLabel = new javax.swing.JLabel();
        displayRendererLabel = new javax.swing.JLabel();
        displayVersionLabel = new javax.swing.JLabel();
        displayVendorLabel = new javax.swing.JLabel();
        displayAdpaterLabel = new javax.swing.JLabel();
        jPanel17 = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        worldSessionLabel = new javax.swing.JLabel();
        worldRootCellsLabel = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        worldTotalCellsLabel = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();
        memoryPanel = new javax.swing.JPanel();
        forceGCButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        systemMemoryPanel = new javax.swing.JPanel();
        directMemoryPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        directBufferMemoryPanel = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jmeBiffersPanel = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        totalBufferSizeTF = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        totalBufferCountTF = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        totalIntBufferCountTF = new javax.swing.JTextField();
        totalByteBufferCountTF = new javax.swing.JTextField();
        totalShortBufferCountTF = new javax.swing.JTextField();
        totalFloatBufferCountTF = new javax.swing.JTextField();
        totalDoubleBufferCountTF = new javax.swing.JTextField();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        intBufferSizeTF = new javax.swing.JTextField();
        byteBufferSizeTF = new javax.swing.JTextField();
        shortBufferSizeTF = new javax.swing.JTextField();
        floatBufferSizeTF = new javax.swing.JTextField();
        doubleBufferSizeTF = new javax.swing.JTextField();
        forceGCButton1 = new javax.swing.JButton();

        setTitle("Client Monitor");

        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder("System Information"));

        jLabel19.setText("Display Adapter:");

        jLabel20.setText("Display Vendor:");

        jLabel21.setText("Display Version:");

        jLabel22.setText("Display Renderer:");

        jLabel23.setText("Display API Version:");

        displayAPIVersionLabel.setText("api version");

        displayRendererLabel.setText("renderer");

        displayVersionLabel.setText("version");

        displayVendorLabel.setText("vendor");

        displayAdpaterLabel.setText("adapter");

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel23)
                    .addComponent(jLabel22)
                    .addComponent(jLabel21)
                    .addComponent(jLabel20)
                    .addComponent(jLabel19))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(displayRendererLabel)
                    .addComponent(displayAPIVersionLabel)
                    .addComponent(displayAdpaterLabel)
                    .addComponent(displayVendorLabel)
                    .addComponent(displayVersionLabel))
                .addContainerGap(718, Short.MAX_VALUE))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(displayAdpaterLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(displayVendorLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(displayVersionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(displayRendererLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(displayAPIVersionLabel))
                .addContainerGap(40, Short.MAX_VALUE))
        );

        jPanel17.setBorder(javax.swing.BorderFactory.createTitledBorder("World Information"));

        jLabel24.setText("Sessions:");

        jLabel25.setText("Root Cells:");

        worldSessionLabel.setText("sessions");

        worldRootCellsLabel.setText("root cells");

        jLabel26.setText("Total Cells:");

        worldTotalCellsLabel.setText("total cells");

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel24)
                    .addComponent(jLabel25)
                    .addComponent(jLabel26))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(worldTotalCellsLabel)
                    .addComponent(worldRootCellsLabel)
                    .addComponent(worldSessionLabel))
                .addContainerGap(768, Short.MAX_VALUE))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(worldSessionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(worldRootCellsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(worldTotalCellsLabel))
                .addContainerGap(151, Short.MAX_VALUE))
        );

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout generalPanelLayout = new javax.swing.GroupLayout(generalPanel);
        generalPanel.setLayout(generalPanelLayout);
        generalPanelLayout.setHorizontalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, generalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(closeButton))
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        generalPanelLayout.setVerticalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 126, Short.MAX_VALUE)
                .addComponent(closeButton)
                .addContainerGap())
        );

        jTabbedPane1.addTab("General", generalPanel);

        forceGCButton.setText("Force GC");
        forceGCButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forceGCButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("System Memory:");

        jLabel2.setText("Direct Buffers");

        systemMemoryPanel.setBackground(java.awt.Color.white);
        systemMemoryPanel.setPreferredSize(new java.awt.Dimension(800, 200));
        systemMemoryPanel.setLayout(new java.awt.BorderLayout());

        directMemoryPanel.setBackground(java.awt.Color.white);
        directMemoryPanel.setPreferredSize(new java.awt.Dimension(800, 200));
        directMemoryPanel.setLayout(new java.awt.BorderLayout());

        jLabel3.setText("Maximum");

        jPanel1.setBackground(java.awt.Color.red);
        jPanel1.setPreferredSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        jPanel2.setBackground(java.awt.Color.blue);
        jPanel2.setPreferredSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        jLabel4.setText("Total");

        jPanel3.setBackground(java.awt.Color.green);
        jPanel3.setPreferredSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        jLabel5.setText("Used");

        jLabel6.setText("Direct Buffer Memory");

        jLabel7.setText("Total");

        jPanel4.setBackground(java.awt.Color.red);
        jPanel4.setPreferredSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        jPanel5.setBackground(java.awt.Color.blue);
        jPanel5.setPreferredSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        jLabel8.setText("Double");

        jPanel6.setBackground(java.awt.Color.green);
        jPanel6.setPreferredSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        jLabel9.setText("Float");

        jLabel10.setText("Int");

        jPanel7.setBackground(java.awt.Color.yellow);
        jPanel7.setPreferredSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        jPanel8.setBackground(java.awt.Color.magenta);
        jPanel8.setPreferredSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        jLabel11.setText("Short");

        jPanel9.setBackground(java.awt.Color.cyan);
        jPanel9.setPreferredSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        jLabel12.setText("Byte");

        directBufferMemoryPanel.setBackground(java.awt.Color.white);
        directBufferMemoryPanel.setPreferredSize(new java.awt.Dimension(800, 200));
        directBufferMemoryPanel.setLayout(new java.awt.BorderLayout());

        jPanel10.setBackground(java.awt.Color.red);
        jPanel10.setPreferredSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        jLabel13.setText("Total");

        jPanel11.setBackground(java.awt.Color.blue);
        jPanel11.setPreferredSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        jLabel14.setText("Double");

        jPanel12.setBackground(java.awt.Color.green);
        jPanel12.setPreferredSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        jLabel15.setText("Float");

        jPanel13.setBackground(java.awt.Color.yellow);
        jPanel13.setPreferredSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        jLabel16.setText("Int");

        jPanel14.setBackground(java.awt.Color.magenta);
        jPanel14.setPreferredSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        jLabel17.setText("Short");

        jPanel15.setBackground(java.awt.Color.cyan);
        jPanel15.setPreferredSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        jLabel18.setText("Byte");

        javax.swing.GroupLayout memoryPanelLayout = new javax.swing.GroupLayout(memoryPanel);
        memoryPanel.setLayout(memoryPanelLayout);
        memoryPanelLayout.setHorizontalGroup(
            memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, memoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(memoryPanelLayout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 722, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, memoryPanelLayout.createSequentialGroup()
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(directBufferMemoryPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 819, Short.MAX_VALUE)
                            .addComponent(directMemoryPanel, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(systemMemoryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 819, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(forceGCButton)
                    .addGroup(memoryPanelLayout.createSequentialGroup()
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(jLabel14)
                            .addComponent(jLabel15)))
                    .addGroup(memoryPanelLayout.createSequentialGroup()
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16)
                            .addComponent(jLabel17)
                            .addComponent(jLabel18)))
                    .addGroup(memoryPanelLayout.createSequentialGroup()
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)))
                    .addGroup(memoryPanelLayout.createSequentialGroup()
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)))
                    .addGroup(memoryPanelLayout.createSequentialGroup()
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12))))
                .addContainerGap())
        );
        memoryPanelLayout.setVerticalGroup(
            memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(memoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(systemMemoryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                    .addGroup(memoryPanelLayout.createSequentialGroup()
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(directMemoryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                    .addGroup(memoryPanelLayout.createSequentialGroup()
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(memoryPanelLayout.createSequentialGroup()
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14)
                            .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16)
                            .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel17)
                            .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel18)))
                    .addComponent(directBufferMemoryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(forceGCButton))
        );

        jTabbedPane1.addTab("Memory", memoryPanel);

        jPanel18.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel27.setText("Total Buffer Count :");

        totalBufferSizeTF.setEditable(false);

        jLabel28.setText("Total Size :");

        totalBufferCountTF.setEditable(false);

        jLabel29.setText("Int Buffer Count :");

        jLabel30.setText("Byte Buffer Count :");

        jLabel32.setText("Float Buffer Count :");

        jLabel31.setText("Short Buffer Count :");

        jLabel33.setText("Double Buffer Count :");

        totalIntBufferCountTF.setEditable(false);

        totalByteBufferCountTF.setEditable(false);

        totalShortBufferCountTF.setEditable(false);

        totalFloatBufferCountTF.setEditable(false);

        totalDoubleBufferCountTF.setEditable(false);

        jLabel34.setText("Int Size :");

        jLabel35.setText("Byte Size :");

        jLabel36.setText("Short Size :");

        jLabel37.setText("Float Size :");

        jLabel38.setText("Double Size :");

        intBufferSizeTF.setEditable(false);

        byteBufferSizeTF.setEditable(false);

        shortBufferSizeTF.setEditable(false);

        floatBufferSizeTF.setEditable(false);

        doubleBufferSizeTF.setEditable(false);

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel33, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel32, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel31, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel30, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel29, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel27, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(totalBufferCountTF, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalDoubleBufferCountTF, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalFloatBufferCountTF, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalByteBufferCountTF, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalIntBufferCountTF, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalShortBufferCountTF, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(41, 41, 41)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel28, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel36, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel37, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel34, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel35, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel38, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(shortBufferSizeTF, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(intBufferSizeTF, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalBufferSizeTF, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(floatBufferSizeTF, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(doubleBufferSizeTF, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(byteBufferSizeTF, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel27)
                            .addComponent(totalBufferCountTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel29)
                            .addComponent(totalIntBufferCountTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel18Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(jLabel30))
                            .addGroup(jPanel18Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(totalByteBufferCountTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel31)
                            .addComponent(totalShortBufferCountTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel32)
                            .addComponent(totalFloatBufferCountTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel33)
                            .addComponent(totalDoubleBufferCountTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel28)
                            .addComponent(totalBufferSizeTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel34)
                            .addComponent(intBufferSizeTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(byteBufferSizeTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel35))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel36)
                            .addComponent(shortBufferSizeTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel37)
                            .addComponent(floatBufferSizeTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel38)
                            .addComponent(doubleBufferSizeTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        forceGCButton1.setText("Force GC");
        forceGCButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forceGCButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jmeBiffersPanelLayout = new javax.swing.GroupLayout(jmeBiffersPanel);
        jmeBiffersPanel.setLayout(jmeBiffersPanelLayout);
        jmeBiffersPanelLayout.setHorizontalGroup(
            jmeBiffersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jmeBiffersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jmeBiffersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(forceGCButton1)
                    .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(466, Short.MAX_VALUE))
        );
        jmeBiffersPanelLayout.setVerticalGroup(
            jmeBiffersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jmeBiffersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(forceGCButton1)
                .addContainerGap(329, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("JME Buffers", jmeBiffersPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 958, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void forceGCButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forceGCButtonActionPerformed
        System.gc();
    }//GEN-LAST:event_forceGCButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ClientMonitorFrame().setVisible(true);
            }
        });
    }

    class GetMemoryTask extends TimerTask {
        @Override
        public void run() {
            BufferStats stats = BufferUtils.getDirectMemoryUsage();

            tick++;
            long total = Runtime.getRuntime().totalMemory();
            long avail = Runtime.getRuntime().freeMemory();
            long max   = Runtime.getRuntime().maxMemory();

            totalSystemMemory.addPoint(BigDecimal.valueOf(tick),
                                       BigDecimal.valueOf(total / (1024 * 1024)));
            availSystemMemory.addPoint(BigDecimal.valueOf(tick),
                                       BigDecimal.valueOf((total - avail) / (1024 * 1024)));
            maxSystemMemory.addPoint(BigDecimal.valueOf(tick),
                                       BigDecimal.valueOf(max / (1024 * 1024)));
            systemMemoryGraph.render();
            systemMemoryGraph.repaint();

            totalBuffers.addPoint(BigDecimal.valueOf(tick), 
                                  BigDecimal.valueOf(stats.totalBuffers));
            doubleBuffers.addPoint(BigDecimal.valueOf(tick),
                                  BigDecimal.valueOf(stats.totalDoubleBuffers));
            floatBuffers.addPoint(BigDecimal.valueOf(tick),
                                  BigDecimal.valueOf(stats.totalFloatBuffers));
            intBuffers.addPoint(BigDecimal.valueOf(tick),
                                  BigDecimal.valueOf(stats.totalIntBuffers));
            shortBuffers.addPoint(BigDecimal.valueOf(tick),
                                  BigDecimal.valueOf(stats.totalShortBuffers));
            byteBuffers.addPoint(BigDecimal.valueOf(tick),
                                  BigDecimal.valueOf(stats.totalByteBuffers));
            directBuffersGraph.render();
            directBuffersGraph.repaint();

            totalBuffersMemory.addPoint(BigDecimal.valueOf(tick),
                                  BigDecimal.valueOf(stats.totalBufferBytes / (1024 * 1024)));
            doubleBuffersMemory.addPoint(BigDecimal.valueOf(tick),
                                  BigDecimal.valueOf(stats.totalDoubleBufferBytes / (1024 * 1024)));
            floatBuffersMemory.addPoint(BigDecimal.valueOf(tick),
                                  BigDecimal.valueOf(stats.totalFloatBufferBytes / (1024 * 1024)));
            intBuffersMemory.addPoint(BigDecimal.valueOf(tick),
                                  BigDecimal.valueOf(stats.totalIntBufferBytes / (1024 * 1024)));
            shortBuffersMemory.addPoint(BigDecimal.valueOf(tick),
                                  BigDecimal.valueOf(stats.totalShortBufferBytes / (1024 * 1024)));
            byteBuffersMemory.addPoint(BigDecimal.valueOf(tick),
                                  BigDecimal.valueOf(stats.totalByteBufferBytes / (1024 * 1024)));
            directBuffersMemoryGraph.render();
            directBuffersMemoryGraph.repaint();

            totalBufferCountTF.setText(Integer.toString(stats.totalBuffers));
            totalByteBufferCountTF.setText(Integer.toString(stats.totalByteBuffers));
            totalIntBufferCountTF.setText(Integer.toString(stats.totalIntBuffers));
            totalShortBufferCountTF.setText(Integer.toString(stats.totalShortBuffers));
            totalFloatBufferCountTF.setText(Integer.toString(stats.totalFloatBuffers));
            totalDoubleBufferCountTF.setText(Integer.toString(stats.totalDoubleBuffers));

            totalBufferSizeTF.setText(Long.toString(stats.totalBufferBytes));
            byteBufferSizeTF.setText(Long.toString(stats.totalByteBufferBytes));
            intBufferSizeTF.setText(Long.toString(stats.totalIntBufferBytes));
            shortBufferSizeTF.setText(Long.toString(stats.totalShortBufferBytes));
            floatBufferSizeTF.setText(Long.toString(stats.totalFloatBufferBytes));
            doubleBufferSizeTF.setText(Long.toString(stats.totalDoubleBufferBytes));
        }
    }

    class GetWorldInfoTask extends TimerTask {
        @Override
        public void run() {
            int sessionCount = 0;
            int rootCellCount = 0;
            int totalCellCount = 0;

            for (ServerSessionManager ssm : LoginManager.getAll()) {
                for (WonderlandSession session : ssm.getAllSessions()) {
                    sessionCount++;

                    CellCache cache = ClientContext.getCellCache(session);
                    if (cache != null) {
                        for (Cell root : cache.getRootCells()) {
                            rootCellCount++;
                            totalCellCount += countCells(root);
                        }
                    }
                }
            }

            worldSessionLabel.setText(String.valueOf(sessionCount));
            worldRootCellsLabel.setText(String.valueOf(rootCellCount));
            worldTotalCellsLabel.setText(String.valueOf(totalCellCount));
        }

        private int countCells(Cell root) {
            int count = 1;
            for (Cell child : root.getChildren()) {
                count += countCells(child);
            }

            return count;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField byteBufferSizeTF;
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel directBufferMemoryPanel;
    private javax.swing.JPanel directMemoryPanel;
    private javax.swing.JLabel displayAPIVersionLabel;
    private javax.swing.JLabel displayAdpaterLabel;
    private javax.swing.JLabel displayRendererLabel;
    private javax.swing.JLabel displayVendorLabel;
    private javax.swing.JLabel displayVersionLabel;
    private javax.swing.JTextField doubleBufferSizeTF;
    private javax.swing.JTextField floatBufferSizeTF;
    private javax.swing.JButton forceGCButton;
    private javax.swing.JButton forceGCButton1;
    private javax.swing.JPanel generalPanel;
    private javax.swing.JTextField intBufferSizeTF;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel jmeBiffersPanel;
    private javax.swing.JPanel memoryPanel;
    private javax.swing.JTextField shortBufferSizeTF;
    private javax.swing.JPanel systemMemoryPanel;
    private javax.swing.JTextField totalBufferCountTF;
    private javax.swing.JTextField totalBufferSizeTF;
    private javax.swing.JTextField totalByteBufferCountTF;
    private javax.swing.JTextField totalDoubleBufferCountTF;
    private javax.swing.JTextField totalFloatBufferCountTF;
    private javax.swing.JTextField totalIntBufferCountTF;
    private javax.swing.JTextField totalShortBufferCountTF;
    private javax.swing.JLabel worldRootCellsLabel;
    private javax.swing.JLabel worldSessionLabel;
    private javax.swing.JLabel worldTotalCellsLabel;
    // End of variables declaration//GEN-END:variables

}
