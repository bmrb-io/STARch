/*
 * Main.java
 *
 * Created on December 22, 2004, 2:40 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/gui/Main.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/10/20 22:20:21 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Main.java,v $
 * Revision 1.5  2005/10/20 22:20:21  dmaziuk
 * typo, chmod fix
 *
 * Revision 1.4  2005/05/20 23:07:53  dmaziuk
 * *** empty log message ***
 *
 * Revision 1.3  2005/05/13 22:45:51  dmaziuk
 * working on better error reporting
 *
 * Revision 1.2  2005/05/10 18:56:43  dmaziuk
 * added functionality
 *
 * Revision 1.1  2005/04/13 17:37:08  dmaziuk
 * *** empty log message ***
 * */

package EDU.bmrb.starch.gui;

/**
 *
 * @author  dmaziuk
 */
public class Main extends javax.swing.JFrame {
    private final static boolean DEBUG = false;
    /** Loop table */
    private EDU.bmrb.starch.LoopTable fLt = null;
    /** Ref. DB */
    private EDU.bmrb.starch.RefDB fRefDb = null;
    /** Error list */
    private EDU.bmrb.starch.ErrorList fErrs = null;
    /** Residue type menu */
    private javax.swing.JMenu fResTypeMenu = null;
    /** Nomenclature menu */
    private javax.swing.JMenu fNomenMenu = null;
    /** Sf_ID option */
    private javax.swing.JCheckBoxMenuItem fSfIdOpt = null;
    /** Grid */
    private javax.swing.JTable fGrid = null;
    /** Error text area */
    private javax.swing.JTextArea fErrors = null;
    /** file chooser */
    private javax.swing.JFileChooser fChooser = null;
    /** Action map */
    private Actions fActions = null;
    /** Properties */
    private java.util.Properties fProps = null;
    /** nomenclature map */
    private EDU.bmrb.starch.Nomenclmap fNomenmap = null;
    /** residue type ID */
    private int fResTypeId = -1;
    /** nomenclature ID */
    private int fNomenId = 0;
//******************************************************************************
    /** Cell renderer for required columns: sets backround to red if value is null */
    static class RedCellRenderer extends javax.swing.JLabel
    implements javax.swing.table.TableCellRenderer {
        public RedCellRenderer( java.awt.Font font ) { 
            super();
            setOpaque( true );
            setFont( font );
        }
        public java.awt.Component getTableCellRendererComponent( javax.swing.JTable table,
        Object value, boolean isSelected, boolean hasFocus, int row, int col ) {
            String txt = null;
            if( value != null ) txt = value.toString();
            if( (txt == null) || (txt.trim().length() < 1) || txt.trim().equals( "." ) ) {
                setBackground( java.awt.Color.RED );
            }
            else {
                if( isSelected ) setBackground( table.getSelectionBackground() );
                else setBackground( table.getBackground() );
            }
            setText( (txt == null) ? "" : txt );
            return this;
        }        
    }
    /** Cell renderer for author residue & atom name: red background if null, 
        yellow background if different from BMBR name */
    static class YellowCellRenderer extends javax.swing.JLabel 
    implements javax.swing.table.TableCellRenderer {
        public YellowCellRenderer( java.awt.Font font ) { 
            super();
            setOpaque( true );
            setFont( font );
        }
        public java.awt.Component getTableCellRendererComponent( javax.swing.JTable table,
        Object value, boolean isSelected, boolean hasFocus, int row, int col ) {
            String txt = null;
            if( value != null ) txt = value.toString();
            if( (txt == null) || (txt.trim().length() < 1) || txt.trim().equals( "." ) )
                setBackground( java.awt.Color.RED );
            else {
//FIXME: hardcoded column number
                String atom = (String) table.getValueAt( row,  col - 5 );
                if( ! atom.equals( txt ) ) setBackground( java.awt.Color.YELLOW );
                else {
                    if( isSelected ) setBackground( table.getSelectionBackground() );
                    else setBackground( table.getBackground() );
                }
            }
            setText( (txt == null) ? "" : txt );
            return this;
        }        
    }
//******************************************************************************
    /** Creates new form Main.
     * @param table loop table
     * @param refdb reference database
     * @param errs error list
     * @param nomenmap nomenclature map
     */
    public Main( EDU.bmrb.starch.LoopTable table, EDU.bmrb.starch.RefDB refdb,
    EDU.bmrb.starch.ErrorList errs, java.util.Properties props,
    EDU.bmrb.starch.Nomenclmap nomenmap ) {
        fActions = new Actions( this, table, refdb );
        fLt = table;
        fRefDb = refdb;
        fErrs = errs;
        fProps = props;
        fNomenmap = nomenmap;
        init( table );
        this.setSize( 500, 300 );
    } //************************************************************************
    /** Initialize components.
     * @param model data source for the grid
     */
    private void init( javax.swing.table.AbstractTableModel model ) {
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
// errors
        fErrors = new javax.swing.JTextArea();
        fErrors.setEditable( false );
        fErrors.setSelectionColor( java.awt.Color.RED );
        fErrors.setSelectedTextColor( java.awt.Color.YELLOW );
        fErrors.addMouseListener( new java.awt.event.MouseAdapter() {
            public void mouseClicked( java.awt.event.MouseEvent evt ) {
                errorAreaMouseClicked( evt );
            }
        } );
// menus
        javax.swing.JMenuBar bar = new javax.swing.JMenuBar();
        javax.swing.JMenu m;
        javax.swing.JMenuItem mi;
// File
        m = new javax.swing.JMenu( "File" );
        fActions.setReadFile( new ReadFile( fLt, fErrs, fErrors, fProps ) );
        fActions.setSaveFile( new SaveFile( fLt, this, fProps ) );
        m.add( new javax.swing.JMenuItem( fActions.getReadFile() ) );
        m.add( new javax.swing.JMenuItem( fActions.getSaveFile() ) );
        m.add( new javax.swing.JSeparator() );
        mi = new javax.swing.JMenuItem( "Exit" );
        mi.setToolTipText( "Quit STARch" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                exitForm( null );
            }
        } );
        m.add( mi );
        bar.add( m );
