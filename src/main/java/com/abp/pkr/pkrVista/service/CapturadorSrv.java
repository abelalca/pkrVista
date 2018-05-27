/**
 * 
 */
package com.abp.pkr.pkrVista.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abp.pkr.pkrVista.dto.HandInfoDto;
import com.abp.pkr.pkrVista.ngc.CapturadorNgcImpl;

/**
 * @author abpubuntu
 *
 */
@RestController
@RequestMapping("/captura")
public class CapturadorSrv {
	
	@Autowired
	@Qualifier("capturadorNgcImpl")
	private CapturadorNgcImpl capturadorNgc;
	
	/**
	 * Servicio que es llamado para tomar un screenshot y extraer la informacion de la mesa
	 *@author abpubuntu
	 *@date May 14, 2017
	 * @return
	 * @throws Exception
	 */
	@GetMapping(value="/mesaInfo")
	public HandInfoDto extraerMesaInfo() throws Exception{
		return capturadorNgc.extraerMesaInfo();
	}

}
