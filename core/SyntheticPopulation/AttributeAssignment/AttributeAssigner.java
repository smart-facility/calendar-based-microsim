package core.SyntheticPopulation.AttributeAssignment;

import java.util.ArrayList;
import java.util.Collections;

import core.HardcodedData;
import core.HardcodedData.PlaceOfWork;
import core.TextFileHandler;
import core.HardcodedData.EmploymentStatus;
import core.SyntheticPopulation.Individual;
import core.SyntheticPopulation.Population;
import core.SyntheticPopulation.PopulationAnalytics;

public class AttributeAssigner {

	/**
	 * 
	 */
	public static void assignWorkPlace() {
		
		ArrayList<Integer> employedIDs = PopulationAnalytics.getIDIndivs(EmploymentStatus.employed);
		int nEmployed = employedIDs.size();
		int initLiveInWorkIn = Population.getInitLiveInWorkIn();
		int initLiveInWorkOut = Population.getInitLiveInWorkOut();
		
		int realLiveInWorkIn = (int)((double)initLiveInWorkIn/(double)(initLiveInWorkIn+initLiveInWorkOut)*(double)nEmployed);
		
		Collections.shuffle(employedIDs);
		
		int countWorkIn = 0;
		
		for (Integer indivID : employedIDs) {
			if (countWorkIn<realLiveInWorkIn) {
				Population.getIndivPool().get(indivID).setWorkPlace(PlaceOfWork.in);
				countWorkIn += 1;
			} else {
				Population.getIndivPool().get(indivID).setWorkPlace(PlaceOfWork.out);
			}
		}
	}
	
	
	/**
	 * 
	 * @param inputFile
	 */
	public static void assignEmploymentStatus(String inputFile) {
		ArrayList<ArrayList<String>> raw = TextFileHandler.readCSV(inputFile);
		
		// assigning employed
		for (int iRow=1; iRow<=raw.size()-1; iRow++) {
			int minAge;
			int maxAge;
			int nEmployed;
			int nUnemployed;
			int nonLabour;
			try {
				minAge = Integer.parseInt(raw.get(iRow).get(0));
				maxAge = Integer.parseInt(raw.get(iRow).get(1));
				nEmployed = Integer.parseInt(raw.get(iRow).get(2));
				nUnemployed = Integer.parseInt(raw.get(iRow).get(3));
				nonLabour = Integer.parseInt(raw.get(iRow).get(4));
				
				int censusTotal = nEmployed + nUnemployed + nonLabour;
				
				// gets individuals in this age group
				ArrayList<Integer> indivsIDThisAgeGroup = PopulationAnalytics.getIDIndividualInAgeGroup(minAge, maxAge);
				int nValidIndivs = indivsIDThisAgeGroup.size();
				int truEmployed = (int)((double)nValidIndivs*(double)nEmployed/(double)censusTotal);
				int truUnemployed = (int)((double)nValidIndivs*(double)nUnemployed/(double)censusTotal);
				int truNonLabour = nValidIndivs - truEmployed - truUnemployed;
				
				Collections.shuffle(indivsIDThisAgeGroup);
				
				for (int i=1; i<=truEmployed; i++) {
					Population.getIndivPool().get(indivsIDThisAgeGroup.get(i-1)).setEmpStat(EmploymentStatus.employed);
				}

				for (int i=truEmployed+1; i<=truEmployed+truUnemployed; i++) {
					Population.getIndivPool().get(indivsIDThisAgeGroup.get(i-1)).setEmpStat(EmploymentStatus.unEmployed);
					Population.getIndivPool().get(indivsIDThisAgeGroup.get(i-1)).setWorkPlace(null);
				}

				for (int i=truEmployed+truUnemployed+1; i<=nValidIndivs; i++) {
					Population.getIndivPool().get(indivsIDThisAgeGroup.get(i-1)).setEmpStat(EmploymentStatus.nonLabour);
					Population.getIndivPool().get(indivsIDThisAgeGroup.get(i-1)).setWorkPlace(null);
				}
				
			} catch (NumberFormatException e) {
				System.out.println("Failed to initialise employment status for age group " + raw.get(iRow).get(0) + "-" + raw.get(iRow).get(1));
				System.out.println("Check " + inputFile);
				continue;
			}
			
		}
		/*
		for (ArrayList<String> row : raw) {
			System.out.println(row.get(0) + ", " + row.get(1) + ", " + row.get(2) + ", " + row.get(3) + ", " + row.get(4));
		}
		*/
	}
	
	
}
