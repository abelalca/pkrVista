/**
 * 
 */
package com.abp.pkr.pkrVista.ngc;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.abp.pkr.pkrVista.dto.HandInfoDto;
import com.abp.pkr.pkrVista.dto.MesaConfig;
import com.abp.pkr.pkrVista.dto.MesaConfig.Zona;
import com.abp.pkr.pkrVista.utl.UtilView;

import ch.qos.logback.classic.Logger;
import net.coobird.thumbnailator.Thumbnails;
import net.sourceforge.tess4j.Tesseract;

/**
 * @author abpubuntu
 *
 */
public class CapturadorOcrNgcImpl implements CapturadorNgc {

	private static final Logger log = (Logger) LoggerFactory.getLogger(CapturadorOcrNgcImpl.class);

	protected static Map<String, BufferedImage> buffPalos = null;
//	protected static Map<String, BufferedImage> buffCartas = null;
	protected static Map<String, BufferedImage> buffColaImagenes = null;

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
		
		// inicializo buffer de imagenes cartas
//		buffCartas = new HashMap<>();
//		String rutaCartas = mesaConfig.getRutaCartas();
//		capturador.filesToBuffer(buffCartas, rutaCartas);		
		

		// inicilizo buffer de cola de imagenes para guardar errores
		buffColaImagenes = new HashMap<>();

	}

	/**
	 * @author Alesso
	 * @date 29018-05-27
	 * @throws Exception
	 */
	protected HandInfoDto procesarZonas(BufferedImage screenImg) throws Exception {
		HandInfoDto handInfoDto = new HandInfoDto();

		String langTesse = mesaConfig.getTessLeng().toUpperCase();
		int factorResize = Integer.valueOf(mesaConfig.getFactorResize());
		Object[] objArr = null;
		Object object = null;

		// Leer Cartas Hero
		List<Zona> configCartas = mesaConfig.getCartas();
		objArr = leerInfoPorOCR(screenImg, configCartas, TIPO_DATO.STR, "CARTAS", 2, handInfoDto);
		String infoCartas = "";
		if (ArrayUtils.isNotEmpty(objArr)) {
			object = objArr[0];
			infoCartas = object.toString();
			infoCartas = infoCartas.replace("10", "T");
		} else {
			throw new Exception("No se pudo reconocer cartas");
		}

		if (infoCartas.length() != 2) {
			throw new Exception("Error al leer cartas");
		}
		log.debug("Leyendo cartas de Hero: " + infoCartas);

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
		log.debug("Leyendo cartas y palos de Hero: " + cartas);

		// Leer numero de jugadores
		Zona numJug = mesaConfig.getNumjug();
		List<Zona> configNumJug = new ArrayList<>();
		configNumJug.add(numJug);
		objArr = leerInfoPorOCR(screenImg, configNumJug, TIPO_DATO.INT, "NUM", factorResize, handInfoDto);
		if (ArrayUtils.isNotEmpty(objArr)) {
			object = objArr[0];
		}
		Integer[] infoNumJug = Arrays.copyOf(objArr, objArr.length, Integer[].class);
		if (ArrayUtils.isEmpty(infoNumJug)) {
			throw new Exception("No se pudo leer numero de jugadores");
		}
		handInfoDto.setNumjug(Integer.valueOf(infoNumJug[0]));
		log.debug("Leyendo numero de jugadores: " + handInfoDto.getNumjug());

		// Leer info stacks
		List<Zona> configStacks = mesaConfig.getStack();
		objArr = leerInfoPorOCR(screenImg, configStacks, TIPO_DATO.DEC, "NUM", factorResize, handInfoDto);
		Double[] infoStacks = Arrays.copyOf(objArr, objArr.length, Double[].class);
		handInfoDto.setStacksBb(infoStacks);
		if (ArrayUtils.isEmpty(infoStacks) || infoStacks.length < 2) {
			throw new Exception("Stacks vacios o null");
		}
		log.debug("Leyendo stacks: " + Arrays.toString(infoStacks));

		// Leer Posicion Hero
		Zona configPosicionHero = mesaConfig.getPosicion();
		List<Zona> configPos = new ArrayList<>();
		configPos.add(configPosicionHero);
		objArr = leerInfoPorOCR(screenImg, configPos, TIPO_DATO.STR, "POS", factorResize, handInfoDto);
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
		log.debug("Leyendo posicion de Hero: " + infoPosiHero);

		// leer silla Hero en la mesa
		String configSillaHero = mesaConfig.getSillahero();
		Integer infoSillaHero = Integer.valueOf(configSillaHero);
		handInfoDto.setSillaHero(infoSillaHero);
		log.debug("Leyendo Silla de Hero: " + configSillaHero);

		// posicion de los jugadores eliminados
		List<Integer> posEliminados = new ArrayList<>();

		int i = 0;
		boolean[] activos = new boolean[configStacks.size()];
		for (Zona zona : configStacks) {
			if (!zona.isLecturaValida()) {
				posEliminados.add(i);
				activos[i] = false;
			} else {
				activos[i] = true;
			}
			i++;
		}
		handInfoDto.setIsActivo(activos);
		log.debug("Leyendo posicion Jugadores Activos: "
				+ posEliminados.stream().map(Object::toString).collect(Collectors.joining(", ")));

		// Leer posicion del button en el array de stacks
		int posBu = -1;
		switch (handInfoDto.getNumjug()) {
		case 3:
			if (infoPosiHero == 0) {
				posBu = 2;
			} else if (infoPosiHero == 1) {
				posBu = 0;
			} else if (infoPosiHero == 2) {
				posBu = 1;
			}
			break;
		case 2:
			if (posEliminados.get(0) == 0 && infoPosiHero == 1) {
				posBu = 0;
			} else if (posEliminados.get(0) == 0 && infoPosiHero == 0) {
				posBu = 1;
			} else if (posEliminados.get(0) == 2 && infoPosiHero == 1) {
				posBu = 1;
			} else if (posEliminados.get(0) == 2 && infoPosiHero == 0) {
				posBu = 0;
			}
		default:
			break;
		}
		handInfoDto.setBtnPos(posBu);
		log.debug("Leyendo posicion de Buton: " + posBu);

		handInfoDto.setUsuario(mesaConfig.getUsuario());
		log.debug("Leyendo usuario: " + handInfoDto.getUsuario());

		handInfoDto.setEstrategia(mesaConfig.getEstrategia());
		log.debug("Leyendo estrategia: " + handInfoDto.getEstrategia());

		return handInfoDto;

	}

	/**
	 * METODO DE PRUEBA PARA VER SI SE PUEDE PROCESAR OCR EN PARALELO CON EL FIN DE
	 * MEJORAR PERFORMANCE
	 * 
	 * @param screenImg
	 * @return
	 * @throws Exception
	 */
	protected HandInfoDto procesarZonasParalelo(BufferedImage screenImg) throws Exception {
		HandInfoDto handInfoDto = new HandInfoDto();

		String langTesse = mesaConfig.getTessLeng().toUpperCase();
		int factorResize = Integer.valueOf(mesaConfig.getFactorResize());
		Object[] objArr = null;
		Object object = null;

		// agregamos todas las zonas a una lista a iterar en paralelo
		List<Zona> listImages = new ArrayList<>();

		Zona numJug = mesaConfig.getNumjug();
		numJug.setNombre("numJug");
		listImages.add(numJug);

		List<Zona> configCartas = mesaConfig.getCartas();
		int j=0;
		for (Zona zona3 : configCartas) {
			zona3.setNombre("cartas" + j);
			j++;
		}
		listImages.addAll(configCartas);

		List<Zona> configStacks = mesaConfig.getStack();
		int i = 0;
		for (Zona zona2 : configStacks) {
			zona2.setNombre("stacks" + i);
			i++;
		}
		listImages.addAll(configStacks);

		Zona configPosicionHero = mesaConfig.getPosicion();
		configPosicionHero.setNombre("posi");
		listImages.add(configPosicionHero);

		Zona[] zonaArray = new Zona[listImages.size()];
		zonaArray = listImages.toArray(zonaArray);

		List<Object> objArry = new ArrayList<>();
		Arrays.stream(zonaArray).parallel().forEach((zona) -> {
			TIPO_DATO tipoDat = null;
			try {
				List<Zona> zonaList = new ArrayList<>();
				zonaList.add(zona);
				int factorSize = Integer.valueOf(mesaConfig.getFactorResize());
				String lengTesse = null;
				if (zona.getNombre().contains("cartas")) {
					lengTesse = "CARTAS";
					factorSize = 2;
					tipoDat = TIPO_DATO.STR;
				}
				if (zona.getNombre().equals("numJug")) {
					lengTesse = "NUM";
					tipoDat = TIPO_DATO.INT;
				}
				if (zona.getNombre().contains("stacks")) {
					lengTesse = "NUM";
//					factorSize = 1;
					tipoDat = TIPO_DATO.DEC;
				}
				if (zona.getNombre().equals("posi")) {
					lengTesse = "POS";
					tipoDat = TIPO_DATO.STR;
				}
				Object[] obj = leerInfoPorOCR(screenImg, zonaList, tipoDat, lengTesse, factorSize, handInfoDto);
				boolean lectura = normalizarDatos(obj, zona, handInfoDto);
				objArry.add(lectura);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		});

		// stacks
		handInfoDto.setStacksBb(handInfoDto.obtenerStack());

		// *** obtener palos por analisis de colores de las cartas
		List<Zona> configPalos = mesaConfig.getPalos();
		objArr = capturador.leerInfoPorPixel(screenImg, configPalos, buffPalos, "", TIPO_DATO.STR);
		String[] infoPalos = Arrays.copyOf(objArr, objArr.length, String[].class);
		String infoCartas = handInfoDto.getHand();
		String cartas = "";
		if (StringUtils.isNotBlank(infoCartas) && infoPalos.length > 1) {
			String first = infoCartas.substring(0, 1);
			String second = infoCartas.substring(1, 2);
			cartas = first + infoPalos[0] + second + infoPalos[1];
		}
		handInfoDto.setHand(cartas);

		if (cartas.length() != 4) {
			throw new Exception("No se pudo reconocer cartas");
		}
		log.debug("Leyendo cartas y palos de Hero: " + cartas);

		// posicion de los jugadores eliminados
		handInfoDto.setIsActivo(handInfoDto.getIsActivo());
		log.debug("Leyendo posicion Jugadores Activos: " + Arrays.toString(handInfoDto.getIsActivo()));

		// Leer posicion del button en el array de stacks
		int posBu = -1;
		Integer infoPosiHero = handInfoDto.getPosHero();
		boolean[] posActivo = handInfoDto.getIsActivo();
		switch (handInfoDto.getNumjug()) {
		case 3:
			if (infoPosiHero == 0) {
				posBu = 2;
			} else if (infoPosiHero == 1) {
				posBu = 0;
			} else if (infoPosiHero == 2) {
				posBu = 1;
			}
			break;
		case 2:
			if (posActivo[2] && infoPosiHero == 1) {
				posBu = 0;
			} else if (posActivo[2] && infoPosiHero == 0) {
				posBu = 1;
			} else if (posActivo[0] && infoPosiHero == 1) {
				posBu = 1;
			} else if (posActivo[0] && infoPosiHero == 0) {
				posBu = 0;
			}
		default:
			break;
		}
		handInfoDto.setBtnPos(posBu);
		log.debug("Leyendo posicion de Buton: " + posBu);

		// leer silla Hero en la mesa
		String configSillaHero = mesaConfig.getSillahero();
		Integer infoSillaHero = Integer.valueOf(configSillaHero);
		handInfoDto.setSillaHero(infoSillaHero);
		log.debug("Leyendo Silla de Hero: " + configSillaHero);

		handInfoDto.setUsuario(mesaConfig.getUsuario());
		log.debug("Leyendo usuario: " + handInfoDto.getUsuario());

		handInfoDto.setEstrategia(mesaConfig.getEstrategia());
		log.debug("Leyendo estrategia: " + handInfoDto.getEstrategia());

		return handInfoDto;

	}

	private boolean normalizarDatos(Object[] objArr, Zona zona, HandInfoDto handInfoDto) throws Exception {
		Object object = null;
		// Leer Cartas Hero
		if (zona.getNombre().contains("cartas")) {
			String infoCartas = "";
			if (ArrayUtils.isNotEmpty(objArr)) {
				object = objArr[0];
				infoCartas = object.toString();
				infoCartas = infoCartas.replace("10", "T");
			} else {
				throw new Exception("Sin datos para leer");
			}
			
			if (("AKQJT98765432".contains(infoCartas))) {
				handInfoDto.addCarta(infoCartas);				
			}else {
				throw new Exception("No se pudo reconocer cartas");				
			}	
			
			if (handInfoDto.getCartas().size() == 2) {
				String mano = String.join("", handInfoDto.getCartas());
				handInfoDto.setHand(mano);
				log.debug("Leyendo cartas de Hero: " + mano);				
			}
		}

		if (zona.getNombre().equals("numJug")) {
			// Leer numero de jugadores
			if (ArrayUtils.isNotEmpty(objArr)) {
				object = objArr[0];
			} else {
				throw new Exception("Sin datos para leer");
			}
			handInfoDto.setNumjug(Integer.valueOf(Integer.valueOf(object.toString())));
			log.debug("Leyendo numero de jugadores: " + handInfoDto.getNumjug());
		}

		if (zona.getNombre().contains("stacks")) {
			// Leer info stacks
			if (objArr == null) {
				throw new Exception("Sin datos para leer");
			}
			int posic = Integer.valueOf(zona.getNombre().substring(6, 7));
			String valor = objArr[0].toString().replace(",", ".");
			handInfoDto.addStack(Double.valueOf(valor), posic);
			log.debug("Leyendo " + zona.getNombre() + ": " + valor);
		}

		if (zona.getNombre().equals("posi")) {
			// Leer Posicion Hero
			if (ArrayUtils.isNotEmpty(objArr)) {
				object = objArr[0];
			} else {
				throw new Exception("Sin datos para leer");
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
			log.debug("Leyendo posicion de Hero: " + infoPosiHero);

		}

		return true;

	}

	/**
	 * @author Alesso
	 * @param handInfoDto
	 * @date 29018-05-27
	 * @throws Exception
	 */
	private Object[] leerInfoPorOCR(BufferedImage screenImg, List<Zona> listaConfig, TIPO_DATO tipoDato,
			String lengTesseract, int factorResize, HandInfoDto handInfoDto) throws Exception {

		List<Object> infoList = new ArrayList<>();

		// itero cada posicion de coordenadas
		for (Zona zona : listaConfig) {
			zona.setLecturaValida(true);
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
				instance.setTessVariable("tessedit_char_whitelist", "0123456789.,");
				List<String> configs = new ArrayList<>();
				configs.add("digits");
				instance.setConfigs(configs);
			} else if (lengTesseract != null && lengTesseract.equals("NUMSTR")) {
				instance.setTessVariable("tessedit_char_whitelist", "0123456789abcdefghijklmnopqrstuvwxyz-");
			} else if (lengTesseract != null && lengTesseract.equals("CARTAS")) {
				instance.setTessVariable("tessedit_char_whitelist", "AKQJ9876543210");
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
				log.debug("obteniendo valor por OCR, valor: {}", result);
				if (StringUtils.isNotBlank(result)) {
					switch (tipoDato) {
					// entero
					case INT:
						infoList.add(Integer.valueOf(result));
						break;
					// decimal
					case DEC:
						result = result.replaceAll(",", ".");
						if (result.substring(result.length() - 1).equals(".")) {
							result = result.substring(0, result.length() - 1);
						}
						try {
							infoList.add(Double.valueOf(result));
						} catch (Exception e) {
							zona.setLecturaValida(false);
						}
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
				throw new Exception("Error al leer imagen por OCR: " + e.getMessage());

			}
		}
		Object[] arr = new Object[infoList.size()];
		arr = infoList.toArray(arr);
		return arr;
	}

	/**
	 * @author abpubuntu
	 * @date Jun 5, 2017
	 * @return
	 * @throws Exception
	 */
	public String guardarInfo() throws Exception {
		String ruta = home + mesaConfig.getRutaBugs();
		Long date = new Date().getTime();

		File tmpImg = new File(ruta + "\\tmpImg.png");
		File tmpJson = new File(ruta + "\\tmpJson");

		BufferedImage bf = ImageIO.read(tmpImg);

		File nuevaImg = new File(ruta + "\\" + date.toString() + ".png");
		File nuevaJson = new File(ruta + "\\" + date.toString());

		if (nuevaImg.exists() && nuevaJson.exists())
			throw new java.io.IOException("files exists");

		UtilView.guardarImagen(bf, ruta + "\\" + date.toString() + ".png");
		boolean successJson = tmpJson.renameTo(nuevaJson);

		if (successJson)
			return date.toString();

		return null;
	}

	/**
	 * @author abpubuntu
	 * @date Jun 4, 2017
	 * @return
	 * @throws Exception
	 */
	@Override
	public HandInfoDto extraerMesaInfo(Zona mesaActual) throws Exception {
		HandInfoDto handInfoDto = null;

		// Si el mouse no esta dentro de ninguna mesa
		if (mesaActual == null) {
			return handInfoDto;
		}
		log.debug("Obteniendo coordenadas mesa actual (x,y): " + mesaActual.getX() + ", " + mesaActual.getY());

		// capturo screen
		Rectangle zona = new Rectangle(mesaActual.getX(), mesaActual.getY(), mesaActual.getAncho(),
				mesaActual.getAlto());
		BufferedImage screenImg = capturador.capturarScreenZona(zona);
		log.debug("Capturando Imagen (ancho,alto): " + screenImg.getWidth() + ", " + screenImg.getHeight());

		almacenarColaImagenes(screenImg, mesaActual);

		// procesamos zonas
		try {
			log.debug("PROCESANDO IMAGEN...");
			handInfoDto = procesarZonasParalelo(screenImg);
		} catch (Exception e) {
			log.error(e.getMessage());

			handInfoDto = null;
		}

		return handInfoDto;
	}

	private void almacenarColaImagenes(BufferedImage screenImg, Zona mesaActual) {

		if (buffColaImagenes.containsKey(mesaActual.getNombre())) {
			buffColaImagenes.remove(mesaActual.getNombre());
		}
		buffColaImagenes.put(mesaActual.getNombre(), screenImg);

	}

	public void almacenarImagenCuadrante(String cuadrante) {
		String ruta = home + mesaConfig.getRutacaptura() + "\\bugs";
		Long date = new Date().getTime();
		try {
			BufferedImage img = buffColaImagenes.get(cuadrante);
			UtilView.guardarImagen(img, ruta + "\\" + cuadrante + "_" + date.toString() + ".png");
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public List<String> obtenerNombresCuadrantes() {
		List<Zona> mesas = mesaConfig.getMesa();
		List<String> cuadrantes = new ArrayList<>();
		for (Zona zona : mesas) {
			cuadrantes.add(zona.getNombre());
		}

		java.util.Collections.sort(cuadrantes);

		return cuadrantes;
	}

}
