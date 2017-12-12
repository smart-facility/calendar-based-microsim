package core.SyntheticPopulation.Features;

import core.HardcodedData.HholdRelSP;
import core.HardcodedData.HholdTypes;
import core.SyntheticPopulation.Household;
import core.SyntheticPopulation.Population;
import core.HardcodedData.*;

public class Dwellings {
	
	static final int nMaxBedrooms = 4;
	
	public static void evaluateDwellingNeeds() {
		for (Household hhold : Population.getHhPool().values()) {
			// if this household is of type NF or HF16, the number of bedrooms needed equals the number of residents
			if (hhold.getHhType().equals(HholdTypes.NF) || hhold.getHhType().equals(HholdTypes.HF16)) {
				int nBedroomsNeeded = Math.min(hhold.getResidentsID().size(), nMaxBedrooms);
				hhold.setnBedroomsNeeded(nBedroomsNeeded);
			} 
			else if (hhold.getHhType().equals(HholdTypes.HF1)) {
				int nBedroomsNeeded = Math.min(hhold.getResidentsID().size()-1, nMaxBedrooms);
				hhold.setnBedroomsNeeded(nBedroomsNeeded);
			} 
			else {
				int nBedroomsNeeded = 1;
				
				int nChildrenU5 = countNonParentIndivsBetweenAge(0, 5, hhold);
				if (nChildrenU5>0) {
					nBedroomsNeeded = nBedroomsNeeded + (int)((nChildrenU5-1)/2) + 1;
				}
									
				int nMale6_18 = countNonParentIndivsBetweenAge(6, 18, hhold, Genders._male);
				if (nMale6_18>0) {
					nBedroomsNeeded = nBedroomsNeeded + (int)((nMale6_18-1)/2) + 1;
				}
				
				int nFemale6_18 = countNonParentIndivsBetweenAge(6, 18, hhold, Genders._female);
				if (nFemale6_18>0) {
					nBedroomsNeeded = nBedroomsNeeded + (int)((nFemale6_18-1)/2) + 1;
				}
				
				int nIndivO19 = countNonParentIndivsBetweenAge(19, 200, hhold);
				nBedroomsNeeded = nBedroomsNeeded + nIndivO19;
				
				nBedroomsNeeded = Math.min(nBedroomsNeeded, nMaxBedrooms);

				hhold.setnBedroomsNeeded(nBedroomsNeeded);
			}
		}
	}
	
	private static int countNonParentIndivsBetweenAge(int minAge, int maxAge, Household hhold) {
		int indivCount = 0;
		for (Integer indivID : hhold.getResidentsID()) {
			if (Population.getIndivPool().get(indivID).getHhRel().equals(HholdRelSP.Married) ||
					Population.getIndivPool().get(indivID).getHhRel().equals(HholdRelSP.LoneParent)) {
				continue;
			}
			if (Population.getIndivPool().get(indivID).getAge()>=minAge && Population.getIndivPool().get(indivID).getAge()<=maxAge) {
				indivCount += 1;
			}
		}
		
		return indivCount;
	}
	
	private static int countNonParentIndivsBetweenAge(int minAge, int maxAge, Household hhold, Genders gender) {
		int indivCount = 0;
		
		for (Integer indivID : hhold.getResidentsID()) {
			if (Population.getIndivPool().get(indivID).getHhRel().equals(HholdRelSP.Married) ||
					Population.getIndivPool().get(indivID).getHhRel().equals(HholdRelSP.LoneParent)) {
				continue;
			}
			if (Population.getIndivPool().get(indivID).getAge()>=minAge && Population.getIndivPool().get(indivID).getAge()<=maxAge && Population.getIndivPool().get(indivID).getGender().equals(gender)) {
				indivCount += 1;
			}
		}
		
		return indivCount;
	}
}
