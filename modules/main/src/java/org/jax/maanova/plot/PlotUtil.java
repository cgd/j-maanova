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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;

/**
 * Some utility functions for plotting with JFreeChart
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class PlotUtil
{
    /**
     * the size in pixels of scatter plot dots
     */
    public static final int SCATTER_PLOT_DOT_SIZE_PIXELS = 5;
    
    /**
     * Create a simple XY renderer which can be used for scatter plots
     * @return  the renderer
     */
    public static XYLineAndShapeRenderer createSimpleScatterPlotRenderer()
    {
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(
                false,
                true);
        
        renderer.setAutoPopulateSeriesShape(false);
        renderer.setAutoPopulateSeriesOutlinePaint(false);
        renderer.setAutoPopulateSeriesOutlineStroke(false);
        renderer.setAutoPopulateSeriesPaint(false);
        
        renderer.setBaseShape(new Ellipse2D.Float(
                -PlotUtil.SCATTER_PLOT_DOT_SIZE_PIXELS / 2F,
                -PlotUtil.SCATTER_PLOT_DOT_SIZE_PIXELS / 2F,
                PlotUtil.SCATTER_PLOT_DOT_SIZE_PIXELS,
                PlotUtil.SCATTER_PLOT_DOT_SIZE_PIXELS));
        
        renderer.setUseOutlinePaint(true);
        renderer.setBaseOutlinePaint(Color.BLACK);
        renderer.setBaseOutlineStroke(new BasicStroke(0.25F));
        
        renderer.setSeriesPaint(0, new Color(0x55, 0x55, 0xFF)); // blue
        renderer.setSeriesPaint(1, new Color(0xFF, 0x55, 0x55)); // red
        
        return renderer;
    }
    
    /**
     * Create a simple monochrome XY renderer which can be used for scatter plots
     * @return  the renderer
     */
    public static XYLineAndShapeRenderer createMonochromeScatterPlotRenderer()
    {
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(
                false,
                true);
        
        renderer.setAutoPopulateSeriesShape(false);
        renderer.setAutoPopulateSeriesOutlinePaint(false);
        renderer.setAutoPopulateSeriesOutlineStroke(false);
        renderer.setAutoPopulateSeriesPaint(false);
        
        renderer.setBaseShape(new Ellipse2D.Float(
                -PlotUtil.SCATTER_PLOT_DOT_SIZE_PIXELS / 2F,
                -PlotUtil.SCATTER_PLOT_DOT_SIZE_PIXELS / 2F,
                PlotUtil.SCATTER_PLOT_DOT_SIZE_PIXELS,
                PlotUtil.SCATTER_PLOT_DOT_SIZE_PIXELS));
        
        renderer.setUseOutlinePaint(true);
        renderer.setBaseOutlinePaint(Color.BLACK);
        renderer.setBaseOutlineStroke(new BasicStroke(0.25F));
        
        renderer.clearSeriesPaints(false);
        renderer.setBasePaint(new Color(0x55, 0x55, 0xFF)); // blue
        
        return renderer;
    }
    
    /**
     * Convert from graph coordinates to Java2D coordinates
     * @param plot
     *          the plot to use
     * @param renderingInfo
     *          the rendering info to use
     * @param graphX
     *          the graph X position to convert
     * @param graphY
     *          the graph Y position to convert
     * @return
     *          the point in Java2D coordinate space
     */
    public static Point2D toJava2DCoordinates(
            XYPlot plot,
            ChartRenderingInfo renderingInfo,
            double graphX,
            double graphY)
    {
        final Rectangle2D dataArea = renderingInfo.getPlotInfo().getDataArea();
        
        final double java2DX = plot.getDomainAxis().valueToJava2D(
                graphX,
                dataArea,
                plot.getDomainAxisEdge());
        final double java2DY = plot.getRangeAxis().valueToJava2D(
                graphY,
                dataArea,
                plot.getRangeAxisEdge());
        
        return new Point2D.Double(java2DX, java2DY);
    }

    /**
     * Get the index of the data point nearest to graphX, graphY.
     * @param xyData
     *          the data points to search through
     * @param graphX
     *          the reference X point
     * @param graphY
     *          the reference Y point
     * @return
     *          the index of the nearest point or -1 if there is no such point
     */
    public static int getNearestDataIndex(
            double[][] xyData,
            double graphX,
            double graphY)
    {
        int nearestIndex = -1;
        
        final double[] xData = xyData[0];
        final double[] yData = xyData[1];
        
        double nearestDistSq = Double.POSITIVE_INFINITY;
        for(int i = 0; i < xData.length; i++)
        {
            double currDistSq =
                Point2D.distanceSq(graphX, graphY, xData[i], yData[i]);
            if(currDistSq < nearestDistSq)
            {
                nearestDistSq = currDistSq;
                nearestIndex = i;
            }
        }
        
        return nearestIndex;
    }
    
    /**
     * Rescales the XY plot to match the viewing area
     * @param viewArea
     *          the viewing area (null means we should use autorange)
     * @param plot
     *          the plot to rescale
     */
    public static void rescaleXYPlot(final Rectangle2D viewArea, final XYPlot plot)
    {
        if(viewArea == null)
        {
            plot.getDomainAxis().setAutoRange(true);
            plot.getRangeAxis().setAutoRange(true);
        }
        else
        {
            plot.getDomainAxis().setRange(
                    new Range(viewArea.getMinX(), viewArea.getMaxX()),
                    true,
                    false);
            plot.getRangeAxis().setRange(
                    new Range(viewArea.getMinY(), viewArea.getMaxY()),
                    true,
                    true);
        }
    }
}