// Edit
        m = new javax.swing.JMenu( "Convert" );
// convert data
        mi = new javax.swing.JMenuItem( "Convert data" );
        mi.setToolTipText( "Convert author data" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fActions.convertData();
                fLt.fireTableDataChanged();
                showErrors();
            }
        } );
        m.add( mi );
        m.add( new javax.swing.JSeparator() );
// delete empty rows
        mi = new javax.swing.JMenuItem( "  Delete empty rows" );
        mi.setToolTipText( "Delete rows where chemical shift value or author atom name is null" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                try {
                    EDU.bmrb.starch.TableUtils.deleteEmptyRows( fLt );
                    EDU.bmrb.starch.TableUtils.reindexRows( fLt );
                    fLt.fireTableDataChanged();
                    showErrors();
                }
                catch( java.sql.SQLException e ) {
                    fErrs.addError( "DB exception: " + e.getMessage() );
                    System.err.println( e );
                    e.printStackTrace();
                }
            }
        } );
        m.add( mi );
// convert residues
        mi = new javax.swing.JMenuItem( "  Convert residues" );
        mi.setToolTipText( "Convert residue labels from _Author_comp_code to Comp_ID" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                try {
                    fActions.convertResidues();
                    EDU.bmrb.starch.TableUtils.reindexRows( fLt );
                    showErrors();
                    fLt.fireTableDataChanged();
                }
                catch( java.sql.SQLException e ) {
                    fErrs.addError( "DB exception: " + e.getMessage() );
                    System.err.println( e );
                    e.printStackTrace();
                }
            }
        } );
        m.add( mi );
// reindex residues
        mi = new javax.swing.JMenuItem( "  Reindex residues" );
        mi.setToolTipText( "Renumber residue sequence (Comp_index_ID and Seq_ID)" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fActions.reindexResidues();
                fLt.fireTableDataChanged();
            }
        } );
        m.add( mi );
// convert atoms
        mi = new javax.swing.JMenuItem( "  Convert atoms" );
        mi.setToolTipText( "Convert atom names from Author_atom_code to Atom_ID" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                try {
                    fActions.convertAtoms();
                    EDU.bmrb.starch.TableUtils.sort( fLt, getResidueTypeId() );
                    EDU.bmrb.starch.TableUtils.reindexRows( fLt );
                    showErrors();
                    fLt.fireTableDataChanged();
                }
                catch( java.sql.SQLException e ) {
                    fErrs.addError( "DB exception: " + e.getMessage() );
                    System.err.println( e );
                    e.printStackTrace();
                }
            }
        } );
        m.add( mi );
// add atom types
        mi = new javax.swing.JMenuItem( "  Add atom types" );
        mi.setToolTipText( "Add atom types to Atom_type column" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fActions.addAtomTypes();
                fLt.fireTableDataChanged();
            }
        } );
        m.add( mi );
