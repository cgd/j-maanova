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

package org.jax.maanova.test.gui;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
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
import org.jax.maanova.madata.MicroarrayExperiment;
import org.jax.maanova.madata.ProbesetRow;
import org.jax.maanova.madata.gui.AddGeneListDialog;
import org.jax.maanova.plot.AreaSelectionListener;
import org.jax.maanova.plot.MaanovaChartPanel;
import org.jax.maanova.plot.PlotUtil;
import org.jax.maanova.plot.SaveChartAction;
import org.jax.maanova.plot.SimpleChartConfigurationDialog;
import org.jax.maanova.test.MaanovaTestResult;
import org.jax.maanova.test.MaanovaTestStatisticSubtype;
import org.jax.maanova.test.MaanovaTestStatisticType;
import org.jax.maanova.test.MaanovaTestStatistics;
import org.jax.r.RUtilities;
import org.jax.util.datastructure.SequenceUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

/**
 * Panel used to render a volcano plot
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class VolcanoPlotPanel extends JPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 2887360960196852317L;
    
    private static final double MIN_PVALUE_THRESHOLD = 1e-9;
    
    /**
     * logger for this class
     */
    private static final Logger LOG = Logger.getLogger(
            VolcanoPlotPanel.class.getName());

    private static final int CURSOR_Y_OFFSET = 16;

    private final MaanovaTestResult maanovaTestResult;
    
    private final JMenuItem saveSelectedPointsMenuItem;
    
    private final DisplayTestResultsAction displayTestResultsAction;
    
    private final JPanel controlPanel;
    
    private final JToolTip toolTip;
    
    private final MaanovaChartPanel chartPanel;
    
    private final JComboBox statisticTypeComboBox;
    
    private final JComboBox statisticSubtypeComboBox;
    
    private final JComboBox testNumberComboBox;
    
    private XYProbeData cachedXYData = null;
    
    private volatile int[] selectedIndices = new int[0];
    
    private final SimpleChartConfigurationDialog chartConfigurationDialog;
    
    private final MouseMotionListener myMouseMotionListener = new MouseMotionAdapter()
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseMoved(MouseEvent e)
        {
            VolcanoPlotPanel.this.mouseMoved(e);
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
            VolcanoPlotPanel.this.clearProbePopup();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseExited(MouseEvent e)
        {
            VolcanoPlotPanel.this.clearProbePopup();
        }
    };

    private final AreaSelectionListener areaSelectionListener = new AreaSelectionListener()
    {
        /**
         * {@inheritDoc}
         */
        public void areaSelected(Rectangle2D area)
        {
            VolcanoPlotPanel.this.areaSelected(area);
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
            VolcanoPlotPanel.this.saveGraphImageAction.setSize(
                    e.getComponent().getSize());
        }
    };
    
    private final SaveChartAction saveGraphImageAction = new SaveChartAction();

    private volatile Rectangle2D viewArea = null;

    private volatile boolean dragToSelect;

    private volatile boolean dragToZoom;
    
    private volatile boolean showTooltip;

    private final TestStatisticItem[] availableTestStatistics;
    
    /**
     * Constructor
     * @param parent the parent frame
     * @param maanovaTestResult
     *          the test result to plot
     */
    public VolcanoPlotPanel(
            JFrame parent,
            MaanovaTestResult maanovaTestResult)
    {
        this(parent, maanovaTestResult, 0, new int[0]);
    }

    /**
     * Constructor
     * @param parent the parent frame
     * @param maanovaTestResult
     *          the test result to plot
     * @param initialTestIndex
     *          the initial test index to use
     * @param selectedIndices
     *          the initially selected indices
     */
    public VolcanoPlotPanel(
            JFrame parent,
            MaanovaTestResult maanovaTestResult,
            int initialTestIndex,
            int[] selectedIndices)
    {
        selectedIndices = SequenceUtilities.uniqueInts(selectedIndices);
        
        this.chartConfigurationDialog = new SimpleChartConfigurationDialog(parent);
        this.chartConfigurationDialog.addOkActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                VolcanoPlotPanel.this.updateDataPoints();
            }
        });
        
        this.maanovaTestResult = maanovaTestResult;
        this.saveSelectedPointsMenuItem = new JMenuItem(
                "Save Selected Points to Gene List");
        this.saveSelectedPointsMenuItem.setEnabled(
                selectedIndices != null && selectedIndices.length >= 1);
        this.saveSelectedPointsMenuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                VolcanoPlotPanel.this.saveSelectedPoints();
            }
        });
        
        this.displayTestResultsAction = new DisplayTestResultsAction(
                "Show Results Table",
                maanovaTestResult);
        this.selectedIndices = selectedIndices;
        
        this.setLayout(new BorderLayout());
        
        JPanel chartAndControlPanel = new JPanel(new BorderLayout());
        this.add(chartAndControlPanel, BorderLayout.CENTER);
        
        this.chartPanel = new MaanovaChartPanel();
        this.chartPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        this.chartPanel.addComponentListener(this.chartComponentListener);
        this.chartPanel.addAreaSelectionListener(this.areaSelectionListener);
        this.chartPanel.addMouseListener(this.chartMouseListener);
        chartAndControlPanel.add(this.chartPanel, BorderLayout.CENTER);
        
        this.controlPanel = new JPanel(new FlowLayout());
        
        ItemListener updateDataItemListener = new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                VolcanoPlotPanel.this.forgetGraphState();
                VolcanoPlotPanel.this.updateDataPoints();
            }
        };
        
        this.statisticTypeComboBox = new JComboBox();
        for(MaanovaTestStatisticType testStatType: MaanovaTestStatisticType.values())
        {
            this.statisticTypeComboBox.addItem(testStatType);
        }
        this.statisticTypeComboBox.addItemListener(updateDataItemListener);
        this.controlPanel.add(this.statisticTypeComboBox);
        
        List<MaanovaTestStatisticSubtype> availableTestSubtypes =
            new ArrayList<MaanovaTestStatisticSubtype>();
        this.statisticSubtypeComboBox = new JComboBox();
        MaanovaTestStatistics fStat = this.maanovaTestResult.getStatistics(
                MaanovaTestStatisticType.F_STAT);
        for(MaanovaTestStatisticSubtype statSubtype: MaanovaTestStatisticSubtype.values())
        {
            if(fStat.hasTestStatistic(statSubtype))
            {
                availableTestSubtypes.add(statSubtype);
                
                if(statSubtype != MaanovaTestStatisticSubtype.F_OBSERVED)
                {
                    this.statisticSubtypeComboBox.addItem(statSubtype);
                }
            }
        }
        this.statisticSubtypeComboBox.addItemListener(updateDataItemListener);
        this.controlPanel.add(this.statisticSubtypeComboBox);
        
        int testStatCount =
            availableTestSubtypes.size() * MaanovaTestStatisticType.values().length;
        this.availableTestStatistics = new TestStatisticItem[testStatCount];
        for(int i = 0; i < MaanovaTestStatisticType.values().length; i++)
        {
            for(int j = 0; j < availableTestSubtypes.size(); j++)
            {
                int flatIndex = i * availableTestSubtypes.size() + j;
                this.availableTestStatistics[flatIndex] = new TestStatisticItem(
                        MaanovaTestStatisticType.values()[i],
                        availableTestSubtypes.get(j));
            }
        }
        
        int testCount = fStat.getContrastCount();
        if(testCount == 1)
        {
            this.testNumberComboBox = null;
        }
        else
        {
            this.testNumberComboBox = new JComboBox();
            for(int i = 1; i <= testCount; i++)
            {
                this.testNumberComboBox.addItem("Test Number " + i);
            }
            this.testNumberComboBox.setSelectedIndex(initialTestIndex);
            this.testNumberComboBox.addItemListener(updateDataItemListener);
            this.controlPanel.add(this.testNumberComboBox);
        }
        
        chartAndControlPanel.add(this.controlPanel, BorderLayout.NORTH);
        
        JMenuBar menu = this.createMenu();
        this.add(menu, BorderLayout.NORTH);
        
        this.forgetGraphState();
        this.updateDataPoints();
        
        this.chartPanel.addMouseMotionListener(this.myMouseMotionListener);
        this.chartPanel.setLayout(null);
        
        this.toolTip = new JToolTip();
    }
    
    private void saveSelectedPoints()
    {
        int[] currSelectedIndices = this.selectedIndices;
        String[] probeIds = this.maanovaTestResult.getParentExperiment().getProbesetIds();
        List<String> selectedGenes = new ArrayList<String>(currSelectedIndices.length);
        for(int i: currSelectedIndices)
        {
            selectedGenes.add(probeIds[i]);
        }
        
        AddGeneListDialog dialog = new AddGeneListDialog(
                (JFrame)org.jax.util.gui.SwingUtilities.getContainingWindow(this),
                this.maanovaTestResult.getParentExperiment(),
                selectedGenes);
        dialog.pack();
        dialog.setVisible(true);
    }
    
    /**
     * Forget about the axis labeling and the zoom level
     */
    private void forgetGraphState()
    {
        this.chartConfigurationDialog.setChartTitle(
                "Volcano Plot for " + this.maanovaTestResult.toString());
        this.chartConfigurationDialog.setXAxisLabel("Fold Change");
        this.chartConfigurationDialog.setYAxisLabel(
                "-log10(" + this.getSelectedStatisticSubtype().toString() + ")");
        
        this.viewArea = null;
    }
    
    private void autoRangeChart()
    {
        this.viewArea = null;
        this.updateDataPoints();
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
                VolcanoPlotPanel.this.chartConfigurationDialog.setVisible(true);
            }
        });
        toolsMenu.add(configureGraphItem);
        toolsMenu.addSeparator();
        
        toolsMenu.add(new AbstractAction("Clear Selections")
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                VolcanoPlotPanel.this.setSelectedIndices(new int[0]);
            }
        });
        toolsMenu.addSeparator();
        
        ButtonGroup dragButtonGroup = new ButtonGroup();
        JCheckBoxMenuItem selectModeCheckBox = new JCheckBoxMenuItem("Drag Cursor to Select");
        selectModeCheckBox.setSelected(true);
        this.dragToSelect = true;
        selectModeCheckBox.addItemListener(new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                VolcanoPlotPanel.this.dragToSelect =
                    e.getStateChange() == ItemEvent.SELECTED;
            }
        });
        dragButtonGroup.add(selectModeCheckBox);
        toolsMenu.add(selectModeCheckBox);
        
        JCheckBoxMenuItem zoomModeCheckBox = new JCheckBoxMenuItem("Drag Cursor to Zoom");
        zoomModeCheckBox.addItemListener(new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                VolcanoPlotPanel.this.dragToZoom =
                    e.getStateChange() == ItemEvent.SELECTED;
            }
        });
        dragButtonGroup.add(zoomModeCheckBox);
        toolsMenu.add(zoomModeCheckBox);
        toolsMenu.addSeparator();
        
        toolsMenu.add(new AbstractAction("Zoom Out")
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                VolcanoPlotPanel.this.autoRangeChart();
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
                VolcanoPlotPanel.this.showTooltip =
                    e.getStateChange() == ItemEvent.SELECTED;
                VolcanoPlotPanel.this.clearProbePopup();
            }
        });
        toolsMenu.add(showTooltipCheckbox);
        toolsMenu.addSeparator();
        
        toolsMenu.add(this.displayTestResultsAction);
        toolsMenu.addSeparator();
        
        toolsMenu.add(this.saveSelectedPointsMenuItem);
        
        JMenu selectPointsFromLisMenu = new JMenu("Select Points From Gene List");
        List<String> geneListNames =
            this.maanovaTestResult.getParentExperiment().getGeneListNames();
        if(geneListNames.isEmpty())
        {
            JMenuItem noListsMenuItem = new JMenuItem("No Gene Lists Available");
            noListsMenuItem.setEnabled(false);
            selectPointsFromLisMenu.add(noListsMenuItem);
        }
        else
        {
            for(final String geneListName: geneListNames)
            {
                JMenuItem currGeneListMenuItem = new JMenuItem(
                        RUtilities.fromRIdentifierToReadableName(geneListName));
                currGeneListMenuItem.addActionListener(new ActionListener()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void actionPerformed(ActionEvent e)
                    {
                        VolcanoPlotPanel.this.selectedIndicesFromGeneList(
                                geneListName);
                    }
                });
                selectPointsFromLisMenu.add(currGeneListMenuItem);
            }
        }
        toolsMenu.add(selectPointsFromLisMenu);
        
        menuBar.add(toolsMenu);
        
        // the help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem helpMenuItem = new JMenuItem("Help...");
        Icon helpIcon = new ImageIcon(VolcanoPlotPanel.class.getResource(
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
                        "volcano-plot",
                        VolcanoPlotPanel.this);
            }
        });
        helpMenu.add(helpMenuItem);
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    private void selectedIndicesFromGeneList(String geneListName)
    {
        MicroarrayExperiment experiment =
            this.maanovaTestResult.getParentExperiment();
        this.setSelectedIndices(
            experiment.getIndicesForGeneListNamed(geneListName));
    }

    private void areaSelected(Rectangle2D area)
    {
        Rectangle2D chartArea = this.chartPanel.toChartRectangle(area);
        
        if(this.dragToSelect)
        {
            this.setSelectedIndices(this.getIndicesInArea(chartArea));
        }
        else if(this.dragToZoom)
        {
            this.viewArea = chartArea;
            this.updateDataPoints();
        }
    }
    
    private void setSelectedIndices(int[] selectedIndices)
    {
        selectedIndices = SequenceUtilities.uniqueInts(selectedIndices);
        if(!Arrays.equals(selectedIndices, this.selectedIndices))
        {
            this.selectedIndices = selectedIndices;
            this.saveSelectedPointsMenuItem.setEnabled(
                    selectedIndices != null && selectedIndices.length >= 1);
            
            this.updateDataPoints();
        }
    }
    
    /**
     * Getter for the indices that fall in the given area
     * @param area  the area (using chart coordinates)
     * @return  the indices
     */
    private int[] getIndicesInArea(Rectangle2D area)
    {
        XYProbeData xyData = this.getXYData();
        double[] xData = xyData.getXData();
        double[] yData = xyData.getYData();
        int[] probeIndices = xyData.getProbeIndices();
        
        int selectedCount = 0;
        int[] mySelectedIndices = new int[xData.length];
        for(int i = 0; i < xData.length; i++)
        {
            if(area.contains(xData[i], yData[i]))
            {
                mySelectedIndices[selectedCount] = probeIndices[i];
                selectedCount++;
            }
        }
        
        // trim the array to size
        if(selectedCount == mySelectedIndices.length)
        {
            return mySelectedIndices;
        }
        else
        {
            int[] trimmedArray = new int[selectedCount];
            for(int i = 0; i < trimmedArray.length; i++)
            {
                trimmedArray[i] = mySelectedIndices[i];
            }
            return trimmedArray;
        }
    }

    private void mouseMoved(MouseEvent e)
    {
        if(this.showTooltip)
        {
            Point2D chartPoint = this.chartPanel.toChartPoint(e.getPoint());
            
            // find the nearest probe
            XYProbeData xyProbeData = this.getXYData();
            double[][] xyData = new double[][] {
                    xyProbeData.getXData(),
                    xyProbeData.getYData()};
            int nearestDotIndex = PlotUtil.getNearestDataIndex(
                    xyData,
                    chartPoint.getX(),
                    chartPoint.getY());
            
            if(nearestDotIndex == -1)
            {
                this.clearProbePopup();
            }
            else
            {
                Point2D probeJava2DCoord =
                    this.getJava2DCoordinates(xyData, nearestDotIndex);
                double java2DDist = probeJava2DCoord.distance(e.getX(), e.getY());
                
                // is the probe close enough to be worth showing (in pixel distance)
                if(java2DDist <= PlotUtil.SCATTER_PLOT_DOT_SIZE_PIXELS * 2)
                {
                    this.showProbePopup(
                            xyProbeData.getProbeIndices()[nearestDotIndex],
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

    private void clearProbePopup()
    {
        if(this.toolTip.getParent() != null)
        {
            this.chartPanel.remove(this.toolTip);
            this.chartPanel.repaint();
        }
    }

    private void showProbePopup(int nearestProbeIndex, int pixelX, int pixelY)
    {
        if(this.toolTip.getParent() == null)
        {
            this.chartPanel.add(this.toolTip);
        }
        
        ProbesetRow nearestProbeset = this.maanovaTestResult.getProbesetRow(
                nearestProbeIndex,
                this.getSelectedTestNumber(),
                this.availableTestStatistics);
        
        if(nearestProbeset == null)
        {
            LOG.severe("Failed to lookup probeset data for index: " + nearestProbeIndex);
        }
        else
        {
            final String rowStart = "<tr><td>";
            final String rowStop = "</td></tr>";
            final String cellDelimiter = "</td><td>";
            StringBuilder tableRowsString = new StringBuilder("<html><table>");
            tableRowsString.append(rowStart);
            tableRowsString.append("ID:");
            tableRowsString.append(cellDelimiter);
            tableRowsString.append(nearestProbeset.getId());
            tableRowsString.append(rowStop);
            for(int i = 0; i < nearestProbeset.getValues().length; i++)
            {
                tableRowsString.append(rowStart);
                tableRowsString.append(this.availableTestStatistics[i].toString());
                tableRowsString.append(':');
                tableRowsString.append(cellDelimiter);
                tableRowsString.append(nearestProbeset.getValues()[i]);
            }
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

    private Point2D getJava2DCoordinates(double[][] xyData, int dotIndex)
    {
        final double graphX = xyData[0][dotIndex];
        final double graphY = xyData[1][dotIndex];
        
        final XYPlot plot = (XYPlot)this.chartPanel.getChart().getPlot();
        final ChartRenderingInfo renderingInfo = this.chartPanel.getChartRenderingInfo();
        
        return PlotUtil.toJava2DCoordinates(plot, renderingInfo, graphX, graphY);
    }

    /**
     * Getter for the selected test statistics
     * @return
     *          the selected test statistics
     */
    private MaanovaTestStatistics getSelectedTestStatistics()
    {
        return this.maanovaTestResult.getStatistics(
                this.getSelectedStatisticType());
    }
    
    private MaanovaTestStatisticType getSelectedStatisticType()
    {
        return (MaanovaTestStatisticType)this.statisticTypeComboBox.getSelectedItem();
    }
    
    private MaanovaTestStatisticSubtype getSelectedStatisticSubtype()
    {
        return (MaanovaTestStatisticSubtype)this.statisticSubtypeComboBox.getSelectedItem();
    }
    
    private int getSelectedTestNumber()
    {
        if(this.testNumberComboBox == null)
        {
            return 0;
        }
        else
        {
            return this.testNumberComboBox.getSelectedIndex();
        }
    }
    
    private void updateDataPoints()
    {
        this.cachedXYData = null;
        XYProbeData xyData = this.getXYData();
        
        DefaultXYDataset xyDataSet = new DefaultXYDataset();
        int[] selectedIndices = this.selectedIndices;
        if(selectedIndices.length == 0)
        {
            xyDataSet.addSeries(
                    "data",
                    new double[][] {xyData.getXData(), xyData.getYData()});
        }
        else
        {
            double[] xData = xyData.getXData();
            double[] yData = xyData.getYData();
            int[] dataIndices = xyData.getProbeIndices();
            
            double[] normalXData = new double[xData.length - selectedIndices.length];
            double[] normalYData = new double[xData.length - selectedIndices.length];
            
            double[] selectedXData = new double[selectedIndices.length];
            double[] selectedYData = new double[selectedIndices.length];
            
            int selectionIndex = 0;
            for(int i = 0; i < xData.length; i++)
            {
                if(selectionIndex < selectedIndices.length &&
                   dataIndices[i] == selectedIndices[selectionIndex])
                {
                    // this is one of the selected points
                    selectedXData[selectionIndex] = xData[i];
                    selectedYData[selectionIndex] = yData[i];
                    
                    selectionIndex++;
                }
                else
                {
                    // this is not a selected point
                    normalXData[i - selectionIndex] = xData[i];
                    normalYData[i - selectionIndex] = yData[i];
                }
            }
            
            xyDataSet.addSeries(
                    "data",
                    new double[][] {normalXData, normalYData});
            xyDataSet.addSeries(
                    "selected data",
                    new double[][] {selectedXData, selectedYData});
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
        xyPlot.setRenderer(PlotUtil.createSimpleScatterPlotRenderer());
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
            this.cachedXYData = this.createXYData(
                    this.getSelectedTestNumber(),
                    this.getSelectedTestStatistics(),
                    this.getSelectedStatisticSubtype());
        }
        
        return this.cachedXYData;
    }
    
    /**
     * Creates volcano plot data points from a JFreeChart {@link XYDataset}
     * from the given test statistics.
     * @param plotIndex
     *          the index of test that we want data points for
     * @param testStatistics
     *          the test statistics to extract from
     * @param testStatisticSubtype
     *          the subtype to extract from
     * @return
     *          the XY points for the volcano plot
     */
    private XYProbeData createXYData(
            int plotIndex,
            MaanovaTestStatistics testStatistics,
            MaanovaTestStatisticSubtype testStatisticSubtype)
    {
        Double[] objXValues =
            this.maanovaTestResult.getFoldChangeValues(plotIndex);
        Double[] objYValues =
            testStatistics.getValues(testStatisticSubtype, plotIndex);
        
        // check the array lengths which should be the same if everything is OK
        if(objXValues.length != objYValues.length)
        {
            throw new IllegalArgumentException(
                    "There is a missmatch between the number of X (" +
                    objXValues.length +
                    ") and Y (" + objYValues.length + ") values");
        }
        
        // first count all non-null pairings
        int nonNullCount = 0;
        for(int i = 0; i < objXValues.length; i++)
        {
            if(objXValues[i] != null && objYValues[i] != null)
            {
                nonNullCount++;
            }
        }
        
        if(nonNullCount != objXValues.length && LOG.isLoggable(Level.WARNING))
        {
            LOG.warning(
                    "Found " + (objXValues.length - nonNullCount) +
                    " NaN data points in the volcano plot data");
        }
        
        // OK, now convert to primitive arrays
        double[] primXValues = new double[nonNullCount];
        double[] primYValues = new double[nonNullCount];
        int[] probeIndices = new int[nonNullCount];
        int primitiveArraysIndex = 0;
        for(int objArraysIndex = 0; objArraysIndex < objXValues.length; objArraysIndex++)
        {
            if(objXValues[objArraysIndex] != null && objYValues[objArraysIndex] != null)
            {
                double yVal = objYValues[objArraysIndex];
                if(yVal < MIN_PVALUE_THRESHOLD)
                {
                    yVal = MIN_PVALUE_THRESHOLD;
                }
                primXValues[primitiveArraysIndex] = objXValues[objArraysIndex];
                primYValues[primitiveArraysIndex] = -Math.log10(yVal);
                probeIndices[primitiveArraysIndex] = objArraysIndex;
                
                primitiveArraysIndex++;
            }
        }
        
        return new XYProbeData(primXValues, primYValues, probeIndices);
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
        public XYProbeData(double[] xData, double[] yData, int[] probeIndices)
        {
            assert xData.length == yData.length;
            assert yData.length == probeIndices.length;
            
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
