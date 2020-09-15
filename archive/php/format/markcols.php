<?php
include '../../php_includes/Globals.inc';
include 'globals.inc';

global $STARCH_VERSION;
global $TMPDIR;
global $HTMLINCLUDES;
global $STARCHDSN;

require_once 'DB.php';
require_once 'functions.php';
require_once 'conv_tdf.php';
require_once 'conv_csv.php';
require_once 'conv_star.php';
require_once 'conv_xeasy.php';
require_once 'conv_sparky.php';
require_once 'conv_pipp.php';
require_once 'conv_camra.php';
require_once 'conv_nmrview.php';

//foreach( array_keys( $_POST ) as $param ) echo "$param => $_POST[$param]<br>";

$ignore_first = 0;
if( $_POST['headers'] != NULL ) {
    $ignore_first = trim( $_POST['headers'] );
    if( preg_match( '/^[0-9]+$/', $ignore_first ) != 1 )
        $ignore_first = 0;
}

$tagcat = trim( $_POST['tagcat'] );
$filetype = trim( $_POST['filetype'] );
if( (strlen( $tagcat ) < 1) || (strlen( $filetype ) < 1) ) {
    echo "<CENTER><STRONG>Missing parameter!</STRONG>";
    echo "<P>Use your browser's Back button to return to previous page</CENTER>";
    return;
}

//echo "$tagcat<br>";
//echo "$filetype<br>";

delete_old_files( $TMPDIR );

if( $filetype === "xeasy" ) {
    if( ($_FILES['protfile']['error'] != 0) || ($_FILES['protfile']['size'] <= 0) ) {
        echo "<CENTER><STRONG>There was an error uploading protons file, please try again!</STRONG>";
        echo "<P>Use your browser's Back button to return to previous page</CENTER>";
	die;
    }
    $protfile = tempnam( $TMPDIR, "upl" );
    if( ! move_uploaded_file( $_FILES['protfile']['tmp_name'], $protfile ) ) {
        echo "<CENTER><STRONG>There was an error uploading protons file, please try again!</STRONG>";
        echo "<P>Use your browser's Back button to return to previous page</CENTER>";
        die;
    }
    if( ($_FILES['uplfile']['error'] != 0) || ($_FILES['uplfile']['size'] <= 0) ) {
        echo "<CENTER><STRONG>There was an error uploading fragments file, please try again!</STRONG>";
        echo "<P>Use your browser's Back button to return to previous page</CENTER>";
	die;
    }
    $seqfile = tempnam( $TMPDIR, "upl" );
    if( ! move_uploaded_file( $_FILES['uplfile']['tmp_name'], $seqfile ) ) {
        echo "<CENTER><STRONG>There was an error uploading fragments file, please try again!</STRONG>";
        echo "<P>Use your browser's Back button to return to previous page</CENTER>";
        die;
    }
} 
else {
    if( ($_FILES['uplfile']['error'] != 0) || ($_FILES['uplfile']['size'] <= 0) ) {
        echo "<CENTER><STRONG>There was an error uploading the file, please try again!</STRONG>";
        echo "<P>Use your browser's Back button to return to previous page</CENTER>";
	die;
    }
    $tgtfile = tempnam( $TMPDIR, "upl" );
//echo "$tgtfile<br>";
    if( ! move_uploaded_file( $_FILES['uplfile']['tmp_name'], $tgtfile ) ) {
        echo "<CENTER><STRONG>There was an error uploading the file, please try again!</STRONG>";
        echo "<P>Use your browser's Back button to return to previous page</CENTER>";
        die;
    }
}


//
// Convert uploaded file to tab-delimited
//
// $columns is array of tagnames: 3.1 where there is a mapping and original ones that didn't map.
// It is filled in by STAR, XEASY, etc. converters and is null when converting CSV or tab-delimited
// files.
//
$columns = NULL;
if( $filetype === "tab-delimited" )
    $filename = convert_tdf( $tgtfile, $ignore_first );
