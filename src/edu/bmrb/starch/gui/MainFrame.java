package edu.bmrb.starch.gui;

/**
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Nov 14, 2006
 * Time: 5:40:21 PM
 *
 * $Id$
 */

public class MainFrame extends javax.swing.JFrame {
    private static final boolean DEBUG = false;
    /** table model. */
    private edu.bmrb.starch.TableModel fModel = null;
    /** nomenclature mapper. */
    private edu.bmrb.starch.Nomenmap fNMap = null;
    /** error list. */
    private edu.bmrb.starch.ErrorList fErrList = null;
    /** controller. */
    private Actions fActions = null;
    /** Atom nomenclatures menu. */
    private javax.swing.JMenu fNomenMenu = null;
    /** Residue types. */
    private javax.swing.JMenu fResTypeMenu = null;
    /** error pane. */
    private javax.swing.JTextArea fErrors = null;
    /** table. */
    private javax.swing.JTable fGrid = null;
    /** current workdir. */
    private String fCwd = null;
    /** Grid header renderer. */
    private HdrRenderer fHr = null;
    /** Grid cell renderer. */
    private CellRenderer fCr = null;
/*******************************************************************************/
    /**
     * Constructor.
     * @param model table model
     * @param nomenmap nomenclature mapper
     * @param errs error list
     * @param actions controller
     */
    public MainFrame( edu.bmrb.starch.TableModel model, edu.bmrb.starch.Nomenmap nomenmap,
                      edu.bmrb.starch.ErrorList errs, Actions actions ) {
        super( "STARch" );
        setSize( 800, 600 );
        fModel = model;
        fErrList = errs;
        fNMap = nomenmap;
        fActions = actions;
        fHr = new HdrRenderer();
        fCr = new CellRenderer( fModel );
        init();
    } //*************************************************************************
    /**
     * Set current working directory for File - Open dialog.
     * @param cwd path
     */
    public void setCwd( String cwd ) {
        fCwd = cwd;
    } //*************************************************************************
    /**
     * Returns nomenclature mapper
     * @return nomenclature mapper
     */
    public edu.bmrb.starch.Nomenmap getNomenmap() {
        return fNMap;
    } //*************************************************************************
    /** Returns data drid.
     * @return grid
     */
    public javax.swing.JTable getGrid() {
        return fGrid;
    } //*************************************************************************
    public edu.bmrb.starch.ErrorList getErrorList() {
        return fErrList;
    } //*************************************************************************
    /**
     * Initialize the GUI.
     */
    private void init() {
        addWindowListener( new java.awt.event.WindowAdapter() {
            public void windowClosing( java.awt.event.WindowEvent evt ) {
                fActions.exitForm();
            }
        } );
// grid
        fGrid = new javax.swing.JTable( fModel );
        fGrid.setAutoResizeMode( javax.swing.JTable.AUTO_RESIZE_OFF );
        fGrid.setShowGrid( true );
        fGrid.setGridColor( java.awt.Color.BLACK );
        fGrid.setAutoscrolls( false );
        fGrid.getTableHeader().setResizingAllowed( true );
        final javax.swing.JScrollPane gridpane = new javax.swing.JScrollPane();
        gridpane.setAutoscrolls( true );
        gridpane.setEnabled( false );
        gridpane.setViewportView( fGrid );
// errors
        fErrors = new javax.swing.JTextArea();
        fErrors.setEditable( false );
        fErrors.setSelectionColor( java.awt.Color.RED );
        fErrors.setSelectedTextColor( java.awt.Color.YELLOW );
        javax.swing.JScrollPane errpane = new javax.swing.JScrollPane();
        errpane.setAutoscrolls( true );
        errpane.setEnabled( false );
        errpane.setViewportView( fErrors );
        java.awt.Dimension d = errpane.getPreferredSize();
//        d.setSize( d.getWidth(), 150 );
        d.setSize( d.getWidth(), d.getHeight() * 5 );
        errpane.setPreferredSize( d );
        fErrors.addMouseListener( new java.awt.event.MouseAdapter() {
            public void mouseClicked( java.awt.event.MouseEvent evt ) {
                errorAreaMouseClicked( evt );
            }
        } );
// menu
        javax.swing.JMenuBar bar = new javax.swing.JMenuBar();
        javax.swing.JMenu m;
        javax.swing.JMenuItem mi;
// file
        m = new javax.swing.JMenu( "File" );
// open
        mi = new javax.swing.JMenuItem( "Open" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fActions.readFile( fCwd );
                showErrors();
            }
        } );
        m.add( mi );