// sort atoms
        mi = new javax.swing.JMenuItem( "  Sort rows" );
        mi.setToolTipText( "Sort atoms in Greek-alphabetical order" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                try {
                    EDU.bmrb.starch.TableUtils.sort( fLt, getResidueTypeId() );
                    EDU.bmrb.starch.TableUtils.reindexRows( fLt );
                    fLt.fireTableRowsUpdated( 0, fLt.getRowCount() );
                }
                catch( java.sql.SQLException e ) {
                    System.err.println( e );
                    e.printStackTrace();
                }
            }
        } );
        m.add( mi );
// reindex atoms
        mi = new javax.swing.JMenuItem( "  Reindex rows" );
        mi.setToolTipText( "Renumber rows" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                try {
                    EDU.bmrb.starch.TableUtils.reindexRows( fLt );
                    fLt.fireTableRowsUpdated( 0, fLt.getRowCount() );
                }
                catch( java.sql.SQLException e ) {
                    System.err.println( e );
                    e.printStackTrace();
                }
            }
        } );
        m.add( mi );
// convert ambiguity codes
        mi = new javax.swing.JMenuItem( "  Edit ambiguity codes" );
        mi.setToolTipText( "Replace incorrect ambiguity codes" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                try {
                    EDU.bmrb.starch.TableUtils.convertAmbicodes( fLt, fRefDb, fErrs );
                    fLt.fireTableRowsUpdated( 0, fLt.getRowCount() );
                    showErrors();
                }
                catch( java.sql.SQLException e ) {
                    System.err.println( e );
                    e.printStackTrace();
                }
            }
        } );
        m.add( mi );
//        m.add( new javax.swing.JSeparator() );
// fix methylenes
        mi = new javax.swing.JMenuItem( "  Fix methylene protons" );
        mi.setToolTipText( "Replace methylene protons e.g. HB1,HB2->HB2,HB3" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fActions.fixMethylenes();
                fLt.fireTableRowsUpdated( 0, fLt.getRowCount() );
//                showErrors();
            }
        } );
        m.add( mi );
        m.add( new javax.swing.JSeparator() );
// copy author data
        mi = new javax.swing.JMenuItem( "Copy author data" );
        mi.setToolTipText( "Copy residue and atom names from _Author* columns without conversion" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                try {
                    EDU.bmrb.starch.TableUtils.copyFromAuthorData( fLt );
                    fLt.fireTableRowsUpdated( 0, fLt.getRowCount() );
                }
                catch( java.sql.SQLException e ) {
                    System.err.println( e );
                    e.printStackTrace();
                }
            }
        } );
        m.add( mi );
        m.add( new javax.swing.JSeparator() );
// convert atoms to 2.1
        mi = new javax.swing.JMenuItem( "Convert atoms to 2.1" );
        mi.setToolTipText( "Convert atom names to NMR-STAR 2.1 format" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                try {
                    EDU.bmrb.starch.TableUtils.convertAtomsTo21( fLt, fRefDb );
                    EDU.bmrb.starch.TableUtils.reindexRows( fLt );
                    fLt.fireTableDataChanged();
                }
                catch( java.sql.SQLException e ) {
                    System.err.println( e );
                    e.printStackTrace();
                }
            }
        } );
        m.add( mi );
        bar.add( m );
// Insert
        m = new javax.swing.JMenu( "Selected..." );
// convert selected residues
        mi = new javax.swing.JMenuItem( "Convert residue(s)" );
        mi.setToolTipText( "Convert selected residue(s)" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( fGrid.getSelectedRowCount() == 0 ) return;
                int [] rows = fGrid.getSelectedRows();
                String [] res = new String[rows.length];
                for( int i = 0; i < rows.length; i++ ) {
                    res[i] = (String) fGrid.getValueAt( rows[i], 10 );
                }
                try {
                    fActions.convertResidues( res );
                    EDU.bmrb.starch.TableUtils.reindexRows( fLt );
                    showErrors();
                    fLt.fireTableDataChanged();
                }
                catch( java.sql.SQLException e ) {
                    fErrs.addError( "DB exception: " + e.getMessage() );
                    System.err.println( e );
                    e.printStackTrace();
                }
            }
        } );
        m.add( mi );
