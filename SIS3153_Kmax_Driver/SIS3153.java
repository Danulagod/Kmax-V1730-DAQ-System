/************************************Author Information**************************************
				
				     Danula Godagama 
			    	 danula.godagama@uky.edu 
			        University of Kentucky 
				       03/19/2020 

**************************************File Information***************************************
				       SIS3153.java
Source for Java driver for STRUCK SIS3153 VME controller. Written based on KJavaDriver example 
provided with Kmax.
*********************************************************************************************/


import java.io.*;
import java.util.Vector;
import java.util.Arrays;
import kmax.ext.KmaxDriver;
import java.lang.NumberFormatException;



public final class SIS3153 implements KmaxDriver {

	static boolean foundJNILib = false;
	static String _nativeFile;
    	static String dirName;

	static {
		// -----------------------------------------------------------------------------------
		// The JNI library may be in several different places.  In general it should be in the
		// same directory as the jar driver file that calls the native routines.  So we look
		// there first.  If it is not there we look elsewhere.

		System.out.println("JAVA_STATIC:-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_" );

		
		// -- Test code to find appropriate directories - remove in final versions
		System.out.println("JAVA_STATIC: Current 'usr.dir' is :: " +  System.getProperty("user.dir")  );
		System.out.println("JAVA_STATIC: Current 'usr.home' is :: " +  System.getProperty("user.home")  );
		System.out.println("JAVA_STATIC: The java.library.path is : " + System.getProperty( "java.library.path" ) );
		//String path = KJNIDriver.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		//System.out.println("JAVA_STATIC: Current jar execution path :: " + path);	
		
		
		//String path = getClass().getResource("").getPath();
		//System.out.println ( new File(KJNIDriver.class.getProtectionDomain().getCodeSource().getLocation().getPath()));
		
		try {
			// First we try the loadLibrary default.  
			System.loadLibrary ("SIS3153");
			foundJNILib = true;
			System.out.println ("JAVA_STATIC: Found the SIS3153 native library with 'loadLibrary()'.");
			System.out.println("JAVA_STATIC:-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_" );

		}
		catch (UnsatisfiedLinkError err) {
			foundJNILib = false; // We will look elsewhare
			System.out.println ("JAVA_STATIC: Could not find SIS3153 native library with 'loadLibrary()'");

		}
		catch (Exception e) {
			// We encountered a fatal exception
			System.out.println("Java.static: Exception while loading the jni library with loadLibrary() " + e.getMessage());
			System.exit(0);
		}
		
		if (!foundJNILib) {

			System.out.println ("JAVA_STATIC: Will try platform specific locations......");
			
			
			// The library was not found in the default path so we will look elsewhere
			// To be platform generic, we have to identify the current OS and version.
			// This way we can maintain one source for all three standard platforms.
			// We do it here so we will know where to find the appropriate dynamic library.
			String lcOSName = System.getProperty("os.name").toLowerCase();
			System.out.println("JAVA.STATIC: Current OS name is :: " + lcOSName);
			
			String currentVersion = System.getProperty("os.version");
			System.out.println("JAVA.STATIC: Current OS version is :: " + currentVersion);
					
			
			
			if (currentVersion.length() > 4) {
				currentVersion = currentVersion.substring(0,4);
			}
			
			if (lcOSName.startsWith("mac os x")) {
				if (currentVersion.compareTo("10.6") < 0) {
					System.out.println("JAVA.STATIC: Current OS version is not supported :: " + currentVersion);
					System.exit(0); 
				}
				dirName = "MacOSX";
			}
			else if (lcOSName.startsWith("linux")) {
				if (currentVersion.compareTo("2.6") < 0) {
					System.out.println("JAVA.STATIC: Current OS version is not supported :: " + currentVersion);
					System.exit(0);
				}
				
				String archName = System.getProperty("os.arch");
				if (archName.equals("i386")) {
					archName = "x86";
				}
				
				dirName = "Linux-" + archName;
			}
			else if (lcOSName.startsWith("windows")) {
				
				if (currentVersion.compareTo("6.0") < 0) {
					System.out.println("Java.static: Current OS version is not supported :: " + currentVersion);
					System.exit(0);
				}
				
				String archName = System.getProperty("os.arch");
				if (archName.equals("i386")) {
					archName = "x86";
				}
				
				dirName = "Windows-" + archName;
				
			}
			else {
				System.out.println("Java.static: Current OS is not supported :: " + lcOSName);
			}
			
			
			System.out.println("JAVA_STATIC: Found valid OS and version :: " + lcOSName + " :: " + currentVersion);

			
			// -----------------------------------------------------------------------------------
			// Now that we have the platform-specific directory, we try to load the native library
			
			// We load here (Kmax toolaheet mode) if the local mode failed to load
			System.out.println("Java.static: Could not load the native library for debugging access.");
			System.out.println("Java.static: Attempting to load the library with for Kmax toolsheet access...");
			try {
				
				// see if we are running from Kmax
				_nativeFile = (System.getProperty("user.dir") + File.separator + 
							   "Extensions" + File.separator +
							   dirName + File.separator + 
							   System.mapLibraryName ("SIS3153")); 
				System.out.println("JAVA_STATIC: Loading nativeFile: " + _nativeFile);
				System.load (_nativeFile);
				
				foundJNILib = true;
				
				System.out.println ("JAVA_STATIC: The SIS3153 native library loaded as a Kmax extension.");
				System.out.println("JAVA_STATIC:-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_" );
				
			}catch (UnsatisfiedLinkError error){
				foundJNILib = false;
				System.out.println ("JAVA_STATIC: Kmax could not load the SIS3153 native library....");
				System.out.println("JAVA_STATIC:-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_" );

				//System.exit(0); 
				
			}
			catch (Exception e) {
				foundJNILib = false;
				System.out.println("JAVA_STATIC: Exception while loading the jni library with Kmax -load(), " + e.getMessage());
				//System.exit(0);
			}
		} // else
	}//static

