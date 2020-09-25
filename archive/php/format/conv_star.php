<?php
include "globals.inc";
//
// convert NMR-STAR file.
//
function convert_star( $filename, &$outfile, $loopnum ) {
//echo "<BR>filename: $filename, loopnum = $loopnum<BR>";
    global $MAXSTRLEN;
    global $TMPDIR;

    $outfile = tempnam( $TMPDIR, "conv" );
    $in = @fopen( $filename, "r" );
    if( $in ) {
        $out = @fopen( $outfile, "w" );
	if( $out ) {
	    $rc = array();
	    $row = 0;
            $col = 0;
	    $numcols = 0;
	    $loopcount = 0;
	    if( $loopnum < 1 ) $loopnum = 1;
            while( !feof( $in ) ) {
	        $buffer = fgets( $in, $MAXSTRLEN );
		$buffer = trim( $buffer );
		if( strlen( $buffer ) < 1 ) continue; // skip blank lines
//echo "<BR><B>$buffer</B><BR>";
		if( preg_match( "/loop_/", $buffer ) == 1 ) {
		    $loopcount++;
		    if( $loopcount == $loopnum ) {
//echo "<BR><B>loop # $loopcount, looking for $loopnum, parsing</B><BR>";
			$buffer = substr( $buffer, 5 );
			do {
//echo "<BR><B>do{}: $buffer</B><BR>";
			    if( strlen( $buffer ) < 1 ) {
				$buffer = fgets( $in, $MAXSTRLEN );
				if( feof( $in ) ) break 2;
			    }
			    $buffer = trim( $buffer );
			    if( strlen( $buffer ) < 1 ) continue; // skip blank lines
			    if( preg_match( "/^#.*/", $buffer ) == 1 ) { // line comments
				$buffer = "";
				continue;
			    }
			    
			    $vals = preg_split( "/\s+/", $buffer );
			    foreach( $vals as $val ) {
				$buffer = substr( $buffer, strlen( $val ) );
				$buffer = trim( $buffer);
//echo "<BR><B>$val : $buffer</B><BR>";
                		$val = trim( $val );
				if( preg_match( "/^stop_$/", $val ) ) {
				    break 3;
				}
// tags
				if( preg_match( "/^_[_[:alnum:].%-]+$/", $val ) == 1 ) {
				    $newtag = get_new_tag( $val );
				    if( $newtag != NULL ) $rc[$newtag] = $numcols;
				    $numcols++;
				}
				else { 
// values
				    if( preg_match( "/^#/", $val ) ) {
					$buffer = "";
					continue 2;
				    }
				
		    		    if( $col >= $numcols ) {
					$col = 1;
				        $row++;
					fwrite( $out, "\n" );
					fwrite( $out, $val );
					fwrite( $out, "\t" );
				    }
				    else {
					$col++;
		        		fwrite( $out, $val );
					fwrite( $out, "\t" );
				    }
				}
			    } // end foreach $val
			} while( true );
		    } // endif $loopcount == $loopnum
		} // endif "loop_"
	    } // endwhile ! feof
	    fclose( $out );
	} // endif $out
	else $outfile = NULL;
        fclose( $in );
	unlink( $filename );
    } // endif $in
    else $outfile = NULL;
    chmod( $outfile, 0644 );
    return $rc;
}
//
// tag map: returns 3.1 tag if it's mapped, $oldtag otherwise
//
function get_new_tag( $oldtag ) {
// CS
// 2.1
    if( $oldtag == "_Atom_shift_assign_ID" )      return "_Atom_chem_shift.ID";
    if( $oldtag == "_Residue_seq_code" )          return "_Atom_chem_shift.Comp_index_ID";
    if( $oldtag == "_Residue_label" )             return "_Atom_chem_shift.Comp_ID";
    if( $oldtag == "_Atom_name" )                 return "_Atom_chem_shift.Atom_ID";
#    if( $oldtag == "_Residue_seq_code" )          return "_Atom_chem_shift.Auth_seq_ID";
#    if( $oldtag == "_Residue_label" )             return "_Atom_chem_shift.Auth_comp_ID";
#    if( $oldtag == "_Atom_name" )                 return "_Atom_chem_shift.Auth_atom_ID";
    if( $oldtag == "_Atom_type" )                 return "_Atom_chem_shift.Atom_type";
    if( $oldtag == "_Chem_shift_value" )          return "_Atom_chem_shift.Val";
    if( $oldtag == "_Chem_shift_value_error" )    return "_Atom_chem_shift.Val_err";
    if( $oldtag == "_Chem_shift_ambiguity_code" ) return "_Atom_chem_shift.Ambiguity_code";
// 3.0
    if( $oldtag == "_Atom_chem_shift.Atom_isotope" )                   return "_Atom_chem_shift.Atom_isotope_number";
    if( $oldtag == "_Atom_chem_shift.Author_seq_code" )                return "_Atom_chem_shift.Auth_seq_ID";
    if( $oldtag == "_Atom_chem_shift.Author_comp_code" )               return "_Atom_chem_shift.Auth_comp_ID";
    if( $oldtag == "_Atom_chem_shift.Author_atom_code" )               return "_Atom_chem_shift.Auth_atom_ID";
    if( $oldtag == "_Atom_chem_shift.Chem_shift_val" )                 return "_Atom_chem_shift.Val";
    if( $oldtag == "_Atom_chem_shift.Chem_shift_val_err" )             return "_Atom_chem_shift.Val_err";
    if( $oldtag == "_Atom_chem_shift.Chem_shift_assign_fig_of_merit" ) return "_Atom_chem_shift.Assign_fig_of_merit";
    if( $oldtag == "_Atom_chem_shift.Chem_shift_ambiguity_code" )      return "_Atom_chem_shift.Ambiguity_code";
    if( $oldtag == "_Atom_chem_shift.Chem_shift_occupancy_ID" )        return "_Atom_chem_shift.Occupancy";
    if( $oldtag == "_Atom_chem_shift.Chem_shift_occupancy" )           return "_Atom_chem_shift.Occupancy";
    if( $oldtag == "_Atom_chem_shift.Entry_atom_ID" )                  return "IGNORE COLUMN";
    if( $oldtag == "_Atom_chem_shift.Derivation_ID" )                  return "IGNORE COLUMN";
// CC
// 2.1
    if( $oldtag == "_Coupling_constant_ID" )          return "_Coupling_constant.ID";
    if( $oldtag == "_Coupling_constant_code" )        return "_Coupling_constant.Code";
    if( $oldtag == "_Atom_one_residue_seq_code" )     return "_Coupling_constant.Comp_index_ID_1";
    if( $oldtag == "_Atom_one_residue_label" )        return "_Coupling_constant.Comp_ID_1";
    if( $oldtag == "_Atom_one_name" )                 return "_Coupling_constant.Atom_ID_1";
    if( $oldtag == "_Atom_two_residue_seq_code" )     return "_Coupling_constant.Comp_index_ID_2";
    if( $oldtag == "_Atom_two_residue_label" )        return "_Coupling_constant.Comp_ID_2";
    if( $oldtag == "_Atom_two_name" )                 return "_Coupling_constant.Atom_ID_2";
#    if( $oldtag == "_Atom_one_residue_seq_code" )     return "_Coupling_constant.Auth_seq_ID_1";
#    if( $oldtag == "_Atom_one_residue_label" )        return "_Coupling_constant.Auth_comp_ID_1";
#    if( $oldtag == "_Atom_one_name" )                 return "_Coupling_constant.Auth_atom_ID_1";
#    if( $oldtag == "_Atom_two_residue_seq_code" )     return "_Coupling_constant.Auth_seq_ID_2";
#    if( $oldtag == "_Atom_two_residue_label" )        return "_Coupling_constant.Auth_comp_ID_2";
#    if( $oldtag == "_Atom_two_name" )                 return "_Coupling_constant.Auth_atom_ID_2";
    if( $oldtag == "_Coupling_constant_value" )       return "_Coupling_constant.Val";
    if( $oldtag == "_Coupling_constant_min_value" )   return "_Coupling_constant.Val_min";
    if( $oldtag == "_Coupling_constant_max_value" )   return "_Coupling_constant.Val_max";
    if( $oldtag == "_Coupling_constant_value_error" ) return "_Coupling_constant.Val_err";
// 3.0
    if( $oldtag == "_Coupling_constant.Min_val" ) return "_Coupling_constant.Val_min";
    if( $oldtag == "_Coupling_constant.Max_val" ) return "_Coupling_constant.Val_max";
// T1
// 2.1
    if( $oldtag == "_Residue_seq_code" ) return "_T1.Auth_seq_ID";
    if( $oldtag == "_Residue_label" )    return "_T1.Auth_comp_ID";
    if( $oldtag == "_Atom_name" )        return "_T1.Auth_atom_ID";
    if( $oldtag == "_T1_value" )         return "_T1.Val";
    if( $oldtag == "_T1_value_error" )   return "_T1.Val_err";
// 3.0
    if( $oldtag == "_T1.Author_seq_ID" ) return "_T1.Auth_seq_ID";
    if( $oldtag == "_T1.T1_val" )        return "_T1.Val";
    if( $oldtag == "_T1.T1_val_err" )    return "_T1.Val_err";
// T1rho
// 2.1
    if( $oldtag == "_Residue_seq_code" )  return "_T1rho.Comp_index_ID";
    if( $oldtag == "_Residue_label" )     return "_T1rho.Comp_ID";
    if( $oldtag == "_Atom_name" )         return "_T1rho.Atom_ID";
#    if( $oldtag == "_Residue_seq_code" )  return "_T1rho.Auth_seq_ID";
#    if( $oldtag == "_Residue_label" )     return "_T1rho.Auth_comp_ID";
#    if( $oldtag == "_Atom_name" )         return "_T1rho.Auth_atom_ID";
    if( $oldtag == "_T1rho_value" )       return "_T1rho.T1rho_val";
    if( $oldtag == "_T1rho_value_error" ) return "_T1rho.T1rho_val_err";
// 3.0
    if( $oldtag == "_T1rho.T1rho_Rex_val" )     return "_T1rho.Rex_val";
    if( $oldtag == "_T1rho.T1rho_Rex_val_err" ) return "_T1rho.Rex_val_err";
// T2
// 2.1
    if( $oldtag == "_Residue_seq_code" ) return "_T2.Comp_index_ID";
    if( $oldtag == "_Residue_label" )    return "_T2.Comp_ID";
    if( $oldtag == "_Atom_name" )        return "_T2.Atom_ID";
#    if( $oldtag == "_Residue_seq_code" ) return "_T2.Auth_seq_ID";
#    if( $oldtag == "_Residue_label" )    return "_T2.Auth_comp_ID";
#    if( $oldtag == "_Atom_name" )        return "_T2.Auth_atom_ID";
    if( $oldtag == "_T2_value" )         return "_T2.T2_val";
    if( $oldtag == "_T2_value_error" )   return "_T2.T2_val_err";
    if( $oldtag == "_Rex_value" )        return "_T2.Rex_val";
    if( $oldtag == "_Rex_error" )        return "_T2.Rex_err";
// Het. NOE
// 2.1
    if( $oldtag == "_Residue_seq_code" )              return "_Heteronucl_NOE.Comp_index_ID_1";
    if( $oldtag == "_Residue_label" )                 return "_Heteronucl_NOE.Comp_ID_1";
#    if( $oldtag == "_Residue_seq_code" )              return "_Heteronucl_NOE.Auth_seq_ID_1";
#    if( $oldtag == "_Residue_label" )                 return "_Heteronucl_NOE.Auth_comp_ID_1";
    if( $oldtag == "_Heteronuclear_NOE_value" )       return "_Heteronucl_NOE.Val";
    if( $oldtag == "_Heteronuclear_NOE_value_error" ) return "_Heteronucl_NOE.Val_err";
// these are broken in table generator: they're free tags in the schema but tablegen puts them in the loop
    if( $oldtag == "_Atom_one_name" )              return "_Heteronucl_NOE.Atom_ID_1";
    if( $oldtag == "_Atom_two_name" )              return "_Heteronucl_NOE.Atom_ID_2";
#    if( $oldtag == "_Atom_one_name" )              return "_Heteronucl_NOE.Auth_atom_ID_1";
#    if( $oldtag == "_Atom_two_name" )              return "_Heteronucl_NOE.Auth_atom_ID_2";
// 3.0
    if( $oldtag == "_Heteronucl_NOE.Heteronucl_NOE_val" )     return "_Heteronucl_NOE.Val";
    if( $oldtag == "_Heteronucl_NOE.Heteronucl_NOE_val_err" ) return "_Heteronucl_NOE.Val_err";
// Hom. NOEs
// 2.1
    if( $oldtag == "_Atom_one_mol_system_component_name" ) return  "_Homonucl_NOE.Entity_assembly_ID_1";
    if( $oldtag == "_Atom_one_residue_seq_code" )          return  "_Homonucl_NOE.Comp_index_ID_1";
    if( $oldtag == "_Atom_one_residue_label" )             return  "_Homonucl_NOE.Comp_ID_1";
    if( $oldtag == "_Atom_one_atom_name" )                 return  "_Homonucl_NOE.Atom_ID_1";
#    if( $oldtag == "_Atom_one_residue_seq_code" )          return  "_Homonucl_NOE.Auth_seq_ID_1";
#    if( $oldtag == "_Atom_one_residue_label" )             return  "_Homonucl_NOE.Auth_comp_ID_1";
#    if( $oldtag == "_Atom_one_atom_name" )                 return  "_Homonucl_NOE.Auth_atom_ID_1";
    if( $oldtag == "_Atom_two_mol_system_component_name" ) return  "_Homonucl_NOE.Entity_assembly_ID_2";
    if( $oldtag == "_Atom_two_residue_seq_code" )          return  "_Homonucl_NOE.Comp_index_ID_2";
    if( $oldtag == "_Atom_two_residue_label" )             return  "_Homonucl_NOE.Comp_ID_2";
    if( $oldtag == "_Atom_two_atom_name" )                 return  "_Homonucl_NOE.Atom_ID_2";
#    if( $oldtag == "_Atom_two_residue_seq_code" )          return  "_Homonucl_NOE.Auth_seq_ID_2";
#    if( $oldtag == "_Atom_two_residue_label" )             return  "_Homonucl_NOE.Auth_comp_ID_2";
#    if( $oldtag == "_Atom_two_atom_name" )                 return  "_Homonucl_NOE.Auth_atom_ID_2";
    if( $oldtag == "_Homonuclear_NOE_value" )              return  "_Homonucl_NOE.Val";
    if( $oldtag == "_Homonuclear_NOE_value_error" )        return  "_Homonucl_NOE.Val_err";
// order parameters
// 2.1
    if( $oldtag == "_Residue_seq_code" )      return "_Order_param.Comp_index_ID";
    if( $oldtag == "_Residue_label" )         return "_Order_param.Comp_ID";
    if( $oldtag == "_Atom_name" )             return "_Order_param.Atom_ID";
#    if( $oldtag == "_Residue_seq_code" )      return "_Order_param.Auth_seq_ID";
#    if( $oldtag == "_Residue_label" )         return "_Order_param.Auth_comp_ID";
#    if( $oldtag == "_Atom_name" )             return "_Order_param.Auth_atom_ID";
    if( $oldtag == "_Model_fit" )             return "_Order_param.Model_fit";
    if( $oldtag == "_S2_value" )              return "_Order_param.Order_param_val";
    if( $oldtag == "_S2_value_fit_error" )    return "_Order_param.Order_param_val_fit_err";
    if( $oldtag == "_Tau_e_value" )           return "_Order_param.Tau_e_val";
    if( $oldtag == "_Tau_e_value_fit_error" ) return "_Order_param.Tau_e_val_fit_err";
    if( $oldtag == "_S2f_value" )             return "_Order_param.Sf2_val";
    if( $oldtag == "_S2f_value_fit_error" )   return "_Order_param.Sf2_val_fit_err";
    if( $oldtag == "_S2s_value" )             return "_Order_param.Ss2_val";
    if( $oldtag == "_S2s_value_fit_error" )   return "_Order_param.Ss2_val_fit_err";
    if( $oldtag == "_Tau_s_value" )           return "_Order_param.Tau_s_val";
    if( $oldtag == "_Tau_s_value_fit_error" ) return "_Order_param.Tau_s_val_fit_err";
    if( $oldtag == "_S2H_value" )             return "_Order_param.SH2_val";
    if( $oldtag == "_S2H_value_fit_error" )   return "_Order_param.SH2_val_fit_err";
    if( $oldtag == "_S2N_value" )             return "_Order_param.SN2_val";
    if( $oldtag == "_S2N_value_fit_error" )   return "_Order_param.SN2_val_fit_err";
// H exch.
// 2.1
    if( $oldtag == "_Residue_seq_code" )            return "_H_exch_rate.Comp_index_ID";
    if( $oldtag == "_Residue_label" )               return "_H_exch_rate.Comp_ID";
    if( $oldtag == "_Atom_name" )                   return "_H_exch_rate.Atom_ID";
#    if( $oldtag == "_Residue_seq_code" )            return "_H_exch_rate.Auth_seq_ID";
#    if( $oldtag == "_Residue_label" )               return "_H_exch_rate.Auth_comp_ID";
#    if( $oldtag == "_Atom_name" )                   return "_H_exch_rate.Auth_atom_ID";
    if( $oldtag == "_H_exchange_rate_value" )       return "_H_exch_rate.Val";
    if( $oldtag == "_H_exchange_rate_min_value" )   return "_H_exch_rate.Val_min";
    if( $oldtag == "_H_exchange_rate_max_value" )   return "_H_exch_rate.Val_max";
    if( $oldtag == "_H_exchange_rate_value_error" ) return "_H_exch_rate.Val_err";
// 3.0
    if( $oldtag == "_H_exch_rate.H_exchange_rate_val" )     return "_H_exch_rate.Val";
    if( $oldtag == "_H_exch_rate.H_exchange_rate_min_val" ) return "_H_exch_rate.Val_min";
    if( $oldtag == "_H_exch_rate.H_exchange_rate_max_val" ) return "_H_exch_rate.Val_max";
    if( $oldtag == "_H_exch_rate.H_exchange_rate_val_err" ) return "_H_exch_rate.Val_err";
// H exch. protection factors
// 2.1
    if( $oldtag == "_Residue_seq_code" )                         return "_H_exch_protection_factor.Comp_index_ID";
    if( $oldtag == "_Residue_label" )                            return "_H_exch_protection_factor.Comp_ID";
    if( $oldtag == "_Atom_name" )                                return "_H_exch_protection_factor.Atom_ID";
#    if( $oldtag == "_Residue_seq_code" )                         return "_H_exch_protection_factor.Auth_seq_ID";
#    if( $oldtag == "_Residue_label" )                            return "_H_exch_protection_factor.Auth_comp_ID";
#    if( $oldtag == "_Atom_name" )                                return "_H_exch_protection_factor.Auth_atom_ID";
    if( $oldtag == "_H_exchange_protection_factor_value" )       return "_H_exch_protection_factor.Val";
    if( $oldtag == "_H_exchange_protection_factor_value_error" ) return "_H_exch_protection_factor.Val_err";
// 3.0
    if( $oldtag == "_H_exch_protection_factor.H_exch_protection_factor_val" )     return "_H_exch_protection_factor.Val";
    if( $oldtag == "_H_exch_protection_factor.H_exch_protection_factor_val_err" ) return "_H_exch_protection_factor.Val_err";
// H exchange rate
    if( $oldtag == "_Residue_seq_code" )            return "_H_exch_rate.Comp_index_ID";
    if( $oldtag == "_Residue_label" )               return "_H_exch_rate.Comp_ID";
    if( $oldtag == "_Atom_name" )                   return "_H_exch_rate.Atom_ID";
#    if( $oldtag == "_Residue_seq_code" )            return "_H_exch_rate.Auth_seq_ID";
#    if( $oldtag == "_Residue_label" )               return "_H_exch_rate.Auth_comp_ID";
#    if( $oldtag == "_Atom_name" )                   return "_H_exch_rate.Auth_atom_ID";
    if( $oldtag == "_H_exchange_rate_value" )       return "_H_exch_rate.Val";
    if( $oldtag == "_H_exchange_rate_min_value" )   return "_H_exch_rate.Val_min";
    if( $oldtag == "_H_exchange_rate_max_value" )   return "_H_exch_rate.Val_max";
    if( $oldtag == "_H_exchange_rate_value_error" ) return "_H_exch_rate.Val_err";
// pKa
// 2.1
    if( $oldtag == "_pKa_list_number" )                   return "_PH_titr_result.ID";
    if( $oldtag == "_Atom_observed_residue_seq_code" )    return "_PH_titr_result.Atom_observed_comp_index_ID";
    if( $oldtag == "_Atom_observed_residue_label" )       return "_PH_titr_result.Atom_observed_comp_ID";
    if( $oldtag == "_Atom_observed_atom_name" )           return "_PH_titr_result.Atom_observed_atom_ID";
#    if( $oldtag == "_Atom_observed_residue_seq_code" )    return "_PH_titr_result.Atom_observed_auth_seq_ID";
#    if( $oldtag == "_Atom_observed_residue_label" )       return "_PH_titr_result.Atom_observed_auth_comp_ID";
#    if( $oldtag == "_Atom_observed_atom_name" )           return "_PH_titr_result.Atom_observed_auth_atom_ID";
    if( $oldtag == "_pKa_hill_coeff_value" )              return "_PH_titr_result.Hill_coeff_val";
    if( $oldtag == "_pKa_hill_coeff_value_fit_error" )    return "_PH_titr_result.Hill_coeff_val_fit_err";
    if( $oldtag == "_High_pH_parameter_fit_value" )       return "_PH_titr_result.High_PH_param_fit_val";
    if( $oldtag == "_High_pH_parameter_fit_value_error" ) return "_PH_titr_result.High_PH_param_fit_val_err";
    if( $oldtag == "_Low_pH_parameter_fit_value" )        return "_PH_titr_result.Low_PH_param_fit_val";
    if( $oldtag == "_Low_pH_parameter_fit_value_error" )  return "_PH_titr_result.Low_PH_param_fit_val_err";
    if( $oldtag == "_pKa_value" )                         return "_PH_titr_result.PKa_val";
    if( $oldtag == "_pKa_value_fit_error" )               return "_PH_titr_result.PKa_val_fit_err";
    if( $oldtag == "_Atom_titr_residue_seq_code" )        return "_PH_titr_result.Atom_titrated_comp_index_ID";
    if( $oldtag == "_Atom_titr_residue_label" )           return "_PH_titr_result.Atom_titrated_comp_ID";
    if( $oldtag == "_Atom_titr_atom_name" )               return "_PH_titr_result.Atom_titrated_atom_ID";
#    if( $oldtag == "_Atom_titr_residue_seq_code" )        return "_PH_titr_result.Atom_titrated_auth_seq_ID";
#    if( $oldtag == "_Atom_titr_residue_label" )           return "_PH_titr_result.Atom_titrated_auth_comp_ID";
#    if( $oldtag == "_Atom_titr_atom_name" )               return "_PH_titr_result.Atom_titrated_auth_atom_ID";
// pH parameters
// 2.1
    if( $oldtag == "_pKa_list_number" )                    return "_PH_param.PH_titr_result_ID";
    if( $oldtag == "_pH_value" )                           return "_PH_param.PH_val";
    if( $oldtag == "_pH_value_error" )                     return "_PH_param.PH_val_err";
    if( $oldtag == "_Observed_NMR_parameter_value" )       return "_PH_param.Observed_NMR_param_val";
    if( $oldtag == "_Observed_NMR_parameter_value_error" ) return "_PH_param.Observed_NMR_param_val_err";
// D/H frac.
// 2.1
    if( $oldtag == "_Residue_seq_code" )            return "_D_H_fractionation_factor.Comp_index_ID";
    if( $oldtag == "_Residue_label" )               return "_D_H_fractionation_factor.Comp_ID";
    if( $oldtag == "_Atom_name" )                   return "_D_H_fractionation_factor.Atom_ID";
#    if( $oldtag == "_Residue_seq_code" )            return "_D_H_fractionation_factor.Auth_seq_ID";
#    if( $oldtag == "_Residue_label" )               return "_D_H_fractionation_factor.Auth_comp_ID";
#    if( $oldtag == "_Atom_name" )                   return "_D_H_fractionation_factor.Auth_atom_ID";
    if( $oldtag == "_H_fractionation_value" )       return "_D_H_fractionation_factor.Val";
    if( $oldtag == "_H_fractionation_value_error" ) return "_D_H_fractionation_factor.Val_err";
// Deduced H bonds
// 2.1
    if( $oldtag == "_Atom_one_mol_system_component_name" ) return "_Deduced_H_bond.Heavy_atom_entity_assembly_ID_1";
    if( $oldtag == "_Atom_one_residue_seq_code" )          return "_Deduced_H_bond.Heavy_atom_comp_index_ID_1";
    if( $oldtag == "_Atom_one_residue_label" )             return "_Deduced_H_bond.Heavy_atom_comp_ID_1";
    if( $oldtag == "_Atom_one_atom_name" )                 return "_Deduced_H_bond.Heavy_atom_atom_ID_1";
#    if( $oldtag == "_Atom_one_residue_seq_code" )          return "_Deduced_H_bond.Heavy_atom_auth_seq_ID_1";
#    if( $oldtag == "_Atom_one_residue_label" )             return "_Deduced_H_bond.Heavy_atom_auth_comp_ID_1";
#    if( $oldtag == "_Atom_one_atom_name" )                 return "_Deduced_H_bond.Heavy_atom_auth_atom_ID_1";
    if( $oldtag == "_Atom_two_mol_system_component_name" ) return "_Deduced_H_bond.Heavy_atom_entity_assembly_ID_2";
    if( $oldtag == "_Atom_two_residue_seq_code" )          return "_Deduced_H_bond.Heavy_atom_comp_index_ID_2";
    if( $oldtag == "_Atom_two_residue_label" )             return "_Deduced_H_bond.Heavy_atom_comp_ID_2";
    if( $oldtag == "_Atom_two_atom_name" )                 return "_Deduced_H_bond.Heavy_atom_atom_ID_2";
#    if( $oldtag == "_Atom_two_residue_seq_code" )          return "_Deduced_H_bond.Heavy_atom_auth_seq_ID_2";
#    if( $oldtag == "_Atom_two_residue_label" )             return "_Deduced_H_bond.Heavy_atom_auth_comp_ID_2";
#    if( $oldtag == "_Atom_two_atom_name" )                 return "_Deduced_H_bond.Heavy_atom_auth_atom_ID_2";
// Torsion angle
// 2.1
    if( $oldtag == "_Atom_one_residue_seq_code" )   return "_Torsion_angle_constraint.Comp_index_ID_1";
    if( $oldtag == "_Atom_one_atom_name" )          return "_Torsion_angle_constraint.Atom_ID_1";
    if( $oldtag == "_Atom_two_residue_seq_code" )   return "_Torsion_angle_constraint.Comp_index_ID_2";
    if( $oldtag == "_Atom_two_atom_name" )          return "_Torsion_angle_constraint.Atom_ID_2";
    if( $oldtag == "_Atom_three_residue_seq_code" ) return "_Torsion_angle_constraint.Comp_index_ID_3";
    if( $oldtag == "_Atom_three_atom_name" )        return "_Torsion_angle_constraint.Atom_ID_3";
    if( $oldtag == "_Atom_four_residue_seq_code" )  return "_Torsion_angle_constraint.Comp_index_ID_4";
    if( $oldtag == "_Atom_four_atom_name" )         return "_Torsion_angle_constraint.Atom_ID_4";
#    if( $oldtag == "_Atom_one_residue_seq_code" )   return "_Torsion_angle_constraint.Auth_seq_ID_1";
#    if( $oldtag == "_Atom_one_atom_name" )          return "_Torsion_angle_constraint.Auth_atom_ID_1";
#    if( $oldtag == "_Atom_two_residue_seq_code" )   return "_Torsion_angle_constraint.Auth_seq_ID_2";
#    if( $oldtag == "_Atom_two_atom_name" )          return "_Torsion_angle_constraint.Auth_atom_ID_2";
#    if( $oldtag == "_Atom_three_residue_seq_code" ) return "_Torsion_angle_constraint.Auth_seq_ID_3";
#    if( $oldtag == "_Atom_three_atom_name" )        return "_Torsion_angle_constraint.Auth_atom_ID_3";
#    if( $oldtag == "_Atom_four_residue_seq_code" )  return "_Torsion_angle_constraint.Auth_seq_ID_4";
#    if( $oldtag == "_Atom_four_atom_name" )         return "_Torsion_angle_constraint.Auth_atom_ID_4";
    if( $oldtag == "_Angle_upper_bound_value" )     return "_Torsion_angle_constraint.Angle_upper_bound_val";
    if( $oldtag == "_Angle_lower_bound_value" )     return "_Torsion_angle_constraint.Angle_lower_bound_val";
    if( $oldtag == "_Source_experiment_label" )     return "_Torsion_angle_constraint.Source_experiment_ID";

    return $oldtag;

}
?>
