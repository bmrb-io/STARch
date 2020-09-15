package edu.bmrb.starch;

/**
 * Reads NMR-STAR loop into DB table.
 * This code will load the first loop form NMR-STAR file into the database.
 * The rest of the file
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Nov 13, 2006
 * Time: 3:54:02 PM
 *
 * $Id$
 */

public class LoopReader
        implements edu.bmrb.sans.ErrorHandler, edu.bmrb.sans.ContentHandler2 {
    private static final boolean DEBUG = false;
    /* JDBC connection. */
    private java.sql.Connection fConn;
    /* Error list. */
    private ErrorList fErrs = null;
    /* Table name (tag category). */
    private String fTableName = null;
    /* loop tags. */
    private java.util.ArrayList<String> fLoopTags;
    /* database columns. */
    private java.util.ArrayList<String> fDbCols;
    /* tag to column map */
    private int [] fColMap = null;
    /* column types. */
    private int [] fColTypes = null;
    /* SQL statement */
    private java.sql.PreparedStatement fInsStat = null;
    /* parse error flag. */
    private boolean fParsedOk;
    /* parsing tags flag. */
    private boolean fParsingTags = true;
    /* column counter. */
    private int fCol = -1;
    /* row counter. */
    private int fRow = 1;
//*******************************************************************************
    /**
     * Constructor.
     * @param conn JDBC connection
     * @param errs error list
     */
    public LoopReader( java.sql.Connection conn, ErrorList errs ) {
        fParsedOk = true;
        fConn = conn;
        fErrs = errs;
        fLoopTags = new java.util.ArrayList<String>();
        fDbCols = new java.util.ArrayList<String>();
    } //*************************************************************************
    /**
     * Returns false if there were any parse errors/warnings.
     * @return true or false
     */
    public boolean parsedOk() {
        return fParsedOk;
    } //*************************************************************************
    /**
     * Returns number of rows in table.
     * @return number of rows
     */
    public int getNumRows() {
        return fRow;
    } //*************************************************************************
    /**
     * Returns table name.
     * @return table name
     */
    public String getTableName() {
        return fTableName;
    } //*************************************************************************
    /**
     * Parses input.
     * @param in input stream
     * @return false if there were parse errors/warnings.
     * @throws java.sql.SQLException
     */
    public boolean parse( java.io.Reader in ) throws java.sql.SQLException {
        if( (fConn == null) || fConn.isClosed() )
            throw new NullPointerException( "Not connected to database" );
        edu.bmrb.sans.STARLexer lex = new edu.bmrb.sans.STARLexer( in );
        Loop3Parser p = new edu.bmrb.starch.Loop3Parser( lex, this, this );
if( DEBUG ) System.err.println( " - parse" );
        p.parse();
        if( ! fConn.getAutoCommit() ) fConn.commit();
        return fParsedOk;
    } //*************************************************************************
    /**
     * Fatal parser error.
     * @param line line number
     * @param col column number
     * @param msg error message
     */
    public void fatalError( int line, int col, String msg ) {
        if( fErrs != null )
            fErrs.add( new Error( Error.Severity.CRIT, fRow, "", "", "", msg ) );
        System.err.printf( "Fatal parser error in line %d, col %d: %s\n", line, col, msg );
        fParsedOk = false;
    } //*************************************************************************
    /**
     * Parser error.
     * @param line line number
     * @param col column number
     * @param msg error message
     * @return true to stop parsing, false to continue
     */
    public boolean error( int line, int col, String msg ) {
        if( fErrs != null )
            fErrs.add( new Error( Error.Severity.CRIT, fRow, "", "", "", msg ) );
        System.err.printf( "Parser error in line %d, col %d: %s\n", line, col, msg );
        fParsedOk = false;
        return false;
    } //*************************************************************************
    /**
     * Parser warning.
     * @param line line number
     * @param col column number
     * @param msg warning message
     * @return true to stop parsing, false to continue
     */
    public boolean warning( int line, int col, String msg ) {
        System.err.printf( "Parser warning in line %d, col %d: %s\n", line, col, msg );
// ignore tokens outside of 1st loop, flag other warnings
        if( msg.indexOf( Loop3Parser.ERR_TOKEN ) < 0 ) {
            if( fErrs != null )
                fErrs.add( new Error( Error.Severity.CRIT, fRow, "", "", "", msg ) );
            fParsedOk = false;
        }
        return false;
    } //*************************************************************************
    /**
     * NMR-STAR tag.
     * Creates table to hold the data.
     * @param line line number
     * @param tag tag name
     * @return false to continue parsing
     */
    public boolean tag( int line, String tag ) {
if( DEBUG ) System.err.printf( " - tag %s\n", tag );
        fLoopTags.add( tag );
        return false;
    } //*************************************************************************
    /**
     * NMR-STAR value
     * @param line line number
     * @param val value
     * @param type delimiter
     * @return false to continue parsing
     */
    public boolean value( int line, String val,
                          edu.bmrb.sans.STARLexer.Types type ) {
        try {
            if( fParsingTags ) {
                if( ! createTable() ) return true;
                fParsingTags = false;
            }
            fCol++;
// new row
            if( fCol == fLoopTags.size() ) {
                fInsStat.setInt( 1, fRow );
if( DEBUG ) System.err.println( fInsStat );
                fInsStat.executeUpdate();
                fCol = 0;
                fRow++;
            }
if( DEBUG ) System.err.printf( " - value %s in row %d, col %d (tag %s)\n", val, fRow, fCol,
                               fLoopTags.get( fCol ) );
            switch( fColTypes[fColMap[fCol]] ) {
                case java.sql.Types.INTEGER :
                    if( (val == null) || val.equals( "?" ) || val.equals( "." ) || val.equals( "@" ) )
                        fInsStat.setNull( fCol + 2, java.sql.Types.INTEGER );
                    else
                        try { fInsStat.setInt( fCol + 2, Integer.parseInt( val ) ); }
                        catch( NumberFormatException nfe ) {
                            fInsStat.setNull( fCol + 2, java.sql.Types.INTEGER );
                            if( fErrs != null )
                                fErrs.add( new Error( Error.Severity.WARN, fRow, val, "", "",
                                                      "Not a number: " + val ) );
                            System.err.printf( "Not a number: %s in row %d\n", val, fRow );
                        }
                    break;
                case java.sql.Types.FLOAT :
                    if( (val == null) || val.equals( "?" ) || val.equals( "." ) || val.equals( "@" ) )
                        fInsStat.setNull( fCol + 2, java.sql.Types.VARCHAR );
                    else {
                        fInsStat.setString( fCol + 2, val );
                        try { Float.parseFloat( val ); }
                        catch( NumberFormatException nfe ) {
                            if( fErrs != null )
                                fErrs.add( new Error( Error.Severity.WARN, fRow, val, "", "",
                                                      "Not a number: " + val ) );
                            System.err.printf( "Not a number: %s in row %d\n", val, fRow );
                        }
                    }
                    break;
                default :
                    if( (val == null) || val.equals( "?" ) || val.equals( "." ) || val.equals( "@" ) )
                        fInsStat.setNull( fCol + 2, java.sql.Types.VARCHAR );
                    else fInsStat.setString( fCol + 2, val );
            }
            return false;
        }
        catch( java.sql.SQLException e ) {
            if( fErrs != null )
                fErrs.add( new Error( Error.Severity.CRIT, 0, "", "", "",
                                      "JDBC exception, parse failed" ) );
            System.err.println( "DB exception, terminating" );
            System.err.println( e );
            e.printStackTrace();
            fParsedOk = false;
            return true;
        }
    } //*************************************************************************
    /**
     * End of loop.
     * Insert last row
     * @param line line number
     * @return false to continue parsing
     */
    public boolean endLoop( int line ) {
        try {
            fInsStat.setInt( 1, fRow );
if( DEBUG ) System.err.println( fInsStat );
            fInsStat.executeUpdate();
            if( ! fConn.getAutoCommit() ) fConn.commit();
            fInsStat.close();
            return false;
        }
        catch( java.sql.SQLException e ) {
            if( fErrs != null )
                fErrs.add( new Error( Error.Severity.CRIT, 0, "", "", "",
                                      "JDBC exception (end of loop), parse failed" ) );
            System.err.println( "DB exception (end of loop), terminating" );
            System.err.println( e );
            e.printStackTrace();
            fParsedOk = false;
            return true;
        }
    } //*************************************************************************
    /**
     * Create DB table to hold loop data.
     * @return false on error (DB exception)
     */
    private boolean createTable() {
        assert( fLoopTags.size() > 0 );
        try {
            fColMap = new int [fLoopTags.size()];
            java.util.ArrayList<Integer> coltypes = new java.util.ArrayList<Integer>();
            int i;
            for( i = 0; i < fColMap.length; i++ ) fColMap[i] = -1;
            String tag = fLoopTags.get( 0 );
            String tagcat = Dictionary.getTagCategory( tag );
            if( tagcat == null ) {
//todo ??
                throw new NullPointerException( "No tag category for " + tag );
            }
            StringBuilder buf = new StringBuilder();
            java.sql.Statement query = fConn.createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                              java.sql.ResultSet.CONCUR_READ_ONLY );
// fetch list of column names and types from the dictionary
// validation dictionary does not have dbType column, use "validator type" instead
/*
            buf.append( "SELECT SEQ,TAGNAME,VALTYPE,VALSIZE,TAGCAT FROM TAGS " +
                    "WHERE TAGCAT=(SELECT TAGCAT FROM TAGS WHERE TAGNAME='" );
            buf.append( tag );
            buf.append( "') AND SFIDFLAG<>'Y' ORDER BY SEQ" );
*/
            buf.append( "SELECT SEQ,TAGNAME,VALTYPE,VALSIZE,TAGCAT FROM TAGS " +
                    "WHERE TAGCAT='" );
            buf.append( tagcat );
            buf.append( "' AND SFIDFLAG<>'Y' ORDER BY SEQ" );

if( DEBUG ) System.err.println( buf );
            int pos;
            int col = 0;
            java.sql.ResultSet rs = query.executeQuery( buf.toString() );
            buf.setLength( 0 );
            while( rs.next() ) {
// table name
                if( fTableName == null ) {
                    fTableName = rs.getString( 5 );
                    buf.append( "CREATE TABLE " );
                    buf.append( fTableName );
                    buf.append( " (ROW INTEGER PRIMARY KEY" );
                }
// column names
                pos = rs.getString( 2 ).indexOf( '.' );
                tag = rs.getString( 2 ).substring( pos + 1 );
if( DEBUG ) System.err.printf( "Adding column %s to table %s\n", tag, fTableName );
                fDbCols.add( tag );
// tag->col map
                for( i = 0; i < fLoopTags.size(); i++ ) {
                    if( fLoopTags.get( i ).equals( rs.getString( 2 ) ) ) {
                        fColMap[i] = col;
                        break;
                    }
                }
// SQL
                buf.append( ", \"" );
                buf.append( tag );
                buf.append( "\" " );
                if( rs.getString( 3 ).equals( "INTEGER" ) ) {
                    buf.append( rs.getString( 3 ) );
                    coltypes.add( java.sql.Types.INTEGER );
                }
                else if( rs.getString( 3 ).equals( "FLOAT" ) ) {
// store as strings to preserve trailing zeros
                    buf.append( "VARCHAR(30)" );
                    coltypes.add( java.sql.Types.FLOAT );
                }
                else {
                    buf.append( "VARCHAR(" );
// "TEXT" fields -- can be up to 2GB in Postgres.
                    if( rs.getInt( 4 ) <= 0 ) buf.append( "4000" );
                    buf.append( rs.getInt( 4 ) );
                    buf.append( ')' );
                    coltypes.add( java.sql.Types.VARCHAR );
                }
                col++;
            }
            rs.close();
            query.close();
            buf.append( ')' );
// column types
            fColTypes = new int [coltypes.size()];
            for( i = 0; i < fColTypes.length; i++ ) fColTypes[i] = coltypes.get( i );
            coltypes.clear();
// create table
            if( fTableName == null ) {
                System.err.println( "No tag category" );
                if( fErrs != null )
                    fErrs.add( new Error( Error.Severity.CRIT, fRow, "", "", "",
                                          "Missing tag category" ) );
                fParsedOk = false;
                return false;
            }
            java.sql.Statement stat = fConn.createStatement();
if( DEBUG ) System.err.printf( "DROP TABLE %s\n", fTableName );
// another way of doing this is to fetch list of tables from database metadata
// and look for fTablename in there.
            try { stat.executeUpdate( "DROP TABLE " + fTableName ); }
            catch( java.sql.SQLException e ) { // table may not exist
//FIXME: this may be postgres-specific: 42P01 is SQL-99 for UNDEFINED TABLE.
// Other engines may return XOPEN codes (or perhaps SQL-99 "INVALID NAME")
                if( ! e.getSQLState().equals( "42P01" ) ) {
                    throw e;
                }
            }
if( DEBUG ) System.err.println( buf );
            stat.executeUpdate( buf.toString() );
//            if( ! fConn.getAutoCommit() ) fConn.commit();
// initialize insert statement
            buf.setLength( 0 );
            buf.append( "INSERT INTO " );
            buf.append( fTableName );
            buf.append( " (ROW" );
            for( String s : fLoopTags ) {
                for( String c : fDbCols ) {
                    pos = s.indexOf( '.' );
                    if( s.substring( pos + 1 ).equals( c ) ) {
                        buf.append( ",\"" );
                        buf.append( c );
                        buf.append( '"' );
                        break;
                    }
                }
            }
            buf.append( ") VALUES (?" );
            for( i = 0; i < fLoopTags.size(); i++ ) buf.append( ",?" );
            buf.append( ')' );
if( DEBUG ) System.err.println( buf );
            fInsStat = fConn.prepareStatement( buf.toString() );
            fConn.setAutoCommit( false );
            buf.setLength( 0 );
            stat.close();
if( DEBUG ) {
    System.err.print( "Tags:  " );
    for( String s: fLoopTags ) System.err.printf( "%32s", s );
    System.err.print(  "\nTypes: " );
    for( i = 0; i < fColTypes.length; i++ ) System.err.printf( "%32d", fColTypes[i] );
    System.err.print(  "\nMap:   " );
    for( i = 0; i < fColMap.length; i++ ) System.err.printf( "%32d", fColMap[i] );
    System.err.print( "\nCols:  " );
    for( String s: fDbCols ) System.err.printf( "%32s", s );
    System.err.println();
}
            return true;
        }
        catch( java.sql.SQLException e ) {
            if( fErrs != null )
                fErrs.add( new Error( Error.Severity.CRIT, fRow, "", "", "",
                                      "JDBC exception (create table), parse failed" ) );
            System.err.println( "DB exception, terminating" );
            System.err.println( e );
            e.printStackTrace();
            fParsedOk = false;
            return false;
        }
    } //*************************************************************************
    public boolean comment( int i, String string ) {
        return false;
    }
    public boolean startData( int i, String string ) {
        return false;
    }
    public void endData( int i, String string ) {
    }
    public boolean startLoop( int i ) {
        return false;
    }
    public boolean startSaveFrame( int i, String string ) {
        return false;
    }
    public boolean endSaveFrame( int i, String string ) {
        return false;
    }
}
