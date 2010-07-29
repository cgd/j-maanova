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

package org.jax.maanova.test;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.maanova.madata.MicroarrayExperiment;
import org.jax.maanova.madata.ProbesetRow;
import org.jax.maanova.test.gui.TestStatisticItem;
import org.jax.r.RCommand;
import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RUtilities;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RObject;
import org.jax.r.jriutilities.SilentRCommand;
import org.rosuda.JRI.REXP;

/**
 * Class that acts as a wrapper around an R/maanova matest object
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MaanovaTestResult extends RObject
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            MaanovaTestResult.class.getName());
    
    /**
     * This is the name that R/maanova assigns to the underlying R type.
     */
    private static final String R_CLASS_STRING = "matest";
    
    private static final String FOLD_CHANGE_VALS_METHOD = "calVolcanoXval";
    
    private static final String PROBESET_ID_COMPONENT = "$probeid";

    private final MicroarrayExperiment parentExperiment;
    
    private volatile TestType testType = null;
    
    /**
     * Constructor
     * @param parentExperiment
     *          the parent experiment for this test result
     * @param accessorExpressionString
     *          the accessor expression
     */
    public MaanovaTestResult(
            MicroarrayExperiment parentExperiment,
            String accessorExpressionString)
    {
        super(parentExperiment.getRInterface(), accessorExpressionString);
        
        this.parentExperiment = parentExperiment;
    }
    
    /**
     * Get the test type for this test result
     * @return  the test type
     */
    public TestType getTestType()
    {
        if(this.testType == null)
        {
            if(JRIUtilityFunctions.inheritsRClass(this, TestType.T_TEST.getRParameterString()))
            {
                this.testType = TestType.T_TEST;
            }
            else if(JRIUtilityFunctions.inheritsRClass(this, TestType.F_TEST.getRParameterString()))
            {
                this.testType = TestType.F_TEST;
            }
        }
        
        return this.testType;
    }
    
    /**
     * Get all of the top-level R objects whose class is {@value #R_CLASS_STRING}
     * @param rInterface
     *          the R interface to extract the objects from
     * @return
     *          the R objects of type {@value #R_CLASS_STRING}
     */
    public static List<RObject> getAllMaanovaTestRObjects(RInterface rInterface)
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
     * Getter for the parent experiment
     * @return the parent experiment
     */
    public MicroarrayExperiment getParentExperiment()
    {
        return this.parentExperiment;
    }
    
    /**
     * Get the fold change (x coordinates) that should be used in a
     * volcano plot. Note that the x-values should be the same no matter what
     * statistic is chosen
     * @param plotIndex
     *          the index of the plot (t-tests will have one plot per
     *          contrast row, f-tests will have a single plot)
     * @return
     *          the coordinates
     */
    public Double[] getFoldChangeValues(int plotIndex)
    {
        RMethodInvocationCommand fcValsMethod = new RMethodInvocationCommand(
                FOLD_CHANGE_VALS_METHOD,
                new RCommandParameter(this.getAccessorExpressionString()));
        
        final RCommand rCmd;
        switch(this.getTestType())
        {
            case F_TEST:
            {
                // cbind does nothing if the arg is already a matrix and if it is a
                // vector it turns the vector into a single column matrix. we need
                // this because the f-test gets a vector
                RMethodInvocationCommand vecFcValsMethod = new RMethodInvocationCommand(
                        "as.vector",
                        new RCommandParameter(fcValsMethod.getCommandText()));
                rCmd = new SilentRCommand(vecFcValsMethod);
            }
            break;
            
            case T_TEST:
            {
                // cbind does nothing if the arg is already a matrix and if it is a
                // vector it turns the vector into a single column matrix. we need
                // this because the f-test gets a vector
                RMethodInvocationCommand matFcValsMethod = new RMethodInvocationCommand(
                        "cbind",
                        new RCommandParameter(fcValsMethod.getCommandText()));
                
                String indexedFcVals = RUtilities.columnIndexExpression(
                        matFcValsMethod.getCommandText(),
                        plotIndex);
                rCmd = new SilentRCommand(indexedFcVals);
            }
            break;
            
            default: throw new IllegalStateException(
                    "Failed to determine if this is a t-test or f-test");
        }
        
        REXP vals = this.getRInterface().evaluateCommand(rCmd);
        
        return JRIUtilityFunctions.extractDoubleValues(vals);
    }
    
    /**
     * Get statistics for the given statistic type
     * @param statType
     *          the statistic type
     * @return
     *          the statistics
     */
    public MaanovaTestStatistics getStatistics(MaanovaTestStatisticType statType)
    {
        String statComponent =
            this.getAccessorExpressionString() +
            statType.getRComponentAccessorString();
        boolean statComponentIsNull = JRIUtilityFunctions.isNull(
                this.getRInterface(),
                statComponent);
        if(statComponentIsNull)
        {
            return null;
        }
        else
        {
            return new MaanovaTestStatistics(this.getRInterface(), statComponent);
        }
    }
    
    /**
     * Get the statistics values for the given test statistic item and
     * contrast index
     * @param testStatisticItem
     *          the statistic we want
     * @param contrastIndex
     *          the contrast index we want
     * @return
     *          the results
     */
    public Double[] getStatisticsValues(
            TestStatisticItem testStatisticItem,
            int contrastIndex)
    {
        MaanovaTestStatistics stats =
            this.getStatistics(testStatisticItem.getTestStatisticType());
        if(stats == null)
        {
            return null;
        }
        else
        {
            return stats.getValues(
                    testStatisticItem.getTestStatisticSubtype(),
                    contrastIndex);
        }
    }
    
    /**
     * Getter for the probeset ID strings
     * @return
     *          the probeset ID strings
     */
    public String[] getProbesetIds()
    {
        SilentRCommand probesetIdsCommand = new SilentRCommand(
                this.probesetIdAcessor());
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
                        this.probesetIdAcessor(),
                        probesetIndex));
        REXP probesetIdsExpr = this.getRInterface().evaluateCommand(
                probesetIdCommand);
        return probesetIdsExpr.asString();
    }
    
    private String probesetIdAcessor()
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
     * Get the statistic value for the given test statistic item and
     * contrast index
     * @param probeIndex
     *          the probe index
     * @param testStatisticItem
     *          the statistic we want
     * @param contrastIndex
     *          the contrast index we want
     * @return
     *          the results
     */
    public Double getStatisticsValue(
            int probeIndex,
            TestStatisticItem testStatisticItem,
            int contrastIndex)
    {
        MaanovaTestStatistics stats =
            this.getStatistics(testStatisticItem.getTestStatisticType());
        if(stats == null)
        {
            return null;
        }
        else
        {
            return stats.getValue(
                    probeIndex,
                    testStatisticItem.getTestStatisticSubtype(),
                    contrastIndex);
        }
    }
    
    /**
     * Getter for the probeset row
     * @param probesetIndex
     *          the probeset index
     * @param contrastIndex
     *          the contrast index
     * @param testStatistics
     *          the kind of statistics that we're looking for
     * @return
     *          the probeset row
     */
    public ProbesetRow getProbesetRow(
            int probesetIndex,
            int contrastIndex,
            TestStatisticItem[] testStatistics)
    {
        String probesetId = this.getProbesetId(probesetIndex);
        
        if(probesetId == null)
        {
            return null;
        }
        
        Double[] values = new Double[testStatistics.length];
        for(int i = 0; i < values.length; i++)
        {
            values[i] = this.getStatisticsValue(
                    probesetIndex,
                    testStatistics[i],
                    contrastIndex);
            if(values[i] == null)
            {
                return null;
            }
        }
        
        return new ProbesetRow(probesetId, values, probesetIndex);
    }

    /**
     * delete this test result from the R environment
     */
    public void delete()
    {
        RMethodInvocationCommand rmMethod = new RMethodInvocationCommand(
                "rm",
                new RCommandParameter(this.getAccessorExpressionString()));
        this.getRInterface().evaluateCommandNoReturn(rmMethod);
    }
}
