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

package org.jax.maanova.fit.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.jax.maanova.madata.MicroarrayExperiment;

/**
 * Action for doing a fit MAANOVA
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class FitMaanovaAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -2210653351941670143L;
    private final MicroarrayExperiment experiment;

    /**
     * Constructor
     */
    public FitMaanovaAction()
    {
        this("Fit ANOVA Model...", null);
    }

    /**
     * Constructor
     * @param name
     *          the name to use for this action
     * @param experiment
     *          the experiment
     */
    public FitMaanovaAction(String name, MicroarrayExperiment experiment)
    {
        super(name,
              new ImageIcon(FitMaanovaAction.class.getResource(
                      "/images/fitmaanova-16x16.png")));
        this.experiment = experiment;
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        FitMaanovaWizard fitMaanovaWizard = new FitMaanovaWizard(this.experiment);
        fitMaanovaWizard.show();
    }
}
