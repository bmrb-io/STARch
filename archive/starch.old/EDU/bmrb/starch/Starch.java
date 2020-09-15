/*
 * Starch.java
 *
 * Created on May 22, 2002, 7:19 PM
 *
 * This software is copyright (c) 2002 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Starch.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2004/08/26 20:37:19 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Starch.java,v $
 * Revision 1.20  2004/08/26 20:37:19  dmaziuk
 * added another input format
 *
 * Revision 1.19  2004/08/26 18:14:30  dmaziuk
 * added new input format
 *
 * Revision 1.18  2003/12/12 23:24:09  dmaziuk
 * Updated to Java 2 collections, added pool of unique string as backing storage
 *
 * Revision 1.17  2003/12/03 20:55:59  dmaziuk
 * added converter for XEASY variant
 *
 * Revision 1.16  2003/04/15 21:26:52  dmaziuk
 * Fixed bugs with comment handling in formats 1 and 2, javadoc warning in
 * Converter. Added another format to Starch.
 *
 * Revision 1.15  2003/01/06 22:45:57  dmaziuk
 * Bugfix release
 *
 *
 */

package EDU.bmrb.starch;
import java.util.*;
import java.io.*;
/**
 * Main STARch class: vector of Converter classes with extra methods etc.
 * @author  dmaziuk
 * @version 1
 */
