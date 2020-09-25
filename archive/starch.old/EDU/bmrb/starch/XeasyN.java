/*
 * XeasyN.java
 *
 * Created on December 3, 2003, 1:59 PM
 *
 * This software is copyright (c) 2003 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/XeasyN.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2003/12/12 23:24:09 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: XeasyN.java,v $
 * Revision 1.2  2003/12/12 23:24:09  dmaziuk
 * Updated to Java 2 collections, added pool of unique string as backing storage
 *
 * Revision 1.1  2003/12/03 20:55:59  dmaziuk
 * added converter for XEASY variant
 *
 */

package EDU.bmrb.starch;

/**
 * Version of XEASY converter with residue sequence numbers in second column
 * of sequence file.
 * @author  dmaziuk
 */
public class XeasyN extends EDU.bmrb.starch.Xeasy {
    
    /** Creates a new instance of XeasyN
     * @param data data store
     * @param errs error list
     * @param restype residue type
     * @param pool string pool
     */
    public XeasyN( Data data, ErrorList errs, int restype, EDU.bmrb.lib.StringPool pool ) {
        super( Starch.XEASYN, data, errs, restype, pool );
    } //************************************************************************
    public boolean readLabels(java.io.BufferedReader in) {
        String s = null;
        String label = null;
        int seqcode;
        s = Utils.readLine( in, 1, fErrs );
        if( s == null ) {
            fErrs.addError( 1, Messages.ERR_EOF );
            return false;
        }
        for( int linenum = 1; s != null; linenum++ ) {
            s = s.trim();
            int pos = s.indexOf( ' ' );
            if( pos < 0 ) pos = s.indexOf( '\t' );
            if( pos > 0 ) { // whitespace can't be at 0 after trim()
                label = s.substring( 0, pos );
                s = s.substring( pos ).trim();
                pos = s.indexOf( ' ' );
                if( pos < 0 ) pos = s.indexOf( '\t' );
                if( pos > 0 ) seqcode = Integer.parseInt( s.substring( 0, pos ) );
                else seqcode = Integer.parseInt( s );
            }
            else {
                label = s;
                seqcode = linenum;
            }
            fData.add( new Residue( fRestype, seqcode, label, fPool ) );
            s = Utils.readLine( in, linenum, fErrs );
        }
        try { in.close(); }
        catch( java.io.IOException e ) { /* ignore it */ }
        return true;
    } //************************************************************************
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if( args.length < 1 ) {
            System.err.println( "Usage: java EDU.bmrb.starch.Xeasy filename" );
            System.err.println( "read input data from filename.seq and filename.prot" );
            return;
        }
        ErrorList errs = new ErrorList( true );
        Data data = new Data( errs );
        EDU.bmrb.lib.StringPool pool = new EDU.bmrb.lib.StringPool();
        XeasyN conv = new XeasyN( data, errs, -1, pool );
// read data
        String fname = args[0] + ".seq";
        java.io.BufferedReader seq, prot;
        try {
             seq = new java.io.BufferedReader( new java.io.InputStreamReader( 
                      new java.io.FileInputStream( fname ) ) );
        }
        catch( Exception e ) {
            errs.addError( Messages.ERR_FOPEN + " " + fname );
            return;
        }
// protons
        fname = args[0] + ".prot";
        try {
            prot = new java.io.BufferedReader( new java.io.InputStreamReader( 
                      new java.io.FileInputStream( fname ) ) );
        }
        catch( Exception e ) {
            try { 
                fname = args[0] + ".xeasy";
                prot = new java.io.BufferedReader( new java.io.InputStreamReader( 
                      new java.io.FileInputStream( fname ) ) );
            }
            catch( Exception ex ) {
                errs.addError( Messages.ERR_FOPEN + " " + fname );
                return;
            }
        }
        conv.readLabels( seq );
        conv.readData( prot );
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
