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

package org.jax.maanova.madata.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.jax.maanova.Maanova;
import org.jax.maanova.madata.MicroarrayExperiment;

/**
 * Scatter plot for array comparison
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ArrayScatterPlotAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 4990089591862850549L;
    
    private final MicroarrayExperiment experiment;

    /**
     * Constructor
     * @param name
     *          the name of this action
     * @param experiment
     *          the experiment to plot arrays for
     */
    public ArrayScatterPlotAction(String name, MicroarrayExperiment experiment)
    {
        super(name);
        
        this.experiment = experiment;
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        ArrayScatterPlotPanel arrayScatterPanel = new ArrayScatterPlotPanel(
                Maanova.getInstance().getApplicationFrame(),
                this.experiment);
        
        Maanova.getInstance().getDesktop().createInternalFrame(
                arrayScatterPanel,
                "Array Scatter Plot for " + this.experiment.toString(),
                null,
                "arrayscatterplot." + this.experiment.getAccessorExpressionString());
    }
}
