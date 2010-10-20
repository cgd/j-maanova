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
package org.jax.maanova.plot;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

/**
 * A dialog for configuring some of the simple stuff in a graph
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SimpleChartConfigurationDialog extends JDialog
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -39471661068315529L;
    
    private volatile String chartTitle = null;
    
    private volatile String xAxisLabel = null;
    
    private volatile String yAxisLabel = null;
    
    private final ConcurrentLinkedQueue<ActionListener> okActionListeners =
        new ConcurrentLinkedQueue<ActionListener>();
    
    /**
     * Constructor
     * @param parent
     *          the parent frame
     */
    public SimpleChartConfigurationDialog(Frame parent)
    {
        super(parent, true);
        this.initComponents();
        this.postGuiInit();
    }
    
    /**
     * take care of the initialization not handled by the GUI builder
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
                SimpleChartConfigurationDialog.this.ok(e);
            }
        });
        
        this.cancelButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                SimpleChartConfigurationDialog.this.cancel(e);
            }
        });
    }
    
    private void ok(ActionEvent e)
    {
        this.chartTitle = this.titleTextField.getText();
        this.xAxisLabel = this.xAxisLabelTextField.getText();
        this.yAxisLabel = this.yAxisLabelTextField.getText();
        
        Iterator<ActionListener> iter = this.okActionListeners.iterator();
        while(iter.hasNext())
        {
            iter.next().actionPerformed(e);
        }
        
        this.setVisible(false);
    }
    
    private void cancel(ActionEvent e)
    {
        this.titleTextField.setText(this.chartTitle);
        this.xAxisLabelTextField.setText(this.xAxisLabel);
        this.yAxisLabelTextField.setText(this.yAxisLabel);
        
        this.setVisible(false);
    }
    
    /**
     * Setter for the chart title
     * @param chartTitle the chart title to set
     */
    public void setChartTitle(final String chartTitle)
    {
        this.chartTitle = chartTitle;
        SwingUtilities.invokeLater(new Runnable()
        {
            /**
             * {@inheritDoc}
             */
            public void run()
            {
                SimpleChartConfigurationDialog.this.titleTextField.setText(
                        chartTitle);
            }
        });
    }
    
    /**
     * Getter for the chart title
     * @return the chartTitle
     */
    public String getChartTitle()
    {
        return this.chartTitle;
    }
    
    /**
     * Setter for the x axis label
     * @param xAxisLabel the xAxisLabel to set
     */
    public void setXAxisLabel(final String xAxisLabel)
    {
        this.xAxisLabel = xAxisLabel;
        SwingUtilities.invokeLater(new Runnable()
        {
            /**
             * {@inheritDoc}
             */
            public void run()
            {
                SimpleChartConfigurationDialog.this.xAxisLabelTextField.setText(
                        xAxisLabel);
            }
        });
    }
    
    /**
     * Getter for the X-axis label
     * @return the xAxisLabel
     */
    public String getXAxisLabel()
    {
        return this.xAxisLabel;
    }
    
    /**
     * Setter for the y axis label
     * @param yAxisLabel the yAxisLabel to set
     */
    public void setYAxisLabel(final String yAxisLabel)
    {
        this.yAxisLabel = yAxisLabel;
        SwingUtilities.invokeLater(new Runnable()
        {
            /**
             * {@inheritDoc}
             */
            public void run()
            {
                SimpleChartConfigurationDialog.this.yAxisLabelTextField.setText(
                        yAxisLabel);
            }
        });
    }
    
    /**
     * Setter for the y axis label
     * @return the yAxisLabel
     */
    public String getYAxisLabel()
    {
        return this.yAxisLabel;
    }
    
    /**
     * Start listening for when the user OK's the changes
     * @param listener  the listener to register
     */
    public void addOkActionListener(ActionListener listener)
    {
        this.okActionListeners.add(listener);
    }
    
    /**
     * Stop listening for when the user clicks the OK button
     * @param listener  the listener to deregister
     */
    public void removeOkActionListener(ActionListener listener)
    {
        this.okActionListeners.remove(listener);
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

        javax.swing.JPanel contentPanel = new javax.swing.JPanel();
        javax.swing.JLabel titleLabel = new javax.swing.JLabel();
        titleTextField = new javax.swing.JTextField();
        javax.swing.JLabel xLabel = new javax.swing.JLabel();
        xAxisLabelTextField = new javax.swing.JTextField();
        javax.swing.JLabel yLabel = new javax.swing.JLabel();
        yAxisLabelTextField = new javax.swing.JTextField();
        javax.swing.JPanel actionPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        titleLabel.setText("Title:");

        xLabel.setText("X Axis Label:");

        yLabel.setText("Y Axis Label:");

        org.jdesktop.layout.GroupLayout contentPanelLayout = new org.jdesktop.layout.GroupLayout(contentPanel);
        contentPanel.setLayout(contentPanelLayout);
        contentPanelLayout.setHorizontalGroup(
            contentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(contentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(contentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(xLabel)
                    .add(titleLabel)
                    .add(yLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(contentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(titleTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                    .add(xAxisLabelTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                    .add(yAxisLabelTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE))
                .addContainerGap())
        );
        contentPanelLayout.setVerticalGroup(
            contentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(contentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(contentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(titleLabel)
                    .add(titleTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(contentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(xLabel)
                    .add(xAxisLabelTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(contentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(yLabel)
                    .add(yAxisLabelTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(contentPanel, gridBagConstraints);

        okButton.setText("OK");
        actionPanel.add(okButton);

        cancelButton.setText("Cancel");
        actionPanel.add(cancelButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(actionPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField titleTextField;
    private javax.swing.JTextField xAxisLabelTextField;
    private javax.swing.JTextField yAxisLabelTextField;
    // End of variables declaration//GEN-END:variables

}
