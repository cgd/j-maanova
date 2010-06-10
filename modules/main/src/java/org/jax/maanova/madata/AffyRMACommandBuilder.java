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
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class AffyRMACommandBuilder implements AffyRMACommandBuilderInterface
{
    private static final String METHOD_NAME = "rma";
    
    private volatile boolean doQuantileNormalization = true;
    
    private volatile boolean doBackgroundCorrection = true;
    
    private volatile String affyBatchAccessor = null;

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
     * Getter for the accessor string used to get to the AffyBatch
     * object that the RMA command expects
     * @return the affyBatchAccessorString
     */
    public String getAffyBatchAccessor()
    {
        return this.affyBatchAccessor;
    }
    
    /**
     * @see #getAffyBatchAccessor()
     * @param affyBatchAccessorString the affyBatchAccessorString to set
     */
    public void setAffyBatchAccessor(String affyBatchAccessorString)
    {
        this.affyBatchAccessor = affyBatchAccessorString;
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
        
        String affyBatchAccessor = this.affyBatchAccessor;
        if(affyBatchAccessor != null && affyBatchAccessor.length() >= 1)
        {
            parameters.add(new RCommandParameter(affyBatchAccessor));
        }
        
        parameters.add(new RCommandParameter(
                "normalize",
                RUtilities.javaBooleanToRBoolean(this.doQuantileNormalization)));
        
        parameters.add(new RCommandParameter(
                "background",
                RUtilities.javaBooleanToRBoolean(this.doBackgroundCorrection)));
        
        return parameters;
    }
}
