<?php
include '../php_includes/Globals.inc';
include "globals.inc";

require_once 'DB.php';

//
// Fetch data types
//
global $STARCHDSN;
$options = array(
    'debug'       => 1,
    'portability' => DB_PORTABILITY_ALL,
);

$conn =& DB::connect( $STARCHDSN, $options );
if ( PEAR::isError( $conn ) )
    die( $conn->getMessage() );

$sql = "select datumtype,tagcat from datumtypes where tablegen='Y'";
//echo $sql;

$res =& $conn->query( $sql );
if ( PEAR::isError($res) )
    die( $res->getMessage() );
$row = array();
$types = array();
while( $res->fetchInto( $row ) ) {
    $types[$row[0]] = $row[1]; //substr( $row[1], 1 ); // trim leading underscore
}
$conn->disconnect();

echo "<tr><td><form method=\"get\" name=\"select_file\" action=\"filesel.php\">\n";
echo "<label for=\"types\">Data types: </label>\n";
echo "<select name=\"tagcat\" id=\"types\">\n";
foreach( array_keys( $types ) as $type )
    echo "<option value=\"$types[$type]\">$type</option>\n";
echo "</select>\n";
echo "<p><input type=\"submit\" value=\"Continue\"></td></tr>\n";
echo "</form></td></tr>\n";

?>
