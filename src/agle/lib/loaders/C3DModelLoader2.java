/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE agle.lib

                            FILE CMODELLOADER2
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
package agle.lib.loaders;

// GL4Java imports
import gl4java.*;

// Java improts
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

// imports
import agle.lib.math.*;
import agle.lib.*;

public class C3DModelLoader2
{
	GLFunc			gl;
	GLUFunc			glu;
	
	public static final int kLower	= 0;		// This stores the ID for the legs model
	public static final int kUpper	= 1;		// This stores the ID for the torso model
	static final int kHead			= 2;		// This stores the ID for the head model
	static final int kWeapon		= 3;		// This stores the ID for the weapon model

	// This enumeration stores all the animations in order from the config file (.cfg)

	static final int BOTH_DEATH1	= 0;		// The first twirling death animation
	static final int BOTH_DEAD1		= 1;		// The end of the first twirling death animation
	static final int BOTH_DEATH2	= 2;		// The second twirling death animation
	static final int BOTH_DEAD2		= 3;		// The end of the second twirling death animation
	static final int BOTH_DEATH3	= 4;		// The back flip death animation
	static final int BOTH_DEAD3		= 5;		// The end of the back flip death animation

	// The next block is the animations that the upper body performs

	static final int TORSO_GESTURE	= 6;		// The torso's gesturing animation
	
	static final int TORSO_ATTACK	= 7;		// The torso's attack1 animation
	static final int TORSO_ATTACK2	= 8;		// The torso's attack2 animation

	static final int TORSO_DROP		= 9;		// The torso's weapon drop animation
	static final int TORSO_RAISE	= 10;		// The torso's weapon pickup animation

	static final int TORSO_STAND	= 11;		// The torso's idle stand animation
	static final int TORSO_STAND2	= 12;		// The torso's idle stand2 animation

	// The final block is the animations that the legs perform

	static final int LEGS_WALKCR	= 13;		// The legs's crouching walk animation
	static final int LEGS_WALK		= 14;		// The legs's walk animation
	static final int LEGS_RUN		= 15;		// The legs's run animation
	static final int LEGS_BACK		= 16;		// The legs's running backwards animation
	static final int LEGS_SWIM		= 17;		// The legs's swimming animation
	
	static final int LEGS_JUMP		= 18;		// The legs's jumping animation
	static final int LEGS_LAND		= 19;		// The legs's landing animation

	static final int LEGS_JUMPB		= 20;		// The legs's jumping back animation
	static final int LEGS_LANDB		= 21;		// The legs's landing back animation

	static final int LEGS_IDLE		= 22;		// The legs's idle stand animation
	static final int LEGS_IDLECR	= 23;		// The legs's idle crouching animation

	static final int LEGS_TURN		= 24;		// The legs's turn animation

	static final int MAX_ANIMATIONS	= 25;		// The define for the maximum amount of animations

	// This holds the header information that is read in at the beginning of the file
	public class tMd3Header
	{ 
		String	fileID;						// This stores the file ID - Must be "IDP3" [4]
		int		version;					// This stores the file version - Must be 15
		String	strFile;					// This stores the name of the file [68]
		int		numFrames;					// This stores the number of animation frames
		int		numTags;					// This stores the tag count
		int		numMeshes;					// This stores the number of sub-objects in the mesh
		int		numMaxSkins;				// This stores the number of skins for the mesh
		int		headerSize;					// This stores the mesh header size
		int		tagStart;					// This stores the offset into the file for tags
		int		tagEnd;						// This stores the end offset into the file for tags
		int		fileSize;					// This stores the file size

		public tMd3Header()
		{
			fileID		= byte2string(4);	m_FilePointer += 4;
			version		= byte2int();
			strFile		= byte2string(68);	m_FilePointer += 68;
			numFrames	= byte2int();
			numTags		= byte2int();
			numMeshes	= byte2int();
			numMaxSkins = byte2int();
			headerSize	= byte2int();
			tagStart	= byte2int();
			tagEnd		= byte2int();
			fileSize	= byte2int();
		}
	
	};

	// This structure is used to read in the mesh data for the .md3 models
	public class tMd3MeshInfo
	{
		String	meshID;						// This stores the mesh ID (We don't care) [4]
		String	strName;					// This stores the mesh name (We do care) [68]
		int		numMeshFrames;				// This stores the mesh aniamtion frame count
		int		numSkins;					// This stores the mesh skin count
		int     numVertices;				// This stores the mesh vertex count
		int		numTriangles;				// This stores the mesh face count
		int		triStart;					// This stores the starting offset for the triangles
		int		headerSize;					// This stores the header size for the mesh
		int     uvStart;					// This stores the starting offset for the UV coordinates
		int		vertexStart;				// This stores the starting offset for the vertex indices
		int		meshSize;					// This stores the total mesh size

		public tMd3MeshInfo()
		{
			meshID			= byte2string(4);	m_FilePointer += 4;
			strName			= byte2string(68);	m_FilePointer += 68;
			numMeshFrames	= byte2int();
			numSkins		= byte2int();
			numVertices		= byte2int();
			numTriangles	= byte2int();
			triStart		= byte2int();
			headerSize		= byte2int();
			uvStart			= byte2int();
			vertexStart		= byte2int();
			meshSize		= byte2int();
		}
	};

	// This is our tag structure for the .MD3 file format.  These are used link other
	// models to and the rotate and transate the child models of that model.
	public class tMd3Tag
	{
		String		strName;					// This stores the name of the tag (I.E. "tag_torso") [64]
		CVector3	vPosition = new CVector3();	// This stores the translation that should be performed
		float[]		rotation = new float[9];	// This stores the 3x3 rotation matrix for this frame

		public tMd3Tag()
		{
			strName = byte2string(64);	m_FilePointer += 64;
			vPosition.x = byte2float();
			vPosition.y = byte2float();
			vPosition.z = byte2float();
			for (int i=0; i < 9; i++ )
			{
				rotation[i] = byte2float();
			}
		}
	};

	// This stores the bone information (useless as far as I can see...)
	public class tMd3Bone
	{
		float[]	mins = new float[3];	// This is the min (x, y, z) value for the bone
		float[]	maxs = new float[3];	// This is the max (x, y, z) value for the bone
		float[]	position = new float[3];// This supposedly stores the bone position???
		float	scale;					// This stores the scale of the bone
		String	creator;				// The modeler used to create the model (I.E. "3DS Max") [16]

		public tMd3Bone()
		{
			mins[0]		= byte2float();
			mins[1]		= byte2float();
			mins[2]		= byte2float();
			maxs[0]		= byte2float();
			maxs[1]		= byte2float();
			maxs[2]		= byte2float();
			position[0] = byte2float();
			position[1] = byte2float();
			position[2] = byte2float();
			scale		= byte2float();
			creator		= byte2string(16);	m_FilePointer += 16;
		}
	};


	// This stores the normals and vertex indices 
	public class tMd3Triangle
	{
	   short[] vertex = new short[3];			// The vertex for this face (scale down by 64.0f) (short [3])
	   int[] normal = new int[2];				// This stores some crazy normal values (not sure...) (char [2])

	   public tMd3Triangle()
	   {
			vertex[0]	= byte2short();
			vertex[1]	= byte2short();
			vertex[2]	= byte2short();
			normal[0]	= byte2byte();
			normal[1]	= byte2byte();
	   }
	};


	// This stores the indices into the vertex and texture coordinate arrays
	class tMd3Face
	{
	   int[] vertexIndices = new int[3];	

	   tMd3Face()
	   {
			vertexIndices[0]	= byte2int();
			vertexIndices[1]	= byte2int();
			vertexIndices[2]	= byte2int();
	   }
	};


	// This stores UV coordinates
	class tMd3TexCoord
	{
	   float[] textureCoord = new float[2];

	   tMd3TexCoord()
	   {
		   textureCoord[0] = byte2float();
		   textureCoord[1] = byte2float();
	   }
	};


	// This stores a skin name (We don't use this, just the name of the model to get the texture)
	class tMd3Skin 
	{
		String strName;		//[68]

		tMd3Skin()
		{
			strName = byte2string(68);	m_FilePointer += 68;
		}
	};
	
	int byte2byte()
	{
		int b1 = (fileContents[m_FilePointer  ] & 0xFF);
		m_FilePointer += 1;
		return (b1);
	}

	short byte2short()
	{
		int s1 = (fileContents[m_FilePointer  ] & 0xFF);
		int s2 = (fileContents[m_FilePointer+1] & 0xFF) << 8;
		m_FilePointer += 2;
		return ((short)(s1 | s2));
	}

	int byte2int()
	{
		int i1 = (fileContents[m_FilePointer  ] & 0xFF);
		int i2 = (fileContents[m_FilePointer+1] & 0xFF) <<  8;
		int i3 = (fileContents[m_FilePointer+2] & 0xFF) << 16;
		int i4 = (fileContents[m_FilePointer+3] & 0xFF) << 24;
		m_FilePointer += 4;
		return (i1 | i2 | i3 | i4);
	}

	float byte2float()
	{
		return Float.intBitsToFloat(byte2int());
	}

	public String byte2string(int size)
	{
		//Look for zero terminated string from byte array
		for (int i=m_FilePointer;i<m_FilePointer + size ;i++ )
		{
			if ((fileContents[i] & 0xFF)== (byte)0)
			{
				return new String(fileContents, m_FilePointer, i - m_FilePointer);
			}
		}
		return new String(fileContents,m_FilePointer, size);
	}

	tMd3Header		m_Header;			// The header data
	tMd3Skin[]		m_pSkins;			// The skin name data (not used)
	tMd3TexCoord[]	m_pTexCoords;		// The texture coordinates
	tMd3Face[]		m_pTriangles;		// Face/Triangle data
	tMd3Triangle[]	m_pVertices;		// Vertex/UV indices
	tMd3Bone[]		m_pBones;			// This stores the bone data (not used)

	// These are are models for the character's head and upper and lower body parts
	public C3DModel m_Head;
	public C3DModel m_Upper;
	public C3DModel m_Lower;

	// This store the players weapon model (optional load)
	public C3DModel m_Weapon;
	
	// This stores the texture array for each of the textures assigned to this model
	int MAX_TEXTURES = 100;						// The maximum amount of textures to load
	int[] m_Textures = new int[MAX_TEXTURES];	
	ArrayList strTextures = new ArrayList();

