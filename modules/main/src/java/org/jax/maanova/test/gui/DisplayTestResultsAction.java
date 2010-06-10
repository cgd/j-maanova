/*
 * Copyright (c) 2009 The Jackson Laboratory
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
 * An action to display MAANOVA test results
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class DisplayTestResultsAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 5976736336587813628L;
    
    private MaanovaTestResult maanovaTestResult;

    private volatile int initialTestIndex = 0;

    /**
     * Constructor
     * @param maanovaTestResult shows tabular results from an matest
     */
    public DisplayTestResultsAction(MaanovaTestResult maanovaTestResult)
    {
        this("Show Results Table for " + maanovaTestResult.toString(),
             maanovaTestResult);
    }
    
    /**
     * Constructor
     * @param name the name to use for this action
     * @param maanovaTestResult shows tabular results from an matest
     */
    public DisplayTestResultsAction(
            String name,
            MaanovaTestResult maanovaTestResult)
    {
        super(name);
        this.maanovaTestResult = maanovaTestResult;
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        TestResultsPanel testResultsPanel = new TestResultsPanel(
                this.maanovaTestResult,
                this.initialTestIndex);
        
        Maanova.getInstance().getDesktop().createInternalFrame(
                testResultsPanel,
                "MAANOVA Test Results for " + this.maanovaTestResult.toString(),
                null,
                "matest_tbl." + this.maanovaTestResult.getAccessorExpressionString());
    }
}
