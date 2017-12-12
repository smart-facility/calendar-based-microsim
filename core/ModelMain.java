package core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;

import org.apache.commons.io.FileUtils;

import core.SyntheticPopulation.*;
import core.SyntheticPopulation.AttributeAssignment.AttributeAssigner;


public class ModelMain {
	
	
	/**
	 * 
	 * example of executing from command prompt
	 * java -jar spEvoByOrder.jar 0 5 "input tables/" "dac2011_sp_28suburbs.csv" "yearlyImmiHholds.csv" "evoOrder.csv" "output/testEvoOrder_2_" 1
	 * or
	 * java -jar spEvoByOrder.jar 0 5 "input tables/" "pop_synth.db" "yearlyImmiHholds.csv" "evoOrder.csv" "output/testEvoOrder_2_" 1
	 * 
	 * @param args
	 *
	 * last arguments added to chose if date of birth is added (1 to add it, 0 otherwise)
	 * note that if it is added, aging needs to be last procedure
	 * @author of this addition: mdumont
	 */
	public static void main(String[] args) {
		
		String seedValueString = args[0];//"0"; //
		String nYearsString = args[1];//"5"; //
		String inputTablesPath = args[2];//"input tables/"; //
		String initSP = args[3];//"pop_synth.db"; //
		//String initSPFilename = args[3];//"dac2011_sp_28suburbs.csv"; //
		String yearlyImmiFilename = args[4];//"yearlyImmiHholds.csv"; //
		String evoOrderFilename = args[5];//"evoOrder.csv"; //
		String outputFolder = args[6];//"output/testEvoOrder_2_"; //
		String combiAgeInput = args[7];
		String combiDeathInput = args[8];
		
		if (Integer.parseInt(combiAgeInput) == 1) {
			CombiAge.combiAge = true;
			System.out.println("You choosed to add the date of birth");
		} else {
			CombiAge.combiAge=false;
			System.out.println("You choosed not to add the date of birth");
		}

		if (Integer.parseInt(combiDeathInput) == 1) {
			CombiAge.combiDeath = true;
			System.out.println("You choosed to add the date of death");
		} else {
			CombiAge.combiDeath=false;
			System.out.println("You choosed not to add the date of death");
		}
		System.out.println(" ");;
		
		/*
		String seedValueString = "0"; //
		String nYearsString = "5"; //
		String inputTablesPath = "input tables/"; //
		//String initSP = "pop_synth.db"; //
		String initSP = "dac2011_sp_28suburbs.csv"; //
		String yearlyImmiFilename = "yearlyImmiHholds.csv"; //
		String evoOrderFilename = "evoOrder.csv"; //
		String outputFolder = "output/testEvoOrder_1_";  //
		*/
		
		String outputTablesPath = outputFolder + seedValueString + "/";
		
		HardcodedData.inputTablesPath = inputTablesPath;
		HardcodedData.initOutPutPath(outputTablesPath);
		String fNameImmiHholds = HardcodedData.inputTablesPath + yearlyImmiFilename;
		
		int nYears;
		try {
			nYears = Integer.parseInt(nYearsString);
		} catch (NumberFormatException e) {
			System.out.println(nYearsString + " is invalid for the number of years for population evolution. Abort evolution!");
			return;
		}
		
		int seedValue;
		try {
			seedValue = Integer.parseInt(seedValueString);
			HardcodedData.setSeed(seedValue);
		} catch (NumberFormatException e) {
			System.out.println(seedValueString + " is invalid for seed value for pseudo-random number generator. Abort evolution!");
			return;
		}
		
		String initSPFileType = initSP.split("[.]")[1];
		if (initSPFileType.equals("csv")) {
			String initSPLocation = HardcodedData.inputTablesPath + initSP;
			PopulationIOHandler.initialisePopulationFromCSV(initSPLocation);
		} else if (initSPFileType.equals("db")) {
			String initSPLocation = "jdbc:sqlite:" + HardcodedData.inputTablesPath + initSP;
			PopulationIOHandler.initialisePopulationFromDB(initSPLocation);
		}
		if (Population.getHhPool()==null || Population.getIndivPool()==null) {
			System.out.println("Failed to retrieve initial population. Aborting evolution!");
			return;
		}
		System.out.println("Finish initialisePopulationFromCSV");
		
		// reads in evolution rates
		Rates.importRates(HardcodedData.inputTablesPath + "rates/birth_rates.csv", 
				HardcodedData.inputTablesPath + "rates/death_rates_female.csv",
				HardcodedData.inputTablesPath + "rates/death_rates_male.csv", 
				HardcodedData.inputTablesPath + "rates/divorce_rates_female.csv", 
				HardcodedData.inputTablesPath + "rates/divorce_rates_male.csv", 
				HardcodedData.inputTablesPath + "rates/marriage_rates_female.csv", 
				HardcodedData.inputTablesPath + "rates/marriage_rates_male.csv");
		
		Rates.importImmiHHoldsByYear(fNameImmiHholds);
		
		if (Rates.getBirthRates()==null ||
				Rates.getDeathRatesFemale()==null || Rates.getDeathRatesMale()==null ||
				Rates.getDivorceRatesFemale()==null || Rates.getDivorceRatesMale()==null ||
				Rates.getMarriageRatesFemale()==null || Rates.getMarriageRatesMale()==null) {
			System.out.println("At least one of the evolution rates are null. Abort evolution!");
			return;
		}
		
		int[] evoOrder = Rates.readInEvolutionOrder(HardcodedData.inputTablesPath + evoOrderFilename);
		if (evoOrder==null) {
			System.out.println("Invalid integer values for evoultion order in " + HardcodedData.inputTablesPath + evoOrderFilename + ". Abort evolution!");
		}
		if (ArrayHandler.max(evoOrder)>4) {
			System.out.println("Max evolution step is larger than 4. Abort evolution!");
		}
		System.out.println("importing data finished.\n");
		
		Population.startEvoWithFunctorAndSimpleHholdMigration(nYears, HardcodedData.outputTablesPath + "totalPopByYear.csv", evoOrder);
	}
}
