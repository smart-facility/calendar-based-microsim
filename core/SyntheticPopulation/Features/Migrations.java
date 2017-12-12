package core.SyntheticPopulation.Features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import core.ArrayHandler;
import core.HardcodedData;
import core.HardcodedData.Genders;
import core.HardcodedData.EmploymentStatus;
import core.HardcodedData.PlaceOfWork;
import core.TextFileHandler;
import core.SyntheticPopulation.Household;
import core.SyntheticPopulation.Individual;
import core.SyntheticPopulation.Population;
import core.SyntheticPopulation.PopulationAnalytics;

public class Migrations {
	
	/**
	 * 
	 * @param vacantDwellings
	 * @return
	 */
	private static int getTotalAvailableDwellings(HashMap<String,Integer> vacantDwellings) {
		int totalAvailDwellings = 0;
		for (Integer nDwellings : vacantDwellings.values()) {
			totalAvailDwellings += nDwellings;
		}
		return totalAvailDwellings;
	}
	
	/**
	 * 
	 * @param totalIndivImmigrants
	 * @param ageDistrib
	 * @param yearIndex
	 * @return
	 */
	public static ArrayList<Integer> immigrateHholds(int totalIndivImmigrants, HashMap<int[],Double> ageDistrib, int yearIndex) {
		HashMap<int[],Integer> targetImmigrantsByAge = new HashMap<int[],Integer>();
		HashMap<int[],Integer> resultImmigrantsByAge = new HashMap<int[],Integer>();
		ArrayList<Integer> immiHholdsID = new ArrayList<Integer>();
		
		if (totalIndivImmigrants<=0) {
			return null;
		}
		
		// initialises number of people immigrated
		int nIndivsImmigrated = 0;
		int nHholdsImmigrated = 0;
		// initialises targetImmigrantsByAge
		targetImmigrantsByAge = calcTargetMigrantsByAge(totalIndivImmigrants,ageDistrib, HardcodedData.pcOvershootingTargetMigrantsByAge);
		// initialises resultImmigrantsByAge
		for (int[] ageBracket : targetImmigrantsByAge.keySet()) {
			resultImmigrantsByAge.put(ageBracket, 0);
		}
		
		while (nIndivsImmigrated<totalIndivImmigrants) {
			// scores each household in the init population based on 
			// (i) the age distribution of the residents, 
			// (ii) the target age distribution of immigrants, and 
			// (iii) the current age distribution of immigrants
			double maxHholdScore = 0;
			int selectedHhID = -1;
			for (Household hhold : Population.getInitHhPool().values()) {
				double scoreThisHhold = calcInitHholdScore(hhold,targetImmigrantsByAge,resultImmigrantsByAge);
				if (scoreThisHhold>maxHholdScore) {
					maxHholdScore = scoreThisHhold;
					selectedHhID = hhold.getId();
				}
			}
			if (selectedHhID==-1) {
				// randomly picks one household in the initial population
				selectedHhID = Population.pickRandomHholdFromInitPool();
			}
			
			// add this household to the current population
			int newHholdID = immigrateHholdFromInitPopToCrnPop(selectedHhID, HardcodedData.unknown);
			nHholdsImmigrated += 1;
			immiHholdsID.add((Integer)newHholdID);
			
			// updates number of individuals immigrated
			nIndivsImmigrated += Population.getHhPool().get(newHholdID).getResidentsID().size();
			
			// updates the resulting counts of emigrants by age
			for (Integer resID : Population.getInitHhPool().get(selectedHhID).getResidentsID()) {
				int residentAge = Population.getInitIndivPool().get(resID).getAge();
				int[] ageBracketThisAge = getAgeBracket(residentAge, resultImmigrantsByAge.keySet());
				int nImmigrantsThisAge = resultImmigrantsByAge.get(ageBracketThisAge);
				nImmigrantsThisAge += 1;
				resultImmigrantsByAge.put(ageBracketThisAge,nImmigrantsThisAge);
			}
		}

		Population.setActualIndivImmigrants1st(nIndivsImmigrated);
		Population.setActualHholdImmigrants1st(nHholdsImmigrated);
		
		
		outputMigrantsSummaryToCSV(targetImmigrantsByAge, resultImmigrantsByAge, HardcodedData.outputTablesPath+"immigrantsSummary_"+Integer.toString(yearIndex)+".csv");
		
		
		return immiHholdsID;
	}

