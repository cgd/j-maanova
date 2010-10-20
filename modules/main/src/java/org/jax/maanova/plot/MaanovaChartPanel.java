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

package org.jax.maanova.plot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JPanel;

import org.jax.maanova.Maanova;
import org.jax.util.concurrent.MultiTaskProgressPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;

/**
 * A chart panel class with some extra functionality for J/Maanova
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MaanovaChartPanel extends JPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -2265706893499823906L;
    
    private Rectangle dragRectangle = null;
    
    private MouseListener selfMouseListener = new MouseAdapter()
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void mousePressed(MouseEvent e)
        {
            MaanovaChartPanel.this.myMousePressed(e);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseReleased(MouseEvent e)
        {
            MaanovaChartPanel.this.myMouseReleased(e);
        }
    };
    
    private MouseMotionListener selfMotionListener = new MouseMotionAdapter()
    {
        @Override
        public void mouseDragged(MouseEvent e)
        {
            MaanovaChartPanel.this.myMouseDragged(e);
        }
    };
    
    private final Runnable getNextChartRunnable = new Runnable()
    {
        public void run()
        {
            while(true)
            {
                MaanovaChartPanel.this.chartImage =
                    MaanovaChartPanel.this.renderChartImageTask.getNextImage();
                MaanovaChartPanel.this.repaint();
            }
        }
    };
    
    private final ConcurrentLinkedQueue<AreaSelectionListener> areaSelectionListeners =
        new ConcurrentLinkedQueue<AreaSelectionListener>();
    
    private final ChartRenderingInfo chartRenderingInfo;
    
    private final RenderChartImageTask renderChartImageTask;

    private final ComponentListener selfComponentListener =
        new ComponentAdapter()
    {
        @Override
        public void componentResized(ComponentEvent e)
        {
            MaanovaChartPanel.this.selfResized();
        }
    };
    
    private volatile BufferedImage chartImage = null;
    
    private static final Color DEFAULT_SELECTION_RECTANGLE_COLOR =
        Color.RED;
    
    /**
     * @see #getSelectionRectangleColor()
     */
    private final Color selectionRectangleColor =
        DEFAULT_SELECTION_RECTANGLE_COLOR;
    
    /**
     * Same as the border color except translucent
     */
    private static final Color DEFAULT_SELECTION_RECTANGLE_FILL_COLOR = new Color(
            DEFAULT_SELECTION_RECTANGLE_COLOR.getRed(),
            DEFAULT_SELECTION_RECTANGLE_COLOR.getGreen(),
            DEFAULT_SELECTION_RECTANGLE_COLOR.getBlue(),
            DEFAULT_SELECTION_RECTANGLE_COLOR.getAlpha() / 16);

    /**
     * @see #getSelectionRectangleFillColor()
     */
    private final Color selectionRectangleFillColor =
        DEFAULT_SELECTION_RECTANGLE_FILL_COLOR;
    
    /**
     * Constructor
     */
    public MaanovaChartPanel()
    {
        this.chartRenderingInfo = new ChartRenderingInfo(null);
        this.renderChartImageTask = new RenderChartImageTask(
                this.chartRenderingInfo);
        this.renderChartImageTask.setSize(this.getWidth(), this.getHeight());
        
        this.addMouseListener(this.selfMouseListener);
        this.addMouseMotionListener(this.selfMotionListener);
        this.addComponentListener(this.selfComponentListener);
        
        Thread renderChartThread = new Thread(this.renderChartImageTask);
        renderChartThread.start();
        
        Thread getChartThread = new Thread(this.getNextChartRunnable);
        getChartThread.start();
        
        getProgressPanel().addTaskToTrack(this.renderChartImageTask);
    }
    
    /**
     * getter for the selection rectangle color
     * @return the selectionRectangleColor
     */
    public Color getSelectionRectangleColor()
    {
        return this.selectionRectangleColor;
    }
    
    /**
     * Get the fill color for the selection rectangle
     * @return the selectionRectangleFillColor
     */
    public Color getSelectionRectangleFillColor()
    {
        return this.selectionRectangleFillColor;
    }
    
    private static MultiTaskProgressPanel getProgressPanel()
    {
        return Maanova.getInstance().getApplicationFrame().getTaskProgressPanel();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void finalize() throws Throwable
    {
        getProgressPanel().removeTaskToTrack(this.renderChartImageTask);
    }
    
    private void selfResized()
    {
        this.renderChartImageTask.setSize(this.getWidth(), this.getHeight());
    }

    /**
     * Getter for the chart
     * @return the chart
     */
    public JFreeChart getChart()
    {
        return this.renderChartImageTask.getChart();
    }
    
    /**
     * Setter for the chart
     * @param chart the chart to set
     */
    public synchronized void setChart(JFreeChart chart)
    {
        this.renderChartImageTask.setChart(chart);
    }
    
    /**
     * Getter for the chart rendering info
     * @return the rendering info
     */
    public ChartRenderingInfo getChartRenderingInfo()
    {
        return this.chartRenderingInfo;
    }

    private void myMouseReleased(MouseEvent e)
    {
        if(this.dragRectangle != null &&
           this.dragRectangle.width != 0 &&
           this.dragRectangle.height != 0)
        {
            this.fireAreaSelectionEvent(this.dragRectangle);
        }
        
        this.dragRectangle = null;
        this.repaint();
    }

    private void myMouseDragged(MouseEvent e)
    {
        if(this.dragRectangle != null)
        {
            this.dragRectangle.width = e.getX() - this.dragRectangle.x;
            this.dragRectangle.height = e.getY() - this.dragRectangle.y;
            
            this.repaint();
        }
    }

    private void myMousePressed(MouseEvent e)
    {
        this.dragRectangle = new Rectangle(e.getX(), e.getY(), 0, 0);
    }
    
    /**
     * Adds an area selection listener
     * @param listener
     *          the listener
     */
    public void addAreaSelectionListener(AreaSelectionListener listener)
    {
        this.areaSelectionListeners.add(listener);
    }
    
    /**
     * Removes the given listener from the area selection listener list
     * @param listener
     *          the listener to remove
     */
    public void removeAreaSelectionListener(AreaSelectionListener listener)
    {
        this.areaSelectionListeners.remove(listener);
    }
    
    private void fireAreaSelectionEvent(Rectangle2D selectionRect)
    {
        Iterator<AreaSelectionListener> iter =
            this.areaSelectionListeners.iterator();
        while(iter.hasNext())
        {
            iter.next().areaSelected(selectionRect);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D)g;
        if(this.chartImage != null)
        {
            if(this.chartImage.getWidth() == this.getWidth() &&
               this.chartImage.getHeight() == this.getHeight())
            {
                g2.drawImage(this.chartImage, 0, 0, this);
            }
            else
            {
                g2.drawImage(
                        this.chartImage,
                        0,
                        0,
                        this.getWidth(),
                        this.getHeight(),
                        this.getBackground(),
                        this);
            }
        }
        
        if(this.dragRectangle != null)
        {
            this.renderDragRectangle(g2);
        }
    }
    /**
     * Render the selection rectangle
     * @param graphics2D
     *          the graphics context to render to
     */
    protected synchronized void renderDragRectangle(Graphics2D graphics2D)
    {
        Rectangle2D nonNegativeSelectionRectangle =
            MaanovaChartPanel.toNonNegativeWidthHeightRectangle(
                    this.dragRectangle);
        graphics2D.setColor(this.getSelectionRectangleFillColor());
        graphics2D.fill(nonNegativeSelectionRectangle);
        graphics2D.setColor(this.getSelectionRectangleColor());
        graphics2D.draw(nonNegativeSelectionRectangle);
    }

    /**
     * Convert a rectangle with negative width or height to one with
     * positive width and height
     * @param rectangle
     *          the rectangle that might have negatives
     * @return
     *          the positive version, or the same instance that was passed
     *          in if it's already positive
     */
    protected static Rectangle2D toNonNegativeWidthHeightRectangle(
            Rectangle2D rectangle)
    {
        if(rectangle.getWidth() < 0 || rectangle.getHeight() < 0)
        {
            final double x;
            final double y;
            final double width;
            final double height;
            
            if(rectangle.getWidth() < 0)
            {
                width = -rectangle.getWidth();
                x = rectangle.getX() + rectangle.getWidth();
            }
            else
            {
                width = rectangle.getWidth();
                x = rectangle.getX();
            }
            
            if(rectangle.getHeight() < 0)
            {
                height = -rectangle.getHeight();
                y = rectangle.getY() + rectangle.getHeight();
            }
            else
            {
                height = rectangle.getHeight();
                y = rectangle.getY();
            }
            
            return new Rectangle2D.Double(x, y, width, height);
        }
        else
        {
            // the rectangle that we have is OK
            return rectangle;
        }
    }
    
    /**
     * Convert the given point in java2d coordinates to chart coordinates.
     * @param java2DPoint the point to convert
     * @return the converted point
     * @throws ClassCastException the plot isn't an XYPlot
     */
    public Point2D toChartPoint(Point2D java2DPoint) throws ClassCastException
    {
        XYPlot plot = this.getChart().getXYPlot();
        Rectangle2D dataArea = this.chartRenderingInfo.getPlotInfo().getDataArea();
        
        double graphX = plot.getDomainAxis().java2DToValue(
                java2DPoint.getX(),
                dataArea,
                plot.getDomainAxisEdge());
        double graphY = plot.getRangeAxis().java2DToValue(
                java2DPoint.getY(),
                dataArea,
                plot.getRangeAxisEdge());
        
        return new Point2D.Double(graphX, graphY);
    }
    
    /**
     * Convert the given rectangle in java2d coordinates to chart coordinates.
     * @param java2DRectangle the rectangle to convert
     * @return the converted rectangle
     * @throws ClassCastException the plot isn't an XYPlot
     */
    public Rectangle2D toChartRectangle(Rectangle2D java2DRectangle) throws ClassCastException
    {
        XYPlot plot = this.getChart().getXYPlot();
        Rectangle2D dataArea = this.chartRenderingInfo.getPlotInfo().getDataArea();
        
        double x1 = plot.getDomainAxis().java2DToValue(
                java2DRectangle.getMinX(),
                dataArea,
                plot.getDomainAxisEdge());
        double y1 = plot.getRangeAxis().java2DToValue(
                java2DRectangle.getMinY(),
                dataArea,
                plot.getRangeAxisEdge());
        double x2 = plot.getDomainAxis().java2DToValue(
                java2DRectangle.getMaxX(),
                dataArea,
                plot.getDomainAxisEdge());
        double y2 = plot.getRangeAxis().java2DToValue(
                java2DRectangle.getMaxY(),
                dataArea,
                plot.getRangeAxisEdge());
        
        return toNonNegativeWidthHeightRectangle(new Rectangle2D.Double(
                x1,
                y1,
                x2 - x1,
                y2 - y1));
    }
}
