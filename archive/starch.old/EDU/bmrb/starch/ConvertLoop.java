/*
 * ConvertLoop.java
 *
 * Created on July 21, 2004, 5:16 PM
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/ConvertLoop.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2004/12/15 20:55:49 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: ConvertLoop.java,v $
 * Revision 1.5  2004/12/15 20:55:49  dmaziuk
 * new classes
 *
 * Revision 1.4  2004/12/14 00:40:24  dmaziuk
 * more functionality
 *
 * Revision 1.3  2004/12/08 23:57:48  dmaziuk
 * added new classes
 *
 * Revision 1.2  2004/11/29 23:19:15  dmaziuk
 * updated loop converter
 *
 * Revision 1.1  2004/07/22 20:01:51  dmaziuk
 * added command-line 2.1 to 3.0 loop converter
 * */

package EDU.bmrb.starch;

/**
 * Command line interface for loop converter.
 * <P>
 * Converts assigned chemical shifts loop between NMR-STAR 2.1 and 3.0, deleting
 * rows where shift value is null and renumbering rows in the process. Command
 * line switches allow adding extra information (shift error, fields not present
 * in NMR-STAR 2.1).
 * <P> 
 * Input file must be an NMR-STAR loop, starting with "loop_" and ending with
 * "stop_". Output file is a pretty-printed NMR-STAR loop starting with "loop_"
 * and ending with "stop_".
 * <P>
 * Usage:
 * <UL>
 *  <LH>java EDU.bmrb.starch.ConvertLoop [-h] [-i FILE] [-l FORMAT] [-o FILE] 
 *      [-x FORMAT] [-e NUM] [-b] [-r NUM] [-a NUM]</LH>
 *  <LI>-h: print usage summary and exit</LI>
 *  <LI>-i FILE: input filename, default: stdin</LI>
 *  <LI>-l FORMAT: input file format, one of STAR2, STAR3 (default: STAR3)</LI>
 *  <LI>-o FILE: output filename, default: stdout</LI>
 *  <LI>-x FORMAT: output file FORMAT, one of STAR2, STAR3 (default: STAR3)</LI>
 *  <LI>-r NUM: set chemical shift error to NUM in rows where it is null/LI>
 *  <LI>-e NUM: set _Atom_chem_shift.Entity_ID to NUM (replaces any existing entity IDs)</LI>
 *  <LI>-b: set Entry_ID to 'NEED_ACC_NUM' in rows that don't have entry ID</LI>
 *  <LI>-a NUM: set _Atom_chem_shift.Entity_assembly_ID to NUM (replaces any existing assembly IDs)</LI>
 * </UL>
 * -a, -b, and -e switches work only with NMR-STAR 3 output (NMR-STAR 2.1 does
 * not have corresp. columns in chemical shifts loop).
 *
 * @author  dmaziuk
 */
