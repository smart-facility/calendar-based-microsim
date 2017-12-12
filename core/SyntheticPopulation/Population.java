package core.SyntheticPopulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang.ArrayUtils;

import core.HardcodedData.*;
import core.SyntheticPopulation.Features.Dwellings;
import core.SyntheticPopulation.Features.Functor;
import core.SyntheticPopulation.Features.Migrations;
import core.SyntheticPopulation.Features.NaturalEvolution;
import core.SyntheticPopulation.Features.Employments;
import core.ArrayHandler;
import core.HardcodedData;
import core.Rates;
import core.TextFileHandler;

public class Population {
	private static HashMap<Integer,Household> initHhPool;
	private static HashMap<Integer,Individual> initIndivPool;
	private static HashMap<Integer,Household> hhPool;
	private static HashMap<Integer,Individual> indivPool;
	
	private static int crnLiveInWorkIn;
	private static int crnLiveInWorkOut;
	private static int crnLiveOutWorkIn;
	
	private static int initLiveInWorkIn;
	private static int initLiveInWorkOut;
	private static int initLiveOutWorkIn;
	
	private static int deadLIWICount;
	private static int deadLIWOCount;
	private static int deadNonLabCount;
	private static int deadUnEmpCount;
	private static int deadEmpCount;
	
	private static int immigrantCount;
	private static int outEmployedImmigrantCount;
	private static int unEmployedImmigrantCount;
	
	private static HashMap<String,int[]> dwellingForecastsByZone;
	private static HashMap<Integer, HashMap<String,Integer>> newDwellingsByZoneByEvoYear;
	
	private static double pcEmigrantsInPopulation;
	private static double pcImmigrantsInPopulation;
	
	
	// this is the number of families immigrating into the area based on the job number available
	// for example if there are 50 jobs available and the ratio is 0.4, the number of immigrant families is 50*0.4 = 20 families.
	// this ratio must be positive and be smaller than or equal to 1 to ensure that any immigrant family takes up at least a job.
	private static double jobImmiFamiRatio; 
	
	// this is the proportion of immigrants already have a job outside the region (e.g. in Sydney). Value 1 means that all immigrants are already employed outside region.
	private static double outEmployedImmgrantRatio;
	
	// this is the proportion of new jobs dedicated to local people that will be allocated to the unemployed people currently in the study area.
	// The remaining of the new jobs will be assigned to immigrants (2nd immigration) that move into the region to pick up new jobs.
	private static double newJobsToLocalUnEmplRatio;
	
	// this is the percentage of NonLabour people among the immigrants.  
	private static double pcNonLabourImmigrants;
	
	/**
	 * 
	 * @author nam
	 *
	 */
	private enum DwellingInfoColumns {
		newlyConstructed(0),
		vacatedByEmigrants(1),
		to1stImmigrants(2),
		toEvolvedHholds(3),
		to2ndImmigrants(4),
		available(5);
		
		private int index;
		
		private DwellingInfoColumns(int newIndex) {
			index = newIndex;
		}
		
		public int getIndex() {
			return index;
		}
	}
	
	private static int actualIndivEmigrants;
	public static void setAcutalIndivEmigrants(int newValue) {
		actualIndivEmigrants = newValue;
	}
	private static int actualHholdEmigrants;
	public static void setAcutalHholdEmigrants(int newValue) {
		actualHholdEmigrants = newValue;
	}
	
	private static int actualIndivImmigrants1st;
	public static void setActualIndivImmigrants1st(int newValue) {
		actualIndivImmigrants1st = newValue;
	}
	private static int actualHholdImmigrants1st;
	public static void setActualHholdImmigrants1st(int newValue) {
		actualHholdImmigrants1st = newValue;
	}
	
