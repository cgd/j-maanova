/*
 * Copyright (c) 2009 The Jackson Laboratory
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
package org.jax.maanova.madata.gui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.jax.maanova.Maanova;
import org.jax.maanova.madata.MicroarrayExperiment;
import org.jax.maanova.project.MaanovaProjectManager;
import org.jax.r.RSyntaxException;
import org.jax.r.RUtilities;

/**
 * A JDialog for adding a list of gene names to a new gene list or an existing
 * gene list
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class AddGeneListDialog extends JDialog
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -4077766762731470611L;
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            AddGeneListDialog.class.getName());
    
    private final MicroarrayExperiment experiment;

    private final List<String> geneListNames;

    private List<String> genesToAdd;
    
    /**
     * Constructor
     * @param parent
     *          the parent frame for this dialog
     * @param experiment
     *          the experiment that we're adding genes to
     * @param genesToAdd
     *          the set of genes to add
     */
    public AddGeneListDialog(
            Frame parent,
            MicroarrayExperiment experiment,
            List<String> genesToAdd)
    {
        super(parent, "Add " + genesToAdd.size() + " Genes", true);
        
        this.experiment = experiment;
        this.genesToAdd = genesToAdd;
        this.geneListNames = experiment.getGeneListNames();
        
        this.initComponents();
        this.postGuiInit();
    }

    /**
     * Handle any initialization that the GUI builder doesn't do for us
     */
    private void postGuiInit()
    {
        if(this.geneListNames.isEmpty())
        {
            this.addToExistingComboBox.addItem("No Existing Gene Lists");
            this.addToNewRadioButton.setSelected(true);
            this.addToExistingRadioButton.setEnabled(false);
        }
        else
        {
            for(String name: this.geneListNames)
            {
                this.addToExistingComboBox.addItem(
                        RUtilities.fromRIdentifierToReadableName(name));
            }
        }
        
        ItemListener newOrExistingChangeListener = new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                AddGeneListDialog.this.updateEnabledSettings();
            }
        };
        this.addToExistingRadioButton.addItemListener(newOrExistingChangeListener);
        this.addToNewRadioButton.addItemListener(newOrExistingChangeListener);
        
        SwingUtilities.invokeLater(new Runnable()
        {
            /**
             * {@inheritDoc}
             */
            public void run()
            {
                AddGeneListDialog.this.updateEnabledSettings();
            }
        });
        
        this.okButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                AddGeneListDialog.this.ok();
            }
        });
        
        this.cancelButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                AddGeneListDialog.this.cancel();
            }
        });
        
        Icon helpIcon = new ImageIcon(AddGeneListDialog.class.getResource(
                "/images/action/help-16x16.png"));
        this.helpButton.setIcon(helpIcon);
        this.helpButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                AddGeneListDialog.this.help();
            }
        });
        
        this.updateEnabledSettings();
    }

    private void help()
    {
        Maanova.getInstance().showHelp("gene-list", this);
    }

    private void cancel()
    {
        this.dispose();
    }

    private void ok()
    {
        try
        {
            if(this.addToExistingRadioButton.isSelected())
            {
                String geneListId = this.geneListNames.get(
                        this.addToExistingComboBox.getSelectedIndex());
                this.experiment.addToGeneListNamed(geneListId, this.genesToAdd);
            }
            else
            {
                String geneListId = RUtilities.fromReadableNameToRIdentifier(
                        this.addToNewTextBox.getText());
                this.experiment.putGeneListNamed(geneListId, this.genesToAdd);
                
                MaanovaProjectManager projectManager = MaanovaProjectManager.getInstance();
                projectManager.refreshProjectDataStructures();
                projectManager.notifyActiveProjectModified();
            }
            
            this.dispose();
        }
        catch(RSyntaxException ex)
        {
            LOG.log(Level.SEVERE, "failed to save gene list", ex);
        }
    }

    private void updateEnabledSettings()
    {
        this.addToExistingComboBox.setEnabled(
                this.addToExistingRadioButton.isSelected());
        this.addToNewTextBox.setEnabled(
                this.addToNewRadioButton.isSelected());
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

        addToButtonGroup = new javax.swing.ButtonGroup();
        addToExistingRadioButton = new javax.swing.JRadioButton();
        addToExistingComboBox = new javax.swing.JComboBox();
        addToNewRadioButton = new javax.swing.JRadioButton();
        addToNewTextBox = new javax.swing.JTextField();
        javax.swing.JPanel actionPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        addToButtonGroup.add(addToExistingRadioButton);
        addToExistingRadioButton.setSelected(true);
        addToExistingRadioButton.setText("Add Genes To Existing List:");

        addToButtonGroup.add(addToNewRadioButton);
        addToNewRadioButton.setText("Add Genes To New List Named:");

        okButton.setText("OK");
        actionPanel.add(okButton);

        cancelButton.setText("Cancel");
        actionPanel.add(cancelButton);

        helpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/help-16x16.png"))); // NOI18N
        helpButton.setText("Help...");
        actionPanel.add(helpButton);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, actionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(addToExistingRadioButton)
                    .add(addToNewRadioButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(addToExistingComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(addToNewTextBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addToExistingRadioButton)
                    .add(addToExistingComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addToNewRadioButton)
                    .add(addToNewTextBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 93, Short.MAX_VALUE)
                .add(actionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup addToButtonGroup;
    private javax.swing.JComboBox addToExistingComboBox;
    private javax.swing.JRadioButton addToExistingRadioButton;
    private javax.swing.JRadioButton addToNewRadioButton;
    private javax.swing.JTextField addToNewTextBox;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton helpButton;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables
}
