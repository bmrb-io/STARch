<?php
include "globals.inc";
//
// convert SPARKY files.
// params: input file name
//         converted file name (output)
// return: array of column headers (NMR-STAR 3.1 tags), output file name
//
// SPARKY file format:
//  group \s+ atom \s+ isotope \s+ shift val. \s+ std.dev. \s+ # assignments
//
//  group is residue code + residue seq. #, e.g. R2
//  isotope is isotope # + atom type, e.g. 13C
//  shift value is average from # of assignments
//  standard deviation is calculated from value and # of assignments,
//     can be an indication of NMR-STAR shift error
//  NMR-STAR figure of merit may be related to # of assignments. 
//
function convert_sparky( $infile, &$filename ) {
    global $MAXSTRLEN;
    global $TMPDIR;
    global $STARCHDSN;

    $filename = tempnam( $TMPDIR, "conv" );
//echo "convert_sparky( $infile, $filename )<br>";
    
// NMR-STAR dictionary order:
    $options = array(
        'debug'       => 1,
        'portability' => DB_PORTABILITY_ALL,
    );
    
    $conn =& DB::connect( $STARCHDSN, $options );
    if ( PEAR::isError( $conn ) )
        die( $conn->getMessage() );
//
// "author" is for BMRB internal use only            
    $sql = "select tagname,compidxid,compid,atomid,atomtype,isotope,val,displseq from starch "
         . "where tagname like '_Atom_chem_shift.%' and author<>'Y' order by displseq";
//echo $sql;
            
    $res =& $conn->query( $sql );
    if ( PEAR::isError($res) )
        die( $res->getMessage() );

    $tags = array();
    $row = array();
    while( $res->fetchInto( $row ) ) {
        for( $j = 1; $j < 7; $j++ )
            if( $row[$j] == 'Y' )
                $tags[$row[0]] = $j - 1;
    }
    $conn->disconnect();

    $in = @fopen( $infile, "r" );
    if( $in ) {
        $out = @fopen( $filename, "w" );
        if( $out ) {
            $i = 1;
            while( !feof( $in ) ) {
                $buffer = fgets( $in, $MAXSTRLEN );
                $buffer = trim( $buffer );
                if( strlen( $buffer ) < 1 ) continue;
                $vals = preg_split( "/\s+/", $buffer );
                if( count( $vals ) < 6 )
                    die( "Short read in line $i of input file" );
                fwrite( $out, substr( $vals[0], 1 ) );    // seq #
                fwrite( $out, "\t" );
                fwrite( $out, substr( $vals[0], 0, 1 ) ); // code
                fwrite( $out, "\t" );
                fwrite( $out, $vals[1] );
                fwrite( $out, "\t" );
                fwrite( $out, preg_replace( "/\d+/", "", $vals[2] ) );
                fwrite( $out, "\t" );
                fwrite( $out, preg_replace( "/\D+/", "", $vals[2] ) );
                fwrite( $out, "\t" );
                fwrite( $out, $vals[3] );
                fwrite( $out, "\t" );
                fwrite( $out, $vals[4] );
                fwrite( $out, "\t" );
                fwrite( $out, $vals[5] );
                fwrite( $out, "\n" );                
                $i++;
            } // endwhile
            fclose( $out );
        } // endif $out
        fclose( $in );
        unlink( $infile );
        return $tags;
    } // endif $in
    else die( "Cannot read $infile" );
}
?>