// convert selected atoms
        mi = new javax.swing.JMenuItem( "Convert atom(s)" );
        mi.setToolTipText( "Convert selected atom(s)" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( fGrid.getSelectedRowCount() == 0 ) return;
                int [] rows = fGrid.getSelectedRows();
                String [] atoms = new String[rows.length];
                String [] res = new String[rows.length];
                int [] seq = new int[rows.length];
                for( int i = 0; i < rows.length; i++ ) {
                    seq[i] = Integer.parseInt((String) fGrid.getValueAt( rows[i], 4 ));
                    res[i] = (String) fGrid.getValueAt( rows[i], 5 );
                    atoms[i] = (String) fGrid.getValueAt( rows[i], 11 );
                }
                try {
                    fActions.convertSelectedAtoms( seq, res, atoms );
                    EDU.bmrb.starch.TableUtils.reindexRows( fLt );
                    showErrors();
                    fLt.fireTableDataChanged();
                }
                catch( java.sql.SQLException e ) {
                    fErrs.addError( "DB exception: " + e.getMessage() );
                    System.err.println( e );
                    e.printStackTrace();
                }
            }
        } );
        m.add( mi );
        m.add( new javax.swing.JSeparator() );
// delete selected rows
        mi = new javax.swing.JMenuItem( "Delete row(s)" );
        mi.setToolTipText( "Delete selected row(s)" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( fGrid.getSelectedRowCount() < 1 ) return;
                try {
                    int [] rows = fGrid.getSelectedRows();
                    for( int i = 0; i < rows.length; i++ ) {
//System.err.println( "Selected row " + rows[i] );
                        fLt.deleteRowById( rows[i] + 1 );
                    }
                    fLt.fireTableDataChanged();
                }
                catch( java.sql.SQLException e ) {
                    System.err.println( e );
                    e.printStackTrace();
                }
            }
        } );
        m.add( mi );
        bar.add( m );
// Insert
        m = new javax.swing.JMenu( "Insert" );
// insert default values
        mi = new javax.swing.JMenuItem( "All defaults" );
        mi.setToolTipText( "Insert default IDs" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fActions.insertDefaultValues();
                fLt.fireTableDataChanged();
            }
        } );
        m.add( mi );
        m.add( new javax.swing.JSeparator() );
// atom isotopes
        mi = new javax.swing.JMenuItem( "Atom isotopes" );
        mi.setToolTipText( "Insert atom isotopes" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fActions.addAtomIsotopes();
                fLt.fireTableDataChanged();
            }
        } );
        m.add( mi );
// shift errors
        mi = new javax.swing.JMenuItem( "Shift errors" );
        mi.setToolTipText( "Insert chemical shift errors for isotope" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fActions.addShiftErrors();
                fLt.fireTableDataChanged();
            }
        } );
        m.add( mi );
        m.add( new javax.swing.JSeparator() );
// accession number
        mi = new javax.swing.JMenuItem( "Entry ID" );
        mi.setToolTipText( "Insert BMRB accession number (Entry_ID)" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fActions.addAccessionNumber();
                fLt.fireTableDataChanged();
//                fLt.fireTableRowsUpdated( 0, fLt.getRowCount() );
            }
        } );
        m.add( mi );
// entity ID
        mi = new javax.swing.JMenuItem( "Entity ID" );
        mi.setToolTipText( "Insert Entity_ID (value of corresp. _Entity.ID tag)" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fActions.addEntityID();
                fLt.fireTableDataChanged();
//                fLt.fireTableRowsUpdated( 0, fLt.getRowCount() );
            }
        } );
        m.add( mi );
// entity assembly ID -- always 1
        mi = new javax.swing.JMenuItem( "Entity assembly ID" );
        mi.setToolTipText( "Insert Entity_assembly_ID (always 1)" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fActions.addEntityAssemblyID();
                fLt.fireTableDataChanged();
            }
        } );
        m.add( mi );
// assigned_chem_shift_list_ID
        mi = new javax.swing.JMenuItem( "Shift list ID" );
        mi.setToolTipText( "Insert assigned_chem_shift_list_ID (value of corresp." +
        " _Assigned_chem_shift_list.ID tag)" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fActions.addListID();
                fLt.fireTableDataChanged();
            }
        } );
        m.add( mi );
        m.add( new javax.swing.JSeparator() );
// saveframe ID
        mi = new javax.swing.JMenuItem( "Saveframe ID" );
        mi.setToolTipText( "Insert Sf_ID (value of corresp. _Assigned_chem_shift_list.Sf_ID tag)" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fActions.addSaveframeID();
                fLt.fireTableDataChanged();
            }
        } );
        m.add( mi );
        bar.add( m );
// validate
        m = new javax.swing.JMenu( "Validate" );
