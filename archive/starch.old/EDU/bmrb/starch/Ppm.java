/*
 * Ppm.java
 *
 * Created on August 26, 2004, 1:59 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Ppm.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2004/08/26 20:37:19 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Ppm.java,v $
 * Revision 1.1  2004/08/26 20:37:19  dmaziuk
 * added another input format
 * */

package EDU.bmrb.starch;

/**
 * Converter for PPM format.
 * <P>
 * PPM format description is available in CAMRA documentation at 
 * <A href="http://www.pence.ca/software/camra/">www.pence.ca/software/camra/</A>.
 * Record format:<pre>
  molNum:Residue_ResId:atom  shift_value [ shift_value]
  </pre>
 * Lines starting with '!' are comments. Shift values of ***.**, 999.99,
 * and -999.99 denote an unknown value.
 * 
 * @author  dmaziuk
 */
public class Ppm extends Converter {
//******************************************************************************    
    /** Creates a new instance of Ppm.
     * @param data data store
     * @param errs error list
     * @param restype residue type
     * @param pool string pool
     */
    public Ppm( Data data, ErrorList errs, int restype, EDU.bmrb.lib.StringPool pool ) {
        super( Starch.PPM, data, errs, restype, pool );
    } //************************************************************************
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
             String label = null, seqno = null, atom = null, shift = null;
             int seq = -1;
             float value = Float.NaN;
             boolean firstline = true;
             Residue r;
             Atom atm;
             while( (line = in.readLine()) != null ) {
                 if( line.charAt( 0 ) == '!' ) continue;
                 if( ! line.matches( ".*\\S+.*" ) ) continue;
                 stok = new java.util.StringTokenizer( line, " \t\n\r\f:_" );
                 i = 0;
                 while( stok.hasMoreTokens() ) {
                     tok = stok.nextToken();
                     switch( i ) {
                         case 0 : // insert row
                             if( firstline ) {
                                 firstline = false;
                                 break;
                             }
                             if( label == null || seqno == null || atom == null || shift == null )
                                 fErrs.addError( linenum, "", "?", Messages.ERR_NOFLDS );
                             else {
                                 try {
                                     seq = Integer.parseInt( seqno );
                                     try { value = Float.parseFloat( shift ); }
                                     catch( NumberFormatException e ) {
                                         fErrs.addError( linenum, seqno, "?", Messages.ERR_INVVAL + shift );
                                     }
                                     if( shift.matches( "-?9{3,}\\.9{2,}" ) ) {
                                         value = Float.NaN;
                                         fErrs.addError( linenum, seqno, "?", Messages.ERR_INVVAL + shift );
                                     }
                                 }
                                 catch( NumberFormatException e ) {
                                     fErrs.addError( linenum, seqno, "?", Messages.ERR_INVCODE );
                                 }
                                 if( ! Float.isNaN( value ) ) {
                                     r = fData.get( seq );
                                     if( r == null ) fData.add( new Residue( fRestype, seq, label, fPool ) );
                                     atm = new Atom( atom, fPool );
                                     atm.setShiftValue( shift );
                                     fData.addAtom( seq, atm );
                                     //out.println( "Inserting " + linenum + " " + seq + " " + label + " " + atom + " " + value );
                                 }
                             }
                             label = null;
                             seqno = null;
                             atom = null;
                             shift = null;
                             seq = -1;
                             value = Float.NaN;
                             break;
                         case 1 : // label
                             label = tok;
                             break;
                         case 2 : // sequence num
                             seqno = tok;
                             break;
                         case 3 : // atom name
                             atom = tok;
                             break;
                         case 4 : // shift value
                             shift = tok;
                             break;
                         default : // ignore everything else
                             break;
                     } // endswitch
                     i++;
                 }
                 linenum++;
                 stok = null;
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
        Converter conv = new Ppm( data, errs, -1, pool );
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
    }
    
    
}
