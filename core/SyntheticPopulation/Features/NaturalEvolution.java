package core.SyntheticPopulation.Features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import core.ArrayHandler;
import core.HardcodedData;
import core.HardcodedData.EmploymentStatus;
import core.HardcodedData.Genders;
import core.HardcodedData.HholdRelSP;
import core.HardcodedData.HholdTypes;
import core.HardcodedData.PlaceOfWork;
import core.SyntheticPopulation.Household;
import core.SyntheticPopulation.Individual;
import core.SyntheticPopulation.Population;
import core.SyntheticPopulation.PopulationAnalytics;
import core.SyntheticPopulation.CombiAge;

public class NaturalEvolution {

	/**
	 * 
	 */
	public static void age() {
		System.out.println("\tStarts age()...");
		Iterator<Entry<Integer, Household>> itHhold = Population.getHhPool().entrySet().iterator();
		// for each household in the hhPool
		while (itHhold.hasNext()) {
			Map.Entry<Integer, Household> entry = itHhold.next();

			Integer hhID = entry.getKey();
			// for each resident in this household
			Iterator<Integer> itIndiv = Population.getHhPool().get(hhID).getResidentsID().iterator();
			while (itIndiv.hasNext()) {
				Integer indivID = itIndiv.next();
				
				// ages and updates relationship of this individual if necessary
				if (Population.getIndivPool().get(indivID)==null) {
					System.out.println("\nWARNING indivPool.get(indivID)==null, indivID = " + indivID + ", hhID = " + hhID);
				}
				
				Population.getIndivPool().get(indivID).age();
			}
		}
		System.out.println("\tEnds age()...");
	}
	
	
	/**
	 * 
	 * @return
	 */
	public static int[] passAwayWithEmplTracking() {
		System.out.println("\tStarts passAway()...");
		
		int[] deadEmpStat = new int[2];
		
		Iterator<Entry<Integer, Household>> itHhold = Population.getHhPool().entrySet().iterator();
		// for each household in the hhPool
		while (itHhold.hasNext()) {
			Map.Entry<Integer, Household> entry = itHhold.next();
			
			Integer hhID = entry.getKey();
			// for each resident in this household
			Iterator<Integer> itIndiv = Population.getHhPool().get(hhID).getResidentsID().iterator();
			while (itIndiv.hasNext()) {
				Integer indivID = itIndiv.next();
				
				// is this individual dead
				if (Population.getIndivPool().get(indivID).isDead()) {
					if (Population.getIndivPool().get(indivID).getEmpStat()!=null) {
//						if (Population.getIndivPool().get(indivID).getEmpStat().equals(EmploymentStatus.employed)) {
//							Population.setDeadEmpCount(Population.getDeadEmpCount()+1);
//							if (Population.getIndivPool().get(indivID).getWorkPlace().equals(PlaceOfWork.in)) {
//								Population.setDeadLIWICount(Population.getDeadLIWICount()+1);
//							} else if (Population.getIndivPool().get(indivID).getWorkPlace().equals(PlaceOfWork.out)) {
//								Population.setDeadLIWOCount(Population.getDeadLIWOCount()+1);
//							}
//						} else if (Population.getIndivPool().get(indivID).getEmpStat().equals(EmploymentStatus.nonLabour)) {
//							Population.setDeadNonLabCount(Population.getDeadNonLabCount()+1);
//						} else if (Population.getIndivPool().get(indivID).getEmpStat().equals(EmploymentStatus.unEmployed)) {
//							Population.setDeadUnEmpCount(Population.getDeadUnEmpCount()+1);
//						}
						if (Population.getIndivPool().get(indivID).getEmpStat().equals(EmploymentStatus.employed) && Population.getIndivPool().get(indivID).getWorkPlace().equals(PlaceOfWork.in)) {
							deadEmpStat[0] += 1;
						} else if (Population.getIndivPool().get(indivID).getEmpStat().equals(EmploymentStatus.employed) && Population.getIndivPool().get(indivID).getWorkPlace().equals(PlaceOfWork.out)) {
							deadEmpStat[1] += 1;
						}
					}
					
					
					Population.getIndivPool().remove(indivID);
					itIndiv.remove();
				}
			}

			// if there is no residents left in this household, remove it from hhPool
			if (Population.getHhPool().get(hhID).getResidentsID()==null || Population.getHhPool().get(hhID).getResidentsID().size()==0) {
				itHhold.remove();
			} else {
				// updates the type of this household to correctly reflect the new composition of residents
				Population.getHhPool().get(hhID).assignAggreHholdType();
			}
		}
		
		System.out.println("\tEnds passAway()...\n");
		
		return deadEmpStat;
	}
	
