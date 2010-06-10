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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.jax.maanova.Maanova;
import org.jax.maanova.madata.MicroarrayExperiment;
import org.jax.maanova.project.MaanovaProjectManager;
import org.jax.r.RCommand;
import org.jax.r.gui.RCommandEditorAndPreviewPanel;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.util.gui.BroadcastingWizardController;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.gui.WizardDialog;
import org.jax.util.gui.WizardEventSupport;
import org.jax.util.gui.WizardListener;
import org.jax.util.gui.WizardDialog.WizardDialogType;

/**
 * Wizard controller class for doing a fitmaanova
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class FitMaanovaWizard implements BroadcastingWizardController
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            FitMaanovaWizard.class.getName());
    
    private final WizardEventSupport wizardEventSupport;
    
    private final FitMaanovaAllPanels fitMaanovaAllPanels;
    
    private final WizardDialog wizardDialog;
    
    private final RCommandEditorAndPreviewPanel rEditorAndPreviewPanel;
    
    /**
     * Constructor
     */
    public FitMaanovaWizard()
    {
        this(null);
    }
    
    /**
     * Constructor
     * @param selectedExperiment
     *          the initially selected experiment
     */
    public FitMaanovaWizard(MicroarrayExperiment selectedExperiment)
    {
        this.wizardEventSupport = new WizardEventSupport(this);
        
        this.fitMaanovaAllPanels = new FitMaanovaAllPanels(
                selectedExperiment,
                this);
        this.rEditorAndPreviewPanel = new RCommandEditorAndPreviewPanel(
                this.fitMaanovaAllPanels);
        this.wizardDialog = new WizardDialog(
                this,
                this.rEditorAndPreviewPanel,
                Maanova.getInstance().getApplicationFrame(),
                "Fit ANOVA Model",
                true,
                WizardDialogType.OK_CANCEL_DIALOG);
    }
    
    /**
     * Show the fitmaanova dialog
     */
    public void show()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            /**
             * {@inheritDoc}
             */
            public void run()
            {
                FitMaanovaWizard.this.wizardDialog.pack();
                FitMaanovaWizard.this.wizardDialog.setVisible(true);
            }
        });
    }
    
    /**
     * {@inheritDoc}
     */
    public void addWizardListener(WizardListener wizardListener)
    {
        this.wizardEventSupport.addWizardListener(wizardListener);
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeWizardListener(WizardListener wizardListener)
    {
        this.wizardEventSupport.removeWizardListener(wizardListener);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isPreviousValid()
    {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean goPrevious() throws IllegalStateException
    {
        throw new IllegalStateException();
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isNextValid()
    {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean goNext() throws IllegalStateException
    {
        throw new IllegalStateException();
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isFinishValid()
    {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean finish() throws IllegalStateException
    {
        if(this.fitMaanovaAllPanels.validateData())
        {
            final RInterface rInterface =
                RInterfaceFactory.getRInterfaceInstance();
            final RCommand[] commands = this.fitMaanovaAllPanels.getCommands();
            final MaanovaProjectManager projectManager =
                MaanovaProjectManager.getInstance();
            
            // TODO this kind of a hack. i want to use
            // evaluateCommandNoReturn, but I can't because the
            // refresh call immediately after will cause swing to block.
            // there is of course a better solution than what I'm doing
            // here, but this works ok
            Runnable evaluateFitRunnable = new Runnable()
            {
                /**
                 * {@inheritDoc}
                 */
                public void run()
                {
                    try
                    {
                        for(RCommand command: commands)
                        {
                            rInterface.evaluateCommand(command);
                        }
                        
                        projectManager.notifyActiveProjectModified();
                        projectManager.refreshProjectDataStructures();
                    }
                    catch(Exception ex)
                    {
                        final String errorMsg =
                            "Error During Fit";
                        LOG.log(Level.SEVERE,
                                errorMsg,
                                ex);
                        MessageDialogUtilities.errorLater(
                                FitMaanovaWizard.this.wizardDialog,
                                ex.getMessage(),
                                errorMsg);
                    }
                }
            };
            
            Thread evaluateFitThread = new Thread(evaluateFitRunnable);
            evaluateFitThread.start();
            
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean cancel()
    {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public void help()
    {
        Maanova.getInstance().showHelp("fit-anova", this.fitMaanovaAllPanels);
    }
}
