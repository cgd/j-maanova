/*
 * Copyright (c) 2010 The Jackson Laboratory
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

package org.jax.maanova.fit.gui;

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
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolTip;

import org.jax.maanova.Maanova;
import org.jax.maanova.fit.FitMaanovaResult;
import org.jax.maanova.plot.AreaSelectionListener;
import org.jax.maanova.plot.MaanovaChartPanel;
import org.jax.maanova.plot.PlotUtil;
import org.jax.maanova.plot.SaveChartAction;
import org.jax.maanova.plot.SimpleChartConfigurationDialog;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;

/**
 * A panel that plots residuals for a {@link FitMaanovaResult}. This is
 * analogous to the resiplot(...) method in R
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ResidualPlotPanel extends JPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -2931761054889899035L;
    
    private static final Logger LOG = Logger.getLogger(
            ResidualPlotPanel.class.getName());
    
    private static final int CURSOR_Y_OFFSET = 16;

    private final FitMaanovaResult fitMaanovaResult;

    private final int dyeCount;
    
    private final int arrayCount;

    private final MaanovaChartPanel chartPanel;

    private final JToolTip toolTip;
    
    private final JPanel controlPanel;

    private final JComboBox dyeComboBox;

    private XYProbeData[] cachedXYData = null;
    
    private final MouseMotionListener myMouseMotionListener = new MouseMotionAdapter()
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseMoved(MouseEvent e)
        {
            ResidualPlotPanel.this.mouseMoved(e);
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
            ResidualPlotPanel.this.clearProbePopup();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseExited(MouseEvent e)
        {
            ResidualPlotPanel.this.clearProbePopup();
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
            ResidualPlotPanel.this.saveGraphImageAction.setSize(
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
            ResidualPlotPanel.this.areaSelected(area);
        }
    };
    
    private final SaveChartAction saveGraphImageAction = new SaveChartAction();

    private volatile Rectangle2D viewArea = null;

    private volatile boolean showTooltip;
    
    private final SimpleChartConfigurationDialog chartConfigurationDialog;
    
    /**
     * Constructor
     * @param parent
     *          the parent frame
     * @param fitMaanovaResult
     *          the fitmaanova result that we'll plot residuals for
     */
    public ResidualPlotPanel(JFrame parent, FitMaanovaResult fitMaanovaResult)
    {
        this.chartConfigurationDialog = new SimpleChartConfigurationDialog(parent);
        this.chartConfigurationDialog.addOkActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ResidualPlotPanel.this.updateDataPoints();
            }
        });
        
        
        this.fitMaanovaResult = fitMaanovaResult;
        this.dyeCount = this.fitMaanovaResult.getParentExperiment().getDyeCount();
        this.arrayCount = this.fitMaanovaResult.getParentExperiment().getMicroarrayCount();
        
        this.setLayout(new BorderLayout());
        
        JPanel chartAndControlPanel = new JPanel(new BorderLayout());
        this.add(chartAndControlPanel, BorderLayout.CENTER);
        
        this.chartPanel = new MaanovaChartPanel();
        this.chartPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        this.chartPanel.addComponentListener(this.chartComponentListener);
        this.chartPanel.addAreaSelectionListener(this.areaSelectionListener);
        this.chartPanel.addMouseListener(this.chartMouseListener);
        this.chartPanel.addMouseMotionListener(this.myMouseMotionListener);
        this.chartPanel.setLayout(null);
        chartAndControlPanel.add(this.chartPanel, BorderLayout.CENTER);
        
        ItemListener updateDataItemListener = new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                ResidualPlotPanel.this.forgetGraphState();
                ResidualPlotPanel.this.updateDataPoints();
            }
        };
        
        if(this.dyeCount <= 1)
        {
            this.controlPanel = null;
            this.dyeComboBox = null;
        }
        else
        {
            this.dyeComboBox = new JComboBox();
            for(int i = 0; i < this.dyeCount; i++)
            {
                this.dyeComboBox.addItem("Dye #" + (i + 1));
            }
            this.dyeComboBox.addItemListener(updateDataItemListener);
            
            this.controlPanel = new JPanel(new FlowLayout());
            this.controlPanel.add(this.dyeComboBox);
            chartAndControlPanel.add(this.controlPanel, BorderLayout.NORTH);
        }
        
        this.add(this.createMenu(), BorderLayout.NORTH);
        
        this.forgetGraphState();
        this.updateDataPoints();
        
        this.toolTip = new JToolTip();
    }
    
    /**
     * Forget about the axis labeling and the zoom level
     */
    private void forgetGraphState()
    {
        this.chartConfigurationDialog.setChartTitle(
                "Residual Plot for " + this.fitMaanovaResult.toString());
        this.chartConfigurationDialog.setXAxisLabel("Y Hat");
        this.chartConfigurationDialog.setYAxisLabel("Residual");
        
        this.viewArea = null;
    }
    
    private void updateDataPoints()
    {
        this.cachedXYData = null;
        XYProbeData[] xyData = this.getXYData();
        
        DefaultXYDataset xyDataSet = new DefaultXYDataset();
        for(int arrayIndex = 0; arrayIndex < xyData.length; arrayIndex++)
        {
            XYProbeData currData = xyData[arrayIndex];
            xyDataSet.addSeries(
                    arrayIndex,
                    new double[][] {currData.getXData(), currData.getYData()});
        }

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

    private int getSelectedDyeIndex()
    {
        if(this.dyeComboBox == null)
        {
            return 0;
        }
        else
        {
            return this.dyeComboBox.getSelectedIndex();
        }
    }

    private synchronized XYProbeData[] getXYData()
    {
        if(this.cachedXYData == null)
        {
            this.cachedXYData = this.createXYData(this.getSelectedDyeIndex());
        }
        
        return this.cachedXYData;
    }
    
    private XYProbeData[] createXYData(int dyeIndex)
    {
        XYProbeData[] probeData = new XYProbeData[this.arrayCount];
        
        for(int arrayIndex = 0; arrayIndex < this.arrayCount; arrayIndex++)
        {
            Double[] dataValues =
                this.fitMaanovaResult.getParentExperiment().getData(
                        dyeIndex,
                        arrayIndex);
            Double[] yHatValues =
                this.fitMaanovaResult.getYHatValues(
                        dyeIndex,
                        arrayIndex);
            
            // check the array lengths which should be the same if everything is OK
            if(dataValues.length != yHatValues.length)
            {
                throw new IllegalArgumentException(
                        "There is a missmatch between the number of data points (" +
                        dataValues.length +
                        ") and y-hat (" + yHatValues.length + ") values");
            }
            
            // first count all non-null pairings
            int nonNullCount = 0;
            for(int i = 0; i < dataValues.length; i++)
            {
                if(dataValues[i] != null && yHatValues[i] != null)
                {
                    nonNullCount++;
                }
            }
            
            if(nonNullCount != dataValues.length &&
               LOG.isLoggable(Level.WARNING))
            {
                LOG.warning(
                        "Found " + (dataValues.length - nonNullCount) +
                        " NaN data points in the residual plot data");
            }
            
            // OK, now convert to primitive arrays
            double[] primXValues = new double[nonNullCount];
            double[] primYValues = new double[nonNullCount];
            int[] probeIndices = new int[nonNullCount];
            int primitiveArraysIndex = 0;
            for(int objArraysIndex = 0; objArraysIndex < dataValues.length; objArraysIndex++)
            {
                if(dataValues[objArraysIndex] != null && yHatValues[objArraysIndex] != null)
                {
                    double data = dataValues[objArraysIndex];
                    double yHat = yHatValues[objArraysIndex];
                    double residual = data - yHat;
                    primXValues[primitiveArraysIndex] = yHat;
                    primYValues[primitiveArraysIndex] = residual;
                    probeIndices[primitiveArraysIndex] = objArraysIndex;
                    
                    primitiveArraysIndex++;
                }
            }
            
            probeData[arrayIndex] =
                new XYProbeData(primXValues, primYValues, probeIndices);
        }
        
        return probeData;
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
                ResidualPlotPanel.this.chartConfigurationDialog.setVisible(true);
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
                ResidualPlotPanel.this.autoRangeChart();
            }
        });
        toolsMenu.addSeparator();
        
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
                ResidualPlotPanel.this.showTooltip =
                    e.getStateChange() == ItemEvent.SELECTED;
                ResidualPlotPanel.this.clearProbePopup();
            }
        });
        toolsMenu.add(showTooltipCheckbox);
        menuBar.add(toolsMenu);
        
        // the help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem helpMenuItem = new JMenuItem(
                "Help...",
                new ImageIcon(ResidualPlotAction.class.getResource(
                        "/images/action/help-16x16.png")));
        helpMenuItem.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                Maanova.getInstance().showHelp(
                        "residual-plot",
                        ResidualPlotPanel.this);
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
    
    private void mouseMoved(MouseEvent e)
    {
        if(this.showTooltip)
        {
            Point2D chartPoint = this.chartPanel.toChartPoint(e.getPoint());
            
            // find the nearest probe
            XYProbeData[] xyProbeData = this.getXYData();
            double nearestDistance = Double.POSITIVE_INFINITY;
            int nearestArrayIndex = -1;
            int nearestDotIndex = -1;
            for(int arrayIndex = 0; arrayIndex < xyProbeData.length; arrayIndex++)
            {
                double[] currXData = xyProbeData[arrayIndex].getXData();
                double[] currYData = xyProbeData[arrayIndex].getYData();
                for(int dotIndex = 0; dotIndex < currXData.length; dotIndex++)
                {
                    double currDist = chartPoint.distanceSq(
                            currXData[dotIndex],
                            currYData[dotIndex]);
                    if(currDist < nearestDistance)
                    {
                        nearestDistance = currDist;
                        nearestArrayIndex = arrayIndex;
                        nearestDotIndex = dotIndex;
                    }
                }
            }
            
            if(nearestArrayIndex == -1)
            {
                this.clearProbePopup();
            }
            else
            {
                XYProbeData nearestArrayData = xyProbeData[nearestArrayIndex];
                Point2D probeJava2DCoord = this.getJava2DCoordinates(
                        nearestArrayData.getXData()[nearestDotIndex],
                        nearestArrayData.getYData()[nearestDotIndex]);
                double java2DDist = probeJava2DCoord.distance(e.getX(), e.getY());
                
                // is the probe close enough to be worth showing (in pixel distance)
                if(java2DDist <= PlotUtil.SCATTER_PLOT_DOT_SIZE_PIXELS * 2)
                {
                    this.showProbePopup(
                            nearestArrayIndex,
                            nearestArrayData.getProbeIndices()[nearestDotIndex],
                            nearestArrayData.getXData()[nearestDotIndex],
                            nearestArrayData.getYData()[nearestDotIndex],
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
            int nearestArrayIndex,
            int nearestProbesetIndex,
            double nearestProbesetYHat,
            double nearestProbesetResidual,
            int pixelX,
            int pixelY)
    {
        if(this.toolTip.getParent() == null)
        {
            this.chartPanel.add(this.toolTip);
        }
        
        String nearestProbesetID = this.fitMaanovaResult.getProbesetId(
                nearestProbesetIndex);
        
        if(nearestProbesetID == null)
        {
            LOG.severe("Failed to lookup probeset name");
        }
        else
        {
            final String rowStart = "<tr><td>";
            final String rowStop = "</td></tr>";
            final String cellDelimiter = "</td><td>";
            StringBuilder tableRowsString = new StringBuilder("<html><table>");
            tableRowsString.append(rowStart);
            tableRowsString.append("Array #:");
            tableRowsString.append(cellDelimiter);
            tableRowsString.append(nearestArrayIndex + 1);
            tableRowsString.append(rowStop);
            tableRowsString.append(rowStart);
            tableRowsString.append("ID:");
            tableRowsString.append(cellDelimiter);
            tableRowsString.append(nearestProbesetID);
            tableRowsString.append(rowStop);
            tableRowsString.append(rowStart);
            tableRowsString.append("Y Hat:");
            tableRowsString.append(cellDelimiter);
            tableRowsString.append(nearestProbesetYHat);
            tableRowsString.append(rowStop);
            tableRowsString.append(rowStart);
            tableRowsString.append("Residual:");
            tableRowsString.append(cellDelimiter);
            tableRowsString.append(nearestProbesetResidual);
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
