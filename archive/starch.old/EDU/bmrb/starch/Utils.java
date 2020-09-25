/*
 * Utils.java
 *
 * Created on December 18, 2002, 4:21 PM
 *
 * This software is copyright (c) 2002 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Utils.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2003/05/01 01:29:57 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Utils.java,v $
 * Revision 1.6  2003/05/01 01:29:57  dmaziuk
 * comment
 *
 * Revision 1.5  2003/05/01 01:28:10  dmaziuk
 * added float validation method
 *
 * Revision 1.4  2003/04/29 23:00:39  dmaziuk
 * documentation fix
 *
 * Revision 1.3  2003/02/07 22:54:06  dmaziuk
 * bugfix: a line with only whitespace crashed PIPP converter
 *
 * Revision 1.2  2003/01/06 22:45:57  dmaziuk
 * Bugfix release
 *
 * Revision 1.1  2002/12/30 22:22:39  dmaziuk
 * major rewrite of data structure
 *
 *
 */

package EDU.bmrb.starch;

/**
 * Useful methods for STARch.
 * <UL>
 *  <LI>readLine() method ignores blank lines, 
 *  <LI>toNMRSTARQuotedString() method wraps a string in appropriate quotes, if needed,
 *  <LI>toInteger() converts string to Integer, ignoring whitespace and brackets,
 *  <LI>toFloat() is similar to the above,
 *  <LI>floatValue(): similar to the above, returns a float (not Float) or NaN
 *  <LI>floatValueError() tries to deduce value error from the number of significant
 *      digits in the value
 * </UL>
 * @author  dmaziuk
 * @version 1
 */
public class Utils {

