package edu.bmrb.starch;

/**
 * DB-aware table model.
 *
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Dec 7, 2006
 * Time: 2:43:40 PM
 *
 * $Id$
 */

public class TableModel extends javax.swing.table.AbstractTableModel {
    private static final boolean DEBUG = false;
    /** JDBC connection. */
    private java.sql.Connection fConn = null;
    /** Dictionary. */
    private Dictionary fDict = null;
    /** Metadata. */
    private Metadata fMeta = null;
    /** error list. */
    private ErrorList fErrs = null;
    /** JDBC rowset. */
    private javax.sql.rowset.JdbcRowSet fRowSet = null;
    /** column list. */
    private java.util.ArrayList<Column> fCols = null;
    /** table name. */
    private String fName = null;
    /** Atom nomenclature ID. */
    private int fNomenId = -1;
    /** Row count. */
    private int fRowCount = 0;
    /** entry ID. */
    private String fEntryId = null;
    /** entity ID. */
    private int [] fEntityIds = null;
    /** residue type ID. */
    private int fRestypeId = -1;
    /** row index column. */
    private Column fRowIdxCol = null;
    /** true after conversion's run. */
    private boolean fConverted = false;
/*******************************************************************************/
    /**
     * Constructor.
     * @param conn JDBC connection
     * @param dict dictionary
     * @param meta metadata
     * @param errs error list
     */
    public TableModel( java.sql.Connection conn, Dictionary dict, Metadata meta,
                       ErrorList errs ) {
        fDict = dict;
        fMeta = meta;
        fErrs = errs;
        fConn = conn;
        try { fRowSet = new com.sun.rowset.JdbcRowSetImpl( fConn ); }
        catch( java.sql.SQLException e ) {
            if( fErrs != null )
                fErrs.add( new Error( Error.Severity.CRIT, 0, "", "", "",
                                      "TableModel(): JDBC exception") );
            System.err.print( "TableModel(): JDBC exception, SQLState " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            throw new NullPointerException( "JDBC problem" );
        }
    } //*************************************************************************
    /**
     * Closes database connection.
     * This method is called by the GUI from exitForm().
     * @throws java.sql.SQLException
     */
    public void close() throws java.sql.SQLException {
        if( (fConn != null) && (! fConn.isClosed()) ) {
            if( ! fConn.getAutoCommit() ) fConn.commit();
            fConn.close();
        }
    } //*************************************************************************
    /**
     * Clears table name and column list.
     * @throws java.sql.SQLException
     */
    public void clear() throws java.sql.SQLException {
        fName = null;
        if( fCols != null ) fCols.clear();
        fRowCount = 0;
        fEntityIds = null;
        fRowIdxCol = null;
        fConverted = false;
    } //*************************************************************************
    /**
     * Table data changed.
     * @throws java.sql.SQLException
     */
    public void updateTable() throws java.sql.SQLException {
        if( fRowSet.getCommand() == null ) {
            if( fName == null )
                throw new NullPointerException( "No table name or SQL command" );
        }
        fRowSet.setCommand( "SELECT * FROM " + fName + " ORDER BY ROW" );
        fRowSet.execute();
        fRowSet.last();
        fRowCount = fRowSet.getRow();
        fRowSet.first();
        fireTableDataChanged();
    } //*************************************************************************
    /**
     * Executes rowset's query and updates table data.
     * @throws java.sql.SQLException
     */
    public void reloadTable() throws java.sql.SQLException {
        if( fRowSet.getCommand() == null ) {
            if( fName == null )
                throw new NullPointerException( "No table name or SQL command" );
        }
        fRowSet.setCommand( "SELECT * FROM " + fName + " ORDER BY ROW" );
        fRowSet.execute();
        fRowSet.last();
        fRowCount = fRowSet.getRow();
        fRowSet.first();
        fEntityIds = null;
        fConverted = false;
        initColumns();
        fEntityIds = new int[getNumColGroups()];
        for( int i = 0; i < fEntityIds.length; i++ ) fEntityIds[i] = -1;
        fireTableChanged( new javax.swing.event.TableModelEvent( this,
                                                                 javax.swing.event.TableModelEvent.HEADER_ROW ) );
    } //*************************************************************************
    /**
     * Rebuild column list.
     * @throws java.sql.SQLException
     */
    private void initColumns() throws java.sql.SQLException {
        if( fCols != null ) fCols.clear();
        else fCols = new java.util.ArrayList<Column>();
        fRowIdxCol = null;
        String tag, tagname;
        Column col;
        int j;
        java.sql.ResultSetMetaData md = fRowSet.getMetaData();
        for( int i = 1; i <= md.getColumnCount(); i++ ) {
            tag = md.getColumnName( i );
            if( tag.toLowerCase().equals( "row" ) ) continue;
            if( tag.toLowerCase().equals( "sf_id" ) ) continue;
            col = new Column( tag );
            StringBuilder sql = new StringBuilder( "SELECT ROWIDX,COMPIDXID,SEQID,COMPID," +
                    "ATOMID,ATOMTYPE,ISOTOPE,AMBICODE,VAL,MINVAL,MAXVAL,ERR,GROUPID,AUTHOR " +
                    "FROM STARCH WHERE TAGNAME='_" );
            sql.append( fName );
            sql.append( '.' );
            sql.append( tag );
            sql.append( '\'' );
            java.sql.Connection conn = fRowSet.getStatement().getConnection();
            java.sql.Statement query = conn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                             java.sql.ResultSet.CONCUR_READ_ONLY );
if( DEBUG ) System.err.println( sql );
            java.sql.ResultSet rs = query.executeQuery( sql.toString() );
            if( ! rs.next() ) {
if( DEBUG ) System.err.printf( "Tag _%s.%s not found\n", fName, tag );
                throw new NullPointerException( "Tag not found: " + tag );
            }
            boolean found = false;
            for( j = 1; j < 13; j++ ) {
                rs.getString( j );
                if( (! rs.wasNull()) && rs.getString( j ).trim().equals( "Y" ) ) {
if( DEBUG ) System.err.printf( "Flag is 'Y' in column %d\n", j );
                    found = true;
                    break;
                }
            }
            if( !found ) { // no flags
                tagname = "_" + fName + "." + tag;
                if( fDict.isLocalId( tagname ) ) {
                    col.setType( ColTypes.LCLID );
                    col.setRequired( true );
                    col.setGroupId( -1 );
                }
                else if( fDict.isEntryId( tagname ) ) {
                    col.setType( ColTypes.ENTRYID );
                    col.setRequired( true );
                    col.setGroupId( -1 );
                }
                else if( fDict.isEntityId( tagname ) ) {
                    col.setType( ColTypes.ENTITYID );
                    col.setRequired( true );
                    col.setGroupId( rs.getInt( 13 ) );
                }
                else if( fDict.isEntityAssemblyId( tagname ) ) {
                    col.setType( ColTypes.ASSYID );
                    col.setRequired( true );
                    col.setGroupId( rs.getInt( 13 ) );
                }
                else {
                    col.setType( ColTypes.OTHER );
                    col.setRequired( false );
                }
            }
            else {
                switch( j ) {
                    case 1:
                        col.setType( ColTypes.ROWIDX );
                        col.setRequired( true );
                        fRowIdxCol = col;
                        break;
                    case 2:
                        col.setType( ColTypes.COMPIDX );
                        col.setRequired( false );
                        break;
                    case 3:
                        rs.getString( 14 );
                        if( ( !rs.wasNull() ) && rs.getString( 14 ).trim().equals( "Y" ) )
                            col.setType( ColTypes.ASEQID );
                        else col.setType( ColTypes.SEQID );
                        col.setRequired( true );
                        break;
                    case 4:
                        rs.getString( 14 );
                        if( ( !rs.wasNull() ) && rs.getString( 14 ).trim().equals( "Y" ) )
                            col.setType( ColTypes.ACOMPID );
                        else col.setType( ColTypes.COMPID );
                        col.setRequired( true );
                        break;
                    case 5:
                        rs.getString( 14 );
                        if( ( !rs.wasNull() ) && rs.getString( 14 ).trim().equals( "Y" ) )
                            col.setType( ColTypes.AATOMID );
                        else col.setType( ColTypes.ATOMID );
                        col.setRequired( true );
                        break;
                    case 6:
                        col.setType( ColTypes.ATOMTYPE );
                        col.setRequired( true );
                        break;
                    case 7:
                        col.setType( ColTypes.ISOTOPE );
                        col.setRequired( true );
                        break;
                    case 8:
                        col.setType( ColTypes.AMBICODE );
                        col.setRequired( true );
                        break;
                    case 9:
                        col.setType( ColTypes.VAL );
                        col.setRequired( false );
                        break;
                    case 10:
                        col.setType( ColTypes.MINVAL );
                        col.setRequired( false );
                        break;
                    case 11:
                        col.setType( ColTypes.MAXVAL );
                        col.setRequired( false );
                        break;
                    case 12:
                        col.setType( ColTypes.ERR );
                        col.setRequired( false );
                        break;
                } // endswitch
            } // endif found
            col.setGroupId( rs.getInt( 13 ) );
            switch( md.getColumnType( i ) ) {
                case java.sql.Types.INTEGER :
                case java.sql.Types.SMALLINT :
                case java.sql.Types.TINYINT :
                    col.setValType( Column.ValTypes.INT );
                    break;
                default :
                    col.setValType( Column.ValTypes.STRING );
            }
// group IDs in the dictionary are counted from 1
            if( col.getGroupId() > 0 ) col.setGroupId( col.getGroupId() - 1 );
            fCols.add( col );
if( DEBUG ) System.err.println( col );
        } // endfor columnCount
if( DEBUG ) {
    System.err.println( "Model Columns:" );
    for( int i = 0; i < fCols.size(); i++ ) System.err.printf( "%3d:  %s, %s\n", i, fCols.get( i ).getDbName(), fCols.get( i ).getType() );
    System.err.println( "\nDB Columns:" );
    for( int i = 1; i < md.getColumnCount() + 1; i++ ) System.err.printf( "%3d: %s\n", i, md.getColumnName( i ) );
    System.err.println();
}
    } //*************************************************************************
    /**
     * Change rowset and re-init.
     * @param rowset row set
     * @throws java.sql.SQLException
     */
    public void setRowSet( javax.sql.rowset.JdbcRowSet rowset ) throws java.sql.SQLException {
        fRowSet = rowset;
        reloadTable();
    } //*************************************************************************
    /**
     * Returns rowset.
     * @return rowset
     */
    public javax.sql.rowset.JdbcRowSet getRowSet() {
        return fRowSet;
    } //*************************************************************************
    /**
     * Returns JDBC connection.
     * @return connection
     */
    public java.sql.Connection  getConnection() {
        return fConn;
    } //*************************************************************************
    /**
     * Returns dictionary.
     * @return dictionary
     */
    public Dictionary getDictionary() {
        return fDict;
    } //*************************************************************************
    /**
     * Returns error list.
     * @return error list
     */
    public ErrorList getErrorList() {
        return fErrs;
    } //*************************************************************************
    /**
     * Changes error list
     * @param errs error list
     */
    public void setErrorList( ErrorList errs ) {
        fErrs = errs;
    } //*************************************************************************
    /**
     * Returns list of column headers.
     * @return list
     */
    public java.util.ArrayList<Column> getColumns() {
        return fCols;
    } //*************************************************************************
    /**
     * Returns true if conversion has run.
     * @return true or false
     */
    public boolean isConverted() {
        return fConverted;
    } //*************************************************************************
    /**
     * Set flag to true after converting the table.
     * @param flag true or false
     */
    public void setConverted( boolean flag ) {
        fConverted = flag;
    } //*************************************************************************
    /**
     * Returns row index column.
     * @return row index column or null
     */
    public Column getRowIndexColumn() {
        return fRowIdxCol;
    } //*************************************************************************
    /**
     * Returns column at specified index
     * @param i column index
     * @return column
     */
    public Column getColumn( int i ) {
        return fCols.get( i );
    } //*************************************************************************
    /**
     * Returns metadata flag.
     * @return true if metadata for the entry is in the database
     */
    public boolean hasMetaData() {
        if( (fEntryId == null) || fEntryId.equals( "NEED_ACCNO" ) ) return false;
        return true;
    } //*************************************************************************
    /**
     * Returns metadata object.
     * @return metadata
     */
    public Metadata getMetaData() {
        return fMeta;
    } //*************************************************************************
    /**
     * Update entry ID.
     * @param id entry id
     */
    public void setEntryId( String id ) {
        fEntryId = id;
    } //*************************************************************************
    /**
     * Returns entry ID.
     * @return entry ID
     */
    public String getEntryId() {
        return fEntryId;
    } //*************************************************************************
    /**
     * Returns table name.
     * @return table name: tag category without leading underscore
     */
    public String getName() {
        return fName;
    } //*************************************************************************
    /**
     * Update table name.
     * @param name new name
     */
    public void setName( String name ) {
        fName = name;
    } //*************************************************************************
    /**
     * Returns nomenclature ID.
     * @return nomenclature ID
     */
    public int getNomenclatureId() {
        return fNomenId;
    } //*************************************************************************
    /**
     * Update nomenclature ID.
     * @param id new ID
     */
    public void setNomenclatureId( int id ) {
        fNomenId = id;
    } //*************************************************************************
    /**
     * Returns residue type ID for column group.
     * @param group group number
     * @return ID
     */
    public int getRestypeId( int group ) throws java.sql.SQLException {
        if( hasMetaData() ) {
            IntStringPair p = fMeta.getResidueType( getEntityId( group ) );
            if( p != null ) return p.getInt();
        }
        return fRestypeId;
    } //*************************************************************************
    /**
     * Update residue type ID.
     * @param id new ID
     */
    public void setRestypeId( int id ) {
        fRestypeId = id;
    } //*************************************************************************
    /**
     * Returns entity ID for specified group.
     * @param group group number
     * @return entity ID or -1
     */
    public int getEntityId( int group ) {
        return fEntityIds[group];
    } //*************************************************************************
    /**
     * Update entity ID for specified group.
     * @param group group number
     * @param id new id
     */
    public void setEntityId( int group, int id ) {
        fEntityIds[group] = id;
    } //*************************************************************************
    /**
     * Returns number of column groups.
     * Some table have more than one triple of {sequence,label,atom} columns.
     * Return the number of tuples.
     * @return 1 or more
     */
    public int getNumColGroups() {
        int rc = 0;
        for( Column c: fCols ) {
            if( c.getGroupId() > rc ) rc = c.getGroupId();
        }
        return( rc + 1 );
    } //*************************************************************************
    /**
     * Returns index of author residue sequence column for given group.
     * @param group group number
     * @return column index
     */
    public int getAuthSeqColIdx( int group ) {
        int rc = -1;
        for( Column c: fCols ) {
            rc++;
            if( (c.getGroupId() == group) && (c.getType() == ColTypes.ASEQID) )
                break;
        }
        return rc;
    } //*************************************************************************
    /**
     * Returns index of residue sequence column for given group.
     * @param group group number
     * @return column index
     */
    public int getSeqColIdx( int group ) {
        int rc = -1;
        for( Column c: fCols ) {
            rc++;
            if( (c.getGroupId() == group) && (c.getType() == ColTypes.SEQID) )
                break;
        }
        return rc;
    } //*************************************************************************
    /**
     * Returns index of author residue label column for given group.
     * @param group group number
     * @return column index
     */
    public int getAuthCompColIdx( int group ) {
        int rc = -1;
        for( Column c: fCols ) {
            rc++;
            if( (c.getGroupId() == group) && (c.getType() == ColTypes.ACOMPID) )
                break;
        }
        return rc;
    } //*************************************************************************
    /**
     * Returns index of residue label column for given group.
     * @param group group number
     * @return column index
     */
    public int getCompColIdx( int group ) {
        int rc = -1;
        for( Column c: fCols ) {
            rc++;
            if( (c.getGroupId() == group) && (c.getType() == ColTypes.COMPID) )
                break;
        }
        return rc;
    } //*************************************************************************
    /**
     * Returns index of author atom name column for given group.
     * @param group group number
     * @return column index
     */
    public int getAuthAtomColIdx( int group ) {
        int rc = -1;
        for( Column c: fCols ) {
            rc++;
            if( (c.getGroupId() == group) && (c.getType() == ColTypes.AATOMID) )
                break;
        }
        return rc;
    } //*************************************************************************
    /**
     * Returns index of atom name column for given group.
     * @param group group number
     * @return column index
     */
    public int getAtomColIdx( int group ) {
        int rc = -1;
        for( Column c: fCols ) {
            rc++;
            if( (c.getGroupId() == group) && (c.getType() == ColTypes.ATOMID) )
                break;
        }
        return rc;
    } //*************************************************************************
    /**
     * Returns index of entity ID column for given group.
     * @param group group number
     * @return column index
     */
    public int getEntityColIdx( int group ) {
        int rc = -1;
        for( Column c: fCols ) {
            rc++;
            if( (c.getGroupId() == group) && (c.getType() == ColTypes.COMPID) )
                break;
        }
        return rc;
    } //*************************************************************************
    /**
     * Returns max row number (promary key)
     * @return max row number
     * @throws java.sql.SQLException
     */
    public int getMaxRow() throws java.sql.SQLException {
        int rc = 0;
        java.sql.Statement query = getConnection().createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                                    java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT MAX(ROW) FROM " + fName );
        if( rs.next() ) {
            rc = rs.getInt( 1 );
            if( rs.wasNull() ) rc = 0;
        }
        rs.close();
        query.close();
if( DEBUG ) System.err.printf( "** Max row = %d\n", rc );
        return rc;
    } //*************************************************************************
/*************** table model methods *******************************************/
   /**
    * Returns row count.
    * @return number of rows
    */
    public int getRowCount() {
        return fRowCount;
    } //*************************************************************************
    /**
     * Returns column count.
     * @return number of columns
     */
    public int getColumnCount() {
        if( fCols != null ) return fCols.size();
        else return 0;
    } //*************************************************************************
    /**
     * Returns column label.
     * @param i column number
     * @return column label
     */
    public String getColumnName( int i ) {
        return fCols.get( i ).getLabel();
    } //*************************************************************************
    /**
     * Returns Integer for ID and number columns and String for others.
     * @param i column nuber
     * @return Integer or String
     */
    public Class<?> getColumnClass( int i ) {
        switch( fCols.get( i ).getValType() ) {
            case INT :
                return Integer.class;
            case STRING :
                return String.class;
            default :
                return Object.class;
        }
    } //*************************************************************************
    /**
     * Returns value of specified cell.
     * @param row row number
     * @param col column number
     * @return value: Integer or String, empty String for NULL
     */
    public Object getValueAt( int row, int col ) {
        try {
//if( DEBUG ) {
//    System.err.printf( "* Get value at %d, %d (%s): ", row, col, fCols.get( col ).getDbName() );
//    java.sql.ResultSetMetaData md = fRowSet.getMetaData();
//    System.err.println( md.getColumnName( col + 1 ) );
//}

            fRowSet.absolute( row + 1 );
            if( getColumnClass( col ) == Integer.class ) {
// +2: count from 1 in DB, plus "row" is hidden
                int i = fRowSet.getInt( col + 2 );
                if( fRowSet.wasNull() ) return "";
                else return i;
            }
            else {
                String str = fRowSet.getString( col + 2 );
                if( fRowSet.wasNull() ) str = "";
                return str;
            }
        }
        catch( java.sql.SQLException e ) {
            if( fErrs != null )
                fErrs.add( new Error( Error.Severity.CRIT, 0, "", "", "",
                                      "TableModel.getValueAt(): JDBC exception") );
            System.err.printf( "TableModel.getValueAt( %d, %d ): JDBC exception, SQLState %s\n",
                               row, col, e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            throw new NullPointerException( "JDBC problem" );
        }
    } //*************************************************************************
    /**
     * Updates value in specified cell.
     * @param val new value
     * @param row row number
     * @param col column number
     */
    public void setValueAt( Object val, int row, int col ) {
        try {
            if( fRowSet.getRow() != (row + 1) ) fRowSet.absolute( row + 1 ); // just in case
// +2: count from 1 in DB, plus "row" is hidden
            if( val == null ) fRowSet.updateNull( col + 2 );
            else {
                if( getColumnClass( col ) == Integer.class )
                    fRowSet.updateInt( col + 2, ((Integer) val).intValue() );
                else fRowSet.updateString( col + 2, val.toString() );
            }
        }
        catch( java.sql.SQLException e ) {
            if( fErrs != null )
                fErrs.add( new Error( Error.Severity.CRIT, 0, "", "", "",
                                      "TableModel.setValueAt(): JDBC exception") );
            System.err.printf( "TableModel.setValueAt( %d, %d ): JDBC exception, SQLState %s\n",
                               row, col, e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            throw new NullPointerException( "JDBC problem" );
        }
    } //*************************************************************************
//todo
    public boolean isCellEditable( int row, int col ) {
        if( fCols.get( col ).getType().equals( ColTypes.ATOMID )
                || fCols.get( col ).getType().equals( ColTypes.AMBICODE ) )
            return true;
        return false;
    }
}
