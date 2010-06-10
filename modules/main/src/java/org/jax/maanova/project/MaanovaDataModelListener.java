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

import java.util.EventListener;

import org.jax.maanova.madata.MicroarrayExperiment;

/**
 * Interface that should be implemented by classes that want to listen for
 * changes to the {@link MaanovaDataModel}.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public interface MaanovaDataModelListener extends EventListener
{
    /**
     * Listener method called for notification that a microarray data object
     * has been added
     * @param source
     *          the data model that the microarray data was added to
     * @param microarrayExperiment
     *          the experiment that was added
     */
    public void microarrayExperimentAdded(
            MaanovaDataModel source,
            MicroarrayExperiment microarrayExperiment);
    
    /**
     * Listener method called for notification that a microarray data object
     * has been removed
     * @param source
     *          the data model that the microarray data was removed from
     * @param microarrayExperiment
     *          the experiment that was removed
     */
    public void microarrayExperimentRemoved(
            MaanovaDataModel source,
            MicroarrayExperiment microarrayExperiment);
}