	// These are global variables used in the Java Port
	byte[] fileContents;
	int m_FilePointer = 0;

	class CQuaternion
	{
		float x, y, z, w;

		CQuaternion()
		{
			x = 0.0f;	y = 0.0f;	z = 0.0f;	w = 1.0f;
		}
		
		// Creates a constructor that will allow us to initialize the quaternion when creating it
		CQuaternion(float X, float Y, float Z, float W) 
		{ 
			x = X;		y = Y;		z = Z;		w = W;
		}

		////////////////////////////// CREATE MATRIX \\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
		/////
		/////	This function converts a quaternion to a rotation matrix
		/////
		////////////////////////////// CREATE MATRIX \\\\\\\\\\\\\\\\\\\\\\\\\\\\\*	

		void CreateMatrix(float[] pMatrix)
		{
			// Make sure the matrix has allocated memory to store the rotation data
			if(pMatrix == null) return;
			
			// Fill in the rows of the 4x4 matrix, according to the quaternion to matrix equations

			// First row
			pMatrix[ 0] = 1.0f - 2.0f * ( y * y + z * z );  
			pMatrix[ 1] = 2.0f * ( x * y - w * z );  
			pMatrix[ 2] = 2.0f * ( x * z + w * y );  
			pMatrix[ 3] = 0.0f;  

			// Second row
			pMatrix[ 4] = 2.0f * ( x * y + w * z );  
			pMatrix[ 5] = 1.0f - 2.0f * ( x * x + z * z );  
			pMatrix[ 6] = 2.0f * ( y * z - w * x );  
			pMatrix[ 7] = 0.0f;  

			// Third row
			pMatrix[ 8] = 2.0f * ( x * z - w * y );  
			pMatrix[ 9] = 2.0f * ( y * z + w * x );  
			pMatrix[10] = 1.0f - 2.0f * ( x * x + y * y );  
			pMatrix[11] = 0.0f;  

			// Fourth row
			pMatrix[12] = 0;  
			pMatrix[13] = 0;  
			pMatrix[14] = 0;  
			pMatrix[15] = 1.0f;

			// Now pMatrix[] is a 4x4 homogeneous matrix that can be applied to an OpenGL Matrix
		}


		///////////////////////////////// CREATE FROM MATRIX \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
		/////
		/////	This creates a quaternion from a 3x3 or a 4x4 matrix, depending on rowColumnCount
		/////
		///////////////////////////////// CREATE FROM MATRIX \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
		void CreateFromMatrix(float[] pTheMatrix, int rowColumnCount)
		{
			// Make sure the matrix has valid memory and it's not expected that we allocate it.
			// Also, we do a check to make sure the matrix is a 3x3 or a 4x4 (must be 3 or 4).
			if((pTheMatrix == null) || ((rowColumnCount != 3) && (rowColumnCount != 4))) return;

			// This function is used to take in a 3x3 or 4x4 matrix and convert the matrix
			// to a quaternion.  If rowColumnCount is a 3, then we need to convert the 3x3
			// matrix passed in to a 4x4 matrix, otherwise we just leave the matrix how it is.
			// Since we want to apply a matrix to an OpenGL matrix, we need it to be 4x4.

			// Point the matrix pointer to the matrix passed in, assuming it's a 4x4 matrix
			float[] pMatrix = pTheMatrix;

			// Create a 4x4 matrix to convert a 3x3 matrix to a 4x4 matrix (If rowColumnCount == 3)
			float[] m4x4 = new float[16];

			// If the matrix is a 3x3 matrix (which it is for Quake3), then convert it to a 4x4
			if(rowColumnCount == 3)
			{
				// Set the 9 top left indices of the 4x4 matrix to the 9 indices in the 3x3 matrix.
				// It would be a good idea to actually draw this out so you can visualize it.
				m4x4[0]  = pTheMatrix[0];	m4x4[1]  = pTheMatrix[1];	m4x4[2]  = pTheMatrix[2];	m4x4[ 3] = 0f;
				m4x4[4]  = pTheMatrix[3];	m4x4[5]  = pTheMatrix[4];	m4x4[6]  = pTheMatrix[5];	m4x4[ 7] = 0f;
				m4x4[8]  = pTheMatrix[6];	m4x4[9]  = pTheMatrix[7];	m4x4[10] = pTheMatrix[8];	m4x4[11] = 0f;

				// Since the bottom and far right indices are zero, set the bottom right corner to 1.
				// This is so that it follows the standard diagonal line of 1's in the identity matrix.
				m4x4[12] = 0f;	m4x4[13] = 0f;	m4x4[14] = 0f;	m4x4[15] = 1;

				// Set the matrix pointer to the first index in the newly converted matrix
				pMatrix = m4x4;
			}

			// The next step, once we made sure we are dealing with a 4x4 matrix, is to check the
			// diagonal of the matrix.  This means that we add up all of the indices that comprise
			// the standard 1's in the identity matrix.  If you draw out the identity matrix of a
			// 4x4 matrix, you will see that they 1's form a diagonal line.  Notice we just assume
			// that the last index (15) is 1 because it is not effected in the 3x3 rotation matrix.

			// Find the diagonal of the matrix by adding up it's diagonal indices.
			// This is also known as the "trace", but I will call the variable diagonal.
			float diagonal = pMatrix[0] + pMatrix[5] + pMatrix[10] + 1;
			float scale = 0.0f;

			// Below we check if the diagonal is greater than zero.  To avoid accidents with
			// floating point numbers, we substitute 0 with 0.00000001.  If the diagonal is
			// great than zero, we can perform an "instant" calculation, otherwise we will need
			// to identify which diagonal element has the greatest value.  Note, that it appears
			// that %99 of the time, the diagonal IS greater than 0 so the rest is rarely used.

			// If the diagonal is greater than zero
			if(diagonal > 0.00000001f)
			{
				// Calculate the scale of the diagonal
				scale = (float)Math.sqrt(diagonal) * 2f;

				// Calculate the x, y, z and w of the quaternion through the respective equation
				x = ( pMatrix[9] - pMatrix[6] ) / scale;
				y = ( pMatrix[2] - pMatrix[8] ) / scale;
				z = ( pMatrix[4] - pMatrix[1] ) / scale;
				w = 0.25f * scale;
			}
			else 
			{
				// If the first element of the diagonal is the greatest value
				if ( pMatrix[0] > pMatrix[5] && pMatrix[0] > pMatrix[10] )  
				{	
					// Find the scale according to the first element, and double that value
					scale  = (float)Math.sqrt( 1.0f + pMatrix[0] - pMatrix[5] - pMatrix[10] ) * 2.0f;

					// Calculate the x, y, z and w of the quaternion through the respective equation
					x = 0.25f * scale;
					y = (pMatrix[4] + pMatrix[1] ) / scale;
					z = (pMatrix[2] + pMatrix[8] ) / scale;
					w = (pMatrix[9] - pMatrix[6] ) / scale;	
				} 
				// Else if the second element of the diagonal is the greatest value
				else if ( pMatrix[5] > pMatrix[10] ) 
				{
					// Find the scale according to the second element, and double that value
					scale  = (float)Math.sqrt( 1.0f + pMatrix[5] - pMatrix[0] - pMatrix[10] ) * 2.0f;
					
					// Calculate the x, y, z and w of the quaternion through the respective equation
					x = (pMatrix[4] + pMatrix[1] ) / scale;
					y = 0.25f * scale;
					z = (pMatrix[9] + pMatrix[6] ) / scale;
					w = (pMatrix[2] - pMatrix[8] ) / scale;
				} 
				// Else the third element of the diagonal is the greatest value
				else 
				{	
					// Find the scale according to the third element, and double that value
					scale  = (float)Math.sqrt( 1.0f + pMatrix[10] - pMatrix[0] - pMatrix[5] ) * 2.0f;

					// Calculate the x, y, z and w of the quaternion through the respective equation
					x = (pMatrix[2] + pMatrix[8] ) / scale;
					y = (pMatrix[9] + pMatrix[6] ) / scale;
					z = 0.25f * scale;
					w = (pMatrix[4] - pMatrix[1] ) / scale;
				}
			}
		}


		/////////////////////////////////////// SLERP \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
		/////
		/////	Returns a spherical linear interpolated quaternion between q1 and q2, with respect to t
		/////
		/////////////////////////////////////// SLERP \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
		CQuaternion Slerp(CQuaternion q1, CQuaternion q2, float t)
		{
			// Create a local quaternion to store the interpolated quaternion
			CQuaternion qInterpolated = new CQuaternion();

			// This function is the milk and honey of our quaternion code, the rest of
			// the functions are an appendage to what is done here.  Everyone understands
			// the terms, "matrix to quaternion", "quaternion to matrix", "create quaternion matrix",
			// "quaternion multiplication", etc.. but "SLERP" is the stumbling block, even a little 
			// bit after hearing what it stands for, "Spherical Linear Interpolation".  What that
			// means is that we have 2 quaternions (or rotations) and we want to interpolate between 
			// them.  The reason what it's called "spherical" is that quaternions deal with a sphere.  
			// Linear interpolation just deals with 2 points primarily, where when dealing with angles
			// and rotations, we need to use sin() and cos() for interpolation.  If we wanted to use
			// quaternions for camera rotations, which have much more instant and jerky changes in 
			// rotations, we would use Spherical-Cubic Interpolation.  The equation for SLERP is this:
			//
			// q = (((b.a)^-1)^t)a
			//
			// Go here for an a detailed explanation and proofs of this equation:
			//
			// http://www.magic-software.com/Documentation/quat.pdf
			//
			// Now, Let's code it

			// Here we do a check to make sure the 2 quaternions aren't the same, return q1 if they are
			if(q1.x == q2.x && q1.y == q2.y && q1.z == q2.z && q1.w == q2.w) 
				return q1;

			// Following the (b.a) part of the equation, we do a dot product between q1 and q2.
			// We can do a dot product because the same math applied for a 3D vector as a 4D vector.
			float result = (q1.x * q2.x) + (q1.y * q2.y) + (q1.z * q2.z) + (q1.w * q2.w);

			// If the dot product is less than 0, the angle is greater than 90 degrees
			if(result < 0.0f)
			{
				// Negate the second quaternion and the result of the dot product
				q2.x = -q2.x;
				q2.y = -q2.y;
				q2.z = -q2.z;
				q2.w = -q2.w;
				result = -result;
			}

			// Set the first and second scale for the interpolation
			float scale0 = 1 - t;
			float scale1 = t;

			// Next, we want to actually calculate the spherical interpolation.  Since this
			// calculation is quite computationally expensive, we want to only perform it
			// if the angle between the 2 quaternions is large enough to warrant it.  If the
			// angle is fairly small, we can actually just do a simpler linear interpolation
			// of the 2 quaternions, and skip all the complex math.  We create a "delta" value
			// of 0.1 to say that if the cosine of the angle (result of the dot product) between
			// the 2 quaternions is smaller than 0.1, then we do NOT want to perform the full on 
			// interpolation using.  This is because you won't really notice the difference.

			// Check if the angle between the 2 quaternions was big enough to warrant such calculations
			if((1 - result) > 0.1f)
			{
				// Get the angle between the 2 quaternions, and then store the sin() of that angle
				float theta = (float)Math.acos(result);
				float sinTheta = (float)Math.sin(theta);

				// Calculate the scale for q1 and q2, according to the angle and it's sine value
				scale0 = (float)Math.sin( ( 1 - t ) * theta) / sinTheta;
				scale1 = (float)Math.sin( ( t * theta) ) / sinTheta;
			}	

			// Calculate the x, y, z and w values for the quaternion by using a special
			// form of linear interpolation for quaternions.
			qInterpolated.x = (scale0 * q1.x) + (scale1 * q2.x);
			qInterpolated.y = (scale0 * q1.y) + (scale1 * q2.y);
			qInterpolated.z = (scale0 * q1.z) + (scale1 * q2.z);
			qInterpolated.w = (scale0 * q1.w) + (scale1 * q2.w);

			// Return the interpolated quaternion
			return qInterpolated;
		}
	};
	
