package edu.bmrb.starch;

/**
 * NMR-STAR loop pretty-printer.
 *
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Dec 5, 2006
 * Time: 2:34:46 PM
 *
 * $Id$
 */

public class LoopWriter {
    private final static boolean DEBUG = false;
    private final static int TABWIDTH = 3;
    private enum Types {
        FLOAT,
        INT,
        STRING
    }
    /**
     * Prints out the table.
     * @param conn JDBC connection
     * @param table table name
     * @param out output stream
     * @param tabwidth tab widths
     * @throws NullPointerException if tag category (table name) not in dictionary,
     *                              query returned no rows, etc. ("should never hapen")
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public static void printLoop( java.sql.Connection conn, String table,
                                  java.io.PrintWriter out, int tabwidth )
            throws java.io.IOException, java.sql.SQLException {
        java.sql.Statement query = conn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.Statement query2 = conn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                          java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs2 = null;
// fetch list of tags in dictionary order
        java.util.ArrayList<String> tags = new java.util.ArrayList<String>();
        String str;
        String rowindex = null;
        java.sql.ResultSet rs = query.executeQuery( "SELECT TAGNAME,VALTYPE,ROWIDXFLAG,SEQ " +
                "FROM TAGS WHERE TAGCAT='" + table + "' AND DELETEFLAG<>'Y' AND SFIDFLAG<>'Y' " +
                "ORDER BY SEQ" );
        while( rs.next() ) {
            tags.add( rs.getString( 1 ) );
            if( rs.getString( 3 ).equals( "Y") ) rowindex = rs.getString( 1 );
        }
        if( tags.size() < 1 ) throw new NullPointerException( "No tags for category " + table );
// column widths
        int i, j;
        if( tabwidth < 1 ) tabwidth = TABWIDTH;
        int [] widths = new int[tags.size()];
        for( i = 0; i < widths.length; i++ ) widths[i] = tabwidth;
        int [] order = new int[tags.size()];
        for( i = 0; i < order.length; i++ ) order[i] = -1;
//FIXME: "LIMIT" is not portable
	StringBuilder sql = new StringBuilder();
        rs = query.executeQuery( "SELECT * FROM " + table + " LIMIT 1" );
        java.sql.ResultSetMetaData md = rs.getMetaData();
        for( j = 0; j < tags.size(); j++ ) {
            for( i = 1; i <= md.getColumnCount(); i++ ) {
                str = "_" + table + "." + md.getColumnName( i );
if( DEBUG ) System.err.printf( "Looking for %s\n", str );
                if( tags.get( j ).equals( str ) ) {
                    if( (rowindex != null) && rowindex.equals( str ) )
                        rowindex = md.getColumnName( i );
                    sql.setLength( 0 );
                    sql.append( "SELECT MAX(CHARACTER_LENGTH(" );
                    switch( md.getColumnType( i ) ) {
                	case java.sql.Types.CHAR :
                	case java.sql.Types.VARCHAR :
                	    sql.append( '"' );
                	    sql.append( md.getColumnName( i ) );
                	    sql.append( '"' );
                	    break;
                	default:
                	    sql.append( "CAST(\"" );
                	    sql.append( md.getColumnName( i ) );
                	    sql.append( "\" AS VARCHAR(127))" );
                    }
                    sql.append( ")) FROM " );
                    sql.append( table );
//                    rs2 = query2.executeQuery( "SELECT MAX(CHARACTER_LENGTH(\"" +
//                            md.getColumnName( i ) + "\")) FROM " + table );
                    rs2 = query2.executeQuery( sql.toString() );
                    if( ! rs2.next() ) throw new NullPointerException( "No rows from SELECT maxwidth" );
                    order[j] = i;
                    widths[j] += rs2.getInt( 1 );
                    break;
                }
            } // endfor i
        } // endfor j
if( DEBUG ) for( i = 0; i < tags.size(); i++ ) System.err.printf( "%s: %d\n", tags.get( i ), widths[i] );
        if( rs2 != null ) rs2.close();
        query2.close();
// print
        int indent = 1;
        for( i = 0; i < (tabwidth * indent); i++ ) out.print( ' ' );
        out.println( "loop_" );
        indent++;
        for( String tag:  tags ) {
            for( i = 0; i < (tabwidth * indent); i++ ) out.print( ' ' );
            out.println( tag );
        }
        out.println();
        tags.clear();
        if( rowindex != null ) str = "SELECT * FROM " + table + " ORDER BY \"" + rowindex + "\"";
        else str = "SELECT * FROM " + table;
        rs = query.executeQuery( str );
        while( rs.next() ) {
            for( i = 0; i < (tabwidth * indent); i++ ) out.print( ' ' );
            for( i = 0; i < order.length; i++ ) {
                str = "%" + widths[i] + "s";
                if( order[i] > 0 ) {
                    rs.getString( order[i] );
                    if( rs.wasNull() ) out.printf( str, "." );
                    else out.printf( str, Utils.quoteForSTAR( rs.getString( order[i] ) ) );
                }
                else out.printf( str, "." );
            }
            out.println();
        }
        indent--;
        for( i = 0; i < (tabwidth * indent); i++ ) out.print( ' ' );
        out.println( "stop_" );
        out.flush();
        rs.close();
        query.close();
    } //*************************************************************************
}
