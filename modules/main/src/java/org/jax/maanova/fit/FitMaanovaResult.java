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

package org.jax.maanova.fit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.maanova.madata.MicroarrayExperiment;
import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RSyntaxException;
import org.jax.r.RUtilities;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RObject;
import org.jax.r.jriutilities.SilentRCommand;
import org.rosuda.JRI.REXP;

/**
 * This object is an encapsulation of the return result of an R/maanova
 * {@code fitmaanova(...)} call.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class FitMaanovaResult extends RObject
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            FitMaanovaResult.class.getName());
    
    /**
     * This is the name that R/maanova assigns to the underlying R type.
     */
    public static final String R_CLASS_STRING = "maanova";
    
    /**
     * The suffix used for the named item that holds the term levels
     */
    private static final String TERM_NAME_SUFFIX = ".level";
    
    private static final String VARIANCE_LEVELS = "S2.level";
    
    private static final String Y_HAT_COMPONENT = "$yhat";
    
    private static final String PROBESET_ID_COMPONENT = "$probeid";
    
    private final MicroarrayExperiment parentExperiment;
    
    /**
     * Constructor
     * @param parentExperiment
     *          the experiment that this belongs to
     * @param accessorExpressionString
     *          the accessor expression that you use in R to get
     *          to the 
     */
    public FitMaanovaResult(
            MicroarrayExperiment parentExperiment,
            String accessorExpressionString)
    {
        super(parentExperiment.getRInterface(), accessorExpressionString);
        
        this.parentExperiment = parentExperiment;
    }
    
    /**
     * Getter for the experiment that this fit belongs to
     * @return the parent
     */
    public MicroarrayExperiment getParentExperiment()
    {
        return this.parentExperiment;
    }
    
    /**
     * Getter for the fit term names
     * @return
     *          get the names
     */
    public List<String> getFitTermNames()
    {
        String[] allNames = JRIUtilityFunctions.getNames(this);
        List<String> fitTermNames = new ArrayList<String>();
        for(String name: allNames)
        {
            if(name.endsWith(TERM_NAME_SUFFIX) && !name.equals(VARIANCE_LEVELS))
            {
                // add the term name to the list after we pull out the suffix
                String termName = name.substring(
                        0,
                        name.length() - TERM_NAME_SUFFIX.length());
                fitTermNames.add(termName);
            }
        }
        
        return fitTermNames;
    }
    
    /**
     * Get the levels available for the given term name
     * @param term
     *          the term that we're getting the levels for
     * @return
     *          the level strings
     */
    public String[] getFitTermLevels(String term)
    {
        try
        {
            // first create term identifier safely and then get the values
            String termId =
                this.getAccessorExpressionString() +
                "$" +
                RUtilities.quoteIdentifierIfRequired(term + TERM_NAME_SUFFIX);
            RMethodInvocationCommand termsAsChar = new RMethodInvocationCommand(
                    "as.character",
                    Collections.singletonList(new RCommandParameter(termId)));
            REXP termRExpression = this.getRInterface().evaluateCommand(
                    new SilentRCommand(termsAsChar));
            
            // TODO confirm that this will always be a vector of strings and not
            //      a vector of factors
            String[] termLevels = termRExpression.asStringArray();
            
            return termLevels;
        }
        catch(RSyntaxException ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to get fit term levels",
                    ex);
            return null;
        }
    }
    
    /**
     * Get the yHat values for the given dye
     * @param dyeIndex the 0-based dye index
     * @param arrayIndex the 0-based array index
     * @return  the yHat's
     */
    public Double[] getYHatValues(int dyeIndex, int arrayIndex)
    {
        int dyeCount = this.getParentExperiment().getDyeCount();
        int colIndex = arrayIndex * dyeCount + dyeIndex;
        
        REXP yHatsExpr = this.getRInterface().evaluateCommand(new SilentRCommand(
                RUtilities.columnIndexExpression(
                        this.getAccessorExpressionString() + Y_HAT_COMPONENT,
                        colIndex)));
        return JRIUtilityFunctions.extractDoubleValues(yHatsExpr);
    }
    
    /**
     * Getter for the probeset ID strings
     * @return
     *          the probeset ID strings
     */
    public String[] getProbesetIds()
    {
        SilentRCommand probesetIdsCommand = new SilentRCommand(
                this.probesetIdAccessor());
        REXP probesetIdsExpr = this.getRInterface().evaluateCommand(
                probesetIdsCommand);
        return probesetIdsExpr.asStringArray();
    }
    
    /**
     * Get the probeset ID at the given index
     * @param probesetIndex
     *          the index
     * @return  the probeset ID string
     */
    public String getProbesetId(int probesetIndex)
    {
        SilentRCommand probesetIdCommand = new SilentRCommand(
                RUtilities.indexExpression(
                        this.probesetIdAccessor(),
                        probesetIndex));
        REXP probesetIdsExpr = this.getRInterface().evaluateCommand(
                probesetIdCommand);
        return probesetIdsExpr.asString();
    }
    
    private String probesetIdAccessor()
    {
        // TODO correct this in R/maanova
        // the as.character is necessary because read.madata sometimes
        // results in integer rather than string data
        return
            "as.character(" +
            this.getAccessorExpressionString() + PROBESET_ID_COMPONENT +
            ")";
    }
    
    /**
     * Get all of the top-level R objects whose class is {@value #R_CLASS_STRING}
     * @param rInterface
     *          the R interface to extract the objects from
     * @return
     *          the R objects of type {@value #R_CLASS_STRING}
     */
    public static List<RObject> getAllFitRObjects(RInterface rInterface)
    {
        List<RObject> fitIdentifiers = JRIUtilityFunctions.getTopLevelObjectsOfType(
                rInterface,
                R_CLASS_STRING);
        
        if(LOG.isLoggable(Level.FINEST))
        {
            StringBuffer message = new StringBuffer(
                    "detected fit data objects:");
            for(RObject currFitId: fitIdentifiers)
            {
                message.append(" " + currFitId.getAccessorExpressionString());
            }
            
            LOG.finest(message.toString());
        }
        
        return fitIdentifiers;
    }

    /**
     * delete this fit result
     */
    public void delete()
    {
        RMethodInvocationCommand rmMethod = new RMethodInvocationCommand(
                "rm",
                new RCommandParameter(this.getAccessorExpressionString()));
        this.getRInterface().evaluateCommandNoReturn(rmMethod);
    }
}
