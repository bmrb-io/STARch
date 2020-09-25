/*
 * Messages.java
 *
 * Created on August 20, 2002, 6:34 PM
 *
 * This software is copyright (c) 2002 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Messages.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2004/08/26 20:37:19 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Messages.java,v $
 * Revision 1.10  2004/08/26 20:37:19  dmaziuk
 * added another input format
 *
 * Revision 1.9  2004/08/26 18:14:30  dmaziuk
 * added new input format
 *
 * Revision 1.8  2003/01/06 22:45:56  dmaziuk
 * Bugfix release
 *
 *
 */

package EDU.bmrb.starch;

/**
 * Error and warning messages.
 * This class has no methods, just string constants -- messages.
 * @author  dmaziuk
 * @version 1
 */
public class Messages {
    /** file open error */
    public static final String ERR_FOPEN = "Error opening file ";
    /** read error */
    public static final String ERR_READ = "Error reading input";
    /** short read */
    public static final String ERR_SHORTREAD = "Short read, line: ";
    /** premature eof */
    public static final String ERR_EOF = "Unexpected EOF on input";
    /** no converter for this format */
    public static final String ERR_NOCONV = "No converter for this format";
    /** not a field count */
    public static final String ERR_FCNT = " does not look like field count";
    /** invalid field name */
    public static final String ERR_NOLOOP = "Missing loop_ tag (wrong input format?)";
    /** invalid field name */
    public static final String ERR_FLDNAME = "Invalid field name (wrong input format?)";
    /** invalid field offset */
    public static final String ERR_FLDOFF = "Invalid field offset";
    /** invalid field length */
    public static final String ERR_FLDLEN = "Invalid field length";
    /** invalid field data */
    public static final String ERR_FLDDAT = "Invalid field data";
    /** invalid sequence code */
    public static final String ERR_INVCODE = "Invalid residue sequence code";
    /** duplicate sequence code */
    public static final String ERR_DUPCODE = "Already have residue sequence code";
    /** missing sequence code */
    public static final String ERR_NOCODE = "Missing residue sequence code";
    /** unknown residue label */
    public static final String ERR_RESLABEL = "Unknown residue label";
    /** duplicate residue label */
    public static final String ERR_DUPLABEL = "Already have residue label";
    /** missing residue label */
    public static final String ERR_NOLABEL = "Missing residue label";
   /** can't find atom type */
    public static final String ERR_NOATOMTYPE = "No atom type";
    /** check atom type */
    public static final String ERR_CHKATOMTYPE = "Check atom type";
    /** invalid atom name */
    public static final String ERR_INVALIDATOM = "Invalid atom";
    /** duplicate atom name */
    public static final String ERR_DUPLICATEATOM = "Duplicate atom name";
    /** missing column(s) */
    public static final String ERR_NOFLDS = "Not enough fields";
    /** invalid shift */
    public static final String ERR_INVVAL = "Invalid value ";
    /** this should never happen */
    public static final String ERR_CANTBE = "This should never happen";
//*******************************************************************************
    /** Creates new Messages */
    public Messages() {
    } //*************************************************************************
} //*****************************************************************************