// author data
        mi = new javax.swing.JMenuItem( "Author data" );
        mi.setToolTipText( "Report invalid atoms in author data" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fErrs.clear();
                fActions.checkAuthorData();
                showErrors();
            }
        } );
        m.add( mi );
        m.add( new javax.swing.JSeparator() );
// duplicates
        mi = new javax.swing.JMenuItem( "Duplicate atoms" );
        mi.setToolTipText( "Report duplicate atoms in residues" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fErrs.clear();
                fActions.checkDuplicates();
                showErrors();
            }
        } );
        m.add( mi );
// invalid
        mi = new javax.swing.JMenuItem( "Invalid atoms" );
        mi.setToolTipText( "Report invalid atoms in residues" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fErrs.clear();
                fActions.checkAtoms();
                showErrors();
            }
        } );
        m.add( mi );
        bar.add( m );
// Options
        m = new javax.swing.JMenu( "Options" );
// residue type
        fResTypeMenu = new javax.swing.JMenu( "Residue type" );
        javax.swing.ButtonGroup grp = new javax.swing.ButtonGroup();
        javax.swing.JCheckBoxMenuItem box = new javax.swing.JCheckBoxMenuItem( "other" );
        java.awt.event.ItemListener il = new java.awt.event.ItemListener() {
            public void itemStateChanged( java.awt.event.ItemEvent evt ) {
                residueTypeChanged( evt );
            }
        };
        box.addItemListener( il );
//        box.setSelected( true );
        grp.add( box );
        fResTypeMenu.add( box );
        String [] restypes = null;
        try { restypes = fRefDb.getResidueTypes(); }
        catch( java.sql.SQLException e ) {
            System.err.println( "Cannot fetch residue types" );
            System.err.println( e );
            e.printStackTrace();
        }
        if( restypes != null )
            for( int i = 0; i < restypes.length; i++ ) {
                box = new javax.swing.JCheckBoxMenuItem( restypes[i] );
                box.addItemListener( il );
if( DEBUG ) System.err.println( "Adding residue type |" + restypes[i] + "|" );
                if( restypes[i].equals( "polypeptide" ) ) {
                    box.setSelected( true );
                    try { fResTypeId = fRefDb.getResidueTypeId( "polypeptide" ); }
                    catch( java.sql.SQLException e ) {
                        System.err.println( e );
                        e.printStackTrace();
                    }
                }
                grp.add( box );
                fResTypeMenu.add( box );
            }
        m.add( fResTypeMenu );
//        m.add( new javax.swing.JSeparator() );
// nomenclature
        fNomenMenu = new javax.swing.JMenu( "Atom nomenclature" );
        grp = new javax.swing.ButtonGroup();
        il = new java.awt.event.ItemListener() {
            public void itemStateChanged( java.awt.event.ItemEvent evt ) {
                nomenclatureChanged( evt );
            }
        };
        try { restypes = fRefDb.getNomenclatures(); }
        catch( java.sql.SQLException e ) {
            System.err.println( "Cannot fetch nomenclature names" );
            System.err.println( e );
            e.printStackTrace();
        }
        if( restypes != null )
            for( int i = 0; i < restypes.length; i++ ) {
if( DEBUG ) System.err.println( "Adding nomenclature |" + restypes[i] + "|" );
                box = new javax.swing.JCheckBoxMenuItem( restypes[i] );
                box.addItemListener( il );
                grp.add( box );
                if( i == 0 ) box.setSelected( true );
                fNomenMenu.add( box );
            }
        m.add( fNomenMenu );
// Sf_ID option
        m.add( new javax.swing.JSeparator() );
        fSfIdOpt = new javax.swing.JCheckBoxMenuItem( "Print Sf_ID" );
        m.add( fSfIdOpt );
        bar.add( m );
// toolbar
        javax.swing.JToolBar tb = new javax.swing.JToolBar();
        tb.add( new javax.swing.JButton( fActions.getReadFile() ) );
        tb.add( new javax.swing.JButton( fActions.getSaveFile() ) );
        tb.add( new javax.swing.JToolBar.Separator() );
        javax.swing.JButton btn = new javax.swing.JButton( "Run all conversions" );
        btn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( fActions.convertData() ) {
                    fActions.insertDefaultValues();
                    fLt.fireTableDataChanged();
                }
                else
                    javax.swing.JOptionPane.showMessageDialog( ((javax.swing.JComponent)evt.getSource()).getParent().getParent(), 
                    "Too many errors, conversion failed.\nCheck error output",
                    "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
                showErrors();
            }
        } );
        tb.add( btn );