	///////////////////////////////// IS IN STRING \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns true if the string strSubString is inside of strString
	/////
	///////////////////////////////// IS IN STRING \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	boolean IsInString(String strString, String strSubString)
	{
		// Make sure both of these strings are valid, return false if any are empty
		if(strString == null || strSubString == null) return false;

		// grab the starting index where the sub string is in the original string
		int index = strString.indexOf(strSubString);

		// Make sure the index returned was valid
		if(index >= 0 && index < strString.length())
			return true;

		// The sub string does not exist in strString.
		return false;
	}

	///////////////////////////////// CMODEL MD3 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This is our CModelMD3 constructor
	/////
	///////////////////////////////// CMODEL MD3 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public C3DModelLoader2(GLFunc pgl, GLUFunc pglu )
	{
		gl = pgl;
		glu = pglu;	
		m_Head = new C3DModel();
		m_Upper = new C3DModel();
		m_Lower = new C3DModel();
		m_Weapon = new C3DModel();
	}

	///////////////////////////////// GET BODY PART \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns a specific model from the character (kLower, kUpper, kHead, kWeapon)
	/////
	///////////////////////////////// GET BODY PART \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public C3DModel GetModel(int whichPart)
	{
		// Return the legs model if desired
		if(whichPart == kLower) 
			return m_Lower;

		// Return the torso model if desired
		if(whichPart == kUpper) 
			return m_Upper;

		// Return the head model if desired
		if(whichPart == kHead) 
			return m_Head;

		// Return the weapon model
		return m_Weapon;
	}
	
	///////////////////////////////// GETMODELDIMENSIONS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This sets our initial width of the model, as well as our center point
	/////
	///////////////////////////////// GETMODELDIMENSIONS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void GetModelDimensions(C3DModel pModel)
	{
		// Initialize some temporary variables to hold the max dimensions found
		float maxWidth = 0, maxHeight = 0, maxDepth = 0;
		// Return from this function if we passed in bad data.  This used to be a check
		// to see if the vertices passed in were allocated, now it's a check for world data.
		if(pModel == null) return;

		// Initialize a variable to hold the total amount of vertices in the scene
		int numberOfVerts = 0;

		// This code is still doing the same things as in the previous tutorials,
		// except that we have to go through every object in the scene to find the
		// center point.

		// Go through all of the object's vertices and add them up to eventually find the center
		for(int i = 0; i < pModel.numOfObjects; i++)
		{
			// Increase the total vertice count
			numberOfVerts += ((C3DObject)pModel.pObject.get(i)).numOfVerts;

			// Add the current object's vertices up
			for(int n = 0; n < ((C3DObject)pModel.pObject.get(i)).numOfVerts; n++)
			{
				// Add the current vertex to the center variable (Using operator overloading)
				pModel.vCenter = CMath3d.Add(pModel.vCenter , ((C3DObject)pModel.pObject.get(i)).pVerts[n]);
			}
		}
		// Divide the total by the number of vertices to get the center point.
		// We could have overloaded the / symbol but I chose not to because we rarely use it.
		pModel.vCenter.x /= numberOfVerts;
		pModel.vCenter.y /= numberOfVerts;	
		pModel.vCenter.z /= numberOfVerts;

		// Now that we have the center point, we want to find the farthest distance from
		// our center point.  That will tell us how big the width of the first node is.
		// Once we get the farthest height, width and depth, we then check them against each
		// other.  Which ever one is higher, we then use that value for the cube width.

		int currentWidth = 0, currentHeight = 0, currentDepth = 0;

		// This code still does the same thing as in the previous octree tutorials,
		// except we need to go through each object in the scene to find the max dimensions.

		// Go through all of the scene's objects
		for(int i = 0; i < pModel.numOfObjects; i++)
		{
			// Go through all of the current objects vertices
			for(int j = 0; j < ((C3DObject)pModel.pObject.get(i)).numOfVerts; j++)
			{
				// Get the distance in width, height and depth this vertex is from the center.
				currentWidth  = (int)Math.abs(((C3DObject)pModel.pObject.get(i)).pVerts[j].x - pModel.vCenter.x);	
				currentHeight = (int)Math.abs(((C3DObject)pModel.pObject.get(i)).pVerts[j].y - pModel.vCenter.y);		
				currentDepth  = (int)Math.abs(((C3DObject)pModel.pObject.get(i)).pVerts[j].z - pModel.vCenter.z);

				// Check if the current width value is greater than the max width stored.
				if(currentWidth  > maxWidth)	maxWidth  = currentWidth;

				// Check if the current height value is greater than the max height stored.
				if(currentHeight > maxHeight)	maxHeight = currentHeight;

				// Check if the current depth value is greater than the max depth stored.
				if(currentDepth > maxDepth)		maxDepth  = currentDepth;
			}
		}
		// Set the member variable dimensions to the max ones found.
		// We multiply the max dimensions by 2 because this will give us the
		// full width, height and depth.  Otherwise, we just have half the size
		// because we are calculating from the center of the scene.
		maxWidth *= 2;		maxHeight *= 2;		maxDepth *= 2;

		pModel.width = maxWidth;
		pModel.height = maxHeight;
		pModel.depth = maxDepth;
	}

	///////////////////////////////// LOAD MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This loads our Quake3 model from the given path and character name
	/////
	///////////////////////////////// LOAD MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public boolean LoadModel(String strPath, String strModel)
		throws IOException
	{
		String strLowerModel;			// This stores the file name for the lower.md3 model
		String strUpperModel;			// This stores the file name for the upper.md3 model
		String strHeadModel;			// This stores the file name for the head.md3 model
		String strLowerSkin;			// This stores the file name for the lower.md3 skin
		String strUpperSkin;			// This stores the file name for the upper.md3 skin
		String strHeadSkin;				// This stores the file name for the head.md3 skin

		// This function is where all the character loading is taken care of.  We use
		// our CLoadMD3 class to load the 3 mesh and skins for the character. Since we
		// just have 1 name for the model, we add that to _lower.md3, _upper.md3 and _head.md3
		// to load the correct mesh files.

		// Make sure valid path and model names were passed in
		if(strPath == null || strModel == null) return false;

		// Store the correct files names for the .md3 and .skin file for each body part.
		// We concatinate this on top of the path name to be loaded from.
		strLowerModel = strPath + "/" + strModel + "_lower.md3";
		strUpperModel = strPath + "/" + strModel + "_upper.md3";
		strHeadModel = strPath + "/" + strModel + "_head.md3";
		
		// Get the skin file names with their path
		strLowerSkin = strPath + "/" + strModel + "_lower.skin";
		strUpperSkin = strPath + "/" + strModel + "_upper.skin";
		strHeadSkin = strPath + "/" + strModel + "_head.skin";

		// Next we want to load the character meshes.  The CModelMD3 class has member
		// variables for the head, upper and lower body parts.  These are of type C3DModel.
		// Depending on which model we are loading, we pass in those structures to ImportMD3.
		// This returns a true of false to let us know that the file was loaded okay.  The
		// appropriate file name to load is passed in for the last parameter.

		// Load the head mesh (*_head.md3) and make sure it loaded properly
		if(!ImportMD3(m_Head, strHeadModel))
		{
			// Display an error message telling us the file could not be found
			System.err.println("Unable to load the HEAD model!");
			return false;
		}

		// Load the upper mesh (*_head.md3) and make sure it loaded properly
		if(!ImportMD3(m_Upper, strUpperModel))		
		{
			// Display an error message telling us the file could not be found
			System.err.println("Unable to load the UPPER model!");
			return false;
		}

		// Load the lower mesh (*_lower.md3) and make sure it loaded properly
		if(!ImportMD3(m_Lower, strLowerModel))
		{
			// Display an error message telling us the file could not be found
			System.err.println("Unable to load the LOWER model!");
			return false;
		}

		// Load the lower skin (*_upper.skin) and make sure it loaded properly
		if(!LoadSkin(m_Lower, strLowerSkin))
		{
			// Display an error message telling us the file could not be found
			System.err.println("Unable to load the LOWER skin!");
			return false;
		}

		// Load the upper skin (*_upper.skin) and make sure it loaded properly
		if(!LoadSkin(m_Upper, strUpperSkin))
		{
			// Display an error message telling us the file could not be found
			System.err.println("Unable to load the UPPER skin!");
			return false;
		}

		// Load the head skin (*_head.skin) and make sure it loaded properly
		if(!LoadSkin(m_Head, strHeadSkin))
		{
			// Display an error message telling us the file could not be found
			System.err.println("Unable to load the HEAD skin!");
			return false;
		}

		// Once the models and skins were loaded, we need to load then textures.
		// We don't do error checking for this because we call CreateTexture() and 
		// it already does error checking.  Most of the time there is only
		// one or two textures that need to be loaded for each character.  There are
		// different skins though for each character.  For instance, you could have a
		// army looking Lara Croft, or the normal look.  You can have multiple types of
		// looks for each model.  Usually it is just color changes though.


		// Load the lower, upper and head textures.  
		LoadModelTextures(m_Lower, strPath);
		LoadModelTextures(m_Upper, strPath);
		LoadModelTextures(m_Head, strPath);
		
		// We added to this function the code that loads the animation config file

		// Add the path and file name prefix to the animation.cfg file
		String strConfigFile = strPath + "/" + strModel + "_animation.cfg";

		// Load the animation config file (*_animation.config) and make sure it loaded properly
		if(!LoadAnimations(strConfigFile))
		{
			// Display an error message telling us the file could not be found
			System.err.println("Unable to load the Animation Config File!");
			return false;
		}
		
		// Link the lower body to the upper body when the tag "tag_torso" is found in our tag array
		LinkModel(m_Lower, m_Upper, "tag_torso");

		// Link the upper body to the head when the tag "tag_head" is found in our tag array
		LinkModel(m_Upper, m_Head, "tag_head");
		
		GetModelDimensions(m_Lower);
		GetModelDimensions(m_Upper);
		GetModelDimensions(m_Head);
		
		// The character was loaded correctly so return true
		return true;
	}

