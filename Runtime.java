/*****************************Author Information******************************
				
				Danula Godagama 
			    danula.godagama@uky.edu 
			    University of Kentucky 
				  03/18/2020 

		The 'Runtime' class provides Kmax runtime behavior.
	Check the documentation for 'Kmax Java Interfaces' for details. 
******************************************************************************/

import kmax.ext.*; 
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.Math;


public class Runtime implements KmaxRuntime {
	
	KmaxToolsheet tlsh; // Store a reference to the toolsheet environment
	
	KmaxDevice dev;
	
	KmaxWidget Txt_DigOutput;
	KmaxWidget Txt_RegAddr;
	KmaxWidget Txt_RegValue;
	KmaxWidget Btn_ReadRegister;
	KmaxWidget Btn_WriteRegister;
	KmaxWidget Cbox_AddrMod;
	KmaxWidget Txt_RecordLength;
	KmaxWidget Txt_PostTrigger;
	KmaxWidget Cbox_ExtTrigger;
	KmaxWidget Cbox_LDVSTrigger;
	KmaxWidget Cbox_TestPattern;
	KmaxWidget Cbox_PulsePolarity;
	
	KmaxWidget Btn_Inti;
	KmaxWidget Btn_Stop;
	KmaxWidget Btn_Start;
	KmaxWidget TxtErrMsgs;
	KmaxWidget TxtOfflineOutput;
	KmaxWidget Chk_AutoRunControl;
	KmaxWidget Chk_WriteData;
	KmaxWidget Txt_RunDuration;
	KmaxWidget Txt_InitialRunNumber;
	KmaxWidget Txt_ReadoutRate;
	
	
	KmaxWidget Cbox_DigModule;
	KmaxWidget Btn_UpdateSettings;
	KmaxWidget Btn_SaveSettings;
	KmaxWidget Btn_LoadSettings;
	KmaxWidget Btn_Clear;
	KmaxWidget Btn_Reset;
	KmaxWidget Btn_Reconfigure;
	KmaxWidget Btn_BoardInfor;
	KmaxWidget Btn_ChannelStatus;
	KmaxWidget Btn_ClockSync;
	KmaxWidget Btn_ClearOutput;
	KmaxWidget Btn_ADCCal;
	KmaxWidget Btn_SwTrigger; 
	
	KmaxWidget txt_scl;
	
	KmaxWidget Chk_EnbleCh[]=new KmaxWidget[16];
	KmaxWidget Chk_EnableCh_Common;
	
	KmaxWidget Txt_BaseLine[]=new KmaxWidget[16];
	KmaxWidget Txt_BaseLine_Common;
	
	KmaxWidget Txt_DCOffset[]=new KmaxWidget[16];
    	KmaxWidget Txt_DCOffset_Common;

    	KmaxWidget Txt_TrigThd[]=new KmaxWidget[16];
    	KmaxWidget Txt_TrigThd_Common;

    	KmaxWidget Cbox_DynamicRange[]=new KmaxWidget[16];
    	KmaxWidget Cbox_DynamicRange_Common;

    	KmaxWidget Cbox_SelfTrig_G[]=new KmaxWidget[8];
    	KmaxWidget Cbox_SelfTrig_Common;

	KmaxWidget Cbox_GlobTrig_G[]=new KmaxWidget[8];
    	KmaxWidget Cbox_GlobTrig_Common;

	KmaxWidget Cbox_TrigOut_G[]=new KmaxWidget[8];
    	KmaxWidget Cbox_TrigOut_Common;

    	KmaxWidget Chk_LDVS_enable;

    	KmaxWidget Cbox_LDVS_G[]=new KmaxWidget[4];
    	KmaxWidget Cbox_LDVS_F[]=new KmaxWidget[4];

	
	int Dig_RegAddr[]={0x8120, //ch enable
					   0x1028,0x1128,0x1228,0x1328,0x1428,0x1528,0x1628,0x1728,0x1828,0x1928,0x1A28,0x1B28,0x1C28,0x1D28,0x1E28,0x1F28, //Dynmic Range
					   0x1080,0x1180,0x1280,0x1380,0x1480,0x1580,0x1680,0x1780,0x1880,0x1980,0x1A80,0x1B80,0x1C80,0x1D80,0x1E80,0x1F80, //Trigger Thd,
					   0x1098,0x1198,0x1298,0x1398,0x1498,0x1598,0x1698,0x1798,0x1898,0x1998,0x1A98,0x1B98,0x1C98,0x1D98,0x1E98,0x1F98,//DC offset
					   0x1084,0x1284,0x1484,0x1684,0x1884,0x1A84,0x1C84,0x1E84,//self trigger,
					   0x810C,
					   0x8110,
					   0x8114,//post trigger
					   0x8020,//custom size
					   0x8000,
					   0x800C,//Buffer code, number of buffers pre Ch 
					   0x811C,
					   0x816C,//Almost full level
					   0x81A0};
	
	int PlotSource[]={0,1,2,3,4,5,6};
	KmaxXYPlot Plot_Waveform;
	KmaxXYPlot PulsePlot_[]=new KmaxXYPlot[7];
	KmaxWidget ChkBox_Source_[]=new KmaxWidget[7];	
	KmaxWidget Cbox_Source_[]=new KmaxWidget[7];
	KmaxWidget Chk_EnablePlot;
	
	int RLength=500; 
	int pulseData[]=new int[1604*200];
	byte convertedPulseData[];
	
	
	
	//long BaseAddr_module[]={0x06000000,0x0B000000,0x07000000};
	//long BaseAddr_module[]={0x0B000000,0x0E000000,0x03000000};
	long BaseAddr_module[]={0x03000000,0x67890000,0x07000000};
	
	long BASEAddr=BaseAddr_module[0];
	
	File logFile;
	FileWriter logWriter;
	KmaxWidget Txt_RunLog; 
	
	
	int runNumber=1;
	boolean IsIntialRunNumSet=false;
	boolean IsAutomated=false;
	boolean IsWriteEnabled=false;
	boolean IsPlottingEnabled=false;
	boolean IsDAQRunning=false;
	
	int runDuration=30; 
	int eventSize[]=new int[BaseAddr_module.length];
	//int eventSize[]={1604,1604,1604};
	boolean setEventSize[]=new boolean[BaseAddr_module.length];
	Timer runTimer;
	Timer readOutTimer;
	Timer HistFillTimer;
	
       

	short chReadoutMask[]=new short[BaseAddr_module.length]; //redu
	File dataFile[]=new File[BaseAddr_module.length];
	BufferedOutputStream dataBuffer[]=new BufferedOutputStream[BaseAddr_module.length];
	BufferedInputStream readBuffer[]=new BufferedInputStream[BaseAddr_module.length];
	
	KmaxHist Histchannel[]=new KmaxHist[16*BaseAddr_module.length];
    Process runAnalysis=null;

	/**
	* The 'init' method is executed at compile time.
	*/
	public void init(KmaxToolsheet toolsheet) {
		tlsh = toolsheet; // Save this reference for use in the toolsheet
		dev=tlsh.getKmaxDevice("VME_DEV");
		Txt_DigOutput=tlsh.getKmaxWidget("Txt_DigOutput");
		Txt_RegAddr=tlsh.getKmaxWidget("Txt_RegAddr");
		Txt_RegValue=tlsh.getKmaxWidget("Txt_RegValue");
		Btn_ReadRegister=tlsh.getKmaxWidget("Btn_ReadRegister");
	    	Btn_WriteRegister=tlsh.getKmaxWidget("Btn_WriteRegister");
	    	Cbox_AddrMod=tlsh.getKmaxWidget("Cbox_AddrMod");
		
		Cbox_DigModule=tlsh.getKmaxWidget("Cbox_DigModule");	
		Btn_UpdateSettings=tlsh.getKmaxWidget("Btn_UpdateSettings");
		Btn_SaveSettings=tlsh.getKmaxWidget("Btn_SaveSettings");
		txt_scl=tlsh.getKmaxWidget("txt_scl");
		
		for(int j=0;j<16;j++){
			
		Chk_EnbleCh[j]=tlsh.getKmaxWidget(String.format("Chk_EnbleCh%d",j));
		Txt_BaseLine[j]=tlsh.getKmaxWidget(String.format("Txt_BaseLine_Ch%d",j));
		Txt_DCOffset[j]=tlsh.getKmaxWidget(String.format("Txt_DCOffset_Ch%d",j));
		Txt_TrigThd[j]=tlsh.getKmaxWidget(String.format("Txt_TrigThd_Ch%d",j));
		Cbox_DynamicRange[j]=tlsh.getKmaxWidget(String.format("Cbox_DynamicRange_Ch%d",j));
		
		}
		
		
		for(int j=0;j<8;j++){
		Cbox_SelfTrig_G[j]=tlsh.getKmaxWidget(String.format("Cbox_SelfTrig_G%d",j));
		Cbox_GlobTrig_G[j]=tlsh.getKmaxWidget(String.format("Cbox_GlobTrig_G%d",j));
		Cbox_TrigOut_G[j]=tlsh.getKmaxWidget(String.format("Cbox_TrigOut_G%d",j));
		
		}
		
		Chk_EnableCh_Common=tlsh.getKmaxWidget("$Chk_EnableCh_Common");
		Txt_BaseLine_Common=tlsh.getKmaxWidget("$Txt_BaseLine_Common");
		Txt_DCOffset_Common=tlsh.getKmaxWidget("$Txt_DCOffset_Common");
		Txt_TrigThd_Common=tlsh.getKmaxWidget("$Txt_TrigThd_Common");
		Cbox_DynamicRange_Common=tlsh.getKmaxWidget("$Cbox_DynamicRange_Common");
		Cbox_SelfTrig_Common=tlsh.getKmaxWidget("$Cbox_SelfTrig_Common");
		Cbox_GlobTrig_Common=tlsh.getKmaxWidget("$Cbox_GlobTrig_Common");
		Cbox_TrigOut_Common=tlsh.getKmaxWidget("$Cbox_TrigOut_Common");
		
		Chk_LDVS_enable=tlsh.getKmaxWidget("$Chk_LDVS_enable");
		
		for(int j=0;j<4;j++){
		Cbox_LDVS_G[j]=tlsh.getKmaxWidget(String.format("Cbox_LDVS_G%d",j+1));
		Cbox_LDVS_F[j]=tlsh.getKmaxWidget(String.format("Cbox_LDVS_F%d",j+1));
		}
		
		Btn_Inti=tlsh.getKmaxWidget("Btn_Inti");
		Btn_Stop=tlsh.getKmaxWidget("Btn_Stop");
		Btn_Start=tlsh.getKmaxWidget("Btn_Start");
		TxtErrMsgs=tlsh.getKmaxWidget("TxtErrMsgs");
		TxtOfflineOutput=tlsh.getKmaxWidget("TxtOfflineOutput");
		Chk_AutoRunControl=tlsh.getKmaxWidget("Chk_AutoRunControl");
		Chk_WriteData=tlsh.getKmaxWidget("Chk_WriteData");
	    	Txt_RunDuration=tlsh.getKmaxWidget("Txt_RunDuration");
		Txt_InitialRunNumber=tlsh.getKmaxWidget("Txt_InitialRunNumber");
		Txt_ReadoutRate=tlsh.getKmaxWidget("Txt_ReadoutRate");
		
		
		
		Cbox_ExtTrigger=tlsh.getKmaxWidget("Cbox_ExtTrigger");
	    	Cbox_LDVSTrigger=tlsh.getKmaxWidget("Cbox_LDVSTrigger");
	    	Cbox_PulsePolarity=tlsh.getKmaxWidget("Cbox_PulsePolarity");
	    	Cbox_TestPattern=tlsh.getKmaxWidget("Cbox_TestPattern");
	    	Txt_RecordLength=tlsh.getKmaxWidget("Txt_RecordLength");
	    	Txt_PostTrigger=tlsh.getKmaxWidget("Txt_PostTrigger");
	
	
		Btn_LoadSettings=tlsh.getKmaxWidget("Btn_LoadSettings");
		Btn_Clear=tlsh.getKmaxWidget("Btn_Clear");
		Btn_Reset=tlsh.getKmaxWidget("Btn_Reset");
		Btn_Reconfigure=tlsh.getKmaxWidget("Btn_Reconfigure");
		Btn_BoardInfor=tlsh.getKmaxWidget("Btn_BoardInfor");
		Btn_ChannelStatus=tlsh.getKmaxWidget("Btn_ChannelStatus");
		Btn_ClockSync=tlsh.getKmaxWidget("Btn_ClockSync");
		Btn_ClearOutput=tlsh.getKmaxWidget("Btn_ClearOutput");
		Btn_ADCCal=tlsh.getKmaxWidget("Btn_ADCCal");
		Btn_SwTrigger=tlsh.getKmaxWidget("Btn_SwTrigger");
		
		
		
		Plot_Waveform=tlsh.getKmaxXYPlot("Plot_Waveform");
		Chk_EnablePlot=tlsh.getKmaxWidget("Chk_EnablePlot");
		Txt_RunLog=tlsh.getKmaxWidget("Txt_RunLog");
		
		for(int k=0;k<BaseAddr_module.length;k++){
			for(int j=0;j<16;j++){
				Histchannel[(16*k)+j]=tlsh.getKmaxHist(String.format("Module_%d_ch_%d",k+1,j));
			
			}
		}

		for(int l=0;l<7;l++){
			PulsePlot_[l]=tlsh.getKmaxXYPlot(String.format("PulsePlot_%d",l));
			Cbox_Source_[l]=tlsh.getKmaxWidget(String.format("Cbox_Source_%d",l));
			ChkBox_Source_[l]=tlsh.getKmaxWidget(String.format("ChkBox_Source_%d",l));
		}
		
		GetPulseSources();
		   	
	}

