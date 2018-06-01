/**
 * 
 */
package com.abp.pkr.pkrVista.service;


import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.abp.pkr.pkrVista.Pru.CapturadorNgcImplPru;
import com.abp.pkr.pkrVista.dto.HandInfoDto;

import ch.qos.logback.classic.Logger;

/**
 * @author abpubuntu
 *
 */
@RestController
@RequestMapping("/pruebas")
public class CapturadorPruebasSrv {

	private static final Logger log = (Logger) LoggerFactory.getLogger(CapturadorPruebasSrv.class);

	@Autowired
	CapturadorNgcImplPru capturadorNgcImplPru;

	/**
	 * Servicio par probar la extraccion de info, desde una imagen existente en
	 * la carpeta especificada en archivo de configuracion
	 * 
	 * @author Alesso
	 * @date May 27, 2017
	 * @return
	 * @throws Exception
	 */
	@GetMapping(value = "/infoDesdeImg")
	public HandInfoDto infoConImagen() throws Exception {
		long ini = System.currentTimeMillis();
		
		//obtener imagen desde carpeta
		HandInfoDto handInfo = capturadorNgcImplPru.extraerInfoArchivoImg();
		
		long fin = System.currentTimeMillis();
		handInfo.setTiempoRest((fin - ini));
		log.debug("tiempo extraer Info mesa: " + (fin - ini));
		
		return handInfo;
	}

	/**
	 * 
	 * @author Alesso
	 * @date May 31, 2018
	 * @return
	 * @throws Exception
	 */
	@GetMapping(value = "/infoRealTime")
	public HandInfoDto infoRealTime() throws Exception {
		long ini = System.currentTimeMillis();
		
		HandInfoDto handInfo = capturadorNgcImplPru.extraerInfoArchivoRealTime();
		
		long fin = System.currentTimeMillis();
		handInfo.setTiempoRest((fin - ini));
		log.debug("tiempo extraer Info mesa: " + (fin - ini));
		
		return handInfo;
	}
	
	@GetMapping(value = "/guardarInfo")
	public String guardarInfo() throws Exception {
		return capturadorNgcImplPru.guardarInfo();
	}

	@ResponseBody
	@RequestMapping(value = "/obtenerImg", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
	public String obtenerImg() throws Exception {		
		return capturadorNgcImplPru.obtenerImg();
	}
	
//	@GetMapping(value = "/pruebaJson")
//	public HandInfoDto pruebaJson() throws Exception {
//		HandInfoDto handInfoDto = new HandInfoDto();
//		handInfoDto.setHand("Kh9s");
//		Double[] stacks = {(double) 10, 23.4, 1.7};
//		handInfoDto.setStacksBb(stacks);
//		handInfoDto.setBtnPos(1);
//		handInfoDto.setNumHand("12345678");
//		handInfoDto.setNumHand("3");
//		return handInfoDto;
//	}

}
