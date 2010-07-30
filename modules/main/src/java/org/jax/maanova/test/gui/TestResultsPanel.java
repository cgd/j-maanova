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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jax.maanova.Maanova;
import org.jax.maanova.configuration.MaanovaApplicationConfigurationManager;
import org.jax.maanova.madata.MicroarrayExperiment;
import org.jax.maanova.madata.ProbesetRow;
import org.jax.maanova.madata.gui.AddGeneListDialog;
import org.jax.maanova.test.MaanovaTestResult;
import org.jax.maanova.test.MaanovaTestStatisticSubtype;
import org.jax.maanova.test.MaanovaTestStatisticType;
import org.jax.maanova.test.MaanovaTestStatistics;
import org.jax.util.Condition;
import org.jax.util.ObjectUtil;
import org.jax.util.datastructure.SequenceUtilities;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.io.CommonFlatFileFormat;
import org.jax.util.io.FileChooserExtensionFilter;
import org.jax.util.io.FlatFileWriter;

/**
 * The panel for displaying test results
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class TestResultsPanel extends JPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 9130392632381226634L;
    
    private static final Logger LOG = Logger.getLogger(
            TestResultsPanel.class.getName());
    
    private static final String PROBESET_ID_HEADER_STRING = "Probeset ID";
    
    private volatile int[] validIndices = null;
    
    private final MaanovaTestResult testResult;
    
    private DefaultTableModel resultsTableModel;
    
    private FilterSortRowsDialog filterSortDialog;
    
    private SubsetColumnsDialog subsetColumnsDialog;
    
    /**
     * Constructor
     * @param testResult
     *          the test results to display
     */
    public TestResultsPanel(MaanovaTestResult testResult)
    {
        this(testResult, 0);
    }
    
    /**
     * Constructor
     * @param testResult
     *          the test results to display
     * @param initialTestIndex
     *          the initial test index that we should display
     */
    public TestResultsPanel(
            MaanovaTestResult testResult,
            int initialTestIndex)
    {
        this.testResult = testResult;
        
        this.initComponents();
        this.postGuiInit(initialTestIndex);
    }
    
    /**
     * take care of the GUI initialization that isn't handled by the GUI
     * builder
     * @param initialTestIndex the initial test index to use
     */
    private void postGuiInit(int initialTestIndex)
    {
        this.resultsTableModel = new DefaultTableModel()
        {
            /**
             * every Serializable is supposed to have one of these
             */
            private static final long serialVersionUID = 2005622745249975240L;

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        this.resultsTable.setModel(this.resultsTableModel);
        this.resultsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void valueChanged(ListSelectionEvent e)
            {
                TestResultsPanel.this.rowSelectionChanged();
            }
        });
        
        MaanovaTestStatistics fStat = this.testResult.getStatistics(
                MaanovaTestStatisticType.F_STAT);
        int testCount = fStat.getContrastCount();
        
        for(int i = 0; i < testCount; i++)
        {
            this.testNumberComboBox.addItem(i + 1);
        }
        this.testNumberComboBox.setSelectedIndex(initialTestIndex);
        
        if(testCount == 1)
        {
            // don't bother the user with a choice for test number if there
            // is only one option that can be chosen
            this.testNumberLabel.setVisible(false);
            this.testNumberComboBox.setVisible(false);
        }
        else
        {
            this.testNumberComboBox.addItemListener(new ItemListener()
            {
                /**
                 * {@inheritDoc}
                 */
                public void itemStateChanged(ItemEvent e)
                {
                    if(e.getStateChange() == ItemEvent.SELECTED)
                    {
                        TestResultsPanel.this.refreshTable();
                    }
                }
            });
        }
        
        this.filterSortRowsButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                TestResultsPanel.this.showFilterSortDialog();
            }
        });
        
        this.subsetColumnsButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                TestResultsPanel.this.showSubsetColumnsDialog();
            }
        });
        
        this.exportToFlatFileButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                TestResultsPanel.this.exportResultsToFlatFile();
            }
        });
        
        this.showVolcanoPlotButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                TestResultsPanel.this.showVolcanoPlot();
            }
        });
        
        this.saveGeneListButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                TestResultsPanel.this.saveGeneList();
            }
        });
        
        Icon helpIcon = new ImageIcon(TestResultsPanel.class.getResource(
                "/images/action/help-16x16.png"));
        this.helpButton.setIcon(helpIcon);
        this.helpButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                TestResultsPanel.this.help();
            }
        });
        
        this.refreshTable();
    }
    
    private void help()
    {
        Maanova.getInstance().showHelp("test-results-table", this);
    }

    private void rowSelectionChanged()
    {
        this.selectedRowCountTextField.setText(Integer.toString(
                this.resultsTable.getSelectedRowCount()));
    }

    private void saveGeneList()
    {
        List<StatisticItem> headerItems = this.getSelectedStatistics();
        ProbesetRow[] matrix = this.getSortedFilteredMatrix(headerItems);
        
        List<String> genes = new ArrayList<String>();
        
        final int[] selectedRowIndices = this.getSelectedRowIndices();
        if(selectedRowIndices.length == 0)
        {
            MessageDialogUtilities.warn(
                    this,
                    "Please select the gene rows that you would " +
                    "like to add to a gene list. To select all rows you " +
                    "can use <control+A> on Windows or <command+A> on " +
                    "Mac OS X.",
                    "No Rows Selected");
            return;
        }
        
        int ii = 0;
        for(int i = 0; i < matrix.length && ii < selectedRowIndices.length; i++)
        {
            if(selectedRowIndices[ii] == i)
            {
                genes.add(matrix[i].getId());
                ii++;
            }
        }
        
        AddGeneListDialog dialog = new AddGeneListDialog(
                (JFrame)org.jax.util.gui.SwingUtilities.getContainingWindow(this),
                this.testResult.getParentExperiment(),
                genes);
        dialog.pack();
        dialog.setVisible(true);
    }
    
    /**
     * Getter for the selected indices
     * @return  the selected indices
     */
    private int[] getSelectedRowIndices()
    {
        int[] indices = this.resultsTable.getSelectedRows();
        Arrays.sort(indices);
        return indices;
    }
    
    /**
     * export the current results to CSV file
     */
    private void exportResultsToFlatFile()
    {
        MaanovaApplicationConfigurationManager manager =
            MaanovaApplicationConfigurationManager.getInstance();
        
        JFileChooser fileChooser = new JFileChooser(
                manager.getStartingDataDirectory());
        fileChooser.setDialogTitle("Export Table to CSV");
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileChooserExtensionFilter(
                "csv",
                "Comma-Separated Values"));
        int response = fileChooser.showSaveDialog(this);
        if(response == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            if(selectedFile != null &&
               (!selectedFile.exists() || MessageDialogUtilities.confirmOverwrite(this, selectedFile)))
            {
                manager.setStartingDataDirectory(selectedFile.getParentFile());
                
                try
                {
                    FlatFileWriter writer = new FlatFileWriter(
                            new BufferedWriter(new FileWriter(selectedFile)),
                            CommonFlatFileFormat.CSV_UNIX);
                    List<StatisticItem> headerItems =
                        this.getSelectedStatistics();
                    String[] headerStrings = new String[headerItems.size() + 1];
                    headerStrings[0] = PROBESET_ID_HEADER_STRING;
                    for(int colIndex = 1; colIndex < headerStrings.length; colIndex++)
                    {
                        headerStrings[colIndex] = headerItems.get(colIndex - 1).toString();
                    }
                    
                    writer.writeRow(headerStrings);
                    
                    ProbesetRow[] matrix = this.getSortedFilteredMatrix(headerItems);
                    for(ProbesetRow row: matrix)
                    {
                        this.writeProbesetRow(writer, row);
                    }
                    
                    writer.flush();
                    writer.close();
                }
                catch(IOException ex)
                {
                    String titleString = "Error Writing Table";
                    LOG.log(Level.SEVERE,
                            titleString,
                            ex);
                    MessageDialogUtilities.errorLater(
                            this,
                            ex.getMessage(),
                            titleString);
                }
            }
        }
    }
    
    /**
     * Write a probeset row
     * @param writer        the flat file writer to write to
     * @param row           the probeset row to write
     * @throws IOException  the IO exception
     */
    private void writeProbesetRow(FlatFileWriter writer, ProbesetRow row) throws IOException
    {
        Double[] rowValues = row.getValues();
        String[] tblRow = new String[rowValues.length + 1];
        tblRow[0] = row.getId();
        
        for(int colIndex = 1; colIndex < tblRow.length; colIndex++)
        {
            tblRow[colIndex] = rowValues[colIndex - 1].toString();
        }
        
        writer.writeRow(tblRow);
    }

    /**
     * Show the subset columns dialog
     */
    private void showSubsetColumnsDialog()
    {
        this.maybeInitSubsetColumnsDialog();
        
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            /**
             * {@inheritDoc}
             */
            public void run()
            {
                TestResultsPanel.this.subsetColumnsDialog.setVisible(true);
            }
        });
    }

    private void maybeInitSubsetColumnsDialog()
    {
        if(this.subsetColumnsDialog == null)
        {
            Window parent = org.jax.util.gui.SwingUtilities.getContainingWindow(this);
            if(parent instanceof Frame)
            {
                this.subsetColumnsDialog = new SubsetColumnsDialog((Frame)parent);
            }
            else
            {
                this.subsetColumnsDialog = new SubsetColumnsDialog((Dialog)parent);
            }
            this.subsetColumnsDialog.pack();
            
            this.subsetColumnsDialog.setStatistics(this.getAllStatistics());
            
            // when the window is made invisible that means it
            // has been closed and we should update the table to take into
            // account any modified settings
            this.subsetColumnsDialog.addComponentListener(new ComponentAdapter()
            {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public void componentHidden(ComponentEvent e)
                {
                    TestResultsPanel.this.refreshTable();
                }
            });
        }
    }

    /**
     * Show the filter/sort dialog
     */
    private void showFilterSortDialog()
    {
        this.maybeInitFilterSortDialog();
        
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            /**
             * {@inheritDoc}
             */
            public void run()
            {
                TestResultsPanel.this.filterSortDialog.setVisible(true);
            }
        });
    }

    private void maybeInitFilterSortDialog()
    {
        if(this.filterSortDialog == null)
        {
            Window parent = org.jax.util.gui.SwingUtilities.getContainingWindow(this);
            if(parent instanceof Frame)
            {
                this.filterSortDialog = new FilterSortRowsDialog(
                        (Frame)parent,
                        this.testResult);
            }
            else
            {
                this.filterSortDialog = new FilterSortRowsDialog(
                        (Dialog)parent,
                        this.testResult);
            }
            this.filterSortDialog.pack();
            
            this.filterSortDialog.setStatistics(this.getAllStatistics());
            
            // when the window is made invisible that means it
            // has been closed and we should update the table to take into
            // account any modified settings
            this.filterSortDialog.addComponentListener(new ComponentAdapter()
            {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public void componentHidden(ComponentEvent e)
                {
                    TestResultsPanel.this.filterSortDialogClosed();
                }
            });
        }
    }

    /**
     * for when the filter/sort dialog has been closed
     */
    private void filterSortDialogClosed()
    {
        String geneList = this.filterSortDialog.getGeneListToFilterBy();
        if(geneList == null)
        {
            this.validIndices = null;
        }
        else
        {
            MicroarrayExperiment experiment = this.testResult.getParentExperiment();
            this.validIndices = experiment.getIndicesForGeneListNamed(geneList);
        }
        this.refreshTable();
    }

    private void refreshTable()
    {
        this.resultsTableModel.setRowCount(0);
        
        List<StatisticItem> selectedStats = this.getSelectedStatistics();
        {
            Vector<Object> headerStrings = new Vector<Object>(selectedStats.size() + 1);
            headerStrings.add(PROBESET_ID_HEADER_STRING);
            headerStrings.addAll(selectedStats);
            this.resultsTableModel.setColumnIdentifiers(headerStrings);
        }
        ProbesetRow[] statsMatrix = this.getSortedFilteredMatrix(selectedStats);
        
        for(ProbesetRow statsRow: statsMatrix)
        {
            Double[] currRowValues = statsRow.getValues();
            String[] tableRow = new String[currRowValues.length + 1];
            tableRow[0] = statsRow.getId();
            
            for(int tblCol = 1; tblCol < tableRow.length; tblCol++)
            {
                tableRow[tblCol] = currRowValues[tblCol - 1].toString();
            }
            
            this.resultsTableModel.addRow(tableRow);
        }
        this.totalRowCountTextField.setText(Integer.toString(statsMatrix.length));
        this.selectedRowCountTextField.setText("0");
    }
    
    private ProbesetRow[] getSortedFilteredMatrix(List<StatisticItem> selectedStats)
    {
        ProbesetRow[] statsMatrix = this.getStatisticsMatrix(selectedStats);
        if(this.filterSortDialog != null)
        {
            if(this.filterSortDialog.isFilteringOn())
            {
                StatisticItem filterStat =
                    this.filterSortDialog.getSelectedFilterStatistic();
                double filterThreshold =
                    this.filterSortDialog.getSelectedFilterThreshold();
                
                statsMatrix = this.filterMatrixByThreshold(
                        selectedStats,
                        statsMatrix,
                        filterStat,
                        filterThreshold);
            }
            
            if(this.filterSortDialog.isSortingOn())
            {
                StatisticItem sortStat =
                    this.filterSortDialog.getSelectedSortStatistic();
                statsMatrix = this.sortMatrix(
                        selectedStats,
                        statsMatrix,
                        sortStat);
            }
        }
        
        return statsMatrix;
    }
    
    private void showVolcanoPlot()
    {
        List<StatisticItem> testStats = this.getSelectedStatistics();
        
        final int[] selectedRowIndices = this.getSelectedRowIndices();
        final int[] selectedGeneIndices = new int[selectedRowIndices.length];
        if(selectedGeneIndices.length > 0)
        {
            final ProbesetRow[] statsMatrix = this.getSortedFilteredMatrix(testStats);
            int ii = 0;
            for(int i = 0; i < statsMatrix.length && ii < selectedRowIndices.length; i++)
            {
                if(selectedRowIndices[ii] == i)
                {
                    selectedGeneIndices[ii] = statsMatrix[i].getIndex();
                    ii++;
                }
            }
        }
        
        Arrays.sort(selectedGeneIndices);
        final VolcanoPlotAction volcanoPlotAction = new VolcanoPlotAction(
                this.testResult,
                this.testNumberComboBox.getSelectedIndex(),
                selectedGeneIndices);
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            /**
             * {@inheritDoc}
             */
            public void run()
            {
                volcanoPlotAction.act();
            }
        });
    }
    
    private ProbesetRow[] sortMatrix(
            List<StatisticItem> matrixHeader,
            final ProbesetRow[] statsMatrix,
            final StatisticItem sortStat)
    {
        final int sortColIndex = matrixHeader.indexOf(sortStat);
        if(sortColIndex == -1)
        {
            String warningMessage =
                "Cannot sort on " + sortStat + " because that column has " +
                "been hidden.";
            LOG.warning(warningMessage);
            MessageDialogUtilities.warn(
                    this,
                    warningMessage,
                    "Cannot Sort");
            return new ProbesetRow[0];
        }
        else
        {
            final boolean reverseSort = requiresReverseOrdering(sortStat);
            final boolean takeAbsValue = sortStat instanceof FoldChangeStatisticItem;
            Comparator<ProbesetRow> sortComparator = new Comparator<ProbesetRow>()
            {
                /**
                 * {@inheritDoc}
                 */
                public int compare(ProbesetRow row1, ProbesetRow row2)
                {
                    Double val1 = row1.getValues()[sortColIndex];
                    if(takeAbsValue && val1 != null && val1.doubleValue() < 0.0)
                    {
                        val1 = new Double(-val1.doubleValue());
                    }
                    
                    Double val2 = row2.getValues()[sortColIndex];
                    if(takeAbsValue && val2 != null && val2.doubleValue() < 0.0)
                    {
                        val2 = new Double(-val2.doubleValue());
                    }
                    
                    int comp = ObjectUtil.compare(val1, val2);
                    return reverseSort ? -comp : comp;
                }
            };
            
            Arrays.sort(statsMatrix, sortComparator);
            
            return statsMatrix;
        }
    }
    
    private boolean requiresReverseOrdering(StatisticItem statisticItem)
    {
        if(statisticItem instanceof FoldChangeStatisticItem)
        {
            return true;
        }
        else
        {
            TestStatisticItem statisticTestItem = (TestStatisticItem)statisticItem;
            switch(statisticTestItem.getTestStatisticSubtype())
            {
                case F_OBSERVED:
                {
                    return true;
                }
                
                default:
                {
                    return false;
                }
            }
        }
    }

    /**
     * Filter values from the given matrix using the given threshold value
     * @param matrixHeader
     *          the header values for the stats matrix
     * @param statsMatrix
     *          the stats matrix to filter
     * @param filterStat
     *          the statistic to filter on
     * @param filterThreshold
     *          the threshold to use
     * @return
     *          the filtered array
     */
    private ProbesetRow[] filterMatrixByThreshold(
            List<StatisticItem> matrixHeader,
            final ProbesetRow[] statsMatrix,
            final StatisticItem filterStat,
            final double filterThreshold)
    {
        int filterColIndex = matrixHeader.indexOf(filterStat);
        if(filterColIndex == -1)
        {
            String warningMessage =
                "Cannot filter on " + filterStat + " because that column has " +
                "been hidden.";
            LOG.warning(warningMessage);
            MessageDialogUtilities.warn(
                    this,
                    warningMessage,
                    "Cannot Filter");
            return new ProbesetRow[0];
        }
        else
        {
            List<ProbesetRow> filteredList = new ArrayList<ProbesetRow>();
            
            final boolean takeAbsValue = filterStat instanceof FoldChangeStatisticItem;
            final boolean reverseOrder = this.requiresReverseOrdering(filterStat);
            final Condition<Double> filterCond = new Condition<Double>()
            {
                public boolean test(Double value)
                {
                    if(value == null)
                    {
                        return false;
                    }
                    else
                    {
                        double dblVal = value.doubleValue();
                        if(takeAbsValue)
                        {
                            dblVal = Math.abs(dblVal);
                        }
                        
                        if(reverseOrder)
                        {
                            return dblVal >= filterThreshold;
                        }
                        else
                        {
                            return dblVal <= filterThreshold;
                        }
                    }
                }
            };
            
            // loop through everything only keeping the values that pass
            // through the filter
            for(int i = 0; i < statsMatrix.length; i++)
            {
                if(filterCond.test(statsMatrix[i].getValues()[filterColIndex]))
                {
                    filteredList.add(statsMatrix[i]);
                }
            }
            
            return filteredList.toArray(new ProbesetRow[filteredList.size()]);
        }
    }

    /**
     * Get the 2D stats matrix. This returns a 2D row x column matrix where
     * the rows can be given directly to a JTable
     * @param testStatisticsItems
     *          the items to extract
     * @return
     *          the matrix
     */
    private ProbesetRow[] getStatisticsMatrix(List<StatisticItem> testStatisticsItems)
    {
        Double[][] matrix = new Double[testStatisticsItems.size()][];
        
        for(int i = 0; i < matrix.length; i++)
        {
            StatisticItem currStatItem = testStatisticsItems.get(i);
            if(currStatItem instanceof FoldChangeStatisticItem)
            {
                matrix[i] = this.testResult.getFoldChangeValues(
                        this.testNumberComboBox.getSelectedIndex());
            }
            else
            {
                TestStatisticItem currTestStatItem = (TestStatisticItem)currStatItem;
                MaanovaTestStatistics stats = this.testResult.getStatistics(
                        currTestStatItem.getTestStatisticType());
                matrix[i] = stats.getValues(
                        currTestStatItem.getTestStatisticSubtype(),
                        this.testNumberComboBox.getSelectedIndex());
            }
        }
        
        // transpose the transpose so that data rows are together
        matrix = SequenceUtilities.transposeMatrix(matrix);
        
        String[] probesetIds = this.testResult.getProbesetIds();
        
        assert probesetIds.length == matrix.length;
        
        ProbesetRow[] probesetRows = new ProbesetRow[matrix.length];
        for(int row = 0; row < probesetRows.length; row++)
        {
            probesetRows[row] = new ProbesetRow(probesetIds[row], matrix[row], row);
        }
        
        return SequenceUtilities.retainIndices(
                this.validIndices,
                probesetRows);
    }
    
    /**
     * Get selected test statistics
     * @return  the selected test statistics
     */
    private List<StatisticItem> getSelectedStatistics()
    {
        if(this.subsetColumnsDialog == null)
        {
            return this.getAllStatistics();
        }
        else
        {
            return this.subsetColumnsDialog.getSelectedStatistics();
        }
    }
    
    private List<StatisticItem> getAllStatistics()
    {
        List<StatisticItem> stats = new ArrayList<StatisticItem>();
        stats.add(new FoldChangeStatisticItem());
        
        for(MaanovaTestStatisticType currType: MaanovaTestStatisticType.values())
        {
            for(MaanovaTestStatisticSubtype currSubtype: MaanovaTestStatisticSubtype.values())
            {
                MaanovaTestStatistics currStats = this.testResult.getStatistics(currType);
                if(currStats.hasTestStatistic(currSubtype))
                {
                    stats.add(new TestStatisticItem(
                            currType,
                            currSubtype));
                }
            }
        }
        
        return stats;
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

        testNumberLabel = new javax.swing.JLabel();
        testNumberComboBox = new javax.swing.JComboBox();
        filterSortRowsButton = new javax.swing.JButton();
        subsetColumnsButton = new javax.swing.JButton();
        javax.swing.JScrollPane resultsTableScrollPane = new javax.swing.JScrollPane();
        resultsTable = new javax.swing.JTable();
        javax.swing.JLabel totalRowCountLabel = new javax.swing.JLabel();
        totalRowCountTextField = new javax.swing.JTextField();
        javax.swing.JLabel selectedRowCountLabel = new javax.swing.JLabel();
        selectedRowCountTextField = new javax.swing.JTextField();
        exportToFlatFileButton = new javax.swing.JButton();
        showVolcanoPlotButton = new javax.swing.JButton();
        saveGeneListButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();

        testNumberLabel.setText("Test Number:");

        filterSortRowsButton.setText("Filter/Sort Rows...");

        subsetColumnsButton.setText("Add/Remove Columns...");

        resultsTableScrollPane.setViewportView(resultsTable);

        totalRowCountLabel.setText("Total Row Count:");

        totalRowCountTextField.setEditable(false);

        selectedRowCountLabel.setText("Selected Row Count:");

        selectedRowCountTextField.setEditable(false);

        exportToFlatFileButton.setText("Export To CSV...");

        showVolcanoPlotButton.setText("Show Volcano Plot...");

        saveGeneListButton.setText("Add Selected Rows to List...");

        helpButton.setText("Help...");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(resultsTableScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(testNumberLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(testNumberComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filterSortRowsButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(subsetColumnsButton))
                    .add(layout.createSequentialGroup()
                        .add(exportToFlatFileButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(showVolcanoPlotButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(saveGeneListButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(helpButton))
                    .add(layout.createSequentialGroup()
                        .add(totalRowCountLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(totalRowCountTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(selectedRowCountLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(selectedRowCountTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(20, 20, 20)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(testNumberLabel)
                    .add(testNumberComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(filterSortRowsButton)
                    .add(subsetColumnsButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(resultsTableScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(totalRowCountLabel)
                    .add(totalRowCountTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(selectedRowCountLabel)
                    .add(selectedRowCountTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(exportToFlatFileButton)
                    .add(showVolcanoPlotButton)
                    .add(saveGeneListButton)
                    .add(helpButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton exportToFlatFileButton;
    private javax.swing.JButton filterSortRowsButton;
    private javax.swing.JButton helpButton;
    private javax.swing.JTable resultsTable;
    private javax.swing.JButton saveGeneListButton;
    private javax.swing.JTextField selectedRowCountTextField;
    private javax.swing.JButton showVolcanoPlotButton;
    private javax.swing.JButton subsetColumnsButton;
    private javax.swing.JComboBox testNumberComboBox;
    private javax.swing.JLabel testNumberLabel;
    private javax.swing.JTextField totalRowCountTextField;
    // End of variables declaration//GEN-END:variables

}