// splitter
        javax.swing.JSplitPane spl = new javax.swing.JSplitPane();
        spl.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
// grid
        fGrid = new javax.swing.JTable( model );
        fGrid.setAutoResizeMode( javax.swing.JTable.AUTO_RESIZE_OFF );
        fGrid.setShowGrid( true );
        fGrid.setGridColor( java.awt.Color.BLACK );
//        fGrid.setColumnSelectionAllowed( true );
//FIXME: remove hardcode?
        RedCellRenderer rdr = new RedCellRenderer( fGrid.getFont() );
        YellowCellRenderer rdy = new YellowCellRenderer( fGrid.getFont() );
        for( int i = 0; i < fGrid.getColumnCount(); i++ ) {
//            if( i < 9 ) fGrid.getColumn( fGrid.getColumnName( i ) ).setPreferredWidth(
//            fGrid.getColumn( fGrid.getColumnName( i ) ).getPreferredWidth() / 2 );
            if( fGrid.getColumnName( i ).equals( "<html>Author<br>seq<br>code</html>" )
            || fGrid.getColumnName( i ).equals( "<html>Author<br>comp<br>code</html>" )
            || fGrid.getColumnName( i ).equals( "<html>Author<br>atom<br>code</html>" ) )
                fGrid.getColumn( fGrid.getColumnName( i ) ).setCellRenderer( rdy );
            else if( i < fGrid.getColumnCount() - 1 )
                fGrid.getColumn( fGrid.getColumnName( i ) ).setCellRenderer( rdr );
        }
//        fGrid.getColumn( "Atom_ID" ).setCellEditor( new AtomNameEditor( this ) );
        fGrid.getColumn( "<html>Atom<br>ID</html>" ).setCellEditor( new AtomNameEditor( this ) );
/*
        fGrid.getColumn( "Comp_index_ID" ).setCellRenderer( rdr );
        fGrid.getColumn( "Seq_ID" ).setCellRenderer( rdr );
        fGrid.getColumn( "Comp_ID" ).setCellRenderer( rdr );
        fGrid.getColumn( "Atom_ID" ).setCellRenderer( rdr );
        fGrid.getColumn( "Atom_type" ).setCellRenderer( rdr );
        fGrid.getColumn( "Atom_isotope" ).setCellRenderer( rdr );
        fGrid.getColumn( "Author_atom_code" ).setCellRenderer( new YellowCellRenderer( fGrid.getFont() ) );
        fGrid.getColumn( "Chem_shift_val" ).setCellRenderer( rdr );
        fGrid.getColumn( "Chem_shift_val_err" ).setCellRenderer( rdr );
 */
        fGrid.getTableHeader().addMouseListener( new java.awt.event.MouseAdapter () {
            public void mouseClicked( java.awt.event.MouseEvent evt ) {
                javax.swing.table.JTableHeader h = (javax.swing.table.JTableHeader) evt.getSource();
                javax.swing.table.TableColumnModel cm = h.getColumnModel();
                int vcol = cm.getColumnIndexAtX( evt.getX() );
                int col = cm.getColumn( vcol ).getModelIndex();
                if( col < 0 ) return;
                fActions.clearColumn( col );
                fLt.fireTableDataChanged();
            }
        } );
if( DEBUG ) {
    System.err.println( "header preferred h = " + fGrid.getTableHeader().getPreferredSize().getHeight() );
    System.err.println( "header h = " + fGrid.getTableHeader().getHeight() );
}
        java.awt.Dimension d = fGrid.getTableHeader().getPreferredSize();
        d.setSize( d.getWidth(), d.getHeight() * 6 );
        fGrid.getTableHeader().setPreferredSize( d );
if( DEBUG ) {
    System.err.println( "* header preferred h = " + fGrid.getTableHeader().getPreferredSize().getHeight() );
    System.err.println( "* header h = " + fGrid.getTableHeader().getHeight() );
}
//        fGrid.getTableHeader().setPreferredSize( new java.awt.Dimension( 
//        fGrid.getTableHeader().getWidth(),fGrid.getTableHeader().getHeight() * 3 ) );
        javax.swing.JScrollPane scr = new javax.swing.JScrollPane( fGrid );
        scr.setAutoscrolls( true );
        scr.setEnabled( false );
//        scr.setViewportView( fGrid );
        spl.setLeftComponent( scr );
// errors
        scr = new javax.swing.JScrollPane();
        scr.setAutoscrolls( true );
        scr.setEnabled( false );
        scr.setViewportView( fErrors );
        spl.setRightComponent( scr );
        spl.setResizeWeight( 1 );