	///////////////////////////////// LOAD WEAPON \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This loads a Quake3 weapon model from the given path and weapon name
	/////
	///////////////////////////////// LOAD WEAPON \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public boolean LoadWeapon(String strPath, String strModel)
		throws IOException
	{
		String strWeaponModel;				// This stores the file name for the weapon model
		String strWeaponShader;				// This stores the file name for the weapon shader.

		// Make sure the path and model were valid, otherwise return false
		if(strPath == null || strModel == null) return false;

		// Concatenate the path and model name together
		strWeaponModel = strPath + "/" + strModel + ".md3";
		
		// Next we want to load the weapon mesh.  The CModelMD3 class has member
		// variables for the weapon model and all it's sub-objects.  This is of type C3DModel.
		// We pass in a reference to this model in to ImportMD3 to save the data read.
		// This returns a true of false to let us know that the weapon was loaded okay.  The
		// appropriate file name to load is passed in for the last parameter.

		// Load the weapon mesh (*.md3) and make sure it loaded properly
		if(!ImportMD3(m_Weapon, strWeaponModel))
		{
			// Display the error message that we couldn't find the weapon MD3 file and return false
			System.err.println("Unable to load the WEAPON model!");
			return false;
		}

		// Unlike the other .MD3 files, a weapon has a .shader file attached with it, not a
		// .skin file.  The shader file has it's own scripting language to describe behaviors
		// of the weapon.  All we care about for this tutorial is it's normal texture maps.
		// There are other texture maps in the shader file that mention the ammo and sphere maps,
		// but we don't care about them for our purposes.  I gutted the shader file to just store
		// the texture maps.  The order these are in the file is very important.  The first
		// texture refers to the first object in the weapon mesh, the second texture refers
		// to the second object in the weapon mesh, and so on.  I didn't want to write a complex
		// .shader loader because there is a TON of things to keep track of.  It's a whole
		// scripting language for goodness sakes! :)  Keep this in mind when downloading new guns.

		// Add the path, file name and .shader extension together to get the file name and path
		strWeaponShader = strPath + "/" + strModel + ".shader";

		// Load our textures associated with the gun from the weapon shader file
		if(!LoadShader(m_Weapon, strWeaponShader))
		{
			// Display the error message that we couldn't find the shader file and return false
			System.err.println("Unable to load the SHADER file!");
			return false;
		}

		// We should have the textures needed for each weapon part loaded from the weapon's
		// shader, so let's load them in the given path.
		LoadModelTextures(m_Weapon, strPath);

		// Just like when we loaded the character mesh files, we need to link the weapon to
		// our character.  The upper body mesh (upper.md3) holds a tag for the weapon.
		// This way, where ever the weapon hand moves, the gun will move with it.

		// Link the weapon to the model's hand that has the weapon tag
		LinkModel(m_Upper, m_Weapon, "tag_weapon");
			
		// The weapon loaded okay, so let's return true to reflect this
		return true;
	}

	///////////////////////////////// LOAD MODEL TEXTURES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This loads the textures for the current model passed in with a directory
	/////
	///////////////////////////////// LOAD WEAPON \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void LoadModelTextures(C3DModel pModel, String strPath)
	{
		// This function loads the textures that are assigned to each mesh and it's
		// sub-objects.  For instance, the Lara Croft character has a texture for the body
		// and the face/head, and since she has the head as a sub-object in the lara_upper.md3 model, 
		// the MD3 file needs to contain texture information for each separate object in the mesh.
		// There is another thing to note too...  Some meshes use the same texture map as another 
		// one. We don't want to load 2 of the same texture maps, so we need a way to keep track of
		// which texture is already loaded so that we don't double our texture memory for no reason.
		// This is controlled with a STL vector list of "strings".  Every time we load a texture
		// we add the name of the texture to our list of strings.  Before we load another one,
		// we make sure that the texture map isn't already in our list.  If it is, we assign
		// that texture index to our current models material texture ID.  If it's a new texture,
		// then the new texture is loaded and added to our characters texture array: m_Textures[].

		// Go through all the materials that are assigned to this model
		for(int i = 0; i < pModel.numOfMaterials; i++)
		{
			// Check to see if there is a file name to load in this material
			if(((CMaterialInfo)pModel.pMaterials.get(i)).strFile != null)
			{
				// Create a boolean to tell us if we have a new texture to load
				boolean bNewTexture = true;

				// Go through all the textures in our string list to see if it's already loaded
				for(int j = 0; j < strTextures.size(); j++)
				{
					// If the texture name is already in our list of texture, don't load it again.
					if( ((CMaterialInfo)pModel.pMaterials.get(i)).strFile.equals( ((String)strTextures.get(j)) ) )
					{
						// We don't need to load this texture since it's already loaded
						bNewTexture = false;

						// Assign the texture index to our current material textureID.
						// This ID will them be used as an index into m_Textures[].
						((CMaterialInfo)pModel.pMaterials.get(i)).texureId = j;
					}
				}

				// Make sure before going any further that this is a new texture to be loaded
				if(bNewTexture == false) continue;
				
				String strFullPath;

				// Add the file name and path together so we can load the texture
				strFullPath = strPath + "/" + ((CMaterialInfo)pModel.pMaterials.get(i)).strFile;

				// We pass in a reference to an index into our texture array member variable.
				// The size() function returns the current loaded texture count.  Initially
				// it will be 0 because we haven't added any texture names to our strTextures list.
				CreateTexture(m_Textures, strFullPath, strTextures.size());

				// Set the texture ID for this material by getting the current loaded texture count
				((CMaterialInfo)pModel.pMaterials.get(i)).texureId = strTextures.size();

				// Now we increase the loaded texture count by adding the texture name to our
				// list of texture names.  Remember, this is used so we can check if a texture
				// is already loaded before we load 2 of the same textures.  Make sure you
				// understand what an STL vector list is.  We have a tutorial on it if you don't.
				strTextures.add(((CMaterialInfo)pModel.pMaterials.get(i)).strFile);
			}
		}
	}

