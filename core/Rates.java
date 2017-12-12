package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import core.HardcodedData.InputBirthRatesColumn;
import core.HardcodedData.InputRatesColumn;
import core.SyntheticPopulation.Population;

public class Rates {
	private static HashMap<Integer,double[]> birthRates;
	private static HashMap<Integer,Double> deathRatesFemale;
	private static HashMap<Integer,Double> deathRatesMale;
	private static HashMap<Integer,Double> divorceRatesFemale;
	private static HashMap<Integer,Double> divorceRatesMale;
	private static HashMap<Integer,Double> marriageRatesFemale;
	private static HashMap<Integer,Double> marriageRatesMale;
	private static HashMap<Integer,Double> jobChangeByYear;
	private static HashMap<Integer,Double> percentLIWOByYear;
	private static double ratioPopulationByJob;
	private static HashMap<Integer,Double> immiHholdsByYear; 
	private static HashMap<int[],Double> emigrantAgeDistrib;
	private static HashMap<int[],Double> immigrantAgeDistrib;
	
	/**
	 * 
	 * @param synPopCSV
	 * @param birthRatesCSV
	 * @param deathRatesFemaleCSV
	 * @param deathRatesMaleCSV
	 * @param divorceRatesFemaleCSV
	 * @param divorceRatesMaleCSV
	 * @param marriageRatesFemaleCSV
	 * @param marriageRatesMaleCSV
	 */
	public static void importRates(String birthRatesCSV, 
			String deathRatesFemaleCSV, String deathRatesMaleCSV, String divorceRatesFemaleCSV, String divorceRatesMaleCSV,
			String marriageRatesFemaleCSV, String marriageRatesMaleCSV) {
		
		setBirthRates(readInBirthRates(birthRatesCSV));
		setDeathRatesFemale(readInRates(deathRatesFemaleCSV));
		setDeathRatesMale(readInRates(deathRatesMaleCSV));
		setDivorceRatesFemale(readInRates(divorceRatesFemaleCSV));
		setDivorceRatesMale(readInRates(divorceRatesMaleCSV));
		setMarriageRatesFemale(readInRates(marriageRatesFemaleCSV));
		setMarriageRatesMale(readInRates(marriageRatesMaleCSV));
	}
	
	/**
	 * 
	 * @param fNamePercentLIWOByYear
	 */
	public static void importPercentLIWOByYear(String fNamePercentLIWOByYear) {
		setPercentLIWOByYear(readInRates(fNamePercentLIWOByYear));
	}
	
	
	/**
	 * 
	 * @param jobChangeByYearCSV
	 */
	public static void importJobChangeByYear(String jobChangeByYearCSV) {
		setJobChangeByYear(readInRates(jobChangeByYearCSV));
	}
	
	
	public static void importImmiHHoldsByYear(String fNameImmiHholds) {
		setImmiHholdsByYear(readInRates(fNameImmiHholds));
	}
	
	
	private static HashMap<int[],Double> prepareMigrantAgeDistrib(HashMap<Integer,Double> rawAgeDistrib) {
		HashMap<int[],Double> finalMigrantAgeDistrib = new HashMap<int[],Double>();
		
		ArrayList<Integer> rawAges = new ArrayList<Integer>(rawAgeDistrib.keySet());
		Collections.sort(rawAges);
		
		for (int iRawAge=0; iRawAge<=rawAges.size()-1; iRawAge++) {
			int lowerBoundAge = rawAges.get(iRawAge);
			int upperBoundAge = 150;
			if (iRawAge<rawAges.size()-1) {
				upperBoundAge = rawAges.get(iRawAge+1)-1;
			}
			int[] crnAgeBracket = new int[] {lowerBoundAge, upperBoundAge};
			double crnRate = rawAgeDistrib.get(rawAges.get(iRawAge));
			finalMigrantAgeDistrib.put(crnAgeBracket, crnRate);
		}
		
		return finalMigrantAgeDistrib;
	}
	
	/**
	 * it is assumed that the age distribution in file fNameNetEmigrantAgeDistrib has been normalised.
	 * @param fNameNetEmigrantAgeDistrib
	 */
	public static void importEmigrantAgeDistrib(String fNameEmigrantAgeDistrib) {
		HashMap<Integer,Double> rawAgeDistrib = readInRates(fNameEmigrantAgeDistrib);
		HashMap<int[],Double> processedAgeDistrib = prepareMigrantAgeDistrib(rawAgeDistrib);
		setEmigrantAgeDistrib(processedAgeDistrib);
	}
	
