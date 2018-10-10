/**
 * 
 */
package com.abp.pkr.pkrVista.service;

import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.abp.pkr.pkrVista.dto.AccionInfoDto;
import com.abp.pkr.pkrVista.dto.HandInfoDto;
import com.abp.pkr.pkrVista.dto.MesaConfig.Zona;
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

	@Autowired
	protected CapturadorNgcImpl capturador;

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

		// mesa que estoy parado, debemos obtener las coord del mouse para
		// saber en que mesa estamos
		Zona mesaActual = capturador.mesaMouse();

		HandInfoDto handInfoDto = null;
		try {
			handInfoDto = capturadorOcrNgcImpl.extraerMesaInfo(mesaActual);
		} catch (Exception e) {
			throw e;
		}

		if (handInfoDto != null) {
			try {
				accion = consumirLogicPre(handInfoDto);
			} catch (Exception e) {
				throw e;
			}

			fin = System.currentTimeMillis();
			handInfoDto.setTiempoRest((fin - ini));
			log.debug("tiempo extraer Accion info de mesa: " + (fin - ini));
			accion.setTiempo(fin - ini);
		}

		return accion;
	}

	@GetMapping(value = "/almacenarImagenCuadrante")
	public void almacenarImagenCuadrante(String cuadrante) throws Exception {
		capturadorOcrNgcImpl.almacenarImagenCuadrante(cuadrante);
	}

	@GetMapping(value = "/obtenerNombresCuadrantes")
	public List<String> obtenerNombresCuadrantes() throws Exception {
		List<String> lista = capturadorOcrNgcImpl.obtenerNombresCuadrantes();
		return lista;
	}

	public AccionInfoDto consumirLogicPre(HandInfoDto handInfoDto) {
		final String url = "http://localhost:8450/proceso/procesarHand";

		RestTemplate restTemplate = new RestTemplate();
		AccionInfoDto accion = restTemplate.postForObject(url, handInfoDto, AccionInfoDto.class);
		log.debug("Consumiendo servicio Rest de Logica Preflop: " + (accion != null ? "Exitoso" : "Fallo"));

		return accion;
	}

}
