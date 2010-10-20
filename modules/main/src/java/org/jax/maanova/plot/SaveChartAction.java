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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

import org.jax.maanova.Maanova;
import org.jax.maanova.configuration.MaanovaApplicationConfigurationManager;
import org.jax.maanova.jaxbgenerated.JMaanovaApplicationState;
import org.jax.r.jaxbgenerated.FileType;
import org.jax.r.jaxbgenerated.ObjectFactory;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.io.PngFileFilter;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

/**
 * An action for saving a JFreeChart image
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SaveChartAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -5295404410662584864L;

    private static final Logger LOG = Logger.getLogger(
            SaveChartAction.class.getName());
    
    private volatile JFreeChart chart;
    
    private volatile Dimension size;
    
    /**
     * Constructor
     */
    public SaveChartAction()
    {
        super("Save Graph as Image ...",
              new ImageIcon(SaveChartAction.class.getResource(
                      "/images/action/export-image-16x16.png")));
        
        this.setEnabled(false);
    }
    
    /**
     * Setter for the chart
     * @param chart the chart to set
     */
    public void setChart(JFreeChart chart)
    {
        this.chart = chart;
        this.updateEnabled();
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
     * Setter for the size
     * @param size the size to set
     */
    public void setSize(Dimension size)
    {
        this.size = size;
        this.updateEnabled();
    }
    
    /**
     * Getter for the size
     * @return the size
     */
    public Dimension getSize()
    {
        return this.size;
    }
    
    private void updateEnabled()
    {
        this.setEnabled(this.chart != null && this.size != null);
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        JFreeChart myChart = this.chart;
        Dimension mySize = this.size;
        
        if(myChart == null || mySize == null)
        {
            LOG.severe("Failed to save graph image because of a null value");
            MessageDialogUtilities.errorLater(
                    Maanova.getInstance().getApplicationFrame(),
                    "Internal error: Failed to save graph image.",
                    "Image Save Failed");
        }
        else
        {
            // use the remembered starting dir
            MaanovaApplicationConfigurationManager configurationManager =
                MaanovaApplicationConfigurationManager.getInstance();
            JMaanovaApplicationState applicationState =
                configurationManager.getApplicationState();
            FileType rememberedJaxbImageDir =
                applicationState.getRecentImageExportDirectory();
            File rememberedImageDir = null;
            if(rememberedJaxbImageDir != null && rememberedJaxbImageDir.getFileName() != null)
            {
                rememberedImageDir = new File(rememberedJaxbImageDir.getFileName());
            }
            
            // select the image file to save
            JFileChooser fileChooser = new JFileChooser(rememberedImageDir);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setApproveButtonText("Save Graph");
            fileChooser.setDialogTitle("Save Graph as Image");
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.addChoosableFileFilter(
                    PngFileFilter.getInstance());
            fileChooser.setFileFilter(
                    PngFileFilter.getInstance());
            int response = fileChooser.showSaveDialog(
                    Maanova.getInstance().getApplicationFrame());
            if(response == JFileChooser.APPROVE_OPTION)
            {
                File selectedFile = fileChooser.getSelectedFile();
                
                // tack on the extension if there isn't one
                // already
                if(!PngFileFilter.getInstance().accept(selectedFile))
                {
                    String newFileName =
                        selectedFile.getName() + "." +
                        PngFileFilter.PNG_EXTENSION;
                    selectedFile =
                        new File(selectedFile.getParentFile(), newFileName);
                }
                
                if(selectedFile.exists())
                {
                    // ask the user if they're sure they want to overwrite
                    String message =
                        "Exporting the graph image to " +
                        selectedFile.getAbsolutePath() + " will overwrite an " +
                        " existing file. Would you like to continue anyway?";
                    if(LOG.isLoggable(Level.FINE))
                    {
                        LOG.fine(message);
                    }
                    
                    boolean overwriteOk = MessageDialogUtilities.confirmOverwrite(
                            Maanova.getInstance().getApplicationFrame(),
                            selectedFile);
                    if(!overwriteOk)
                    {
                        if(LOG.isLoggable(Level.FINE))
                        {
                            LOG.fine("overwrite canceled");
                        }
                        return;
                    }
                }
                
                try
                {
                    ChartUtilities.saveChartAsPNG(
                            selectedFile,
                            myChart,
                            mySize.width,
                            mySize.height);
                    
                    File parentDir = selectedFile.getParentFile();
                    if(parentDir != null)
                    {
                        // update the "recent image directory"
                        ObjectFactory objectFactory = new ObjectFactory();
                        FileType latestJaxbImageDir = objectFactory.createFileType();
                        latestJaxbImageDir.setFileName(
                                parentDir.getAbsolutePath());
                        applicationState.setRecentImageExportDirectory(
                                latestJaxbImageDir);
                    }
                }
                catch(Exception ex)
                {
                    LOG.log(Level.SEVERE,
                            "failed to save graph image",
                            ex);
                }
            }
        }
    }
}
