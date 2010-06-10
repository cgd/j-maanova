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

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jax.maanova.Maanova;
import org.jax.maanova.fit.FitMaanovaResult;
import org.jax.maanova.fit.gui.ResidualPlotAction;
import org.jax.maanova.project.MaanovaProjectManager;
import org.jax.util.gui.SafeDeleteAction;

/**
 * The project tree node representation of a fit maanova object
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class FitMaanovaTreeNode
extends DefaultMutableTreeNode
implements MouseListener
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 4835226858149279841L;

    /**
     * Constructor
     * @param fitMaanovaResult
     *          the result data that this tree node is for
     */
    public FitMaanovaTreeNode(FitMaanovaResult fitMaanovaResult)
    {
        super(fitMaanovaResult, false);
    }
    
    /**
     * Get the fit maanova data object for this tree node
     * @return
     *          the fit maanova data object
     */
    public FitMaanovaResult getFitMaanovaResult()
    {
        return (FitMaanovaResult)this.getUserObject();
    }
    
    /**
     * {@inheritDoc}
     */
    public void mouseClicked(MouseEvent e)
    {
        if(e.isPopupTrigger())
        {
            this.popupTriggered(e);
        }
    }

    /**
     * Don't care
     * @param e
     *          the event we don't care about
     */
    public void mouseEntered(MouseEvent e)
    {
        // no-op
    }

    /**
     * Don't care
     * @param e
     *          the event we don't care about
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
     * @param e
     *          the event we're responding to
     */
    @SuppressWarnings("serial")
    private void popupTriggered(MouseEvent e)
    {
        Maanova maanova = Maanova.getInstance();
        
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(new ResidualPlotAction(this.getFitMaanovaResult()));
        popupMenu.addSeparator();
        popupMenu.add(new SafeDeleteAction(
                this.getFitMaanovaResult().toString(),
                maanova.getApplicationFrame(),
                maanova.getDesktop())
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void delete()
            {
                FitMaanovaTreeNode.this.delete();
            }
        });
        
        popupMenu.show(
                (Component)e.getSource(),
                e.getX(),
                e.getY());
    }

    private void delete()
    {
        this.getFitMaanovaResult().delete();
        MaanovaProjectManager projMgr = MaanovaProjectManager.getInstance();
        projMgr.refreshProjectDataStructures();
        projMgr.notifyActiveProjectModified();
    }
}
