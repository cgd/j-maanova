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
import org.jax.maanova.project.MaanovaProjectManager;
import org.jax.maanova.test.MaanovaTestResult;
import org.jax.maanova.test.gui.DisplayTestResultsAction;
import org.jax.maanova.test.gui.VolcanoPlotAction;
import org.jax.util.gui.SafeDeleteAction;

/**
 * The project tree node representation of a maanova test object
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MaanovaTestTreeNode
extends DefaultMutableTreeNode
implements MouseListener
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -2580474038390527695L;

    /**
     * Constructor
     * @param maanovaTestResult
     *          the result data that this tree node is for
     */
    public MaanovaTestTreeNode(MaanovaTestResult maanovaTestResult)
    {
        super(maanovaTestResult, false);
    }
    
    /**
     * Getter for the MAANOVA test result (this is just a typecast version
     * of {@link #getUserObject()}
     * @return
     *          the test result
     */
    public MaanovaTestResult getMaanovaTestResult()
    {
        return (MaanovaTestResult)this.getUserObject();
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
    }

    /**
     * Don't care
     * @param e
     *          the event we don't care about
     */
    public void mouseExited(MouseEvent e)
    {
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
        popupMenu.add(new VolcanoPlotAction(this.getMaanovaTestResult()));
        popupMenu.add(new DisplayTestResultsAction(this.getMaanovaTestResult()));
        popupMenu.addSeparator();
        popupMenu.add(new SafeDeleteAction(
                this.getMaanovaTestResult().toString(),
                maanova.getApplicationFrame(),
                maanova.getDesktop())
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void delete()
            {
                MaanovaTestTreeNode.this.delete();
            }
        });
        
        popupMenu.show(
                (Component)e.getSource(),
                e.getX(),
                e.getY());
    }

    private void delete()
    {
        this.getMaanovaTestResult().delete();
        MaanovaProjectManager projMgr = MaanovaProjectManager.getInstance();
        projMgr.refreshProjectDataStructures();
        projMgr.notifyActiveProjectModified();
    }
}
