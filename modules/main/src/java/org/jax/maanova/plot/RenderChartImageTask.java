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

import java.awt.image.BufferedImage;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.util.concurrent.SimpleLongRunningTask;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;

/**
 * A long running task that is useful for rendering chart images outside
 * of the AWT Event thread.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class RenderChartImageTask
extends SimpleLongRunningTask
implements Runnable
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            RenderChartImageTask.class.getName());
    
    private volatile JFreeChart chart;
    
    private volatile int width;
    
    private volatile int height;
    
    private final ChartRenderingInfo chartRenderingInfo;
    
    private final ArrayBlockingQueue<BufferedImage> bufferedImageQueue;
    
    private final ArrayBlockingQueue<Object> renderRequestQueue;
    
    private final ChartChangeListener chartChageListener = new ChartChangeListener()
    {
        /**
         * {@inheritDoc}
         */
        public void chartChanged(ChartChangeEvent event)
        {
            RenderChartImageTask.this.renderRequestQueue.offer(new Object());
        }
    };
    
    /**
     * Constructor
     * @param chartRenderingInfo
     *          the chart rendering info
     */
    public RenderChartImageTask(ChartRenderingInfo chartRenderingInfo)
    {
        this.chartRenderingInfo = chartRenderingInfo;
        this.bufferedImageQueue = new ArrayBlockingQueue<BufferedImage>(1);
        this.renderRequestQueue = new ArrayBlockingQueue<Object>(1);
    }
    
    /**
     * Setter for the chart
     * @param chart the chart
     */
    public void setChart(JFreeChart chart)
    {
        if(this.chart != null)
        {
            this.chart.removeChangeListener(this.chartChageListener);
        }
        
        this.chart = chart;
        
        if(chart != null)
        {
            chart.addChangeListener(this.chartChageListener);
        }
        
        this.renderRequestQueue.offer(new Object());
    }
    
    /**
     * Getter for the chart
     * @return the chart
     */
    public JFreeChart getChart()
    {
        return this.chart;
    }
    
    /**
     * Update the image size
     * @param width
     *          the new width
     * @param height
     *          the new height
     */
    public void setSize(int width, int height)
    {
        this.width = width;
        this.height = height;
        
        this.renderRequestQueue.offer(new Object());
    }
    
    /**
     * {@inheritDoc}
     */
    public void run()
    {
        try
        {
            while(true)
            {
                // wait until the chart needs to be updated
                this.renderRequestQueue.take();
                
                if(this.getWorkUnitsCompleted() == 1)
                {
                    // each render cycle is a new work unit
                    this.setTotalWorkUnits(1);
                    this.setWorkUnitsCompleted(0);
                }
                
                // get a snapshot of the updated chart width and height
                JFreeChart tmpChart = this.chart;
                int tmpWidth = this.width;
                int tmpHeight = this.height;
                
                // if any values are bad leave the buffered image as null
                BufferedImage bi = null;
                if(tmpChart != null && tmpWidth > 0 && tmpHeight > 0)
                {
                    bi = tmpChart.createBufferedImage(
                            tmpWidth,
                            tmpHeight,
                            this.chartRenderingInfo);
                }
                
                if(bi != null)
                {
                    // clear any old image before putting our shiny new image
                    this.bufferedImageQueue.poll();
                    this.bufferedImageQueue.put(bi);
                }
                
                // don't bother setting work to complete if we know there
                // is a pending request in the queue
                if(this.renderRequestQueue.isEmpty())
                {
                    this.setWorkUnitsCompleted(1);
                }
            }
        }
        catch(InterruptedException ex)
        {
            LOG.log(Level.SEVERE,
                    "Error rendering chart image",
                    ex);
        }
    }
    
    /**
     * Getter for the next image. This function blocks until there is an
     * updated image to get
     * @return
     *          the next buffered image
     */
    public BufferedImage getNextImage()
    {
        try
        {
            return this.bufferedImageQueue.take();
        }
        catch(InterruptedException ex)
        {
            LOG.log(Level.SEVERE,
                    "Error getting image from buffered image queue",
                    ex);
            return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getTaskName()
    {
        return "Rendering Graph Image";
    }
}