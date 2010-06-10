package org.jax.maanova.madata.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jax.maanova.configuration.MaanovaApplicationConfigurationManager;
import org.jax.maanova.madata.ArrayType;
import org.jax.maanova.madata.ReadMicroarrayDataCommandBuilder;
import org.jax.maanova.madata.ReplicateSummaryMethod;
import org.jax.r.RCommand;
import org.jax.r.RSyntaxException;
import org.jax.r.RUtilities;
import org.jax.r.gui.RCommandEditorPanel;
import org.jax.util.TextWrapper;
import org.jax.util.gui.CharacterDelimitedViewDialog;
import org.jax.util.gui.SimplifiedDocumentListener;
import org.jax.util.io.CommonFlatFileFormat;

/**
 * A panel for generating the R command to read in microarray data
 * from the tab delimited format understood by R/maanova.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ReadMicroarrayDataPanel extends RCommandEditorPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -3262020753972962789L;
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            ReadMicroarrayDataPanel.class.getName());

    private static final int MAX_ROW_COUNT = 200;
    
    private final SpinnerNumberModel probeIdColumnSpinnerModel = new SpinnerNumberModel(
            1,
            1,
            Integer.MAX_VALUE,
            1);
    
    private final SpinnerNumberModel intensityColumnSpinnerModel = new SpinnerNumberModel(
            1,
            1,
            Integer.MAX_VALUE,
            1);
    
    private final SpinnerNumberModel metarowColumnSpinnerModel = new SpinnerNumberModel(
            1,
            1,
            Integer.MAX_VALUE,
            1);
    
    private final SpinnerNumberModel metacolumnColumnSpinnerModel = new SpinnerNumberModel(
            1,
            1,
            Integer.MAX_VALUE,
            1);
    
    private final SpinnerNumberModel rowColumnSpinnerModel = new SpinnerNumberModel(
            1,
            1,
            Integer.MAX_VALUE,
            1);
    
    private final SpinnerNumberModel columnColumnSpinnerModel = new SpinnerNumberModel(
            1,
            1,
            Integer.MAX_VALUE,
            1);
    
    private final SpinnerNumberModel numReplicatesSpinnerModel = new SpinnerNumberModel(
            1,
            1,
            Integer.MAX_VALUE,
            1);
    
    /**
     * A change listener that triggers an update to the R command
     */
    private final ChangeListener commandChangeListener = new ChangeListener()
    {
        /**
         * {@inheritDoc}
         */
        public void stateChanged(ChangeEvent e)
        {
            ReadMicroarrayDataPanel.this.updateRCommand();
        }
    };
    
    /**
     * A document listener that triggers an update to the R command
     */
    private final DocumentListener commandDocumentListener = new SimplifiedDocumentListener()
    {
        /**
         * {@inheritDoc}
         */
        @Override
        protected void anyUpdate(DocumentEvent e)
        {
            ReadMicroarrayDataPanel.this.updateRCommand();
        }
    };
    
    /**
     * A item listener that triggers and update to the R command
     */
    private final ItemListener commandItemListener = new ItemListener()
    {
        /**
         * {@inheritDoc}
         */
        public void itemStateChanged(ItemEvent e)
        {
            ReadMicroarrayDataPanel.this.updateRCommand();
        }
    };
    
    /**
     * the command builder that we use to create a new read microarray
     * data command
     */
    private final ReadMicroarrayDataCommandBuilder commandBuilder =
        new ReadMicroarrayDataCommandBuilder();

    private final JDialog parentDialog;
    
    /**
     * Constructor
     * @param parentDialog
     *          the parent dialog for this dialog
     */
    public ReadMicroarrayDataPanel(JDialog parentDialog)
    {
        this.parentDialog = parentDialog;
        this.initComponents();
        this.postGuiInit();
    }
    
    /**
     * Take care of the initialization that isn't handled by the GUI builder
     */
    private void postGuiInit()
    {
        // initialize probe ID stuff
        this.probeIdColumnCheckBox.setSelected(
                this.commandBuilder.isProbeIdColumnValid());
        this.probeIdColumnCheckBox.addChangeListener(new ChangeListener()
        {
            /**
             * {@inheritDoc}
             */
            public void stateChanged(ChangeEvent e)
            {
                ReadMicroarrayDataPanel.this.updateRCommand();
                ReadMicroarrayDataPanel.this.refreshProbeIdColumnEnabled();
            }
        });
        this.probeIdColumnSpinnerModel.setValue(
                this.commandBuilder.getProbeIdColumn());
        this.probeIdColumnSpinnerModel.addChangeListener(this.commandChangeListener);
        this.probeIdColumnSpinner.setModel(this.probeIdColumnSpinnerModel);
        
        // initialize the spinners (mostly column spinners)
        this.intensityColumnSpinnerModel.setValue(this.commandBuilder.getIntensityColumn());
        this.metarowColumnSpinnerModel.setValue(this.commandBuilder.getMetarowColumn());
        this.metacolumnColumnSpinnerModel.setValue(this.commandBuilder.getMetacolumnColumn());
        this.rowColumnSpinnerModel.setValue(this.commandBuilder.getRowColumn());
        this.columnColumnSpinnerModel.setValue(this.commandBuilder.getColumnColumn());
        this.numReplicatesSpinnerModel.setValue(this.commandBuilder.getNumberOfReplicates());
        
        this.intensityColumnSpinner.setModel(this.intensityColumnSpinnerModel);
        this.metarowColumnSpinner.setModel(this.metarowColumnSpinnerModel);
        this.metacolumnColumnSpinner.setModel(this.metacolumnColumnSpinnerModel);
        this.rowColumnSpinner.setModel(this.rowColumnSpinnerModel);
        this.columnColumnSpinner.setModel(this.columnColumnSpinnerModel);
        this.numReplicatesSpinner.setModel(this.numReplicatesSpinnerModel);
        
        this.intensityColumnSpinnerModel.addChangeListener(this.commandChangeListener);
        this.metarowColumnSpinnerModel.addChangeListener(this.commandChangeListener);
        this.metacolumnColumnSpinnerModel.addChangeListener(this.commandChangeListener);
        this.rowColumnSpinnerModel.addChangeListener(this.commandChangeListener);
        this.columnColumnSpinnerModel.addChangeListener(this.commandChangeListener);
        this.numReplicatesSpinnerModel.addChangeListener(new ChangeListener()
        {
            /**
             * {@inheritDoc}
             */
            public void stateChanged(ChangeEvent e)
            {
                ReadMicroarrayDataPanel.this.updateRCommand();
                ReadMicroarrayDataPanel.this.refreshCollapseReplicatesEnabled();
            }
        });
        
        // initialize the input files
        this.dataFileTextField.getDocument().addDocumentListener(
                this.commandDocumentListener);
        this.designFileTextField.getDocument().addDocumentListener(
                this.commandDocumentListener);
        this.browseDataFileButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ReadMicroarrayDataPanel.this.browseDataFiles();
            }
        });
        this.previewDataFileButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ReadMicroarrayDataPanel.this.previewDataFile();
            }
        });
        this.browseDesignFileButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ReadMicroarrayDataPanel.this.browseDesignFiles();
            }
        });
        this.previewDesignFileButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ReadMicroarrayDataPanel.this.previewDesignFile();
            }
        });
        
        // initialize the check boxes
        this.includeMetarowMetacolumnCheckBox.setSelected(
                this.commandBuilder.getMetarowAndMetacolumnValid());
        this.spotFlagIncludedCheckBox.setSelected(
                this.commandBuilder.getFilesIncludeSpotFlag());
        this.log2TransformCheckBox.setSelected(
                this.commandBuilder.getLogTwoTransformData());
        
        this.includeMetarowMetacolumnCheckBox.addItemListener(new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                ReadMicroarrayDataPanel.this.updateRCommand();
                ReadMicroarrayDataPanel.this.refreshMetarowMetacolumnEnabled();
            }
        });
        this.spotFlagIncludedCheckBox.addItemListener(
                this.commandItemListener);
        this.log2TransformCheckBox.addItemListener(
                this.commandItemListener);
        
        // initialize the array type
        for(ArrayType arrayType: ArrayType.values())
        {
            this.arrayTypeComboBox.addItem(arrayType);
        }
        this.arrayTypeComboBox.setSelectedItem(
                this.commandBuilder.getArrayType());
        
        this.arrayTypeComboBox.addItemListener(new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                ReadMicroarrayDataPanel.this.updateRCommand();
                ReadMicroarrayDataPanel.this.refreshAllEnabled();
            }
        });
        
        // initialize the "collapse replicates" combo box
        for(ReplicateSummaryMethod replicateSummaryMethod: ReplicateSummaryMethod.values())
        {
            this.collapseReplicatesComboBox.addItem(replicateSummaryMethod);
        }
        this.collapseReplicatesComboBox.setSelectedItem(
                this.commandBuilder.getReplicateSummaryMethod());
        
        this.collapseReplicatesComboBox.addItemListener(
                this.commandItemListener);
        
        // initialize the data name
        this.dataNameTextField.getDocument().addDocumentListener(
                this.commandDocumentListener);
        
        this.refreshAllEnabled();
    }
    
    /**
     * open up a preview of the design file
     */
    private void previewDesignFile()
    {
        CharacterDelimitedViewDialog.viewFlatFile(
                "Microarray Design Preview",
                this.commandBuilder.getDesignFileName(),
                CommonFlatFileFormat.TAB_DELIMITED_UNIX,
                this.commandBuilder.getFilesIncludeHeader(),
                this.parentDialog,
                MAX_ROW_COUNT);
    }

    /**
     * open up a preview of the data file
     */
    private void previewDataFile()
    {
        CharacterDelimitedViewDialog.viewFlatFile(
                "Microarray Data Preview",
                this.commandBuilder.getDataFileName(),
                CommonFlatFileFormat.TAB_DELIMITED_UNIX,
                this.commandBuilder.getFilesIncludeHeader(),
                this.parentDialog,
                MAX_ROW_COUNT);
    }
    
    /**
     * open up the file chooser for design files
     */
    private void browseDesignFiles()
    {
        MaanovaApplicationConfigurationManager manager =
            MaanovaApplicationConfigurationManager.getInstance();
        
        JFileChooser fileChooser = new JFileChooser(
                manager.getStartingDataDirectory());
        fileChooser.setDialogTitle("Select Microarray Design File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        int response = fileChooser.showOpenDialog(this);
        if(response == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            if(selectedFile != null)
            {
                this.designFileTextField.setText(
                        selectedFile.getAbsolutePath());
                
                // update the starting directory which is the default location
                // that the file chooser opens to
                File newMicroarrayStartingDirectory =
                    selectedFile.getParentFile();
                manager.setStartingDataDirectory(newMicroarrayStartingDirectory);
            }
        }
    }

    /**
     * open up the file chooser for data files
     */
    private void browseDataFiles()
    {
        MaanovaApplicationConfigurationManager manager =
            MaanovaApplicationConfigurationManager.getInstance();
        
        JFileChooser fileChooser = new JFileChooser(
                manager.getStartingDataDirectory());
        fileChooser.setDialogTitle("Select Microarray Data File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        int response = fileChooser.showOpenDialog(this);
        if(response == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            if(selectedFile != null)
            {
                this.dataFileTextField.setText(
                        selectedFile.getAbsolutePath());
                
                // update the starting directory which is the default location
                // that the file chooser opens to
                File newMicroarrayStartingDirectory =
                    selectedFile.getParentFile();
                manager.setStartingDataDirectory(newMicroarrayStartingDirectory);
            }
        }
    }

    /**
     * refresh all of the enabled/disabled settings
     */
    private void refreshAllEnabled()
    {
        // en/disable the two-color specific stuff
        boolean isTwoColor =
            this.commandBuilder.getArrayType() == ArrayType.TWO_COLOR;
        this.twoColorArraySettingsPanel.setEnabled(isTwoColor);
        this.includeMetarowMetacolumnCheckBox.setEnabled(isTwoColor);
        this.rowColumnLabel.setEnabled(isTwoColor);
        this.rowColumnSpinner.setEnabled(isTwoColor);
        this.columnColumnLabel.setEnabled(isTwoColor);
        this.columnColumnSpinner.setEnabled(isTwoColor);
        this.numReplicatesLabel.setEnabled(isTwoColor);
        this.numReplicatesSpinner.setEnabled(isTwoColor);
        
        this.refreshProbeIdColumnEnabled();
        this.refreshMetarowMetacolumnEnabled();
        this.refreshCollapseReplicatesEnabled();
    }

    /**
     * refresh the enabled state of the probe ID spinner
     */
    private void refreshProbeIdColumnEnabled()
    {
        this.probeIdColumnSpinner.setEnabled(
                this.commandBuilder.isProbeIdColumnValid());
    }

    /**
     * refresh the enabled state of the collapse replicates check box
     */
    private void refreshCollapseReplicatesEnabled()
    {
        // collapse replicates is only enabled for two color arrays with
        // >= 2 replicates
        boolean collapseReplicatesEnabled =
            this.commandBuilder.getArrayType() == ArrayType.TWO_COLOR &&
            this.commandBuilder.getNumberOfReplicates() >= 2;
        
        this.collapseReplicatesLabel.setEnabled(collapseReplicatesEnabled);
        this.collapseReplicatesComboBox.setEnabled(collapseReplicatesEnabled);
    }

    /**
     * Refresh the enabled state of the metarow and metacolumn
     */
    private void refreshMetarowMetacolumnEnabled()
    {
        // metarow and metacol are only enabled for two-color arrays
        boolean metarowAndMetacolumnEnabled =
            this.commandBuilder.getArrayType() == ArrayType.TWO_COLOR &&
            this.includeMetarowMetacolumnCheckBox.isSelected();
        this.metarowColumnLabel.setEnabled(metarowAndMetacolumnEnabled);
        this.metarowColumnSpinner.setEnabled(metarowAndMetacolumnEnabled);
        this.metacolumnColumnLabel.setEnabled(metarowAndMetacolumnEnabled);
        this.metacolumnColumnSpinner.setEnabled(metarowAndMetacolumnEnabled);
    }
    
    /**
     * Validate the data in this panel
     * @return
     *          true iff the data is valid
     */
    public boolean validateData()
    {
        String readableMicroarrayName =
            this.dataNameTextField.getText().trim();
        
        // validate the data name
        String validationErrorMessage =
            RUtilities.getErrorMessageForReadableName(
                    readableMicroarrayName);

        if(validationErrorMessage == null)
        {
            if(readableMicroarrayName.length() == 0)
            {
                validationErrorMessage =
                    "The microarray data name cannot be empty. See help for " +
                    "more detailed information.";
            }
            else
            {
                // validate the input files
                File experimentDataFile = new File(
                        this.dataFileTextField.getText().trim());
                if(!experimentDataFile.isFile())
                {
                    validationErrorMessage =
                        "The data file \"" + this.dataFileTextField.getText() +
                        "\" is either missing, or a directory. Please select " +
                        "an existing file before proceeding.";
                }
                else
                {
                    File designFile = new File(
                            this.designFileTextField.getText().trim());
                    if(!designFile.isFile())
                    {
                        validationErrorMessage =
                            "The design file \"" + this.designFileTextField.getText() +
                            "\" is either missing, or a directory. Please select " +
                            "an existing file before proceeding.";
                    }
                    else
                    {
                        // make sure that all the column numbers are unique...
                        // this is done selectively because we shouldn't be
                        // validating for any of the column numbers in a
                        // greyed out spinner
                        Map<Integer, String> columnNumberMap =
                            new HashMap<Integer, String>();
                        
                        List<String> labelList = new ArrayList<String>();
                        List<SpinnerNumberModel> spinnerList = new ArrayList<SpinnerNumberModel>();
                        
                        if(this.probeIdColumnCheckBox.isSelected())
                        {
                            labelList.add(this.probeIdColumnCheckBox.getText());
                            spinnerList.add(this.probeIdColumnSpinnerModel);
                        }
                        
                        labelList.add(this.intensityColumnLabel.getText());
                        spinnerList.add(this.intensityColumnSpinnerModel);
                        
                        if(this.commandBuilder.getArrayType() == ArrayType.TWO_COLOR)
                        {
                            if(this.commandBuilder.getMetarowAndMetacolumnValid())
                            {
                                labelList.add(this.metarowColumnLabel.getText());
                                spinnerList.add(this.metarowColumnSpinnerModel);
                                
                                labelList.add(this.metacolumnColumnLabel.getText());
                                spinnerList.add(this.metacolumnColumnSpinnerModel);
                            }
                            
                            labelList.add(this.rowColumnLabel.getText());
                            spinnerList.add(this.rowColumnSpinnerModel);
                            
                            labelList.add(this.columnColumnLabel.getText());
                            spinnerList.add(this.columnColumnSpinnerModel);
                        }
                        
                        int columnValidationCount = labelList.size();
                        for(int i = 0;
                            i < columnValidationCount && validationErrorMessage == null;
                            i++)
                        {
                            validationErrorMessage = this.validateColumnIsUnique(
                                    columnNumberMap,
                                    labelList.get(i),
                                    spinnerList.get(i));
                        }
                    }
                }
            }
        }

        if(validationErrorMessage != null)
        {
            JOptionPane.showMessageDialog(
                    this,
                    TextWrapper.wrapText(
                            validationErrorMessage,
                            TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                            "Validation Failed",
                            JOptionPane.WARNING_MESSAGE);
            return false;
        }
        else
        {
            return true;
        }
    }
    
    /**
     * Convenience function for validating that the given column model contains
     * a unique numbering
     * @param previousColumnsMap
     *          a map containing previous column number and name value pairs.
     *          this map gets updated by this function call if it's
     *          successful
     * @param columnLabel
     *          the label for the current column
     * @param columnModel
     *          the spinner model for the current column
     * @return
     *          null if its valid and an error message if it isn't
     */
    private String validateColumnIsUnique(
            Map<Integer, String> previousColumnsMap,
            String columnLabel,
            SpinnerNumberModel columnModel)
    {
        String columnName = this.labelStringToPlainString(columnLabel);
        int columnValue = columnModel.getNumber().intValue();
        
        if(previousColumnsMap.containsKey(columnValue))
        {
            String prevColumnName = previousColumnsMap.get(columnValue);
            return "Column numbering must be unique. Both \"" +
                   prevColumnName + "\" and \"" + columnName +
                   "\" are specifying column number: " + columnValue +
                   ". Please resolve this before proceeding.";
        }
        else
        {
            previousColumnsMap.put(columnValue, columnName);
            return null;
        }
    }
    
    /**
     * Convenience function for getting a string name from a label
     * @param labelString
     *          the label string
     * @return
     *          the plain string string
     */
    private String labelStringToPlainString(String labelString)
    {
        labelString = labelString.trim();
        
        // if there's a trailing colon, remove it
        if(labelString.endsWith(":"))
        {
            return labelString.substring(0, labelString.length() - 1);
        }
        else
        {
            return labelString;
        }
    }
    
    /**
     * Update the R command to reflect the current state of the GUI
     */
    private void updateRCommand()
    {
        // update the probe ID
        this.commandBuilder.setProbeIdColumn(
                this.probeIdColumnSpinnerModel.getNumber().intValue());
        this.commandBuilder.setProbeIdColumnValid(
                this.probeIdColumnCheckBox.isSelected());
        
        // update the array type
        this.commandBuilder.setArrayType(
                (ArrayType)this.arrayTypeComboBox.getSelectedItem());
        
        // update the number of replicates
        this.commandBuilder.setNumberOfReplicates(
                this.numReplicatesSpinnerModel.getNumber().intValue());
        
        // update the command using the spinner values
        this.commandBuilder.setIntensityColumn(
                this.intensityColumnSpinnerModel.getNumber().intValue());
        this.commandBuilder.setMetarowColumn(
                this.metarowColumnSpinnerModel.getNumber().intValue());
        this.commandBuilder.setMetacolumnColumn(
                this.metacolumnColumnSpinnerModel.getNumber().intValue());
        this.commandBuilder.setRowColumn(
                this.rowColumnSpinnerModel.getNumber().intValue());
        this.commandBuilder.setColumnColumn(
                this.columnColumnSpinnerModel.getNumber().intValue());
        
        // update the input files
        this.commandBuilder.setDataFileName(
                this.dataFileTextField.getText());
        this.commandBuilder.setDesignFileName(
                this.designFileTextField.getText());
        
        // update the boolean values
        this.commandBuilder.setMetarowAndMetacolumnValid(
                this.includeMetarowMetacolumnCheckBox.isSelected());
        this.commandBuilder.setFilesIncludeSpotFlag(
                this.spotFlagIncludedCheckBox.isSelected());
        this.commandBuilder.setLogTwoTransformData(
                this.log2TransformCheckBox.isSelected());
        
        // update the data object name
        try
        {
            String dataRIdentifier = RUtilities.fromReadableNameToRIdentifier(
                    this.dataNameTextField.getText());
            this.commandBuilder.setMicroarrayDataName(
                    dataRIdentifier);
        }
        catch(RSyntaxException ex)
        {
            LOG.log(Level.FINE,
                    "can't convert readable name to an R identifier",
                    ex);
        }
        
        this.fireCommandModified();
    }
    
    /**
     * {@inheritDoc}
     */
    public RCommand[] getCommands()
    {
        return new RCommand[] {this.commandBuilder.getCommand()};
    }
    
    /**
     * Getter for the command builder
     * @return the commandBuilder
     */
    public ReadMicroarrayDataCommandBuilder getCommandBuilder()
    {
        return this.commandBuilder;
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

        javax.swing.JPanel fileInputPanel = new javax.swing.JPanel();
        javax.swing.JLabel dataFileLabel = new javax.swing.JLabel();
        dataFileTextField = new javax.swing.JTextField();
        browseDataFileButton = new javax.swing.JButton();
        previewDataFileButton = new javax.swing.JButton();
        javax.swing.JLabel designFileLabel = new javax.swing.JLabel();
        designFileTextField = new javax.swing.JTextField();
        browseDesignFileButton = new javax.swing.JButton();
        previewDesignFileButton = new javax.swing.JButton();
        javax.swing.JPanel generalSettingsPanel = new javax.swing.JPanel();
        javax.swing.JLabel arrayTypeLabel = new javax.swing.JLabel();
        arrayTypeComboBox = new javax.swing.JComboBox();
        probeIdColumnSpinner = new javax.swing.JSpinner();
        intensityColumnLabel = new javax.swing.JLabel();
        intensityColumnSpinner = new javax.swing.JSpinner();
        log2TransformCheckBox = new javax.swing.JCheckBox();
        probeIdColumnCheckBox = new javax.swing.JCheckBox();
        twoColorArraySettingsPanel = new javax.swing.JPanel();
        collapseReplicatesLabel = new javax.swing.JLabel();
        collapseReplicatesComboBox = new javax.swing.JComboBox();
        javax.swing.JLabel numReplicatesLabel1 = new javax.swing.JLabel();
        javax.swing.JLabel numReplicatesLabel2 = new javax.swing.JLabel();
        numReplicatesSpinner = new javax.swing.JSpinner();
        numReplicatesLabel = new javax.swing.JLabel();
        spotFlagIncludedCheckBox = new javax.swing.JCheckBox();
        rowColumnLabel = new javax.swing.JLabel();
        rowColumnSpinner = new javax.swing.JSpinner();
        columnColumnLabel = new javax.swing.JLabel();
        columnColumnSpinner = new javax.swing.JSpinner();
        includeMetarowMetacolumnCheckBox = new javax.swing.JCheckBox();
        metarowColumnLabel = new javax.swing.JLabel();
        metarowColumnSpinner = new javax.swing.JSpinner();
        metacolumnColumnLabel = new javax.swing.JLabel();
        metacolumnColumnSpinner = new javax.swing.JSpinner();
        dataNameLabel = new javax.swing.JLabel();
        dataNameTextField = new javax.swing.JTextField();

        fileInputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Input Files"));

        dataFileLabel.setText("Experiment Data:");

        browseDataFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/browse-16x16.png"))); // NOI18N
        browseDataFileButton.setText("Browse ...");

        previewDataFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/preview-16x16.png"))); // NOI18N
        previewDataFileButton.setText("Preview ...");

        designFileLabel.setText("Experiment Design:");

        browseDesignFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/browse-16x16.png"))); // NOI18N
        browseDesignFileButton.setText("Browse ...");

        previewDesignFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/preview-16x16.png"))); // NOI18N
        previewDesignFileButton.setText("Preview ...");

        org.jdesktop.layout.GroupLayout fileInputPanelLayout = new org.jdesktop.layout.GroupLayout(fileInputPanel);
        fileInputPanel.setLayout(fileInputPanelLayout);
        fileInputPanelLayout.setHorizontalGroup(
            fileInputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(fileInputPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(fileInputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(designFileLabel)
                    .add(dataFileLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fileInputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(dataFileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                    .add(designFileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE))
                .add(14, 14, 14)
                .add(fileInputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(browseDataFileButton)
                    .add(browseDesignFileButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fileInputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(previewDataFileButton)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, previewDesignFileButton))
                .addContainerGap())
        );
        fileInputPanelLayout.setVerticalGroup(
            fileInputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(fileInputPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(fileInputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(fileInputPanelLayout.createSequentialGroup()
                        .add(fileInputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(dataFileLabel)
                            .add(previewDataFileButton)
                            .add(browseDataFileButton)
                            .add(dataFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fileInputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(designFileLabel)
                            .add(designFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(fileInputPanelLayout.createSequentialGroup()
                        .add(33, 33, 33)
                        .add(fileInputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(previewDesignFileButton)
                            .add(browseDesignFileButton))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        generalSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("General Settings"));

        arrayTypeLabel.setText("Array Type:");

        probeIdColumnSpinner.setPreferredSize(new java.awt.Dimension(50, 24));

        intensityColumnLabel.setText("First Intensity Column:");

        intensityColumnSpinner.setPreferredSize(new java.awt.Dimension(50, 24));

        log2TransformCheckBox.setText("Log2 Transform Intensity Values");

        probeIdColumnCheckBox.setText("Probe ID Column (Optional):");

        org.jdesktop.layout.GroupLayout generalSettingsPanelLayout = new org.jdesktop.layout.GroupLayout(generalSettingsPanel);
        generalSettingsPanel.setLayout(generalSettingsPanelLayout);
        generalSettingsPanelLayout.setHorizontalGroup(
            generalSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(generalSettingsPanelLayout.createSequentialGroup()
                .add(5, 5, 5)
                .add(generalSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(generalSettingsPanelLayout.createSequentialGroup()
                        .add(arrayTypeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(arrayTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(generalSettingsPanelLayout.createSequentialGroup()
                        .add(generalSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(intensityColumnLabel)
                            .add(probeIdColumnCheckBox))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(generalSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(probeIdColumnSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(generalSettingsPanelLayout.createSequentialGroup()
                                .add(intensityColumnSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(log2TransformCheckBox)))))
                .add(27, 27, 27))
        );
        generalSettingsPanelLayout.setVerticalGroup(
            generalSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(generalSettingsPanelLayout.createSequentialGroup()
                .add(10, 10, 10)
                .add(generalSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(arrayTypeLabel)
                    .add(arrayTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(14, 14, 14)
                .add(generalSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(probeIdColumnSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(probeIdColumnCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(generalSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(intensityColumnLabel)
                    .add(intensityColumnSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(log2TransformCheckBox))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        twoColorArraySettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Two-Color Array Settings"));

        collapseReplicatesLabel.setText("Collapse Replicates To:");

        numReplicatesLabel1.setText("Number of Replicates:");

        numReplicatesLabel2.setText("Number of Replicates:");

        numReplicatesSpinner.setPreferredSize(new java.awt.Dimension(50, 24));

        numReplicatesLabel.setText("Number of Replicates:");

        spotFlagIncludedCheckBox.setText("Data Includes Spot Flag");

        rowColumnLabel.setText("Probe Row Column:");

        rowColumnSpinner.setPreferredSize(new java.awt.Dimension(50, 24));

        columnColumnLabel.setText("Probe Column Column:");

        columnColumnSpinner.setPreferredSize(new java.awt.Dimension(50, 24));

        includeMetarowMetacolumnCheckBox.setText("Data Includes Metarow and Metacolumn");

        metarowColumnLabel.setText("Probe Metarow Column:");

        metarowColumnSpinner.setPreferredSize(new java.awt.Dimension(50, 24));

        metacolumnColumnLabel.setText("Probe Metacolumn Column:");

        metacolumnColumnSpinner.setPreferredSize(new java.awt.Dimension(50, 24));

        org.jdesktop.layout.GroupLayout twoColorArraySettingsPanelLayout = new org.jdesktop.layout.GroupLayout(twoColorArraySettingsPanel);
        twoColorArraySettingsPanel.setLayout(twoColorArraySettingsPanelLayout);
        twoColorArraySettingsPanelLayout.setHorizontalGroup(
            twoColorArraySettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(twoColorArraySettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(twoColorArraySettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(includeMetarowMetacolumnCheckBox)
                    .add(twoColorArraySettingsPanelLayout.createSequentialGroup()
                        .add(twoColorArraySettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(metarowColumnLabel)
                            .add(numReplicatesLabel)
                            .add(rowColumnLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(twoColorArraySettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(twoColorArraySettingsPanelLayout.createSequentialGroup()
                                .add(metarowColumnSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(metacolumnColumnLabel))
                            .add(twoColorArraySettingsPanelLayout.createSequentialGroup()
                                .add(numReplicatesSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(collapseReplicatesLabel))
                            .add(twoColorArraySettingsPanelLayout.createSequentialGroup()
                                .add(rowColumnSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(columnColumnLabel)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(twoColorArraySettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(columnColumnSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(collapseReplicatesComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(metacolumnColumnSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(spotFlagIncludedCheckBox))
                .addContainerGap(65, Short.MAX_VALUE))
        );
        twoColorArraySettingsPanelLayout.setVerticalGroup(
            twoColorArraySettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(twoColorArraySettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(includeMetarowMetacolumnCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(twoColorArraySettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(metarowColumnLabel)
                    .add(metarowColumnSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(metacolumnColumnLabel)
                    .add(metacolumnColumnSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(twoColorArraySettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rowColumnLabel)
                    .add(rowColumnSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(columnColumnLabel)
                    .add(columnColumnSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(twoColorArraySettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(numReplicatesLabel)
                    .add(collapseReplicatesLabel)
                    .add(numReplicatesSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(collapseReplicatesComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(spotFlagIncludedCheckBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        dataNameLabel.setText("Microarray Data Object Name:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, twoColorArraySettingsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, generalSettingsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
                    .add(fileInputPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(dataNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(dataNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 153, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(fileInputPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(generalSettingsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(twoColorArraySettingsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(dataNameLabel)
                    .add(dataNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox arrayTypeComboBox;
    private javax.swing.JButton browseDataFileButton;
    private javax.swing.JButton browseDesignFileButton;
    private javax.swing.JComboBox collapseReplicatesComboBox;
    private javax.swing.JLabel collapseReplicatesLabel;
    private javax.swing.JLabel columnColumnLabel;
    private javax.swing.JSpinner columnColumnSpinner;
    private javax.swing.JTextField dataFileTextField;
    private javax.swing.JLabel dataNameLabel;
    private javax.swing.JTextField dataNameTextField;
    private javax.swing.JTextField designFileTextField;
    private javax.swing.JCheckBox includeMetarowMetacolumnCheckBox;
    private javax.swing.JLabel intensityColumnLabel;
    private javax.swing.JSpinner intensityColumnSpinner;
    private javax.swing.JCheckBox log2TransformCheckBox;
    private javax.swing.JLabel metacolumnColumnLabel;
    private javax.swing.JSpinner metacolumnColumnSpinner;
    private javax.swing.JLabel metarowColumnLabel;
    private javax.swing.JSpinner metarowColumnSpinner;
    private javax.swing.JLabel numReplicatesLabel;
    private javax.swing.JSpinner numReplicatesSpinner;
    private javax.swing.JButton previewDataFileButton;
    private javax.swing.JButton previewDesignFileButton;
    private javax.swing.JCheckBox probeIdColumnCheckBox;
    private javax.swing.JSpinner probeIdColumnSpinner;
    private javax.swing.JLabel rowColumnLabel;
    private javax.swing.JSpinner rowColumnSpinner;
    private javax.swing.JCheckBox spotFlagIncludedCheckBox;
    private javax.swing.JPanel twoColorArraySettingsPanel;
    // End of variables declaration//GEN-END:variables

}
