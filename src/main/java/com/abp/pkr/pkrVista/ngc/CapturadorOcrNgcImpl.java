/**
 * 
 */
package com.abp.pkr.pkrVista.ngc;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.BeanFactoryAspectInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.abp.pkr.pkrVista.dto.AccionInfoDto;
import com.abp.pkr.pkrVista.dto.HandInfoDto;
import com.abp.pkr.pkrVista.dto.MesaConfig;
import com.abp.pkr.pkrVista.dto.MesaConfig.Zona;
import com.abp.pkr.pkrVista.utl.UtilView;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Concurrent.Filters.Grayscale;
import Catalano.Imaging.Tools.ObjectiveFidelity;
import ch.qos.logback.classic.Logger;
import net.coobird.thumbnailator.Thumbnails;
import net.sourceforge.tess4j.Tesseract;

/**
 * @author abpubuntu
 *
 */
public class CapturadorOcrNgcImpl implements CapturadorNgc {

	private static final Logger log = (Logger) LoggerFactory.getLogger(CapturadorOcrNgcImpl.class);

	protected static Map<String, BufferedImage> buffColaImagenes = null;

	protected static List<HandInfoDto> bufferManosAnalizadas = new ArrayList<>();
	protected static Map<String, Integer> bufferValorCiegaAnterior = new HashMap<>();

	// protected static Map<String, BufferedImage> buffCartas = null;
	protected static Map<String, byte[]> buffPalos = null;
	protected static Map<String, byte[]> buffStacks = null;
	protected static Map<String, byte[]> buffCartas = null;
	protected static Map<String, byte[]> buffPosicion = null;
	protected static Map<String, byte[]> buffCiegas = null;

	protected static Map<String, FastBitmap> buffImgPalos = null;
	protected static Map<String, FastBitmap> buffimgStacks = null;
	protected static Map<String, FastBitmap> buffImgCartas = null;
	protected static Map<String, FastBitmap> buffImgPosicion = null;
	protected static Map<String, FastBitmap> buffImgCiegas = null;

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

		// inicializo buffer de imagenes: stacks
		buffimgStacks = new HashMap<>();
		buffStacks = new HashMap<>();
		String rutaStacks = mesaConfig.getRutaStacks();
		capturador.filesToFastBitMap(buffimgStacks, rutaStacks);
		capturador.filesToBuffer(buffStacks, rutaStacks);

		// inicializo buffer de imagenes: posicion
		buffImgPosicion = new HashMap<>();
		buffPosicion = new HashMap<>();
		String rutaPosicion = mesaConfig.getRutaPosicion();
		capturador.filesToFastBitMap(buffImgPosicion, rutaPosicion);
		capturador.filesToBuffer(buffPosicion, rutaPosicion);

		// inicializo buffer de imagenes: cartas
		buffImgCartas = new HashMap<>();
		buffCartas = new HashMap<>();
		String rutaCartas = mesaConfig.getRutaCartas();
		capturador.filesToFastBitMap(buffImgCartas, rutaCartas);
		capturador.filesToBuffer(buffCartas, rutaCartas);

		// inicializo buffer de imagenes: palos
		buffImgPalos = new HashMap<>();
		buffPalos = new HashMap<>();
		String rutaPalos = mesaConfig.getRutaPalos();
		capturador.filesToFastBitMap(buffImgPalos, rutaPalos);
		capturador.filesToBuffer(buffPalos, rutaPalos);

		// inicializo buffer de imagenes: ciegas
		buffImgCiegas = new HashMap<>();
		buffCiegas = new HashMap<>();
		String rutaCiegas = mesaConfig.getRutaCiegas();
		capturador.filesToFastBitMap(buffImgCiegas, rutaCiegas);
		capturador.filesToBuffer(buffCiegas, rutaCiegas);

