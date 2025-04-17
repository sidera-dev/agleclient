/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE agle.lib

                            FILE CMATERIALINFO
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

// This holds the information for a material.  It may be a texture map of a color.
// Some of these are not used, but I left them because you will want to eventually
// read in the UV tile ratio and the UV tile offset for some models.
package agle.lib.loaders;

import java.io.*;
import java.lang.*;
import java.util.*;

public class CMaterialInfo implements Serializable
{
	public String  strName;				// The texture name
	public String  strFile;				// The texture file name (If this is set it's a texture map)
	public byte[]  color = new byte[3]; // The color of the object (R, G, B)
	public int   texureId;				// the texture ID
	public float uTile;					// u tiling of texture  (Currently not used)
	public float vTile;					// v tiling of texture	(Currently not used)
	public float uOffset;				// u offset of texture	(Currently not used)
	public float vOffset;				// v offset of texture	(Currently not used)
} ;