	/**
	* The 'GO' method is executed when the GO button is clicked.
	*/
	public void GO(KmaxToolsheet toolsheet) {

		tlsh.setProperty("STATUSSTR", "Toosheet Started !!!");
		Btn_Start.setProperty("ENABLED","FALSE");
		Btn_Stop.setProperty("ENABLED","FALSE");
		Txt_RunLog.setProperty("TEXT","");
		Txt_DigOutput.setProperty("TEXT","");
		Txt_ReadoutRate.setProperty("TEXT","");
		Chk_AutoRunControl.setProperty("VALUE","0");
		Chk_WriteData.setProperty("VALUE","0");
		Txt_RunDuration.setProperty("ENABLED","FALSE");
		Txt_InitialRunNumber.setProperty("ENABLED","TRUE");
		Txt_InitialRunNumber.setProperty("VALUE",Integer.toString(runNumber));
		
		IsIntialRunNumSet=false;
		IsAutomated=false;
		IsWriteEnabled=false;
		
		for(int j=0;j<BaseAddr_module.length;j++){
			setEventSize[j]=false;
		}
		
		//Change VME address to activate 16mb BLT space
		RelocateVMEAddress();
		
		try{
			SetLogFile();
		}catch (IOException e){
			tlsh.setProperty("STATUSSTR", "Error setting the log file");;
		}
		
		for(int j=0;j<Histchannel.length;j++){
			Histchannel[j].clear();
		}

		Refresh_DIG_Properties_Tab();
		IsPlottingEnabled=false;
		Chk_EnablePlot.setProperty("VALUE","0");
		clearPlots();
		for(int l=0;l<7;l++){
			Cbox_Source_[l].setProperty("VALUE",String.format("%d",l));
			ChkBox_Source_[l].setProperty("VALUE","0");
		}

		

	}

	/**
	* The 'SRQ' method is executed when a device requests service.
	*/
	public void SRQ(KmaxDevice device) {
		tlsh.setProperty("STATUSSTR", "Hello SRQ event!");
		
	}

	/**
	* The 'HALT' method is executed when the HALT button is clicked.
	*/
	public void HALT(KmaxToolsheet toolsheet) {
		
		//shutdown DAQ in all digitizers
		/*
		int writeData[]={0x0000};

		for(int j=0;j<BaseAddr_module.length;j++){

			BASEAddr=BaseAddr_module[j];
    			Write_DIG_Register(0x8100,0x9,writeData);

		}
		*/

		if(IsDAQRunning){

			Stop_DAQ();
			
		}

		//Disable all remaining timers
		if(readOutTimer!=null){
			readOutTimer.cancel();
		}
		
		if(runTimer!=null){
			runTimer.cancel();
		}

		if(HistFillTimer!=null){
			HistFillTimer.cancel();
		}
			
	}
	
	
	
	/**
	* This method will initialize the DAQ in Digitizers.
	*/
	public void Initialize_DAQ() {
		
		
		//Clear all Digitizers
		for(int j=0;j<BaseAddr_module.length;j++){
			
			BASEAddr=BaseAddr_module[j];
			Clear_Digitizer();
			
		}
		
		tlsh.setProperty("STATUSSTR", "Initialized...");
		
				
	}
	
	
	


	/**
	* This method will start the DAQ in Digitizers.
	*/
	public void Start_DAQ() {

		//Create Data Files
		if(IsWriteEnabled){
			
			if(!Create_Data_Files(runNumber)){
				
				tlsh.setProperty("STATUSSTR", "toolsheet halted !!! check for existing data files");
				tlsh.HALT();
				return;
			}
			
			
		}else{
			Create_Data_Files(-1);  //write to cache 
		}
		Create_readBuffers();
		
		//Copy Trigout data
		int tempTrigOutData[]={0x0};
		Read_DIG_Register(BaseAddr_module[0],0x8110,0x9,tempTrigOutData);
		
		//flush Trigout register
		int empthyTrigoutData[]={0x0};
		Write_DIG_Register(BaseAddr_module[0],0x8110,0x9,empthyTrigoutData);

		int tempData[]={0x1324}; //Start-DAQ command to digitizer. 
								//Also, set Memory Full Mode Selection to one buffer FREE
								//Enable LDVS BUSY, 
								//Enable LDVS VETO, 
								 
		
		//Start DAQ in Digitizers
		for(int j=0;j<BaseAddr_module.length;j++){
			Write_DIG_Register(BaseAddr_module[BaseAddr_module.length-(j+1)],0x8100,0x9,tempData);
		}
		
		try{
			Thread.sleep(10);
		}catch(InterruptedException e){TxtErrMsgs.setProperty("INSERT", "Thread Exception");}
		
		//re-write trigout data
		Write_DIG_Register(BaseAddr_module[0],0x8110,0x9,tempTrigOutData);
	
		
					
		//update runlog
		try{
			logWriter=new FileWriter(logFile,true);
		
			PrintWriter print_line = new PrintWriter(logWriter);
			if(IsWriteEnabled){
				print_line.printf(Integer.toString(runNumber)+"\t\t"+java.time.LocalTime.now().truncatedTo(ChronoUnit.SECONDS));
			}else{
				print_line.printf("cache"+"\t\t"+java.time.LocalTime.now().truncatedTo(ChronoUnit.SECONDS));
			}
			print_line.close();
			UpdateRunLog();
		}catch (IOException e){
			TxtErrMsgs.setProperty("INSERT","\nerror while updating the run log!\n");
		}

		
		/****************************************************Readout Timer Task**********************************************/
		TimerTask readOutTask = new TimerTask() {
			
			int totalEvents=0;
			int countSecond=0;
			float dataRate=0;
        		public void run() {
					
					int numEvents[]=new int[BaseAddr_module.length];
					int err=1;
					
					for(int j=0;j<BaseAddr_module.length;j++){
					////for(int j=0;j<1;j++) {
						
            					dev.readInt(BaseAddr_module[j],0x812C,0x9,numEvents,j,1);
						
						if( numEvents[j]>0){
							
							if(!setEventSize[j]){
								dev.readInt(BaseAddr_module[j],0x814C,0x9,eventSize,j,1);	
								setEventSize[j]=true;
							}	
						
							if(numEvents[j]>200){ //set 200 event limit 
							
								numEvents[j]=200;
							
							}

							for(int k=0;k<numEvents[j];k++){
								
								
								BLT(BaseAddr_module[j],pulseData,k*eventSize[j],eventSize[j]);
								//int err=dev.readInt(BaseAddr_module[j],0x0000,0x0B,pulseData,0,eventSize[j]*numEvents[j]);
								//err=dev.readInt(BaseAddr_module[j],0x0000,0x0B,pulseData,k*eventSize[j],eventSize[j]);
								
								//TxtErrMsgs.setProperty("INSERT",Integer.toHexString(pulseData[k*eventSize[j]])+"\n");
								//TxtErrMsgs.setProperty("INSERT",Integer.toHexString(pulseData[k*eventSize[j]+2])+"\n");
							
								

							}
						
							//if((err==0)||true){
								
								convertedPulseData=IntArraytoByte(pulseData);
							
								//if(IsWriteEnabled){
							
									try {		
										dataBuffer[j].write(convertedPulseData,0,eventSize[j]*numEvents[j]*4);
									
									     } catch (IOException e) {
											tlsh.setProperty("STATUSSTR", "File Write Exception !!!");
										}
								//}
							//}
						}

						if(countSecond%9==0){
											
							if(IsPlottingEnabled){

								plotEvent(j);
							}
							
						}
						
					}
					
					totalEvents+=numEvents[0];
					countSecond+=1;
					
					if(countSecond%10==0){
					
						dataRate=(totalEvents*4)*(eventSize[0]+eventSize[1])/1000; 
						Txt_ReadoutRate.setProperty("INSERT","Trig Rate :"+Integer.toString(totalEvents)+"/s (Data Rate: "+Float.toString(dataRate)+" kB/s)"+"\n");
						totalEvents=0;
						countSecond=0;
						
					}
					
					
					
        		}
				
    		};
		/****************************************************Readout Timer Task**********************************************/

		
		readOutTimer=new Timer();
		readOutTimer.scheduleAtFixedRate(readOutTask,100, 100);
		tlsh.setProperty("STATUSSTR", "DAQ Started...");
			
				
	}