public class ConvertLoop {
    private final static boolean DEBUG = false; //true;
    /** Creates a new instance of ConvertLoop */
    public ConvertLoop() {
    }
    /** Prints usage summary. */
    public static void usage() {
        System.out.print( "Usage: java EDU.bmrb.starch.ConvertLoop [-h] [-i FILE]" );
        System.out.print( " [-f FORMAT] [-o FILE] [-x FORMAT] [-e NUM] [-b]" );
        System.out.println( " [-r NUM] [-a NUM]" );
        System.out.println( " -h: print usage summary and exit" );
        System.out.println( " -i FILE: input filename, default: stdin" );
        System.out.print( " -f FORMAT: input file format, one of STAR2, STAR3 " );
        System.out.println( "(default: STAR2)" );
        System.out.println( " -o FILE: output filename, default: stdout" );
        System.out.print( " -x FORMAT: output file FORMAT, one of STAR2, STAR3, XEASY " );
        System.out.println( "(default: STAR3)" );
        System.out.print( " -a NUM: set entity assembly ID to NUM in all rows" );
        System.out.println( " (replaces any existing assembly ID)" );
        System.out.print( "-b: set Entry_ID to 'NEED_ACC_NUM' in rows that don't" );
        System.out.println( " have entry ID" );
        System.out.print( " -e NUM: set entity ID to NUM " );
        System.out.println( "(replaces any existing entity IDs)" );
        System.out.print( " -l NUM: set Assigned_chemical_shift_list_ID to NUM in all rows" );
        System.out.println( " (replaces any existing IDs)" );
        System.out.print( " -r NUM: set chemical shift error to NUM in rows" );
        System.out.println( " that don't have shift error" );
        System.out.print( " -s NUM: add (STAR3) .Sf_ID tags with value of NUM " );
        System.out.println( "(.Sf_ID tags are not printed by default)" );
    } //************************************************************************
    /** Main method.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
long now = System.currentTimeMillis();
        try {
            gnu.getopt.Getopt g = new gnu.getopt.Getopt( "ConvertLoop", args,
            "hbi:o:f:x:e:r:a:s:l:" );
            int opt;
            String infile = null;
            String outfile = null;
            String tmp;
            int informat = LoopTable.STAR2;
            int outformat = LoopTable.STAR3;
            int eid = 0;
            boolean add_eid = false;
            int assid = 0;
            boolean add_assid = false;
            boolean add_accno = false;
            boolean add_sfid = false;
            int sfid = 0;
            int listid = 0;
            boolean add_listid = false;
            float error = Float.NaN;
            while( (opt = g.getopt()) != -1 )
                switch( opt ) {
                    case 'a' :
                        tmp = g.getOptarg();
                        try { assid = Integer.parseInt( tmp ); }
                        catch( NumberFormatException ne ) {
                            usage();
                            System.exit( 1 );
                        }
                        add_assid = true;
                        break;
                    case 'b' :
                        add_accno = true;
                        break;
                    case 'e' :
                        tmp = g.getOptarg();
                        try { eid = Integer.parseInt( tmp ); }
                        catch( NumberFormatException ne ) {
                            usage();
                            System.exit( 1 );
                        }
                        add_eid = true;
                        break;
                    case 'l' :
                        tmp = g.getOptarg();
                        try { listid = Integer.parseInt( tmp ); }
                        catch( NumberFormatException ne ) {
                            usage();
                            System.exit( 1 );
                        }
                        add_listid = true;
                        break;
                    case 'i' :
                        infile = g.getOptarg();
                        break;
                    case 'f' :
                        tmp = g.getOptarg();
                        if( tmp.toUpperCase().equals( "STAR2" ) )
                            informat = LoopTable.STAR2;
                        else if( tmp.toUpperCase().equals( "STAR3" ) )
                            informat = LoopTable.STAR3;
                        else {
                            usage();
                            System.exit( 1 );
                        }
                        break;
                    case 'o' :
                        outfile = g.getOptarg();
                        break;
                    case 'r' :
                        tmp = g.getOptarg();
                        try { error = Float.parseFloat( tmp ); }
                        catch( NumberFormatException ne ) {
                            usage();
                            System.exit( 1 );
                        }
                        break;
                    case 's' :
                        tmp = g.getOptarg();
                        try { sfid = Integer.parseInt( tmp ); }
                        catch( NumberFormatException ne ) {
                            usage();
                            System.exit( 1 );
                        }
                        add_sfid = true;
                        break;
                    case 'x' :
                        tmp = g.getOptarg();
                        if( tmp.toUpperCase().equals( "STAR2" ) )
                            outformat = LoopTable.STAR2;
                        else if( tmp.toUpperCase().equals( "STAR3" ) )
                            outformat = LoopTable.STAR3;
                        else if( tmp.toUpperCase().equals( "XEASY" ) )
                            outformat = LoopTable.XEASY;
                        else {
                            usage();
                            System.exit( 1 );
                        }
                        break;
                    default :
                        usage();
                        return;
                }
            LoopTable t = new LoopTable();
            t.connect();
            t.createTable();
            java.io.Reader in;
            if( infile == null ) 
                in = new java.io.InputStreamReader( System.in, "ISO-8859-15" );
            else in = new java.io.InputStreamReader( 
            new java.io.FileInputStream( infile ), "ISO-8859-15" );
            switch( informat ) {
                case LoopTable.STAR2 : 
                    Star2Reader r2 = new Star2Reader( t );
                    r2.parse( in );
                    in.close();
                    r2 = null;
                    break;
                case LoopTable.STAR3 :
                    Star3Reader r3 = new Star3Reader( t );
                    r3.parse( in );
                    in.close();
                    r3 = null;
            }
//t.print( System.out );
            t.deleteEmptyRows();
//
//FIXME: nomenclature conversion instread of plain copy
//
//System.out.println( "Before copyAuthorData()" );
//t.print( System.out );
            if( informat != LoopTable.STAR3 ) t.copyFromAuthorData();
//t.print( System.out );
//NOTE: if residue seq, lable, & atom name are missing, sort and reindex
// won't work (& will run out of memory)
            t.sort();
//t.print( System.out );
            t.reindexRows();
//t.print( System.out );
            if( add_accno ) t.addEntryId();
            if( add_eid ) t.addEntityId( eid );
            if( add_assid ) t.addAssemblyId( assid );
            if( add_sfid ) t.addSaveframeId( sfid );
            if( add_listid ) t.addListId( listid );
            if( ! Float.isNaN( error ) ) t.addShiftError( error );
            t.addIsotopes();
            if( DEBUG ) {
                System.out.println( "*** UPDATED" );
                t.print( System.out );
            }
            java.io.PrintWriter out;
            if( outfile == null ) out = new java.io.PrintWriter( 
            new java.io.OutputStreamWriter( System.out, "ISO-8859-15" ) );
            else out = new java.io.PrintWriter( new java.io.OutputStreamWriter( 
            new java.io.FileOutputStream( outfile ), "ISO-8859-15" ) );
            Writer w;
            switch( outformat ) {
                case LoopTable.STAR2 :
                    w = new Star2Writer( t );
                    w.print( out );
                    break;
                case LoopTable.STAR3 :
                    w = new Star3Writer( t );
                    w.setSfIdFlag( add_sfid );
                    w.print( out );
                    break;
                case LoopTable.XEASY :
                    w = new XeasyWriter( t );
                    w.print( out );
                    break;
            }
            out.flush();
            t.disconnect();
System.err.println( "Elapsed: " + (System.currentTimeMillis() - now) );
        }
        catch( Exception e ) {
            System.err.println( e.getMessage() );
            e.printStackTrace();
        }
    } //************************************************************************
}
