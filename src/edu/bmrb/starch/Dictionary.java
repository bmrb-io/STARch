package edu.bmrb.starch;

/**
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Dec 14, 2006
 * Time: 2:02:25 PM
 *
 * $Id$
 */

public class Dictionary {
    private static final boolean DEBUG = false;
    /** JDBC comnnection. */
    private java.sql.Connection fConn = null;
/*******************************************************************************/
    /**
     * Constructor.
     * @param conn JDBC connection.
     */
    public Dictionary( java.sql.Connection conn ) {
        fConn = conn;
    } //*************************************************************************
    /**
     * Returns list of nomenclatures.
     * @return list of atom nomenclatures or null
     */
    public IntStringPair [] getNomenclatures() {
        try {
            java.util.ArrayList<IntStringPair> list = new java.util.ArrayList<IntStringPair>();
            int id;
            String name;
            java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                             java.sql.ResultSet.CONCUR_READ_ONLY );
            java.sql.ResultSet rs = query.executeQuery( "SELECT ID,NAME FROM NOMENCLATURES ORDER BY ID" );
            while( rs.next() ) {
                id = rs.getInt( 1 );
                if( rs.wasNull() ) id = -1;
                name = rs.getString( 2 );
                if( rs.wasNull() ) name = null;
                if( (name != null) && (id > -1) ) list.add( new IntStringPair( id, name ) );
            }
            rs.close();
            query.close();
            if( list.size() < 1 ) return null;
            edu.bmrb.starch.IntStringPair [] rc = new IntStringPair[list.size()];
            for( id = 0; id < list.size(); id++ ) rc[id] = list.get( id );
            list.clear();
            return rc;
        }
        catch( java.sql.SQLException e ) {
            System.err.print( "DB exception, cannot get list of nomenclatures.\nSQL state " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            return null;
        }
    } //*************************************************************************
    /**
     * Returns list of residue types.
     * @return array of IntStringPairs or null
     */
    public IntStringPair [] getResidueTypes() {
        try {
            java.util.ArrayList<IntStringPair> types = new java.util.ArrayList<IntStringPair>();
            java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                             java.sql.ResultSet.CONCUR_READ_ONLY );
// names in RESTYPES don't have "(L)" or "(D)"
            java.sql.ResultSet rs = query.executeQuery( "SELECT ID,NAME FROM RESTYPES ORDER BY ID" );
            while( rs.next() ) {
                rs.getString( 2 );
                if( ! rs.wasNull() )
                    types.add( new edu.bmrb.starch.IntStringPair( rs.getInt( 1 ), rs.getString( 2 ) ) );
            }
            rs.close();
            query.close();
            if( types.size() < 1 ) return null;
            edu.bmrb.starch.IntStringPair [] rc = new edu.bmrb.starch.IntStringPair[types.size()];
            for( int i = 0; i < types.size(); i++ ) rc[i] = types.get( i );
            types.clear();
            return rc;
        }
        catch( java.sql.SQLException e ) {
            System.err.print( "DB exception, cannot get list of residue types.\nSQL state " );
            System.err.println( e.getSQLState() );
            System.err.println( e );
            e.printStackTrace();
            return null;
        }
    } //*************************************************************************
    /**
     * Returns list of atom type : atom isotope pairs
     * @return array of pairs or null
     * @throws java.sql.SQLException
     */
    public StringStringPair [] getAtomIsotopes() throws java.sql.SQLException {
        java.util.ArrayList<StringStringPair> types = new java.util.ArrayList<StringStringPair>();
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT ATYPE,ISOTOPE FROM ISOTOPES" );
        while( rs.next() ) types.add( new StringStringPair( rs.getString( 1 ), rs.getString( 2 ) ) );
        rs.close();
        query.close();
        if( types.size() < 1 ) return null;
        StringStringPair [] rc = new StringStringPair[types.size()];
        for( int i = 0; i < types.size(); i++ ) rc[i] = types.get( i );
        types.clear();
        return rc;
    } //*************************************************************************
    /**
     * Returns true if tag is the local ID tag.
     * @param tag tag name
     * @return true or false
     * @throws java.sql.SQLException
     */
    public boolean isLocalId( String tag )
            throws java.sql.SQLException {
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT LOCALIDFLAG FROM TAGS WHERE TAGNAME='" +
                tag + "'" );
        boolean rc = false;
        if( rs.next() ) {
            String str = rs.getString( 1 );
            if( (! rs.wasNull()) && str.toUpperCase().equals( "Y" ) ) rc = true;
        }
        rs.close();
        query.close();
        return rc;
    } //*************************************************************************
    /**
     * Returns true if tag is the entry ID tag.
     * @param tag tag name
     * @return true or false
     * @throws java.sql.SQLException
     */
    public boolean isEntryId( String tag )
            throws java.sql.SQLException {
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT ENTRYIDFLAG FROM TAGS WHERE TAGNAME='" +
                tag + "'" );
        boolean rc = false;
        if( rs.next() ) {
            String str = rs.getString( 1 );
            if( (! rs.wasNull()) && str.toUpperCase().equals( "Y" ) ) rc = true;
        }
        rs.close();
        query.close();
        return rc;
    } //*************************************************************************
    /**
     * Returns true if tag is the entity ID tag.
     * @param tag tag name
     * @return true or false
     * @throws java.sql.SQLException
     */
    public boolean isEntityId( String tag )
            throws java.sql.SQLException {
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT t2.SFCAT FROM TAGS t1, TAGS t2," +
                "TAGRELS r WHERE t1.TAGNAME='" + tag + "' AND t2.LOCALIDFLAG='Y' AND r.CHLDSEQ=" +
                "t1.SEQ AND t2.SEQ=r.PRNTSEQ" );
        boolean rc = false;
        if( rs.next() ) {
            String str = rs.getString( 1 );
            if( (! rs.wasNull()) && str.toLowerCase().equals( "entity" ) ) rc = true;
        }
        rs.close();
        query.close();
        return rc;
    } //*************************************************************************
    /**
     * Returns true if tag is the entity assembly ID tag.
     * @param tag tag name
     * @return true or false
     * @throws java.sql.SQLException
     */
    public boolean isEntityAssemblyId( String tag )
            throws java.sql.SQLException {
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
//todo: FIXME???
        java.sql.ResultSet rs = query.executeQuery( "SELECT t2.SFCAT FROM TAGS t1, TAGS t2," +
                "TAGRELS r WHERE t1.TAGNAME='" + tag + "' AND t2.TAGNAME='_Entity_assembly.ID' " +
                "AND r.CHLDSEQ=t1.SEQ AND t2.SEQ=r.PRNTSEQ" );
        boolean rc = false;
        if( rs.next() ) {
            String str = rs.getString( 1 );
            if( (! rs.wasNull()) && str.toLowerCase().equals( "assembly" ) ) rc = true;
        }
        rs.close();
        query.close();
        return rc;
    } //*************************************************************************
    /**
     * Expands atom into a list of atoms.
     * If atom is a pseudo-atom, expands it into list of BMRB atoms. If it's a
     * BMRB atom, resulting list has only one element: original atom. If atom is
     * invalid, returns null.
     * @param nomid nomenclature ID
     * @param restypeid residue type ID
     * @param label residue label (comp ID)
     * @param atom atom name
     * @return list or null
     * @throws java.sql.SQLException
     */
    public IntStringPair [] getAtoms( int nomid, int restypeid, String label,
                                      String atom )
            throws java.sql.SQLException {
        java.util.ArrayList<IntStringPair> list = new java.util.ArrayList<IntStringPair>();
        java.sql.PreparedStatement pquery = fConn.prepareStatement( "SELECT a.ID," +
                "a.NAME FROM ATOMS a,PSEUDOS p WHERE p.ID=? AND a.ID=p.ATOMID",
                                                                    java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                                    java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs2 = null;
        StringBuilder sql = new StringBuilder();
        sql.append( "SELECT a.ID,a.NAME,a.PSEUDO FROM ATOMS a,RESIDUES r WHERE r.NOMID=" );
        sql.append( nomid );
        sql.append( " AND r.TYPEID=" );
        sql.append( restypeid );
        sql.append( " AND (r.LABEL='" );
        sql.append( Utils.quoteForDB( label.toUpperCase() ) );
        sql.append( "' OR r.CODE='" );
        sql.append( Utils.quoteForDB( label.toUpperCase() ) );
        sql.append( "') AND a.NAME='" );
        sql.append( Utils.quoteForDB( atom.toUpperCase() ) );
// spec. case: residue ID 0 is for mapping that apply to all atoms
        sql.append( "' AND (a.RESID=r.ID OR (a.RESID=0 AND a.PSEUDO='Y')) ORDER BY a.ID" );
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                          java.sql.ResultSet.CONCUR_READ_ONLY );
if( DEBUG ) System.err.println( sql );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        while( rs.next() ) {
            if( rs.getString( 3 ).equals( "N" ) )
                list.add( new IntStringPair( rs.getInt( 1 ), rs.getString( 2 ) ) );
            else {
                pquery.setInt( 1, rs.getInt( 1 ) );
if( DEBUG ) System.err.println( pquery );
                rs2 = pquery.executeQuery();
                while( rs2.next() )
                    list.add( new IntStringPair( rs2.getInt( 1 ), rs2.getString( 2 ) ) );
            }
        }
        if( rs2 != null ) rs2.close();
        pquery.close();
        rs.close();
        query.close();
        if( list.size() < 1 ) return null;
        IntStringPair [] rc = new IntStringPair[list.size()];
        for( int i = 0; i < rc.length; i++ ) rc[i] = list.get( i );
        list.clear();
        return rc;
    } //*************************************************************************
    /**
     * Returns saveframe category for tag category.
     * @param tagcat tag category.
     * @return saverfame category or null
     */
    public String getSaveframeCategory( String tagcat ) throws java.sql.SQLException {
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        StringBuilder sql = new StringBuilder();
        sql.append( "SELECT SFCAT FROM TAGS WHERE TAGCAT='" );
        sql.append( tagcat );
        sql.append( '\'' );
        String sfcat = null;
if( DEBUG ) System.err.println( sql );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        if( rs.next() ) {
            sfcat = rs.getString( 1 );
            if( rs.wasNull() ) sfcat = null;
        }
        if( sfcat == null ) {
            System.err.printf( "No saveframe category for %s\n", tagcat );
        }
        rs.close();
        query.close();
        return sfcat;
    } //*************************************************************************
    /**
     * Returns methylene protons for residue.
     * @param restypeid residue type ID
     * @param label residue label (comp ID)
     * @return pair of [original,replacement] pairs or null if there are no methylenes.
     * @throws java.sql.SQLException DB error
     */
    public java.util.List<Pair<Pair<String, String>, Pair<String,String>>> getMethylenes(
            int restypeid, String label ) throws java.sql.SQLException {
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        StringBuilder sql = new StringBuilder();
        sql.append( "SELECT m.ATOM1,m.ATOM2,m.NEW1,m.NEW2 FROM METHYLENES m JOIN " +
                "RESIDUES r ON r.ID=m.RESID WHERE r.TYPEID=" );
        sql.append( restypeid );
        sql.append( " AND (r.LABEL='" );
        sql.append( label.toUpperCase() );
        sql.append( "' OR r.CODE='" );
        sql.append( label.toUpperCase() );
        sql.append( "')" );
        java.util.ArrayList<Pair<Pair<String, String>, Pair<String, String>>> rc
                = new java.util.ArrayList<Pair<Pair<String, String>, Pair<String, String>>>();
if( DEBUG ) System.err.println( sql );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        while( rs.next() ) {
            Pair<String, String> first = new Pair<String, String>( rs.getString( 1 ), rs.getString( 3 ) );
            Pair<String, String> second = new Pair<String, String>( rs.getString( 2 ), rs.getString( 4 ) );
            rc.add( new Pair<Pair<String, String>, Pair<String, String>>( first, second ) );
        }
        rs.close();
        query.close();
        if( rc.size() < 1 ) return null;
        return rc;
    } //*************************************************************************
    /**
     * Returns methyl protons for residue.
     * @param restypeid residue type id
     * @param label residue label or code
     * @return array of (3) proton names or null
     * @throws java.sql.SQLException
     */
    public java.util.List<String []> getMethyls( int restypeid, String label )
            throws java.sql.SQLException {
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        StringBuilder sql = new StringBuilder();
        sql.append( "SELECT m.NAME FROM METHYLS m JOIN RESIDUES r ON m.RESID=r.ID " +
//                "JOIN RESTYPES t ON r.TYPEID=t.ID WHERE (r.LABEL='" );
                "WHERE (r.LABEL='" );
        sql.append( label.toUpperCase() );
        sql.append( "' OR r.CODE='" );
        sql.append( label.toUpperCase() );
        sql.append( "') AND r.TYPEID=" );
        sql.append( restypeid );
if( DEBUG ) System.err.println( sql );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        java.util.List<String []> rc = new java.util.ArrayList<String[]>();
        String [] meth = new String[3];
        int i = 0;
        while( rs.next() ) {
            meth[i] = rs.getString( 1 );
            i++;
            if( i == 3 ) {
                i = 0;
                rc.add( meth );
                meth = new String[3];
            }
        }
// if DB is broken (!= 3 methyl protons), it should crash somewhere later on
        rs.close();
        query.close();
if( DEBUG )
    for( String [] s : rc ) System.err.printf( "Meth: %s %s %s\n", s[0], s[1], s[2] );
        return rc;
    } //*************************************************************************
    public static String getTagCategory( String tag ) {
        if( (tag == null) || (tag.trim().length() < 1) ) return null;
        java.util.regex.Pattern p3 = java.util.regex.Pattern.compile( "_([_a-zA-Z0-9]+)\\.[^.]+" );
        java.util.regex.Pattern p2 = java.util.regex.Pattern.compile( "_[_a-zA-Z0-9]+[^.]+" );
        java.util.regex.Matcher m = p3.matcher( tag );
        if( m.matches() ) {
if( DEBUG ) System.err.printf( "Tag %s matches 3.x, group1 = %s\n", tag, m.group( 1 ) );
            return m.group( 1 );
        }
        else {
            m = p2.matcher( tag );
//todo
// in 2.1 datum type is stored in saveframe category which we don't have. Most
// tables are chemical shifts, for anything else they'll have to use a converter.
// maybe read the saveframe?
            if( m.matches() ) return "Atom_chem_shift";
        }
        return null;
    } //*************************************************************************
    /**
     * Return default ambiguity code for atom.
     * @param restypeid residue type ID
     * @param compid residue label or code
     * @param atomid atom name
     * @return ambguity code or -1
     * @throws java.sql.SQLException
     */
    public int getDefaultAmbicode( int restypeid, String compid, String atomid )
            throws java.sql.SQLException {
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        StringBuilder sql = new StringBuilder();
        sql.append( "SELECT a.AMBICODE FROM ATOMS a JOIN RESIDUES r ON a.RESID=r.ID " +
                "WHERE (r.LABEL='" );
        sql.append( Utils.quoteForDB( compid ) );
        sql.append( "' OR r.CODE='" );
        sql.append( Utils.quoteForDB( compid ) );
        sql.append( "') AND r.TYPEID=" );
        sql.append( restypeid );
        sql.append( " AND a.NAME='" );
        sql.append( Utils.quoteForDB( atomid ) );
        sql.append( '\'' );
if( DEBUG )
    System.err.println( sql );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        int rc;
        if( ! rs.next() ) rc = -1;
        else {
            rc = rs.getInt( 1 );
            if( rs.wasNull() ) rc = -1;
        }
        rs.close();
        query.close();
        return rc;
    } //*************************************************************************
}
