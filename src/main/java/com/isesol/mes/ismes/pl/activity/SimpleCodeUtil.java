package com.isesol.mes.ismes.pl.activity;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import org.jbarcode.JBarcode;
import org.jbarcode.encode.Code128Encoder;
import org.jbarcode.paint.BaseLineTextPainter;
import org.jbarcode.paint.WidthCodedPainter;
import com.isesol.ismes.platform.module.bean.File;

public class SimpleCodeUtil {
	
	
	/**生成一维条形码
	 * @param contents
	 * @param fname
	 * @param dest
	 * @return
	 * @throws Exception
	 */
	public static File createBarcode(String contents) throws Exception { 
		JBarcode localJBarcode = new JBarcode(Code128Encoder.getInstance(),WidthCodedPainter.getInstance(),BaseLineTextPainter.getInstance());
		BufferedImage localBufferedImage = localJBarcode.createBarcode(contents); 
	    InputStream is = null;         
	    localBufferedImage.flush();                
	    ByteArrayOutputStream bs = new ByteArrayOutputStream();     
	    ImageOutputStream imOut;       
	    imOut = ImageIO.createImageOutputStream(bs);   
    	ImageIO.write(localBufferedImage, "png",imOut);       
    	is= new ByteArrayInputStream(bs.toByteArray());           
    	return  new File(contents, null,is , "png",  0);
	} 
	  
}
