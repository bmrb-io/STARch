<?php
include "globals.inc";
//
// convert CAMRA files.
//
// convert Garret file.
// params: input file name
//         converted file name (output)
// return: array of column headers (NMR-STAR 3.1 tags), output file name
//
// Garret file format:
//  # line comment
//  1 ALA    -- res. # and label
//  C 180.00 -- atom and shift
//  ...
//  ; 0.0    -- end residue
//
function convert_garret( $infile, &$filename ) {
    global $MAXSTRLEN;
    global $TMPDIR;
    global $STARCHDSN;
    
    $filename = tempnam( $TMPDIR, "conv" );
//echo "convert_garret( $infile, $filename )<br>";
    
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
    $sql = "select tagname,compidxid,compid,atomid,val,displseq from starch "
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
            $match = array();
            $parsing_residue = false;
            while( !feof( $in ) ) {
                $buffer = fgets( $in, $MAXSTRLEN );
                $buffer = trim( $buffer );
                if( strlen( $buffer ) < 1 ) continue;
                if( preg_match( "/^#.*/", $buffer ) == 1 ) continue; // line comment
                if( preg_match( "/(\d+)\s+([a-zA-Z]+)/", $buffer, &$match ) == 1 ) {
                    $parsing_residue = true;
                    $seq_no = $match[1];
                    $label = $match[2];
                }
                else if( $parsing_residue ) {
                    if( preg_match( "/;(\s+.*)*/", $buffer ) == 1 ) { // end residue
                        $parsing_residue = false;
                    }
                    else {
                        $vals = preg_split( "/\s+/", $buffer );
                        if( count( $vals ) < 2 )
                            die( "Short read in line $i of input file" );
                        fwrite( $out, $seq_no );    // seq #
                        fwrite( $out, "\t" );
                        fwrite( $out, $label );    // residue
                        fwrite( $out, "\t" );
                        fwrite( $out, $vals[0] );    // atom
                        fwrite( $out, "\t" );
                        if( preg_match( "/-?9+\.9+/", $vals[1] ) == 1 )
                            fwrite( $out, "?" );    // shift: 999.999 is a NULL
                        else fwrite( $out, $vals[1] );
                        fwrite( $out, "\n" );
                    }
                } // endif parsing residue
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
//
// convert PPM file.
// params: input file name
//         converted file name (output)
// return: array of column headers (NMR-STAR 3.1 tags), output file name
//
// Garret file format:
//  !Sequence:AD
//  1:ALA_1:N    180.00
//  1:ASP_2:C    30.00
//  ...
//
function convert_ppm( $infile, &$filename ) {
    global $MAXSTRLEN;
    global $TMPDIR;
    global $STARCHDSN;

    $filename = tempnam( $TMPDIR, "conv" );
//echo "convert_ppm( $infile, $filename )<br>";
    
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
    $sql = "select tagname,compidxid,compid,atomid,val,displseq from starch "
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
            $match = array();
            $parsing_residue = false;
            while( !feof( $in ) ) {
                $buffer = fgets( $in, $MAXSTRLEN );
                $buffer = trim( $buffer );
                if( strlen( $buffer ) < 1 ) continue;
                if( preg_match( "/^\d+:([a-zA-Z]+)_(\d+):([a-zA-Z0-9]+)\s+(.+)/", $buffer, &$match ) == 1 ) {
                    fwrite( $out, $match[2] );
                    fwrite( $out, "\t" );
                    fwrite( $out, $match[1] );
                    fwrite( $out, "\t" );
                    fwrite( $out, $match[3] );
                    fwrite( $out, "\t" );
                    if( preg_match( "/-?[9*]+\.[9*]+/", $match[4] ) == 1 )
                        fwrite( $out, "?" );    // shift: 999.999 is a NULL
                    else fwrite( $out, $match[4] );
                    fwrite( $out, "\n" );
                }
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
