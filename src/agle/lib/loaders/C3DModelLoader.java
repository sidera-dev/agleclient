/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE agle.lib

                            FILE C3DMODELLOADER
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
package agle.lib.loaders;

import java.util.ArrayList;
import java.lang.*;
import java.util.*;
import java.io.*;

import agle.lib.math.*;

public class C3DModelLoader 
{
	//>------ Primary CChunk, at the beginning of each file
	static final int PRIMARY =			0x4D4D;

	//>------ Main CChunks
	static final int  OBJECTINFO =		0x3D3D;		// This gives the version of the mesh and is found right before the material and object information
	static final int  VERSION =			0x0002;		// This gives the version of the .3ds file
	static final int  EDITKEYFRAME =	0xB000;		// This is the header for all of the key frame info

	//>------ sub defines of OBJECTINFO
	static final int  MATERIAL =		0xAFFF;		// This stored the texture info
	static final int  OBJECT =			0x4000;		// This stores the Faces, vertices, etc...

	//>------ sub defines of MATERIAL
	static final int  MATNAME =			0xA000;		// This holds the material name
	static final int  MATDIFFUSE =		0xA020;		// This holds the color of the object/material
	static final int  MATMAP =			0xA200;		// This is a header for a new material
	static final int  MATMAPFILE =		0xA300;		// This holds the file name of the texture

	static final int  OBJECT_MESH =		0x4100;		// This lets us know that we are reading a new object

	//>------ sub defines of OBJECT_MESH
	static final int  OBJECT_VERTICES =	0x4110;		// The objects vertices
	static final int  OBJECT_FACES =	0x4120;		// The objects faces
	static final int  OBJECT_MATERIAL =	0x4130;		// This is found if the object has a material, either texture map or color
	static final int  OBJECT_UV =		0x4140;		// The UV texture coordinates


	// Here is our structure for our 3DS indicies (since .3DS stores 4 unsigned shorts)
	public class CIndices
	{
		short a, b, c;
		short bVisible;						// This will hold point1, 2, and 3 index's into the vertex array plus a visible flag
	}

	// This holds the CChunk info
	public class CChunk
	{
		int ID;								// The CChunk's ID
		int length;							// The length of the CChunk
		int bytesRead;						// The amount of bytes read within that CChunk
	}

	// These are used through the loading process to hold the CChunk information
	CChunk m_CurrenCChunk;
	CChunk m_TempCChunk;
	C3DModel model;

	ByteLoader loader;						// Java Port replacement for fread() [kind of...]

	public class ByteLoader
	{
		InputStream is = null;
		int readLength = 0;

		ByteLoader(InputStream is)
		{
			this.is = is;
		}
		byte readByte()
		{
			readLength = 1;
			return ( (byte)nextByte() );
		}
		int readShort()
		{
			readLength = 2;
			int b1 = nextByte();
			int b2 = nextByte() << 8;
			return (b1 | b2);
		}
		int readInt()
		{
			readLength = 4;
			int b1 = nextByte();
			int b2 = nextByte() << 8;
			int b3 = nextByte() << 16;
			int b4 = nextByte() << 24;
			return (b1 | b2 | b3 | b4);
		}
		float readFloat()
		{
			return Float.intBitsToFloat(readInt());
		}
		String readString()
		{
			byte[] b = new byte[256];
			//Look for zero terminated string from byte array
			for (int i=0;i<256 ;i++ )
			{
				b[i] = readByte();
				readLength = i;

				if (b[i] == (byte)0)
				{
					return new String(b, 0, i);
				}
			}
			return new String(b);
		}
		void skip(int n)
		{
			int actual = 0;
			int tries = 0;
			try
			{
				while ( (actual != n) && (tries < 100) )
				{
					actual += is.skip(n - actual);
					tries++;
				}
				if (tries == 100)
				{
					System.err.println("IO Error, failed to skip ahead "+n+" bytes.");
				}
			}
			catch (IOException ioe)
			{
				System.err.println("IO Error, failed to skip ahead "+n+" bytes.");
				ioe.printStackTrace();
			}
		}
		int nextByte()
		{
			int result = 0;
			try
			{
				result = is.read() & 0xFF;
			}
			catch (IOException ioe)
			{
				System.err.println("IO Error, failed to read the next byte in the stream.");
				ioe.printStackTrace();
			}
			return result;
		}
		void close()
		{
			try
			{
				is.close();
			}
			catch (IOException ioe)
			{
				System.err.println("IO Error, failed to close the input file.");
				ioe.printStackTrace();
			}
		}
	}

