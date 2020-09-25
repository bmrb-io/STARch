/*
 * MetadataReader.java
 *
 * Created on May 6, 2005, 12:36 PM
 *
 * This software is copyright (c) 2005 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/MetadataReader.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/05/10 18:56:42 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: MetadataReader.java,v $
 * Revision 1.1  2005/05/10 18:56:42  dmaziuk
 * added functionality
 * */

package EDU.bmrb.starch;

/**
 * Reads values from metadata file.
 * <P>
 * Use: set either assigned chemical shift list ID or data file name, call
 * <CODE>parse()</CODE> with the filename of BMRB entry as parameter, use
 * getter methods to read default chemical shift error values for standard
 * isotopes.
 *
 * @author  dmaziuk
 */
public class MetadataReader
implements EDU.bmrb.sansj.ContentHandler, EDU.bmrb.sansj.ErrorHandler {
    private static final boolean DEBUG = false;
    /** entry id tag */
    public static final String TAG_ENTRYID = "_Entry.ID";
    /** saveframe key tag */
    public static final String TAG_SFKEY = "_Assigned_chem_shift_list.ID";
    /** data file name tag */
    public static final String TAG_DATAFILE = "_Assigned_chem_shift_list.Data_file_name";
    /** shift error tags */
    public static final String [] TAGS_ERR = {
        "_Assigned_chem_shift_list.Chem_shift_1H_err",
        "_Assigned_chem_shift_list.Chem_shift_2H_err",
        "_Assigned_chem_shift_list.Chem_shift_13C_err",
        "_Assigned_chem_shift_list.Chem_shift_15N_err",
        "_Assigned_chem_shift_list.Chem_shift_31P_err",
        "_Assigned_chem_shift_list.Chem_shift_19F_err"
    };
    /** 1H shift error tag */
    public static final int TAGIDX_1HERR = 0;
    /** 2H shift error tag */
    public static final int TAGIDX_2HERR = 1;
    /** 13C shift error tag */
    public static final int TAGIDX_13CERR = 2;
    /** 15N shift error tag */
    public static final int TAGIDX_15NERR = 3;
    /** 31P shift error tag */
    public static final int TAGIDX_31PERR = 4;
    /** 19F shift error tag */
    public static final int TAGIDX_19FERR = 5;
    /** entry ID */
    private String fAccno = null;
    /** data file name */
    private String fFilename = null;
    /** assigned chem. shift list id */
    private String fListId = null;
    /** 1H error value */
    private String f1Herr = null;
    /** 2H error value */
    private String f2Herr = null;
    /** 13C error value */
    private String f13Cerr = null;
    /** 15N error value */
    private String f15Nerr = null;
    /** 31P error value */
    private String f31Perr = null;
    /** 19F error value */
    private String f19Ferr = null;
    /** assigned chemical shift list id from metadata */
    private String fMetaListId = null;
    /** true if found the right saveframe */
    private boolean fGotData = false;
//FIXME!!
    /** error list */
    private ErrorList fErrs = null;
//******************************************************************************
    /** Creates a new instance of MetadataReader.
     * @param errs error list
     */
    public MetadataReader( ErrorList errs ) {
        fErrs = errs;
    } //************************************************************************
    /** Changes data filename.
     * Data file name is used to find the correct chemical shifts saveframe.
     * @param filename file name
     */
    public void setDataFilename( String filename ) {
        fFilename = new java.io.File( filename ).getName();
    } //************************************************************************
    /** Returns data filename.
     * Data file name is used to find the correct chemical shifts saveframe.
     * @return file name
     */
    public String getDataFilename() {
        return fFilename;
    } //************************************************************************
    /** Changes assigned chemical shift list ID.
     * List ID is used to find the correct chemical shifts saveframe.
     * @param listid list ID
     */
    public void setListId( String listid ) {
        fListId = listid;
    } //************************************************************************
    /** Returns assigned chemical shift list ID.
     * List ID is used to find the correct chemical shifts saveframe.
     * @return list ID
     */
    public String getListId() {
        return fListId;
    } //************************************************************************
    /** Returns entry ID.
     * @return entry ID
     */
    public String getEntryId() {
        return fAccno;
    } //************************************************************************
    /** Returns 1H error value
     * @return value or null
     */
    public String get1Herror() {
        return f1Herr;
    } //************************************************************************
    /** Returns 2H error value
     * @return value or null
     */
    public String get2Herror() {
        return f2Herr;
    } //************************************************************************
    /** Returns 13C error value
     * @return value or null
     */
    public String get13Cerror() {
        return f13Cerr;
    } //************************************************************************
    /** Returns 15N error value
     * @return value or null
     */
    public String get15Nerror() {
        return f15Nerr;
    } //************************************************************************
    /** Returns 31P error value
     * @return value or null
     */
    public String get31Perror() {
        return f31Perr;
    } //************************************************************************
    /** Returns 19F error value
     * @return value or null
     */
    public String get19Ferror() {
        return f19Ferr;
    } //************************************************************************
    /** Returns assigned chemical shift list ID (metadata).
     * @return list ID
     */
    public String getMetadataListId() {
        return fMetaListId;
    } //************************************************************************
    /** Parses NMR-STAR file.
     * @param filename file to parse
     * @throws java.io.IOException 
     */
    public void parse( String filename ) throws java.io.IOException {
        if( (fFilename == null) && (fListId == null) )
            throw new IllegalArgumentException( "Need either list ID or data file name" );
if( DEBUG ) System.err.println( "Parse " + filename + ", list id = " + fListId + ", data file = " + fFilename );
        java.io.Reader in = new java.io.FileReader( filename );
        EDU.bmrb.sansj.STARLexer lex = new EDU.bmrb.sansj.STARLexer( in );
        EDU.bmrb.sansj.SansParser p = new EDU.bmrb.sansj.SansParser( lex, this, this );
        fGotData = false;
        p.parse();
    } //************************************************************************
    /** Prins usage summary to stdout */
    public static void usage() {
        System.out.println( "Usage: java EDU.bmrb.starch.MetadataReader <-f file> " +
        "[-d file] [-l ID] [-h]" );
        System.out.println( " -f file: input file (NMR-STAR metadata file)" );
        System.out.println( " -d file: _Assigned_chem_shift_list.Data_file_name value" );
        System.out.println( " -l ID: _Atom_chem_shift.Assigned_chem_shift_list_ID value" );
        System.out.println( "either -d or -l must be specified, if both are present -d is used" );
        System.out.println( " -h: print this message and exit" );
    } //************************************************************************
    /** Main method -- testing.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        gnu.getopt.Getopt g = new gnu.getopt.Getopt( "MetadataReader", args, "d:f:l:h" );
        int opt;
        String infile = null;
        ErrorList errs = new ErrorList();
        MetadataReader mdr = new MetadataReader( errs );
        while( (opt = g.getopt()) != -1 ) {
            switch( opt ) {
                case 'd' : // data filename
                    mdr.setDataFilename( g.getOptarg() );
                    break;
                case 'f' : // input filename
                    infile = g.getOptarg();
                    break;
                case 'l' : // Chemical shift list ID
                    mdr.setListId( g.getOptarg() );
                    break;
                default :
                    usage();
                    System.exit( 1 );
            }
        }
        if( infile == null ) {
            usage();
            System.exit( 2 );
        }
        try {
            mdr.parse( infile );
            if( errs.size() > 0 ) errs.printErrors( System.out );
            System.out.println( "\n" );
            System.out.println( "1H error: " + mdr.get1Herror() );
            System.out.println( "2H error: " + mdr.get2Herror() );
            System.out.println( "13C error: " + mdr.get13Cerror() );
            System.out.println( "15N error: " + mdr.get15Nerror() );
            System.out.println( "31P error: " + mdr.get31Perror() );
            System.out.println( "19F error: " + mdr.get19Ferror() );
            System.out.println( "List ID: " + mdr.getMetadataListId() );
        }
        catch( Exception e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
//************** callbacks *****************************************************
    /** Parse error.
     * @param line line number
     * @param col column number
     * @param str error message
     */
    public void error( int line, int col, String str ) {
//        if( fErrs != null ) fErrs.add(...);
        System.err.print( "Parse error in line " );
        System.err.print( line );
        System.err.print( ", column " );
        System.err.print( col );
        System.err.print( ": " );
        System.err.println( str );
    } //************************************************************************
    /** Parse warning.
     * @param line line number
     * @param col column number
     * @param str error message
     * @return false to continue parsing
     */
    public boolean warning( int line, int col, String str ) {
//        if( fErrs != null ) fErrs.add(...);
        System.err.print( "Parse warning in line " );
        System.err.print( line );
        System.err.print( ", column " );
        System.err.print( col );
        System.err.print( ": " );
        System.err.println( str );
        return false;
    } //************************************************************************
    /** End of saveframe.
     * @param line line number
     * @param str saveframe name
     * @return false to continue parsing
     */
    public boolean endSaveFrame( int line, String str ) {
// if we have data, stop parsing
        if( fGotData ) return true;
        else {
// zero out the values
            f1Herr = null;
            f2Herr = null;
            f13Cerr = null;
            f15Nerr = null;
            f31Perr = null;
            f19Ferr = null;
            fMetaListId = null;
        }
        return false;
    } //************************************************************************
    /** tag/value pair.
     * @param node tag/value
     * @return false to continue parsing
     */
    public boolean data( EDU.bmrb.sansj.DataItemNode node ) {
        if( node.getName().equals( TAG_ENTRYID ) ) {
            fAccno = node.getValue();
            return false;
        }
        if( node.getName().equals( TAG_SFKEY ) ) {
            if( fFilename == null ) {
// select by list id
                if( node.getValue().equals( fListId ) ) {
                    fGotData = true;
                    return false;
                }
            }
            else {
// select by data filename
                fMetaListId = node.getValue();
            }
        }
        if( node.getName().equals( TAG_DATAFILE ) ) {
            if( node.getValue().equals( fFilename ) ) {
                fGotData = true;
                return false;
            }
        }
        for( int i = 0; i < TAGS_ERR.length; i++ ) {
            if( node.getName().equals( TAGS_ERR[i] ) ) {
                switch( i ) {
                    case TAGIDX_1HERR :
                        f1Herr = node.getValue();
                        return false;
                    case TAGIDX_2HERR :
                        f2Herr = node.getValue();
                        return false;
                    case TAGIDX_13CERR :
                        f13Cerr = node.getValue();
                        return false;
                    case TAGIDX_15NERR :
                        f15Nerr = node.getValue();
                        return false;
                    case TAGIDX_31PERR :
                        f31Perr = node.getValue();
                        return false;
                    case TAGIDX_19FERR :
                        f19Ferr = node.getValue();
                        return false;
                }
            }
        }
        return false;
    } //************************************************************************    
    public boolean comment(int param, String str) {
        return false;
    }
    public boolean startData(int param, String str) {
        return false;
    }
    public boolean startSaveFrame(int param, String str) {
        return false;
    }
    public boolean startLoop(int param) {
        return false;
    }
    public boolean endLoop(int param) {
        return false;
    }
    public void endData(int param, String str) {
    }
}