else if( $filetype === "comma-delimited" ) {
    $filename = convert_csv( $tgtfile, $ignore_first );
}
else if( $filetype === "star" ) {
    $columns = convert_star( $tgtfile, &$filename, trim( $_POST['loop'] ) );
}
else if( $filetype === "xeasy" ) {
    $columns = convert_xeasy( $seqfile, $protfile, &$filename, isset( $_POST['seqnums'] ) );
}
else if( $filetype === "xeasyp" ) {
    if( $_POST["seq"] == NULL ) {
	echo "Missing residue sequence!<BR>\nUse your browser's Back button to return to previous page\n";
	die;
    }
    if( $_POST["num"] == NULL ) $columns = convert_xeasy_p( $tgtfile, $_POST["seq"], 1, &$filename );
    else $columns = convert_xeasy_p( $tgtfile, $_POST["seq"], $_POST["num"], &$filename );
}
else if( $filetype === "sparky" ) {
    $columns = convert_sparky( $tgtfile, &$filename );
}
else if( $filetype === "pipp" ) {
    $columns = convert_pipp( $tgtfile, &$filename );
}
else if( $filetype === "garret" ) {
    $columns = convert_garret( $tgtfile, &$filename );
}
else if( $filetype === "ppm" ) {
    $columns = convert_ppm( $tgtfile, &$filename );
}
else if( $filetype === "format4" ) {
    $columns = convert_f4( $tgtfile, &$filename );
}
else if( $filetype === "format5" ) {
    $columns = convert_f5( $tgtfile, &$filename );
}
else if( $filetype === "nmrview" ) {
    if( $_POST["seq"] == NULL ) {
	echo "Missing residue sequence!<BR>\nUse your browser's Back button to return to previous page\n";
	die;
    }
    if( $_POST["num"] == NULL ) $columns = convert_nmrview( $_POST["seq"], 1, $tgtfile, &$filename );
    else $columns = convert_nmrview( $_POST["seq"], $_POST["num"], $tgtfile, &$filename );
}
else {
    echo "<CENTER><STRONG>Unsupported file type!</STRONG>";
    echo "<P>Use your browser's Back button to return to previous page</CENTER>";
    return;
}
chmod( $filename, 0644 );
//
//foreach( array_keys( $columns ) as $param ) echo "$param => $columns[$param]<br>";

if( $filename == NULL ) {
    echo "<CENTER><STRONG>Conversion failed!</STRONG>";
    echo "<P>Use your browser's Back button to return to previous page</CENTER>";
    return;
}
//
// Show uploaded file
//
$data = show_file( $filename, 10 );
if( $data == NULL ) {
    echo "<CENTER><STRONG>Could not read converted file!</STRONG>";
    echo "<P>Use your browser's Back button to return to previous page</CENTER>";
    return;
}

$numcols = count( $data[1] );
//echo "numcols = $numcols<br>";

//
// Fetch tags
//
$options = array(
    'debug'       => 1,
    'portability' => DB_PORTABILITY_ALL,
);

$conn =& DB::connect( $STARCHDSN, $options );
if ( PEAR::isError( $conn ) )
    die( $conn->getMessage() );

$sql = "select tagname,displname,displseq from starch where tagname like '_";
if( $STARCH_VERSION == "internal" ) $sql .= $tagcat .".%' order by displseq";
else $sql .= $tagcat .".%' and tablegen='Y' order by displseq";
//echo $sql;

$res =& $conn->query( $sql );
if ( PEAR::isError($res) )
    die( $res->getMessage() );
$row = array();
$tags = array();
while( $res->fetchInto( $row ) ) {
//echo "$row[0] => $row[1]<br>";
    if( preg_match( "/\.Sf_ID$/", $row[0] ) == 0 )
	$tags[$row[0]] = $row[1];
//    array_push( $tags, substr( $row[0], strpos( $row[0], '.' ) + 1 ) );
}
$conn->disconnect();
//
// $tags: assoc. array of 3.1 tags and corresp. display labels
//
//foreach( array_keys( $tags ) as $tag )
//    echo "$tag --> $tags[$tag]<br>";

?>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<HTML>
    <HEAD>
        <TITLE>Data converter</TITLE>
        <link REL="stylesheet" TYPE="text/css" HREF="/stylesheets/main.css" TITLE="main stylesheet">
    </HEAD>

    <BODY>
        <CENTER>
            <?php
                readfile( "$HTMLINCLUDES/xmenu.html" );
	    ?>
            <TABLE BORDER="0" CELLPADDING="0" CELLSPACING="0" ALIGN="LEFT">  <!-- layout table -->
            <!-- This is a two column table. It displays a side column menu and the main page -->
                <TR><TD class="sidemenuCol">
		    <?php
		        readfile( "$HTMLINCLUDES/deposit_menu.html" );
		    ?>
		</TD>
		<TD>
