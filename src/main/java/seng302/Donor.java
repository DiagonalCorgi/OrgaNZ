package seng302;

import seng302.Utilities.BloodType;
import seng302.Utilities.Gender;
import seng302.Utilities.Organ;
import seng302.Utilities.OrganAlreadyRegisteredException;
import seng302.Utilities.Region;

import static java.util.Optional.ofNullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The main donor class.
 *
 *@author Dylan Carlyle, Jack Steel
 *@version sprint 1.
 *date 05/03/2018
 */

public class Donor {

    private final LocalDateTime created_on;
    private LocalDateTime modified_on;

    private String firstName;
    private String lastName;
    private String middleName;

    private String currentAddress;
    private Region region;

    private Gender gender;
    private BloodType bloodType;

    private double height;
    private double weight;
    private double BMI;

    private LocalDate dateOfBirth;
    private LocalDate dateOfDeath;
    private int age;

    private Map<Organ, Boolean> organStatus;

    private ArrayList<String> updateLog = new ArrayList<>();

    private int uid;

    public Donor() {
        created_on = LocalDateTime.now();
    }

    /**
     * Create a new donor object
     * @param firstName First name string
     * @param middleName Middle name(s). May be null
     * @param lastName Last name string
     * @param dateOfBirth LocalDate formatted date of birth
     * @param uid A unique user ID. Should be queried to ensure uniqueness
     */
    public Donor(String firstName, String middleName, String lastName, LocalDate dateOfBirth, int uid) {
        created_on = LocalDateTime.now();

        this.uid = uid;

        gender = Gender.UNSPECIFIED;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;

        initOrgans();
    }

    private void initOrgans() {
        organStatus = new HashMap<>();
        for (Organ o: Organ.values()) {
            organStatus.put(o, false);
        }
    }

    private void addUpdate(String function) {
        LocalDateTime timestamp = LocalDateTime.now();
        updateLog.add(String.format("%s; updated %s", timestamp, function));
        modified_on = LocalDateTime.now();
    }

    /**
     * Set a single organs donation status
     * @param organ The organ to be set
     * @param value Boolean value to set the status too
     * @throws OrganAlreadyRegisteredException Thrown if the organ is set to true when it already is
     */
    public void setOrganStatus(Organ organ, boolean value) throws OrganAlreadyRegisteredException {
        if (value && organStatus.get(organ)) {
            throw new OrganAlreadyRegisteredException(organ.toString() + " is already registered for donation");
        }
        addUpdate(organ.toString());
        organStatus.replace(organ, value);
    }

    public Map<Organ, Boolean> getOrganStatus() {
        return organStatus;
    }

    /**
     * Get the donors organ donation status, with a formatted string listing the organs to be donated
     * @return A formatted string listing the organs to be donated
     */
    public String getDonorOrganStatusString() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Organ, Boolean> entry : organStatus.entrySet()) {
            if (entry.getValue()) {
                if (builder.length() != 0) {
                    builder.append(", ");
                }
                builder.append(entry.getKey().toString());
            }
        }
        if (builder.length() == 0) {
            return String.format("User: %s. Name: %s %s %s, no organs registered for donation", uid, firstName, ofNullable(middleName).orElse(""), lastName);
        } else {
            return String.format("User: %s. Name: %s %s %s, Donation status: %s", uid, firstName, ofNullable(middleName).orElse(""), lastName, builder.toString());
        }
    }

    /**
     * Returns a preformatted string of the users change history
     * @return Formatted string with newlines
     */
    public String getUpdatesString() {
        StringBuilder out = new StringBuilder(String.format("User: %s. Name: %s %s %s, updates:\n", uid, firstName, ofNullable(middleName).orElse(""), lastName));
        for (String update : updateLog) {
            out.append(update).append('\n');
        }
        return out.toString();
    }

    /**
     * Get a formatted string with the donors user information. Does not include organ donation status
     * @return Formatted string with the donors user information. Does not include organ donation status
     */
    public String getDonorInfoString() {
        return String.format("User: %s. Name: %s %s %s, date of birth: %tF, date of death: %tF, gender: %s," +
                        " height: %scm, weight: %skg, blood type: %s, current address: %s, region: %s," +
                        " created on: %s, modified on: %s",
                uid, firstName, ofNullable(middleName).orElse(""), lastName, dateOfBirth, dateOfDeath, gender,
                height, weight, bloodType, currentAddress, region, created_on, modified_on);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        addUpdate("firstName");
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        addUpdate("lastName");
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        addUpdate("middleNames");
        this.middleName = middleName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        addUpdate("dateOfBirth");
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDate getDateOfDeath() {
        return dateOfDeath;
    }

    public void setDateOfDeath(LocalDate dateOfDeath) {
        addUpdate("dateOfDeath");
        this.dateOfDeath = dateOfDeath;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        addUpdate("gender");
        this.gender = gender;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        addUpdate("height");
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        addUpdate("weight");
        this.weight = weight;
    }

    public BloodType getBloodType() {
        return bloodType;
    }

    public void setBloodType(BloodType bloodType) {
        addUpdate("bloodType");
        this.bloodType = bloodType;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(String currentAddress) {
        addUpdate("currentAddress");
        this.currentAddress = currentAddress;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        addUpdate("region");
        this.region = region;
    }

    public int getUid() {
        return uid;
    }

    public LocalDateTime getCreationdate() {
		return created_on;
	}

	public LocalDateTime getModified_on() {
		return modified_on;
	}

	/**
	 * Calculates the BMI of the Donor based off their height and weight - BMI = weight/height^2.
	 * If either field is 0, the result returned is 0.
	 * @return the users calculated BMI.
	 */
	public double getBMI() {
		if (weight == 0 || height == 0) {
			BMI = 0;
		} else {
			BMI = weight / (height * 0.01 * height * 0.01);
		}
		return BMI;
	}

	/**
	 * Calculates the users age based on their date of birth and date of death. If the date of death is null the age
	 * is calculated base of the LocalDate.now().
	 * @return age of the Donor.
	 */
	public int getAge() {
    	if (dateOfDeath == null) {
			age = Period.between(dateOfBirth, LocalDate.now()).getYears();
		} else {
    		age = Period.between(dateOfBirth, dateOfDeath).getYears();
		}
		return age;
	}

    /**
     * Takes a string and checks if each space separated string section matches one of the names
     * @param searchParam The string to be checked
     * @return True if all sections of the passed string match any of the names of the donor
     */
	public boolean nameContains(String searchParam) {
	    String lowerSearch = searchParam.toLowerCase();
	    String[] splitSearchItems = lowerSearch.split("\\s+");

	    boolean isMatch = true;
	    for (String string : splitSearchItems) {
            if (!firstName.toLowerCase().contains(string) && (middleName == null || !middleName.toLowerCase().contains(string)) && !lastName.toLowerCase().contains(string)) {
	            isMatch = false;
	            break;
            }
        }

	    return isMatch;
    }

    /**
     * Donor objects are identified by their uid
     *
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Donor))
            return false;
        Donor d = (Donor) obj;
        return d.uid == this.uid;
    }
}