package edu.bmrb.starch;

/**
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Dec 14, 2006
 * Time: 3:58:52 PM
 *
 * $Id$
 */

public class Metadata {
    private static final boolean DEBUG = false;
    /** JDBC connection. */
    private java.sql.Connection fConn = null;
/**************************************************************************** **/
    /**
     * Constructor.
     * @param conn JDBC connection.
     */
    public Metadata( java.sql.Connection conn ) {
        fConn = conn;
    } //*************************************************************************
    /**
     * Returns residue type name and ID.
     * @param eid entity ID
     * @return type name/id or null
     * @throws java.sql.SQLException
     */
    public IntStringPair getResidueType( int eid )
            throws java.sql.SQLException {
        String restype = null;
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
// NOTE: _Entity.ID is an integer in the proper schema, however, in validator all columns are
// varchars (except for auto-generated sfids). Hence the parameter type mismatcha nd the cast.
        java.sql.ResultSet rs = query.executeQuery( "SELECT ENTITY FROM ENTRYRESTYPES " +
                "WHERE EID='" + eid + "'" );
        if( rs.next() ) {
            restype = rs.getString( 1 );
            if( rs.wasNull() || restype.equals( "?" ) || restype.equals( "." ) )
                restype = null;
        }
        int resid = -1;
        if( restype != null ) {
// polymer types in entry have (L) or (D) ones in RESTYPES don't
            if( (restype.indexOf( "(L)" ) > -1) || (restype.indexOf( "(D)") > -1) )
                restype = restype.substring( 0, restype.length() - 3 );
            rs = query.executeQuery( "SELECT ID FROM RESTYPES WHERE NAME='" + restype + "'" );
            if( rs.next() ) {
                resid = rs.getInt( 1 );
                if( rs.wasNull() ) resid = -1;
            }
        }
        rs.close();
        query.close();
        if( (restype == null) || (resid < 0) ) return null;
        return new IntStringPair( resid, restype );
    } //*************************************************************************
    /**
     * Returns entry id.
     * @return accession number or null
     * @throws java.sql.SQLException
     */
    public String getEntryId() throws java.sql.SQLException {
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT v.VAL FROM ENTRYVALS v,ENTRYTAGS t " +
                "WHERE t.NAME='_Entry.ID' AND v.TAGID=t.ID" );
        String rc;
        if( ! rs.next() ) rc = null;
        else {
            rc = rs.getString( 1 );
            if( rs.wasNull() || rc.equals( "." ) || rc.equals( "?" ) ) rc = null;
        }
        rs.close();
        query.close();
        return rc;
    } //*************************************************************************
    /**
     * Returns list of entity names and IDs
     * @return list
     * @throws java.sql.SQLException
     */
    public IntStringPair [] getEntities() throws java.sql.SQLException {
        java.util.ArrayList<IntStringPair> list = new java.util.ArrayList<IntStringPair>();
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT v1.VAL,v2.VAL FROM ENTRYVALS v1," +
                "ENTRYVALS v2,ENTRYTAGS t1,ENTRYTAGS t2 WHERE t1.NAME='_Entity.ID' AND " +
                "t2.NAME='_Entity.Name' AND t1.SFID=t2.SFID AND v1.TAGID=t1.ID AND v2.TAGID=t2.ID" );
        while( rs.next() ) list.add( new edu.bmrb.starch.IntStringPair( rs.getInt( 1 ), rs.getString( 2 ) ) );
        rs.close();
        query.close();
        if( list.size() < 1 ) return null;
        edu.bmrb.starch.IntStringPair p;
        for( java.util.Iterator<IntStringPair> i = list.iterator(); i.hasNext(); ) {
            p = i.next();
            if( (p.getString() == null) || p.getString().equals( "?" ) || p.getString().equals( "." ) )
                i.remove();
        }
        edu.bmrb.starch.IntStringPair [] rc = new IntStringPair[list.size()];
        for( int i = 0; i < list.size(); i++ ) rc[i] = list.get( i );
        list.clear();
        return rc;
    } //*************************************************************************
    /**
     * Returns list of entity ID / entity assembly ID pairs.
     * @return list of ID pairs or null
     * @throws java.sql.SQLException
     */
    public java.util.ArrayList<Pair<Integer, Integer>> getEntityAssebmlyIds()
            throws java.sql.SQLException {
        String str;
        java.util.ArrayList<Pair<Integer, Integer>> list = new java.util.ArrayList<Pair<Integer, Integer>>();
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT v1.VAL,v2.VAL FROM ENTRYVALS v1," +
                "ENTRYVALS v2,ENTRYTAGS t1,ENTRYTAGS t2 WHERE t1.NAME='_Entity_assembly.ID' AND " +
                "t2.NAME='_Entity_assembly.Entity_ID' AND v1.TAGID=t1.ID AND v2.TAGID=t2.ID " +
                "AND v1.LOOPROW=v2.LOOPROW" );
        while( rs.next() ) {
            str = rs.getString( 1 );
            if( rs.wasNull() || str.equals( "?" ) || str.equals( "." ) ) continue;
            str = rs.getString( 2 );
            if( rs.wasNull() || str.equals( "?" ) || str.equals( "." ) ) continue;
            list.add( new Pair<Integer, Integer>( rs.getInt( 1 ), rs.getInt( 2 ) ) );
        }
        rs.close();
        query.close();
        if( list.size() < 1 ) return null;
        return list;
    } //*************************************************************************
    /**
     * Returns list of saveframe name : saveframe local ID pairs.
     * @param sfcat saveframe category
     * @return list of IDs or null
     * @throws java.sql.SQLException
     */
    public java.util.ArrayList<Pair<Integer, String>> getLocalIds( String sfcat )
            throws java.sql.SQLException {
        java.util.ArrayList<Pair<Integer, String>> list = new java.util.ArrayList<Pair<Integer, String>>();
        String str;
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        StringBuilder sql = new StringBuilder();
        sql.append( "SELECT s.NAME,v.VAL FROM ENTRYSFS s,ENTRYTAGS e,ENTRYVALS v,TAGS t " +
                "WHERE t.SFCAT='" );
        sql.append( sfcat );
        sql.append( "' AND t.LOCALIDFLAG='Y' AND t.LOOPFLAG<>'Y' AND t.SEQ=e.SEQ " +
                "AND s.ID=e.SFID AND v.TAGID=e.ID" );
// saveframe name : saveframe local ID pairs
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        while( rs.next() ) {
            str = rs.getString( 1 );
            if( rs.wasNull() || str.equals( "?" ) || str.equals( "." ) ) continue;
            str = rs.getString( 2 );
            if( rs.wasNull() || str.equals( "?" ) || str.equals( "." ) ) continue;
            list.add( new Pair<Integer, String>( rs.getInt( 2 ), rs.getString( 1 ) ) );
        }
        rs.close();
        query.close();
        if( list.size() < 1 ) {
            System.err.printf( "No saveframe local IDs found for %s\n", sfcat );
            return null;
        }
        return list;
    } //*************************************************************************
    /**
     * Returns list of residue sequence number : residue label pairs.
     * @param eid entity ID
     * @return null if there is no _Entity_comp_index loop for this entity
     * @throws java.sql.SQLException
     */
    public java.util.ArrayList<Pair<Integer, String>> getCompIndexIds( String eid )
            throws java.sql.SQLException {
        if( (eid == null) || (eid.trim().length() < 1) ) return null;
        eid = eid.trim();
        java.util.ArrayList<Pair<Integer, String>> list = new java.util.ArrayList<Pair<Integer, String>>();
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        String str;
        StringBuilder sql = new StringBuilder();
        sql.append( "SELECT v2.VAL,v3.VAL,v1.LOOPROW FROM ENTRYVALS v1,ENTRYVALS v2," +
                "ENTRYVALS v3,ENTRYTAGS t1,ENTRYTAGS t2,ENTRYTAGS t3 WHERE t1.NAME='" );
        sql.append( "_Entity_comp_index.Entity_ID" );
        sql.append( "' AND v1.VAL='" );
        sql.append( eid.trim() );
        sql.append( "' AND t2.NAME='_Entity_comp_index.ID'" );
        sql.append( " AND t3.NAME='_Entity_comp_index.Comp_ID'" );
        sql.append( " AND v1.TAGID=t1.ID AND v2.TAGID=t2.ID AND v3.TAGID=t3.ID" );
        sql.append( " AND v2.LOOPID=v1.LOOPID AND v3.LOOPID=v1.LOOPID" );
        sql.append( " AND v2.LOOPROW=v1.LOOPROW AND v3.LOOPROW=v1.LOOPROW" );
        sql.append( " ORDER BY v1.LOOPROW" );
// sequence num : label pairs
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        while( rs.next() ) {
            str = rs.getString( 1 );
            if( rs.wasNull() || str.equals( "?" ) || str.equals( "." ) ) continue;
            str = rs.getString( 2 );
            if( rs.wasNull() || str.equals( "?" ) || str.equals( "." ) ) continue;
            list.add( new Pair<Integer, String>( rs.getInt( 1 ), rs.getString( 2 ) ) );
        }
        rs.close();
        query.close();
        if( list.size() < 1 ) {
            System.err.printf( "No _Entity_comp_index loop for entity %s\n", eid );
            return null;
        }
        return list;
    } //*************************************************************************
}
