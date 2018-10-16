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
import org.springframework.beans.factory.annotation.Autowired;

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

	// protected static Map<String, BufferedImage> buffCartas = null;
	protected static Map<String, byte[]> buffPalos = null;
	protected static Map<String, byte[]> buffStacks = null;
	protected static Map<String, byte[]> buffCartas = null;
	protected static Map<String, byte[]> buffPosicion = null;
	protected static Map<String, byte[]> buffNumJug = null;

	protected static Map<String, FastBitmap> buffImgPalos = null;
	protected static Map<String, FastBitmap> buffimgStacks = null;
	protected static Map<String, FastBitmap> buffImgCartas = null;
	protected static Map<String, FastBitmap> buffImgPosicion = null;
	protected static Map<String, FastBitmap> buffImgNumJug = null;

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

		// inicializo buffer de imagenes: numero jugadores en la mesa
		buffImgNumJug = new HashMap<>();
		buffNumJug = new HashMap<>();
		String rutaNumjug = mesaConfig.getRutaNumjug();
		capturador.filesToFastBitMap(buffImgNumJug, rutaNumjug);
		capturador.filesToBuffer(buffNumJug, rutaNumjug);

		// inicilizo buffer de cola de imagenes para guardar errores
		buffColaImagenes = new HashMap<>();
	}

	/**
	 * almacena en memoria los ultimos 16 hands analizadas para no volverlas a
	 * analizar nuevamente
	 * 
	 * @param bufferManos
	 * @param handInfoDto
	 * @return
	 */
	private boolean almacenarLectura(List<HandInfoDto> bufferManos, HandInfoDto handInfoDto) {

		for (HandInfoDto h : bufferManos) {
			try {
				boolean b = h.equals(handInfoDto);
				if (b) {
					return false;
				}
			} catch (Exception e) {
				return true;
			}
		}

		if (bufferManos.size() > 16) {
			bufferManos.remove(0);
		}
		bufferManos.add(handInfoDto);

		return true;

	}

	/**
	 * Adapta los datos capturados por histograma y los corrige para que queden bien
	 * para ser retornados
	 * 
	 * @param mapaLectura
	 * @param handInfoDto
	 * @throws Exception
	 */
	public void normalizarDatosHistograma(Map<String, String> mapaLectura, HandInfoDto handInfoDto) throws Exception {

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
				Double val = Double.valueOf(valor);
				handInfoDto.addStack(val, posic);
				log.debug("Leyendo info stack > " + key + " : " + valor);
			}

			// Leer info posicion
			if (key.contains("posicion")) {
				if (value == null) {
					throw new Exception("Sin datos para leer posicion");
				}
				handInfoDto.setPosHero(value.trim());
				log.debug("Leyendo info posicion > " + key + " : " + value);
			}

			// Leer info cartas
			if (key.contains("cartas")) {
				if (value == null) {
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

			// Leer info numjug
			if (key.contains("numJug")) {
				if (value == null) {
					throw new Exception("Sin datos para leer numero jugadores");
				}
				handInfoDto.setNumjug(Integer.valueOf(value.trim()));
				log.debug("Leyendo info numero jugadores > " + key + " : " + value);
			}
		}

		// seteo stacksBB
		handInfoDto.setStacksBb(handInfoDto.obtenerStack());

		// seteo la hand
		String cartas = String.join("", handInfoDto.getCartas());
		handInfoDto.setHand(cartas);

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

	}

	/**
	 * Analiza los datos por histograma con libreria Catalano
	 * 
	 * @param screenImg
	 * @return
	 * @throws Exception
	 */
	protected HandInfoDto procesarZonasPorHistograma(BufferedImage screenImg) throws Exception {
		HandInfoDto handInfoDto = new HandInfoDto();
		long inicio = System.currentTimeMillis();
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
			if (zona.getNombre().contains("numJug")) {
				mapa = buffImgNumJug;
			}
			if (zona.getNombre().contains("posi")) {
				mapa = buffImgPosicion;
			}
			if (zona.getNombre().contains("palos")) {
				mapa = buffImgPalos;
			}

			try {
				// seleccionamos el buffer que mas se parezca a la imagen
				long ini = System.currentTimeMillis();
				String lectura = leerInfoPorHistograma(subImg, mapa, zona);
				mapaLectura.put(zona.getNombre(), lectura);
				long fin = System.currentTimeMillis();
				log.debug("tiempo lectura de zona  " + zona.getNombre() + " : " + (fin - ini) + " , valor de lectura: "
						+ lectura);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		});

		normalizarDatosHistograma(mapaLectura, handInfoDto);

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

		// creamos una lista para obtener iterar la lectura de imagenes y obtener la mas
		// probable
		int numItera = Integer.valueOf(mesaConfig.getNumIteraCaptura().trim());

		List<BufferedImage> screenList = new ArrayList<>();
		for (int i = 0; i < numItera; i++) {
			screenList.add(screenImg);
		}

		// procesamos zonas
		List<HandInfoDto> hdto = new ArrayList<>();
		screenList.parallelStream().forEach(screen -> {
			try {
				log.debug("Procesando imagen para obtener informacion de mesa: " + mesaActual.getNombre());
				// seleccionamos tipo de procesamiento configurado y procesamos la imagen
				String tipoOcr = mesaConfig.getTipoOCR();
				if (tipoOcr.equals("histogram")) {
					hdto.add(procesarZonasPorHistograma(screen));				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		});

		// encontrar el resultado mas probable de todas las lecturas
//		Map<String, Integer> cuenta = new HashMap<>();
//		
//		for (int i = 1; i < hdto.size(); i++) {
//			if (hdto.get(i).equals(hdto.get(i-1))) {
//				cuenta.put(hdto.get(i).concat(), )
//			}
//			
//		}
		
		
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

		// almacenar mano leida para no procesarla dos veces
		boolean procesarLectura = almacenarLectura(bufferManosAnalizadas, resComp);
		if (!procesarLectura) {
			TimeUnit.MILLISECONDS.sleep(Integer.valueOf(mesaConfig.getWaitAnalisis().trim()));
			throw new Exception("Mano actual ya ha sido analizada: " + resComp.getHand());
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

		Zona numJug = mesaConfig.getNumjug();
		numJug.setNombre("numJug");
		listImages.add(numJug);

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

		Zona configPosicionHero = mesaConfig.getPosicion();
		configPosicionHero.setNombre("posicion");
		listImages.add(configPosicionHero);

		Zona[] zonaArray = new Zona[listImages.size()];
		zonaArray = listImages.toArray(zonaArray);

		return zonaArray;
	}

	public BufferedImage extraerSubImagen(BufferedImage img, Zona zona) {
		Rectangle rec = new Rectangle((int) zona.getX(), (int) zona.getY(), zona.getAncho(), (int) zona.getAlto());
		BufferedImage imgRec = UtilView.recortarImagen(img, rec);
		return imgRec;
	}

}
