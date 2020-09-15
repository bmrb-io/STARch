/*
 * LoopTable.java
 *
 * Created on July 19, 2004, 6:33 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/LoopTable.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/10/20 22:20:20 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: LoopTable.java,v $
 * Revision 1.3  2005/10/20 22:20:20  dmaziuk
 * typo, chmod fix
 *
 * Revision 1.2  2005/05/10 18:56:42  dmaziuk
 * added functionality
 *
 * Revision 1.1  2005/04/13 17:37:06  dmaziuk
 * *** empty log message ***
 *
 * Revision 1.5  2004/12/15 20:55:50  dmaziuk
 * new classes
 *
 * Revision 1.4  2004/12/14 00:40:25  dmaziuk
 * more functionality
 *
 * Revision 1.3  2004/12/08 23:57:49  dmaziuk
 * added new classes
 *
 * Revision 1.2  2004/11/29 23:19:16  dmaziuk
 * updated loop converter
 *
 * Revision 1.1  2004/07/22 20:01:51  dmaziuk
 * added command-line 2.1 to 3.0 loop converter
 * */

package EDU.bmrb.starch;

/**
 * Database storage for chemical shifts loop.
 * <P>
 * Includes methods for deleting rows where shift value is null and adding
 * column values (e.g. shift error, entity id).
 * @author  dmaziuk
 */
