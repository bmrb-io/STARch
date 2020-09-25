/*
 * Converter.java
 *
 * Created on May 16, 2002, 5:55 PM
 *
 * This software is copyright (c) 2002 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Converter.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2003/12/12 23:24:08 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Converter.java,v $
 * Revision 1.10  2003/12/12 23:24:08  dmaziuk
 * Updated to Java 2 collections, added pool of unique string as backing storage
 *
 * Revision 1.9  2003/04/15 21:26:51  dmaziuk
 * Fixed bugs with comment handling in formats 1 and 2, javadoc warning in
 * Converter. Added another format to Starch.
 *
 * Revision 1.8  2003/01/06 22:45:55  dmaziuk
 * Bugfix release
 *
 *
 */

package EDU.bmrb.starch;
import java.util.Vector;
import java.io.*;
/**
 * Parent class for all STARch converters.
 * <BR>
 * Forces derived classes to implement <CODE>boolean read( )</CODE>.<BR>
 * Also contains useful constants and methods common to all subclasses.
 * @author  dmaziuk
 * @version 1
 */
public abstract class Converter {
    /** input format name */
    protected String fFormat = null;
    /** data store */
    protected Data fData = null;
    /** error list */
    protected ErrorList fErrs = null;
    /** string pool */
    protected EDU.bmrb.lib.StringPool fPool = null;
    /** residue type */
    protected int fRestype = -1;
//*******************************************************************************
    /** Creates new Converter.
     * @param format input format
     * @param data data store
     * @param errs error list
     * @param restype residue type
     * @param pool string pool
     */
    public Converter( String format, Data data, ErrorList errs, int restype, EDU.bmrb.lib.StringPool pool ) {
        if( (format == null) || (data == null) || (errs == null) )
            throw new NullPointerException( "Parameter mising" );
        fFormat = format;
        fData = data;
        fErrs = errs;
        fRestype = restype;
        fPool = pool;
    } //*************************************************************************
    /** Returns input format
     * @return input format
     */
    public String getFormat() {
        return fFormat;
    } //*************************************************************************
    /** Sets residue type
     * @param restype residue type
     */
    public void setResidueType( int restype ) {
        fRestype = restype;
    } //*************************************************************************
    /* Force derived classes to implement read( InputStream ) */
    /** Reads data from input stream.
     * @param in BufferedReader input stream reader
     * @return false on error, true otherwise
     */
    public abstract boolean read( BufferedReader in );
}
