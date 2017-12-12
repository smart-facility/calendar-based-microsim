package core.SyntheticPopulation;

import java.util.Random;

import core.ArrayHandler;
import core.HardcodedData;
import core.HardcodedData.EmploymentStatus;
import core.Rates;
import core.HardcodedData.Genders;
import core.HardcodedData.HholdRelSP;
import core.HardcodedData.PlaceOfWork;
import core.SyntheticPopulation.CombiAge;

public class Individual {

	private int id;
	private int age;
	private Genders gender;
	private HholdRelSP hhRel;
	private int incomeWkly;
	private EmploymentStatus empStat;
	private PlaceOfWork workPlace;
	/**
	 * add dayBirth to adapt probabilities and death day
	 * @author addition : mdumont
	 */
	private int dayBirth;
	private int dayDeath;
	
	
	public Individual(int newId, int newAge, Genders newGender, HholdRelSP newHhRel, int income) {
		this.setId(newId);
		this.setAge(newAge);
		this.setGender(newGender);
		this.setHhRel(newHhRel);
		this.setIncomeWkly(income);
		this.setDayBirth();
		this.setDayDeath(false); //-1 if will survive to the year
	}

	/**
	 * increases the age of this individual by 1.
	 * changes his/her relationship to O15Child if he/she is an U15Child and age>HardcodedData.maxAgeU15.
	 * changes his/her relationship to O15Child if he/she is an Student and age>HardcodedData.maxAgeStu.
	 */
	public void age() {
		this.setAge(this.getAge()+1);
		if (this.getHhRel().equals(HholdRelSP.U15Child) && this.getAge()>HardcodedData.maxAgeU15) {
			this.setHhRel(HholdRelSP.O15Child);
		} else if (this.getHhRel().equals(HholdRelSP.Student) && this.getAge()>HardcodedData.maxAgeStu) {
			this.setHhRel(HholdRelSP.O15Child);
		}
	}


	/**
	 * determines if this individual is dead by comparing a random double number with the death probability of his/her age and gender.
	 * @return
	 */
	public boolean isDead() {

		/**
		 * divorceProb1 = proba to divorce at the age of 1st January
		 * divorceProb2 = proba to divorce at the age of 31 December (+1)
		 * divorceProb = proba to divorce during the whole year
		 * @author addition : mdumont
		 */
		double deathProb;
		double deathProb1;
		double deathProb2;

		if (this.getGender().equals(Genders._female)) {
			int queryAge = (Integer)this.getAge();
			int maxAgeInRates = ArrayHandler.max(ArrayHandler.toInt(Rates.getDeathRatesFemale().keySet()));
			if (queryAge>maxAgeInRates) 
				queryAge = maxAgeInRates;

			if (!Rates.getDeathRatesFemale().containsKey((Integer)queryAge)) 
				return false;
			deathProb1 = Rates.getDeathRatesFemale().get((Integer)queryAge);
			if (queryAge == maxAgeInRates) {
				deathProb = deathProb1;
			} else{
				deathProb2 = Rates.getDeathRatesFemale().get((Integer)(queryAge+1));
				deathProb = ((deathProb1*this.dayBirth) + (deathProb2*(365-this.dayBirth)))/365.0;
			}
		} 
		else {
			int queryAge = (Integer)this.getAge();
			int maxAgeInRates = ArrayHandler.max(ArrayHandler.toInt(Rates.getDeathRatesMale().keySet()));
			if (queryAge>maxAgeInRates) 
				queryAge = maxAgeInRates;

			if (!Rates.getDeathRatesMale().containsKey((Integer)queryAge)) 
				return false;
			deathProb1 = Rates.getDeathRatesMale().get((Integer)queryAge);
			if (queryAge == maxAgeInRates) {
				deathProb = deathProb1;
			} else{
				deathProb2 = Rates.getDeathRatesMale().get((Integer)(queryAge+1));
				deathProb = ((deathProb1*this.dayBirth) + (deathProb2*(365-this.dayBirth)))/365.0;
			}
		}

		double randDouble = HardcodedData.random.nextDouble();
		
		if (randDouble<=deathProb){
			//System.out.println("dead");
			this.setDayDeath(true);
			return true;
		}
		else {
			//System.out.println("not dead");
			return false;
		}
	}


