/**
 * 
 */
package com.abp.pkr.pkrVista.Pru;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.springframework.stereotype.Controller;

import com.abp.pkr.pkrVista.dto.HandInfoDto;
import com.abp.pkr.pkrVista.ngc.CapturadorOcrNgcImpl;
import com.abp.pkr.pkrVista.utl.UtilView;
import com.google.gson.Gson;

/**
 * @author abpubuntu
 *
 */
@Controller
public class CapturadorNgcImplPru extends CapturadorOcrNgcImpl {

	public HandInfoDto extraerInfoArchivoImg() throws Exception {
		HandInfoDto handInfoDto = new HandInfoDto();
		
		// leemos imagen desde directorio
		String ruta = home+ mesaConfig.getRutacaptura() + "\\" + mesaConfig.getNombrearchivo() + mesaConfig.getFormato();
		BufferedImage img = ImageIO.read(new File(ruta));

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

		//Guardar Imagen y info
		String ruta = home + mesaConfig.getRutacaptura();
		UtilView.guardarImagen(screenImg, ruta+"tmpImg.png");
		
		// procesamos zonas
		handInfoDto = procesarZonas(screenImg);
		
		Gson gson = new Gson();		
		gson.toJson(handInfoDto, new FileWriter(ruta+"tmpJson.json"));	

		return handInfoDto;
	}

	/**
	 *@author abpubuntu
	 *@date Jun 5, 2017
	 * @return
	 * @throws Exception 
	 */
	public String guardarInfo() throws Exception {
		String ruta = home + mesaConfig.getRutacaptura();
		Long date= new Date().getTime();

		File tmpImg = new File(ruta+"tmpImg.png");
		File tmpJson = new File(ruta+"tmpJson.json");

		File nuevaImg = new File(ruta+date.toString()+".png");
		File nuevaJson = new File(ruta+date.toString()+".json");

		if (nuevaImg.exists() && nuevaJson.exists())
		   throw new java.io.IOException("files exists");
		
		boolean successImg = tmpImg.renameTo(nuevaImg);
		boolean successJson = tmpJson.renameTo(nuevaJson);
		
		if(successImg && successJson) return date.toString();
		
		return null;
	}


	public String obtenerImg() throws IOException {
		String ruta = home + mesaConfig.getRutacaptura();
		File tmpImg = new File(ruta+"tmpImg.png");		
		InputStream in = FileUtils.openInputStream(tmpImg);
		byte[] bArry = IOUtils.toByteArray(in);
		
		StringBuilder sb = new StringBuilder();
		sb.append("data:image/png;base64,");
		sb.append(StringUtils.newStringUtf8(Base64.encodeBase64(bArry, false)));
		String encoded = sb.toString();	
		
		return encoded;
	}

}
