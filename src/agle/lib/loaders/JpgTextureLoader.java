/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE agle.lib

                            FILE JPGTEXTURELOADER
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package agle.lib.loaders;

import gl4java.*;
import gl4java.utils.textures.*;
import java.awt.*;      
import java.awt.image.*;
import java.net.*;      
import javax.swing.*; 

///////////////////////////////// JPGTEXTURELOADER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	This is the class constructor
/////
///////////////////////////////// JPGTEXTURELOADER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public class JpgTextureLoader extends AWTTextureLoader
{
	public JpgTextureLoader(GLFunc gl, GLUFunc glu)
	{
		super(null, gl, glu);
	}
	
	public boolean readTexture(String file)
	{
		try {
			ImageIcon icon = new ImageIcon(file);
			Image img = icon.getImage();
			return readTexture(img);
		} catch (Exception ex) {
			System.out.println("AWTTextureLoader.readTexture <"+file+"> failed !\n"+ex);
		}
	    return false;
	}

	public boolean readTexture(Image img)
	{
		try {
			BufferedImage image = new BufferedImage(img.getWidth(null),
													img.getHeight(null),
													BufferedImage.TYPE_INT_RGB);

			Graphics g = image.createGraphics();
			g.drawImage(img,0,0,null);

			imageWidth = image.getWidth();
			imageHeight = image.getHeight();

			// Read entire image (doesn't throw exceptions)
			int[] iPixels = new int[imageWidth * imageHeight];

			PixelGrabber pp=new PixelGrabber(img,0,0,imageWidth, imageHeight,iPixels,0,imageWidth);
			try 
			{
				pp.grabPixels();
			} 
			catch (InterruptedException e) 
			{
				System.err.println("interrupted waiting for pixel!");
				error=true;
				return false;
			}
			if ((pp.getStatus() & ImageObserver.ABORT) != 0) 
			{
				System.err.println("image fetch aborted or errored");
				error=true;
				return false;
			}

			int imagetype = image.getType();
			switch(imagetype)
			{
				case BufferedImage.TYPE_INT_RGB:
					glFormat=GL_RGB;
					break;
				case BufferedImage.TYPE_INT_ARGB:
				case BufferedImage.TYPE_INT_ARGB_PRE:
					glFormat=GL_RGBA;
					break;
				default:
					error=true;
					System.err.println("unsupported format: "+imagetype);
					return false;
			}

			setTextureSize();
			pixel=new byte[imageWidth * imageHeight * getComponents()];
            
			int offset=0;
			int aPixel;
			int y_desc;
			for(y_desc=imageHeight-1; y_desc>=0; y_desc--)
			{
				for(int x=0;x<imageWidth;x++)
				{
					aPixel = iPixels[y_desc*imageWidth + x];
					// red
					pixel[offset++]=
					new Integer( (aPixel  >> 16) & 0xff ).byteValue();
			
					// green 
					pixel[offset++]=
					new Integer( (aPixel  >>  8) & 0xff ).byteValue();
			
					// blue 
					pixel[offset++]=
					new Integer( (aPixel       ) & 0xff ).byteValue();
			
					// alpha
					if(glFormat==GL_RGBA)
						pixel[offset++]=
						new Integer( (aPixel  >> 24) & 0xff ).byteValue();
				}
			}
			return true;
			} catch (Exception e) {
			System.out.println("An exception occured, while loading an AWTTexture");
			System.out.println(e);
			error=true;
			}
		return false;
	}
}
