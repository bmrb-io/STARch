package edu.bmrb.starch;

/**
 * Utility methods.
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Dec 1, 2006
 * Time: 5:52:42 PM
 *
 * $Id$
 */

public class Utils {
    private static final boolean DEBUG = false;
    /**
     * Convert tag name to column label.
     * Replaces underscores with html line breaks and adds html open/close tags.
     * @param name tag name
     * @return column label
     */
    public static String ColNameToLabel( String name ) {
//        return "<html>" + name.replaceAll( "_", "<br>" ) + "</html>";
        return name.replaceAll( "_", "\n" );
    } //*************************************************************************
    /**
     * Convert column label to tag name.
     * Replaces html line breaks with underscores and removes html open/close tags.
     * @param label column label
     * @return tag name
     */
    public static String ColLabelToName( String label ) {
        String str = label.replaceAll( "<html>", "" );
        str = str.replaceAll( "</html>", "" );
//        str = str.replaceAll("<br>", "_" );
        str = str.replaceAll("\n", "_" );
        return str;
    } //*************************************************************************
    /**
     * Quotes string for STAR
     * @param str string to quote
     * @return quoted string
     */
    public static String quoteForSTAR( String str ) {
        if( (str == null) || (str.trim().length() < 1) ) return ".";
        if( str.indexOf( "\n" ) > -1 ) return "\n;\n" + str + "\n;\n";
        if( str.matches( ".*\\s+'.*") || str.matches( ".*'\\s+.*" ) )
            return "\"" + str + "\"";
        if( str.matches( ".*\\s+\".*") || str.matches( ".*\"\\s+.*" ) )
            return "'" + str + "'";
        if( str.matches( ".*\\s+.*" ) )
            return "\"" + str + "\"";
        if( str.toLowerCase().matches( "data_\\p{Alnum}.*" )
                || str.toLowerCase().matches( "save_\\p{Alnum}.*" )
                || str.toLowerCase().matches( "loop_\\p{Alnum}.*" )
                || str.toLowerCase().matches( "stop_\\p{Alnum}.*" ) )
            return "\"" + str + "\"";
        if( str.charAt( 0 ) == '_' )
            return "\"" + str + "\"";
        return str;
    } //*************************************************************************
    /**
     * Quotes string for use in SQL statement.
     * @param str string to quote
     * @return original string with every single quote replaced with two
     */
    public static String quoteForDB( String str ) {
        return str.replaceAll( "'", "''" );
    } //*************************************************************************


    /**
     * Returns residue type ID.
     * @param conn JDBC connection
     * @param type residue type name
     * @return ID or -1
     * @throws java.sql.SQLException
     */
    public static int getResidueTypeId( java.sql.Connection conn, String type )
            throws java.sql.SQLException {
        java.sql.Statement query = conn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT ID FROM RESTYPES WHERE NAME='" +
                type + "'" );
        int rc;
        if( ! rs.next() ) rc = -1;
        else rc = rs.getInt( 1 );
        rs.close();
        query.close();
        return rc;
    } //*************************************************************************
    /**
     * Extract residue sequence from metadata.
     * @param conn JDBC connection
     * @param eid entity ID
     * @return sequence or null
     * @throws java.sql.SQLException
     */
    public static String getMetadataSequence( java.sql.Connection conn, int eid )
            throws java.sql.SQLException {
        String rc = null;
        java.sql.Statement query = conn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT v2.VAL FROM ENTRYVALS v1," +
                "ENTRYVALS v2,ENTRYTAGS t1,ENTRYTAGS t2 WHERE t1.NAME='_Entity.ID' AND " +
                "v1.VAL='" + eid + "' AND t2.NAME='_Entity.Polymer_seq_one_letter_code' " +
                "AND v1.TAGID=t1.ID AND v2.TAGID=t2.ID AND t1.SFID=t2.SFID" );
        if( rs.next() ) {
            rc = rs.getString( 1 );
            if( rs.wasNull() || rc.equals( "." ) || rc.equals( "?" ) )
                rc = null;
        }
        if( rc == null ) {
            rs = query.executeQuery( "SELECT v2.VAL FROM ENTRYVALS v1,ENTRYVALS v2," +
                    "ENTRYTAGS t1,ENTRYTAGS t2 WHERE t1.NAME='_Entity.ID' AND v1.VAL='" +
                    eid + "' AND t2.NAME='_Entity.Polymer_seq_one_letter_code_can' " +
                    "AND v1.TAGID=t1.ID AND v2.TAGID=t2.ID AND t1.SFID=t2.SFID" );
            if( rs.next() ) {
                rc = rs.getString( 1 );
                if( rs.wasNull() || rc.equals( "." ) || rc.equals( "?" ) )
                    rc = null;
            }
        }
        rs.close();
        query.close();
        if( rc != null ) rc = rc.replaceAll( "\n", "" );
