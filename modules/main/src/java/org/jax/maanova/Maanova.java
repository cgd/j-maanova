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
package org.jax.maanova;

import java.awt.Component;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.help.DefaultHelpBroker;
import javax.help.HelpSet;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import org.jax.maanova.configuration.MaanovaApplicationConfigurationManager;
import org.jax.maanova.project.MaanovaProjectManager;
import org.jax.maanova.project.gui.MaanovaProjectTree;
import org.jax.r.gui.ApplicationFrame;
import org.jax.r.jriutilities.BioconductorPackageDependency;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.r.jriutilities.RPackageDependency;
import org.jax.r.jriutilities.RPackageDependency.PackageStatus;
import org.jax.util.TypeSafeSystemProperties;
import org.jax.util.TypeSafeSystemProperties.OsFamily;
import org.jax.util.datastructure.SequenceUtilities;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.gui.desktoporganization.Desktop;
import org.jax.util.project.ProjectManager;

/**
 * The main J/maanova class that gets launched by the {@link MaanovaLauncher}
 */
public class Maanova
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            Maanova.class.getName());
    
    private static Maanova instance;
    
    private final Desktop desktop;
    private final MaanovaProjectTree projectTree;

    private final ApplicationFrame applicationFrame;
    private final MaanovaMainMenuManager maanovaMainMenuManager;
    private DefaultHelpBroker helpBroker;
    private HelpSet helpSet;
    
    private final RPackageDependency[] dependencies;
    
    /**
     * Getter for the singleton Maanova instance
     * @return
     *          the singleton instance
     */
    public static Maanova getInstance()
    {
        // we have 2 if's so that we don't need to waste time synchronizing
        // unless it's needed
        if(Maanova.instance == null)
        {
            synchronized(Maanova.class)
            {
                if(Maanova.instance == null)
                {
                    Maanova.instance = new Maanova();
                }
            }
        }
        return Maanova.instance;
    }
    
    /**
     *  constructor for initial screen
     */
    private Maanova()
    {
        RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
        this.dependencies = new RPackageDependency[] {
                new BioconductorPackageDependency(rInterface, "maanova", "1.16.0"),
                new BioconductorPackageDependency(rInterface, "affy", "1.22.0")};
        
        this.initializeHelp();
        this.desktop = new Desktop();
        
        this.projectTree = new MaanovaProjectTree();
        this.projectTree.setProjectManager(MaanovaProjectManager.getInstance());
        
        this.maanovaMainMenuManager = new MaanovaMainMenuManager(
                this.desktop.getWindowMenu());
        
        final MaanovaProjectManager projectManager =
            MaanovaProjectManager.getInstance();
        this.applicationFrame = new ApplicationFrame(
                "J/maanova - GUI for R/maanova ",
                rInterface,
                this.maanovaMainMenuManager.getMenuBar(),
                this.desktop,
                this.projectTree,
                projectManager);
        
        this.showGui();
        this.initialCallsToR();
        
        projectManager.addPropertyChangeListener(
                ProjectManager.ACTIVE_PROJECT_PROPERTY_NAME,
                new PropertyChangeListener()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        Maanova.this.getDesktop().closeAllWindows();
                    }
                });
        
        projectManager.addPropertyChangeListener(
                ProjectManager.ACTIVE_PROJECT_FILE_PROPERTY_NAME,
                new PropertyChangeListener()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        MaanovaApplicationConfigurationManager.getInstance().notifyActiveProjectFileChanged(
                                projectManager.getActiveProjectFile());
                    }
                });
        
        // Show tool tips immediately
        ToolTipManager.sharedInstance().setInitialDelay(0);
        // Keep the tool tip showing through the whole program
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
    }
    
    private void initializeHelp()
    {
        try
        {
            URL hsURL = HelpSet.findHelpSet(
                    Maanova.class.getClassLoader(),
                    "org-jax-maanova-help/hs/main.hs");
            this.helpSet = new HelpSet(null, hsURL);
            this.helpBroker = (DefaultHelpBroker)this.helpSet.createHelpBroker();
        }
        catch(Exception ex)
        {
            LOG.log(Level.WARNING, "Failed to initialize application help", ex);
        }
    }
    
    /**
     * Getter for the help broker
     * @return the help broker
     */
    public DefaultHelpBroker getHelpBroker()
    {
        return this.helpBroker;
    }
    
    /**
     * Getter for the help set
     * @return the help set
     */
    public HelpSet getHelpSet()
    {
        return this.helpSet;
    }

    /**
     * Getter for the main application frame
     * @return
     *          the application frame
     */
    public ApplicationFrame getApplicationFrame()
    {
        return this.applicationFrame;
    }

    /**
     * Getter for the desktop
     * @return
     *          the desktop
     */
    public Desktop getDesktop()
    {
        return this.desktop;
    }

    /**
     * Getter for the project tree
     * @return
     *          the project tree
     */
    public MaanovaProjectTree getProjectTree()
    {
        return this.projectTree;
    }

    /**
     * Execute some initialization calls to R.
     */
    private void initialCallsToR()
    {
        MaanovaApplicationConfigurationManager configurationManager =
            MaanovaApplicationConfigurationManager.getInstance();
        configurationManager.setSaveOnExit(true);
        
        // intial calls to R
        final RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
        if(TypeSafeSystemProperties.getOsFamily() == OsFamily.WINDOWS_OS_FAMILY)
        {
            String comment = "increase memory allocation on Windows";
            rInterface.insertComment(comment);
            rInterface.evaluateCommandNoReturn("memory.limit(2048)");
        }
        
        List<RPackageDependency> dependenciesToInstall =
            new ArrayList<RPackageDependency>();
        for(RPackageDependency dependency : this.dependencies)
        {
            dependency.showVersionInfo();
            if(dependency.getPackageStatus() != PackageStatus.PACKAGE_OK)
            {
                dependenciesToInstall.add(dependency);
            }
        }
        
        if(!dependenciesToInstall.isEmpty())
        {
            List<String> missingPackageNames = new ArrayList<String>();
            for(RPackageDependency currDep : dependenciesToInstall)
            {
                missingPackageNames.add(
                    currDep.getPackageName() + " (" +
                    currDep.getMinimumVersion() + ")");
            }
            String missingMessage =
                "The following required package dependencies are either missing or " +
                "out of date: " + SequenceUtilities.toString(missingPackageNames) +
                ". Would you like J/maanova to try to install these " +
                "dependencies automatically?";
            boolean doInstall = MessageDialogUtilities.ask(
                    this.applicationFrame,
                    missingMessage,
                    "Install Missing Packages");
            
            if(doInstall)
            {
                for(RPackageDependency dependency : dependenciesToInstall)
                {
                    dependency.installPackage();
                    dependency.showVersionInfo();
                    switch(dependency.getPackageStatus())
                    {
                        case PACKAGE_OK:
                        {
                            // no-op
                        }
                        break;
                        
                        case PACKAGE_MISSING:
                        {
                            MessageDialogUtilities.errorLater(
                                    this.applicationFrame,
                                    "Failed to install " + dependency.getPackageName() +
                                    ". Please install the package manually.",
                                    "Package Install Failed");
                        }
                        return;
                        
                        case PACKAGE_TOO_OLD:
                        {
                            MessageDialogUtilities.errorLater(
                                    this.applicationFrame,
                                    "The installed version of " + dependency.getPackageName() +
                                    " is still too old. The minimum required " +
                                    "version is " + dependency.getMinimumVersion() +
                                    " but found version " + dependency.getInstalledVersion() +
                                    ". Please install the required version manually: " +
                                    dependency.getPackageName() + " " +
                                    dependency.getMinimumVersion() + " or greater",
                                    "Package Out of Date");
                        }
                        return;
                    }
                }
            }
        }
        
        for(RPackageDependency dependency : this.dependencies)
        {
            dependency.loadPackage();
        }
    }

    /**
     * Create the GUI and show it.
     */
    private void showGui()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            /**
             * {@inheritDoc}
             */
            public void run()
            {
                Maanova.this.applicationFrame.setVisible(true);
            }
        });
    }
    
    /**
     * Show help using the application frame as the parent component
     * @param helpId the help ID to show
     */
    public void showHelp(String helpId)
    {
        this.showHelp(helpId, this.getApplicationFrame());
    }

    /**
     * Show help
     * @param helpId            the help ID to show
     * @param parentComponent   the parent component to use
     */
    public void showHelp(String helpId, Component parentComponent)
    {
        Window parentWindow = org.jax.util.gui.SwingUtilities.getContainingWindow(
                parentComponent);
        this.showHelp(helpId, parentWindow);
    }

    /**
     * Show help
     * @param helpId        the help ID to show
     * @param parentWindow  the parent window to use
     */
    public void showHelp(String helpId, Window parentWindow)
    {
        try
        {
            DefaultHelpBroker hb = Maanova.getInstance().getHelpBroker();
            if(!hb.isDisplayed())
            {
                hb.setActivationWindow(parentWindow);
                hb.setDisplayed(true);
            }
            hb.setCurrentID(helpId);
        }
        catch(Exception ex)
        {
            String message = "Failed to show help window.";
            LOG.log(Level.WARNING, message, ex);
            MessageDialogUtilities.warn(
                    Maanova.getInstance().getApplicationFrame(),
                    message,
                    "Help Failed");
        }
    }

    /**
     * The entry point for the J/maanova application.
     * @param args
     *      command line arguments
     */
    public static void main (String[] args)
    {
        // set the look and feel
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                /**
                 * {@inheritDoc}
                 */
                public void run()
                {
                    try
                    {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    }
                    catch(Exception ex)
                    {
                        LOG.log(Level.WARNING, "failed to set system look-and-feel", ex);
                    }
                }
            });
        }
        catch(Exception ex)
        {
            LOG.log(Level.WARNING,
                    "caught exception trying to set look-and-feel",
                    ex);
        }

        Maanova.writeSystemConfigurationToLog();
        Maanova.getInstance();
    }

    /**
     * Log some system configuration information
     */
    private static void writeSystemConfigurationToLog()
    {
        if(LOG.isLoggable(Level.FINE))
        {
            LOG.fine("Environment Variables:");
            for(Map.Entry<String, String> currEnvEntry: System.getenv().entrySet())
            {
                LOG.fine(currEnvEntry.getKey() + "=" + currEnvEntry.getValue());
            }
            
            LOG.fine("Properties:");
            for(Map.Entry<Object, Object> currEnvEntry: System.getProperties().entrySet())
            {
                LOG.fine(currEnvEntry.getKey() + "=" + currEnvEntry.getValue());
            }
            
            LOG.fine("Max Memory: " + Runtime.getRuntime().maxMemory());
        }
    }
}
