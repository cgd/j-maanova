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

import java.awt.CardLayout;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import org.jax.maanova.Maanova;
import org.jax.maanova.project.MaanovaProject;
import org.jax.maanova.project.MaanovaProjectManager;
import org.jax.maanova.test.TestModelCommandBuilder;
import org.jax.r.RCommand;
import org.jax.r.gui.RCommandEditor;
import org.jax.r.gui.RCommandEditorListener;
import org.jax.r.gui.RCommandEditorPanel;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.util.gui.BroadcastingWizardController;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.gui.WizardEventSupport;
import org.jax.util.gui.WizardListener;

/**
 * A wizard panel for R/maanova's matest functionality
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MaanovaTestWizardContentPanel
extends RCommandEditorPanel
implements BroadcastingWizardController
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -1689899688802804670L;
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            MaanovaTestWizardContentPanel.class.getName());

    private final WizardEventSupport wizardEventSupport;
    
    private final TestModelCommandBuilder commandBuilder;
    
    private final MaanovaTestInitialPanel maanovaTestInitialPanel;
    
    private final MaanovaTTestContrastsPanel maanovaTTestContrastsPanel;
    
    private final MaanovaTestFinalPanel maanovaTestFinalPanel;
    
    private final RCommandEditorListener internalCommandEditorListener =
        new RCommandEditorListener()
        {
            public void commandModified(RCommandEditor editor)
            {
                MaanovaTestWizardContentPanel.this.fireCommandModified();
            }
        };
    
    private volatile JPanel activePanel;
    
    private final CardLayout cardLayout;
    
    /**
     * Constructor
     * @param project
     *          the project that we're doing a maanova test for
     */
    public MaanovaTestWizardContentPanel(MaanovaProject project)
    {
        this.wizardEventSupport = new WizardEventSupport(this);
        this.commandBuilder = new TestModelCommandBuilder();
        
        this.maanovaTestInitialPanel = new MaanovaTestInitialPanel(
                project,
                this.commandBuilder);
        this.maanovaTTestContrastsPanel = new MaanovaTTestContrastsPanel(
                this.commandBuilder);
        this.maanovaTestFinalPanel = new MaanovaTestFinalPanel(
                this.commandBuilder);
        
        this.maanovaTestInitialPanel.addRCommandEditorListener(
                this.internalCommandEditorListener);
        this.maanovaTTestContrastsPanel.addRCommandEditorListener(
                this.internalCommandEditorListener);
        this.maanovaTestFinalPanel.addRCommandEditorListener(
                this.internalCommandEditorListener);
        
        this.cardLayout = new CardLayout();
        this.setLayout(this.cardLayout);
        
        this.activePanel = this.maanovaTestInitialPanel;
        
        this.add(this.maanovaTestInitialPanel,
                 this.maanovaTestInitialPanel.getClass().getName());
        this.add(this.maanovaTTestContrastsPanel,
                 this.maanovaTTestContrastsPanel.getClass().getName());
        this.add(this.maanovaTestFinalPanel,
                 this.maanovaTestFinalPanel.getClass().getName());
    }
    
    private void setActivePanel(JPanel newActivePanel)
    {
        this.activePanel = newActivePanel;
        this.cardLayout.show(this, newActivePanel.getClass().getName());
    }
    
    /**
     * {@inheritDoc}
     */
    public RCommand[] getCommands()
    {
        return new RCommand[] {this.commandBuilder.getCommand()};
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
        this.wizardEventSupport.addWizardListener(wizardListener);
    }

    /**
     * {@inheritDoc}
     */
    public boolean cancel()
    {
        this.wizardEventSupport.fireWizardCancelled();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean finish() throws IllegalStateException
    {
        if(this.activePanel == this.maanovaTestFinalPanel)
        {
            if(this.maanovaTestFinalPanel.validateData())
            {
                final RInterface rInterface =
                    RInterfaceFactory.getRInterfaceInstance();
                final RCommand command = this.commandBuilder.getCommand();
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
                            rInterface.evaluateCommand(command);
                            projectManager.notifyActiveProjectModified();
                            projectManager.refreshProjectDataStructures();
                        }
                        catch(Exception ex)
                        {
                            final String errorMsg =
                                "Error During MAANOVA Test";
                            LOG.log(Level.SEVERE,
                                    errorMsg,
                                    ex);
                            MessageDialogUtilities.errorLater(
                                    MaanovaTestWizardContentPanel.this,
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
        else
        {
            throw new IllegalStateException();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean goNext() throws IllegalStateException
    {
        if(this.activePanel == this.maanovaTestInitialPanel)
        {
            if(this.maanovaTestInitialPanel.validateData())
            {
                switch(this.commandBuilder.getTestType())
                {
                    case F_TEST:
                    {
                        this.setActivePanel(this.maanovaTestFinalPanel);
                    }
                    break;
                    
                    case T_TEST:
                    {
                        this.maanovaTTestContrastsPanel.maybeReinitializeContrastMatrix();
                        this.setActivePanel(this.maanovaTTestContrastsPanel);
                    }
                    break;
                    
                    default:
                    {
                        throw new IllegalStateException(
                                "unexpected test type: " +
                                this.commandBuilder.toString());
                    }
                }
                return true;
            }
            else
            {
                return false;
            }
        }
        else if(this.activePanel == this.maanovaTTestContrastsPanel)
        {
            if(this.maanovaTTestContrastsPanel.validateData())
            {
                this.setActivePanel(this.maanovaTestFinalPanel);
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            throw new IllegalStateException();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean goPrevious() throws IllegalStateException
    {
        if(this.activePanel == this.maanovaTestFinalPanel)
        {
            switch(this.commandBuilder.getTestType())
            {
                case F_TEST:
                {
                    this.setActivePanel(this.maanovaTestInitialPanel);
                }
                break;
                
                case T_TEST:
                {
                    this.setActivePanel(this.maanovaTTestContrastsPanel);
                }
                break;
                
                default:
                {
                    throw new IllegalStateException(
                            "unexpected test type: " +
                            this.commandBuilder.toString());
                }
            }
            return true;
        }
        else if(this.activePanel == this.maanovaTTestContrastsPanel)
        {
            this.setActivePanel(this.maanovaTestInitialPanel);
            return true;
        }
        else
        {
            throw new IllegalStateException();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void help()
    {
        Maanova.getInstance().showHelp("test-anova", this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFinishValid()
    {
        return this.activePanel == this.maanovaTestFinalPanel;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isNextValid()
    {
        return this.activePanel != this.maanovaTestFinalPanel;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPreviousValid()
    {
        return this.activePanel != this.maanovaTestInitialPanel;
    }
}
