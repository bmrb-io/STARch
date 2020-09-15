<?php
include "globals.inc";
//
// convert comma-delimited file.
// removes blank lines from the file.
// if ignore_first is true, strips first row.
//
function convert_csv( $filename, $ignore_first ) {
    global $MAXSTRLEN;
    global $TMPDIR;

    $outfile = tempnam( $TMPDIR, "conv" );
    $in = @fopen( $filename, "r" );
    if( $in ) {
        $out = @fopen( $outfile, "w" );
	if( $out ) {
            $i = 0;
            while( !feof( $in ) ) {
	        $buffer = fgetcsv( $in, $MAXSTRLEN );
                $i++;
//echo "$i : $buffer : $ignore_first<br>\n";
                if( ($ignore_first != NULL) && ($i > $ignore_first) ) {
//echo "processing<br>\n";
  		    if( (count( $buffer ) == 1) && ($buffer[0] == NULL) )
		        continue; // blank line
		    foreach( $buffer as $val ) {
	                $val = trim( $val );
	                if( strlen( $val ) < 1 ) fwrite( $out, "?" );
		        else fwrite( $out, $val );
		        fwrite( $out, "\t" );
		    }
		    fwrite( $out, "\n" );
		} // endif $ignore_first
	    } // endwhile
	    fclose( $out );
	} // endif $out
	else $outfile = NULL;
        fclose( $in );
	unlink( $filename );
    } // endif $in
    else $outfile = NULL;
    chmod( $outfile, 0644 );
    return $outfile;
}
//
// convert "format4" (comma-delimited, one residue per line) file.
//
function convert_f5( $infile, &$outfile ) {
    global $MAXSTRLEN;
    global $TMPDIR;
    global $STARCHDSN;

    $outfile = tempnam( $TMPDIR, "conv" );

// NMR-STAR dictionary tags
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
        $out = @fopen( $outfile, "w" );
	if( $out ) {
	    $cols = array();
            $i = 0;
            while( !feof( $in ) ) {
	        $buffer = fgetcsv( $in, $MAXSTRLEN );
                $i++;
                if( $i == 1 ) {
                    for( $j = 0; $j < count( $buffer ); $j++ )
                        $cols[$j] = strtoupper( $buffer[$j] );
                }
                else {
                    if( count( $buffer ) < 3 ) continue; // skip short rows
                    for( $j = 2; $j < count( $buffer ); $j++ ) {
                        fwrite( $out, $buffer[1] ); // seq #
                        fwrite( $out, "\t" );
                        fwrite( $out, $buffer[0] ); // label
                        fwrite( $out, "\t" );
                        fwrite( $out, $cols[$j] ); // atom
                        fwrite( $out, "\t" );
	                if( strlen( trim( $buffer[$j] ) ) < 1 ) fwrite( $out, "?" );
		        else fwrite( $out, trim( $buffer[$j] ) );  // value
                        fwrite( $out, "\n" );
                    } // endfor
                }
	    } // endwhile
	    fclose( $out );
	} // endif $out
	else $outfile = NULL;
        fclose( $in );
	unlink( $infile );
    } // endif $in
    else $outfile = NULL;
    return $tags;
}
?>