	///////////////////////////////// CLOAD3DS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This constructor initializes the CChunk data
	/////
	///////////////////////////////// CLOAD3DS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public C3DModelLoader()
	{
		m_CurrenCChunk = new CChunk();				// Initialize and allocate our current CChunk
		m_TempCChunk = new CChunk();					// Initialize and allocate a temporary CChunk
	}

	///////////////////////////////// IMPORT 3DS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This is called by the client to open the .3ds file, read it, then clean up
	/////
	///////////////////////////////// IMPORT 3DS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public C3DModel Import3DS(String file)
	{
		File f;
		FileInputStream is;
		BufferedInputStream in;
		C3DModel pModel;
		try
		{
			f = new File(file);  
			is = new FileInputStream(f);    
		}
		catch (Exception e)
		{
			System.out.println("Failed to connect to ");
			e.printStackTrace();
			return null;
		}
		// wrap a buffer to make reading more efficient (faster)
		in = new BufferedInputStream(is);

		loader = new ByteLoader(in);

		// Once we have the file open, we need to read the very first data CChunk
		// to see if it's a 3DS file.  That way we don't read an invalid file.
		// If it is a 3DS file, then the first CChunk ID will be equal to PRIMARY (some hex num)

		// Read the first chuck of the file to see if it's a 3DS file
		ReadCChunk(m_CurrenCChunk);

		// Make sure this is a 3DS file
		if (m_CurrenCChunk.ID != PRIMARY)
		{
			System.out.println("Unable to load PRIMARY chuck from file!");
			return null;
		}

		// Now we actually start reading in the data.  ProcessNexCChunk() is recursive
		pModel = new C3DModel();
		// Begin loading objects, by calling this recursive function
		ProcessNexCChunk(pModel, m_CurrenCChunk);

		// After we have read the whole 3DS file, we want to calculate our own vertex normals.
		ComputeNormals(pModel);

		// Clean up after everything
		CleanUp();

		return pModel;
	}

	///////////////////////////////// CLEAN UP \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function cleans up our allocated memory and closes the file
	/////
	///////////////////////////////// CLEAN UP \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void CleanUp()
	{
		loader.close();						// Close the current file pointer
	}

