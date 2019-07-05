/**
 * 
 */
package com.abp.pkr.pkrVista.service;

import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.abp.pkr.pkrVista.dto.AccionInfoDto;
import com.abp.pkr.pkrVista.dto.HandInfoDto;
import com.abp.pkr.pkrVista.dto.MesaConfig;
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
	
	@Autowired
	protected MesaConfig mesaConfig;

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
		log.debug("LLAMANDO SERVICIO REST /captura/mesaInfo");
		
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
			accion.setTiempo(-1);
			log.error(e.getMessage());
			return null;
		}

		if (handInfoDto != null) {
			try {
				accion = consumirLogicPre(handInfoDto);
				fin = System.currentTimeMillis();
				handInfoDto.setTiempoRest((fin - ini));
				log.debug("Tiempo extraer Accion info TOTAL de mesa: " + (fin - ini));
				accion.setTiempo(fin - ini);
			} catch (Exception e) {
				accion.setTiempo(-2);
				log.error(e.getMessage());
				return null;
			}

			accion.setMesaNombre(mesaActual.getNombre());
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
	
	@GetMapping(value = "/borrarCache")
	public void borrarCache() throws Exception {
		final String url = "http://localhost:8450/proceso/borrarCache";
		
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getForEntity(url, String.class);
	}

	public AccionInfoDto consumirLogicPre(HandInfoDto handInfoDto) {
		final String url = "http://localhost:8450/proceso/procesarHand";
		log.debug("--------------------------------------");	
		log.debug("llamando servicio Logica preflop desde PkrVista: " + url);	
		log.debug("Mano a analizar: {}, Stacks: {}, posicion:{}, numJugadoresActivos: {}", handInfoDto.getHand(), handInfoDto.getStacksBb(), handInfoDto.getPosHero(), handInfoDto.getNumjug());
		
		RestTemplate restTemplate = new RestTemplate();
		AccionInfoDto accion = restTemplate.postForObject(url, handInfoDto, AccionInfoDto.class);
		log.debug("Consumiendo servicio Rest de Logica Preflop: " + (accion != null ? "Exitoso" : "Fallo"));
		log.debug("--------------------------------------");

		return accion;
	}

}
