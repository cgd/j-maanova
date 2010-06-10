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

import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import org.jax.maanova.madata.MicroarrayExperiment;
import org.jax.maanova.madata.MicroarrayExperimentDesign;

/**
 * A panel for showing experiment design
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ExperimentDesignPanel extends JPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -1733755111542389300L;
    
    /**
     * Constructor
     * @param experiment the experiment that this panel will show the design for
     */
    public ExperimentDesignPanel(MicroarrayExperiment experiment)
    {
        this.initComponents();
        this.postGuiInit(experiment);
    }

    /**
     * do initialization after GUI build init
     */
    @SuppressWarnings("serial")
    private void postGuiInit(MicroarrayExperiment experiment)
    {
        MicroarrayExperimentDesign design = experiment.getDesign();
        DefaultTableModel tableModel = new DefaultTableModel(
                design.getDesignData(),
                design.getDesignFactors())
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        
        this.designTable.setModel(tableModel);
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

        javax.swing.JScrollPane designScrollPane = new javax.swing.JScrollPane();
        designTable = new javax.swing.JTable();

        designScrollPane.setViewportView(designTable);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(designScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(designScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable designTable;
    // End of variables declaration//GEN-END:variables
}
