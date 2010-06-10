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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.maanova.fit.FitMaanovaResult;
import org.jax.maanova.test.MaanovaTestResult;
import org.jax.r.RAssignmentCommand;
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
 * Class representing the underlying "madata" R type
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MicroarrayExperiment extends RObject
{
    /**
     * This is the name that R/maanova assigns to the underlying R type.
     */
    public static final String R_CLASS_STRING = "madata";
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            MicroarrayExperiment.class.getName());
    
    private static final String DYE_COUNT_COMPONENT     = "$n.dye";
    private static final String NUM_ARRAYS_COMPONENT    = "$n.array";
    private static final String NUM_GENES_COMPONENT     = "$n.gene";
    private static final String DATA_COMPONENT          = "$data";
    private static final String PROBESET_ID_COMPONENT   = "$probeid";
    private static final String GENE_LISTS_COMPONENT    = "$gene_lists";
    
    /**
     * Constructor
     * @param rInterface
     *          the R interface that this object belongs to
     * @param accessorExpressionString
     *          the accessor expression that you use in R to get
     *          to the 
     */
    public MicroarrayExperiment(
            RInterface rInterface,
            String accessorExpressionString)
    {
        super(rInterface, accessorExpressionString);
    }
    
    /**
     * Getter for the number of arrays in the experiment
     * @return
     *          the # of arrays
     */
    public int getMicroarrayCount()
    {
        REXP numArraysRExpression = this.getRInterface().evaluateCommand(
                new SilentRCommand(
                        this.getAccessorExpressionString() +
                        NUM_ARRAYS_COMPONENT));
        return JRIUtilityFunctions.extractIntegerValue(numArraysRExpression);
    }
    
    /**
     * Getter for the number of genes in the experiment
     * @return
     *          the # of genes
     */
    public int getGeneCount()
    {
        REXP numGenesRExpression = this.getRInterface().evaluateCommand(
                new SilentRCommand(
                        this.getAccessorExpressionString() +
                        NUM_GENES_COMPONENT));
        return JRIUtilityFunctions.extractIntegerValue(numGenesRExpression);
    }
    
    /**
     * Get all of the top-level R objects whose class is {@value #R_CLASS_STRING}
     * @param rInterface
     *          the R interface to extract the objects from
     * @return
     *          the R objects of type {@value #R_CLASS_STRING}
     */
    public static List<RObject> getAllMicroarrayExperimentRObjects(RInterface rInterface)
    {
        List<RObject> microarrayIdentifiers = JRIUtilityFunctions.getTopLevelObjectsOfType(
                rInterface,
                R_CLASS_STRING);
        
        if(LOG.isLoggable(Level.FINEST))
        {
            StringBuffer message = new StringBuffer(
                    "detected microarray data objects:");
            for(RObject currMicroarrayId: microarrayIdentifiers)
            {
                message.append(" " + currMicroarrayId.getAccessorExpressionString());
            }
            
            LOG.finest(message.toString());
        }
        
        return microarrayIdentifiers;
    }
    
    /**
     * Get the gene list names
     * @return  the gene list names
     */
    public List<String> getGeneListNames()
    {
        RObject geneListsObj = new RObject(
                this.getRInterface(),
                this.getAccessorExpressionString() + GENE_LISTS_COMPONENT);
        
        if(JRIUtilityFunctions.isNull(geneListsObj))
        {
            return Collections.emptyList();
        }
        else
        {
            String[] names = JRIUtilityFunctions.getNames(geneListsObj);
            return new ArrayList<String>(Arrays.asList(names));
        }
    }
    
    /**
     * Getter for the gene list with the given name
     * @param listName the gene list name
     * @return the gene list for the given name
     */
    public String[] getGeneListNamed(String listName)
    {
        String geneListAccessor =
            this.getAccessorExpressionString() + GENE_LISTS_COMPONENT +
            '$' + listName;
        REXP geneListRExp = this.getRInterface().evaluateCommand(new SilentRCommand(
                geneListAccessor));
        return geneListRExp.asStringArray();
    }
    
    /**
     * Getter for the indices for gene list with the given name
     * @param listName  the name
     * @return          the indices
     */
    public int[] getIndicesForGeneListNamed(String listName)
    {
        String[] allGenesList = this.getProbesetIds();
        Map<String, Integer> geneIndexMap = new HashMap<String, Integer>();
        for(int i = 0; i < allGenesList.length; i++)
        {
            geneIndexMap.put(allGenesList[i], i);
        }
        
        String[] geneList = this.getGeneListNamed(listName);
        int[] indices = new int[geneList.length];
        for(int i = 0; i < geneList.length; i++)
        {
            indices[i] = geneIndexMap.get(geneList[i]);
        }
        
        return indices;
    }
    
    /**
     * Adds the given gene list to this microarray experiment
     * @param listName
     *          the list name
     * @param genes
     *          the genes in the list
     */
    public void putGeneListNamed(String listName, List<String> genes)
    {
        String geneListsAccessor =
            this.getAccessorExpressionString() + GENE_LISTS_COMPONENT;
        if(JRIUtilityFunctions.isNull(new RObject(this.getRInterface(), geneListsAccessor)))
        {
            // there is no slot for the gene lists so we need to make one
            this.getRInterface().evaluateCommandNoReturn(new SilentRCommand(
                    geneListsAccessor + " <- list()"));
        }
        
        String currListAccessor = geneListsAccessor + '$' + listName;
        String geneVectorStr = RUtilities.stringListToRVector(genes);
        
        this.getRInterface().evaluateCommand(new SilentRCommand(
                currListAccessor + " <- " + geneVectorStr));
    }
    
    /**
     * Add the given genes to a list name
     * @param listName  the list name to add to
     * @param genes     the genes
     */
    public void addToGeneListNamed(String listName, List<String> genes)
    {
        Set<String> geneListSet = new HashSet<String>(
                Arrays.asList(this.getGeneListNamed(listName)));
        geneListSet.addAll(genes);
        
        List<String> newGeneList = new ArrayList<String>(geneListSet);
        Collections.sort(newGeneList);
        
        this.putGeneListNamed(listName, newGeneList);
    }
    
    /**
     * Removes the list with the given name
     * @param listName  the name
     */
    public void removeGeneListNamed(String listName)
    {
        String geneListsAccessor =
            this.getAccessorExpressionString() + GENE_LISTS_COMPONENT;
        
        RCommand assignNullCommand = new SilentRCommand(new RAssignmentCommand(
                geneListsAccessor + '$' + listName,
                "NULL"));
        this.getRInterface().evaluateCommandNoReturn(assignNullCommand);
    }
    
    /**
     * Getter for the design
     * @return the design of this microarray experiment
     */
    public MicroarrayExperimentDesign getDesign()
    {
        return new MicroarrayExperimentDesign(
                this.getRInterface(),
                this.getAccessorExpressionString() + "$design");
    }
    
    /**
     * Getter for the data
     * @param dyeIndex the 0-based dye index
     * @param arrayIndex the 0-based array index
     * @return
     *          the data
     */
    public Double[] getData(int dyeIndex, int arrayIndex)
    {
        int dyeCount = this.getDyeCount();
        int colIndex = arrayIndex * dyeCount + dyeIndex;
        
        REXP yHatsExpr = this.getRInterface().evaluateCommand(new SilentRCommand(
                RUtilities.columnIndexExpression(
                        this.getAccessorExpressionString() + DATA_COMPONENT,
                        colIndex)));
        return JRIUtilityFunctions.extractDoubleValues(yHatsExpr);
    }
    
    /**
     * Getter for the data
     * @param probeIndex the 0-based array index
     * @return the data
     */
    public Double[] getDataRow(int probeIndex)
    {
        REXP yHatsExpr = this.getRInterface().evaluateCommand(new SilentRCommand(
                RUtilities.rowIndexExpression(
                        this.getAccessorExpressionString() + DATA_COMPONENT,
                        probeIndex)));
        return JRIUtilityFunctions.extractDoubleValues(yHatsExpr);
    }
    
    /**
     * Getter for the {@link FitMaanovaResult}s that belong to this
     * experiment
     * @return
     *          the {@link FitMaanovaResult}s
     */
    public Set<FitMaanovaResult> getFitMaanovaResults()
    {
        List<RObject> fitMaanovaRObjects = FitMaanovaResult.getAllFitRObjects(
                this.getRInterface());
        
        this.removeObjectsNotOwnedByThis(fitMaanovaRObjects);
        
        Set<FitMaanovaResult> fitMaanovaResults =
            new HashSet<FitMaanovaResult>(fitMaanovaRObjects.size());
        for(RObject fitRObject: fitMaanovaRObjects)
        {
            fitMaanovaResults.add(new FitMaanovaResult(
                    this,
                    fitRObject.getAccessorExpressionString()));
        }
        
        return fitMaanovaResults;
    }
    
    /**
     * Getter for the {@link MaanovaTestResult}s that belong to this experiment
     * @return
     *          the {@link MaanovaTestResult}s
     */
    public Set<MaanovaTestResult> getMaanovaTestResults()
    {
        List<RObject> maanovaTestRObjects = MaanovaTestResult.getAllMaanovaTestRObjects(
                this.getRInterface());
        
        this.removeObjectsNotOwnedByThis(maanovaTestRObjects);
        
        Set<MaanovaTestResult> maanovaTestResults =
            new HashSet<MaanovaTestResult>(maanovaTestRObjects.size());
        for(RObject testRObject: maanovaTestRObjects)
        {
            maanovaTestResults.add(new MaanovaTestResult(
                    this,
                    testRObject.getAccessorExpressionString()));
        }
        
        return maanovaTestResults;
    }
    
    /**
     * Getter for the dye count
     * @return
     *          the dye count
     */
    public int getDyeCount()
    {
        String dyeCountAccessor =
            this.getAccessorExpressionString() + DYE_COUNT_COMPONENT;
        REXP dyeCountExpr = this.getRInterface().evaluateCommand(new SilentRCommand(
                dyeCountAccessor));
        
        if(dyeCountExpr.asInt() >= 1)
        {
            return dyeCountExpr.asInt();
        }
        else
        {
            // TODO this should be dyeCountExpr.asInt() but for some reason JRI
            //      returns 0 here even when the value is 1!! This is a disturbing
            //      bug and I'm not sure what the root cause is just yet
            
            return (int)dyeCountExpr.asDouble();
        }
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
        REXP probesetIdExpr = this.getRInterface().evaluateCommand(
                probesetIdCommand);
        return probesetIdExpr.asString();
    }
    
    /**
     * Getter for all probeset IDs
     * @return  the probeset IDs
     */
    public String[] getProbesetIds()
    {
        SilentRCommand probesetIdsCommand = new SilentRCommand(
                this.probesetIdAccessor());
        REXP probesetIdsExpr = this.getRInterface().evaluateCommand(
                probesetIdsCommand);
        return probesetIdsExpr.asStringArray();
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
     * delete this experiment and all of its children
     */
    public void delete()
    {
        // delete all child objects
        for(String listName : this.getGeneListNames())
        {
            this.removeGeneListNamed(listName);
        }
        
        for(MaanovaTestResult test : this.getMaanovaTestResults())
        {
            test.delete();
        }
        
        for(FitMaanovaResult fitResult : this.getFitMaanovaResults())
        {
            fitResult.delete();
        }
        
        // delete this object
        RMethodInvocationCommand rmMethod = new RMethodInvocationCommand(
                "rm",
                new RCommandParameter(this.getAccessorExpressionString()));
        this.getRInterface().evaluateCommandNoReturn(rmMethod);
    }
}
