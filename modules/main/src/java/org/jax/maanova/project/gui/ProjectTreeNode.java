package org.jax.maanova.project.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jax.maanova.project.MaanovaProject;
import org.jax.util.gui.Iconifiable;

/**
 * tree node for project
 */
class ProjectTreeNode
extends DefaultMutableTreeNode
implements MouseListener, Iconifiable
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -4569227725656529445L;
    
    private static final String ICON_RESOURCE =
        "/images/project-16x16.png";
    private static final Icon SHARED_ICON;
    static
    {
        URL iconUrl = ProjectTreeNode.class.getResource(ICON_RESOURCE);
        SHARED_ICON = new ImageIcon(iconUrl);
    }
    
    /**
     * the String to use if {@link MaanovaProject#getName()} is null
     */
    private static final String DEFAULT_PROJECT_NAME = "New J/maanova Project";
    
    /**
     * @param project
     */
    public ProjectTreeNode(MaanovaProject project)
    {
        super(project);
        
        if(project == null)
        {
            throw new NullPointerException(
                    "Project can't be null");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        int childCount = this.getChildCount();
        String childCountString =
            " (" + (childCount == 0 ? "empty" : childCount) + ")";
        String name = this.getProject().getName();
        return (name == null ? DEFAULT_PROJECT_NAME : name) +
               childCountString;
    }
    
    /**
     * Getter for the project
     * @return the project
     */
    public MaanovaProject getProject()
    {
        return (MaanovaProject)this.getUserObject();
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
        // TODO implement me
    }
    
    /**
     * {@inheritDoc}
     */
    public Icon getIcon()
    {
        return SHARED_ICON;
    }
}