/*
 * Copyright (c) 2009 The Jackson Laboratory
 * 
 * This software was developed by Gary Churchill's Lab at The Jackson
 * Laboratory (see http://research.jax.org/faculty/churchill).
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jax.maanova.madata.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolTip;

import org.jax.maanova.Maanova;
import org.jax.maanova.madata.MicroarrayExperiment;
import org.jax.maanova.madata.MicroarrayExperimentDesign;
import org.jax.maanova.plot.AreaSelectionListener;
import org.jax.maanova.plot.MaanovaChartPanel;
import org.jax.maanova.plot.PlotUtil;
import org.jax.maanova.plot.SaveChartAction;
import org.jax.maanova.plot.SimpleChartConfigurationDialog;
import org.jax.util.gui.MessageDialogUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;

/**
 * A scatter plot that compares intensities for two arrays at a time
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ArrayScatterPlotPanel extends JPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 7778496395222070792L;

    private static final int CURSOR_Y_OFFSET = 16;

    private static final Logger LOG = Logger.getLogger(
            ArrayScatterPlotPanel.class.getName());
    
    private final MicroarrayExperiment experiment;

    private final int dyeCount;
    
    private final MaanovaChartPanel chartPanel;

    private final JToolTip toolTip;
    private volatile boolean showTooltip;
    
    private final JPanel controlPanel;

    private final JComboBox array1ComboBox;
    
    private final JComboBox array2ComboBox;

    private XYProbeData cachedXYData = null;
    
    private final MouseMotionListener myMouseMotionListener = new MouseMotionAdapter()
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseMoved(MouseEvent e)
        {
            ArrayScatterPlotPanel.this.mouseMoved(e);
        }
    };
    
    private final MouseListener chartMouseListener = new MouseAdapter()
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void mousePressed(MouseEvent e)
        {
            ArrayScatterPlotPanel.this.clearProbePopup();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseExited(MouseEvent e)
        {
            ArrayScatterPlotPanel.this.clearProbePopup();
        }
    };

    private final ComponentListener chartComponentListener = new ComponentAdapter()
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void componentResized(ComponentEvent e)
        {
            ArrayScatterPlotPanel.this.saveGraphImageAction.setSize(
                    e.getComponent().getSize());
        }
    };
    
    private final AreaSelectionListener areaSelectionListener = new AreaSelectionListener()
    {
        /**
         * {@inheritDoc}
         */
        public void areaSelected(Rectangle2D area)
        {
            ArrayScatterPlotPanel.this.areaSelected(area);
        }
    };
    
    private final SaveChartAction saveGraphImageAction = new SaveChartAction();

    private volatile Rectangle2D viewArea = null;

    private final SimpleChartConfigurationDialog chartConfigurationDialog;
    
    /**
     * Constructor
     * @param parent
     *          the parent frame
     * @param experiment
     *          the microarray experiment that we're going to be plotting data
     *          for
     */
    public ArrayScatterPlotPanel(JFrame parent, MicroarrayExperiment experiment)
    {
        this.chartConfigurationDialog = new SimpleChartConfigurationDialog(parent);
        this.chartConfigurationDialog.addOkActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ArrayScatterPlotPanel.this.updateDataPoints();
            }
        });
        
        
        this.experiment = experiment;
        this.dyeCount = experiment.getDyeCount();
        
        this.setLayout(new BorderLayout());
        
        JPanel chartAndControlPanel = new JPanel(new BorderLayout());
        this.add(chartAndControlPanel, BorderLayout.CENTER);
        
        this.chartPanel = new MaanovaChartPanel();
        this.chartPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        this.chartPanel.addMouseMotionListener(this.myMouseMotionListener);
        this.chartPanel.addMouseListener(this.chartMouseListener);
        this.chartPanel.addComponentListener(this.chartComponentListener);
        this.chartPanel.addAreaSelectionListener(this.areaSelectionListener);
        this.chartPanel.setLayout(null);
        chartAndControlPanel.add(this.chartPanel, BorderLayout.CENTER);
        
        ItemListener updateDataItemListener = new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                ArrayScatterPlotPanel.this.forgetGraphState();
                ArrayScatterPlotPanel.this.updateDataPoints();
            }
        };
        
        this.array1ComboBox = this.initializeArrayComboBox(this.dyeCount);
        this.array1ComboBox.addItemListener(updateDataItemListener);
        this.array2ComboBox = this.initializeArrayComboBox(this.dyeCount);
        this.array2ComboBox.setSelectedIndex(1);
        this.array2ComboBox.addItemListener(updateDataItemListener);
        
        this.controlPanel = new JPanel(new FlowLayout());
        this.controlPanel.add(this.array1ComboBox);
        this.controlPanel.add(new JLabel("vs."));
        this.controlPanel.add(this.array2ComboBox);
        chartAndControlPanel.add(this.controlPanel, BorderLayout.NORTH);
        
        this.add(this.createMenu(), BorderLayout.NORTH);
        
        this.forgetGraphState();
        this.updateDataPoints();
        
        this.toolTip = new JToolTip();
    }
    
    private void mouseMoved(MouseEvent e)
    {
        if(this.showTooltip)
        {
            Point2D chartPoint = this.chartPanel.toChartPoint(e.getPoint());
            
            // find the nearest probe
            XYProbeData xyProbeData = this.getXYData();
            double nearestDistance = Double.POSITIVE_INFINITY;
            int nearestDotIndex = -1;
            double[] xData = xyProbeData.getXData();
            double[] yData = xyProbeData.getYData();
            for(int dotIndex = 0; dotIndex < xData.length ; dotIndex++)
            {
                double currDist = chartPoint.distanceSq(
                        xData[dotIndex],
                        yData[dotIndex]);
                if(currDist < nearestDistance)
                {
                    nearestDistance = currDist;
                    nearestDotIndex = dotIndex;
                }
            }
            
            if(nearestDotIndex == -1)
            {
                this.clearProbePopup();
            }
            else
            {
                Point2D probeJava2DCoord = this.getJava2DCoordinates(
                        xData[nearestDotIndex],
                        yData[nearestDotIndex]);
                double java2DDist = probeJava2DCoord.distance(e.getX(), e.getY());
                
                // is the probe close enough to be worth showing (in pixel distance)
                if(java2DDist <= PlotUtil.SCATTER_PLOT_DOT_SIZE_PIXELS * 2)
                {
                    this.showProbePopup(
                            xyProbeData.getProbeIndices()[nearestDotIndex],
                            xData[nearestDotIndex],
                            yData[nearestDotIndex],
                            e.getX(),
                            e.getY());
                }
                else
                {
                    this.clearProbePopup();
                }
            }
        }
    }
    
    private Point2D getJava2DCoordinates(double graphX, double graphY)
    {
        final XYPlot plot = (XYPlot)this.chartPanel.getChart().getPlot();
        final ChartRenderingInfo renderingInfo = this.chartPanel.getChartRenderingInfo();
        
        return PlotUtil.toJava2DCoordinates(plot, renderingInfo, graphX, graphY);
    }

    private void clearProbePopup()
    {
        if(this.toolTip.getParent() != null)
        {
            this.chartPanel.remove(this.toolTip);
            this.chartPanel.repaint();
        }
    }

    private void showProbePopup(
            int nearestProbesetIndex,
            double firstArrayIntensity,
            double secondArrayIntensity,
            int pixelX,
            int pixelY)
    {
        if(this.toolTip.getParent() == null)
        {
            this.chartPanel.add(this.toolTip);
        }
        
        String nearestProbesetID = this.experiment.getProbesetId(
                nearestProbesetIndex);
        
        if(nearestProbesetID == null)
        {
            LOG.severe("Failed to lookup probeset name");
        }
        else
        {
            int firstArrayNumber = this.array1ComboBox.getSelectedIndex() + 1;
            int secondArrayNumber = this.array2ComboBox.getSelectedIndex() + 1;
            
            final String rowStart = "<tr><td>";
            final String rowStop = "</td></tr>";
            final String cellDelimiter = "</td><td>";
            StringBuilder tableRowsString = new StringBuilder("<html><table>");
            tableRowsString.append(rowStart);
            tableRowsString.append("ID:");
            tableRowsString.append(cellDelimiter);
            tableRowsString.append(nearestProbesetID);
            tableRowsString.append(rowStop);
            tableRowsString.append(rowStart);
            tableRowsString.append("Array " + firstArrayNumber + ":");
            tableRowsString.append(cellDelimiter);
            tableRowsString.append(firstArrayIntensity);
            tableRowsString.append(rowStop);
            tableRowsString.append(rowStart);
            tableRowsString.append("Array " + secondArrayNumber + ":");
            tableRowsString.append(cellDelimiter);
            tableRowsString.append(secondArrayIntensity);
            tableRowsString.append(rowStop);
            tableRowsString.append("</table></html>");
            
            this.toolTip.setTipText(tableRowsString.toString());
            
            // if the tool tip goes off the right edge of the screen, move it to the
            // left side of the cursor
            final int tooltipX;
            if(pixelX + this.toolTip.getPreferredSize().width >
               this.chartPanel.getWidth())
            {
                tooltipX = pixelX - this.toolTip.getPreferredSize().width;
            }
            else
            {
                tooltipX = pixelX;
            }
            
            final int tooltipY;
            if(pixelY + this.toolTip.getPreferredSize().height + CURSOR_Y_OFFSET >
               this.chartPanel.getHeight())
            {
                tooltipY =
                    (pixelY - this.toolTip.getPreferredSize().height) -
                    CURSOR_Y_OFFSET;
            }
            else
            {
                tooltipY = pixelY + CURSOR_Y_OFFSET;
            }
            this.toolTip.setLocation(tooltipX, tooltipY);
            
            this.toolTip.setSize(this.toolTip.getPreferredSize());
        }
    }
    
    private JComboBox initializeArrayComboBox(int dyeCount)
    {
        JComboBox arrayComboBox = new JComboBox();
        MicroarrayExperimentDesign design = this.experiment.getDesign();
        String[] arrayCol = design.getColumnNamed(MicroarrayExperimentDesign.ARRAY_COL_NAME);
        if(dyeCount == 1)
        {
            for(int i = 0; i < arrayCol.length; i++)
            {
                arrayComboBox.addItem(arrayCol[i]);
            }
        }
        else
        {
            String[] dyeCol = design.getColumnNamed(MicroarrayExperimentDesign.DYE_COL_NAME);
            if(dyeCol.length != arrayCol.length)
            {
                String errorMessage =
                    "Expected the number of design terms to be the same for \"" +
                    MicroarrayExperimentDesign.ARRAY_COL_NAME + "\" and \"" +
                    MicroarrayExperimentDesign.DYE_COL_NAME +
                    "\" but a missmatch was found: " + arrayCol.length + " vs. " +
                    dyeCol.length;
                LOG.severe(errorMessage);
                MessageDialogUtilities.error(
                        this,
                        errorMessage,
                        "Design Term Count Missmatch");
            }
            else
            {
                for(int i = 0; i < arrayCol.length; i++)
                {
                    arrayComboBox.addItem(arrayCol[i] + ", " + dyeCol[i]);
                }
            }
        }
        
        return arrayComboBox;
    }
    
    /**
     * Forget about the axis labeling and the zoom level
     */
    private void forgetGraphState()
    {
        this.chartConfigurationDialog.setChartTitle(
                "Array Comparison Scatter Plot");
        this.chartConfigurationDialog.setXAxisLabel(
                this.array1ComboBox.getSelectedItem().toString());
        this.chartConfigurationDialog.setYAxisLabel(
                this.array2ComboBox.getSelectedItem().toString());
        
        this.viewArea = null;
    }
    
    private void updateDataPoints()
    {
        this.cachedXYData = null;
        XYProbeData currData = this.getXYData();
        
        DefaultXYDataset xyDataSet = new DefaultXYDataset();
        xyDataSet.addSeries(
                "data",
                new double[][] {currData.getXData(), currData.getYData()});

        JFreeChart scatterPlot = ChartFactory.createScatterPlot(
                this.chartConfigurationDialog.getChartTitle(),
                this.chartConfigurationDialog.getXAxisLabel(),
                this.chartConfigurationDialog.getYAxisLabel(),
                xyDataSet,
                PlotOrientation.VERTICAL,
                false,
                false,
                false);
        
        XYPlot xyPlot = (XYPlot)scatterPlot.getPlot();
        xyPlot.setRenderer(PlotUtil.createMonochromeScatterPlotRenderer());
        if(this.viewArea != null)
        {
            PlotUtil.rescaleXYPlot(this.viewArea, xyPlot);
        }
        
        this.saveGraphImageAction.setChart(scatterPlot);
        this.chartPanel.setChart(scatterPlot);
    }

    private synchronized XYProbeData getXYData()
    {
        if(this.cachedXYData == null)
        {
            int index1 = this.array1ComboBox.getSelectedIndex();
            int index2 = this.array2ComboBox.getSelectedIndex();
            
            int array1 = index1 / this.dyeCount;
            int dye1 = index1 % this.dyeCount;
            
            int array2 = index2 / this.dyeCount;
            int dye2 = index2 % this.dyeCount;
            
            this.cachedXYData = this.createXYData(array1, dye1, array2, dye2);
        }
        
        return this.cachedXYData;
    }
    
    private XYProbeData createXYData(int array1, int dye1, int array2, int dye2)
    {
        Double[] xValues = this.experiment.getData(dye1, array1);
        Double[] yValues = this.experiment.getData(dye2, array2);
        
        // check the array lengths which should be the same if everything is OK
        if(xValues.length != yValues.length)
        {
            throw new IllegalArgumentException(
                    "There is a missmatch between the number of data points (" +
                    xValues.length +
                    ") and (" + yValues.length + ")");
        }
        
        // first count all non-null pairings
        int nonNullCount = 0;
        for(int i = 0; i < xValues.length; i++)
        {
            if(xValues[i] != null && yValues[i] != null)
            {
                nonNullCount++;
            }
        }
        
        if(nonNullCount != xValues.length &&
           LOG.isLoggable(Level.WARNING))
        {
            LOG.warning(
                    "Found " + (xValues.length - nonNullCount) +
                    " NaN data points in the scatter plot data");
        }
        
        // OK, now convert to primitive arrays
        double[] primXValues = new double[nonNullCount];
        double[] primYValues = new double[nonNullCount];
        int[] probeIndices = new int[nonNullCount];
        int primitiveArraysIndex = 0;
        for(int objArraysIndex = 0; objArraysIndex < xValues.length; objArraysIndex++)
        {
            if(xValues[objArraysIndex] != null && yValues[objArraysIndex] != null)
            {
                double x = xValues[objArraysIndex];
                double y = yValues[objArraysIndex];
                primXValues[primitiveArraysIndex] = x;
                primYValues[primitiveArraysIndex] = y;
                probeIndices[primitiveArraysIndex] = objArraysIndex;
                
                primitiveArraysIndex++;
            }
        }
        
        return new XYProbeData(primXValues, primYValues, probeIndices);
    }
    
    @SuppressWarnings("serial")
    private JMenuBar createMenu()
    {
        JMenuBar menuBar = new JMenuBar();
        
        // the file menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(this.saveGraphImageAction);
        menuBar.add(fileMenu);
        
        // the tools menu
        JMenu toolsMenu = new JMenu("Tools");
        JMenuItem configureGraphItem = new JMenuItem("Configure Graph...");
        configureGraphItem.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ArrayScatterPlotPanel.this.chartConfigurationDialog.setVisible(true);
            }
        });
        toolsMenu.add(configureGraphItem);
        toolsMenu.addSeparator();
        
        toolsMenu.add(new AbstractAction("Zoom Out")
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ArrayScatterPlotPanel.this.autoRangeChart();
            }
        });
        
        JCheckBoxMenuItem showTooltipCheckbox =
            new JCheckBoxMenuItem("Show Info Popup for Nearest Point");
        showTooltipCheckbox.setSelected(true);
        this.showTooltip = true;
        showTooltipCheckbox.addItemListener(new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                ArrayScatterPlotPanel.this.showTooltip =
                    e.getStateChange() == ItemEvent.SELECTED;
                ArrayScatterPlotPanel.this.clearProbePopup();
            }
        });
        toolsMenu.add(showTooltipCheckbox);
        menuBar.add(toolsMenu);
        
        // the help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem helpMenuItem = new JMenuItem("Help...");
        Icon helpIcon = new ImageIcon(ArrayScatterPlotPanel.class.getResource(
                "/images/action/help-16x16.png"));
        helpMenuItem.setIcon(helpIcon);
        helpMenuItem.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                Maanova.getInstance().showHelp(
                        "array-scatter-plot",
                        ArrayScatterPlotPanel.this);
            }
        });
        helpMenu.add(helpMenuItem);
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    private void autoRangeChart()
    {
        this.viewArea = null;
        this.updateDataPoints();
    }
    
    private void areaSelected(Rectangle2D area)
    {
        Rectangle2D chartArea = this.chartPanel.toChartRectangle(area);
        
        this.viewArea = chartArea;
        this.updateDataPoints();
    }
    
    private class XYProbeData
    {
        private final double[] xData;
        
        private final double[] yData;
        
        private final int[] probeIndices;

        /**
         * Constructor
         * @param xData the x axis data
         * @param yData the y axis data
         * @param probeIndices  the indices for the corresponding probes
         */
        public XYProbeData(
                double[] xData,
                double[] yData,
                int[] probeIndices)
        {
            this.xData = xData;
            this.yData = yData;
            this.probeIndices = probeIndices;
        }
        
        /**
         * Getter for the probe indices
         * @return the probeIndices
         */
        public int[] getProbeIndices()
        {
            return this.probeIndices;
        }
        
        /**
         * Getter for the X data
         * @return the xData
         */
        public double[] getXData()
        {
            return this.xData;
        }
        
        /**
         * Getter for the Y data
         * @return the yData
         */
        public double[] getYData()
        {
            return this.yData;
        }
    }
}
