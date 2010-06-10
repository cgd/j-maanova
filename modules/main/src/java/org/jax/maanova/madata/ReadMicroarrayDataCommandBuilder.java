/*
 * Copyright (c) 2008 The Jackson Laboratory
 *
 * Permission is hereby granted, free of charge, to any person obtaining  a copy
 * of this software and associated documentation files (the  "Software"), to
 * deal in the Software without restriction, including  without limitation the
 * rights to use, copy, modify, merge, publish,  distribute, sublicense, and/or
 * sell copies of the Software, and to  permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be  included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,  EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF  MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY  CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,  TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE  SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.jax.maanova.madata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jax.r.RAssignmentCommand;
import org.jax.r.RCommand;
import org.jax.r.RCommandBuilder;
import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RUtilities;

/**
 * A class for reading in microarray data.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ReadMicroarrayDataCommandBuilder implements RCommandBuilder
{
    /**
     * The R method name to use
     */
    private static final String METHOD_NAME = "read.madata";
    
    private volatile boolean dataFileIsReallyAnObject = false;
    
    private volatile String dataFileName = "";
    
    private volatile String designFileName = "";
    
    private volatile ArrayType arrayType = ArrayType.ONE_COLOR;
    
    private volatile boolean filesIncludeHeader = true;
    
    private volatile boolean filesIncludeSpotFlag = false;
    
    private volatile int numberOfReplicates = 1;
    
    private volatile ReplicateSummaryMethod replicateSummaryMethod =
        ReplicateSummaryMethod.NO_SUMMARIZATION;
    
    private volatile boolean logTwoTransformData = false;
    
    private volatile boolean metarowAndMetacolumnValid = true;
    
    private volatile int metarowColumn = 1;
    
    private volatile int metacolumnColumn = 1;
    
    private volatile int rowColumn = 1;
    
    private volatile int columnColumn = 1;
    
    private volatile int probeIdColumn = 1;
    
    private volatile boolean probeIdColumnValid = true;
    
    private volatile int intensityColumn = 1;
    
    private volatile String microarrayDataName = "";
    
    private volatile boolean matchDataToDesign = false;
    
    /**
     * Should we match the data to the design automatically
     * @return true if we should handle array/design ordering automatically
     */
    public boolean getMatchDataToDesign()
    {
        return this.matchDataToDesign;
    }
    
    /**
     * Should we automatically match data to design
     * @param matchDataToDesign the matchDataToDesign to set
     */
    public void setMatchDataToDesign(boolean matchDataToDesign)
    {
        this.matchDataToDesign = matchDataToDesign;
    }
    
    /**
     * Determines if the {@link #dataFileName} is really an object, in which
     * case we should not surround it by quotes like we would normally do.
     * We need this option because sometimes we give read.madata a dataframe
     * or matrix instead of a file
     * @return true if it's a dataframe or matrix
     */
    public boolean getDataFileIsReallyAnObject()
    {
        return this.dataFileIsReallyAnObject;
    }
    
    /**
     * @see #getDataFileIsReallyAnObject()
     * @param dataFileIsReallyAnObject
     *          the new value for {@link #getDataFileIsReallyAnObject()}
     */
    public void setDataFileIsReallyAnObject(boolean dataFileIsReallyAnObject)
    {
        this.dataFileIsReallyAnObject = dataFileIsReallyAnObject;
    }
    
    /**
     * Getter for figuring out if the metarow and meta column data are
     * valid. Note that even if this function returns true, metarow and
     * metacolumn are only truly valid if the array type is two-color
     * @see #getMetacolumnColumn()
     * @see #getMetarowColumn()
     * @return true iff they're set valid
     */
    public boolean getMetarowAndMetacolumnValid()
    {
        return this.metarowAndMetacolumnValid;
    }
    
    /**
     * Setter for the valid flag on metarow and metacolumn
     * @see #getMetarowAndMetacolumnValid()
     * @see #getMetacolumnColumn()
     * @see #getMetarowColumn()
     * @param metarowAndMetacolumnValid the new "valid" value
     */
    public void setMetarowAndMetacolumnValid(boolean metarowAndMetacolumnValid)
    {
        this.metarowAndMetacolumnValid = metarowAndMetacolumnValid;
    }

    /**
     * Getter for the data file name
     * @return
     *          the data file name
     */
    public String getDataFileName()
    {
        return this.dataFileName;
    }

    /**
     * Setter for the data file name
     * @param dataFileName
     *          the data file name
     */
    public void setDataFileName(String dataFileName)
    {
        this.dataFileName = dataFileName;
    }

    /**
     * Getter for the design file name
     * @return
     *          the design file name
     */
    public String getDesignFileName()
    {
        return this.designFileName;
    }

    /**
     * Setter for the design file name
     * @param designFileName
     *          the design file name
     */
    public void setDesignFileName(String designFileName)
    {
        this.designFileName = designFileName;
    }

    /**
     * Getter for the array type
     * @return
     *          the array type
     */
    public ArrayType getArrayType()
    {
        return this.arrayType;
    }

    /**
     * Setter for the array type
     * @param arrayType
     *          the array type
     */
    public void setArrayType(ArrayType arrayType)
    {
        this.arrayType = arrayType;
    }

    /**
     * Getter for determining if the input files include a header row
     * @return
     *          true if they do contain a header row
     */
    public boolean getFilesIncludeHeader()
    {
        return this.filesIncludeHeader;
    }

    /**
     * Setter for determining if the input files include a header row
     * @param filesIncludeHeader
     *          set to true to indicate that there is a header
     */
    public void setFilesIncludeHeader(boolean filesIncludeHeader)
    {
        this.filesIncludeHeader = filesIncludeHeader;
    }

    /**
     * Setter for determining if the input files have a spot flag
     * @return
     *          true iff there is a spot flag
     */
    public boolean getFilesIncludeSpotFlag()
    {
        return this.filesIncludeSpotFlag;
    }

    /**
     * Setter for determining if the input files have a spot flag
     * @param filesIncludeSpotFlag
     *          true iff the input files contain a spot flag
     */
    public void setFilesIncludeSpotFlag(boolean filesIncludeSpotFlag)
    {
        this.filesIncludeSpotFlag = filesIncludeSpotFlag;
    }

    /**
     * Getter for the number of replicates
     * @return
     *          the number of replicates
     */
    public int getNumberOfReplicates()
    {
        return this.numberOfReplicates;
    }

    /**
     * Setter for the number of replicates
     * @param numberOfReplicates
     *          the number of replicates
     */
    public void setNumberOfReplicates(int numberOfReplicates)
    {
        this.numberOfReplicates = numberOfReplicates;
    }

    /**
     * Getter for the replicate summary method that should be used
     * @return
     *          the replicate summary method that should be used
     */
    public ReplicateSummaryMethod getReplicateSummaryMethod()
    {
        return this.replicateSummaryMethod;
    }

    /**
     * Setter for the replicate summary method that should be used
     * @param replicateSummaryMethod
     *          the new value for the replicate summary method that should
     *          be used
     */
    public void setReplicateSummaryMethod(
            ReplicateSummaryMethod replicateSummaryMethod)
    {
        this.replicateSummaryMethod = replicateSummaryMethod;
    }

    /**
     * Getter for determining if the data should be log2 transformed
     * @return
     *          true if the data will be log2 transformed
     */
    public boolean getLogTwoTransformData()
    {
        return this.logTwoTransformData;
    }

    /**
     * Setter for determining if the data should be log2 transformed
     * @param logTwoTransformData
     *          iff true then the data will be log2 transformed
     */
    public void setLogTwoTransformData(boolean logTwoTransformData)
    {
        this.logTwoTransformData = logTwoTransformData;
    }

    /**
     * Getter for the column where we can read metarow values
     * @see #getMetarowAndMetacolumnValid()
     * @return
     *          the column number for metarows
     */
    public int getMetarowColumn()
    {
        return this.metarowColumn;
    }

    /**
     * Setter for the column where we can read metarow values
     * @param metarowColumn
     *          the column number for metarows
     */
    public void setMetarowColumn(int metarowColumn)
    {
        this.metarowColumn = metarowColumn;
    }

    /**
     * Getter for the column where metacolumn values can be read
     * @see #getMetarowAndMetacolumnValid()
     * @return
     *          the column where metacolumn values can be read
     */
    public int getMetacolumnColumn()
    {
        return this.metacolumnColumn;
    }

    /**
     * Setter for the column where metacolumn values can be read
     * @param metacolumnColumn
     *          the metacolumn column
     */
    public void setMetacolumnColumn(int metacolumnColumn)
    {
        this.metacolumnColumn = metacolumnColumn;
    }

    /**
     * Getter for the column where row values can be read
     * @return
     *          the column where row values can be read
     */
    public int getRowColumn()
    {
        return this.rowColumn;
    }

    /**
     * Setter for the column where row values can be read
     * @param rowColumn
     *          the row column
     */
    public void setRowColumn(int rowColumn)
    {
        this.rowColumn = rowColumn;
    }

    /**
     * Getter for the column where column values can be read
     * @return
     *          the column column
     */
    public int getColumnColumn()
    {
        return this.columnColumn;
    }

    /**
     * Setter for the column where column values can be read
     * @param columnColumn
     *          the column
     */
    public void setColumnColumn(int columnColumn)
    {
        this.columnColumn = columnColumn;
    }

    /**
     * Getter for the probe ID column to use
     * @see #isProbeIdColumnValid()
     * @return
     *          the probe ID column
     */
    public int getProbeIdColumn()
    {
        return this.probeIdColumn;
    }

    /**
     * Setter for the probe ID column to use
     * @param probeIdColumn
     *          the probe ID column
     */
    public void setProbeIdColumn(int probeIdColumn)
    {
        this.probeIdColumn = probeIdColumn;
    }
    
    /**
     * Getter for determining if the probe ID column is valid or not
     * @see #getProbeIdColumn()
     * @return
     *          true if it is valid
     */
    public boolean isProbeIdColumnValid()
    {
        return this.probeIdColumnValid;
    }
    
    /**
     * Setter for determining if the probe ID column is valid or not
     * @param probeIdColumnValid
     *          the new validity value for the probe ID
     */
    public void setProbeIdColumnValid(boolean probeIdColumnValid)
    {
        this.probeIdColumnValid = probeIdColumnValid;
    }
    
    /**
     * Getter for the intensity column that should be used
     * @return
     *          the intensity column to use
     */
    public int getIntensityColumn()
    {
        return this.intensityColumn;
    }

    /**
     * Setter for the intensity column that should be used
     * @param intensityColumn
     *          the intensity column
     */
    public void setIntensityColumn(int intensityColumn)
    {
        this.intensityColumn = intensityColumn;
    }
    
    /**
     * Getter for the name that's used for the microarray data
     * @return the microarrayDataName
     */
    public String getMicroarrayDataName()
    {
        return this.microarrayDataName;
    }
    
    /**
     * Setter for the name that's used for the microarray data
     * @param microarrayDataName the microarrayDataName to set
     */
    public void setMicroarrayDataName(String microarrayDataName)
    {
        this.microarrayDataName = microarrayDataName;
    }
    
    /**
     * Getter for the read.madata command
     * @return
     *          the command
     */
    public RCommand getCommand()
    {
        List<RCommandParameter> commandParameters =
            this.getCommandParameters();
        
        RMethodInvocationCommand readMadataMethodCommand = new RMethodInvocationCommand(
                METHOD_NAME,
                commandParameters);
        
        String microarrayDataName = this.microarrayDataName;
        if(microarrayDataName == null || microarrayDataName.trim().length() == 0)
        {
            return readMadataMethodCommand;
        }
        else
        {
            return new RAssignmentCommand(
                    microarrayDataName.trim(),
                    readMadataMethodCommand.getCommandText());
        }
    }

    /**
     * Getter for the command parameters
     * @return
     *          the command parameters
     */
    private List<RCommandParameter> getCommandParameters()
    {
        List<RCommandParameter> parameters = new ArrayList<RCommandParameter>();
        
        String dataFileName = this.dataFileName;
        if(dataFileName != null && dataFileName.length() >= 1)
        {
            if(this.dataFileIsReallyAnObject)
            {
                parameters.add(new RCommandParameter(
                        "datafile",
                        dataFileName));
            }
            else
            {
                File dataFile = new File(dataFileName);
                parameters.add(new RCommandParameter(
                        "datafile",
                        RUtilities.javaStringToRString(
                                dataFile.getAbsolutePath())));
            }
        }
        
        String designFileName = this.designFileName;
        if(designFileName != null && designFileName.length() >= 1)
        {
            File designFile = new File(designFileName);
            parameters.add(new RCommandParameter(
                    "designfile",
                    RUtilities.javaStringToRString(
                            designFile.getAbsolutePath())));
        }
        
        ArrayType arrayType = this.arrayType;
        if(arrayType != null)
        {
            parameters.add(new RCommandParameter(
                    "arrayType",
                    RUtilities.javaStringToRString(arrayType.getRValue())));
        }
        
        parameters.add(new RCommandParameter(
                "header",
                RUtilities.javaBooleanToRBoolean(this.filesIncludeHeader)));
        
        // deal with two-color specific parameters
        if(arrayType == ArrayType.TWO_COLOR)
        {
            parameters.add(new RCommandParameter(
                    "spotflag",
                    RUtilities.javaBooleanToRBoolean(this.filesIncludeSpotFlag)));
            
            int numberOfReplicates = this.numberOfReplicates;
            parameters.add(new RCommandParameter(
                    "n.rep",
                    RUtilities.javaIntToRInt(numberOfReplicates)));
            
            if(numberOfReplicates >= 2)
            {
                ReplicateSummaryMethod replicateSummaryMethod = this.replicateSummaryMethod;
                if(replicateSummaryMethod != null)
                {
                    parameters.add(new RCommandParameter(
                            "avgreps",
                            RUtilities.javaIntToRInt(
                                    replicateSummaryMethod.getRValue())));
                }
            }
            
            // two-color specific column settings
            if(this.metarowAndMetacolumnValid)
            {
                parameters.add(new RCommandParameter(
                        "metarow",
                        RUtilities.javaIntToRInt(this.metarowColumn)));
                parameters.add(new RCommandParameter(
                        "metacol",
                        RUtilities.javaIntToRInt(this.metacolumnColumn)));
            }
            parameters.add(new RCommandParameter(
                    "row",
                    RUtilities.javaIntToRInt(this.rowColumn)));
            parameters.add(new RCommandParameter(
                    "col",
                    RUtilities.javaIntToRInt(this.columnColumn)));
        }
        
        // general column settings
        // use 0 for an invalid (non-existant) probe ID column
        parameters.add(new RCommandParameter(
                "probeid",
                this.probeIdColumnValid ?
                        RUtilities.javaIntToRInt(this.probeIdColumn) :
                        RUtilities.javaIntToRInt(0)));
        parameters.add(new RCommandParameter(
                "intensity",
                RUtilities.javaIntToRInt(this.intensityColumn)));
        
        parameters.add(new RCommandParameter(
                "log.trans",
                RUtilities.javaBooleanToRBoolean(this.logTwoTransformData)));
        
        if(this.matchDataToDesign)
        {
            parameters.add(new RCommandParameter(
                    "matchDataToDesign",
                    RUtilities.javaBooleanToRBoolean(true)));
        }
        
        return parameters;
    }
}
