<?php
include '../../php_includes/Globals.inc';
include "globals.inc";
require_once 'DB.php';
//
// delete files older than 1 day from $dir.
//
function delete_old_files( $dir ) {
    $OLD = time() - 86400; // 24 hours
//    $OLD = time() - 60; // 1 minute - testing

    if( is_dir( $dir ) ) {
        if( $dh = opendir( $dir ) ) {
	    while( ($file = readdir( $dh )) !== false ) {
	        if( ($file == ".") || ($file == "..") ) continue;
//echo "<STRONG>delete_old_files: $dir$file</STRONG><BR>";
                if( fileatime( $dir . $file ) < $OLD ) {
//echo "<STRONG>delete_old_files: deleting $dir$file</STRONG><BR>";
                    unlink( $dir . $file );
		}
	    }
	    closedir( $dh );
	}
    }
}
//
// Return array of file types for given data type (tag category)
//
function get_file_types( $tagcat ) {
    $rc = array();
    
    if( $tagcat === "Atom_chem_shift" ) {
        $rc["xeasy"] = "<A href=\"help/xeasy.shtml\">XEASY</A> both protons (shifts) and sequence (fragments) files";
        $rc["xeasyp"] = "<A href=\"help/xeasy.shtml\">XEASY</A> protons file only";
        $rc["sparky"] = "<A href=\"help/sparky.shtml\">Sparky</A>";
        $rc["nmrview"] = "NMRVIEW <A href=\"help/nmr_ppm.shtml\">PPM</A>";
        $rc["pipp"] = "<A href=\"help/pipp.shtml\">PIPP</A>";
        $rc["garret"] = "CAMRA <A href=\"help/garret.shtml\">Garret</A>";
        $rc["ppm"] = "CAMRA <A href=\"help/cam_ppm.shtml\">PPM</A>";
    }

    $rc["star"] = "<A href=\"help/star.shtml\">NMR-STAR</A> previous version(s)<P><STRONG>NOTE: comma-separated files are preferred</STRONG></P>";
    $rc["tab-delimited"] = "<A href=\"help/csv1.shtml\">tab-delimited: one atom per line</A>, values separated by tab and/or spaces";
    $rc["comma-delimited"] = "<A href=\"help/csv1.shtml\">comma-separated: one atom per line</A>, values separated by commas";

    if( ($tagcat === "Atom_chem_shift") 
     || ($tagcat === "T1") ) {
        $rc["format4"] = "<A href=\"help/csv2.shtml\">tab-delimited: one residue per line</A>, values separated by tab and/or spaces";
        $rc["format5"] = "<A href=\"help/csv2.shtml\">comma-separated: one residue per line</A>, values separated by commas";
    }
//    if( $tagcat === "H_exch_protection_factor" ) {
//        $rc["format4"] = "tab-delimited: one residue per line, values separated by tab and/or spaces";
//    }
//    if( $tagcat === "H_exch_rate" ) {
//        $rc["format4"] = "tab-delimited: one residue per line, values separated by tab and/or spaces";
//    }
    

    return $rc;
}
//
// read space- or tab-delimited file and return first $numlines lines
// skip first line (column headers) if $ignore_first is true
//
function show_file( $filename, $numlines ) {
    global $MAXSTRLEN;
    $handle = @fopen( $filename, "r" );
    if( $handle ) {
        $rc = array();
        $i = 0;
        while( !feof( $handle ) ) {
	    if( ($numlines > 0) && ($i > $numlines) ) break;
	    $buffer = fgets( $handle, $MAXSTRLEN );
//echo "$i : $buffer<br>\n";
//	    if( $ignore_first ) 
//	        if( $i == 0 ) {
//		    $ignore_first = false;
//	            continue;
//		}
	    $i++;
	    $buffer = trim( $buffer );
	    if( strlen( $buffer ) < 1 ) continue;
	    $line = preg_split( "/[\s]+/", $buffer );
	    $rc[$i] = $line;
	}
        fclose( $handle );
	$cnt = 0;
	foreach( $rc as $line )
	    if( $cnt < count( $line ) )
	        $cnt = count( $line );
//echo "max. cols = $cnt<br>\n";
        foreach( $rc as &$line ) {
	    for( $i = count( $line ); $i < $cnt; $i++ )
	        $line[$i] = "?";
	}
	return $rc;
    }
    return NULL;
}
//
// Fetch data types
//
function fetch_data_type( $tagcat ) {
    global $STARCHDSN;
    $options = array(
        'debug'       => 1,
        'portability' => DB_PORTABILITY_ALL,
    );

    $conn =& DB::connect( $STARCHDSN, $options );
    if ( PEAR::isError( $conn ) )
        return NULL;

    $sql = "select datumtype from datumtypes where tagcat='" . $tagcat . "'";
//echo $sql;

    $res =& $conn->query( $sql );
    if ( PEAR::isError($res) ) {
        $conn->disconnect();
	return NULL;
    }
    $row = array();
    if( $res->fetchInto( $row ) )
        $rc = $row[0];
    else
        $rc = NULL;
    $conn->disconnect();
    return $rc;
}
//
// Convert residue sequence string to array.
// params: sequence string
//         starting sequence number
// return: array of (seq.# => code)
//
function convert_sequence( $seq, $start ) {
//echo "SEQ: $seq<br>START $start<br>\n";
    $rc = array();
    $seqtmp = str_split( preg_replace( "/\s+/", "", $seq ) );
//echo "SEQ: $seq<br>START $start<br>\n";
    foreach( $seqtmp as $s ) {
	$rc[$start] = $s;
//echo "rc[ $start ] = $s<br>\n";
	$start++;
    }
    return $rc;
}
//
// Sort chem. shift loop table by residue and atom
// parameters: loop rows
//
function sort_by_atom( $a, $b ) {
    if( ($a["_Atom_chem_shift.Comp_ID"] == $b["_Atom_chem_shift.Comp_ID"])
     && ($a["_Atom_chem_shift.Atom_ID"] == $b["_Atom_chem_shift.Atom_ID"]) )
	return 0;
    if( $a["_Atom_chem_shift.Comp_ID"] == $b["_Atom_chem_shift.Comp_ID"] )
	return( $a["_Atom_chem_shift.Atom_ID"] > $b["_Atom_chem_shift.Atom_ID"] ? 1 : -1 );
    return( $a["_Atom_chem_shift.Comp_ID"] > $b["_Atom_chem_shift.Comp_ID"] ? 1 : -1 );
    
}
//
// Sort chem. shift loop table by ro number
// parameters: loop rows
//
function sort_by_row( $a, $b ) {
    return( $a["_Atom_chem_shift.ID"] > $b["_Atom_chem_shift.ID"] ? 1 : -1 );    
}
?>
