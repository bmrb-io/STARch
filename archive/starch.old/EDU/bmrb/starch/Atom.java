/*
 * Atom.java
 *
 * Created on December 18, 2002, 1:53 PM
 *
 * This software is copyright (c) 2002 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Atom.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2004/07/22 20:01:50 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Atom.java,v $
 * Revision 1.10  2004/07/22 20:01:50  dmaziuk
 * added command-line 2.1 to 3.0 loop converter
 *
 * Revision 1.9  2004/07/02 19:37:33  dmaziuk
 * changed handling of ambiguity codes
 *
 * Revision 1.8  2004/03/12 00:10:51  dmaziuk
 * changed pretty-printing of STAR3
 *
 * Revision 1.7  2003/12/23 23:27:56  dmaziuk
 * Bugfix: null comments
 *
 * Revision 1.6  2003/12/23 01:36:24  dmaziuk
 * *** empty log message ***
 *
 * Revision 1.5  2003/12/23 01:30:42  dmaziuk
 * *** empty log message ***
 *
 * Revision 1.4  2003/12/12 23:24:08  dmaziuk
 * Updated to Java 2 collections, added pool of unique string as backing storage
 *
 * Revision 1.3  2003/01/06 22:45:55  dmaziuk
 * Bugfix release
 *
 * Revision 1.2  2003/01/03 01:52:08  dmaziuk
 * Major bugfix/added functionality:
 * turned off "bad" nomenclature conversions -- ones that caused loss of data,
 * added a command-line parameter for residue types,
 * atom names are now quoted properly.
 *
 * Revision 1.1  2002/12/30 22:22:37  dmaziuk
 * major rewrite of data structure
 *
 *
 */

package EDU.bmrb.starch;

/**
 * Class that holds one atom.
 * An atom has a name, type, chemical shift value, error, ambiguity code, 
 * and an optional comment. Atom name and type are always converted to upper
 * case. Chemical shift value and error are stored as strings because we're supposed
 * to leave them "as provided by the author". Converting them to floats loses
 * trailing zeroes (which may or may not be significant). Comments do not contain
 * duplicate strings (i.e. duplicate comments are not added).
 * Atom can be printed out as a CSV or NMR-STAR 2.1 (default) string.
 * @author  dmaziuk
 * @version 1
 */
