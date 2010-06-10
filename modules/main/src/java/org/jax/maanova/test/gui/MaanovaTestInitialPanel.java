/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.maanova.test.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.jax.maanova.fit.FitMaanovaResult;
import org.jax.maanova.fit.MixedModelSolutionMethod;
import org.jax.maanova.madata.MicroarrayExperiment;
import org.jax.maanova.project.MaanovaProject;
import org.jax.maanova.test.TestModelCommandBuilder;
import org.jax.maanova.test.TestType;
import org.jax.r.RCommand;
import org.jax.r.gui.RCommandEditorPanel;
import org.jax.util.gui.CheckableListTableModel;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.gui.Validatable;

/**
 * panel for selecting the fit and terms that should be tested
 */
public class MaanovaTestInitialPanel
extends RCommandEditorPanel
implements Validatable
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 6610799970830720392L;

    private static final String NO_FITS_AVAILABLE = "No Fits Available";
    
    private final Map<FitMaanovaResult, MicroarrayExperiment> fitToExperimentMap;
    
    private final CheckableListTableModel termsToTestTableModel;

    private final TestModelCommandBuilder commandBuilder;

    /**
     * Constructor
     * @param project
     *          the project that we're using
     * @param commandBuilder
     *          the test command builder that this panel will modify
     */
    public MaanovaTestInitialPanel(
            MaanovaProject project,
            TestModelCommandBuilder commandBuilder)
    {
        this.commandBuilder = commandBuilder;
        this.fitToExperimentMap =
            new HashMap<FitMaanovaResult, MicroarrayExperiment>();
        this.termsToTestTableModel =
            new CheckableListTableModel(
                    new String[] {"Test Term", "Term Name"},
                    1);
        
        this.initComponents();
        this.postGuiInit(project);
    }
    
    /**
     * take care of the initialization that wasn't handled by 
     * @param project the project that we're doing a test for
     */
    private void postGuiInit(MaanovaProject project)
    {
        // initialize the table
        this.termsToTestTable.setModel(this.termsToTestTableModel);
        this.termsToTestTableModel.addTableModelListener(new TableModelListener()
        {
            /**
             * {@inheritDoc}
             */
            public void tableChanged(TableModelEvent e)
            {
                MaanovaTestInitialPanel.this.termsToTestTableModelChanged();
            }
        });
        
        // initialize the drop down along with any eventing
        MicroarrayExperiment[] allExperiments =
            project.getDataModel().getMicroarrays();
        for(MicroarrayExperiment experiment: allExperiments)
        {
            for(FitMaanovaResult fit: experiment.getFitMaanovaResults())
            {
                this.fitToExperimentMap.put(fit, experiment);
                this.experimentAndFitComboBox.addItem(fit);
            }
        }
        
        // TODO shouldn't fitmaanova and matest share the same methods? it
        //      does not appear that that is the case
        for(MixedModelSolutionMethod method: MixedModelSolutionMethod.values())
        {
            this.methodComboBox.addItem(method);
        }
        this.methodComboBox.addItemListener(new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                MaanovaTestInitialPanel.this.selectedMethodChanged();
            }
        });
        this.selectedMethodChanged();
        
        if(this.fitToExperimentMap.isEmpty())
        {
            this.experimentAndFitComboBox.addItem(NO_FITS_AVAILABLE);
        }
        else
        {
            this.experimentAndFitComboBox.addItemListener(new ItemListener()
            {
                /**
                 * {@inheritDoc}
                 */
                public void itemStateChanged(ItemEvent e)
                {
                    if(e.getStateChange() == ItemEvent.SELECTED)
                    {
                        MaanovaTestInitialPanel.this.selectedFitChanged();
                    }
                }
            });
        }
        this.selectedFitChanged();
        
        this.testTypeComboBox.addItem(TestType.F_TEST);
        this.testTypeComboBox.addItem(TestType.T_TEST);
        this.testTypeComboBox.addItemListener(new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                // don't worry about the deselected events. we don't need them
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    MaanovaTestInitialPanel.this.selectedTestTypeChanged();
                }
            }
        });
        this.selectedTestTypeChanged();
    }
    
    /**
     * Respond to a change in the test method
     */
    private void selectedMethodChanged()
    {
        this.commandBuilder.setMixedModelSolutionMethod(
                (MixedModelSolutionMethod)this.methodComboBox.getSelectedItem());
        
        this.fireCommandModified();
    }

    /**
     * respond to a change in the selected test type
     */
    private void selectedTestTypeChanged()
    {
        this.commandBuilder.setTestType(
                (TestType)this.testTypeComboBox.getSelectedItem());
        
        this.fireCommandModified();
    }

    /**
     * respond to a change in the terms to test table model
     */
    private void termsToTestTableModelChanged()
    {
        List<String> termsToTest = new ArrayList<String>();
        int rowCount = this.termsToTestTableModel.getRowCount();
        for(int i = 0; i < rowCount; i++)
        {
            Boolean selected = (Boolean)this.termsToTestTableModel.getValueAt(i, 0);
            if(selected)
            {
                termsToTest.add((String)this.termsToTestTableModel.getValueAt(i, 1));
            }
        }
        this.commandBuilder.setTermsToTest(termsToTest.toArray(
                new String[termsToTest.size()]));
        
        FitMaanovaResult selectedFit = this.getSelectedFit();
        List<String> levelsToTest = new ArrayList<String>();
        if(selectedFit != null)
        {
            for(String term: termsToTest)
            {
                levelsToTest.addAll(Arrays.asList(
                        selectedFit.getFitTermLevels(term)));
            }
        }
        this.commandBuilder.setLevelsToTest(levelsToTest.toArray(
                new String[levelsToTest.size()]));
        
        this.fireCommandModified();
    }

    /**
     * respond to the user selecting a new fit
     */
    private void selectedFitChanged()
    {
        final FitMaanovaResult selectedFit = this.getSelectedFit();
        final MicroarrayExperiment selectedExperiment;
        if(selectedFit == null)
        {
            selectedExperiment = null;
        }
        else
        {
            selectedExperiment = this.fitToExperimentMap.get(selectedFit);
        }
        
        this.commandBuilder.setFitResultParameter(
                selectedFit == null ?
                        null :
                        selectedFit.getAccessorExpressionString());
        this.commandBuilder.setMadataParameter(
                selectedExperiment == null ?
                        null :
                        selectedExperiment.getAccessorExpressionString());
        
        this.updateTermsToTestTable(selectedFit);
        
        this.fireCommandModified();
    }

    /**
     * Get the selected fit result
     * @return  the selected fit or null
     */
    private FitMaanovaResult getSelectedFit()
    {
        Object selectedItem = this.experimentAndFitComboBox.getSelectedItem();
        if(selectedItem instanceof FitMaanovaResult)
        {
            return (FitMaanovaResult)selectedItem;
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Update the terms table with a newly selected fit result
     * @param selectedFit
     *          the newly selected fit
     */
    private void updateTermsToTestTable(FitMaanovaResult selectedFit)
    {
        this.termsToTestTableModel.setRowCount(0);
        if(selectedFit != null)
        {
            for(String termName: selectedFit.getFitTermNames())
            {
                this.termsToTestTableModel.addRow(new Object[] {
                        Boolean.FALSE,
                        termName});
            }
        }
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
        if(this.commandBuilder.getFitResultParameter() == null)
        {
            errorMessage =
                "Cannot proceed without any fit result selected";
        }
        else if(this.commandBuilder.getTermsToTest() == null ||
                this.commandBuilder.getTermsToTest().length == 0)
        {
            errorMessage =
                "You must have at least one term selected before proceeding";
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
                    "Invalid Data");
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

        javax.swing.JLabel experimentAndFitLabel = new javax.swing.JLabel();
        experimentAndFitComboBox = new javax.swing.JComboBox();
        testTypeLabel = new javax.swing.JLabel();
        testTypeComboBox = new javax.swing.JComboBox();
        methodLabel = new javax.swing.JLabel();
        methodComboBox = new javax.swing.JComboBox();
        javax.swing.JLabel termsToTestLabel = new javax.swing.JLabel();
        javax.swing.JScrollPane termsToTestScrollPane = new javax.swing.JScrollPane();
        termsToTestTable = new javax.swing.JTable();

        experimentAndFitLabel.setText("Experiment and Fit:");

        testTypeLabel.setText("Test Type:");

        methodLabel.setText("Method:");

        termsToTestLabel.setText("Term(s) to Test:");

        termsToTestScrollPane.setMinimumSize(new java.awt.Dimension(100, 100));
        termsToTestScrollPane.setViewportView(termsToTestTable);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(termsToTestScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
                    .add(termsToTestLabel)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(experimentAndFitLabel)
                            .add(testTypeLabel)
                            .add(methodLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(methodComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(testTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(experimentAndFitComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(288, 288, 288)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(experimentAndFitLabel)
                    .add(experimentAndFitComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(testTypeLabel)
                    .add(testTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(methodLabel)
                    .add(methodComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(termsToTestLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(termsToTestScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox experimentAndFitComboBox;
    private javax.swing.JComboBox methodComboBox;
    private javax.swing.JLabel methodLabel;
    private javax.swing.JTable termsToTestTable;
    private javax.swing.JComboBox testTypeComboBox;
    private javax.swing.JLabel testTypeLabel;
    // End of variables declaration//GEN-END:variables
}
