/**
 * 
 */
package com.abp.pkr.pkrVista.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.abp.pkr.pkrVista.Pru.CapturadorNgcImplPru;
import com.abp.pkr.pkrVista.dto.AccionInfoDto;
import com.abp.pkr.pkrVista.dto.HandInfoDto;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

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
	 * Servicio par probar la extraccion de info, desde una imagen existente en la
	 * carpeta especificada en archivo de configuracion
	 * 
	 * @author Alesso
	 * @date May 27, 2017
	 * @return
	 * @throws Exception
	 */
	@GetMapping(value = "/infoDesdeImg")
	public HandInfoDto infoConImagen() throws Exception {
		long ini = System.currentTimeMillis();

		// obtener imagen desde carpeta
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

	@GetMapping(value = "/pruebaVista")
	public AccionInfoDto pruebaVista() throws Exception {
		AccionInfoDto acc = new AccionInfoDto();
		// Map<String, AccionVsPlayer> map = new HashMap<>();
		// AccionVsPlayer apDEF = new AccionVsPlayer();
		// AccionVsPlayer apREG = new AccionVsPlayer();
		//
		// acc.setHand("AA");
		// acc.setInfVsEffStack(13.0);
		// acc.setInfVsPlayer("SB");
		// acc.setNumJug("3max");
		// acc.setPosHero("BU");
		// acc.setStackHero(20.0);
		// acc.setSupVsEffStack(17.0);
		// acc.setSupVsPlayer("BB");
		//
		// apDEF.setInfQA1("defInfQA1");
		// apDEF.setInfQA2("defInfQA2");
		// apDEF.setInfQA3("defInfQA3");
		// apDEF.setSupQA1("defSupQA1");
		// apDEF.setSupQA2("defSupQA2");
		// apDEF.setSupQA3("defSupQA3");
		//
		// apREG.setInfQA1("regInfQA1");
		// apREG.setInfQA3("regInfQA3");
		// apREG.setSupQA2("defSupQA1");
		// apREG.setSupQA3("regSupQA2");
		//
		// map.put("DEF", apDEF);
		// map.put("REG", apREG);
		//
		// acc.setAccionVsPlayer(map);

		String home = System.getProperty("user.home");
		Gson gson = new Gson();

		try {

			FileReader fila = new FileReader(
					new File(home + "\\Documents\\03-Alessandro\\01-poker\\appSoft\\pruebaVista.json"));

			if (!fila.ready()) {
				return null;
			}

			acc = gson.fromJson(fila, AccionInfoDto.class);
			
			
		} catch (IOException e) {
			throw new IOException("error al leer json file");
		}

		return acc;
	}

}
