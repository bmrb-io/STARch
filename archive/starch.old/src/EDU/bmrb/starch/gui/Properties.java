/*
 * Properties.java
 *
 * Created on January 18, 2005, 2:55 PM
 *
 * This software is copyright (c) 2005 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/gui/Properties.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/04/13 17:37:08 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Properties.java,v $
 * Revision 1.1  2005/04/13 17:37:08  dmaziuk
 * *** empty log message ***
 * */

package EDU.bmrb.starch.gui;

/**
 * Wrapper for Properties object plus constants.
 *
 * @author  dmaziuk
 */
public class Properties {
    /** default config file */
    public static final String DEF_PROPFILE = "starch.properties";
    /* property keys */
    /** Working directory */
    public static final String KEY_WORKDIR = "start.directory";
    // menus
// open file
    /** Open file key */
    public static final String KEY_OPENFILE = "action.openfile.label";
    /** Open file default value */
    public static final String VAL_OPENFILE = "Read file";
    /** Open file tooltip key */
    public static final String KEY_OPENFILE_TIP = "action.openfile.tip";
    /** Open file icon key */
    public static final String KEY_OPENFILE_ICON = "action.openfile.icon";
// save file
    /** Save file key */
    public static final String KEY_SAVEFILE = "action.savefile.label";
    /** Open file default value */
    public static final String VAL_SAVEFILE = "Save file";
    /** Save file tooltip key */
    public static final String KEY_SAVEFILE_TIP = "action.savefile.tip";
    /** Save file icon key */
    public static final String KEY_SAVEFILE_ICON = "action.savefile.icon";
// convert author data
    /** Convert author data key */
    public static final String KEY_CONVERTDATA = "action.convertdata.label";
    /** Convert author data default value */
    public static final String VAL_CONVERTDATA = "Convert data";
    /** Convert author data tooltip key */
    public static final String KEY_CONVERTDATA_TIP = "action.convertdata.tip";
    /** Convert author data icon key */
    public static final String KEY_CONVERTDATA_ICON = "action.convertdata.icon";
// copy author data
    /** Copy author data key */
    public static final String KEY_COPYDATA = "action.copydata.label";
    /** Copy author data default value */
    public static final String VAL_COPYDATA = "Copy author data";
    /** Copy author data tooltip key */
    public static final String KEY_COPYDATA_TIP = "action.copydata.tip";
    /** Copy author data icon key */
    public static final String KEY_COPYDATA_ICON = "action.copydata.icon";
// convert residues
    /** Convert residues key */
    public static final String KEY_CONVERTRES = "action.convertres.label";
    /** Convert residues default value */
    public static final String VAL_CONVERTRES = "Convert residues";
    /** Convert residues tooltip key */
    public static final String KEY_CONVERTRES_TIP = "action.convertres.tip";
    /** Convert residues icon key */
    public static final String KEY_CONVERTRES_ICON = "action.convertres.icon";
// convert atoms
    /** Convert author data key */
    public static final String KEY_CONVERTATOMS = "action.convertatoms.label";
    /** Convert author data default value */
    public static final String VAL_CONVERTATOMS = "Convert atoms";
    /** Convert author data tooltip key */
    public static final String KEY_CONVERTATOMS_TIP = "action.convertatoms.tip";
    /** Convert author data icon key */
    public static final String KEY_CONVERTATOMS_ICON = "action.convertatoms.icon";
// add atom types
    /** Add atom types key */
    public static final String KEY_ADDTYPES = "action.addtypes.label";
    /** Add atom types default value */
    public static final String VAL_ADDTYPES = "Add atom types";
    /** Add atom types tooltip key */
    public static final String KEY_ADDTYPES_TIP = "action.addtypes.tip";
    /** Add atom types icon key */
    public static final String KEY_ADDTYPES_ICON = "action.addtypes.icon";
// reindex residues
    /** Reindex residues key */
    public static final String KEY_REIDXRES = "action.reidxres.label";
    /** Reindex residues default value */
    public static final String VAL_REIDXRES = "Reindex residues";
    /** Reindex residues tooltip key */
    public static final String KEY_REIDXRES_TIP = "action.reidxres.tip";
    /** Reindex residues icon key */
    public static final String KEY_REIDXRES_ICON = "action.reidxres.icon";
// sort atoms
    /** Sort atoms key */
    public static final String KEY_SORTROWS = "action.sortatoms.label";
    /** Sort atoms default value */
    public static final String VAL_SORTROWS = "Sort atoms";
    /** Sort atoms tooltip key */
    public static final String KEY_SORTROWS_TIP = "action.sortatoms.tip";
    /** Sort atoms icon key */
    public static final String KEY_SORTROWS_ICON = "action.sortatoms.icon";
// reindex atoms
    /** Reindex atoms key */
    public static final String KEY_REIDXROWS = "action.reidxatoms.label";
    /** Reindex atoms default value */
    public static final String VAL_REIDXROWS = "Reindex atoms";
    /** Reindex atoms tooltip key */
    public static final String KEY_REIDXROWS_TIP = "action.reidxatoms.tip";
    /** Reindex atoms icon key */
    public static final String KEY_REIDXROWS_ICON = "action.reidxatoms.icon";
// add isotopes
    /** Add isotopes key */
    public static final String KEY_ISOTOPES = "action.addisotopes.label";
    /** Add isotopes default value */
    public static final String VAL_ISOTOPES = "Atom isotopes";
    /** Add isotopes tooltip key */
    public static final String KEY_ISOTOPES_TIP = "action.addisotopes.tip";
    /** Add isotopes icon key */
    public static final String KEY_ISOTOPES_ICON = "action.addisotopes.icon";
// add shift error
    /** Add shift error key */
    public static final String KEY_SHERR = "action.adderror.label";
    /** Add shift error default value */
    public static final String VAL_SHERR = "Chemical shift error";
    /** Add shift error tooltip key */
    public static final String KEY_SHERR_TIP = "action.adderror.tip";
    /** Add shift error icon key */
    public static final String KEY_SHERR_ICON = "action.adderror.icon";
// add accession number
    /** Add accession number key */
    public static final String KEY_ACCNO = "action.addaccno.label";
    /** Add accession number default value */
    public static final String VAL_ACCNO = "BMRB accession number";
    /** Add accession number tooltip key */
    public static final String KEY_ACCNO_TIP = "action.addaccno.tip";
    /** Add accession number icon key */
    public static final String KEY_ACCNO_ICON = "action.addaccno.icon";
// add entity ID
    /** Add entity ID key */
    public static final String KEY_EID = "action.addentityid.label";
    /** Add entity ID default value */
    public static final String VAL_EID = "Entity ID";
    /** Add entity ID tooltip key */
    public static final String KEY_EID_TIP = "action.addentityid.tip";
    /** Add entity ID icon key */
    public static final String KEY_EID_ICON = "action.addentityid.icon";
// add assembly ID
    /** Add assembly ID key */
    public static final String KEY_AID = "action.addassyid.label";
    /** Add assembly ID default value */
    public static final String VAL_AID = "Entity assembly ID";
    /** Add assembly ID tooltip key */
    public static final String KEY_AID_TIP = "action.addassyid.tip";
    /** Add assembly ID icon key */
    public static final String KEY_AID_ICON = "action.addassyid.icon";
// add chemical shift list ID
    /** Add chemical shift list ID key */
    public static final String KEY_LID = "action.addlistid.label";
    /** Add chemical shift list ID default value */
    public static final String VAL_LID = "Chemical shift list ID";
    /** Add chemical shift list ID tooltip key */
    public static final String KEY_LID_TIP = "action.addlistid.tip";
    /** Add chemical shift list ID icon key */
    public static final String KEY_LID_ICON = "action.addlistid.icon";
// add saveframe ID
    /** Add saveframe ID key */
    public static final String KEY_SFID = "action.addsfid.label";
    /** Add saveframe ID default value */
    public static final String VAL_SFID = "Saveframe ID";
    /** Add saveframe ID tooltip key */
    public static final String KEY_SFID_TIP = "action.addsfid.tip";
    /** Add saveframe ID icon key */
    public static final String KEY_SFID_ICON = "action.addsfid.icon";
// delete empty rows
    /** Delete empty rows key */
    public static final String KEY_DELROWS = "action.delempty.label";
    /** Delete empty rows default value */
    public static final String VAL_DELROWS = "Delete empty rows";
    /** Delete empty rows tooltip key */
    public static final String KEY_DELROWS_TIP = "action.delempty.tip";
    /** Delete empty rows icon key */
    public static final String KEY_DELROWS_ICON = "action.delempty.icon";
// delete selected row
    /** Delete selected row key */
    public static final String KEY_DELSROW = "action.delsel.label";
    /** Delete selected row default value */
    public static final String VAL_DELSROW = "Delete row";
    /** Delete selected row tooltip key */
    public static final String KEY_DELSROW_TIP = "action.delsel.tip";
    /** Delete selected row icon key */
    public static final String KEY_DELSROW_ICON = "action.delsel.icon";
    /** properties */
    private java.util.Properties fProps = null;
//******************************************************************************
    /** Creates a new instance of Properties */
    public Properties() {
        loadProps( System.getProperty( "user.home" ) + java.io.File.separator
        + DEF_PROPFILE );
    } //************************************************************************
    /** Creates new instance of Properties with specified properties.
     * @param props properties
     */
    public Properties( java.util.Properties props ) {
        fProps = props;
    } //************************************************************************
    /** Creates new instance of Properties and reads properties from specified file.
     * @param propfile config file name
     * @throws java.io.IOException -- problem reading config file
     */
    public Properties( String propfile ) {
        loadProps( propfile );
    } //************************************************************************
    /** Loads properties from file.
     * @param file filename
     */
    protected void loadProps( String propfile ) {
        fProps = new java.util.Properties( initDefaults() );
        java.io.File f = new java.io.File( propfile );
        if( ! f.exists() ) return;
        try {
            java.io.InputStream in = new java.io.FileInputStream( propfile );
            fProps.load( in );
            in.close();
        }
        catch( java.io.IOException e ) {
            System.err.print( "Cannot load properties from " );
            System.err.println( propfile );
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Returns default properties
     * @return default properties
     */
    public static java.util.Properties initDefaults() {
        java.util.Properties defaults = new java.util.Properties();
        defaults.put( KEY_OPENFILE, VAL_OPENFILE );
        defaults.put( KEY_SAVEFILE, VAL_SAVEFILE );
        defaults.put( KEY_CONVERTDATA, VAL_CONVERTDATA );
        defaults.put( KEY_COPYDATA, VAL_COPYDATA );
        defaults.put( KEY_CONVERTATOMS, VAL_CONVERTATOMS );
        defaults.put( KEY_CONVERTRES, VAL_CONVERTRES );
        defaults.put( KEY_ADDTYPES, VAL_ADDTYPES );
        defaults.put( KEY_REIDXRES, VAL_REIDXRES );
        defaults.put( KEY_SORTROWS, VAL_SORTROWS );
        defaults.put( KEY_REIDXROWS, VAL_REIDXROWS );
        defaults.put( KEY_ISOTOPES, VAL_ISOTOPES );
        defaults.put( KEY_SHERR, VAL_SHERR );
        defaults.put( KEY_ACCNO, VAL_ACCNO );
        defaults.put( KEY_EID, VAL_EID );
        defaults.put( KEY_AID, VAL_AID );
        defaults.put( KEY_LID, VAL_LID );
        defaults.put( KEY_SFID, VAL_SFID );
        defaults.put( KEY_DELROWS, VAL_DELROWS );
        defaults.put( KEY_DELSROW, VAL_DELSROW );
        return defaults;
    } //************************************************************************
    /** Returns properties.
     * @return properties object
     */
    public java.util.Properties getProperties() {
        return fProps;
    } //************************************************************************
    /** Returns property value for the key.
     * @param key property key
     * @return value
     */
    public String get( String key ) {
        return fProps.getProperty( key );
    } //************************************************************************
    /** Prints defaults to stdout.
     * @param args command line arguments
     */
    public static void main( String [] args ) {
        java.util.Properties p = Properties.initDefaults();
        String key;
        java.util.Enumeration keys = p.keys();
        while( keys.hasMoreElements() ) {
            key = keys.nextElement().toString();
            System.out.print( key );
            System.out.print( " = " );
            System.out.println( p.getProperty( key ) );
        }
        System.out.flush();
    } //************************************************************************
}