		// inicilizo buffer de cola de imagenes para guardar errores
		buffColaImagenes = new HashMap<>();
	}

	/**
	 * Adapta los datos capturados por histograma y los corrige para que queden bien
	 * para ser retornados
	 * 
	 * @param mapaLectura
	 * @param handInfoDto
	 * @param contador 
	 * @param zonaCuadrante
	 * @throws Exception
	 */
	public void normalizarDatosHistograma(Map<String, String> mapaLectura, HandInfoDto handInfoDto, String mesaActual, List<Integer> contador)
			throws Exception {

		
		// posicion de los jugadores eliminados
		handInfoDto.setIsActivo(handInfoDto.getIsActivo());
		log.debug("Leyendo posicion Jugadores Activos: " + Arrays.toString(handInfoDto.getIsActivo()));

		boolean[] isActivo = handInfoDto.getIsActivo();

		// Leer info posicion
		for (Entry<String, String> entry : mapaLectura.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			if (key.contains("posi")) {
				if (value == null) {
					throw new Exception("Sin datos para leer posicion");
				}

				String pos = "";
				if (value.equals("D") && handInfoDto.getNumjug() == 3) {
					if (key.equals("posi0")) {
						pos = "SB";
					}
					if (key.equals("posi1")) {
						pos = "BU";
					}
					if (key.equals("posi2")) {
						pos = "BB";
					}
				}

				if (value.equals("D") && handInfoDto.getNumjug() == 2) {
					if (key.equals("posi0") && isActivo[0]) {
						pos = "BB";
					}
					if (key.equals("posi1") && isActivo[0]) {
						pos = "SB";
					}
					if (key.equals("posi1") && isActivo[2]) {
						pos = "SB";
					}
					if (key.equals("posi2") && isActivo[2]) {
						pos = "BB";
					}
				}

				handInfoDto.setPosHero(pos);
				log.debug("Leyendo info posicion > " + key + " : " + value);
			}
		}

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
		
		
		
		
		for (Entry<String, String> entry : mapaLectura.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			// Leer info cartas
			if (key.contains("cartas")) {
				if (value == null || value.contains("X")) {
					throw new Exception("Sin datos para leer cartas");
				}
				if (value.trim().length() > 1) {
					value = value.trim().substring(0, 1);
				}
				int posi = Integer.valueOf(key.substring(6, 7));
				if (posi == 0 && "AKQJT98765432".contains(value.trim())) {
					handInfoDto.addCarta(value.trim(), 0);
				}
				if (posi == 1 && "AKQJT98765432".contains(value.trim())) {
					handInfoDto.addCarta(value.trim(), 2);
				}
				log.debug("Leyendo info carta > " + key + " : " + value);
			}

			// Leer info palos
			if (key.contains("palos")) {
				if (value == null) {
					throw new Exception("Sin datos para leer palos");
				}
				value = value.substring(0, 1);
				int palopos = Integer.valueOf(key.substring(5, 6));
				if (palopos == 0) {
					handInfoDto.addCarta(value.trim(), 1);
				}
				if (palopos == 1) {
					handInfoDto.addCarta(value.trim(), 3);
				}
				log.debug("Leyendo info palos > " + key + " : " + value);
			}

		}

		// Leyendo ciegas
		for (Entry<String, String> entry : mapaLectura.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (key.contains("ciegas")) {
				if (value == null || value.contains("X")) {
					throw new Exception("Sin datos para leer ciegas");
				}

				Integer valorActualCiega = null;
				try {
					valorActualCiega = Integer.valueOf(value.trim());
				} catch (Exception e) {
					throw new Exception("Error leyendo valor de la ciega");
				}

				handInfoDto.setCiega(valorActualCiega);
			}

		}

		// leyendo stacks
		int cuentaFallidosStacks = 0;
		for (Entry<String, String> entry : mapaLectura.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			// Leer info stacks
			if (key.contains("stacks")) {
				if (value == null) {
					throw new Exception("Sin datos para leer stacks");
				}
				int posic = Integer.valueOf(key.substring(6, 7));
				String valor = value.toString().replace(",", ".");
				int index = valor.indexOf("_");
				if (index > 0) {
					valor = valor.substring(0, index);
				}

				try {
					Double val = Double.valueOf(valor);
					if (val > 0) {
						handInfoDto.addStack(val, posic);
					}
				} catch (Exception e) {
					cuentaFallidosStacks++;
				}
				log.debug("Leyendo info stack > " + key + " : " + valor);
			}
		}

		// para corregir bug de la primera mano, como tengo las cartas, entonces seteo
		// todos los stacks a 25 bbs cuando se falla en las 3 lecturas
		if (cuentaFallidosStacks == 3) {
			log.debug("primera mano del sit para el cuadrante {}", mesaActual);
			handInfoDto.addStack(25.0, 0);
			handInfoDto.addStack(25.0, 1);
			handInfoDto.addStack(25.0, 2);
		}

		// se debe actualizar los stacks llegado el caso del que la ciega halla subido
		Integer valorActualCiega = handInfoDto.getCiega();
		Integer valorCiegaAnterior = bufferValorCiegaAnterior.get(mesaActual);
		if (valorCiegaAnterior != null) {
			log.debug("cambio de nivel de ciega, nivel anterior: {} y nivel actual: {}", valorCiegaAnterior,
					valorActualCiega);
			// obtenemos factor conversor entre ciega anterior y ciega actual
			Double factor = (double) (Double.valueOf(valorCiegaAnterior) / Double.valueOf(valorActualCiega));
			log.debug("factor de cambio de ciegas es: {}", factor);
			if (factor < 1.0) {
				Map<Integer, Double> tmpStacks = handInfoDto.getTmpStacks();
				for (Entry<Integer, Double> tmp : tmpStacks.entrySet()) {
					log.debug(
							"stack actual leido en la mesa SIN cambio de ciegas, posicion stack: {} y valor stack: {}",
							tmp.getKey(), tmp.getValue());
					tmpStacks.put(tmp.getKey(), tmp.getValue() * factor);
				}
				handInfoDto.setTmpStacks(tmpStacks);
			}
		}
		
				
		// actualizamos el mapa con el valor actual de la ciega
		Integer numItera = Integer.valueOf(mesaConfig.getNumIteraCaptura());
		log.debug("contador: {} y numero iteraciones: {}", contador.size(), numItera);
		if (contador.size()==numItera) {
			log.debug("entro a borrar el valor de la ciega anterior y actualizarlo por la nueva");
			try {
				bufferValorCiegaAnterior.remove(mesaActual);				
			} catch (Exception e) {
				// falla el remove pero continua
			}
			bufferValorCiegaAnterior.put(mesaActual, valorActualCiega);			
		}		

		for (Entry<String, Integer> tmp : bufferValorCiegaAnterior.entrySet()) {
			log.debug("stacks en el buffer de historial de ciegas, mesa: {} y valor ciega: {}", tmp.getKey(),
					tmp.getValue().toString());
		}

		Map<Integer, Double> tmpStacks = handInfoDto.getTmpStacks();
		for (Entry<Integer, Double> tmp : tmpStacks.entrySet()) {
			log.debug("stack actual leido en la mesa CON cambio de ciegas, posicion stack: {} y valor stack: {}",
					tmp.getKey(), tmp.getValue());
		}
		
		
		// seteo la hand
		String cartas = String.join("", handInfoDto.getCartas());
		handInfoDto.setHand(cartas);

		// seteo stacksBB
		handInfoDto.setStacksBb(handInfoDto.obtenerStack());

		handInfoDto.setNumjug(handInfoDto.getNumjug());

		// leer silla Hero en la mesa
		String configSillaHero = mesaConfig.getSillahero();
		Integer infoSillaHero = Integer.valueOf(configSillaHero);
		handInfoDto.setSillaHero(infoSillaHero);
		log.debug("Leyendo Silla de Hero: " + configSillaHero);

		handInfoDto.setUsuario(mesaConfig.getUsuario());
		log.debug("Leyendo usuario: " + handInfoDto.getUsuario());

		handInfoDto.setEstrategia(mesaConfig.getEstrategia());
		log.debug("Leyendo estrategia: " + handInfoDto.getEstrategia());

	}

	/**
	 * Analiza los datos por histograma con libreria Catalano
	 * 
	 * @param screenImg
	 * @param mesaActual
	 * @param contador 
	 * @return
	 * @throws Exception
	 */
	protected HandInfoDto procesarZonasPorHistograma(BufferedImage screenImg, String mesaActual, List<Integer> contador) throws Exception {
		HandInfoDto handInfoDto = new HandInfoDto();
		long inicio = System.currentTimeMillis();

		// donde se listan todas las zonas a leer de la pantalla
		Zona[] listaZonas = listarTodasZonas();

		Map<String, String> mapaLectura = new HashMap<>();
		Arrays.stream(listaZonas).parallel().forEach(zona -> {
			// se extrae subimagen recortada por las coordenadas de la zona
			BufferedImage subImg = extraerSubImagen(screenImg, zona);

			// seleccionamos el mapa de imagenes que le vamos a pasar al lector de pixeles
			Map<String, FastBitmap> mapa = null;
			if (zona.getNombre().contains("stacks")) {
				mapa = buffimgStacks;
			}
			if (zona.getNombre().contains("cartas")) {
				mapa = buffImgCartas;
			}
			if (zona.getNombre().contains("posi")) {
				mapa = buffImgPosicion;
			}
			if (zona.getNombre().contains("palos")) {
				mapa = buffImgPalos;
			}
			if (zona.getNombre().contains("ciegas")) {
				mapa = buffImgCiegas;
			}
			log.debug("Obteniendo informacion de : {} ", zona.getNombre());
			try {
				// seleccionamos el buffer que mas se parezca a la imagen
				long ini = System.currentTimeMillis();
				String lectura = leerInfoPorHistograma(subImg, mapa, zona);
				log.debug("... informacion obtenida para {} es : {} ", zona.getNombre(), lectura);
				mapaLectura.put(zona.getNombre(), lectura);
				long fin = System.currentTimeMillis();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		});

		normalizarDatosHistograma(mapaLectura, handInfoDto, mesaActual, contador);

		long finale = System.currentTimeMillis();
		log.debug(">>> tiempo  lectura total de la imagen: " + (finale - inicio));
		return handInfoDto;

	}

	/**
	 * usa la libreria de catalano para extraer info de pantalla
	 * 
	 * @param screenImg
	 * @param mapHistogram
	 * @param zona
	 * @return
	 * @throws Exception
	 */
	private String leerInfoPorHistograma(BufferedImage screenImg, Map<String, FastBitmap> mapHistogram, Zona zona)
			throws Exception {

		if (mesaConfig.getCapturazonas().trim().equals("true")) {
			UtilView.guardarImagen(screenImg, home + mesaConfig.getRutacaptura() + "\\screen_" + zona.getNombre() + "_"
					+ System.currentTimeMillis() + ".png");
		}

		FastBitmap original = new FastBitmap(screenImg);
		Grayscale org = new Grayscale();
		org.applyInPlace(original);

		Map<String, Double> disper = new HashMap<>();
		mapHistogram.entrySet().parallelStream().forEach(map -> {
			FastBitmap reconstructed = map.getValue();

			ObjectiveFidelity o = new ObjectiveFidelity(original, reconstructed);

			String catalanoError = mesaConfig.getCatalanoError();
			double error = Double.MAX_VALUE;
			if (catalanoError.trim().equals("mse")) {
				error = o.getMSE();
			}
			if (catalanoError.trim().equals("snr")) {
				error = o.getSNR();
			}
			if (catalanoError.trim().equals("psnr")) {
				error = o.getPSNR();
			}
			if (catalanoError.trim().equals("mae")) {
				error = o.getMAE();
			}
			if (catalanoError.trim().equals("dsnr")) {
				error = o.getDSNR();
			}
			if (catalanoError.trim().equals("uqi")) {
				error = o.getUniversalQualityIndex();
			}
			if (catalanoError.trim().equals("toterror")) {
				error = o.getTotalError();
			}

			disper.put(map.getKey(), error);
		});

		// seleccionamos el mapa con menor dispersion
		Stack<Double> bestval = new Stack<>();
		bestval.push(Double.MAX_VALUE);
		Stack<String> bestmap = new Stack<>();
		bestmap.push("");

		disper.forEach((key, value) -> {
			if (value != null && value < bestval.get(0)) {
				bestval.pop();
				bestval.push(value);
				bestmap.pop();
				bestmap.push(key);
			}
		});
		String mejorCompare = bestmap.pop();

		return mejorCompare;
	}

	/**
	 * guarda info como la image
	 * 
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

		// tiempo de retraso
		try {
			int sleep = Integer.valueOf(mesaConfig.getWaitAnalisis().trim());
			Thread.sleep(sleep);
		} catch (Exception e) {
			// TODO: handle exception
		}

		// Si el mouse no esta dentro de ninguna mesa
		if (mesaActual == null) {
			log.debug("Mouse no se encuentra dentro de ninguna zona");
			return handInfoDto;
		}
		log.debug("Obteniendo coordenadas de zona bajo el mouse, nombre zona: {}", mesaActual.getNombre());
		log.debug("Obteniendo coordenadas mesa actual (x,y): " + mesaActual.getX() + ", " + mesaActual.getY());

		// capturo screen
		Rectangle zona = new Rectangle(mesaActual.getX(), mesaActual.getY(), mesaActual.getAncho(),
				mesaActual.getAlto());
		BufferedImage screenImg = capturador.capturarScreenZona(zona);
		log.debug("Capturando Imagen (ancho,alto): " + screenImg.getWidth() + ", " + screenImg.getHeight());

		// creamos una lista para obtener iterar la lectura de imagenes y obtener la mas
		// probable
		int numItera = Integer.valueOf(mesaConfig.getNumIteraCaptura().trim());

		Map<Integer, BufferedImage> screenList = new HashMap<>();
		for (int i = 0; i < numItera; i++) {
			screenList.put(i, screenImg);
		}

		// procesamos zonas
		List<HandInfoDto> hdto = new ArrayList<>();
		List<String> errores = new ArrayList<>();
		List<Integer> contador= new ArrayList<>();
		contador.add(1);
		screenList.entrySet().parallelStream().forEach(entry -> {
			BufferedImage screen = entry.getValue();
			Integer imagenIdx = entry.getKey();
			log.debug("!!!! Procesando imagen para obtener informacion de datos de la mesa: {} , numeroIteracion: {}",
					mesaActual.getNombre(), imagenIdx);
			try {
				// seleccionamos tipo de procesamiento configurado y procesamos la imagen
				String tipoOcr = mesaConfig.getTipoOCR();
				if (tipoOcr.equals("histogram")) {
					hdto.add(procesarZonasPorHistograma(screen, mesaActual.getNombre(), contador));
				}
			} catch (Exception e) {
				log.error(e.getMessage());
				errores.add(e.getMessage());
			}
			contador.add(1);
		});
		
		contador.clear();

		if (errores.size() > 0) {
			log.debug("Error procesando OCR: {}", errores.get(0));
			throw new Exception(errores.get(0));
		}

		// obteniendo el resultado mas comun entre todas las iteraciones
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

		// almaceno la imagen de la ultima mesa analizada de cada cuadrante para poder
		// guardarla si el usuario desea por la interfaz
		almacenarColaImagenes(screenImg, mesaActual);

		return resComp;
	}

	private void almacenarColaImagenes(BufferedImage screenImg, Zona mesaActual) {

		if (buffColaImagenes.containsKey(mesaActual.getNombre())) {
			buffColaImagenes.remove(mesaActual.getNombre());
		}
		buffColaImagenes.put(mesaActual.getNombre(), screenImg);

	}

	public void almacenarImagenCuadrante(String cuadrante) {
		String ruta = home + mesaConfig.getRutaBugs();
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

	public Zona[] listarTodasZonas() {
		// agregamos todas las zonas a una lista a iterar en paralelo
		List<Zona> listImages = new ArrayList<>();

		List<Zona> configCartas = mesaConfig.getCartas();
		int j = 0;
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

		List<Zona> configPalos = mesaConfig.getPalos();
		int k = 0;
		for (Zona zona5 : configPalos) {
			zona5.setNombre("palos" + k);
			k++;
		}
		listImages.addAll(configPalos);

		List<Zona> configPosicionHero = mesaConfig.getPosicion();
		k = 0;
		for (Zona zona6 : configPosicionHero) {
			zona6.setNombre("posi" + k);
			k++;
		}
		listImages.addAll(configPosicionHero);

		Zona configCiegas = mesaConfig.getCiegas();
		configCiegas.setNombre("ciegas");
		listImages.addAll(Arrays.asList(configCiegas));

		Zona[] zonaArray = new Zona[listImages.size()];
		zonaArray = listImages.toArray(zonaArray);

		return zonaArray;
	}

	public BufferedImage extraerSubImagen(BufferedImage img, Zona zona) {
		Rectangle rec = new Rectangle((int) zona.getX(), (int) zona.getY(), zona.getAncho(), (int) zona.getAlto());
		BufferedImage imgRec = UtilView.recortarImagen(img, rec);
		return imgRec;
	}

	public void hudSegunAcciones(HandInfoDto handInfoDto, AccionInfoDto accion) {

	}

}
