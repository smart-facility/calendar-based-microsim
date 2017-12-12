package core.SyntheticPopulation.Features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import core.HardcodedData;
import core.HardcodedData.EmploymentStatus;
import core.HardcodedData.PlaceOfWork;
import core.Rates;
import core.SyntheticPopulation.Household;
import core.SyntheticPopulation.Individual;
import core.SyntheticPopulation.Population;
import core.SyntheticPopulation.PopulationAnalytics;

public class Employments {
	
//	public static void readInNonLabourData(String filename) {
//		ArrayList<>
//	}
	
	/**
	 * assigns status 'unemployed' to all individuals >15 in immigrating households
	 * @param immiHholdsID list of ID of immigrating households
	 */
	public static void labelEmploymentStatusToImmigrants(ArrayList<Integer> immiHholdsID, double pcNonLabour, int iYear, int[] emiEmpStat) {
		int emiEmplIn = emiEmpStat[0];
		int emiEmplOut = emiEmpStat[1];
		
		int totalEmployedEmigrants = emiEmpStat[0] + emiEmpStat[1];
		
		// gets ID of immigrant individuals eligible for work, i.e. >=15 yo
		ArrayList<Integer> workableImmigrants = new ArrayList<Integer>();
		for (Integer hholdID : immiHholdsID) {
			Household hhold = Population.getHhPool().get(hholdID);
			ArrayList<Integer> residentsID = hhold.getResidentsID();
			for (Integer indivID : residentsID) {
				if (Population.getIndivPool().get(indivID).getAge()>=HardcodedData.minAgeO15) {
					workableImmigrants.add(indivID);
				}
			}
		}
		
		// calculates the number of immigrants having a job inside and having a job outside.
		// note, we're assuming that jobs left behind by emigrants are passed over to immigrants, regardless whether the jobs are inside or outside.
		int immiEmplIn = emiEmplIn;
		int immiEmplOut = emiEmplOut;
		if (workableImmigrants.size() < emiEmplIn+emiEmplOut) {
			double pcEmiEmplIn = (double)emiEmplIn/(double)(emiEmplIn+emiEmplOut);
			immiEmplIn = (int)((double)workableImmigrants.size()*pcEmiEmplIn);
			immiEmplOut = workableImmigrants.size() - immiEmplIn;
		}
		int totalImmiEmpl = immiEmplIn + immiEmplOut;
		
		Collections.shuffle(workableImmigrants,HardcodedData.random);
		// assigning employed inside to immiEmplIn immigrants
		for (int i=0; i<=immiEmplIn-1; i++) {
			Integer indivID = workableImmigrants.get(i);
			Population.getIndivPool().get(indivID).setEmpStat(EmploymentStatus.employed);
			Population.getIndivPool().get(indivID).setWorkPlace(PlaceOfWork.in);
		}
		
		// assigning employed outside to immiEmplOut immigrants
		for (int i=immiEmplIn; i<=totalImmiEmpl-1; i++) {
			Integer indivID = workableImmigrants.get(i);
			Population.getIndivPool().get(indivID).setEmpStat(EmploymentStatus.employed);
			Population.getIndivPool().get(indivID).setWorkPlace(PlaceOfWork.out);
			Population.setOutEmployedImmigrantCount(Population.getOutEmployedImmigrantCount()+1);
		}
		
		// calculates the number of immigrants that are non-labour
		int nNonLabour = Math.min((int)((double)workableImmigrants.size()*pcNonLabour), workableImmigrants.size()-totalImmiEmpl);
		// assigns non-labour status to this number of immigrants (random)
		for (int i=totalImmiEmpl; i<=totalImmiEmpl+nNonLabour-1; i++) {
			Integer indivID = workableImmigrants.get(i);
			Population.getIndivPool().get(indivID).setEmpStat(EmploymentStatus.nonLabour);
			Population.getIndivPool().get(indivID).setWorkPlace(null);
		}
		
		// assigns the rest of workable immigrants as unemployed
		for (int i=totalImmiEmpl+nNonLabour; i<=workableImmigrants.size()-1; i++) {
			Integer indivID = workableImmigrants.get(i);
			Population.getIndivPool().get(indivID).setEmpStat(EmploymentStatus.unEmployed);
			Population.getIndivPool().get(indivID).setWorkPlace(null);
			Population.setUnEmployedImmigrantCount(Population.getUnEmployedImmigrantCount()+1);
		}
		
		
	}
	
