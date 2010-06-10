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
