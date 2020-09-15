/*
 * AtomSorter.java
 *
 * Created on August 18, 2005, 2:23 PM
 *
 * This software is copyright (c) 2005 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/AtomSorter.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/10/20 22:20:19 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: AtomSorter.java,v $
 * Revision 1.1  2005/10/20 22:20:19  dmaziuk
 * typo, chmod fix
 * */

package EDU.bmrb.starch;

/**
 * Sorts residues and atoms.
 * @author  dmaziuk
 */
public class AtomSorter implements java.util.Comparator {
    private final static boolean DEBUG = false;
    /** residue type */
    private int fResTypeId = -1;
//******************************************************************************
    /** Creates a new instance of AtomSorter for AminoAcids */
    public AtomSorter() {
        fResTypeId = 0;
    } //************************************************************************
    /** Creates a new instance of AtomSorter.
     * param restype residue type
     */
    public AtomSorter( int restype ) {
        fResTypeId = restype;
    } //************************************************************************
    /** Compares two atoms
     * @param atom1 first atom
     * @param atom2 second atom
     * @return 0, 1, or -1
     */
    public int compare( Object atom1, Object atom2 ) {
        if( atom1 == atom2 ) return 0;
        if( atom1 == null && atom2 != null ) return -1;
        if( atom1 != null && atom2 == null ) return 1;
        String a1 = ((TableUtils.Atom) atom1).getName();
        String a2 = ((TableUtils.Atom) atom2).getName();
        if( a1 == null && a2 == null ) return 0;
        if( a1 == null && a2 != null ) return -1;
        if( a1 != null && a2 == null ) return 1;
        if( a1.equals( a2 ) ) return 0;
        switch( fResTypeId ) {
            case 0 : // amino-acid
                return compareAA( a1, a2 );
            case 1 : // DNA
            case 2 : // RNA
                return compareNA( a1, a2 );
            default:
                return a1.compareTo( a2 );
        }
    } //************************************************************************
    /** Compares two AA atoms
     * @param a1 first atom name
     * @param a2 second atom name
     * @return 0, 1, or -1
     */
    public int compareAA( String a1, String a2 ) {
        int rc;
        if( a1.charAt( 0 ) != a2.charAt( 0 ) ) {
            switch( a1.charAt( 0 ) ) {
                case 'H' : return -1;
                case 'C' :
                    if( a2.charAt( 0 ) == 'H' ) return 1;
                    else return -1;
                case 'N' :
                    if( (a2.charAt( 0 ) == 'H')
                    || (a2.charAt( 0 ) == 'C') ) return 1;
                    else return -1;
                default :
                    if( (a2.charAt( 0 ) == 'H')
                    || (a2.charAt( 0 ) == 'C')
                    || (a2.charAt( 0 ) == 'C') ) return 1;
                    else {
                        rc = a1.charAt( 0 ) - a2.charAt( 0 );
                        if( rc < 0 ) return -1;
                        else if( rc > 0 ) return 1;
                        return rc;
                    }
            } // endswitch
        }
// both names start with the same character
        if( (a1.length() == 1) && (a2.length() > 1) ) return -1;
        if( (a1.length() > 1) && (a2.length() == 1) ) return 1;
// both names are 2 or more characters long
        if( a1.charAt( 1 ) != a2.charAt( 1 ) ) {
            switch( a1.charAt( 1 ) ) {
                case 'A' : return -1;
                case 'B' :
                    if( a2.charAt( 1 ) == 'A' ) return 1;
                    else return -1;
                case 'G' :
                    if( (a2.charAt( 1 ) == 'A')
                    || (a2.charAt( 1 ) == 'B') ) return 1;
                    else return -1;
                case 'D' :
                    if( (a2.charAt( 1 ) == 'A')
                    || (a2.charAt( 1 ) == 'B')
                    || (a2.charAt( 1 ) == 'G') ) return 1;
                    else return -1;
                case 'E' :
                    if( (a2.charAt( 1 ) == 'E')
                    || (a2.charAt( 1 ) == 'Z')
                    || (a2.charAt( 1 ) == 'H') ) return -1;
                    else return 1;
                case 'Z' :
                    if( (a2.charAt( 1 ) == 'Z')
                    || (a2.charAt( 1 ) == 'H') ) return -1;
                    else return 1;
                case 'H' :
                    if( a2.charAt( 1 ) == 'H' ) return -1;
                    else return 1;
                default:
                    return a1.substring( 1, 2 ).compareTo( a2.substring( 1, 2 ) );
            } // endswitch
        }
// both names start with 2 same characters
        if( (a1.length() == 2) && (a2.length() > 2) ) return -1;
        if( (a1.length() > 2) && (a2.length() == 2) ) return 1;
// both names are 3 or more characters long
        try {
            int thisnum = Integer.parseInt( a1.substring( 2 ) );
            int othernum = Integer.parseInt( a2.substring( 2 ) );
            if( thisnum < othernum ) return -1;
            else if( thisnum > othernum ) return 1;
            else return 0;
        }
        catch( NumberFormatException e ) {
            return a1.substring( 2 ).compareTo( a2.substring( 2 ) );
        }
    } //************************************************************************
    /** Compares two nucleic acid atoms
     * Ordering: H, C, N, O[P], P, then group based on primes, then sort by number
     * within the group.
     * @param a1 first atom name
     * @param a2 second atom name
     * @return 0, 1, or -1
     */
    public int compareNA( String a1, String a2 ) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile( "(\\p{Alpha}{1,})(\\p{Digit}*)('*)" );
        java.util.regex.Matcher m1, m2;
        m1 = p.matcher( a1 );
        m2 = p.matcher( a2 );
        if( m1.matches() && m2.matches() ) {
/*
if( DEBUG ) {
    System.err.println( "*** Atom 1 " + a1 + " " + m1.groupCount() + ":" );
    for( int i = 0; i < m1.groupCount(); i++ ) {
        System.err.print( (i + 1) + ": " );
        System.err.println( m1.group( i + 1 ) );
    }
    System.err.println( "*** Atom 2 " + a2 + " "  + m2.groupCount() + ":" );
    for( int i = 0; i < m2.groupCount(); i++ ) {
        System.err.print( (i + 1) + ": " );
        System.err.println( m2.group( i + 1 ) );
    }
}
 */
            if( (m1.groupCount() == 3) && (m2.groupCount() == 3) ) {
                if( m1.group( 1 ).charAt( 0 ) != m2.group( 1 ).charAt( 0 ) ) {
                    switch( m1.group( 1 ).charAt( 0 ) ) {
                        case 'H' :
                            return -1;
                        case 'C' :
                            if( m2.group( 1 ).charAt( 0 ) == 'H' ) return 1;
                            return -1;
                        case 'N' :
                            if( (m2.group( 1 ).charAt( 0 ) == 'H')
                            || (m2.group( 1 ).charAt( 0 ) == 'C') ) return 1;
                            return -1;
                        case 'O' :
                            if( m2.group( 1 ).charAt( 0 ) == 'P' ) return -1;
                            return 1;
                        case 'P' :
                            return 1;
                        default :
                            return m1.group( 1 ).compareTo( m2.group( 1 ) );
                    }
                }
                else { // same atom type: 1st chars are equal
// no-prime < prime < double-prime
                    if( m1.group( 3 ).length() != m2.group( 3 ).length() ) {
                        if( m1.group( 3 ).length() > m2.group( 3 ).length() ) return 1;
                        else return -1;
                    }
                    else { // same prime
                        if( (m1.group( 2 ).length() == 0) && (m2.group( 2 ).length() > 0) )
                            return -1;
                        else if( (m1.group( 2 ).length() > 0) && (m2.group( 2 ).length() == 0) )
                            return 1;
                        else { // compare numbers
                            try {
                                int n1 = Integer.parseInt( m1.group( 2 ) );
                                int n2 = Integer.parseInt( m2.group( 2 ) );
                                if( n1 < n2 ) return -1;
                                else if( n1 > n2 ) return 1;
                                else return 0;
                            }
                            catch( NumberFormatException e ) { // can never happen
                                return m1.group( 2 ).compareTo( m2.group( 2 ) );
                            }
                        }
                    }
                }
            }
        }
if( DEBUG ) System.err.println( a1 + " match: " + m1.matches() + ", " + a2 + " match: " + m2.matches() );
// pop invalid atoms on top
        if( m1.matches() ) return 1;
        if( m2.matches() ) return -1;
        return a1.compareTo( a2 );
    } //************************************************************************
}