public class Atom implements java.io.Serializable, Comparable {
    /** atom id */
    private int fId = -1;
    /** atom name */
    private String fName = null;
    /** atom type */
    private String fType = null;
    /** chemical shift value */
    private String fShift = null;
    /** chemical shift error */
    private String fShiftErr = null;
    /** chemical shift ambiguity code */
    private int fAmb = 0;
    /** comment */
    private StringBuffer fComm = null;
    /** pool of unique strings */
    private EDU.bmrb.lib.StringPool fPool = null;
//*******************************************************************************
    /** Creates new Atom.
     * @param id atom id
     * @param name atom name
     * @param pool string pool
     */
    public Atom( int id, String name, EDU.bmrb.lib.StringPool pool ) {
        fId = id;
        fPool = pool;
        if( name == null ) fName = null;
        if( fPool != null ) fName = pool.add( name.toUpperCase() );
        else fName = name.toUpperCase();
//        fComm = new StringBuffer();
    } //*************************************************************************
    /** Creates new Atom.
     * @param name atom name
     * @param pool string pool
     */
    public Atom( String name, EDU.bmrb.lib.StringPool pool ) {
        fPool = pool;
        if( name == null ) fName = null;
        if( fPool != null ) fName = pool.add( name.toUpperCase() );
        else fName = name.toUpperCase();
//        fComm = new StringBuffer();
    } //*************************************************************************
    /** Returns atom id.
     * @return atom id
     */
    public int getId() {
        return fId;
    } //*************************************************************************
    /** Sets atom id.
     * @param id atom id
     */
    public void setId( int id ) {
        fId = id;
    } //*************************************************************************
    /** Returns atom name.
     * @return atom name
     */
    public String getName() {
        return fName;
    } //*************************************************************************
    /** Sets atom name.
     * @param name atom name
     */
    public void setName( String name ) {
        if( name == null ) fName = null;
        if( fPool != null ) fName = fPool.add( name.toUpperCase() );
        else fName = name.toUpperCase();
    } //*************************************************************************
    /** Returns atom type.
     * @return atom type
     */
    public String getType() {
        return fType;
    } //*************************************************************************
    /** Sets atom type.
     * @param type atom type
     */
    public void setType( String type ) {
        if( type == null ) fType = null;
        else { 
            if( fPool != null ) fType = fPool.add( type.toUpperCase() );
	    else fType = type.toUpperCase();
	}
//        else fType = type.toUpperCase();
    } //*************************************************************************
    /** Returns chemical shift value.
     * It is stored as string to preserve trailing zeroes.
     * @return chemical shift value
     */
    public String getShiftValue() {
        return fShift;
    } //*************************************************************************
    /** Sets chemical shift value.
     * It is stored as string to preserve trailing zeroes.
     * @param val chemical shift value
     */
    public void setShiftValue( String val ) {
        if( fPool != null ) fShift = fPool.add( val );
        else fShift = val;
    } //*************************************************************************
    /** Returns chemical shift error.
     * It is stored as string to preserve trailing zeroes.
     * @return chemical shift error
     */
    public String getShiftError() {
        return fShiftErr;
    } //*************************************************************************
    /** Sets chemical shift error.
     * It is stored as string to preserve trailing zeroes.
     * @param val chemical shift error
     */
    public void setShiftError( String val ) {
        if( fPool != null ) fShiftErr = fPool.add( val );
        else fShiftErr = val;
    } //*************************************************************************
    /** Returns chemical shift ambiguity code.
     * @return chemical shift ambiguity code
     */
    public int getShiftAmbiguityCode() {
        return fAmb;
    } //*************************************************************************
    /** Sets chemical shift ambiguity code.
     * @param val chemical shift ambiguity code
     */
    public void setShiftAmbiguityCode( int val ) {
        fAmb = val;
    } //*************************************************************************
    /** Returns comment.
     * @return comment
     */
    public String getComment() {
        if( (fComm != null) && (fComm.length() > 0) ) return fComm.toString();
        else return null;
    } //*************************************************************************
    /** Adds text to comment.
     * @param str comment
     */
    public void addComment( String str ) {
        if( (str == null) || (str.length() < 1) ) return;
        if( fComm == null ) fComm = new StringBuffer();
        if( fComm.length() < 1 ) fComm.append( str );
        else if( fComm.toString().indexOf( str ) < 0 ) {
            fComm.append( ' ' );
            fComm.append( str );
        }
    } //*************************************************************************
    /** Sets comment.
     * If parameter is null, removes existing comment.
     * @param str comment
     */
    public void setComment( String str ) {
        fComm.setLength( 0 );
        if( str != null ) fComm.append( str );
    } //*************************************************************************
    /** Returns this atom as NMR-STAR 2.1 (tab-delimited) string.
     * Format: name, type, shift value, shift error, ambiguity code, #comment. 
     * Dots are printed for missing values, excluding comments.
     * @return tab-delimited string.
     */
    public String toSTARString() {
        StringBuffer buf = new StringBuffer();
        buf.append( Utils.toNMRSTARQuotedString( fName ) + "\t" );
        if( (fType == null) || (fType.length() < 1) ) buf.append( ".\t" );
        else buf.append( Utils.toNMRSTARQuotedString( fType ) + "\t" );
        if( (fShift == null) || (fShift.length() < 1) ) buf.append( ".\t" );
        else buf.append( fShift + "\t" );
        if( (fShiftErr == null) || (fShiftErr.length() < 1) ) buf.append( ".\t" );
        else buf.append( fShiftErr + "\t" );
        if( fAmb == 0 ) buf.append( "." );
        else if( fAmb < 0 ) buf.append( "?" );
        else buf.append( fAmb );
// TODO: figure out where #null comes from
//        if( (fComm != null) && (fComm.length() > 0) && (! "null".equals( fComm.toString() )) ) 
//System.err.println( "fComm is " + ((fComm == null)?" null " : " not null ") + fComm );
        if( fComm != null )
            buf.append( "\t# " + fComm );
        return buf.toString();
    } //*************************************************************************
    /** Returns this atom as comma-separated string.
     * Format: name, type, shift value, shift error, ambiguity code, #comment. 
     * @return comma-separated string.
     */
    public String toCSVString() {
        StringBuffer buf = new StringBuffer();
        buf.append( "\"" + fName + "\"," );
        if( (fType == null) || (fType.length() < 1) ) buf.append( "," );
        else buf.append( "\"" + fType + "\"," );
        if( (fShift == null) || (fShift.length() < 1) ) buf.append( "," );
        else buf.append( fShift + "," );
        if( (fShiftErr == null) || (fShiftErr.length() < 1) ) buf.append( "," );
        else buf.append( fShiftErr + "," );
        if( fAmb == 0 ) buf.append( "," );
        else if( fAmb < 0 ) buf.append( "?," );
        else buf.append( fAmb + "," );
        if( fComm != null ) buf.append( fComm );
        return buf.toString();
    } //*************************************************************************
    /** Returns this atom as NMR-STAR 2.1 (tab-delimited) string.
     * Format: name, type, shift value, shift error, ambiguity code, #comment. 
     * Dots are printed for missing values, excluding comments.
     * @return tab-delimited string.
     */
    public String toString() {
        return toSTARString();
    } //************************************************************************
    /** Compares two atoms.
     * Natural order for atoms: H,C,N, other first, then A,B,G,D,E,Z,H, if present,
     * then the number, if present. (Atoms without the Greek letter are sorted
     * first, i.e. H is less then HA. Atoms without a number are sorted first, 
     * i.e. HA is less then HA1).
     * @param obj object to compare this one to
     * @return -1 if this atom is less then obj, 0 if they are equal, 1 if this
     * atom is greater
     * @throws ClassCastException if obj is null or is of different type
     */
    public int compareTo(Object obj) {
        if( obj == this ) return 0;
        Atom a = (Atom) obj;
        int rc;
        String aname = a.getName();
        if( fName.charAt( 0 ) != aname.charAt( 0 ) ) {
            switch( fName.charAt( 0 ) ) {
                case 'H' : return -1;
                case 'C' :
                    if( aname.charAt( 0 ) == 'H' ) return 1;
                    else return -1;
                case 'N' :
                    if( (aname.charAt( 0 ) == 'H') 
                    || (aname.charAt( 0 ) == 'C') ) return 1;
                    else return -1;
                default :
                    if( (aname.charAt( 0 ) == 'H') 
                    || (aname.charAt( 0 ) == 'C')
                    || (aname.charAt( 0 ) == 'C') ) return 1;
                    else {
                        rc = fName.charAt( 0 ) - aname.charAt( 0 );
                        if( rc < 0 ) return -1;
                        else if( rc > 0 ) return 1;
                        return rc;
                    }
            } // endswitch
        }
// both names start with the same character
        if( (fName.length() == 1) && (aname.length() > 1) ) return -1;
        if( (fName.length() > 1) && (aname.length() == 1) ) return 1;
// both names are 2 or more characters long
        if( fName.charAt( 1 ) != aname.charAt( 1 ) ) {
            switch( fName.charAt( 1 ) ) {
                case 'A' : return -1;
                case 'B' :
                    if( aname.charAt( 1 ) == 'A' ) return 1;
                    else return -1;
                case 'G' :
                    if( (aname.charAt( 1 ) == 'A')
                    || (aname.charAt( 1 ) == 'B') ) return 1;
                    else return -1;
                case 'D' :
                    if( (aname.charAt( 1 ) == 'A')
                    || (aname.charAt( 1 ) == 'B')
                    || (aname.charAt( 1 ) == 'G') ) return 1;
                    else return -1;
                case 'E' :
                    if( (aname.charAt( 1 ) == 'E')
                    || (aname.charAt( 1 ) == 'Z')
                    || (aname.charAt( 1 ) == 'H') ) return -1;
                    else return 1;
                case 'Z' :
                    if( (aname.charAt( 1 ) == 'Z')
                    || (aname.charAt( 1 ) == 'H') ) return -1;
                    else return 1;
                case 'H' :
                    if( aname.charAt( 1 ) == 'H' ) return -1;
                    else return 1;
            } // endswitch
        }
// both names start with 2 same characters
        if( (fName.length() == 2) && (aname.length() > 2) ) return -1;
        if( (fName.length() > 2) && (aname.length() == 2) ) return 1;
// both names are 3 or more characters long
        int thisnum = Integer.parseInt( fName.substring( 2 ) );
        int othernum = Integer.parseInt( aname.substring( 2 ) );
        if( thisnum < othernum ) return -1;
        else if( thisnum > othernum ) return 1;
        else return 0;
    } //************************************************************************
    /** Returns true if compareTo( obj ) returns 0.
     * @param obj atom to compare
     * @return true or false
     */
    public boolean equals( Object obj ) {
        return this.compareTo( obj ) == 0;
    } //************************************************************************
}
