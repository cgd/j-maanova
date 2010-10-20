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
