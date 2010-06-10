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

package org.jax.maanova.madata;

import java.util.ArrayList;
import java.util.List;

import org.jax.r.RAssignmentCommand;
import org.jax.r.RCommand;
import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RUtilities;

/**
 * A command builder for reading affymetrix data
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class AffyReadDataCommandBuilder implements AffyReadDataCommandBuilderInterface
{
    private static final String METHOD_NAME = "read.affybatch";
    
    private volatile String[] celFiles = new String[0];
    
    private volatile String cdfFile = "";
    
    private volatile boolean celFilesCompressed = false;
    
    private volatile String resultObjectName = null;
    
    /**
     * Getter for the name that should be used for the resulting data object
     * @return the name to use
     */
    public String getResultObjectName()
    {
        return this.resultObjectName;
    }
    
    /**
     * @see #getResultObjectName()
     * @param resultObjectName the name
     */
    public void setResultObjectName(String resultObjectName)
    {
        this.resultObjectName = resultObjectName;
    }
    
    /**
     * Getter for the CEL file names
     * @return the celFiles
     */
    public String[] getCelFiles()
    {
        return this.celFiles;
    }
    
    /**
     * Setter for the CEL file names
     * @param celFiles the celFiles to set
     */
    public void setCelFiles(String[] celFiles)
    {
        this.celFiles = celFiles;
    }
    
    /**
     * Getter for the CDF file name
     * @return the cdfFile
     */
    public String getCdfFile()
    {
        return this.cdfFile;
    }
    
    /**
     * Setter for the CDF file name
     * @param cdfFile the cdfFile to set
     */
    public void setCdfFile(String cdfFile)
    {
        this.cdfFile = cdfFile;
    }
    
    /**
     * Determine if the CEL files are compressed or not
     * @return true iff they're compressed
     */
    public boolean getCelFilesCompressed()
    {
        return this.celFilesCompressed;
    }
    
    /**
     * Determines if the CEL files are treated as compressed or not
     * @param celFilesCompressed the celFilesCompressed to set
     */
    public void setCelFilesCompressed(boolean celFilesCompressed)
    {
        this.celFilesCompressed = celFilesCompressed;
    }
    
    /**
     * Get the command for the current settings
     * @return
     *          the command
     */
    public RCommand getCommand()
    {
        List<RCommandParameter> commandParameters =
            this.getCommandParameters();
        
        RMethodInvocationCommand readAffyMethodCommand = new RMethodInvocationCommand(
                METHOD_NAME,
                commandParameters);
        
        String resultObjectName = this.resultObjectName;
        if(resultObjectName == null)
        {
            return readAffyMethodCommand;
        }
        else
        {
            return new RAssignmentCommand(
                    resultObjectName.trim(),
                    readAffyMethodCommand.getCommandText());
        }
    }
    
    /**
     * Get the parameters for the current settings
     * @return
     *          the parameter list
     */
    private List<RCommandParameter> getCommandParameters()
    {
        List<RCommandParameter> parameters = new ArrayList<RCommandParameter>();
        
        String[] celFiles = this.celFiles;
        if(celFiles != null && celFiles.length >= 1)
        {
            String celFilesRExpression =
                RUtilities.stringArrayToRVector(celFiles);
            parameters.add(new RCommandParameter(
                    "filenames",
                    celFilesRExpression));
        }
        
        String cdfFile = this.cdfFile;
        if(cdfFile != null && cdfFile.length() >= 1)
        {
            parameters.add(new RCommandParameter(
                    "cdfname",
                    RUtilities.javaStringToRString(this.cdfFile)));
        }
        
        if(this.celFilesCompressed)
        {
            parameters.add(new RCommandParameter(
                    "compress",
                    RUtilities.javaBooleanToRBoolean(true)));
        }
        
        return parameters;
    }
}
