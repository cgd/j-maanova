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

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jax.maanova.Maanova;
import org.jax.maanova.madata.MicroarrayExperiment;
import org.jax.maanova.madata.gui.EditGeneListAction;
import org.jax.maanova.project.MaanovaProjectManager;
import org.jax.r.RUtilities;
import org.jax.util.gui.SafeDeleteAction;

/**
 * Class for a gene list tree node
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class GeneListTreeNode
extends DefaultMutableTreeNode
implements MouseListener
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -1198474612166044365L;
    
    private final String geneListId;

    private final MicroarrayExperiment experiment;
    
    /**
     * Constructor
     * @param experiment
     *          the microarray experiment for this node
     * @param geneListId
     *          the gene list ID
     */
    public GeneListTreeNode(MicroarrayExperiment experiment, String geneListId)
    {
        this.experiment = experiment;
        this.geneListId = geneListId;
    }
    
    /**
     * Getter for the experiment
     * @return the experiment
     */
    public MicroarrayExperiment getExperiment()
    {
        return this.experiment;
    }
    
    /**
     * Getter for the gene list ID
     * @return the gene list ID
     */
    public String getGeneListId()
    {
        return this.geneListId;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return RUtilities.fromRIdentifierToReadableName(this.geneListId);
    }

    /**
     * {@inheritDoc}
     */
    public void mouseClicked(MouseEvent e)
    {
        // no-op
    }

    /**
     * {@inheritDoc}
     */
    public void mouseEntered(MouseEvent e)
    {
        // no-op
    }

    /**
     * {@inheritDoc}
     */
    public void mouseExited(MouseEvent e)
    {
        // no-op
    }

    /**
     * {@inheritDoc}
     */
    public void mousePressed(MouseEvent e)
    {
        if(e.isPopupTrigger())
        {
            this.popupTriggered(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void mouseReleased(MouseEvent e)
    {
        if(e.isPopupTrigger())
        {
            this.popupTriggered(e);
        }
    }
    
    /**
     * Respond to a popup trigger event.
     * @param e the event we're responding to
     */
    @SuppressWarnings("serial")
    private void popupTriggered(MouseEvent e)
    {
        Maanova maanova = Maanova.getInstance();
        
        JPopupMenu popupMenu = new JPopupMenu();
        EditGeneListAction editGeneListAction = new EditGeneListAction(
                this.experiment,
                this.geneListId);
        
        popupMenu.add(editGeneListAction);
        popupMenu.addSeparator();
        popupMenu.add(new SafeDeleteAction(
                RUtilities.fromRIdentifierToReadableName(this.geneListId),
                maanova.getApplicationFrame(),
                maanova.getDesktop())
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void delete()
            {
                GeneListTreeNode.this.delete();
            }
        });
        
        popupMenu.show(
                (Component)e.getSource(),
                e.getX(),
                e.getY());
    }

    /**
     * delete this gene list from our R environment
     */
    private void delete()
    {
        this.experiment.removeGeneListNamed(this.geneListId);
        MaanovaProjectManager projMgr = MaanovaProjectManager.getInstance();
        projMgr.refreshProjectDataStructures();
        projMgr.notifyActiveProjectModified();
    }
}