	///////////////////////////////// PROCESS NEXT CChunk\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function reads the main sections of the .3DS file, then dives deeper with recursion
	/////
	///////////////////////////////// PROCESS NEXT CChunk\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void ProcessNexCChunk(C3DModel pModel, CChunk pPreviousCChunk)
	{
		C3DObject newObject = null;						// This is used to add to our object list
		CMaterialInfo newTexture = null;				// This is used to add to our material list
		int version = 0;								// This will hold the file version

		m_CurrenCChunk = new CChunk();					// Allocate a new CChunk				

		// Below we check our CChunk ID each time we read a new CChunk.  Then, if
		// we want to extract the information from that CChunk, we do so.
		// If we don't want a CChunk, we just read past it.  

		// Continue to read the sub CChunks until we have reached the length.
		// After we read ANYTHING we add the bytes read to the CChunk and then check
		// check against the length.
		while (pPreviousCChunk.bytesRead < pPreviousCChunk.length)
		{
			// Read next CChunk
			ReadCChunk(m_CurrenCChunk);

			// Check the CChunk ID
			switch (m_CurrenCChunk.ID)
			{
			case VERSION:							// This holds the version of the file
				// This CChunk has an unsigned short that holds the file version.
				// Since there might be new additions to the 3DS file format in 4.0,
				// we give a warning to that problem.

				// Read the file version and add the bytes read to our bytesRead variable
				version = loader.readShort();
				m_CurrenCChunk.bytesRead += loader.readLength;
				
				loader.skip(m_CurrenCChunk.length - m_CurrenCChunk.bytesRead);
				m_CurrenCChunk.bytesRead += m_CurrenCChunk.length - m_CurrenCChunk.bytesRead;

				// If the file version is over 3, give a warning that there could be a problem
				if (version > 0x03)
					System.out.println("This 3DS file is over version 3 so it may load incorrectly!");
				break;

			case OBJECTINFO:							// This holds the version of the mesh
				// This CChunk holds the version of the mesh.  It is also the head of the MATERIAL
				// and OBJECT CChunks.  From here on we start reading in the material and object info.
				// Read the next CChunk
				ReadCChunk(m_TempCChunk);

				// Get the version of the mesh
				version = loader.readShort();
				m_TempCChunk.bytesRead += loader.readLength;

				loader.skip(m_TempCChunk.length - m_TempCChunk.bytesRead);
				m_TempCChunk.bytesRead += m_TempCChunk.length - m_TempCChunk.bytesRead;

				// Increase the bytesRead by the bytes read from the last CChunk
				m_CurrenCChunk.bytesRead += m_TempCChunk.bytesRead;

				// Go to the next CChunk, which is the object has a texture, it should be MATERIAL, then OBJECT.
				ProcessNexCChunk(pModel, m_CurrenCChunk);
				break;

			case MATERIAL:							// This holds the material information
				// This CChunk is the header for the material info CChunks

				// Increase the number of materials
				pModel.numOfMaterials++;
				newTexture = new CMaterialInfo();		// This is used to add to our material list

				// Add a empty texture structure to our texture list.
				// If you are unfamiliar with STL's "vector" class, all push_back()
				// does is add a new node onto the list.  I used the vector class
				// so I didn't need to write my own link list functions.  
				pModel.pMaterials.add(newTexture);

				// Proceed to the material loading function
				ProcessNextMaterialCChunk(pModel, m_CurrenCChunk);
				break;

			case OBJECT:							// This holds the name of the object being read
				// This CChunk is the header for the object info CChunks.  It also
				// holds the name of the object.
				newObject = new C3DObject();			// This is used to add to our object list

				// Increase the object count
				pModel.numOfObjects++;
			
				// Add a new tObject node to our list of objects (like a link list)
				pModel.pObject.add(newObject);
				
				// Get the name of the object and store it, then add the read bytes to our byte counter.
				newObject.strName = loader.readString();
				m_CurrenCChunk.bytesRead += loader.readLength + 1;
				
				// Now proceed to read in the rest of the object information
				ProcessNextObjecCChunk(pModel, newObject, m_CurrenCChunk);
				break;

			case EDITKEYFRAME:
				// Because I wanted to make this a SIMPLE tutorial as possible, I did not include
				// the key frame information.  This CChunk is the header for all the animation info.
				// In a later tutorial this will be the subject and explained thoroughly.

				//ProcessNextKeyFrameCChunk(pModel, m_CurrenCChunk);

				// Read past this CChunk and add the bytes read to the byte counter
				loader.skip(m_CurrenCChunk.length - m_CurrenCChunk.bytesRead);
				m_CurrenCChunk.bytesRead += m_CurrenCChunk.length - m_CurrenCChunk.bytesRead;
				break;

			default: 
				
				// If we didn't care about a CChunk, then we get here.  We still need
				// to read past the unknown or ignored CChunk and add the bytes read to the byte counter.
				loader.skip(m_CurrenCChunk.length - m_CurrenCChunk.bytesRead);
				m_CurrenCChunk.bytesRead += m_CurrenCChunk.length - m_CurrenCChunk.bytesRead;
				break;
			}

			// Add the bytes read from the last CChunk to the previous CChunk passed in.
			pPreviousCChunk.bytesRead += m_CurrenCChunk.bytesRead;
		}

		// Free the current CChunk and set it back to the previous CChunk (since it started that way)
		m_CurrenCChunk = pPreviousCChunk;
	}


