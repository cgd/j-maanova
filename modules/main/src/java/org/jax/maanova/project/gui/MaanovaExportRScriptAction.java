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

package org.jax.maanova.project.gui;

import javax.swing.JFrame;

import org.jax.maanova.Maanova;
import org.jax.maanova.configuration.MaanovaApplicationConfigurationManager;
import org.jax.maanova.project.MaanovaProjectManager;
import org.jax.r.configuration.RApplicationConfigurationManager;
import org.jax.r.gui.ExportRScriptAction;
import org.jax.util.project.ProjectManager;

/**
 * A maanova implementation of the abstract {@link ExportRScriptAction} class
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MaanovaExportRScriptAction extends ExportRScriptAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -499243785808892676L;

    /**
     * {@inheritDoc}
     */
    @Override
    protected RApplicationConfigurationManager getApplicationConfigurationManager()
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
}
