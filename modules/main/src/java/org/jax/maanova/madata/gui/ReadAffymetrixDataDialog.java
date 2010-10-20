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
package org.jax.maanova.madata.gui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.jax.maanova.Maanova;
import org.jax.maanova.project.MaanovaProjectManager;
import org.jax.r.RCommand;
import org.jax.r.gui.RCommandEditorAndPreviewPanel;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.util.gui.MessageDialogUtilities;

/**
 * Dialog for reading in affy data
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ReadAffymetrixDataDialog extends javax.swing.JDialog
{
    /**
     * every {@link java.io.Serializable} is supposed to have
     * one of these
     */
    private static final long serialVersionUID = -3702851356816985128L;
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            ReadAffymetrixDataDialog.class.getName());
    
    private final ReadAffymetrixDataPanel readAffyDataPanel;
    
    /**
     * Constructor
     * @param parent
     *          the parent frame for this dialog
     */
    public ReadAffymetrixDataDialog(Frame parent)
    {
        super(parent, "Read Affymetrix Microarray Data", true);
        
        this.readAffyDataPanel = new ReadAffymetrixDataPanel(this);
        
        this.initComponents();
        this.postGuiInit();
    }

    /**
     * take care of the initialization that isn't handled by the GUI builder
     */
    private void postGuiInit()
    {
        this.okButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ReadAffymetrixDataDialog.this.ok();
            }
        });
        
        this.cancelButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ReadAffymetrixDataDialog.this.cancel();
            }
        });
        
        Icon helpIcon = new ImageIcon(ReadAffymetrixDataDialog.class.getResource(
                "/images/action/help-16x16.png"));
        this.helpButton.setIcon(helpIcon);
        this.helpButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ReadAffymetrixDataDialog.this.help();
            }
        });
    }

    private void help()
    {
        Maanova.getInstance().showHelp("load-affy-data", this);
    }

    /**
     * Cancel the action
     */
    private void cancel()
    {
        this.dispose();
    }

    /**
     * OK the action. Validate and run the command.
     */
    private void ok()
    {
        if(this.readAffyDataPanel.validateData())
        {
            if(LOG.isLoggable(Level.FINE))
            {
                LOG.fine("read affy command is valid. issuing command");
            }
            
            final RCommand[] commands = this.readAffyDataPanel.getCommands();
            
            Runnable evaluateReadAffyRunnable = new Runnable()
            {
                /**
                 * {@inheritDoc}
                 */
                public void run()
                {
                    try
                    {
                        RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
                        for(RCommand command: commands)
                        {
                            rInterface.evaluateCommand(command);
                        }
                        
                        MaanovaProjectManager projectManager =
                            MaanovaProjectManager.getInstance();
                        projectManager.refreshProjectDataStructures();
                        projectManager.notifyActiveProjectModified();
                    }
                    catch(Exception ex)
                    {
                        final String errorMsg =
                            "Failed to Read Affymetrix Data";
                        LOG.log(Level.SEVERE,
                                errorMsg,
                                ex);
                        MessageDialogUtilities.errorLater(
                                ReadAffymetrixDataDialog.this,
                                ex.getMessage(),
                                errorMsg);
                    }
                }
            };
            Thread evaluateReadAffyThread = new Thread(evaluateReadAffyRunnable);
            evaluateReadAffyThread.start();
            
            this.dispose();
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("all")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel mainDataEditorPanel = new RCommandEditorAndPreviewPanel(this.readAffyDataPanel);
        javax.swing.JPanel actionPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().add(mainDataEditorPanel, java.awt.BorderLayout.CENTER);

        okButton.setText("OK");
        actionPanel.add(okButton);

        cancelButton.setText("Cancel");
        actionPanel.add(cancelButton);

        helpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/help-16x16.png"))); // NOI18N
        helpButton.setText("Help...");
        actionPanel.add(helpButton);

        getContentPane().add(actionPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton helpButton;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables

}