// save
        mi = new javax.swing.JMenuItem( "Save" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( ! fActions.saveFile( fCwd ) ) showErrors();
            }
        } );
        m.add( mi );
        m.add( new javax.swing.JSeparator() );
// exit
        mi = new javax.swing.JMenuItem( "Exit" );
        mi.setToolTipText( "Quit STARch" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fActions.exitForm();
            }
        } );
        m.add( mi );
        bar.add( m );
// precheck
        m = new javax.swing.JMenu( "Precheck" );
        mi = new javax.swing.JMenuItem( "Delete empty rows" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( ! fActions.deleteEmptyRows() ) {
                    showErrors();
                }
            }
        } );
        m.add( mi );
        mi = new javax.swing.JMenuItem( "Move author data" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( ! fActions.moveAuthorData() ) {
                    showErrors();
                }
            }
        } );
        m.add( mi );
        mi = new javax.swing.JMenuItem( "Check/insert entry ID" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( ! fActions.insertEntryId() ) {
                    showErrors();
                }
            }
        } );
        m.add( mi );
        mi = new javax.swing.JMenuItem( "Check/insert entity IDs" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( ! fActions.insertEntityId() ) {
                    showErrors();
                }
            }
        } );
        m.add( mi );
        mi = new javax.swing.JMenuItem( "Check for missing names" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fActions.checkMissingNames();
                showErrors();
            }
        } );
        m.add( mi );
        mi = new javax.swing.JMenuItem( "Check author data" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fActions.checkAuthorData();
                showErrors();
            }
        } );
        m.add( mi );
        bar.add( m );
// Convert
        m = new javax.swing.JMenu( "Convert" );
        mi = new javax.swing.JMenuItem( "Convert residues" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( ! fActions.convertResidues() ) {
                    showErrors();
                }
            }
        } );
        m.add( mi );
        mi = new javax.swing.JMenuItem( "Convert atoms" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( ! fActions.convertAtoms() ) {
                    showErrors();
                }
            }
        } );
        m.add( mi );
        mi = new javax.swing.JMenuItem( "Renumber residues" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( ! fActions.renumberResidues() ) {
                    showErrors();
                }
            }
        } );
        m.add( mi );
        mi = new javax.swing.JMenuItem( "Add atom types" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( ! fActions.insertAtomTypes() ) {
                    showErrors();
                }
            }
        } );
        m.add( mi );
        mi = new javax.swing.JMenuItem( "Sort atoms" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( ! fActions.sortAtoms() ) {
                    showErrors();
                }
            }
        } );
        m.add( mi );
        mi = new javax.swing.JMenuItem( "Reindex rows" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( ! fActions.reindexRows() ) {
                    showErrors();
                }
            }
        } );
        m.add( mi );
        mi = new javax.swing.JMenuItem( "Add atom isotopes" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( ! fActions.insertAtomIsotopes() ) {
                    showErrors();
                }
            }
        } );
        m.add( mi );
        mi = new javax.swing.JMenuItem( "Insert entity assembly IDs" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( ! fActions.insertEntityAssemblyIds() ) {
                    showErrors();
                }
            }
        } );
        m.add( mi );
        mi = new javax.swing.JMenuItem( "Insert local ID" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( ! fActions.insertLocalId() ) {
                    showErrors();
                }
            }
        } );
        m.add( mi );
// ambiguity codes
        m.add( new javax.swing.JSeparator() );
        mi = new javax.swing.JMenuItem( "Insert ambiguity codes" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( ! fActions.insertDefaultAmbicodes() ) {
                    showErrors();
                }
            }
        } );
        m.add( mi );
        mi = new javax.swing.JMenuItem( "Fix ambiguity codes" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( ! fActions.replaceAmbicodes() ) {
                    showErrors();
                }
            }
        } );
        m.add( mi );
        bar.add( m );
