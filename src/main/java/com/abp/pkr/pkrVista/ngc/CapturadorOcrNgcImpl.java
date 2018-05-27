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
		// inicializo buffer de imagenes cartas > palos
		buffPalos = new HashMap<>();
		String rutaPalos = mesaConfig.getRutapalos();
		capturador.filesToBuffer(buffPalos, rutaPalos);

	}

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

	/**
	 * @author abpubuntu
	 * @date Jun 4, 2017
	 * @param img
	 * @return
	 * @throws Exception
	 */
	protected HandInfoDto procesarZonas(BufferedImage screenImg) throws Exception {

		HandInfoDto handInfoDto = new HandInfoDto();
		String langTesse = mesaConfig.getTessLeng().toUpperCase();
		int factorResize = Integer.valueOf(mesaConfig.getFactorResize());
		Object[] objArr = null;
		Object object = null;

		// Leer info de tourney num
		long ini = System.currentTimeMillis();
		Zona configTorNum = mesaConfig.getTourneynum();
		List<Zona> configTourneyNum = new ArrayList<>();
		configTourneyNum.add(configTorNum);
		object = leerInfo(screenImg, configTourneyNum, TIPO_DATO.STR, null, factorResize)[0];
		String infoTourneyNum = object.toString();
		infoTourneyNum = infoTourneyNum.replace("o", "0");
		handInfoDto.setNumTourney(infoTourneyNum);

		long fin = System.currentTimeMillis();
		System.out.println("Leer numero Torneo: " + (fin - ini));

		try {
			Integer.valueOf(infoTourneyNum);
		} catch (Exception e) {
			throw e;
		}

		// Leer Cartas Hero
		ini = System.currentTimeMillis();
		Zona configCartas = mesaConfig.getCartas();
		List<Zona> configCartasHero = new ArrayList<>();
		configCartasHero.add(configCartas);
		objArr = leerInfo(screenImg, configCartasHero, TIPO_DATO.STR, "CARTAS", 2);
		String infoCartas = "";
		if (ArrayUtils.isNotEmpty(objArr)) {
			object = objArr[0];
			infoCartas = object.toString();
			infoCartas = infoCartas.replace("10", "T");
		}

		fin = System.currentTimeMillis();
		System.out.println("Leer cartas Hero: " + (fin - ini));	

		//*** obtener palos por analisis de colores de las cartas
		ini = System.currentTimeMillis();
		List<Zona> configPalos = mesaConfig.getPalos();
		objArr = capturador.leerInfo(screenImg, configPalos, buffPalos, "", TIPO_DATO.STR);
		String[] infoPalos = Arrays.copyOf(objArr, objArr.length, String[].class);

		fin = System.currentTimeMillis();
		System.out.println("Leer palos Hero: " + (fin - ini));

		String cartas = "";
		if (StringUtils.isNotBlank(infoCartas) && infoPalos.length > 1) {
			String first = infoCartas.substring(0, 1);
			String second = infoCartas.substring(1, 2);
			cartas = first + infoPalos[0] + second + infoPalos[1];
		}
		handInfoDto.setHand(cartas);
		
		if(cartas.length()!= 4){
			throw new Exception("No se pudo leer las cartas");
		}

		// Leer info stacks
		ini = System.currentTimeMillis();
		List<Zona> configStacks = mesaConfig.getStack();
		objArr = leerInfo(screenImg, configStacks, TIPO_DATO.DEC, langTesse, factorResize);
		Double[] infoStacks = Arrays.copyOf(objArr, objArr.length, Double[].class);
		handInfoDto.setStacksBb(infoStacks);

		fin = System.currentTimeMillis();
		System.out.println("Leer Stacks: " + (fin - ini));
		
		if(ArrayUtils.isEmpty(infoStacks)){
			throw new Exception("Stacks vacios o null");
		}

		// Leer info History
		ini = System.currentTimeMillis();
		List<Zona> configHistory = mesaConfig.getHistory();
		objArr = leerInfo(screenImg, configHistory, TIPO_DATO.STR, langTesse, factorResize);
		String[] infoHistory = Arrays.copyOf(objArr, objArr.length, String[].class);
		handInfoDto.setHistory(infoHistory);

		fin = System.currentTimeMillis();
		System.out.println("Leer History: " + (fin - ini));

		// Leer Posicion Hero
		ini = System.currentTimeMillis();
		Zona configPosicionHero = mesaConfig.getPosicion();
		List<Zona> configPos = new ArrayList<>();
		configPos.add(configPosicionHero);
		objArr = leerInfo(screenImg, configPos, TIPO_DATO.STR, langTesse, factorResize);
		if (ArrayUtils.isNotEmpty(objArr)) {
			object = objArr[0];
		}
		String infoPHero = object.toString();
		Integer infoPosiHero = -1;
		switch (infoPHero) {
		case "BB":
			infoPosiHero = 0;
			break;
		case "SB":
		case "5B":
			infoPosiHero = 1;
			break;
		case "BU":
		case "B0":
			infoPosiHero = 2;
			break;
		case "CO":
			infoPosiHero = 3;
			break;
		case "MP2":
			infoPosiHero = 4;
			break;
		case "MP1":
			infoPosiHero = 5;
			break;
		case "MP":
			infoPosiHero = 6;
			break;
		case "UTG3":
			infoPosiHero = 7;
			break;
		case "UTG2":
			infoPosiHero = 8;
			break;
		case "UTG1":
			infoPosiHero = 9;
			break;
		case "UTG":
			infoPosiHero = 10;
			break;
		default:
			break;
		}
		handInfoDto.setPosHero(infoPosiHero);

		fin = System.currentTimeMillis();
		System.out.println("Leer Posicion Hero: " + (fin - ini));
		
		if(infoPosiHero == -1){
			throw new Exception("No se pudo leer posicion de Hero");
		}

		// leer tipo de mesa
		ini = System.currentTimeMillis();
		Zona configTiMesa = mesaConfig.getTipomesa();
		List<Zona> configTipoMesa = new ArrayList<>();
		configTipoMesa.add(configTiMesa); // tipo de dato para metodo leer
											// info
		object = mesaConfig.getTipojuego();
		// object = leerInfo(screenImg, configTipoMesa, TIPO_DATO.STR,
		// null)[0];
		String infoTipoMesa = object.toString();
		handInfoDto.setTipoMesa(infoTipoMesa);

		fin = System.currentTimeMillis();
		System.out.println("Leer Tipo Mesa: " + (fin - ini));

		// leer silla Hero en la mesa
		String configSillaHero = mesaConfig.getSillahero();
		Integer infoSillaHero = Integer.valueOf(configSillaHero);
		handInfoDto.setSillaHero(infoSillaHero);

		// leer tipo de Jugador (por el momento el dafult sera READLESS
		// hasta
		// que se implemente lectura de notas)
		String infoJugadorDefault = mesaConfig.getJugadordefault();
		int numPlayers = handInfoDto.getStacksBb().length;
		String[] tipoJugador = new String[numPlayers];
		for (int i = 0; i < numPlayers; i++) {
			tipoJugador[i] = infoJugadorDefault;
		}
		handInfoDto.setTipoJugador(tipoJugador);

		// Leer posicion del button en el array de stacks
		// ArrayList<String>.add("digits")
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

		// Leer count de las manos jugadas en ese torneo
		ini = System.currentTimeMillis();
		Zona configNumhand = mesaConfig.getNumhand();
		List<Zona> configNumHand = new ArrayList<>();
		configNumHand.add(configNumhand);
		objArr = leerInfo(screenImg, configNumHand, TIPO_DATO.INT, langTesse, factorResize);
		if (ArrayUtils.isNotEmpty(objArr)) {
			object = objArr[0];
			String infoNumHand = object.toString();
			handInfoDto.setNumHand(infoNumHand.trim());
		}

		fin = System.currentTimeMillis();
		System.out.println("Leer num Hand: " + (fin - ini));

		return handInfoDto;
	}

	/**
	 * @author abpubuntu
	 * @date Jun 4, 2017
	 * @param screenImg
	 * @param configHistory
	 * @param str
	 * @return
	 */
	private Object[] leerInfo(BufferedImage screenImg, List<Zona> listaConfig, TIPO_DATO tipoDato, String lengTesseract,
			int factorResize) {
		List<Object> infoList = new ArrayList<>();
		// itero cada posicion de coordenadas
		for (Zona zona : listaConfig) {
			BufferedImage recortada = screenImg.getSubimage(zona.getX(), zona.getY(), zona.getAncho(), zona.getAlto());
			// UtilView.guardarImagen(recortada,
			// home+"/Documents/img/capturas/recorte");
			Tesseract instance = new Tesseract();
			instance.setDatapath(mesaConfig.getTessdata());
			instance.setPageSegMode(7);

			// instanciamos el trainer>"pkr" o el default que es "eng" o si es
			// para reconocer solo num el "NUM"
			if (lengTesseract != null && lengTesseract.equals("PKR")) {
				instance.setLanguage(mesaConfig.getTessLeng());
			} else if (lengTesseract != null && lengTesseract.equals("NUM")) {
				// instance.setTessVariable("tessedit_char_whitelist",
				// "0123456789");
				List<String> configs = new ArrayList<>();
				configs.add("digits");
				instance.setConfigs(configs);
			} else if (lengTesseract != null && lengTesseract.equals("NUMSTR")) {
				instance.setTessVariable("tessedit_char_whitelist", "0123456789abcdefghijklmnopqrstuvwxyz-");
			} else if (lengTesseract != null && lengTesseract.equals("CARTAS")) {
				instance.setTessVariable("tessedit_char_whitelist", "AKQJ1098765432");
			}

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

}
