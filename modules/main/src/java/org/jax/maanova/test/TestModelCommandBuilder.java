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

package org.jax.maanova.test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.jax.maanova.fit.MixedModelSolutionMethod;
import org.jax.r.RAssignmentCommand;
import org.jax.r.RCommand;
import org.jax.r.RCommandBuilder;
import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RUtilities;
import org.jax.util.math.NumericUtilities;

/**
 * For building matest commands
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class TestModelCommandBuilder implements RCommandBuilder
{
    /**
     * Shuffle method to apply to permutations
     */
    public enum ShuffleMethod {
        /**
         * see R/maanova docs
         */
        SAMPLE
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String getRParameterString()
            {
                return "sample";
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Sample";
            }
        },
        
        /**
         * see R/maanova docs
         */
        RESIDUAL
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String getRParameterString()
            {
                return "resid";
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Residual";
            }
        };
        
        /**
         * Getter for the string that R/maanova uses to represent this shuffle
         * method
         * @return  the R/maanova string (no quotes, you have to add those)
         */
        public abstract String getRParameterString();
    }
    
    /**
     * The F-statistic(s) that should be caluculated
     */
    public enum FStatisticToCalculate {
        /**
         * see R/maanova docs 
         */
        JustStandardFStatistic {
            /**
             * {@inheritDoc}
             */
            @Override
            public String getRParameter()
            {
                return "c(1, 0)";
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Standard F Statistic";
            }
        },
        
        /**
         * see R/maanova docs 
         */
        JustFSStatistic {
            /**
             * {@inheritDoc}
             */
            @Override
            public String getRParameter()
            {
                return "c(0, 1)";
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "FS Statistic";
            }
        },
        
        /**
         * see R/maanova docs 
         */
        BothFAndFsStatistics {
            /**
             * {@inheritDoc}
             */
            @Override
            public String getRParameter()
            {
                return "c(1, 1)";
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Both F and Fs Statistics";
            }
        };

        /**
         * Getter for the r parameter to use.
         * @return
         *          the R parameter
         */
        public abstract String getRParameter();
    }
    
    /**
     * The R method name to use
     */
    private static final String METHOD_NAME = "matest";
    
    private volatile String testResultDataName;
    
    private volatile String madataParameter;
    
    private volatile String fitResultParameter;
    
    private volatile Number[][] fTestContrastMatrix;
    
    private volatile Number[][] tTestContrastMatrix;
    
    private volatile String[] termsToTest;
    
    private volatile String[] levelsToTest;
    
    private volatile int permutationCount = 1000;
    
    private volatile double criticalThreshold = 0.9;
    
    private volatile TestType testType;
    
    private volatile ShuffleMethod shuffleMethod;
    
    private volatile MixedModelSolutionMethod mixedModelSolutionMethod;
    
    private volatile FStatisticToCalculate fStatisticToCalculate;
    
    private volatile boolean poolPValues = true;
    
    private volatile boolean verbose = true;
    
    /**
     * getter for the test result data name
     * @return the testResultDataName
     */
    public String getTestResultDataName()
    {
        return this.testResultDataName;
    }
    
    /**
     * setter for the test result data name
     * @param testResultDataName the testResultDataName to set
     */
    public void setTestResultDataName(String testResultDataName)
    {
        this.testResultDataName = testResultDataName;
    }
    
    /**
     * Getter for the madata parameter
     * @return the parameter
     */
    public String getMadataParameter()
    {
        return this.madataParameter;
    }
    
    /**
     * Setter for the madata parameter
     * @param madataParameter the parameter to set
     */
    public void setMadataParameter(String madataParameter)
    {
        this.madataParameter = madataParameter;
    }
    
    /**
     * Getter for the fit result parameter
     * @return the fit result parameter
     */
    public String getFitResultParameter()
    {
        return this.fitResultParameter;
    }
    
    /**
     * Setter for the fit result parameter
     * @param fitResultParameter the new parameter value
     */
    public void setFitResultParameter(String fitResultParameter)
    {
        this.fitResultParameter = fitResultParameter;
    }
    
    /**
     * Getter for the contrast matrix that should be used when
     * {@link #getTestType()} == {@link TestType#F_TEST}. Null means
     * don't pass any matrix to the R command
     * @return the matrix
     */
    public Number[][] getFTestContrastMatrix()
    {
        return this.fTestContrastMatrix;
    }
    
    /**
     * Setter for the F-test contrast matrix
     * @see #getFTestContrastMatrix()
     * @param fTestContrastMatrix the matrix
     */
    public void setFTestContrastMatrix(Number[][] fTestContrastMatrix)
    {
        this.fTestContrastMatrix = fTestContrastMatrix;
    }
    
    /**
     * Getter for the contrast matrix that should be used when
     * {@link #getTestType()} == {@link TestType#T_TEST}. Null means
     * use the all pairs matrix.
     * @return the tTestContrastMatrix
     */
    public Number[][] getTTestContrastMatrix()
    {
        return this.tTestContrastMatrix;
    }
    
    /**
     * Setter for the t-test contrast matrix
     * @see #getTTestContrastMatrix()
     * @param testContrastMatrix the contrast matrix to use for a t-test
     */
    public void setTTestContrastMatrix(Number[][] testContrastMatrix)
    {
        this.tTestContrastMatrix = testContrastMatrix;
    }
    
    /**
     * Setter for the terms that should be tested
     * @param termsToTest the termsToTest to set
     */
    public void setTermsToTest(String[] termsToTest)
    {
        this.termsToTest = termsToTest;
    }
    
    /**
     * Getter for the terms that should be tested
     * @return the termsToTest
     */
    public String[] getTermsToTest()
    {
        return this.termsToTest;
    }
    
    /**
     * Getter for all of the term levels that should be tested
     * @see #getTermsToTest()
     * @return the levels
     */
    public String[] getLevelsToTest()
    {
        return this.levelsToTest;
    }
    
    /**
     * Setter for the levels to test. This should be all of the terms available
     * from {@link #getTermsToTest()}
     * @param levelsToTest the levels
     */
    public void setLevelsToTest(String[] levelsToTest)
    {
        this.levelsToTest = levelsToTest;
    }
    
    /**
     * Getter for the permutation cont
     * @return the permutation count
     */
    public int getPermutationCount()
    {
        return this.permutationCount;
    }
    
    /**
     * Setter for the permutation count
     * @param permutationCount the permutation count
     */
    public void setPermutationCount(int permutationCount)
    {
        this.permutationCount = permutationCount;
    }
    
    /**
     * Getter for the critical threshold
     * @return the critical threshold
     */
    public double getCriticalThreshold()
    {
        return this.criticalThreshold;
    }
    
    /**
     * Setter for the critical threshold
     * @param criticalThreshold
     *          the criticalThreshold to set
     */
    public void setCriticalThreshold(double criticalThreshold)
    {
        this.criticalThreshold = criticalThreshold;
    }
    
    /**
     * Getter for the test type
     * @return the test type
     */
    public TestType getTestType()
    {
        return this.testType;
    }
    
    /**
     * Setter for the test type
     * @param testType
     *          the testType to set
     */
    public void setTestType(TestType testType)
    {
        this.testType = testType;
    }
    
    /**
     * Getter for the shuffle method
     * @return the shuffle method
     */
    public ShuffleMethod getShuffleMethod()
    {
        return this.shuffleMethod;
    }
    
    /**
     * Setter for the shuffle method
     * @param shuffleMethod the shuffle method
     */
    public void setShuffleMethod(ShuffleMethod shuffleMethod)
    {
        this.shuffleMethod = shuffleMethod;
    }
    
    /**
     * Getter for the MME method we should use
     * @return the method to use
     */
    public MixedModelSolutionMethod getMixedModelSolutionMethod()
    {
        return this.mixedModelSolutionMethod;
    }
    
    /**
     * Setter for the MME method we should use
     * @param mixedModelSolutionMethod the method to use
     */
    public void setMixedModelSolutionMethod(
            MixedModelSolutionMethod mixedModelSolutionMethod)
    {
        this.mixedModelSolutionMethod = mixedModelSolutionMethod;
    }
    
    /**
     * Getter for the F statistic that should be calculated
     * @return the statistic
     */
    public FStatisticToCalculate getFStatisticToCalculate()
    {
        return this.fStatisticToCalculate;
    }
    
    /**
     * Setter for the F statistic that should be calculated
     * @param statisticToCalculate the statistic
     */
    public void setFStatisticToCalculate(
            FStatisticToCalculate statisticToCalculate)
    {
        this.fStatisticToCalculate = statisticToCalculate;
    }
    
    /**
     * Getter to determine if we're pooling p-values
     * @return true iff we should pool p-values
     */
    public boolean getPoolPValues()
    {
        return this.poolPValues;
    }
    
    /**
     * Setter for deciding if we will pool p-values
     * @param poolPValues if true then we pool p-values
     */
    public void setPoolPValues(boolean poolPValues)
    {
        this.poolPValues = poolPValues;
    }
    
    /**
     * determines if we're using verbose output
     * @return the verbose
     */
    public boolean isVerbose()
    {
        return this.verbose;
    }
    
    /**
     * Modifies the verbosity setting of the command
     * @param verbose the verbose to set
     */
    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }
    
    /**
     * {@inheritDoc}
     */
    public RCommand getCommand()
    {
        List<RCommandParameter> commandParameters = this.getCommandParameters();
        
        RMethodInvocationCommand readMadataMethodCommand = new RMethodInvocationCommand(
                METHOD_NAME,
                commandParameters);
        
        String dataName = this.getTestResultDataName();
        if(dataName == null || dataName.trim().length() == 0)
        {
            return readMadataMethodCommand;
        }
        else
        {
            return new RAssignmentCommand(
                    dataName.trim(),
                    readMadataMethodCommand.getCommandText());
        }
    }
    
    /**
     * Get the command parameters for the matest command
     * @return
     *          the parameters
     */
    private List<RCommandParameter> getCommandParameters()
    {
        List<RCommandParameter> commandParameters =
            new ArrayList<RCommandParameter>();
        
        String madataParameter = this.madataParameter;
        if(madataParameter != null)
        {
            // maybe add the data parameter
            commandParameters.add(new RCommandParameter(
                    "data",
                    madataParameter));
        }
        
        // maybe add the fit result parameter
        String fitResultParameter = this.fitResultParameter;
        if(fitResultParameter != null)
        {
            commandParameters.add(new RCommandParameter(
                    "anovaobj",
                    fitResultParameter));
        }
        
        // maybe add the term parameter
        String[] termsToTest = this.termsToTest;
        if(termsToTest != null && termsToTest.length >= 0)
        {
            if(termsToTest.length == 1)
            {
                // for one we should just use the string by itself
                commandParameters.add(new RCommandParameter(
                        "term",
                        RUtilities.javaStringToRString(termsToTest[0])));
            }
            else
            {
                // for more than one we need a vector
                String termsRVector = RUtilities.stringArrayToRVector(termsToTest);
                commandParameters.add(new RCommandParameter(
                        "term",
                        termsRVector));
            }
        }
        
        TestType testType = this.testType;
        if(testType != null)
        {
            commandParameters.add(new RCommandParameter(
                    "test.type",
                    RUtilities.javaStringToRString(testType.getRParameterString())));
        }
        
        boolean tTest = testType == TestType.T_TEST;
        Number[][] contrastMatrix =
            tTest ? this.tTestContrastMatrix : this.fTestContrastMatrix;
        if(contrastMatrix == null)
        {
            if(tTest)
            {
                String[] levelsToTest = this.levelsToTest;
                if(levelsToTest != null)
                {
                    RCommandParameter levelCountParameter =
                        new RCommandParameter(Integer.toString(levelsToTest.length));
                    RCommand allPairwiseRCommand = new RMethodInvocationCommand(
                            "PairContrast",
                            new RCommandParameter[] {levelCountParameter});
                    commandParameters.add(new RCommandParameter(
                            "Contrast",
                            allPairwiseRCommand.getCommandText()));
                }
            }
            else
            {
                // for F-test do nothing here
            }
        }
        else
        {
            String newContrastMatrixRCommand =
                this.toNewRMatrixCommand(contrastMatrix).getCommandText();
            commandParameters.add(new RCommandParameter(
                    "Contrast",
                    newContrastMatrixRCommand));
        }
        
        MixedModelSolutionMethod mixedModelSolutionMethod =
            this.mixedModelSolutionMethod;
        if(mixedModelSolutionMethod != null)
        {
            commandParameters.add(new RCommandParameter(
                    "MME.method",
                    RUtilities.javaStringToRString(mixedModelSolutionMethod.getRParameterString())));
        }
        
        FStatisticToCalculate fStatisticToCalculate = this.fStatisticToCalculate;
        if(fStatisticToCalculate != null)
        {
            commandParameters.add(new RCommandParameter(
                    "test.method",
                    fStatisticToCalculate.getRParameter()));
        }
        
        int permutationCount = this.permutationCount;
        commandParameters.add(new RCommandParameter(
                "n.perm",
                Integer.toString(permutationCount)));
        
        if(permutationCount >= 2)
        {
            BigDecimal roundedCritical = NumericUtilities.roundToDecimalPositionBigDecimal(
                    this.criticalThreshold,
                    -3);
            commandParameters.add(new RCommandParameter(
                    "critical",
                    roundedCritical.toString()));
            
            ShuffleMethod shuffleMethod = this.shuffleMethod;
            if(shuffleMethod != null)
            {
                commandParameters.add(new RCommandParameter(
                        "shuffle.method",
                        RUtilities.javaStringToRString(
                                shuffleMethod.getRParameterString())));
            }
            
            commandParameters.add(new RCommandParameter(
                    "pval.pool",
                    RUtilities.javaBooleanToRBoolean(this.poolPValues)));
        }
        
        commandParameters.add(new RCommandParameter(
                "verbose",
                RUtilities.javaBooleanToRBoolean(this.verbose)));
        
        return commandParameters;
    }
    
    /**
     * Convert the given 2D number array into an R new matrix command
     * @param contrastMatrix
     *          the 2D array to convert
     * @return
     *          the R {@code new("matrix", ...)} command
     */
    private RCommand toNewRMatrixCommand(Number[][] contrastMatrix)
    {
        final int nrow = contrastMatrix.length;
        final int ncol;
        if(nrow == 0)
        {
            ncol = 0;
        }
        else
        {
            ncol = contrastMatrix[0].length;
        }
        
        List<RCommandParameter> rNewParams = new ArrayList<RCommandParameter>();
        rNewParams.add(new RCommandParameter(
                RUtilities.javaStringToRString("matrix")));
        rNewParams.add(new RCommandParameter(
                "nrow",
                Integer.toString(nrow)));
        rNewParams.add(new RCommandParameter(
                "ncol",
                Integer.toString(ncol)));
        rNewParams.add(new RCommandParameter(
                "byrow",
                RUtilities.javaBooleanToRBoolean(true)));
        if(nrow != 0 && ncol != 0)
        {
            final Number[] contrastArray = new Number[ncol * nrow];
            for(int row = 0; row < nrow; row++)
            {
                // make sure all of the row lengths match
                assert row == 0 || contrastMatrix[row - 1].length == contrastMatrix[row].length;
                
                final int rowStart = row * ncol;
                for(int col = 0; col < ncol; col++)
                {
                    contrastArray[rowStart + col] = contrastMatrix[row][col];
                }
            }
            
            rNewParams.add(new RCommandParameter(
                    RUtilities.objectArrayToRVector(contrastArray)));
        }
        
        return new RMethodInvocationCommand("new", rNewParams);
    }
}
