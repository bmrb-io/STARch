/*
 * Copyright (c) 2006 Board of Regents University of Wisconsin.
 * All Rights Reserved.
 */
package edu.bmrb.starch;

/**
 * NMR-STAR v.3 loop parser.
 *
 * This parser ignores comments and throws warnings for any tokens other than
 * "loop_". The loops are parsed the same way as sans Parser2 does except loop
 * count errors trigger error, not warning callback.
 *
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Aug 28, 2006
 * Time: 2:23:32 PM
 *
 * $Id$
 */

public class Loop3Parser {
    private static final boolean DEBUG = false;
    /** scanner error. */
    public static final String ERR_LEXER = "Scanner error: ";
    /** scanner warning. */
    public static final String WARN_LEXER = "Scanner warning: ";
    /** illegal token. */
    public static final String ERR_TOKEN = "Unexpected token: ";
    /** EOF. */
    public static final String WARN_EOF = "Premature end of input";
    /** No tags. */
    public static final String ERR_NOTAGS = "Loop with no tags";
    /** No values. */
    public static final String ERR_NOVALS = "Loop with no values";
    /** Loop count error. */
    public static final String ERR_LOOPCNT = "Loop count error";
    /** Value expected. */
    public static final String ERR_NOTVAL = "Value expected: ";
    /** scanner. */
    private edu.bmrb.sans.STARLexer fLex;
    /** content handler. */
    private edu.bmrb.sans.ContentHandler2 fCh;
    /** error handler. */
    private edu.bmrb.sans.ErrorHandler fEh;
//*******************************************************************************
    /**
     * Creates new Loop3Parser.
     * @param lex scanner
     * @param ch content handler
     * @param eh error handler
     */
    public Loop3Parser( edu.bmrb.sans.STARLexer lex,
                        edu.bmrb.sans.ContentHandler2 ch,
                        edu.bmrb.sans.ErrorHandler eh ) {
        fLex = lex;
        fCh = ch;
        fEh = eh;
    } //*************************************************************************
    /**
     * Parses input.
     */
    public void parse() {
        if( (fLex == null) || (fCh == null) || (fEh == null) )
            throw new NullPointerException( "Parser not initialized" );
        edu.bmrb.sans.STARLexer.Types tok;
        try {
            do {
                tok = fLex.yylex();
                switch( tok ) {
                    case ERROR :
                        fEh.fatalError( fLex.getLine(), fLex.getColumn(), ERR_LEXER + fLex.yytext() );
                        return;
                    case WARNING :
                        if( fEh.warning( fLex.getLine(), fLex.getColumn(), WARN_LEXER + fLex.yytext() ) )
                            return;
                        break;
                    case COMMENT: // ignore
                        break;
                    case LOOPSTART :
                        if( fCh.startLoop( fLex.getLine() ) ) return;
                        parseLoop();
                        return;
                    default :
                        if( fEh.warning( fLex.getLine(), fLex.getColumn(), ERR_TOKEN + fLex.yytext() ) )
                            return;
                }
            } while( tok != edu.bmrb.sans.STARLexer.Types.EOF );
        }
        catch( java.io.IOException e ) {
            fEh.fatalError( fLex.getLine(), fLex.getColumn(), e.getMessage() );
            System.err.println( e );
            e.printStackTrace();
        }
    } //*************************************************************************

    /**
     * Parses loop.
     * @return true to stop parsing.
     * @throws java.io.IOException re-thrown from the scanner.
     */
    private boolean parseLoop() throws java.io.IOException {
        edu.bmrb.sans.STARLexer.Types tok;
        int numtags = 0;
        int numvals = 0;
        int loopcol = 0;
        int lastline = -1;
        int wrongline = -1;
        int wrongcol = -1;
        boolean parsing_tags = true;
        String val;
        do {
            tok = fLex.yylex();
            switch( tok ) {
                case ERROR :
                    fEh.fatalError( fLex.getLine(), fLex.getColumn(), ERR_LEXER + fLex.yytext() );
                    return true;
                case WARNING :
                    if( fEh.warning( fLex.getLine(), fLex.getColumn(), WARN_LEXER + fLex.yytext() ) )
                        return true;
                    break;
                case COMMENT: // ignore
                    break;
                case EOF :
                    if( fEh.warning( fLex.getLine(), fLex.getColumn(), WARN_EOF ) )
                        return true;
                    // otherwise proceed to end-of-loop
                case STOP :
                    if( numtags < 1 ) { // no tags
                        fEh.fatalError( fLex.getLine(), fLex.getColumn(), ERR_NOTAGS );
                        return true;
                    }
                    if( numvals < 1 ) { // no values
                        fEh.fatalError( fLex.getLine(), fLex.getColumn(), ERR_NOVALS );
                        return true;
                    }
                    if( (numvals % numtags) != 0 ) { // loop count error
                        if( wrongline < 0 ) wrongline = fLex.getLine();
                        if( wrongcol < 0 ) wrongcol = 0;
                        if( fEh.error( wrongline, wrongcol, ERR_LOOPCNT ) )
                            return true;
                    }
                    return fCh.endLoop( fLex.getLine() );
                case TAGNAME :
                    if( ! parsing_tags ) {
                        fEh.fatalError( fLex.getLine(), fLex.getColumn(), ERR_NOTVAL + fLex.getText() );
                        return true;
                    }
                    numtags++;
                    if( fCh.tag( fLex.getLine(), fLex.getText() ) )
                        return true;
                    break;
                case DVNSINGLE :
                case DVNDOUBLE :
                case DVNSEMICOLON :
                case DVNFRAMECODE :
                case DVNNON :
                    if( parsing_tags ) parsing_tags = false;
                    val = fLex.getText();
                    if( tok == edu.bmrb.sans.STARLexer.Types.DVNSEMICOLON ) { // strip leading \n
                        if( val.indexOf( System.getProperty( "line.separator" ) ) == 0 )
                            val = val.substring( System.getProperty( "line.separator" ).length() );
                    }
                    numvals++;
                    loopcol++;
                    if( loopcol == numtags ) { // new loop row
// This saves the last line# where new loop row started on new line -- that is
// where loop count was still OK.
                        if( lastline < fLex.getLine() ) {
                            if( wrongline < 0 ) {
                                wrongline = fLex.getLine();
                                wrongcol = fLex.getColumn();
                            }
                            lastline = fLex.getLine();
                        }
                        loopcol = 0;
                    }
if( DEBUG ) System.err.printf( "* value: %s\n", val );
                    if( fCh.value( fLex.getLine(), val, tok ) )
                        return true;
                    break;
                default :
                    if( fEh.error( fLex.getLine(), fLex.getColumn(), ERR_TOKEN + fLex.yytext() ) )
                        return true;
            }
        } while( tok != edu.bmrb.sans.STARLexer.Types.EOF );
        return false;
    } //*************************************************************************
} //*****************************************************************************