	///////////////////////////////// PROCESS NEXT OBJECT CChunk \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function handles all the information about the objects in the file
	/////
	///////////////////////////////// PROCESS NEXT OBJECT CChunk \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void ProcessNextObjecCChunk(C3DModel pModel, C3DObject pObject, CChunk pPreviousCChunk)
	{
		// Allocate a new CChunk to work with
		m_CurrenCChunk = new CChunk();

		// Continue to read these CChunks until we read the end of this sub CChunk
		while (pPreviousCChunk.bytesRead < pPreviousCChunk.length)
		{
			// Read the next CChunk
			ReadCChunk(m_CurrenCChunk);

			// Check which CChunk we just read
			switch (m_CurrenCChunk.ID)
			{
			case OBJECT_MESH:					// This lets us know that we are reading a new object
			
				// We found a new object, so let's read in it's info using recursion
				ProcessNextObjecCChunk(pModel, pObject, m_CurrenCChunk);
				break;

			case OBJECT_VERTICES:				// This is the objects vertices
				ReadVertices(pObject, m_CurrenCChunk);
				break;

			case OBJECT_FACES:					// This is the objects face information
				ReadVertexCIndices(pObject, m_CurrenCChunk);
				break;

			case OBJECT_MATERIAL:				// This holds the material name that the object has
				
				// This CChunk holds the name of the material that the object has assigned to it.
				// This could either be just a color or a texture map.  This CChunk also holds
				// the faces that the texture is assigned to (In the case that there is multiple
				// textures assigned to one object, or it just has a texture on a part of the object.
				// Since most of my game objects just have the texture around the whole object, and 
				// they aren't multitextured, I just want the material name.

				// We now will read the name of the material assigned to this object
				ReadObjectMaterial(pModel, pObject, m_CurrenCChunk);			
				break;

			case OBJECT_UV:						// This holds the UV texture coordinates for the object

				// This CChunk holds all of the UV coordinates for our object.  Let's read them in.
				ReadUVCoordinates(pObject, m_CurrenCChunk);
				break;

			default:  

				// Read past the ignored or unknown CChunks
				loader.skip(m_CurrenCChunk.length - m_CurrenCChunk.bytesRead);
				m_CurrenCChunk.bytesRead += m_CurrenCChunk.length - m_CurrenCChunk.bytesRead;
				break;
			}

			// Add the bytes read from the last CChunk to the previous CChunk passed in.
			pPreviousCChunk.bytesRead += m_CurrenCChunk.bytesRead;
		}

		// Free the current CChunk and set it back to the previous CChunk (since it started that way)
		m_CurrenCChunk = pPreviousCChunk;
	}


	///////////////////////////////// PROCESS NEXT MATERIAL CChunk \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function handles all the information about the material (Texture)
	/////
	///////////////////////////////// PROCESS NEXT MATERIAL CChunk \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void ProcessNextMaterialCChunk(C3DModel pModel, CChunk pPreviousCChunk)
	{
		// Allocate a new CChunk to work with
		m_CurrenCChunk = new CChunk();

		// Continue to read these CChunks until we read the end of this sub CChunk
		while (pPreviousCChunk.bytesRead < pPreviousCChunk.length)
		{
			// Read the next CChunk
			ReadCChunk(m_CurrenCChunk);

			// Check which CChunk we just read in
			switch (m_CurrenCChunk.ID)
			{
			case MATNAME:							// This CChunk holds the name of the material
				
				// Here we read in the material name
				String matName = loader.readString();
				((CMaterialInfo)pModel.pMaterials.get(pModel.numOfMaterials - 1)).strName = matName;
				m_CurrenCChunk.bytesRead += loader.readLength + 1;
				break;

			case MATDIFFUSE:						// This holds the R G B color of our object
				ReadColorCChunk((CMaterialInfo)pModel.pMaterials.get(pModel.numOfMaterials - 1), m_CurrenCChunk);
				break;
			
			case MATMAP:							// This is the header for the texture info
				
				// Proceed to read in the material information
				ProcessNextMaterialCChunk(pModel, m_CurrenCChunk);
				break;

			case MATMAPFILE:						// This stores the file name of the material

				// Here we read in the material's file name
				String matFile = loader.readString();
				((CMaterialInfo)pModel.pMaterials.get(pModel.numOfMaterials - 1)).strFile = matFile;
				m_CurrenCChunk.bytesRead += loader.readLength + 1;
				break;
			
			default:  

				// Read past the ignored or unknown CChunks
				loader.skip(m_CurrenCChunk.length - m_CurrenCChunk.bytesRead);
				m_CurrenCChunk.bytesRead += m_CurrenCChunk.length - m_CurrenCChunk.bytesRead;
				break;
			}

			// Add the bytes read from the last CChunk to the previous CChunk passed in.
			pPreviousCChunk.bytesRead += m_CurrenCChunk.bytesRead;
		}

		// Free the current CChunk and set it back to the previous CChunk (since it started that way)
		m_CurrenCChunk = pPreviousCChunk;
	}

