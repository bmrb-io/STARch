<?php
include "globals.inc";
require_once "functions.php";
//
// convert XEASY files.
// params: sequence file name
//         protons file name
//         converted file name (output)
//         seq. numbers flag: if true, 2nd column of sequence file contains numbers
//          (otherwise residue number is the line number)
// return: array of column headers (NMR-STAR 3.1 tags), output file name
//
function convert_xeasy( $seqfile, $protfile, &$filename, $seqnums ) {
    global $TMPDIR;

    $filename = tempnam( $TMPDIR, "conv" );
//echo "convert_xeasy( $seqfile, $protfile, $filename, $seqnums )<br>";
    $seq = conv_xeasy_frags( $seqfile, $seqnums );
//foreach( array_keys( $seq ) as $key ) echo "$key => $seq[$key]<br>";
    conv_xeasy_prot( $protfile, $seq, $filename );
    return fetch_xeasy_tags();
}
//
// convert XEASY protons file and sequence string
// params: protons file name
//         sequence string
//         starting seq.#
//         converted file name (output)
// return: array of column headers (NMR-STAR 3.1 tags), output file name
//
function convert_xeasy_p( $protfile, $seq, $start, &$filename ) {
    global $TMPDIR;

    $filename = tempnam( $TMPDIR, "conv" );
    $residues = convert_sequence( $seq, $start );
    conv_xeasy_prot( $protfile, $residues, $filename );
    return fetch_xeasy_tags();
}
//
// fetch NMR-STAR tags
// XEASY column order: #, val, err, atom, seq#. Label in $seq[seq#].
// return: list of tags in NMR-STAR dictionary order
//
function fetch_xeasy_tags() {
    global $STARCHDSN;
    $options = array(
        'debug'       => 1,
        'portability' => DB_PORTABILITY_ALL,
    );
    
    $conn =& DB::connect( $STARCHDSN, $options );
    if ( PEAR::isError( $conn ) )
        die( $conn->getMessage() );
//
// "author" is for BMRB internal use only            
    $sql = "select tagname,rowidx,val,err,atomid,compidxid,compid,displseq from starch "
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
    return $tags;
}
//
// Convert XEASY protons file.
// params: protons file name
//         array of residue labels (seq.# => label)
//         output filename
// return: output filename
//
function conv_xeasy_prot( $protfile, $comps, &$outfile ) {
    global $MAXSTRLEN;
    $in = @fopen( $protfile, "r" );
    if( $in ) {
        $out = @fopen( $outfile, "w" );
        if( $out ) {
            $vals = array();
            $i = 1;
            while( !feof( $in ) ) {
                $buffer = fgets( $in, $MAXSTRLEN );
                $buffer = trim( $buffer );
                if( strlen( $buffer ) < 1 ) continue;
                $vals = preg_split( "/\s+/", $buffer );
                if( count( $vals ) < 5 )
                    die( "Short read in line $i of protons file" );
                fwrite( $out, $vals[0] );
                fwrite( $out, "\t" );
                if( $vals[1] == "999.000" ) fwrite( $out, "?" );
                else fwrite( $out, $vals[1] );
                fwrite( $out, "\t" );
                fwrite( $out, $vals[2] );
                fwrite( $out, "\t" );
                fwrite( $out, $vals[3] );
                fwrite( $out, "\t" );
                fwrite( $out, $vals[4] );
                fwrite( $out, "\t" );
                fwrite( $out, $comps[$vals[4]] );
                fwrite( $out, "\n" );                
                $i++;
            } // endwhile
            fclose( $out );
        } // endif $out
        else die( "Cannot open $outfile for writing" );
        fclose( $in );
        unlink( $protfile );
    } // endif $in
    else die( "Cannot read $protfile" );
    return $outfile;
}
//
// convert XEASY fragments file
// params: fragments file name
//         seq.# flag: if true, read sequence numbers from 2nd column
//                     (otherwise number them sequentially from 1)
// return: array of residue labels (seq.# => label)
//
function conv_xeasy_frags( $fragfile, $seqnums ) {
    global $MAXSTRLEN;
    $in = @fopen( $fragfile, "r" );
    if( $in ) {
        $seq = array();
	$vals = array();
    	$i = 1;
    	while( !feof( $in ) ) {
	    $buffer = fgets( $in, $MAXSTRLEN );
    	    $buffer = trim( $buffer );
    	    if( strlen( $buffer ) < 1 ) continue;
    	    $vals = preg_split( "/\s+/", $buffer );
    	    if( $seqnums && (count( $vals ) > 1) ) $seq[$vals[1]] = $vals[0];
    	    else $seq[$i] = $vals[0];
	    $i++;
        }
        fclose( $in );
        unlink( $fragfile );
    }
    else die( "Cannot read $seqfile" );
    return $seq;
}
?>
