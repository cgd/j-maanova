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

package org.jax.maanova.test.gui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;

import org.jax.util.gui.CheckableListTableModel;

/**
 * Subset columns
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SubsetColumnsDialog extends JDialog
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 6342241980452491913L;
    
    private CheckableListTableModel columnSelectionModel;
    
    /**
     * Constructor
     * @param parent
     *          the parent frame
     */
    public SubsetColumnsDialog(Frame parent)
    {
        super(parent, "Add/Remove Columns", true);
        this.initComponents();
        this.postGuiInit();
    }
    
    /**
     * Constructor
     * @param parent    the parent dialog
     */
    public SubsetColumnsDialog(Dialog parent)
    {
        super(parent, "Add/Remove Columns", true);
        this.initComponents();
        this.postGuiInit();
    }

    private void postGuiInit()
    {
        this.columnSelectionModel = new CheckableListTableModel(
                new String[] {"Selected", "Statistic"},
                1);
        this.columnSelectionTable.setModel(this.columnSelectionModel);
        
        this.closeButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                SubsetColumnsDialog.this.close();
            }
        });
        
        this.addWindowListener(new WindowAdapter()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void windowClosing(WindowEvent e)
            {
                SubsetColumnsDialog.this.close();
            }
        });
    }
    
    /**
     * close this dialog
     */
    private void close()
    {
        this.setVisible(false);
    }

    /**
     * Setter for the statistics that the user can [en/dis]able
     * @param statistics the testStatistics to set
     */
    public void setStatistics(List<StatisticItem> statistics)
    {
        this.columnSelectionModel.setRowCount(0);
        for(StatisticItem stat: statistics)
        {
            this.columnSelectionModel.addRow(new Object[] {Boolean.TRUE, stat});
        }
    }
    
    /**
     * Get the list of statistics that the user selected
     * @return  the list
     */
    public List<StatisticItem> getSelectedStatistics()
    {
        int rowCount = this.columnSelectionModel.getRowCount();
        List<StatisticItem> selectedStats =
            new ArrayList<StatisticItem>(rowCount);
        for(int row = 0; row < rowCount; row++)
        {
            Boolean selected = (Boolean)this.columnSelectionModel.getValueAt(
                    row,
                    0);
            if(selected)
            {
                StatisticItem currStat =
                    (StatisticItem)this.columnSelectionModel.getValueAt(row, 1);
                selectedStats.add(currStat);
            }
        }
        return selectedStats;
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

        javax.swing.JPanel mainPanel = new javax.swing.JPanel();
        javax.swing.JScrollPane columnSelectionPane = new javax.swing.JScrollPane();
        columnSelectionTable = new javax.swing.JTable();
        javax.swing.JPanel controlPanel = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        columnSelectionPane.setViewportView(columnSelectionTable);

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(columnSelectionPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(columnSelectionPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(mainPanel, gridBagConstraints);

        closeButton.setText("Close");
        controlPanel.add(closeButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(controlPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JTable columnSelectionTable;
    // End of variables declaration//GEN-END:variables

}
