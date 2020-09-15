<?php
include '../../php_includes/Globals.inc';
include 'globals.inc';
include 'functions.php';

global $STARCH_VERSION;
global $STARCHDSN;
global $MAXSTRLEN;

require_once 'DB.php';

parse_str( $_SERVER['QUERY_STRING'], $params );

$filename = trim( $params['filename'] );
$tagcat = trim( $params['tagcat'] );
//$ignore_first = trim( $params['ignore_first'] );
if( (strlen( $filename ) < 1) || (strlen( $tagcat ) < 1) ) {
    echo "Missing parameter!\n";
    return;
}

//foreach( array_keys( $params ) as $key ) echo "$key => $params[$key]<br>\n";
//echo "\n\n";

//
// DB
//
$options = array(
    'debug'       => 1,
    'portability' => DB_PORTABILITY_ALL,
);
$conn =& DB::connect( $STARCHDSN, $options );
if ( PEAR::isError( $conn ) )
    die( $conn->getMessage() );
//
// fetch tags in dictionary order, mark value tags
//
$sql = "select tagname,val,err,maxval,minval,rowidx,displseq from starch where tagname like '_";
if( $STARCH_VERSION == "internal" )
    $sql .= $tagcat . ".%' and tagname<>'_" . $tagcat . ".Sf_ID' and author<>'Y' order by displseq";
else $sql .= $tagcat . ".%' and tagname<>'_" . $tagcat . ".Sf_ID' and tablegen='Y' order by displseq";
//echo $sql;

$res =& $conn->query( $sql );
if ( PEAR::isError($res) )
    die( $res->getMessage() );
$row = array();
$cols = array();
$value_tags = array();
$idxtag = -1;
$i = 0;
while( $res->fetchInto( $row ) ) {
//    $tag = substr( $row[0], strpos( $row[0], '.' ) + 1 );
    $tag = $row[0];
    $cols[$tag] = -1;
    if( ($row[1] === "Y") || ($row[2] === "Y") || ($row[3] === "Y") || ($row[4] === "Y") )
        $value_tags[] = $tag;
    if( $row[5] === "Y" ) $idxtag = $i;
    $i++;
}

foreach( array_keys( $cols ) as $tag ) {
    foreach( array_keys( $params ) as $key ) {
        if( preg_match( "/col[0-9]+/", $key ) != 0 ) {
//echo "tag: $tag, param: $key, val = $params[$key]\n";
            if( $params[$key] === $tag ) {
	        $cols[$tag] = substr( $key, 3 );
		break;
	    }
	}
    }
}

$loop = array();
$looprow = array();
//
// read file into a table
//
$handle = @fopen( $filename, "r" );
if( ! $handle ) {
    echo "Error reading input!\n";
    return;
}

$lineno = 0;
$value = false;
while( !feof( $handle ) ) {
    $buffer = fgets( $handle, $MAXSTRLEN );
    $lineno++;
    $buffer = trim( $buffer );
    if( strlen( $buffer ) < 1 ) continue;
    $line = preg_split( "/[\s]+/", $buffer );
    $colno = 0;
    $looprow[$tag] = $lineno;
    foreach( array_keys( $cols ) as $tag ) {
        if( ($cols[$tag] >= count( $line )) || ($cols[$tag] == -1) ) {
    	    $looprow[$tag] = "?";
    	}
    	else {
    	    if( $line[$cols[$tag]] == "." ) $looprow[$tag] = "?";
    	    else $looprow[$tag] = $line[$cols[$tag]];
    	}
	$colno++;
    }
    $loop[] = $looprow;
}
fclose( $handle );
unlink( $filename );

$compid = "";
$atomid = "";
$ambicode = "?";
if( $params["tagcat"] == "Atom_chem_shift" ) {
    if( $params["ambicodes"] == "true" ) {
	usort( $loop, "sort_by_atom" );
	for( $i = 0; $i < count( $loop ); $i++ ) {
	    if( ($compid == "") || ($compid != $loop[$i]["_Atom_chem_shift.Comp_ID"])
	     || ($atomid == "") || ($atomid != $loop[$i]["_Atom_chem_shift.Atom_ID"]) ) {
		$compid = $loop[$i]["_Atom_chem_shift.Comp_ID"];
		$atomid = $loop[$i]["_Atom_chem_shift.Atom_ID"];
		$sql = "select a.ambicode from atoms a join residues r on r.id=a.resid ";
		$sql .= "where (r.label='" . $compid . "' or r.code='" . $compid . "') ";
		$sql .= "and a.name='" . $atomid . "'";
		$res =& $conn->query( $sql );
		if ( ! PEAR::isError($res) ) {
		    if( $res->fetchInto( $row ) ) {
			if( $row[0] != 0 ) $ambicode = $row[0];
			else $ambicode = "?";
		    }
		    else $ambicode = "?";
		}
		else $ambicode = "?";
	    }
	    if( $loop[$i]["_Atom_chem_shift.Ambiguity_code"] == "?" )
	        $loop[$i]["_Atom_chem_shift.Ambiguity_code"] = $ambicode;
	} // endforeach
	usort( $loop, "sort_by_row" );
    }
}
$conn->disconnect();

header( "Content-Type: text/plain" );
header( "Content-Disposition: inline; filename=data.str" );
//
// print ambiguity code header
//
if( file_exists( "ambi.txt" ) ) readfile( "ambi.txt" );
//
// print loop header
//
echo "loop_\n";
foreach( array_keys( $cols ) as $tag )
//    echo "    _$tagcat.$tag\n";
    echo "    $tag\n";
echo "\n";
foreach( $loop as $looprow ) {
    foreach( array_keys( $cols ) as $col )
	printf( "%-15s", $looprow[$col] );
    echo "\n";
}
//
// end loop
//
echo "\nstop_\n";

?>