// options
// nomenclatures
        m = new javax.swing.JMenu( "Options" );
        edu.bmrb.starch.IntStringPair[] list = fModel.getDictionary().getNomenclatures();
        if( list != null ) {
            fNomenMenu = new javax.swing.JMenu( "Nomenclature" );
            javax.swing.ButtonGroup grp = new javax.swing.ButtonGroup();
            java.awt.event.ItemListener il = new java.awt.event.ItemListener() {
                public void itemStateChanged( java.awt.event.ItemEvent evt ) {
                    nomenclatureChanged( evt );
                }
            };
            for( edu.bmrb.starch.IntStringPair p : list ) {
                edu.bmrb.starch.gui.CBMenuItem box = new CBMenuItem( p.getString() );
                box.setID( p.getInt() );
                box.addItemListener( il );
                if( p.getString().equals( "BMRB" ) ) box.setSelected( true );
                grp.add( box );
                fNomenMenu.add( box );
            }
            m.add( fNomenMenu );
            nomenclatureChanged( null );
        }
// residue type
        list = fModel.getDictionary().getResidueTypes();
        if( list != null ) {
            fResTypeMenu = new javax.swing.JMenu( "Residue type" );
            javax.swing.ButtonGroup grp = new javax.swing.ButtonGroup();
            java.awt.event.ItemListener il = new java.awt.event.ItemListener() {
                public void itemStateChanged( java.awt.event.ItemEvent evt ) {
                    restypeChanged( evt );
                }
            };
            for( edu.bmrb.starch.IntStringPair p : list ) {
                edu.bmrb.starch.gui.CBMenuItem box = new CBMenuItem( p.getString() );
                box.setID( p.getInt() );
                box.addItemListener( il );
                grp.add( box );
                fResTypeMenu.add( box );
            }
            m.add( fResTypeMenu );
        }
// clear errors
        m.add( new javax.swing.JSeparator() );
        mi = new javax.swing.JMenuItem( "Clear errors" );
        mi.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fErrList.clear();
                fErrors.setText( "" );
            }
        } );
        m.add( mi );
        bar.add( m );
// toolbar
        javax.swing.JToolBar tb = new javax.swing.JToolBar();
        javax.swing.JButton btn = new javax.swing.JButton( "Open file" );
        btn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                fActions.readFile( fCwd );
                showErrors();
            }
        } );
        tb.add( btn );
        btn = new javax.swing.JButton( "Save file" );
        btn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                if( ! fActions.saveFile( fCwd ) ) showErrors();
            }
        } );
        tb.add( btn );
        btn = new javax.swing.JButton( "Precheck" );
        btn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
//                if( ! fActions.precheck() ) showErrors();
                fErrList.clear();
                fActions.precheck();
                if( fErrList.size() > 0 ) showErrors();
            }
        } );
        tb.add( btn );
        btn = new javax.swing.JButton( "Convert" );
        btn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
//                if( ! fActions.convert() ) showErrors();
                fActions.convert();
                if( fErrList.size() > 0 ) showErrors();
            }
        } );
        tb.add( btn );