	/**
	* This method will stop the DAQ in Digitizers.
	*/
	public void Stop_DAQ() {
		
		//Copy Trigout data
		int tempTrigOutData[]={0x0};
		Read_DIG_Register(BaseAddr_module[0],0x8110,0x9,tempTrigOutData);
		
		//flush Trigout register
		int empthyTrigoutData[]={0x0};
		Write_DIG_Register(BaseAddr_module[0],0x8110,0x9,empthyTrigoutData);
		
		try{
			Thread.sleep(10);
		}catch(InterruptedException e){TxtErrMsgs.setProperty("INSERT", "Thread Exception");}

		int tempData[]={0x1320}; //Stop-DAQ command to digitizer resiter 
		
		//Stop DAQ in Digitizers
		for(int j=0;j<BaseAddr_module.length;j++){
			Write_DIG_Register(BaseAddr_module[j],0x8100,0x9,tempData);
		}
		
		//re-write trigout data
		Write_DIG_Register(BaseAddr_module[0],0x8110,0x9,tempTrigOutData);

		//update runlog
		
		try{
			logWriter=new FileWriter(logFile,true);
		
		PrintWriter print_line = new PrintWriter(logWriter);
		print_line.printf("\t\t"+java.time.LocalTime.now().truncatedTo(ChronoUnit.SECONDS)+"\t\t"+Long.toString(Read_Scalars())+"\n");
		print_line.close();
		runNumber+=1;
		
		}catch (IOException e){
			e.printStackTrace();
		}
		
			
		try{
			UpdateRunLog();
			
			//check number of events stored before cancel
			Thread.sleep(1000);
			readOutTimer.cancel(); 
			
			//if(IsWriteEnabled){
				
				for(int j=0;j<BaseAddr_module.length;j++){
        			dataBuffer[j].flush(); 
     				dataBuffer[j].close(); 
				
				}
			//}

			TxtOfflineOutput.setProperty("TEXT","");
			
			Process makeTrees1=java.lang.Runtime.getRuntime().exec(String.format("/home/mkovash/Kmax_Stuff/offline/offline 1 %d",runNumber-1));
			Process makeTrees2=java.lang.Runtime.getRuntime().exec(String.format("/home/mkovash/Kmax_Stuff/offline/offline 2 %d",runNumber-1));
			ProcessReader readTreeProcess1=new ProcessReader(makeTrees1,makeTrees1.getInputStream(),1);
			ProcessReader readTreeProcess2=new ProcessReader(makeTrees2,makeTrees2.getInputStream(),2);
			readTreeProcess1.start();
			readTreeProcess2.start();
			
			
			FileWriter filelist=new FileWriter("/home/mkovash/Kmax_Stuff/offline/filelist.txt",true);
			PrintWriter printRunNumber = new PrintWriter(filelist);
			printRunNumber.printf(Integer.toString(runNumber-1)+"\n");
			printRunNumber.close();
			
			
		}catch (Exception excep){
			//TxtOfflineOutput.setProperty("TEXT",excep.printStackTrace());
			excep.printStackTrace();
		}

		 
        
		tlsh.setProperty("STATUSSTR", "DAQ Stoped...");
		Txt_ReadoutRate.setProperty("TEXT","");

		
	}


	
	/**
	* This method will disable all the widgets in the digitizer properties panel.
	*/
	public void Enable_DIG_Widgets(boolean enable){
		
		String EnabledValue;
		
		if(enable){
			EnabledValue="TRUE";
		}else{
			EnabledValue="FALSE";
		}
		
		
		Txt_DigOutput.setProperty("ENABLED",EnabledValue);
		Txt_RegAddr.setProperty("ENABLED",EnabledValue);
		Txt_RegValue.setProperty("ENABLED",EnabledValue);
		
		Btn_UpdateSettings.setProperty("ENABLED",EnabledValue);
		Btn_SaveSettings.setProperty("ENABLED",EnabledValue);
		Btn_LoadSettings.setProperty("ENABLED",EnabledValue);
		Btn_Clear.setProperty("ENABLED",EnabledValue);
		Btn_Reset.setProperty("ENABLED",EnabledValue);
		Btn_Reconfigure.setProperty("ENABLED",EnabledValue);
		Btn_BoardInfor.setProperty("ENABLED",EnabledValue);
		Btn_ChannelStatus.setProperty("ENABLED",EnabledValue);
		Btn_ClockSync.setProperty("ENABLED",EnabledValue);
		Btn_ClearOutput.setProperty("ENABLED",EnabledValue);
		Btn_ADCCal.setProperty("ENABLED",EnabledValue);
		Btn_SwTrigger.setProperty("ENABLED",EnabledValue);
		
		for(int j=0;j<16;j++){
			
			Chk_EnbleCh[j].setProperty("ENABLED",EnabledValue);
			Txt_BaseLine[j].setProperty("ENABLED",EnabledValue);
			Txt_DCOffset[j].setProperty("ENABLED",EnabledValue);
			Txt_TrigThd[j].setProperty("ENABLED",EnabledValue);
			Cbox_DynamicRange[j].setProperty("ENABLED",EnabledValue);
		
		}
		
		
		for(int j=0;j<8;j++){

			Cbox_SelfTrig_G[j].setProperty("ENABLED",EnabledValue);
			Cbox_GlobTrig_G[j].setProperty("ENABLED",EnabledValue);
			Cbox_TrigOut_G[j].setProperty("ENABLED",EnabledValue);
		
		}
		
		Chk_EnableCh_Common.setProperty("ENABLED",EnabledValue);
		Txt_BaseLine_Common.setProperty("ENABLED",EnabledValue);
		Txt_DCOffset_Common.setProperty("ENABLED",EnabledValue);
		Txt_TrigThd_Common.setProperty("ENABLED",EnabledValue);
		Cbox_DynamicRange_Common.setProperty("ENABLED",EnabledValue);
		Cbox_SelfTrig_Common.setProperty("ENABLED",EnabledValue);
		Cbox_GlobTrig_Common.setProperty("ENABLED",EnabledValue);
		Cbox_TrigOut_Common.setProperty("ENABLED",EnabledValue);
		
		Chk_LDVS_enable.setProperty("ENABLED",EnabledValue);
		
		for(int j=0;j<4;j++){

			Cbox_LDVS_G[j].setProperty("ENABLED",EnabledValue);
			Cbox_LDVS_F[j].setProperty("ENABLED",EnabledValue);
		}
		
		Cbox_TestPattern.setProperty("ENABLED",EnabledValue);
		Cbox_ExtTrigger.setProperty("ENABLED",EnabledValue);
	    	Cbox_LDVSTrigger.setProperty("ENABLED",EnabledValue);
	    	Cbox_PulsePolarity.setProperty("ENABLED",EnabledValue);
	    	Txt_RecordLength.setProperty("ENABLED",EnabledValue);
	    	Txt_PostTrigger.setProperty("ENABLED",EnabledValue);
	    
		
		
		Btn_ReadRegister.setProperty("ENABLED",EnabledValue);
	    	Btn_WriteRegister.setProperty("ENABLED",EnabledValue);
	    	Cbox_AddrMod.setProperty("ENABLED",EnabledValue);
	
		
	}
	
	
	
	
	public int DIG_GetBoardInfor() {	// Board information 
		
		int infor=0;
		int readData[]={0};
		int IOResult=Read_DIG_Register(0x8140,0x09,readData);
						
		if(IOResult==1){

			Txt_DigOutput.setProperty("INSERT","\n*******Board Information*******\n");
	
			infor=readData[0];
			
			if((infor&0xFF)==0x0E){

				Txt_DigOutput.setProperty("INSERT","Digizer Family Code:725\n");		

			}
		

			if((infor&0xFF)==0x0B){

				Txt_DigOutput.setProperty("INSERT","Digizer Family Code:730\n");

			}

		

			if((infor&0xFF00)==0x0100){

				Txt_DigOutput.setProperty("INSERT","Channel Memory Size:640 kS\n");

			}

		

			if((infor&0xFF00)==0x0800){

				Txt_DigOutput.setProperty("INSERT","Channel Memory Size:5.12 Ms\n");

			}
		

			if((infor&0xFF0000)==0x100000){
	
				Txt_DigOutput.setProperty("INSERT","Number of channels:16\n");
			}

		

			if((infor&0xFF0000)==0x080000){

				Txt_DigOutput.setProperty("INSERT","Number of channels:8\n");

			}

			IOResult=Read_DIG_Register(0x8124,0x09,readData);
		
			if(IOResult==1){
				GetFPGAInfor(readData[0]);
			}

		}

		return IOResult;
	

	}  
	
	public void GetFPGAInfor(int infor) {	//FPGA information 

	

	Txt_DigOutput.setProperty("INSERT","\nROC FPGA Firmware Revision: " + String.valueOf((infor&0xFF00)>>8)+"."+String.valueOf(infor&0xFF)+"\n");

	Txt_DigOutput.setProperty("INSERT","ROC FPGA Firmware Date    : " + String.valueOf(2000+((infor&0xF0000000)>>28))+ "/"+String.valueOf((infor&0x0F000000)>>24)+"/"+String.valueOf((infor&0x00F00000)>>20)+String.valueOf((infor&0x000F0000)>>16)+"\n");

	}
	
	
	/******************************************Update Digitizer settings************************************************/
	
	public void Cbox_DigModule(KmaxWidget widget) {
		
		
		Refresh_DIG_Properties_Tab();
				
		
	}
	
	
    public void Refresh_DIG_Properties_Tab(){

		int module_ID=Integer.parseInt(Cbox_DigModule.getProperty("VALUE"));
		
		BASEAddr=BaseAddr_module[module_ID];
				
		int checkConnection=DIG_GetBoardInfor();
				
		if(checkConnection==1){
			
			Read_Dig_Ch_Enable_Settings();
			Read_Dig_DCOffset_Settings();
			Read_Dig_TrigThd_Settings();
			Read_DIG_DynamicRange_Settings();
			Read_DIG_SelfTrigger_Settings();
			Read_DIG_GolbTrigger_AND_TrigOut_Settings();
			Read_Dig_PulsePolarity_AND_TestPattern_Settings();
			Read_Dig_LDVSFeatures_Settings();
			Read_Dig_RecordLength_Settings();
			Read_Dig_PostTrigger_Settings();
					
		}else{
			Txt_DigOutput.setProperty("INSERT","\n**********Digitizer failed to respond !!!! **********\n");
			Enable_DIG_Widgets(false);
				
			return;
				
		}


	} 
	
	
	public int Read_DIG_Register(int regAddr,int AddrMod,int regData[]){
		
		int err=dev.readInt(BASEAddr,regAddr,AddrMod,regData,0,1);
		
		if(err==0 && regData[0]!=0xFFFFFFFF){
			
			//Txt_DigOutput.setProperty("INSERT","\n*************Read successful !!!*************\n");	
			return 1;
			
		}else if(err==0){
		
			
			Txt_DigOutput.setProperty("INSERT","*************Error in reading address "+Integer.toHexString(regAddr)+"*************\n");
			Txt_DigOutput.setProperty("INSERT","*************Returned a NULL value*************\n");
			
			return 0;	
					
		}else{

			Txt_DigOutput.setProperty("INSERT","*************Error in reading address "+Integer.toHexString(regAddr)+"*************\n");
			Txt_DigOutput.setProperty("INSERT",dev.getErrorMessage(err)+"\n");
			return 0;

		}
		
	}


	/*
	******Overload method to Read_DIG_Register
	*/
	public int Read_DIG_Register(long BaseAddr,long regAddr,int AddrMod,int regData[]){
		
		int err=dev.readInt(BaseAddr,regAddr,AddrMod,regData,0,1);
		
		if(err==0 && regData[0]!=0xFFFFFFFF){
			
			//Txt_DigOutput.setProperty("INSERT","\n*************Read successful !!!*************\n");	
			return 1;
			
		}else if(err==0){
		
			
			Txt_DigOutput.setProperty("INSERT","*************Error in reading address "+Long.toHexString(regAddr)+"*************\n");
			Txt_DigOutput.setProperty("INSERT","*************Returned a NULL value*************\n");
			
			return 0;	
					
		}else{

			Txt_DigOutput.setProperty("INSERT","*************Error in reading address "+Long.toHexString(regAddr)+"*************\n");
			Txt_DigOutput.setProperty("INSERT",dev.getErrorMessage(err)+"\n");
			return 0;

		}
		
	}

	
	public int Write_DIG_Register(int regAddr,int AddMod,int writeData[]){
		
		int err=dev.writeInt(BASEAddr,regAddr,AddMod,writeData,0,1);
		
		if(err==0){
			
			//Txt_DigOutput.setProperty("INSERT","\n*************Write successful !!!*************");	
			return 1;
			
		}else{
		
			Txt_DigOutput.setProperty("INSERT","*************Error in writing to address "+Integer.toHexString(regAddr)+"*************\n");
			Txt_DigOutput.setProperty("INSERT",dev.getErrorMessage(err)+"\n");
			return 0;
					
		}
		
	}

	/*
	******Overload method to Write_DIG_Register
	*/
	public int Write_DIG_Register(long BaseAddr,long regAddr,int AddrMod,int writeData[]){
		
		int err=dev.writeInt(BaseAddr,regAddr,AddrMod,writeData,0,1);
		
		if(err==0){
			
			//Txt_DigOutput.setProperty("INSERT","\n*************Write successful !!!*************");	
			return 1;
			
		}else{
		
			Txt_DigOutput.setProperty("INSERT","*************Error in writing to address "+Long.toHexString(regAddr)+"*************\n");
			Txt_DigOutput.setProperty("INSERT",dev.getErrorMessage(err)+"\n");
			return 0;
					
		}
		
	}

	
	