	static String versionStr = "Version 1.0";

	public static final int DRV_ERR_NO_ERROR = 0;
	public static final int DRV_ERR_NULL_ARRAY = 1001;
	public static final int DRV_ERR_BAD_ELEMENT_COUNT = 1002;
	public static final int DRV_ERR_BAD_ELEMENT_OFFSET = 1003;
	public static final int DRV_ERR_NO_SUCH_ADDRESS = 1004;
	public static final int DRV_ERR_MEMORY_ERROR = 1005;
	public static final int DRV_ERR_METHOD_UNIMPLEMENTED = 1007;
	public static final int DRV_ERR_NO_SUCH_FUNCTION = 1008;
	public static final int DRV_ERR_BAD_BUFFER_SIZE = 1009;
	public static final int DRV_ERR_JNI_ERROR = 1010;


	public SIS3153(){

	}


	public int init (int mode, int size, int busid, int deviceid, int slotid, long polladdr, int pollmod) {
	        
		if(!foundJNILib){
			return DRV_ERR_JNI_ERROR;
		}

		return nativeInit();
	       
	}

	public native int nativeInit();
	
	public native int nativeClose();

	public int poll (int[] pollData) {
		
		pollData[0]=0x0;
        	return 0;
        
    	} 


    	public int readByte (long base, long addr, int func, byte[] buf, int offset, int count) {
		return DRV_ERR_METHOD_UNIMPLEMENTED;
	} 

    	public int writeByte (long base, long addr, int func, byte[] buf, int offset, int count) {
		return DRV_ERR_METHOD_UNIMPLEMENTED;
	} 
	
	
    	public int readShort (long base, long addr, int func, short[] buf, int offset, int count) {
		return DRV_ERR_METHOD_UNIMPLEMENTED;
	}

    	public int writeShort (long base, long addr, int func, short[] buf, int offset, int count) {
		return DRV_ERR_METHOD_UNIMPLEMENTED;
	} 
    

	public int readInt (long base, long addr, int func, int[] buf, int offset, int count) {

		
		if (count < 1){ return DRV_ERR_BAD_ELEMENT_COUNT;}
		if (offset < 0){ return DRV_ERR_BAD_ELEMENT_OFFSET;}
		if (buf.length < offset + count){ return DRV_ERR_BAD_ELEMENT_COUNT;}
		
		//System.out.println(Long.toHexString((base&0xFFFFFFFF)|(addr&0xFFFFFFFF)));
		return nativeReadInt((base&0xFFFFFFFF)|(addr&0xFFFFFFFF),func,buf,offset,count);
		
	} 

