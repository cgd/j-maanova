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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.jax.maanova.fit.FitMaanovaCommand;
import org.jax.maanova.test.TestModelCommandBuilder;
import org.jax.r.RCommand;
import org.jax.r.gui.RCommandEditorPanel;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.gui.Validatable;

/**
 * Panel for specifying the contrast matrix to be used in the test
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MaanovaTTestContrastsPanel
extends RCommandEditorPanel
implements Validatable
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -70658650818802606L;
    
    private final TestModelCommandBuilder commandBuilder;
    
    /**
     * define a contrast matrix table that will only except valid contrast
     * values
     */
    private final DefaultTableModel contrastMatrixTableModel =
        new DefaultTableModel()
        {
            /**
             * every {@link java.io.Serializable} is supposed to have one of these
             */
            private static final long serialVersionUID = 5897371696761971620L;
    
            /**
             * {@inheritDoc}
             */
            @Override
            public Class<?> getColumnClass(int columnIndex)
            {
                return Double.class;
            }
        };
    
    /**
     * Constructor
     * @param commandBuilder
     *          the command builder that this panel will edit
     */
    public MaanovaTTestContrastsPanel(TestModelCommandBuilder commandBuilder)
    {
        this.commandBuilder = commandBuilder;
        
        this.initComponents();
        this.postGuiInit();
    }

    /**
     * take care of the initialization that wasn't handled by the GUI builder
     */
    private void postGuiInit()
    {
        // initialize the table buttons
        this.addTestButton.setIcon(new ImageIcon(
                MaanovaTTestContrastsPanel.class.getResource(
                        "/images/action/add-16x16.png")));
        this.addTestButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                MaanovaTTestContrastsPanel.this.addNewContrastRow();
            }
        });
        this.removeTestButton.setIcon(new ImageIcon(
                MaanovaTTestContrastsPanel.class.getResource(
                        "/images/action/remove-16x16.png")));
        this.removeTestButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                MaanovaTTestContrastsPanel.this.removeSelectedContrastRows();
            }
        });
        
        // initialize the contrast matrix table
        this.contrastMatrixTable.setAutoResizeMode(
                JTable.AUTO_RESIZE_OFF);
        this.contrastMatrixTable.setModel(this.contrastMatrixTableModel);
        this.maybeReinitializeContrastMatrix();
        this.contrastMatrixTableModel.addTableModelListener(new TableModelListener()
        {
            /**
             * {@inheritDoc}
             */
            public void tableChanged(TableModelEvent e)
            {
                MaanovaTTestContrastsPanel.this.contrastMatrixModified();
            }
        });
        this.contrastMatrixTable.getSelectionModel().setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.contrastMatrixTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void valueChanged(ListSelectionEvent e)
                    {
                        MaanovaTTestContrastsPanel.this.refreshRemoveTestButton();
                    }
                });
        
        ItemListener allPairwiseVsCustomListener = new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                MaanovaTTestContrastsPanel.this.allPairwiseVsCustomContrastStateChanged();
            }
        };
        this.allPairwiseContrastsRadioButton.addItemListener(
                allPairwiseVsCustomListener);
        this.customContrastsRadioButton.addItemListener(
                allPairwiseVsCustomListener);
        this.allPairwiseVsCustomContrastStateChanged();
    }
    
    /**
     * Reinitialize the contrast matrix, but only if the contrast levels have
     * changed since the last initialization
     */
    public void maybeReinitializeContrastMatrix()
    {
        String[] currLevels = this.getCurrentContrastLevels();
        String[] newLevels = this.commandBuilder.getLevelsToTest();
        
        if(!Arrays.equals(currLevels, newLevels))
        {
            this.contrastMatrixTableModel.setRowCount(0);
            this.contrastMatrixTableModel.setColumnIdentifiers(newLevels);
            
            int levelCount = newLevels.length;
            int levelCountChoose2 = (levelCount * (levelCount - 1)) / 2;
            this.allPairwiseContrastsRadioButton.setText(
                    "Test All Pairwise Contrasts (" +
                    levelCountChoose2 +
                    " Tests)");
        }
    }
    
    private String[] getCurrentContrastLevels()
    {
        int colCount = this.contrastMatrixTableModel.getColumnCount();
        String[] currLevels = new String[colCount];
        for(int i = 0; i < colCount; i++)
        {
            currLevels[i] = this.contrastMatrixTableModel.getColumnName(i);
        }
        
        return currLevels;
    }
    
    private void allPairwiseVsCustomContrastStateChanged()
    {
        boolean customMatrixSelected = this.customContrastsRadioButton.isSelected();
        this.contrastMatrixTable.setEnabled(customMatrixSelected);
        this.addTestButton.setEnabled(customMatrixSelected);
        
        this.refreshRemoveTestButton();
        
        this.contrastMatrixModified();
    }
    
    private void refreshRemoveTestButton()
    {
        int selectedRowCount = this.contrastMatrixTable.getSelectedRowCount();
        if(selectedRowCount == 0)
        {
            this.removeTestButton.setEnabled(false);
        }
        else
        {
            this.removeTestButton.setEnabled(
                    this.customContrastsRadioButton.isSelected());
        }
    }

    private void removeSelectedContrastRows()
    {
        // remove the selected rows from highest index to lowest index so that
        // the row indices don't change before we have a chance to delete them
        int[] selectedRows = this.contrastMatrixTable.getSelectedRows();
        Arrays.sort(selectedRows);
        for(int i = selectedRows.length - 1; i >= 0; i--)
        {
            this.contrastMatrixTableModel.removeRow(selectedRows[i]);
        }
    }

    private void addNewContrastRow()
    {
        int colCount = this.contrastMatrixTableModel.getColumnCount();
        Double[] newRow = new Double[colCount];
        for(int i = 0; i < newRow.length; i++)
        {
            newRow[i] = Double.valueOf(0.0);
        }
        this.contrastMatrixTableModel.addRow(newRow);
    }

    /**
     * This function is called when we should update the {@link FitMaanovaCommand}
     * in response to a change in the GUI
     */
    private void contrastMatrixModified()
    {
        boolean customMatrixSelected = this.customContrastsRadioButton.isSelected();
        if(customMatrixSelected)
        {
            this.commandBuilder.setTTestContrastMatrix(
                    this.getContrastMatrix());
        }
        else
        {
            this.commandBuilder.setTTestContrastMatrix(null);
        }
        this.fireCommandModified();
    }
    
    /**
     * Extracts the contrast matrix from the table widget
     * @return
     *          the matrix
     */
    private Number[][] getContrastMatrix()
    {
        int rows = this.contrastMatrixTableModel.getRowCount();
        int cols = this.contrastMatrixTableModel.getColumnCount();
        
        Number[][] matrix = new Number[rows][cols];
        for(int row = 0; row < rows; row++)
        {
            for(int col = 0; col < cols; col++)
            {
                matrix[row][col] =
                    (Number)this.contrastMatrixTableModel.getValueAt(row, col);
            }
        }
        
        return matrix;
    }

    /**
     * {@inheritDoc}
     */
    public RCommand[] getCommands()
    {
        return new RCommand[] {this.commandBuilder.getCommand()};
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean validateData()
    {
        String errorMessage = null;
        
        if(this.customContrastsRadioButton.isSelected())
        {
            // If the user is asking for a custom matrix make sure that they
            // actually provide one!
            if(this.contrastMatrixTableModel.getRowCount() == 0)
            {
                errorMessage =
                    "The \"" + this.customContrastsRadioButton.getText() +
                    "\" option is selected but no rows have been added to the " +
                    "matrix. Please either add some rows to your custom " +
                    " contrast matrix or select the \"" +
                    this.allPairwiseContrastsRadioButton.getText() +
                    "\" option instead.";
            }
        }
        
        if(errorMessage == null)
        {
            return true;
        }
        else
        {
            MessageDialogUtilities.warn(
                    this,
                    errorMessage,
                    "Validation Failed");
            return false;
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

        javax.swing.ButtonGroup contrastOptionsButtonGroup = new javax.swing.ButtonGroup();
        allPairwiseContrastsRadioButton = new javax.swing.JRadioButton();
        customContrastsRadioButton = new javax.swing.JRadioButton();
        javax.swing.JScrollPane contrastMatrixScrollPane = new javax.swing.JScrollPane();
        contrastMatrixTable = new javax.swing.JTable();
        addTestButton = new javax.swing.JButton();
        removeTestButton = new javax.swing.JButton();

        contrastOptionsButtonGroup.add(allPairwiseContrastsRadioButton);
        allPairwiseContrastsRadioButton.setSelected(true);
        allPairwiseContrastsRadioButton.setText("Test All Pairwise Contrasts (# Tests)");

        contrastOptionsButtonGroup.add(customContrastsRadioButton);
        customContrastsRadioButton.setText("Test With Custom Contrast Matrix (One t-test Per Matrix Row):");

        contrastMatrixScrollPane.setViewportView(contrastMatrixTable);

        addTestButton.setText("Add Test");

        removeTestButton.setText("Remove Test");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(contrastMatrixScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(addTestButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(removeTestButton))
                    .add(allPairwiseContrastsRadioButton)
                    .add(customContrastsRadioButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(allPairwiseContrastsRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(customContrastsRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(contrastMatrixScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addTestButton)
                    .add(removeTestButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addTestButton;
    private javax.swing.JRadioButton allPairwiseContrastsRadioButton;
    private javax.swing.JTable contrastMatrixTable;
    private javax.swing.JRadioButton customContrastsRadioButton;
    private javax.swing.JButton removeTestButton;
    // End of variables declaration//GEN-END:variables
}
