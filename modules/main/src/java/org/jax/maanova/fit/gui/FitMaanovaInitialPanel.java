/*
 * Copyright (c) 2008 The Jackson Laboratory
 *
 * Permission is hereby granted, free of charge, to any person obtaining  a copy
 * of this software and associated documentation files (the  "Software"), to
 * deal in the Software without restriction, including  without limitation the
 * rights to use, copy, modify, merge, publish,  distribute, sublicense, and/or
 * sell copies of the Software, and to  permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be  included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,  EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF  MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY  CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,  TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE  SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.jax.maanova.fit.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;

import org.jax.maanova.fit.FitMaanovaCommand;
import org.jax.maanova.fit.InteractivePredictor;
import org.jax.maanova.fit.MixedModelSolutionMethod;
import org.jax.maanova.madata.MicroarrayExperiment;
import org.jax.maanova.madata.MicroarrayExperimentDesign;
import org.jax.maanova.project.MaanovaProject;
import org.jax.maanova.project.MaanovaProjectManager;
import org.jax.r.RCommand;
import org.jax.r.RSyntaxException;
import org.jax.r.RUtilities;
import org.jax.r.gui.RCommandEditorPanel;
import org.jax.util.datastructure.SequenceUtilities;
import org.jax.util.gui.CheckableListTableModel;
import org.jax.util.gui.MessageDialogUtilities;

/**
 * Use this panel to edit fitqtl R commands
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class FitMaanovaInitialPanel extends RCommandEditorPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -2891530987811064009L;
    
    private static final int IS_RANDOM_TERM_COLUMN = 0;
    
    private static final int IS_COVARIATE_TERM_COLUMN = 1;
    
    private static final int PREDICTOR_COLUMN = 2;
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            FitMaanovaInitialPanel.class.getName());
    
    private final FitMaanovaCommand fitMaanovaCommand;
    
    private final CheckableListTableModel predictorTableModel;
    
    private final TableModelListener predictorTableModelListener =
        new TableModelListener()
        {
            /**
             * {@inheritDoc}
             */
            public void tableChanged(TableModelEvent e)
            {
                FitMaanovaInitialPanel.this.predictorTableChanged(e);
            }
        };
    
    private final ItemListener updateRCommandItemListener = new ItemListener()
    {
        public void itemStateChanged(ItemEvent e)
        {
            FitMaanovaInitialPanel.this.updateRCommand();
        }
    };
    
    /**
     * Constructor
     * @param selectedExperiment
     *          microarray experiment selection that this panel should start out
     *          with
     */
    public FitMaanovaInitialPanel(MicroarrayExperiment selectedExperiment)
    {
        this.fitMaanovaCommand = new FitMaanovaCommand();
        this.predictorTableModel = new CheckableListTableModel(
                new String[] {"Random", "Covariate", "Term"},
                2);
        this.initComponents();
        this.postGuiInit(selectedExperiment);
    }
    
    /**
     * Do the GUI initialization that wasn't handled by the GUI builder
     * @param selectedExperiment
     *          the initially selected microarray experiment
     */
    private void postGuiInit(MicroarrayExperiment selectedExperiment)
    {
        this.fitResultNameTextField.getDocument().addDocumentListener(
                new DocumentListener()
                {
                    public void changedUpdate(DocumentEvent e)
                    {
                        FitMaanovaInitialPanel.this.updateRCommand();
                    }

                    public void insertUpdate(DocumentEvent e)
                    {
                        FitMaanovaInitialPanel.this.updateRCommand();
                    }

                    public void removeUpdate(DocumentEvent e)
                    {
                        FitMaanovaInitialPanel.this.updateRCommand();
                    }
                });
        
        ListSelectionListener selectionListener = new ListSelectionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void valueChanged(ListSelectionEvent e)
            {
                FitMaanovaInitialPanel.this.modelSelectionsChanged();
            }
        };
        
        this.modelInputsList.setModel(new DefaultListModel());
        this.modelInputsList.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.modelInputsList.getSelectionModel().addListSelectionListener(
                selectionListener);
        
        this.modelPredictorTable.setModel(this.predictorTableModel);
        this.predictorTableModel.addTableModelListener(
                this.predictorTableModelListener);
        this.modelPredictorTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        TableColumnModel columnModel =
            this.modelPredictorTable.getColumnModel();
        columnModel.getColumn(IS_RANDOM_TERM_COLUMN).setPreferredWidth(0);
        columnModel.getColumn(IS_COVARIATE_TERM_COLUMN).setPreferredWidth(0);
        this.modelPredictorTable.getSelectionModel().addListSelectionListener(
                selectionListener);
        
        MaanovaProject activeProject =
            MaanovaProjectManager.getInstance().getActiveProject();
        MicroarrayExperiment[] microarrayExperiments =
            activeProject.getDataModel().getMicroarrays();
        for(MicroarrayExperiment microarrayExperiment: microarrayExperiments)
        {
            this.microarrayExperimentComboBox.addItem(microarrayExperiment);
        }
        
        if(selectedExperiment != null)
        {
            this.microarrayExperimentComboBox.setSelectedItem(
                    selectedExperiment);
        }
        
        for(MixedModelSolutionMethod fitMethod: MixedModelSolutionMethod.values())
        {
            this.fitMethodComboBox.addItem(fitMethod);
        }
        this.fitMethodComboBox.addItemListener(this.updateRCommandItemListener);
        
        this.verboseOutputCheckBox.addItemListener(
                this.updateRCommandItemListener);
        
        this.updateRCommand();
        this.modelSelectionsChanged();
    }
    
    /**
     * Respond to a change in the predictor terms table
     * @param e
     *          the change event
     */
    private void predictorTableChanged(TableModelEvent e)
    {
        if(e.getType() == TableModelEvent.UPDATE &&
           e.getFirstRow() == e.getLastRow())
        {
            int row = e.getFirstRow();
            int column = e.getColumn();
            
            if(column == IS_RANDOM_TERM_COLUMN)
            {
                Boolean randomIsSet =
                    (Boolean)this.predictorTableModel.getValueAt(
                            row,
                            IS_RANDOM_TERM_COLUMN);
                Boolean covariateIsSet =
                    (Boolean)this.predictorTableModel.getValueAt(
                            row,
                            IS_COVARIATE_TERM_COLUMN);
                
                // turn off the covariate checkbox if they're both set
                if(randomIsSet.booleanValue() && covariateIsSet.booleanValue())
                {
                    this.predictorTableModel.setValueAt(
                            Boolean.FALSE,
                            row,
                            IS_COVARIATE_TERM_COLUMN);
                }
            }
            else if(column == IS_COVARIATE_TERM_COLUMN)
            {
                Boolean randomIsSet =
                    (Boolean)this.predictorTableModel.getValueAt(
                            row,
                            IS_RANDOM_TERM_COLUMN);
                Boolean covariateIsSet =
                    (Boolean)this.predictorTableModel.getValueAt(
                            row,
                            IS_COVARIATE_TERM_COLUMN);
                
                // turn off the random checkbox if they're both set
                if(randomIsSet.booleanValue() && covariateIsSet.booleanValue())
                {
                    this.predictorTableModel.setValueAt(
                            Boolean.FALSE,
                            row,
                            IS_RANDOM_TERM_COLUMN);
                }
            }
            
            this.updateRCommand();
        }
    }

    /**
     * Get the selected microarray experiment
     * @return
     *          the selected experiment
     */
    private MicroarrayExperiment getSelectedExperiment()
    {
        return (MicroarrayExperiment)this.microarrayExperimentComboBox.getSelectedItem();
    }
    
    /**
     * Update the selected experiment
     */
    private void selectedExperimentChanged()
    {
        MicroarrayExperiment selectedExperiment = this.getSelectedExperiment();
        
        // replace the old inputs with inputs from the new experiment
        DefaultListModel modelInputsListModel =
            this.getModelInputsListModel();
        modelInputsListModel.clear();
        MicroarrayExperimentDesign design =
            selectedExperiment.getDesign();
        for(String designFactor: design.getDesignFactors())
        {
            modelInputsListModel.addElement(designFactor);
        }
        
        // clear the predictors list
        this.predictorTableModel.setRowCount(0);
        
        // update the command to reflect these changes
        this.updateRCommand();
    }

    /**
     * {@inheritDoc}
     */
    public RCommand[] getCommands()
    {
        FitMaanovaCommand fitMaanovaCommand = this.fitMaanovaCommand;
        if(fitMaanovaCommand == null)
        {
            return new RCommand[0];
        }
        else
        {
            return new RCommand[] {fitMaanovaCommand};
        }
    }
    
    /**
     * Getter for the model inputs list model
     * @return
     *          the model
     */
    private DefaultListModel getModelInputsListModel()
    {
        return (DefaultListModel)this.modelInputsList.getModel();
    }
    
    /**
     * Get the selected predictor
     * @return
     *          the selected predictor
     */
    private InteractivePredictor getSelectedPredictor()
    {
        int selectedRow = this.modelPredictorTable.getSelectedRow();
        if(selectedRow == -1)
        {
            return null;
        }
        else
        {
            return (InteractivePredictor)this.predictorTableModel.getValueAt(
                    selectedRow,
                    PREDICTOR_COLUMN);
        }
    }
    
    /**
     * Respond to a change in model selection (either inputs table or predictors)
     */
    private void modelSelectionsChanged()
    {
        int selectedInputsCount =
            this.modelInputsList.getSelectedIndices().length;
        InteractivePredictor selectedPredictor = this.getSelectedPredictor();
        
        this.addPredictorsButton.setEnabled(
                selectedInputsCount > 0);
        this.addInteractivePredictorButton.setEnabled(
                selectedInputsCount == 2);
        this.appendInteractionToPredictorButton.setEnabled(
                selectedInputsCount > 0 &&
                selectedPredictor != null &&
                selectedPredictor.getInteractiveTerms().length == 1);
        this.removePredictorButton.setEnabled(
                selectedPredictor != null);
    }
    
    /**
     * This function is called when we should update the {@link FitMaanovaCommand}
     * in response to a change in the GUI
     */
    private void updateRCommand()
    {
        String resultIdentifier = null;
        MicroarrayExperiment selectedExperiment = this.getSelectedExperiment();
        String resultName = this.getFitResultName();
        if(selectedExperiment != null && resultName != null && resultName.length() >= 1)
        {
            try
            {
                resultIdentifier = RUtilities.fromReadableNameToRIdentifier(
                        resultName);
                resultIdentifier =
                    selectedExperiment.getAccessorExpressionString() + "." +
                    resultIdentifier;
            }
            catch(RSyntaxException ex)
            {
                if(LOG.isLoggable(Level.FINE))
                {
                    LOG.log(Level.FINE,
                            "can't convert fit result to R identifier",
                            ex);
                }
            }
        }
        this.fitMaanovaCommand.setFitAssigneeIdentifier(
                resultIdentifier);
        
        this.fitMaanovaCommand.setMicroarrayExperiment(
                this.getSelectedExperiment());
        this.fitMaanovaCommand.setFormula(this.getPredictors());
        this.fitMaanovaCommand.setMethod(this.getFitMethod());
        this.fitMaanovaCommand.setPrintVerboseOutput(
                this.verboseOutputCheckBox.isSelected());
        this.fitMaanovaCommand.setRandomPredictors(this.getRandomPredictors());
        this.fitMaanovaCommand.setCovariatePredictors(this.getCovariatePredictors());
        
        this.fireCommandModified();
    }
    
    private InteractivePredictor[] getRandomPredictors()
    {
        int predictorCount = this.predictorTableModel.getRowCount();
        
        List<InteractivePredictor> randoms = new ArrayList<InteractivePredictor>();
        for(int row = 0; row < predictorCount; row++)
        {
            Boolean checked = (Boolean)this.predictorTableModel.getValueAt(
                    row,
                    IS_RANDOM_TERM_COLUMN);
            if(checked)
            {
                randoms.add(this.getPredictorAt(row));
            }
        }
        
        return randoms.toArray(new InteractivePredictor[randoms.size()]);
    }
    
    private InteractivePredictor[] getCovariatePredictors()
    {
        int predictorCount = this.predictorTableModel.getRowCount();
        
        List<InteractivePredictor> covariates = new ArrayList<InteractivePredictor>();
        for(int row = 0; row < predictorCount; row++)
        {
            Boolean checked = (Boolean)this.predictorTableModel.getValueAt(
                    row,
                    IS_COVARIATE_TERM_COLUMN);
            if(checked)
            {
                covariates.add(this.getPredictorAt(row));
            }
        }
        
        return covariates.toArray(new InteractivePredictor[covariates.size()]);
    }
    
    /**
     * Get the currently selected fit method
     * @return
     *          the fit method
     */
    private MixedModelSolutionMethod getFitMethod()
    {
        return (MixedModelSolutionMethod)this.fitMethodComboBox.getSelectedItem();
    }

    /**
     * Get the fit result name that's currently entered in the GUI
     * @return
     *          the result name that's entered
     */
    private String getFitResultName()
    {
        return this.fitResultNameTextField.getText().trim();
    }
    
    /**
     * Get the selected inputs
     * @return
     *          the selected inputs
     */
    private String[] getSelectedInputs()
    {
        Object[] selectedValues = this.modelInputsList.getSelectedValues();
        String[] selectedInputs = new String[selectedValues.length];
        for(int i = 0; i < selectedInputs.length; i++)
        {
            selectedInputs[i] = (String)selectedValues[i];
        }
        
        return selectedInputs;
    }
    
    private InteractivePredictor[] getPredictors()
    {
        InteractivePredictor[] predictors =
            new InteractivePredictor[this.predictorTableModel.getRowCount()];
        for(int i = 0; i < predictors.length; i++)
        {
            predictors[i] = (InteractivePredictor)this.predictorTableModel.getValueAt(
                    i,
                    PREDICTOR_COLUMN);
        }
        return predictors;
    }
    
    /**
     * Create a single interactive predictor out of the selected
     * inputs
     */
    private void addSelectedInputsAsInteractivePredictor()
    {
        String[] selectedInputs = this.getSelectedInputs();
        assert selectedInputs.length == 2;
        
        InteractivePredictor newPredictor = new InteractivePredictor(selectedInputs);
        List<InteractivePredictor> resolvedList = this.expandPredictorsForAddition(
                Arrays.asList(newPredictor));
        this.addPredictor(newPredictor);
        this.addPredictors(resolvedList);
    }
    
    private void addPredictor(InteractivePredictor newPredictor)
    {
        this.addPredictors(Collections.singletonList(newPredictor));
    }

    private void addPredictors(List<InteractivePredictor> resolvedPredictorList)
    {
        if(!resolvedPredictorList.isEmpty())
        {
            for(InteractivePredictor predictor: resolvedPredictorList)
            {
                this.predictorTableModel.addRow(new Object[] {
                        Boolean.FALSE,
                        Boolean.FALSE,
                        predictor});
            }
            this.updateRCommand();
        }
    }

    /**
     * Create separate one-term predictors out of each selected input
     */
    private void addSelectedInputsAsPredictors()
    {
        String[] selectedInputs = this.getSelectedInputs();
        List<InteractivePredictor> predictorsToAdd =
            new ArrayList<InteractivePredictor>(selectedInputs.length);
        for(int i = 0; i < selectedInputs.length; i++)
        {
            predictorsToAdd.add(new InteractivePredictor(selectedInputs[i]));
        }
        
        this.addPredictors(predictorsToAdd);
    }

    private List<InteractivePredictor> expandPredictorsForAddition(
            List<InteractivePredictor> predictorsToExpand)
    {
        List<InteractivePredictor> existingPredictors = Arrays.asList(
                this.getPredictors());
        List<InteractivePredictor> predictorsToAdd = new ArrayList<InteractivePredictor>();
        
        // expand any complex predictors into the terms that they are built
        // from
        for(InteractivePredictor predictorToExpand: predictorsToExpand)
        {
            if(predictorToExpand.getInteractiveTerms().length >= 2)
            {
                for(String predictorTerm: predictorToExpand.getInteractiveTerms())
                {
                    InteractivePredictor subPredictor = new InteractivePredictor(predictorTerm);
                    if(!existingPredictors.contains(subPredictor) &&
                       !predictorsToAdd.contains(subPredictor))
                    {
                        predictorsToAdd.add(subPredictor);
                    }
                }
            }
        }
        
        return predictorsToAdd;
    }

    /**
     * Append selected input terms as interactions to the selected predictor
     */
    private void appendSelectedInputsToSelectedPredictor()
    {
        String[] selectedInputs = this.getSelectedInputs();
        InteractivePredictor selectedPredictor = this.getSelectedPredictor();
        
        // before we do any real work see if the inputs are already in the
        // predictor
        List<String> overlappingInputs = new ArrayList<String>(
                Arrays.asList(selectedInputs));
        overlappingInputs.retainAll(Arrays.asList(
                selectedPredictor.getInteractiveTerms()));
        if(!overlappingInputs.isEmpty())
        {
            MessageDialogUtilities.warn(
                    this,
                    "The following inputs will not be appended to " +
                    selectedPredictor.toString() + " since they are " +
                    "redundant: " +
                    SequenceUtilities.toString(overlappingInputs, ", "),
                    "Redundant Inputs Selected");
        }
        
        // combine the inputs
        Set<String> combinedInputSet = new HashSet<String>(Arrays.asList(selectedInputs));
        combinedInputSet.addAll(Arrays.asList(selectedPredictor.getInteractiveTerms()));
        String[] combinedInputs = combinedInputSet.toArray(
                new String[combinedInputSet.size()]);
        Arrays.sort(combinedInputs);
        
        // replace the old predictor
        InteractivePredictor newPredictor = new InteractivePredictor(
                combinedInputs);
        this.predictorTableModel.setValueAt(
                newPredictor,
                this.modelPredictorTable.getSelectedRow(),
                PREDICTOR_COLUMN);
        
        // add any inputs that aren't yet in the model
        this.addPredictors(this.expandPredictorsForAddition(
                Collections.singletonList(newPredictor)));
    }

    /**
     * Remove any selected predictors
     */
    private void removeSelectedPredictors()
    {
        int[] selectedPredictorRows = this.modelPredictorTable.getSelectedRows();
        Arrays.sort(selectedPredictorRows);
        
        if(selectedPredictorRows.length > 0)
        {
            int totalPredictorCount = this.predictorTableModel.getRowCount();
            
            // check to see if we need to remove any dependent terms
            Set<String> singleTermsToRemove = new HashSet<String>();
            for(int i = 0; i < selectedPredictorRows.length; i++)
            {
                InteractivePredictor currPredictor =
                    (InteractivePredictor)this.predictorTableModel.getValueAt(
                            selectedPredictorRows[i],
                            PREDICTOR_COLUMN);
                if(currPredictor.isSingleTerm())
                {
                    singleTermsToRemove.add(currPredictor.getTerm());
                }
            }
            
            List<Integer> dependentPredictorRows = new ArrayList<Integer>();
            for(int i = 0; i < totalPredictorCount; i++)
            {
                // nothing to do if it's a single predictor or if it's
                // already selected for deletion
                InteractivePredictor currPredictor = this.getPredictorAt(i);
                if((!currPredictor.isSingleTerm()) &&
                   (Arrays.binarySearch(selectedPredictorRows, i) <= -1))
                {
                    // see if this predictor is dependent on anything we're
                    // deleting
                    for(String term: currPredictor.getInteractiveTerms())
                    {
                        if(singleTermsToRemove.contains(term))
                        {
                            dependentPredictorRows.add(i);
                            break;
                        }
                    }
                }
            }
            
            // warn the user...
            if(this.shouldWeDeleteDependents(dependentPredictorRows))
            {
                List<Integer> everyPredictorRowToDelete = SequenceUtilities.toIntegerList(
                        selectedPredictorRows);
                everyPredictorRowToDelete.addAll(dependentPredictorRows);
                Collections.sort(everyPredictorRowToDelete);
                
                for(int i = everyPredictorRowToDelete.size() - 1; i >= 0 ; i--)
                {
                    this.predictorTableModel.removeRow(
                            everyPredictorRowToDelete.get(i));
                }
                
                this.updateRCommand();
            }
        }
    }
    
    /**
     * Ask the user if we should delete the given dependent predictors
     * @param dependentPredictorRows
     *          the rows of the dependent predictors
     * @return
     *          true if the user says OK (or if there's nothing to delete)
     */
    private boolean shouldWeDeleteDependents(
            List<Integer> dependentPredictorRows)
    {
        if(dependentPredictorRows.isEmpty())
        {
            return true;
        }
        else
        {
            StringBuffer message = new StringBuffer(
                    "The following terms are dependent on the terms that you " +
                    "have selected and will also be deleted: ");
            int dependentPredictorCount = dependentPredictorRows.size();
            for(int i = 0; i < dependentPredictorCount; i++)
            {
                if(i >= 1)
                {
                    message.append(", ");
                }
                
                InteractivePredictor dependentPredictor =
                    this.getPredictorAt(dependentPredictorRows.get(i));
                message.append(dependentPredictor.toString());
            }
            
            message.append('\n');
            message.append("Would you like to continue?");
            
            return MessageDialogUtilities.ask(
                    this,
                    message.toString(),
                    "Delete Dependent Terms?");
        }
    }

    /**
     * Get the predictor at the given index
     * @param index
     *          the index
     * @return
     *          the predictor
     */
    private InteractivePredictor getPredictorAt(int index)
    {
        return (InteractivePredictor)this.predictorTableModel.getValueAt(
                index,
                PREDICTOR_COLUMN);
    }
    
    /**
     * Validate the data in this panel is OK
     * @return
     *          true iff the data is valid
     */
    public boolean validateData()
    {
        String message = null;
        
        InteractivePredictor[] formula = this.fitMaanovaCommand.getFormula();
        if(formula == null || formula.length == 0)
        {
            message =
                "Cannot fit a model without any predictors. " +
                "Please add at least one predictor before continuing.";
        }
        else if(this.getFitResultName().length() == 0)
        {
            message =
                "Please enter a name for the fit result before continuing.";
        }
        else
        {
            message = RUtilities.getErrorMessageForReadableName(
                    this.getFitResultName());
        }
        
        if(message == null)
        {
            return true;
        }
        else
        {
            MessageDialogUtilities.warn(
                    this,
                    message,
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

        microarrayExperimentLabel = new javax.swing.JLabel();
        microarrayExperimentComboBox = new javax.swing.JComboBox();
        addPredictorsButton = new javax.swing.JButton();
        addInteractivePredictorButton = new javax.swing.JButton();
        appendInteractionToPredictorButton = new javax.swing.JButton();
        removePredictorButton = new javax.swing.JButton();
        modelSplitPane = new javax.swing.JSplitPane();
        modelInputsPanel = new javax.swing.JPanel();
        modelInputsLabel = new javax.swing.JLabel();
        modelInputsScrollPane = new javax.swing.JScrollPane();
        modelInputsList = new javax.swing.JList();
        modelPredictorPanel = new javax.swing.JPanel();
        modelPredictorLabel = new javax.swing.JLabel();
        modelPredictorScrollPanel = new javax.swing.JScrollPane();
        modelPredictorTable = new javax.swing.JTable();
        fitMethodLabel = new javax.swing.JLabel();
        fitMethodComboBox = new javax.swing.JComboBox();
        verboseOutputCheckBox = new javax.swing.JCheckBox();
        fitResultNameLabel = new javax.swing.JLabel();
        fitResultNameTextField = new javax.swing.JTextField();

        microarrayExperimentLabel.setText("Microarray Experiment:");

        microarrayExperimentComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                microarrayExperimentComboBoxItemStateChanged(evt);
            }
        });

        addPredictorsButton.setText("Add Term(s)");
        addPredictorsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPredictorsButtonActionPerformed(evt);
            }
        });

        addInteractivePredictorButton.setText("Add as Interactive Term");
        addInteractivePredictorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addInteractivePredictorButtonActionPerformed(evt);
            }
        });

        appendInteractionToPredictorButton.setText("Append Interaction to Term");
        appendInteractionToPredictorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                appendInteractionToPredictorButtonActionPerformed(evt);
            }
        });

        removePredictorButton.setText("Remove Term");
        removePredictorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removePredictorButtonActionPerformed(evt);
            }
        });

        modelSplitPane.setResizeWeight(0.5);
        modelSplitPane.setMinimumSize(new java.awt.Dimension(13, 100));
        modelSplitPane.setPreferredSize(new java.awt.Dimension(13, 100));

        modelInputsLabel.setText("Model Inputs:");

        modelInputsScrollPane.setViewportView(modelInputsList);

        org.jdesktop.layout.GroupLayout modelInputsPanelLayout = new org.jdesktop.layout.GroupLayout(modelInputsPanel);
        modelInputsPanel.setLayout(modelInputsPanelLayout);
        modelInputsPanelLayout.setHorizontalGroup(
            modelInputsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(modelInputsPanelLayout.createSequentialGroup()
                .add(modelInputsLabel)
                .addContainerGap(346, Short.MAX_VALUE))
            .add(modelInputsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
        );
        modelInputsPanelLayout.setVerticalGroup(
            modelInputsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(modelInputsPanelLayout.createSequentialGroup()
                .add(modelInputsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(modelInputsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE))
        );

        modelSplitPane.setLeftComponent(modelInputsPanel);

        modelPredictorLabel.setText("Model Terms:");

        modelPredictorScrollPanel.setViewportView(modelPredictorTable);

        org.jdesktop.layout.GroupLayout modelPredictorPanelLayout = new org.jdesktop.layout.GroupLayout(modelPredictorPanel);
        modelPredictorPanel.setLayout(modelPredictorPanelLayout);
        modelPredictorPanelLayout.setHorizontalGroup(
            modelPredictorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(modelPredictorPanelLayout.createSequentialGroup()
                .add(modelPredictorLabel)
                .addContainerGap(127, Short.MAX_VALUE))
            .add(modelPredictorScrollPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
        );
        modelPredictorPanelLayout.setVerticalGroup(
            modelPredictorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(modelPredictorPanelLayout.createSequentialGroup()
                .add(modelPredictorLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(modelPredictorScrollPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE))
        );

        modelSplitPane.setRightComponent(modelPredictorPanel);

        fitMethodLabel.setText("Fit Method:");

        verboseOutputCheckBox.setSelected(true);
        verboseOutputCheckBox.setText("Print Verbose Output During Fit");

        fitResultNameLabel.setText("Name Your Fit Result:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(modelSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 657, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(microarrayExperimentLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(microarrayExperimentComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(addPredictorsButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addInteractivePredictorButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(appendInteractionToPredictorButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removePredictorButton))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(fitResultNameLabel)
                            .add(fitMethodLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(fitMethodComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(verboseOutputCheckBox))
                            .add(fitResultNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 102, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(microarrayExperimentLabel)
                    .add(microarrayExperimentComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(modelSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addPredictorsButton)
                    .add(addInteractivePredictorButton)
                    .add(appendInteractionToPredictorButton)
                    .add(removePredictorButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fitMethodLabel)
                    .add(fitMethodComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(verboseOutputCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fitResultNameLabel)
                    .add(fitResultNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addPredictorsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPredictorsButtonActionPerformed
        this.addSelectedInputsAsPredictors();
    }//GEN-LAST:event_addPredictorsButtonActionPerformed

    private void addInteractivePredictorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addInteractivePredictorButtonActionPerformed
        this.addSelectedInputsAsInteractivePredictor();
    }//GEN-LAST:event_addInteractivePredictorButtonActionPerformed

    private void appendInteractionToPredictorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_appendInteractionToPredictorButtonActionPerformed
        this.appendSelectedInputsToSelectedPredictor();
    }//GEN-LAST:event_appendInteractionToPredictorButtonActionPerformed

    private void removePredictorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePredictorButtonActionPerformed
        this.removeSelectedPredictors();
    }//GEN-LAST:event_removePredictorButtonActionPerformed

    private void microarrayExperimentComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_microarrayExperimentComboBoxItemStateChanged
        this.selectedExperimentChanged();
    }//GEN-LAST:event_microarrayExperimentComboBoxItemStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addInteractivePredictorButton;
    private javax.swing.JButton addPredictorsButton;
    private javax.swing.JButton appendInteractionToPredictorButton;
    private javax.swing.JComboBox fitMethodComboBox;
    private javax.swing.JLabel fitMethodLabel;
    private javax.swing.JLabel fitResultNameLabel;
    private javax.swing.JTextField fitResultNameTextField;
    private javax.swing.JComboBox microarrayExperimentComboBox;
    private javax.swing.JLabel microarrayExperimentLabel;
    private javax.swing.JLabel modelInputsLabel;
    private javax.swing.JList modelInputsList;
    private javax.swing.JPanel modelInputsPanel;
    private javax.swing.JScrollPane modelInputsScrollPane;
    private javax.swing.JLabel modelPredictorLabel;
    private javax.swing.JPanel modelPredictorPanel;
    private javax.swing.JScrollPane modelPredictorScrollPanel;
    private javax.swing.JTable modelPredictorTable;
    private javax.swing.JSplitPane modelSplitPane;
    private javax.swing.JButton removePredictorButton;
    private javax.swing.JCheckBox verboseOutputCheckBox;
    // End of variables declaration//GEN-END:variables
    
}
