/************************************Author Information**************************************
				
				     Danula Godagama 
			    	 danula.godagama@uky.edu 
			        University of Kentucky 
				       03/19/2020 

**************************************File Information***************************************
				       TimeTest.C
A ROOT macro designed to test the clock synchronization between two digitizer modules set up 
to run in synchronization mode. This macro plots the difference between trigger timestamps(TTT) 
by the two modules. In a successful syncronization, this difference should be constant for all 
the events. Therefore, histogram HtimeDist should have a single peak. Double peaks indicate the
synchronization is unsuccessful.

This macro can by started by .x TimeTest.C command in the ROOT console  
*********************************************************************************************/
{
TFile *f1=TFile::Open("/home/mkovash/Kmax_Stuff/Replays/Module_1/Replay_950002.root","READ");
TTree *tree1=(TTree*)f1->Get("tree");

TFile *f2=TFile::Open("/home/mkovash/Kmax_Stuff/Replays/Module_3/Replay_950002.root","READ");
TTree *tree2=(TTree*)f2->Get("tree");
ULong64_t TimeStamp1;
ULong64_t TimeStamp2;
float Q1,Q2;
float CFDch0;
float CFDch1;
unsigned long long DeltaT;
tree1->SetBranchAddress("TimeStamp",&TimeStamp1);
tree2->SetBranchAddress("TimeStamp",&TimeStamp2);
tree1->SetBranchAddress("CFD_0",&CFDch0);
tree2->SetBranchAddress("CFD_0",&CFDch1);
tree1->SetBranchAddress("Q_0",&Q1);
tree2->SetBranchAddress("Q_0",&Q2);

tree1->GetEntry(0);
tree2->GetEntry(0);
DeltaT=(TimeStamp2-TimeStamp1);


TH1D *HtimeDist=new TH1D("HtimeDist","deltaT TimeStamp",100,DeltaT-50,DeltaT+50);
HtimeDist->GetXaxis()->SetTitle("Clock Cycles");
TH1D *HdeltaCFD=new TH1D("HdeltaCFD","deltaT CFD",80,-20,20);
HdeltaCFD->GetXaxis()->SetTitle("ns");
long entries=tree1->GetEntries();

TH2F *Q1vsQ2=new TH2F("Q1vsQ2","Q1 vs Q2",500,0,500,500,0,500);

for(int j=0;j<entries;j++){
tree1->GetEntry(j);
tree2->GetEntry(j);
DeltaT=(TimeStamp2-TimeStamp1);
HtimeDist->Fill(DeltaT);
//printf("%d\n",DeltaT);
//printf("CFDch0:%f\n",CFDch0);
HdeltaCFD->Fill(CFDch0-CFDch1);
Q1vsQ2->Fill(Q1,Q2);

}
HtimeDist->Draw();
//HtimeDist->GetXaxis()->SetTitle("ns");
//HdeltaCFD->Draw();
}