//
        setJMenuBar( bar );
        getContentPane().add( tb, java.awt.BorderLayout.NORTH );
        getContentPane().add( spl, java.awt.BorderLayout.CENTER );
        
        pack();
    } //************************************************************************
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {
        try { 
            fLt.disconnect();
            fRefDb.disconnect();
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
        }
        System.exit(0);
    } //************************************************************************
    /** Returns action map.
     * @return action map
     */
    public Actions getActions() {
        return fActions;
    } //************************************************************************
    /** Returns main table.
     * @return table
     */
    public javax.swing.JTable getGrid() {
        return fGrid;
    } //************************************************************************
    /** Returns reference db.
     * @return refdb
     */
    public EDU.bmrb.starch.RefDB getRefdb() {
        return fRefDb;
    } //************************************************************************
    /** Returns DB table.
     * @return table
     */
    public EDU.bmrb.starch.LoopTable getTable() {
        return fLt;
    } //************************************************************************
    /** Returns residue type.
     * @return residue type
     */
    public String getResidueType() {
        for( int i = 0; i < fResTypeMenu.getItemCount(); i++ ) {
            if( fResTypeMenu.getItem( i ) instanceof javax.swing.JCheckBoxMenuItem )
                if( ((javax.swing.JCheckBoxMenuItem) fResTypeMenu.getItem( i )).isSelected() )
                    return ((javax.swing.JCheckBoxMenuItem) fResTypeMenu.getItem( i )).getText();
        }
        return null;
    } //************************************************************************
    /** Returns residue type id.
     * @return residue type id (-1 for "other")
     */
    public int getResidueTypeId() {
        return fResTypeId;
    } //************************************************************************
    /** Fetch residue type ID when residue type changes.
     * @param evt check box event
     */
    public void residueTypeChanged( java.awt.event.ItemEvent evt ) {
        for( int i = 0; i < fResTypeMenu.getItemCount(); i++ ) {
            if( fResTypeMenu.getItem( i ) instanceof javax.swing.JCheckBoxMenuItem )
                if( ((javax.swing.JCheckBoxMenuItem) fResTypeMenu.getItem( i )).isSelected() ) {
                    String restype = ((javax.swing.JCheckBoxMenuItem) fResTypeMenu.getItem( i )).getText();
                    if( restype.equals( "other" ) ) fResTypeId = -1;
                    else {
                        try { fResTypeId = fRefDb.getResidueTypeId( restype ); }
                        catch( java.sql.SQLException e ) {
                            System.err.println( e );
                            e.printStackTrace();
                        }
                    }
                    break;
                }
        }
    } //************************************************************************
    /** Fetch nomenclature ID when nomenclature changes.
     * @param evt check box event
     */
    public void nomenclatureChanged( java.awt.event.ItemEvent evt ) {
if( DEBUG ) System.err.println( "nomenclatureChanged: " + fNomenMenu.getItemCount() );
        javax.swing.JCheckBoxMenuItem m;
        for( int i = 0; i < fNomenMenu.getItemCount(); i++ ) {
            if( fNomenMenu.getItem( i ) instanceof javax.swing.JCheckBoxMenuItem ) {
                m = (javax.swing.JCheckBoxMenuItem) fNomenMenu.getItem( i );
if( DEBUG ) System.err.println( "item: " + m.getText() );
                if( m.isSelected() ) {
                    String name = ((javax.swing.JCheckBoxMenuItem) fNomenMenu.getItem( i )).getText();
if( DEBUG ) System.err.println( "selected: " + name );
                    try { fNomenId = fRefDb.getNomenclatureId( name ); }
                    catch( java.sql.SQLException e ) {
                        System.err.println( e );
                        e.printStackTrace();
                    }
                    break;
                }
            }
        } // endfor
if( DEBUG ) System.err.println( "Nomenclature ID = " + fNomenId );
    } //************************************************************************
    /** Returns nomenclature id.
     * @return nomenclature id
     */
    public int getNomenclatureId() {
        return fNomenId;
    } //************************************************************************
    /** Returns error list.
     * @return error list
     */
    public EDU.bmrb.starch.ErrorList getErrorList() {
        return fErrs;
    } //************************************************************************
    /** Returns nomenclature map.
     * @return nomenclature map
     */
    public EDU.bmrb.starch.Nomenclmap getNomenmap() {
        return fNomenmap;
    } //************************************************************************
    /** Displays errors. */
    public void showErrors() {
        fErrors.setText( "" );
        if( fErrs.size() > 0 ) {
            fErrs.sort();
            int svr = EDU.bmrb.starch.Error.SVR_CRIT;
            EDU.bmrb.starch.Error err;
            for( java.util.Iterator i = fErrs.iterator(); i.hasNext(); ) {
                err = (EDU.bmrb.starch.Error) i.next();
                if( svr != err.getSeverity() ) {
                    fErrors.append( "\n" );
                    svr = err.getSeverity();
                }
                fErrors.append( err.toString() );
                fErrors.append( "\n" );
            }
//            fErrors.setText( fErrs.toString() );
        }
        fErrs.clear();
    } //************************************************************************
    /** Returns true if "print saveframe IDs" option is selected.
     * @return true or false
     */
    public boolean isPrintSfIds() {
        return fSfIdOpt.isSelected();
    } //************************************************************************
    /** Error text area mouse click.
     * @param evt mouse click event
     */
    private void errorAreaMouseClicked( java.awt.event.MouseEvent evt ) {
// limit to main button only
        if( evt.getButton() == java.awt.event.MouseEvent.BUTTON1 ) {
            int pos = fErrors.viewToModel( new java.awt.Point( evt.getX(), evt.getY() ) );
            try {
                int start = javax.swing.text.Utilities.getRowStart( fErrors, pos );
                int end = javax.swing.text.Utilities.getRowEnd( fErrors, pos );
                String msg = fErrors.getDocument().getText( start, end - start );
                fErrors.setSelectionStart( start );
                fErrors.setSelectionEnd( end );
// error number
                start = msg.indexOf( EDU.bmrb.starch.Error.SEPARATOR );
                if( start < 0 ) return;
// author residue sequence number
                start = msg.indexOf( ':', start + 1 );
                if( start < 0 ) return;
                end = msg.indexOf( ':', start + 1 );
                if( end < 0 ) return;
                String seq = msg.substring( start + 1, end );
                if( seq.trim().length() < 1 ) return;
// author residue label
                start = end + 1;
                end = msg.indexOf( ':', start );
                if( end < 0 ) return;
                String label = msg.substring( start, end );
                if( label.trim().length() < 1 ) label = null; 
// author atom
                start = end + 1;
                end = msg.indexOf( ':', start );
                if( end < 0 ) return;
                String atom = msg.substring( start, end );
                if( atom.trim().length() < 1 ) atom = null; 
                for( int i = 0; i < fGrid.getRowCount(); i++ ) {
                    if( fGrid.getValueAt( i, 9 ).toString().equals( seq ) ) {
                        if( atom == null ) {
                            if( label != null ) {
                                if( fGrid.getValueAt( i, 10 ).toString().equals( label ) ) {
                                    fGrid.setRowSelectionInterval( i, i );
                                    fGrid.scrollRectToVisible( fGrid.getCellRect( i, 0, true ) );
                                    break;
                                }
                            }
                            else {
                                fGrid.setRowSelectionInterval( i, i );
                                fGrid.scrollRectToVisible( fGrid.getCellRect( i, 0, true ) );
                                break;
                            }
                        }
                        else { // find atom
                            if( fGrid.getValueAt( i, 11 ).toString().equals( atom ) ) {
                                fGrid.setRowSelectionInterval( i, i );
                                fGrid.scrollRectToVisible( fGrid.getCellRect( i, 0, true ) );
                                break;
                            }
                        }
                    }
                }
            }
            catch( javax.swing.text.BadLocationException e ) { /* ignore */ }
        }
    } //************************************************************************
    /** Returns number of selected row.
     * @return row number or -1
     */
    public int getSelectedRow() {
        return fGrid.getSelectedRow();
    } //************************************************************************
    /** Main method
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            EDU.bmrb.starch.ErrorList errs = new EDU.bmrb.starch.ErrorList();
            EDU.bmrb.starch.LoopTable lt = new EDU.bmrb.starch.LoopTable();
            if( args.length > 0 ) FileChooser.getInstance( lt, args[0] );
            EDU.bmrb.starch.RefDB rd = new EDU.bmrb.starch.RefDB();
            EDU.bmrb.starch.Nomenclmap nm = new EDU.bmrb.starch.Nomenclmap( rd );
            Properties p = new Properties();
            lt.connect();
            lt.createTable();
            rd.connect();
            Main form = new Main( lt, rd, errs, p.getProperties(), nm );
            form.show();
        }
        catch( Exception e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
}
