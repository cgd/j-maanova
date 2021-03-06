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

package org.jax.maanova.varplot;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.jax.maanova.fit.FitMaanovaResult;

/**
 * A class for plotting the variance components of a fit
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class PlotVarianceComponentsAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 4374781966791791508L;

    private final FitMaanovaResult fitResult;
    
    /**
     * constructor
     * @param fitResult the fit resutl that this action will plot
     */
    public PlotVarianceComponentsAction(FitMaanovaResult fitResult)
    {
        super("Plot Variance Components...");
        this.fitResult = fitResult;
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
    }
}
