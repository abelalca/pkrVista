/**
 * 
 */
package com.abp.pkr.pkrVista.ngc;

import com.abp.pkr.pkrVista.dto.HandInfoDto;

/**
 * @author abpubuntu
 *
 */
public interface CapturadorNgc {
	
	
	// Esto despues se puede tomar de archivos de configuracion
	static final String home = System.getProperty("user.home");
	
	// tipo de dato para metodo leer info
	enum TIPO_DATO {
		INT, // entero
		DEC, // decimal
		STR // string
	}
	
	// tipo de dato para metodo leer info
	enum TIPO_LANG_TESSEC {
		PKR, // entero
		NUM, // decimal
		STR, // string
		MIX // num and string
	}	

	public HandInfoDto extraerMesaInfo() throws Exception;

}
