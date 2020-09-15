/*
 * Copyright (c) 2006 Board of Regents University of Wisconsin.
 * All Rights Reserved.
 */

package edu.bmrb.starch;

/**
 * Comparator for sorting atoms.
 *
 * todo: atom should be a string-string pair where 1st elt is atom name and 2nd is
 * todo: atom type. Name is parsed into {type}{alpha code}{number}{prime mark(s)}
 *
 * Amino acid atom names consist of atom type, (optional) Greek letter, and a
 * number (but only if there's Greek letter). Nucleic acids have atom type
 * followed by optional number and/or one or two prime marks (single quotes).
 * Sort order: 1st character: H, C, N, other. 2nd char: Greek-alphabetical: A, B,
 * G, D, E, Z, H, or by number. 3rd and subsequent chars: sort by number.
 * Amino acids: 'H', 'HA', 'HA1'. Nucleic acids: "H", "H'" (H-prime), "H''" (H-
 * double prime), "H2".
 *
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Dec 18, 2006
 * Time: 5:32:58 PM
 *
 * $Id$
 */

public class AtomSorter implements java.util.Comparator<StringStringPair> {
    private static final boolean DEBUG = false;
    /** atom type order. */
    private String [] TYPES = { "H", "C", "N", "O", "P" };
    /** Greek-alphabetical order. */
//    private char [] GRAMMAE = { 'A', 'B', 'G', 'D', 'E', 'Z', 'H' };
    private String [] GRAMMAE = { "A", "B", "G", "D", "E", "Z", "H" };
    /**
     * Compare two atoms (string-string pairs).
     * @param p1 first atom
     * @param p2 second atom
     * @return negative integer, zero, or positive integer if the first argument
     * is less than, equal to, or greater than the second.
     */
    public int compare( StringStringPair p1, StringStringPair p2 ) {
        if( p1 == p2 ) return 0;
        if( p1 == null ) return -1; // other can't be null here
        if( p2 == null ) return 1;
        String name1 = p1.getFirst();
        String name2 = p2.getFirst();
        if( (name1 == null) && (name2 == null) ) return 0;
        if( name1 == null ) return -1;
        if( name2 == null ) return 1;
        if( name1.equals( name2 ) ) return 0;
// compare types
        int i, j;
        for( i = 0; i < TYPES.length; i++ )
            if( p1.getSecond().equals( TYPES[i] ) )
                break;
        for( j = 0; j < TYPES.length; j++ )
            if( p2.getSecond().equals( TYPES[j] ) )
                break;
        if( i != j ) return( i - j );
        if( i == TYPES.length ) // both types not in the list
            return( name1.compareTo( name2 ) );
// compare names
        StringBuilder buf = new StringBuilder();
        buf.append( '(' );
        buf.append( p1.getSecond() );
        buf.append( ")(\\p{Alpha}*)(\\d*)(.*)");
        java.util.regex.Pattern pat = java.util.regex.Pattern.compile( buf.toString() );
        java.util.regex.Matcher m1 = pat.matcher( name1 );
if( DEBUG ) {
    System.err.printf( "Name1 %s %s pattern %s with %d groups\n",
                       name1, (m1.matches()?"matches":"doesn't match"),
                       buf.toString(), (m1.matches()?m1.groupCount():0) );
    for( int c = 0; c <= m1.groupCount(); c++ ) System.err.printf( "* group %d: |%s|\n",
                                                                  c, m1.group( c ) );
}
        buf.setLength( 0 );
        buf.append( '(' );
        buf.append( p2.getSecond() );
        buf.append( ")(\\p{Alpha}*)(\\d*)(.*)");
        pat = java.util.regex.Pattern.compile( buf.toString() );
        java.util.regex.Matcher m2 = pat.matcher( name2 );
if( DEBUG ) {
    System.err.printf( "Name2 %s %s pattern %s with %d groups\n",
                       name2, (m2.matches()?"matches":"doesn't match"),
                       buf.toString(), (m2.matches()?m2.groupCount():0) );
    for( int c = 0; c <= m2.groupCount(); c++ ) System.err.printf( "* group %d: |%s|\n",
                                                                  c, m2.group( c ) );
}
// Matchers throw IllegalState if you don't call matches() before group() 
        if( m1.matches() && (! m2.matches()) ) { // m2 is b0rked
System.err.printf( "m2 does not match: %s, %s\n", name2, p2.getSecond() );
            return 1;
        }
        if( m2.matches() && (! m1.matches()) ) { // m1 is b0rked
System.err.printf( "m1 does not match: %s, %s\n", name1, p1.getSecond() );
            return -1;
        }
        if( ! (m1.matches() && m2.matches()) ) {
System.err.printf( "m1 and 2 do not match: %s, %s - %s, %s\n", name1, p1.getSecond(), name2, p2.getSecond() );
            return( name1.compareTo( name2 ) );
        }
// amino acids
// todo: if AA atoms can be H1, H2, do this on residue type instead
        if( (m1.group( 2 ).length() > 0) || (m2.group( 2 ).length() > 0) )
            return compareAA( m1, m2 );
// else it's nucleic acids with numbers and/or primes
        return compareNA( m1, m2 );
    } //*************************************************************************
    /**
     * Compare two amino acid atoms.
     * @param m1 1st atom name
     * @param m2 2nd atom name
     * @return negative integer, zero, or positive integer if the first argument
     * is less than, equal to, or greater than the second.
     */
    private int compareAA( java.util.regex.Matcher m1, java.util.regex.Matcher m2 ) {
        if( (m1.group( 2 ).length() < 1) && (m2.group( 2 ).length() > 0) ) return -1;
        if( (m1.group( 2 ).length() > 0) && (m2.group( 2 ).length() < 1) ) return 1;
        int i, j;
// compare greek letter
        for( i = 0; i < GRAMMAE.length; i++ )
            if( m1.group( 2 ).equals( GRAMMAE[i] ) )
                break;
        for( j = 0; j < GRAMMAE.length; j++ )
            if( m2.group( 2 ).equals( GRAMMAE[j] ) )
                break;
        if( i != j ) return( i - j );
        if( i == GRAMMAE.length ) // both not in list
            return( m1.group( 0 ).compareTo( m2.group( 0 ) ) );
// compare numbers
        if( (m1.group( 3 ).length() < 1) && (m2.group( 3 ).length() > 0) ) return -1;
        if( (m1.group( 3 ).length() > 0) && (m2.group( 3 ).length() < 1) ) return 1;
        try { i = Integer.parseInt( m1.group( 3 ) ); }
        catch( NumberFormatException e ) { i = 0; }
        try { j = Integer.parseInt( m2.group( 3 ) ); }
        catch( NumberFormatException e ) { j = 0; }
        if( i != j ) return( i - j );
        return 0;
    } //*************************************************************************
    /**
     * Compare two nucleic acid atoms.
     * @param m1 1st atom name
     * @param m2 2nd atom name
     * @return negative integer, zero, or positive integer if the first argument
     * is less than, equal to, or greater than the second.
     */
    private int compareNA( java.util.regex.Matcher m1, java.util.regex.Matcher m2 ) {
// compare numbers
        if( (m1.group( 3 ).length() < 1) && (m2.group( 3 ).length() > 0) ) return -1;
        if( (m1.group( 3 ).length() > 0) && (m2.group( 3 ).length() < 1) ) return 1;
        int i, j;
        try { i = Integer.parseInt( m1.group( 3 ) ); }
        catch( NumberFormatException e ) { i = 0; }
        try { j = Integer.parseInt( m2.group( 3 ) ); }
        catch( NumberFormatException e ) { j = 0; }
        if( i != j ) return( i - j );
// this will sort double-prime after single prime
        return( m1.group( 4 ).compareTo( m2.group( 4 ) ) );
    } //*************************************************************************
}