	/**
	 * 
	 */
	public static HashMap<String,Integer> passAwayWithVacatedDwellings() {
		System.out.println("\tStarts passAwayWithVacatedDwellings()...");
		
		HashMap<String,Integer> vacatedDwellings = new HashMap<String,Integer>();
		
		Iterator<Entry<Integer, Household>> itHhold = Population.getHhPool().entrySet().iterator();
		// for each household in the hhPool
		while (itHhold.hasNext()) {
			Map.Entry<Integer, Household> entry = itHhold.next();
			
			Integer hhID = entry.getKey();
			// for each resident in this household
			Iterator<Integer> itIndiv = Population.getHhPool().get(hhID).getResidentsID().iterator();
			while (itIndiv.hasNext()) {
				Integer indivID = itIndiv.next();
				if (Population.getIndivPool().get(indivID)==null) {
					System.out.println("\nWARNING indivPool.get(indivID)==null, indivID = " + indivID + ", hhID = " + hhID);
				}
				
				// is this individual dead
				if (Population.getIndivPool().get(indivID).isDead()) {
					
//					System.out.println("individual " + indivID + ", aged " + Population.getIndivPool().get(indivID).getAge() + " is dead");
//					if (Population.getIndivPool().get(indivID).getEmpStat()==null) {
//						System.out.println("Population.getIndivPool().get(" + indivID + ").getEmpStat().equals(null)");
//					}
					
					if (Population.getIndivPool().get(indivID).getEmpStat()!=null) {
						if (Population.getIndivPool().get(indivID).getEmpStat().equals(EmploymentStatus.employed)) {
							Population.setDeadEmpCount(Population.getDeadEmpCount()+1);
							if (Population.getIndivPool().get(indivID).getWorkPlace().equals(PlaceOfWork.in)) {
								Population.setDeadLIWICount(Population.getDeadLIWICount()+1);
							} else if (Population.getIndivPool().get(indivID).getWorkPlace().equals(PlaceOfWork.out)) {
								Population.setDeadLIWOCount(Population.getDeadLIWOCount()+1);
							}
						} else if (Population.getIndivPool().get(indivID).getEmpStat().equals(EmploymentStatus.nonLabour)) {
							Population.setDeadNonLabCount(Population.getDeadNonLabCount()+1);
						} else if (Population.getIndivPool().get(indivID).getEmpStat().equals(EmploymentStatus.unEmployed)) {
							Population.setDeadUnEmpCount(Population.getDeadUnEmpCount()+1);
						}
					}
					
					
					Population.getIndivPool().remove(indivID);
					itIndiv.remove();
				}

				// if this person survives the death test and is unemployed and is above 60, turn him/her to non-labour
				else {
					if (Population.getIndivPool().get(indivID).getAge()>=60 &&
							Population.getIndivPool().get(indivID).getEmpStat()!=null && Population.getIndivPool().get(indivID).getEmpStat().equals(EmploymentStatus.unEmployed)) {
						Population.getIndivPool().get(indivID).setEmpStat(EmploymentStatus.nonLabour);
					}
				}
				
			}

			// if there is no residents left in this household, remove it from hhPool
			if (Population.getHhPool().get(hhID).getResidentsID()==null || Population.getHhPool().get(hhID).getResidentsID().size()==0) {
				String thisHholdZoneName = Population.getHhPool().get(hhID).getZoneName();
				int nVacatedThisZone = 0;
				if (vacatedDwellings.containsKey(thisHholdZoneName)) {
					nVacatedThisZone = vacatedDwellings.get(thisHholdZoneName);
				}
				nVacatedThisZone += 1;
				vacatedDwellings.put(thisHholdZoneName, (Integer)nVacatedThisZone);
				itHhold.remove();
			} else {
				// updates the type of this household to correctly reflect the new composition of residents
				Population.getHhPool().get(hhID).assignAggreHholdType();
			}
		}
		
		System.out.println("\tEnds passAway()...");
		
		return vacatedDwellings;
	}
	
	/**
	 * 
	 */
	public static void passAway() {
		System.out.println("\tStarts passAway()...");
		
		Iterator<Entry<Integer, Household>> itHhold = Population.getHhPool().entrySet().iterator();
		// for each household in the hhPool
		while (itHhold.hasNext()) {
			Map.Entry<Integer, Household> entry = itHhold.next();
			
			Integer hhID = entry.getKey();
			// for each resident in this household
			Iterator<Integer> itIndiv = Population.getHhPool().get(hhID).getResidentsID().iterator();
			while (itIndiv.hasNext()) {
				Integer indivID = itIndiv.next();
				if (Population.getIndivPool().get(indivID)==null) {
					System.out.println("\nWARNING indivPool.get(indivID)==null, indivID = " + indivID + ", hhID = " + hhID);
				}
				
				// is this individual dead
				if (Population.getIndivPool().get(indivID).isDead()) {
					
					
//					
					/*
					 * if addition mdumont
					 */
					if (!CombiAge.combiDeath){
					//	System.out.println("individual " + indivID + ", aged " + Population.getIndivPool().get(indivID).getAge() + " is dead");
//					if (Population.getIndivPool().get(indivID).getEmpStat()==null) {
//						System.out.println("Population.getIndivPool().get(" + indivID + ").getEmpStat().equals(null)");
//					}
					
					if (Population.getIndivPool().get(indivID).getEmpStat()!=null) {
						if (Population.getIndivPool().get(indivID).getEmpStat().equals(EmploymentStatus.employed)) {
							Population.setDeadEmpCount(Population.getDeadEmpCount()+1);
							if (Population.getIndivPool().get(indivID).getWorkPlace().equals(PlaceOfWork.in)) {
								Population.setDeadLIWICount(Population.getDeadLIWICount()+1);
							} else if (Population.getIndivPool().get(indivID).getWorkPlace().equals(PlaceOfWork.out)) {
								Population.setDeadLIWOCount(Population.getDeadLIWOCount()+1);
							}
						} else if (Population.getIndivPool().get(indivID).getEmpStat().equals(EmploymentStatus.nonLabour)) {
							Population.setDeadNonLabCount(Population.getDeadNonLabCount()+1);
						} else if (Population.getIndivPool().get(indivID).getEmpStat().equals(EmploymentStatus.unEmployed)) {
							Population.setDeadUnEmpCount(Population.getDeadUnEmpCount()+1);
						}
					}
						Population.getIndivPool().remove(indivID);
						itIndiv.remove();
					}
				}
				// if this person survives the death test and is unemployed and is above 60, turn him/her to non-labour
				else {
					if (Population.getIndivPool().get(indivID).getAge()>=60 &&
							Population.getIndivPool().get(indivID).getEmpStat()!=null && Population.getIndivPool().get(indivID).getEmpStat().equals(EmploymentStatus.unEmployed)) {
						Population.getIndivPool().get(indivID).setEmpStat(EmploymentStatus.nonLabour);
					}
				}
				
			}
			
			if (!CombiAge.combiDeath){	
				// if there is no residents left in this household, remove it from hhPool
				if (Population.getHhPool().get(hhID).getResidentsID()==null || Population.getHhPool().get(hhID).getResidentsID().size()==0) {
					itHhold.remove();
				} else {
					// updates the type of this household to correctly reflect the new composition of residents
					Population.getHhPool().get(hhID).assignAggreHholdType();
				}
			}
		}
		
		System.out.println("\tEnds passAway()...");
	}
	
	

