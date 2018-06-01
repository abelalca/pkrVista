/**
 * 
 */
package com.abp.pkr.pkrVista.ngc;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.abp.pkr.pkrVista.dto.HandInfoDto;
import com.abp.pkr.pkrVista.dto.MesaConfig;
import com.abp.pkr.pkrVista.dto.MesaConfig.Zona;
import com.abp.pkr.pkrVista.ngc.CapturadorNgc.TIPO_DATO;

import ch.qos.logback.classic.Logger;
import net.coobird.thumbnailator.Thumbnails;
import net.sourceforge.tess4j.ITessAPI.TessBaseAPI;
import net.sourceforge.tess4j.Tesseract;

/**
 * @author abpubuntu
 *
 */
public class CapturadorOcrNgcImpl implements CapturadorNgc {

	private static final Logger log = (Logger) LoggerFactory.getLogger(CapturadorOcrNgcImpl.class);

	protected static Map<String, BufferedImage> buffPalos = null;

	@Autowired
	protected CapturadorNgcImpl capturador;

	@Autowired
	protected MesaConfig mesaConfig;

	public MesaConfig getMesaConfig() {
		return mesaConfig;
	}

	public void setMesaConfig(MesaConfig mesaConfig) {
		this.mesaConfig = mesaConfig;
	}

	public CapturadorNgcImpl getCapturador() {
		return capturador;
	}

	public void setCapturador(CapturadorNgcImpl capturador) {
		this.capturador = capturador;
	}

	@PostConstruct
	public void inicializarParametros() throws Exception {
		// inicializo buffer de imagenes palos
		buffPalos = new HashMap<>();
		String rutaPalos = mesaConfig.getRutaPalos();
		capturador.filesToBuffer(buffPalos, rutaPalos);
	}

	/**
	 * @author Alesso
	 * @date 29018-05-27
	 * @throws Exception
	 */
	protected HandInfoDto procesarZonas(BufferedImage screenImg) throws Exception {
		log.debug("Procesando Imagen cuadrante");
		HandInfoDto handInfoDto = new HandInfoDto();

		String langTesse = mesaConfig.getTessLeng().toUpperCase();
		int factorResize = Integer.valueOf(mesaConfig.getFactorResize());
		Object[] objArr = null;
		Object object = null;

		// Leer Cartas Hero
		Zona configCartas = mesaConfig.getCartas();
		List<Zona> configCartasHero = new ArrayList<>();
		configCartasHero.add(configCartas);
		objArr = leerInfoPorOCR(screenImg, configCartasHero, TIPO_DATO.STR, "CARTAS", 2);
		String infoCartas = "";
		if (ArrayUtils.isNotEmpty(objArr)) {
			object = objArr[0];
			infoCartas = object.toString();
			infoCartas = infoCartas.replace("10", "T");
		} else {
			throw new Exception("No se pudo reconocer cartas");
		}

		// *** obtener palos por analisis de colores de las cartas
		List<Zona> configPalos = mesaConfig.getPalos();
		objArr = capturador.leerInfoPorPixel(screenImg, configPalos, buffPalos, "", TIPO_DATO.STR);
		String[] infoPalos = Arrays.copyOf(objArr, objArr.length, String[].class);

		String cartas = "";
		if (StringUtils.isNotBlank(infoCartas) && infoPalos.length > 1) {
			String first = infoCartas.substring(0, 1);
			String second = infoCartas.substring(1, 2);
			cartas = first + infoPalos[0] + second + infoPalos[1];
		}
		handInfoDto.setHand(cartas);

		if (cartas.length() != 4) {
			throw new Exception("No se pudo leer las cartas y/o palos");
		}

		// Leer info stacks
		List<Zona> configStacks = mesaConfig.getStack();
		objArr = leerInfoPorOCR(screenImg, configStacks, TIPO_DATO.DEC, "NUM", factorResize);
		Double[] infoStacks = Arrays.copyOf(objArr, objArr.length, Double[].class);
		handInfoDto.setStacksBb(infoStacks);
		if (ArrayUtils.isEmpty(infoStacks)) {
			throw new Exception("Stacks vacios o null");
		}

		// Leer Posicion Hero
		Zona configPosicionHero = mesaConfig.getPosicion();
		List<Zona> configPos = new ArrayList<>();
		configPos.add(configPosicionHero);
		objArr = leerInfoPorOCR(screenImg, configPos, TIPO_DATO.STR, "POS", factorResize);
		if (ArrayUtils.isNotEmpty(objArr)) {
			object = objArr[0];
		}
		String infoPHero = object.toString();
		Integer infoPosiHero = -1;
		if (infoPHero.trim().contains("BB")) {
			infoPosiHero = 0;
		} else if (infoPHero.trim().contains("SB")) {
			infoPosiHero = 1;
		} else if (infoPHero.trim().contains("BU")) {
			infoPosiHero = 2;
		}
		

		handInfoDto.setPosHero(infoPosiHero);
		if (infoPosiHero == -1) {
			throw new Exception("No se pudo leer posicion de Hero");
		}

		
		// leer silla Hero en la mesa
		String configSillaHero = mesaConfig.getSillahero();
		Integer infoSillaHero = Integer.valueOf(configSillaHero);
		handInfoDto.setSillaHero(infoSillaHero);
		
		// Leer posicion del button en el array de stacks
		int posBu = 2;
		int diffBu = infoPosiHero - posBu;
		Integer posHeroMesa = null;
		if (diffBu >= 0) {
			posHeroMesa = infoSillaHero + diffBu;
		} else {
			if (infoSillaHero + diffBu >= 0) {
				posHeroMesa = infoSillaHero + diffBu;
			} else {
				posHeroMesa = infoSillaHero + Math.abs(infoSillaHero + diffBu);
			}
		}
		handInfoDto.setBtnPos(posHeroMesa);
		
		return handInfoDto;

	}

