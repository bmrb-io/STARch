package edu.bmrb.starch;

/**
 * Main for STARch.
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Nov 15, 2006
 * Time: 12:50:11 PM
 *
 * $Id$
 */

public class Main {
    private static final boolean DEBUG = false;
    /** JDBC driver class. */
    public static String PROPKEY_DBDRIVER = "DB.Driver";
    /** JDBC URL. */
    public static String PROPKEY_DBURL = "DB.Url";
    /** DB username. */
    public static String PROPKEY_DBUSER = "DB.User";
    /** DB password. */
    public static String PROPKEY_DBPASSWD = "DB.Password";
    /**
     * Main method
     * @param args command line parameters
     */
    public static void main( String [] args ) {
        try {
            gnu.getopt.Getopt g = new gnu.getopt.Getopt( "Main", args, "d:" );
            int opt;
            String cwd = null;
            while( (opt = g.getopt()) != -1 ) {
                switch( opt ) {
                    case 'd' : // work dir
                        cwd = g.getOptarg();
                        break;
                }
            }
// properties
            java.util.Properties props = new java.util.Properties();
            java.io.FileInputStream in = new java.io.FileInputStream( System.getProperty( "user.home" ) +
                "/validator.properties.devel" );
            props.load( in );
            in.close();
            if( cwd == null ) cwd = System.getProperty( "user.dir" );
// DB
            Class.forName( props.getProperty( PROPKEY_DBDRIVER ) );
            java.sql.Connection conn = java.sql.DriverManager.getConnection( props.getProperty( PROPKEY_DBURL ),
                                                                             props.getProperty(  PROPKEY_DBUSER ),
                                                                             props.getProperty( PROPKEY_DBPASSWD ) );
// Main
            ErrorList errs = new ErrorList();
            Dictionary dict = new Dictionary( conn );
            Metadata meta = new Metadata( conn );
            TableModel model = new TableModel( conn, dict, meta, errs );
            Nomenmap nomenmap = new Nomenmap( model );
            edu.bmrb.starch.gui.Actions act = new edu.bmrb.starch.gui.Actions( model );
            edu.bmrb.starch.gui.MainFrame frame = new edu.bmrb.starch.gui.MainFrame( model, nomenmap, errs, act );
            act.setParentComponent( frame );
            frame.setCwd( cwd );
            frame.setVisible( true );
        }
        catch( Exception e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //*************************************************************************
}
