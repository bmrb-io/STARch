<?php
include "globals.inc";
//
// convert PIPP files.
// params: input file name
//         converted file name (output)
// return: array of column headers (NMR-STAR 3.1 tags), output file name
//
//
function convert_pipp( $infile, &$filename ) {
    global $MAXSTRLEN;
    global $TMPDIR;
    global $STARCHDSN;

    $filename = tempnam( $TMPDIR, "conv" );
//echo "convert_pipp( $infile, $filename )<br>";
    
// NMR-STAR dictionary order:
    global $DSN;
    $options = array(
        'debug'       => 1,
        'portability' => DB_PORTABILITY_ALL,
    );
    
    $conn =& DB::connect( $STARCHDSN, $options );
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

    $match = array();
    $parsing_residue = false;
    $parsing_atoms = false;
    $in = @fopen( $infile, "r" );
    if( $in ) {
        $out = @fopen( $filename, "w" );
        if( $out ) {
            $i = 1;
            while( !feof( $in ) ) {
                $buffer = fgets( $in, $MAXSTRLEN );
                $buffer = trim( $buffer );
                if( strlen( $buffer ) < 1 ) continue;
// new residue
                if( preg_match( "/RES_ID\s+(\d+)/", $buffer, &$match ) == 1 ) {
                    $parsing_residue = true;
                    $seq_no = $match[1];
                }
                if( $parsing_residue == true ) {
// end of residue
                    if( preg_match( "/END_RES_DEF/", $buffer ) == 1) {
                        $parsing_residue = false;
                        $parsing_atoms = false;
                    }
                    else if (  preg_match( "/RES_TYPE\s+([a-zA-Z]+)/", $buffer, &$match ) == 1 ) {
                        $label = $match[1];
                    }
                    else if ( preg_match( "/HETEROGENEITY\s+.+/", $buffer ) == 1 ) {
                        $parsing_atoms = true;
                    }
                    else if ( preg_match( "/SPIN_SYSTEM_ID\s+.+/", $buffer ) == 1 ) {
                        $parsing_atoms = true;
                    }
                    else {
                        if( $parsing_atoms == true ) {
                            $vals = preg_split( "/\s+/", $buffer );
                            if( count( $vals ) < 2 )
                                die( "Short read in line $i of input file" );
                            fwrite( $out, $seq_no );   // seq #
                            fwrite( $out, "\t" );
                            fwrite( $out, $label );    // code
                            fwrite( $out, "\t" );
                            fwrite( $out, $vals[0] );  // atom
                            fwrite( $out, "\t" );
                            fwrite( $out, $vals[1] );  // shift
                            fwrite( $out, "\n" );
                        } // endif parsing_atoms
                    }
                } // endif parsing_residue
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
