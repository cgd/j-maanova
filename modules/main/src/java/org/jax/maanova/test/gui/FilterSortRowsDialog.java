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

package org.jax.maanova.test.gui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.jax.maanova.test.MaanovaTestResult;
import org.jax.maanova.test.gui.StatisticItem.Formatting;

/**
 * Dialog for filtering and sorting test results rows
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class FilterSortRowsDialog extends JDialog
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 1794321535677198447L;
    
    private final Map<StatisticItem, SpinnerNumberModel> filterModels =
        new HashMap<StatisticItem, SpinnerNumberModel>();
    
    private DefaultComboBoxModel filterStatModel;
    
    private DefaultComboBoxModel sortStatModel;

    private final MaanovaTestResult testResult;

    /**
     * Constructor
     * @param parent the parent for this dialog
     * @param testResult the test result that we're filtering
     */
    public FilterSortRowsDialog(Frame parent, MaanovaTestResult testResult)
    {
        super(parent, "Filter/Sort Rows", true);
        this.testResult = testResult;
        
        this.initComponents();
        this.postGuiInit();
    }
    
    /**
     * Constructor
     * @param parent the parent for this dialog
     * @param testResult the test result that we're filtering
     */
    public FilterSortRowsDialog(Dialog parent, MaanovaTestResult testResult)
    {
        super(parent, true);
        this.testResult = testResult;
        
        this.initComponents();
        this.postGuiInit();
    }

    /**
     * take care of the initialization not handled by the GUI builder
     */
    private void postGuiInit()
    {
        this.filterStatModel = new DefaultComboBoxModel();
        this.sortStatModel = new DefaultComboBoxModel();
        
        this.filterStatComboBox.setModel(this.filterStatModel);
        this.sortStatComboBox.setModel(this.sortStatModel);
        
        this.filterStatComboBox.addItemListener(new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                if(ItemEvent.SELECTED == e.getStateChange())
                {
                    FilterSortRowsDialog.this.refreshFilterSpinnerModel();
                }
            }
        });
        
        this.filterCheckBox.addItemListener(new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                FilterSortRowsDialog.this.refreshGUI();
            }
        });
        
        List<String> geneListNames =
            this.testResult.getParentExperiment().getGeneListNames();
        if(geneListNames.isEmpty())
        {
            this.filterByGeneListCheckBox.setEnabled(false);
            this.filterByGeneListComboBox.addItem("No Gene Lists Available");
            this.filterByGeneListComboBox.setEnabled(false);
        }
        else
        {
            for(String geneListName: geneListNames)
            {
                this.filterByGeneListComboBox.addItem(geneListName);
            }
        }
        
        this.sortCheckBox.addItemListener(new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                FilterSortRowsDialog.this.refreshGUI();
            }
        });
        
        this.refreshFilterSpinnerModel();
        this.refreshGUI();
        
        this.closeButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                FilterSortRowsDialog.this.close();
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
                FilterSortRowsDialog.this.close();
            }
        });
    }

    private void close()
    {
        this.setVisible(false);
    }

    /**
     * refresh the graphical elements (except for the table)
     */
    private void refreshGUI()
    {
        boolean filterSelected = this.filterCheckBox.isSelected();
        this.filterStatComboBox.setEnabled(filterSelected);
        this.filterSpinner.setEnabled(filterSelected);
        
        boolean sortSelected = this.sortCheckBox.isSelected();
        this.sortStatComboBox.setEnabled(sortSelected);
    }
    
    /**
     * Determine if row filtering is turned on
     * @return  true if filtering is on
     */
    public boolean isFilteringOn()
    {
        return this.filterCheckBox.isSelected();
    }
    
    /**
     * Getter for the filter statistic. Only valid if {@link #isFilteringOn()}
     * is true
     * @return  the statistic
     */
    public StatisticItem getSelectedFilterStatistic()
    {
        return (StatisticItem)this.filterStatComboBox.getSelectedItem();
    }
    
    /**
     * Getter for the selected filter threshold. This is only valid if
     * {@link #isFilteringOn()} is true
     * @return  the selected filter threshold
     */
    public double getSelectedFilterThreshold()
    {
        SpinnerNumberModel selectedModel = this.filterModels.get(
                this.getSelectedFilterStatistic());
        return selectedModel.getNumber().doubleValue();
    }
    
    /**
     * Get the gene list that we should filter by or null if we should not
     * filter on any gene list
     * @return  the gene list to filter by or null
     */
    public String getGeneListToFilterBy()
    {
        if(this.filterByGeneListCheckBox.isSelected())
        {
            return (String)this.filterByGeneListComboBox.getSelectedItem();
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Determine if row sorting is on
     * @return  true if sorting is on
     */
    public boolean isSortingOn()
    {
        return this.sortCheckBox.isSelected();
    }

    /**
     * Getter for the selected sort statistic. only valid if
     * {@link #isSortingOn()} is true
     * @return  the selected sort statistic
     */
    public StatisticItem getSelectedSortStatistic()
    {
        return (StatisticItem)this.sortStatComboBox.getSelectedItem();
    }
    
    /**
     * Refresh the filter spinner model
     */
    private void refreshFilterSpinnerModel()
    {
        StatisticItem selectedFilterStat = this.getSelectedFilterStatistic();
        if(selectedFilterStat != null)
        {
            SpinnerNumberModel spinnerNumModel = this.filterModels.get(selectedFilterStat);
            if(spinnerNumModel == null)
            {
                spinnerNumModel = this.makeFilterSpinnerModel(selectedFilterStat);
                this.filterModels.put(selectedFilterStat, spinnerNumModel);
            }
            
            this.filterSpinner.setModel(spinnerNumModel);
        }
    }
    
    /**
     * Create a new filter spinner model
     * @param statisticItem
     *          the item that we're creating the spinner model for
     * @return
     *          the new spinner number model
     */
    private SpinnerNumberModel makeFilterSpinnerModel(StatisticItem statisticItem)
    {
        if(statisticItem instanceof FoldChangeStatisticItem)
        {
            return new SpinnerNumberModel(
                    10.0,               // starting value
                    0.0,                // min value
                    Integer.MAX_VALUE,  // max value
                    1.0);               // step size
        }
        else
        {
            TestStatisticItem testStatisticItem = (TestStatisticItem)statisticItem;
            switch(testStatisticItem.getTestStatisticSubtype())
            {
                // F observed seems to be bound between 0 and inf
                case F_OBSERVED:
                {
                    return new SpinnerNumberModel(
                            10.0,               // starting value
                            0.0,                // min value
                            Integer.MAX_VALUE,  // max value
                            1.0);               // step size
                }
                
                // everything else seems to be bound between 0 and 1
                default:
                {
                    return new SpinnerNumberModel(
                            0.1,                // starting value
                            0.0,                // min value
                            1.0,                // max value
                            0.1);               // step size
                }
            }
        }
    }

    /**
     * Set the valid test stats for our filter
     * @param statistics the stats
     */
    public void setStatistics(final List<StatisticItem> statistics)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            /**
             * {@inheritDoc}
             */
            public void run()
            {
                FilterSortRowsDialog.this.setStatisticsNow(statistics);
            }
        });
    }
    
    /**
     * Set the valid stats for our filter assuming that we're running
     * in the AWT thread
     * @param statistics the stats
     */
    public void setStatisticsNow(final List<StatisticItem> statistics)
    {
        this.filterStatModel.removeAllElements();
        this.sortStatModel.removeAllElements();
        
        for(StatisticItem currStats: statistics)
        {
            this.filterStatModel.addElement(
                    currStats.copyWithNewFormatting(Formatting.FILTER));
            this.sortStatModel.addElement(
                    currStats.copyWithNewFormatting(Formatting.SORT));
        }
        
        this.pack();
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
        filterCheckBox = new javax.swing.JCheckBox();
        filterStatComboBox = new javax.swing.JComboBox();
        filterSpinner = new javax.swing.JSpinner();
        filterByGeneListCheckBox = new javax.swing.JCheckBox();
        filterByGeneListComboBox = new javax.swing.JComboBox();
        sortCheckBox = new javax.swing.JCheckBox();
        sortStatComboBox = new javax.swing.JComboBox();
        javax.swing.JPanel controlPanel = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        filterCheckBox.setText("Filter Where");

        filterByGeneListCheckBox.setText("Filter By Gene List");

        sortCheckBox.setText("Sort Probesets By");

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(filterCheckBox)
                    .add(sortCheckBox)
                    .add(filterByGeneListCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sortStatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(filterStatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filterSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 86, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(filterByGeneListComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(filterCheckBox)
                    .add(filterStatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(filterSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(filterByGeneListCheckBox)
                    .add(filterByGeneListComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sortCheckBox)
                    .add(sortStatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
    private javax.swing.JCheckBox filterByGeneListCheckBox;
    private javax.swing.JComboBox filterByGeneListComboBox;
    private javax.swing.JCheckBox filterCheckBox;
    private javax.swing.JSpinner filterSpinner;
    private javax.swing.JComboBox filterStatComboBox;
    private javax.swing.JCheckBox sortCheckBox;
    private javax.swing.JComboBox sortStatComboBox;
    // End of variables declaration//GEN-END:variables
}
