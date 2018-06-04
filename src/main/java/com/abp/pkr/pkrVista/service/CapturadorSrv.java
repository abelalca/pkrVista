/**
 * 
 */
package com.abp.pkr.pkrVista.service;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abp.pkr.pkrVista.dto.HandInfoDto;
import com.abp.pkr.pkrVista.ngc.CapturadorNgcImpl;
import com.abp.pkr.pkrVista.ngc.CapturadorOcrNgcImpl;

import ch.qos.logback.classic.Logger;

/**
 * @author abpubuntu
 *
 */
@RestController
@RequestMapping("/captura")
public class CapturadorSrv {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(CapturadorSrv.class);
	
	
	@Autowired
	private CapturadorOcrNgcImpl capturadorOcrNgcImpl;
	
	/**
	 * Servicio que es llamado para tomar un screenshot y extraer la informacion de la mesa
	 *@author abpubuntu
	 *@date May 14, 2017
	 * @return
	 * @throws Exception
	 */
	@GetMapping(value="/mesaInfo")
	public HandInfoDto extraerMesaInfo() throws Exception{
		long ini = System.currentTimeMillis();
		
		HandInfoDto handInfoDto= capturadorOcrNgcImpl.extraerMesaInfo();		
		
		long fin = System.currentTimeMillis();		
		log.debug("tiempo extraer Info mesa: " + (fin - ini));
		
		if (handInfoDto != null) {
			handInfoDto.setTiempoRest((fin - ini));			
		}
		
		return handInfoDto;
	}

}