	/**
	 *  delete died people - usefull when date of death activated
	 * @author: mdumont
	 */
	public static void makeDie() {
		System.out.println("\tStarts deleting dead people()...");
		
		Iterator<Entry<Integer, Household>> itHhold = Population.getHhPool().entrySet().iterator();
		// for each household in the hhPool
		while (itHhold.hasNext()) {
			Map.Entry<Integer, Household> entry = itHhold.next();
			
			Integer hhID = entry.getKey();
			// for each resident in this household
			Iterator<Integer> itIndiv = Population.getHhPool().get(hhID).getResidentsID().iterator();
			while (itIndiv.hasNext()) {
				Integer indivID = itIndiv.next();
				if (Population.getIndivPool().get(indivID)==null) {
					System.out.println("\nWARNING indivPool.get(indivID)==null, indivID = " + indivID + ", hhID = " + hhID);
				}
				
				// is this individual dead
				if (Population.getIndivPool().get(indivID).getDayDeath() > 0) {	
//					System.out.println("individual " + indivID + ", aged " + Population.getIndivPool().get(indivID).getAge() + " is dead");
//					if (Population.getIndivPool().get(indivID).getEmpStat()==null) {
//						System.out.println("Population.getIndivPool().get(" + indivID + ").getEmpStat().equals(null)");
//					}
					
					if (Population.getIndivPool().get(indivID).getEmpStat()!=null) {
						if (Population.getIndivPool().get(indivID).getEmpStat().equals(EmploymentStatus.employed)) {
							Population.setDeadEmpCount(Population.getDeadEmpCount()+1);
							if (Population.getIndivPool().get(indivID).getWorkPlace().equals(PlaceOfWork.in)) {
								Population.setDeadLIWICount(Population.getDeadLIWICount()+1);
							} else if (Population.getIndivPool().get(indivID).getWorkPlace().equals(PlaceOfWork.out)) {
								Population.setDeadLIWOCount(Population.getDeadLIWOCount()+1);
							}
						} else if (Population.getIndivPool().get(indivID).getEmpStat().equals(EmploymentStatus.nonLabour)) {
							Population.setDeadNonLabCount(Population.getDeadNonLabCount()+1);
						} else if (Population.getIndivPool().get(indivID).getEmpStat().equals(EmploymentStatus.unEmployed)) {
							Population.setDeadUnEmpCount(Population.getDeadUnEmpCount()+1);
						}
					}
					
					Population.getIndivPool().remove(indivID);
					itIndiv.remove();
				}
								
			}

			// if there is no residents left in this household, remove it from hhPool
			if (Population.getHhPool().get(hhID).getResidentsID()==null || Population.getHhPool().get(hhID).getResidentsID().size()==0) {

				itHhold.remove();

			} else {

				// updates the type of this household to correctly reflect the new composition of residents

				Population.getHhPool().get(hhID).assignAggreHholdType();

			}

		}
		
		System.out.println("\tEnds makeDie()...");
	}
	/**
	 * 
	 */
	public static void divorce() {
		System.out.println("\tStarts divorce()...");
		
		ArrayList<Integer> newDevorcees = new ArrayList<Integer>();
		
		Iterator<Entry<Integer, Household>> itHhold = Population.getHhPool().entrySet().iterator();
		// for each household in the hhPool
		while (itHhold.hasNext()) {
			Map.Entry<Integer, Household> entry = itHhold.next();
			
			Integer hhID = entry.getKey();
			
			ArrayList<Integer> marriedIDs = Population.getHhPool().get(hhID).getIndivIDOfHHRel(HholdRelSP.Married);
			
			// if this household has 2 married individuals and at least one of them wants to divorce
			if (marriedIDs.size()==2 && 
					(Population.getIndivPool().get(marriedIDs.get(0)).isDivorced() || Population.getIndivPool().get(marriedIDs.get(1)).isDivorced())) {
				
				// determines Id of the individual leaving the household
				Integer leavingIndivID = determineIDLeavingDevorcedIndiv(marriedIDs);

				newDevorcees.add(leavingIndivID);
				
				// removes the ID of the leaving individual from the list of residents in the existing household
				Population.getHhPool().get(hhID).getResidentsID().remove(leavingIndivID);
				// changes the household relationship of the remaining married individual to LoneParent.
				Integer remMarriedIndivID = marriedIDs.get(0);
				if (remMarriedIndivID.equals(leavingIndivID)) 
					remMarriedIndivID = marriedIDs.get(1);
				Population.getIndivPool().get(remMarriedIndivID).setHhRel(HholdRelSP.LoneParent);
				
				// cleans and reclassifies the type of the existing household
				Population.getHhPool().get(hhID).assignAggreHholdType();
			}
		}
		
		// allocates each of the people who got divorced into one new household
		for (Integer indivID : newDevorcees) {
			makeNewHholdForDevorcee(indivID);
		}
		
		System.out.println("\tEnds divorce()...");
	}
	
	
	/**
	 * decides the person leaving the household of a divorced couple.
	 * The leaving person is a male if the couple is different sex. If the couple is same sex, the leaving person is any of the 2.
	 * 
	 * @param marriedIDs
	 * @return
	 */
	private static Integer determineIDLeavingDevorcedIndiv(ArrayList<Integer> marriedIDs) {
		Integer leavingIndivID = marriedIDs.get(1);
		// if these two married individuals have different gender
		if (!Population.getIndivPool().get(marriedIDs.get(0)).getGender().equals(Population.getIndivPool().get(marriedIDs.get(1)).getGender())) {
			// the leaving individual is the male
			if (Population.getIndivPool().get(marriedIDs.get(0)).getGender().equals(Genders._male))
				leavingIndivID = marriedIDs.get(0);
			else 
				leavingIndivID = marriedIDs.get(1);
		}
		return leavingIndivID;
	}
	
	
	/**
	 * creates a new household for each devorcee in the population.
	 * The household type will be NF. The relationship of the devorcee is set to LonePerson
	 * @param leavingIndivID
	 */
	private static void makeNewHholdForDevorcee(Integer leavingIndivID) {
		// sets the relationship of the leaving individual to NonFamilyMember
		Population.getIndivPool().get(leavingIndivID).setHhRel(HholdRelSP.LonePerson);
		
		// creates a new household for the leaving individual
		int newhhID = PopulationAnalytics.calculateNewHholdID();
		ArrayList<Integer> newResidentIDs = new ArrayList<Integer>();
		newResidentIDs.add(leavingIndivID);
		
		Household newHhold = new Household(newhhID, HholdTypes.NF, newResidentIDs, HardcodedData.unknown, HardcodedData.unknown);
		Population.addHholdToPopulation(newHhold);
	}
	
	
	/**
	 * 
	 */
	public static HashMap<String,Integer> marryWithVacatedDwellings() {
		System.out.println("\tStarts marry()...");
		
		// gets a list of males and females ready to get married in each household
		HashMap<Integer,ArrayList<Integer>> marryIndivsByHhID = new HashMap<Integer,ArrayList<Integer>>();
		ArrayList<Integer> marryingHhIDs = new ArrayList<Integer>();
		for (Integer hhID : Population.getHhPool().keySet()) {
			for (Integer indivID : Population.getHhPool().get(hhID).getResidentsID()) {
				if (Population.getIndivPool().get(indivID).isMarried()) {
					ArrayList<Integer> marryIndivs = new ArrayList<Integer>();
					if (marryIndivsByHhID.containsKey(hhID)) {
						marryIndivs = marryIndivsByHhID.get(hhID);
					}
					marryIndivs.add(indivID);
					marryIndivsByHhID.put(hhID, marryIndivs);
					if (!marryingHhIDs.contains(hhID)) {
						marryingHhIDs.add(hhID);
					}
				}
			}
		}
		
		int nHholdsCreated = 0;
		while (marryIndivsByHhID.size()>0) {
			// randomly picks 2 households from marryIndivsByHhID.keySet()
			if (marryIndivsByHhID.size()>=2) {
				int nhhCreated = marryIndivsFrom2Hholds(marryIndivsByHhID);
				nHholdsCreated += nhhCreated;
			} else if (marryIndivsByHhID.size()==1) {
				marryIndivsFromSameHhold(marryIndivsByHhID);
				nHholdsCreated += 1;
			}
			
			// removes hhIDs with empty marry individual ID from marryIndivsByHhID
			Iterator<Entry<Integer, ArrayList<Integer>>> it = marryIndivsByHhID.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Integer, ArrayList<Integer>> entry = it.next();
				ArrayList<Integer> marryIDs = entry.getValue();
				if (marryIDs==null || marryIDs.size()==0) {
					it.remove();
				}
			}
		}
		