	public int Reset_Digitizer(){
		
		int writeData[]={0xFFFF};
		int result=Write_DIG_Register(0xEF24,0x09,writeData);
		
		if(result==1){
			Txt_DigOutput.setProperty("INSERT","Reset successful!\n");
			return 1;
		}else{
			return 0;
		}
		
	}
	
	
	public int Clear_Digitizer(){
	
		int writeData[]={0xFFFF};
		int result=Write_DIG_Register(0xEF28,0x09,writeData);
		
		if(result==1){
			Txt_DigOutput.setProperty("INSERT","Clear successful!\n");
			return 1;
		}else{
			return 0;
		}
		
	}
	
	
	public int DIG_ClockSync(){
		
		int writeData[]={0xFFFF};
		int result=Write_DIG_Register(0x813C,0x09,writeData);
		
		if(result==1){
			Txt_DigOutput.setProperty("INSERT","Sync successful!\n");
			return 1;
		}else{
			return 0;
		}
			
	}
	
	
	public int Get_DIG_ChStatus(){
		
		Txt_DigOutput.setProperty("INSERT","\n*************Reading Channel Status*************\n\n");	
		
		int readData[]={0};
		int result;
		int returnValue=0;
		
		for(int j=0;j<16;j++){
			
			Txt_DigOutput.setProperty("INSERT","\n\n*************Channel["+String.valueOf(j)+"]:*************");
			
			readData[0]=0;
			result=Read_DIG_Register((0x1088+(0x100*j)),0x09,readData);
		
		if(result==1){
			
			if((readData[0]&0x0001)==0x1){
			
				Txt_DigOutput.setProperty("INSERT","Channel["+String.valueOf(j)+"]: Memory Full\n");	
				
			}
			
			if((readData[0]&0x0002)==0x2){
			
				Txt_DigOutput.setProperty("INSERT","Channel["+String.valueOf(j)+"]: Memory Empthy\n");	
				
			}
			
			
			
			if((readData[0]&0x0004)==0x4){

				Txt_DigOutput.setProperty("INSERT","Channel["+String.valueOf(j)+"]: Channel Busy\n");	
				returnValue=1;

			}

	
			if((readData[0]&0x0004)==0x0) {

				Txt_DigOutput.setProperty("INSERT","Channel["+String.valueOf(j)+"]: DC Offset Updated\n");

			}

	
			if((readData[0]&0x0008)==0x8){

				Txt_DigOutput.setProperty("INSERT","Channel["+String.valueOf(j)+"]: Calibration Done\n");

			}

	
			else{

				Txt_DigOutput.setProperty("INSERT","Channel["+String.valueOf(j)+"]: Calibration Not Done\n");
				returnValue=2;
			}

	

			if((readData[0]&0x0100)==0x100) {

				Txt_DigOutput.setProperty("INSERT","Channel["+String.valueOf(j)+"]: Channel Shutdown\n");
				returnValue=1;

			}
			
			
			
		}else{
			
			Txt_DigOutput.setProperty("INSERT","\nChannel["+String.valueOf(j)+"]: Reading Status failed !!!\n\n");
			returnValue=1;
		}
		
		} //channel loop 
		
		return returnValue;  //0 if ready for ADC cal and 1 if not, 2 Calibration not done 
		
	}
	
	
	
	public int ADC_Calibration(){
		
		
		int ChStatus=Get_DIG_ChStatus();
		
		if(ChStatus==0){
		
			Txt_DigOutput.setProperty("INSERT","\n**********Ready for ADC Calibration*******************\n");
			int writeData[]={0xFFFF};
			int writeResult=Write_DIG_Register(0xEF28,0x09,writeData);	
			if( writeResult==1){
				
				Txt_DigOutput.setProperty("INSERT","\n**********ADC Calibration Initiated*******************\n");
				// pause
				Txt_DigOutput.setProperty("TEXT","");
				ChStatus=Get_DIG_ChStatus();
				if(ChStatus==2){
					Txt_DigOutput.setProperty("INSERT","\n**********One Or More ADCs Failed*******************\n");
				}else{
					Txt_DigOutput.setProperty("INSERT","\n**********ADC Calibration successful*******************\n");
				}
			
			}else{
				Txt_DigOutput.setProperty("INSERT","\n**********ADC Calibration Failed*******************\n");
			}
		
			
		}else{
			Txt_DigOutput.setProperty("INSERT","\n**********One Or More Channels Not Ready*******************\n");
		}
		
		
		
		return 0;
		
		
	}
	
	
	public boolean Read_Dig_Ch_Enable_Settings(){
		
		int ReadData[]={0};
		//ReadData[0]=0xE5CB;
		int readResult=Read_DIG_Register(0x8120,0x09,ReadData);
		
		if(readResult==1){
			
		int ChannelMask[]=new int[16];
		
	    for(int j=0;j<16;j++){
		
			ChannelMask[j]=((ReadData[0]>>j)&0001);
			Chk_EnbleCh[j].setProperty("VALUE",String.valueOf(ChannelMask[j]));
		}
		
		}
		
		return true;		
		
		
		}
			
		
	public boolean Read_Dig_DCOffset_Settings(){
			
			int ReadData[]={0};
			
			int readResult=0;
			
			for(int j=0;j<16;j++){
		
				readResult=Read_DIG_Register(0x1098+(0x100*j),0x09,ReadData);
				if(readResult==1){
					Txt_DCOffset[j].setProperty("VALUE",String.valueOf((ReadData[0]&0x00FFFF)));
				}
			
			}
			
			return true;		
		
		}
		
		
		
		public boolean Read_Dig_TrigThd_Settings(){
			
			int ReadData[]={0};
			
			int readResult=0;
			
			for(int j=0;j<16;j++){
		
				readResult=Read_DIG_Register(0x1080+(0x100*j),0x09,ReadData);
				if(readResult==1){
					Txt_TrigThd[j].setProperty("VALUE",String.valueOf((ReadData[0]&0x03FFF)));
				}
			
			}
			
			return true;		
		
		}
		
		
		
		public boolean Read_DIG_DynamicRange_Settings(){
			
			int ReadData[]={0};
			
			int readResult=0;
			
			for(int j=0;j<16;j++){
		
				readResult=Read_DIG_Register(0x1028+(0x100*j),0x09,ReadData);
				if(readResult==1){
					Cbox_DynamicRange[j].setProperty("VALUE",String.valueOf((ReadData[0]&0x0001)));
				}
			
			}
			
			return true;		
		
		}
		
		
		
		
		public boolean Read_DIG_SelfTrigger_Settings(){
			
			int ReadData[]={0};
			
			int readResult=0;
			
			for(int j=0;j<8;j++){
		
				readResult=Read_DIG_Register(0x1084+(0x100*(j*2)),0x09,ReadData);
				if(readResult==1){
					Cbox_SelfTrig_G[j].setProperty("VALUE",String.valueOf((ReadData[0]&0x0003)));
				}
			
			}
			
			return true;		
		
		}
		
		
		
		public int Read_DIG_GolbTrigger_AND_TrigOut_Settings(){
			

			//*******************************************Global Trigger**********************************************//
			int readData[]={0};
			int readResult=0;
			int extTrigger_GlobalMask=0;
			int LDVSTrigger_GlobalMask=0;
			int extTrigger_TrigOut=0;
			int LDVSTrigger_TrigOut=0;
			
			readResult=Read_DIG_Register(0x810C,0x09,readData);
			
			if(readResult==1){
					
					for(int j=0;j<8;j++){
						
					Cbox_GlobTrig_G[j].setProperty("VALUE",String.valueOf(((readData[0]>>j)&0x1)));
					
					}


			//External trigger
			extTrigger_GlobalMask=(readData[0]&0x40000000)>>30;
			
			//LDVS trigger
			LDVSTrigger_GlobalMask=(readData[0]&0x20000000)>>29;
			
					
			}

				
			//*******************************************Trigger Out***************************************************//


			readResult=readResult&Read_DIG_Register(0x8110,0x09,readData);
			
			if(readResult==1){
					
					for(int j=0;j<8;j++){
						
					Cbox_TrigOut_G[j].setProperty("VALUE",String.valueOf(((readData[0]>>j)&0001)));
					
					}


			//External trigger
			extTrigger_TrigOut=(readData[0]&0x40000000)>>30;
			
			//LDVS trigger
			LDVSTrigger_TrigOut=(readData[0]&0x20000000)>>29;
				
					
			}

			//Ext trigger option
			if(extTrigger_GlobalMask==1){

				if(extTrigger_TrigOut==1){

					Cbox_ExtTrigger.setProperty("VALUE","3");
				}else{

					Cbox_ExtTrigger.setProperty("VALUE","2");
				}
				
			}else{

				if(extTrigger_TrigOut==1){

					Cbox_ExtTrigger.setProperty("VALUE","1");
				}else{

					Cbox_ExtTrigger.setProperty("VALUE","0");
				}
				

				
			}


			//LDVS trigger option
			if(LDVSTrigger_GlobalMask==1){

				if(LDVSTrigger_TrigOut==1){

					Cbox_LDVSTrigger.setProperty("VALUE","3");
				}else{

					Cbox_LDVSTrigger.setProperty("VALUE","2");
				}
				
			}else{

				if(LDVSTrigger_TrigOut==1){

					Cbox_LDVSTrigger.setProperty("VALUE","1");
				}else{

					Cbox_LDVSTrigger.setProperty("VALUE","0");
				}
				

				
			}


			return readResult;		
		
		}
		
		public int Read_Dig_PulsePolarity_AND_TestPattern_Settings(){
			
				int PulsePolarity_Option=Integer.parseInt(Cbox_PulsePolarity.getProperty("VALUE"));
				int Pattern_Option=Integer.parseInt(Cbox_TestPattern.getProperty("VALUE"));
				int readData[]={0x0};
				int readResult=Read_DIG_Register(0x8000,0x9,readData);

				if(readResult==1){ 

						if((readData[0]&0xF)==0x0){ //test pattern disabled 

							Cbox_TestPattern.setProperty("VALUE","0");
						}else{

							Cbox_TestPattern.setProperty("VALUE","1");
						}

						if((readData[0]&0xF0)==0x50){ //Negative polarity 

							Cbox_PulsePolarity.setProperty("VALUE","1");
						}else{

							Cbox_PulsePolarity.setProperty("VALUE","0");
						}

				}

				
				return readResult;
			
		}


		/**
		* This method will read post trigger settings from the digitizer 
		* and update the properties panel 
		*/
		public int Read_Dig_PostTrigger_Settings(){
			
			int readData[]={0};
						
			int readResult=Read_DIG_Register(0x8114,0x09,readData);
			
			if(readResult==1){

				Txt_PostTrigger.setProperty("VALUE",Integer.toUnsignedString(readData[0]*4));	
				
			}
			
			
			return readResult;	
			
		}



		/**
		* This method will read record length settings from the digitizer 
		* and update the properties panel
		*/
		public int Read_Dig_RecordLength_Settings(){
			
			int readData[]={0};
				
			int readResult=Read_DIG_Register(0x8020,0x09,readData);
			
			if(readResult==1){
				
				Txt_RecordLength.setProperty("VALUE",Integer.toUnsignedString(readData[0]*10));				
				//recordLength=(readData[0]*10)+4;
				
				
			}
			
			
			return readResult;	
	
					
		}


