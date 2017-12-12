package core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.io.FileUtils;

import core.SyntheticPopulation.Household;
import core.SyntheticPopulation.Population;

public class HardcodedData {
	//public static String inputTablesPath = "input tables/";
	public static String inputTablesPath;
	
	//public static String outputTablesPath = "post processing/output SP evolution/";
	public static String outputTablesPath;
	
	public static int minIncomeWkly; // will be assigned to individuals losing job
	public static double inflationYrly; // e.g. takes value of 0.02 for 2% yearly inflation 
	
	public static final String unknown = "Unknown";
	public static final int minAgeMarried = 18;
	//public static final int ageOfConsent = 16; // mindAgeParentChild
	public static final int maxdAgeParentChild = 50;
	public static final int meandAgeMarriedCouple = 2;
	public static final int stddAgeMarriedCouple = 2;
	public static final int minAgeU15 = 0;
	public static final int maxAgeU15 = 14;
	public static final int minAgeStu = 15;
	public static final int maxAgeStu = 24;
	public static final int minAgeO15 = 15;
	public static final int retiredAge = 65;
	
	
	public static final double pcOvershootingTargetMigrantsByAge = 0; 
	public static final double minHholdScoreForMigration = 0;
	public static final double minIndivScoreForMigration = 0.001;
	
	public static final double scaleFactorForEmigratingKids = 2;
	public static final double scaleFactorForImmigratingKids = 10;
	public static final double scaleFactorForImmigrating15_49Female = 1.5;
	
	public static final double femaleBabyChance = 0.55;
	
	public static Random random = new Random();
	
	public static void setSeed(long seed) {
		random.setSeed(seed);
	}
	
	
	public enum PlaceOfWork {
		in, out;
	}
	
	
	public enum EmploymentStatus {
		nonLabour(-1), unEmployed(0), employed(1);
		
		private int value;
		
		private EmploymentStatus(int newValue) {
			value = newValue;
		}
		
		public int getValue() {
			return value;
		}
	}
	
	
	public enum InitSPColumns {
		hhID(0), indivID(1), age(2), gender(3), hhRel(4), hhType(5), zoneID(6), zoneDescription(7), incomeWkly(8);
		
		private int index;
		
		private InitSPColumns (int newIndex) {
			index = newIndex;
		}
		
		public int getIndex() {
			return index;
		}
	}
	
	public enum HholdTypes {
		NF(0), HF1(1), HF2(2), HF3(3), HF4(4), HF5(5), HF6(6), HF7(7), HF8(8), HF9(9), HF10(10), HF11(11), HF12(12), HF13(13), HF14(14), HF15(15), HF16(16), 
		Unknown(17);
		
		private int index;
		
		private HholdTypes (int newIndex) {
			index = newIndex;
		}
		
		public int getIndex() {
			return index;
		} 
		
		public static int getIndexFromString(String hhType) {
			int hhIndex = -1;
			
			for (HholdTypes hh : HholdTypes.values()) {
				if (hh.toString()==hhType) {
					return hh.getIndex();
				}
			}
			
			return hhIndex;
		}
		
		public static HholdTypes getHholdTypeFromIndex(int newIndex) {
			for (HholdTypes hh : HholdTypes.values()) {
				if (hh.getIndex()==newIndex) {
					return hh;
				}
			}
			return null;
		}
		
		public static HholdTypes getHholdTypeFromString(String hhType) {
			for (HholdTypes hh : HholdTypes.values()) {
				if (hh.toString()==hhType) {
					return hh;
				}
			}
			return null;
		}
		
		public static HholdTypes[] getValidHFTypes() {
			return new HholdTypes[] {HF1, HF2, HF3, HF4, HF5, HF6, HF7, HF8, HF9, HF10, HF11, HF12, HF13, HF14, HF15, HF16};
		}
	}
	
	
	public enum AggreHholdTypes {
		couple(0), 
		coupleU15(1), coupleO15(2), coupleU15O15(3), 
		loneParentU15(4), loneParentO15(5), loneParentU15O15(6),  
		Other(7);

		private int index;

		private AggreHholdTypes (int newIndex) {
			index = newIndex;
		}
	}
	
	public enum HholdRelSP {
		Married(0),LoneParent(1),U15Child(2),Student(3),O15Child(4),Relative(5),GroupHhold(6),LonePerson(7),Unknown(8);
		
		private int index;
		
		private HholdRelSP (int newIndex) {
			index = newIndex;
		}
		
		public int getIndex() {
			return index;
		}
		
		public static int getNumberOfValidElements() {
			return (HholdRelSP.values().length - 1); // ignoring type 'Unknown';
		}
		
		public static HholdRelSP getHholdRelSPByIndex(int queryIndex) {
			for (HholdRelSP hhRelSP : HholdRelSP.values()) {
				if (hhRelSP.index==queryIndex) {
					return hhRelSP;
				}
			}
			return null;
		}
		
		public static HholdRelSP[] getValidHholdRelSP() {
			HholdRelSP[] validHhRels = {HholdRelSP.Married, HholdRelSP.LoneParent, HholdRelSP.U15Child, HholdRelSP.Student, HholdRelSP.O15Child, HholdRelSP.Relative,
					HholdRelSP.GroupHhold, HholdRelSP.LonePerson};
			return validHhRels;
		}
	}
	

	/**
	 * values of Gender (i.e. "_male" and "_female") must be exactly the same the values in column 'note' of table B22.
	 * @author nhuynh
	 *
	 */
	public enum Genders {
		_female(0), _male(1);
		
		private int value;
		
		private Genders (int newIndex) {
			value = newIndex;
		}
		
		public int getValue() {
			return value;
		} 
		
		public static Genders getGenderByValue(int val) {
			if (val==0) return Genders._female;
			else return Genders._male;
		}
	}
	
	
	/**
	 * 
	 * @author nhuynh
	 *
	 */
	public enum InputBirthRatesColumn {
		age(0), firstChild(1), secondChild(2), thirdChild(3), fourthChild(4), fifthChild(5), sixthChildMore(6);
		
		private int index;
		
		private InputBirthRatesColumn(int newIndex) {
			index = newIndex;
		}
		
		public int getIndex() {
			return index;
		}
	}
	
	
	/**
	 * 
	 * @author nhuynh
	 *
	 */
	public enum InputRatesColumn {
		age(0), probability(1);
		
		private int index;
		
		private InputRatesColumn(int newIndex) {
			index = newIndex;
		}
		
		public int getIndex() {
			return index;
		}
	}
	
	
	private static ArrayList<String> zoneNames;
	
	public static void initialiseZoneNames() {
		zoneNames = new ArrayList<String>();
		for (Household hhold : Population.getHhPool().values()) {
			if (!zoneNames.contains(hhold.getZoneName())) {
				zoneNames.add(hhold.getZoneName());
			}
		}
	}
	
	public static ArrayList<String> getZoneNames() {
		return zoneNames;
	}
	
	
	public static void initOutPutPath(String outputPath) {
		HardcodedData.outputTablesPath = outputPath;
		try {
			FileUtils.forceMkdir(new File(HardcodedData.outputTablesPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
