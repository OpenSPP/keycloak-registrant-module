package openspp.keycloak.user.storage;

public class QueryConfigurations {

    private String count;
    private String listAll;
    private String findById;
    private String findByUsername;
    private String findBySearchTerm;
    private String findPasswordHash;
    private String hashingAlgorithm;
    private JDBC JDBC;

    public QueryConfigurations(String count, String listAll, String findById, String findByUsername,
            String findBySearchTerm, String findPasswordHash, String hashingAlgorithm, JDBC JDBC) {
        this.count = count;
        this.listAll = listAll;
        this.findById = findById;
        this.findByUsername = findByUsername;
        this.findBySearchTerm = findBySearchTerm;
        this.findPasswordHash = findPasswordHash;
        this.hashingAlgorithm = hashingAlgorithm;
        this.JDBC = JDBC;
    }

    public JDBC getJDBCDriver() {
        return JDBC;
    }

    public String getCount() {
        return count;
    }

    public String getListAll() {
        return listAll;
    }

    public String getFindById() {
        return findById;
    }

    public String getFindByUsername() {
        return findByUsername;
    }

    public String getFindBySearchTerm() {
        return findBySearchTerm;
    }

    public String getFindPasswordHash() {
        return findPasswordHash;
    }

    public String getHashingAlgorithm() {
        return hashingAlgorithm;
    }

    public boolean isBlowfish() {
        return hashingAlgorithm.toLowerCase().contains("blowfish");
    }

    public boolean isPBKDF2SHA256() {
        return hashingAlgorithm.toLowerCase().contains("pbkdf2-sha256");
    }

    public boolean isPlainText() {
        return hashingAlgorithm.toLowerCase().contains("plaintext");
    }
}
