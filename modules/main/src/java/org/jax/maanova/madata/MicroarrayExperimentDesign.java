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

package org.jax.maanova.madata;

import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RObject;

/**
 * Getter for the experiment design object
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MicroarrayExperimentDesign extends RObject
{
    /**
     * The name to be used for the array column
     */
    public static final String ARRAY_COL_NAME = "Array";
    
    /**
     * The header name used for the dye column
     */
    public static final String DYE_COL_NAME = "Dye";
    
    /**
     * Constructor
     * @param rInterface
     *          the R interface that this design is attached to
     * @param accessorExpressionString
     *          the accessor for this R interface
     */
    public MicroarrayExperimentDesign(
            RInterface rInterface,
            String accessorExpressionString)
    {
        super(rInterface, accessorExpressionString);
    }
    
    /**
     * Getter for the different factors available in the design
     * @return
     *          the factors
     */
    public String[] getDesignFactors()
    {
        return JRIUtilityFunctions.getColumnNames(this);
    }
    
    /**
     * Getter for the design data
     * @return the design data
     */
    public String[][] getDesignData()
    {
        int rowCount = JRIUtilityFunctions.getNumberOfRows(this);
        
        RMethodInvocationCommand asMatrixMethod = new RMethodInvocationCommand(
                "as.matrix",
                new RCommandParameter(this.getAccessorExpressionString()));
        RObject thisAsMatrix = new RObject(
                this.getRInterface(),
                asMatrixMethod.getCommandText());
        String[][] designData = new String[rowCount][];
        for(int rowIndex = 0; rowIndex < rowCount; rowIndex++)
        {
            designData[rowIndex] = JRIUtilityFunctions.getRowStrings(
                    thisAsMatrix,
                    rowIndex);
        }
        
        return designData;
    }
    
    /**
     * Getter for the column with the given name. This function uses a case
     * insensitive search and also trims whitespace so an exact match is not
     * required
     * @param colName the column name that we're fetching data for
     * @return  the values in the array column
     */
    public String[] getColumnNamed(String colName)
    {
        String[] designFactors = this.getDesignFactors();
        
        // search for an exact match first
        int colIndex = -1;
        for(int i = 0; i < designFactors.length; i++)
        {
            if(colName.equals(designFactors[i]))
            {
                colIndex = i;
                break;
            }
        }
        
        // try a relaxed search if the exact match failed
        if(colIndex == -1)
        {
            colName = colName.trim();
            for(int i = 0; i < designFactors.length; i++)
            {
                if(colName.equalsIgnoreCase(designFactors[i].trim()))
                {
                    colIndex = i;
                    break;
                }
            }
        }
        
        if(colIndex == -1)
        {
            return null;
        }
        else
        {
            String[][] designData = this.getDesignData();
            String[] column = new String[designData.length];
            for(int row = 0; row < designData.length; row++)
            {
                column[row] = designData[row][colIndex];
            }
            
            return column;
        }
    }
}