	///////////////////////////////// LOAD ANIMATIONS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This loads the .cfg file that stores all the animation information
	/////
	///////////////////////////////// LOAD ANIMATIONS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public boolean LoadAnimations(String strConfigFile)
		throws IOException
	{
		// This function is given a path and name to an animation config file to load.
		// The implementation of this function is arbitrary, so if you have a better way
		// to parse the animation file, that is just as good.  Whatever works.
		// Basically, what is happening here, is that we are grabbing an animation line:
		//
		// "0	31	0	25		// BOTH_DEATH1"
		//
		// Then parsing it's values.  The first number is the starting frame, the next
		// is the frame count for that animation (endFrame would equal startFrame + frameCount),
		// the next is the looping frames (ignored), and finally the frames per second that
		// the animation should run at.  The end of this line is the name of the animation.
		// Once we get that data, we store the information in our CAnimationInfo object, then
		// after we finish parsing the file, the animations are assigned to each model.  
		// Remember, that only the torso and the legs objects have animation.  It is important
		// to note also that the animation prefixed with BOTH_* are assigned to both the legs
		// and the torso animation list, hence the name "BOTH" :)

		// Create an animation object for every valid animation in the Quake3 Character
		CAnimationInfo[] animations = new CAnimationInfo[MAX_ANIMATIONS];

		InputStream is = null;
		int fileSize = 0;
		File f;
		BufferedInputStream in;
		try
		{
			f = new File(strConfigFile);  
			is = new FileInputStream(f);    
		}
		catch (Exception e)
		{
			System.out.println("Failed to connect to ");
			e.printStackTrace();
			return false;
		}
		// wrap a buffer to make reading more efficient (faster)
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		String strWord = "";				// This stores the current word we are reading in
		String strLine = "";				// This stores the current line we read in
		int currentAnim = 0;				// This stores the current animation count
		int torsoOffset = 0;				// The offset between the first torso and leg animation
		StringTokenizer tokenizer;

		// Here we go through every word in the file until a numeric number is found.
		// This is how we know that we are on the animation lines, and past header info.
		// This of course isn't the most solid way, but it works fine.  It wouldn't hurt
		// to put in some more checks to make sure no numbers are in the header info.
		while ((strLine = reader.readLine()) != null)
		{
			// skip blank lines
			if (strLine.length() == 0)
			{
				continue;
			}
			// If the first character of the word is NOT a number, we haven't hit an animation line
			if(!Character.isDigit( strLine.charAt(0) ))
			{
				continue;
			}

			// If we get here, we must be on an animation line, so let's parse the data.
			// We should already have the starting frame stored in strWord, so let's extract it.
			tokenizer = new StringTokenizer(strLine);

			// Read in the number of frames, the looping frames, then the frames per second
			// for this current animation we are on.
			int startFrame		= Integer.parseInt(tokenizer.nextToken());
			int numOfFrames		= Integer.parseInt(tokenizer.nextToken());
			int loopingFrames	= Integer.parseInt(tokenizer.nextToken());
			int framesPerSecond = Integer.parseInt(tokenizer.nextToken());
			
			// Initialize the current animation structure with the data just read in
			animations[currentAnim] = new CAnimationInfo();
			animations[currentAnim].startFrame		= startFrame;
			animations[currentAnim].endFrame		= startFrame + numOfFrames;
			animations[currentAnim].loopingFrames	= loopingFrames;
			animations[currentAnim].framesPerSecond = framesPerSecond;

			// Read past the "//" and read in the animation name (I.E. "BOTH_DEATH1").
			// This might not be how every config file is set up, so make sure.
			tokenizer.nextToken();

			// Copy the name of the animation to our animation structure
			animations[currentAnim].strName = tokenizer.nextToken();

			// If the animation is for both the legs and the torso, add it to their animation list
			if(IsInString(strLine, "BOTH"))
			{
				// Add the animation to each of the upper and lower mesh lists
				m_Upper.pAnimations.add(animations[currentAnim]);
				m_Lower.pAnimations.add(animations[currentAnim]);
			}
			// If the animation is for the torso, add it to the torso's list
			else if(IsInString(strLine, "TORSO"))
			{
				m_Upper.pAnimations.add(animations[currentAnim]);
			}
			// If the animation is for the legs, add it to the legs's list
			else if(IsInString(strLine, "LEGS"))
			{	
				// Because I found that some config files have the starting frame for the
				// torso and the legs a different number, we need to account for this by finding
				// the starting frame of the first legs animation, then subtracting the starting
				// frame of the first torso animation from it.  For some reason, some exporters
				// might keep counting up, instead of going back down to the next frame after the
				// end frame of the BOTH_DEAD3 anim.  This will make your program crash if so.
				
				// If the torso offset hasn't been set, set it
				if(torsoOffset == 0)
					torsoOffset = animations[LEGS_WALKCR].startFrame - animations[TORSO_GESTURE].startFrame;

				// Minus the offset from the legs animation start and end frame.
				animations[currentAnim].startFrame -= torsoOffset;
				animations[currentAnim].endFrame -= torsoOffset;

				// Add the animation to the list of leg animations
				m_Lower.pAnimations.add(animations[currentAnim]);
			}
		
			// Increase the current animation count
			currentAnim++;
		}	

		// Store the number if animations for each list by the STL vector size() function
		m_Lower.numOfAnimations = m_Lower.pAnimations.size();
		m_Upper.numOfAnimations = m_Upper.pAnimations.size();
		m_Head.numOfAnimations = m_Head.pAnimations.size();
		m_Weapon.numOfAnimations = m_Head.pAnimations.size();

		// Return a success
		return true;
	}

	///////////////////////////////// LINK MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This links the body part models to each other, along with the weapon
	/////
	///////////////////////////////// LINK MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	void  LinkModel(C3DModel pModel, C3DModel pLink, String strTagName)
	{
		// Make sure we have a valid model, link and tag name, otherwise quit this function
		if(pModel == null || pLink ==null || strTagName == null) return;

		// This function is used to link 2 models together at a psuedo joint.  For instance,
		// if we were animating an arm, we would link the top part of the arm to the shoulder,
		// then the forearm to would be linked to the top part of the arm, then the hand to
		// the forearm.  That is what is meant by linking.  That way, when we rotate the
		// arm at the shoulder, the rest of the arm will move with it because they are attached
		// to the same matrix that moves the top of the arm.  You can think of the shoulder
		// as the arm's parent node, and the rest are children that are subject to move to where
		// ever the top part of the arm goes.  That is how bone/skeletal animation works.
		//
		// So, we have an array of tags that have a position, rotation and name.  If we want
		// to link the lower body to the upper body, we would pass in the lower body mesh first,
		// then the upper body mesh, then the tag "tag_torso".  This is a tag that quake set as
		// as a standard name for the joint between the legs and the upper body.  This tag was
		// saved with the lower.md3 model.  We just need to loop through the lower body's tags,
		// and when we find "tag_torso", we link the upper.md3 mesh too that tag index in our
		// pLinks array.  This is an array of pointers to hold the address of the linked model.
		// Quake3 models are set up in a weird way, but usually you would just forget about a
		// separate array for links, you would just have a pointer to a C3DModel in the tag
		// structure, which in retrospect, you wouldn't have a tag array, you would have
		// a bone/joint array.  Stayed tuned for a bone animation tutorial from scratch.  This
		// will show you exactly what I mean if you are confused.

		// Go through all of our tags and find which tag contains the strTagName, then link'em
		for(int i = 0; i < pModel.numOfTags; i++)
		{
			// If this current tag index has the tag name we are looking for
			if( pModel.pTags[i].strName.equals(strTagName) )
			{
				// Link the model's link index to the link (or model/mesh) and return
				pModel.pLinks[i] = pLink;
				return;
			}
		}
	}

	///////////////////////////////// UPDATE MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This sets the current frame of animation, depending on it's fps and t
	/////
	///////////////////////////////// UPDATE MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*	
	void UpdateModel(C3DModel pModel)
	{
		// Initialize a start and end frame, for models with no animation
		int startFrame = 0;
		int endFrame   = 1;
		
		// This function is used to keep track of the current and next frames of animation
		// for each model, depending on the current animation.  Some models down have animations,
		// so there won't be any change.

		// Here we grab the current animation that we are on from our model's animation list
		CAnimationInfo pAnim = (CAnimationInfo)pModel.pAnimations.get(pModel.currentAnim);

		// If there is any animations for this model
		if(pModel.numOfAnimations != 0)
		{
			// Set the starting and end frame from for the current animation
			startFrame = pAnim.startFrame;
			endFrame   = pAnim.endFrame;
		}
		
		// This gives us the next frame we are going to.  We mod the current frame plus
		// 1 by the current animations end frame to make sure the next frame is valid.
		pModel.nextFrame = (pModel.currentFrame + 1) % endFrame;

		// If the next frame is zero, that means that we need to start the animation over.
		// To do this, we set nextFrame to the starting frame of this animation.
		if(pModel.nextFrame == 0) {
			pModel.nextFrame =  startFrame;
		}	

		// Next, we want to get the current time that we are interpolating by.  Remember,
		// if t = 0 then we are at the beginning of the animation, where if t = 1 we are at the end.
		// Anything from 0 to 1 can be thought of as a percentage from 0 to 100 percent complete.
		SetCurrentTime(pModel);
	}

	///////////////////////////////// DRAW MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This recursively draws all the character nodes, starting with the legs
	/////
	///////////////////////////////// DRAW MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void DrawModel()
	{
		// This is the function that is called by the client (you) when using the 
		// CModelMD3 class object.  You will notice that we rotate the model by
		// -90 degrees along the x-axis.  This is because most modelers have z up
		// so we need to compensate for this.  Usually I would just switch the
		// z and y values when loading in the vertices, but the rotations that
		// are stored in the tags (joint info) are a matrix, which makes it hard
		// to change those to reflect Y up.  I didn't want to mess with that so
		// this 1 rotate will fix this problem.

		// Rotate the model to compensate for the z up orientation that the model was saved
		gl.glRotatef(-90, 1, 0, 0);

		// Since we have animation now, when we draw the model the animation frames need
		// to be updated.  To do that, we pass in our lower and upper models to UpdateModel().
		// There is no need to pass in the head or weapon, since they don't have any animation.

		// Update the leg and torso animations
		UpdateModel(m_Lower);
		UpdateModel(m_Upper);
		
		// Draw the first link, which is the lower body.  This will then recursively go
		// through the models attached to this model and drawn them.
		DrawLink(m_Lower);
	}

	///////////////////////////////// DRAW LINK \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This draws the current mesh with an effected matrix stack from the last mesh
	/////
	///////////////////////////////// DRAW LINK \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	void DrawLink(C3DModel pModel)
	{
		// Draw the current model passed in (Initially the legs)
		RenderModel(pModel);

		// Though the changes to this function from the previous tutorial aren't huge, they
		// are pretty powerful.  Since animation is in effect, we need to create a rotational
		// matrix for each key frame, at each joint, to be applied to the child nodes of that 
		// object.  We can also slip in the interpolated translation into that same matrix.
		// The big thing in this function is interpolating between the 2 rotations.  The process
		// involves creating 2 quaternions from the current and next key frame, then using
		// slerp (spherical linear interpolation) to find the interpolated quaternion, then
		// converting that quaternion to a 4x4 matrix, adding the interpolated translation
		// to that matrix, then finally applying it to the current model view matrix in OpenGL.
		// This will then effect the next objects that are somehow explicitly or inexplicitly
		// connected and drawn from that joint.

		// Create some local variables to store all this crazy interpolation data
		CQuaternion qQuat				= new CQuaternion();
		CQuaternion qNextQuat			= new CQuaternion();
		CQuaternion qInterpolatedQuat	= new CQuaternion();
		float[] pMatrix;
		float[] pNextMatrix;
		float[] finalMatrix = new float[16];

		// Now we need to go through all of this models tags and draw them.
		for(int i = 0; i < pModel.numOfTags; i++)
		{
			// Get the current link from the models array of links (Pointers to models)
			C3DModel pLink = pModel.pLinks[i];

			// If this link has a valid address, let's draw it!
			if(pLink != null)
			{			
				// To find the current translation position for this frame of animation, we times
				// the currentFrame by the number of tags, then add i.  This is similar to how
				// the vertex key frames are interpolated.
				CVector3 vOldPosition = pModel.pTags[pModel.currentFrame * pModel.numOfTags + i].vPosition;

				// Grab the next key frame translation position
				CVector3 vNextPosition = pModel.pTags[pModel.nextFrame * pModel.numOfTags + i].vPosition;
			
				// By using the equation: p(t) = p0 + t(p1 - p0), with a time t,
				// we create a new translation position that is closer to the next key frame.
				CVector3 vPosition = new CVector3();
				vPosition.x = vOldPosition.x + pModel.t * (vNextPosition.x - vOldPosition.x);
				vPosition.y	= vOldPosition.y + pModel.t * (vNextPosition.y - vOldPosition.y);
				vPosition.z	= vOldPosition.z + pModel.t * (vNextPosition.z - vOldPosition.z);

				// Now comes the more complex interpolation.  Just like the translation, we
				// want to store the current and next key frame rotation matrix, then interpolate
				// between the 2.

				// Get a pointer to the start of the 3x3 rotation matrix for the current frame
				pMatrix = pModel.pTags[pModel.currentFrame * pModel.numOfTags + i].rotation;

				// Get a pointer to the start of the 3x3 rotation matrix for the next frame
				pNextMatrix = pModel.pTags[pModel.nextFrame * pModel.numOfTags + i].rotation;

				// Now that we have 2 1D arrays that store the matrices, let's interpolate them

				// Convert the current and next key frame 3x3 matrix into a quaternion
				qQuat.CreateFromMatrix( pMatrix, 3);
				qNextQuat.CreateFromMatrix( pNextMatrix, 3 );

				// Using spherical linear interpolation, we find the interpolated quaternion
				qInterpolatedQuat = qQuat.Slerp(qQuat, qNextQuat, pModel.t);

				// Here we convert the interpolated quaternion into a 4x4 matrix
				qInterpolatedQuat.CreateMatrix( finalMatrix );
				
				// To cut out the need for 2 matrix calls, we can just slip the translation
				// into the same matrix that holds the rotation.  That is what index 12-14 holds.
				finalMatrix[12] = vPosition.x;
				finalMatrix[13] = vPosition.y;
				finalMatrix[14] = vPosition.z;

				// Start a new matrix scope
				gl.glPushMatrix();
				
					// Finally, apply the rotation and translation matrix to the current matrix
					gl.glMultMatrixf( finalMatrix );

					// Recursively draw the next model that is linked to the current one.
					// This could either be a body part or a gun that is attached to
					// the hand of the upper body model.
					DrawLink(pLink);

				// End the current matrix scope
				gl.glPopMatrix();
			}
		}

	}