		/**
		* This method will read LDVS new features settings from the digitizer 
		* and update the properties panel 
		*/
		public int Read_Dig_LDVSFeatures_Settings(){
			
			int readData[]={0};
			int writeData[]={0};
			int LDVS_Directions=0;
			int IOResult=0;
			
			IOResult=Read_DIG_Register(0x811C,0x09,readData);

			if(IOResult==1){

				if((readData[0]&0x100)==0x100){  //LDVS new features enabled
	
					Chk_LDVS_enable.setProperty("VALUE","1");

					LDVS_Directions=((readData[0]>>2)&0xF);
					Cbox_LDVS_G[0].setProperty("VALUE",Integer.toString((LDVS_Directions&0x1)));
					Cbox_LDVS_G[1].setProperty("VALUE",Integer.toString((LDVS_Directions&0x2)>>1));
					Cbox_LDVS_G[2].setProperty("VALUE",Integer.toString((LDVS_Directions&0x4)>>2));
					Cbox_LDVS_G[3].setProperty("VALUE",Integer.toString((LDVS_Directions&0x8)>>3));

					Enable_Disable_LDVS_CombBoxes(1);

				}else{  //LDVS new features disabled

					Chk_LDVS_enable.setProperty("VALUE","0");
					Enable_Disable_LDVS_CombBoxes(0);
				
				}

			}

			
			return IOResult;
		}


		

		
		public void $Chk_EnableCh_Common(KmaxWidget widget) {
		
			String tempStr=Chk_EnableCh_Common.getProperty("VALUE");
		
			for(int j=0;j<16;j++){
				Chk_EnbleCh[j].setProperty("VALUE",tempStr);
			}
		
		}
		
		
		public void $Txt_BaseLine_Common(KmaxWidget widget){
			
			String tempStr=Txt_BaseLine_Common.getProperty("VALUE");
		
			for(int j=0;j<16;j++){
				Txt_BaseLine[j].setProperty("VALUE",tempStr);
			}
				
			
		}
		
		
		public void $Txt_DCOffset_Common(KmaxWidget widget){
			
			String tempStr=Txt_DCOffset_Common.getProperty("VALUE");
		
			for(int j=0;j<16;j++){
				Txt_DCOffset[j].setProperty("VALUE",tempStr);
			}
				
			
		}
		
		
		
		public void $Txt_TrigThd_Common(KmaxWidget widget){
			
			String tempStr=Txt_TrigThd_Common.getProperty("VALUE");
		
			for(int j=0;j<16;j++){
				Txt_TrigThd[j].setProperty("VALUE",tempStr);
			}
			
		}
		
		
		public void $Cbox_DynamicRange_Common(KmaxWidget widget){
			
			String tempStr=Cbox_DynamicRange_Common.getProperty("VALUE");
		
			for(int j=0;j<16;j++){
				Cbox_DynamicRange[j].setProperty("VALUE",tempStr);
			}
			
		}
		
		
		public void $Cbox_SelfTrig_Common(KmaxWidget widget){
				
			String tempStr=Cbox_SelfTrig_Common.getProperty("VALUE");
		
			for(int j=0;j<8;j++){
				Cbox_SelfTrig_G[j].setProperty("VALUE",tempStr);
			}
			
			
		}
		
		public void $Cbox_GlobTrig_Common(KmaxWidget widget){
				
			String tempStr=Cbox_GlobTrig_Common.getProperty("VALUE");
		
			for(int j=0;j<8;j++){
				Cbox_GlobTrig_G[j].setProperty("VALUE",tempStr);
			}
			
			
		}
				
		public void $Cbox_TrigOut_Common(KmaxWidget widget){
				
			String tempStr=Cbox_TrigOut_Common.getProperty("VALUE");
		
			for(int j=0;j<8;j++){
				Cbox_TrigOut_G[j].setProperty("VALUE",tempStr);
			}
			
			
		}
		
		
		
		/**
		* This method will write channel enable settings to the digitizer 
		* from the properties panel 
		*/
		public int Write_Dig_Ch_Enable_Settings(){
			
			int regValue=0;
			int chBit=0;
			
			for(int j=0;j<16;j++){
			
				chBit=Integer.parseInt(Chk_EnbleCh[j].getProperty("VALUE"));
				regValue=regValue|(chBit<<j);
			
			}
		
				
			int WriteData[]={regValue};
		
			int writeResult=Write_DIG_Register(0x8120,0x09,WriteData);
	
			return writeResult;
			
			
		}
		
		
		
		/**
		* This method will write baseline settings to the digitizer 
		* from the properties panel 
		*/
		public int Write_Dig_Baseline_Settings(){
			
			int ReadData[]={0};
			int writeResult=0;
			int returnValue=1;
			
			for(int j=0;j<16;j++){
				
				ReadData[0]=Integer.parseInt(Txt_BaseLine[j].getProperty("VALUE"));
				writeResult=Write_DIG_Register(0x1098+(0x100*j),0x09,ReadData);
				
				if(writeResult!=1){
					
					 returnValue=0;
				}
			
			}
			
			return returnValue;		
			
		}
		
		
		
		
		/**
		* This method will write DCoffset settings to the digitizer 
		* from the properties panel 
		*/
		public int Write_Dig_DCOffset_Settings(){
			
			int ReadData[]={0};
			int writeResult=0;
			int returnValue=1;
			
			for(int j=0;j<16;j++){
				
				ReadData[0]=Integer.parseInt(Txt_DCOffset[j].getProperty("VALUE"));
				writeResult=Write_DIG_Register(0x1098+(0x100*j),0x09,ReadData);
				
				if(writeResult!=1){
					
					 returnValue=0;
				}
			
			}
			
			return returnValue;		
		
			
		}
		
		
		
		/**
		* This method will write Trigger threshold settings to the digitizer 
		* from the properties panel 
		*/
		public int Write_Dig_TrigThd_Settings(){
			
			int ReadData[]={0};
			int writeResult=0;
			int returnValue=1;
			
			for(int j=0;j<16;j++){
				
				ReadData[0]=Integer.parseInt(Txt_TrigThd[j].getProperty("VALUE"));
				writeResult=Write_DIG_Register(0x1080+(0x100*j),0x09,ReadData);
				
				if(writeResult!=1){
					
					 returnValue=0;
					 
					 break;
				}
			
			}
			
			return returnValue;	
			
		}
		
		
		
		/**
		* This method will write dynamic range settings to the digitizer 
		* from the properties panel 
		*/
		public int Write_Dig_DynamicRange_Settings(){
			
			int ReadData[]={0};
			int writeResult=0;
			int returnValue=1;
			
			for(int j=0;j<16;j++){
				
				ReadData[0]=Integer.parseInt(Cbox_DynamicRange[j].getProperty("VALUE"));
				writeResult=Write_DIG_Register(0x1028+(0x100*j),0x09,ReadData);
				
				if(writeResult!=1){
					
					 returnValue=0;
					 
					 break;
				}
			
			}
			
			return returnValue;
		
			
		}
		
		
		
		/**
		* This method will write self trigger settings to the digitizer 
		* from the properties panel 
		*/
		public int Write_Dig_SelfTrigger_Settings(){
			
			int ReadData[]={0};
			int writeResult=0;
			int returnValue=1;
			
			for(int j=0;j<8;j++){
				
				ReadData[0]=Integer.parseInt(Cbox_SelfTrig_G[j].getProperty("VALUE"));
				writeResult=Write_DIG_Register(0x1084+(0x100*(j*2)),0x09,ReadData);
				
				if(writeResult!=1){
					
					 returnValue=0;
					 
					 break;
				}
			
			}
			
			return returnValue;
			
		}
		
		
		
		/**
		* This method will write global trigger settings to the digitizer 
		* from the properties panel 
		*/
		public int Write_Dig_GlobTrigger_Settings(){
			
			int writeData[]={0};
						
			int triggerMask=0x0;
			int groupBit=0;
			
			
			for(int j=0;j<8;j++){
				
				groupBit=Integer.parseInt(Cbox_GlobTrig_G[j].getProperty("VALUE"));
				triggerMask=triggerMask|(groupBit<<j);
			
			
			}

			//Ext trigger 
			int ExtTrigger_Option=Integer.parseInt(Cbox_ExtTrigger.getProperty("VALUE"));

			if((ExtTrigger_Option==2)||(ExtTrigger_Option==3)){
					
				triggerMask=triggerMask|0xC0000000;
				
			}

			//LDVS trigger
			int LDVSTrigger_Option=Integer.parseInt(Cbox_LDVSTrigger.getProperty("VALUE"));


			if((LDVSTrigger_Option==2)||(LDVSTrigger_Option==3)){
					
				triggerMask=triggerMask|0xA0000000;
				
			}

			writeData[0]=triggerMask;
			int IOresult=Write_DIG_Register(0x810C,0x09,writeData);
				
			return IOresult;
	
					
		}
		
		
		/**
		* This method will write front pannel trigger-out settings to the digitizer 
		* from the properties panel 
		*/
		public int Write_Dig_TrigOut_Settings(){
			
			int writeData[]={0};
			int triggerMask=0;
			int groupBit=0;
			
			
			for(int j=0;j<8;j++){
				
				groupBit=Integer.parseInt(Cbox_TrigOut_G[j].getProperty("VALUE"));
				triggerMask=triggerMask|(groupBit<<j);
			
			
			}

			//Ext trigger 
			int ExtTrigOut_Option=Integer.parseInt(Cbox_ExtTrigger.getProperty("VALUE"));

			if((ExtTrigOut_Option==1)||(ExtTrigOut_Option==3)){
					
				triggerMask=triggerMask|0xC0000000;
				
			}

			//LDVS trigger
			int LDVSTrigOut_Option=Integer.parseInt(Cbox_LDVSTrigger.getProperty("VALUE"));


			if((LDVSTrigOut_Option==1)||(LDVSTrigOut_Option==3)){
					
				triggerMask=triggerMask|0xA0000000;
				
			}
			
			writeData[0]=triggerMask;
			int IOresult=Write_DIG_Register(0x8110,0x09,writeData);
			
			return IOresult;	
	
					
		}
		
		
		/**
		* This method will write record length settings to the digitizer 
		* from the properties panel and change the the size of the pulse data array according to record length
		*/
		public int Write_Dig_RecordLength_Settings(){
			
			int writeData[]={0};
			int returnValue=0;

						
			writeData[0]=(Integer.parseUnsignedInt(Txt_RecordLength.getProperty("VALUE")))/10;
			int writeResult=Write_DIG_Register(0x8020,0x09,writeData);
			
			if(writeResult==1){
				
				RLength=Integer.parseUnsignedInt(Txt_RecordLength.getProperty("VALUE"));		
				returnValue=1;
				
			}
			
			
			return returnValue;	
	
					
		}
		
		
		
