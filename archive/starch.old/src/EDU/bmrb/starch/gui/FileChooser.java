/*
 * FileChooser.java
 *
 * Created on January 5, 2005, 2:03 PM
 * This software is copyright (c) 2005 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/gui/FileChooser.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/05/10 18:56:43 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: FileChooser.java,v $
 * Revision 1.2  2005/05/10 18:56:43  dmaziuk
 * added functionality
 *
 * Revision 1.1  2005/04/13 17:37:08  dmaziuk
 * *** empty log message ***
 * */

package EDU.bmrb.starch.gui;

/**
 * Input file chooser.
 *
 * @author  dmaziuk
 */
public class FileChooser extends javax.swing.JFileChooser {
//*************** Format chooser ***********************************************
    class FormatChooser extends javax.swing.filechooser.FileFilter {
        private String format = null;
        public FormatChooser( String format ) {
            this.format = format;
        }
        public boolean accept(java.io.File file) {
            return true;
        }
        public String getDescription() {
            return format;
        }
    }
//******************************************************************************
    /** XEASY with no sequence numbers */
    public static final int XEASY_NOSEQ = 0;
    /** XEASY with sequence numbers */
    public static final int XEASY_SEQ = 1;
    /** singleton instance */
    private static FileChooser fInstance = null;
    /** Loop table */
    private EDU.bmrb.starch.LoopTable fLt = null;
    /** Output file filters */
    private FormatChooser [] fOutputFormats = null;
    /** Input file filters */
    private FormatChooser [] fInputFormats = null;
    /** XEASY protons filter */
    private FormatChooser [] fXFormats = null;
    /** true if lists input formats */
    private boolean fInputChooser = true;
    /** true if lists output formats */
    private boolean fOutputChooser = true;
//******************************************************************************
    /** Factory constructor.
     * @param table loop table
     */
/*    public static FileChooser getInstance( EDU.bmrb.starch.LoopTable table ) {
        if( fInstance == null ) fInstance = new FileChooser( table, null );
        return fInstance;
    } //************************************************************************
 */
    /** Factory constructor.
     * @param table loop table
     * @param startdir initial directory
     */
    public static FileChooser getInstance( EDU.bmrb.starch.LoopTable table,
    String startdir ) {
        if( fInstance == null ) fInstance = new FileChooser( table, startdir );
        return fInstance;
    } //************************************************************************
    /** Creates a new instance of FileChooser.
     * @param table loop table
     * @param startdir initial directory
     */
    public FileChooser( EDU.bmrb.starch.LoopTable table, String startdir ) {
        fLt = table;
        fInputFormats = new FormatChooser[fLt.FORMATS.length];
        for( int i = 0; i < fInputFormats.length; i++ ) {
            if( i == fLt.XEASY )
                fInputFormats[i] = new FormatChooser( fLt.FORMATS[i] + " protons file" );
            else fInputFormats[i] = new FormatChooser( fLt.FORMATS[i] );
        }
        fOutputFormats = new FormatChooser[fLt.FORMATS.length];
        for( int i = 0; i < fOutputFormats.length; i++ )
            fOutputFormats[i] = new FormatChooser( fLt.FORMATS[i] );
        fXFormats = new FormatChooser[2];
        fXFormats[XEASY_NOSEQ] = new FormatChooser( fLt.FORMATS[fLt.XEASY] +
        " sequence file" );
        fXFormats[XEASY_SEQ] = new FormatChooser( fLt.FORMATS[fLt.XEASY] +
        " sequence file with residue numbers" );
        initFileOpen();
        this.setFileFilter( fInputFormats[fLt.STAR2] );
        if( startdir != null ) this.setCurrentDirectory( new java.io.File( startdir ) );
    } //************************************************************************
    /** Adds list of input formats to file chooser */
    public void initFileOpen() {
        this.setDialogTitle( "Open data file" );
        this.resetChoosableFileFilters();
        this.setAcceptAllFileFilterUsed( false );
        for( int i = 0; i < fInputFormats.length; i++ )
            this.addChoosableFileFilter( fInputFormats[i] );
        this.setFileFilter( fInputFormats[fLt.STAR2] );
        fInputChooser = true;
        fOutputChooser = false;
    } //************************************************************************
    /** Resets list of input formats to "XEASY protons" */
    public void initXeasyProtOpen() {
        this.setDialogTitle( "Open XEASY protons file" );
        this.resetChoosableFileFilters();
        this.setAcceptAllFileFilterUsed( false );
        for( int i = 0; i < fXFormats.length; i++ )
            this.addChoosableFileFilter( fXFormats[i] );
        this.addChoosableFileFilter( fXFormats[XEASY_NOSEQ] );
        fInputChooser = false;
        fOutputChooser = false;
    } //************************************************************************
    /** Adds NMR-STAR 3 input format to file chooser */
    public void initMetadataFileOpen() {
        this.setDialogTitle( "Open metadata file (BMRB entry)" );
        this.resetChoosableFileFilters();
        this.setAcceptAllFileFilterUsed( false );
        this.addChoosableFileFilter( fInputFormats[fLt.STAR3] );
        this.setFileFilter( fInputFormats[fLt.STAR3] );
        fInputChooser = true;
        fOutputChooser = false;
    } //************************************************************************
    /** Adds list of output formats to file chooser */
    public void initFileSave() {
        this.resetChoosableFileFilters();
        this.setAcceptAllFileFilterUsed( false );
        for( int i = 0; i < fOutputFormats.length; i++ ) {
            this.addChoosableFileFilter( fOutputFormats[i] );
        }
        this.setFileFilter( fOutputFormats[fLt.STAR3] );
        fInputChooser = false;
        fOutputChooser = true;
    } //************************************************************************
    /** Returns true if current file filters are for input formats.
     * @return true or false
     */
    public boolean isInputChooser() {
        return fInputChooser;
    } //************************************************************************
    /** Returns true if current file filters are for output formats.
     * @return true or false
     */
    public boolean isOutputChooser() {
        return fOutputChooser;
    } //************************************************************************
    /** Returns true if chosen file type is "XEASY sequence with residue numbers".
     * @return true or false
     */
    public boolean isXeasyWithNumbers() {
        return( this.getFileFilter() == fXFormats[XEASY_SEQ] );
    } //************************************************************************
}
