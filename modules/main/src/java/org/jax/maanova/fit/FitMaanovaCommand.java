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

package org.jax.maanova.fit;

import java.util.ArrayList;
import java.util.List;

import org.jax.maanova.madata.MicroarrayExperiment;
import org.jax.r.RAssignmentCommand;
import org.jax.r.RCommand;
import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RUtilities;

/**
 * For calling R/maanova's fitmaanova(...) command
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class FitMaanovaCommand implements RCommand
{
    private static final String FIT_MAANOVA_METHOD_NAME = "fitmaanova";
    
    private volatile InteractivePredictor[] formula = new InteractivePredictor[0];
    
    private volatile String fitAssigneeIdentifier = "";
    
    private volatile MicroarrayExperiment microarrayExperiment = null;
    
    private volatile InteractivePredictor[] randomPredictors = new InteractivePredictor[0];
    
    private volatile InteractivePredictor[] covariatePredictors = new InteractivePredictor[0];
    
    private volatile MixedModelSolutionMethod method = null;
    
    private volatile boolean printVerboseOutput = true;
    
    private volatile boolean subtractColumnMeans = false;
    
    /**
     * Getter for the formula used by this fit
     * @return the formula
     */
    public InteractivePredictor[] getFormula()
    {
        return this.formula;
    }
    
    /**
     * Setter for the formula used by this fit
     * @param formula the formula to set
     */
    public void setFormula(InteractivePredictor[] formula)
    {
        this.formula = formula;
    }
    
    /**
     * Getter for the fit assignee
     * @return
     *          the fit assignee
     */
    public String getFitAssigneeIdentifier()
    {
        return this.fitAssigneeIdentifier;
    }
    
    /**
     * Setter for the fit assignee
     * @param fitAssigneeIdentifier
     *          the fit assignee
     */
    public void setFitAssigneeIdentifier(String fitAssigneeIdentifier)
    {
        this.fitAssigneeIdentifier = fitAssigneeIdentifier;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getCommandText()
    {
        List<RCommandParameter> commandParameters = this.getCommandParameters();
        RMethodInvocationCommand fitMaanovaMethodInvocation =
            new RMethodInvocationCommand(
                    FIT_MAANOVA_METHOD_NAME,
                    commandParameters);
        String fitMethodText = fitMaanovaMethodInvocation.getCommandText();
        
        String fitAssigneeIdentifier = this.fitAssigneeIdentifier;
        if(fitAssigneeIdentifier == null || fitAssigneeIdentifier.length() == 0)
        {
            return fitMethodText;
        }
        else
        {
            RAssignmentCommand fitMaanovaAssignment = new RAssignmentCommand(
                    fitAssigneeIdentifier,
                    fitMethodText);
            return fitMaanovaAssignment.getCommandText();
        }
    }
    
    /**
     * Getter for the covariate predictors
     * @return
     *          the covariate predictors
     */
    public InteractivePredictor[] getCovariatePredictors()
    {
        return this.covariatePredictors;
    }
    
    /**
     * Setter for the covariate predictors
     * @param covariatePredictors
     *          the covariate predictors
     */
    public void setCovariatePredictors(
            InteractivePredictor[] covariatePredictors)
    {
        this.covariatePredictors = covariatePredictors;
    }
    
    /**
     * Getter for the method to use for solving the mixed model equation
     * @return
     *          the method 
     */
    public MixedModelSolutionMethod getMethod()
    {
        return this.method;
    }
    
    /**
     * Setter for the mixed model solver method
     * @param method
     *          the method
     */
    public void setMethod(MixedModelSolutionMethod method)
    {
        this.method = method;
    }
    
    /**
     * Getter for the microarray experment to fit
     * @return
     *          the experiment to fit
     */
    public MicroarrayExperiment getMicroarrayExperiment()
    {
        return this.microarrayExperiment;
    }
    
    /**
     * Setter for the microarray experiment to fit
     * @param microarrayExperiment
     *          the experiment to fit
     */
    public void setMicroarrayExperiment(
            MicroarrayExperiment microarrayExperiment)
    {
        this.microarrayExperiment = microarrayExperiment;
    }
    
    /**
     * Getter for the random predictors to use
     * @return
     *          the random predictors to use
     */
    public InteractivePredictor[] getRandomPredictors()
    {
        return this.randomPredictors;
    }
    
    /**
     * Setter for the random predictors to use
     * @param randomPredictors
     *          the random predictors
     */
    public void setRandomPredictors(InteractivePredictor[] randomPredictors)
    {
        this.randomPredictors = randomPredictors;
    }
    
    /**
     * Determines if this fit function will print verbose output or not
     * @return
     *          true iff this fit will print verbose output
     */
    public boolean getPrintVerboseOutput()
    {
        return this.printVerboseOutput;
    }

    /**
     * Setter which determines if verbose output will be printed or not
     * @param printVerboseOutput
     *          if true then this fit will print verbose output
     */
    public void setPrintVerboseOutput(boolean printVerboseOutput)
    {
        this.printVerboseOutput = printVerboseOutput;
    }

    /**
     * Determine if this fit should subtract the column means or not
     * @return
     *          true iff we should subtract the column means
     */
    public boolean getSubtractColumnMeans()
    {
        return this.subtractColumnMeans;
    }

    /**
     * Setter for determining if we should subtract column means or not
     * @param subtractColumnMeans
     *          if true then we'll subtract means
     */
    public void setSubtractColumnMeans(boolean subtractColumnMeans)
    {
        this.subtractColumnMeans = subtractColumnMeans;
    }

    /**
     * Getter for the command parameter list given this command's current
     * property settings
     * @return
     *          the command parameters
     */
    private List<RCommandParameter> getCommandParameters()
    {
        List<RCommandParameter> commandParameters =
            new ArrayList<RCommandParameter>();
        
        // build the fitmaanova command parameters. here's how they're
        // documented by R/maanova
        // fitmaanova(madata, formula, random= ~1, covariate = ~1, mamodel,
        //            inits20,method=c("REML","ML","MINQE-I","MINQE-UI", "noest"),
        //            verbose=TRUE, subCol=FALSE)

        // madata
        MicroarrayExperiment microarrayExperiment = this.microarrayExperiment;
        if(microarrayExperiment != null)
        {
            commandParameters.add(new RCommandParameter(
                    "madata",
                    microarrayExperiment.getAccessorExpressionString()));
        }
        
        // formula
        InteractivePredictor[] formula = this.getFormula();
        if(formula != null)
        {
            commandParameters.add(new RCommandParameter(
                    "formula",
                    InteractivePredictor.toRFormulaString(formula)));
        }
        
        // random
        InteractivePredictor[] randomPredictors = this.randomPredictors;
        if(randomPredictors != null && randomPredictors.length >= 1)
        {
            commandParameters.add(new RCommandParameter(
                    "random",
                    InteractivePredictor.toRFormulaString(randomPredictors)));
        }
        
        // covariate
        InteractivePredictor[] covariatePredictors = this.covariatePredictors;
        if(covariatePredictors != null && covariatePredictors.length >= 1)
        {
            commandParameters.add(new RCommandParameter(
                    "covariate",
                    InteractivePredictor.toRFormulaString(covariatePredictors)));
        }
        
        // method
        MixedModelSolutionMethod method = this.method;
        if(method != null)
        {
            commandParameters.add(new RCommandParameter(
                    "method",
                    RUtilities.javaStringToRString(method.getRParameterString())));
        }
        
        // verbose
        commandParameters.add(new RCommandParameter(
                "verbose",
                RUtilities.javaBooleanToRBoolean(this.printVerboseOutput)));
        
        // subCol
        commandParameters.add(new RCommandParameter(
                "subCol",
                RUtilities.javaBooleanToRBoolean(this.subtractColumnMeans)));
        
        return commandParameters;
    }
}