		// corrects household relationship of remaining residents in households that have individuals just got married.
		// if any of these households have no residents, remove them from the household pool.
		int nHholdsRemoved = 0;
		int nHholdsRemovedUnknownZone = 0;
		HashMap<String,Integer> vacatedDwellingsDueToMarriage = new HashMap<String,Integer>();
		for (Integer marryingHhID : marryingHhIDs) {
			if (Population.getHhPool().get(marryingHhID).getResidentsID()==null || Population.getHhPool().get(marryingHhID).getResidentsID().size()==0) {
				nHholdsRemoved += 1;
				String thisHhZoneName = Population.getHhPool().get(marryingHhID).getZoneName();
				if (thisHhZoneName==HardcodedData.unknown) {
					nHholdsRemovedUnknownZone += 1;
				} else {
					int nVacatedDwellingsThisZone = 0;
					if (vacatedDwellingsDueToMarriage.containsKey(thisHhZoneName)) {
						nVacatedDwellingsThisZone = vacatedDwellingsDueToMarriage.get(thisHhZoneName);
					}
					nVacatedDwellingsThisZone += 1;
					vacatedDwellingsDueToMarriage.put(thisHhZoneName, (Integer)nVacatedDwellingsThisZone);
				}
				Population.getHhPool().remove(marryingHhID);
				
			} else {
				Population.getHhPool().get(marryingHhID).correctRelationshipInHhold();
				Population.getHhPool().get(marryingHhID).assignAggreHholdType();
			}
		}
				
