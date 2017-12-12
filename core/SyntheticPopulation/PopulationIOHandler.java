package core.SyntheticPopulation;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import core.HardcodedData;
import core.TextFileHandler;
import core.HardcodedData.Genders;
import core.HardcodedData.HholdRelSP;
import core.HardcodedData.HholdTypes;
import core.HardcodedData.InitSPColumns;

public class PopulationIOHandler {

	/**
	 * 
	 * @param dbTableDetail
	 */
	public static void initialisePopulationFromDB(String dbTableDetail) {
		Population.setHhPool(new HashMap<Integer,Household>());
		Population.setIndivPool(new HashMap<Integer,Individual>());
		
		// connect to the database
		System.out.println("Connect to the data base");

		Connection c = null;
		try {

			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection(dbTableDetail);
			System.out.println("Connection successful!");	

			// query the data base
			Statement querry = c.createStatement();
			String sql = "SELECT * from ps" ;

			ResultSet results = querry.executeQuery( sql );
			int count = 0;
			
			while(results.next()) {

				int hholdID = results.getInt("hhID");
				int indivID = results.getInt("indivID");
				int age = results.getInt("age");
				String genderStr = results.getString("gender");
				String hhRelStr = results.getString("hhRel");
				String hhTypeStr = results.getString("hhType");
				int zoneID = results.getInt("zoneID");
				int income = results.getInt("income");

				
				Genders gender = Genders.valueOf(genderStr);
				HholdRelSP hhRel = HholdRelSP.valueOf(hhRelStr);
				HholdTypes hhType = HholdTypes.valueOf(hhTypeStr);
				
				// create agent here and add it to a set of results
				// adds this new individual to Population.indivPool
				Individual newIndiv = new Individual(indivID, age, gender, hhRel, income);
				//Individual newIndiv = new Individual(indivID, age, gender, hhRel, income);
				Population.getIndivPool().put((Integer)indivID, newIndiv);
				
				// adds this new individual to household. 
				// If the household doesn't exist, create a new one and add it to Population.hhPool
				ArrayList<Integer> residentsID = new ArrayList<Integer>();
				Household hhold;
				if (Population.getHhPool().containsKey((Integer)hholdID)) {
					hhold = Population.getHhPool().get((Integer)hholdID);
					residentsID = Population.getHhPool().get((Integer)hholdID).getResidentsID();
				} else {
					hhold = new Household(hholdID, hhType, residentsID, Integer.toString(zoneID), HardcodedData.unknown);
				}
				residentsID.add((Integer)indivID);
				hhold.setResidentsID(residentsID);
				Population.getHhPool().put((Integer)hholdID, hhold);
				
				// print the agent:
				//System.out.println("Indiv read: " + hhID + "," + indivID + "," + age + "," + gender + "," + hhRel + "," + hhType + "," + zoneID + "," + income);

				count++;
			}
			System.out.println("COUNT: " + String.valueOf(count));

		} catch ( Exception e ) {
			System.out.println("Error: could not connect to the data base!");
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}
	
	
	/**
	 * 
	 * @param csvFile
	 */
	public static void initialisePopulationFromCSV(String csvFile) {
		ArrayList<ArrayList<String>> rawSP = TextFileHandler.readCSV(csvFile);
		
		if (rawSP==null) {
			return;
		}
		
		Population.setHhPool(new HashMap<Integer,Household>());
		Population.setIndivPool(new HashMap<Integer,Individual>());
		
		Population.setInitHhPool(new HashMap<Integer,Household>());
		Population.setInitIndivPool(new HashMap<Integer,Individual>());
		
		
		for (ArrayList<String> indRec : rawSP) {
//			System.out.println(indRec.get(InitSPColumns.hhID.getIndex()) + ", " + 
//								indRec.get(InitSPColumns.indivID.getIndex()) + ", " + 
//								indRec.get(InitSPColumns.age.getIndex()) + ", " +
//								indRec.get(InitSPColumns.gender.getIndex()) + ", " +
//								indRec.get(InitSPColumns.hhRel.getIndex()) + ", " +
//								indRec.get(InitSPColumns.hhType.getIndex()) + ", " +
//								indRec.get(InitSPColumns.zoneID.getIndex()) + ", " +
//								indRec.get(InitSPColumns.zoneDescription.getIndex()));
			
			int hholdID, indivID, age, income;
			
			try {
				hholdID = Integer.parseInt(indRec.get(InitSPColumns.hhID.getIndex()));
				indivID = Integer.parseInt(indRec.get(InitSPColumns.indivID.getIndex()));
				age = Integer.parseInt(indRec.get(InitSPColumns.age.getIndex()));
				//income = Integer.parseInt(indRec.get(InitSPColumns.incomeWkly.getIndex()));
				Genders gender = Genders.valueOf(indRec.get(InitSPColumns.gender.getIndex()));
				HholdRelSP hhRel = HholdRelSP.valueOf(indRec.get(InitSPColumns.hhRel.getIndex()));
				HholdTypes hhType = HholdTypes.valueOf(indRec.get(InitSPColumns.hhType.getIndex()));
				String zoneName = indRec.get(InitSPColumns.zoneID.getIndex());
				String zoneDesc = indRec.get(InitSPColumns.zoneDescription.getIndex());
				
				// adds this new individual to Population.indivPool
				Individual newIndiv = new Individual(indivID, age, gender, hhRel, 0);
				//Individual newIndiv = new Individual(indivID, age, gender, hhRel, income);
				Population.getIndivPool().put((Integer)indivID, newIndiv);
				
				// adds this new individual to household. 
				// If the household doesn't exist, create a new one and add it to Population.hhPool
				ArrayList<Integer> residentsID = new ArrayList<Integer>();
				Household hhold;
				if (Population.getHhPool().containsKey((Integer)hholdID)) {
					hhold = Population.getHhPool().get((Integer)hholdID);
					residentsID = Population.getHhPool().get((Integer)hholdID).getResidentsID();
				} else {
					hhold = new Household(hholdID, hhType, residentsID, zoneName, zoneDesc);
				}
				residentsID.add((Integer)indivID);
				hhold.setResidentsID(residentsID);
				Population.getHhPool().put((Integer)hholdID, hhold);
				
			} catch (NumberFormatException e) {
				continue;
			}
		}
		
		
		
		// assigns aggregated household type to households in Population.hhPool, and
		for (Household hhold : Population.getHhPool().values()) {
			// assigns aggregated household type to households in Population.hhPool
			hhold.assignAggreHholdType();
		}
		
		// puts a copy of each initial individual into Population.initIndivPool, and a copy of each initial household into Population.initHhPool
		makeCopyOfInitPopulation();
		
		rawSP.clear();
	}
	
	
	/**
	 * puts a copy of each initial individual into Population.initIndivPool, and a copy of each initial household into Population.initHhPool
	 */
	private static void makeCopyOfInitPopulation() {
		int initIndivID = 0;
		int initHholdID = 0;
		for (Household hhold : Population.getHhPool().values()) {
			// puts a copy of each individual into Population.initIndivPool
			ArrayList<Integer> residentsID = hhold.getResidentsID();
			ArrayList<Integer> initResidentsID = new ArrayList<Integer>();
			for (Integer resID : residentsID) {
				initIndivID -= 1;
				int initIndivAge = Population.getIndivPool().get(resID).getAge();
				int initIndivIncome = Population.getIndivPool().get(resID).getIncomeWkly();
				Genders initIndivGender = Population.getIndivPool().get(resID).getGender();
				HholdRelSP initIndivHhRel = Population.getIndivPool().get(resID).getHhRel();
				Population.getInitIndivPool().put((Integer)initIndivID, new Individual((Integer)initIndivID, initIndivAge, initIndivGender, initIndivHhRel, initIndivIncome));
				initResidentsID.add((Integer)initIndivID);
			}

			// puts a copy this household into Population.initHhPool
			initHholdID -= 1;
			Household initHhold = new Household(initHholdID, hhold.getHhType(), initResidentsID, hhold.getZoneName(), hhold.getZoneDescription(),hhold.getAggreHhType());
			Population.getInitHhPool().put((Integer)initHholdID, initHhold);
		}
	}
	
	/**
	 * 
	 * @param outputCSV
	 */
	public static void outputPopulation(String outputCSV) {
		ArrayList<String[]> outputPop = new ArrayList<String[]>();
		String[] header = new String[] {"hhID", "indivID", "age", "gender", "hhRel", "hhType", "aggreHhType", "zoneDescription", "zoneName", "income", "employment", "workplace", "nBedrooms"};
		//outputPop.add(header);

		for (Integer hhID : Population.getHhPool().keySet()) {
			ArrayList<Integer> residentsID = Population.getHhPool().get(hhID).getResidentsID();
			
			for (Integer indivID : residentsID) {
				String[] details = new String[header.length];
				details[0] = Integer.toString(hhID);
				details[1] = Integer.toString(indivID);
				details[2] = Integer.toString(Population.getIndivPool().get(indivID).getAge());
				details[3] = Population.getIndivPool().get(indivID).getGender().toString();
				details[4] = Population.getIndivPool().get(indivID).getHhRel().toString();
				details[5] = Population.getHhPool().get(hhID).getHhType().toString();
				if (Population.getHhPool().get(hhID).getAggreHhType()==null) {
					details[6] = "N/A";
				} else {
					details[6] = Population.getHhPool().get(hhID).getAggreHhType().toString();
				}
				
				if (Population.getHhPool().get(hhID).getZoneDescription()==null) {
					details[7] = "N/A";
				} else {
					details[7] = Population.getHhPool().get(hhID).getZoneDescription();
				}
				
				if (Population.getHhPool().get(hhID).getZoneName()==null) {
					details[8] = "N/A";
				} else {
					details[8] = Population.getHhPool().get(hhID).getZoneName();
				}
				
				details[9] = Integer.toString(Population.getIndivPool().get(indivID).getIncomeWkly());

				if (Population.getIndivPool().get(indivID).getEmpStat()==null) {
					details[10] = "N/A";
				} else {
					details[10] = Population.getIndivPool().get(indivID).getEmpStat().toString();
				}
				
				if (Population.getIndivPool().get(indivID).getWorkPlace()==null) {
					details[11] = "N/A";
				} else {
					details[11] = Population.getIndivPool().get(indivID).getWorkPlace().toString();
				}
				
				details[12] = Integer.toString(Population.getHhPool().get(hhID).getnBedroomsNeeded());
				
				outputPop.add(details);
			}
		}
		
		TextFileHandler.writeToCSV(outputCSV, header, outputPop);
	}
	
	
	/**
	 * 
	 * @param outputCSV
	 * @param appendToExistingOutput
	 */
	public static void outputPopulation(String outputCSV, String[] header, boolean appendToExistingOutput) {
		ArrayList<String[]> outputPop = new ArrayList<String[]>();
		
		for (Integer hhID : Population.getHhPool().keySet()) {
			ArrayList<Integer> residentsID = Population.getHhPool().get(hhID).getResidentsID();
			
			for (Integer indivID : residentsID) {
				String[] details = new String[header.length];
				details[0] = Integer.toString(hhID);
				details[1] = Integer.toString(indivID);
				details[2] = Integer.toString(Population.getIndivPool().get(indivID).getAge());
				details[3] = Population.getIndivPool().get(indivID).getGender().toString();
				details[4] = Population.getIndivPool().get(indivID).getHhRel().toString();
				details[5] = Population.getHhPool().get(hhID).getHhType().toString();
				details[6] = Population.getHhPool().get(hhID).getAggreHhType().toString();
				details[7] = Population.getHhPool().get(hhID).getZoneDescription();
				details[8] = Population.getHhPool().get(hhID).getZoneName();
				details[9] = Integer.toString(Population.getIndivPool().get(indivID).getIncomeWkly());
				
				if (Population.getIndivPool().get(indivID).getEmpStat()==null) {
					details[10] = "N/A";
				} else {
					details[10] = Population.getIndivPool().get(indivID).getEmpStat().toString();
				}
				
				if (Population.getIndivPool().get(indivID).getWorkPlace()==null) {
					details[11] = "N/A";
				} else {
					details[11] = Population.getIndivPool().get(indivID).getWorkPlace().toString();
				}
				
				outputPop.add(details);
			}
			
			
		}
		
		TextFileHandler.writeToCSV(outputCSV, header, outputPop, appendToExistingOutput);
	}
}