	///////////////////////////////// SET CURRENT TIME \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This sets time t for the interpolation between the current and next key frame
	/////
	///////////////////////////////// SET CURRENT TIME \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	void SetCurrentTime(C3DModel pModel)
	{
		// This function is very similar to finding the frames per second.
		// Instead of checking when we reach a second, we check if we reach
		// 1 second / our animation speed. (1000 ms / animationSpeed).
		// That's how we know when we need to switch to the next key frame.
		// In the process, we get the t value for how far we are at to going to the
		// next animation key frame.  We use time to do the interpolation, that way
		// it runs the same speed on any persons computer, regardless of their specs.
		// It might look choppier on a junky computer, but the key frames still be
		// changing the same time as the other persons, it will just be not as smooth
		// of a transition between each frame.  The more frames per second we get, the
		// smoother the animation will be.  Since we are working with multiple models 
		// we don't want to create static variables, so the t and elapsedTime data are 
		// stored in the model's structure.
		
		// Return if there is no animations in this model
		if(pModel.pAnimations.size() == 0) return;

		// Get the current time in milliseconds
		long time = System.currentTimeMillis();
		
		// Find the time that has elapsed since the last time that was stored
		long elapsedTime = time - pModel.lastTime;

		// Store the animation speed for this animation in a local variable
		int animationSpeed = ((CAnimationInfo)pModel.pAnimations.get(pModel.currentAnim)).framesPerSecond;

		// To find the current t we divide the elapsed time by the ratio of:
		//
		// (1_second / the_animation_frames_per_second)
		//
		// Since we are dealing with milliseconds, we need to use 1000
		// milliseconds instead of 1 because we are using GetTickCount(), which is in 
		// milliseconds. 1 second == 1000 milliseconds.  The t value is a value between 
		// 0 to 1.  It is used to tell us how far we are from the current key frame to 
		// the next key frame.
		float t = elapsedTime / (1000f / animationSpeed);

		// If our elapsed time goes over the desired time segment, start over and go 
		// to the next key frame.
		if ( elapsedTime >= (1000.0f / animationSpeed) )
		{
			// Set our current frame to the next key frame (which could be the start of the anim)
			pModel.currentFrame = pModel.nextFrame;

			// Set our last time for the model to the current time
			pModel.lastTime = time;
		}

		// Set the t for the model to be used in interpolation
		pModel.t = t;
	}

	///////////////////////////////// RENDER MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This renders the model data to the screen
	/////
	///////////////////////////////// RENDER MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	void RenderModel(C3DModel pModel)
	{
		// This function actually does the rendering to OpenGL.  If you have checked out
		// our other file loading tutorials, it looks pretty much the same as those.  I
		// left out the normals though.  You can go to any other loading and copy the code
		// from those.  Usually the Quake models creating the lighting effect in their textures
		// anyway.  

		// Make sure we have valid objects just in case. (size() is in the STL vector class)
		if(pModel.pObject == null) return;

		// Go through all of the objects stored in this model
		for(int i = 0; i < pModel.numOfObjects; i++)
		{
			// Get the current object that we are displaying
			C3DObject pObject = (C3DObject) pModel.pObject.get(i);

			// Now that we have animation for our model, we need to interpolate between
			// the vertex key frames.  The .md3 file format stores all of the vertex 
			// key frames in a 1D array.  This means that in order to go to the next key frame,
			// we need to follow this equation:  currentFrame * numberOfVertices
			// That will give us the index of the beginning of that key frame.  We just
			// add that index to the initial face index, when indexing into the vertex array.

			// Find the current starting index for the current key frame we are on
			int currentIndex = pModel.currentFrame * pObject.numOfVerts; 

			// Since we are interpolating, we also need the index for the next key frame
			int nextIndex = pModel.nextFrame * pObject.numOfVerts; 

			// If the object has a texture assigned to it, let's bind it to the model.
			// This isn't really necessary since all models have textures, but I left this
			// in here to keep to the same standard as the rest of the model loaders.
			if(pObject.bHasTexture)
			{
				// Turn on texture mapping
				gl.glEnable(gl.GL_TEXTURE_2D);

				// Grab the texture index from the materialID index into our material list
				int textureID = ((CMaterialInfo)pModel.pMaterials.get(pObject.materialID)).texureId;

				// Bind the texture index that we got from the material textureID
				gl.glBindTexture(gl.GL_TEXTURE_2D, m_Textures[textureID]);
			}
			else
			{
				// Turn off texture mapping
				gl.glDisable(gl.GL_TEXTURE_2D);
			}

			// Start drawing our model triangles
			gl.glBegin(gl.GL_TRIANGLES);

				// Go through all of the faces (polygons) of the object and draw them
				for(int j = 0; j < pObject.numOfFaces; j++)
				{
					// Go through each vertex of the triangle and draw it.
					for(int whichVertex = 0; whichVertex < 3; whichVertex++)
					{
						// Get the index for the current point in the face list
						int index = pObject.pFaces[j].vertIndex[whichVertex];
						
						gl.glNormal3f(-pObject.pNormals[ index ].x, -pObject.pNormals[ index ].y, -pObject.pNormals[ index ].z);
						
						// Make sure there is texture coordinates for this (%99.9 likelyhood)
						if(pObject.pTexVerts != null) 
						{
							// Assign the texture coordinate to this vertex
							gl.glTexCoord2f(pObject.pTexVerts[ index ].x, 
										 	pObject.pTexVerts[ index ].y);
						}
												
						// Like in the MD2 Animation tutorial, we use linear interpolation
						// between the current and next point to find the point in between,
						// depending on the model's "t" (0.0 to 1.0).

						// Store the current and next frame's vertex by adding the current
						// and next index to the initial index given from the face data.
						CVector3 vPoint1 = pObject.pVerts[ currentIndex + index ];
						CVector3 vPoint2 = pObject.pVerts[ nextIndex + index];

						// By using the equation: p(t) = p0 + t(p1 - p0), with a time t,
						// we create a new vertex that is closer to the next key frame.
						gl.glVertex3f(vPoint1.x + pModel.t * (vPoint2.x - vPoint1.x),
								      vPoint1.y + pModel.t * (vPoint2.y - vPoint1.y),
								      vPoint1.z + pModel.t * (vPoint2.z - vPoint1.z));
					}
				}

			// Stop drawing polygons
			gl.glEnd();
		}
	}

	///////////////////////////////// SET TORSO ANIMATION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This sets the current animation that the upper body will be performing
	/////
	///////////////////////////////// SET TORSO ANIMATION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void SetTorsoAnimation(String strAnimation)
	{
		// Go through all of the animations in this model
		for(int i = 0; i < m_Upper.numOfAnimations; i++)
		{
			// If the animation name passed in is the same as the current animation's name
			if( ((CAnimationInfo)m_Upper.pAnimations.get(i)).strName.equals(strAnimation) )
			{
				// Set the legs animation to the current animation we just found and return
				m_Upper.currentAnim = i;
				m_Upper.currentFrame = ((CAnimationInfo)m_Upper.pAnimations.get(m_Upper.currentAnim)).startFrame;
				return;
			}
		}
	}


	///////////////////////////////// SET LEGS ANIMATION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This sets the current animation that the lower body will be performing
	/////
	///////////////////////////////// SET LEGS ANIMATION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void SetLegsAnimation(String strAnimation)
	{
		// Go through all of the animations in this model
		for(int i = 0; i < m_Lower.numOfAnimations; i++)
		{
			// If the animation name passed in is the same as the current animation's name
			if( ((CAnimationInfo)m_Lower.pAnimations.get(i)).strName.equals(strAnimation) )
			{
				// Set the legs animation to the current animation we just found and return
				m_Lower.currentAnim = i;
				m_Lower.currentFrame = ((CAnimationInfo)m_Lower.pAnimations.get(m_Lower.currentAnim)).startFrame;
				return;
			}
		}
	}

	//////////////////////////  BELOW IS THE LOADER CLASS //////////////////////////////


