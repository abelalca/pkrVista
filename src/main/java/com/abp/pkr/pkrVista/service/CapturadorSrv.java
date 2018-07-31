/**
 * 
 */
package com.abp.pkr.pkrVista.service;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.abp.pkr.pkrVista.dto.AccionInfoDto;
import com.abp.pkr.pkrVista.dto.HandInfoDto;
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
	 * Servicio que es llamado para tomar un screenshot y extraer la informacion de
	 * la mesa
	 * 
	 * @author abpubuntu
	 * @date May 14, 2017
	 * @return
	 * @throws Exception
	 */
	@GetMapping(value = "/mesaInfo")
	public AccionInfoDto extraerMesaInfo() throws Exception {
		long ini = System.currentTimeMillis();
		long fin = System.currentTimeMillis();
		AccionInfoDto accion = new AccionInfoDto();

		HandInfoDto handInfoDto = capturadorOcrNgcImpl.extraerMesaInfo();

		if (handInfoDto != null) {
			try {
				RestTemplate restTemplate = new RestTemplate();
				accion = restTemplate.getForObject("http://localhost:8450/proceso/procesarHand", AccionInfoDto.class);				
			} catch (Exception e) {
				throw e;
			}

			fin = System.currentTimeMillis();
			handInfoDto.setTiempoRest((fin - ini));
			log.debug("tiempo extraer Info mesa: " + (fin - ini));
		}

		return accion;
	}

}
