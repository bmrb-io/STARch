/*
 * Dictionary.java
 *
 * Created on May 23, 2005, 5:52 PM
 *
 * This software is copyright (c) 2005 Board of Regents, University of Wisconsin.
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
 * Wrapper interface to NMR-STAR dictionary.
 
 * @author  dmaziuk
 */
public class Dictionary {
    private final static boolean DEBUG = false;
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
//******************************************************************************
    /** Creates a new instance of Dictionary */
    public Dictionary() {
    } //************************************************************************
    /** Retrieves list of tags based on tag category.
     * @param props properties
     * @param category tag category
     * @return array of tag names or null
     * @throws ClassNotFoundException
     * @throws java.sql.SQLException
     */
    public static final String [] getLoopTags( java.util.Properties props,
    String category ) throws ClassNotFoundException, java.sql.SQLException {
// connect to DB
        Class.forName( props.getProperty( JDBC_DRIVER ) );
        String url = props.getProperty( JDBC_URL );
        if( url == null || url.length() < 1 )
            throw new NullPointerException( "Error in properties file: no DB URL" );
        String usr = props.getProperty( JDBC_USER );
        if( usr == null || usr.length() < 1 )
            throw new NullPointerException( "Error in properties file: no DB URL" );
        java.sql.Connection conn = java.sql.DriverManager.getConnection( url, usr,
        props.getProperty( JDBC_PASSWD ) );
// fetch tag list
        java.sql.Statement query = conn.createStatement(
        java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT TAGNAME,SEQ FROM TAGS " +
        "WHERE TAGNAME LIKE '" + category + "%' AND DELETEFLAG<>'Y' ORDER BY SEQ" );
        java.util.ArrayList tags = new java.util.ArrayList();
        while( rs.next() )
            tags.add( rs.getString( 1 ) );
        rs.close();
        query.close();
        conn.close();
        conn = null;
        if( tags.size() < 1 ) return null;
        String [] rc = new String[tags.size()];
        for( int i = 0; i < rc.length; i++ ) rc[i] = (String) tags.get( i );
        return rc;
    } //************************************************************************
    /** Main method
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    } //************************************************************************
}
