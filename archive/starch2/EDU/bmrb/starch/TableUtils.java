/*
 * TableUtils.java
 *
 * Created on December 23, 2004, 1:23 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source$
 * 
 * AUTHOR:      $Author$
 * DATE:        $Date$
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log$ */

package EDU.bmrb.starch;

/**
 * Editing functions for LoopTable
 * @author  dmaziuk
 */
public class TableUtils {
    private final static boolean DEBUG = false;
//******************************************************************************
    /** atom class */
    static class Atom {
        private int fId = -1;
        private String fName = null;
        public Atom( int id, String name ) {
            fId = id;
            fName = name;
        }
        public int getId() { return fId; }
        public String getName() { return fName; }
    }
    /**
     * Compartator for sorting atoms
     */
    static class AtomSorter implements java.util.Comparator {
        public int compare( Object atom1, Object atom2 ) {
            if( atom1 == atom2 ) return 0;
            if( atom1 == null && atom2 != null ) return -1;
            if( atom1 != null && atom2 == null ) return 1;
            String a1 = ((Atom) atom1).getName();
            String a2 = ((Atom) atom2).getName();
            if( a1 == null && a2 == null ) return 0;
            if( a1 == null && a2 != null ) return -1;
            if( a1 != null && a2 == null ) return 1;
            if( a1.equals( a2 ) ) return 0;
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
    /** Creates a new instance of TableUtils */
    public TableUtils() {
    } //************************************************************************
    /** Adds Entry ID to rows where it's null.
     * @param table LoopTable
     * @param id BMRB accession number
     * @throws java.sql.SQLException
     */
     public static void addEntryId( LoopTable table, String id )
     throws java.sql.SQLException {
         StringBuffer sql = new StringBuffer( "UPDATE LOOP SET ENTRYID='" );
         sql.append( id );
         sql.append( "' WHERE ENTRYID IS NULL" );
         java.sql.Statement stat = table.getConnection().createStatement();
         stat.executeUpdate( sql.toString() );
         stat.getConnection().commit();
         stat.close();
    } //************************************************************************
    /** Sets Entry ID to "NEED_ACC_NUM" in rows where Entry ID is null.
     * @param table LoopTable
     * @throws java.sql.SQLException
     */
     public static void addEntryId( LoopTable table )
     throws java.sql.SQLException {
         addEntryId( table, "NEED_ACC_NUM" );
    } //************************************************************************
    /** Sets Entity ID to <CODE>num</CODE> where it is null.
     * @param table LoopTable
     * @param num new entity id
     * @throws java.sql.SQLException
     */
     public static void addEntityId( LoopTable table, int num )
     throws java.sql.SQLException {
         StringBuffer sql = new StringBuffer( "UPDATE LOOP SET ENTITYID=" );
         sql.append( num );
         sql.append( " WHERE ENTITYID IS NULL" );
         java.sql.Statement stat = table.getConnection().createStatement();
         stat.executeUpdate( sql.toString() );
         stat.getConnection().commit();
         stat.close();
    } //************************************************************************
    /** Sets Entity ID to <CODE>num</CODE> in all rows.
     * @param table LoopTable
     * @param num new entity id
     * @throws java.sql.SQLException
     */
     public static void replaceEntityId( LoopTable table, int num )
     throws java.sql.SQLException {
         StringBuffer sql = new StringBuffer( "UPDATE LOOP SET ENTITYID=" );
         sql.append( num );
         java.sql.Statement stat = table.getConnection().createStatement();
         stat.executeUpdate( sql.toString() );
         stat.getConnection().commit();
         stat.close();
    } //************************************************************************
    /** Sets entity assembly id to <CODE>num</CODE> in all rows.
     * @param table LoopTable
     * @param num chemical shift error
     * @throws java.sql.SQLException
     */
     public static void replaceAssemblyId( LoopTable table, int num )
     throws java.sql.SQLException {
         StringBuffer sql = new StringBuffer( "UPDATE LOOP SET ASSYID=" );
         sql.append( num );
//         sql.append( " WHERE ASSYID IS NULL" );
         java.sql.Statement stat = table.getConnection().createStatement();
         stat.executeUpdate( sql.toString() );
         stat.getConnection().commit();
         stat.close();
    } //************************************************************************
    /** Sets saveframe id to <CODE>num</CODE> where it's null.
     * @param table LoopTable
     * @param num saveframe id
     * @throws java.sql.SQLException
     */
     public static void addSaveframeId( LoopTable table, int num )
     throws java.sql.SQLException {
         StringBuffer sql = new StringBuffer( "UPDATE LOOP SET SFID=" );
         sql.append( num );
         sql.append( " WHERE SFID IS NULL" );
         java.sql.Statement stat = table.getConnection().createStatement();
         stat.executeUpdate( sql.toString() );
         stat.getConnection().commit();
         stat.close();
    } //************************************************************************
    /** Sets saveframe id to <CODE>num</CODE> in all rows.
     * @param table LoopTable
     * @param num saveframe id
     * @throws java.sql.SQLException
     */
     public static void replaceSaveframeId( LoopTable table, int num )
     throws java.sql.SQLException {
         StringBuffer sql = new StringBuffer( "UPDATE LOOP SET SFID=" );
         sql.append( num );
         java.sql.Statement stat = table.getConnection().createStatement();
         stat.executeUpdate( sql.toString() );
         stat.getConnection().commit();
         stat.close();
    } //************************************************************************
    /** Sets assigned chem shift list id to <CODE>num</CODE> where it's null.
     * @param table LoopTable
     * @param num chemical shift list id
     * @throws java.sql.SQLException
     */
     public static void addListId( LoopTable table, int num )
     throws java.sql.SQLException {
         StringBuffer sql = new StringBuffer( "UPDATE LOOP SET LISTID=" );
         sql.append( num );
         sql.append( " WHERE LISTID IS NULL" );
         java.sql.Statement stat = table.getConnection().createStatement();
         stat.executeUpdate( sql.toString() );
         stat.getConnection().commit();
         stat.close();
    } //************************************************************************
    /** Sets assigned chem shift list id to <CODE>num</CODE> in all rows.
     * @param table LoopTable
     * @param num chemical shift list id
     * @throws java.sql.SQLException
     */
     public static void replaceListId( LoopTable table, int num )
     throws java.sql.SQLException {
         StringBuffer sql = new StringBuffer( "UPDATE LOOP SET LISTID=" );
         sql.append( num );
         java.sql.Statement stat = table.getConnection().createStatement();
         stat.executeUpdate( sql.toString() );
         stat.getConnection().commit();
         stat.close();
    } //************************************************************************
    /** Renumbers residues.
     * @param table LoopTable
     * @param starrt starting number
     * @throws java.sql.SQLException
     */
     public static void reindexResidues( LoopTable table, int start )
     throws java.sql.SQLException {
         java.sql.Statement query = table.getConnection().createStatement(
         java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
         java.sql.PreparedStatement stat = table.getConnection().prepareStatement(
         "UPDATE LOOP SET COMPIDXID=?,SEQID=? WHERE ASEQCODE=? AND COMPID=?" );
         java.sql.ResultSet rs = query.executeQuery( "SELECT DISTINCT ASEQCODE," +
         "COMPID FROM LOOP ORDER BY ASEQCODE" );
         while( rs.next() ) {
//if( DEBUG ) System.err.println( "reindex residues:" + rs.getString( 2 ) );
             rs.getString( 2 );
             if( rs.wasNull() ) {
//                 throw new NullPointerException( "No Comp_ID" );
                 rs.close();
                 query.close();
                 stat.close();
                 return;
             }
             stat.setInt( 1, start );
             stat.setInt( 2, start );
             stat.setInt( 3, rs.getInt( 1 ) );
             stat.setString( 4, rs.getString( 2 ) );
             stat.executeUpdate();
             start++;
//if( DEBUG ) System.err.println( stat );
         }
         rs.close();
         query.close();
         stat.getConnection().commit();
         stat.close();
    } //************************************************************************
    /** Deletes rows where chemical shift value is null.
     * @param table LoopTable
     * @throws java.sql.SQLException
     */
     public static void deleteEmptyRows( LoopTable table )
     throws java.sql.SQLException {
         java.sql.Statement stat = table.getConnection().createStatement();
         stat.executeUpdate( "DELETE FROM LOOP WHERE SVALUE IS NULL OR AATMCODE IS NULL" );
         stat.getConnection().commit();
         stat.close();
//         reindexRows( table );
    } //************************************************************************
    /** Sets shift id to row number in all rows.
     * @param table LoopTable
     * @throws java.sql.SQLException
     */
     public static void reindexRows( LoopTable table )
     throws java.sql.SQLException {
         int row = 1;
         StringBuffer sql = new StringBuffer( "UPDATE LOOP SET " );
         sql.append( LoopTable.DB_COLS[LoopTable.ID_COL] );
         sql.append( "=? WHERE " );
         sql.append( LoopTable.DB_COLS[LoopTable.PK_COL] );
         sql.append( "=?" );
         java.sql.PreparedStatement pstat = table.getConnection().prepareStatement( sql.toString() );
         sql.setLength( 0 );
         sql.append( "SELECT " );
         sql.append( LoopTable.DB_COLS[LoopTable.PK_COL] );
         sql.append( " FROM LOOP ORDER BY " );
         sql.append( LoopTable.DB_COLS[LoopTable.PK_COL] );
         java.sql.Statement query = table.getConnection().createStatement(
         java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
         java.sql.ResultSet rs = query.executeQuery( sql.toString() );
         while( rs.next() ) {
             pstat.setInt( 1, row );
             pstat.setInt( 2, rs.getInt( 1 ) );
             pstat.executeUpdate();
             row++;
         }
         pstat.getConnection().commit();
         pstat.close();
         rs.close();
         query.close();
         java.sql.Statement stat = table.getConnection().createStatement();
         stat.executeUpdate( "UPDATE LOOP SET PK=(ID - 1)" );
         stat.getConnection().commit();
         stat.close();
    } //************************************************************************
     /** Returns max atom id
      * @param table LoopTable
      * @throws java.sql.SQLException
      */
     public static int getNextId( LoopTable table ) throws java.sql.SQLException {
         int rc = -1;
         StringBuffer sql = new StringBuffer( "SELECT MAX(" );
         sql.append( LoopTable.DB_COLS[LoopTable.ID_COL] );
         sql.append( ") FROM LOOP" );
// get max row number
         java.sql.Statement query = table.getConnection().createStatement(
         java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
         java.sql.ResultSet rs = query.executeQuery( sql.toString() );
         if( rs.next() ) {
             rc = rs.getInt( 1 );
             if( rs.wasNull() ) rc = -1;
         }
         rc++;
         rs.close();
         query.close();
         return rc;
     } //***********************************************************************
     /** Sorts rows by atom name.
      * Sort order: H,C,N,other; A,B,G,D,E,Z,H, 'H' before 'HA', "HA' before 'HA1'.
      * @param table LoopTable
      * @throws java.sql.SQLException
      */
     public static void sort( LoopTable table ) throws java.sql.SQLException {
         int row = 1;
// atoms in residue query
         StringBuffer sql = new StringBuffer( "SELECT " );
         sql.append( LoopTable.DB_COLS[LoopTable.PK_COL] );
         sql.append( ",ATOMID FROM LOOP WHERE SEQID=? AND COMPID=?" );
         java.sql.PreparedStatement pquery = table.getConnection().prepareStatement( sql.toString(),
         java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
         java.sql.ResultSet atomrs = null;
// update ID statement
         sql.setLength( 0 );
         sql.append( "UPDATE LOOP SET " );
         sql.append( LoopTable.DB_COLS[LoopTable.ID_COL] );
         sql.append( "=? WHERE " );
         sql.append( LoopTable.DB_COLS[LoopTable.PK_COL] );
         sql.append( "=?" );
         java.sql.PreparedStatement pstat = table.getConnection().prepareStatement( sql.toString() );
         java.sql.Statement query = table.getConnection().createStatement(
         java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
// list residues
         int i;
         AtomSorter c = new AtomSorter();
         java.util.List atoms = new java.util.ArrayList();
         java.sql.ResultSet rs = query.executeQuery(
         "SELECT DISTINCT SEQID,COMPID FROM LOOP ORDER BY SEQID");
         while( rs.next() ) {
             atoms.clear();
             pquery.setInt( 1, rs.getInt( 1 ) );
             pquery.setString( 2, rs.getString( 2 ) );
             atomrs = pquery.executeQuery();
             while( atomrs.next() ) {
                 atoms.add( new Atom( atomrs.getInt( 1 ), atomrs.getString( 2 ) ) );
             }
             java.util.Collections.sort( atoms, c );
             for( i = 0; i < atoms.size(); i++ ) {
if( DEBUG ) System.err.println( "Set idx to " + row + " for PK " + rs.getInt( 1 ) +
"," + rs.getString( 2 ) + "," + ((Atom)atoms.get( i )).getName() + "," + ((Atom)atoms.get( i )).getId() );
                 pstat.setInt( 1, row );
                 pstat.setInt( 2, ((Atom) atoms.get( i )).getId() );
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
         java.sql.Statement stat = table.getConnection().createStatement();
         stat.executeUpdate( "UPDATE LOOP SET PK=(ID - 1)" );
         stat.getConnection().commit();
         stat.close();
     } //************************************************************************
     /** Adds atom isotopes.
      * @param table LoopTable
      * @param type atom type
      * @param isotopr atom isotope
      * @throws java.sql.SQLException
      */
     public static void addIsotopes( LoopTable table, String type, String isotope )
     throws java.sql.SQLException {
         StringBuffer sql = new StringBuffer( "UPDATE LOOP SET ISOTOPE='" );
         sql.append( isotope );
         sql.append( "' WHERE ISOTOPE IS NULL AND ATOMTYPE='" );
         sql.append( type );
         sql.append( '\'' );
         java.sql.Statement stat = table.getConnection().createStatement();
         stat.executeUpdate( sql.toString() );
         stat.getConnection().commit();
         stat.close();
     } //***********************************************************************
     /** Copies atom and residue names from author columns in all rows.
      * @param table LoopTable
      * @throws java.sql.SQLException
      */
     public static void copyFromAuthorData( LoopTable table )
     throws java.sql.SQLException {
         java.sql.Statement stat = table.getConnection().createStatement();
         stat.executeUpdate( "UPDATE LOOP SET COMPID=ACOMPCODE,ATOMID=AATMCODE" );
         stat.getConnection().commit();
         stat.close();
     } //***********************************************************************
    /** Sets chemical shift error to <CODE>val</CODE> in rows where it is null.
      * @param table LoopTable
     * @param val chemical shift error -- stored asd string instead of float in
     * case trailing zeros are significant
     * @throws java.sql.SQLException
     */
     public static void addShiftError( LoopTable table, String val )
     throws java.sql.SQLException {
         StringBuffer sql = new StringBuffer( "UPDATE LOOP SET SERROR='" );
         sql.append( val );
         sql.append( "' WHERE SERROR IS NULL" );
         java.sql.Statement stat = table.getConnection().createStatement();
         stat.executeUpdate( sql.toString() );
         stat.getConnection().commit();
         stat.close();
    } //************************************************************************
    /** Sets chemical shift error to <CODE>val</CODE> for specified atom type
     * and isotope where error is null.
     * @param table LoopTable
     * @param type atom type
     * @param isotope atom isotope
     * @param val chemical shift error -- stored asd string instead of float in
     * case trailing zeros are significant
     * @throws java.sql.SQLException
     */
     public static void addShiftError( LoopTable table, String type, int isotope,
     String val ) throws java.sql.SQLException {
         StringBuffer sql = new StringBuffer( "UPDATE LOOP SET SERROR='" );
         sql.append( val );
         sql.append( "' WHERE ATOMTYPE='" );
         sql.append( type );
         sql.append( "' AND ISOTOPE=" );
         sql.append( isotope );
         sql.append( " AND SERROR IS NULL" );
         java.sql.Statement stat = table.getConnection().createStatement();
         stat.executeUpdate( sql.toString() );
         stat.getConnection().commit();
         stat.close();
    } //************************************************************************
     /** Adds atom types.
      * @param table LoopTable
      * @param refdb reference DB
      * @param restype residue type id
      * @throws java.sql.SQLException
      */
     public static void addAtomTypes( LoopTable table, RefDB refdb, int restype )
     throws java.sql.SQLException {
// atom type query
         StringBuffer sql = new StringBuffer( "SELECT a.TYPE FROM ATOMS a,RESIDUES r" +
         " WHERE r.TYPEID=" );
         sql.append( restype );
         sql.append( " AND r.LABEL=? AND a.NAME=? AND a.RESID=r.ID" );
         java.sql.PreparedStatement pquery = refdb.getConnection().prepareStatement(
         sql.toString(), java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
         java.sql.ResultSet typers = null;
// update statement
         java.sql.PreparedStatement pstat = table.getConnection().prepareStatement(
         "UPDATE LOOP SET ATOMTYPE=? WHERE COMPID=? AND ATOMID=? AND ATOMTYPE IS NULL" );
//
         java.sql.Statement query = table.getConnection().createStatement(
         java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
         java.sql.ResultSet rs = query.executeQuery( "SELECT DISTINCT COMPID,ATOMID FROM LOOP" );
         while( rs.next() ) {
             pquery.setString( 1, rs.getString( 1 ) );
             pquery.setString( 2, rs.getString( 2 ) );
             typers = pquery.executeQuery();
             if( typers.next() ) {
                 pstat.setString( 1, typers.getString( 1 ) );
                 pstat.setString( 2, rs.getString( 1 ) );
                 pstat.setString( 3, rs.getString( 2 ) );
                 pstat.executeUpdate();
             }
         }
         pstat.getConnection().commit();
         pstat.close();
         if( typers != null ) typers.close();
         pquery.close();
         rs.close();
         query.close();
    } //************************************************************************
     /** Expands pseudo atom.
      * @param table LoopTable
      * @param refdb reference DB
      * @param row table row
      * @param atoms list of atom ids
      * @throws java.sql.SQLException
      */
     public static void expandPseudoAtom( LoopTable table, RefDB refdb, int row,
     int [] atoms ) throws java.sql.SQLException {
if( DEBUG ) System.err.println( "expandPseudoAtom " + atoms[0] + " in row " + row );
         String atom = refdb.getAtomName( atoms[0] );
         table.set( row, LoopTable.ATOM_COL, atom );
if( DEBUG ) table.print( System.err );
         if( atoms.length == 1 ) return;
         int i;
// insert statement
         StringBuffer sql = new StringBuffer( "INSERT INTO LOOP (" );
         for( i = 0; i < LoopTable.DB_COLS.length - 1; i++ ) {
             sql.append( LoopTable.DB_COLS[i] );
             if( i < LoopTable.DB_COLS.length - 2 ) sql.append( ',' );
         }
         sql.append( ") VALUES (" );
         for( i = 0; i < LoopTable.DB_COLS.length - 1; i++ ) {
             sql.append( '?' );
             if( i < LoopTable.DB_COLS.length - 2 ) sql.append( ',' );
         }
         sql.append( ')' );
if( DEBUG ) System.err.println( sql );
         java.sql.PreparedStatement pstat = table.getConnection().prepareStatement( sql.toString() );
// query
         sql.setLength( 0 );
         sql.append( "SELECT " );
         for( i = 0; i < LoopTable.DB_COLS.length - 1; i++ ) {
             sql.append( LoopTable.DB_COLS[i] );
             if( i < LoopTable.DB_COLS.length - 2 ) sql.append( ',' );
         }
         sql.append( " FROM LOOP WHERE " );
         sql.append( LoopTable.DB_COLS[LoopTable.ID_COL] );
         sql.append( '=' );
         sql.append( row );
         java.sql.Statement query = table.getConnection().createStatement(
         java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
if( DEBUG ) System.err.println( sql );
         java.sql.ResultSet rs = query.executeQuery( sql.toString() );
         rs.next();
         for( i = 1; i < atoms.length; i++ ) {
             for( int j = 0; j < LoopTable.DB_COLS.length - 1; j++ ) {
                 if( j == LoopTable.ATOM_COL ) {
                     pstat.setString( j + 1, refdb.getAtomName( atoms[i] ) );
                 }
                 else {
                     if( LoopTable.DB_COLTYPES[j].equals( "INTEGER" ) ) {
                         rs.getInt( j + 1 );
                         if( rs.wasNull() ) pstat.setNull( j + 1, java.sql.Types.INTEGER );
                         else pstat.setInt( j + 1, rs.getInt( j + 1 ) );
                     }
                     else {
                         rs.getString( j + 1 );
                         if( rs.wasNull() ) pstat.setNull( j + 1, java.sql.Types.VARCHAR );
                         else pstat.setString( j + 1, rs.getString( j + 1 ) );
                     }
                 }
             }
if( DEBUG ) System.err.println( pstat );
             pstat.executeUpdate();
         }
         pstat.getConnection().commit();
         pstat.close();
         rs.close();
         query.close();
    } //************************************************************************
     /** Expands pseudo atom.
      * @param table LoopTable
      * @param refdb reference DB
      * @param row table row
      * @param atom original atom name
      * @param newatoms list of atom ids
      * @boolean replace if false, don't replace existing atom (only replace NULLs)
      * @throws java.sql.SQLException
      * @throws IllegalArgumentException if atom name is invalid
      */
     public static void replaceAtom( LoopTable table, RefDB refdb, int seqno,
     String atom, int [] newatoms, boolean replace ) throws java.sql.SQLException {
if( DEBUG ) System.err.println( "replaceAtom " + atom + " in residue " + seqno );
         StringBuffer sql = new StringBuffer( "SELECT PK,COMPID,ATOMID,ASEQCODE," +
         "ACOMPCODE,AATMCODE,SVALUE,SERROR,SFOMERIT,SAMBICODE,SOCCID,SRESID " +
         "FROM LOOP WHERE SEQID=" );
         sql.append( seqno );
         sql.append( " AND AATMCODE='" );
         sql.append( atom );
         sql.append( '\'' );
         int id = getNextId( table );
         String name = null;
         java.sql.Statement query = table.getConnection().createStatement(
         java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
         java.sql.ResultSet rs = query.executeQuery( sql.toString() );
if( DEBUG ) System.err.println( sql );
         if( ! rs.next() ) // should never happen
             throw new IllegalArgumentException( "No such atom: " + atom );
         java.sql.PreparedStatement pstat = null;
         if( newatoms.length > 1 ) {
             sql.setLength( 0 );
             sql.append( "INSERT INTO LOOP (ID,COMPIDXID,SEQID,COMPID,ATOMID,ASEQCODE,ACOMPCODE," +
             "AATMCODE,SVALUE,SERROR,SFOMERIT,SAMBICODE,SOCCID,SRESID) VALUES(" +
             "?,?,?,?,?,?,?,?,?,?,?,?,?,?)" );
             pstat = table.getConnection().prepareStatement( sql.toString() );
         }
         for( int i = 0; i < newatoms.length; i++ ) {
             name = refdb.getAtomName( newatoms[i] );
             if( name == null ) continue;
// update first atom
             if( i == 0 ) {
                 sql.setLength( 0 );
                 sql.append( "UPDATE LOOP SET ATOMID='" );
                 sql.append( name );
                 sql.append( "' WHERE PK=" );
                 sql.append( rs.getInt( 1 ) );
                 if( ! replace ) sql.append( " AND ATOMID IS NULL" );
                 java.sql.Statement stat = table.getConnection().createStatement();
                 int count = stat.executeUpdate( sql.toString() );
                 stat.getConnection().commit();
                 stat.close();
                 if( count < 1 ) break; // do not replace existing atoms
             }
             else {
// insert the rest
                 pstat.setInt( 1, id );
                 pstat.setInt( 2, seqno );
                 pstat.setInt( 3, seqno );
                 rs.getString( 2 );
                 if( rs.wasNull() ) pstat.setNull( 4, java.sql.Types.VARCHAR );
                 else pstat.setString( 4, rs.getString( 2 ) );
                 pstat.setString( 5, name );
                 rs.getString( 6 );
                 if( rs.wasNull() ) pstat.setNull( 6, java.sql.Types.VARCHAR );
                 else pstat.setString( 6, rs.getString( 4 ) );
                 rs.getString( 5 );
                 if( rs.wasNull() ) pstat.setNull( 7, java.sql.Types.VARCHAR );
                 else pstat.setString( 7, rs.getString( 5 ) );
                 rs.getString( 6 );
                 if( rs.wasNull() ) pstat.setNull( 8, java.sql.Types.VARCHAR );
                 else pstat.setString( 8, rs.getString( 6 ) );
                 rs.getString( 7 );
                 if( rs.wasNull() ) pstat.setNull( 9, java.sql.Types.VARCHAR );
                 else pstat.setString( 9, rs.getString( 7 ) );
                 rs.getString( 8 );
                 if( rs.wasNull() ) pstat.setNull( 10, java.sql.Types.VARCHAR );
                 else pstat.setString( 10, rs.getString( 8 ) );
                 rs.getString( 9 );
                 if( rs.wasNull() ) pstat.setNull( 11, java.sql.Types.VARCHAR );
                 else pstat.setString( 11, rs.getString( 9 ) );
                 rs.getInt( 10 );
                 if( rs.wasNull() ) pstat.setNull( 12, java.sql.Types.INTEGER );
                 else pstat.setInt( 12, rs.getInt( 10 ) );
                 rs.getString( 11 );
                 if( rs.wasNull() ) pstat.setNull( 13, java.sql.Types.VARCHAR );
                 else pstat.setString( 13, rs.getString( 11 ) );
                 rs.getString( 12 );
                 if( rs.wasNull() ) pstat.setNull( 14, java.sql.Types.VARCHAR );
                 else pstat.setString( 14, rs.getString( 12 ) );
                 pstat.executeUpdate();
                 id++;
             }
         }
         if( pstat != null ) {
             pstat.getConnection().commit();
             pstat.close();
         }
         rs.close();
         query.close();
    } //************************************************************************
     /** Copies atom name from _Author_atom_code to Atom_ID.
      * @param table LoopTable
      * @param refdb reference DB
      * @param seqno residue sequence number (Seq_ID)
      * @param atom atom name (_Author_atom_code)
      * @throws java.sql.SQLException
      */
     public static void copyAtom( LoopTable table, RefDB refdb, int seqno,
     String atom ) throws java.sql.SQLException {
if( DEBUG ) System.err.println( "copyAtom " + atom + " in residue " + seqno );
         StringBuffer sql = new StringBuffer( "UPDATE LOOP SET ATOMID='" );
         sql.append( atom );
         sql.append( "' WHERE SEQID=" );
         sql.append( seqno );
         sql.append( " AND AATMCODE='" );
         sql.append( atom );
         sql.append( "' AND ATOMID IS NULL" );
         java.sql.Statement stat = table.getConnection().createStatement();
         stat.executeUpdate( sql.toString() );
         stat.getConnection().commit();
         stat.close();
    } //************************************************************************
     /** Deletes values from specified column.
      * @param table LoopTable
      * @param name column name
      * @throws java.sql.SQLException
      */
     public static void clearColumn( LoopTable table, String name )
     throws java.sql.SQLException {
         java.sql.Statement stat = table.getConnection().createStatement();
         stat.executeUpdate( "UPDATE LOOP SET " + name + "=NULL" );
         stat.getConnection().commit();
         stat.close();
     } //***********************************************************************
     /** Convert residue.
      * Only converts residues where Comp_ID is null.
      * @param table LoopTable
      * @param refdb reference DB
      * @param residue original residue label or code (_Author_comp_code)
      * @param resid new residue (Comp_ID) id
      * @throws java.sql.SQLException
      */
      public static void replaceResidue( LoopTable table, RefDB refdb, String residue,
      int resid ) throws java.sql.SQLException {
          String label = refdb.getResidueLabel( resid );
          if( label == null ) throw new IllegalArgumentException( "Invalid residue: " + resid );
          StringBuffer sql = new StringBuffer( "UPDATE LOOP SET COMPID='" );
          sql.append( label );
          sql.append( "' WHERE ACOMPCODE='" );
          sql.append( residue );
          sql.append( "' AND COMPID IS NULL" );
          java.sql.Statement stat = table.getConnection().createStatement();
          stat.executeUpdate( sql.toString() );
          stat.getConnection().commit();
          stat.close();
     } //***********************************************************************
     /** Convert residues.
      * Only converts residues where Comp_ID is null.
      * @param table LoopTable
      * @param refdb reference DB
      * @param nomenmap nomenclature map
      * @param nomid nomenclature id
      * @param restype residue type id
      * @param islabel true if _Author_comp_code contains 3-letter labels, false
      * if 1-letter codes
      * @param errs error list
      * @throws java.sql.SQLException
      */
      public static void convertResidues( LoopTable table, RefDB refdb, Nomenclmap nomenmap,
      int nomid, int restype, boolean islabel, ErrorList errs )
      throws java.sql.SQLException {
          int [] ids = null;
          java.sql.PreparedStatement stat = table.getConnection().prepareStatement(
          "UPDATE LOOP SET COMPID=? WHERE ACOMPCODE=? AND COMPID IS NULL" );
          java.sql.Statement query = table.getConnection().createStatement(
          java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
          java.sql.ResultSet rs = query.executeQuery( "SELECT DISTINCT ACOMPCODE FROM LOOP" );
          while( rs.next() ) {
              ids = nomenmap.convertResidue( nomid, restype, rs.getString( 1 ), islabel, errs );
              if( (ids == null) || (ids.length < 1) ) {
// copy residue
                  stat.setString( 1, rs.getString( 1 ) );
                  stat.setString( 2, rs.getString( 1 ) );
              }
              else {
//FIXME: this assumes there's only one residue id in ids
                  stat.setString( 1, refdb.getResidueLabel( ids[0] ) );
                  stat.setString( 2, rs.getString( 1 ) );
              }
              stat.executeUpdate();
          }
          stat.getConnection().commit();
          stat.close();
          rs.close();
          query.close();
     } //***********************************************************************
     /** Copies residue label from _Author_comp_code to Comp_ID.
      * @param table LoopTable
      * @param refdb reference DB
      * @param seqno residue sequence number (Seq_ID)
      * @throws java.sql.SQLException
      */
     public static void copyResidue( LoopTable table, RefDB refdb, int seqno )
     throws java.sql.SQLException {
if( DEBUG ) System.err.println( "copyResidue " + seqno );
         StringBuffer sql = new StringBuffer( "UPDATE LOOP SET COMPID=ACOMPCODE " +
         "WHERE SEQID=" );
         sql.append( seqno );
         sql.append( " AND COMPID IS NULL" );
         java.sql.Statement stat = table.getConnection().createStatement();
         stat.executeUpdate( sql.toString() );
         stat.getConnection().commit();
         stat.close();
    } //************************************************************************
     /** Convert atoms.
      * Only converts atoms where Atom_ID is null.
      * @param table LoopTable
      * @param refdb reference DB
      * @param nomenmap nomenclature map
      * @param nomid nomenclature id
      * @param restype residue type id
      * @param errs error list
      * @throws java.sql.SQLException
      */
      public static void convertAtoms( LoopTable table, RefDB refdb, Nomenclmap nomenmap,
      int nomid, int restype, ErrorList errs )
      throws java.sql.SQLException {
          int resid = -1;
          int [] ids = null;
          java.sql.Statement query = table.getConnection().createStatement(
          java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
          java.sql.ResultSet rs = query.executeQuery( "SELECT DISTINCT SEQID,COMPID,AATMCODE FROM LOOP" );
          while( rs.next() ) {
              resid = refdb.getResidueId( nomid, restype, rs.getString( 2 ) );
              if( resid < 0 ) {
                  errs.addError( rs.getString( 1 ), rs.getString( 2 ), rs.getString( 3 ), "Unknown residue" );
                  continue;
              }
              ids = nomenmap.convertAtom( nomid, resid, rs.getString( 1 ), rs.getString( 2 ),
              rs.getString( 3 ), errs );
              if( ids != null ) replaceAtom( table, refdb, rs.getInt( 1 ), rs.getString( 3 ), ids, false );
              else copyAtom( table, refdb, rs.getInt( 1 ), rs.getString( 3 ) );
          }
          rs.close();
          query.close();
     } //***********************************************************************
      /** Returns list of sequence numbers of residues that have specified atom.
       * @param table LoopTable
       * @param refdb reference DB
       * @param label residue label (Comp_ID)
       * @param atom atom name (Author_atom_code)
       * @return list of atoms or null
       */
      public static int [] getSequenceNumbers( LoopTable table, RefDB refdb,
      String label, String atom ) {
          try {
              StringBuffer sql = new StringBuffer( "SELECT DISTINCT SEQID FROM " +
              "LOOP WHERE COMPID='" );
              sql.append( label );
              sql.append( "' AND AATMCODE='" );
              sql.append( atom );
              sql.append( '\'' );
              java.util.List seq = new java.util.ArrayList();
              java.sql.Statement query = table.getConnection().createStatement(
              java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
              java.sql.ResultSet rs = query.executeQuery( sql.toString() );
              while( rs.next() ) {
                  seq.add( new Integer( rs.getInt( 1 ) ) );
              }
              rs.close();
              query.close();
              int [] rc = null;
              if( seq.size() > 0 ) {
                  rc = new int[seq.size()];
                  for( int i = 0; i < rc.length; i++ )
                      rc[i] = ((Integer) seq.get( i )).intValue();
              }
              seq.clear();
              return rc;
          }
          catch( java.sql.SQLException e ) {
              System.err.println( e );
              e.printStackTrace();
              return null;
          }
     } //***********************************************************************
     /** Replace ambiguity codes.
      * @param table LoopTable
      * @param refdb reference DB
      * @param errs error list
      * @throws java.sql.SQLException
      */
      public static void convertAmbicodes( LoopTable table, RefDB refdb, ErrorList errs )
      throws java.sql.SQLException {
// compare sifhts of resid,atom1 and resid,atom2. If identical and ambiguity codes
// are as in table, replace.
          java.sql.PreparedStatement pstat = table.getConnection().prepareStatement(
          "UPDATE LOOP SET SAMBICODE=? WHERE SEQID=? AND ATOMID=?" );
          java.sql.PreparedStatement pquery = table.getConnection().prepareStatement(
          "SELECT t1.SEQID,t1.ATOMID,t2.ATOMID,t1.SAMBICODE,t2.SAMBICODE,t1.SVALUE," +
          "t2.SVALUE,t1.COMPID FROM LOOP t1,LOOP t2 WHERE t1.COMPID=? AND t1.ATOMID=? " +
          "AND t2.ATOMID=? AND t1.SEQID=t2.SEQID", 
          java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
          java.sql.ResultSet rs1 = null;
          java.sql.Statement query = refdb.getConnection().createStatement(
          java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
          java.sql.ResultSet rs = query.executeQuery( "SELECT r.LABEL,a1.NAME," +
          "a2.NAME,c.ORGCODE,c.NEWCODE FROM RESIDUES r,ATOMS a1,ATOMS a2," +
          "AMBIREPLACE c WHERE r.ID=c.RESID AND a1.ID=c.ATOMID1 AND a2.ID=c.ATOMID2" );
          while( rs.next() ) {
if( DEBUG ) {
System.err.print( "rs row = " );
for( int k = 1; k < 6; k++ ) System.err.print( rs.getString( k ) + " " );
System.err.println();
}
              pquery.setString( 1, rs.getString( 1 ) );
              pquery.setString( 2, rs.getString( 2 ) );
              pquery.setString( 3, rs.getString( 3 ) );
if( DEBUG ) System.err.println( pquery );
              rs1 = pquery.executeQuery();
//              if( rs1.next() ) {
              while( rs1.next() ) {
if( DEBUG ) System.err.println( "** Got row: " + rs1.getInt( 1 ) + " " + rs1.getString( 2 ) + " " + rs1.getString( 3 ) );
// compare shifts and ambiguity codes
                  if( rs1.getFloat( 6 ) == rs1.getFloat( 7 ) ) {
                      if( rs1.getInt( 4 ) == rs.getInt( 4 ) ) {
                          pstat.setInt( 1, rs.getInt( 5 ) );
                          pstat.setInt( 2, rs1.getInt( 1 ) );
                          pstat.setString( 3, rs1.getString( 2 ) );
                          if( errs != null ) errs.addWarning( rs1.getString( 1 ),
                          rs1.getString( 8 ), rs1.getString( 2 ), "Replacing ambiguity code " +
                          rs1.getInt( 4 ) + " with " + rs.getInt( 5 ) );
if( DEBUG ) System.err.println( pstat );
                          pstat.executeUpdate();
                      }
                      if( rs1.getInt( 5 ) == rs.getInt( 4 ) ) {
                          pstat.setInt( 1, rs.getInt( 5 ) );
                          pstat.setInt( 2, rs1.getInt( 1 ) );
                          pstat.setString( 3, rs1.getString( 3 ) );
                          if( errs != null ) errs.addWarning( rs1.getString( 1 ),
                          rs1.getString( 8 ), rs1.getString( 3 ), "Replacing ambiguity code " +
                          rs1.getInt( 5 ) + " with " + rs.getInt( 5 ) );
if( DEBUG ) System.err.println( pstat );
                          pstat.executeUpdate();
                      }
                  }
              }
          }
          pstat.getConnection().commit();
          pstat.close();
          if( rs1 != null ) rs1.close();
          pquery.close();
          rs.close();
          query.close();
     } //***********************************************************************
     /** Convert atom names to NMR-STAR 2.1.
      * Shrink methyls into pseudoatoms.
      * @param table LoopTable
      * @param refdb reference DB
      * @throws java.sql.SQLException
      */
      public static void convertAtomsTo21( LoopTable table, RefDB refdb )
      throws java.sql.SQLException {
          java.util.List seqno = new java.util.ArrayList();
          java.sql.PreparedStatement ustat = table.getConnection().prepareStatement(
          "UPDATE LOOP SET ATOMID=? WHERE SEQID=? AND ATOMID=?" );
          java.sql.PreparedStatement dstat = table.getConnection().prepareStatement(
          "DELETE FROM LOOP WHERE SEQID=? AND ATOMID=?" );
          java.sql.PreparedStatement pquery = table.getConnection().prepareStatement(
          "SELECT SEQID,ATOMID FROM LOOP WHERE COMPID=? AND ATOMID=?",
          java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
          java.sql.ResultSet rs1 = null;
          java.sql.Statement query = refdb.getConnection().createStatement(
          java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
          java.sql.ResultSet rs = query.executeQuery( "SELECT RESLABEL,ATOM30," +
          "ATOM21 FROM STAR3TO2 ORDER BY RESLABEL,ATOM21" );
          while( rs.next() ) {
//quick hack: there's always 3 atom30's per atom21. replace name in 1st, delete the other 2
              seqno.clear();
              pquery.setString( 1, rs.getString( 1 ) );
              pquery.setString( 2, rs.getString( 2 ) );
if( DEBUG ) System.err.println( pquery );
              rs1 = pquery.executeQuery();
              while( rs1.next() ) {
                  seqno.add( new Integer( rs1.getInt( 1 ) ) );
                  ustat.setString( 1, rs.getString( 3 ) );
                  ustat.setInt( 2, rs1.getInt( 1 ) );
                  ustat.setString( 3, rs1.getString( 2 ) );
if( DEBUG ) System.err.println( ustat );
                  ustat.executeUpdate();
              }
              for( int i = 0; i < 2; i++ ) {
                  rs.next();
                  for( int j = 0; j < seqno.size(); j++ ) {
                      dstat.setInt( 1, ((Integer) seqno.get( j )).intValue() );
                      dstat.setString( 2, rs.getString( 2 ) );
if( DEBUG ) System.err.println( dstat );
                      dstat.executeUpdate();
                  }
              }
          }
          dstat.getConnection().commit();
          dstat.close();
          ustat.getConnection().commit();
          ustat.close();
          if( rs1 != null ) rs1.close();
          pquery.close();
          rs.close();
          query.close();
     } //***********************************************************************
     /** Fix methylene protons.
      * Replaces methylene protons e.g. HB1,HB2 with HB2,HB3.
      * @param table LoopTable
      * @param refdb reference DB
      * @param errs error list
      * @throws java.sql.SQLException
      */
      public static void fixMethylenes( LoopTable table, RefDB refdb, ErrorList errs )
      throws java.sql.SQLException {
// insert
          java.sql.PreparedStatement pstat = table.getConnection().prepareStatement(
          "UPDATE LOOP SET ATOMID=? WHERE SEQID=? AND AATMCODE=?" );
// original atoms: pairs
          java.sql.PreparedStatement pquery = table.getConnection().prepareStatement(
          "SELECT DISTINCT l1.SEQID,l1.AATMCODE,l2.AATMCODE FROM LOOP l1,LOOP l2 " +
          "WHERE l1.COMPID=? AND l1.ATOMID=? AND l2.ATOMID=? AND l1.SEQID=l2.SEQID",
          java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
// and singles
          java.sql.PreparedStatement pquery2 = table.getConnection().prepareStatement(
          "SELECT DISTINCT SEQID,AATMCODE FROM LOOP WHERE COMPID=? AND ATOMID=?",
          java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
          java.sql.ResultSet rs1 = null;
// main loop
          java.sql.Statement query = refdb.getConnection().createStatement(
          java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
          java.sql.ResultSet rs = query.executeQuery( "SELECT RESLABEL,ATOM1," +
          "ATOM2,NEW1,NEW2 FROM METHYLENES" );
          while( rs.next() ) {
// replace pairs
              pquery.setString( 1, rs.getString( 1 ) );
              pquery.setString( 2, rs.getString( 2 ) );
              pquery.setString( 3, rs.getString( 3 ) );
if( DEBUG ) System.err.println( pquery );
              rs1 = pquery.executeQuery();
              while( rs1.next() ) {
if( DEBUG ) System.err.println( "*** Got " + rs1.getInt( 1 ) );
                  pstat.setString( 1, rs.getString( 4 ) );
                  pstat.setInt( 2, rs1.getInt( 1 ) );
                  pstat.setString( 3, rs1.getString( 2 ) );
                  if( errs != null ) errs.addWarning( rs1.getString( 1 ), rs.getString( 1 ), 
                  rs1.getString( 2 ), "Replacing with " + rs.getString( 4 ) );
if( DEBUG ) System.err.println( pstat );
                  pstat.executeUpdate();
                  pstat.setString( 1, rs.getString( 5 ) );
                  pstat.setInt( 2, rs1.getInt( 1 ) );
                  pstat.setString( 3, rs1.getString( 3 ) );
                  if( errs != null ) errs.addWarning( rs1.getString( 1 ), rs.getString( 1 ), 
                  rs1.getString( 3 ), "Replacing with " + rs.getString( 5 ) );
if( DEBUG ) System.err.println( pstat );
                  pstat.executeUpdate();
              }
// now check for 1's
//WARNING: this assumes 1's are always in ATOM1
              pquery2.setString( 1, rs.getString( 1 ) );
              pquery2.setString( 2, rs.getString( 2 ) );
if( DEBUG ) System.err.println( pquery2 );
              rs1 = pquery2.executeQuery();
              while( rs1.next() ) {
if( DEBUG ) System.err.println( "*** *** Got " + rs1.getInt( 1 ) );
                  pstat.setString( 1, rs.getString( 4 ) );
                  pstat.setInt( 2, rs1.getInt( 1 ) );
                  pstat.setString( 3, rs1.getString( 2 ) );
                  if( errs != null ) errs.addWarning( rs1.getString( 1 ), rs.getString( 1 ), 
                  rs1.getString( 2 ), "Replacing with " + rs.getString( 4 ) );
if( DEBUG ) System.err.println( pstat );
                  pstat.executeUpdate();
              }
              
          }
          pstat.getConnection().commit();
          pstat.close();
          if( rs1 != null ) rs1.close();
          pquery.close();
          pquery2.close();
          rs.close();
          query.close();
     } //***********************************************************************
}