	///////////////////////////////// READ CChunk \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function reads in a CChunk ID and it's length in bytes
	/////
	///////////////////////////////// READ CChunk \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void ReadCChunk(CChunk pCChunk)
	{
		// This reads the CChunk ID which is 2 bytes.
		// The CChunk ID is like OBJECT or MATERIAL.  It tells what data is
		// able to be read in within the CChunks section. 
		pCChunk.ID = loader.readShort();
		pCChunk.bytesRead = loader.readLength;

		// Then, we read the length of the CChunk which is 4 bytes.
		// This is how we know how much to read in, or read past.
		pCChunk.length = loader.readInt();
		pCChunk.bytesRead += loader.readLength;
	}

	///////////////////////////////// READ COLOR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function reads in the RGB color data
	/////
	///////////////////////////////// READ COLOR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void ReadColorCChunk(CMaterialInfo pMaterial, CChunk pCChunk)
	{
		// Read the color CChunk info
		ReadCChunk(m_TempCChunk);

		// Read in the R G B color (3 bytes - 0 through 255)
		pMaterial.color[0] = loader.readByte();
		pMaterial.color[1] = loader.readByte();
		pMaterial.color[2] = loader.readByte();
		m_TempCChunk.bytesRead += 3 * loader.readLength;

		// Add the bytes read to our CChunk
		pCChunk.bytesRead += m_TempCChunk.bytesRead;
	}


	///////////////////////////////// READ VERTEX INDECES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function reads in the CIndices for the vertex array
	/////
	///////////////////////////////// READ VERTEX INDECES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void ReadVertexCIndices(C3DObject pObject, CChunk pPreviousCChunk)
	{
		int index = 0;					// This is used to read in the current face index

		// In order to read in the vertex CIndices for the object, we need to first
		// read in the number of them, then read them in.  Remember,
		// we only want 3 of the 4 values read in for each face.  The fourth is
		// a visibility flag for 3D Studio Max that doesn't mean anything to us.

		// Read in the number of faces that are in this object (int)
		pObject.numOfFaces = loader.readShort();
		pPreviousCChunk.bytesRead += loader.readLength;

		// Alloc enough memory for the faces and initialize the structure
		pObject.pFaces = new CFace [pObject.numOfFaces];

		// Go through all of the faces in this object
		for(int i = 0; i < pObject.numOfFaces; i++)
		{
			pObject.pFaces[i] = new CFace();
			// Next, we read in the A then B then C index for the face, but ignore the 4th value.
			// The fourth value is a visibility flag for 3D Studio Max, we don't care about this.
			for(int j = 0; j < 4; j++)
			{
				// Read the first vertice index for the current face 
				index = loader.readShort();
				pPreviousCChunk.bytesRead += loader.readLength;

				if(j < 3)
				{
					// Store the index in our face structure.
					pObject.pFaces[i].vertIndex[j] = index;
				}
			}
		}
	}


	///////////////////////////////// READ UV COORDINATES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function reads in the UV coordinates for the object
	/////
	///////////////////////////////// READ UV COORDINATES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void ReadUVCoordinates(C3DObject pObject, CChunk pPreviousCChunk)
	{
		// In order to read in the UV CIndices for the object, we need to first
		// read in the amount there are, then read them in.

		// Read in the number of UV coordinates there are (int)
		pObject.numTexVertex = loader.readShort();
		pPreviousCChunk.bytesRead += loader.readLength;

		// Allocate memory to hold the UV coordinates
		pObject.pTexVerts = new CVector2 [pObject.numTexVertex];

		// Read in the texture coodinates (an array 2 float)
		for (int i=0 ;i<pObject.numTexVertex ;i++ )
		{
			pObject.pTexVerts[i] = new CVector2
										(
											loader.readFloat(),
											loader.readFloat()
										);
		}
		pPreviousCChunk.bytesRead += pPreviousCChunk.length - pPreviousCChunk.bytesRead;
	}


