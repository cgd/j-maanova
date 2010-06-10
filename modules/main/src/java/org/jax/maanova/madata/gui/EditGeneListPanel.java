/*
 * Copyright (c) 2010 The Jackson Laboratory
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jax.util.datastructure.SequenceUtilities;

/**
 * panel for editing the gene list
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class EditGeneListPanel extends JPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -7060043848729889847L;
    private final List<String> geneList;
    
    /**
     * Constructor.
     * @param genesInList
     *          the genes that are currently in the gene list
     * @param allGenes
     *          all gene names
     */
    public EditGeneListPanel(final String[] genesInList, final String[] allGenes)
    {
        this.geneList = new Vector<String>(Arrays.asList(genesInList));
        this.initComponents();
        this.postGuiInit(allGenes);
    }
    
    /**
     * Getter for the gene list
     * @return the gene list
     */
    public List<String> getGeneList()
    {
        return this.geneList;
    }
    
    /**
     * handle the GUI initialization that the GUI builder doesn't take care of
     * @param allGenes all gene names
     */
    private void postGuiInit(final String[] allGenes)
    {
        Icon addIcon = new ImageIcon(EditGeneListPanel.class.getResource(
                "/images/action/back-16x16.png"));
        this.addButton.setIcon(addIcon);
        this.addButton.setText("");
        this.addButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                EditGeneListPanel.this.addGenes();
            }
        });
        
        Icon removeIcon = new ImageIcon(EditGeneListPanel.class.getResource(
                "/images/action/forward-16x16.png"));
        this.removeButton.setIcon(removeIcon);
        this.removeButton.setText("");
        this.removeButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                EditGeneListPanel.this.removeGenes();
            }
        });
        
        // initialize the genes in list list
        this.genesInListList.setModel(new DefaultListModel());
        DefaultListModel genesInListModel = this.getGenesInListModel();
        for(String gene: this.geneList)
        {
            genesInListModel.addElement(gene);
        }
        
        this.genesInListList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void valueChanged(ListSelectionEvent e)
            {
                EditGeneListPanel.this.geneInListSelectionChanged();
            }
        });
        
        // initialize the other genes list
        this.otherGenesList.setModel(new DefaultListModel());
        HashSet<String> genesInListSet = new HashSet<String>(this.geneList);
        DefaultListModel otherGenesListModel = this.getOtherGenesListModel();
        for(String gene: allGenes)
        {
            if(!genesInListSet.contains(gene))
            {
                otherGenesListModel.addElement(gene);
            }
        }
        
        this.otherGenesList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void valueChanged(ListSelectionEvent e)
            {
                EditGeneListPanel.this.otherGenesListSelectionChanged();
            }
        });
    }
    
    private void geneInListSelectionChanged()
    {
        this.removeButton.setEnabled(this.genesInListList.getSelectedIndex() != -1);
    }

    private void otherGenesListSelectionChanged()
    {
        this.addButton.setEnabled(this.otherGenesList.getSelectedIndex() != -1);
    }

    /**
     * Getter for the other genes in the list model
     * @return  the other genes in the list model
     */
    private DefaultListModel getOtherGenesListModel()
    {
        return (DefaultListModel)this.otherGenesList.getModel();
    }
    
    /**
     * Getter for the genes in the list model
     * @return  the genes in the list model
     */
    private DefaultListModel getGenesInListModel()
    {
        return (DefaultListModel)this.genesInListList.getModel();
    }
    
    private void removeGenes()
    {
        DefaultListModel genesInListModel = this.getGenesInListModel();
        DefaultListModel otherGenesModel = this.getOtherGenesListModel();
        int[] indicesToRemove = this.genesInListList.getSelectedIndices();
        Arrays.sort(indicesToRemove);
        SequenceUtilities.reverseIntArray(indicesToRemove);
        
        synchronized(this.geneList)
        {
            for(int index: indicesToRemove)
            {
                String geneToRemove = (String)genesInListModel.get(index);
                otherGenesModel.addElement(geneToRemove);
                genesInListModel.remove(index);
                this.geneList.remove(index);
            }
        }
    }

    private void addGenes()
    {
        DefaultListModel otherGenesModel = this.getOtherGenesListModel();
        DefaultListModel genesInListModel = this.getGenesInListModel();
        int[] indicesToAdd = this.otherGenesList.getSelectedIndices();
        Arrays.sort(indicesToAdd);
        SequenceUtilities.reverseIntArray(indicesToAdd);
        
        synchronized(this.geneList)
        {
            for(int index: indicesToAdd)
            {
                String geneToAdd = (String)otherGenesModel.get(index);
                genesInListModel.addElement(geneToAdd);
                this.geneList.add(geneToAdd);
                otherGenesModel.remove(index);
            }
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
        java.awt.GridBagConstraints gridBagConstraints;

        javax.swing.JLabel genesInListLabel = new javax.swing.JLabel();
        javax.swing.JPanel spacerPanel = new javax.swing.JPanel();
        javax.swing.JLabel otherGenesListLabel = new javax.swing.JLabel();
        javax.swing.JScrollPane genesInListScrollPane = new javax.swing.JScrollPane();
        genesInListList = new javax.swing.JList();
        javax.swing.JPanel addRemovePanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        javax.swing.JScrollPane otherGenesScrollPane = new javax.swing.JScrollPane();
        otherGenesList = new javax.swing.JList();

        setLayout(new java.awt.GridBagLayout());

        genesInListLabel.setText("Genes in List:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(genesInListLabel, gridBagConstraints);

        org.jdesktop.layout.GroupLayout spacerPanelLayout = new org.jdesktop.layout.GroupLayout(spacerPanel);
        spacerPanel.setLayout(spacerPanelLayout);
        spacerPanelLayout.setHorizontalGroup(
            spacerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );
        spacerPanelLayout.setVerticalGroup(
            spacerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(spacerPanel, gridBagConstraints);

        otherGenesListLabel.setText("Genes Not in List:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(otherGenesListLabel, gridBagConstraints);

        genesInListScrollPane.setViewportView(genesInListList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(genesInListScrollPane, gridBagConstraints);

        addRemovePanel.setLayout(new java.awt.GridBagLayout());

        addButton.setText("Add");
        addButton.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        addRemovePanel.add(addButton, gridBagConstraints);

        removeButton.setText("Remove");
        removeButton.setEnabled(false);
        addRemovePanel.add(removeButton, new java.awt.GridBagConstraints());

        add(addRemovePanel, new java.awt.GridBagConstraints());

        otherGenesScrollPane.setViewportView(otherGenesList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(otherGenesScrollPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JList genesInListList;
    private javax.swing.JList otherGenesList;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
}
