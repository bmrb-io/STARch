/*
 * TableToLoop.java
 *
 * Created on April 7, 2005, 5:14 PM
 *
 * This software is copyright (c) 2005 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/TableToLoop.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/06/20 22:29:31 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: TableToLoop.java,v $
 * Revision 1.3  2005/06/20 22:29:31  dmaziuk
 * vacation
 *
 * Revision 1.2  2005/05/20 23:07:52  dmaziuk
 * *** empty log message ***
 *
 * Revision 1.1  2005/04/13 17:37:07  dmaziuk
 * *** empty log message ***
 * */

package EDU.bmrb.starch;

/**
 * Converts a space (tab) - delimited file to NMR-STAR loop.
 * <P>
 * File format: first non-blank line must contain headers: NMR-STAR 3.0 tags.
 * Program does a dictionary lookup (using JDBC connection specified in
 * ~/validator.properties file) to retrieve all loop tags. Empty columns are
 * filled with question marks.
 * <P>
 * Command line arguments:
 * <UL>
 *  <LI>-c FILE: configuration file (default: ~/validator.properties)</LI>
 *  <LI>-i FILE: input file (default: stdin)</LI>
 *  <LI>-o FILE: output file (default: stdout)</LI>
 * </UL>
 *
 * @author  dmaziuk
 */
