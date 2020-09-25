/*
 * LoopTable.java
 *
 * Created on July 19, 2004, 6:33 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/LoopTable.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2004/12/15 20:55:50 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: LoopTable.java,v $
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
public class LoopTable {
    private final static boolean DEBUG = true;
    /** true if uding HSQLDB in-memory db */
    private static final boolean MEMDB = true;
    /** NMR-STAR 2.1 */
    public static final int STAR2 = 2;
    /** NMR-STAR 3.0 */
    public static final int STAR3 = 3;
    /** XEASY */
    public static final int XEASY = 4;
    /** DB column names */
    public static final String [] DB_COLS = {
        "ID",
        "ASSYID",
        "ENTITYID",
        "COMPIDXID",
        "SEQID",
        "COMPID",
        "ATOMID",
        "ATOMTYPE",
        "ISOTOPE",
        "ASEQCODE",
        "ACOMPCODE",
        "AATMCODE",
        "SVALUE",
        "SERROR",
        "SFOMERIT",
        "SAMBICODE",
        "SOCCID",
        "SDERIVID",
        "SFID",
        "ENRYATMID",
        "ENTRYID",
        "LISTID"//,
//        "COMMENT"
    };
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
//        "COMMENT"
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
        "_Atom_chem_shift.Assigned_chem_shift_list_ID"
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
    /** DB column names */
    public static final String [] DB_COLS2 = {
        "ID",
        "ASEQCODE",
        "ACOMPCODE",
        "AATMCODE",
        "ATOMTYPE",
        "SVALUE",
        "SERROR",
        "SAMBICODE"
    };
    /** primary key: integer column */
    public static final int ID_COL = 0;
    /** primary key: integer column */
    public static final int ID_COL2 = 0;
    /** saveframe id column */
    public static final int SFID_COL = 18;
//
// NOTE: change tmp file handling if you change these
//
    /** JDBC driver */
    public static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";
    /** DB URL */
    public static final String JDBC_URL = "jdbc:hsqldb:";
    /** DB URL for in-memory DB */
    public static final String JDBCMEM_URL = "jdbc:hsqldb:mem:starch";
    /** DB username */
    public static final String JDBC_USER = "sa";
    /** DB password */
    public static final String JDBC_PASSWD = "";
    /** db file name */
    private java.io.File fTmpName = null;
//
//
//
    /** JDBC connection */
    private java.sql.Connection fConn = null;
    /** insert statement */
    private java.sql.PreparedStatement fInsertStat = null;
    /** buffer */
    private StringBuffer fBuf = null;
    /** row number */
    private int fRowNum = 1;
//******************************************************************************
    /**
     * Delete HSQLDB files.
     */
    class cleanupThread extends Thread {
        private java.io.File fFile;
        cleanupThread( java.io.File tmpname ) {
            fFile = tmpname;
        }
        public void run() {
            java.io.File tmpdir = fFile.getParentFile();
            final String tmp = fFile.getAbsolutePath().substring( 0,
            fFile.getAbsolutePath().length() - 4 );
            java.io.File [] dbfiles = tmpdir.listFiles( new java.io.FilenameFilter() {
                public boolean accept( java.io.File dir, String name ) {
                    String f = dir.getAbsolutePath() + java.io.File.separator + name;
                    return f.indexOf( tmp ) == 0;
                }
            } );
            for( int i = 0; i < dbfiles.length; i++ )
                dbfiles[i].delete();
        }
    }