	/**
	 * 
	 * @param nYears
	 * @param outputFile
	 * @param evolutionOrder
	 */
	public static void startEvoInclEmigrationAndImmigration(int startYear, int nYears, int[] evolutionOrder) {
		Dwellings.evaluateDwellingNeeds();
		PopulationIOHandler.outputPopulation(HardcodedData.outputTablesPath + "year0_SP.csv");
		
		String[] headerPopulationSummary = new String[] {"year","population",
				"m_0_4","m_5_9",
				"m_10_14","m_15_19",
				"m_20_24","m_25_29",
				"m_30_34","m_35_39",
				"m_40_44","m_45_49",
				"m_50_54","m_55_59",
				"m_60_64","m_65_69",
				"m_70_74","m_75_79",
				"m_80_84","m_85_above",
				"f_0_4","f_5_9",
				"f_10_14","f_15_19",
				"f_20_24","f_25_29",
				"f_30_34","f_35_39",
				"f_40_44","f_45_49",
				"f_50_54","f_55_59",
				"f_60_64","f_65_69",
				"f_70_74","f_75_79",
				"f_80_84","f_85_above"};
		int[][] popSummary = new int[nYears+1][headerPopulationSummary.length];
		popSummary[0] = new int[] {startYear, Population.getIndivPool().size(),
				getMaleCountsBetweenAges(0,4),getMaleCountsBetweenAges(5,9),
				getMaleCountsBetweenAges(10,14),getMaleCountsBetweenAges(15,19),
				getMaleCountsBetweenAges(20,24),getMaleCountsBetweenAges(25,29),
				getMaleCountsBetweenAges(30,34),getMaleCountsBetweenAges(35,39),
				getMaleCountsBetweenAges(40,44),getMaleCountsBetweenAges(45,49),
				getMaleCountsBetweenAges(50,54),getMaleCountsBetweenAges(55,59),
				getMaleCountsBetweenAges(60,64),getMaleCountsBetweenAges(65,69),
				getMaleCountsBetweenAges(70,74),getMaleCountsBetweenAges(75,79),
				getMaleCountsBetweenAges(80,84),getMaleCountsBetweenAges(85,199),
				getFemaleCountsBetweenAges(0,4),getFemaleCountsBetweenAges(5,9),
				getFemaleCountsBetweenAges(10,14),getFemaleCountsBetweenAges(15,19),
				getFemaleCountsBetweenAges(20,24),getFemaleCountsBetweenAges(25,29),
				getFemaleCountsBetweenAges(30,34),getFemaleCountsBetweenAges(35,39),
				getFemaleCountsBetweenAges(40,44),getFemaleCountsBetweenAges(45,49),
				getFemaleCountsBetweenAges(50,54),getFemaleCountsBetweenAges(55,59),
				getFemaleCountsBetweenAges(60,64),getFemaleCountsBetweenAges(65,69),
				getFemaleCountsBetweenAges(70,74),getFemaleCountsBetweenAges(75,79),
				getFemaleCountsBetweenAges(80,84),getFemaleCountsBetweenAges(85,199)};
		
		
		String[] headerHholdSummary = new String[] {"year", "nHholds", "couple", "coupleU15", "coupleO15", "coupleU15O15", 
				"loneParentU15", "loneParentO15", "loneParentU15O15", "Other"};
		int[][] hholdSummary = new int[nYears+1][headerHholdSummary.length];
		hholdSummary[0] = new int[] {startYear, Population.getHhPool().size(),
				PopulationAnalytics.getHholdsOfType(AggreHholdTypes.couple),
				PopulationAnalytics.getHholdsOfType(AggreHholdTypes.coupleU15),
				PopulationAnalytics.getHholdsOfType(AggreHholdTypes.coupleO15),
				PopulationAnalytics.getHholdsOfType(AggreHholdTypes.coupleU15O15),
				PopulationAnalytics.getHholdsOfType(AggreHholdTypes.loneParentU15),
				PopulationAnalytics.getHholdsOfType(AggreHholdTypes.loneParentO15),
				PopulationAnalytics.getHholdsOfType(AggreHholdTypes.loneParentU15O15),
				PopulationAnalytics.getHholdsOfType(AggreHholdTypes.Other)};
		
		
		String[] headerMigrationSummary = new String[] {"year","population","nHholds","newHholdsCreatedFromEvolution", "totalVacDwellingsFromEvo",
				"newlyConstructedDwellings","emigrants","emigrantHholds","vacatedDwellings","immigrants1st","immigrantHholds1st","dwellingsToImmigrants1st",
				"dwellingsToNewHholdsFromEvo",
				"extraEmigrantHholds","extraEmigrants","immigrantHholds2nd","immigrants2nd","dwellingsToImmigrants2nd"};
		int[][] migrationSummary = new int[nYears+1][headerMigrationSummary.length];
		migrationSummary[0][0] = startYear;
		migrationSummary[0][1] = Population.getIndivPool().size();
		migrationSummary[0][2] = Population.getHhPool().size();
		migrationSummary[0][3] = 0;
		migrationSummary[0][4] = 0;
		migrationSummary[0][5] = 0;
		migrationSummary[0][6] = 0;
		migrationSummary[0][7] = 0;
		migrationSummary[0][8] = 0;
		migrationSummary[0][9] = 0;
		migrationSummary[0][10] = 0;
		migrationSummary[0][11] = 0;
		migrationSummary[0][12] = 0;
		migrationSummary[0][13] = 0;
		migrationSummary[0][14] = 0;
		migrationSummary[0][15] = 0;
		migrationSummary[0][16] = 0;
		migrationSummary[0][17] = 0;
		//totalPop[0][9] = Population.getIndivCountsBetweenAges(5,11);
		//totalPop[0][10] = Population.getFemaleCountsBetweenAges(15,49);
		
		for (int iYear=1; iYear<=nYears; iYear++) {
			int totalPopLastYear = Population.getIndivPool().size();
			int totalNewlyConstructedDwellings = 0;
			int totalIndivEmigrants = (int)Math.round(pcEmigrantsInPopulation*(double)totalPopLastYear);
			int totalActualEmigrants = 0;
			int totalHholdEmigrants = 0;
			int totalVacatedDwellings = 0;
			int totalIndivImmigrants = (int)Math.round(pcImmigrantsInPopulation*(double)totalPopLastYear);
			int totalActualImmigrants1st = 0;
			int totalHholdImmigrants1st = 0;
			int totalDwellingsToImmigrants1st = 0;
			int totalNewHholdsFromEvolution = 0;
			int totalDwellingsAssignedToEvoHholds = 0;
			int nExtraEmigrants = 0;
			int nExtraEmigrantHholds = 0;
			int totalImmigrants2nd = 0;
			int totalHholdImmigrants2nd = 0;
			int totalDwellingsToImmigrants2nd = 0;
			int totalVacDwellingsByEvo = 0;
			
			//evolvePopulation();
			HashMap<String,Integer> vacatedDwellingsFromEvo = evolvePopulationWithVacatedDwellings();
			// gets new households created from evolution processes, i.e. those with 'Unknown' as zoneName
			ArrayList<Integer> newEvoHholdIDs = new ArrayList<Integer>();
			for (Household hhold : Population.getHhPool().values()) {
				if (hhold.getZoneName().equals(HardcodedData.unknown)) {
					newEvoHholdIDs.add(hhold.getId());
				}
			}
			totalNewHholdsFromEvolution = newEvoHholdIDs.size();
			//System.out.println("Finishing evolution year " + iYear + ". Newly created households " + totalNewHholdsFromEvolution);
			
			
			HashMap<String,Integer> availDwellingsByZone = newDwellingsByZoneByEvoYear.get(iYear);
			// calculates the total number of dwellings remaining available after all above migration/allocation steps
			int totalDwellingAvailableNow = 0;
			for (Integer value : availDwellingsByZone.values()) {
				totalDwellingAvailableNow += value;
			}
			System.out.println("totalDwellingAvailable newly constructed " + totalDwellingAvailableNow);
			
			// initialises HashMap for tracking dwellings assignment during evolution
			HashMap<String,int[]> dwellingsTrackingByZone = new HashMap<String,int[]>();
			for (String zone : availDwellingsByZone.keySet()) {
				int[] dwellingInfoThisZone = new int[DwellingInfoColumns.values().length];
				dwellingInfoThisZone[DwellingInfoColumns.newlyConstructed.getIndex()] = availDwellingsByZone.get(zone);
				dwellingsTrackingByZone.put(zone,dwellingInfoThisZone);
				// calculates the total number of new dwellings available in this year
				totalNewlyConstructedDwellings += availDwellingsByZone.get(zone);
			}
			
			// updates availDwellingsByZone for vacated dwellings that are available due to deaths
			// NOTE, we do not update dwellingsTrackingByZone with these dwellings yet.
			for (String zone : availDwellingsByZone.keySet()) {
				if (vacatedDwellingsFromEvo.containsKey(zone)) {
					int nVacatedDwellingsByDeaths = vacatedDwellingsFromEvo.get(zone);
					int crnAvailDwellingsThisZone = availDwellingsByZone.get(zone);
					availDwellingsByZone.put(zone, nVacatedDwellingsByDeaths+crnAvailDwellingsThisZone);
					totalVacDwellingsByEvo += nVacatedDwellingsByDeaths;
				}
			}
			// calculates the total number of dwellings remaining available after all above migration/allocation steps
			totalDwellingAvailableNow = 0;
			for (Integer value : availDwellingsByZone.values()) {
				totalDwellingAvailableNow += value;
			}
			System.out.println("totalDwellingAvailable adding vacatedByDeaths " + totalDwellingAvailableNow);
			
			// emigrate households out of the region following a predefined number of individuals by age
			HashMap<String,Integer> vacatedDwellings = Migrations.emigrateHholds(totalIndivEmigrants, Rates.getEmigrantAgeDistrib(), newEvoHholdIDs, iYear);
			// updates dwellingsTrackingByZone, availDwellingsByZone
			for (String zone : dwellingsTrackingByZone.keySet()) {
				int[] dwellingInfoThisZone = dwellingsTrackingByZone.get(zone);
				if (vacatedDwellings.containsKey(zone)) {
					int nVacatedDwellings = vacatedDwellings.get(zone);
					int crnAvailDwellingsThisZone = availDwellingsByZone.get(zone);
					dwellingInfoThisZone[DwellingInfoColumns.vacatedByEmigrants.getIndex()] = nVacatedDwellings;
					dwellingInfoThisZone[DwellingInfoColumns.available.getIndex()] = nVacatedDwellings+crnAvailDwellingsThisZone;
					availDwellingsByZone.put(zone, nVacatedDwellings+crnAvailDwellingsThisZone);
					totalVacatedDwellings += vacatedDwellings.get(zone);
				}
				dwellingsTrackingByZone.put(zone, dwellingInfoThisZone);
			}
			totalActualEmigrants = actualIndivEmigrants;
			totalHholdEmigrants = actualHholdEmigrants;
			System.out.println("\nnHholds after emigration " + Population.getHhPool().values().size());
			
			// calculates the total number of dwellings remaining available after all above migration/allocation steps
			totalDwellingAvailableNow = 0;
			for (Integer value : availDwellingsByZone.values()) {
				totalDwellingAvailableNow += value;
			}
			System.out.println("totalDwellingAvailable adding vacatedByEmigrants " + totalDwellingAvailableNow);
			
			
			// immigrates households into the region following a predefined number of individuals by age
			HashMap<String,Integer> allocatedDwellings = Migrations.immigrateHholds(totalIndivImmigrants, Rates.getImmigrantAgeDistrib(), availDwellingsByZone, iYear);
			// updates dwellingsTrackingByZone, availDwellingsByZone
			for (String zone : dwellingsTrackingByZone.keySet()) {
				int[] dwellingInfoThisZone = dwellingsTrackingByZone.get(zone);
				dwellingInfoThisZone[DwellingInfoColumns.available.getIndex()] = availDwellingsByZone.get(zone);
				if (allocatedDwellings.containsKey(zone)) {
					dwellingInfoThisZone[DwellingInfoColumns.to1stImmigrants.getIndex()] = allocatedDwellings.get(zone);
					totalDwellingsToImmigrants1st += allocatedDwellings.get(zone);
				}
				dwellingsTrackingByZone.put(zone, dwellingInfoThisZone);
			}
			totalActualImmigrants1st = actualIndivImmigrants1st;
			totalHholdImmigrants1st = actualHholdImmigrants1st;
			System.out.println("\nnHholds after 1st immigration " + Population.getHhPool().values().size());
			
			// calculates the total number of dwellings remaining available after all above migration/allocation steps
			totalDwellingAvailableNow = 0;
			for (Integer value : availDwellingsByZone.values()) {
				totalDwellingAvailableNow += value;
			}
			System.out.println("totalDwellingAvailable after 1stImmigrants " + totalDwellingAvailableNow);
			
			// allocates remaining households that were created from evolution steps to dwellings 
			//System.out.println("Newly created households after migration " + newEvoHholdIDs.size());
			//System.out.println("totalDwellingAvailableNow " + totalDwellingAvailableNow);
			Iterator<Integer> it = newEvoHholdIDs.iterator();
			while (it.hasNext()) {
				Integer hhID = it.next();
				for (String zone : availDwellingsByZone.keySet()) {
					int nDwellingsAvailThisZone = availDwellingsByZone.get(zone);
					if (nDwellingsAvailThisZone>0) {
						hhPool.get(hhID).setZoneName(zone);
						availDwellingsByZone.put(zone, nDwellingsAvailThisZone-1);
						it.remove();
						totalDwellingsAssignedToEvoHholds += 1;
						break;
					}
				}
			}
			//System.out.println("Number of dwellings assigned to evolved households " + totalDwellingsAssignedToEvoHholds);
			//System.out.println("Created households remain homeless " + newEvoHholdIDs.size());
			
			// calculates the total number of dwellings remaining available after all above migration/allocation steps
			totalDwellingAvailableNow = 0;
			for (Integer value : availDwellingsByZone.values()) {
				totalDwellingAvailableNow += value;
			}
			System.out.println("\ntotalDwellingAvailable after allocated to newHholdFromEvo " + totalDwellingAvailableNow);
			
			//System.out.println("totalDwellingAvailableNow (which equals number of extra immigrant households) " + totalDwellingAvailableNow);
			// this total number of dwellings remaining available defines the number of extra immigrant households that will be brought into the region.
			// However, if there are still households from the evolution steps remain homeless, it means there's no more dwellings available and thus no more immigrants needed.
			// Also, these homeless households will be removed from the current population (allocated to outside of the region).
			nExtraEmigrantHholds = newEvoHholdIDs.size();
			if (newEvoHholdIDs.size()>0) {
				// removes these households from the population
				it = newEvoHholdIDs.iterator();
				while (it.hasNext()) {
					Integer hhID = it.next();
					nExtraEmigrants += hhPool.get(hhID).getResidentsID().size();
					Population.removeHhold(hhID);
				}
				//System.out.println("NOTE: extraIndivEmigrants " + nExtraEmigrants + ", extraHholdEmigrants " + nExtraEmigrantHholds);
			} else {
				ArrayList<Integer> immiHholdsID = Migrations.immigrateHholdBased(totalDwellingAvailableNow);
				totalHholdImmigrants2nd = immiHholdsID.size();
				// assigns a dwelling to these households
				for (Integer hhID : immiHholdsID) {
					for (String zone : availDwellingsByZone.keySet()) {
						int nDwellingsAvailThisZone = availDwellingsByZone.get(zone);
						if (nDwellingsAvailThisZone>0) {
							totalImmigrants2nd += hhPool.get(hhID).getResidentsID().size();
							totalDwellingsToImmigrants2nd += 1;
							hhPool.get(hhID).setZoneName(zone);
							availDwellingsByZone.put(zone, nDwellingsAvailThisZone-1);
							break;
						}
					}
				}
				//System.out.println("Number of extra households immigrated " + totalHholdImmigrants2nd + ", allocated to " + totalDwellingsToImmigrants2nd + " dwellings");
			}
			System.out.println("\nnHholds after 2nd immigration " + Population.getHhPool().values().size());
			
			// calculates the total number of dwellings remaining available after all above migration/allocation steps
			totalDwellingAvailableNow = 0;
			for (Integer value : availDwellingsByZone.values()) {
				totalDwellingAvailableNow += value;
			}
			System.out.println("\ntotalDwellingAvailable after 2ndImmgrants " + totalDwellingAvailableNow);
			//System.out.println("totalDwellingAvailableNow (after everything and which should be 0) " + totalDwellingAvailableNow);
			
			Dwellings.evaluateDwellingNeeds();
			
			migrationSummary[iYear] = new int[] {iYear+startYear, Population.getIndivPool().size(), Population.getHhPool().size(),
					totalNewHholdsFromEvolution,totalVacDwellingsByEvo,totalNewlyConstructedDwellings,
					totalActualEmigrants, totalHholdEmigrants, totalVacatedDwellings,
					totalActualImmigrants1st, totalHholdImmigrants1st, totalDwellingsToImmigrants1st,
					totalDwellingsAssignedToEvoHholds, nExtraEmigrantHholds, nExtraEmigrants,
					totalHholdImmigrants2nd, totalImmigrants2nd, totalDwellingsToImmigrants2nd};
			
			popSummary[iYear] = new int[] {iYear+startYear, Population.getIndivPool().size(),
					getMaleCountsBetweenAges(0,4),getMaleCountsBetweenAges(5,9),
					getMaleCountsBetweenAges(10,14),getMaleCountsBetweenAges(15,19),
					getMaleCountsBetweenAges(20,24),getMaleCountsBetweenAges(25,29),
					getMaleCountsBetweenAges(30,34),getMaleCountsBetweenAges(35,39),
					getMaleCountsBetweenAges(40,44),getMaleCountsBetweenAges(45,49),
					getMaleCountsBetweenAges(50,54),getMaleCountsBetweenAges(55,59),
					getMaleCountsBetweenAges(60,64),getMaleCountsBetweenAges(65,69),
					getMaleCountsBetweenAges(70,74),getMaleCountsBetweenAges(75,79),
					getMaleCountsBetweenAges(80,84),getMaleCountsBetweenAges(85,199),
					getFemaleCountsBetweenAges(0,4),getFemaleCountsBetweenAges(5,9),
					getFemaleCountsBetweenAges(10,14),getFemaleCountsBetweenAges(15,19),
					getFemaleCountsBetweenAges(20,24),getFemaleCountsBetweenAges(25,29),
					getFemaleCountsBetweenAges(30,34),getFemaleCountsBetweenAges(35,39),
					getFemaleCountsBetweenAges(40,44),getFemaleCountsBetweenAges(45,49),
					getFemaleCountsBetweenAges(50,54),getFemaleCountsBetweenAges(55,59),
					getFemaleCountsBetweenAges(60,64),getFemaleCountsBetweenAges(65,69),
					getFemaleCountsBetweenAges(70,74),getFemaleCountsBetweenAges(75,79),
					getFemaleCountsBetweenAges(80,84),getFemaleCountsBetweenAges(85,199)};
			
			hholdSummary[iYear] = new int[] {iYear+startYear, Population.getHhPool().size(),
					PopulationAnalytics.getHholdsOfType(AggreHholdTypes.couple),
					PopulationAnalytics.getHholdsOfType(AggreHholdTypes.coupleU15),
					PopulationAnalytics.getHholdsOfType(AggreHholdTypes.coupleO15),
					PopulationAnalytics.getHholdsOfType(AggreHholdTypes.coupleU15O15),
					PopulationAnalytics.getHholdsOfType(AggreHholdTypes.loneParentU15),
					PopulationAnalytics.getHholdsOfType(AggreHholdTypes.loneParentO15),
					PopulationAnalytics.getHholdsOfType(AggreHholdTypes.loneParentU15O15),
					PopulationAnalytics.getHholdsOfType(AggreHholdTypes.Other)};
			
			PopulationIOHandler.outputPopulation(HardcodedData.outputTablesPath + "year" + Integer.toString(iYear) + "_SP.csv");
			System.out.println("writing year " + iYear + "SP.csv finished.");
			
			/*
			ArrayList<String[]> outStr = new ArrayList<String[]>();
			for (String zone : dwellingsTrackingByZone.keySet()) {
				outStr.add(new String[] {zone, 
										Integer.toString(dwellingsTrackingByZone.get(zone)[DwellingInfoColumns.newlyConstructed.getIndex()]),
										Integer.toString(dwellingsTrackingByZone.get(zone)[DwellingInfoColumns.vacatedByEmigrants.getIndex()]),
										Integer.toString(dwellingsTrackingByZone.get(zone)[DwellingInfoColumns.to1stImmigrants.getIndex()]),
										Integer.toString(dwellingsTrackingByZone.get(zone)[DwellingInfoColumns.toEvolvedHholds.getIndex()]),
										Integer.toString(dwellingsTrackingByZone.get(zone)[DwellingInfoColumns.to2ndImmigrants.getIndex()]),
										Integer.toString(dwellingsTrackingByZone.get(zone)[DwellingInfoColumns.available.getIndex()])});
			}
			TextFileHandler.writeToCSV(HardcodedData.outputTablesPath + "dwellingTracking_" + iYear + ".csv", 
					new String[] {"zone","newlyConstructed","vacatedByEmigrants","to1stImmigrants","toEvolvedHholds","to2ndImmigrants","available"}, 
					outStr);
			*/
		}
		
		// writes migrationSummary to file
		ArrayList<String[]> outStr = new ArrayList<String[]>();
		for (int i=0; i<=migrationSummary.length-1; i++) {
			String[] newRow = new String[migrationSummary[i].length];
			for (int j=0; j<=newRow.length-1; j++) {
				newRow[j] = Integer.toString(migrationSummary[i][j]);
			}
			outStr.add(newRow);
			/*
			outStr.add(new String[] {Integer.toString(migrationSummary[i][0]), Integer.toString(migrationSummary[i][1]), Integer.toString(migrationSummary[i][2]), Integer.toString(migrationSummary[i][3]), 
									Integer.toString(migrationSummary[i][4]), Integer.toString(migrationSummary[i][5]), Integer.toString(migrationSummary[i][6]), Integer.toString(migrationSummary[i][7]),
									Integer.toString(migrationSummary[i][8]), Integer.toString(migrationSummary[i][9]), Integer.toString(migrationSummary[i][10]), Integer.toString(migrationSummary[i][11]),
									Integer.toString(migrationSummary[i][12]), Integer.toString(migrationSummary[i][13], Integer.toString(migrationSummary[i][14], Integer.toString(migrationSummary[i][15],
									Integer.toString(migrationSummary[i][16])});
			*/
		}
		TextFileHandler.writeToCSV(HardcodedData.outputTablesPath + "migrationSummary.csv", headerMigrationSummary, outStr);
		
		// write popSummary to file
		outStr = new ArrayList<String[]>();
		for (int i=0; i<=popSummary.length-1; i++) {
			String[] newRow = new String[popSummary[i].length];
			for (int j=0; j<=newRow.length-1; j++) {
				newRow[j] = Integer.toString(popSummary[i][j]);
			}
			outStr.add(newRow);
		}
		TextFileHandler.writeToCSV(HardcodedData.outputTablesPath + "populationSummary.csv", headerPopulationSummary, outStr);
		
		// write hholdSummary to file
		outStr = new ArrayList<String[]>();
		for (int i=0; i<=hholdSummary.length-1; i++) {
			String[] newRow = new String[hholdSummary[i].length];
			for (int j=0; j<=newRow.length-1; j++) {
				newRow[j] = Integer.toString(hholdSummary[i][j]);
			}
			outStr.add(newRow);
		}
		TextFileHandler.writeToCSV(HardcodedData.outputTablesPath + "hholdSummary.csv", headerHholdSummary, outStr);
	}
	
	
	/**
	 * 
	 * @param nYears
	 * @param outputFile
	 * @return
	 */
	public static void startEvoWithFunctorAndSimpleHholdMigration(int nYears, String outputFile, int[] evolutionOrder) {
		PopulationIOHandler.outputPopulation(HardcodedData.outputTablesPath + "year0_SP.csv");
		
		int[][] totalPop = new int[nYears+1][4];
		totalPop[0][0] = Population.getIndivPool().size();
		totalPop[0][1] = Population.getHhPool().size();
		totalPop[0][2] = 0;
		totalPop[0][3] = 0;
		
		for (int iYear=1; iYear<=nYears; iYear++) {
			// immigrates households into the region following a predefined number for this year
			System.out.println("\timmigrating households...");
			ArrayList<Integer> immiHholdsID = Migrations.immigrateHholdBased(Rates.getImmiHholdsByYear().get((Integer)iYear).intValue());
			System.out.println("\tend immigrating households...");
			
			// gets number of individual immigrants
			int nImmigrants = 0;
			for (Integer hhID : immiHholdsID) {
				nImmigrants += Population.getHhPool().get(hhID).getResidentsID().size();
			}
			
			evolvePopulationWithFunctor(evolutionOrder);


			System.out.println("evolving SP year " + iYear + " finished.\n");
						
			//totalPop[iYear] = new int[] {Population.getIndivPool().size(), Population.getImmigrantCount()};
			totalPop[iYear] = new int[] {Population.getIndivPool().size(), Population.getHhPool().size(), nImmigrants, immiHholdsID.size()};
			
			PopulationIOHandler.outputPopulation(HardcodedData.outputTablesPath + "year" + Integer.toString(iYear) + "_SP.csv");
			//System.out.println("writing year " + iYear + "SP.csv finished.");
		}
		
		// writes totalPop to file
		ArrayList<String[]> outStr = new ArrayList<String[]>();
		for (int i=0; i<=totalPop.length-1; i++) {
			outStr.add(new String[] {Integer.toString(i), 
									Integer.toString(totalPop[i][0]), Integer.toString(totalPop[i][1]), 
									Integer.toString(totalPop[i][2]), Integer.toString(totalPop[i][3])});
		}
		TextFileHandler.writeToCSV(outputFile, new String[] {"yearIndex","population","nHholds","nImmigrants","nHhImmigrants"}, outStr);
	}
	
	
	/**
	 * 
	 * @param youngerAge
	 * @param olderAge
	 * @return
	 */
	private static int getMaleCountsBetweenAges(int youngerAge, int olderAge) {
		int count = 0;
		for (Individual indiv : indivPool.values()) {
			if (indiv.getGender().equals(HardcodedData.Genders._male) && indiv.getAge()>=youngerAge && indiv.getAge()<=olderAge) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 
	 * @param youngerAge
	 * @param olderAge
	 * @return
	 */
	private static int getIndivCountsBetweenAges(int youngerAge, int olderAge) {
		int count = 0;
		for (Individual indiv : indivPool.values()) {
			if (indiv.getAge()>=youngerAge && indiv.getAge()<=olderAge) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 
	 */
	private static void randomlyAssignZoneToHholds() {
		for (Household hhold : Population.getHhPool().values()) {
			if (hhold.getZoneName().equals(HardcodedData.unknown)) {
				String randomZone = HardcodedData.getZoneNames().get(HardcodedData.random.nextInt(HardcodedData.getZoneNames().size()));
				hhold.setZoneName(randomZone);
			}
		}
	}
	
	
	/**
	 * assigns a zone to new households (created during evolution) based on the projections of new dwellings in each zone that were created using the urban forecast model.
	 * Such dwelling projections were provided by City Futures @ UNSW as part of the DAC project (2016)
	 * @param iYear index of year in the evolution and also in the dwelling forecasts. 0 represents the year of the initial population. 1 is 1st year of the evolution and so on.
	 */
	private static int[] assignZonesToHholdsBasedOnUFM(int iYear, ArrayList<Integer> newHholdIDs) {
		HashMap<String,Integer> newDwellingsByZone = newDwellingsByZoneByEvoYear.get(iYear);
		ArrayList<Integer> toRemoveHholdID = new ArrayList<Integer>();
		
		int totalNewDwellingsThisYear = 0;
		for (String zoneName : newDwellingsByZone.keySet()) {
			if (newDwellingsByZone.get(zoneName)>0)
				totalNewDwellingsThisYear = totalNewDwellingsThisYear + newDwellingsByZone.get(zoneName);
		}
		System.out.println("Year " + iYear + ", new dwellings before allocting hholds " + totalNewDwellingsThisYear);
		
		int nHholdsAllocated = 0;
		int nIndivsAllocated = 0;
		
		// randomly shuffle newHholdIDs
		Collections.shuffle(newHholdIDs);
		
		for (Integer hhID : newHholdIDs) {
			Household hhold = Population.getHhPool().get(hhID);
			
			// identify a zone that has at least 1 new dwelling
			String newZone = null;
			for (String zoneName : newDwellingsByZone.keySet()) {
				if (newDwellingsByZone.get(zoneName)>0) {
					newZone = zoneName;
					break;
				}
			}
			 
			if (newZone!=null) { // if such a zone is found
				// sets this household to this zone
				hhold.setZoneName(newZone);
				nHholdsAllocated += 1;
				nIndivsAllocated += hhold.getResidentsID().size();
				// updates the available new dwellings in this zone
				int newDwellingsThisZone = newDwellingsByZone.get(newZone);
				newDwellingsThisZone = newDwellingsThisZone - 1;
				newDwellingsByZone.put(newZone, newDwellingsThisZone);
			} else {
				// no new dwellings available in the study for this year. This household needs to relocate to outside of study area and thus is removed from the population.
				toRemoveHholdID.add(hhold.getId());
			}
		}

		System.out.println("Year " + iYear + ", hholds allocated " + nHholdsAllocated);
		
		totalNewDwellingsThisYear = 0;
		for (String zoneName : newDwellingsByZone.keySet()) {
			if (newDwellingsByZone.get(zoneName)>0)
				totalNewDwellingsThisYear = totalNewDwellingsThisYear + newDwellingsByZone.get(zoneName);
		}
		System.out.println("Year " + iYear + ", new dwellings after allocting hholds " + totalNewDwellingsThisYear);
		
		int countRemovedHholds = toRemoveHholdID.size();
		int countRemovedIndivs = 0;
		
		// removes households in toRemoveHholdID from hhPool (and their residents from indivPool)
		for (Integer hholdID : toRemoveHholdID) {
			// removes residents;
			for (Integer indivID : Population.getHhPool().get(hholdID).getResidentsID()) {
				countRemovedIndivs = countRemovedIndivs+1;
				Population.getIndivPool().remove(indivID);
			}
			Population.getHhPool().remove(hholdID);
		}
		
		return new int[] {countRemovedHholds,countRemovedIndivs, nHholdsAllocated, nIndivsAllocated};
		//System.out.println("Year " + iYear + ", hholds allocated to outside " + toRemoveHholdID.size() + "\n");
	}
	
	/**
	 * 
	 * @param nYears
	 * @param outputFile
	 * @return
	 */
	public static void startEvolutionWithEmployment(int nYears, String outputFile) {
		PopulationIOHandler.outputPopulation(HardcodedData.outputTablesPath + "year0_SP.csv");
		int startYear = 0;
		
		String[] headerPopulationSummary = new String[] {"year","population", "hholds", "indivInHholds", 
														"fMarried15_19", "fMarried20_24", "fMarried25_29", "fMarried30_34", "fMarried35_39", "fMarried40_44", "fMarried45_49", "newborns",
														"m_0_4","m_5_9","m_10_14","m_15_19","m_20_24","m_25_29","m_30_34","m_35_39","m_40_44","m_45_49", 
														"m_50_54","m_55_59","m_60_64","m_65_69","m_70_74","m_75_79","m_80_84","m_85_above",
														"f_0_4","f_5_9","f_10_14","f_15_19","f_20_24","f_25_29","f_30_34","f_35_39","f_40_44","f_45_49",
														"f_50_54","f_55_59","f_60_64","f_65_69","f_70_74","f_75_79","f_80_84","f_85_above"};
		int[][] popSummary = new int[nYears+1][headerPopulationSummary.length];
		
		int indivInHholds = 0;
		for (Household hhold : Population.getHhPool().values()) {
			indivInHholds = indivInHholds + hhold.getResidentsID().size();
		}
		
		popSummary[0] = new int[] {startYear, Population.getIndivPool().size(), Population.getHhPool().size(), indivInHholds,
				getFemaleCountsBetweenAges(15, 19, HholdRelSP.Married), 
				getFemaleCountsBetweenAges(20, 24, HholdRelSP.Married), getFemaleCountsBetweenAges(25, 29, HholdRelSP.Married), 
				getFemaleCountsBetweenAges(30, 34, HholdRelSP.Married), getFemaleCountsBetweenAges(35, 39, HholdRelSP.Married), 
				getFemaleCountsBetweenAges(40, 44, HholdRelSP.Married), getFemaleCountsBetweenAges(45, 49, HholdRelSP.Married),
				getIndivCountsBetweenAges(0,0),
				getMaleCountsBetweenAges(0,4),getMaleCountsBetweenAges(5,9),
				getMaleCountsBetweenAges(10,14),getMaleCountsBetweenAges(15,19),
				getMaleCountsBetweenAges(20,24),getMaleCountsBetweenAges(25,29),
				getMaleCountsBetweenAges(30,34),getMaleCountsBetweenAges(35,39),
				getMaleCountsBetweenAges(40,44),getMaleCountsBetweenAges(45,49),
				getMaleCountsBetweenAges(50,54),getMaleCountsBetweenAges(55,59),
				getMaleCountsBetweenAges(60,64),getMaleCountsBetweenAges(65,69),
				getMaleCountsBetweenAges(70,74),getMaleCountsBetweenAges(75,79),
				getMaleCountsBetweenAges(80,84),getMaleCountsBetweenAges(85,199),
				getFemaleCountsBetweenAges(0,4),getFemaleCountsBetweenAges(5,9),
				getFemaleCountsBetweenAges(10,14),getFemaleCountsBetweenAges(15,19),
				getFemaleCountsBetweenAges(20,24),getFemaleCountsBetweenAges(25,29),
				getFemaleCountsBetweenAges(30,34),getFemaleCountsBetweenAges(35,39),
				getFemaleCountsBetweenAges(40,44),getFemaleCountsBetweenAges(45,49),
				getFemaleCountsBetweenAges(50,54),getFemaleCountsBetweenAges(55,59),
				getFemaleCountsBetweenAges(60,64),getFemaleCountsBetweenAges(65,69),
				getFemaleCountsBetweenAges(70,74),getFemaleCountsBetweenAges(75,79),
				getFemaleCountsBetweenAges(80,84),getFemaleCountsBetweenAges(85,199)};
		
		
		String[] headerHholdSummary = new String[] {"year", "nHholds", "couple", "coupleU15", "coupleO15", "coupleU15O15", 
				"loneParentU15", "loneParentO15", "loneParentU15O15", "Other"};
		int[][] hholdSummary = new int[nYears+1][headerHholdSummary.length];
		hholdSummary[0] = new int[] {startYear, Population.getHhPool().size(),
				PopulationAnalytics.getHholdsOfType(AggreHholdTypes.couple),
				PopulationAnalytics.getHholdsOfType(AggreHholdTypes.coupleU15),
				PopulationAnalytics.getHholdsOfType(AggreHholdTypes.coupleO15),
				PopulationAnalytics.getHholdsOfType(AggreHholdTypes.coupleU15O15),
				PopulationAnalytics.getHholdsOfType(AggreHholdTypes.loneParentU15),
				PopulationAnalytics.getHholdsOfType(AggreHholdTypes.loneParentO15),
				PopulationAnalytics.getHholdsOfType(AggreHholdTypes.loneParentU15O15),
				PopulationAnalytics.getHholdsOfType(AggreHholdTypes.Other)};
		
		String[] headerEmplStatSummary = new String[] {"U15","O15","nonLabour","unEmployed","employed","employedIn","employedOut","LOWI"};
		int[][] emplStatSummary = new int[nYears+1][headerEmplStatSummary.length];
		emplStatSummary[0] = outputEmplStat();
		
		for (int iYear=1; iYear<=nYears; iYear++) {
			int totalPopLastYear = Population.getIndivPool().size();
			int totalIndivEmigrants = (int)Math.round(pcEmigrantsInPopulation*(double)totalPopLastYear);
			int totalIndivImmigrants = (int)Math.round(pcImmigrantsInPopulation*(double)totalPopLastYear);
			
			Population.setDeadEmpCount(0);
			Population.setDeadUnEmpCount(0);
			Population.setDeadNonLabCount(0);
			Population.setDeadLIWICount(0);
			Population.setDeadLIWOCount(0);
			Population.setOutEmployedImmigrantCount(0);
			Population.setUnEmployedImmigrantCount(0);
			Population.setImmigrantCount(0);
			
			System.out.println("Checkpoint 1 - before natural evolution");
			outputEmplStat();
			
			
			int[] deadEmpStat = evolvePopulationWithJobTracking();
			System.out.println("evolving SP year " + iYear + " finished.");
			System.out.println(deadEmpStat[0] + " dead indivs working in, " + deadEmpStat[1] + " dead indivs working out.");
			
			System.out.println("Checkpoint 2 - after natural evolution");
			outputEmplStat();
			
			// gets new households created from evolution processes, i.e. those with 'Unknown' as zoneName
			ArrayList<Integer> newEvoHholdIDs = new ArrayList<Integer>();
			for (Household hhold : Population.getHhPool().values()) {
				if (hhold.getZoneName().equals(HardcodedData.unknown)) {
					newEvoHholdIDs.add(hhold.getId());
				}
			}
			
			System.out.println("Checkpoint 3 - before updateEmploymentStatusExistingPop");
			outputEmplStat();
			
			// updates employment status for existing population
			// kids turn 15 become unemployed
			// unemployed people turn 65 (retiredAge) become non-labour
			Employments.updateEmploymentStatusExistingPop(deadEmpStat);
			
			System.out.println("Checkpoint 4 - after updateEmploymentStatusExistingPop");
			outputEmplStat();
			
			/*
			 * IMMIGRATION v1, ADDING RANDOM HOUSEHOLDS INTO CURRENT POPULATION
			 */
			// immigrating households into the region following a predefined number for this year
			//ArrayList<Integer> immiHholdsID = Migrations.immigrateHholdBased(Rates.getImmiHholdsByYear().get((Integer)iYear).intValue());
			//popDebugRecords[iYear][PopDebugHeader._1stImmiInd.getColIndex()] = Population.getImmigrantCount();
			/*
			 * END IMMIGRATION V1
			 */
			
			/*
			 * IMMIGRATION V2, SEPARATE EMIGRATION AND IMMIGRATION FOLLOWING AGE PROFILE OF INDIVIDUAL IMMIGRANTS
			 */
			// emigrate households out of the region following a predefined number of individuals by age
			int[] emiEmplStat = Migrations.emigrateHholdsTrackingEmpl(totalIndivEmigrants, Rates.getEmigrantAgeDistrib(), newEvoHholdIDs, iYear);
			System.out.println("Checkpoint 5 - after emigrateHholdsTrackingEmpl");
			outputEmplStat();
			
			// immigrates households into the region following a predefined number of individuals by age
			ArrayList<Integer> immiHholdsID = Migrations.immigrateHholds(totalIndivImmigrants, Rates.getImmigrantAgeDistrib(), iYear);
			System.out.println("Checkpoint 6 - after immigrateHholds");
			outputEmplStat();
			/*
			 * END IMMIGRATION V2
			 */
			
			// labels new immigrants that are O15 as 'unemployed' or employed outside
			Employments.labelEmploymentStatusToImmigrants(immiHholdsID, Population.getPcNonLabourImmigrants(), iYear, emiEmplStat);
			
			System.out.println("Checkpoint 7 - after labelEmploymentStatusToImmigrants");
			outputEmplStat();
			
			
			// updates employment status based on the number of new/lost jobs this year.
			// gets the number of job change this year
			
			double deltaJob = Rates.getJobChangeByYear().get((Integer)iYear);
			Employments.updateEmploymentStatusBasedOnJobChange(deltaJob,iYear);
			
			System.out.println("Checkpoint 8 - after labelEmploymentStatusToImmigrants");
			emplStatSummary[iYear] = outputEmplStat();
			
			indivInHholds = 0;
			for (Household hhold : Population.getHhPool().values()) {
				indivInHholds = indivInHholds + hhold.getResidentsID().size();
			}
			
			popSummary[iYear] = new int[] {iYear+startYear, Population.getIndivPool().size(), Population.getHhPool().size(), indivInHholds,
					getFemaleCountsBetweenAges(15, 19, HholdRelSP.Married), 
					getFemaleCountsBetweenAges(20, 24, HholdRelSP.Married), getFemaleCountsBetweenAges(25, 29, HholdRelSP.Married), 
					getFemaleCountsBetweenAges(30, 34, HholdRelSP.Married), getFemaleCountsBetweenAges(35, 39, HholdRelSP.Married), 
					getFemaleCountsBetweenAges(40, 44, HholdRelSP.Married), getFemaleCountsBetweenAges(45, 49, HholdRelSP.Married),
					getIndivCountsBetweenAges(0,0),
					getMaleCountsBetweenAges(0,4),getMaleCountsBetweenAges(5,9),
					getMaleCountsBetweenAges(10,14),getMaleCountsBetweenAges(15,19),
					getMaleCountsBetweenAges(20,24),getMaleCountsBetweenAges(25,29),
					getMaleCountsBetweenAges(30,34),getMaleCountsBetweenAges(35,39),
					getMaleCountsBetweenAges(40,44),getMaleCountsBetweenAges(45,49),
					getMaleCountsBetweenAges(50,54),getMaleCountsBetweenAges(55,59),
					getMaleCountsBetweenAges(60,64),getMaleCountsBetweenAges(65,69),
					getMaleCountsBetweenAges(70,74),getMaleCountsBetweenAges(75,79),
					getMaleCountsBetweenAges(80,84),getMaleCountsBetweenAges(85,199),
					getFemaleCountsBetweenAges(0,4),getFemaleCountsBetweenAges(5,9),
					getFemaleCountsBetweenAges(10,14),getFemaleCountsBetweenAges(15,19),
					getFemaleCountsBetweenAges(20,24),getFemaleCountsBetweenAges(25,29),
					getFemaleCountsBetweenAges(30,34),getFemaleCountsBetweenAges(35,39),
					getFemaleCountsBetweenAges(40,44),getFemaleCountsBetweenAges(45,49),
					getFemaleCountsBetweenAges(50,54),getFemaleCountsBetweenAges(55,59),
					getFemaleCountsBetweenAges(60,64),getFemaleCountsBetweenAges(65,69),
					getFemaleCountsBetweenAges(70,74),getFemaleCountsBetweenAges(75,79),
					getFemaleCountsBetweenAges(80,84),getFemaleCountsBetweenAges(85,199)};
			
			hholdSummary[iYear] = new int[] {iYear+startYear, Population.getHhPool().size(),
					PopulationAnalytics.getHholdsOfType(AggreHholdTypes.couple),
					PopulationAnalytics.getHholdsOfType(AggreHholdTypes.coupleU15),
					PopulationAnalytics.getHholdsOfType(AggreHholdTypes.coupleO15),
					PopulationAnalytics.getHholdsOfType(AggreHholdTypes.coupleU15O15),
					PopulationAnalytics.getHholdsOfType(AggreHholdTypes.loneParentU15),
					PopulationAnalytics.getHholdsOfType(AggreHholdTypes.loneParentO15),
					PopulationAnalytics.getHholdsOfType(AggreHholdTypes.loneParentU15O15),
					PopulationAnalytics.getHholdsOfType(AggreHholdTypes.Other)};
			
			PopulationIOHandler.outputPopulation(HardcodedData.outputTablesPath + "year" + Integer.toString(iYear) + "_SP.csv");
			System.out.println("writing year " + iYear + "SP.csv finished.\n"); 
		}
		
		// write popSummary to file
		ArrayList<String[]> outStr = new ArrayList<String[]>();
		for (int i=0; i<=popSummary.length-1; i++) {
			String[] newRow = new String[popSummary[i].length];
			for (int j=0; j<=newRow.length-1; j++) {
				newRow[j] = Integer.toString(popSummary[i][j]);
			}
			outStr.add(newRow);
		}
		TextFileHandler.writeToCSV(HardcodedData.outputTablesPath + "populationSummary.csv", headerPopulationSummary, outStr);
		
		// write hholdSummary to file
		outStr = new ArrayList<String[]>();
		for (int i=0; i<=hholdSummary.length-1; i++) {
			String[] newRow = new String[hholdSummary[i].length];
			for (int j=0; j<=newRow.length-1; j++) {
				newRow[j] = Integer.toString(hholdSummary[i][j]);
			}
			outStr.add(newRow);
		}
		TextFileHandler.writeToCSV(HardcodedData.outputTablesPath + "hholdSummary.csv", headerHholdSummary, outStr);
		
		// write employment status summary to file
		outStr = new ArrayList<String[]>();
		for (int i=0; i<=emplStatSummary.length-1; i++) {
			String[] newRow = new String[emplStatSummary[i].length];
			for (int j=0; j<=newRow.length-1; j++) {
				newRow[j] = Integer.toString(emplStatSummary[i][j]);
			}
			outStr.add(newRow);
		}
		TextFileHandler.writeToCSV(HardcodedData.outputTablesPath + "emplStatSummary.csv", headerEmplStatSummary, outStr);
	}
	
	/**
	 * evolves population and tracks the number of jobs the deads leave behind
	 * @return
	 */
	private static int[] evolvePopulationWithJobTracking() {
		// ages
		NaturalEvolution.age();
		//System.out.println("\n\tage() finished...");

		// passes away
		int[] deadEmpStat = NaturalEvolution.passAwayWithEmplTracking();
		//System.out.println("\n\tpassAway() finished...");

		// divorces
		NaturalEvolution.divorce();
		//System.out.println("\tdivorce() finished...");

		// marries
		NaturalEvolution.marry();
		//System.out.println("\tmarry() finished...");

		// gives birth
		NaturalEvolution.giveBirth();
		//System.out.println("\tgiveBirth() finished...");

		// re-evaluate the household type according to 17 categories
		for (Household hhold : getHhPool().values()) {
			hhold.compute17HholdTypesy();
		}
		
		return deadEmpStat;
	}
	
	/**
	 * 
	 */
	public static int[] outputEmplStat() {
		int[] emplStatSummary = new int[] {PopulationAnalytics.getIDIndividualInAgeGroup(0,14).size(),
											PopulationAnalytics.getIDIndividualAboveAge(15).size(),
											PopulationAnalytics.countIndivs(EmploymentStatus.nonLabour),
											PopulationAnalytics.countIndivs(EmploymentStatus.unEmployed),
											PopulationAnalytics.countIndivs(EmploymentStatus.employed),
											PopulationAnalytics.countIndivs(EmploymentStatus.employed, PlaceOfWork.in),
											PopulationAnalytics.countIndivs(EmploymentStatus.employed, PlaceOfWork.out),
											Population.getCrnLiveOutWorkIn()};
		System.out.println("\tU15 " + emplStatSummary[0] + ", " + "O15 " + emplStatSummary[1] + ", " + 
							"nonLabour " + emplStatSummary[2] + ", " + "unemployed " + emplStatSummary[3] + ", " +
							"employed " + emplStatSummary[4] + ", " + "employedIn " + emplStatSummary[5] + ", " + "employedOut " + emplStatSummary[6] + ", " +
							"LOWI " + emplStatSummary[7]);
		return emplStatSummary;
	}

	
	/**
	 * 
	 * @param hholdID
	 */
	public static void removeHhold(Integer hholdID) {
		for (Integer resID : hhPool.get(hholdID).getResidentsID()) {
			Population.getIndivPool().remove(resID);
		}
		hhPool.remove(hholdID);
	}
	

	/**
	 * 
	 * @param nYears
	 * @param outputFile
	 * @return
	 */
	public static int[] startEvolution(int nYears, String outputFile) {
		PopulationIOHandler.outputPopulation(HardcodedData.outputTablesPath + "year0_SP.csv");
		
		int[] totalPop = new int[nYears+1];
		totalPop[0] = Population.getIndivPool().size();
		
		for (int iYear=1; iYear<=nYears; iYear++) {
			Population.evolvePopulation();
			System.out.println("evolving SP year " + iYear + " finished.");
			
			totalPop[iYear] = Population.getIndivPool().size();
			
			PopulationIOHandler.outputPopulation(HardcodedData.outputTablesPath + "year" + Integer.toString(iYear) + "_SP.csv");
			System.out.println("writing year" + iYear + "SP.csv finished.");
		}
		
		// writes totalPop to file
		ArrayList<String[]> outStr = new ArrayList<String[]>();
		for (int i=0; i<=totalPop.length-1; i++) {
			outStr.add(new String[] {Integer.toString(i), Integer.toString(totalPop[i])});
		}
		TextFileHandler.writeToCSV(outputFile, new String[] {"year","population"}, outStr);
		
		return totalPop;
	}
	
	/**
	 * gets new households created from evolution processes, i.e. those with 'Unknown' as zoneName
	 * @return
	 */
	public static int countHholdsWithUnknownZonename() {
		int nNewHholds = 0;
		for (Household hhold : Population.getHhPool().values()) {
			if (hhold.getZoneName().equals(HardcodedData.unknown)) {
				nNewHholds += 1;
			}
		}
		return nNewHholds;
	}
	
	/**
	 * 
	 * @return
	 */
	public static int countHholdsWithNonResidents() {
		int nHholds0Resident = 0;
		for (Household hhold : Population.getHhPool().values()) {
			if (hhold.getResidentsID()==null || hhold.getResidentsID().size()==0) {
				nHholds0Resident += 1;
			}
		}
		return nHholds0Resident;
	}
	
	/**
	 * evolves the population
	 */
	private static HashMap<String,Integer> evolvePopulationWithVacatedDwellings() {
		// ages
		NaturalEvolution.age();
		//System.out.println("\n\tage() finished...");
		System.out.println("\tnewHholds " + countHholdsWithUnknownZonename());
		System.out.println("\thholds)Residents " + countHholdsWithNonResidents());
		System.out.println("\tnHholds " + Population.getHhPool().values().size() + "\n");
		
		// passes away
		HashMap<String,Integer> vacatedDwellingsFromDeaths = NaturalEvolution.passAwayWithVacatedDwellings();
		//System.out.println("\n\tpassAway() finished...");
		int totalVacDwellingsByDeaths = 0;
		for (String zone : vacatedDwellingsFromDeaths.keySet()) {
			totalVacDwellingsByDeaths += vacatedDwellingsFromDeaths.get(zone);
		}
		System.out.println("\ttotalVacDwellingsByDeaths " + totalVacDwellingsByDeaths);
		System.out.println("\tnewHholds " + countHholdsWithUnknownZonename());
		System.out.println("\thholds)Residents " + countHholdsWithNonResidents());
		System.out.println("\tnHholds " + Population.getHhPool().values().size() + "\n");
		
		// divorces
		NaturalEvolution.divorce();
		//System.out.println("\tdivorce() finished...");
		System.out.println("\tnewHholds " + countHholdsWithUnknownZonename());
		System.out.println("\thholds)Residents " + countHholdsWithNonResidents());
		System.out.println("\tnHholds " + Population.getHhPool().values().size() + "\n");
		
		// marries
		HashMap<String,Integer> vacatedDwellingsFromMarriages = NaturalEvolution.marryWithVacatedDwellings();
		int nVacDwellingsByMarriages = 0;
		for (String zone : vacatedDwellingsFromMarriages.keySet()) {
			nVacDwellingsByMarriages += vacatedDwellingsFromMarriages.get(zone);
		}
		//System.out.println("\tmarry() finished...");
		System.out.println("\tnVacDwellingsByMarriages " + nVacDwellingsByMarriages);
		System.out.println("\tnewHholds " + countHholdsWithUnknownZonename());
		System.out.println("\thholds)Residents " + countHholdsWithNonResidents());
		System.out.println("\tnHholds " + Population.getHhPool().values().size() + "\n");
		
		// gives birth
		NaturalEvolution.giveBirth();
		//System.out.println("\tgiveBirth() finished...");
		System.out.println("\tnewHholds " + countHholdsWithUnknownZonename());
		System.out.println("\thholds)Residents " + countHholdsWithNonResidents());
		System.out.println("\tnHholds " + Population.getHhPool().values().size() + "\n");
		
		// re-evaluate the household type according to 17 categories
		for (Household hhold : getHhPool().values()) {
			hhold.compute17HholdTypesy();
		}
		
		System.out.println("\tnHholds at the end of evolution " + Population.getHhPool().values().size() + "\n");
		
		// collates vacated dwellings from deaths and marriages
		HashMap<String,Integer> vacatedDwellings = new HashMap<String,Integer>();
		for (String zone : vacatedDwellingsFromDeaths.keySet()) {
			vacatedDwellings.put(zone, vacatedDwellingsFromDeaths.get(zone));
		}
		for (String zone : vacatedDwellingsFromMarriages.keySet()) {
			if (vacatedDwellings.containsKey(zone)) {
				vacatedDwellings.put(zone, vacatedDwellings.get(zone) + vacatedDwellingsFromMarriages.get(zone));
			} else {
				vacatedDwellings.put(zone, vacatedDwellingsFromMarriages.get(zone));
			}
		}
		
		return vacatedDwellings;
	}
	
	/**
	 * evolves the population
	 */
	private static void evolvePopulation() {
		// ages
		NaturalEvolution.age();
		//System.out.println("\n\tage() finished...");
		
		// passes away
		NaturalEvolution.passAway();
		//System.out.println("\n\tpassAway() finished...");
		
		// divorces
		NaturalEvolution.divorce();
		//System.out.println("\tdivorce() finished...");
		
		// marries
		NaturalEvolution.marry();
		//System.out.println("\tmarry() finished...");
		
		// gives birth
		NaturalEvolution.giveBirth();
		//System.out.println("\tgiveBirth() finished...");
		
		// re-evaluate the household type according to 17 categories
		for (Household hhold : getHhPool().values()) {
			hhold.compute17HholdTypesy();
		}
	}
		
	
	private static void evolvePopulationWithFunctor(int[] executeOrder) {
		Functor[] functors = new Functor[] {
			new Functor() { 
					@Override
					public void execute() { NaturalEvolution.age(); } 
				},
			
			new Functor() { 
					@Override
					public void execute() { NaturalEvolution.passAway(); } 
				},
			
			new Functor() {
					@Override
					public void execute() { NaturalEvolution.divorce(); }
				},
			
			new Functor() {
					@Override
					public void execute() { NaturalEvolution.marry(); }
				},
			
			new Functor() {
					@Override
					public void execute() { NaturalEvolution.giveBirth(); }
				},
			
		};
		
		for (int i=0; i<=executeOrder.length-1; i++) {
			int evolveStep = executeOrder[i];
			functors[evolveStep].execute();
		}
		
		
		/*
		 * If day of birth, people dying this year need to be deleted
		 * @author mdumont
		 */
		if (CombiAge.combiDeath){
			NaturalEvolution.makeDie();
		}

		// re-evaluate the household type according to 17 categories
		for (Household hhold : getHhPool().values()) {
			hhold.compute17HholdTypesy();
		}
	}
	
	/**
	 * adds a new individual indiv to indivPool. If ID of the new individual indiv already exists in indivPool, indiv is not added (i.e the existing individual is not replaced by indiv).
	 * @param indiv
	 */
	public static void addIndivToPopulation(Individual indiv) {
		if (!indivPool.containsKey((Integer)indiv.getId())) {
			indivPool.put((Integer)indiv.getId(), indiv);
		}
	}
	
	
	/**
	 * adds a new resident to an existing household in hhPool. If ID of the new resident (newResidentID) already exists, this resident is not added to this household.
	 * @param hhID ID of the household to which new resident will be added.
	 * @param newResidentID ID of the new resident.
	 */
	public static void addIndivToHhold(int hhID, int newResidentID) {
		Household hhold = hhPool.get((Integer)hhID);
		ArrayList<Integer> crnResidents = hhold.getResidentsID();
		if (!crnResidents.contains((Integer)newResidentID)) {
			crnResidents.add((Integer)newResidentID);
			hhold.setResidentsID(crnResidents);
		}
		
	}
	
	
	/**
	 * adds a new household hhold to hhPool. If ID of the new household hhold already exists in hhPool, hhold is not added (i.e the existing household is not replaced by hhold).
	 * @param hhold
	 */
	public static void addHholdToPopulation(Household hhold) {
		if (!hhPool.containsKey(hhold.getId())) {
			hhPool.put((Integer)hhold.getId(),hhold);
		}
	}
		
	
	/**
	 * 
	 * @return an ID of a new individual, which is calculated by adding 1 to the max ID in the Population.initIndivPool.
	 */
	public static int calculateNewIndivIDInitPool() {
		int[] existingIndivIDs = ArrayHandler.toInt(initIndivPool.keySet());
		if (existingIndivIDs==null || existingIndivIDs.length==0) {
			return 0;
		} else {
			int max = Collections.max(Arrays.asList(ArrayUtils.toObject(existingIndivIDs)));
			return max+1;
			//return (existingIndivIDs[ArrayHandler.getIndexOfMax(existingIndivIDs)] + 1);
		}
		
	}
	
	
	/**
	 * 
	 * @return an ID of a new household, which is calculated by adding 1 to the max ID in Population.initHhPool.
	 */
	public static int calculateNewHholdIDInitPool() {
		int[] existingHhIDs = ArrayHandler.toInt(initHhPool.keySet());
		if (existingHhIDs==null || existingHhIDs.length==0) {
			return 0;
		} else {
			int max = Collections.max(Arrays.asList(ArrayUtils.toObject(existingHhIDs)));
			return max+1;
			//return (existingHhIDs[ArrayHandler.getIndexOfMax(existingHhIDs)] + 1);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public static Integer pickRandomHholdFromCrnPool() {
		ArrayList<Integer> crnHholdIDs = new ArrayList<Integer>(hhPool.keySet());
		Integer randHhID = crnHholdIDs.get(HardcodedData.random.nextInt(crnHholdIDs.size()));
		return randHhID;
	}
	
	/**
	 * 
	 * @return
	 */
	public static Integer pickRandomHholdFromInitPool() {
		ArrayList<Integer> initHholdIDs = new ArrayList<Integer>(initHhPool.keySet());
		Integer randHhID = initHholdIDs.get(HardcodedData.random.nextInt(initHholdIDs.size()));
		return randHhID;
	}
	
	/**
	 * 
	 * @param youngerAge
	 * @param olderAge
	 * @param hhRel
	 * @return
	 */
	private static int getFemaleCountsBetweenAges(int youngerAge, int olderAge, HholdRelSP hhRel) {
		int count = 0;
		for (Individual indiv : indivPool.values()) {
			if (indiv.getGender().equals(HardcodedData.Genders._female) && indiv.getAge()>=youngerAge && indiv.getAge()<=olderAge && indiv.getHhRel().equals(hhRel)) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 
	 * @param youngerAge
	 * @param olderAge
	 * @return
	 */
	private static int getFemaleCountsBetweenAges(int youngerAge, int olderAge) {
		int count = 0;
		for (Individual indiv : indivPool.values()) {
			if (indiv.getGender().equals(HardcodedData.Genders._female) && indiv.getAge()>=youngerAge && indiv.getAge()<=olderAge) {
				count++;
			}
		}
		return count;
	}
	
	
	public static HashMap<Integer, Household> getHhPool() {
		return hhPool;
	}
	public static void setHhPool(HashMap<Integer, Household> hhPool) {
		Population.hhPool = hhPool;
	}
	public static HashMap<Integer, Individual> getIndivPool() {
		return indivPool;
	}
	public static void setIndivPool(HashMap<Integer, Individual> indivPool) {
		Population.indivPool = indivPool;
	}

	public static HashMap<Integer,Household> getInitHhPool() {
		return initHhPool;
	}

	public static void setInitHhPool(HashMap<Integer,Household> initHhPool) {
		Population.initHhPool = initHhPool;
	}

	public static HashMap<Integer,Individual> getInitIndivPool() {
		return initIndivPool;
	}

	public static void setInitIndivPool(HashMap<Integer,Individual> initIndivPool) {
		Population.initIndivPool = initIndivPool;
	}


	public static double getJobImmiFamiRatio() {
		return jobImmiFamiRatio;
	}


	public static void setJobImmiFamiRatio(double jobImmiFamiRatio) {
		Population.jobImmiFamiRatio = jobImmiFamiRatio;
	}


	public static HashMap<String,int[]> getDwellingForecastsByZone() {
		return dwellingForecastsByZone;
	}


	public static void setDwellingForecastsByZone(HashMap<String,int[]> dwellingForecastsByZone) {
		Population.dwellingForecastsByZone = dwellingForecastsByZone;
	}
	
	public static HashMap<Integer, HashMap<String, Integer>> getNewDwellingsByZoneByEvoYear() {
		return newDwellingsByZoneByEvoYear;
	}

	public static void setNewDwellingsByZoneByEvoYear(
			HashMap<Integer, HashMap<String, Integer>> newDwellingsByZoneByEvoYear) {
		Population.newDwellingsByZoneByEvoYear = newDwellingsByZoneByEvoYear;
	}
	
	public static double getPcEmigrantsInPopulation() {
		return pcEmigrantsInPopulation;
	}

	public static void setPcEmigrantsInPopulation(double pcEmigrantsInPopulation) {
		Population.pcEmigrantsInPopulation = pcEmigrantsInPopulation;
	}


	public static double getPcImmigrantsInPopulation() {
		return pcImmigrantsInPopulation;
	}

	public static void setPcImmigrantsInPopulation(double pcImmigrantsInPopulation) {
		Population.pcImmigrantsInPopulation = pcImmigrantsInPopulation;
	}

	public static double getOutEmployedImmgrantRatio() {
		return outEmployedImmgrantRatio;
	}

	public static void setOutEmployedImmgrantRatio(double outEmployedImmgrantRatio) {
		Population.outEmployedImmgrantRatio = outEmployedImmgrantRatio;
	}

	public static double getNewJobsToLocalUnEmplRatio() {
		return newJobsToLocalUnEmplRatio;
	}

	public static void setNewJobsToLocalUnEmplRatio(double newJobsToLocalUnEmplRatio) {
		Population.newJobsToLocalUnEmplRatio = newJobsToLocalUnEmplRatio;
	}

	public static double getPcNonLabourImmigrants() {
		return pcNonLabourImmigrants;
	}

	public static void setPcNonLabourImmigrants(double pcNonLabourImmigrants) {
		Population.pcNonLabourImmigrants = pcNonLabourImmigrants;
	}

	public static int getCrnLiveInWorkIn() {
		return crnLiveInWorkIn;
	}

	public static void setCrnLiveInWorkIn(int crnLiveInWorkIn) {
		Population.crnLiveInWorkIn = crnLiveInWorkIn;
	}

	public static int getCrnLiveInWorkOut() {
		return crnLiveInWorkOut;
	}

	public static void setCrnLiveInWorkOut(int crnLiveInWorkOut) {
		Population.crnLiveInWorkOut = crnLiveInWorkOut;
	}

	public static int getCrnLiveOutWorkIn() {
		return crnLiveOutWorkIn;
	}

	public static void setCrnLiveOutWorkIn(int crnLiveOutWorkIn) {
		Population.crnLiveOutWorkIn = crnLiveOutWorkIn;
	}

	public static int getInitLiveInWorkIn() {
		return initLiveInWorkIn;
	}

	public static void setInitLiveInWorkIn(int initLiveInWorkIn) {
		Population.initLiveInWorkIn = initLiveInWorkIn;
	}

	public static int getInitLiveInWorkOut() {
		return initLiveInWorkOut;
	}

	public static void setInitLiveInWorkOut(int initLiveInWorkOut) {
		Population.initLiveInWorkOut = initLiveInWorkOut;
	}

	public static int getInitLiveOutWorkIn() {
		return initLiveOutWorkIn;
	}

	public static void setInitLiveOutWorkIn(int initLiveOutWorkIn) {
		Population.initLiveOutWorkIn = initLiveOutWorkIn;
	}

	public static int getOutEmployedImmigrantCount() {
		return outEmployedImmigrantCount;
	}

	public static void setOutEmployedImmigrantCount(int outEmployedImmigrantCount) {
		Population.outEmployedImmigrantCount = outEmployedImmigrantCount;
	}

	public static int getUnEmployedImmigrantCount() {
		return unEmployedImmigrantCount;
	}

	public static void setUnEmployedImmigrantCount(int unEmployedImmigrantCount) {
		Population.unEmployedImmigrantCount = unEmployedImmigrantCount;
	}

	public static int getDeadLIWICount() {
		return deadLIWICount;
	}

	public static void setDeadLIWICount(int deadLIWICount) {
		Population.deadLIWICount = deadLIWICount;
	}

	public static int getDeadLIWOCount() {
		return deadLIWOCount;
	}

	public static void setDeadLIWOCount(int deadLIWOCount) {
		Population.deadLIWOCount = deadLIWOCount;
	}

	public static int getDeadNonLabCount() {
		return deadNonLabCount;
	}

	public static void setDeadNonLabCount(int deadNonLabCount) {
		Population.deadNonLabCount = deadNonLabCount;
	}

	public static int getDeadUnEmpCount() {
		return deadUnEmpCount;
	}

	public static void setDeadUnEmpCount(int deadUnEmpCount) {
		Population.deadUnEmpCount = deadUnEmpCount;
	}

	public static int getDeadEmpCount() {
		return deadEmpCount;
	}

	public static void setDeadEmpCount(int deadEmpCount) {
		Population.deadEmpCount = deadEmpCount;
	}

	public static int getImmigrantCount() {
		return immigrantCount;
	}

	public static void setImmigrantCount(int immigrantCount) {
		Population.immigrantCount = immigrantCount;
	}


}
