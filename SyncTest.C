/************************************Author Information**************************************
				
				     Danula Godagama 
			    	 danula.godagama@uky.edu 
			        University of Kentucky 
				       03/19/2020 

**************************************File Information***************************************
				       SyncTest.C
A ROOT macro designed to test the clock synchronization between two digitizer modules set up 
to run in synchronization mode. This macro plots the difference between trigger timestamps(TTT) 
by the two modules. In a successful syncronization, this difference should be constant for all 
the events. Therefore, histogram HtimeDist should have a single peak. Double peaks indicate the
synchronization is unsuccessful.

This macro can by started by .x TimeTest.C command in the ROOT console 
The user should change the file locations in lines 20 and 23.
*********************************************************************************************/
{
TFile *f1=TFile::Open("/home/mkovash/Kmax_Stuff/Replays/Module_1/Replay_900001.root","READ");
TTree *tree1=(TTree*)f1->Get("tree");

TFile *f2=TFile::Open("/home/mkovash/Kmax_Stuff/Replays/Module_2/Replay_900001.root","READ");
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


TH1D *HtimeDist=new TH1D("HtimeDist","Trigger timeStamp(TTT) difference",100,DeltaT-50,DeltaT+50);
HtimeDist->GetXaxis()->SetTitle("Clock Cycles");

TH1D *HdeltaCFD=new TH1D("HdeltaCFD","CFD difference",80,-20,20);
HdeltaCFD->GetXaxis()->SetTitle("ns");

TH2F *Q1vsQ2=new TH2F("Q1vsQ2","Charge disposition by the signal",500,0,500,500,0,500);
Q1vsQ2->GetXaxis()->SetTitle("Digitizer_1[channel]");
Q1vsQ2->GetYaxis()->SetTitle("Digitizer_2[channel]");

long entries=tree1->GetEntries();

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
TCanvas *C1 =new TCanvas("C1","Synchronization Test",800,800);
C1->Divide(2,2);
C1->cd(1);
HtimeDist->Draw();
C1->cd(2);
HdeltaCFD->Draw();
C1->cd(3);
Q1vsQ2->Draw("colz");
//HtimeDist->GetXaxis()->SetTitle("ns");
//HdeltaCFD->Draw();
}