	/**
	 * 
	 * @param totalIndivEmigrants
	 * @param ageDistrib
	 * @param newEvoHholdIDs
	 * @param yearIndex
	 * @return
	 */
	public static int[] emigrateHholdsTrackingEmpl(int totalIndivEmigrants, HashMap<int[],Double> ageDistrib, ArrayList<Integer> newEvoHholdIDs, int yearIndex) {
		int[] countEmiEmplStat = new int[2];
		HashMap<int[],Integer> targetEmigrantsByAge = new HashMap<int[],Integer>();
		HashMap<int[],Integer> resultEmigrantsByAge = new HashMap<int[],Integer>();
		
		// initialises number of people emigrated
		int nIndivsEmigrated = 0;
		int nHholdsEmigrated = 0;
		// initialises targetEmigrantsByAge
		targetEmigrantsByAge = calcTargetMigrantsByAge(totalIndivEmigrants,ageDistrib,HardcodedData.pcOvershootingTargetMigrantsByAge);
		// initialises resultEmigrantsByAge
		for (int[] ageBracket : targetEmigrantsByAge.keySet()) {
			resultEmigrantsByAge.put(ageBracket, 0);
		}

		// selects emigrating households from current population until totalIndivEmigrants is satisfied
		int runCount = 0;
		while (nIndivsEmigrated<totalIndivEmigrants) {
			runCount++;
			// scoring each household in the population based on (i) the age of the residents, (ii) the target age distribution of emigrants, and (iii) the current age distribution of emigrants
			double maxExistHholdScore = 0;
			int selectedExistHhID = -1;
			double maxNewHholdScore = 0;
			int selectedNewHhID = -1;
			for (Household hhold : Population.getHhPool().values()) {
				double scoreThisHhold = calcCrnHholdScore(hhold, targetEmigrantsByAge, resultEmigrantsByAge);
				if (hhold.getZoneName()==HardcodedData.unknown) {
					if (scoreThisHhold>=maxNewHholdScore) {
						maxNewHholdScore = scoreThisHhold;
						selectedNewHhID = hhold.getId();
					}
				} else {
					if (scoreThisHhold>=maxExistHholdScore) {
						maxExistHholdScore = scoreThisHhold;
						selectedExistHhID = hhold.getId();
					}
				}
			}
			int selectedHhID = selectedExistHhID;
			if (maxNewHholdScore>=maxExistHholdScore) {
				selectedHhID = selectedNewHhID;
			}
			if (selectedHhID==-1) {
				// randomly picks one household in the current population to be the first emigrant household
				System.out.println("Pick randomly a household; year " + yearIndex + ", runCount " + runCount);
				System.out.println("\tmaxExHhScore " + maxExistHholdScore + ", selectedExHhID " + selectedExistHhID + ", maxNewHhScore " + maxNewHholdScore + ", selectedNewHhID " + selectedNewHhID);
				if (newEvoHholdIDs!=null && newEvoHholdIDs.size()>0) {
					selectedHhID = newEvoHholdIDs.get(HardcodedData.random.nextInt(newEvoHholdIDs.size()));
				} else {
					selectedHhID = Population.pickRandomHholdFromCrnPool();
				}
			}
			if (newEvoHholdIDs.contains(selectedHhID)) {
				newEvoHholdIDs.remove((Integer)selectedHhID);
			}
			
			// updates the number of employed inside and employed outside among the emigrants
			for (Integer indivID : Population.getHhPool().get(selectedHhID).getResidentsID()) {
				if (Population.getIndivPool().get(indivID).getEmpStat()==null) {
					continue;
				}
				if (Population.getIndivPool().get(indivID).getEmpStat().equals(EmploymentStatus.employed) && 
						Population.getIndivPool().get(indivID).getWorkPlace().equals(PlaceOfWork.in)) {
					countEmiEmplStat[0] += 1;
				} else if (Population.getIndivPool().get(indivID).getEmpStat().equals(EmploymentStatus.employed) && 
						Population.getIndivPool().get(indivID).getWorkPlace().equals(PlaceOfWork.out)) {
					countEmiEmplStat[1] += 1;
				}
			}
			
			// updates the resulting counts of emigrants by age
			for (Integer resID : Population.getHhPool().get(selectedHhID).getResidentsID()) {
				int residentAge = Population.getIndivPool().get(resID).getAge();
				int[] ageBracketThisAge = getAgeBracket(residentAge, resultEmigrantsByAge.keySet());
				int nEmigrantsThisAge = resultEmigrantsByAge.get(ageBracketThisAge);
				nEmigrantsThisAge += 1;
				resultEmigrantsByAge.put(ageBracketThisAge,nEmigrantsThisAge);
			}
			// updates the total number of individuals emigrated
			nIndivsEmigrated += Population.getHhPool().get(selectedHhID).getResidentsID().size();
			
			// removes randHhID and its residents from the population
			Population.removeHhold(selectedHhID);
			nHholdsEmigrated += 1;
		}
		Population.setAcutalIndivEmigrants(nIndivsEmigrated);
		Population.setAcutalHholdEmigrants(nHholdsEmigrated);
		
		outputMigrantsSummaryToCSV(targetEmigrantsByAge, resultEmigrantsByAge, HardcodedData.outputTablesPath+"emigrantsSummary_"+Integer.toString(yearIndex)+".csv");
		
		return countEmiEmplStat;
	}
	public static HashMap<String,Integer> immigrateHholds(int totalIndivImmigrants, HashMap<int[],Double> ageDistrib, HashMap<String,Integer> totalVacantDwellings, int yearIndex) {
		HashMap<int[],Integer> targetImmigrantsByAge = new HashMap<int[],Integer>();
		HashMap<int[],Integer> resultImmigrantsByAge = new HashMap<int[],Integer>();
		HashMap<String,Integer> allocatedDwellings = new HashMap<String,Integer>();
		ArrayList<String[]> immiHholdDetails = new ArrayList<>();
		
		if (totalIndivImmigrants<=0) {
			return allocatedDwellings;
		}
		
		// initialises number of people immigrated
		int nIndivsImmigrated = 0;
		int nHholdsImmigrated = 0;
		// initialises targetImmigrantsByAge
		targetImmigrantsByAge = calcTargetMigrantsByAge(totalIndivImmigrants,ageDistrib, HardcodedData.pcOvershootingTargetMigrantsByAge);
		// initialises resultImmigrantsByAge
		for (int[] ageBracket : targetImmigrantsByAge.keySet()) {
			resultImmigrantsByAge.put(ageBracket, 0);
		}
		
		int totalAvailDwellings = getTotalAvailableDwellings(totalVacantDwellings);
		
		while (totalAvailDwellings>0 && nIndivsImmigrated<totalIndivImmigrants) {
			// scores each household in the init population based on 
			// (i) the age distribution of the residents, 
			// (ii) the target age distribution of immigrants, and 
			// (iii) the current age distribution of immigrants
			double maxHholdScore = 0;
			int selectedHhID = -1;
			for (Household hhold : Population.getInitHhPool().values()) {
				double scoreThisHhold = calcInitHholdScore(hhold,targetImmigrantsByAge,resultImmigrantsByAge);
				if (scoreThisHhold>maxHholdScore) {
					maxHholdScore = scoreThisHhold;
					selectedHhID = hhold.getId();
				}
			}
			if (selectedHhID==-1) {
				// randomly picks one household in the initial population
				selectedHhID = Population.pickRandomHholdFromInitPool();
			}
			
			// randomly picks an SA1 (which has vacant dwellings) this household will reside in
			String zoneName = HardcodedData.unknown;
			for (String zone : totalVacantDwellings.keySet()) {
				int nDwellingsThisZone = totalVacantDwellings.get(zone);
				if (nDwellingsThisZone>0) {
					zoneName = zone;
					totalVacantDwellings.put(zone, nDwellingsThisZone-1);
					break;
				}
			}
			
			// add this household to the current population
			int newHholdID = immigrateHholdFromInitPopToCrnPop(selectedHhID, zoneName);
			nHholdsImmigrated += 1;
			
			// updates details of immigrant households
			//for (Integer resID : Population.getHhPool().get(newHholdID).getResidentsID()) {
			//	immiHholdDetails.add(new String[] {Integer.toString(newHholdID), Integer.toString(resID), Integer.toString(Population.getIndivPool().get(resID).getAge()), zoneName});
			//}
			
			// updates number of individuals immigrated
			nIndivsImmigrated += Population.getHhPool().get(newHholdID).getResidentsID().size();
			
			// updates the resulting counts of emigrants by age
			for (Integer resID : Population.getInitHhPool().get(selectedHhID).getResidentsID()) {
				int residentAge = Population.getInitIndivPool().get(resID).getAge();
				int[] ageBracketThisAge = getAgeBracket(residentAge, resultImmigrantsByAge.keySet());
				int nImmigrantsThisAge = resultImmigrantsByAge.get(ageBracketThisAge);
				nImmigrantsThisAge += 1;
				resultImmigrantsByAge.put(ageBracketThisAge,nImmigrantsThisAge);
			}
			
			// updates number of available dwellings in the region
			totalAvailDwellings = totalAvailDwellings - 1;
			
			// updates dwellings allocated
			int nAllocatedDwellingsThisZone = 0;
			if (allocatedDwellings.containsKey(zoneName)) {
				nAllocatedDwellingsThisZone = allocatedDwellings.get(zoneName);
			}
			allocatedDwellings.put(zoneName,nAllocatedDwellingsThisZone+1);
		}
		
		/*
		int totalResultImmigrantsByAge = 0;
		for (Integer value : resultImmigrantsByAge.values()) {
			totalResultImmigrantsByAge += value;
		}
		*/
		Population.setActualIndivImmigrants1st(nIndivsImmigrated);
		Population.setActualHholdImmigrants1st(nHholdsImmigrated);
		
		outputMigrantsSummaryToCSV(targetImmigrantsByAge, resultImmigrantsByAge, HardcodedData.outputTablesPath+"immigrantsSummary_"+Integer.toString(yearIndex)+".csv");
		//TextFileHandler.writeToCSV(HardcodedData.outputTablesPath+"immigrantsDetails_"+Integer.toString(yearIndex)+".csv", 
		//		new String[] {"immiHholdID", "immiIndivID", "immiIndivAge", "toZone"}, 
		//		immiHholdDetails);
		
		return allocatedDwellings;
	}
	
	
	/**
	 * 
	 * @param totalIndivEmigrants
	 * @param ageDistrib
	 * @param yearIndex
	 * @return
	 */
	public static HashMap<String,Integer> emigrateHholds(int totalIndivEmigrants, HashMap<int[],Double> ageDistrib, ArrayList<Integer> newEvoHholdIDs, int yearIndex) {
		HashMap<int[],Integer> targetEmigrantsByAge = new HashMap<int[],Integer>();
		HashMap<int[],Integer> resultEmigrantsByAge = new HashMap<int[],Integer>();
		HashMap<String,Integer> vacatedDwellings = new HashMap<String,Integer>(); // HashMap<ZoneName,numberOfDwellings>
		ArrayList<String[]> emiHholdDetails = new ArrayList<String[]>();
		
		if (totalIndivEmigrants<=0) {
			return vacatedDwellings;
		}
		
		// initialises number of people emigrated
		int nIndivsEmigrated = 0;
		int nHholdsEmigrated = 0;
		// initialises targetEmigrantsByAge
		targetEmigrantsByAge = calcTargetMigrantsByAge(totalIndivEmigrants,ageDistrib,HardcodedData.pcOvershootingTargetMigrantsByAge);
		// inititalises resultEmigrantsByAge
		for (int[] ageBracket : targetEmigrantsByAge.keySet()) {
			resultEmigrantsByAge.put(ageBracket, 0);
		}
		
		// selects emigrating households from current population until totalIndivEmigrants is satisfied
		int runCount = 0;
		while (nIndivsEmigrated<totalIndivEmigrants) {
			runCount++;
			// scoring each household in the population based on (i) the age of the residents, (ii) the target age distribution of emigrants, and (iii) the current age distribution of emigrants
			double maxExistHholdScore = 0;
			int selectedExistHhID = -1;
			double maxNewHholdScore = 0;
			int selectedNewHhID = -1;
			for (Household hhold : Population.getHhPool().values()) {
				double scoreThisHhold = calcCrnHholdScore(hhold, targetEmigrantsByAge, resultEmigrantsByAge);
				if (hhold.getZoneName()==HardcodedData.unknown) {
					if (scoreThisHhold>=maxNewHholdScore) {
						maxNewHholdScore = scoreThisHhold;
						selectedNewHhID = hhold.getId();
					}
				} else {
					if (scoreThisHhold>=maxExistHholdScore) {
						maxExistHholdScore = scoreThisHhold;
						selectedExistHhID = hhold.getId();
					}
				}
			}
			int selectedHhID = selectedExistHhID;
			if (maxNewHholdScore>=maxExistHholdScore) {
				selectedHhID = selectedNewHhID;
			}
			if (selectedHhID==-1) {
				// randomly picks one household in the current population to be the first emigrant household
				System.out.println("Pick randomly a household; year " + yearIndex + ", runCount " + runCount);
				System.out.println("\tmaxExHhScore " + maxExistHholdScore + ", selectedExHhID " + selectedExistHhID + ", maxNewHhScore " + maxNewHholdScore + ", selectedNewHhID " + selectedNewHhID);
				if (newEvoHholdIDs!=null && newEvoHholdIDs.size()>0) {
					selectedHhID = newEvoHholdIDs.get(HardcodedData.random.nextInt(newEvoHholdIDs.size()));
				} else {
					selectedHhID = Population.pickRandomHholdFromCrnPool();
				}
			}
			if (newEvoHholdIDs.contains(selectedHhID)) {
				newEvoHholdIDs.remove((Integer)selectedHhID);
			}
			
			// updates the number of vacated dwellings in each zone
			if (Population.getHhPool().get(selectedHhID).getZoneName()==null) {
				System.out.println("Hhold " + selectedHhID + " has no zone name.");
			}
			String zoneName = Population.getHhPool().get(selectedHhID).getZoneName();
			if (zoneName!=HardcodedData.unknown) {
				int nVacatedDwellingsThisZone = 0;
				if (vacatedDwellings.containsKey(zoneName)) {
					nVacatedDwellingsThisZone = vacatedDwellings.get(zoneName);
				}
				vacatedDwellings.put(zoneName, nVacatedDwellingsThisZone += 1);
			}
			// updates details of emigrant households
			//for (Integer resID : Population.getHhPool().get(selectedHhID).getResidentsID()) {
			//	emiHholdDetails.add(new String[] {Integer.toString(selectedHhID), Integer.toString(resID), Integer.toString(Population.getIndivPool().get(resID).getAge()), zoneName});
			//}
			// updates the resulting counts of emigrants by age
			for (Integer resID : Population.getHhPool().get(selectedHhID).getResidentsID()) {
				int residentAge = Population.getIndivPool().get(resID).getAge();
				int[] ageBracketThisAge = getAgeBracket(residentAge, resultEmigrantsByAge.keySet());
				int nEmigrantsThisAge = resultEmigrantsByAge.get(ageBracketThisAge);
				nEmigrantsThisAge += 1;
				resultEmigrantsByAge.put(ageBracketThisAge,nEmigrantsThisAge);
			}
			// updates the total number of individuals emigrated
			nIndivsEmigrated += Population.getHhPool().get(selectedHhID).getResidentsID().size();
			
			// removes randHhID and its residents from the population
			Population.removeHhold(selectedHhID);
			nHholdsEmigrated += 1;
		}
		/*
		int totalResultEmigrantsByAge = 0;
		for (Integer value : resultEmigrantsByAge.values()) {
			totalResultEmigrantsByAge += value;
		}
		*/
		Population.setAcutalIndivEmigrants(nIndivsEmigrated);
		Population.setAcutalHholdEmigrants(nHholdsEmigrated);
		
		outputMigrantsSummaryToCSV(targetEmigrantsByAge, resultEmigrantsByAge, HardcodedData.outputTablesPath+"emigrantsSummary_"+Integer.toString(yearIndex)+".csv");
		//TextFileHandler.writeToCSV(HardcodedData.outputTablesPath+"emigrantsDetails_"+Integer.toString(yearIndex)+".csv", new String[] {"emiHholdID", "emiIndivID", "emiIndivAge", "fromZone"}, emiHholdDetails);
		
		return vacatedDwellings;
	}

	
	
