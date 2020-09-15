/*
 * Star2Reader.java
 *
 * Created on December 14, 2004, 5:56 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Star2Reader.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2004/12/15 20:55:50 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Star2Reader.java,v $
 * Revision 1.1  2004/12/15 20:55:50  dmaziuk
 * new classes
 * */

package EDU.bmrb.starch;

/**
 * Reads chemical shift loop in NMR-STAR 2.1 format.
 *
 * @author  dmaziuk
 */
public class Star2Reader implements Reader, EDU.bmrb.sansj.ContentHandler,
EDU.bmrb.sansj.ErrorHandler {
    private final static boolean DEBUG = false;
    /** DB column names */
    public static final String [] DB_COLS = {
        "ID",
        "COMPIDXID",
        "SEQID",
        "ASEQCODE",
        "ACOMPCODE",
        "AATMCODE",
        "ATOMTYPE",
        "SVALUE",
        "SERROR",
        "SAMBICODE"
    };
    /** ID column (NMR-STAR 2.1) */
    public static final int ID_COL = 0;
    /** residue sequence column (NMR-STAR 2.1) */
    public static final int SEQ_COL = 1;
    /** DB storage */
    private LoopTable fLt = null;
    /** error list */
    private ErrorList fErrs = null;
    /** insert statement */
    private java.sql.PreparedStatement fInsertStat = null;
    /** buffer */
    private StringBuffer fBuf = null;
    /** true when parsing a loop */
    private boolean fInLoop = false;
    /** first loop tag */
    private String fFirstTag = null;
    /** row number */
    private int fRowNum = 0;
    /** parse result flag */
    private boolean fOk = true;
//******************************************************************************
    /** Creates a new instance of Star2Reader.
     * @param lt LoopTable
     * @param errs error list
     */
    public Star2Reader( LoopTable lt, ErrorList errs ) {
        fLt = lt;
        fErrs = errs;
        fBuf = new StringBuffer();
    } //************************************************************************
    /** Returns parse result: success or failure.
     * @return true or false
     */
    public boolean parsedOk() {
        return fOk;
    } //************************************************************************
      /** Parses input.
     * @param in input stream
     */
    public void parse( java.io.BufferedReader in ) {
        int i;
        fBuf.setLength( 0 );
        fBuf.append( "INSERT INTO LOOP (" );
        for( i = 0; i < DB_COLS.length; i++ ) {
            fBuf.append( DB_COLS[i] );
            if( i != (DB_COLS.length - 1) ) fBuf.append( ',' );
        }
        fBuf.append( ") VALUES (" );
        for( i = 0; i < DB_COLS.length; i++ ) {
            fBuf.append( '?' );
            if( i != (DB_COLS.length - 1) ) fBuf.append( ',' );
        }
        fBuf.append( ')' );
//System.err.println( fBuf );
        try {
            fInsertStat = fLt.getConnection().prepareStatement( fBuf.toString() );
        }
        catch( java.sql.SQLException e ) {
            if( fErrs != null ) fErrs.addError( 0, "Cannot prepare statement" );
            System.err.println( "Cannot prepare statement" );
            System.err.println( e );
            e.printStackTrace();
            return;
        }
        EDU.bmrb.sansj.STARLexer lex = new EDU.bmrb.sansj.STARLexer( in );
        EDU.bmrb.sansj.LoopParser p = new EDU.bmrb.sansj.LoopParser( lex, this, this );
        p.parse();
        try {
            fInsertStat.getConnection().commit();
            fInsertStat.close();
            java.sql.Statement stat = fLt.getConnection().createStatement();
            stat.executeUpdate( "UPDATE LOOP SET COMPIDXID=SEQID" );
            stat.getConnection().commit();
            stat.close();
        }
        catch( java.sql.SQLException e ) {
            if( fErrs != null ) fErrs.addError( 0, "Cannot commit work" );
            System.err.println( "Cannot commit" );
            System.err.println( e );
            e.printStackTrace();
            fOk = false;
        }
    } //************************************************************************
    /** Inserts row into database.
     * Call after populating the row with add()s.
     * @throws java.sql.SQLException
     */
    protected void insertRow() throws java.sql.SQLException {
        fInsertStat.executeUpdate();
    } //************************************************************************
    /** Main method -- testing.
     * 1st argument, if present, is input file name. Otherwise reads stdin.
     * Dumps database table in CSV format to stdout when finished.
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        LoopTable t = new LoopTable();
        ErrorList errs = new ErrorList();
        Star2Reader r = new Star2Reader( t, errs );
        try {
            t.connect();
            t.createTable();
            java.io.BufferedReader in;
            if( args.length < 1 ) 
                in = new java.io.BufferedReader( new java.io.InputStreamReader( 
                System.in, "ISO-8859-15" ) );
            else in = new java.io.BufferedReader( new java.io.InputStreamReader( 
            new java.io.FileInputStream( args[0] ), "ISO-8859-15" ) );
            r.parse( in );
            in.close();
            t.print( System.out );
            t.disconnect();
        }
        catch( Exception e ) {
            System.err.println( e.getMessage() );
            e.printStackTrace();
        }
    } //************************************************************************
//*********************** callbacks ********************************************
    /** Parse error.
     * @param param line number
     * @param param1 column number
     * @param str message
     */
    public void error(int param, int param1, String str) {
        if( fErrs != null ) fErrs.addError( param, "Parse error: " + str );
        System.err.print( "Parse error in line " );
        System.err.print( param );
        System.err.print( ", column " );
        System.err.print( param1 );
        System.err.print( ": " );
        System.err.println( str );
        fOk = false;
    } //************************************************************************
    /** Parser warning.
     * @param param line number
     * @param param1 column number
     * @param str message
     * @return false to continue parsing
     */
    public boolean warning(int param, int param1, String str) {
        if( fErrs != null ) fErrs.addWarning( param, "Parse warning: " + str );
        System.err.print( "Parser warning in line " );
        System.err.print( param );
        System.err.print( ", column " );
        System.err.print( param1 );
        System.err.print( ": " );
        System.err.println( str );
        if( str.trim().equals( "Loop count error" ) ) {
            System.err.println( "Terminating" );
            fOk = false;
            return true;
        }
        return false;
    } //************************************************************************
    /** NMR-STAR comment.
     * @param param line number
     * @param str comment text
     * @return false to continue parsing
     */
    public boolean comment(int param, String str) {
        return false; // strip all comments
    } //************************************************************************
    /** Start of data block.
     * @param param line number
     * @param str data block name
     * @return false to continue parsing
     */
    public boolean startData(int param, String str) {
        return false;
    } //************************************************************************
    /** End of data block.
     * @param param line number
     * @param str data block name
     */
    public void endData(int param, String str) {
    } //************************************************************************
    /** Start of saveframe.
     * @param param line number
     * @param str saveframe name
     * @return false to continue parsing
     */
    public boolean startSaveFrame(int param, String str) {
        return false;
    } //************************************************************************
    /** End of saveframe.
     * @param param line number
     * @param str saveframe name
     * @return false to continue parsing
     */
    public boolean endSaveFrame(int param, String str) {
        return false;
    } //************************************************************************
    /** Start of loop.
     * @param param line number
     * @return false to continue parsing
     */
    public boolean startLoop(int param) {
        fInLoop = true; // assume chemical shifts loop is the first loop in the file
        return false;
    } //************************************************************************
    /** End of loop.
     * @param param line number
     * @return true to stop the parser
     */
    public boolean endLoop(int param) {
        try { insertRow(); }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
        }
        return true;
    } //************************************************************************
    /** Tag/value pair.
     * @param node tag/value pair
     * @return false to continue parsing
     */
    public boolean data(EDU.bmrb.sansj.DataItemNode node ) {
        if( ! fInLoop ) return false; // ignore free tags
        if( fFirstTag == null ) fFirstTag = node.getName();
        else if( fFirstTag.equals( node.getName() ) ) {
            try { 
                fRowNum++;
                insertRow();
if( DEBUG ) System.err.println( fRowNum + " rows inserted" );
            }
            catch( java.sql.SQLException e ) {
                if( fErrs != null ) fErrs.addError( node.getValueLine(), "Cannot insert row" );
                System.err.println( "Cannot insert row" );
                System.err.println( fInsertStat );
                System.err.println( e );
                e.printStackTrace();
            }
        }
        try {
            int i;
            for( i = 0; i < LoopTable.LOOP_TAGS2.length; i++ )
                if( node.getName().equals( LoopTable.LOOP_TAGS2[i] ) ) break;
            if( i == LoopTable.LOOP_TAGS2.length ) 
                throw new NullPointerException( "Invalid tag: " + node.getName() );
            if( i == ID_COL ) fInsertStat.setInt( i + 1, fRowNum );
            else if( i == SEQ_COL ) { // _Residue_seq_code
                if( (node.getValue() == null) || (node.getValue().length() == 0)
                || node.getValue().equals( "." ) ) {
                    if( fErrs != null ) fErrs.addError( node.getValueLine(), "Missing residue sequence number" );
                    System.err.println( "Missing residue sequence number" );
                    fOk = false;
                    return true;
                }
                else {
                    fInsertStat.setString( 2, node.getValue() );
                    fInsertStat.setString( 3, node.getValue() );
                    fInsertStat.setString( 4, node.getValue() );
                }
            }
            else {
                if( (node.getValue() == null) || (node.getValue().length() == 0)
                || node.getValue().equals( "." ) )
                    fInsertStat.setNull( i + 3, java.sql.Types.VARCHAR );
                else fInsertStat.setString( i + 3, node.getValue() );
            }
        }
        catch( java.sql.SQLException se ) {
            System.err.println( "Cannot add " + node.getName() + ":" + node.getValue() );
            System.err.println( se );
            se.printStackTrace();
if( DEBUG ) System.err.println( fInsertStat );
        }
        return false;
    } //************************************************************************
}