public class LoopTable extends javax.swing.table.AbstractTableModel {
    private final static boolean DEBUG = false; //true;
    /** true if uding HSQLDB in-memory db */
//    private static final boolean MEMDB = true;
    /** File formats */
    public static final String [] FORMATS = { "NMR-STAR 2.1",
    "NMR-STAR 3.0", "XEASY", "Garret", "PPM" };
    /** NMR-STAR 2.1 */
    public static final int STAR2 = 0;
    /** NMR-STAR 3.0 */
    public static final int STAR3 = 1;
    /** XEASY */
    public static final int XEASY = 2;
    /** Garret */
    public static final int GARRET = 3;
    /** PPM */
    public static final int PPM = 4;
    /** DB column names */
    public static final String [] DB_COLS = {
        "ID",           // 0
        "ASSYID",       // 1
        "ENTITYID",     // 2
        "COMPIDXID",    // 3
        "SEQID",        // 4
        "COMPID",       // 5
        "ATOMID",       // 6
        "ATOMTYPE",     // 7
        "ISOTOPE",      // 8
        "ASEQCODE",     // 9
        "ACOMPCODE",    // 10
        "AATMCODE",     // 11
        "SVALUE",       // 12
        "SERROR",       // 13
        "SFOMERIT",     // 14
        "SAMBICODE",    // 15
        "SOCCID",       // 16
        "SDERIVID",     // 17
        "SFID",         // 18
        "ENRYATMID",    // 19
        "ENTRYID",      // 20
        "LISTID",       // 21
        "COMMENT",      // 22
        "PK"            // 23
    };
    /** primary key: integer column */
    public static final int PK_COL = 23;
    /** ID column */
    public static final int ID_COL = 0;
    /** residue sequence column */
    public static final int SEQ_COL = 4;
    /** residue label column */
    public static final int LABEL_COL = 5;
    /** atom name column */
    public static final int ATOM_COL = 6;
    /** author atom name column */
    public static final int AATOM_COL = 11;
    /** saveframe id column */
    public static final int SFID_COL = 18;
    /** comment column */
    public static final int COMMENT_COL = 22;
    /** DB column types */
    public static final String [] DB_COLTYPES = {
        "INTEGER",
        "INTEGER",
        "INTEGER",
        "INTEGER",
        "INTEGER",
        "VARCHAR(10)",
        "VARCHAR(10)",
        "VARCHAR(3)",
        "VARCHAR(10)",
        "INTEGER",
        "VARCHAR(10)",
        "VARCHAR(10)",
        "VARCHAR(20)", // string instead of float -- keep trailing zeros
        "VARCHAR(20)",
        "VARCHAR(20)",
        "INTEGER",
        "VARCHAR(20)",
        "VARCHAR(127)",
        "INTEGER",
        "VARCHAR(127)",
        "VARCHAR(127)",
        "VARCHAR(127)",
        "VARCHAR(256)",
        "INTEGER IDENTITY PRIMARY KEY"
    };
        /** NMR-STAR 3 tags */
    public static final String [] LOOP_TAGS3 = {
        "_Atom_chem_shift.ID",
        "_Atom_chem_shift.Entity_assembly_ID",
        "_Atom_chem_shift.Entity_ID",
        "_Atom_chem_shift.Comp_index_ID",
        "_Atom_chem_shift.Seq_ID",
        "_Atom_chem_shift.Comp_ID",
        "_Atom_chem_shift.Atom_ID",
        "_Atom_chem_shift.Atom_type",
        "_Atom_chem_shift.Atom_isotope",
        "_Atom_chem_shift.Author_seq_code",
        "_Atom_chem_shift.Author_comp_code",
        "_Atom_chem_shift.Author_atom_code",
        "_Atom_chem_shift.Chem_shift_val",
        "_Atom_chem_shift.Chem_shift_val_err",
        "_Atom_chem_shift.Chem_shift_assign_fig_of_merit",
        "_Atom_chem_shift.Chem_shift_ambiguity_code",
        "_Atom_chem_shift.Chem_shift_occupancy_ID",
        "_Atom_chem_shift.Derivation_ID",
        "_Atom_chem_shift.Sf_ID",
        "_Atom_chem_shift.Entry_atom_ID",
        "_Atom_chem_shift.Entry_ID",
        "_Atom_chem_shift.Assigned_chem_shift_list_ID",
        "_Fake.Comments"
    };
    /** NMR-STAR 2.1 tags */
    public static final String [] LOOP_TAGS2 = {
        "_Atom_shift_assign_ID",
        "_Residue_seq_code",
        "_Residue_label",
        "_Atom_name",
        "_Atom_type",
        "_Chem_shift_value",
        "_Chem_shift_value_error",
        "_Chem_shift_ambiguity_code"
    };
    /** shift value column */
    public static final int SHIFTVAL_COL2 = 5;
    /** shift error column */
    public static final int SHIFTERR_COL2 = 6;
    /** shift ambiguity code column */
    public static final int SHIFTAMC_COL2 = 7;
    /** JDBC driver */
    public static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";
    /** DB URL */
//    public static final String JDBC_URL = "jdbc:hsqldb:";
    /** DB URL for in-memory DB */
    public static final String JDBCMEM_URL = "jdbc:hsqldb:mem:starch";
    /** DB username */
    public static final String JDBC_USER = "sa";
    /** DB password */
    public static final String JDBC_PASSWD = "";
//
//
//
    /** JDBC connection */
    private java.sql.Connection fConn = null;
    /** insert statement */
//    private java.sql.PreparedStatement fInsertStat = null;
    /** buffer */
    private StringBuffer fBuf = null;
    /** filename */
    private String fFilename = null;
//******************************************************************************
    /** Creates a new instance of LoopTable. */
    public LoopTable() {
        fBuf = new StringBuffer();
    } //************************************************************************
    /** Returns length of the string.
     * @param str string
     * @return length
     */
    public static int strlen( String str ) {
        if( str == null ) return 0;
        return str.length();
    } //************************************************************************
    /** Returns filename
     * @return file name or null
     */
    public String getDataFilename() {
        return fFilename;
    } //************************************************************************
    /** Changes filename
     * @param filename data file name
     */
    public void setDataFilename( String filename ) {
        fFilename = filename;
    } //************************************************************************
//*********** Table model methods **********************************************
    /** Returns number of columns.
     * @return column count
     */
    public int getColumnCount() {
        return DB_COLS.length - 1;
    } //************************************************************************
    /** Returns number of rows.
     * @return row count
     */
    public int getRowCount() {
        int rc = 0;
        if( fConn != null )
            try {
                if( fConn.isClosed() ) return rc;
                java.sql.Statement query = fConn.createStatement(
                java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
                java.sql.ResultSet rs = query.executeQuery( "SELECT COUNT(*) FROM LOOP" );
                if( rs.next() ) {
                    rc = rs.getInt( 1 );
                    if( rs.wasNull() ) rc = 0;
                }
                rs.close();
                query.close();
            }
            catch( java.sql.SQLException e ) {
                System.err.println( e );
                e.printStackTrace();
                return 0;
            }
        return rc;
    } //************************************************************************
    /** Returns column name.
     * @param col number
     * @return column name
     */
    public String getColumnName( int col ) {
        String rc = LOOP_TAGS3[col].substring( LOOP_TAGS3[col].indexOf( "." ) + 1 );
        return "<html>" + rc.replaceAll( "_", "<br>" ) + "</html>";
    } //************************************************************************
    /** Returns column's data type.
     * @param col column number
     * @return data type
     */
    public Class getColumnClass( int col ) {
        if( DB_COLTYPES[col].equals( "INTEGER" ) ) return Integer.class;
        else return String.class;
    } //************************************************************************
    /** Returns true if cell is editable.
     * @param row row number
     * @param col column number
     * @return true or false
     */
    public boolean isCellEditable( int row, int col ) {
//TODO
        switch( col ) {
            case 6  : // atom name
            case 14 : // figure of merit
            case 15 : // ambiguity code
            case 16 : // occupancy id
            case 17 : // derivation id
                return true;
            default : return false;
        }
    } //************************************************************************
    /** Returns value at specified row and column.
     * @param row row number
     * @param col column number
     * @return value
     */
    public Object getValueAt( int row, int col ) {
        try {
//if( DEBUG ) System.err.println( "LoopTable: getValueAt " + row + "," + col );
            return getById( row + 1, col );
        }
        catch( Exception e ) {
            System.err.println( e );
            e.printStackTrace();
            return null;
        }
    } //************************************************************************
    /** Updates a cell.
     * @param value value
     * @param row row number
     * @param col column number
     */
    public void setValueAt( Object value, int row, int col ) {
if( DEBUG ) System.err.println( "setValueAt " + row + "," + col + ": " + value );
        if( fConn != null )
            try {
                if( fConn.isClosed() ) return;
// IDs start from 1 in the table, rows start from 0 in the grid
//                if( value == null || ((String) value).trim().length() < 1 )
                if( value == null || value.toString().trim().length() < 1 )
                    setById( row + 1, col, null );
                else setById( row + 1, col, value.toString() );
            }
            catch( Exception e ) {
                System.err.println( e );
                e.printStackTrace();
            }
    } //************************************************************************
//************************* DB *************************************************
    /** Connects to database.
     * @throws ClassNotFoundException: no JDBC driver
     * @throws java.io.IOException: error creating temp. filename
     * @throws java.sql.SQLException: problem with JDBC connection
     */
    public void connect() throws java.sql.SQLException, java.io.IOException,
    ClassNotFoundException {
        if( fConn != null )
            if(! fConn.isClosed() )
                return;
        Class.forName( JDBC_DRIVER );
        fConn =java.sql.DriverManager.getConnection( JDBCMEM_URL, JDBC_USER, JDBC_PASSWD );
    } //************************************************************************
    /** Disconnects from database.
     * @throws java.sql.SQLException: connection commit() or close()
     */
    public void disconnect() throws java.sql.SQLException {
        if( fConn == null ) return;
        if( fConn.isClosed() ) return;
        fConn.commit();
        fConn.close();
    } //************************************************************************
    /** Returns JDBC connection.
     * @return DB connection
     */
    public java.sql.Connection getConnection() {
        return fConn;
    } //************************************************************************
    /** Creates db table.
     * @throws java.sql.SQLException
     */
    public void createTable() throws java.sql.SQLException {
        java.sql.Statement stat = fConn.createStatement();
        stat.executeUpdate( "DROP TABLE LOOP IF EXISTS" );
        fBuf.setLength( 0 );
        fBuf.append( "CREATE TABLE LOOP (" );
        for( int i = 0; i < DB_COLS.length; i++ ) {
            fBuf.append( DB_COLS[i] );
            fBuf.append( ' ' );
            fBuf.append( DB_COLTYPES[i] );
            if( i != (DB_COLS.length - 1) ) fBuf.append( ',' );
        }
        fBuf.append( ')' );
if( DEBUG ) System.err.println( fBuf );
        stat.executeUpdate( fBuf.toString() );
        fConn.commit();
    } //************************************************************************
    /** Insert value into the table.
     * @param row DB row number
     * @param col DB column number
     * @param val value
     * @throws java.sql.SQLException
     */
    public void set( int row, int col, String val ) throws java.sql.SQLException {
if( DEBUG ) System.err.println( "set " + row + "," + col );
        fBuf.setLength( 0 );
        fBuf.append( "UPDATE LOOP SET " );
        fBuf.append( DB_COLS[col] );
        fBuf.append( '=' );
        if( val == null || val.equals( "?" ) ) fBuf.append( "NULL" );
        else {
            if( DB_COLTYPES[col].indexOf( "CHAR" ) > -1 ) fBuf.append( '\'' );
            fBuf.append( val.toUpperCase() );
            if( DB_COLTYPES[col].indexOf( "CHAR" ) > -1 ) fBuf.append( '\'' );
        }
        fBuf.append( " WHERE " );
        fBuf.append( DB_COLS[ID_COL] );
        fBuf.append( "=" );
        fBuf.append( row );
        if( DEBUG ) System.err.println( fBuf );
        java.sql.Statement stat = fConn.createStatement();
        stat.executeUpdate( fBuf.toString() );
        fConn.commit();
        stat.close();
    } //************************************************************************
    /** Insert value into the table.
     * @param id row ID
     * @param col column number
     * @param val value
     * @throws java.sql.SQLException
     */
    public void setById( int id, int col, String val ) throws java.sql.SQLException {
        if( col < 0 || col >= DB_COLS.length )
            throw new IndexOutOfBoundsException( "Invalid column number: " + col );
if( DEBUG ) System.err.println( "setById " + id + "," + col + ": " + val );
// atom name editing is handled by custom JTable column editor
        if( col != ATOM_COL ) {
            fBuf.setLength( 0 );
            fBuf.append( "UPDATE LOOP SET " );
            fBuf.append( DB_COLS[col] );
            fBuf.append( '=' );
            if( val == null || val.equals( "?" ) ) fBuf.append( "NULL" );
            else {
                if( DB_COLTYPES[col].indexOf( "CHAR" ) > -1 ) fBuf.append( '\'' );
                fBuf.append( val.toUpperCase() );
                if( DB_COLTYPES[col].indexOf( "CHAR" ) > -1 ) fBuf.append( '\'' );
            }
            fBuf.append( " WHERE " );
            fBuf.append( DB_COLS[ID_COL] );
            fBuf.append( "=" );
            fBuf.append( id );
if( DEBUG ) System.err.println( fBuf );
            java.sql.Statement stat = fConn.createStatement();
            stat.executeUpdate( fBuf.toString() );
            stat.getConnection().commit();
            stat.close();
        }
    } //************************************************************************
    /** Returns value at specified ID,col.
     * @param id ID
     * @param col column number
     * @return value or "."
     * @throws java.sql.SQLException
     */
    public String getById( int id, int col ) throws Exception {
        String rc = ""; //".";
        if( col < 0 || col >= DB_COLS.length )
            throw new IndexOutOfBoundsException( "Invalid column number: " + col );
        fBuf.setLength( 0 );
        fBuf.append( "SELECT " );
        fBuf.append( DB_COLS[col] );
        fBuf.append( " FROM LOOP WHERE " );
        fBuf.append( DB_COLS[ID_COL] );
        fBuf.append( "=" );
        fBuf.append( id );
        java.sql.Statement query = fConn.createStatement(
        java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( fBuf.toString() );
        if( rs.next() ) {
            rc = rs.getString( 1 );
            if( rs.wasNull() ) rc = ""; //".";
        }
        else 
            throw new IndexOutOfBoundsException( "Invalid row ID: " + id );
        rs.close();
        query.close();
        return rc;
    } //************************************************************************
    /** Deletes row with specified ID.
     * @param id ID
     * @throws java.sql.SQLException
     */
    public void deleteRowById( int id ) throws java.sql.SQLException {
        fBuf.setLength( 0 );
        fBuf.append( "DELETE FROM LOOP WHERE " );
        fBuf.append( DB_COLS[ID_COL] );
        fBuf.append( "=" );
        fBuf.append( id );
        java.sql.Statement stat = fConn.createStatement();
        stat.executeUpdate( fBuf.toString() );
        fConn.commit();
        stat.close();
        TableUtils.reindexRows( this );
    } //************************************************************************
    /** Deletes all rows. */
    public void clear() throws java.sql.SQLException {
        java.sql.Statement stat = fConn.createStatement();
        stat.executeUpdate( "DELETE FROM LOOP" );
        fConn.commit();
        stat.close();
    } //************************************************************************
     /** prints table */
     public void print( java.io.PrintStream out ) throws java.sql.SQLException {
         int i, j;
         for( i = 0; i < DB_COLS.length; i++ ) {
             out.print( DB_COLS[i] );
             if( i < DB_COLS.length - 1 ) out.print( ',' );
         }
         out.println();
         fBuf.setLength( 0 );
         out.println();
         fBuf.append( "SELECT " );
         for( i = 0; i < DB_COLS.length; i++ ) {
             fBuf.append( DB_COLS[i] );
             if( i < DB_COLS.length - 1 ) fBuf.append( ',' );
         }
         fBuf.append( " FROM LOOP ORDER BY " );
         fBuf.append( LoopTable.DB_COLS[LoopTable.ID_COL] );
         java.sql.Statement query = getConnection().createStatement(
         java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
         java.sql.ResultSet rs = query.executeQuery( fBuf.toString() );
         while( rs.next() ) {
             for( j = 1; j <= DB_COLS.length; j++ ) {
                 out.print( rs.getString( j ) );
                 if( j < DB_COLS.length ) out.print( ',' );
             }
             out.println();
         }
         rs.close();
         query.close();
    } //************************************************************************
    /** Main method.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LoopTable t = new LoopTable();
        try {
            t.connect();
            t.createTable();
            t.disconnect();
        }
        catch( Exception e ) {
            System.err.println( e.getMessage() );
            e.printStackTrace();
        }
    } //************************************************************************
}
