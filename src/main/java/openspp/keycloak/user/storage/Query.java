package openspp.keycloak.user.storage;

public class Query {
    public final static String DATABASE = "spp_partner_oidc";

    public static String getCount() {
        String query = """
            SELECT COUNT(*) FROM %s
        """;
        return String.format(query, DATABASE);
    }

    public static String getListAll() {
        String query = """
            SELECT
                id,
                id AS partner_id,
                username,
                email,
                phone,
                first_name,
                last_name,
                full_name
            FROM %s
        """;
        return String.format(query, DATABASE);
    }

    public static String getFindById() {
        String query = """
            SELECT
                id,
                id AS partner_id,
                username,
                email,
                phone,
                first_name,
                last_name,
                full_name
            FROM %s
            WHERE
                \"id\" = ? 
        """;
        return String.format(query, DATABASE);
    }

    public static String getFindByUsername() {
        String query = """
            SELECT
                id,
                id AS partner_id,
                username,
                email,
                phone,
                first_name,
                last_name,
                full_name
            FROM %s
            WHERE
                \"username\" = ? 
        """;
        return String.format(query, DATABASE);
    }

    public static String getFindByBeneficiaryForm() {
        String query = """
            SELECT
                id,
                id AS partner_id,
                username,
                email,
                phone,
                first_name,
                last_name,
                full_name,
                is_group,
                kind_name,
                id_type_name,
                id_type_value
            FROM %s
            WHERE
                \"username\" = ? OR
                (\"id_type_name\" = 'Unified ID' AND \"id_type_value\" = ? AND \"phone\" = ?)
        """;
        return String.format(query, DATABASE);
    }

    public static String getFindBySearchTerm() {
        String query = """
            SELECT
                id,
                id AS partner_id,
                username,
                email,
                phone,
                first_name,
                last_name,
                full_name
            FROM %s
            WHERE
                \"username\" ILIKE (?) or \"email\" ILIKE (?) or \"full_name\" ILIKE (?)
        """;
        return String.format(query, DATABASE);
    }

    public static String getFindPasswordHash() {
        String query = """
            SELECT password FROM %s WHERE \"username\" = ? 
        """;
        return String.format(query, DATABASE);
    }

    public static String getFindPasswordHashAlt() {
        String query = """
            SELECT password FROM %s WHERE \"id_type_value\" = ? 
        """;
        return String.format(query, DATABASE);
    }
}