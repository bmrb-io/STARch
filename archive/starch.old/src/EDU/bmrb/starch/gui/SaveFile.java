/*
 * SaveFile.java
 *
 * Created on January 14, 2005, 5:22 PM
 *
 * This software is copyright (c) 2005 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/gui/SaveFile.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/04/13 17:37:08 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: SaveFile.java,v $
 * Revision 1.1  2005/04/13 17:37:08  dmaziuk
 * *** empty log message ***
 * */

package EDU.bmrb.starch.gui;

/**
 * File/save action.
 * @author  dmaziuk
 */
public class SaveFile extends javax.swing.AbstractAction {
    private static final boolean DEBUG = false;
    /** loop table */
    private EDU.bmrb.starch.LoopTable fLt = null;
    /** parent form */
    private Main fParent = null;
    /** Properties */
    private java.util.Properties fProps = null;
//******************************************************************************
    /** Creates a new instance of SaveFile.
     * @param table LoopTable
     * @param parent main form
     * @param props properties
     */
    public SaveFile( EDU.bmrb.starch.LoopTable table, Main parent,
    java.util.Properties props ) {
        super( props.getProperty( Properties.KEY_SAVEFILE ) );
        fProps = props;
        fLt = table;
        fParent = parent;
        putValue( javax.swing.Action.SHORT_DESCRIPTION, props.getProperty( Properties.KEY_SAVEFILE_TIP ) );
        if( props.getProperty( Properties.KEY_OPENFILE_ICON ) != null ) {
            javax.swing.ImageIcon i = new javax.swing.ImageIcon( props.getProperty( Properties.KEY_SAVEFILE_ICON ) );
            putValue( javax.swing.Action.SMALL_ICON, i );
        }
    } //************************************************************************
    /** Reads input file(s).
     * @param evt event
     */
    public void actionPerformed( java.awt.event.ActionEvent evt ) {
if( DEBUG ) {
    try { fLt.print( System.err ); }
    catch( java.sql.SQLException e ) {}
}
        FileChooser chooser = FileChooser.getInstance( fLt, fProps.getProperty( Properties.KEY_WORKDIR ) );
        if( ! chooser.isOutputChooser() ) chooser.initFileSave();
// pick file
        int i = chooser.showSaveDialog( (javax.swing.JComponent) evt.getSource() );
        if( i != javax.swing.JFileChooser.APPROVE_OPTION ) return;
        for( i = 0; i < fLt.FORMATS.length; i++ )
            if( chooser.getFileFilter().getDescription().equals( fLt.FORMATS[i] ) ) {
                break;
            }
        try {
            java.io.PrintWriter out =  new java.io.PrintWriter(
            new java.io.OutputStreamWriter( new java.io.FileOutputStream(
            chooser.getSelectedFile() ), "ISO-8859-15" ) );
            EDU.bmrb.starch.Writer w = null;
            switch( i ) {
                case EDU.bmrb.starch.LoopTable.STAR2 :
                    w = new EDU.bmrb.starch.Star2Writer( fLt );
                    break;
                case EDU.bmrb.starch.LoopTable.STAR3 :
                    w = new EDU.bmrb.starch.Star3Writer( fLt );
                    if( fParent.isPrintSfIds() ) w.setSfIdFlag( true );
                    else w.setSfIdFlag( false );
                    break;
                case EDU.bmrb.starch.LoopTable.XEASY :
                    w = new EDU.bmrb.starch.XeasyWriter( fLt );
                    break;
                case EDU.bmrb.starch.LoopTable.GARRET :
                    w = new EDU.bmrb.starch.GarretWriter( fLt );
                    break;
                case EDU.bmrb.starch.LoopTable.PPM :
                    w = new EDU.bmrb.starch.PpmWriter( fLt );
                    break;
            }
            if( w != null ) w.print( out );
            out.close();
        }
        catch( java.io.IOException ie ) {
            System.err.println( ie );
            ie.printStackTrace();
        }
        catch( java.sql.SQLException se ) {
            System.err.println( se );
            se.printStackTrace();
        }
        catch( Exception e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
}
