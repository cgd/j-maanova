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

package org.jax.maanova.test.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.jax.maanova.Maanova;
import org.jax.maanova.test.MaanovaTestResult;

/**
 * An action for creating a volcano plot
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class VolcanoPlotAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -9040568240831187217L;
    
    private final MaanovaTestResult maanovaTestResult;

    private final int initialTestIndex;

    private final int[] selectedIndices;
    
    /**
     * Constructor
     * @param maanovaTestResult
     *          the test result to show a plot for
     */
    public VolcanoPlotAction(MaanovaTestResult maanovaTestResult)
    {
        this(maanovaTestResult, 0, new int[0]);
    }
    
    /**
     * Constructor
     * @param maanovaTestResult
     *          the test result to show a plot for
     * @param initialTestIndex
     *          the initial test index
     * @param selectedIndices
     *          the selected test indices
     */
    public VolcanoPlotAction(
            MaanovaTestResult maanovaTestResult,
            int initialTestIndex,
            int[] selectedIndices)
    {
        this("Show Volcano Plot for " + maanovaTestResult.toString(),
             maanovaTestResult,
             initialTestIndex,
             selectedIndices);
    }
    
    /**
     * Constructor
     * @param name
     *          the name to use for this action
     * @param maanovaTestResult
     *          the test result to show a plot for
     * @param initialTestIndex
     *          the initial test index
     * @param selectedIndices
     *          the selected test indices
     */
    public VolcanoPlotAction(
            String name,
            MaanovaTestResult maanovaTestResult,
            int initialTestIndex,
            int[] selectedIndices)
    {
        super(name);
        this.maanovaTestResult = maanovaTestResult;
        this.initialTestIndex = initialTestIndex;
        this.selectedIndices = selectedIndices;
    }
    
    /**
     * Getter for the initial test index
     * @return the initial test index
     */
    public int getInitialTestIndex()
    {
        return this.initialTestIndex;
    }
    
    /**
     * Getter for the selected indices
     * @return the selected the selected indices
     */
    public int[] getSelectedIndices()
    {
        return this.selectedIndices;
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        this.act();
    }
    
    /**
     * Perform the action to show the volcano plot
     */
    public void act()
    {
        VolcanoPlotPanel volcanoPlotPanel = new VolcanoPlotPanel(
                Maanova.getInstance().getApplicationFrame(),
                this.maanovaTestResult,
                this.initialTestIndex,
                this.selectedIndices);
        
        Maanova.getInstance().getDesktop().createInternalFrame(
                volcanoPlotPanel,
                "Volcano Plot for " + this.maanovaTestResult.toString(),
                null,
                "vp." + this.maanovaTestResult.getAccessorExpressionString());
    }
}