// splitter
        javax.swing.JSplitPane splitter = new javax.swing.JSplitPane();
        splitter.setOrientation( javax.swing.JSplitPane.VERTICAL_SPLIT );
        splitter.setLeftComponent( gridpane );
        splitter.setRightComponent( errpane );
        splitter.setResizeWeight( 1 );
        splitter.setOneTouchExpandable( true );
        setJMenuBar( bar );
        getContentPane().add( tb, java.awt.BorderLayout.NORTH );
        getContentPane().add( splitter, java.awt.BorderLayout.CENTER );
    } //*************************************************************************
    /**
     * Rebuild grid columns.
     */
    public void initColumns() {
if( DEBUG ) System.err.printf( "Init renderers, numcols %d\n", fModel.getColumns().size() );
        javax.swing.table.TableColumn col;
        java.util.Enumeration<javax.swing.table.TableColumn> e = fGrid.getColumnModel().getColumns();
        while( e.hasMoreElements() ) {
            col = e.nextElement();
            col.setHeaderRenderer( fHr );
            col.setCellRenderer( fCr );
if( DEBUG ) System.err.printf( "Add renderer for column %s\n", col.getHeaderValue().toString() );
        }
        AtomNameEditor ed = new AtomNameEditor( this );
        for( edu.bmrb.starch.Column c : fModel.getColumns() ) {
            if( c.getType().equals( edu.bmrb.starch.ColTypes.ATOMID ) ) {
if( DEBUG ) System.err.printf( "Add editor for column %s\n", c.getLabel() );
                fGrid.getColumn( c.getLabel() ).setCellEditor( ed );
            }
        }

    } //*************************************************************************
    /**
     * Show error messages.
     */
    public void showErrors() {
        fErrors.setText( "" );
        edu.bmrb.starch.Error.Severity s = null;
        for( edu.bmrb.starch.Error e : fErrList ) {
            if( s == null ) s = e.getSeverity();
            else if( s != e.getSeverity() ) {
                fErrors.append( "\n" );
                s = e.getSeverity();
            }
            fErrors.append( e.toString() );
            fErrors.append( "\n" );
            fErrors.setCaretPosition( 0 );
        }
    } //*************************************************************************
    /**
     * Nomenclature change event.
     * @param evt event
     */
    private void nomenclatureChanged( java.awt.event.ItemEvent evt ) {
        for( int i = 0; i < fNomenMenu.getItemCount(); i++ ) {
            if( fNomenMenu.getItem( i ) instanceof edu.bmrb.starch.gui.CBMenuItem ) {
                if( (( edu.bmrb.starch.gui.CBMenuItem) fNomenMenu.getItem( i )).isSelected() ) {
                    fModel.setNomenclatureId( ((edu.bmrb.starch.gui.CBMenuItem) fNomenMenu.getItem( i )).getID() );
                    return;
                }
            }
        }
    } //*************************************************************************
    /**
     * Residue type change event.
     * @param evt event
     */
    private void restypeChanged( java.awt.event.ItemEvent evt ) {
        for( int i = 0; i < fResTypeMenu.getItemCount(); i++ ) {
            if( fResTypeMenu.getItem( i ) instanceof edu.bmrb.starch.gui.CBMenuItem ) {
                if( (( edu.bmrb.starch.gui.CBMenuItem) fResTypeMenu.getItem( i )).isSelected() ) {
                    fModel.setRestypeId( ((edu.bmrb.starch.gui.CBMenuItem) fResTypeMenu.getItem( i )).getID() );
                    return;
                }
            }
        }
    } //*************************************************************************
    /**
     * Mouse click on error message.
     * Highlights the message and trues to move cursor to corresp. row in the grid.
     * @param evt mouse click event
     */
    private void errorAreaMouseClicked( java.awt.event.MouseEvent evt ) {
// limit to button 1 only
        if( evt.getButton() == java.awt.event.MouseEvent.BUTTON1 ) {
            int pos = fErrors.viewToModel( new java.awt.Point( evt.getX(), evt.getY() ) );
            try {
                int start = javax.swing.text.Utilities.getRowStart( fErrors, pos );
                int end = javax.swing.text.Utilities.getRowEnd( fErrors, pos );
                String msg = fErrors.getDocument().getText( start, end - start );
                fErrors.setSelectionStart( start );
                fErrors.setSelectionEnd( end );
                String val;
                int rownum = -1;
                String [] fields = msg.split( ":" );
if( DEBUG ) System.err.printf( "Error message '%s' split into %d\n", msg, fields.length );
                if( fields.length > 1 ) {
                    int idxcol = -1;
                    String row = fields[1].trim();
if( DEBUG ) System.err.printf( "Row: '%s'\n", row );
                    if( row.length() > 0 ) {
                        try { rownum = Integer.parseInt( row ); }
                        catch( NumberFormatException e ) { rownum = -1; }
                    }
                    if( rownum >= 0 ) {
                        for( int i = 0; i < fGrid.getColumnCount(); i++ )
                            if( fGrid.getColumnName( i ).equals( fModel.getRowIndexColumn().getLabel() ) ) {
                                idxcol = i;
                                break;
                            }
if( DEBUG ) System.err.printf( "Idxcol: %d\n", idxcol );
                        if( idxcol >= 0 )
                            for( int i = 0; i < fGrid.getRowCount(); i++ ) {
                                if( fGrid.getValueAt( i, idxcol ) instanceof String )
                                    val = (String) fGrid.getValueAt( i, idxcol );
                                else val = fGrid.getValueAt( i, idxcol ).toString();
if( DEBUG ) System.err.printf( "Value at: %d, %d is '%s', compare to '%s'\n", i, idxcol, val, row );
                                if( val.equals( row ) ) {
                                    fGrid.setRowSelectionInterval( i, i );
                                    fGrid.scrollRectToVisible( fGrid.getCellRect( i, 0, true ) );
                                    return;
                                }
                            }
                    } // endif row >= 0
                    else {
                        String seq = null;
                        if( fields.length > 2 ) {
                            seq = fields[2].trim();
                            if( seq.length() < 1 ) seq = null;
                        }
                        String label = null;
                        if( fields.length > 3 ) {
                            label = fields[3].trim();
                            if( label.length() < 1 ) label = null;
                        }
                        String atom = null;
                        if( fields.length > 4 ) {
                            atom = fields[4].trim();
                            if( atom.length() < 1 ) atom = null;
                        }
                        if( (seq == null) && (label == null) && (atom == null) )
                            return;
//todo
// select by seq:label:atom
                        int seqcol = -1;
                        int labelcol = -1;
                        int atomcol = -1;
                        boolean amatch, lmatch;
                        for( int group = 0; group < fModel.getNumColGroups(); group++ ) {
                            for( int i = 0; i < fGrid.getColumnCount(); i++ ) {
                                if( fGrid.getColumn( fGrid.getColumnName( i ) ).getModelIndex()
                                        == fModel.getAuthSeqColIdx( group ) )
                                    seqcol = i;
                                if( fGrid.getColumn( fGrid.getColumnName( i ) ).getModelIndex()
                                        == fModel.getAuthCompColIdx( group ) )
                                    labelcol = i;
                                if( fGrid.getColumn( fGrid.getColumnName( i ) ).getModelIndex()
                                        == fModel.getAuthAtomColIdx( group ) )
                                    atomcol = i;
                            }
                            lmatch = false;
                            for( int i = 0; i < fGrid.getRowCount(); i++ ) {
// match ((sequence or label) and atom)
                                if( (seqcol >= 0) && (seq != null) ) {
                                    if( fGrid.getValueAt( i, seqcol ) instanceof String )
                                        val = (String) fGrid.getValueAt( i, seqcol );
                                    else val = fGrid.getValueAt( i, seqcol ).toString();
                                    if( val.equals( seq ) ) lmatch = true;
                                }
                                else if( (labelcol >= 0) && (label != null) ) {
                                    if( fGrid.getValueAt( i, labelcol ) instanceof String )
                                        val = (String) fGrid.getValueAt( i, labelcol );
                                    else val = fGrid.getValueAt( i, labelcol ).toString();
                                    if( val.equals( label ) ) lmatch = true;
                                }
                                if( lmatch ) {
                                    if( (atomcol >= 0) && (atom != null) ) {
                                        if( fGrid.getValueAt( i, atomcol ) instanceof String )
                                            val = (String) fGrid.getValueAt( i, atomcol );
                                        else val = fGrid.getValueAt( i, atomcol ).toString();
                                        if( val.equals( atom ) ) {
                                            fGrid.setRowSelectionInterval( i, i );
                                            fGrid.scrollRectToVisible( fGrid.getCellRect( i, 0, true ) );
                                            return;
                                        }
                                    }
                                    else {
                                        fGrid.setRowSelectionInterval( i, i );
                                        fGrid.scrollRectToVisible( fGrid.getCellRect( i, 0, true ) );
                                        return;
                                    }
                                }
                            }
                        } // endfor col. group
                    }
                } // endif fields.length > 1
            }
            catch( javax.swing.text.BadLocationException e ) {
                System.err.println( e );
                e.printStackTrace();
            }
        }
    } //*************************************************************************
}