	public native int nativeReadInt(long addr, int func, int[] buf, int offset, int count);

    	public int writeInt (long base, long addr, int func, int[] buf, int offset, int count) {

		if (count < 1) return DRV_ERR_BAD_ELEMENT_COUNT;
		if (offset < 0) return DRV_ERR_BAD_ELEMENT_OFFSET;
		if (buf.length < offset + count) return DRV_ERR_BAD_ELEMENT_COUNT;
		
		return nativewriteInt((base&0xFFFFFFFF)|(addr&0xFFFFFFFF),func,buf,offset,count);
	} 

	public native int nativewriteInt(long addr, int func, int[] buf, int offset, int count);

	 public int readLong (long base, long addr, int func, long[] buf, int offset, int count) {
		return DRV_ERR_METHOD_UNIMPLEMENTED;
	} 
	
	
	public int writeLong (long base, long addr, int func, long[] buf, int offset, int count) {
		return DRV_ERR_METHOD_UNIMPLEMENTED;
	} 
    
    	public int readFloat (long base, long addr, int func, float[] buf, int offset, int count) {
		return DRV_ERR_METHOD_UNIMPLEMENTED;
    	} 
    
    	public int writeFloat (long base, long addr, int func, float[] buf, int offset, int count) {
		return DRV_ERR_METHOD_UNIMPLEMENTED;
    	} 
	
       	public int readDouble (long base, long addr, int func, double[] buf, int offset, int count) {
		return DRV_ERR_METHOD_UNIMPLEMENTED;
	}
    
    	public int writeDouble (long base, long addr, int func, double[] buf, int offset, int count) {
		return DRV_ERR_METHOD_UNIMPLEMENTED;
	} 
	
    	public int readStrBuf (long base, long addr, int func, StringBuffer buf, int offset, int count) {

	       return DRV_ERR_METHOD_UNIMPLEMENTED;
	} 

	public int writeStrBuf (long base, long addr, int func, StringBuffer buf, int offset, int count) {
		return DRV_ERR_METHOD_UNIMPLEMENTED;
	} 


	public String getErrorMessage (int errNum) {
		
        switch (errNum ) {
            case DRV_ERR_NO_ERROR:
                return "No error.";
            case DRV_ERR_JNI_ERROR:
				return "The JNI library could not be loaded.";
			case DRV_ERR_METHOD_UNIMPLEMENTED:
                return "The driver method is unemplemented.";
            case DRV_ERR_BAD_BUFFER_SIZE:
				return "The data buffer is not of the correct size.";
			case DRV_ERR_NO_SUCH_ADDRESS:
                return "There is no such address in this device.";
            case DRV_ERR_NO_SUCH_FUNCTION:
                return "There is no such function in this device at this address.";
            case DRV_ERR_BAD_ELEMENT_OFFSET:
                return "There is no such element in this driver buffer.";
            case DRV_ERR_BAD_ELEMENT_COUNT:
                return "Illegal count for this driver or IO buffer.";
            case DRV_ERR_NULL_ARRAY:
				 return "The array is null.";
            case DRV_ERR_MEMORY_ERROR:
                return "A memory error occured in the dirver.";
	    case 1011:
		return "USB BUS access error.";
	    case 1012:
		return "USB device open error, check usb permissions.";
            case 1013:
		return "No devices found.";
	    case 1014:
		return "Error reading from the opend device";
	    
            default:
                return "Unknown error in SIS3153 Driver.";
        }            
    }
	

    public static void main(String[] args){
	SIS3153 Dev= new SIS3153();
	int ret=Dev.init (0, 1, 0, 0, 0, 0, 0);
	int data[]={0xFF};
	//int ret=Dev.readInt(0x0B000000, 0x8124, 0x09,data, 0, 1);
	System.out.println ("\nreturn value:"+Integer.toString(ret));
	
    }

}