		/**
		* This method will write post trigger settings to the digitizer 
		* from the properties panel 
		*/
		public int Write_Dig_PostTrigger_Settings(){
			
			/*
			int writeData[]={0};
			int NumPostTrigSamples=Integer.parseUnsignedInt(Txt_PostTrigger.getProperty("VALUE"));	
			writeData[0]=(NumPostTrigSamples-80)/8; //80 is the constant ConstantLatency
			if(writeData[0]<0){
				writeData[0]=5;
			}
			int writeResult=Write_DIG_Register(0x8114,0x09,writeData);
			
			return writeResult;	
			*/
			
			
			int writeData[]={0};
			int NumPostTrigSamples=Integer.parseUnsignedInt(Txt_PostTrigger.getProperty("VALUE"));	
			writeData[0]=(NumPostTrigSamples)/4; //112 is the constant ConstantLatency
			//if(writeData[0]<28){
			//	writeData[0]=28;
			//}
			int writeResult=Write_DIG_Register(0x8114,0x09,writeData);
			
			return writeResult;	
			
		}

			
		/**
		* This method will write test pattern enable/disable pulse polarity settings to the digitizer 
		* from the properties panel 
		*/
		public int Write_Dig_PulsePolarity_AND_TestPattern_Settings(){
			
				int PulsePolarity_Option=Integer.parseInt(Cbox_PulsePolarity.getProperty("VALUE"));
				int Pattern_Option=Integer.parseInt(Cbox_TestPattern.getProperty("VALUE"));
				int writeData[]={0x0};
				int writeResult=0;
		

				if(PulsePolarity_Option==0){ //Positive polarity

					if(Pattern_Option==0){ 
					//test pattern disabled
				
						writeData[0]=0x10;
					}				
					else{ //test pattern enabled
				
						writeData[0]=0x18;
				
					}

				}else{ //Negative polarity 

					if(Pattern_Option==0){ 
					//test pattern disabled
				
						writeData[0]=0x50;

					}else{ //test pattern enabled

						writeData[0]=0x58; 
					}

							

				}	


				writeResult=Write_DIG_Register(0x8000,0x9,writeData);
				
				return writeResult;
			
		}

		
		/**
		* This method will write LDVS new features settings to the digitizer 
		* from the properties panel 
		*/
		public int Write_Dig_LDVSFeatures_Settings(){
			
			int readData[]={0};
			int writeData[]={0};
			int Mask=0xFFFFFEC3;
			int IOResult=0;
			
			int LDVSEnable=Integer.parseInt(Chk_LDVS_enable.getProperty("VALUE"));
			Read_DIG_Register(0x811C,0x09,readData);
			
			if(LDVSEnable==1){
				
				writeData[0]=0x100; //enable LDVS new features
				
				writeData[0]=writeData[0]+(Integer.parseInt(Cbox_LDVS_G[0].getProperty("VALUE"))<<2);     //group input output 
				writeData[0]=writeData[0]+(Integer.parseInt(Cbox_LDVS_G[1].getProperty("VALUE"))<<3);
				writeData[0]=writeData[0]+(Integer.parseInt(Cbox_LDVS_G[2].getProperty("VALUE"))<<4);
				writeData[0]=writeData[0]+(Integer.parseInt(Cbox_LDVS_G[3].getProperty("VALUE"))<<5);
				
				writeData[0]=(readData[0]&Mask)|writeData[0];
				
				IOResult=Write_DIG_Register(0x811C,0x09,writeData);
				
				//nBusy-nVETO function
				writeData[0]=0x2222;
				IOResult=IOResult&Write_DIG_Register(0x81A0,0x09,writeData);
				
				
				
			}else{
				
				writeData[0]=readData[0]^0x00000100;
				IOResult=Write_DIG_Register(0x811C,0x09,writeData);
				
			}
			
			return IOResult;
		}
		
		
		
		public void $Chk_LDVS_enable(KmaxWidget widget){
			
			int enableLDVS=Integer.parseInt(Chk_LDVS_enable.getProperty("VALUE"));	
			Enable_Disable_LDVS_CombBoxes(enableLDVS);
			
		
			
		}

		public void Enable_Disable_LDVS_CombBoxes(int LDVSenabled){
			
			if(LDVSenabled==1){
				
				Cbox_LDVS_G[0].setProperty("ENABLED","TRUE");
				Cbox_LDVS_G[1].setProperty("ENABLED","TRUE");
				Cbox_LDVS_G[2].setProperty("ENABLED","TRUE");
				Cbox_LDVS_G[3].setProperty("ENABLED","TRUE");
				
				Cbox_LDVS_F[0].setProperty("ENABLED","TRUE");
				Cbox_LDVS_F[1].setProperty("ENABLED","TRUE");
				Cbox_LDVS_F[2].setProperty("ENABLED","TRUE");
				Cbox_LDVS_F[3].setProperty("ENABLED","TRUE");
				
			}else{
				
				Cbox_LDVS_G[0].setProperty("ENABLED","FALSE");
				Cbox_LDVS_G[1].setProperty("ENABLED","FALSE");
				Cbox_LDVS_G[2].setProperty("ENABLED","FALSE");
				Cbox_LDVS_G[3].setProperty("ENABLED","FALSE");
				
				Cbox_LDVS_F[0].setProperty("ENABLED","FALSE");
				Cbox_LDVS_F[1].setProperty("ENABLED","FALSE");
				Cbox_LDVS_F[2].setProperty("ENABLED","FALSE");
				Cbox_LDVS_F[3].setProperty("ENABLED","FALSE");
			}


		}
		
		
		/**
		* This method will adjust some parameters of digitizer modules 
		* to provide protection from event misalignment
		*/
		public void Write_DIG_EventSync_Protection_Parameters(){
			
			int IOResult=0;
			int writeData[]={0};
			int readData[]={0};
			
			//set number of buffers per channel
			int bufferCode=0xA;
			writeData[0]=bufferCode;
			IOResult=Write_DIG_Register(0x800C,0x09,writeData);
			
			//raise the BUSY signal when 90% of the buffers are FULL
			int AlmostFullLevel=(int)(0.60*Math.pow(2,bufferCode));
			writeData[0]=AlmostFullLevel;
			IOResult=Write_DIG_Register(0x816C,0x09,writeData);

			//set 48bit TTT
			IOResult=Read_DIG_Register(0x811C,0x09,readData);
			writeData[0]=(readData[0]&0xFF9FFFFF)|0x00400000;
			IOResult=Write_DIG_Register(0x811C,0x09,writeData);
			
			//Bus error and 64Bit align
			//writeData[0]=0x30;
			//IOResult=Write_DIG_Register(0xEF00,0x09,writeData);
			
			//set extended VETO
			writeData[0]=0xF;
			IOResult=Write_DIG_Register(0x81C4,0x09,writeData);
			
			
			
		}
		
		
		
		public void Btn_ReadRegister(KmaxWidget widget){
			
			int regAddr=Integer.parseInt(Txt_RegAddr.getProperty("VALUE"),16);
			int regData[]={0};
			int readResult=Read_DIG_Register(regAddr,0x9,regData);
			
			if(readResult==1){
				Txt_RegValue.setProperty("VALUE",Integer.toHexString(regData[0]).toUpperCase());	
			}else{
				Txt_RegValue.setProperty("VALUE","########");
			}
			
		}
		
		
		public void Btn_WriteRegister(KmaxWidget widget){
			
			int regAddr=Integer.parseUnsignedInt(Txt_RegAddr.getProperty("VALUE"),16);
			int regValue=Integer.parseUnsignedInt(Txt_RegValue.getProperty("VALUE"),16);
			//int AddrMod=Integer.parseInt(C.getProperty("VALUE"),16);
			
			int tempData[]={regValue};
			
			Write_DIG_Register(regAddr,0x9,tempData);
			
		}
		
		
		public void Btn_Reset(KmaxWidget widget){
			
			Reset_Digitizer();
			
		}
		
		
		public void Btn_Clear(KmaxWidget widget){
			
			Clear_Digitizer();
			
		}
		
		
		public void Btn_ADCCal(KmaxWidget widget){
			
			ADC_Calibration();
			
			
		}
		
		public void Btn_ClockSync(KmaxWidget widget){
			
			DIG_ClockSync();
			
			
		}
		
		
		public void Btn_BoardInfor(KmaxWidget widget){
			
			DIG_GetBoardInfor();
			
			
		}
		
		
		public void Btn_ChannelStatus(KmaxWidget widget){
			
			Get_DIG_ChStatus();
			
			
		}
		
	
	
		public void Btn_Inti(KmaxWidget widget){
			
			if(!IsIntialRunNumSet){
				
				Set_InItial_RunNumber();
				
			}
			
			Initialize_DAQ();
			Btn_Start.setProperty("ENABLED","TRUE");
			
			
			if(IsAutomated){
				runTimer=new Timer();
			}


			for(int j=0;j<BaseAddr_module.length;j++){
				setEventSize[j]=false;
			}

			HistFillTimer=new Timer();
			
		}	
		
		
		public void Btn_Start(KmaxWidget widget){
			
			
			
			TimerTask repeatedTask = new TimerTask() {
        		public void run() {
					
            		Stop_DAQ();	
					try{
					 Thread.sleep(500);
					}catch(InterruptedException e){
						
						e.printStackTrace();
		
					}
					Initialize_DAQ();
					Start_DAQ();	
			
        		}
    			};
			
			Start_DAQ();
			IsDAQRunning=true;
			
			if(IsAutomated){
				
				runDuration=(Integer.parseInt(Txt_RunDuration.getProperty("VALUE")))*60000;
				runTimer.scheduleAtFixedRate(repeatedTask,runDuration, runDuration);
				
			}
			
			//Disable Digitizer Widgets
			Btn_Stop.setProperty("ENABLED","TRUE");
			Btn_Start.setProperty("ENABLED","FALSE");
			Enable_DIG_Widgets(false);
			Cbox_DigModule.setProperty("ENABLED","FALSE");
			Btn_Inti.setProperty("ENABLED","FALSE");	
			Chk_AutoRunControl.setProperty("ENABLED","FALSE");
			Chk_WriteData.setProperty("ENABLED","FALSE");
			Txt_RunDuration.setProperty("ENABLED","FALSE");

			///////////////Histogram Fill Timer Task/////////////////////////
			TimerTask histFillTask = new TimerTask() {
			int HistUpdateCount=0;
			
        		public void run() {

				HistUpdateCount+=1;
				Write_Event_to_Histograms();	
				if(HistUpdateCount==60000){ //1 mins
					Update_Histograms();
					HistUpdateCount=0;
				}
			
        		}
    			};	
			
			HistFillTimer.scheduleAtFixedRate(histFillTask,5000,1);
			
		}
		
		
		public void Btn_Stop(KmaxWidget widget){
			
			Stop_DAQ();
			IsDAQRunning=false;	
			
			if(IsAutomated){		
				runTimer.cancel(); 
			}
			
			
			//Disable Digitizer Widgets
			Btn_Stop.setProperty("ENABLED","FALSE");
			Btn_Inti.setProperty("ENABLED","TRUE");
			Enable_DIG_Widgets(true);
			Cbox_DigModule.setProperty("ENABLED","TRUE");
			Chk_AutoRunControl.setProperty("ENABLED","TRUE");
			Chk_WriteData.setProperty("ENABLED","TRUE");
			Txt_RunDuration.setProperty("ENABLED","TRUE");

			HistFillTimer.cancel();
			
			Refresh_DIG_Properties_Tab();
				
			
		}
		
		public void Btn_SwTrigger(KmaxWidget widget){
			
			int tempData[]={0xFFFF};
			Write_DIG_Register(0x8108,0x9,tempData);
			
			
		}
		
		public void Btn_EditFilelist(KmaxWidget widget){
			
			try{
				 //if(runAnalysis!=null){
					//runAnalysis.destroy();
				 //}
				 Process editFilelist=java.lang.Runtime.getRuntime().exec("gedit /home/mkovash/Kmax_Stuff/offline/filelist.txt");
			}catch(Exception excep){
				excep.printStackTrace();
			}
			
			
		}
		
		
		public void Chk_AutoRunControl(KmaxWidget widget){
			
			int runControlStatus=Integer.parseInt(Chk_AutoRunControl.getProperty("VALUE"));
			
			if(runControlStatus==1){
				
				Txt_RunDuration.setProperty("ENABLED","TRUE");
				IsAutomated=true;
				
				
			}else{
				
				Txt_RunDuration.setProperty("ENABLED","FALSE");
				IsAutomated=false;
			}
		}
		
		public void Chk_WriteData(KmaxWidget widget){
			
			int WriteStatus=Integer.parseInt(Chk_WriteData.getProperty("VALUE"));
			
			if(WriteStatus==1){
				
				IsWriteEnabled=true;
				
			}else{
				IsWriteEnabled=false;
			}
		}
		
		
		public void Chk_EnablePlot(KmaxWidget widget) {

			
			int plotValue=Integer.parseInt(Chk_EnablePlot.getProperty("VALUE"));
			
			if(plotValue==1){

				IsPlottingEnabled=true;
				
			}else{

				IsPlottingEnabled=false;
				clearPlots();
			}

		}


