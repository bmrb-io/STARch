<?php
include "globals.inc";
//
// convert tab- or space-delimited file.
// removes blank lines from the file.
// if ignore_first is true, strips first row.
//
function convert_tdf( $filename, $ignore_first ) {
    global $MAXSTRLEN;
    global $TMPDIR;

    $outfile = tempnam( $TMPDIR, "conv" );
    $in = @fopen( $filename, "r" );
    if( $in ) {
        $out = @fopen( $outfile, "w" );
	if( $out ) {
            $i = 0;
            while( !feof( $in ) ) {
	        $buffer = fgets( $in, $MAXSTRLEN );
                $i++;
//echo "$i : $buffer : $ignore_first<br>\n";
                if( ($ignore_first != NULL) && ($i > $ignore_first) ) {
//echo "processing<br>\n";
	            $buffer = trim( $buffer );
	            if( strlen( $buffer ) < 1 ) continue;
		    fwrite( $out, $buffer );
		    fwrite( $out, "\n" );
		} // endif $ignore_first
	    } // endwhile
	    fclose( $out );
//            chmod( $outfile, 0644 );
	} // endif $out
	else $outfile = NULL;
        fclose( $in );
	unlink( $filename );
    } // endif $in
    else $outfile = NULL;
    return $outfile;
}
//
// convert "format4" (tab- or space-delimited, one residue per line) file.
//
function convert_f4( $infile, &$outfile ) {
    global $MAXSTRLEN;
    global $TMPDIR;
    global $STARCHDSN;

    $outfile = tempnam( $TMPDIR, "conv" );

// NMR-STAR dictionary tags
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

    $in = @fopen( $infile, "r" );
    if( $in ) {
        $out = @fopen( $outfile, "w" );
	if( $out ) {
	    $cols = array();
            $i = 0;
            while( !feof( $in ) ) {
	        $buffer = fgets( $in, $MAXSTRLEN );
                $buffer = trim( $buffer );
	        if( strlen( $buffer ) < 1 ) continue;
                $i++;
                $row = preg_split( "/\s+/", $buffer );
                if( $i == 1 ) {
                    for( $j = 0; $j < count( $row ); $j++ )
                        $cols[$j] = strtoupper( $row[$j] );
                }
                else {
                    if( count( $row ) < 3 ) continue; // skip short rows
                    for( $j = 2; $j < count( $row ); $j++ ) {
                        fwrite( $out, $row[1] ); // seq #
                        fwrite( $out, "\t" );
                        fwrite( $out, $row[0] ); // label
                        fwrite( $out, "\t" );
                        fwrite( $out, $cols[$j] ); // atom
                        fwrite( $out, "\t" );
                        fwrite( $out, $row[$j] ); // value
                        fwrite( $out, "\n" );
                    } // endfor
                }
	    } // endwhile
	    fclose( $out );
//            chmod( $outfile, 0644 );
	} // endif $out
	else $outfile = NULL;
        fclose( $in );
	unlink( $infile );
    } // endif $in
    else $outfile = NULL;
    return $tags;
}
?>
