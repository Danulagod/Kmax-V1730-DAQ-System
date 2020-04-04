/************************************Author Information**************************************
				
				     Danula Godagama 
			    	 danula.godagama@uky.edu 
			        University of Kentucky 
				       03/18/2020 

**************************************File Information***************************************
				       offline.cxx
Perform charge integration(QDC) and constant fraction discrimination(CFD) on raw data. 
Also, check for saturated pulses. Create a ROOT based tree with the processed information. 
Build with: g++ -o offline offline.cxx `root-config --cflags --glibs`
It's autimatically executed by the toolsheet program after each run, If it's located at
Kmax_Stuff/offline directory.
Also, it can be manually executed by ./offline X Y 
where X is Module No. and Y is Run No.
*********************************************************************************************/

#include "TROOT.h"
#include "TPluginManager.h"
#include "TApplication.h"
#include "TH1F.h"
#include "TH1D.h"
#include "TAxis.h"
#include "TF1.h"
#include "TF2.h"
#include "TH2F.h"
#include "TFormula.h"
#include "TFile.h"
#include "TList.h"
#include "TMath.h"
#include "TROOT.h"
#include "TLine.h"
#include "TCanvas.h"
#include "TGraphErrors.h"
#include "TGraph.h"
#include "TMultiGraph.h"
#include "TLegend.h"
#include "TTree.h"
#include "TStyle.h"
#include <vector>
#include <iostream>
#include <fstream>
#include <string>
#include <stdint.h>
#include <sys/stat.h>
#include <omp.h>
#include <unistd.h>
using namespace std;


FILE *myfile=NULL;
char *eventData=NULL;


int eventSize=0;
int channelMask[16];
int enabledChannels=0;
int recordLength=0;
long numEvents=0;
unsigned long firstTTT=0L;

void setEventSize();
void setEnabledChs();
void setRecordLength();
void setNumEvents();
float *QDC(int *pulse,int evt);
void readEvent(int event);
float CFD(int *pulse,TH1D *CFDPlot=NULL);


TH1D *Signal;
TH1D *Plastic;
float Q[16];
float Qtail[16];
float isSaturated[16];
float CFDVal[16];
unsigned long  TimeStamp;
static int pulseNum=0;
TTree *tree;
TH1D *HPlot[100];

int main(int argc,char **arg) {

	char *DatafileName =Form("/home/mkovash/Kmax_Stuff/DATA/Module_%s/Run_%s.bin",arg[1],arg[2]);  
	cout<<DatafileName<<"\n";
	
	myfile=fopen( DatafileName,"rb");	
	if(myfile==NULL){
		cout<<"Data file not found!\n";
		return 0;
	}

	
	TString OutputFileName=Form("/home/mkovash/Kmax_Stuff/Replays/Module_%s/Replay_%s.root",arg[1],arg[2]);
	TFile *replay=new TFile(OutputFileName, "RECREATE");


	
	for(int i=0;i<100;++i){
		HPlot[i]=new TH1D(Form("HPlot_%i",i),Form("HPlot_%i",i),520,0,520);
	}

	tree=new TTree("tree","tree_Data");
	for(int j=0;j<16;j++){
		tree->Branch(Form("Q_%i",j),&Q[j],Form("Q_%i/F",j));
		tree->Branch(Form("Qtail_%i",j),&Qtail[j],Form("Qtail_%i/F",j));
		tree->Branch(Form("IsSaturated_%i",j),&isSaturated[j],Form("IsSaturated_%i/F",j));
		tree->Branch(Form("CFD_%i",j),&CFDVal[j],Form("CFD_%i/F",j));
		
	
	}
	tree->Branch("TimeStamp",&TimeStamp,"TimeStamp/l");
	
	setEventSize();
	setEnabledChs();
	setRecordLength();
	setNumEvents();
	eventData=new char[eventSize*4];
	
	
	for(int i=0;i<numEvents;i++){
		
		readEvent(i);	
		if(i%50000==0){
			Printf("processing Event:%i",i);
		}
		

	}		
	replay->Write();
	printf("ROOT Tree stored on ");
	printf(OutputFileName);
	printf("\n");
	
}

void readEvent(int event){

	fread(eventData,1,eventSize*4,myfile);
	int voltage=0x0;
	int exPulse[recordLength*2]={0};
	int index=0;
	float *result=NULL;
	int eventNumber=0x0;
	long timeStamp32=0x0L;
	long pattern=0x0L;
	

	for(int i=0;i<4;i++){
			
			pattern=((eventData[i+4]&0xFF)<<(24-i*8))|pattern;
			//eventNumber=((eventData[i+8]&0xFF)<<(24-i*8))|eventNumber;
			timeStamp32=((eventData[i+12]&0xFF)<<(24-i*8))|timeStamp32;
			
	}

	
	timeStamp32=timeStamp32&0xFFFFFFFF;
	pattern=pattern&0x00FFFF00;
	TimeStamp=((pattern<<24)|timeStamp32)&0xFFFFFFFFFFFF;
		
	
	
	for(int j=0;j<16;j++){

		if(channelMask[j]!=1){
			Q[j]=0.0;
			continue;
		}
		for(int k=0;k<recordLength*2;k++){
		
			voltage=((eventData[2*k+16+index*(recordLength*4)]&0xFF)<<8)|(eventData[2*k+17+index*(recordLength*4)]&0xFF);

			if(k%2==0){
				exPulse[k+1]=voltage;	
			}else{
				exPulse[k-1]=voltage;	
			}

		}
		index++;

		if((event<100)&&(j==0)){

			result=QDC(exPulse,1);

		}else{		
			result=QDC(exPulse,0);

		}
		Q[j]=result[0];
		Qtail[j]=result[1];
		isSaturated[j]=result[2];
		CFDVal[j]=result[3];
		
		
	}
	tree->Fill();
	
	
}

