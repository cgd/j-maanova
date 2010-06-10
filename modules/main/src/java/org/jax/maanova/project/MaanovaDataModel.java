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

package org.jax.maanova.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jax.maanova.madata.MicroarrayExperiment;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RObject;

/**
 * This class is the root access point for all of the R data that
 * a J/maanova project cares about
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MaanovaDataModel
{
    private final RInterface rInterface;
    
    private final ConcurrentLinkedQueue<MaanovaDataModelListener> listenerList =
        new ConcurrentLinkedQueue<MaanovaDataModelListener>();
    
    private final Map<String, MicroarrayExperiment> identifierToMicroarrayExperimentMap =
        Collections.synchronizedMap(new HashMap<String, MicroarrayExperiment>());

    /**
     * Constructor
     * @param rInterface
     *          the R interface to use
     */
    public MaanovaDataModel(RInterface rInterface)
    {
        this.rInterface = rInterface;
        this.updateAll();
    }
    
    /**
     * Returns a mapping from the R identifier (AKA the accessor string)
     * of a microarray data object and the java type that represents that
     * R object
     * @return
     *          the mapping
     */
    public Map<String, MicroarrayExperiment> getMicroarrayExperimentMap()
    {
        return this.identifierToMicroarrayExperimentMap;
    }
    
    /**
     * A convenience function that uses {@link #getMicroarrayExperimentMap()}
     * to construct an array of microarray data objects
     * @return
     *          the array
     */
    public MicroarrayExperiment[] getMicroarrays()
    {
        synchronized(this.identifierToMicroarrayExperimentMap)
        {
            MicroarrayExperiment[] microarrays =
                new MicroarrayExperiment[this.identifierToMicroarrayExperimentMap.size()];
            return this.identifierToMicroarrayExperimentMap.values().toArray(microarrays);
        }
    }
    
    /**
     * Calling this function refreshes all of the data structures and makes
     * sure that the java types match up with the R types
     */
    public void updateAll()
    {
        List<RObject> rMicroarrays = MicroarrayExperiment.getAllMicroarrayExperimentRObjects(
                this.rInterface);
        
        // add new microarrays
        List<MicroarrayExperiment> addedMicroarrays = new ArrayList<MicroarrayExperiment>();
        for(RObject rMicroarrayObj: rMicroarrays)
        {
            MicroarrayExperiment matchingMircroarray = this.identifierToMicroarrayExperimentMap.get(
                    rMicroarrayObj.getAccessorExpressionString());
            if(matchingMircroarray == null)
            {
                matchingMircroarray = new MicroarrayExperiment(
                        this.rInterface,
                        rMicroarrayObj.getAccessorExpressionString());
                this.identifierToMicroarrayExperimentMap.put(
                        rMicroarrayObj.getAccessorExpressionString(),
                        matchingMircroarray);
                addedMicroarrays.add(matchingMircroarray);
            }
        }
        
        // remove any missing microarrays
        List<MicroarrayExperiment> removedMicroarrays = new ArrayList<MicroarrayExperiment>();
        synchronized(this.identifierToMicroarrayExperimentMap)
        {
            Iterator<MicroarrayExperiment> microarrayEntryIter =
                this.identifierToMicroarrayExperimentMap.values().iterator();
            while(microarrayEntryIter.hasNext())
            {
                MicroarrayExperiment currMicroarray = microarrayEntryIter.next();
                boolean foundMatch = false;
                for(RObject currMicroarrayRObject: rMicroarrays)
                {
                    if(currMicroarray.getAccessorExpressionString().equals(
                       currMicroarrayRObject.getAccessorExpressionString()))
                    {
                        foundMatch = true;
                    }
                }
                
                if(!foundMatch)
                {
                    removedMicroarrays.add(currMicroarray);
                    microarrayEntryIter.remove();
                }
            }
        }
        
        // handle notification
        for(MicroarrayExperiment currAddedMicroarrays: addedMicroarrays)
        {
            this.fireMicroarrayExperimentAdded(currAddedMicroarrays);
        }
        
        for(MicroarrayExperiment currRemovedMicroarrays: removedMicroarrays)
        {
            this.fireMicroarrayExperimentRemoved(currRemovedMicroarrays);
        }
    }
    
    /**
     * Add the given listener to the listener list
     * @param maanovaDataModelListener
     *          the listener to add
     */
    public void addMaanovaDataModelListener(
            MaanovaDataModelListener maanovaDataModelListener)
    {
        this.listenerList.add(maanovaDataModelListener);
    }
    
    /**
     * Remove the given listener from the listener list
     * @param maanovaDataModelListener
     *          the listener to remove
     */
    public void removeMaanovaDataModelListener(
            MaanovaDataModelListener maanovaDataModelListener)
    {
        this.listenerList.remove(maanovaDataModelListener);
    }
    
    /**
     * Notifies all of the listeners that microarray data has been added
     * @param addedMicroarrayExperiment
     *          the data that was added
     */
    private void fireMicroarrayExperimentAdded(MicroarrayExperiment addedMicroarrayExperiment)
    {
        Iterator<MaanovaDataModelListener> listenerIter =
            this.listenerList.iterator();
        while(listenerIter.hasNext())
        {
            listenerIter.next().microarrayExperimentAdded(this, addedMicroarrayExperiment);
        }
    }
    
    /**
     * Notifies all of the listeners that microarray data has been removed
     * @param removedMicroarrayExperiment
     *          the data that was removed
     */
    private void fireMicroarrayExperimentRemoved(MicroarrayExperiment removedMicroarrayExperiment)
    {
        Iterator<MaanovaDataModelListener> listenerIter =
            this.listenerList.iterator();
        while(listenerIter.hasNext())
        {
            listenerIter.next().microarrayExperimentRemoved(this, removedMicroarrayExperiment);
        }
    }
}