//******************************************************************************
    /**
     * Compartator for sorting atoms
     */
    class AtomSorter implements java.util.Comparator {
        public int compare( Object atom1, Object atom2 ) {
            if( atom1 == atom2 ) return 0;
            if( atom1 == null && atom2 != null ) return -1;
            if( atom1 != null && atom2 == null ) return 1;
            String a1 = (String) atom1;
            String a2 = (String) atom2;
            int rc;
            if( a1.charAt( 0 ) != a2.charAt( 0 ) ) {
                switch( a1.charAt( 0 ) ) {
                    case 'H' : return -1;
                    case 'C' :
                        if( a2.charAt( 0 ) == 'H' ) return 1;
                        else return -1;
                    case 'N' :
                        if( (a2.charAt( 0 ) == 'H') 
                        || (a2.charAt( 0 ) == 'C') ) return 1;
                        else return -1;
                    default :
                        if( (a2.charAt( 0 ) == 'H') 
                        || (a2.charAt( 0 ) == 'C')
                        || (a2.charAt( 0 ) == 'C') ) return 1;
                        else {
                            rc = a1.charAt( 0 ) - a2.charAt( 0 );
                            if( rc < 0 ) return -1;
                            else if( rc > 0 ) return 1;
                            return rc;
                        }
                } // endswitch
            }
// both names start with the same character
            if( (a1.length() == 1) && (a2.length() > 1) ) return -1;
            if( (a1.length() > 1) && (a2.length() == 1) ) return 1;
// both names are 2 or more characters long
            if( a1.charAt( 1 ) != a2.charAt( 1 ) ) {
                switch( a1.charAt( 1 ) ) {
                    case 'A' : return -1;
                    case 'B' :
                        if( a2.charAt( 1 ) == 'A' ) return 1;
                        else return -1;
                    case 'G' :
                        if( (a2.charAt( 1 ) == 'A')
                        || (a2.charAt( 1 ) == 'B') ) return 1;
                        else return -1;
                    case 'D' :
                        if( (a2.charAt( 1 ) == 'A')
                        || (a2.charAt( 1 ) == 'B')
                        || (a2.charAt( 1 ) == 'G') ) return 1;
                        else return -1;
                    case 'E' :
                        if( (a2.charAt( 1 ) == 'E')
                        || (a2.charAt( 1 ) == 'Z')
                        || (a2.charAt( 1 ) == 'H') ) return -1;
                        else return 1;
                    case 'Z' :
                        if( (a2.charAt( 1 ) == 'Z')
                        || (a2.charAt( 1 ) == 'H') ) return -1;
                        else return 1;
                    case 'H' :
                        if( a2.charAt( 1 ) == 'H' ) return -1;
                        else return 1;
                } // endswitch
            }
// both names start with 2 same characters
            if( (a1.length() == 2) && (a2.length() > 2) ) return -1;
            if( (a1.length() > 2) && (a2.length() == 2) ) return 1;
// both names are 3 or more characters long
            int thisnum = Integer.parseInt( a1.substring( 2 ) );
            int othernum = Integer.parseInt( a2.substring( 2 ) );
            if( thisnum < othernum ) return -1;
            else if( thisnum > othernum ) return 1;
            else return 0;
        }
    }
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
    /** Connects to database.
     * @throws ClassNotFoundException: no JDBC driver
     * @throws java.io.IOException: error creating temp. filename
     * @throws java.sql.SQLException: problem with JDBC connection
     */
    public void connect() throws java.sql.SQLException, java.io.IOException,
    ClassNotFoundException {
        if( fConn != null )
            if(! fConn.isClosed() ) return;
        Class.forName( JDBC_DRIVER );
        if( MEMDB ) {
            fConn =java.sql.DriverManager.getConnection( JDBCMEM_URL, JDBC_USER, JDBC_PASSWD );
        }
        else {
            fTmpName = java.io.File.createTempFile( "starch", "tmp" );
            fTmpName.deleteOnExit();
            String tmp = fTmpName.getAbsolutePath();
            tmp = tmp.substring( 0, tmp.length() - 4 );
//System.err.println( tmp );
            fBuf.setLength( 0 );
            fBuf.append( JDBC_URL );
            fBuf.append( fTmpName );
            fConn =java.sql.DriverManager.getConnection( fBuf.toString(), JDBC_USER, JDBC_PASSWD );
// add hook to delete db files on exit
            Runtime.getRuntime().addShutdownHook( new LoopTable.cleanupThread( fTmpName ) );
        }
        fConn.setAutoCommit( false );
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
//        try { stat.executeUpdate( "DROP TABLE LOOP" ); }
//        catch( java.sql.SQLException e ) { /* ignore it */ }
        stat.executeUpdate( "DROP TABLE LOOP IF EXISTS" );
        fBuf.setLength( 0 );
        fBuf.append( "CREATE " );
        if( MEMDB ) fBuf.append( "CACHED " );
        fBuf.append( "TABLE LOOP (" );
        for( int i = 0; i < DB_COLS.length; i++ ) {
            fBuf.append( DB_COLS[i] );
            fBuf.append( ' ' );
            fBuf.append( DB_COLTYPES[i] );
            if( i != (DB_COLS.length - 1) ) fBuf.append( ',' );
        }
        fBuf.append( ')' );
//System.err.println( fBuf );
        stat.executeUpdate( fBuf.toString() );
        fConn.commit();
    } //************************************************************************
    /** Insert value into the table.
     * @param row row number
     * @param tag NMR-STAR 3.0 tag
     * @param val value
     * @throws java.sql.SQLException
     */
    public void set( int row, String tag, String val ) throws Exception {
        for( int i = 0; i < LOOP_TAGS3.length; i++ )
            if( LOOP_TAGS3[i].equals( tag ) ) {
                set( row, i, val );
                return;
            }
        throw new IllegalArgumentException( "Invalid tag: " + tag );
    } //************************************************************************
    /** Insert value into the table.
     * @param row row number
     * @param col column number
     * @param val value
     * @throws java.sql.SQLException
     */
    public void set( int row, int col, String val ) throws Exception {
        if( col < 0 || col >= DB_COLS.length )
            throw new IndexOutOfBoundsException( "Invalid column number: " + col );
        if( val.equals( "." ) || val.equals( "?" ) ) return;
        java.sql.Statement stat = fConn.createStatement();
        fBuf.setLength( 0 );
        fBuf.append( "SELECT " );
        fBuf.append( DB_COLS[ID_COL] );
        fBuf.append( " FROM LOOP WHERE " );
        fBuf.append( DB_COLS[ID_COL] );
        fBuf.append( "=" );
        fBuf.append( row );
        java.sql.ResultSet rs = stat.executeQuery( fBuf.toString() );
        if( rs.next() ) {
            fBuf.setLength( 0 );
            fBuf.append( "UPDATE LOOP SET " );
            fBuf.append( DB_COLS[col] );
            fBuf.append( '=' );
            if( DB_COLTYPES[col].indexOf( "CHAR" ) > -1 ) fBuf.append( '\'' );
            fBuf.append( val );
            if( DB_COLTYPES[col].indexOf( "CHAR" ) > -1 ) fBuf.append( '\'' );
            fBuf.append( " WHERE " );
            fBuf.append( DB_COLS[ID_COL] );
            fBuf.append( "=" );
            fBuf.append( row );
        }
        else {
            fBuf.setLength( 0 );
            fBuf.append( "INSERT INTO LOOP (" );
            fBuf.append( DB_COLS[ID_COL] );
            fBuf.append( ',' );
            fBuf.append( DB_COLS[col] );
            fBuf.append( ") VALUES (" );
            fBuf.append( row );
            fBuf.append( ',' );
            if( DB_COLTYPES[col].indexOf( "CHAR" ) > -1 ) fBuf.append( '\'' );
            fBuf.append( val );
            if( DB_COLTYPES[col].indexOf( "CHAR" ) > -1 ) fBuf.append( '\'' );
            fBuf.append( ')' );
        }
        rs.close();
        stat.executeUpdate( fBuf.toString() );
        fConn.commit();
        stat.close();
    } //************************************************************************
    /** Returns value at specified row,col.
     * @param row row number
     * @param col column number
     * @return value or "."
     * @throws java.sql.SQLException
     */
    public String get( int row, int col ) throws Exception {
        if( col < 0 || col >= DB_COLS.length )
            throw new IndexOutOfBoundsException( "Invalid column number: " + col );
        String rc = ".";
        fBuf.setLength( 0 );
        fBuf.append( "SELECT " );
        fBuf.append( DB_COLS[col] );
        fBuf.append( " FROM LOOP WHERE " );
        fBuf.append( DB_COLS[ID_COL] );
        fBuf.append( "=" );
        fBuf.append( row );
        java.sql.Statement stat = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = stat.executeQuery( fBuf.toString() );
        if( rs.next() ) {
            rc = rs.getString( 1 );
            if( rs.wasNull() ) rc = ".";
        }
        else 
            throw new IndexOutOfBoundsException( "Invalid row number: " + row );
        rs.close();
        stat.close();
        return rc;
    } //************************************************************************
    /** Deletes rows where chemical shift value is null.
     * @throws java.sql.SQLException
     */
     public void deleteEmptyRows() throws java.sql.SQLException {
         java.sql.Statement stat = fConn.createStatement();
         stat.executeUpdate( "DELETE FROM LOOP WHERE SVALUE IS NULL" );
         fConn.commit();
         stat.close();
    } //************************************************************************
     /** Sorts rows by atom name.
      * Sort order: H,C,N,other; A,B,G,D,E,Z,H, 'H' before 'HA', "HA' before 'HA1'.
      * @throws java.sql.SQLException
      */
     public void sort() throws java.sql.SQLException {
         int row = -1;
// atoms in residue query
         fBuf.setLength( 0 );
         fBuf.append( "SELECT " );
         fBuf.append( DB_COLS[ID_COL] );
         fBuf.append( ",ATOMID FROM LOOP WHERE SEQID=? AND COMPID=?" );
         java.sql.PreparedStatement pquery = fConn.prepareStatement( fBuf.toString(),
         java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
         java.sql.ResultSet atomrs = null;
// update statement
         fBuf.setLength( 0 );
         fBuf.append( "UPDATE LOOP SET " );
         fBuf.append( DB_COLS[ID_COL] );
         fBuf.append( "=? WHERE SEQID=? AND COMPID=? AND ATOMID=?" );
         java.sql.PreparedStatement pstat = fConn.prepareStatement( fBuf.toString() );
         fBuf.setLength( 0 );
         fBuf.append( "SELECT MAX(" );
         fBuf.append( DB_COLS[ID_COL] );
         fBuf.append( ") FROM LOOP" );
// get max row number
         java.sql.Statement query = fConn.createStatement(
         java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
         java.sql.ResultSet rs = query.executeQuery( fBuf.toString() );
         if( rs.next() ) {
             row = rs.getInt( 1 );
             if( rs.wasNull() ) row = -1;
         }
         if( row < 0 ) throw new java.sql.SQLException( "Cannot fetch max row" );
// start new numbers at max + 10
         row += 10;
// list residues
         int i;
         LoopTable.AtomSorter c = new LoopTable.AtomSorter();
         java.util.List atoms = new java.util.ArrayList();
         rs = query.executeQuery( "SELECT DISTINCT SEQID,COMPID FROM LOOP ORDER BY SEQID");
         while( rs.next() ) {
             atoms.clear();
             pquery.setInt( 1, rs.getInt( 1 ) );
             pquery.setString( 2, rs.getString( 2 ) );
             atomrs = pquery.executeQuery();
             while( atomrs.next() ) {
                 atoms.add( atomrs.getString( 2 ) );
             }
             java.util.Collections.sort( atoms, c );
             pstat.setInt( 2, rs.getInt( 1 ) );
             pstat.setString( 3, rs.getString( 2 ) );
             for( i = 0; i < atoms.size(); i++ ) {
if( DEBUG ) System.err.println( "Set idx to " + row + " for " + rs.getInt( 1 ) +
"," + rs.getString( 2 ) + "," + atoms.get( i ) );
                 pstat.setInt( 1, row );
                 pstat.setString( 4, (String) atoms.get( i ) );
                 pstat.executeUpdate();
                 pstat.getConnection().commit();
                 row++;
             }
         }
// cleanup
         pstat.getConnection().commit();
         pstat.close();
         if( atomrs != null ) atomrs.close();
         pquery.close();
         rs.close();
         query.close();
     } //************************************************************************
    /** Sets Entry ID to "NEED_ACC_NUM" in rows where Entry ID is null.
     * @throws java.sql.SQLException
     */
     public void addEntryId() throws java.sql.SQLException {
         java.sql.Statement stat = fConn.createStatement();
         stat.executeUpdate( "UPDATE LOOP SET ENTRYID='NEED_ACC_NUM' WHERE ENTRYID IS NULL" );
         fConn.commit();
         stat.close();
    } //************************************************************************
    /** Sets Entity ID to <CODE>num</CODE> in all rows.
     * @param num new entity id
     * @throws java.sql.SQLException
     */
     public void addEntityId( int num ) throws java.sql.SQLException {
         fBuf.setLength( 0 );
         fBuf.append( "UPDATE LOOP SET ENTITYID=" );
         fBuf.append( num );
         java.sql.Statement stat = fConn.createStatement();
         stat.executeUpdate( fBuf.toString() );
         fConn.commit();
         stat.close();
    } //************************************************************************
    /** Sets chemical shift error to <CODE>num</CODE> in rows where it is null.
     * @param num chemical shift error
     * @throws java.sql.SQLException
     */
     public void addShiftError( float num ) throws java.sql.SQLException {
         fBuf.setLength( 0 );
         fBuf.append( "UPDATE LOOP SET SERROR=" );
         fBuf.append( num );
         fBuf.append( " WHERE SERROR IS NULL" );
         java.sql.Statement stat = fConn.createStatement();
         stat.executeUpdate( fBuf.toString() );
         fConn.commit();
         stat.close();
    } //************************************************************************
    /** Sets entity assembly id to <CODE>num</CODE> in all rows.
     * @param num chemical shift error
     * @throws java.sql.SQLException
     */
     public void addAssemblyId( int num ) throws java.sql.SQLException {
         fBuf.setLength( 0 );
         fBuf.append( "UPDATE LOOP SET ASSYID=" );
         fBuf.append( num );
//         fBuf.append( " WHERE ASSYID IS NULL" );
         java.sql.Statement stat = fConn.createStatement();
         stat.executeUpdate( fBuf.toString() );
         fConn.commit();
         stat.close();
    } //************************************************************************
    /** Sets saveframe id to <CODE>num</CODE> in all rows.
     * @param num chemical shift error
     * @throws java.sql.SQLException
     */
     public void addSaveframeId( int num ) throws java.sql.SQLException {
if( DEBUG ) System.err.println( "Adding sf id = " + num );
         fBuf.setLength( 0 );
         fBuf.append( "UPDATE LOOP SET SFID=" );
         fBuf.append( num );
         java.sql.Statement stat = fConn.createStatement();
         stat.executeUpdate( fBuf.toString() );
         fConn.commit();
         stat.close();
    } //************************************************************************
    /** Sets shift id to row number in all rows.
     * @throws java.sql.SQLException
     */
     public void reindexRows() throws java.sql.SQLException {
         int row = 1;
         fBuf.setLength( 0 );
         fBuf.append( "UPDATE LOOP SET " );
         fBuf.append( DB_COLS[ID_COL] );
         fBuf.append( "=? WHERE " );
         fBuf.append( DB_COLS[ID_COL] );
         fBuf.append( "=?" );
         java.sql.PreparedStatement pstat = fConn.prepareStatement( fBuf.toString() );
         fBuf.setLength( 0 );
         fBuf.append( "SELECT " );
         fBuf.append( DB_COLS[ID_COL] );
         fBuf.append( " FROM LOOP ORDER BY " );
         fBuf.append( DB_COLS[ID_COL] );
         java.sql.Statement query = fConn.createStatement(
         java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
         java.sql.ResultSet rs = query.executeQuery( fBuf.toString() );
         while( rs.next() ) {
             pstat.setInt( 1, row );
             pstat.setInt( 2, rs.getInt( 1 ) );
             pstat.executeUpdate();
             row++;
         }
         fConn.commit();
         query.close();
    } //************************************************************************
    /** Copies atom and residue names to author columns in all rows.
     * @throws java.sql.SQLException
     */
     public void copyToAuthorData() throws java.sql.SQLException {
         java.sql.Statement stat = fConn.createStatement();
         stat.executeUpdate(
         "UPDATE LOOP SET ASEQCODE=SEQID,ACOMPCODE=COMPID,AATMCODE=ATOMID" );
         fConn.commit();
         stat.close();
     } //***********************************************************************
    /** Copies atom and residue names from author columns in all rows.
     * @throws java.sql.SQLException
     */
     public void copyFromAuthorData() throws java.sql.SQLException {
         java.sql.Statement stat = fConn.createStatement();
         stat.executeUpdate(
         "UPDATE LOOP SET SEQID=ASEQCODE,COMPID=ACOMPCODE,ATOMID=AATMCODE" );
         fConn.commit();
         stat.close();
     } //***********************************************************************
    /** Adds standard atom isotopes.
     * @throws java.sql.SQLException
     */
     public void addIsotopes() throws java.sql.SQLException {
         java.sql.PreparedStatement stat = fConn.prepareStatement(
         "UPDATE LOOP SET ISOTOPE=? WHERE ISOTOPE IS NULL AND ATOMTYPE=?" );
// 1H
         stat.setInt( 1, 1 );
         stat.setString( 2, "H" );
         stat.executeUpdate();
// 13C
         stat.setInt( 1, 13 );
         stat.setString( 2, "C" );
         stat.executeUpdate();
// 15N
         stat.setInt( 1, 15 );
         stat.setString( 2, "N" );
         stat.executeUpdate();
// 17O
         stat.setInt( 1, 17 );
         stat.setString( 2, "O" );
         stat.executeUpdate();
// 19F
         stat.setInt( 1, 19 );
         stat.setString( 2, "F" );
         stat.executeUpdate();
// 31P
         stat.setInt( 1, 31 );
         stat.setString( 2, "P" );
         stat.executeUpdate();
// 33S
         stat.setInt( 1, 33 );
         stat.setString( 2, "S" );
         stat.executeUpdate();
// 43Ca
         stat.setInt( 1, 43 );
         stat.setString( 2, "Ca" );
         stat.executeUpdate();
         fConn.commit();
         stat.close();
     } //***********************************************************************
    /** Sets assigned chem shift list id to <CODE>num</CODE> in all rows.
     * @param num chemical shift list id
     * @throws java.sql.SQLException
     */
     public void addListId( int num ) throws java.sql.SQLException {
if( DEBUG ) System.err.println( "Adding list id = " + num );
         fBuf.setLength( 0 );
         fBuf.append( "UPDATE LOOP SET LISTID=" );
         fBuf.append( num );
         java.sql.Statement stat = fConn.createStatement();
         stat.executeUpdate( fBuf.toString() );
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
