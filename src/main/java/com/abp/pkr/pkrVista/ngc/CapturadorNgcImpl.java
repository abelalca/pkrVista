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
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.abp.pkr.pkrVista.dto.HandInfoDto;
import com.abp.pkr.pkrVista.dto.MesaConfig;
import com.abp.pkr.pkrVista.dto.MesaConfig.Zona;
import com.abp.pkr.pkrVista.utl.UtilView;

import ch.qos.logback.classic.Logger;

/**
 * @author abpubuntu
 *
 */
@Controller
public class CapturadorNgcImpl implements CapturadorNgc {

	@Autowired
	protected MesaConfig mesaConfig;

	private static final Logger log = (Logger) LoggerFactory.getLogger(CapturadorNgcImpl.class);

	public MesaConfig getMesaConfig() {
		return mesaConfig;
	}

	public void setMesaConfig(MesaConfig mesaConfig) {
		this.mesaConfig = mesaConfig;
	}

	protected Object[] leerInfoPorPixel(BufferedImage img, List<Zona> listaConfig, Map<String, BufferedImage> bufferImg,
			String delimiter, TIPO_DATO tipoDato) throws Exception {

		List<Object> infoList = new ArrayList<>();
		// itero cada posicion del stack
		for (Zona zona : listaConfig) {
			Rectangle scanZona = new Rectangle(zona.getX(), zona.getY(), zona.getAncho(), zona.getAlto());
			Rectangle imgZona = new Rectangle(zona.getX(), zona.getY(), 0, 0);
			List<String> lectura = recorrerZonaImg(scanZona, imgZona, img, bufferImg);
			if (lectura.size() != 0) {
				String stackConcat = String.join(delimiter, lectura);
				try {
					switch (tipoDato) {
					// entero
					case INT:
						infoList.add(Double.valueOf(stackConcat));
						break;
					// decimal
					case DEC:
						infoList.add(Double.valueOf(stackConcat) / 10d);
						break;
					// string
					case STR:
						infoList.add(stackConcat);
						break;
					default:
						infoList.add(stackConcat);
						break;
					}
				} catch (Exception e) {
					throw e;
				}
			}
		}
		Object[] arr = new Object[infoList.size()];
		arr = infoList.toArray(arr);
		return arr;
	}

	/**
	 * Recibe scanZona=Area donde esta la info y inicioZona=punto inicial de partida
	 * y screenImg= imagen del screenshot y comparaImg= imagen en directorio para
	 * comparar. Este metodo compara un directorio de imgenes con la img,
	 * recorriendo cada zona donde esta la info
	 */
	private List<String> recorrerZonaImg(Rectangle scanZona, Rectangle inicioZona, BufferedImage screenImg,
			Map<String, BufferedImage> comparaImg) throws Exception {

		List<String> encontrados = new ArrayList<>();
		// recorrer mientras la coordenada de la imgZona llegue a el ultimo
		// punto de la scanZona
		Map<String, BufferedImage> comp = new HashMap<>();
		boolean seguir = true;
		String fileName;
		int trasladar = 1;
		while (seguir) {
			comp = comparaImgDir(screenImg, inicioZona, comparaImg);
			if (comp.size() > 0) {
				Map.Entry<String, BufferedImage> key = comp.entrySet().iterator().next();
				fileName = key.getKey();
				if (fileName != null) {
					if (fileName.contains("_")) {
						fileName = fileName.split("_")[0];
					}
					encontrados.add(fileName);
					trasladar = key.getValue().getWidth();
				}
			} else {
				fileName = null;
				trasladar = 1;
			}

			// trasladamos la imagen el ancho de la imagen que coincidio
			inicioZona.translate(trasladar, 0);
			if ((scanZona.getX() + scanZona.getWidth()) <= inicioZona.getX()) {
				seguir = false;
			}
			log.trace("corrimiento zona, coord X: " + inicioZona.getX() + " , ancho: " + inicioZona.getWidth());
		}
		return encontrados;
	}

