/*
 * Data.java
 *
 * Created on May 16, 2002, 6:05 PM
 *
 * This software is copyright (c) 2002 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Data.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2004/09/01 22:15:17 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Data.java,v $
 * Revision 1.22  2004/09/01 22:15:17  dmaziuk
 * makefile fix
 *
 * Revision 1.21  2004/07/02 19:37:33  dmaziuk
 * changed handling of ambiguity codes
 *
 * Revision 1.20  2004/03/12 00:10:51  dmaziuk
 * changed pretty-printing of STAR3
 *
 * Revision 1.19  2004/03/11 23:37:49  dmaziuk
 * updated to current dictionary
 *
 * Revision 1.18  2003/12/23 23:27:56  dmaziuk
 * Bugfix: null comments
 *
 * Revision 1.17  2003/12/23 01:42:59  dmaziuk
 * *** empty log message ***
 *
 * Revision 1.15  2003/12/23 01:27:05  dmaziuk
 * *** empty log message ***
 *
 * Revision 1.14  2003/12/12 23:24:08  dmaziuk
 * Updated to Java 2 collections, added pool of unique string as backing storage
 *
 * Revision 1.13  2003/12/12 00:16:35  dmaziuk
 * Porting to Java 2
 *
 * Revision 1.12  2003/01/06 22:45:55  dmaziuk
 * Bugfix release
 *
 *
 */

package EDU.bmrb.starch;
/**
 * Data store: vector of residues.
 * @author  dmaziuk
 * @version 1
 * @see Residue
 */