	///////////////////////////////// IMPORT MD3 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This is called by the client to open the .Md3 file, read it, then clean up
	/////
	///////////////////////////////// IMPORT MD3 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public boolean ImportMD3(C3DModel pModel, String file)
		throws IOException
	{
		InputStream is = null;
		int fileSize = 0;
		File f;
		BufferedInputStream in;
		try
		{
			f = new File(file);  
			is = new FileInputStream(f);  
			fileSize = (int)f.length(); 
		}
		catch (Exception e)
		{
			System.out.println("Failed to connect to ");
			e.printStackTrace();
			return false;
		}
		// wrap a buffer to make reading more efficient (faster)
		BufferedInputStream bis = new BufferedInputStream(is);

		fileContents = new byte[fileSize];

		// Read the entire file into memory
		bis.read(fileContents, 0, fileSize);

		// Close the .md3 file that we opened
		bis.close();

		// Open the MD3 file in binary
		m_FilePointer = 0;
		
		// Now that we know the file was found and it's all cool, let's read in
		// the header of the file.  If it has the correct 4 character ID and version number,
		// we can continue to load the rest of the data, otherwise we need to print an error.

		// Read the header data and store it in our m_Header member variable
		m_Header = new tMd3Header();

		// Get the 4 character ID
		String ID = m_Header.fileID;

		// The ID MUST equal "IDP3" and the version MUST be 15, or else it isn't a valid
		// .MD3 file.  This is just the numbers ID Software chose.

		// Make sure the ID == IDP3 and the version is this crazy number '15' or else it's a bad egg
		if(!ID.equals("IDP3") || m_Header.version != 15)
		{
			// Display an error message for bad file format, then stop loading
			System.err.println("Invalid file format (Version not 15): " + file + "!");
			return false;
		}
		
		// Read in the model and animation data
		ReadMD3Data(pModel);

		// Return a success
		return true;
	}

	///////////////////////////////// READ MD3 DATA \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function reads in all of the model's data, except the animation frames
	/////
	///////////////////////////////// READ MD3 DATA \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	void ReadMD3Data(C3DModel pModel)
	{
		int i = 0;

		// This member function is the BEEF of our whole file.  This is where the
		// main data is loaded.  The frustrating part is that once the data is loaded,
		// you need to do a billion little things just to get the model loaded to the screen
		// in a correct manner.

		// Here we allocate memory for the bone information and read the bones in.
		m_pBones = new tMd3Bone [m_Header.numFrames];
		for (i = 0; i < m_Header.numFrames ; i++)
		{
			m_pBones[i] = new tMd3Bone();
		}

		// Since we don't care about the bone positions, we just free it immediately.
		// It might be cool to display them so you could get a visual of them with the model.

		// Free the unused bones
		m_pBones = null;

		// Next, after the bones are read in, we need to read in the tags.  Below we allocate
		// memory for the tags and then read them in.  For every frame of animation there is
		// an array of tags.
		pModel.pTags = new tMd3Tag [m_Header.numFrames * m_Header.numTags];
		for (i = 0 ; i < m_Header.numFrames * m_Header.numTags ; i++)
		{
			pModel.pTags[i] = new tMd3Tag();
		}

		// Assign the number of tags to our model
		pModel.numOfTags = m_Header.numTags;
		
		// Now we want to initialize our links.  Links are not read in from the .MD3 file, so
		// we need to create them all ourselves.  We use a double array so that we can have an
		// array of pointers.  We don't want to store any information, just pointers to C3DModels.
		pModel.pLinks = new C3DModel[m_Header.numTags];
		
		// Initilialize our link pointers to NULL
		for (i = 0; i < m_Header.numTags; i++)
			pModel.pLinks[i] = null;

		// Now comes the loading of the mesh data.  We want to use ftell() to get the current
		// position in the file.  This is then used to seek to the starting position of each of
		// the mesh data arrays.

		// Get the current offset into the file
		int meshOffset = m_FilePointer;

		// Create a local meshHeader that stores the info about the mesh
		tMd3MeshInfo meshHeader = new tMd3MeshInfo();

		// Go through all of the sub-objects in this mesh
		for (int j = 0; j < m_Header.numMeshes; j++)
		{
			// Seek to the start of this mesh and read in it's header
			m_FilePointer = meshOffset;
			meshHeader = new tMd3MeshInfo();

			// Here we allocate all of our memory from the header's information
			m_pSkins     = new tMd3Skin [meshHeader.numSkins];
			m_pTexCoords = new tMd3TexCoord [meshHeader.numVertices];
			m_pTriangles = new tMd3Face [meshHeader.numTriangles];
			m_pVertices  = new tMd3Triangle [meshHeader.numVertices * meshHeader.numMeshFrames];

			// Read in the skin information
			for (i = 0; i < meshHeader.numSkins ; i++)
			{
				m_pSkins[i] = new tMd3Skin();
			}
			
			// Seek to the start of the triangle/face data, then read it in
			m_FilePointer = meshOffset + meshHeader.triStart;
			for (i = 0; i < meshHeader.numTriangles ; i++)
			{
				m_pTriangles[i] = new tMd3Face();
			}

			// Seek to the start of the UV coordinate data, then read it in
			m_FilePointer = meshOffset + meshHeader.uvStart;
			for (i = 0; i < meshHeader.numVertices ; i++)
			{
				m_pTexCoords[i] = new tMd3TexCoord();
			}

			// Seek to the start of the vertex/face index information, then read it in.
			m_FilePointer = meshOffset + meshHeader.vertexStart;
			for (i = 0; i < meshHeader.numMeshFrames * meshHeader.numVertices ; i++)
			{
				m_pVertices[i] = new tMd3Triangle();
			}

			// Now that we have the data loaded into the Quake3 structures, let's convert them to
			// our data types like C3DModel and C3DObject.  That way the rest of our model loading
			// code will be mostly the same as the other model loading tutorials.
			ConvertDataStructures(pModel, meshHeader);
			
			// Computa le normali del modello
			ComputeNormals(pModel);
			
			// Free all the memory for this mesh since we just converted it to our structures
			m_pSkins = null;    
			m_pTexCoords = null;
			m_pTriangles = null;
			m_pVertices = null;   

			// Increase the offset into the file
			meshOffset += meshHeader.meshSize;
		}
	}

	///////////////////////////////// CONVERT DATA STRUCTURES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function converts the .md3 structures to our own model and object structures
	/////
	///////////////////////////////// CONVERT DATA STRUCTURES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	void ConvertDataStructures(C3DModel pModel, tMd3MeshInfo meshHeader)
	{
		int i = 0;

		// This is function takes care of converting all of the Quake3 structures to our
		// structures that we have been using in all of our mode loading tutorials.  You
		// do not need this function if you are going to be using the Quake3 structures.
		// I just wanted to make it modular with the rest of the tutorials so you (me really) 
		// can make a engine out of them with an abstract base class.  Of course, each model
		// has some different data variables inside of the, depending on each format, but that
		// is perfect for some cool inheritance.  Just like in the .MD2 tutorials, we only
		// need to load in the texture coordinates and face information for one frame
		// of the animation (eventually in the next tutorial).  Where, the vertex information
		// needs to be loaded for every new frame, since it's vertex key frame animation 
		// used in .MD3 models.  Half-life models do NOT do this I believe.  It's just
		// pure bone/skeletal animation.  That will be a cool tutorial if the time ever comes.

		// Increase the number of objects (sub-objects) in our model since we are loading a new one
		pModel.numOfObjects++;
			
		// Create a empty object structure to store the object's info before we add it to our list
		C3DObject currentMesh = new C3DObject();

		// Copy the name of the object to our object structure
		currentMesh.strName = meshHeader.strName;
		//debug(".. .. .. name = " + meshHeader.strName);

		// Assign the vertex, texture coord and face count to our new structure
		currentMesh.numOfVerts   = meshHeader.numVertices;
		currentMesh.numTexVertex = meshHeader.numVertices;
		currentMesh.numOfFaces   = meshHeader.numTriangles;

		// Allocate memory for the vertices, texture coordinates and face data.
		// Notice that we multiply the number of vertices to be allocated by the
		// number of frames in the mesh.  This is because each frame of animation has a 
		// totally new set of vertices.  This will be used in the next animation tutorial.
		currentMesh.pVerts    = new CVector3[currentMesh.numOfVerts * meshHeader.numMeshFrames];
		currentMesh.pTexVerts = new CVector2[currentMesh.numOfVerts];
		currentMesh.pFaces    = new CFace[currentMesh.numOfFaces];

		// Go through all of the vertices and assign them over to our structure
		for (i=0; i < currentMesh.numOfVerts * meshHeader.numMeshFrames; i++)
		{
			// For some reason, the ratio 64 is what we need to divide the vertices by,
			// otherwise the model is gargantuanly huge!  If you use another ratio, it
			// screws up the model's body part position.  I found this out by just
			// testing different numbers, and I came up with 65.  I looked at someone
			// else's code and noticed they had 64, so I changed it to that.  I have never
			// read any documentation on the model format that justifies this number, but
			// I can't get it to work without it.  Who knows....  Maybe it's different for
			// 3D Studio Max files verses other software?  You be the judge.  I just work here.. :)
			currentMesh.pVerts[i] = new CVector3();
			currentMesh.pVerts[i].x =  m_pVertices[i].vertex[0] / 64.0f;
			currentMesh.pVerts[i].y =  m_pVertices[i].vertex[1] / 64.0f;
			currentMesh.pVerts[i].z =  m_pVertices[i].vertex[2] / 64.0f;
		}

		// Go through all of the uv coords and assign them over to our structure
		for (i=0; i < currentMesh.numTexVertex; i++)
		{
			// Since I changed the images to bitmaps, we need to negate the V ( or y) value.
			// This is because I believe that TARGA (.tga) files, which were originally used
			// with this model, have the pixels flipped horizontally.  If you use other image
			// files and your texture mapping is crazy looking, try deleting this negative.
			currentMesh.pTexVerts[i] = new CVector2();
			currentMesh.pTexVerts[i].x =  m_pTexCoords[i].textureCoord[0];
			currentMesh.pTexVerts[i].y = -m_pTexCoords[i].textureCoord[1];
		}

		// Go through all of the face data and assign it over to OUR structure
		for(i=0; i < currentMesh.numOfFaces; i++)
		{
			// Assign the vertex indices to our face data
			currentMesh.pFaces[i] = new CFace();
			currentMesh.pFaces[i].vertIndex[0] = m_pTriangles[i].vertexIndices[0];
			currentMesh.pFaces[i].vertIndex[1] = m_pTriangles[i].vertexIndices[1];
			currentMesh.pFaces[i].vertIndex[2] = m_pTriangles[i].vertexIndices[2];

			// Assign the texture coord indices to our face data (same as the vertex indices)
			currentMesh.pFaces[i].coordIndex[0] = m_pTriangles[i].vertexIndices[0];
			currentMesh.pFaces[i].coordIndex[1] = m_pTriangles[i].vertexIndices[1];
			currentMesh.pFaces[i].coordIndex[2] = m_pTriangles[i].vertexIndices[2];
		}

		// Here we add the current object to our list object list
		pModel.pObject.add(currentMesh);
	}
	