	/**
	 * 
	 * @param immiHholdsID
	 */
	public static void labelImmigrantsAsUnemployed(ArrayList<Integer> immiHholdsID) {
		
		for (Integer hholdID : immiHholdsID) {
			Household hhold = Population.getHhPool().get(hholdID);
			ArrayList<Integer> residentsID = hhold.getResidentsID();
			for (Integer indivID : residentsID) {
				if (Population.getIndivPool().get(indivID).getAge()>=HardcodedData.minAgeO15) {
					Population.getIndivPool().get(indivID).setEmpStat(EmploymentStatus.unEmployed);
					Population.getIndivPool().get(indivID).setWorkPlace(null);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param iYear
	 */
	public static void updateLIWOByGivenRate(int iYear) {
		int crnLIWI = PopulationAnalytics.countIndivs(EmploymentStatus.employed, PlaceOfWork.in);
		int crnLIWO = PopulationAnalytics.countIndivs(EmploymentStatus.employed, PlaceOfWork.out);
		double percentLIWOThisYear = Rates.getPercentLIWOByYear().get((Integer)iYear);
		
		int newLIWO = (int)((double)crnLIWI * percentLIWOThisYear / (1 - percentLIWOThisYear));
		
		if (newLIWO>crnLIWO) {// if we need to assign more outside jobs to local residents
			ArrayList<Integer> unemployedID = PopulationAnalytics.getIDIndivs(EmploymentStatus.unEmployed);
			Collections.shuffle(unemployedID,HardcodedData.random);
			for (int i=0; i<=Math.min(newLIWO-crnLIWO, unemployedID.size())-1; i++) {
				Population.getIndivPool().get(unemployedID.get(i)).setEmpStat(EmploymentStatus.employed);
				Population.getIndivPool().get(unemployedID.get(i)).setWorkPlace(PlaceOfWork.out);
			}
		}
	}
	
	
	/**
	 * 
	 */
	public static int match_LIWO_With_LIWI() {
		double initLIWI = Population.getInitLiveInWorkIn();
		double initLIWO = Population.getInitLiveInWorkOut();
		
		int crnLIWI = PopulationAnalytics.countIndivs(EmploymentStatus.employed, PlaceOfWork.in);
		int crnLIWO = PopulationAnalytics.countIndivs(EmploymentStatus.employed, PlaceOfWork.out);
		
		int newLIWO = (int)((double)crnLIWI*initLIWO/initLIWI);
		
		if (newLIWO>crnLIWO) { // if we need to assign more outside jobs to local residents
			ArrayList<Integer> unemployedID = PopulationAnalytics.getIDIndivs(EmploymentStatus.unEmployed);
			Collections.shuffle(unemployedID,HardcodedData.random);
			for (int i=0; i<=Math.min(newLIWO-crnLIWO, unemployedID.size())-1; i++) {
				Population.getIndivPool().get(unemployedID.get(i)).setEmpStat(EmploymentStatus.employed);
				Population.getIndivPool().get(unemployedID.get(i)).setWorkPlace(PlaceOfWork.out);
			}
			return (newLIWO-crnLIWO);
		}
		return 0;
	}
	
	/**
	 * 
	 */
	public static void updateEmploymentStatusBasedOnJobChange(double deltaJob, int iYear) {
		double percentLiveInWorkIn = (double)Population.getInitLiveInWorkIn()/((double)Population.getInitLiveInWorkIn() + (double)Population.getInitLiveOutWorkIn());
		double percentLiveOutWorkIn = 1 - percentLiveInWorkIn;
		
		if (deltaJob<0) {
			int lostJobsLiveInWorkIn = (int)(deltaJob*percentLiveInWorkIn);
			int lostJobsLiveOutWorkIn = (int)(deltaJob*percentLiveOutWorkIn);
			
			// updates the number of people commuting into the region for work
			int newPeopleLiveOutWorkIn = Math.max(0, Population.getCrnLiveOutWorkIn() + lostJobsLiveOutWorkIn);
			Population.setCrnLiveOutWorkIn(newPeopleLiveOutWorkIn);
			
			// gets ID of employed individuals working in the region and shuffle them
			ArrayList<Integer> employedIndivID = PopulationAnalytics.getIDIndivs(EmploymentStatus.employed, PlaceOfWork.in);
			Collections.shuffle(employedIndivID,HardcodedData.random);
			// changes deltaJobsLiveInWorkIn people from employed to unEmployed or nonLabour (if the individual is older than retired age)
			for (int i=0; i<=Math.abs(lostJobsLiveInWorkIn)-1; i++) {
				Integer selID = employedIndivID.get(i);
				if (Population.getIndivPool().get(selID).getAge()>=HardcodedData.retiredAge) {
					Population.getIndivPool().get(selID).setEmpStat(EmploymentStatus.nonLabour);
				} else {
					Population.getIndivPool().get(selID).setEmpStat(EmploymentStatus.unEmployed);
				}
				// set workplace to null for this individual;
				Population.getIndivPool().get(selID).setWorkPlace(null);
			}
			
		} else {
			//handleNewJobs_v1(deltaJob, percentLiveInWorkIn, percentLiveOutWorkIn, iYear);
			handleNewJobs_v2(deltaJob, percentLiveInWorkIn, percentLiveOutWorkIn, iYear);
		}
	}
	
	
	/**
	 * this version of handling new jobs assigns part of the new jobs to unemployed local people and the remaining to immigrants.
	 * The proportion of jobs to local unemployed follows a predefined value.
	 * 
	 * @param deltaJob
	 * @param percentLiveInWorkIn
	 * @param percentLiveOutWorkIn
	 * @param iYear
	 */
	private static void handleNewJobs_v2(double deltaJob, double percentLiveInWorkIn, double percentLiveOutWorkIn, int iYear) {
		int newJobsLiveInWorkIn = (int)(deltaJob*percentLiveInWorkIn);
		int newJobsLiveOutWorkIn = (int)(deltaJob*percentLiveOutWorkIn); 
		
		System.out.println(iYear + ", " + newJobsLiveInWorkIn + ", " + newJobsLiveOutWorkIn + ", " + deltaJob);
		
		// updates the number of people commuting into the in the region for work
		int newPeopleLiveOutWorkIn = Math.max(0, Population.getCrnLiveOutWorkIn() + newJobsLiveOutWorkIn);
		Population.setCrnLiveOutWorkIn(newPeopleLiveOutWorkIn);
		
		int newJobsToLocalUnEmpl = (int)((double)newJobsLiveInWorkIn*Population.getNewJobsToLocalUnEmplRatio());
		int newJobsToMigrants = newJobsLiveInWorkIn - newJobsToLocalUnEmpl;
		
		// ASSIGNS JOBS TO UNEMPLOYED LOCAL PEOPLE
		// gets ID of unemployed individuals
		ArrayList<Integer> unEmployedIndivID = PopulationAnalytics.getIDIndivs(EmploymentStatus.unEmployed);
		// randomly convert these unemployed local people to employed
		Collections.shuffle(unEmployedIndivID,HardcodedData.random);
		for (int i=0; i<=newJobsToLocalUnEmpl-1; i++) {
			Integer selID = unEmployedIndivID.get(i);
			Population.getIndivPool().get(selID).setEmpStat(EmploymentStatus.employed);
			// sets place of work of this individual to "in"
			Population.getIndivPool().get(selID).setWorkPlace(PlaceOfWork.in);
		}
		System.out.println("assigned " + newJobsToLocalUnEmpl + " jobs to local unemployed.");
		
		
		// ASSIGNS JOBS TO 2ND WAVE IMMIGRANTS
		// brings in new people and households (2nd wave immigration)
		// calculates the number of immigrating households into the region for these jobs
		int nImmiFami = (int)((double)newJobsToMigrants*Population.getJobImmiFamiRatio());
		// brings nImmiFami households into the region
		ArrayList<Integer> immiHholdIDs = Migrations.immigrateHholdBased(nImmiFami);
		// output number of immigrants by age group
		Migrations.outputMigrantsToCSV(immiHholdIDs, Rates.getImmigrantAgeDistrib().keySet(), HardcodedData.outputTablesPath+"immigrants2ndSummary_"+Integer.toString(iYear)+".csv");
		
		// assigns employment status for people in these households
		assignEmploymentStatToImmiHholds_v2(immiHholdIDs, newJobsToMigrants, iYear);
		
		System.out.println("assigned " + newJobsToMigrants + " jobs to migrants.");
		
		// updates LIWO to maintain LIWI/LIWO ratio
		//int dNewLIWO = Employments.match_LIWO_With_LIWI();
		updateLIWOByGivenRate(iYear);
	}
	
	/**
	 * 
	 */
	// _2ndImmiInd(26), _2ndImmiO15(27), _2ndImmiEmp(28), _2ndImmiUnEmp(29), _2ndImmiNonLab(30);
	private static void assignEmploymentStatToImmiHholds_v2(ArrayList<Integer> immiHholdIDs, int nJobs, int iYear) {
		// assigns at least 1 job to each household. The person having the job is anyone above 15 years old.
		int nJobsAssigned = 0;
		while (nJobsAssigned<nJobs) {
			Collections.shuffle(immiHholdIDs,HardcodedData.random);
			for (Integer hholdID : immiHholdIDs) {
				Household hhold = Population.getHhPool().get(hholdID);
				ArrayList<Integer> resIDs = new ArrayList<Integer>();
				resIDs = hhold.getResidentsID();
				Collections.shuffle(resIDs,HardcodedData.random);
				for (Integer indivID : resIDs) {
					if (Population.getIndivPool().get(indivID).getAge()>=HardcodedData.minAgeO15 && Population.getIndivPool().get(indivID).getEmpStat()==null) {
						Population.getIndivPool().get(indivID).setEmpStat(EmploymentStatus.employed);
						// sets place of work of this individual to "in"
						Population.getIndivPool().get(indivID).setWorkPlace(PlaceOfWork.in);
						nJobsAssigned += 1;

						break;
					}
				}
				
				if (nJobsAssigned>=nJobs) {
					break;
				}
			}
		}
		
		// get id of workable immigrants who are not yet employed
		ArrayList<Integer> workableImmiRemainID = new ArrayList<Integer>();
		int countAllWorkableImmi = 0;
		for (Integer hholdID : immiHholdIDs) {
			Household hhold = Population.getHhPool().get(hholdID);
			ArrayList<Integer> resIDs = new ArrayList<Integer>();
			resIDs = hhold.getResidentsID();
			for (Integer indivID : resIDs) {
				if (Population.getIndivPool().get(indivID).getAge()>=HardcodedData.minAgeO15) {
					countAllWorkableImmi += 1;
					if (Population.getIndivPool().get(indivID).getEmpStat()==null) {
						workableImmiRemainID.add(indivID);
					}
				}
			}
		}
		
		// calculate the number of nonLabour among all the workableImmigrants
		int nNonLabour = (int)(Population.getPcNonLabourImmigrants()*(double)countAllWorkableImmi);
		// now the actual nNonLabour is the smaller between (desired) nNonLabour and the remaining workable immigrants
		nNonLabour = Math.min(nNonLabour, workableImmiRemainID.size());
		
		// assign nonLabour status to workable immigrants
		Collections.shuffle(workableImmiRemainID,HardcodedData.random);
		for (int i=0; i<=nNonLabour-1; i++) {
			Integer indivID = workableImmiRemainID.get(i);
			Population.getIndivPool().get(indivID).setEmpStat(EmploymentStatus.nonLabour);
			Population.getIndivPool().get(indivID).setWorkPlace(null);
		}
		
		for (int i=nNonLabour; i<=workableImmiRemainID.size()-1; i++) {
			Integer indivID = workableImmiRemainID.get(i);
			Population.getIndivPool().get(indivID).setEmpStat(EmploymentStatus.unEmployed);
			Population.getIndivPool().get(indivID).setWorkPlace(null);
		}
	}
	
	
	/**
	 * updates employment status of the existing population after ageing 1 year.
	 * Particularly, kids turn 15 become unemployed and unemployed people turn 65 (retired age) become non-labour.
	 * Also assigns jobs from the deads to the local unemployed.
	 */
	public static void updateEmploymentStatusExistingPop(int[] deadEmpStat) {
		// gets ID of kids aged 15 in the population and assign them as unemployed
		int new15UnEmployed = 0;
		int new15NonLabour = 0;
		int newRetNonLabour = 0;
		int newEmplInFromRetirees = 0;
		int newEmplOutFromRetirees = 0;	
		double participationRate = 0.92;
		double retiringRate = 0.98;
		for (Individual indiv : Population.getIndivPool().values()) {
			if (indiv.getAge()==HardcodedData.minAgeO15 && indiv.getEmpStat()==null) {
				double randChance = HardcodedData.random.nextDouble();
				if (randChance < participationRate) {
					indiv.setEmpStat(EmploymentStatus.unEmployed);
					new15UnEmployed += 1;
				} else {
					indiv.setEmpStat(EmploymentStatus.nonLabour);
					new15NonLabour += 1;
				}
			} else if (indiv.getAge()==HardcodedData.retiredAge && indiv.getEmpStat()==EmploymentStatus.unEmployed) {
				indiv.setEmpStat(EmploymentStatus.nonLabour);
				newRetNonLabour += 1;
			} else if (indiv.getAge()>=HardcodedData.retiredAge && indiv.getEmpStat()==EmploymentStatus.employed) {
				double randChance = HardcodedData.random.nextDouble();
				if (randChance < retiringRate) {
					indiv.setEmpStat(EmploymentStatus.nonLabour);
					if (indiv.getWorkPlace().equals(PlaceOfWork.in)) {
						newEmplInFromRetirees += 1;
					} else if (indiv.getWorkPlace().equals(PlaceOfWork.out)) {
						newEmplOutFromRetirees += 1;
					}
					indiv.setWorkPlace(null);
				}
			}
		}
		
		// add employments from new retirees to deadEmpStat
		int totalNewEmpIn = deadEmpStat[0] + newEmplInFromRetirees;
		int totalNewEmpOut = deadEmpStat[1] + newEmplOutFromRetirees;
		
		ArrayList<Integer> unemployedIDs = new ArrayList<Integer>();
		for (Individual indiv : Population.getIndivPool().values()) {
			if (indiv.getEmpStat()!=null && indiv.getEmpStat().equals(EmploymentStatus.unEmployed)) {
				unemployedIDs.add(indiv.getId());
			}
		}
		
		Collections.shuffle(unemployedIDs,HardcodedData.random);
		
		int countNewLIWI = 0;
		// assigning employment inside to local unemployed
		for (int i=0; i<=totalNewEmpIn-1; i++) {
			Integer indivID = unemployedIDs.get(i);
			Population.getIndivPool().get(indivID).setEmpStat(EmploymentStatus.employed);
			Population.getIndivPool().get(indivID).setWorkPlace(PlaceOfWork.in);
			countNewLIWI += 1;
		}
		
		int countNewLIWO = 0;
		// assigning employment outside to local unemployed
		for (int i=totalNewEmpIn; i<=totalNewEmpIn+totalNewEmpOut-1; i++) {
			Integer indivID = unemployedIDs.get(i);
			Population.getIndivPool().get(indivID).setEmpStat(EmploymentStatus.employed);
			Population.getIndivPool().get(indivID).setWorkPlace(PlaceOfWork.out);
			countNewLIWO += 1;
		}
		
		System.out.println("updateEmploymentStatusExistingPop, new15UnEmployed " + new15UnEmployed + ", new15NonLabour " + new15NonLabour + ", newRetNonLab " + newRetNonLabour + 
				", countNewLIWI " + countNewLIWI + ", countNewLIWO " + countNewLIWO + ", newEmplInFromRetirees" + newEmplInFromRetirees + ", newEmplOutFromRetirees" + newEmplOutFromRetirees);
	}
	
	
	/**
	 * 
	 * @param immiHholdIDs
	 * @param nJobs
	 */
	private static void assignEmploymentStatToImmiHholds(ArrayList<Integer> immiHholdIDs, int nJobs) {
		// first labels any immigrants that are over 15 as 'unEmployed'
		Employments.labelImmigrantsAsUnemployed(immiHholdIDs);
		
		int countJobsAssigned = 0;
		
		// assigns 1 job to each household. The person having the job is anyone above 15 years old.
		for (Integer hholdID : immiHholdIDs) {
			Household hhold = Population.getHhPool().get(hholdID);
			ArrayList<Integer> resIDs = new ArrayList<Integer>();
			resIDs = hhold.getResidentsID();
			Collections.shuffle(resIDs,HardcodedData.random);
			for (Integer indivID : resIDs) {
				if (Population.getIndivPool().get(indivID).getAge()>=HardcodedData.minAgeO15) {
					Population.getIndivPool().get(indivID).setEmpStat(EmploymentStatus.employed);
					// sets place of work of this individual to "in"
					Population.getIndivPool().get(indivID).setWorkPlace(PlaceOfWork.in);
					
					countJobsAssigned += 1;
					
					break;
				}
			}
		}
		
		int nJobsRemain = nJobs - immiHholdIDs.size();
		int nJobsRemainAlloc = 0;
		if (nJobsRemain>0) {
			for (Integer hholdID : immiHholdIDs) {
				Household hhold = Population.getHhPool().get(hholdID);
				// looks for a person in this household older than 15 and not yet employed (ie employment status is null)
				// assigns a job, ie employment status is employed
				ArrayList<Integer> resIDs = new ArrayList<Integer>();
				resIDs = hhold.getResidentsID();
				for (Integer indivID : resIDs) {
					if (Population.getIndivPool().get(indivID).getAge()>=HardcodedData.minAgeO15 && 
							Population.getIndivPool().get(indivID).getEmpStat().equals(EmploymentStatus.unEmployed)) {
						Population.getIndivPool().get(indivID).setEmpStat(EmploymentStatus.employed);
						// sets place of work of this individual to "in"
						Population.getIndivPool().get(indivID).setWorkPlace(PlaceOfWork.in);
						nJobsRemainAlloc +=1;
						countJobsAssigned += 1;
						if (nJobsRemainAlloc>=nJobsRemain) {
							break;
						}
					}
				}
				if (nJobsRemainAlloc>=nJobsRemain) {
					break;
				}
			}
		}
				
		System.out.println("assigned " + countJobsAssigned + " jobs to immigrants (out of " + nJobs + " to be assigned).");
	}
	
	
}
