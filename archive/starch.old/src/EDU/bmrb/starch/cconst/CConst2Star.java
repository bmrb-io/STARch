/*
 * CConst2Star.java
 *
 * Created on May 17, 2005, 2:40 PM
 *
 * This software is copyright (c) 2005 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/cconst/CConst2Star.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/05/20 23:07:52 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: CConst2Star.java,v $
 * Revision 1.1  2005/05/20 23:07:52  dmaziuk
 * *** empty log message ***
 * */

package EDU.bmrb.starch.cconst;
import EDU.bmrb.starch.*;
/**
 * Converts output of c_const applet to full NMR-STAR 3.0 loop.
 *
 * @author  dmaziuk
 */
public class CConst2Star implements EDU.bmrb.sansj.ContentHandler,
EDU.bmrb.sansj.ErrorHandler {
    private static final boolean DEBUG = true; //false;
    /** default properties file */
    public static final String DEF_PROPFILE = "validator.properties";
    /* property keys */
    /** JDBC driver */
    public static final String KEY_DRIVER = "DB.Driver";
    /** JDBC URL */
    public static final String KEY_URL = "DB.Url";
    /** DB username */
    public static final String KEY_USER = "DB.User";
    /** DB password */
    public static final String KEY_PASSWD = "DB.Password";
    /** tag */
    public static final String TAG_CCID = "_Coupling_constant.ID";
    /** properties */
    private java.util.Properties fProps = null;
    /** JDBC connection */
    private java.sql.Connection fConn = null;
    /** Loop table */
    private StringTable fTbl = null;
    /** first tag flag */
    private String fFirstTag = null;
    /** row number */
    private int fRow = -1;
//******************************************************************************
    /** Creates a new instance of CConst2Star.
     * @param props properties
     */
    public CConst2Star( java.util.Properties props ) {
        fProps = props;
    } //************************************************************************
    /** Connects to database.
     * @throws ClassNotFoundException: no JDBC driver
     * @throws java.sql.SQLException: problem with JDBC connection
     */
    public void connect() throws java.sql.SQLException, ClassNotFoundException {
        if( fConn != null )
            if(! fConn.isClosed() ) return;
        Class.forName( fProps.getProperty( KEY_DRIVER ) );
        String url = fProps.getProperty( KEY_URL );
        if( url == null || url.length() < 1 )
            throw new NullPointerException( "Error in properties file: no DB URL" );
        String usr = fProps.getProperty( KEY_USER );
        if( usr == null || usr.length() < 1 )
            throw new NullPointerException( "Error in properties file: no DB URL" );
        fConn =java.sql.DriverManager.getConnection( url, usr, fProps.getProperty( KEY_PASSWD ) );
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
    public String [] fetchLoopTags( String tag ) throws java.sql.SQLException,
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
        java.util.List tags = new java.util.ArrayList();
        while( rs.next() )
            tags.add( rs.getString( 1 ) );
        rs.close();
        query.close();
        if( tags.size() < 1 )
            throw new IllegalArgumentException( "Tag not in dictionary: " + tag );
        String [] rc = new String[tags.size()];
        int i = 0;
        for( java.util.Iterator itr = tags.iterator(); itr.hasNext(); i++ )
            rc[i] = (String) itr.next();
        tags.clear();
        return rc;
    } //************************************************************************
    /** Parses input.
     * @param in input stream
     * @return false on error
     */
    public boolean parse( java.io.Reader in ) {
        String [] tags = null;
        try { tags = fetchLoopTags( TAG_CCID ); }
        catch( Exception e ) {
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
//FIXME: development dictionary tags, remove when dictionary is updated
        {
            java.util.ArrayList hack = new java.util.ArrayList();
            int i = 0;
            for( i = 0; i < tags.length; i++ ) hack.add( tags[i] );
            boolean found;
            String [] DEV_TAGS = {
                "_Coupling_constant.Auth_seq_ID_1",
                "_Coupling_constant.Auth_comp_ID_1",
                "_Coupling_constant.Auth_atom_ID_1",
                "_Coupling_constant.Auth_atom_name_1",
                "_Coupling_constant.Auth_seq_ID_2",
                "_Coupling_constant.Auth_comp_ID_2",
                "_Coupling_constant.Auth_atom_ID_2",
                "_Coupling_constant.Auth_atom_name_2"
            };
            for( i = 0; i < DEV_TAGS.length; i++ ) {
                found = false;
                for( int j = 0; j < tags.length; j++ )
                    if( tags[j].equals( DEV_TAGS[i] ) ) {
                        found = true;
                        break;
                    }
                if( ! found ) hack.add( DEV_TAGS[i] );
            }
            tags = new String[hack.size()];
            for( i = 0; i < hack.size(); i++ ) tags[i] = (String) hack.get( i );
        }
        fTbl = new StringTable( tags );
        EDU.bmrb.sansj.STARLexer lex = new EDU.bmrb.sansj.STARLexer( in );
        EDU.bmrb.sansj.LoopParser lp = new EDU.bmrb.sansj.LoopParser( lex, this, this );
        lp.parse();
        return true;
    } //************************************************************************
    /** Prints table.
     * @param out output stream
     */
    public void print( java.io.PrintStream out ) {
        fTbl.print( out );
    } //************************************************************************
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            if( args.length < 1 ) System.exit( 1 );
            
            String propfile = System.getProperty( "user.home" ) +
            java.io.File.separator + DEF_PROPFILE;
            java.util.Properties props = new java.util.Properties();
            java.io.InputStream pin = new java.io.FileInputStream( propfile );
            props.load( pin );
            pin.close();
            pin = null;
            
            CConst2Star conv = new CConst2Star( props );
            conv.connect();
            java.io.Reader in = new java.io.FileReader( args[0] );
            conv.parse( in );
            conv.print( System.out );
            conv.close();
        }
        catch( Exception e ) {
            System.err.println( e );
            e.printStackTrace();
            System.exit( 2 );
        }
    }
//*********************** callbacks ********************************************
    /** Parse error.
     * @param line line number
     * @param col column number
     * @param str error message
     */
    public void error( int line, int col, String str ) {
//        if( fErrs != null ) fErrs.add(...);
        System.err.print( "Parse error in line " );
        System.err.print( line );
        System.err.print( ", column " );
        System.err.print( col );
        System.err.print( ": " );
        System.err.println( str );
    } //************************************************************************
    /** Parse warning.
     * @param line line number
     * @param col column number
     * @param str error message
     * @return false to continue parsing
     */
    public boolean warning( int line, int col, String str ) {
//        if( fErrs != null ) fErrs.add(...);
        System.err.print( "Parse warning in line " );
        System.err.print( line );
        System.err.print( ", column " );
        System.err.print( col );
        System.err.print( ": " );
        System.err.println( str );
        return false;
    } //************************************************************************
    public boolean data( EDU.bmrb.sansj.DataItemNode node ) {
        if( fFirstTag == null ) fFirstTag = node.getName();
        if( node.getName().equals( fFirstTag ) ) {
if( DEBUG ) System.err.println( "Add row " + node.getValue() );
            fTbl.addRow();
            fRow++;
        }
        fTbl.put( fRow, node.getName(), node.getValue() );
        return false;
    } //************************************************************************
    public boolean comment(int param, String str) {
        return false;
    }
    public void endData(int param, String str) {
    }
    public boolean endLoop(int param) {
        return false;
    }
    public boolean endSaveFrame(int param, String str) {
        return false;
    }
    public boolean startData(int param, String str) {
        return false;
    }
    public boolean startLoop(int param) {
        return false;
    }
    public boolean startSaveFrame(int param, String str) {
        return false;
    }
}
