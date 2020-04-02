/************************************Author Information**************************************
				
				     Danula Godagama 
			    	 danula.godagama@uky.edu 
			        University of Kentucky 
				       03/19/2020 

**************************************File Information***************************************
				       SIS3153.h
Source of the native driver for STRUCK SIS3153 VME controller. Written to facilitate native 
functions of SIS3153 Java driver.
*********************************************************************************************/
#include <SIS3153.h>
#include <sis3150usb_vme.h>
#include <sis3150usb_vme_calls.h>
#include <stdio.h>

/*
 * Class:     SIS3153
 * Method:    nativeInit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_SIS3153_nativeInit(JNIEnv *env, jobject ob){
	
	int ret=0;
	unsigned int data=0x0;

	if(IsOpen){
		
		//reading from the module firmware register 
		if(sis3150Usb_Register_Single_Read(devs[0].hDev,0x1,&data)==0x0){
			return 0;//initialization successful
		}
	}

	ret=FindAll_SIS3150USB_Devices(devs, &found, 4);
	
	if(ret != 0){
           return 1011;
    	}

	if(found > 0){
		
		ret= Sis3150usb_OpenDriver((PCHAR)devs[0].cDName, &devs[0]);
		if(ret != 0){
           		return 1012; //error opening the device, no sufficient usb permissions 
    		}
		IsOpen=true;
		return 0; //initialization successful
		

		
	}else{
		return 1013; //No devices found 
	}


}

JNIEXPORT jint JNICALL Java_SIS3153_nativeClose(JNIEnv *env, jobject ob){
	return Sis3150usb_CloseDriver(devs[0].hDev);
}

/*
 * Class:     SIS3153
 * Method:    nativeReadInt
 * Signature: (J[I)I
 */
JNIEXPORT jint JNICALL Java_SIS3153_nativeReadInt(JNIEnv *env, jobject ob, jlong addr, jint func, jintArray buf, jint offset, jint count){

	unsigned int VMEAddr=(addr&0xFFFFFFFF);
	unsigned int data[count]={0};
	unsigned int recived_nof_words=0;
	int ret=-1;

	// We need to get pointers to the java VM arrays
	jboolean isCopy = JNI_FALSE;
	jint *pAry = (jint *)env->GetPrimitiveArrayCritical(buf, &isCopy);
	if (pAry == NULL) {
		return -1001;
	}	
	jint *pData = pAry + offset; // ! pointer arithmetic

	if((func==0x9)||(func==0x109)){
		
		//printf("Native Libarary VME address:%x\n",VMEAddr);
		//ret=vme_A32D32_read(devs[0].hDev,VMEAddr,data);
		ret=sis3150Usb_Vme_Single_Read(devs[0].hDev,VMEAddr,func,4,data);
		pData[0]=data[0];
		
			

        }else if(func==0xB){

		ret=vme_A32BLT32_read(devs[0].hDev,VMEAddr,data,count,&recived_nof_words);
		//ret=vme_A32MBLT64_read(devs[0].hDev,VMEAddr,data,count,&recived_nof_words);
		if((ret==0)&&(count==recived_nof_words)){
			for(int i=0;i<count;++i){
				pData[i]=data[i];
			}
		}

	}else if(func==0x1000){

		ret=sis3150Usb_Register_Single_Read(devs[0].hDev,VMEAddr,data);
		pData[0]=data[0];

	}else{
		return -1000;
	}

	env->ReleasePrimitiveArrayCritical(buf, pAry, 0);
	return ret;
}

/*
 * Class:     SIS3153
 * Method:    nativewriteInt
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_SIS3153_nativewriteInt(JNIEnv *env, jobject ob, jlong addr, jint func, jintArray buf, jint offset, jint count){

	unsigned int VMEAddr=(int)(addr&0xFFFFFFFF);
	unsigned int data[]={0};
	int ret=-1;

	// We need to get pointers to the java VM arrays
	jboolean isCopy = JNI_FALSE;
	jint *pAry = (jint *)env->GetPrimitiveArrayCritical(buf, &isCopy);
	if (pAry == NULL) {
		return -1001;
	}	
	jint *pData = pAry + offset; // ! pointer arithmetic

	if((func==0x9)||(func==0x109)){
		
		data[0]=pData[0];
		ret=sis3150Usb_Vme_Single_Write(devs[0].hDev,VMEAddr,func,4,data[0]);
		//ret=vme_A32D32_write(devs[0].hDev,VMEAddr,data[0]);
		
			

        }else if(func==0xB){

		//ret=vme_A32D32_Write(devs[0].hDev,VMEAddr, data);

	}else if(func==0x1000){

		data[0]=pData[0];
		ret=sis3150Usb_Register_Single_Write(devs[0].hDev,VMEAddr,data[0]);
		

	}else{
		return -1000;
	}

	env->ReleasePrimitiveArrayCritical(buf, pAry, 0);
	return ret;
}