	///////////////////////////////// READ VERTICES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function reads in the vertices for the object
	/////
	///////////////////////////////// READ VERTICES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void ReadVertices(C3DObject pObject, CChunk pPreviousCChunk)
	{
		// Like most CChunks, before we read in the actual vertices, we need
		// to find out how many there are to read in.  Once we have that number
		// we then fread() them into our vertice array.

		// Read in the number of vertices (int)
		pObject.numOfVerts = loader.readShort();
		pPreviousCChunk.bytesRead += loader.readLength;

		// Allocate the memory for the verts and initialize the structure
		pObject.pVerts = new CVector3 [pObject.numOfVerts];

		// Read in the array of vertices (an array of 3 floats)
		for (int i=0 ;i<pObject.numOfVerts ;i++ )
		{
			pObject.pVerts[i] = new CVector3
										(
											loader.readFloat(),
											loader.readFloat(),
											loader.readFloat()
										);
		}
		pPreviousCChunk.bytesRead += pPreviousCChunk.length - pPreviousCChunk.bytesRead;

		// Now we should have all of the vertices read in.  Because 3D Studio Max
		// Models with the Z-Axis pointing up (strange and ugly I know!), we need
		// to flip the y values with the z values in our vertices.  That way it
		// will be normal, with Y pointing up.  If you prefer to work with Z pointing
		// up, then just delete this next loop.  Also, because we swap the Y and Z
		// we need to negate the Z to make it come out correctly.

		// Go through all of the vertices that we just read and swap the Y and Z values
		for(int i = 0; i < pObject.numOfVerts; i++)
		{
			// Store off the Y value
			float fTempY = pObject.pVerts[i].y;

			// Set the Y value to the Z value
			pObject.pVerts[i].y = pObject.pVerts[i].z;

			// Set the Z value to the Y value, 
			// but negative Z because 3D Studio max does the opposite.
			pObject.pVerts[i].z = -fTempY;
		}
	}


	///////////////////////////////// READ OBJECT MATERIAL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function reads in the material name assigned to the object and sets the materialID
	/////
	///////////////////////////////// READ OBJECT MATERIAL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void ReadObjectMaterial(C3DModel pModel, C3DObject pObject, CChunk pPreviousCChunk)
	{
		// *What is a material?*  - A material is either the color or the texture map of the object.
		// It can also hold other information like the brightness, shine, etc... Stuff we don't
		// really care about.  We just want the color, or the texture map file name really.

		// Here we read the material name that is assigned to the current object.
		// strMaterial should now have a string of the material name, like "Material #2" etc..
		String strMaterial = loader.readString();
		pPreviousCChunk.bytesRead += loader.readLength + 1;

		// Now that we have a material name, we need to go through all of the materials
		// and check the name against each material.  When we find a material in our material
		// list that matches this name we just read in, then we assign the materialID
		// of the object to that material index.  You will notice that we passed in the
		// model to this function.  This is because we need the number of textures.
		// Yes though, we could have just passed in the model and not the object too.

		// Go through all of the textures
		for(int i = 0; i < pModel.numOfMaterials; i++)
		{
			// If the material we just read in matches the current texture name
			if(strMaterial.equals( ((CMaterialInfo)pModel.pMaterials.get(i)).strName) )
			{
				// Set the material ID to the current index 'i' and stop checking
				pObject.materialID = i;

				// Now that we found the material, check if it's a texture map.
				// If the strFile has a string length of 1 and over it's a texture
				if(((CMaterialInfo)pModel.pMaterials.get(i)).strFile != null)
				{
					// Set the object's flag to say it has a texture map to bind.
					pObject.bHasTexture = true;
				}	
				break;
			}
			else
			{
				// Set the ID to -1 to show there is no material for this object
				pObject.materialID = -1;
			}
		}

		// Read past the rest of the CChunk since we don't care about shared vertices
		// You will notice we subtract the bytes already read in this CChunk from the total length.
		loader.skip(pPreviousCChunk.length - pPreviousCChunk.bytesRead);
		pPreviousCChunk.bytesRead += pPreviousCChunk.length - pPreviousCChunk.bytesRead;
	}			