	/**
	 * it is assumed that the age distribution in file fNameImmigrantAgeDistrib has been normalised
	 * @param fNameImmigrantAgeDistrib
	 */
	public static void importImmigrantAgeDistrib(String fNameImmigrantAgeDistrib) {
		HashMap<Integer,Double> rawAgeDistrib = readInRates(fNameImmigrantAgeDistrib);
		HashMap<int[],Double> processedAgeDistrib = prepareMigrantAgeDistrib(rawAgeDistrib);
		setImmigrantAgeDistrib(processedAgeDistrib);
	}
	
	/**
	 * 
	 * @param birthRatesCSV
	 * @return
	 */
	private static HashMap<Integer,double[]> readInBirthRates(String birthRatesCSV) {
		ArrayList<ArrayList<String>> rawCSV = TextFileHandler.readCSV(birthRatesCSV);
		
		if (rawCSV==null) {
			return null;
		}
		
		HashMap<Integer,double[]> birthRates = new HashMap<Integer,double[]>();
		
		for (ArrayList<String> row : rawCSV) {
			String ageStr = row.get(InputBirthRatesColumn.age.getIndex());
			String child1 = row.get(InputBirthRatesColumn.firstChild.getIndex());
			String child2 = row.get(InputBirthRatesColumn.secondChild.getIndex());
			String child3 = row.get(InputBirthRatesColumn.thirdChild.getIndex());
			String child4 = row.get(InputBirthRatesColumn.fourthChild.getIndex());
			String child5 = row.get(InputBirthRatesColumn.fifthChild.getIndex());
			String child6 = row.get(InputBirthRatesColumn.sixthChildMore.getIndex());
			
			int age;
			double prob1Child;
			double prob2Child;
			double prob3Child;
			double prob4Child;
			double prob5Child;
			double prob6Child;
			try {
				age = Integer.parseInt(ageStr);
				prob1Child = Double.parseDouble(child1);
				prob2Child = Double.parseDouble(child2);
				prob3Child = Double.parseDouble(child3);
				prob4Child = Double.parseDouble(child4);
				prob5Child = Double.parseDouble(child5);
				prob6Child = Double.parseDouble(child6);
			} catch (Exception e) {
				continue;
			}
			
			birthRates.put(age, new double[] {prob1Child, prob2Child, prob3Child, prob4Child, prob5Child, prob6Child});
		}
				
		return birthRates;
	}
	
	
	/**
	 * 
	 * @param ratesCSV
	 * @return
	 */
	private static HashMap<Integer,Double> readInRates(String ratesCSV) {
		ArrayList<ArrayList<String>> rawCSV = TextFileHandler.readCSV(ratesCSV);
		
		if (rawCSV==null) {
			return null;
		}
		
		HashMap<Integer,Double> rates = new HashMap<Integer,Double>();
		
		for (ArrayList<String> row : rawCSV) {
			String ageStr = row.get(InputRatesColumn.age.getIndex());
			String probStr = row.get(InputRatesColumn.probability.getIndex());
			
			int age;
			double prob;
			try {
				age = Integer.parseInt(ageStr);
				prob = Double.parseDouble(probStr);
			} catch (Exception e) {
				continue;
			}
			
			rates.put(age, prob);
		}
		
		return rates;
	}
	
	
	/**
	 * 
	 * @param evoOrderFilename
	 * @return
	 */
	public static int[] readInEvolutionOrder(String evoOrderFilename) {
		ArrayList<ArrayList<String>> rawData = TextFileHandler.readCSV(evoOrderFilename);
		int[] evoOrder = new int[rawData.size()];
		for (int i=0; i<=rawData.size()-1; i++) {
			try {
				evoOrder[i] = Integer.parseInt(rawData.get(i).get(0));
			} catch (Exception e) {
				return null;
			}
		}
		
		
		return evoOrder;
	}
	