	/**
	 * @author Alesso
	 * @date 29018-05-27
	 * @throws Exception
	 */
	private Object[] leerInfoPorOCR(BufferedImage screenImg, List<Zona> listaConfig, TIPO_DATO tipoDato,
			String lengTesseract, int factorResize) {

		List<Object> infoList = new ArrayList<>();

		// itero cada posicion de coordenadas
		for (Zona zona : listaConfig) {
			// recortamos subimagen
			BufferedImage recortada = screenImg.getSubimage(zona.getX(), zona.getY(), zona.getAncho(), zona.getAlto());
			// UtilView.guardarImagen(recortada,
			// home+"/Documents/img/capturas/recorte");

			// iniciamos tesseract
			Tesseract instance = new Tesseract();
			instance.setDatapath(mesaConfig.getTessdata());
			instance.setPageSegMode(7);

			// instanciamos el tesserac para reconocer segun lenguaje que necesitamos
			if (lengTesseract != null && lengTesseract.equals("PKR")) {
				instance.setLanguage(mesaConfig.getTessLeng());
			} else if (lengTesseract != null && lengTesseract.equals("NUM")) {
				instance.setTessVariable("tessedit_char_whitelist","0123456789.");
				List<String> configs = new ArrayList<>();
				configs.add("digits");
				instance.setConfigs(configs);
			} else if (lengTesseract != null && lengTesseract.equals("NUMSTR")) {
				instance.setTessVariable("tessedit_char_whitelist", "0123456789abcdefghijklmnopqrstuvwxyz-");
			} else if (lengTesseract != null && lengTesseract.equals("CARTAS")) {
				instance.setTessVariable("tessedit_char_whitelist", "AKQJ1098765432");
			} else if (lengTesseract != null && lengTesseract.equals("POS")) {
				instance.setTessVariable("tessedit_char_whitelist", "SBU");
			}

			// obtenemos imagen por OCR
			try {
				// Ampliamos imagen size
				int ancho = (int) (recortada.getWidth() * factorResize);
				int alto = (int) (recortada.getHeight() * factorResize);
				recortada = Thumbnails.of(recortada).size(ancho, alto).asBufferedImage();

				String result = instance.doOCR(recortada);

				result = result.replaceAll("[\n]+", "\r").replaceAll("\\s", "");
				if (StringUtils.isNotBlank(result)) {
					switch (tipoDato) {
					// entero
					case INT:
						infoList.add(Integer.valueOf(result));
						break;
					// decimal
					case DEC:
						infoList.add(Double.valueOf(result));
						break;
					// string
					case STR:
						infoList.add(result);
						break;
					default:
						infoList.add(result);
						break;
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		Object[] arr = new Object[infoList.size()];
		arr = infoList.toArray(arr);
		return arr;
	}

	// **********************VIEJO*************************
	// **********************VIEJO*************************
	// **********************VIEJO*************************

	/**
	 * @author abpubuntu
	 * @date Jun 4, 2017
	 * @return
	 * @throws Exception
	 */
	@Override
	public HandInfoDto extraerMesaInfo() throws Exception {
		HandInfoDto handInfoDto = null;

		// mesa que estoy parado, debemos obtener las coord del mouse para
		// saber en que mesa estamos
		Zona mesaActual = capturador.mesaMouse();

		// Si el mouse no esta dentro de ninguna mesa
		if (mesaActual == null) {
			return handInfoDto;
		}

		// capturo screen
		Rectangle zona = new Rectangle(mesaActual.getX(), mesaActual.getY(), mesaActual.getAncho(),
				mesaActual.getAlto());
		BufferedImage screenImg = capturador.capturarScreenZona(zona);

		// procesamos zonas
		handInfoDto = procesarZonas(screenImg);

		return handInfoDto;
	}


}
