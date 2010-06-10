/*
 * Copyright (c) 2008 The Jackson Laboratory
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

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.jax.maanova.Maanova;
import org.jax.maanova.configuration.MaanovaApplicationConfigurationManager;
import org.jax.maanova.madata.MicroarrayExperiment;
import org.jax.maanova.madata.MicroarrayExperimentDesign;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.io.CommonFlatFileFormat;
import org.jax.util.io.FileChooserExtensionFilter;
import org.jax.util.io.FlatFileWriter;

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ExportDataAction extends AbstractAction
{
    private static final Logger LOG = Logger.getLogger(ExportDataAction.class.getName());
    
    private static final String PROBESET_ID_HEADER_STRING = "Probeset ID";
    
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -2517468806402896894L;
    
    private final MicroarrayExperiment experiment;

    /**
     * Constructor
     * @param name              the name
     * @param experiment        the experiment to export
     */
    public ExportDataAction(
            final String name,
            final MicroarrayExperiment experiment)
    {
        super(name);
        
        this.experiment = experiment;
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        JFrame parentFrame = Maanova.getInstance().getApplicationFrame();
        
        MaanovaApplicationConfigurationManager manager =
            MaanovaApplicationConfigurationManager.getInstance();
        
        JFileChooser fileChooser = new JFileChooser(
                manager.getStartingDataDirectory());
        fileChooser.setDialogTitle("Export Data to CSV");
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileChooserExtensionFilter(
                "csv",
                "Comma-Separated Values"));
        int response = fileChooser.showSaveDialog(parentFrame);
        if(response == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            if(selectedFile != null &&
               (!selectedFile.exists() ||
               MessageDialogUtilities.confirmOverwrite(parentFrame, selectedFile)))
            {
                manager.setStartingDataDirectory(selectedFile.getParentFile());
                
                try
                {
                    int dyeCount = this.experiment.getDyeCount();
                    MicroarrayExperimentDesign design = this.experiment.getDesign();
                    String[] arrays = design.getColumnNamed(MicroarrayExperimentDesign.ARRAY_COL_NAME);
                    final String[] currRow = new String[arrays.length + 1];
                    currRow[0] = PROBESET_ID_HEADER_STRING;
                    if(dyeCount >= 2)
                    {
                        String[] dyes = design.getColumnNamed(MicroarrayExperimentDesign.DYE_COL_NAME);
                        for(int i = 0; i < arrays.length; i++)
                        {
                            currRow[i + 1] = arrays[i] + "-" + dyes[i];
                        }
                    }
                    else
                    {
                        for(int i = 0; i < arrays.length; i++)
                        {
                            currRow[i + 1] = arrays[i];
                        }
                    }
                    
                    FlatFileWriter writer = new FlatFileWriter(
                            new BufferedWriter(new FileWriter(selectedFile)),
                            CommonFlatFileFormat.CSV_UNIX);
                    writer.writeRow(currRow);
                    
                    String[] probeIds = this.experiment.getProbesetIds();
                    for(int probeIndex = 0; probeIndex < probeIds.length; probeIndex++)
                    {
                        currRow[0] = probeIds[probeIndex];
                        
                        Double[] probeVals = this.experiment.getDataRow(probeIndex);
                        for(int i = 0; i < probeVals.length; i++)
                        {
                            currRow[i + 1] = probeVals[i] == null ? "" : probeVals[i].toString();
                        }
                        
                        writer.writeRow(currRow);
                    }
                    
                    writer.flush();
                    writer.close();
                }
                catch(IOException ex)
                {
                    String titleString = "Error Writing Table";
                    LOG.log(Level.SEVERE,
                            titleString,
                            ex);
                    MessageDialogUtilities.errorLater(
                            parentFrame,
                            ex.getMessage(),
                            titleString);
                }
            }
        }
    }
}
