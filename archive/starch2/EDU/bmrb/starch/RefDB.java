/*
 * RefDB.java
 *
 * Created on December 28, 2004, 2:59 PM
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
 * Reference database/
 * @author  dmaziuk
 */
public class RefDB {
    private static final boolean DEBUG = false;
    /** default config file */
    public static final String DEF_PROPFILE = "validator.properties";
    /* property keys */
    /** JDBC driver */
    public static final String JDBC_DRIVER = "DB.Driver";
    /** DB URL */
    public static final String JDBC_URL = "DB.Url";
    /** DB username */
    public static final String JDBC_USER = "DB.User";
    /** DB password */
    public static final String JDBC_PASSWD = "DB.Password";
    /** properties */
    private java.util.Properties fProps = null;
    /** JDBC connection */
    private java.sql.Connection fConn = null;
//******************************************************************************
    /** Creates a new instance of RefDB.
     * @throws java.io.IOException -- problem reading config file
     */
    public RefDB() throws java.io.IOException {
        readProps( System.getProperty( "user.home" ) + java.io.File.separator
        + DEF_PROPFILE );
    } //************************************************************************
    /** Creates new instance of RefDB with specified properties.
     * @param props properties
     */
    public RefDB( java.util.Properties props ) {
        fProps = props;
    } //************************************************************************
    /** Creates new instance of RefDB and reads properties from specified file.
     * @param propfile config file name
     * @throws java.io.IOException -- problem reading config file
     */
    public RefDB( String propfile ) throws java.io.IOException {
        readProps( propfile );
    } //************************************************************************
    /** Loads properties from file.
     * @param file filename
     * @throws java.io.IOException -- problem reading config file
     */
    protected void readProps( String propfile ) throws java.io.IOException {
        java.io.InputStream in = new java.io.FileInputStream( propfile );
        fProps = new java.util.Properties();
        fProps.load( in );
        in.close();
    } //************************************************************************
    /** Connects to database.
     * @throws ClassNotFoundException: no JDBC driver
     * @throws java.sql.SQLException: problem with JDBC connection
     */
    public void connect() throws java.sql.SQLException, ClassNotFoundException {
        if( fConn != null )
            if(! fConn.isClosed() ) return;
        Class.forName( fProps.getProperty( JDBC_DRIVER ) );
        String url = fProps.getProperty( JDBC_URL );
        if( url == null || url.length() < 1 )
            throw new NullPointerException( "Error in properties file: no DB URL" );
        String usr = fProps.getProperty( JDBC_USER );
        if( usr == null || usr.length() < 1 )
            throw new NullPointerException( "Error in properties file: no DB URL" );
        fConn =java.sql.DriverManager.getConnection( url, usr, fProps.getProperty( JDBC_PASSWD ) );
    } //************************************************************************
    /** Disconnects from database.
     * @throws java.sql.SQLException: connection commit() or close()
     */
    public void close() throws java.sql.SQLException {
        disconnect();
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
    /** Returns residue type id.
     * @param restype residue type
     * @return id or -1
     */
    public int getResidueTypeId( String restype ) throws java.sql.SQLException {
        int rc = -1;
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT ID FROM RESTYPES WHERE NAME='"
        + restype + "'" );
        if( rs.next() ) {
            rc = rs.getInt( 1 );
            if( rs.wasNull() ) rc = -1;
        }
        rs.close();
        query.close();
        return rc;
    } //************************************************************************
    /** Returns list of residue types.
     * @return array of strings or null
     * @throws java.sql.SQLException
     */
    public String [] getResidueTypes() throws java.sql.SQLException {
        java.util.List arr = new java.util.ArrayList();
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT NAME,ID FROM RESTYPES ORDER BY ID" );
        while( rs.next() ) {
            arr.add( rs.getString( 1 ) );
        }
        if( arr.size() < 1 ) return null;
        String [] rc = new String[arr.size()];
        for( int i = 0; i < arr.size(); i++ )
            rc[i] = (String) arr.get( i );
        arr.clear();
        return rc;
    } //************************************************************************
    /** Returns nomenclature id.
     * @param name nomenclature name
     * @return id or -1
     */
    public int getNomenclatureId( String name ) throws java.sql.SQLException {
        int rc = 0;
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT ID FROM NOMENCLATURES WHERE NAME='"
        + name + "'" );
        if( rs.next() ) {
            rc = rs.getInt( 1 );
            if( rs.wasNull() ) rc = 0;
        }
        rs.close();
        query.close();
if( DEBUG ) System.err.println( "ID for " + name + " nomenclature = " + rc );
        return rc;
    } //************************************************************************
    /** Returns list of nomenclatures.
     * @return array of strings or null
     * @throws java.sql.SQLException
     */
    public String [] getNomenclatures() throws java.sql.SQLException {
        java.util.List arr = new java.util.ArrayList();
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT NAME,ID FROM NOMENCLATURES ORDER BY ID" );
        while( rs.next() ) {
            arr.add( rs.getString( 1 ) );
        }
        rs.close();
        query.close();
        if( arr.size() < 1 ) return null;
        String [] rc = new String[arr.size()];
        for( int i = 0; i < arr.size(); i++ )
            rc[i] = (String) arr.get( i );
        arr.clear();
        return rc;
    } //************************************************************************
    /** Returns residue ID.
     * @param nomid nomenclature id
     * @param restype residue type id
     * @param label residue label
     * @return residue id or -1
     * @throws java.sql.SQLException
     */
    public int getResidueId( int nomid, int restype, String label )
    throws java.sql.SQLException {
        StringBuffer sql = new StringBuffer( "SELECT ID FROM RESIDUES WHERE TYPEID=" );
        sql.append( restype );
        sql.append( " AND NOMID=" );
        sql.append( nomid );
        sql.append( " AND LABEL='" );
        sql.append( label );
        sql.append( '\'' );
        int rc = -1;
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        if( rs.next() ) {
            rc = rs.getInt( 1 );
        }
        rs.close();
        query.close();
        return rc;
    } //************************************************************************
    /** Returns residue label.
     * @param resid residue id
     * @return residue label or null
     * @throws java.sql.SQLException
     */
    public String getResidueLabel( int resid )
    throws java.sql.SQLException {
        StringBuffer sql = new StringBuffer( "SELECT LABEL FROM RESIDUES WHERE ID=" );
        sql.append( resid );
        String rc = null;
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        if( rs.next() ) {
            rc = rs.getString( 1 );
            if( rs.wasNull() ) rc = null;
        }
        rs.close();
        query.close();
        return rc;
    } //************************************************************************
    /** Returns list of atom types.
     * @return array of strings or null
     * @throws java.sql.SQLException
     */
    public String [] getAtomTypes() throws java.sql.SQLException {
        java.util.List arr = new java.util.ArrayList();
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT DISTINCT TYPE FROM ISOTOPES ORDER BY TYPE" );
        while( rs.next() ) {
            arr.add( rs.getString( 1 ) );
        }
        if( arr.size() < 1 ) return null;
        String [] rc = new String[arr.size()];
        for( int i = 0; i < arr.size(); i++ )
            rc[i] = (String) arr.get( i );
        arr.clear();
        return rc;
    } //************************************************************************
    /** Returns list of atom isotopes.
     * @param type atom type
     * @return array of ints or null
     * @throws java.sql.SQLException
     */
    public int [] getAtomIsotopes( String type ) throws java.sql.SQLException {
        java.util.List arr = new java.util.ArrayList();
        StringBuffer sql = new StringBuffer( "SELECT ISOTOPE FROM ISOTOPES WHERE TYPE ='" );
        sql.append( type );
        sql.append( "' ORDER BY ISOTOPE" );
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        while( rs.next() ) {
            arr.add( new Integer( rs.getInt( 1 ) ) );
        }
        if( arr.size() < 1 ) return null;
        int [] rc = new int[arr.size()];
        for( int i = 0; i < arr.size(); i++ )
            rc[i] = ((Integer) arr.get( i )).intValue();
        arr.clear();
        return rc;
    } //************************************************************************
    /** Returns list of atom isotopes.
     * @return array of strings: "1H", "2H", etc., or null
     * @throws java.sql.SQLException
     */
    public String [] getAtomIsotopes() throws java.sql.SQLException {
        java.util.List arr = new java.util.ArrayList();
        StringBuffer buf = new StringBuffer();
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT ISOTOPE,TYPE FROM ISOTOPES ORDER BY TYPE,ISOTOPE" );
        while( rs.next() ) {
            buf.setLength( 0 );
            buf.append( rs.getString( 1 ) );
            buf.append( rs.getString( 2 ) );
            arr.add( buf.toString() );
        }
        if( arr.size() < 1 ) return null;
        String [] rc = new String[arr.size()];
        for( int i = 0; i < arr.size(); i++ )
            rc[i] = (String) arr.get( i );
        arr.clear();
        return rc;
    } //************************************************************************
    /** Returns atom type.
     * @param restype residue type
     * @param residue residue label
     * @param atom atom name
     * @return atom type or null
     * @throws java.sql.SQLException
     */
    public String getAtomType( String restype, String residue, String atom )
    throws java.sql.SQLException {
        StringBuffer sql = new StringBuffer( "SELECT a.TYPE FROM ATOMS a,RESIDUES r," +
        "RESTYPES t WHERE t.NAME='" );
        sql.append( restype );
        sql.append( "' AND r.LABEL='" );
        sql.append( residue );
        sql.append( "' AND a.NAME='" );
        sql.append( atom );
        sql.append( "' AND a.PSEUDO<>'Y' AND r.TYPEID=t.ID AND a.RESID=r.ID" );
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        String rc = null;
        if( rs.next() ) {
            rc = rs.getString( 1 );
            if( rs.wasNull() ) rc = null;
        }
        rs.close();
        query.close();
        return rc;
    } //************************************************************************
    /** Returns atom id.
     * @param nomid nomenclature id
     * @param resid residue id
     * @param atom atom name
     * @return atom number or -1
     * @throws java.sql.SQLException
     */
    public int getAtomId( int nomid, int resid, String atom )
    throws java.sql.SQLException {
        StringBuffer sql = new StringBuffer( "SELECT ID FROM ATOMS WHERE NOMID=" );
        sql.append( nomid );
        sql.append( " AND RESID=" );
        sql.append( resid );
        sql.append( " AND NAME='" );
        sql.append( atom );
        sql.append( '\'' );
if( DEBUG ) System.err.println( sql );
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        int rc = -1;
        if( rs.next() ) {
if( DEBUG ) System.err.println( "getAtomId: got resultset" );
            rc = rs.getInt( 1 );
            if( rs.wasNull() ) rc = -1;
        }
        rs.close();
        query.close();
if( DEBUG ) System.err.println( "getAtomId: return " + rc );
        return rc;
    } //************************************************************************
    /** Returns atom id.
     * @param nomid nomenclature id
     * @param restype residue type id
     * @param residue residue label
     * @param atom atom name
     * @return atom number or -1
     * @throws java.sql.SQLException
     */
    public int getAtomId( int nomid, int restype, String residue, String atom )
    throws java.sql.SQLException {
        StringBuffer sql = new StringBuffer( "SELECT a.ID FROM ATOMS a,RESIDUES r" +
        " WHERE r.TYPEID=" );
        sql.append( restype );
        sql.append( " AND r.LABEL='" );
        sql.append( residue );
        sql.append( "' AND a.NAME='" );
        sql.append( atom );
        sql.append( "' AND a.NOMID=" );
        sql.append( nomid );
        sql.append( " AND a.RESID=r.ID" );
if( DEBUG ) System.err.println( sql );
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        int rc = -1;
        if( rs.next() ) {
if( DEBUG ) System.err.println( "getAtomId: got resultset" );
            rc = rs.getInt( 1 );
            if( rs.wasNull() ) rc = -1;
        }
        rs.close();
        query.close();
if( DEBUG ) System.err.println( "getAtomId: return " + rc );
        return rc;
    } //************************************************************************
    /** Returns atom id.
     * @param restype residue type
     * @param residue residue label
     * @param atom atom name
     * @return atom number or -1
     * @throws java.sql.SQLException
     */
    public int getAtomId( String restype, String residue, String atom )
    throws java.sql.SQLException {
        StringBuffer sql = new StringBuffer( "SELECT a.ID FROM ATOMS a,RESIDUES r," +
        "RESTYPES t WHERE t.NAME='" );
        sql.append( restype );
        sql.append( "' AND r.LABEL='" );
        sql.append( residue );
        sql.append( "' AND a.NAME='" );
        sql.append( atom );
//        sql.append( "' AND a.PSEUDO<>'Y' AND r.TYPEID=t.ID AND a.RESID=r.ID" );
        sql.append( "' AND r.TYPEID=t.ID AND a.RESID=r.ID" );
if( DEBUG ) System.err.println( sql );
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        int rc = -1;
        if( rs.next() ) {
if( DEBUG ) System.err.println( "getAtomId: got resultset" );
            rc = rs.getInt( 1 );
            if( rs.wasNull() ) rc = -1;
        }
        rs.close();
        query.close();
if( DEBUG ) System.err.println( "getAtomId: return " + rc );
        return rc;
    } //************************************************************************
    /** Returns BMRB atom id(s).
     * @param nomid nomenclature id
     * @param resid residue id
     * @param atom atom name
     * @return atom number(s) or null
     * @throws java.sql.SQLException
     */
    public int [] getBMRBAtomIds( int nomid, int resid, String atom )
    throws java.sql.SQLException {
        StringBuffer sql = new StringBuffer( "SELECT n.BMRBID FROM ATOMNOM n,ATOMS a" );
        sql.append( " WHERE a.NAME='" );
        sql.append( atom );
        sql.append( "' AND a.NOMID=" );
        sql.append( nomid );
        sql.append( " AND a.RESID=" );
        sql.append( resid );
        sql.append( " AND a.ID=n.ATOMID" );
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        java.util.List atoms = new java.util.ArrayList();
        while( rs.next() ) {
            rs.getInt( 1 );
            if( ! rs.wasNull() ) atoms.add( new Integer( rs.getInt( 1 ) ) );
        }
        rs.close();
        query.close();
if( DEBUG ) for( int i = 0; i < atoms.size(); i++ ) System.err.println( "**** " + atoms.get( i ) );
        if( atoms.size() < 1 ) return null;
        int [] rc = new int[atoms.size()];
        for( int i = 0; i < atoms.size(); i++ ) rc[i] = ((Integer) atoms.get( i )).intValue();
        atoms.clear();
        return rc;
    } //************************************************************************
    /** Returns BMRB atom id(s).
     * @param atomid ID of non-BMRB atom
     * @return atom number or null
     * @throws java.sql.SQLException
     */
    public int [] getBMRBAtomIds( int atomid )
    throws java.sql.SQLException {
        StringBuffer sql = new StringBuffer( "SELECT BMRBID FROM ATOMNOM WHERE " +
        "ATOMID=" );
        sql.append( atomid );
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        java.util.List atoms = new java.util.ArrayList();
        while( rs.next() ) {
            rs.getInt( 1 );
            if( ! rs.wasNull() ) atoms.add( new Integer( rs.getInt( 1 ) ) );
        }
        rs.close();
        query.close();
if( DEBUG ) for( int i = 0; i < atoms.size(); i++ ) System.err.println( "*** " + atoms.get( i ) );
        if( atoms.size() < 1 ) return null;
        int [] rc = new int[atoms.size()];
        for( int i = 0; i < atoms.size(); i++ ) rc[i] = ((Integer) atoms.get( i )).intValue();
        atoms.clear();
        return rc;
    } //************************************************************************
    /** Returns atom name.
     * @param atomid atom number
     * @return name or null
     * @throws java.sql.SQLException
     */
    public String getAtomName( int atomid ) throws java.sql.SQLException {
        StringBuffer sql = new StringBuffer( "SELECT NAME FROM ATOMS WHERE ID=" );
        sql.append( atomid );
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        String rc = null;
        if( rs.next() ) {
            rc = rs.getString( 1 );
            if( rs.wasNull() ) rc = null;
        }
        rs.close();
        query.close();
        return rc;
    } //************************************************************************
    /** Returns atom nomenclature.
     * @param atomid atom number
     * @return nomenclature name or null
     * @throws java.sql.SQLException
     */
    public String getAtomNomenclature( int atomid ) throws java.sql.SQLException {
        StringBuffer sql = new StringBuffer( "SELECT n.NAME FROM ATOMS a," +
        "NOMENCLATURES n WHERE a.ID=" );
        sql.append( atomid );
        sql.append( " AND n.ID=a.NOMID" );
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        String rc = null;
        if( rs.next() ) {
            rc = rs.getString( 1 );
            if( rs.wasNull() ) rc = null;
        }
        rs.close();
        query.close();
        return rc;
    } //************************************************************************
    /** Returns true if atom is a pseudo-atom.
     * @param atomid atom number
     * @return true or false
     * @throws java.sql.SQLException
     */
    public boolean isPseudoAtom( int atomid ) throws java.sql.SQLException {
        StringBuffer sql = new StringBuffer( "SELECT ID FROM ATOMS WHERE ID=" );
        sql.append( atomid );
        sql.append( " AND PSEUDO='Y'" );
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        boolean rc = false;
        if( rs.next() ) rc = true;
        rs.close();
        query.close();
        return rc;
    } //************************************************************************
    /** Converts pseudo-atom ID to list of atom IDs.
     * @param atomid atom number
     * @return array of atom IDs or null
     * @throws java.sql.SQLException
     */
    public int [] expandPseudoAtom( int atomid ) throws java.sql.SQLException {
// check nomenclature
        java.util.List ids = new java.util.ArrayList();
        StringBuffer sql = new StringBuffer( "SELECT n.NAME FROM ATOMS a," +
        "NOMENCLATURES n WHERE a.ID=" );
        sql.append( atomid );
        sql.append( " AND a.NOMID=n.ID" );
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        rs.next();
        if( ! rs.getString( 1 ).equals( "BMRB" ) ) {
            sql.setLength( 0 );
            sql.append( "SELECT BMRBID FROM ATOMNOM WHERE ATOMID=" );
            sql.append( atomid );
            rs = query.executeQuery( sql.toString() );
            while( rs.next() ) {
                ids.add( new Integer( rs.getInt( 1 ) ) );
            }
        }
        else ids.add( new Integer( atomid ) );
        if( ids.size() < 1 ) { // no atoms
            System.err.println( "No record for pseudoatom " + atomid );
            rs.close();
            query.close();
            return null;
        }
// expand
        sql.setLength( 0 );
        sql.append( "SELECT ATOMID FROM PSEUDOS WHERE ID=" );
        sql.append( ((Integer) ids.get( 0 )).intValue() );
        if( ids.size() > 1 ) {
            for( int i = 1; i < ids.size(); i++ ) {
                sql.append( " OR ID=" );
                sql.append( ((Integer) ids.get( i )).intValue() );
            }
        }
        ids.clear();
        rs = query.executeQuery( sql.toString() );
        while( rs.next() ) {
            ids.add( new Integer( rs.getInt( 1 ) ) );
        }
        rs.close();
        query.close();
        int [] rc = new int[ids.size()];
        for( int i = 0; i < ids.size(); i++ ) rc[i] = ((Integer) ids.get( i )).intValue();
        ids.clear();
        return rc;
    } //************************************************************************
    /** Converts special-case atoms ("any" residue).
     * @param residue residue label
     * @param atom atom name
     * @return atom name
     */
    public String convertAnyAtom( String residue, String atom ) 
    throws java.sql.SQLException {
        StringBuffer sql = new StringBuffer( "SELECT TYPEID FROM RESIDUES WHERE LABEL='" );
        sql.append( residue );
        sql.append( '\'' );
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
if( DEBUG ) System.err.println( "checking " + atom );
if( DEBUG ) System.err.println( sql );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        if( ! rs.next() ) {
if( DEBUG ) System.err.println( "failed check restype " + residue );
            query.close();
            return atom;
        }
        int typeid = rs.getInt( 1 );
        String rc = atom.toUpperCase();
        sql.setLength( 0 );
        sql.append( "SELECT a.ID FROM ATOMS a,RESIDUES r WHERE a.NAME='" );
        sql.append( rc );
        sql.append( "' AND a.PSEUDO='Y' AND r.LABEL='any' AND r.TYPEID=" );
        sql.append( typeid );
        sql.append( " AND a.RESID=r.ID" );
if( DEBUG ) System.err.println( sql );
        rs = query.executeQuery( sql.toString() );
        if( ! rs.next() ) {
if( DEBUG ) System.err.println( "failed check pseudoId " + typeid );
            query.close();
            return rc;
        }
        sql.setLength( 0 );
        sql.append( "SELECT a.NAME FROM ATOMS a,PSEUDOS p WHERE p.ID=" );
        sql.append( rs.getInt( 1 ) );
        sql.append( " AND a.ID=p.ATOMID" );
if( DEBUG ) System.err.println( sql );
        rs = query.executeQuery( sql.toString() );
        if( ! rs.next() ) {
if( DEBUG ) System.err.println( "no id" );
            query.close();
            return rc;
        }
        else {
            rc = rs.getString( 1 );
            rs.close();
            query.close();
if( DEBUG ) System.err.println( "return " + rc );
            return rc;
        }
    } //************************************************************************
    /** Main method -- tests connection.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            RefDB r = new RefDB();
            r.connect();
            r.close();
        }
        catch( Exception e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
}
