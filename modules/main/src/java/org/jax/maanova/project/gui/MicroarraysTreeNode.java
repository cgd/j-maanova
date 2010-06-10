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
 * Tree node for a bunch of arrays this node should always be a child to
 * {@link MicroarrayExperimentTreeNode}
 */
class MicroarraysTreeNode
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
        URL iconUrl = MicroarraysTreeNode.class.getResource(ICON_RESOURCE);
        SHARED_ICON = new ImageIcon(iconUrl);
    }
    
    /**
     * Constructor
     * @param microarrayExperiment
     *          the microarray data for this node
     */
    public MicroarraysTreeNode(MicroarrayExperiment microarrayExperiment)
    {
        super(microarrayExperiment);
    }
    
    /**
     * Getter for the microarray experiment
     * @return
     *          the microarray experiment
     */
    public MicroarrayExperiment getMicroarrayExperiment()
    {
        return (MicroarrayExperiment)this.getUserObject();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        int childCount = this.getChildCount();
        return
            "Arrays (" +
            (childCount == 0 ? "empty" : childCount) +
            ")";
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
    private void popupTriggered(MouseEvent e)
    {
        // TODO show the popup menu for microarrays
    }
    
    /**
     * {@inheritDoc}
     */
    public Icon getIcon()
    {
        return SHARED_ICON;
    }
}