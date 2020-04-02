/************************************Author Information**************************************
				
				     Danula Godagama 
			    	 danula.godagama@uky.edu 
			        University of Kentucky 
				       03/19/2020 

**************************************File Information***************************************
				       SIS3153.h
		Header of the native driver for STRUCK SIS3153 VME controller. 
*********************************************************************************************/
#include <jni.h>
#include <sis3150usb_vme.h>
#ifndef _Included_SIS3153
#define _Included_SIS3153
#ifdef __cplusplus
extern "C" {
#endif
#undef SIS3153_DRV_ERR_NO_ERROR
#define SIS3153_DRV_ERR_NO_ERROR 0L
#undef SIS3153_DRV_ERR_NULL_ARRAY
#define SIS3153_DRV_ERR_NULL_ARRAY 1001L
#undef SIS3153_DRV_ERR_BAD_ELEMENT_COUNT
#define SIS3153_DRV_ERR_BAD_ELEMENT_COUNT 1002L
#undef SIS3153_DRV_ERR_BAD_ELEMENT_OFFSET
#define SIS3153_DRV_ERR_BAD_ELEMENT_OFFSET 1003L
#undef SIS3153_DRV_ERR_NO_SUCH_ADDRESS
#define SIS3153_DRV_ERR_NO_SUCH_ADDRESS 1004L
#undef SIS3153_DRV_ERR_MEMORY_ERROR
#define SIS3153_DRV_ERR_MEMORY_ERROR 1005L
#undef SIS3153_DRV_ERR_METHOD_UNIMPLEMENTED
#define SIS3153_DRV_ERR_METHOD_UNIMPLEMENTED 1007L
#undef SIS3153_DRV_ERR_NO_SUCH_FUNCTION
#define SIS3153_DRV_ERR_NO_SUCH_FUNCTION 1008L
#undef SIS3153_DRV_ERR_BAD_BUFFER_SIZE
#define SIS3153_DRV_ERR_BAD_BUFFER_SIZE 1009L
#undef SIS3153_DRV_ERR_JNI_ERROR
#define SIS3153_DRV_ERR_JNI_ERROR 1010L



static struct SIS3150USB_Device_Struct devs[4];
unsigned static int found;
static bool IsOpen=false;


/*
 * Class:     SIS3153
 * Method:    nativeInit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_SIS3153_nativeInit
  (JNIEnv *env, jobject ob);

JNIEXPORT jint JNICALL Java_SIS3153_nativeClose(JNIEnv *env, jobject ob);


/*
 * Class:     SIS3153
 * Method:    nativeReadInt
 * Signature: (JI[III)I
 */
JNIEXPORT jint JNICALL Java_SIS3153_nativeReadInt
  (JNIEnv *env, jobject ob, jlong addr, jint func, jintArray buf, jint offset, jint count);

/*
 * Class:     SIS3153
 * Method:    nativewriteInt
 * Signature: (JI[III)I
 */
JNIEXPORT jint JNICALL Java_SIS3153_nativewriteInt
  (JNIEnv *env, jobject ob, jlong addr, jint func, jintArray buf, jint offset, jint count);

#ifdef __cplusplus
}
#endif
#endif
