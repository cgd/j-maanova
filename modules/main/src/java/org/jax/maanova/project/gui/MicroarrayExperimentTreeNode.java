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
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jax.maanova.Maanova;
import org.jax.maanova.madata.MicroarrayExperiment;
import org.jax.maanova.madata.gui.ArrayScatterPlotAction;
import org.jax.maanova.madata.gui.ExportDataAction;
import org.jax.maanova.madata.gui.ShowExperimentDesignAction;
import org.jax.maanova.project.MaanovaProjectManager;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.util.gui.Iconifiable;
import org.jax.util.gui.SafeDeleteAction;
import org.jax.util.gui.SafeDeleteAction.DeleteMessage;

/**
 * Tree node for microarray data
 */
class MicroarrayExperimentTreeNode
extends DefaultMutableTreeNode
implements MouseListener, Iconifiable
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 168223299L;
    
    private static final String ICON_RESOURCE =
        "/images/microarray-16x16.png";
    private static final Icon SHARED_ICON;
    static
    {
        URL iconUrl = MicroarrayExperimentTreeNode.class.getResource(ICON_RESOURCE);
        SHARED_ICON = new ImageIcon(iconUrl);
    }
    
    private final FitMaanovasTreeNode fitMaanovasTreeNode;

    private final MaanovaTestsTreeNode maanovaTestsTreeNode;
    
    private final GeneListsTreeNode geneListsTreeNode;

    private volatile String nodeString;
    
    /**
     * Constructor
     * @param microarrayExperiment
     *          the microarray experiment for this node
     */
    public MicroarrayExperimentTreeNode(MicroarrayExperiment microarrayExperiment)
    {
        super(microarrayExperiment);
        this.fitMaanovasTreeNode = new FitMaanovasTreeNode(microarrayExperiment);
        this.maanovaTestsTreeNode = new MaanovaTestsTreeNode(microarrayExperiment);
        this.geneListsTreeNode = new GeneListsTreeNode(microarrayExperiment);
    }
    
    /**
     * Getter for the tree node used for all of the "fit" objects
     * @return the fitMaanovasTreeNode
     */
    public FitMaanovasTreeNode getFitMaanovasTreeNode()
    {
        return this.fitMaanovasTreeNode;
    }
    
    /**
     * Getter for the microarray experiment
     * @return
     *          the microarray data
     */
    public MicroarrayExperiment getMicroarrayExperiment()
    {
        return (MicroarrayExperiment)this.getUserObject();
    }
    
    /**
     * Getter for the tests tree node
     * @return the maanovaTestsTreeNode
     */
    public MaanovaTestsTreeNode getMaanovaTestsTreeNode()
    {
        return this.maanovaTestsTreeNode;
    }
    
    /**
     * Getter for the gene list tree node
     * @return the geneListsTreeNode
     */
    public GeneListsTreeNode getGeneListsTreeNode()
    {
        return this.geneListsTreeNode;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        if(this.nodeString == null)
        {
            this.nodeString = this.initString();
        }
        
        return this.nodeString;
    }
    
    private String initString()
    {
        if(JRIUtilityFunctions.isTopLevelObject(this.getMicroarrayExperiment()))
        {
            MicroarrayExperiment maExperiment = this.getMicroarrayExperiment();
            int arrayCount = maExperiment.getMicroarrayCount();
            int dyeCount = maExperiment.getDyeCount();
            int geneCount = maExperiment.getGeneCount();
            
            return
                maExperiment.toString() + " (" +
                arrayCount + (arrayCount == 1 ? " Array, " : " Arrays, ") +
                dyeCount + (dyeCount == 1 ? " Dye, " : " Dyes, ") +
                geneCount + (geneCount == 1 ? " Gene)" : " Genes)");
        }
        else
        {
            return "Error: Missing Object";
        }
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
        popupMenu.add(new ExportDataAction(
                "Export Microarray Data to CSV",
                this.getMicroarrayExperiment()));
        popupMenu.add(new ShowExperimentDesignAction(
                "Show Design Table",
                this.getMicroarrayExperiment()));
        popupMenu.add(new ArrayScatterPlotAction(
                "Scatter Plot Array Intensities",
                this.getMicroarrayExperiment()));
        popupMenu.addSeparator();
        popupMenu.add(new SafeDeleteAction(
                this.getMicroarrayExperiment().toString(),
                maanova.getApplicationFrame(),
                maanova.getDesktop(),
                true,
                DeleteMessage.CANT_UNDO_RECURSIVE_WARNING)
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void delete()
            {
                MicroarrayExperimentTreeNode.this.delete();
            }
        });
        
        popupMenu.show(
                (Component)e.getSource(),
                e.getX(),
                e.getY());
    }
    
    private void delete()
    {
        this.getMicroarrayExperiment().delete();
        MaanovaProjectManager projMgr = MaanovaProjectManager.getInstance();
        projMgr.refreshProjectDataStructures();
        projMgr.notifyActiveProjectModified();
    }

    /**
     * {@inheritDoc}
     */
    public Icon getIcon()
    {
        return SHARED_ICON;
    }
}
