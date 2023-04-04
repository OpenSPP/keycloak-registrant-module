package openspp.keycloak.user.storage;

import lombok.Data;

@Data
public class QueryConfigurations {

    private String count;
    private String listAll;
    private String findById;
    private String findByUsername;
    private String findByBeneficiaryForm;
    private String findBySearchTerm;
    private String findPasswordHash;
    private String findPasswordHashAlt;
    private JDBC JDBC;

    public QueryConfigurations(String count, String listAll, String findById, String findByUsername,
            String findByBeneficiaryForm, String findBySearchTerm, String findPasswordHash, String findPasswordHashAlt,
            JDBC jdbc) {
        this.count = count;
        this.listAll = listAll;
        this.findById = findById;
        this.findByUsername = findByUsername;
        this.findByBeneficiaryForm = findByBeneficiaryForm;
        this.findBySearchTerm = findBySearchTerm;
        this.findPasswordHash = findPasswordHash;
        this.findPasswordHashAlt = findPasswordHashAlt;
        this.JDBC = jdbc;
    }
}
