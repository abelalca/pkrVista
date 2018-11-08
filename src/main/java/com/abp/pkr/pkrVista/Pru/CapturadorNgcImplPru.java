/**
 * 
 */
package com.abp.pkr.pkrVista.Pru;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import com.abp.pkr.pkrVista.dto.AccionInfoDto;
import com.abp.pkr.pkrVista.dto.HandInfoDto;
import com.abp.pkr.pkrVista.dto.MesaConfig;
import com.abp.pkr.pkrVista.dto.MesaConfig.Zona;
import com.abp.pkr.pkrVista.ngc.CapturadorOcrNgcImpl;
import com.abp.pkr.pkrVista.utl.UtilView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import ch.qos.logback.classic.Logger;

/**
 * @author abpubuntu
 *
 */
@Controller
public class CapturadorNgcImplPru extends CapturadorOcrNgcImpl {

	private static final Logger log = (Logger) LoggerFactory.getLogger(CapturadorNgcImplPru.class);

	public HandInfoDto extraerInfoArchivoImg() throws Exception {
		HandInfoDto handInfoDto = new HandInfoDto();

		// leemos imagen desde directorio
		String ruta = home + mesaConfig.getRutacaptura() + "\\" + mesaConfig.getNombrearchivo()
				+ mesaConfig.getFormato();
		BufferedImage screenImg = ImageIO.read(new File(ruta));
		log.debug("leyendo imagen desde directorio: " + ruta);

		// procesamos zonas
		long ini = System.currentTimeMillis();
		
		// creamos una lista para obtener iterar la lectura de imagenes y obtener la mas
		// probable
		int numItera = Integer.valueOf(mesaConfig.getNumIteraCaptura().trim());

		List<BufferedImage> screenList = new ArrayList<>();
		for (int i = 0; i < numItera; i++) {
			screenList.add(screenImg);
		}
		
		List<HandInfoDto> hdto = new ArrayList<>();
		screenList.stream().parallel().forEach(screen -> {
			try {
				log.debug("Procesando imagen para obtener infor mesa...");
				
				// seleccionamos tipo de procesamiento configurado y procesamos la imagen
				String tipoOcr = mesaConfig.getTipoOCR();
				log.debug("tipo de procesamiento: " + tipoOcr);
				if (tipoOcr.trim().equals("histogram")) {					
					hdto.add(procesarZonasPorHistograma(screenImg));	
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}			
		});
		
		Map<String, List<HandInfoDto>> result = hdto.stream().collect(Collectors.groupingBy(HandInfoDto::concat));

		Map<String, Integer> resCount = new HashMap<>();
		result.entrySet().forEach(entry -> {
			String key = entry.getKey();
			Integer value = entry.getValue().size();
			resCount.put(key, value);
		});

		String maxKey = resCount.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();

		HandInfoDto resComp = null;
		for (HandInfoDto hd : hdto) {
			if (hd.concat().equals(maxKey)) {
				resComp = hd;
				break;
			}
		}	
		long fin = System.currentTimeMillis();
		log.debug("...tiempo procesado imagen: " + (fin - ini));
		resComp.setTiempoRest((fin-ini));
		
		return resComp;
	}

	public HandInfoDto extraerInfoArchivoRealTime() throws Exception {
		HandInfoDto handInfoDto = new HandInfoDto();

		// capturo screen
		Rectangle zona = new Rectangle(mesaConfig.getMesa().get(0).getX(), mesaConfig.getMesa().get(0).getY(),
				mesaConfig.getMesa().get(0).getAncho(), mesaConfig.getMesa().get(0).getAlto());
		BufferedImage screenImg = capturador.capturarScreenZona(zona);

		// Guardar Imagen y info
		String ruta = home + mesaConfig.getRutaBugs();
		UtilView.guardarImagen(screenImg, ruta + "\\tmpImg.png");

		// procesamos zonas
		long ini = System.currentTimeMillis();
		// handInfoDto = procesarZonasParalelo(screenImg);
		long fin = System.currentTimeMillis();
		handInfoDto.setTiempoRest(fin - ini);

		try (Writer writer = new FileWriter(ruta + "\\tmpJson")) {
			Gson gson = new GsonBuilder().create();
			gson.toJson(handInfoDto, writer);
		}

		return handInfoDto;
	}

	public String obtenerImg() throws IOException {
		String ruta = home + mesaConfig.getRutaBugs();
		File tmpImg = new File(ruta + "\\tmpImg.png");
		InputStream in = FileUtils.openInputStream(tmpImg);
		byte[] bArry = IOUtils.toByteArray(in);

		StringBuilder sb = new StringBuilder();
		sb.append("data:image/png;base64,");
		sb.append(StringUtils.newStringUtf8(Base64.encodeBase64(bArry, false)));
		String encoded = sb.toString();

		return encoded;
	}

	/**
	 * Metodo que llama a otro rest de accione preflop
	 * 
	 * @param handInfoDto
	 * @return
	 */
	public AccionInfoDto consumirLogicPre(HandInfoDto handInfoDto) {
		final String url = "http://localhost:8450/proceso/procesarHand";

		RestTemplate restTemplate = new RestTemplate();
		AccionInfoDto accion = restTemplate.postForObject(url, handInfoDto, AccionInfoDto.class);
		log.debug("Consumiendo servicio Rest de Logica Preflop: " + (accion != null ? "Exitoso" : "Fallo"));

		return accion;
	}

	public HandInfoDto extraerInfoArchivoJson() throws Exception {

		// leemos imagen desde directorio
		String ruta = home + mesaConfig.getRutaJson() + "\\" + mesaConfig.getNombreJson() + ".json";
		log.debug("leyendo Json prueba desde directorio: " + ruta);

		// convertimos json to HandDtoInfo
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new FileReader(ruta));
		HandInfoDto dataJson = gson.fromJson(reader, HandInfoDto.class);

		return dataJson;
	}

	public boolean capturarZona(String zonaNombre) throws Exception {
		boolean resp = false;
		
		Zona mesa = null;
		for (Zona zona : mesaConfig.getMesa()) {
			if (zona.getNombre().trim().equals(zonaNombre.trim())) {
				mesa = zona;
			}
		}
		if (mesa == null) {
			throw new Exception("no existe zona con ese nombre");
		}

		// capturo screen
		Rectangle zona = new Rectangle(mesa.getX(), mesa.getY(), mesa.getAncho(), mesa.getAlto());
		BufferedImage screenImg = capturador.capturarScreenZona(zona);

		Zona[] listaZonas = listarTodasZonas();

		Arrays.stream(listaZonas).forEach(z -> {
			BufferedImage subImg = extraerSubImagen(screenImg, z);
			UtilView.guardarImagen(subImg, home + mesaConfig.getRutacaptura() + "\\screen_" + zonaNombre + "-"
					+ z.getNombre() + "_" + System.currentTimeMillis() + ".png");
		});

		resp=true;
		return resp;
	}

}
