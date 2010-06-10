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

package org.jax.maanova.project.gui;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.jax.maanova.Maanova;
import org.jax.maanova.project.MaanovaProjectManager;
import org.jax.util.TextWrapper;
import org.jax.util.project.ProjectManager;

/**
 * Action for saving the maanova project
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SaveMaanovaProjectAction extends AbstractSaveMaanovaProjectAction
{
    /**
     * every {@link java.io.Serializable} has one of these
     */
    private static final long serialVersionUID = -4435938386490315099L;

    /**
     * logger
     */
    private static final Logger LOG = Logger.getLogger(
            SaveMaanovaProjectAction.class.getName());
    
    /**
     * the name the user sees
     */
    private static final String ACTION_NAME = "Save Project";
    
    /**
     * the icon resource location
     */
    private static final String ICON_RESOURCE_LOCATION =
        "/images/action/save-16x16.png";
    
    /**
     * Constructor
     */
    public SaveMaanovaProjectAction()
    {
        super(ACTION_NAME,
              new ImageIcon(SaveMaanovaProjectAction.class.getResource(
                          ICON_RESOURCE_LOCATION)));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void performSave()
    {
        SaveMaanovaProjectAction.saveProject();
    }
    
    /**
     * Save the project
     */
    private static void saveProject()
    {
        ProjectManager projectManager = MaanovaProjectManager.getInstance();
        File activeProjFile = projectManager.getActiveProjectFile();
        
        if(activeProjFile == null)
        {
            // since we don't have an active file this is the same as a
            // "save as"
            if(LOG.isLoggable(Level.FINE))
            {
                LOG.fine(
                        "calling save as since we don't have an existing " +
                        "file name for the project");
            }
            SaveMaanovaProjectAsAction.saveProjectAs();
        }
        else
        {
            // try to save to the project file
            if(!projectManager.saveActiveProject(activeProjFile))
            {
                // there was a problem... tell the user
                String message =
                    "Failed to save to selected J/maanova project file: " +
                    activeProjFile.getAbsolutePath();
                LOG.info(message);
                
                JOptionPane.showMessageDialog(
                        Maanova.getInstance().getApplicationFrame(),
                        TextWrapper.wrapText(
                                message,
                                TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                        "Error Saving Project",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