	/**
	 * initialises dwelling projections that were provided by City Futures @ UNSW as part of the DAC project (2016)
	 * @param dwellingProjectFileName
	 */
	public static void initDwellingForecasts(String dwellingProjectFileName) {
		ArrayList<ArrayList<String>> rawData = TextFileHandler.readCSV(dwellingProjectFileName);
		
		HashMap<Integer, HashMap<String,Integer>> newDwellingsByZoneByEvoYear = new HashMap<Integer, HashMap<String,Integer>>();
		
		//System.out.println(rawData.size());
		
		for (ArrayList<String> row : rawData) {
			String zoneName = row.get(0);
			for (int iYear=1; iYear<=row.size()-1; iYear++) {
				try {
					int newDwellings = Integer.parseInt(row.get(iYear));
					
					HashMap<String,Integer> newDwellingsThisYear = new HashMap<String,Integer>();
					if (newDwellingsByZoneByEvoYear.containsKey(iYear)) {
						newDwellingsThisYear = newDwellingsByZoneByEvoYear.get(iYear);
					}
					newDwellingsThisYear.put(zoneName, newDwellings);
					
					newDwellingsByZoneByEvoYear.put(iYear, newDwellingsThisYear);
				} catch (Exception e) {
					continue;
				} 
			}
		}
		
		Population.setNewDwellingsByZoneByEvoYear(newDwellingsByZoneByEvoYear);
	}
	
	
	
	public static HashMap<Integer, double[]> getBirthRates() {
		return birthRates;
	}
	public static void setBirthRates(HashMap<Integer, double[]> birthRates) {
		Rates.birthRates = birthRates;
	}
	public static HashMap<Integer, Double> getDeathRatesFemale() {
		return deathRatesFemale;
	}
	public static void setDeathRatesFemale(HashMap<Integer, Double> deathRatesFemale) {
		Rates.deathRatesFemale = deathRatesFemale;
	}
	public static HashMap<Integer, Double> getDeathRatesMale() {
		return deathRatesMale;
	}
	public static void setDeathRatesMale(HashMap<Integer, Double> deathRatesMale) {
		Rates.deathRatesMale = deathRatesMale;
	}
	public static HashMap<Integer, Double> getDivorceRatesFemale() {
		return divorceRatesFemale;
	}
	public static void setDivorceRatesFemale(
			HashMap<Integer, Double> divorceRatesFemale) {
		Rates.divorceRatesFemale = divorceRatesFemale;
	}
	public static HashMap<Integer, Double> getDivorceRatesMale() {
		return divorceRatesMale;
	}
	public static void setDivorceRatesMale(HashMap<Integer, Double> divorceRatesMale) {
		Rates.divorceRatesMale = divorceRatesMale;
	}
	public static HashMap<Integer, Double> getMarriageRatesFemale() {
		return marriageRatesFemale;
	}
	public static void setMarriageRatesFemale(
			HashMap<Integer, Double> marriageRatesFemale) {
		Rates.marriageRatesFemale = marriageRatesFemale;
	}
	public static HashMap<Integer, Double> getMarriageRatesMale() {
		return marriageRatesMale;
	}
	public static void setMarriageRatesMale(
			HashMap<Integer, Double> marriageRatesMale) {
		Rates.marriageRatesMale = marriageRatesMale;
	}
	public static HashMap<Integer,Double> getJobChangeByYear() {
		return jobChangeByYear;
	}
	public static void setJobChangeByYear(HashMap<Integer,Double> jobChangeByYear) {
		Rates.jobChangeByYear = jobChangeByYear;
	}
	public static double getRatioPopulationByJob() {
		return ratioPopulationByJob;
	}
	public static void setRatioPopulationByJob(double ratioPopulationByJob) {
		Rates.ratioPopulationByJob = ratioPopulationByJob;
	}


	public static HashMap<Integer,Double> getImmiHholdsByYear() {
		return immiHholdsByYear;
	}


	public static void setImmiHholdsByYear(HashMap<Integer,Double> immiHholdsByYear) {
		Rates.immiHholdsByYear = immiHholdsByYear;
	}
	
	public static HashMap<int[], Double> getEmigrantAgeDistrib() {
		return emigrantAgeDistrib;
	}


	public static void setEmigrantAgeDistrib(HashMap<int[], Double> emigrantAgeDistrib) {
		Rates.emigrantAgeDistrib = emigrantAgeDistrib;
	}

	public static HashMap<int[], Double> getImmigrantAgeDistrib() {
		return immigrantAgeDistrib;
	}


	public static void setImmigrantAgeDistrib(HashMap<int[], Double> immigrantAgeDistrib) {
		Rates.immigrantAgeDistrib = immigrantAgeDistrib;
	}

	public static HashMap<Integer,Double> getPercentLIWOByYear() {
		return percentLIWOByYear;
	}

	public static void setPercentLIWOByYear(HashMap<Integer,Double> percentLIWOByYear) {
		Rates.percentLIWOByYear = percentLIWOByYear;
	}
}
