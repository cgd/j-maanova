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

import org.jax.maanova.Maanova;
import org.jax.maanova.fit.FitMaanovaResult;

/**
 * An action for creating a residual plot
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ResidualPlotAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -2979047396452930083L;
    
    private final FitMaanovaResult fitMaanovaResult;
    
    /**
     * Constructor
     * @param fitMaanovaResult
     *          the fit result to show a plot for
     */
    public ResidualPlotAction(FitMaanovaResult fitMaanovaResult)
    {
        this("Show Residual Plot for " + fitMaanovaResult.toString(), fitMaanovaResult);
    }

    /**
     * Constructor
     * @param name
     *          the name of the action
     * @param fitMaanovaResult
     *          the fit result to show a plot for
     */
    public ResidualPlotAction(String name, FitMaanovaResult fitMaanovaResult)
    {
        super(name);
        this.fitMaanovaResult = fitMaanovaResult;
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        ResidualPlotPanel residualPlotPanel = new ResidualPlotPanel(
                Maanova.getInstance().getApplicationFrame(),
                this.fitMaanovaResult);
        Maanova.getInstance().getDesktop().createInternalFrame(
                residualPlotPanel,
                "Residual Plot for " + this.fitMaanovaResult.toString(),
                null,
                "residplot." + this.fitMaanovaResult.getAccessorExpressionString());
    }
}
