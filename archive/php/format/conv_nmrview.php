<?php
include "globals.inc";
require_once "functions.php";
//
// convert NMRVIEW PPM file.
// params: residue sequence string
//         starting number for residue sequence
//         ppm file name
//         converted file name (output)
// return: array of column headers (NMR-STAR 3.1 tags), output file name
//
function convert_nmrview( $sequence, $start, $protfile, &$filename ) {
    global $MAXSTRLEN;
    global $TMPDIR;
    global $STARCHDSN;

    $filename = tempnam( $TMPDIR, "conv" );
//echo "convert_nmrview( $seqfile, $protfile, $filename, $seqnums )<br>";
    $seq = convert_sequence( $sequence, $start );
//foreach( array_keys( $seq ) as $key ) echo "$key => $seq[$key]<br>";
//
// PPM column order: seq#.atom val ambiguity. Label in $seq[seq#].
// NMR-STAR dictionary order:
    $options = array(
        'debug'       => 1,
        'portability' => DB_PORTABILITY_ALL,
    );
    
    $conn =& DB::connect( $STARCHDSN , $options );
    if ( PEAR::isError( $conn ) )
        die( $conn->getMessage() );
//
// "author" is for BMRB internal use only            
    $sql = "select tagname,compidxid,compid,atomid,val,ambicode from starch "
         . "where tagname like '_Atom_chem_shift.%' and author<>'Y' order by displseq";
//echo $sql;
            
    $res =& $conn->query( $sql );
    if ( PEAR::isError($res) )
        die( $res->getMessage() );

    $tags = array();
    $row = array();
    while( $res->fetchInto( $row ) ) {
        for( $j = 1; $j < 6; $j++ )
            if( $row[$j] == 'Y' )
                $tags[$row[0]] = $j - 1;
    }
    $conn->disconnect();

    $atm = array();
    $in = @fopen( $protfile, "r" );
    if( $in ) {
        $out = @fopen( $filename, "w" );
        if( $out ) {
            $i = 1;
            while( !feof( $in ) ) {
                $buffer = fgets( $in, $MAXSTRLEN );
                $buffer = trim( $buffer );
                if( strlen( $buffer ) < 1 ) continue;
                $vals = preg_split( "/\s+/", $buffer );
                if( count( $vals ) < 3 )
                    die( "Short read in line $i of ppm file" );
                $atm = preg_split( "/\./", $vals[0] );
                fwrite( $out, $atm[0] );
                fwrite( $out, "\t" );
//                if( $atm[0] < 1 ) {
//                    die( "Can handle positive sequence numbers only, sorry." );
//                }
                fwrite( $out, $seq[$atm[0]] );
                fwrite( $out, "\t" );
                fwrite( $out, $atm[1] );
                fwrite( $out, "\t" );
                fwrite( $out, $vals[1] );
                fwrite( $out, "\t" );
                fwrite( $out, $vals[2] );
                fwrite( $out, "\t" );
                fwrite( $out, $vals[3] );
                fwrite( $out, "\n" );                
                $i++;
            } // endwhile
            fclose( $out );
        } // endif $out
        fclose( $in );
        unlink( $protfile );
        return $tags;
    } // endif $in
    else die( "Cannot read $protfile" );
}
?>
