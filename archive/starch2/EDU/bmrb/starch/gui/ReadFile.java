/*
 * ReadFile.java
 *
 * Created on January 5, 2005, 1:32 PM
 *
 * This software is copyright (c) 2005 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source$
 * 
 * AUTHOR:      $Author$
 * DATE:        $Date$
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log$ */

package EDU.bmrb.starch.gui;

/**
 * File read action.
 *
 * @author  dmaziuk
 */
public class ReadFile extends javax.swing.AbstractAction {
    private final static boolean DEBUG = false; //true;
    /** loop table */
    private EDU.bmrb.starch.LoopTable fLt = null;
    /** Error list */
    private EDU.bmrb.starch.ErrorList fErrList = null;
    /** Error text area */
    private javax.swing.JTextArea fErrPane = null;
    /** Properties */
    private java.util.Properties fProps = null;
//******************************************************************************
    /** Creates a new instance of ReadFile.
     * @param table loop table
     * @param errlist error list
     * @param errpane error text pane
     * @param props properties
     */
    public ReadFile( EDU.bmrb.starch.LoopTable table, EDU.bmrb.starch.ErrorList errlist,
    javax.swing.JTextArea errpane, java.util.Properties props ) {
        super( props.getProperty( Properties.KEY_OPENFILE ) );
        fProps = props;
        fLt = table;
        fErrList = errlist;
        fErrPane = errpane;
        putValue( javax.swing.Action.SHORT_DESCRIPTION, props.getProperty( Properties.KEY_OPENFILE_TIP ) );
        if( props.getProperty( Properties.KEY_OPENFILE_ICON ) != null ) {
            javax.swing.ImageIcon i = new javax.swing.ImageIcon( props.getProperty( Properties.KEY_OPENFILE_ICON ) );
            putValue( javax.swing.Action.SMALL_ICON, i );
        }
    } //************************************************************************
    /** Reads input file(s).
     * @param evt event
     */
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        FileChooser chooser = FileChooser.getInstance( fLt, fProps.getProperty( Properties.KEY_WORKDIR ) );
        if( ! chooser.isInputChooser() ) chooser.initFileOpen();
        java.io.File protfile = null;
// pick file
        int i = chooser.showOpenDialog( (javax.swing.JComponent) evt.getSource() );
        if( i != javax.swing.JFileChooser.APPROVE_OPTION ) return;
        for( i = 0; i < chooser.getChoosableFileFilters().length; i++ )
            if( chooser.getChoosableFileFilters()[i] == chooser.getFileFilter() ) {
                protfile = chooser.getSelectedFile();
                break;
            }
        if( i == fLt.XEASY ) {
// get protons file name
            chooser.initXeasyProtOpen();
            i = chooser.showOpenDialog( (javax.swing.JComponent) evt.getSource() );
            if( i != javax.swing.JFileChooser.APPROVE_OPTION ) return;
// parse XEASY
            EDU.bmrb.starch.XeasyReader xr = new EDU.bmrb.starch.XeasyReader( fLt, fErrList );
            xr.setHasSequenceNumbers( chooser.isXeasyWithNumbers() );
            try {
                java.io.BufferedReader in = new java.io.BufferedReader(
                new java.io.InputStreamReader( new java.io.FileInputStream(
                protfile ), "ISO-8859-15" ) );
                xr.parseProtons( in );
                in = new java.io.BufferedReader( new java.io.InputStreamReader(
                new java.io.FileInputStream( chooser.getSelectedFile() ), "ISO-8859-15" ) );
                xr.parseResudies( in );
                if( ! xr.parsedOk() ) fLt.clear();
                else EDU.bmrb.starch.TableUtils.reindexRows( fLt );
            }
            catch( java.io.IOException e ) {
                System.err.println( e );
                e.printStackTrace();
            }
            catch( java.sql.SQLException se ) {
                System.err.println( se );
                se.printStackTrace();
            }
            chooser.initFileOpen();
        }
        else {
            try {
                java.io.BufferedReader in = new java.io.BufferedReader(
                new java.io.InputStreamReader( new java.io.FileInputStream(
                protfile ), "ISO-8859-15" ) );
                EDU.bmrb.starch.Reader r = null;
                switch( i ) {
                    case EDU.bmrb.starch.LoopTable.STAR2 :
                        r = new EDU.bmrb.starch.Star2Reader( fLt, fErrList );
                        break;
                    case EDU.bmrb.starch.LoopTable.STAR3 :
                        r = new EDU.bmrb.starch.Star3Reader( fLt, fErrList );
                        break;
                    case EDU.bmrb.starch.LoopTable.GARRET :
                        r = new EDU.bmrb.starch.GarretReader( fLt, fErrList );
                        break;
                    case EDU.bmrb.starch.LoopTable.PPM :
                        r = new EDU.bmrb.starch.PpmReader( fLt, fErrList );
                        break;
                }
                fLt.createTable();
                r.parse( in );
                in.close();
if( DEBUG ) {
    fLt.print( System.err );
    System.err.println( "***************************" );
}
                if( ! r.parsedOk() ) fLt.clear();
                else EDU.bmrb.starch.TableUtils.reindexRows( fLt );
if( DEBUG ) fLt.print( System.err );
            }
            catch( java.io.IOException e ) {
                System.err.println( e );
                e.printStackTrace();
            }
            catch( java.sql.SQLException se ) {
                System.err.println( se );
                se.printStackTrace();
            }
        }
// redraw the table & show error messages
        fLt.fireTableDataChanged();
        if( fErrList != null && fErrList.size() > 0 )
            if( fErrPane != null ) fErrPane.setText( fErrList.toString() );
    } //************************************************************************
}