<?php

echo "<FORM method=\"get\" name=\"convert_file\" action=\"showstar.php\">\n";
echo "<input type=\"hidden\" name=\"filename\" value=\"$filename\">\n";
echo "<input type=\"hidden\" name=\"tagcat\" value=\"$tagcat\">\n";
//echo "<input type=\"hidden\" name=\"ignore_first\" value=\"$ignore_first\">\n";
echo "<table class=\"content\"  border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"left\">\n";
//echo "<tr><td class=\"topContentTitle\" colspan=\"$numcols\">";
echo "<tr><td class=\"topContentTitle\" colspan=\"3\">";
echo "<h1>STARch: convert ";
if( $filetype == "xeasyp" ) echo " XEASY ";
else if( ($filetype == "format4") || ($filetype == "format5") ) echo " delimited ";
else echo " $filetype ";
$datum = fetch_data_type( $tagcat );
if( $datum == NULL ) echo "data file";
else echo "$datum file";
echo "</h1></td></tr>\n";
echo "<tr><td colspan=\"$numcols\">";

?>

    <P>
        Verify (or select from drop-down lists) column headers for each column in the uploaded file
        and click on <STRONG>Convert</STRONG> when done. Use your browser's <EM>File - Save As</EM>
        menu to save the result.
    <P>
        A valid data table must contain <STRONG>at least</STRONG> the following columns:
	<UL>
	    <LI><STRONG>Comp index ID</STRONG> (Residue sequence number)</LI>
	    <LI><STRONG>Comp ID</STRONG> (Residue label or code)</LI>
	    <LI><STRONG>Atom ID</STRONG> (Atom name)</LI>
	    <LI>Value: either <STRONG>value</STRONG> (and pref. <STRONG>Value error</STRONG>)
	        or both <STRONG>Max. value</STRONG> and <STRONG>Min. value</STRONG></LI>
	</UL>
    <P>
        Columns marked <STRONG>IGNORE COLUMN</STRONG> will be excluded from converted table.
    <P>
        Note: only first 10 rows of uploaded file are shown in the table below.
<?php

echo "</td></tr>\n";
if( $tagcat == "Atom_chem_shift" ) {
    echo "<tr><td colspan=\"$numcols\"><hr></td></tr>\n";
    echo "<tr><td colspan=\"$numcols\">&nbsp;</td></tr>\n";
    echo "<tr><td colspan=\"$numcols\">";
    echo "<input type=\"checkbox\" name=\"ambicodes\" value=\"true\" checked>Include default ambiguity codes</input>";
    echo "</td></tr>\n";
    echo "<tr><td colspan=\"$numcols\">&nbsp;</td></tr>\n";
}

/*
echo "<td colspan=\"3\">";
foreach( array_keys( $tags ) as $tag ) {
    foreach( array_keys( $columns ) as $column ) {
        echo "tag: $tag, column: $column<br>";
    }
}
echo "</td></tr><tr>";
*/

for( $i = 0; $i < $numcols; $i++ ) {
    echo "<td><select name=\"col";
    echo $i;
    echo "\">\n";
    $selected = false;
    foreach( array_keys( $tags ) as $tag ) {
echo "tag: $tag, col: $i, columns[tag]: $columns[$tag] <br>\n";
        echo "<option";
        if( $columns != NULL ) {
// if tag is mapped
	    if( $columns[$tag] === $i ) {
                echo " selected";
		$selected = true;
	    }
	}
        echo " value=\"$tag\">$tags[$tag]</option>\n";
    } // endfor tags
echo "<!-- selected: $selected -->\n";
    echo "<option value=\"skip\"";
// if tag is not mapped, default is "IGNORE COLUMN"
    if( ! $selected ) echo " selected";
    echo ">IGNORE COLUMN</option>\n";
    echo "</select></td>\n";
}
echo "</tr>\n";

foreach( $data as $row ) {
    echo "<tr>";
    foreach( $row as $val ) {
        echo "<td>$val</td>";
    }
    echo "</tr>\n";
}

echo "<tr><td colspan=\"$numcols\">&nbsp;</td></tr>\n";
echo "<tr><td><input type=\"submit\" value=\"convert\"></td></tr>\n";
echo "</table></form></td></tr>\n";
readfile( "$HTMLINCLUDES/footer.html" );
?>

         </TABLE>
    </BODY>
</HTML>
