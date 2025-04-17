/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE agle.lib

                            FILE BMPTEXTURELOADER
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package agle.lib.loaders;

import gl4java.*;
import gl4java.utils.textures.*;
import java.net.*;
import java.io.*;

///////////////////////////////// BMPTEXTURELOADER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	This is the class constructor
/////
///////////////////////////////// BMPTEXTURELOADER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public class BmpTextureLoader extends IOTextureLoader
{
	public BmpTextureLoader(GLFunc gl, GLUFunc glu)
	{
		super(gl, glu);
	}
	
	public boolean readTexture(InputStream is)
	{
		// wrap a buffer to make reading more efficient (faster)
		BufferedInputStream in = new BufferedInputStream(is);

		glFormat = GL_RGB;  // 24bpp support only

		/* seek through the bmp header, up to the width/height:*/
		skip(in, 18);

		/* No 100% errorchecking anymore!!!*/

		/* read the width*/
		imageWidth = getInt(in);
		
		/* read the height */
		imageHeight = getInt(in);
		
		/* calculate the size (assuming 24 bits or 3 bytes per pixel).*/
		int size = imageWidth * imageHeight * 3;

		/* read the planes*/
		int planes = getShort(in);
		if (planes != 1) 
		{
			System.out.println("BMPTextureLoader Error: Planes is not 1!");
			return false;
		}

		/* read the bpp*/
		int bpp = getShort(in);
		if (bpp != 24)
		{
			System.out.println("BMPTextureLoader Error: bpp is not 24!");
			return false;
		}
		
		/* seek past the rest of the bitmap header.*/
		skip(in, 24);

		/* read the data. */
		pixel = new byte[size];

		try
		{
			in.read(pixel,0,size);
			in.close();
		}
		catch (IOException ioe)
		{
			System.out.println("BMPTextureLoader Error: Failed to read pixels from image file.");
			ioe.printStackTrace();
			return false;
		}

		byte temp;
		for (int i=0;i<size;i+=3) 
		{ /* reverse all of the colors. (bgr -> rgb)*/
			temp = pixel[i];
			pixel[i] = pixel[i+2];
			pixel[i+2] = temp;
		}
		
		setTextureSize();
		/* we're done.*/

		return true;
	}

	static int getInt(InputStream in)
	{
		int c=0, c1=0, c2=0, c3=0;

		try
		{
			/* get 4 bytes*/
			c  = (in.read() & 0xFF);  
			c1 = (in.read() & 0xFF) <<  8;  
			c2 = (in.read() & 0xFF) << 16;  
			c3 = (in.read() & 0xFF) << 24;  

		}
		catch (IOException ioe)
		{
			System.out.println("BMPTextureLoader Error: Failed to read from image file.");
			ioe.printStackTrace();
		}
		return (c | c1 | c2 | c3);
	}

	static int getShort(InputStream in)
	{
		int c=0, c1=0;
		try
		{
			/*get 2 bytes*/
			c  = (in.read() & 0xFF);  
			c1 = (in.read() & 0xFF) << 8;  

		}
		catch (IOException ioe)
		{
			System.out.println("BMPTextureLoader Error: Failed to read from image file.");
			ioe.printStackTrace();
		}
		return (c | c1);
	}
	
	static void skip(InputStream in, int ahead)
	{
		try
		{
			in.skip(ahead);
		}
		catch (IOException ioe)
		{
			System.out.println("BMPTextureLoader Error: Failed to skip "+ahead+" bytes from image file.");
			ioe.printStackTrace();
		}
	}
}