    /** Creates new Utils */
    public Utils() {
    } //*************************************************************************
    /** Returns true if argument is a floating-point number.
     * Iterates through characters in string and checks each. This method returns
     * true if input string contains optional + or -, followed by "inf" (case-insensitive)
     * or zero or more digits, followed by a dot. If there are no digits before 
     * the dot, there must be at least one after it. Otherwise, the dot may be 
     * followed by zero or more digits and an 'e' (case-insensitive). 'E' may be 
     * followed by optional + or -, and at least one digit. IOW,
     * [+-]? ((([0-9]*\.[0-9]+)|([0-9]+\.[0-9]*)) ([Ee][+-]?[0-9]+)?) | ([iI][nN][fF])
     * @param val input string
     * @return true or false
     */
    public static boolean isFloat( String val ) {
        if( (val == null) || val.equals( "" ) ) return false;
        boolean seen_dot = false;
        boolean seen_exp = false;
        boolean check_digit = false;
        boolean has_exp = false;
        int last_char = -1;
        int digit = 0;
        int exp = 1;
        int dot = 2;
        int sign = 3;
        int len = val.length();
        for( int i = 0; i < len; i++ ) {
            switch( val.charAt( i ) ) {
                case '+' : // legal at the start or after E
                case '-' :
                    if( check_digit ) return false;
                    if( (i != 0) && ! ((val.charAt(i - 1) == 'E') || (val.charAt(i - 1) == 'e')) ) return false;
                    last_char = sign;
                    break;
                case 'I' :
                case 'i' : // inf
                    if( (i + 2) >= len ) return false;
                    if( ((val.charAt( i + 1 ) == 'n') || (val.charAt( i + 1 ) == 'N'))
                    && ((val.charAt( i + 2 ) == 'f') || (val.charAt( i + 2 ) == 'F')) )
                        return true;
                case 'e' : // there can be only one
                case 'E' :
                    if( seen_exp ) return false;
                    if( check_digit ) return false;
                    seen_exp = true;
                    last_char = exp;
                    break;
                case '.' : // there can be only one
                    if( seen_dot ) return false;
                    if( last_char != digit ) {
                        if( (i + 1) >= len ) return false;
                        check_digit = true;
                    }
                    seen_dot = true;
                    last_char = dot;
                    break;
                case '0' :
                case '1' :
                case '2' :
                case '3' :
                case '4' :
                case '5' :
                case '6' :
                case '7' :
                case '8' :
                case '9' :
                    if( seen_exp ) has_exp = true;
                    check_digit = false;
                    last_char = digit;
                    break;
                default : 
                    return false;
            }
        } // endfor
        if( seen_exp && (!has_exp) ) return false;
        return true;
    } //*************************************************************************
    /** Converts String to Float.
     * Unlike Float.valueOf(), this method doesn't throw exceptions and can 
     * convert values surrounded by whitespace and/or brackets.
     * @param val String
     * @return Float value or null
     */
    public static Float toFloat( String val ) {
        if( val == null ) return null;
        Float tmpf = null;
        String tmps = ( val.replace( '(', ' ' )).replace( ')', ' ' );
        try { tmpf = Float.valueOf( tmps.trim() ); }
        catch ( NumberFormatException e ) { return null; }
        return tmpf;
    } //*************************************************************************
    /** Converts String to float.
     * Unlike Float.valueOf(), this method doesn't throw exceptions and can 
     * convert values surrounded by whitespace and/or brackets.
     * @param val String
     * @return float value or Float.NaN
     */
    public static float floatValue( String val ) {
        if( val == null ) return Float.NaN;
        Float tmpf = null;
        String tmps = ( val.replace( '(', ' ' )).replace( ')', ' ' );
        try { tmpf = Float.valueOf( tmps.trim() ); }
        catch ( NumberFormatException e ) { return Float.NaN; }
        return tmpf.floatValue();
    } //*************************************************************************
    /** Returns value error as float.
     * This method counts number of digits after the dot in val (N) and returns
     * 1 * 10 ^ -N
     * @param val String
     * @return float value or Float.NaN
     */
    public static float floatValueError( String val ) {
        if( val == null ) return Float.NaN;
        Float tmpf = null;
        String tmps = ( val.replace( '(', ' ' )).replace( ')', ' ' );
        tmps = tmps.trim();
// check if it's a float
        try { tmpf = Float.valueOf( tmps.trim() ); }
        catch ( NumberFormatException e ) { return Float.NaN; }
// get error
        int dot = tmps.indexOf( '.' );
        dot = tmps.substring( dot + 1 ).length();
        return (float) Math.pow( 1, ( dot * ( -1 ) ) );
    } //*************************************************************************
    /** Converts String to Integer.
     * Unlike Integer.valueOf(), this method doesn't throw exceptions and can 
     * convert values surrounded by whitespace and/or brackets.
     * @param val String
     * @return Integer value or null
     */
    public static Integer toInteger( String val ) {
        if( val == null ) return null;
        Integer tmpi = null;
        String tmps = ( val.replace( '(', ' ' )).replace( ')', ' ' );
        try { tmpi = Integer.valueOf( tmps.trim() ); }
        catch ( NumberFormatException e ) { return null; }
        return tmpi;
    } //*************************************************************************
    /** Adds proper NMR-STAR quotes to a string
     * @param str string
     * @return input string quoted as per NMR-STAR standard
     */
    public static String toNMRSTARQuotedString( String str ) {
        if( (str == null) || (str.length() < 1 ) ) return ".";
        int spc = str.indexOf( ' ' );
        int tab = str.indexOf( "\t" );
        int nl = str.indexOf( "\n" );
        boolean has_whitespace = ((spc > -1) || (tab > -1));
        int dq = str.indexOf( '"' );
        int sq = str.indexOf( "'" );
// has newline -- use semicolons
        if( nl > -1 ) return "\n;\n" + str + "\n;\n";
// has whitespace, single and double quote(s) -- use semicolons
        if( has_whitespace && (sq > -1) && (dq > -1) ) return "\n;\n" + str + "\n;\n";
// has whitespace and no double quote(s) -- use double quotes
        if( has_whitespace && (dq < 0) ) return "\"" + str + "\"";
// has whitespace and double quote(s) -- use single quotes
        if( has_whitespace && (dq > -1) ) return "'" + str + "'";
// has single quote(s) -- use double quotes
        if( (sq > -1) && (dq < 0) ) return "\"" + str + "\"";
// has double quote(s) -- use single quotes
        if( (sq < 0) && (dq > -1) ) return "'" + str + "'";
// otherwise return as is
        return str;
    } //*************************************************************************
    /** Reads lines from input stream, skipping blank ones.
     * Note that line numbers logged by this methods are off by the number of
     * blank lines in the input.
     * @param in BufferedReader input stream
     * @param num int line number (for error messages)
     * @return String line or null on EOF or I/O exception
     * @param errs error list
     */
    public static String readLine( java.io.BufferedReader in, int num, ErrorList errs ) {
        String s = null;
        do {
            try { s = in.readLine(); }
            catch( java.io.IOException e ) { 
                errs.addError( num, Messages.ERR_READ );
                return null; 
            }
            if( s == null ) return null;
        } while( s.trim().equals( "" ) ); // skip blank lines
        return s;
    } //*************************************************************************
}
