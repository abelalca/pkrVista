/**
 * 
 */
package com.abp.pkr.pkrVista.ngc;

import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import com.abp.pkr.pkrVista.dto.AccionInfoDto;
import com.abp.pkr.pkrVista.dto.HandInfoDto;
import com.abp.pkr.pkrVista.dto.MesaConfig;
import com.abp.pkr.pkrVista.dto.MesaConfig.Zona;
import com.abp.pkr.pkrVista.utl.UtilView;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Concurrent.Filters.Grayscale;
import ch.qos.logback.classic.Logger;

/**
 * @author abpubuntu
 *
 */
@Controller
public class CapturadorNgcImpl implements CapturadorNgc {

	@Autowired
	protected MesaConfig mesaConfig;

	@Autowired
	protected CapturadorNgcImpl capturador;

	private static final Logger log = (Logger) LoggerFactory.getLogger(CapturadorNgcImpl.class);

	public MesaConfig getMesaConfig() {
		return mesaConfig;
	}

	public void setMesaConfig(MesaConfig mesaConfig) {
		this.mesaConfig = mesaConfig;
	}

	protected void filesToBuffer(Map<String, byte[]> buffer, String ruta) throws Exception {
		try {
			File rut = new File(home + ruta);
			List<File> archivos = UtilView.obtenerFilesDir(rut);

			for (File file : archivos) {
				BufferedImage imgComp = ImageIO.read(file);
				byte[] pixels = ((DataBufferByte) imgComp.getRaster().getDataBuffer()).getData();
				String archName = file.getName().split(mesaConfig.getFormato())[0];
				buffer.put(archName, pixels);
			}

		} catch (Exception e) {
			log.error("Error al inicializar buffer de ruta: {}", ruta);
			throw e;
		}
	}

	protected void filesToBufferImg(Map<String, BufferedImage> buffer, String ruta) throws Exception {
		try {
			File rut = new File(home + ruta);
			List<File> archivos = UtilView.obtenerFilesDir(rut);

			for (File file : archivos) {
				BufferedImage imgComp = ImageIO.read(file);
				String archName = file.getName().split(mesaConfig.getFormato())[0];
				buffer.put(archName, imgComp);
			}

		} catch (Exception e) {
			log.error("Error al inicializar buffer de ruta: {}", ruta);
			throw e;
		}
	}

	protected void filesToFastBitMap(Map<String, FastBitmap> buffer, String ruta) throws Exception {
		try {
			File rut = new File(home + ruta);
			List<File> archivos = UtilView.obtenerFilesDir(rut);

			for (File file : archivos) {
				BufferedImage imgComp = ImageIO.read(file);
				FastBitmap reconstructed = new FastBitmap(imgComp);
				Grayscale rec = new Grayscale();
				rec.applyInPlace(reconstructed);
				String archName = file.getName().split(mesaConfig.getFormato())[0];
				buffer.put(archName, reconstructed);
			}

		} catch (Exception e) {
			log.error("Error al inicializar buffer de ruta: {}", ruta);
			throw e;
		}
	}

	/**
	 * Me dice sobre que mesa se encuentra el mouse
	 * 
	 * @author Alesso
	 * @date May 27, 2018
	 * @return
	 */
	public Zona mesaMouse() {
		Point p = MouseInfo.getPointerInfo().getLocation();
		List<Zona> mesas = mesaConfig.getMesa();
		Zona mesaActual = null;
		for (Zona mesa : mesas) {

			if (p.getX() > mesa.getX() && p.getX() < (mesa.getX() + mesa.getAncho())) {
				if (p.getY() > mesa.getY() && p.getY() < (mesa.getY() + mesa.getAlto())) {
					mesaActual = new Zona();
					mesaActual.setX(mesa.getX());
					mesaActual.setY(mesa.getY());
					mesaActual.setAlto(mesa.getAlto());
					mesaActual.setAncho(mesa.getAncho());
					mesaActual.setLecturaValida(true);
					mesaActual.setNombre(mesa.getNombre());
				}
			}
		}
		return mesaActual;
	}

	/**
	 * Captura pantalla de determinada zona
	 * 
	 * @author abpubuntu
	 * @date May 9, 2017
	 * @param rutaGuardar
	 * @param zona
	 * @return
	 * @throws Exception
	 */
	public BufferedImage capturarScreenZona(Rectangle zona) throws Exception {
		log.trace("Capturando screen en ruta= {}, y hora= {}", home + mesaConfig.getRutacaptura(),
				new Date().getTime());

		BufferedImage screenImage = null;
		try {
			Robot robot = new Robot();
			screenImage = robot.createScreenCapture(zona);

			// String fileName = home + mesaConfig.getRutacaptura() +
			// mesaConfig.getNombrearchivo() + mesaConfig.getFormato();
			// ImageIO.write(screenImage, mesaConfig.getFormato(), new
			// File(fileName));
		} catch (Exception ex) {
			log.error("Error capturando imagen en ruta= {} y hora= {}", home + mesaConfig.getRutacaptura(),
					new Date().getTime());
			throw ex;
		}
		return screenImage;
	}

	@Override
	public HandInfoDto extraerMesaInfo(Zona mesaActual) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
