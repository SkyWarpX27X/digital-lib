package ua.fictionallibrary.digital_lib.user;

import jakarta.persistence.*;

import java.time.Year;
import java.util.UUID;

@Entity
@Table(name = "library_cards")
public class LibraryCard {
    @Id
    private UUID ownerId;
    @Column(nullable = false)
    String email;
    @Column(nullable = false, length = 12)
    String postCode;
    @Column(nullable = false)
    Year birthYear;
    @Column(nullable = false)
    String livingAddress;
    @Column
    String workOrStudyAddress;
    @Column
    String organisation;
    @Column
    String workPosition;
    @OneToOne
    @JoinColumn(name = "owner_id")
    @MapsId
    private User user;

    public LibraryCard(UUID ownerId, String email, String postCode, Year birthYear, String livingAddress, String workOrStudyAddress, String organisation, String workPosition, User user) {
        this.ownerId = ownerId;
        this.email = email;
        this.postCode = postCode;
        this.birthYear = birthYear;
        this.livingAddress = livingAddress;
        this.workOrStudyAddress = workOrStudyAddress;
        this.organisation = organisation;
        this.workPosition = workPosition;
        this.user = user;
    }

    protected LibraryCard() {}

    public String toString() {
        String maskedEmail = email.replaceAll("(^.).*(@.*$)", "$1***$2");
        String maskedPostCode = postCode.replaceAll("\\S", "*");
        String maskedLivingAddress = livingAddress.split(" ")[0] + "*****";
        String workAddress = workOrStudyAddress == null ? "" : ", адреса роботи/навчання: " + workOrStudyAddress.split(" ")[0] + "*****";
        String organisationInfo = organisation == null ? "" : ", організація: " + organisation;
        String workPositionInfo = workPosition == null ? "" : ", посада: " + workPosition;
        return "Id власника: "+ownerId+", email: "+maskedEmail+", індекс: "+maskedPostCode+", рік народження: "+birthYear
                +", адреса проживання: "+maskedLivingAddress+workAddress+organisationInfo+workPositionInfo;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID id) {
        this.ownerId = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public Year getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(Year birthYear) {
        this.birthYear = birthYear;
    }

    public String getLivingAddress() {
        return livingAddress;
    }

    public void setLivingAddress(String livingAddress) {
        this.livingAddress = livingAddress;
    }

    public String getWorkOrStudyAddress() {
        return workOrStudyAddress;
    }

    public void setWorkOrStudyAddress(String workOrStudyAddress) {
        this.workOrStudyAddress = workOrStudyAddress;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getWorkPosition() {
        return workPosition;
    }

    public void setWorkPosition(String workPosition) {
        this.workPosition = workPosition;
    }

}