	///////////////////////////////// COMPUTER NORMALS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function computes the normals and vertex normals of the objects
	/////
	///////////////////////////////// COMPUTER NORMALS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	void ComputeNormals(C3DModel pModel)
	{
		CVector3 vVector1 = new CVector3();
		CVector3 vVector2 = new CVector3();
		CVector3 vNormal = new CVector3();
		CVector3[] vPoly = new CVector3[3];

		// If there are no objects, we can skip this part
		if(pModel.numOfObjects <= 0)
			return;

		// What are vertex normals?  And how are they different from other normals?
		// Well, if you find the normal to a triangle, you are finding a "Face Normal".
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

				vVector1 = CMath3d.Vector(vPoly[0], vPoly[2]);		// Get the vector of the polygon (we just need 2 sides for the normal)
				vVector2 = CMath3d.Vector(vPoly[2], vPoly[1]);		// Get a second vector of the polygon

				vNormal  = CMath3d.Cross(vVector1, vVector2);		// Return the cross product of the 2 vectors (normalize vector, but not a unit vector)
				pTempNormals[i] = vNormal;							// Save the un-normalized normal for the vertex normals
				vNormal  = CMath3d.Normalize(vNormal);				// Normalize the cross product to give us the polygons normal

				pNormals[i] = vNormal;								// Assign the normal to the list of normals
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


	///////////////////////////////// LOAD SKIN \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This loads the texture information for the model from the *.skin file
	/////
	///////////////////////////////// LOAD SKIN \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	boolean LoadSkin(C3DModel pModel, String strSkin)
		throws IOException
	{
		// Make sure valid data was passed in
		if(pModel == null || strSkin == null) return false;

		// This function is used to load a .skin file for the .md3 model associated
		// with it.  The .skin file stores the textures that need to go with each
		// object and subject in the .md3 files.  For instance, in our Lara Croft model,
		// her upper body model links to 2 texture; one for her body and the other for
		// her face/head.  The .skin file for the lara_upper.md3 model has 2 textures:
		//
		// u_torso,models/players/xxx/default.bmp
		// u_head,models/players/xxx/default_h.bmp
		//
		// Notice the first word, then a comma.  This word is the name of the object
		// in the .md3 file.  Remember, each .md3 file can have many sub-objects.
		// The next bit of text is the Quake3 path into the .pk3 file where the 
		// texture for that model is stored  Since we don't use the Quake3 path
		// because we aren't making Quake, I just grab the texture name at the
		// end of the string and disregard the rest.  of course, later this is
		// concatenated to the original MODEL_PATH that we passed into load our character.
		// So, for the torso object it's clear that default.bmp is assigned to it, where
		// as the head model with the pony tail, is assigned to default_h.bmp.  Simple enough.
		// What this function does is go through all the lines of the .skin file, and then
		// goes through all of the sub-objects in the .md3 file to see if their name is
		// in that line as a sub string.  We use our cool IsInString() function for that.
		// If it IS in that line, then we know that we need to grab it's texture file at
		// the end of the line.  I just parse backwards until I find the last '/' character,
		// then copy all the characters from that index + 1 on (I.E. "default.bmp").
		// Remember, it's important to note that I changed the texture files from .tga
		// files to .bmp files because that is what all of our tutorials use.  That way
		// you don't have to sift through tons of image loading code.  You can write or
		// get your own if you really want to use the .tga format.

		// Open the skin file
		InputStream is = null;
		int fileSize = 0;
		File f;
		BufferedInputStream in;
		try
		{
			f = new File(strSkin);  
			is = new FileInputStream(f);    
		}
		catch (Exception e)
		{
			System.out.println("Failed to connect to ");
			e.printStackTrace();
			return false;
		}

		// wrap a buffer to make reading more efficient (faster)
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		// These 2 variables are for reading in each line from the file, then storing
		// the index of where the bitmap name starts after the last '/' character.
		String strLine;
		int textureNameStart = 0;

		// Go through every line in the .skin file
		while ((strLine = reader.readLine()) != null)
		{
			// Loop through all of our objects to test if their name is in this line
			for(int i = 0; i < pModel.numOfObjects; i++)
			{
				// Check if the name of this object appears in this line from the skin file
				if( IsInString(strLine, ((C3DObject)pModel.pObject.get(i)).strName) )			
				{			
					// To extract the texture name, we loop through the string, starting
					// at the end of it until we find a '/' character, then save that index + 1.
					textureNameStart = strLine.lastIndexOf("/") + 1;

					// Create a local material info structure
					CMaterialInfo texture = new CMaterialInfo();

					// Copy the name of the file into our texture file name variable.
					// Notice that with string we can pass in the address of an index
					// and it will only pass in the characters from that point on. Cool huh?
					// So now the strFile name should hold something like ("bitmap_name.bmp")
					texture.strFile = strLine.substring(textureNameStart);
					
					// The tile or scale for the UV's is 1 to 1 
					texture.uTile = texture.uTile = 1;

					// Store the material ID for this object and set the texture boolean to true
					((C3DObject)pModel.pObject.get(i)).materialID = pModel.numOfMaterials;
					((C3DObject)pModel.pObject.get(i)).bHasTexture = true;

					// Here we increase the number of materials for the model
					pModel.numOfMaterials++;

					// Add the local material info structure to our model's material list
					pModel.pMaterials.add(texture);
				}
			}
		}

		// Close the file and return a success
		reader.close();
		return true;
	}

	///////////////////////////////// LOAD SHADER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This loads the basic shader texture info associated with the weapon model
	/////
	///////////////////////////////// LOAD SHADER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	boolean LoadShader(C3DModel pModel, String strShader)
		throws IOException
	{
		// Make sure valid data was passed in
		if(pModel == null || strShader == null) return false;

		// This function is used to load the .shader file that is associated with
		// the weapon model.  Instead of having a .skin file, weapons use a .shader file
		// because it has it's own scripting language to describe the behavior of the
		// weapon.  There is also many other factors like environment map and sphere map
		// textures, among other things.  Since I am not trying to replicate Quake, I
		// just care about the weapon's texture.  I went through each of the blocks
		// in the shader file and deleted everything except the texture name (of course
		// I changed the .tga files to .bmp for our purposes).  All this file now includes
		// is a texture name on each line.  No parsing needs to be done.  It is important
		// to keep in mind that the order of which these texture are stored in the file
		// is in the same order each sub-object is loaded in the .md3 file.  For instance,
		// the first texture name on the first line of the shader is the texture for
		// the main gun object that is loaded, the second texture is for the second sub-object
		// loaded, and so on. I just want to make sure that you understand that I hacked
		// up the .shader file so I didn't have to parse through a whole language.  This is
		// not a normal .shader file that we are loading.  I only kept the relevant parts.

		// Open the shader file
		InputStream is = null;
		int fileSize = 0;
		File f;
		BufferedInputStream in;
		try
		{
			f = new File(strShader);  
			is = new FileInputStream(f);    
		}
		catch (Exception e)
		{
			System.out.println("Failed to connect to ");
			e.printStackTrace();
			return false;
		}
		// wrap a buffer to make reading more efficient (faster)
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		// These variables are used to read in a line at a time from the file, and also
		// to store the current line being read so that we can use that as an index for the 
		// textures, in relation to the index of the sub-object loaded in from the weapon model.
		String strLine;
		int currentIndex = 0;
		
		// Go through and read in every line of text from the file
		while ((strLine = reader.readLine()) != null)
		{
			// Create a local material info structure
			CMaterialInfo texture = new CMaterialInfo();

			// Copy the name of the file into our texture file name variable
			texture.strFile = strLine;
					
			// The tile or scale for the UV's is 1 to 1 
			texture.uTile = texture.uTile = 1;

			// Store the material ID for this object and set the texture boolean to true
			((C3DObject)pModel.pObject.get(currentIndex)).materialID = pModel.numOfMaterials;
			((C3DObject)pModel.pObject.get(currentIndex)).bHasTexture = true;

			// Here we increase the number of materials for the model
			pModel.numOfMaterials++;

			// Add the local material info structure to our model's material list
			pModel.pMaterials.add(texture);

			// Here we increase the material index for the next texture (if any)
			currentIndex++;
		}

		// Close the file and return a success
		reader.close();
		return true;
	}

	void CreateTexture(int[] textureArray, String strFileName, int textureID)
	{
		int[] tempArray = new int[1];
		gl.glGenTextures(1, tempArray);
		textureArray[textureID] = tempArray[0];

		BmpTextureLoader texLoader = new BmpTextureLoader(gl, glu);
		texLoader.readTexture(strFileName);
		if (texLoader.isOk())
		{
			// This sets the alignment requirements for the start of each pixel row in memory.
			gl.glPixelStorei (gl.GL_UNPACK_ALIGNMENT, 1);

			gl.glBindTexture(gl.GL_TEXTURE_2D, textureArray[textureID]);

			//Assign the mip map levels and texture info
			gl.glTexParameteri(gl.GL_TEXTURE_2D,gl.GL_TEXTURE_MIN_FILTER,gl.GL_LINEAR_MIPMAP_NEAREST);
			gl.glTexParameteri(gl.GL_TEXTURE_2D,gl.GL_TEXTURE_MAG_FILTER,gl.GL_LINEAR_MIPMAP_LINEAR);

			glu.gluBuild2DMipmaps(gl.GL_TEXTURE_2D, texLoader.getComponents(), texLoader.getImageWidth(), texLoader.getImageHeight(), texLoader.getGLFormat(), gl.GL_UNSIGNED_BYTE, texLoader.getTexture());	
		}
	}
}