if( DEBUG ) System.err.printf( "Seq. from metadata:\n%s\n", rc );
        return rc;
    } //*************************************************************************
    /**
     * Extract residue sequence from data table
     * @param conn JDBC connection
     * @param table table name (tag category)
     * @param seqcol sequence column (tag) name
     * @param lblcol label column (tag) name
     * @param nomid nomenclature ID
     * @param restypeid residue type number
     * @return sequence or null
     * @throws java.sql.SQLException
     */
    public static String getDataSequence( java.sql.Connection conn, String table,
                                          String seqcol, String lblcol, int nomid,
                                          int restypeid ) throws java.sql.SQLException {
        String rc = null;
        java.sql.Statement query = conn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        StringBuilder sql = new StringBuilder( "SELECT CODE FROM RESIDUES WHERE TYPEID=" );
        sql.append( restypeid );
        sql.append( " AND NOMID=" );
        sql.append( nomid );
        sql.append( " AND (LABEL=? OR CODE=?)" );
        java.sql.PreparedStatement pquery = conn.prepareStatement( sql.toString(),
                                                                   java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                                   java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs2 = null;
        sql.setLength( 0 );
        sql.append( "SELECT DISTINCT \"" );
        sql.append( seqcol );
        sql.append( "\",\"" );
        sql.append( lblcol );
        sql.append( "\" FROM ");
        sql.append( table );
        sql.append( " ORDER BY \"" );
        sql.append( seqcol );
        sql.append( '"' );
if( DEBUG ) System.err.println( sql );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        sql.setLength( 0 );
        String str;
        while( rs.next() ) {
            str = "X";
            pquery.setString( 1, rs.getString( 2 ) );
            pquery.setString( 2, rs.getString( 2 ) );
            rs2 = pquery.executeQuery();
            if( rs.next() ) {
                str = rs.getString( 1 );
                if( rs.wasNull() ) str = "X";
            }
            sql.append( str );
        }
        if( rs2 != null ) rs2.close();
        pquery.close();
        rs.close();
        query.close();
        return sql.toString();
    } //*************************************************************************
    /**
     * Compares residue sequence in data and metadata
     * @param conn JDBC connection
     * @param eid entity ID
     * @param table data table name
     * @param tag data tag name
     * @return starting number for residue sequence or -1 if sequences don't match
     * @throws java.sql.SQLException
     */
    public static int getSequenceStart( java.sql.Connection conn, int eid, String table,
                                        String tag ) throws java.sql.SQLException {
        String metaseq = null;
        java.sql.Statement query = conn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT v2.VAL FROM ENTRYVALS v1," +
                "ENTRYVALS v2,ENTRYTAGS t1,ENTRYTAGS t2 WHERE t1.NAME='_Entity.ID' AND " +
                "v1.VAL='" + eid + "' AND t2.NAME='_Entity.Polymer_seq_one_letter_code' " +
                "AND v1.TAGID=t1.ID AND v2.TAGID=t2.ID AND t1.SFID=t2.SFID" );
        if( rs.next() ) {
            metaseq = rs.getString( 1 );
            if( rs.wasNull() || metaseq.equals( "." ) || metaseq.equals( "?" ) )
                metaseq = null;
        }
        if( metaseq == null ) {
            rs = query.executeQuery( "SELECT v2.VAL FROM ENTRYVALS v1,ENTRYVALS v2," +
                    "ENTRYTAGS t1,ENTRYTAGS t2 WHERE t1.NAME='_Entity.ID' AND v1.VAL='" +
                    eid + "' AND t2.NAME='_Entity.Polymer_seq_one_letter_code_can' " +
                    "AND v1.TAGID=t1.ID AND v2.TAGID=t2.ID AND t1.SFID=t2.SFID" );
            if( rs.next() ) {
                metaseq = rs.getString( 1 );
                if( rs.wasNull() || metaseq.equals( "." ) || metaseq.equals( "?" ) )
                    metaseq = null;
            }
        }
if( DEBUG ) System.err.printf( "Seq. from metadata:\n%s\n", metaseq );
        if( metaseq == null ) {
            rs.close();
            query.close();
            return -1;
        }
        return 0;
    } //*************************************************************************
}
