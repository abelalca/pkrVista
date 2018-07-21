/**
 * 
 */
package com.abp.pkr.pkrVista.Pru;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Date;

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
import com.abp.pkr.pkrVista.ngc.CapturadorOcrNgcImpl;
import com.abp.pkr.pkrVista.utl.UtilView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
		BufferedImage img = ImageIO.read(new File(ruta));
		log.debug("leyendo imagen desde directorio: " + ruta);

		// procesamos zonas
		handInfoDto = procesarZonas(img);

		return handInfoDto;
	}

	public HandInfoDto extraerInfoArchivoRealTime() throws Exception {
		HandInfoDto handInfoDto = new HandInfoDto();

		// capturo screen
		Rectangle zona = new Rectangle(mesaConfig.getMesa().get(0).getX(), mesaConfig.getMesa().get(0).getY(),
				mesaConfig.getMesa().get(0).getAncho(), mesaConfig.getMesa().get(0).getAlto());
		BufferedImage screenImg = capturador.capturarScreenZona(zona);

		// Guardar Imagen y info
		String ruta = home + mesaConfig.getRutacaptura();
		UtilView.guardarImagen(screenImg, ruta + "\\tmpImg.png");

		// procesamos zonas
		handInfoDto = procesarZonas(screenImg);

		try (Writer writer = new FileWriter(ruta + "\\tmpJson")) {
			Gson gson = new GsonBuilder().create();
			gson.toJson(handInfoDto, writer);
		}

		return handInfoDto;
	}


	public String obtenerImg() throws IOException {
		String ruta = home + mesaConfig.getRutacaptura();
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
	 * @param handInfoDto
	 * @return
	 */
	public AccionInfoDto consumirLogicPre(HandInfoDto handInfoDto) {
		final String url = "http://localhost:8450/proceso/procesarHand";

		RestTemplate restTemplate = new RestTemplate();
		AccionInfoDto accion = restTemplate.postForObject(url, handInfoDto, AccionInfoDto.class);
		log.debug("Consumiendo servicio Rest de Logica Preflop: " + (accion != null? "Exitoso" : "Fallo"));

		return accion;
	}

}