	/**
	 * determines if this individual is divorced by comparing a random double number with the divorce probability of his/her age and gender.
	 * @return
	 */
	public boolean isDivorced() {

		double divorceProb;
		double divorceProb1;
		double divorceProb2;

		/**
		 * divorceProb1 = proba to divorce at the age of 1st January
		 * divorceProb2 = proba to divorce at the age of 31 December (+1)
		 * divorceProb = proba to divorce during the whole year
		 * @author addition : mdumont
		 */
		if (this.getGender().equals(Genders._female)) {
			int queryAge = (Integer)this.getAge();
			int maxAgeInRates = ArrayHandler.max(ArrayHandler.toInt(Rates.getDivorceRatesFemale().keySet()));
			if (queryAge>maxAgeInRates) 
				queryAge = (Integer)maxAgeInRates;

			if (!Rates.getDivorceRatesFemale().containsKey((Integer)queryAge))
				return false;
			divorceProb1 = Rates.getDivorceRatesFemale().get((Integer)queryAge);
			if (queryAge == maxAgeInRates) {
				divorceProb = divorceProb1;
			} else{
				divorceProb2 = Rates.getDivorceRatesFemale().get((Integer)(queryAge + 1));
				divorceProb = ((divorceProb1*this.dayBirth) + (divorceProb2*(365-this.dayBirth)))/365.0;
			}
			
			
		} else {
			int queryAge = (Integer)this.getAge();
			int maxAgeInRates = ArrayHandler.max(ArrayHandler.toInt(Rates.getDivorceRatesMale().keySet()));
			if (queryAge>maxAgeInRates) 
				queryAge = (Integer)maxAgeInRates;

			if (!Rates.getDivorceRatesMale().containsKey((Integer)queryAge))
				return false;
			divorceProb1 = Rates.getDivorceRatesMale().get((Integer)queryAge);
			if (queryAge == maxAgeInRates) {
				divorceProb = divorceProb1;
			} else{
				divorceProb2 = Rates.getDivorceRatesMale().get((Integer)(queryAge + 1));
				divorceProb = ((divorceProb1*this.dayBirth) + (divorceProb2*(365-this.dayBirth)))/365.0;
			}
			
		}


		double randDouble = HardcodedData.random.nextDouble();
		if (randDouble<=divorceProb) {
			/*
			 * Addition of date of death, check if he divorced before to die
			 */ 
			if (this.dayDeath > 0){
				randDouble = HardcodedData.random.nextDouble();
				if (randDouble<=(this.dayDeath/365))	return true;
				else return false;
			} else return true;
		} else return false;
	}


	/**
	 * determines if this individual is getting married by comparing a random double number with the marriage probability of his/her age and gender.
	 * if this individual is already married or is an u15Child, return false.
	 * @return
	 */
	public boolean isMarried() {
		if (this.getHhRel().equals(HholdRelSP.Married) || this.getHhRel().equals(HholdRelSP.U15Child)) 
			return false;

		double marryProb1;
		double marryProb2;
		double marryProb;
		

			/**
			 * marryProb1 = proba to marry at the age of 1st January
			 * marryProb2 = proba to marry at the age of 31 December (+1)
			 * marryProb = proba to marry during the whole year
			 * @author addition : mdumont
			 */
		if (this.getGender().equals(Genders._female)) {
			int queryAge = (Integer)this.getAge();
			int maxAgeInRates = ArrayHandler.max(ArrayHandler.toInt(Rates.getMarriageRatesFemale().keySet()));
			if (queryAge>maxAgeInRates) 
				queryAge = (Integer)maxAgeInRates;

			if (!Rates.getMarriageRatesFemale().containsKey((Integer)queryAge))
				return false;
			marryProb1 = Rates.getMarriageRatesFemale().get((Integer)queryAge);
			if (queryAge == maxAgeInRates){
				marryProb=marryProb1;
			} else {
				marryProb2 = Rates.getMarriageRatesFemale().get((Integer)(queryAge + 1));
				marryProb =  ((marryProb1*this.dayBirth) + (marryProb2*(365-this.dayBirth)))/365.0;
			}	

		} else {
			int queryAge = (Integer)this.getAge();
			int maxAgeInRates = ArrayHandler.max(ArrayHandler.toInt(Rates.getMarriageRatesMale().keySet()));
			if (queryAge>maxAgeInRates) 
				queryAge = (Integer)maxAgeInRates;

			if (!Rates.getMarriageRatesMale().containsKey((Integer)queryAge))
				return false;

			marryProb1 = Rates.getMarriageRatesMale().get((Integer)queryAge);
			if (queryAge == maxAgeInRates){
				marryProb = marryProb1;
			} else {
				marryProb2 = Rates.getMarriageRatesMale().get((Integer)(queryAge + 1));
				marryProb =  ((marryProb1*this.dayBirth) + (marryProb2*(365-this.dayBirth)))/365.0;
			}
			
		}
		
		double randDouble = HardcodedData.random.nextDouble();
		if (randDouble<=marryProb) {
			/*
			 * Addition of date of death, check if he got married before to die
			 */ 
			if (this.dayDeath > 0){
				randDouble = HardcodedData.random.nextDouble();
				if (randDouble<=(this.dayDeath/365))	return true;
				else return false;
			} else return true;
		}
		else return false;
	}


