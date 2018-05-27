/**
 * 
 */
package com.abp.pkr.pkrVista.utl;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * 
 * @author abpubuntu
 *
 */
public class UtilView {

	private static final Logger log = (Logger) LoggerFactory.getLogger(UtilView.class);

	/**
	 * Retorna lista de archivos en un directorio
	 * 
	 * @author abpubuntu
	 * @date May 9, 2017
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	public static List<File> obtenerFilesDir(File dir) throws IOException {
		List<File> files = new ArrayList<File>();
		File[] listOfFiles = dir.listFiles();
		if(!ArrayUtils.isEmpty(listOfFiles)){
			for (File file : listOfFiles) {
				if (file.isFile()) {
					files.add(file);
				}
			}
			//ordenamos por nombre de archivo
			Comparator<File> comNombre = new Comparator<File>(){
				@Override
				public int compare(File f1, File f2) {
					// TODO Auto-generated method stub
					return f1.getName().compareTo(f2.getName());
				}
			};
			Collections.sort(files, comNombre);			
		}
		return files;
	}

	/**
	 * Compara dos arreglos de enteros, si queremos exactamente igualdad
	 * marginError=0
	 * 
	 * @author abpubuntu
	 * @date May 9, 2017
	 * @param array1
	 * @param array2
	 * @param marginError
	 * @return
	 * @throws Exception
	 */
	public static boolean compareArraysInt(int[] array1, int[] array2, double marginError) throws Exception {
		boolean b = true;
		int count = 0;
		if (array1 != null && array2 != null) {
			if (array1.length != array2.length) {
				b = false;
			} else {
				for (int i = 0; i < array2.length; i++) {
					if (array2[i] != array1[i]) {
						count++;
					}
				}
				if (array2.length * marginError <= count) {
					b = false;
				}
				log.trace("Comparando: Pixeles diferentes= {}, Pixeles totales= {}, valido= {}", count, array2.length,
						b);
			}
		} else {
			b = false;
		}
		return b;
	}

	/**
	 * @author abpubuntu
	 * @date May 13, 2017
	 * @param img
	 * @param ruta
	 */
	public static void guardarImagen(BufferedImage img, String ruta) {
		try {
			ImageIO.write(img, "png", new File(ruta));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retorna una imgagen en blanco y negro y la corta segun un rectangulo
	 * 
	 * @author abpubuntu
	 * @date May 13, 2017
	 * @param img
	 * @param rec
	 * @return
	 */
	public static BufferedImage recortarImagen(BufferedImage img, Rectangle rec) {
		BufferedImage subImg = img;
		if (rec != null) {
			subImg = img.getSubimage((int) rec.getX(), (int) rec.getY(),
					(int) rec.getWidth(), (int) rec.getHeight());
		}
//		Conversiona a blanco y negro toma demasiado tiempo, no es recomendrble hacerlo
//		BufferedImage imgBW = new BufferedImage(subImg.getWidth(), subImg.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
//		Graphics2D graphics = imgBW.createGraphics();
//		graphics.drawImage(img, 0, 0, null);
		
		return subImg;
	}

}
