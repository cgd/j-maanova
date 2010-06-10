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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jax.r.RAssignmentCommand;
import org.jax.r.RCommand;
import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RUtilities;

/**
 * Command builder for running the justRMA command from the affy package
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class AffyJustRMACommandBuilder
implements AffyReadDataCommandBuilderInterface, AffyRMACommandBuilderInterface
{
    private static final String METHOD_NAME = "justRMA";
    
    private volatile boolean doQuantileNormalization = true;
    
    private volatile boolean doBackgroundCorrection = true;
    
    private volatile String resultObjectName = null;
    
    private volatile String[] celFiles = new String[0];
    
    private volatile String cdfFile = "";
    
    private volatile boolean celFilesCompressed = false;
    
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
     * Determines if background correction should be performed
     * @return true if we should do background correction
     */
    public boolean getDoBackgroundCorrection()
    {
        return this.doBackgroundCorrection;
    }
    
    /**
     * @see #getDoBackgroundCorrection()
     * @param doBackgroundCorrection the doBackgroundCorrection to set
     */
    public void setDoBackgroundCorrection(boolean doBackgroundCorrection)
    {
        this.doBackgroundCorrection = doBackgroundCorrection;
    }
    
    /**
     * Determines if we should do quantile normalization on the affy data or
     * not
     * @return the doQuantileNormalization
     */
    public boolean getDoQuantileNormalization()
    {
        return this.doQuantileNormalization;
    }
    
    /**
     * @see #getDoQuantileNormalization()
     * @param doQuantileNormalization the doQuantileNormalization to set
     */
    public void setDoQuantileNormalization(boolean doQuantileNormalization)
    {
        this.doQuantileNormalization = doQuantileNormalization;
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
        
        RMethodInvocationCommand methodInvocationCommand = new RMethodInvocationCommand(
                METHOD_NAME,
                commandParameters);
        
        String resultObjectName = this.resultObjectName;
        if(resultObjectName == null || resultObjectName.trim().length() == 0)
        {
            return methodInvocationCommand;
        }
        else
        {
            return new RAssignmentCommand(
                    resultObjectName.trim(),
                    methodInvocationCommand.getCommandText());
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
            celFiles = celFiles.clone();
            File firstCelFile = new File(celFiles[0]);
            String firstParentDirName = firstCelFile.getParent();
            final String celFilePath;
            if(this.allStartWith(celFiles, firstParentDirName))
            {
                // simplify the dirs by factoring out the common parent dir
                for(int i = 0; i < celFiles.length; i++)
                {
                    celFiles[i] = new File(celFiles[i]).getName();
                }
                celFilePath = RUtilities.javaStringToRString(firstParentDirName);
            }
            else
            {
                celFilePath = "NULL";
            }
            
            String celFilesRExpression = RUtilities.stringArrayToRVector(celFiles);
            parameters.add(new RCommandParameter(
                    "filenames",
                    celFilesRExpression));
            
            parameters.add(new RCommandParameter("celfile.path", celFilePath));
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
        
        parameters.add(new RCommandParameter(
                "normalize",
                RUtilities.javaBooleanToRBoolean(this.doQuantileNormalization)));
        
        parameters.add(new RCommandParameter(
                "background",
                RUtilities.javaBooleanToRBoolean(this.doBackgroundCorrection)));
        
        return parameters;
    }

    private boolean allStartWith(String[] strings, String startStr)
    {
        for(String string: strings)
        {
            if(!string.startsWith(startStr))
            {
                return false;
            }
        }
        
        return true;
    }
}