public class Data {
    // output formats
    /** NMR-STAR 2.1 */
    public static final int STAR2 = 0;
    /** NMR-STAR 3.0 */
    public static final int STAR3 = 1;
    /** CSV */
    public static final int CSV = 2;
    /** XEASY */
    public static final int XEASY = 3;
    /** format names */
    public static final String [] OUTPUT_FORMATS = { "NMR-STAR 2.1", "NMR-STAR 3.0",
    "CSV", "XEASY" };
    /* NMR-STAR tags */
    /** "loop_" */
    public static final String LOOP_START = "loop_";
    /** "stop_" */
    public static final String LOOP_END = "stop_";
    /** NMR-STAR 2.1 tags */
    public static final String [] fTags = {
        "_Atom_shift_assign_ID", 
        "_Residue_seq_code", 
        "_Residue_label",
        "_Atom_name", 
        "_Atom_type", 
        "_Chem_shift_value", 
        "_Chem_shift_value_error", 
        "_Chem_shift_ambiguity_code" };
    /* indices */
    /** magic number: input column that doesn't have corresponding STAR tag */
    public static final int NOSUCHTAG = -2;
    /** number of columns in chemical shifts loop */
    public static final int NUM_LOOP_COLS = 8;
    /** "_Atom_shift_assign_ID" */
    public static final int SHIFT_ID = 0;
    /** "_Residue_seq_code" */
    public static final int SEQ_CODE = 1;
    /** "_Residue_label" */
    public static final int LABEL = 2;
    /** "_Atom_name" */
    public static final int ATOM_NAME = 3;
    /** "_Atom_type" */
    public static final int ATOM_TYPE = 4;
    /** "_Chem_shift_value" */
    public static final int SHIFT_VAL = 5;
    /** "_Chem_shift_value_error" */
    public static final int SHIFT_ERR = 6;
    /** "_Chem_shift_ambiguity_code" */
    public static final int SHIFT_AMB = 7;
    /** comments */
    public static final int COMMENTS = 8;
    /** NMR-STAR 3.0 tags */
    public static final String [] fTags3 = {
        "_Atom_chem_shift.Sf_ID",
        "_Atom_chem_shift.Entry_ID",
        "_Atom_chem_shift.Assigned_chemical_shift_list_ID",
        "_Atom_chem_shift.ID",
        "_Atom_chem_shift.Entry_atom_ID",
        "_Atom_chem_shift.Entity_assembly_ID",
        "_Atom_chem_shift.Entity_ID",
        "_Atom_chem_shift.Comp_index_ID",
        "_Atom_chem_shift.Seq_ID",
        "_Atom_chem_shift.Comp_ID",
        "_Atom_chem_shift.Atom_ID",
        "_Atom_chem_shift.Atom_type",
        "_Atom_chem_shift.Atom_isotope",
        "_Atom_chem_shift.Author_seq_code",
        "_Atom_chem_shift.Author_comp_code",
        "_Atom_chem_shift.Author_atom_code",
        "_Atom_chem_shift.Chem_shift_val",
        "_Atom_chem_shift.Chem_shift_val_err",
        "_Atom_chem_shift.Chem_shift_assign_fig_of_merit",
        "_Atom_chem_shift.Chem_shift_ambiguity_code",
        "_Atom_chem_shift.Chem_shift_occupancy_ID",
        "_Atom_chem_shift.Chem_shift_derivation_ID" };
    /** tab width */
    public static final int TABWIDTH = 8;
    /** residues */
    private java.util.ArrayList fRes = null;
    /** output format */
    private int fOutformat = -1;
    /** error list */
    private ErrorList fErrs = null;
//*******************************************************************************
    /** Creates new Data.
     * @param errs error list
     */
    public Data( ErrorList errs ) {
        fRes = new java.util.ArrayList();
        fErrs = errs;
    } //*************************************************************************
    /** Sets output format.
     * @param format output format
     */
    public void setOutputFormat( int format ) {
        fOutformat = format;
    } //*************************************************************************
    /** Returns number of residues.
     * @return int number of elements
     */
    public int size() {
        return fRes.size();
    } //*************************************************************************
    /** Removes all residues. */
    public void clear() {
        fRes.clear();
    } //*************************************************************************
    /** Removes residue at specified index.
     * @param index index
     */
    public void removeElementAt( int index ) {
        fRes.remove( index );
    } //*************************************************************************
    /** Adds a residue.
     * If residue with the same sequence code and label already exists, it is 
     * not added.
     * @param residue residue
     */
    public void add( Residue residue ) {
        if( residue == null ) return;
        Residue r;
        for( int i = 0; i < fRes.size(); i++ ) {
            r = (Residue) fRes.get( i );
            if( r.getLabel().equals( residue.getLabel() ) 
            && (r.getSequenceCode() == residue.getSequenceCode() ) ) {
//                fErrs.addError( Integer.toString( r.getSequenceCode() ), 
//                r.getLabel(), Messages.ERR_INVCODE + ": " + residue.getLabel() +
//                " " + residue.getSequenceCode() );
                return;
            }
        }
        fRes.add( residue );
    } //*************************************************************************
    /** Returns residue with specified number.
     * @param code residue sequence code
     * @return residue or null
     */
    public Residue get( int code ) {
        if( (code < 0) || (code >= fRes.size()) ) return null;
        return (Residue) fRes.get( code );
    } //*************************************************************************
    /** Adds atom to a residue.
     * If atom with the same name already exists in the residue, adds chemical
     * shift value, error, and ambiguity code to existing record.
     * @param seqcode residue sequence code
     * @param atom atom
     */
    public void addAtom( int seqcode, Atom atom ) {
        if( atom == null ) return;
        int i;
        Residue r = null;
        for( i = 0; i < fRes.size(); i++ ) 
            if( ((Residue) fRes.get( i )).getSequenceCode() == seqcode ) {
                r = (Residue) fRes.get( i );
                break;
            }
        if( r == null ) {
            fErrs.addError( Integer.toString( seqcode ), "?", Messages.ERR_INVCODE
            + "(" + atom.getName() + ")" );
            return;
        }
        java.util.List atoms = r.getAtoms();
        boolean found = false;
        for( i = 0; i < atoms.size(); i++ ) {
            if( atom.getName().equals( ((Atom) atoms.get( i )).getName() ) ) {
                found = true; 
                break;
            }
        }
        if( found ) {
            ((Atom) atoms.get( i )).setShiftValue( atom.getShiftValue() );
            ((Atom) atoms.get( i )).setShiftError( atom.getShiftError() );
            ((Atom) atoms.get( i )).setShiftAmbiguityCode( atom.getShiftAmbiguityCode() );
        }
        else atoms.add( atom );
    } //*************************************************************************
    /** Returns this object as String.
     * This method prints out a tab-separated table.
     * @return String
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for( int i = 0; i < fRes.size(); i++ )
            buf.append( ((Residue)fRes.get( i )).toString() + "\n" );
        return buf.toString();
    } //*************************************************************************
    /** Returns this object as Comma-Separated Values string.
     * This method prints out a CSV table without null values.
     * <STRONG>Note</STRONG>: resulting string may be huge. In most cases you 
     * will want to use the more efficient printCSV() method.
     * @return String
     * @see #printCSV( java.io.PrintStream )
     */
    public String toCSV() {
        StringBuffer buf = new StringBuffer();
        int row = 1;
        for( int i = 0; i < fRes.size(); i++ ) {
            for( int j = 0; j < ((Residue) fRes.get( i )).size(); j++ ) {
                buf.append( row + "," + ((Residue) fRes.get( i )).atomCSVString( j ) + "\n" );
                row++;
            }
        }
        return buf.toString();
    } //*************************************************************************
    /** Returns this object as NMR-STAR 2.1 loop.
     * This method prints out NMR-STAR loop tags followed by a tab-separated 
     * table with empty values as '.'.
     * <STRONG>Note</STRONG>: resulting string may be huge. In most cases you 
     * will want to use the more efficient printStar() method.
     * @return String or null
     * @see #printStar( java.io.PrintStream )
     */
    public String toStar() {
        StringBuffer buf = new StringBuffer();
        int row = 1;
        for( int i = 0; i < fRes.size(); i++ ) {
            for( int j = 0; j < ((Residue) fRes.get( i )).size(); j++ ) {
                buf.append( row + "\t" + ((Residue) fRes.get( i )).atomSTARString( j ) + "\n" );
                row++;
            }
        }
        return buf.toString();
    } //*************************************************************************
    /** Returns this object as NMR-STAR 3.0 loop.
     * <STRONG>Note</STRONG>: resulting string may be huge. In most cases you 
     * will want to use the more efficient printStar3() method.
     * @return String or null
     * @see #printStar3( java.io.PrintStream )
     */
    public String toStar3() {
        StringBuffer buf = new StringBuffer();
        int i;
// header
        buf.append( LOOP_START + "\n" );
        for( i = 0; i < fTags3.length; i++ ) 
            buf.append( "    " + fTags3[i] + "\n" );
        buf.append( "\n" );
// data
        int row = 1;
        Residue r;
        java.util.List atoms;
        Atom a;
        for( i = 0; i < fRes.size(); i++ ) {
            r = (Residue) fRes.get( i );
            atoms = r.getAtoms();
            for( int j = 0; j < atoms.size(); j++ ) {
                a = (Atom) atoms.get( j );
                buf.append( ".    .    .    " );
                buf.append( row + "    " );
                buf.append( ".    .    .    .    " );
                buf.append( r.getSequenceCode() + "    " );
                buf.append( Utils.toNMRSTARQuotedString( r.getLabel() ) + "    " );
                buf.append( Utils.toNMRSTARQuotedString( a.getName() ) + "    " );
                if( a.getType() == null ) buf.append( ".    " );
                else buf.append( Utils.toNMRSTARQuotedString( a.getType() ) );
                buf.append( ".    .    .    .    " );
                if( a.getShiftValue() == null ) buf.append( ".    " );
                else buf.append( a.getShiftValue() + "    " );
                if( a.getShiftError() == null ) buf.append( ".    " );
                else buf.append( a.getShiftError() + "    " );
                buf.append( ".    " );
                if( a.getShiftAmbiguityCode() == 0 ) buf.append( ".    " );
                else buf.append( a.getShiftAmbiguityCode() + "    " );
                buf.append( ".    .\n" );
                row++;
            }
        }
        buf.append( LOOP_END + "\n" );
        return buf.toString();
    } //*************************************************************************
    /** Prints this object out in Comma-Separated Values format.
     * @param out output stream
     */
    public void printCSV( java.io.PrintStream out ) {
        if( out == null ) return;
        int i;
// header
        for( i = 0; i < (NUM_LOOP_COLS - 1); i++ ) 
            out.print( "\"" + fTags[i] + "\"," );
        out.println( "\"" + fTags[i] + "\"" );
// data
        int row = 1;
        for( i = 0; i < fRes.size(); i++ ) {
            for( int j = 0; j < ((Residue) fRes.get( i )).size(); j++ ) {
                out.println( row + "," + ((Residue) fRes.get( i )).atomCSVString( j ) );
                row++;
            }
        }
        out.flush();
    } //*************************************************************************
    /** Prints this object out in NMR-STAR 2.1 format.
     * @param out output stream
     */
    public void printStar( java.io.PrintStream out ) {
        if( out == null ) return;
        int i;
// header
        out.println( LOOP_START );
        for( i = 0; i < fTags.length; i++ ) 
            out.println( "\t" + fTags[i] );
        out.println();
// data
        int row = 1;
        for( i = 0; i < fRes.size(); i++ ) {
            for( int j = 0; j < ((Residue) fRes.get( i )).size(); j++ ) {
                out.println( row + "\t" + ((Residue) fRes.get( i )).atomSTARString( j ) );
                row++;
            }
        }
        out.println( LOOP_END );
        out.flush();
    } //*************************************************************************
    /** Prints this object out in NMR-STAR 3.0 format.
     * @param out output stream
     */
    public void printStar3( java.io.PrintStream out ) {
        if( out == null ) return;
        int i;
// header
        out.println( LOOP_START );
        for( i = 0; i < fTags3.length; i++ ) 
            out.println( "    " + fTags3[i] );
        out.println();
// data
        int row = 1;
        Residue r;
        java.util.List atoms;
        Atom a;
        for( i = 0; i < fRes.size(); i++ ) {
            r = (Residue) fRes.get( i );
            atoms = r.getAtoms();
            for( int j = 0; j < atoms.size(); j++ ) {
                a = (Atom) atoms.get( j );
                out.print( ".    .    .    " );
                out.print( row + "    " );
                out.print( ".    .    .    .    " );
                out.print( r.getSequenceCode() + "\t" );
                out.print( Utils.toNMRSTARQuotedString( r.getLabel() ) + "    " );
                out.print( Utils.toNMRSTARQuotedString( a.getName() ) + "    " );
                if( a.getType() == null ) out.print( ".    " );
                else out.print( Utils.toNMRSTARQuotedString( a.getType() ) + "    " );
                out.print( ".    .    .    .    " );
                if( a.getShiftValue() == null ) out.print( ".    " );
                else out.print( a.getShiftValue() + "    " );
                if( a.getShiftError() == null ) out.print( ".    " );
                else out.print( a.getShiftError() + "    " );
                out.print( ".    " );
                if( a.getShiftAmbiguityCode() == 0 ) out.print( ".    " );
                else out.print( a.getShiftAmbiguityCode() + "    " );
                out.print( ".    .    " );
//                if( (a.getComment() == null) || (a.getComment().length() < 1) 
//                || ("null".equals( a.getComment().trim() )) ) 
                if( a.getComment() == null )
                    out.println();
                else out.println( "# " + a.getComment() );
                
                row++;
            }
        }
        out.println( LOOP_END );
        out.flush();
    } //*************************************************************************
    /** Prints this object out in format set by previous call to setOutputFormat().
     * @param out output stream
     * @see #setOutputFormat( int )
     */
    public void print( java.io.PrintStream out ) {
        switch( fOutformat ) {
            case STAR3 : 
                printStar3( out );
                break;
            case CSV :
                printCSV( out );
                break;
            case XEASY :
                printXeasy( out );
                break;
            default : printStar( out );
        }
    } //*************************************************************************
    /** Prints out file header in format set by previous call to setOutputFormat().
     * @param out output stream
     * @see #setOutputFormat( int )
     */
    public void printHeader( java.io.PrintStream out ) {
        int i;
        switch( fOutformat ) {
            case STAR3 : 
                out.println( LOOP_START );
                for( i = 0; i < fTags3.length; i++ ) out.println( "\t" + fTags3[i] );
                out.println();
                break;
            case CSV :
                for( i = 0; i < (NUM_LOOP_COLS - 1); i++ ) out.print( "\"" + fTags[i] + "\"," );
                out.println( "\"" + fTags[i] + "\"" );
                break;
            case XEASY :
                break;
            default : 
                out.println( LOOP_START );
                for( i = 0; i < fTags.length; i++ ) out.println( "\t" + fTags[i] );
                out.println();
        }
    } //*************************************************************************
    /** Prints out file footer in format set by previous call to setOutputFormat().
     * @param out output stream
     * @see #setOutputFormat( int )
     */
    public void printFooter( java.io.PrintStream out ) {
        switch( fOutformat ) {
            case CSV :
                break;
            case XEASY :
                break;
            default :
                out.println( LOOP_END );
        }
    } //*************************************************************************
    /** Prints residue out in format set by previous call to setOutputFormat().
     * @param out output stream
     * @param residue residue
     * @param row row number
     * @return row number
     */
    public int printResidue( java.io.PrintStream out, Residue residue, int row ) {
        switch( fOutformat ) {
            case STAR3 : 
	        int i;
                java.util.List atoms = residue.getAtoms();
                Atom a;
                for( int j = 0; j < atoms.size(); j++ ) {
                    a = (Atom) atoms.get( j );
                    out.print( '.' );
		    for( i = 0; i < TABWIDTH - 1; i++ ) out.print( ' ' );
                    out.print( '.' );
		    for( i = 0; i < TABWIDTH - 1; i++ ) out.print( ' ' );
                    out.print( '.' );
		    for( i = 0; i < TABWIDTH - 1; i++ ) out.print( ' ' );
                    out.print( row );
		    for( i = 0; i < (TABWIDTH - Integer.toString( row ).length()); i++ )
		        out.print( ' ' );
                    out.print( '.' );		    
		    for( i = 0; i < TABWIDTH - 1; i++ ) out.print( ' ' );
                    out.print( '.' );
		    for( i = 0; i < TABWIDTH - 1; i++ ) out.print( ' ' );
                    out.print( '.' );
		    for( i = 0; i < TABWIDTH - 1; i++ ) out.print( ' ' );
                    out.print( '.' );
		    for( i = 0; i < TABWIDTH - 1; i++ ) out.print( ' ' );
                    out.print( residue.getSequenceCode() );
		    for( i = 0; i < (TABWIDTH - Integer.toString(residue.getSequenceCode()).length()); i++ )
		        out.print( ' ' );
                    out.print( Utils.toNMRSTARQuotedString( residue.getLabel() ) );
		    for( i = 0; i < (TABWIDTH - residue.getLabel().length()); i++ )
		        out.print( ' ' );
                    out.print( Utils.toNMRSTARQuotedString( a.getName() ) );
		    for( i = 0; i < (TABWIDTH - a.getName().length()); i++ ) out.print( ' ' );
                    if( a.getType() == null ) {
		        out.print( '.' );
		        for( i = 0; i < TABWIDTH - 1; i++ ) out.print( ' ' );
		    }
                    else {
		        out.print( Utils.toNMRSTARQuotedString( a.getType() ) );
		        for( i = 0; i < (TABWIDTH - a.getType().length()); i++ ) out.print( ' ' );
		    }
                    out.print( '.' );		    
		    for( i = 0; i < TABWIDTH - 1; i++ ) out.print( ' ' );
                    out.print( '.' );
		    for( i = 0; i < TABWIDTH - 1; i++ ) out.print( ' ' );
                    out.print( '.' );
		    for( i = 0; i < TABWIDTH - 1; i++ ) out.print( ' ' );
                    out.print( '.' );
		    for( i = 0; i < TABWIDTH - 1; i++ ) out.print( ' ' );
                    if( a.getShiftValue() == null ) {
		        out.print( '.' );
   		        for( i = 0; i < TABWIDTH - 1; i++ ) out.print( ' ' );
		    }
                    else {
		        out.print( a.getShiftValue() );
                        for( i = 0; i < (TABWIDTH - a.getShiftValue().length()); i++ )
			    out.print( ' ' );
		    }
                    if( a.getShiftError() == null ) {
		        out.print( '.' );
   		        for( i = 0; i < TABWIDTH - 1; i++ ) out.print( ' ' );
		    }
                    else {
		        out.print( a.getShiftError() );
                        for( i = 0; i < (TABWIDTH - a.getShiftError().length()); i++ )
			    out.print( ' ' );
		    }
                    out.print( '.' );
		    for( i = 0; i < TABWIDTH - 1; i++ ) out.print( ' ' );
		    
                    if( a.getShiftAmbiguityCode() == 0 ) {
		        out.print( '.' );
   		        for( i = 0; i < TABWIDTH - 1; i++ ) out.print( ' ' );
		    }
                    else if( a.getShiftAmbiguityCode() < 0 ) {
		        out.print( '?' );
   		        for( i = 0; i < TABWIDTH - 1; i++ ) out.print( ' ' );
		    }
                    else {
                        out.print( a.getShiftAmbiguityCode() );
                        for( i = 0; i < (TABWIDTH - Integer.toString( a.getShiftAmbiguityCode()).length()); i++ )
			    out.print( ' ' );
		    }
                    out.print( '.' );
		    for( i = 0; i < TABWIDTH - 1; i++ ) out.print( ' ' );
                    out.print( '.' );
		    for( i = 0; i < TABWIDTH - 1; i++ ) out.print( ' ' );
                    if( (a.getComment() == null) || (a.getComment().length() < 1) ) 
                        out.println();
                    else out.println( "# " + a.getComment() );
                    row++;
                }
                break;
            case CSV :
                for( int j = 0; j < residue.size(); j++ ) {
                    out.println( row + "," + residue.atomCSVString( j ) );
                    row++;
                }
                break;
            case XEASY :
                break;
            default : 
                for( int j = 0; j < residue.size(); j++ ) {
                    out.println( row + "\t" + residue.atomSTARString( j ) );
                    row++;
                }
        }
        out.flush();
        return row;
    } //************************************************************************
    /** Prints residue list in Xeasy format.
     * @param out output stream
     */
    public void printXeasyResidues( java.io.PrintWriter out ) {
        for( int i = 0; i < fRes.size(); i++ ) {
            out.print( ((Residue) fRes.get( i )).getLabel() );
            for( int j = 0; j < TABWIDTH; j++ ) out.print( ' ' );
            out.println( ((Residue) fRes.get( i )).getSequenceCode() );
        }
        out.flush();
    } //************************************************************************
    /** Prints atom ("protons") list in Xeasy format.
     * @param out output stream
     */
    public void printXeasyProtons( java.io.PrintWriter out ) {
        java.util.List atoms;
        int id = 1;
        for( int i = 0; i < fRes.size(); i++ ) {
            atoms = ((Residue) fRes.get( i )).getAtoms();
//System.err.println( atoms.size() );
            for( int j = 0; j < atoms.size(); j++ ) {
                out.print( id );
                for( int k = 0; k < TABWIDTH; k++ ) out.print( ' ' );
                if( ((Atom) atoms.get( j )).getShiftValue() != null )
                    out.print( ((Atom) atoms.get( j )).getShiftValue() );
                else out.print( "***.**" );
                for( int k = 0; k < TABWIDTH; k++ ) out.print( ' ' );
                if( ((Atom) atoms.get( j )).getShiftError() != null )
                    out.print( ((Atom) atoms.get( j )).getShiftError() );
                else out.print( "***.**" );
                for( int k = 0; k < TABWIDTH; k++ ) out.print( ' ' );
                out.print( ((Atom) atoms.get( j )).getName() );
                for( int k = 0; k < TABWIDTH; k++ ) out.print( ' ' );
                out.println( ((Residue) fRes.get( i )).getSequenceCode() );
                id++;
            }
        }
        out.flush();
    } //************************************************************************
    /** Prints this object in Xeasy format.
     * Output goes to single file with a separator line between residue and
     * proton sections.
     * @param out output stream
     */
    public void printXeasy( java.io.PrintWriter out ) {
        printXeasyResidues( out );
        out.println();
        out.println( "############ END OF RESIDUES ### START OF PROTONS ##########" );
        out.println();
        printXeasyProtons( out );
        out.println();
    } //************************************************************************
    /** Prints this object in Xeasy format.
     * Output goes to single file with a separator line between residue and
     * proton sections.
     * @param out output stream
     */
    public void printXeasy( java.io.PrintStream out ) {
        printXeasy( new java.io.PrintWriter( out ) );
    } //************************************************************************
    /** Main method, for testing.
     * @param args the command line arguments
     */
    public static void main ( String args[] ) {
        ErrorList errs = new ErrorList( true );
        Data d = new Data( errs );
        EDU.bmrb.lib.StringPool pool = new EDU.bmrb.lib.StringPool();
        System.out.println( "Adding a test residue" );
        Residue r = new Residue( 1, "label", pool );
        r.addAtom( new Atom( "atom1", pool ) );
        d.add( r );
        d.addAtom( 1, new Atom( "atom2", pool ) );
        System.out.println( "*** toString(): ***" );
        System.out.println( d.toString() );
        System.out.println( "*** printCSV(): ***" );
        d.printCSV( System.out );
        System.out.println( "*** printStar(): ***" );
        d.printStar( System.out );
        System.out.println( "*** printStar3(): ***" );
        d.printStar3( System.out );
        if( errs.size() > 0 ) errs.printErrors( System.out );
    } //*************************************************************************
}