		System.out.println("\tEnds marry()...");
		
		return vacatedDwellingsDueToMarriage;
	}
	
	
	/**
	 * 
	 */
	public static void marry() {
		System.out.println("\tStarts marry()...");
		
		// gets a list of males and females ready to get married in each household
		HashMap<Integer,ArrayList<Integer>> marryIndivsByHhID = new HashMap<Integer,ArrayList<Integer>>();
		ArrayList<Integer> marryingHhIDs = new ArrayList<Integer>();
		for (Integer hhID : Population.getHhPool().keySet()) {
			for (Integer indivID : Population.getHhPool().get(hhID).getResidentsID()) {
				if (Population.getIndivPool().get(indivID).isMarried()) {
					ArrayList<Integer> marryIndivs = new ArrayList<Integer>();
					if (marryIndivsByHhID.containsKey(hhID)) {
						marryIndivs = marryIndivsByHhID.get(hhID);
					}
					marryIndivs.add(indivID);
					marryIndivsByHhID.put(hhID, marryIndivs);
					if (!marryingHhIDs.contains(hhID)) {
						marryingHhIDs.add(hhID);
					}
				}
			}
		}
		
		int nHholdsCreated = 0;
		while (marryIndivsByHhID.size()>0) {
			// randomly picks 2 households from marryIndivsByHhID.keySet()
			if (marryIndivsByHhID.size()>=2) {
				int nhhCreated = marryIndivsFrom2Hholds(marryIndivsByHhID);
				nHholdsCreated += nhhCreated;
			} else if (marryIndivsByHhID.size()==1) {
				marryIndivsFromSameHhold(marryIndivsByHhID);
				nHholdsCreated += 1;
			}
			
			// removes hhIDs with empty marry individual ID from marryIndivsByHhID
			Iterator<Entry<Integer, ArrayList<Integer>>> it = marryIndivsByHhID.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Integer, ArrayList<Integer>> entry = it.next();
				ArrayList<Integer> marryIDs = entry.getValue();
				if (marryIDs==null || marryIDs.size()==0) {
					it.remove();
				}
			}
		}
		
		// corrects household relationship of remaining residents in households that have individuals just got married.
		// if any of these households have no residents, remove them from the household pool.
		int nHholdsRemoved = 0;
		int nHholdsRemovedUnknownZone = 0;
		for (Integer marryingHhID : marryingHhIDs) {
			if (Population.getHhPool().get(marryingHhID).getResidentsID()==null || Population.getHhPool().get(marryingHhID).getResidentsID().size()==0) {
				nHholdsRemoved += 1;
				if (Population.getHhPool().get(marryingHhID).getZoneName()==HardcodedData.unknown) {
					nHholdsRemovedUnknownZone += 1;
				}
				Population.getHhPool().remove(marryingHhID);
				
			} else {
				Population.getHhPool().get(marryingHhID).correctRelationshipInHhold();
				Population.getHhPool().get(marryingHhID).assignAggreHholdType();
			}
		}
		
		System.out.println("\tEnds marry()...");
	}
	
	
	/**
	 * 
	 * @param marryIndivsByHhID
	 */
	private static int marryIndivsFrom2Hholds(HashMap<Integer,ArrayList<Integer>> marryIndivsByHhID) {
		// randomly picks 2 households from the collection of households that have individuals who want to marry (i.e. from marryIndivsByHhID) 
		int[] hhIDs = ArrayHandler.pickRandomFromArray(ArrayHandler.toInt(marryIndivsByHhID.keySet()), null, 2, HardcodedData.random);
		Integer h1ID = hhIDs[0];
		Integer h2ID = hhIDs[1];
		// picks a male from marryInH1
		Integer maleH1 = pickIndivFromList(marryIndivsByHhID.get(h1ID), Genders._male);
		// picks a female from marryInH1
		Integer femaleH1 = pickIndivFromList(marryIndivsByHhID.get(h1ID), Genders._female);
		
		// picks a male from marryInH2
		Integer maleH2 = pickIndivFromList(marryIndivsByHhID.get(h2ID), Genders._male);
		// picks a female from marryInH2
		Integer femaleH2 = pickIndivFromList(marryIndivsByHhID.get(h2ID), Genders._female);
		
		int nHholdCreated = 0;
		
		// matches and marries them
		if (maleH1!=null && femaleH2!=null) {
			// removes maleH1 from marryInH1 and femaleH2 from marryInH2 and updates marryIndivsByHhID
			marryIndivsByHhID.get(h1ID).remove(maleH1);
			marryIndivsByHhID.get(h2ID).remove(femaleH2);
			// marries them by removing them from their current households and putting them into a new household
			marryIndividuals(maleH1, femaleH2, h1ID, h2ID, marryIndivsByHhID);
			nHholdCreated = 1;
		} 
		else if (femaleH1!=null && maleH2!=null) {
			// removes femaleH1 from marryInH1 and maleH2 from marryInH2 and updates marryIndivsByHhID
			//removeMarryIndivFromAvailList(femaleH1, maleH2, h1ID, h2ID, marryIndivsByHhID);
			marryIndivsByHhID.get(h1ID).remove(femaleH1);
			marryIndivsByHhID.get(h2ID).remove(maleH2);
			// marries them by removing them from their current households and putting them into a new household
			marryIndividuals(femaleH1, maleH2, h1ID, h2ID, marryIndivsByHhID);
			nHholdCreated = 1;
		} 
		else if (femaleH1!=null && femaleH2!=null) {
			// removes femaleH1 from marryInH1 and femaleH2 from marryInH2 and updates marryIndivsByHhID
			//removeMarryIndivFromAvailList(femaleH1, femaleH2, h1ID, h2ID, marryIndivsByHhID);
			marryIndivsByHhID.get(h1ID).remove(femaleH1);
			marryIndivsByHhID.get(h2ID).remove(femaleH2);
			// marries them by removing them from their current households and putting them into a new household
			marryIndividuals(femaleH1, femaleH2, h1ID, h2ID, marryIndivsByHhID);
			nHholdCreated = 1;
		} 
		else if (maleH1!=null && maleH2!=null) {
			// removes maleH1 from marryInH1 and maleH2 from marryInH2 and updates marryIndivsByHhID
			//removeMarryIndivFromAvailList(maleH1, maleH2, h1ID, h2ID, marryIndivsByHhID);
			marryIndivsByHhID.get(h1ID).remove(maleH1);
			marryIndivsByHhID.get(h2ID).remove(maleH2);
			// marries them by removing them from their current households and putting them into a new household
			marryIndividuals(maleH1, maleH2, h1ID, h2ID, marryIndivsByHhID);
			nHholdCreated = 1;
		}
		
		return nHholdCreated;
	}
	
	
	/**
	 * picks the ID of the first individual with the given gender in the given indivIDList.
	 * @param indivIDList
	 * @param gender
	 * @return
	 */
	private static Integer pickIndivFromList(ArrayList<Integer> indivIDList, Genders gender) {
		Integer idPicked = null;
		for (Integer indivID : indivIDList) {
			if (Population.getIndivPool().get(indivID).getGender().equals(gender)) {
				idPicked = indivID;
				break;
			}
		}
		return idPicked;
	}
	
	
	/**
	 * marries individuals indiv1ID and indiv2ID by removing them from their current households and putting them into a new household.
	 * if they are LoneParent, all children not available for marrying (i.e. not in marryIDsivsByHhold) must accompany them to the new household.
	 * @param indiv1ID
	 * @param indiv2ID
	 * @param h1ID
	 * @param h2ID
	 * @param marryIDsByHhold
	 */
	private static void marryIndividuals(Integer indiv1ID, Integer indiv2ID, Integer h1ID, Integer h2ID, HashMap<Integer,ArrayList<Integer>> marryIDsByHhold) {
		
		// creates an ArrayList of ID of individuals in the new household, starting with indiv1ID and indiv2ID
		ArrayList<Integer> newResidents = new ArrayList<Integer>();
		newResidents.add(indiv1ID);
		newResidents.add(indiv2ID);
		
		// if indiv1ID is a lone parent
		if (Population.getIndivPool().get(indiv1ID).getHhRel().equals(HholdRelSP.LoneParent)) {
			// adds any children (U15Child, Student, O15Child) in h1ID that will follow indiv1ID to the new household AND removes these individuals from h1ID.
			// Note that the relationship of these individuals don't change in the new household.
			
			//System.out.println("marryIndividuals: Hhold " + h1ID + " has LoneParent, hhSize " + hhPool.get(h1ID).getResidentsID().size());
			Iterator<Integer> itIndiv = Population.getHhPool().get(h1ID).getResidentsID().iterator();
			while (itIndiv.hasNext()) {
				Integer indID = itIndiv.next();
				
				if (indID.equals(indiv1ID)) continue;
				
				if (Population.getIndivPool().get(indID).getHhRel().equals(HholdRelSP.U15Child)) {
					newResidents.add(indID);
					// removes indID from h1ID
					itIndiv.remove();
					//System.out.println("\tRemoved 1 resident from hhold " + h1ID + ", hhSize " + hhPool.get(h1ID).getResidentsID().size());
				} else if (Population.getIndivPool().get(indID).getHhRel().equals(HholdRelSP.Student) || Population.getIndivPool().get(indID).getHhRel().equals(HholdRelSP.O15Child)) {
					if (marryIDsByHhold.get(h1ID)!=null && !marryIDsByHhold.get(h1ID).contains(indID)) {
						newResidents.add(indID);
						// removes indID from h1ID
						itIndiv.remove();
						//System.out.println("\tRemoved 1 resident from hhold " + h1ID + ", hhSize " + hhPool.get(h1ID).getResidentsID().size());
					}
				}
			}
		}
		
		// if indiv2ID is a lone parent
		if (Population.getIndivPool().get(indiv2ID).getHhRel().equals(HholdRelSP.LoneParent)) {
			// adds any individuals in h2ID that will follow indiv2ID to the new household AND removes these individuals from h2ID.
			// Note that the relationship of these individuals don't change in the new household.
			//System.out.println("marryIndividuals: Hhold " + h2ID + " has LoneParent, hhSize " + hhPool.get(h2ID).getResidentsID().size());
			Iterator<Integer> itIndiv = Population.getHhPool().get(h2ID).getResidentsID().iterator();
			while (itIndiv.hasNext()) {
				Integer indID = itIndiv.next();
				
				if (indID.equals(indiv2ID)) continue;
				
				if (Population.getIndivPool().get(indID).getHhRel().equals(HholdRelSP.U15Child)) {
					newResidents.add(indID);
					// removes indID from h2ID
					itIndiv.remove();
					//System.out.println("\tRemoved 1 resident from hhold " + h2ID + ", hhSize " + hhPool.get(h2ID).getResidentsID().size());
				} else if (Population.getIndivPool().get(indID).getHhRel().equals(HholdRelSP.Student) || Population.getIndivPool().get(indID).getHhRel().equals(HholdRelSP.O15Child)) {
					if (marryIDsByHhold.get(h2ID)!=null && !marryIDsByHhold.get(h2ID).contains(indID)) {
						newResidents.add(indID);
						// removes indID from h2ID
						itIndiv.remove();
						//System.out.println("\tRemoved 1 resident from hhold " + h2ID + ", hhSize " + hhPool.get(h2ID).getResidentsID().size());
					}
				}
			}
		}
		
		// removes indiv1ID from h1ID
		Population.getHhPool().get(h1ID).getResidentsID().remove(indiv1ID);
		// changes relationship of indiv1ID to Married
		Population.getIndivPool().get(indiv1ID).setHhRel(HholdRelSP.Married);
//		System.out.println("\n\tnewly wed " + indiv1ID + ", " + indivPool.get(indiv1ID).getHhRel().toString());

		// removes indiv2ID from h2ID
		Population.getHhPool().get(h2ID).getResidentsID().remove(indiv2ID);
		// changes relationship of indiv2ID to Married
		Population.getIndivPool().get(indiv2ID).setHhRel(HholdRelSP.Married);
//		System.out.println("\tnewly wed " + indiv2ID + ", " + indivPool.get(indiv2ID).getHhRel().toString());
		
		// constructs a new household
		Household newHhold = new Household(PopulationAnalytics.calculateNewHholdID(), HholdTypes.Unknown, newResidents, HardcodedData.unknown, HardcodedData.unknown);
		newHhold.assignAggreHholdType();

		// adds this household to hhPool
		Population.addHholdToPopulation(newHhold);
	}
	
	
	/**
	 * 
	 * @param marryIndivsByHhID
	 */
	private static void marryIndivsFromSameHhold(HashMap<Integer,ArrayList<Integer>> marryIndivsByHhID) {
		Integer hhID = ArrayHandler.toInt(marryIndivsByHhID.keySet())[0];
		ArrayList<Integer> marryIDs = marryIndivsByHhID.get(hhID);
		
		// if there is only 1 available marrying individual in this household
		if (marryIDs.size()<=1) {
			// empty the list of available marrying individuals of this household and updates marryIndivsByHhID.
			marryIndivsByHhID.put(hhID, new ArrayList<Integer>());
			return;
		}
		
		Integer marry1 = marryIDs.get(0);
		Integer marry2 = null;
		
		if (Population.getIndivPool().get(marry1).getHhRel().equals(HholdRelSP.Married) || Population.getIndivPool().get(marry1).getHhRel().equals(HholdRelSP.LoneParent) ||
				Population.getIndivPool().get(marry1).getHhRel().equals(HholdRelSP.U15Child) || Population.getIndivPool().get(marry1).getHhRel().equals(HholdRelSP.Student) || 
				Population.getIndivPool().get(marry1).getHhRel().equals(HholdRelSP.O15Child) || Population.getIndivPool().get(marry1).getHhRel().equals(HholdRelSP.Relative)) {
			for (Integer id : marryIDs) {
				if (id==marry1) continue;
				if (!Population.getIndivPool().get(id).getHhRel().equals(HholdRelSP.Married) && !Population.getIndivPool().get(id).getHhRel().equals(HholdRelSP.LoneParent) &&
						!Population.getIndivPool().get(id).getHhRel().equals(HholdRelSP.U15Child) && !Population.getIndivPool().get(id).getHhRel().equals(HholdRelSP.Student) && 
						!Population.getIndivPool().get(id).getHhRel().equals(HholdRelSP.O15Child) && !Population.getIndivPool().get(id).getHhRel().equals(HholdRelSP.Relative)) {
					marry2 = id;
					break;
				}
			}
			// if all individuals available for marriage in this household are relative to each other
			if (marry2==null) {
				// empty the list of available marrying individuals of this household and updates marryIndivsByHhID.
				marryIndivsByHhID.put(hhID, new ArrayList<Integer>());
				return;
			} 
		} else {
			marry2 = marryIDs.get(1);
		}
		
		// updates list of marrying individuals in marryIndivsByHhID
		marryIDs.remove(marry1);
		marryIDs.remove(marry2);
		marryIndivsByHhID.put(hhID, marryIDs);
		
		marryIndividuals(marry1, marry2, hhID, marryIndivsByHhID);
	}
	
	
	/**
	 * 
	 * @param indiv1ID
	 * @param indiv2ID
	 * @param hhID
	 * @param marryIDsByHhold
	 */
	private static void marryIndividuals(Integer indiv1ID, Integer indiv2ID, Integer hhID, HashMap<Integer,ArrayList<Integer>> marryIDsByHhold) {
		// creates an ArrayList of ID of individuals in the new household, starting with indiv1ID and indiv2ID
		ArrayList<Integer> newResidents = new ArrayList<Integer>();
		newResidents.add(indiv1ID);
		newResidents.add(indiv2ID);
		
		// if either indiv1ID or indiv2ID is a lone parent
		if (Population.getIndivPool().get(indiv1ID).getHhRel().equals(HholdRelSP.LoneParent) || Population.getIndivPool().get(indiv2ID).getHhRel().equals(HholdRelSP.LoneParent)) {
			// adds any children (U15Child, Student, O15Child) in hhID that will move to the new household AND removes these individuals from hhID.
			// Note that the relationship of these individuals don't change in the new household.
			Iterator<Integer> itIndiv = Population.getHhPool().get(hhID).getResidentsID().iterator();
			while (itIndiv.hasNext()) {
				Integer indID = itIndiv.next();
				
				if (indID.equals(indiv1ID) || indID.equals(indiv2ID)) continue;
				
				if (Population.getIndivPool().get(indID).getHhRel().equals(HholdRelSP.U15Child)) {
					newResidents.add(indID);
					// removes indID from hhID
					itIndiv.remove();
				} else if (Population.getIndivPool().get(indID).getHhRel().equals(HholdRelSP.Student) || Population.getIndivPool().get(indID).getHhRel().equals(HholdRelSP.O15Child)) {
					if (marryIDsByHhold.get(hhID)!=null && !marryIDsByHhold.get(hhID).contains(indID)) {
						newResidents.add(indID);
						// removes indID from hhID
						itIndiv.remove();
					}
				}
			}
		}
		
		// removes indiv1ID and indiv2ID from hhID
		Population.getHhPool().get(hhID).getResidentsID().remove(indiv1ID);
		Population.getHhPool().get(hhID).getResidentsID().remove(indiv2ID);
		
		// constructs a new household
		Household hhold = new Household(PopulationAnalytics.calculateNewHholdID(), HholdTypes.Unknown, newResidents, HardcodedData.unknown, HardcodedData.unknown);
		hhold.assignAggreHholdType();
		// adds this household to hhPool
		Population.addHholdToPopulation(hhold);
	}
	
	
	/**
	 * 
	 */
	public static void giveBirth() {
		System.out.println("\tStarts giveBirth()...");
		
		Iterator<Entry<Integer, Household>> itHhold = Population.getHhPool().entrySet().iterator();
		// for each household in the hhPool
		while (itHhold.hasNext()) {
			Map.Entry<Integer, Household> entry = itHhold.next();
			Integer hhID = entry.getKey();
			
			ArrayList<Individual> newbornsThisHhold = new ArrayList<Individual>();
			
			Iterator<Integer> itIndiv = Population.getHhPool().get(hhID).getResidentsID().iterator();
			while (itIndiv.hasNext()) {
				Integer indID = itIndiv.next();
				// if this household has Married female
				if (Population.getIndivPool().get(indID).getHhRel().equals(HholdRelSP.Married) && Population.getIndivPool().get(indID).getGender().equals(Genders._female)) {
					ArrayList<Integer> childIDs = Population.getHhPool().get(hhID).getIndivIDOfHHRel(HholdRelSP.U15Child);
					childIDs = Population.getHhPool().get(hhID).getIndivIDOfHHRel(HholdRelSP.Student, childIDs);
					childIDs = Population.getHhPool().get(hhID).getIndivIDOfHHRel(HholdRelSP.O15Child, childIDs);
					int nCrntChildren = childIDs.size();
					if (Population.getIndivPool().get(indID).isHavingBaby(nCrntChildren)) {
						// constructs a new individual with age 0, relationship U15Child, random gender,
						//Individual newborn = new Individual(PopulationAnalytics.calculateNewIndivID(), 0, 
						//		Genders.getGenderByValue(HardcodedData.random.nextInt(Genders.values().length)+1), HholdRelSP.U15Child, 0);
						
						Genders newBabyGender = null;
						double randGender = HardcodedData.random.nextDouble();
						if (randGender<=HardcodedData.femaleBabyChance) {
							newBabyGender = Genders._female;
						} else {
							newBabyGender = Genders._male;
						}
						Individual newborn = new Individual(PopulationAnalytics.calculateNewIndivID(), 0, newBabyGender, HholdRelSP.U15Child, 0);
						
						// if this newborn survives
						if (!newborn.isDead()) {
							// adds him or her to indivPool
							Population.addIndivToPopulation(newborn);
							// add him or her to the list of newborns to be added to this household later
							newbornsThisHhold.add(newborn);
						}
					}
				}
			}
			
			// for each newborn who survives
			for (Individual newborn : newbornsThisHhold) {
				// adds him or her to the resident list of this household
				Population.addIndivToHhold(hhID, newborn.getId());
			}
			
			// reclassifies the type of this household
			Population.getHhPool().get(hhID).assignAggreHholdType();
		}
		
		System.out.println("\tEnds giveBirth()...");
	}
}
