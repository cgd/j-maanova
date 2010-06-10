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
 * An action that simply shows the experiment's design
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ShowExperimentDesignAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 4628996313229133395L;

    private final MicroarrayExperiment experiment;
    
    /**
     * Constructor
     * @param name
     *          the action name to use
     * @param experiment
     *          the experiment whose design we want to show
     */
    public ShowExperimentDesignAction(String name, MicroarrayExperiment experiment)
    {
        super(name);
        
        this.experiment = experiment;
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        ExperimentDesignPanel designPanel = new ExperimentDesignPanel(
                this.experiment);
        
        Maanova.getInstance().getDesktop().createInternalFrame(
                designPanel,
                this.experiment.toString() + " Design",
                null,
                "experimentdesign." + this.experiment.getAccessorExpressionString());
    }
}
