/*
 * Star3Reader.java
 *
 * Created on December 8, 2004, 7:10 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/Star3Reader.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/10/20 22:20:20 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Star3Reader.java,v $
 * Revision 1.2  2005/10/20 22:20:20  dmaziuk
 * typo, chmod fix
 *
 * Revision 1.1  2005/04/13 17:37:07  dmaziuk
 * *** empty log message ***
 *
 * Revision 1.1  2004/12/15 20:55:51  dmaziuk
 * new classes
 * */

package EDU.bmrb.starch;

/**
 * Reads chemical shift loop in NMR-STAR 3.0 format.
 *
 * @author  dmaziuk
 */
public class Star3Reader implements Reader, EDU.bmrb.sansj.ContentHandler,
EDU.bmrb.sansj.ErrorHandler {
    private final static boolean DEBUG = false;
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
    /** error flag */
    private boolean fOk = true;
    /** error message */
    private String fMessage = null;
    /** true if there's no sequence code in the row. */
    private boolean fHasSeq = true;
//******************************************************************************
    /** Creates a new instance of Star3Reader.
     * @param lt LoopTable
     * @param errs error list
     */
    public Star3Reader( LoopTable lt, ErrorList errs ) {
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
        for( i = 0; i < LoopTable.DB_COLS.length; i++ ) {
            fBuf.append( LoopTable.DB_COLS[i] );
            if( i != (LoopTable.DB_COLS.length - 1) ) fBuf.append( ',' );
        }
        fBuf.append( ") VALUES (" );
        for( i = 0; i < LoopTable.DB_COLS.length; i++ ) {
            fBuf.append( '?' );
            if( i != (LoopTable.DB_COLS.length - 1) ) fBuf.append( ',' );
        }
        fBuf.append( ')' );
if( DEBUG ) System.err.println( fBuf );
        fMessage = null;
        try {
            fInsertStat = fLt.getConnection().prepareStatement( fBuf.toString() );
        }
        catch( java.sql.SQLException e ) {
            if( fErrs != null ) fErrs.addError( 0, "Cannot prepare statement" );
            System.err.println( "Cannot prepare statement" );
            System.err.println( e );
            e.printStackTrace();
            fMessage = "DB error: cannot prepare statement";
            fOk = false;
            return;
        }
        EDU.bmrb.sansj.STARLexer lex = new EDU.bmrb.sansj.STARLexer( in );
        EDU.bmrb.sansj.LoopParser p = new EDU.bmrb.sansj.LoopParser( lex, this, this );
        p.parse();
        try {
            fInsertStat.getConnection().commit();
            fInsertStat.close();
        }
        catch( java.sql.SQLException e ) {
            if( fErrs != null ) fErrs.addError( 0, "Cannot commit work" );
            System.err.println( "Cannot commit" );
            System.err.println( e );
            e.printStackTrace();
            fOk = false;
            fMessage = "DB error: cannot commit work: " + e.getMessage();
        }
    } //************************************************************************
    /** Inserts row into database.
     * Call after populating the row with add()s.
     * @throws java.sql.SQLException
     */
    protected void insertRow() throws java.sql.SQLException {
if( DEBUG ) System.err.println( fInsertStat );
        fInsertStat.executeUpdate();
	fHasSeq = true;
    } //************************************************************************
    /** Returns error message.
     * @return error message or null.
     */
    public String getMessage() {
        return fMessage;
    } //************************************************************************
    /** Main method -- testing.
     * 1st argument, if present, is input file name. Otherwise reads stdin.
     * Dumps database table in CSV format to stdout when finished.
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        LoopTable t = new LoopTable();
        ErrorList errs = new ErrorList();
        Star3Reader r = new Star3Reader( t, errs );
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
        fMessage = "Parse error in line " + param + ": " + str;
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
            fOk = false;
            fMessage = "Loop count error in line " + param + ": " + str;
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
// this uses set() in table interface. Unfortunately, it takes 18 seconds where
// the other data() takes 4.
    public boolean data_new(EDU.bmrb.sansj.DataItemNode node) {
        if( ! fInLoop ) return false; // ignore free tags
        try {
            if( fFirstTag == null ) fFirstTag = node.getName();
            else if( fFirstTag.equals( node.getName() ) ) fRowNum++;
            int i;
            for( i = 0; i < LoopTable.LOOP_TAGS3.length; i++ )
                if( LoopTable.LOOP_TAGS3[i].equals( node.getName() ) ) {
                    fLt.setById( fRowNum + 1, i, node.getValue() );
                    break;
                }
        }
        catch( Exception e ) {
            System.err.println( "Cannot add " + node.getName() + ":" + node.getValue() );
            System.err.println( e );
            e.printStackTrace();
        }
        return false;
    } //************************************************************************
    /** Tag/value pair.
     * @param node tag/value pair
     * @return false to continue parsing
     */
    public boolean data(EDU.bmrb.sansj.DataItemNode node) {
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
            int i, seq;
            float val;
            for( i = 0; i < LoopTable.LOOP_TAGS3.length; i++ )
                if( node.getName().equals( LoopTable.LOOP_TAGS3[i] ) ) break;
            if( i == LoopTable.LOOP_TAGS3.length ) 
                throw new NullPointerException( "Invalid tag: " + node.getName() );
            switch( i ) {
                case LoopTable.ID_COL : 
                    fInsertStat.setInt( i + 1, fRowNum );
                    break;
                case LoopTable.SEQ_COL :
                case 3 :
                    try {
                        seq = Integer.parseInt( node.getValue() );
                        fInsertStat.setInt( 4, seq );
                        fInsertStat.setInt( 5, seq );
                    }
                    catch( NumberFormatException ne ) {
                        if( fErrs != null ) fErrs.addError( node.getValueLine(),
                        "Sequence code is not a number: " + node.getValue() );
                        System.err.print( "Sequence code is not a number at line " );
                        System.err.print( node.getValueLine() );
                        System.err.print( ": " );
                        System.err.println( node.getValue() );
                        fMessage = "Sequence code is not a number at line " + node.getValueLine();
//                        fOk = false;
                        fHasSeq = false;
//                        return true;
                    }
                    break;
                case 9 : 
                    try {
                        seq = Integer.parseInt( node.getValue() );
                        fInsertStat.setInt( 10, seq );
                    }
                    catch( NumberFormatException ne ) {
                        if( fErrs != null ) fErrs.addError( node.getValueLine(),
                        "Author sequence code is not a number: " + node.getValue() );
                        System.err.print( "Author sequence code is not a number at line " );
                        System.err.print( node.getValueLine() );
                        System.err.print( ": " );
                        System.err.println( node.getValue() );
			if( ! fHasSeq ) return true;
                    }
                    break;
                case 12 :
                    if( (node.getValue() == null) || (node.getValue().length() == 0)
                    || node.getValue().equals( "." ) )
                        fInsertStat.setNull( 13, java.sql.Types.VARCHAR );
                    else try {
                        val = Float.parseFloat( node.getValue() );
                        fInsertStat.setString( 13, node.getValue() );
                    }
                    catch( NumberFormatException ne ) {
                        if( fErrs != null ) fErrs.addError( node.getValueLine(),
                        "Shift value is not a number: " + node.getValue() );
                        System.err.print( "Shift value is not a number at line " );
                        System.err.print( node.getValueLine() );
                        System.err.print( ": " );
                        System.err.println( node.getValue() );
                        fInsertStat.setNull( 13, java.sql.Types.FLOAT );
                    }
                    break;
                case 13 :
                    if( (node.getValue() == null) || (node.getValue().length() == 0)
                    || node.getValue().equals( "." ) )
                        fInsertStat.setNull( 14, java.sql.Types.VARCHAR );
                    else try {
                        val = Float.parseFloat( node.getValue() );
                        fInsertStat.setString( 14, node.getValue() );
                    }
                    catch( NumberFormatException ne ) {
                        if( fErrs != null ) fErrs.addError( node.getValueLine(),
                        "Shift error is not a number: " + node.getValue() );
                        System.err.print( "Shift error is not a number at line " );
                        System.err.print( node.getValueLine() );
                        System.err.print( ": " );
                        System.err.println( node.getValue() );
                        fInsertStat.setNull( 14, java.sql.Types.FLOAT );
                    }
                    break;
                case 15 :
                    if( (node.getValue() == null) || (node.getValue().length() == 0)
                    || node.getValue().equals( "." ) )
                        fInsertStat.setNull( 16, java.sql.Types.VARCHAR );
                    else try {
                        seq = Integer.parseInt( node.getValue() );
                        fInsertStat.setInt( 16, seq );
                    }
                    catch( NumberFormatException ne ) {
                        if( fErrs != null ) fErrs.addError( node.getValueLine(),
                        "Ambiguity code is not a number: " + node.getValue() );
                        System.err.print( "Ambiguity is not a number at line " );
                        System.err.print( node.getValueLine() );
                        System.err.print( ": " );
                        System.err.println( node.getValue() );
                        fInsertStat.setNull( 16, java.sql.Types.INTEGER );
                    }
                    break;
                default :
                    if( (node.getValue() == null) || (node.getValue().length() == 0)
                    || node.getValue().equals( "." ) )
                        fInsertStat.setNull( i + 1, java.sql.Types.VARCHAR );
                    else fInsertStat.setString( i + 1, node.getValue() );
            } // endswitch
/*
            
            if( i == LoopTable.ID_COL ) fInsertStat.setInt( i + 1, fRowNum );
            else {
                if( (node.getValue() == null) || (node.getValue().length() == 0)
                || node.getValue().equals( "." ) )
                    fInsertStat.setNull( i + 1, java.sql.Types.VARCHAR );
                else fInsertStat.setString( i + 1, node.getValue() );
            }
 */
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