	/**
	 * 
	 * @param targetEmigrantsByAge
	 * @param resultEmigrantsByAge
	 * @param fileName
	 */
	private static void outputMigrantsSummaryToCSV(HashMap<int[],Integer> targetMigrantsByAge, HashMap<int[],Integer> resultMigrantsByAge, String fileName) {
		ArrayList<String[]> outputArray = new ArrayList<>();
		for (int[] ageBracket : targetMigrantsByAge.keySet()) {
			if (!resultMigrantsByAge.containsKey(ageBracket)) {
				System.out.println("resultMigrantsByAge doesn't have ageBracket [" + ageBracket[0] + "," + ageBracket[1] + "].");
			}
			String[] crnLine = {Integer.toString(ageBracket[0]), 
								Integer.toString(ageBracket[1]), 
								Integer.toString(targetMigrantsByAge.get(ageBracket)), 
								Integer.toString(resultMigrantsByAge.get(ageBracket))};
			outputArray.add(crnLine);
		}
		TextFileHandler.writeToCSV(fileName, new String[] {"lowerAge","upperAge","targetMigrants","resultMigrants"}, outputArray);
	}
	
	
	/**
	 * 
	 * @param queryAge
	 * @param ageBrackets
	 * @return
	 */
	private static int[] getAgeBracket(int queryAge, Set<int[]> ageBrackets) {
		for (int[] ageBracket : ageBrackets) {
			if (queryAge>=ageBracket[0] && queryAge<=ageBracket[1]) {
				return ageBracket;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param hhold
	 * @param targetEmigrantsByAge
	 * @param resultEmigrantsByAge
	 * @return
	 */
	private static double calcCrnHholdScore(Household hhold, HashMap<int[],Integer> targetMigrantsByAge, HashMap<int[],Integer> resultMigrantsByAge) {
		double hholdScore = 1;
		for (Integer resID : hhold.getResidentsID()) {
			int residentAge = Population.getIndivPool().get(resID).getAge();
			
			int nThisAgeInTarget = 0;
			int[] bracketThisAge = getAgeBracket(residentAge,targetMigrantsByAge.keySet());
			if (bracketThisAge!=null) {
				nThisAgeInTarget = targetMigrantsByAge.get(bracketThisAge);
			}
			
			int nThisAgeInResult = 0;
			bracketThisAge = getAgeBracket(residentAge,resultMigrantsByAge.keySet());
			if (bracketThisAge!=null) {
				nThisAgeInResult = resultMigrantsByAge.get(bracketThisAge);
			}
			
			double indivScore = 0;
			if (nThisAgeInTarget!=0) {
				double theoreticalScore = (double)(nThisAgeInTarget-nThisAgeInResult)/(double)nThisAgeInTarget;
				if (residentAge<=14) {
					theoreticalScore = theoreticalScore * HardcodedData.scaleFactorForEmigratingKids;
				}
				indivScore = Math.max(0, theoreticalScore); // ensures that number of actual emigrants of this age does not exceed the target value
				if (indivScore==0) {
					indivScore = HardcodedData.minIndivScoreForMigration;
				}
				
			}
			hholdScore = hholdScore*indivScore;
		}
		//hholdScore = Math.max(HardcodedData.minHholdScoreForMigration, hholdScore);
		
		return hholdScore;
	}
	
	/**
	 * 
	 * @param hhold
	 * @param targetMigrantsByAge
	 * @param resultMigrantsByAge
	 * @return
	 */
	private static double calcInitHholdScore(Household hhold, HashMap<int[],Integer> targetMigrantsByAge, HashMap<int[],Integer> resultMigrantsByAge) {
		double hholdScore = 1;
		for (Integer resID : hhold.getResidentsID()) {
			int residentAge = Population.getInitIndivPool().get(resID).getAge();
			
			int nThisAgeInTarget = 0;
			int[] bracketThisAge = getAgeBracket(residentAge,targetMigrantsByAge.keySet());
			if (bracketThisAge!=null) {
				nThisAgeInTarget = targetMigrantsByAge.get(bracketThisAge);
			}
			
			int nThisAgeInResult = 0;
			bracketThisAge = getAgeBracket(residentAge,resultMigrantsByAge.keySet());
			if (bracketThisAge!=null) {
				nThisAgeInResult = resultMigrantsByAge.get(bracketThisAge);
			}
			
			double indivScore = 0;
			if (nThisAgeInTarget!=0) {
				double theoreticalScore = (double)(nThisAgeInTarget-nThisAgeInResult)/(double)nThisAgeInTarget;
				if (residentAge<=HardcodedData.maxAgeU15) {
					theoreticalScore = theoreticalScore * HardcodedData.scaleFactorForImmigratingKids;
				}
				if (residentAge<=49 && Population.getInitIndivPool().get(resID).getGender().equals(Genders._female)) {
					theoreticalScore = theoreticalScore * HardcodedData.scaleFactorForImmigrating15_49Female;
				}
				indivScore = Math.max(0, theoreticalScore); // ensures that number of actual emigrants of this age does not exceed the target value
				if (indivScore==0) {
					indivScore = HardcodedData.minIndivScoreForMigration;
				}
			}
			hholdScore = hholdScore*indivScore;
		}
		//hholdScore = Math.max(HardcodedData.minHholdScoreForMigration, hholdScore);
		
		return hholdScore;
	}
	
	/**
	 * 
	 * @param totalIndivEmigrants
	 * @param ageDistrib
	 * @param epsilon
	 * @return
	 */
	private static HashMap<int[],Integer> calcTargetMigrantsByAge(int totalIndivMigrants,HashMap<int[],Double> ageDistrib, double epsilon) {
		HashMap<int[],Integer> targetMigrantsByAge = new HashMap<int[],Integer>();
		
		double sumValue = 0;
		for (Double value : ageDistrib.values()) {
			sumValue = sumValue + value;
		}
		
		// in the unlikely event that sum of values in ageDistrib is zero, and totalIndivMigrants is non-zero, assign a proportionally equal value to each age in ageDistrib
		if (sumValue<=0) {
			int equalVal = (int)Math.round((double)totalIndivMigrants/(double)ageDistrib.size()*(1+epsilon));
			for (int[] ageGroup : ageDistrib.keySet()) {
				targetMigrantsByAge.put(ageGroup, equalVal);
			}
		} else { // calculates the desired number of emigrants in each age following the given distribution ageDistrib
			for (int[] ageGroup : ageDistrib.keySet()) {
				double pcThisAgeGroup = ageDistrib.get(ageGroup);
				int nEmiIndivsThisAge = (int)Math.round(totalIndivMigrants * pcThisAgeGroup * (1+epsilon));
				targetMigrantsByAge.put(ageGroup, nEmiIndivsThisAge);
			}
		}
		
		return targetMigrantsByAge;
	}
	
	
	
	/**
	 * 
	 * @param nImmigrants
	 */
	public static void immigrateIndivBased(int nImmigrants) {
		int nPopImmigrated = 0;
		while (nPopImmigrated<nImmigrants) {
			//int[] hhIDs = ArrayHandler.toInt(initHhPool.keySet());
			//int selectedHhID = ArrayHandler.pickRandomFromArray(hhIDs, null, 1, HardcodedData.random)[0];
			Integer selectedHhID = (Integer)ArrayHandler.pickARandomElementFromSet(Population.getInitHhPool().keySet());
			
			ArrayList<Integer> initResidentsID = Population.getInitHhPool().get(selectedHhID).getResidentsID();
			
			ArrayList<Integer> newResidentsID = new ArrayList<Integer>();
			// adds new residents to Population.indivPool
			for (Integer resID : initResidentsID) {
				if (!Population.getInitIndivPool().containsKey(resID)) {
					System.out.println(resID + " doesn't exist in Population.initIndivPool. initResidentsID.size()=" + initResidentsID.size() + ", selectedHhID=" + selectedHhID);
					System.out.println("Other residents ID in init hhPool:");
					for (Integer otherRes : initResidentsID) {
						System.out.println(otherRes);
					}
					System.exit(0);
				}
				
				Individual newIndiv = new Individual(
						PopulationAnalytics.calculateNewIndivID(), 
						Population.getInitIndivPool().get(resID).getAge(), 
						Population.getInitIndivPool().get(resID).getGender(), 
						Population.getInitIndivPool().get(resID).getHhRel(),
						Population.getInitIndivPool().get(resID).getIncomeWkly());
				Population.addIndivToPopulation(newIndiv);
				
				newResidentsID.add((Integer)newIndiv.getId());
			}
			
			// adds this selectedHhID to Population.hhPool
			Household newHhold = new Household(PopulationAnalytics.calculateNewHholdID(), Population.getInitHhPool().get(selectedHhID).getHhType(), newResidentsID, HardcodedData.unknown, HardcodedData.unknown);
			newHhold.assignAggreHholdType();
			Population.addHholdToPopulation(newHhold);
			
			nPopImmigrated += newResidentsID.size();
			//System.out.println("\t\t" + nPopImmigrated + " immigrated");
			
			initResidentsID = null;
		}
	}
	
	
	/**
	 * 
	 * @param nImmigrantHholds
	 * @return ArrayList of ID of immigrated households
	 */
	public static ArrayList<Integer> immigrateHholdBased(int nImmigrantHholds) {
		
		ArrayList<Integer> immiHholdID = new ArrayList<Integer>();
		
		int immiIndivCounts = 0;
		
		int nHholdImmigrated = 0;
		while (nHholdImmigrated<nImmigrantHholds) {
			//int[] hhIDs = ArrayHandler.toInt(initHhPool.keySet());
			//int selectedHhID = ArrayHandler.pickRandomFromArray(hhIDs, null, 1, HardcodedData.random)[0];
			Integer selectedHhID = (Integer)ArrayHandler.pickARandomElementFromSet(Population.getInitHhPool().keySet());
			
			int newHholdID = immigrateHholdFromInitPopToCrnPop(selectedHhID, HardcodedData.unknown);
			
			/*
			ArrayList<Integer> initResidentsID = new ArrayList<Integer>();
			initResidentsID = Population.getInitHhPool().get(selectedHhID).getResidentsID();
			
			ArrayList<Integer> newResidentsID = new ArrayList<Integer>();
			// adds new residents to Population.indivPool
			for (Integer resID : initResidentsID) {
				if (!Population.getInitIndivPool().containsKey(resID)) {
					System.out.println(resID + " doesn't exist in Population.initIndivPool. initResidentsID.size()=" + initResidentsID.size() + ", selectedHhID=" + selectedHhID);
					System.out.println("Other residents ID in init hhPool:");
					for (Integer otherRes : initResidentsID) {
						System.out.println(otherRes);
					}
					System.exit(0);
				}
				
				int newIndivID = PopulationAnalytics.calculateNewIndivID();
				
				Individual newIndiv = new Individual(
						newIndivID, 
						Population.getInitIndivPool().get(resID).getAge(), 
						Population.getInitIndivPool().get(resID).getGender(), 
						Population.getInitIndivPool().get(resID).getHhRel(),
						Population.getInitIndivPool().get(resID).getIncomeWkly());
				Population.addIndivToPopulation(newIndiv);
				
				newResidentsID.add((Integer)newIndivID);
			}
			
			// adds this selectedHhID to Population.hhPool
			int newHholdID = PopulationAnalytics.calculateNewHholdID();
			Household newHhold = new Household(newHholdID, Population.getInitHhPool().get(selectedHhID).getHhType(), newResidentsID, HardcodedData.unknown, HardcodedData.unknown);
			newHhold.assignAggreHholdType();
			Population.addHholdToPopulation(newHhold);
			*/
			
			immiIndivCounts += Population.getInitHhPool().get(selectedHhID).getResidentsID().size();
			nHholdImmigrated += 1;
			
			immiHholdID.add((Integer)newHholdID);
		}
		
		//System.out.println("immigrated " + immiHholdID.size() + " hholds (of totally " + nImmigrantHholds + " hholds required), and " + immiIndivCounts + " indivs.");
		Population.setImmigrantCount(immiIndivCounts);
		
		return immiHholdID;
	}
	
	/**
	 * adds a household (and its residents) from initial population to the current population
	 * @param selectedHhID
	 * @return ID of immigrated household in the current population
	 */
	private static int immigrateHholdFromInitPopToCrnPop(Integer selectedInitHhID, String newZoneName) {
		ArrayList<Integer> initResidentsID = new ArrayList<Integer>();
		initResidentsID = Population.getInitHhPool().get(selectedInitHhID).getResidentsID();
		
		ArrayList<Integer> newResidentsID = new ArrayList<Integer>();
		// adds new residents to Population.indivPool
		for (Integer resID : initResidentsID) {
			if (!Population.getInitIndivPool().containsKey(resID)) {
				System.out.println(resID + " doesn't exist in Population.initIndivPool. initResidentsID.size()=" + initResidentsID.size() + ", selectedInitHhID=" + selectedInitHhID);
				System.out.println("Other residents ID in init hhPool:");
				for (Integer otherRes : initResidentsID) {
					System.out.println(otherRes);
				}
				System.exit(0);
			}
			
			int newIndivID = PopulationAnalytics.calculateNewIndivID();
			
			Individual newIndiv = new Individual(
					newIndivID, 
					Population.getInitIndivPool().get(resID).getAge(), 
					Population.getInitIndivPool().get(resID).getGender(), 
					Population.getInitIndivPool().get(resID).getHhRel(),
					Population.getInitIndivPool().get(resID).getIncomeWkly());
			Population.addIndivToPopulation(newIndiv);
			
			newResidentsID.add((Integer)newIndivID);
		}
		
		// adds this selectedHhID to Population.hhPool
		int newHholdID = PopulationAnalytics.calculateNewHholdID();
		Household newHhold = new Household(newHholdID, Population.getInitHhPool().get(selectedInitHhID).getHhType(), newResidentsID, newZoneName, HardcodedData.unknown);
		newHhold.assignAggreHholdType();
		Population.addHholdToPopulation(newHhold);
		
		return newHholdID;
	}
	
	/**
	 * 
	 * @param migrantHholdsID
	 * @param ageBrackets Rates.getImmigrantAgeDistrib().keySet()
	 * @param fileName
	 */
	public static void outputMigrantsToCSV(ArrayList<Integer> migrantHholdsID, Set<int[]> ageBrackets, String fileName) {
		HashMap<int[],Integer> migrantsByAgeGrp = new HashMap<int[],Integer>();
		for (Integer hhID : migrantHholdsID) {
			Household hhold = Population.getHhPool().get(hhID);
			for (Integer indivID : hhold.getResidentsID()) {
				int[] ageBracket = getAgeBracket(Population.getIndivPool().get(indivID).getAge(), ageBrackets);
				int nIndivsThisAge = 0;
				if (migrantsByAgeGrp.containsKey(ageBracket)) {
					nIndivsThisAge = migrantsByAgeGrp.get(ageBracket);
				}
				nIndivsThisAge += 1;
				migrantsByAgeGrp.put(ageBracket,nIndivsThisAge);
			}
		}

		ArrayList<String[]> outputArray = new ArrayList<>();
		for (int[] ageBracket : migrantsByAgeGrp.keySet()) {
			String[] crnLine = {Integer.toString(ageBracket[0]), Integer.toString(ageBracket[1]), Integer.toString(migrantsByAgeGrp.get(ageBracket))};
			outputArray.add(crnLine);
		}
		
		TextFileHandler.writeToCSV(fileName, new String[] {"lowerAge","upperAge","migrants"}, outputArray);
	}
}
