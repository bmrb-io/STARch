package edu.bmrb.starch.gui;

/**
 * Actions.
 * This class contains GUI dialog boxes and wrappers for methods in Nomenmap.
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Dec 11, 2006
 * Time: 3:04:12 PM
 *
 * $Id$
 */

public class Actions {
    private static final boolean DEBUG = false;
    /** table model. */
    private edu.bmrb.starch.TableModel fModel = null;
    /** gui frame for showing dialog boxes. */
    private MainFrame fFrame = null;
/*******************************************************************************/
    /**
     * Constructor.
     * @param model table model
     */
    public Actions( edu.bmrb.starch.TableModel model ) {
        fModel = model;
    } //*************************************************************************
    /**
     * Change parent component.
     * @param frame parent component for dialog boxes
     */
    public void setParentComponent( MainFrame frame ) {
        fFrame = frame;
    } //*************************************************************************
    /**
     * Shows "confirm entry ID dialog".
     * @param accno entry ID
     * @return true if ID is correct
     */
    public boolean confirmEntryId( String accno ) {
        int rc = javax.swing.JOptionPane.showConfirmDialog( fFrame,
                                                            "Entry ID for this table is " + accno,
                                                            "Confirm entry ID",
                                                            javax.swing.JOptionPane.YES_NO_OPTION,
                                                            javax.swing.JOptionPane.QUESTION_MESSAGE );
        if( rc == javax.swing.JOptionPane.YES_OPTION ) return true;
        return false;
    } //*************************************************************************
    /**
     * Shows "select atom nomenclature first" dialog.
     */
    public void showNomenclatureError() {
        javax.swing.JOptionPane.showMessageDialog( fFrame,
                                                   "Select atom nomenclature from Options menu first",
                                                   "Error",
                                                   javax.swing.JOptionPane.ERROR_MESSAGE );
    } //*************************************************************************
    /**
     * Shows "select residue type first" dialog.
     */
    public void showRestypeError() {
        javax.swing.JOptionPane.showMessageDialog( fFrame,
                                                   "Select residue type from Options menu first",
                                                   "Error",
                                                   javax.swing.JOptionPane.ERROR_MESSAGE );
    } //*************************************************************************
    /**
     * Shows "select residue type or load metadata first" dialog.
     */
    public void showRestypeOrMetaError() {
        javax.swing.JOptionPane.showMessageDialog( fFrame,
                                                   "Select residue type from Options menu\n" +
                                                           "or from metadata first",
                                                   "Error",
                                                   javax.swing.JOptionPane.ERROR_MESSAGE );
    } //*************************************************************************
    /**
     * Shows "select entity" dialog.
     * @param group group number
     * @param choices list of entities
     * @param selected default selection
     * @return selected entity or null
     */
    public String selectEntity( int group, String [] choices, String selected ) {
        String str = (String) javax.swing.JOptionPane.showInputDialog( fFrame,
                                                                       "Entity for column group " + (group + 1),
                                                                       "Select entity",
                                                                       javax.swing.JOptionPane.QUESTION_MESSAGE,
                                                                       null,
                                                                       choices,
                                                                       selected );
        if( (str == null) || (str.length() < 1) ) return null;
        return str;
    } //*************************************************************************
    /**
     * Shows "select saveframe" dialog.
     * @param choices list of saveframes
     * @param selected default selection
     * @return selected entity or null
     */
    public String selectSaveframe( String [] choices, String selected ) {
        String str = (String) javax.swing.JOptionPane.showInputDialog( fFrame,
                                                                       "Saveframe name",
                                                                       "Select saveframe",
                                                                       javax.swing.JOptionPane.QUESTION_MESSAGE,
                                                                       null,
                                                                       choices,
                                                                       selected );
        if( (str == null) || (str.length() < 1) ) return null;
        return str;
    } //*************************************************************************
    /**
     * Shows "select ID" dialog for numeric values.
     * @param msg dialog message
     * @param dfl default value
     * @return number, null if user cancels out
     */
    public Integer selectId( String msg, int dfl ) {
        String str = (String) javax.swing.JOptionPane.showInputDialog( fFrame,
                                                                       msg,
                                                                       "Enter a number",
                                                                       javax.swing.JOptionPane.QUESTION_MESSAGE,
                                                                       null,
                                                                       null,
                                                                       dfl );
        if( (str == null) || (str.trim().length() < 1) ) return null;
        try {
            return( new Integer( str.trim() ) );
        }
        catch( NumberFormatException e ) {
//todo
            return null;
        }
    } //*************************************************************************
    /**
     * Shows "select ID" dialog for string values.
     * @param msg dialog message
     * @return null if user cancels out
     */
    public String selectStr( String msg ) {
        String str = (String) javax.swing.JOptionPane.showInputDialog( fFrame,
                                                                       msg,
                                                                       "Enter a value",
                                                                       javax.swing.JOptionPane.QUESTION_MESSAGE );
        if( (str == null) || (str.trim().length() < 1) ) return null;
        return str.trim();
    } //*************************************************************************
    /**
     * Quit.
     */
    public void exitForm() {
        try {
            fModel.close();
            if( fModel.getRowSet().getStatement() != null )
                fModel.close();
            System.exit( 0 );
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
            System.exit( 1 );
        }
    } //*************************************************************************
    /**
     * File - Open
     * @param wd working directory
     * @return false on error
     */
    public boolean readFile( String wd ) {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setDialogTitle( "Open data file" );
        javax.swing.filechooser.FileFilter f = new javax.swing.filechooser.FileFilter() {
            public boolean accept( java.io.File file ) {
                return true;
            }
            public String getDescription() {
                return "NMR-STAR 3.1 loop";
            }
        };
        chooser.setFileFilter( f );
        if( wd != null ) chooser.setCurrentDirectory( new java.io.File( wd ) );
        int rc = chooser.showOpenDialog( fFrame );
        if( rc != javax.swing.JFileChooser.APPROVE_OPTION ) {
            chooser.setSelectedFile( null );
            return false;
        }
        fFrame.setCwd( chooser.getCurrentDirectory().getAbsolutePath() );
        try {
            fModel.getErrorList().clear();
            fModel.clear();
            java.io.Reader in = new java.io.InputStreamReader( new java.io.FileInputStream(
                    chooser.getSelectedFile() ), "ISO-8859-15" );
            edu.bmrb.starch.LoopReader rdr = new edu.bmrb.starch.LoopReader( fModel.getConnection(),
                                                                             fModel.getErrorList() );
            rdr.parse( in );
            in.close();
            if( ! rdr.parsedOk() ) return false;
            fModel.setName( rdr.getTableName() );
if( DEBUG ) System.err.printf( "SELECT * FROM %s ORDER BY ROW\n", fModel.getName() );
            fModel.reloadTable();
            fFrame.initColumns();
            fFrame.setTitle( "STARch: " + fModel.getName() );
            return true;
        }
        catch( java.io.IOException e ) {
            fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                  0, "", "", "", "I/O exception" ) );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
        catch( java.sql.SQLException e ) {
            fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                  0, "", "", "", "JDBC exception" ) );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //*************************************************************************
    /**
     * File - save.
     * @param wd working directory
     * @return false on error
     */
    public boolean saveFile( String wd ) {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setDialogTitle( "Save data to file" );
        javax.swing.filechooser.FileFilter f = new javax.swing.filechooser.FileFilter() {
            public boolean accept( java.io.File file ) {
                return true;
            }
            public String getDescription() {
                return "NMR-STAR 3.1 loop";
            }
        };
        chooser.setFileFilter( f );
        if( wd != null ) chooser.setCurrentDirectory( new java.io.File( wd ) );
        int rc = chooser.showSaveDialog( fFrame );
        if( rc != javax.swing.JFileChooser.APPROVE_OPTION ) {
            chooser.setSelectedFile( null );
            return false;
        }
        fFrame.setCwd( chooser.getCurrentDirectory().getAbsolutePath() );
        try {
            java.io.PrintWriter out = new java.io.PrintWriter(
                    new java.io.FileWriter( chooser.getSelectedFile() ) );
            edu.bmrb.starch.LoopWriter.printLoop( fModel.getConnection(), fModel.getName(),
                                  out, 3 );
            out.close();
            return true;
        }
        catch( java.io.IOException e ) {
            fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                  0, "", "", "", "I/O exception" ) );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
        catch( java.sql.SQLException e ) {
            fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                  0, "", "", "", "JDBC exception" ) );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //*************************************************************************
    /**
     * Insert entry ID.
     * @return false on error (JDBC exception)
     */
    public boolean insertEntryId() {
        try {
            if( fModel.getEntryId() == null ) {
                String id = fModel.getMetaData().getEntryId();
                if( id == null ) fModel.setEntryId( "NEED_ACCNO" );
                else {
                    if( confirmEntryId( id ) ) fModel.setEntryId( id );
                    else fModel.setEntryId( "NEED_ACCNO" );
                }
            }
            fFrame.getNomenmap().insertEntryId( true );
            fModel.updateTable();
            return true;
        }
        catch( java.sql.SQLException e ) {
            if( fModel.getErrorList() != null )
                fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                      0, "", "", "",
                                                                      "JDBC exception, can't insert entry ID" ) );
            System.err.print( "DB exception, state " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //*************************************************************************
    /**
     * Insert entity ID.
     * @return false on error (JDBC exception)
     */
    public boolean insertEntityId() {
        try {
// if entry id is null, we haven't checked metadata yet
            if( fModel.getEntryId() == null ) {
                String id = fModel.getMetaData().getEntryId();
                if( id == null ) fModel.setEntryId( "NEED_ACCNO" );
                else {
                    if( confirmEntryId( id ) ) fModel.setEntryId( id );
                    else fModel.setEntryId( "NEED_ACCNO" );
                }
            }
// if entry ID is NEED_ACCNO, we don't have metadata in the database
// if it isn't, fetch entity IDs from metadata where needed
            if( ! fModel.getEntryId().equals( "NEED_ACCNO" ) ) {
                String [] entities = null;
                String entity;
                edu.bmrb.starch.IntStringPair [] list = fModel.getMetaData().getEntities();
                if( list != null ) {
                    entities = new String[list.length];
                    for( int i = 0; i < list.length; i++ ) entities[i] = list[i].getString();
                }
                for( int i = 0; i < fModel.getNumColGroups(); i++ ) {
                    if( fModel.getEntityId( i ) < 0 ) {
                        if( list != null ) {
                            if( list.length > 1 ) {
                                entity = selectEntity( i, entities, entities[0] );
                                if( entity != null )
                                    for( edu.bmrb.starch.IntStringPair p: list )
                                        if( p.getString().equals( entity ) ) {
                                            fModel.setEntityId( i, p.getInt() );
                                            break;
                                        }
                            } //endif list.length
                            else {
                                fModel.setEntityId( i, list[0].getInt() );
                                break;
                            }
                        }
                    } // endif model.entity_ID < 0
                }
            }
// finally
            for( int i = 0; i < fModel.getNumColGroups(); i++ )
                fFrame.getNomenmap().insertEntityId( i, true );
            fModel.updateTable();
            return true;
        }
        catch( java.sql.SQLException e ) {
            if( fModel.getErrorList() != null )
                fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                      0, "", "", "",
                                                                      "JDBC exception, can't insert entity ID" ) );
            System.err.print( "DB exception, state " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //*************************************************************************
    /**
     * Insert default values.
     * @return false on error (JDBC problem)
     */
    public boolean insertAllDefaults() {
        try {
            if( fModel.getEntryId() == null ) {
                String id = fModel.getMetaData().getEntryId();
                if( id == null ) fModel.setEntryId( "NEED_ACCNO" );
                else {
                    if( confirmEntryId( id ) ) fModel.setEntryId( id );
                    else fModel.setEntryId( "NEED_ACCNO" );
                }
            }
            fFrame.getNomenmap().insertAllDefaults( true );
            fModel.updateTable();
            return true;
        }
        catch( java.sql.SQLException e ) {
            if( fModel.getErrorList() != null )
                fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                      0, "", "", "",
                                                                      "JDBC exception, can't insert default values" ) );
            System.err.print( "DB exception, state " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //*************************************************************************
    /**
     * Delete rows with no values.
     * @return false on error (JDBC problem)
     */
    public boolean deleteEmptyRows() {
        try {
            fFrame.getNomenmap().deleteEmptyRows();
            fModel.updateTable();
            return true;
        }
        catch( java.sql.SQLException e ) {
            if( fModel.getErrorList() != null )
                fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                      0, "", "", "",
                                                                      "JDBC exception, can't delete empty rows" ) );
            System.err.print( "DB exception, state " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //*************************************************************************
    /**
     * Reindex rows.
     * @return false on error (JDBC problem)
     */
    public boolean reindexRows() {
        try {
            fFrame.getNomenmap().reindexRows();
            fModel.updateTable();
            return true;
        }
        catch( java.sql.SQLException e ) {
            if( fModel.getErrorList() != null )
                fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                      0, "", "", "",
                                                                      "JDBC exception, can't reindex rows" ) );
            System.err.print( "DB exception, state " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //*************************************************************************
    /**
     * Check if author data can be converted without manual editing.
     * @return true or false
     */
    public boolean checkAuthorData() {
        try {
            boolean rc = true;
            for( int i = 0; i < fModel.getNumColGroups(); i++ ) {
                if( fModel.getRestypeId( i ) < 0 ) {
                    showRestypeOrMetaError();
                    return false;
                }
                if( ! fFrame.getNomenmap().checkAuthorData( i ) ) rc = false;
            }
            return rc;
        }
        catch( java.sql.SQLException e ) {
            if( fModel.getErrorList() != null )
                fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                      0, "", "", "",
                                                                      "JDBC exception, can't check author data" ) );
            System.err.print( "DB exception, state " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //*************************************************************************
    /**
     * Checks for missing residue sequence numbers, residue and atom names.
     * @return false if there are NULL names
     */
    public boolean checkMissingNames() {
        try {
            return fFrame.getNomenmap().checkNullNames();
        }
        catch( java.sql.SQLException e ) {
            if( fModel.getErrorList() != null )
                fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                      0, "", "", "",
                                                                      "JDBC exception, can't check author data" ) );
            System.err.print( "DB exception, state " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //*************************************************************************
    /**
     * Wrapper for methods in "Precheck" menu.
     * @return false if any of the checks failed.
     */
    public boolean precheck() {
        boolean rc = true;
        if( ! deleteEmptyRows() ) return false;
        if( ! moveAuthorData() ) rc = false;
        if( ! insertEntryId() ) rc = false;
        if( ! insertEntityId() ) rc = false;
        if( ! checkMissingNames() ) return false;
        if( ! checkAuthorData() ) rc = false;
        return rc;
    } //*************************************************************************
    /**
     * Sorts atoms.
     * @return false on DB exception
     */
    public boolean sortAtoms() {
        try {
            fFrame.getNomenmap().sort();
            return true;
        }
        catch( java.sql.SQLException e ) {
            if( fModel.getErrorList() != null )
                fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                      0, "", "", "",
                                                                      "JDBC exception, can't sort atoms" ) );
            System.err.print( "DB exception, state " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //*************************************************************************
    /**
     * Convert residue labels.
     * @return false on errors
     */
    public boolean convertResidues() {
        try {
            boolean rc = true;
            for( int i = 0; i < fModel.getNumColGroups(); i++ ) {
                if( ! fFrame.getNomenmap().convertResidues( i ) )
                    rc = false;
            }
            return rc;
        }
        catch( java.sql.SQLException e ) {
            if( fModel.getErrorList() != null )
                fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                      0, "", "", "",
                                                                      "JDBC exception, can't convert residues" ) );
            System.err.print( "DB exception, state " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //*************************************************************************
    /**
     * Renumber residues in BMRB seq ID and comp index ID columns.
     * @return false on DB exception
     */
    public boolean renumberResidues() {
        try {
            Integer start;
            for( int i = 0; i < fModel.getNumColGroups(); i++ ) {
                start = selectId( "Starting residue number for group " + (i + 1), 1 );
                if( start != null )
                    fFrame.getNomenmap().renumberResidues( start, i );
            }
            return true;
        }
        catch( java.sql.SQLException e ) {
            if( fModel.getErrorList() != null )
                fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                      0, "", "", "",
                                                                      "JDBC exception, can't renumber residues" ) );
            System.err.print( "DB exception, state " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //*************************************************************************
    /**
     * Convert atom names.
     * @return false on errors
     */
    public boolean convertAtoms() {
        try {
            boolean rc = true;
            for( int i = 0; i < fModel.getNumColGroups(); i++ ) {
                if( ! fFrame.getNomenmap().convertAtoms( i ) )
                    rc = false;
            }
            return rc;
        }
        catch( java.sql.SQLException e ) {
            if( fModel.getErrorList() != null )
                fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                      0, "", "", "",
                                                                      "JDBC exception, can't convert atoms" ) );
            System.err.print( "DB exception, state " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //*************************************************************************
    /**
     * Insert atom types.
     * @return false on JDBC exception
     */
    public boolean insertAtomTypes() {
        try {
            for( int i = 0; i < fModel.getNumColGroups(); i++ ) {
                fFrame.getNomenmap().addAtomTypes( i );
            }
            return true;
        }
        catch( java.sql.SQLException e ) {
            if( fModel.getErrorList() != null )
                fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                      0, "", "", "",
                                                                      "JDBC exception, can't insert atom types" ) );
            System.err.print( "DB exception, state " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //*************************************************************************
    /**
     * Insert atom isotopes.
     * @return false on JDBC exception
     */
    public boolean insertAtomIsotopes() {
        try {
            IsotopeSelector sel = IsotopeSelector.createSelector( fModel.getDictionary() );
            int rc;
            javax.swing.JCheckBox box;
            String type, isotope;
            for( int i = 0; i < fModel.getNumColGroups(); i++ ) {
                rc = javax.swing.JOptionPane.showConfirmDialog( fFrame, sel,
                                                                "Insert atom isotopes in group " + (i + 1),
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION );
                if( rc == javax.swing.JOptionPane.OK_OPTION ) {
                    for( int j = 0; j < sel.getComponentCount(); j++ )
                        if( sel.getComponent( j ) instanceof javax.swing.JCheckBox ) {
                            box = (javax.swing.JCheckBox) sel.getComponent( j );
                            if( box.isSelected() ) {
                                isotope = box.getText().split( "\\p{Alpha}" )[0];
                                type = box.getText().substring( box.getText().indexOf( isotope ) + isotope.length() );
                                fFrame.getNomenmap().addAtomIsotopes( i, type, isotope );
                            }
                        }
                }
            }
            return true;
        }
        catch( java.sql.SQLException e ) {
            if( fModel.getErrorList() != null )
                fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                      0, "", "", "",
                                                                      "JDBC exception, can't insert atom isotopes" ) );
            System.err.print( "DB exception, state " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //*************************************************************************
    /**
     * Move data from regular columns to author columns.
     * @return false on JDBC exception
     */
    public boolean moveAuthorData() {
        try {
            for( int i = 0; i < fModel.getNumColGroups(); i++ ) {
                fFrame.getNomenmap().moveAuthorData( i );
            }
            return true;
        }
        catch( java.sql.SQLException e ) {
            if( fModel.getErrorList() != null )
                fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                      0, "", "", "",
                                                                      "JDBC exception, can't move author data" ) );
            System.err.print( "DB exception, state " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //*************************************************************************
    /**
     * Insert entity assembly IDs.
     * @return false on error or JDBC exception
     */
    public boolean insertEntityAssemblyIds() {
if( DEBUG ) System.err.println( "InsertEntityAssemblyIds()" );
        try {
            java.util.ArrayList<edu.bmrb.starch.Pair<Integer, Integer>> ids =
                    fModel.getMetaData().getEntityAssebmlyIds();
            if( ids == null ) {
                if( fModel.getErrorList() != null )
                    fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.ERR,
                                                                          0, "", "", "",
                                                                          "No entity assembly IDs in metadata" ) );
                return false;
            }
            int eid = -1;
            boolean rc = true;
            for( int i = 0; i < fModel.getNumColGroups(); i++ ) {
                eid = fModel.getEntityId( i );
                if( eid < 0 ) {
                    if( fModel.getErrorList() != null )
                        fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.ERR,
                                                                              0, "", "", "",
                                                                              ("No entity ID for group " + i) ) );
                    rc = false;
                    continue;
                }
                for( edu.bmrb.starch.Pair<Integer, Integer> p : ids )
                    if( p.getFirst() == eid ) {
                        fFrame.getNomenmap().insertEntityAssemblyId( i, eid, p.getSecond() );
// only one entity per column
                        break;
                    }
            }
            return rc;
        }
        catch( java.sql.SQLException e ) {
            if( fModel.getErrorList() != null )
                fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                      0, "", "", "",
                                                                      "JDBC exception, can't insert IDs" ) );
            System.err.print( "DB exception, state " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //*************************************************************************
    /**
     * Innsert local IDs.
     * @return false on error/exception
     */
    public boolean insertLocalId() {
        try {
            String sfcat = fModel.getDictionary().getSaveframeCategory( fModel.getName() );
            if( sfcat == null ) { // should never happen
                if( fModel.getErrorList() != null )
                    fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                          0, "", "", "",
                                                                          "No saveframe category" ) );
                return false;
            }
            java.util.ArrayList<edu.bmrb.starch.Pair<Integer, String>> idlist = fModel.getMetaData().getLocalIds( sfcat );
            if( idlist == null ) {
                if( fModel.getErrorList() != null )
                    fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                          0, "", "", "",
                                                                          "No saveframes in category " + sfcat ) );
                return false;
            }
            int id = -1;
            if( idlist.size() > 1 ) {
                String [] names = new String[idlist.size()];
                for( int i = 0; i < names.length; i++ ) names[i] = idlist.get( i ).getSecond();
                sfcat = selectSaveframe( names, names[0] );
                if( sfcat == null ) {
                    if( fModel.getErrorList() != null )
                        fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                              0, "", "", "",
                                                                              "No saveframe selected" ) );
                    return false;
                }
                for( edu.bmrb.starch.Pair<Integer, String> p : idlist )
                    if( p.getSecond().equals( sfcat ) ) {
                        id = p.getFirst();
                        break;
                    }
            }
            else id = idlist.get( 0 ).getFirst();
            if( id < 0 ) {
                if( fModel.getErrorList() != null )
                    fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                          0, "", "", "",
                                                                          "Invalid saveframe ID" ) );
                return false;
            }
            fFrame.getNomenmap().insertLocalIds( id );
            return true;
        }
        catch( java.sql.SQLException e ) {
            if( fModel.getErrorList() != null )
                fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                      0, "", "", "",
                                                                      "JDBC exception, can't insert local IDs" ) );
            System.err.print( "DB exception, state " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //*************************************************************************
    /**
     * Wrapper to run all conversions.
     * @return false on error
     */
    public boolean convert() {
        edu.bmrb.starch.ErrorList errs = fModel.getErrorList();
        for( edu.bmrb.starch.Error err : errs ) {
            if( err.getSeverity().equals( edu.bmrb.starch.Error.Severity.CRIT ) ) {
                javax.swing.JOptionPane.showMessageDialog( fFrame,
                                                           "Table has critical errors, cannot convert",
                                                           "Error",
                                                           javax.swing.JOptionPane.ERROR_MESSAGE );
                return false;
            }
        }
        if( ! convertResidues() ) return false;
        if( ! convertAtoms() ) return false;
        if( ! renumberResidues() ) return false;
        if( ! insertAtomTypes() ) return false;
        if( ! sortAtoms() ) return false;
        if( ! reindexRows() ) return false;
        if( ! insertAtomIsotopes() ) return false;
        if( ! insertEntityAssemblyIds() ) return false;
        if( ! insertLocalId() ) return false;
        fModel.setConverted( true );
        return true;
    } //*************************************************************************
    /**
     * Insert default ambiguity codes.
     * @return false on error (JDBC problem)
     */
    public boolean insertDefaultAmbicodes() {
        try {
            for( int i = 0; i < fModel.getNumColGroups(); i++ )
                fFrame.getNomenmap().insertDefaultAmbiCodes( i );
            return true;
        }
        catch( java.sql.SQLException e ) {
            if( fModel.getErrorList() != null )
                fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                      0, "", "", "",
                                                                      "JDBC exception, can't insert default ambiguity codes" ) );
            System.err.print( "DB exception, state " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //*************************************************************************
    /**
     * Fix ambiguity codes.
     * @return false on error (JDBC problem)
     */
    public boolean replaceAmbicodes() {
        try {
            for( int i = 0; i < fModel.getNumColGroups(); i++ )
                fFrame.getNomenmap().convertAmbiCodes( i );
            return true;
        }
        catch( java.sql.SQLException e ) {
            if( fModel.getErrorList() != null )
                fModel.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                      0, "", "", "",
                                                                      "JDBC exception, can't replace ambiguity codes" ) );
            System.err.print( "DB exception, state " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //*************************************************************************
}