		public void clearPlots(){
			
			Plot_Waveform.clear();
			for(int l=0;l<7;l++){
				PulsePlot_[l].clear();
			
			}
	
		}
		
		
		
		public void Btn_DisplayAnalysis(KmaxWidget widget){
			
			try{
				 if(runAnalysis!=null){
					runAnalysis.destroy();
				 }
				 runAnalysis=java.lang.Runtime.getRuntime().exec("/home/mkovash/Kmax_Stuff/offline/DIANA");
			}catch(Exception excep){
				excep.printStackTrace();
			}

		}
		
		
		public void Btn_SaveSettings(KmaxWidget widget)throws IOException {
			
			String filePath = tlsh.showFileChooser("Create Digitizer Settings FIle", "config", "New");
			
			if(filePath ==null) {
				tlsh.setProperty("STATUSSTR", "The file choolser returned null.");
				return;
			}

			int Dig_RegValue[]=new int[Dig_RegAddr.length];
			int err=1;

			for(int j=0;j<Dig_RegAddr.length;j++){
				err=dev.readInt(BASEAddr,Dig_RegAddr[j],0x9,Dig_RegValue,j,1);

				if(err!=0){
					Txt_DigOutput.setProperty("INSERT","\n\n********Error occurred !!! settings not saved**********\n\n");
					break;
				}

			}
			
			
			FileWriter write = new FileWriter( filePath+".config" , false);
			PrintWriter print_line = new PrintWriter( write );
			
					
			

			for(int j=0;j<Dig_RegAddr.length;j++){
				print_line.printf( "%d" + "%n" ,Dig_RegValue[j]);
			}

			print_line.close();
			Txt_DigOutput.setProperty("INSERT","\n*********settings saved*****************\n");
			
			
		}
		
		public void logDigitizerSettings(){
			
			int Dig_RegValue[]=new int[Dig_RegAddr.length];
			int err=1;

			FileWriter writer=null;
			PrintWriter print_line=null;

			for(int k=0;k<BaseAddr_module.length;k++){
			for(int j=0;j<Dig_RegAddr.length;j++){
				err=dev.readInt(BaseAddr_module[k],Dig_RegAddr[j],0x9,Dig_RegValue,j,1);

				if(err!=0){
					 TxtErrMsgs.setProperty("INSERT", "Error occurred while saving digitizer settings !!!\n");	
					break;
				}

			}
			
			writer = new FileWriter( "/home/mkovash/Kmax_Stuff/Settings/Module_"+Integer.toString(k+1)+"/run_"+Integer.toString(runNumber)+".config" , false);
			print_line = new PrintWriter( writer );
			
					
			

			for(int j=0;j<Dig_RegAddr.length;j++){
				print_line.printf( "%d" + "%n" ,Dig_RegValue[j]);
			}

			print_line.close();
			
			}
		}
		
		
		public void Btn_LoadSettings(KmaxWidget widget)throws IOException {
				
			String filePath = tlsh.showFileChooser("Open Digitizer Settings FIle", "config", "Open");
			
			if(filePath ==null) {
				tlsh.setProperty("STATUSSTR", "The file choolser returned null.");
				return;
				
			}
			
			
			FileReader read = new FileReader(filePath);
			BufferedReader textReader=new BufferedReader(read);
						
			int writeData[]={0};
			
			for(int j=0;j<Dig_RegAddr.length;j++){
				writeData[0]=Integer.parseInt(textReader.readLine());
				Write_DIG_Register(Dig_RegAddr[j],0x9,writeData);

			}

			textReader.close();
			
			//Read back settings from the digitizer
			Read_Dig_Ch_Enable_Settings();
			Read_Dig_DCOffset_Settings();
			Read_Dig_TrigThd_Settings();
			Read_DIG_DynamicRange_Settings();
			Read_DIG_SelfTrigger_Settings();
			Read_DIG_GolbTrigger_AND_TrigOut_Settings();
			Read_Dig_PulsePolarity_AND_TestPattern_Settings();
			Read_Dig_LDVSFeatures_Settings();
			Read_Dig_RecordLength_Settings();
			Read_Dig_PostTrigger_Settings();
			
			
			
		}
		
		
		public void Btn_UpdateSettings(KmaxWidget widget){
			
			int updateResult=0;
			
			updateResult+=Write_Dig_Ch_Enable_Settings();
			updateResult+=Write_Dig_DCOffset_Settings();
			updateResult+=Write_Dig_TrigThd_Settings();
			updateResult+=Write_Dig_DynamicRange_Settings();
			updateResult+=Write_Dig_SelfTrigger_Settings();
			updateResult+=Write_Dig_GlobTrigger_Settings();
			updateResult+=Write_Dig_TrigOut_Settings();
			updateResult+=Write_Dig_RecordLength_Settings();
			updateResult+=Write_Dig_PostTrigger_Settings();
			updateResult+=Write_Dig_PulsePolarity_AND_TestPattern_Settings();
			updateResult+=Write_Dig_LDVSFeatures_Settings();
			Write_DIG_EventSync_Protection_Parameters();
			
			
			if(updateResult==11){
				
				Txt_DigOutput.setProperty("INSERT","\n\n********Update Successful**********\n\n");				
						
			}else{

				Txt_DigOutput.setProperty("INSERT","\n\n********Update Not Completed !!!**********\n\n");
			}
			
		}
		
		
		public void Btn_ClearOutput(KmaxWidget widget){
		
			Txt_DigOutput.setProperty("TEXT","");
		
		}

		
		public void Btn_Reconfigure(KmaxWidget widget){

			int writeData[]={0xFFF};
			int writeResult=Write_DIG_Register(0xEF34,0x9,writeData);

			if(writeResult==1){
				Txt_DigOutput.setProperty("INSERT","\n\n********Configuration Reload Successful**********\n\n");
			}else{

				Txt_DigOutput.setProperty("INSERT","\n\n********Error in Configuration!!!*********\n\n");
			}
			

		}
		
		
		public void Set_InItial_RunNumber(){
			
			runNumber=Integer.parseUnsignedInt(Txt_InitialRunNumber.getProperty("VALUE"));
			IsIntialRunNumSet=true;
			Txt_InitialRunNumber.setProperty("ENABLED","FALSE");
			
		}


		/*************************************waveform plotting*************************************/
		public void plotEvent(int moduleNo) {	

			//TxtErrMsgs.setProperty("INSERT",Integer.toString(eventSize[moduleNo])+"\n");	
			short ChMask=(short)((pulseData[1]&0xFF)|((pulseData[2]&0xFF000000)>>16));
			int index,pulseIndex,chIndex;
			double[] dataPoint = new double[6];
			double RecordValue;
			
			chIndex=0;
			for(int k=0;k<16;k++){
				
				if(((ChMask>>k)&0x1)==1){
							
						for(int l=0;l<7;l++){	
							if(PlotSource[l]==(k+moduleNo*16)){
								
								PulsePlot_[l].clear();
								index=0;
								pulseIndex=4+(chIndex*RLength/2);
								while(index<RLength){

									if(index%2==0){
										RecordValue=(pulseData[pulseIndex]&0xFFFF);
									}else{
										RecordValue=pulseData[pulseIndex]>>>16;
										pulseIndex+=1;
									}

									dataPoint[0] = index;
									dataPoint[1] = RecordValue;
									dataPoint[2] = 0;
									dataPoint[3] = 0;
									dataPoint[4] = 0;
									dataPoint[5] = 0; 
									PulsePlot_[l].addDataPoint(dataPoint);
									index++;

				
								}
								PulsePlot_[l].update();
								Plot_Waveform.update();
							
								
						  }
						}
							
							
					chIndex++;
				}
			}
		

			
				
		}


	


		/*************************************Log file management*************************************/

		public void SetLogFile() throws IOException{
			
			logFile=new File("./LOGS/RunLog_"+java.time.LocalDate.now()+".log");
			
			if (!logFile.exists()){
				FileWriter logWriter = new FileWriter(logFile,true);
				PrintWriter print_line = new PrintWriter( logWriter );
				print_line.printf( "Run Number\t\tStart\t\tEnd\t\tScalar\n");
				print_line.close();
				
			}
			
			
		}
		
		
		public void UpdateRunLog() throws IOException{
			
			
			FileReader read = new FileReader(logFile);
			BufferedReader textReader=new BufferedReader(read);
			
			String tmp;
			Txt_RunLog.setProperty("TEXT","");
			while ((tmp = textReader.readLine()) != null)
 			{
     			Txt_RunLog.setProperty("INSERT",tmp+"\n");
  			}
			
			textReader.close();
			
			
		}
		
		/*************************************Data file management*************************************/
		
		public void Update_Ch_Readout_Mask(){
			
			int tempData[]={0x0};
			
			for(int j=0;j<BaseAddr_module.length;j++){
				Read_DIG_Register(BaseAddr_module[j],0x8120,0x9,tempData);
				chReadoutMask[j]=(short)(tempData[0]&0xFFFF);
			}
		}
		
		
		public boolean Create_Data_Files(int runNumber){

			String fileName="Run_"+Integer.toString(runNumber)+".bin";

			if(runNumber==-1){
				fileName="cache.bin";
				removeExistingCacheFiles();
			}
			
			FileOutputStream fout[]=new FileOutputStream[BaseAddr_module.length];

			for(int j=0;j<BaseAddr_module.length;j++){

				dataFile[j]=new File(String.format("./DATA/Module_%d/",j+1)+fileName);

				if (dataFile[j].exists()){

					tlsh.setProperty("STATUSSTR", "Data Files allready exist !!! Adjust intital run number");
					return false;
				}

				try{
					fout[j] = new FileOutputStream(dataFile[j]);
					dataBuffer[j] = new BufferedOutputStream(fout[j]);

				} catch (FileNotFoundException e){

					tlsh.setProperty("STATUSSTR", "Error creating data buffers !!!");
					return false;
				}
			
			}
									
			
			
			return true;
		}

		public void removeExistingCacheFiles(){

			for(int j=0;j<BaseAddr_module.length;j++){

				File CacheFile=new File(String.format("./DATA/Module_%d/",j+1)+"cache.bin");
				CacheFile.delete();
				
				
			}

		}
		
				
		/***************************************************Scalar functions***************************************************/
		/****************************************These methods manages VM-USB scalars******************************************/
		
		public void Setup_Scalars(){
			
			// Enable VM-USB scalars A and B as a combined 64bit scalar 
			int writeData[]={0x80540000};
			dev.writeInt(0,0x10,0x1000,writeData,0,1);
			
			writeData[0]=0x0980000;
			dev.writeInt(0,0x10,0x1000,writeData,0,1);
		}
		
		public long Read_Scalars(){
			
			//Freeze scalars
			int writeData[]={0x80540000};
			//dev.writeInt(0,0x10,0x1000,writeData,0,1);
			
			//Read Scalar data
			int scalarData[]={0,0};
			//dev.readInt(0,0x1C,0x1000,scalarData,0,1);
			//dev.readInt(0,0x20,0x1000,scalarData,1,1);
			
			//long Scalar_count=(long)(scalarData[0]+scalarData[1]);
			long Scalar_count=(long)(scalarData[0]);
			//Reset Scalars
			writeData[0]=0x80CD0000;
			//dev.writeInt(0,0x10,0x1000,writeData,0,1);
			
			return Scalar_count;
		
		}
		
		public void Reset_Scalars(){
			
			//Reset Scalars
			int writeData[]={0x80CD0000};
			dev.writeInt(0,0x10,0x1000,writeData,0,1);
		
		}
		
		
		public void Setup_SCLR(KmaxWidget widget){
			
			Setup_Scalars();
		}
		
		public void Read_SCLR(KmaxWidget widget){
			
			txt_scl.setProperty("TEXT",Long.toString(Read_Scalars()));;
		}
		