public class Starch {
    // format constants
    /** Format 1 */
    public static final String FORMAT1 = "FORMAT1";
    /** Format 2 */
    public static final String FORMAT2 = "FORMAT2";
    /** Format 3 */
    public static final String FORMAT3 = "NUCLEICACID";
    /** Format 4 */
    public static final String FORMAT4 = "FORMAT4";
    /** PIPP */
    public static final String PIPP = "PIPP";
    /** XEASY */
    public static final String XEASY = "XEASY";
    /** XEASY with sequence numbers */
    public static final String XEASYN = "XEASY_N";
    /** NMR-STAR 2.1 */
    public static final String STAR2 = "NMR-STAR2.1";
    /** garret */
    public static final String GARRET = "GARRET";
    /** ppm */
    public static final String PPM = "PPM";
    /** Residue type */
    private int fRestype = -1;
    /** current input format */
    private String fInFormat = null;
    /** Vector of Converter objects */
    private java.util.List fConv = null;
    /** Map of integer format IDs to Converter objects */
    private java.util.Map fConvMap = null;
    /** Data object */
    private Data fData = null;
    /** verbose flag */
    private boolean fVerbose = false;
    /** List of error messages */
    private ErrorList fErrs = null;
    /** string pool */
    private EDU.bmrb.lib.StringPool fPool = null;
//*******************************************************************************
    /** Creates new Starch.
     * @param errs error list
     * @param data data store
     * @param restype residue type
     * @param pool string pool
     */
    public Starch( Data data, ErrorList errs, int restype, EDU.bmrb.lib.StringPool pool ) {
        if( (data == null) || (errs == null) )
            throw new NullPointerException( "Parameter missing" );
        fData = data;
        fErrs = errs;
        fRestype = restype;
        fPool = pool;
        fConv = new java.util.ArrayList();
        fConvMap = new java.util.HashMap();
        init_converters();
    } //*************************************************************************
    /** Creates converters */
    private void init_converters() {
        Converter c = new Format1( fData, fErrs, fRestype, fPool );
        fConv.add( c );
        fConvMap.put( c.getFormat(), c );
        Format2 ftwo = new Format2( fData, fErrs, fRestype, fPool );
        fConv.add( ftwo );
        fConvMap.put( ftwo.getFormat(), ftwo );
        Format3 fthree = new Format3( fData, fErrs, fRestype, fPool );
        fConv.add( fthree );
        fConvMap.put( fthree.getFormat(), fthree );
        Pipp fpip = new Pipp( fData, fErrs, fRestype, fPool );
        fConv.add( fpip );
        fConvMap.put( fpip.getFormat(), fpip );
        Xeasy xeasy = new Xeasy( fData, fErrs, fRestype, fPool );
        fConv.add( xeasy );
        fConvMap.put( xeasy.getFormat(), xeasy );
        XeasyN xeasyn = new XeasyN( fData, fErrs, fRestype, fPool );
        fConv.add( xeasyn );
        fConvMap.put( xeasyn.getFormat(), xeasyn );
        Star2 fstar2 = new Star2( fData, fErrs, fRestype, fPool );
        fConv.add( fstar2 );
        fConvMap.put( fstar2.getFormat(), fstar2 );
        Format4 four = new Format4( fData, fErrs, fRestype, fPool );
        fConv.add( four );
        fConvMap.put( four.getFormat(), four );
        Garret fg = new Garret( fData, fErrs, fRestype, fPool );
        fConv.add( fg );
        fConvMap.put( fg.getFormat(), fg );
        Converter ppm = new Ppm( fData, fErrs, fRestype, fPool );
        fConv.add( ppm );
        fConvMap.put( ppm.getFormat(), ppm );
    } //*************************************************************************
    /** Sets residue type
     * @param restype residue type
     */
    public void setResidueType( int restype ) {
        fRestype = restype;
    } //*************************************************************************
    /** Sets input format
     * @param format input format
     */
    public void setInputFormat( String format ) {
        fInFormat = format;
    } //*************************************************************************
    /** Returns list of input formats
     * @return list of formats
     */
    public String [] listInputFormats() {
        String rc [] = new String[ fConv.size() ];
        for( int i = 0; i < rc.length; i++ )
            rc[i] = ((Converter) fConv.get( i )).getFormat();
        return rc;
    } //*************************************************************************
    /** Converts data
     * @param file input file
     * @return false if there were errors
     */
    public boolean convert( String file ) {
        if( fInFormat.equals( XEASY ) || fInFormat.equals( XEASYN ) )
            convertXEASY( file );
        else convertFile( file );
        return true;
    } //*************************************************************************
    /** Converts data ohter than XEASY
     * @param file input file
     * @return false if there were errors
     */
    public boolean convertFile( String file ) {
        Converter conv = (Converter) fConvMap.get( fInFormat );
        if( conv == null ) {
            fErrs.addError( Messages.ERR_NOCONV );
            return false;
        }
        java.io.BufferedReader in;
        try {
             in = new java.io.BufferedReader( new java.io.InputStreamReader( 
                      new java.io.FileInputStream( file ) ) );
        }
        catch( Exception e ) {
            fErrs.addError( Messages.ERR_FOPEN + file );
            return false;
        }
        conv.read( in );
        return true;
    } //*************************************************************************
    /** Converts XEASY data.
     * XEASY is a special case because data comes in two files. We try to open
     * filename.seq first, and then filename.prot. If there's no filenam.prot,
     * try filename.xeasy.
     * @param file input filename
     * @return false if there were errors
     */
    public boolean convertXEASY( String file ) {
        Xeasy conv;
        if( fInFormat.equals( XEASY ) ) conv = (Xeasy) fConvMap.get( XEASY );
        else conv = (Xeasy) fConvMap.get( XEASYN );
        if( conv == null ) {
            fErrs.addError( Messages.ERR_NOCONV );
            return false;
        }
// labels
        String fname = file + ".seq";
        java.io.BufferedReader seq, prot;
        try {
             seq = new java.io.BufferedReader( new java.io.InputStreamReader( 
                      new java.io.FileInputStream( fname ) ) );
        }
        catch( Exception e ) {
            fErrs.addError( Messages.ERR_FOPEN + fname );
            return false;
        }
// protons
        fname = file + ".prot";
        try {
            prot = new java.io.BufferedReader( new java.io.InputStreamReader( 
                      new java.io.FileInputStream( fname ) ) );
        }
        catch( Exception e ) {
            try { 
                fname = file + ".xeasy";
                prot = new java.io.BufferedReader( new java.io.InputStreamReader( 
                      new java.io.FileInputStream( fname ) ) );
            }
            catch( Exception ex ) {
                fErrs.addError( Messages.ERR_FOPEN + fname );
                return false;
            }
        }
        conv.readLabels( seq );
        conv.readData( prot );
        return true;
    } //*************************************************************************
    /** Sets up pointer to Data object.
     * This method can be used to remove existing pointer (by passing null as
     * parameter).
     * @param data Data object
     */
    public void setData( Data data ) {
        fData = data;
    } //*************************************************************************
    /** Returns Data object.
     * @return Data object
     */
    public Data getData() {
        return fData;
    } //*************************************************************************
    /** Sets verbose flag.
     * Calls setVerbose() in ErrorList.
     * @param verbose boolean
     * @see ErrorList#setVerbose( boolean )
     */
    public void setVerbose( boolean verbose ) {
        fErrs.setVerbose( verbose );
    } //*************************************************************************
    /** Returns verbose flag.
     * Calls getVerbose() in ErrorList.
     * @return boolean verbose flag
     * @see ErrorList#getVerbose()
     */
    public boolean getVerbose() {
        return fErrs.getVerbose();
    } //*************************************************************************
    /** Returns residue type.
     * @return residue type
     */
    public int getResidueType() {
        return fRestype;
    } //*************************************************************************
    /** Returns number of available Conveters.
     * @return int size
     */
    public int size() {
        return fConv.size();
    } //*************************************************************************
    /** Returns Converter for specified format.
     * @param format String input format
     * @return Converter object or null
     */
    public Converter get( String format ) {
        if( (format == null) || format.equals( "" ) ) return null;
        return (Converter) fConvMap.get( format );
    } //*************************************************************************
    /** Returns list of known output formats as array of Strings.
     * @return list of input formats
     */
    public String [] listOutputFormats() {
        return fData.OUTPUT_FORMATS;
    } //*************************************************************************
    /** Returns list of available input formats.
     * @return String list of input formats
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if( fConvMap.size() == 0 ) buf.append( "No converters available" );
        else {
            java.util.Iterator i = fConvMap.keySet().iterator();
            while( i.hasNext() )
                buf.append( (String) i.next() );
        }
        return buf.toString();
    } //*************************************************************************
} //*****************************************************************************