float *QDC(int *pulse,int evt){

	float BaseLineSamples=50;
	float BaseLine=0.0;
	float Charge=0.0;
	float tail=0.0;
	bool IsSaturated=false;
	static float results[]={0,0,0,0}; //{Charge,tail,IsSaturated(1:yes,0:No}
	int SignalArray[500];
	
	for(int a=0;a<BaseLineSamples;a++){

		BaseLine+=pulse[a];

	}
	
	BaseLine=BaseLine/BaseLineSamples;
	
	for(int a=0;a<500;++a){
		SignalArray[a]=(BaseLine-pulse[a]);
		Charge+=SignalArray[a]*2; //sampling time 
		if(pulse[a]==0x0){
			IsSaturated=true;
		}

		if(a>350){
			tail+=SignalArray[a]*2;
		}

	}
	
	results[0]=Charge/1000;
	results[1]=tail/1000;
	
	if(IsSaturated){
		results[2]=1;
	}else{
		results[2]=0;
	}

	
	if(evt==1){
		results[3]=CFD(SignalArray,HPlot[pulseNum]);
		++pulseNum;

	}else{
		results[3]=CFD(SignalArray);
	}

	return results;

}

float CFD(int *pulse,TH1D *CFDPlot){

	int Delay=1;
	float fraction=0.6;
	double CFDMax=0.0;
  	double CFDMin=0.0;	
  	int CFDMinX=0;
  	int CFDMaxX=0;
	float ZeroPoint;

	int delayedSignal[500+Delay]={0};
	int CFDSignal[500+Delay];
	
	for (int k = 0; k < 500; ++k){

	delayedSignal[k+Delay]=pulse[k];
	CFDSignal[k]=delayedSignal[k]+(-1*fraction*pulse[k]);
	
	if(CFDSignal[k]>CFDMax){
	CFDMax=CFDSignal[k];
	CFDMaxX=k;
	}
	
	if(CFDSignal[k]<CFDMin){
	CFDMin=CFDSignal[k];
	CFDMinX=k;
	}

	} // now CFDSignal array is filled

	if(CFDPlot!=NULL){
		for (int m = 0; m < 500; ++m){

			CFDPlot->Fill(m,CFDSignal[m]);
		}
		
	}

	for (int l = CFDMinX; l < CFDMaxX; ++l) {

		if(CFDSignal[l]>0){

	
			if(CFDSignal[l-1]==0){
			ZeroPoint=(l-1);
			break;
			}
	
		ZeroPoint=((l-1)+(1/(((float)CFDSignal[l]/(-1)*(float)CFDSignal[l-1])+1)));
		
		break;
	
		}
        }
	
	return ZeroPoint;


}

void setEventSize(){

	char firstWord[4];
	fread(firstWord,1,4,myfile);
	for(int i=0;i<4;i++){
			
			eventSize=((firstWord[i+0]&0xFF)<<(24-i*8))|eventSize;
	}

	//cout<<std::hex<<eventSize<<"\n";
	eventSize=eventSize&0xFFFFFFF;
	cout<<"Event Size:"<<eventSize<<"\n";
}


void setEnabledChs(){

	
	char headerWord[8];
	fread(headerWord,1,8,myfile);
	int word=0;
	
	cout<<"Enabled Channels:{";
	for(int i=0;i<8;i++){
			
			if(((headerWord[3]>>i)&0x1)==1){
				channelMask[i]=1;
				cout<<i<<",";	
				enabledChannels+=1;
			}
	}

	for(int i=8;i<16;i++){
			
			if(((headerWord[4]>>(i-8))&0x1)==1){
				channelMask[i]=1;
				cout<<i<<",";	
				enabledChannels+=1;
			}
	}


	cout<<"}\nNumber of enabled Channels:"<<enabledChannels<<"\n";



}

void setRecordLength(){
	recordLength=(eventSize-4)/enabledChannels;
	cout<<"Record Length:"<<recordLength*2<<" samples\n";
}

void setNumEvents(){

	rewind(myfile);
	fseek(myfile,0,SEEK_END);
  	numEvents = ftell(myfile)/(eventSize*4);
	rewind(myfile);
	cout<<"Number of Events:"<<numEvents<<"\n";

}