public class TableToLoop {
    private final static boolean DEBUG = true; //false;
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
    /** list of loop tags */
    private java.util.HashMap fTagsByName = null;
    /** file tags */
    private String [] fTags = null;
    /** values */
    private String [] fVals = null;
    /** list of loop tags, sorted by dictionary number */
    private java.util.TreeMap fTagsByNum = null;
    /** JDBC connection */
    private java.sql.Connection fConn = null;
//******************************************************************************
    /** Creates a new instance of TableToLoop.
     * @param props properties
     */
    public TableToLoop( java.util.Properties props ) {
        fProps = props;
        fTagsByName = new java.util.HashMap();
        fTagsByNum = new java.util.TreeMap();
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
        if( fConn == null ) return;
        if( fConn.isClosed() ) return;
        if( ! fConn.getAutoCommit() ) fConn.commit();
        fConn.close();
        fConn = null;
    } //************************************************************************
    /** Retrieves loop tags.
     * @param tag tag name
     * @throws ClassNotFoundExeption from connect()
     * @throws java.sql.SQLException
     * @throws IllegalArgumentException invalid tag
     */
    public void fetchLoopTags( String tag ) throws java.sql.SQLException,
    ClassNotFoundException {
        String tagcat = null;
        int pos = tag.indexOf( '.' );
        if( pos < 0 ) throw new IllegalArgumentException( "Invalid tag: " + tag );
        tagcat = tag.substring( 0, pos + 1 );
if( DEBUG ) System.err.println( "Tag category: " + tagcat );
        Integer num;
        if( fConn == null ) connect();
        java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT TAGNAME,SEQ FROM TAGS " +
        "WHERE TAGNAME LIKE '" + tagcat + "%' AND DELETEFLAG<>'Y' ORDER BY SEQ" );
        while( rs.next() ) {
            num = new Integer( rs.getInt( 2 ) );
            fTagsByName.put( rs.getString( 1 ), num );
            fTagsByNum.put( num, rs.getString( 1 ) );
        }
        rs.close();
        query.close();
        close();
        if( fTagsByName.size() < 1 )
            throw new IllegalArgumentException( "Tag not in dictionary: " + tag );
    } //************************************************************************
    /** Converts file.
     * @param in input stream
     * @param out output stream
     * @throws ClassNotFoundExeption from fetchLoopTags()
     * @throws java.sql.SQLException from fetchLoopTags()
     * @throws IllegalArgumentException from fetchLoopTags()
     * @throws NullPointerException wrong number of columns
     * @throws java.io.IOException
     */
    public void convert( java.io.BufferedReader in, java.io.PrintWriter out )
    throws java.io.IOException, java.sql.SQLException, ClassNotFoundException {
        String str;
        int lineno = 0;
        int i;
        while( (str = in.readLine()) != null ) {
            lineno++;
            str = str.trim();
            if( str.length() < 1 ) continue;
            fTags = str.split( "\\s+" );
            break;
        }
if( DEBUG ) {
    System.err.println( "File tags: " );
    for( int k = 0; k < fTags.length; k++ ) System.err.println( fTags[k] );
}
        fetchLoopTags( fTags[0] );
if( DEBUG ) {
    System.err.println( "Loop tags: " );
    for( java.util.Iterator k = fTagsByName.keySet().iterator(); k.hasNext(); )
        System.err.println( (String) k.next() );
}
        java.util.Iterator itr;
        out.print( "    " );
        out.println( "loop_" );
        for( itr = fTagsByNum.values().iterator(); itr.hasNext(); ) {
            out.print( "        " );
            out.println( itr.next() );
        }
        out.println();
        fVals = new String[fTagsByName.size()];
        String [] fields;
        Integer key;
        int idx;
        while( (str = in.readLine()) != null ) {
            lineno++;
            str = str.trim();
            if( str.length() < 1 ) continue;
            fields = str.split( "\\s+" );
            if( fields.length != fTags.length )
                throw new java.io.IOException( "Wrong number of values in line " + lineno );
            for( i = 0; i < fVals.length; i++ )
                fVals[i] = "?";
            for( i = 0; i < fields.length; i++ ) {
                key = (Integer) fTagsByName.get( fTags[i] );
if( DEBUG ) System.err.println( "Key = " + key );
                idx = 0;
                for( itr = fTagsByNum.keySet().iterator(); itr.hasNext(); idx++ ) {
                    if( key.intValue() == ((Integer) itr.next()).intValue() ) {
                        fVals[idx] = fields[i];
                        break;
                    }
                }
            }
if( DEBUG ) {
    System.err.println( "Loop row: " );
    for( int k = 0; k < fVals.length; k++ )
        System.err.println( fVals[k] );
}
            out.print( "        " );
            for( i = 0; i < fVals.length; i++ ) {
                out.print( fVals[i] );
                if( i < (fVals.length - 1) ) out.print( "    " );
            }
            out.println();
        } // endwhile
        out.println();
        out.print( "    " );
        out.print( "stop_" );
    } //************************************************************************
    /** Prints usage summary */
    public static void usage() {
        System.err.println( "Usage: java EDU.bmrb.starch.InsertLabels " +
        "[-c FILE] [-i FILE] [-o FILE]" );
        System.err.print( "-c FILE: config file (default: " );
        System.err.print( System.getProperty( "user.home" ) );
        System.err.print( java.io.File.separator );
        System.err.print( DEF_PROPFILE );
        System.err.println( ')' );
        System.err.println( "-i FILE: input file (default: stdin)" );
        System.err.println( "-o FILE: output file (default: stdout)" );
    } //************************************************************************
    /** Main method.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            String propfile = null;
            String infile = null;
            String outfile = null;
            int opt;
            gnu.getopt.Getopt g = new gnu.getopt.Getopt( "TableToLoop", args, "c:i:o:h" );
            while( (opt = g.getopt()) != -1 ) {
                switch( opt ) {
                    case 'c' : // config file
                        propfile = g.getOptarg();
                        break;
                    case 'i' : // input file
                        infile = g.getOptarg();
                        break;
                    case 'o' : // output file
                        outfile = g.getOptarg();
                        break;
                    default : 
                        usage();
                        System.exit( 2 );
                }
            }
            if( propfile == null ) propfile = System.getProperty( "user.home" ) +
            java.io.File.separator + DEF_PROPFILE;
            java.util.Properties props = new java.util.Properties();
            java.io.InputStream pin = new java.io.FileInputStream( propfile );
            props.load( pin );
            pin.close();
            pin = null;
            TableToLoop conv = new TableToLoop( props );
            java.io.BufferedReader in;
            if( infile == null ) in = new java.io.BufferedReader(
            new java.io.InputStreamReader( System.in ) );
            else in = new java.io.BufferedReader( new java.io.FileReader( infile ) );
            java.io.PrintWriter out;
            if( outfile == null ) out = new java.io.PrintWriter( System.out );
            else out = new java.io.PrintWriter( new java.io.FileOutputStream( outfile ) );
            conv.convert( in, out );
            in.close();
            out.flush();
            out.close();
        }
        catch( Exception e ) {
            System.err.println( e );
            e.printStackTrace();
            System.exit( 1 );
        }
    } //************************************************************************
}
