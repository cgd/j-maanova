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

import java.awt.BorderLayout;

import org.jax.maanova.madata.MicroarrayExperiment;
import org.jax.r.RCommand;
import org.jax.r.gui.RCommandEditorListener;
import org.jax.r.gui.RCommandEditorPanel;
import org.jax.util.gui.BroadcastingWizardController;
import org.jax.util.gui.WizardFlipPanel;

/**
 * A panel that contains all of the maanova wizard panels we flip through
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class FitMaanovaAllPanels extends RCommandEditorPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 1776512640037041004L;

    private final WizardFlipPanel wizardFlipPanel;
    
    private final FitMaanovaInitialPanel fitMaanovaInitialPanel;

    private RCommandEditorPanel[] allPanels;
    
    /**
     * Constructor
     * @param selectedExperiment
     *          the experiment to start with (can be null)
     * @param wizard
     *          the wizard to use
     */
    public FitMaanovaAllPanels(
            MicroarrayExperiment selectedExperiment,
            BroadcastingWizardController wizard)
    {
        this.setLayout(new BorderLayout());
        
        this.fitMaanovaInitialPanel = new FitMaanovaInitialPanel(
                selectedExperiment);
        this.allPanels = new RCommandEditorPanel[] {this.fitMaanovaInitialPanel};
        this.wizardFlipPanel = new WizardFlipPanel(
                this.allPanels,
                wizard);
        this.add(this.wizardFlipPanel, BorderLayout.CENTER);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BorderLayout getLayout()
    {
        return (BorderLayout)super.getLayout();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addRCommandEditorListener(
            RCommandEditorListener editorListener)
    {
        // delegate to the other panels
        for(RCommandEditorPanel panel: this.allPanels)
        {
            panel.addRCommandEditorListener(editorListener);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void removeRCommandEditorListener(
            RCommandEditorListener editorListener)
    {
        // delegate to the other panels
        for(RCommandEditorPanel panel: this.allPanels)
        {
            panel.removeRCommandEditorListener(editorListener);
        }
    }
    
    /**
     * Getter for the flip panel
     * @return the flip panel
     */
    public WizardFlipPanel getWizardFlipPanel()
    {
        return this.wizardFlipPanel;
    }
    
    /**
     * {@inheritDoc}
     */
    public RCommand[] getCommands()
    {
        return this.fitMaanovaInitialPanel.getCommands();
    }

    /**
     * Validate the data
     * @return
     *          true if the data in the dialog is valid for an OK or "finish"
     *          action
     */
    public boolean validateData()
    {
        return this.fitMaanovaInitialPanel.validateData();
    }
}
