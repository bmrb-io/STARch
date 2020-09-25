/*
 * Garret.java
 *
 * Created on August 25, 2004, 6:26 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Garret.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2004/08/26 20:37:19 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Garret.java,v $
 * Revision 1.2  2004/08/26 20:37:19  dmaziuk
 * added another input format
 *
 * Revision 1.1  2004/08/26 18:14:30  dmaziuk
 * added new input format
 * */

package EDU.bmrb.starch;

/**
 * Converter for garret format.
 * <P>
 * Garret format example is available in CAMRA documentation at 
 * <A href="http://www.pence.ca/software/camra/">www.pence.ca/software/camra/</A>.
 * Record format:<pre>
   1 ALA
   HA 4.130
   C  180.000
   ; 0.0
  </pre>
 * Shift values of 999.99 and -999.99 denote an unknown value.
 *
 * @author  dmaziuk
 */
public class Garret extends Converter {
    /** record delimiter */
    public static final char ENDREC = ';';
    /** comment char */
    public static final char COMMENT = '#';
//******************************************************************************    
    /** Creates a new instance of Garret.
     * @param data data store
     * @param errs error list
     * @param restype residue type
     * @param pool string pool
     */
    public Garret( Data data, ErrorList errs, int restype, EDU.bmrb.lib.StringPool pool ) {
        super( Starch.GARRET, data, errs, restype, pool );
    } //*************************************************************************
    /** Reads data from input stream.
     * @param in BufferedReader input stream
     * @return false on critical error, true otherwise
     */
    public boolean read(java.io.BufferedReader in) {
        try {
            String line;
            int linenum = 0;
            java.util.StringTokenizer stok;
            String tok;
            int i;
            String seqno = null, label = null, atom = null, shift = null;
            int seq = -1;
            float value = Float.NaN;
            boolean start_rek = true;
            boolean have_seq = false;
            while( (line = in.readLine()) != null ) {
                if( ! line.matches( ".*\\S+.*" ) ) continue;
                if( line.trim().charAt( 0 ) == COMMENT ) continue;
// EOR
                if( line.trim().charAt( 0 ) == ENDREC ) {
                    atom = null;
                    label = null;
                    seqno = null;
                    have_seq = false;
                    start_rek = true;
                    continue;
                }
                stok = new java.util.StringTokenizer( line );
                if( start_rek ) {                   // new residue
                    if( stok.countTokens() < 2 ) {
                        fErrs.addError( linenum, "", "?", Messages.ERR_NOFLDS );
                        continue;
                    }
                    seqno = stok.nextToken();
                    try { 
                        seq = Integer.parseInt( seqno );
                        have_seq = true;
                    }
                    catch( NumberFormatException e ) {
                        fErrs.addError( linenum, seqno, "?", Messages.ERR_INVCODE );
                        continue;
                    }
                    label = stok.nextToken();
                    if( (label != null) && have_seq )
                        fData.add( new Residue( fRestype, seq, label, fPool ) );
                    start_rek = false;
                }
                else {  // atoms
                    if( stok.countTokens() < 2 ) {
                        fErrs.addError( linenum, seqno, "?", Messages.ERR_NOFLDS );
                        continue;
                    }
                    atom = stok.nextToken();
                    shift = stok.nextToken();
                    if( shift.matches( "-?9{3,}\\.9{2,}" ) ) {
                        value = Float.NaN;
                        fErrs.addError( linenum, seqno, "?", Messages.ERR_INVVAL + shift );
                        continue;
                    }
                    else try { value = Float.parseFloat( shift ); }
                    catch( NumberFormatException e ) {
                        fErrs.addError( linenum, seqno, "?", Messages.ERR_INVVAL + shift );
                        continue;
                    }
// insert row
                    if( have_seq && (label != null) && (atom != null) 
                    && (! Float.isNaN( value )) ) {
                        Atom atm = new Atom( atom, fPool );
                        atm.setShiftValue( shift );
                        fData.addAtom( seq, atm );
                    }
                }
                stok = null;
                linenum++;
            } // endwhile readline()
            in.close();
            return true;
        }
        catch( java.io.IOException e ) {
            System.err.println( e.getMessage() );
            e.printStackTrace();
            return false;
        }
    } //************************************************************************
    /** Main method.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ErrorList errs = new ErrorList( true );
        Data data = new Data( errs );
        EDU.bmrb.lib.StringPool pool = new EDU.bmrb.lib.StringPool();
        Converter conv = new Garret( data, errs, -1, pool );
        java.io.BufferedReader in;
        try {
            if( args.length > 0 ) {
                in = new java.io.BufferedReader( new java.io.InputStreamReader( 
                      new java.io.FileInputStream( args[0] ) ) );
            }
            else in = new java.io.BufferedReader( new java.io.InputStreamReader( System.in ) );
        }
        catch( Exception e ) {
            errs.addError( Messages.ERR_FOPEN + " " + args[0] );
            return;
        }
        conv.read( in );
        data.setOutputFormat( Data.STAR2 );
        Residue r;
        int row = 1;
        data.printHeader( System.out );
        while( data.size() > 0 ) {
            r = data.get( 0 );
            row = data.printResidue( System.out, r, row );
            data.removeElementAt( 0 );
        }
        data.printFooter( System.out );
// print out errors
        if( errs.size() < 1 ) return;
        errs.printErrors( System.err );
    } //************************************************************************
}
