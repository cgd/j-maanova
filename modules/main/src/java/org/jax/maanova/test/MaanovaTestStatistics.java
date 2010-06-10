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

import org.jax.r.RUtilities;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RObject;
import org.jax.r.jriutilities.SilentRCommand;
import org.rosuda.JRI.REXP;

/**
 * Class for representing the test statistics in an matest object
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MaanovaTestStatistics extends RObject
{
    /**
     * Constructor
     * @param rInterface
     *          the R interface
     * @param accessorExpressionString
     *          the accessor
     */
    public MaanovaTestStatistics(
            RInterface rInterface,
            String accessorExpressionString)
    {
        super(rInterface, accessorExpressionString);
    }
    
    /**
     * Gets the column count which applies to all statistics that this
     * contains
     * @return
     *          the column count which should correspond to the number of
     *          contrasts in the case of a t-test (this value should
     *          always be 1 in the case of an F or Fs test)
     */
    public int getContrastCount()
    {
        String colCountAccessor =
            this.getAccessorExpressionString() +
            MaanovaTestStatisticSubtype.F_OBSERVED.getRComponentAccessorString();
        
        return JRIUtilityFunctions.getNumberOfColumns(new RObject(
                this.getRInterface(),
                colCountAccessor));
    }
    
    /**
     * Determine if this contains the given statistic
     * @param testStatistic
     *          the statistic
     * @return
     *          true if this has the given statistic
     */
    public boolean hasTestStatistic(MaanovaTestStatisticSubtype testStatistic)
    {
        String componentId =
                this.getAccessorExpressionString() +
                testStatistic.getRComponentAccessorString();
        
        return !JRIUtilityFunctions.isNull(this.getRInterface(), componentId);
    }
    
    /**
     * Extracts doubles from the given statistic using
     * {@link JRIUtilityFunctions#extractDoubleValues(REXP)}
     * @param testStatistic
     *          the test statistic to extract
     * @param contrastIndex
     *          for a test that includes a number of contrasts
     * @return
     *          the double values
     */
    public Double[] getValues(
            MaanovaTestStatisticSubtype testStatistic,
            int contrastIndex)
    {
        String componentId = RUtilities.columnIndexExpression(
                this.getAccessorExpressionString() + testStatistic.getRComponentAccessorString(),
                contrastIndex);
        if(JRIUtilityFunctions.isNull(this.getRInterface(), componentId))
        {
            return null;
        }
        else
        {
            REXP componentExpr = this.getRInterface().evaluateCommand(new SilentRCommand(
                    componentId));
            Double[] values = JRIUtilityFunctions.extractDoubleValues(componentExpr);
            
            return values;
        }
    }
    
    /**
     * Extracts double from the given statistic using
     * @param probesetIndex
     *          the probeset index to use
     * @param testStatistic
     *          the test statistic to extract
     * @param contrastIndex
     *          for a test that includes a number of contrasts
     * @return
     *          the double value
     */
    public Double getValue(
            int probesetIndex,
            MaanovaTestStatisticSubtype testStatistic,
            int contrastIndex)
    {
        String componentId = RUtilities.columnIndexExpression(
                this.getAccessorExpressionString() + testStatistic.getRComponentAccessorString(),
                contrastIndex);
        if(JRIUtilityFunctions.isNull(this.getRInterface(), componentId))
        {
            return null;
        }
        else
        {
            REXP componentExpr = this.getRInterface().evaluateCommand(new SilentRCommand(
                    RUtilities.indexExpression(componentId, probesetIndex)));
            double value = componentExpr.asDouble();
            
            return value == Double.NaN ? null : value;
        }
    }
}
