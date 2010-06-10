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

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.jax.maanova.Maanova;
import org.jax.maanova.project.MaanovaProjectManager;
import org.jax.r.gui.ApplicationFrame;
import org.jax.r.project.LoadProjectFileTask;
import org.jax.util.TextWrapper;
import org.jax.util.project.ProjectManager;

/**
 * An action for launching a recently opened MAANOVA project
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class LoadRecentMaanovaProjectAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 36996041493958443L;

    private final File maanovaProjectFile;

    /**
     * Constructor
     * @param index
     *          the file index (used for the label)
     * @param maanovaProjectFile
     *          the project file
     */
    public LoadRecentMaanovaProjectAction(
            int index,
            File maanovaProjectFile)
    {
        super((index + 1) + ". " + maanovaProjectFile.getName());
        
        this.maanovaProjectFile = maanovaProjectFile;
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        // prompt the user if they're about to lose unsaved changes
        ProjectManager projectManager = MaanovaProjectManager.getInstance();
        if(projectManager.isActiveProjectModified())
        {
            String message =
                "The current project contains unsaved modifications. Loading " +
                "a new project will cause these modifications to be lost. " +
                "Would you like to continue without saving?";
            int response = JOptionPane.showConfirmDialog(
                    Maanova.getInstance().getApplicationFrame(),
                    TextWrapper.wrapText(
                            message,
                            TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                    "Unsaved Project Modifications",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if(response == JOptionPane.CLOSED_OPTION || response == JOptionPane.CANCEL_OPTION)
            {
                return;
            }
        }
        
        ApplicationFrame appFrame = Maanova.getInstance().getApplicationFrame();
        LoadProjectFileTask loadTask = new LoadProjectFileTask(
                this.maanovaProjectFile,
                projectManager,
                appFrame,
                "J/maanova");
        appFrame.getTaskProgressPanel().addTaskToTrack(loadTask, true);
        Thread loadThread = new Thread(loadTask);
        loadThread.start();
    }
}