		/****************************************Charge Integration on pulseData*******************************************/
		public double QDC(short pulseData[]){
			
			int baseLine_length=50;
			int record_length=RLength;
			double Avg_baseLine=0;
			double Q=0;
			
			for(int i=0;i<baseLine_length;i++){
				
				Avg_baseLine+=pulseData[i];
			}
			Avg_baseLine=Avg_baseLine/baseLine_length;
			
			for(int i=0;i<record_length;i++){
				
				Q+=(Avg_baseLine-pulseData[i])*2;
				//Q+=(pulseData[i]-Avg_baseLine)*2;
			}
			
			Q=Q/1000;
			
			if(Q<0 || Q>20000){
				
				Q=0.0;
			}
			
			//TxtErrMsgs.setProperty("INSERT",Double.toString(Q)+"\n");
			return Q;
			
			
		}
		
		/***************************************Manage realtime Histograms********************************************************/
		
		public void Create_readBuffers(){
			
			FileInputStream fIn[] = new FileInputStream[BaseAddr_module.length];

			try{

				for(int j=0;j<BaseAddr_module.length;j++){
									
					fIn[j] = new FileInputStream(dataFile[j]);
					readBuffer[j] = new BufferedInputStream(fIn[j]);
			
				}
						
			} catch (FileNotFoundException e){
				
				tlsh.setProperty("STATUSSTR", "Error creating read buffers !!!");
			}
			
			
			
			
		}
		
		public void Write_Event_to_Histograms(){

			int eventHeader[]=new int[4];
			short ChMask;
			short pulse[];
			int chIndex,numBytesRead;
			byte readArray[];

			
			
		    	for(int j=0;j<BaseAddr_module.length;j++){
			//for(int j=0;j<1;j++){

				if(!setEventSize[j]){

					continue;
				}

				
				readArray=new byte[eventSize[j]*4];

				try{
					readBuffer[j].mark(0);
					numBytesRead=readBuffer[j].read(readArray,0,eventSize[j]*4);
					
			
				}catch (IOException e){
					TxtErrMsgs.setProperty("INSERT","read buffor exception\n");
					return;
				}
				
				if(numBytesRead==eventSize[j]*4){

					eventHeader[0]=byteToInt(readArray,0);
					eventHeader[1]=byteToInt(readArray,4);
					eventHeader[2]=byteToInt(readArray,8);
					eventHeader[3]=byteToInt(readArray,12);

					ChMask=(short)((eventHeader[1]&0xFF)|((eventHeader[2]&0xFF000000)>>16));
				
					chIndex=0;
					for(int k=0;k<16;k++){
				
						if(((ChMask>>k)&0x1)==1){
							
							pulse=byteToShortArray(readArray,16+chIndex*RLength*2,RLength);
							HistFill(Histchannel[k+(j*16)],QDC(pulse));
							chIndex++;
						}
					}

				}else if(numBytesRead<eventSize[j]*4){

					try{
						readBuffer[j].reset();
					}catch (IOException e){
						TxtErrMsgs.setProperty("INSERT","read buffor exception\n");
						return;
					}
				}
				

		    	}		
			
		}
		
		/*************************************************Buffer Managment************************************************/
		
		public static int byteToInt(byte[] b,int offset) {
    		
      		return ((b[offset]&0xff) << 24 )| (b[offset+1] & 0xff) << 16 | (b[offset+2] & 0xff) << 8 | (b[offset+3] & 0xff);
    
  		}


		public static short[] byteToShortArray(byte[] b,int offset,int recordLength) {
			
			short pluse[]=new short[recordLength];
			int index=0;
			for(int i=0;i<recordLength*2;i++){
				
				if(i%2==0){
					pluse[index]=(short)((b[offset+i]<<8)|(b[offset+i+1]&0xFF));
					index++;
				}else{
					continue;
				}
			}
						
			return pluse;
    
  		}


		public static byte[] InttoByte(int intValue){	//convert int to byte array


		byte byteArray[]=new byte[4];
		
		for(int j=0;j<4;j++){


			byteArray[3-j]=(byte)(intValue>>8*j);

		}

		return byteArray;

		

	}


	public static byte[] IntArraytoByte(int intArray[]){    //convert int array to byte a byte array


		byte byteArray[]=new byte[4*intArray.length];
		for(int j=0;j<intArray.length;j++){

			System.arraycopy(InttoByte(intArray[j]),0,byteArray,4*j,4);
		}

		return byteArray;
	}


	  public void HistFill(KmaxHist Hist,double QValue){
		
		int channelArray[]=Hist.get1DData();
		int Hscale=(int)(20000/channelArray.length);
		int Qchannel=(int)QValue/Hscale;
		channelArray[Qchannel]+=1;
		Hist.set1DData(channelArray);
		//Hist.update();
		
	  }
	

	public void Update_Histograms(){

		
		for(int i=0;i<16*BaseAddr_module.length;i++){
			Histchannel[i].update();
		}
		
		tlsh.setProperty("STATUSSTR", "Histograms updated !!!");

	}	
	
	public void Clear_Histograms(){
	
		for(int j=0;j<Histchannel.length;j++){
			Histchannel[j].clear();
		}
		
		tlsh.setProperty("STATUSSTR", "Histograms cleared !!!");
	
	}

	/**
	* The 'Btn_UpdateHistograms' method is executed when the widget activated.
	*/
	public void Btn_UpdateHistograms(KmaxWidget widget) {

		Update_Histograms();
		

	}
	
	public void Btn_ClearHistograms(KmaxWidget widget) {

		Clear_Histograms();

	}
	
	
	public void BLT(long BAddr,int destination[],int offset,int length){
		
		int err=dev.readInt(BAddr,0x0L,0x0B,destination,offset,length);
		
		
		if(err!=0 || (destination[offset]==0xFFFFFFFF)){

			TxtErrMsgs.setProperty("INSERT", "Error occurred while block transfer !!!\n");	

			if(err!=0){
				
				TxtErrMsgs.setProperty("INSERT",dev.getErrorMessage(err)+"\n");
				
			}else{

				TxtErrMsgs.setProperty("INSERT","Returned a NULL value !!!");
			}	
		}

			
		
	}
	
	public void RelocateVMEAddress(){
		
		int readoutCtrlData[]={0x140};
		int relocatedVMEAddr[]={0};
		
		for(int i=0;i<BaseAddr_module.length;i++){
			
			relocatedVMEAddr[0]=0x1000*(i+1);
			Write_DIG_Register(BaseAddr_module[i],0xEF00,0x09,readoutCtrlData);
			Write_DIG_Register(BaseAddr_module[i],0xEF10,0x09,relocatedVMEAddr);
			BaseAddr_module[i]=(0x10000000)*(i+1);
			
		}
		
	}
	
	public void GetPulseSources(){
		String Str="";
		for(int i=0;i<BaseAddr_module.length;i++){
			for(int j=0;j<16;j++){
				Str+=String.format("Module[%d]:Channel[%d]\n",i,j);
			}
		}
		
		for(int k=0;k<7;k++){
			Cbox_Source_[k].setProperty("ITEMS",Str);
		}
	}	
	
	/**
	* The 'Cbox_Source_0' method is executed when the widget activated.
	*/
	public void Cbox_Source_0(KmaxWidget widget) {

		PlotSource[0]=Integer.parseInt(Cbox_Source_[0].getProperty("VALUE"));
		PulsePlot_[0].clear();

	}
	
	
	public void Cbox_Source_1(KmaxWidget widget) {

		PlotSource[1]=Integer.parseInt(Cbox_Source_[1].getProperty("VALUE"));
		PulsePlot_[1].clear();

	}
	
	public void Cbox_Source_2(KmaxWidget widget) {

		PlotSource[2]=Integer.parseInt(Cbox_Source_[2].getProperty("VALUE"));
		PulsePlot_[2].clear();

	}
	
	
	public void Cbox_Source_3(KmaxWidget widget) {

		PlotSource[3]=Integer.parseInt(Cbox_Source_[3].getProperty("VALUE"));
		PulsePlot_[3].clear();

	}
	
	public void Cbox_Source_4(KmaxWidget widget) {

		PlotSource[4]=Integer.parseInt(Cbox_Source_[4].getProperty("VALUE"));
		PulsePlot_[4].clear();

	}
	
	public void Cbox_Source_5(KmaxWidget widget) {

		PlotSource[5]=Integer.parseInt(Cbox_Source_[5].getProperty("VALUE"));
		PulsePlot_[5].clear();

	}
	
	public void Cbox_Source_6(KmaxWidget widget) {

		PlotSource[6]=Integer.parseInt(Cbox_Source_[6].getProperty("VALUE"));
		PulsePlot_[6].clear();

	}

	public void ChkBox_Source_0(KmaxWidget widget) {

		int enabled=Integer.parseInt(ChkBox_Source_[0].getProperty("VALUE"));
		if(enabled==1){
			Plot_Waveform.addOverlay(PulsePlot_[0]);	
		
		}else{
			Plot_Waveform.removeOverlay(PulsePlot_[0]);	
		}
	}
	
	
	public void ChkBox_Source_1(KmaxWidget widget) {

		int enabled=Integer.parseInt(ChkBox_Source_[1].getProperty("VALUE"));
		if(enabled==1){
			Plot_Waveform.addOverlay(PulsePlot_[1]);	
		
		}else{
			Plot_Waveform.removeOverlay(PulsePlot_[1]);	
		}
	}
	
	public void ChkBox_Source_2(KmaxWidget widget) {

		int enabled=Integer.parseInt(ChkBox_Source_[2].getProperty("VALUE"));
		if(enabled==1){
			Plot_Waveform.addOverlay(PulsePlot_[2]);	
		
		}else{
			Plot_Waveform.removeOverlay(PulsePlot_[2]);	
		}
	}
	
	public void ChkBox_Source_3(KmaxWidget widget) {

		int enabled=Integer.parseInt(ChkBox_Source_[3].getProperty("VALUE"));
		if(enabled==1){
			Plot_Waveform.addOverlay(PulsePlot_[3]);	
		
		}else{
			Plot_Waveform.removeOverlay(PulsePlot_[3]);	
		}
	}
	
	public void ChkBox_Source_4(KmaxWidget widget) {

		int enabled=Integer.parseInt(ChkBox_Source_[4].getProperty("VALUE"));
		if(enabled==1){
			Plot_Waveform.addOverlay(PulsePlot_[4]);	
		
		}else{
			Plot_Waveform.removeOverlay(PulsePlot_[4]);	
		}
	}
	
	public void ChkBox_Source_5(KmaxWidget widget) {

		int enabled=Integer.parseInt(ChkBox_Source_[5].getProperty("VALUE"));
		if(enabled==1){
			Plot_Waveform.addOverlay(PulsePlot_[5]);	
		
		}else{
			Plot_Waveform.removeOverlay(PulsePlot_[5]);	
		}
	}
	
	public void ChkBox_Source_6(KmaxWidget widget) {

		int enabled=Integer.parseInt(ChkBox_Source_[6].getProperty("VALUE"));
		if(enabled==1){
			Plot_Waveform.addOverlay(PulsePlot_[6]);	
		
		}else{
			Plot_Waveform.removeOverlay(PulsePlot_[6]);	
		}
	}


	//***********************************************Process Reader*********************************************//
	class ProcessReader extends Thread {
		InputStream is;
		Process Proc;
		int thisModule;
		
		ProcessReader(Process myProcess,InputStream is,int module) {
			this.is = is;
			this.Proc=myProcess;
			this.thisModule=module;
		}
	
		public void run() {
			try{
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				
				while ( (line = br.readLine()) != null) {
					//System.out.println(type + ">" + line);
					TxtOfflineOutput.setProperty("INSERT","[Module_"+Integer.toString(thisModule)+"] "+line + "\n");
				}
				
				Proc.destroy();
					
			} catch (IOException ioe){
				ioe.printStackTrace(); 
			}
		}
	} // End ProcessReader
		
} // End of the Runtime object