	/**
	 * 
	 * @param nCrntChildren
	 * @return
	 */
	public boolean isHavingBaby(int nCrntChildren) {

		if (this.getHhRel().equals(HholdRelSP.Married) && this.getGender().equals(Genders._female)) {

			int queryAge = (Integer)this.getAge();
			int maxAgeInRates = ArrayHandler.max(ArrayHandler.toInt(Rates.getBirthRates().keySet()));
			if (queryAge>maxAgeInRates) 
				queryAge = (Integer)maxAgeInRates;

			if (!Rates.getBirthRates().containsKey((Integer)queryAge)) {
				return false;
			}

			int orderNewBaby = nCrntChildren + 1; // if nCrntChildren is 0, then this female is having 1st baby (orderNewBaby=1), and so on. 

			int childColumn = orderNewBaby-1;
			if (orderNewBaby>HardcodedData.InputBirthRatesColumn.sixthChildMore.getIndex()) {
				childColumn = HardcodedData.InputBirthRatesColumn.sixthChildMore.getIndex()-1;
			}
			/**
			 * havingBabyProb1 = proba to have a child at the age of 1st January
			 * havingBabyProb2 = proba to have a child at the age of 31 December (+1)
			 * havingBabyProb = proba to have a child during the whole year
			 * @author addition : mdumont
			 */
			double havingBabyProb1 = Rates.getBirthRates().get(queryAge)[childColumn];
			double havingBabyProb2;
			double havingBabyProb;
			if (Rates.getBirthRates().containsKey((Integer)(queryAge+1))) {
				havingBabyProb2 = Rates.getBirthRates().get((Integer)(queryAge+1))[childColumn];
				havingBabyProb = ((havingBabyProb1*this.dayBirth) + (havingBabyProb2*(365-this.dayBirth)))/365.0;
			} else havingBabyProb=havingBabyProb1;
			

			/**
			 * Print everything to check
			 * @author : mdumont
			 */
			//System.out.println("Age maman:" + queryAge);
			//System.out.println("N babies:" + nCrntChildren);
			//System.out.println("Birth day:" + this.dayBirth);
			//System.out.println(havingBabyProb1);
			//System.out.println(havingBabyProb2);
			//System.out.println(havingBabyProb);
			//System.out.println(" ");

			double randDouble = HardcodedData.random.nextDouble();
			if (randDouble<=havingBabyProb) {
				/*
			 	* Addition of date of death, check if he has the baby before to die
			 	*/ 
				if (this.dayDeath > 0){
					randDouble = HardcodedData.random.nextDouble();
					if (randDouble<=(this.dayDeath/365.0))	return true;
					else return false;
				} else return true;
			}
				
			else return false;
		} else {
			return false;
		}
	}

	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Genders getGender() {
		return gender;
	}

	public void setGender(Genders gender) {
		this.gender = gender;
	}

	/**
	 * addition for dates of birth
	 * @author : mdumont
	 */

	public void setDayBirth() {
		if (CombiAge.combiAge) {
			Random random = new Random();
			this.dayBirth = (int)(Math.round(random.nextDouble()*364 + 1));
		} else{
			this.dayBirth = 365;
		}
		
	}

		
	public int getDayBirth(){
		return dayBirth;			
	}

	/**
	 * addition for dates of death
	 * -1 if survive to the year
	 * @author : mdumont
	 */

	public void setDayDeath(boolean willDie){
		if (CombiAge.combiDeath && willDie) {
			Random random = new Random();
			this.dayDeath = (int)(Math.round(random.nextDouble()*364 + 1));
		} else{
			this.dayDeath = -1;
		}
		
	}

	public int getDayDeath(){
		return dayDeath;
	}

	public HholdRelSP getHhRel() {
		return hhRel;
	}

	public void setHhRel(HholdRelSP hhRel) {
		this.hhRel = hhRel;
	}

	public int getIncomeWkly() {
		return incomeWkly;
	}

	public void setIncomeWkly(int incomeWkly) {
		this.incomeWkly = incomeWkly;
	}

	public EmploymentStatus getEmpStat() {
		return empStat;
	}

	public void setEmpStat(EmploymentStatus empStat) {
		this.empStat = empStat;
	}

	public PlaceOfWork getWorkPlace() {
		return workPlace;
	}

	public void setWorkPlace(PlaceOfWork workPlace) {
		this.workPlace = workPlace;
	}
}
