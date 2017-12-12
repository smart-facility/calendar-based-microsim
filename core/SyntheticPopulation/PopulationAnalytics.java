package core.SyntheticPopulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang.ArrayUtils;

import core.ArrayHandler;
import core.HardcodedData.AggreHholdTypes;
import core.HardcodedData.EmploymentStatus;
import core.HardcodedData.PlaceOfWork;

public class PopulationAnalytics {
	
	
	/**
	 * 
	 * @param emplStat
	 * @param workPlace
	 * @return
	 */
	public static int countIndivs(EmploymentStatus emplStat, PlaceOfWork workPlace) {
		int countResult = 0;
		
		for (Individual indiv : Population.getIndivPool().values()) {
			if (indiv.getEmpStat()!=null && indiv.getEmpStat().equals(emplStat) && indiv.getWorkPlace()!=null && indiv.getWorkPlace().equals(workPlace)) {
				countResult += 1;
			}
		}
		
		return countResult;
	}
	
	
	/**
	 * 
	 * @param emplStat
	 * @return
	 */
	public static ArrayList<Integer> getIDIndivs(EmploymentStatus emplStat, PlaceOfWork workPlace) {
		ArrayList<Integer> selectedIndivs = new ArrayList<Integer>();
		
		for (Integer indivID : Population.getIndivPool().keySet()) {
			Individual indiv = Population.getIndivPool().get(indivID);
			if (indiv.getEmpStat()!=null && indiv.getEmpStat().equals(emplStat) && indiv.getWorkPlace()!=null && indiv.getWorkPlace().equals(workPlace)) {
				selectedIndivs.add(indivID);
			}
		}
		
		return selectedIndivs;
	}
	
	/**
	 * 
	 * @param emplStat
	 * @return
	 */
	public static int countIndivs(EmploymentStatus emplStat) {
		int countResult = 0;
		
		for (Individual indiv : Population.getIndivPool().values()) {
			if (indiv.getEmpStat()!=null && indiv.getEmpStat().equals(emplStat)) {
				countResult += 1;
			}
		}
		
		return countResult;
	}
	
	
	/**
	 * 
	 * @param emplStat
	 * @return
	 */
	public static ArrayList<Integer> getIDIndivs(EmploymentStatus emplStat) {
		ArrayList<Integer> selectedIndivs = new ArrayList<Integer>();
		
		for (Integer indivID : Population.getIndivPool().keySet()) {
			Individual indiv = Population.getIndivPool().get(indivID);
			if (indiv.getEmpStat()!=null && indiv.getEmpStat().equals(emplStat)) {
				selectedIndivs.add(indivID);
			}
		}
		
		return selectedIndivs;
	}
	
	
	/**
	 * returns ID of individuals in Population.indivPool whose age is greater than or equal to minAge and smaller than or equal to maxAge.
	 * if maxAge in negative, the search will return ID of individuals whos age is greater than or equal to minAge.
	 * @param minAge
	 * @param maxAge
	 * @return
	 */
	public static ArrayList<Integer> getIDIndividualInAgeGroup(int minAge, int maxAge) {
		ArrayList<Integer> selectedIndivs = new ArrayList<Integer>();
		if (maxAge<0) {
			for (Integer indivID : Population.getIndivPool().keySet()) {
				if (Population.getIndivPool().get(indivID).getAge()>=minAge) {
					selectedIndivs.add(indivID);
				}
			}
		} else {
			for (Integer indivID : Population.getIndivPool().keySet()) {
				if (Population.getIndivPool().get(indivID).getAge()>=minAge && Population.getIndivPool().get(indivID).getAge()<=maxAge) {
					selectedIndivs.add(indivID);
				}
			}
		}

		return selectedIndivs;
	}
	
	
	/**
	 * returns ID of individuals in Population.indivPool whose age is greater than or equal to minAge.
	 * 
	 * @param minAge
	 * @return
	 */
	public static ArrayList<Integer> getIDIndividualAboveAge(int minAge) {
		ArrayList<Integer> selectedIndivs = new ArrayList<Integer>();
		for (Integer indivID : Population.getIndivPool().keySet()) {
			if (Population.getIndivPool().get(indivID).getAge()>=minAge) {
				selectedIndivs.add(indivID);
			}
		}
		return selectedIndivs;
	}
	
	
	/**
	 * sorts indivIDs by descending age of the corresponding individuals in the individual pool.
	 * If an ID in indivIDs does not exist in individual pool, it is not included in the return list. 
	 * @param indivIDs
	 * @return
	 */
	public static ArrayList<Integer> sortindivIDByAge(ArrayList<Integer> indivIDs) {
				
		// counts valid ID in indivIDs
		ArrayList<Integer> validIDs = new ArrayList<Integer>();
		for (int i=0; i<=indivIDs.size()-1; i++) {
			Integer crnID = indivIDs.get(i);
			if (Population.getIndivPool().containsKey(crnID)) {
				validIDs.add(crnID);
			}
		}
		
		// get age of indivs from validIDs
		int[] ages = new int[validIDs.size()];
		for (int i=0; i<=validIDs.size()-1; i++) {
			ages[i] = Population.getIndivPool().get(validIDs.get(i)).getAge();
		}
		
		int[] sortedAgeIdx = ArrayHandler.sortedIndices(ages);
		
		ArrayList<Integer> sortedValidIDs = new ArrayList<Integer>();
		for (int i=sortedAgeIdx.length-1; i>=0; i--) {
			sortedValidIDs.add(validIDs.get(sortedAgeIdx[i]));
		}
		
		return sortedValidIDs;
	}
	
	
	/**
	 * 
	 * @return an ID of a new household, which is calculated by adding 1 to the max ID in the household pool.
	 */
	public static int calculateNewHholdID() {
		int[] existingHhIDs = ArrayHandler.toInt(Population.getHhPool().keySet());
		int max = Collections.max(Arrays.asList(ArrayUtils.toObject(existingHhIDs)));
		return max+1;
		//return (existingHhIDs[ArrayHandler.getIndexOfMax(existingHhIDs)] + 1);
	}
	
	
	/**
	 * 
	 * @return an ID of a new individual, which is calculated by adding 1 to the max ID in the individual pool.
	 */
	public static int calculateNewIndivID() {
		int[] existingIndivIDs = ArrayHandler.toInt(Population.getIndivPool().keySet());
		int max = Collections.max(Arrays.asList(ArrayUtils.toObject(existingIndivIDs)));
		return max+1;
		//return (existingIndivIDs[ArrayHandler.getIndexOfMax(existingIndivIDs)] + 1);
	}
	
	
	public static int getHholdsOfType(AggreHholdTypes aggreHholdType) {
		int nHholdsThisType = 0;
		for (Household hhold : Population.getHhPool().values()) {
			if (hhold.getAggreHhType().equals(aggreHholdType)) {
				nHholdsThisType+=1;
			}
		}
		return nHholdsThisType;
	}
}
