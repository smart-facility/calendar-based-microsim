package core.SyntheticPopulation.Features;

import java.util.ArrayList;

import core.ArrayHandler;
import core.HardcodedData;
import core.SyntheticPopulation.Individual;
import core.SyntheticPopulation.Population;

public class Incomes {
	
	/**
	 * 
	 */
	public static void increaseIncome() {
		for (Individual indiv : Population.getIndivPool().values()) {
			if (indiv.getAge()>HardcodedData.maxAgeU15) {
				int newIncome = (int)(indiv.getIncomeWkly()*(1+HardcodedData.inflationYrly));
				indiv.setIncomeWkly(newIncome);
			}
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public static int calculateTotalWklyIncome() {
		int totalPopIncomeWkly = 0;
		for (Individual indiv : Population.getIndivPool().values()) {
			totalPopIncomeWkly += indiv.getIncomeWkly();
		}
		return totalPopIncomeWkly;
	}
	
	
	/**
	 * reduces the weekly income of nIndivs (randomly selected from Population.indivPool) to the income specified by HardcodedData.minIncomeWkly
	 * If the person already has income lower than HardcodedData.minIncomeWkly, his/her income is dropped to 0.
	 * @param nJobLost
	 */
	public static void reduceIncomeToMin(int nJobLost) {
		
		// gets ID of individual in Population.indivPool that are between 15 and 65 inclusively and weekly income is larger than 0.
		ArrayList<Integer> workingIndivsID = new ArrayList<Integer>();
		for (Integer indivID : Population.getIndivPool().keySet()) {
			int crnAge = Population.getIndivPool().get(indivID).getAge();
			int crnIncome = Population.getIndivPool().get(indivID).getIncomeWkly();
			if (crnAge>=HardcodedData.minAgeO15 && crnAge<=HardcodedData.retiredAge && crnIncome>0) {
				workingIndivsID.add(indivID);
			}
		}
		
		if (workingIndivsID==null || workingIndivsID.size()==0) { // no one in the population is working.
			return; // don't need to change anything.
		} else if (workingIndivsID.size()<nJobLost) {
			nJobLost = workingIndivsID.size();
		}
		
		ArrayList<Integer> selectedIDs = ArrayHandler.pickRandomFromList(workingIndivsID, nJobLost);
		
		//int[] selectedIDs = ArrayHandler.pickRandomFromArray(ArrayHandler.toInt(workingIndivsID), null, nJobLost, HardcodedData.random);
		for (Integer indivID : selectedIDs) {
			if (Population.getIndivPool().get(indivID).getIncomeWkly()>HardcodedData.minIncomeWkly) {
				Population.getIndivPool().get(indivID).setIncomeWkly(HardcodedData.minIncomeWkly);
			} else {
				Population.getIndivPool().get(indivID).setIncomeWkly(0);
			}
		}
		
		workingIndivsID = null;
	}
}
