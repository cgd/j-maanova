/*
 * Copyright (c) 2010 The Jackson Laboratory
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

package org.jax.maanova;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.jax.maanova.configuration.MaanovaApplicationConfigurationManager;
import org.jax.maanova.fit.FitMaanovaResult;
import org.jax.maanova.fit.gui.FitMaanovaAction;
import org.jax.maanova.fit.gui.ResidualPlotAction;
import org.jax.maanova.madata.MicroarrayExperiment;
import org.jax.maanova.madata.gui.ArrayScatterPlotAction;
import org.jax.maanova.madata.gui.ExportDataAction;
import org.jax.maanova.madata.gui.ImportAffymetrixDataAction;
import org.jax.maanova.madata.gui.ImportTabDelimitedMicroarrayDataAction;
import org.jax.maanova.madata.gui.ShowExperimentDesignAction;
import org.jax.maanova.project.MaanovaDataModel;
import org.jax.maanova.project.MaanovaProjectManager;
import org.jax.maanova.project.gui.MaanovaExportRDataAction;
import org.jax.maanova.project.gui.MaanovaExportRScriptAction;
import org.jax.maanova.project.gui.RecentMaanovaProjectsMenu;
import org.jax.maanova.project.gui.SaveMaanovaProjectAction;
import org.jax.maanova.project.gui.SaveMaanovaProjectAsAction;
import org.jax.maanova.test.MaanovaTestResult;
import org.jax.maanova.test.gui.DisplayTestResultsAction;
import org.jax.maanova.test.gui.MaanovaTestModelAction;
import org.jax.maanova.test.gui.VolcanoPlotAction;
import org.jax.r.configuration.RApplicationConfigurationManager;
import org.jax.r.project.LoadProjectAction;
import org.jax.util.concurrent.MultiTaskProgressPanel;
import org.jax.util.project.ProjectManager;

/**
 * The class responsible for managing the maanova application's main menu bar
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MaanovaMainMenuManager
{
    private final JMenuBar menuBar;
    private FitMaanovaAction fitMaanovaAction;
    private MaanovaTestModelAction maanovaTestAction;
    
    private final JMenu showDesignMenu;
    private final JMenuItem noDesignsToShowItem;
    
    private final JMenu arraysToPlotMenu;
    private final JMenuItem noArraysToPlotItem;
    
    private final JMenu exportDataToCSVMenu;
    private final JMenuItem noDataToExportItem;
    
    private final JMenu fitsToPlotMenu;
    private final JMenuItem noFitsToPlotItem;
    
    private final JMenu testsToPlotMenu;
    private final JMenuItem noTestsToPlotItem;
    
    private final JMenu testsToShowMenu;
    private final JMenuItem noTestsToShowItem;
    
    /**
     * Constructor
     * @param windowMenu    the window menu
     */
    public MaanovaMainMenuManager(JMenu windowMenu)
    {
        this.menuBar = new JMenuBar();
        
        this.showDesignMenu = new JMenu("Show Experiment Design");
        this.noDesignsToShowItem = new JMenuItem("No Experiments Loaded");
        this.noDesignsToShowItem.setEnabled(false);
        
        this.arraysToPlotMenu = new JMenu("Plot Array Intensities");
        this.noArraysToPlotItem = new JMenuItem("No Arrays to Plot");
        this.noArraysToPlotItem.setEnabled(false);
        
        this.exportDataToCSVMenu = new JMenu("Export Microarray Data to CSV");
        this.noDataToExportItem = new JMenuItem("No Data to Export");
        this.noDataToExportItem.setEnabled(false);
        
        this.fitsToPlotMenu = new JMenu("Plot Resuduals vs. Estimates");
        this.noFitsToPlotItem = new JMenuItem("No Fits to Plot");
        this.noFitsToPlotItem.setEnabled(false);
        
        this.testsToPlotMenu = new JMenu("Plot Test Results");
        this.noTestsToPlotItem = new JMenuItem("No Test Results to Plot");
        this.noTestsToPlotItem.setEnabled(false);
        
        this.testsToShowMenu = new JMenu("Test Results Tables");
        this.noTestsToShowItem = new JMenuItem("No Test Results to Show");
        this.noTestsToShowItem.setEnabled(false);
        
        this.initializeMenuBar(windowMenu);
    }
    
    /**
     * Getter for the menu bar that we're managing
     * @return the menu bar
     */
    public JMenuBar getMenuBar()
    {
        return this.menuBar;
    }
    
    /**
     * Fill in and initialize all of the menu bar actions.
     * @param windowMenu    the window menu
     */
    @SuppressWarnings("serial")
    private void initializeMenuBar(JMenu windowMenu)
    {
        MenuListener refreshMenuListener = new MenuListener()
        {
            /**
             * {@inheritDoc}
             */
            public void menuSelected(MenuEvent e)
            {
                MaanovaMainMenuManager.this.refreshMenu();
            }
            
            /**
             * {@inheritDoc}
             */
            public void menuCanceled(MenuEvent e)
            {
                // don't care
            }
            
            /**
             * {@inheritDoc}
             */
            public void menuDeselected(MenuEvent e)
            {
                // don't care
            }
        };
        
        // initialize the file menu
        final JMenu fileMenu = new JMenu("File");
        fileMenu.add(new LoadProjectAction()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            protected RApplicationConfigurationManager getConfigurationManager()
            {
                return MaanovaApplicationConfigurationManager.getInstance();
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            protected JFrame getParentFrame()
            {
                return Maanova.getInstance().getApplicationFrame();
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            protected ProjectManager getProjectManager()
            {
                return MaanovaProjectManager.getInstance();
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            protected String getProjectTypeName()
            {
                return "J/maanova";
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            protected MultiTaskProgressPanel getTaskProgressPanel()
            {
                return Maanova.getInstance().getApplicationFrame().getTaskProgressPanel();
            }
        });
        fileMenu.add(new RecentMaanovaProjectsMenu());
        
        final JMenu importMicroarrayDataMenu = new JMenu("Load Microarray Data");
        importMicroarrayDataMenu.add(new ImportTabDelimitedMicroarrayDataAction());
        importMicroarrayDataMenu.add(new ImportAffymetrixDataAction());
        
        fileMenu.add(importMicroarrayDataMenu);
        fileMenu.add(new JSeparator());
        fileMenu.add(new SaveMaanovaProjectAction());
        fileMenu.add(new SaveMaanovaProjectAsAction());
        fileMenu.add(new MaanovaExportRScriptAction());
        fileMenu.add(new MaanovaExportRDataAction());
        fileMenu.add(this.exportDataToCSVMenu);
        fileMenu.add(new JSeparator());
        fileMenu.add(new AbstractAction("Quit")
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                Maanova maanova = Maanova.getInstance();
                maanova.getApplicationFrame().closeApplication();
            }
        });
        this.menuBar.add(fileMenu);
        
        fileMenu.addMenuListener(refreshMenuListener);
        
        // the analysis menu
        final JMenu analysisMenu = new JMenu("Analysis");
        
        analysisMenu.add(this.showDesignMenu);
        analysisMenu.add(this.arraysToPlotMenu);
        analysisMenu.addSeparator();
        
        this.fitMaanovaAction = new FitMaanovaAction();
        analysisMenu.add(this.fitMaanovaAction);
        analysisMenu.add(this.fitsToPlotMenu);
        analysisMenu.addSeparator();
        
        // the test menu
        this.maanovaTestAction = new MaanovaTestModelAction();
        analysisMenu.add(this.maanovaTestAction);
        analysisMenu.add(this.testsToShowMenu);
        analysisMenu.add(this.testsToPlotMenu);
        
        this.menuBar.add(analysisMenu);
        
        analysisMenu.addMenuListener(refreshMenuListener);
        
        // the window menu
        this.menuBar.add(windowMenu);
        
        // the help menu
        final JMenu helpMenu = new JMenu("Help");
        
        final JMenuItem helpTopicsMenuItem = new JMenuItem("Help Topics...");
        helpTopicsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        Icon helpIcon = new ImageIcon(MaanovaMainMenuManager.class.getResource(
                "/images/action/help-16x16.png"));
        helpTopicsMenuItem.setIcon(helpIcon);
        helpTopicsMenuItem.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                Maanova.getInstance().showHelp("first-topic");
            }
        });
        helpMenu.add(helpTopicsMenuItem);
        
        this.menuBar.add(helpMenu);
    }

    private void refreshMenu()
    {
        MaanovaDataModel dataModel =
            MaanovaProjectManager.getInstance().getActiveProject().getDataModel();
        MicroarrayExperiment[] experiments = dataModel.getMicroarrays();
        
        List<FitMaanovaResult> fitResults = new ArrayList<FitMaanovaResult>();
        List<MaanovaTestResult> testResults = new ArrayList<MaanovaTestResult>();
        
        for(MicroarrayExperiment currArray: experiments)
        {
            fitResults.addAll(currArray.getFitMaanovaResults());
            testResults.addAll(currArray.getMaanovaTestResults());
        }
        
        this.fitMaanovaAction.setEnabled(experiments.length >= 1);
        this.maanovaTestAction.setEnabled(!fitResults.isEmpty());
        
        this.showDesignMenu.removeAll();
        this.arraysToPlotMenu.removeAll();
        this.exportDataToCSVMenu.removeAll();
        if(experiments.length == 0)
        {
            this.showDesignMenu.add(this.noDesignsToShowItem);
            this.arraysToPlotMenu.add(this.noArraysToPlotItem);
            this.exportDataToCSVMenu.add(this.noDataToExportItem);
        }
        else
        {
            for(MicroarrayExperiment experiment: experiments)
            {
                String exprName = experiment.toString();
                this.showDesignMenu.add(new ShowExperimentDesignAction(
                        exprName + " Design...",
                        experiment));
                this.arraysToPlotMenu.add(new ArrayScatterPlotAction(
                        exprName + " Arrays...",
                        experiment));
                this.exportDataToCSVMenu.add(new ExportDataAction(
                        "Export " + exprName + " Data...",
                        experiment));
            }
        }
        
        this.fitsToPlotMenu.removeAll();
        if(fitResults.isEmpty())
        {
            this.fitsToPlotMenu.add(this.noFitsToPlotItem);
        }
        else
        {
            for(FitMaanovaResult currFit: fitResults)
            {
                this.fitsToPlotMenu.add(new ResidualPlotAction(
                        currFit.toString(),
                        currFit));
            }
        }
        
        this.testsToShowMenu.removeAll();
        this.testsToPlotMenu.removeAll();
        if(testResults.isEmpty())
        {
            this.testsToShowMenu.add(this.noTestsToShowItem);
            this.testsToPlotMenu.add(this.noTestsToPlotItem);
        }
        else
        {
            for(MaanovaTestResult currTest: testResults)
            {
                this.testsToShowMenu.add(new DisplayTestResultsAction(
                        currTest.toString(),
                        currTest));
                this.testsToPlotMenu.add(new VolcanoPlotAction(currTest));
            }
        }
    }
}
