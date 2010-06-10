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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jax.maanova.madata.MicroarrayExperiment;
import org.jax.util.gui.Iconifiable;

/**
 * Tree node for displaying gene lists
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class GeneListsTreeNode
extends DefaultMutableTreeNode
implements MouseListener, Iconifiable
{
    /**
     * every {@link java.io.Serializable} is supposed
     */
    private static final long serialVersionUID = -5079891464261909784L;

    private static final String ICON_RESOURCE =
        "/images/genelists-16x16.png";
    private static final Icon SHARED_ICON;
    static
    {
        URL iconUrl = GeneListsTreeNode.class.getResource(ICON_RESOURCE);
        SHARED_ICON = new ImageIcon(iconUrl);
    }

    private final MicroarrayExperiment microarrayExperiment;
    
    /**
     * Constructor
     * @param microarrayExperiment
     *          the experiment that this gene list node is for
     */
    public GeneListsTreeNode(MicroarrayExperiment microarrayExperiment)
    {
        this.microarrayExperiment = microarrayExperiment;
    }
    
    /**
     * Getter for the experiment
     * @return the microarrayExperiment
     */
    public MicroarrayExperiment getMicroarrayExperiment()
    {
        return this.microarrayExperiment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        int childCount = this.getChildCount();
        return
            "Gene Lists (" +
            (childCount == 0 ? "empty" : childCount) +
            ")";
    }

    /**
     * {@inheritDoc}
     */
    public void mouseClicked(MouseEvent e)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void mouseEntered(MouseEvent e)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void mouseExited(MouseEvent e)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void mousePressed(MouseEvent e)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void mouseReleased(MouseEvent e)
    {
    }

    /**
     * {@inheritDoc}
     */
    public Icon getIcon()
    {
        return SHARED_ICON;
    }
}