	///////////////////////////////// COMPUTER NORMALS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function computes the normals and vertex normals of the objects
	/////
	///////////////////////////////// COMPUTER NORMALS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void ComputeNormals(C3DModel pModel)
	{
		CVector3 vVector1 = new CVector3();
		CVector3 vVector2 = new CVector3();
		CVector3 vNormal = new CVector3();
		CVector3[] vPoly = new CVector3[3];

		// If there are no objects, we can skip this part
		if(pModel.numOfObjects <= 0)
			return;

		// What are vertex normals?  And how are they different from other normals?
		// Well, if you find the normal to a triangle, you are finding a "face Normal".
		// If you give OpenGL a face normal for lighting, it will make your object look
		// really flat and not very round.  If we find the normal for each vertex, it makes
		// the smooth lighting look.  This also covers up blocky looking objects and they appear
		// to have more polygons than they do.    Basically, what you do is first
		// calculate the face normals, then you take the average of all the normals around each
		// vertex.  It's just averaging.  That way you get a better approximation for that vertex.

		// Go through each of the objects to calculate their normals
		for(int index = 0; index < pModel.numOfObjects; index++)
		{
			// Get the current object
			C3DObject pObject = (C3DObject)pModel.pObject.get(index);

			// Here we allocate all the memory we need to calculate the normals
			CVector3[] pNormals		= new CVector3 [pObject.numOfFaces];
			CVector3[] pTempNormals	= new CVector3 [pObject.numOfFaces];
			pObject.pNormals		= new CVector3 [pObject.numOfVerts];

			// Go though all of the faces of this object
			for(int i=0; i < pObject.numOfFaces; i++)
			{						
				// To cut down LARGE code, we extract the 3 points of this face
				vPoly[0] = pObject.pVerts[pObject.pFaces[i].vertIndex[0]];
				vPoly[1] = pObject.pVerts[pObject.pFaces[i].vertIndex[1]];
				vPoly[2] = pObject.pVerts[pObject.pFaces[i].vertIndex[2]];

				// Now let's calculate the face normals (Get 2 vectors and find the cross product of those 2)

				vVector1 = CMath3d.Vector(vPoly[0], vPoly[2]);	// Get the vector of the polygon (we just need 2 sides for the normal)
				vVector2 = CMath3d.Vector(vPoly[2], vPoly[1]);	// Get a second vector of the polygon

				vNormal  = CMath3d.Cross(vVector1, vVector2);	// Return the cross product of the 2 vectors (normalize vector, but not a unit vector)
				pTempNormals[i] = vNormal;					// Save the un-normalized normal for the vertex normals
				vNormal  = CMath3d.Normalize(vNormal);		// Normalize the cross product to give us the polygons normal

				pNormals[i] = vNormal;						// Assign the normal to the list of normals
			}

			//////////////// Now Get The Vertex Normals /////////////////

			CVector3 vSum = new CVector3();
			CVector3 vZero = vSum;
			int shared=0;

			for (int i = 0; i < pObject.numOfVerts; i++)		// Go through all of the vertices
			{
				for (int j = 0; j < pObject.numOfFaces; j++)	// Go through all of the triangles
				{												// Check if the vertex is shared by another face
					if (pObject.pFaces[j].vertIndex[0] == i || 
						pObject.pFaces[j].vertIndex[1] == i || 
						pObject.pFaces[j].vertIndex[2] == i)
					{
						vSum = CMath3d.Add(vSum, pTempNormals[j]);// Add the un-normalized normal of the shared face
						shared++;								// Increase the number of shared triangles
					}
				}      
				
				// Get the normal by dividing the sum by the shared.  We negate the shared so it has the normals pointing out.
				pObject.pNormals[i] = CMath3d.Divide(vSum, (float)(-shared));

				// Normalize the normal for the final vertex normal
				pObject.pNormals[i] = CMath3d.Normalize(pObject.pNormals[i]);	

				vSum = vZero;									// Reset the sum
				shared = 0;										// Reset the shared
			}
		}
	}
	
/////////////////////////////////////////////////////////////////////////////////
	
	///////////////////////////////// IMPORTJOBJ \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Importa un oggetto da file java obj
	/////
	///////////////////////////////// IMPORTJOBJ \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public C3DModel ImportJOBJ(String file)
	{
		C3DModel Obj = new C3DModel();
		try {
			FileInputStream fin = new FileInputStream("../data/" + file);
			ObjectInputStream ois = new ObjectInputStream(fin);
			Obj = (C3DModel) ois.readObject();
			ois.close();
		} catch (Exception e) {
				 e.printStackTrace();
		}
	return Obj;
	}

}
