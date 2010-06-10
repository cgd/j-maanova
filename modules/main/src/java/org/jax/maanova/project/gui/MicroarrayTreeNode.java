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
 * Tree node for a bunch of arrays this node should always be a child to a
 * {@link MicroarraysTreeNode}
 */
class MicroarrayTreeNode
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
        URL iconUrl = MicroarrayTreeNode.class.getResource(ICON_RESOURCE);
        SHARED_ICON = new ImageIcon(iconUrl);
    }

    private final int arrayIndex;
    
    /**
     * Constructor
     * @param microarrayExperiment
     *          the microarray experiment for this node
     * @param arrayIndex
     *          the zero based array index that this node is for
     */
    public MicroarrayTreeNode(
            MicroarrayExperiment microarrayExperiment,
            int arrayIndex)
    {
        super(microarrayExperiment);
        this.arrayIndex = arrayIndex;
    }
    
    /**
     * @return the arrayNumber
     */
    public int getArrayNumber()
    {
        return this.arrayIndex;
    }
    
    /**
     * Getter for the microarray data
     * @return
     *          the microarray data
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
        return "Array #" + (this.arrayIndex + 1);
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