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

import java.awt.Frame;

import javax.swing.Icon;

import org.jax.maanova.Maanova;
import org.jax.maanova.project.MaanovaProjectManager;
import org.jax.util.project.AbstractSaveProjectAction;
import org.jax.util.project.ProjectManager;

/**
 * Abstract base for "save" and "save as"
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public abstract class AbstractSaveMaanovaProjectAction extends AbstractSaveProjectAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -4191370087639208414L;
    
    /**
     * Constructor
     * @param name
     *          the action name
     * @param icon
     *          the action icon
     */
    public AbstractSaveMaanovaProjectAction(String name, Icon icon)
    {
        super(name, icon);
    }
    
    /**
     * Constructor
     * @param name
     *          the action name
     */
    public AbstractSaveMaanovaProjectAction(String name)
    {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Frame getParentFrame()
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