	/**
	 * Metodo recibe screenImg=imagen del screenshot y imgZona=coordenada para
	 * comparar, este metodo recorre todas las imagenes de comparacion
	 * 
	 */
	private Map<String, BufferedImage> comparaImgDir(BufferedImage screenImg, Rectangle imgZona,
			Map<String, BufferedImage> comparaImg) throws Exception {

		double marginError = mesaConfig.getMargenerror().doubleValue();
		Map<String, BufferedImage> arch = new HashMap<>();
		for (Map.Entry<String, BufferedImage> file : comparaImg.entrySet()) {
			double error = comparaImgZona(file.getValue(), screenImg, imgZona, file.getKey());
			if (error <= (marginError * 100)) {
				arch.put(file.getKey(), file.getValue());
				// a la primera coincidencia rompemos el ciclo
				break;
			}
		}
		return arch;
	}

	private double imagePctError(BufferedImage imgA, BufferedImage imgB) throws Exception {
		long diff = 0;
		double n = imgA.getWidth() * imgA.getHeight() * 3;
		double p = 0;
		double marginError = mesaConfig.getMargenerror().doubleValue();
		for (int y = 0; y < imgA.getHeight(); y++) {
			for (int x = 0; x < imgA.getWidth(); x++) {
				int rgb1 = imgA.getRGB(x, y);
				int rgb2 = imgB.getRGB(x, y);
				int r1 = new Color(rgb1).getRed();
				int g1 = new Color(rgb1).getGreen();
				int b1 = new Color(rgb1).getBlue();
				int r2 = new Color(rgb2).getRed();
				int g2 = new Color(rgb2).getGreen();
				int b2 = new Color(rgb2).getBlue();
				diff += Math.abs(r1 - r2);
				diff += Math.abs(g1 - g2);
				diff += Math.abs(b1 - b2);
				// Si el error es mayor que el margen de error, salgase de una
				// vez
				p = diff / n / 255.0;
				if (p > marginError) {
					return 100;
				}
			}
		}
		p = diff / n / 255.0;
		return (p * 100.0);
	}

	/**
	 * Compara un archivo con una imagen screeshot con coordenada x,y que tiene el
	 * ancho y alto del archivo
	 * 
	 * @author abpubuntu
	 * @date May 13, 2017
	 * @param file
	 * @param img
	 * @param zonaCompara
	 * @return
	 * @throws Exception
	 */
	private double comparaImgZona(BufferedImage file, BufferedImage img, Rectangle zonaCompara, String filename)
			throws Exception {

		double error = 1001;
		try {
			BufferedImage imgCoRec = UtilView.recortarImagen(file, null);
			Rectangle rec = new Rectangle((int) zonaCompara.getX(), (int) zonaCompara.getY(), file.getWidth(),
					(int) file.getHeight());
			BufferedImage imgRec = UtilView.recortarImagen(img, rec);

			// String rutaGuardar = home + mesaConfig.getRutacaptura();
			// UtilView.guardarImagen(imgCoRec, rutaGuardar + "comp");
			// UtilView.guardarImagen(imgRec, rutaGuardar + "img_" + rec.getX()
			// + "_f_" + filename + "_" + rec.getWidth());

			error = imagePctError(imgRec, imgCoRec);
			log.debug("Comparando Imagenes, imagen comparar= {}", file.toString());
		} catch (Exception e) {
			log.error("Error comparando imagenes, imagen comparar= {}", file.toString());
			throw e;
		}
		return error;
	}

	protected void filesToBuffer(Map<String, BufferedImage> buffer, String ruta) throws Exception {
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

	/**
	 * Me dice sobre que mesa se encuentra el mouse
	 * 
	 * @author Alesso
	 * @date May 27, 2018
	 * @return
	 */
	protected Zona mesaMouse() {
		Point p = MouseInfo.getPointerInfo().getLocation();
		List<Zona> mesas = mesaConfig.getMesa();
		Zona mesaActual = null;
		for (Zona mesa : mesas) {

			if (p.getX() > mesa.getX() && p.getX() < (mesa.getX() + mesa.getAncho())) {
				if (p.getY() > mesa.getY() && p.getY() < (mesa.getY() + mesa.getAlto())) {
					mesaActual = new Zona();
					mesaActual.setX(mesa.getX());
					mesaActual.setY(mesa.getY());
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
	public HandInfoDto extraerMesaInfo() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


}
