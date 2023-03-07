package openspp.keycloak.user.storage;

import lombok.Data;

@Data
public class QueryConfigurations {

    private String count;
    private String listAll;
    private String findById;
    private String findByUsername;
    private String findBySearchTerm;
    private String findPasswordHash;
    private JDBC JDBC;

    public QueryConfigurations(String count, String listAll, String findById, String findByUsername,
            String findBySearchTerm, String findPasswordHash, JDBC jdbc) {
        this.count = count;
        this.listAll = listAll;
        this.findById = findById;
        this.findByUsername = findByUsername;
        this.findBySearchTerm = findBySearchTerm;
        this.findPasswordHash = findPasswordHash;
        this.JDBC = jdbc;
    }
}
