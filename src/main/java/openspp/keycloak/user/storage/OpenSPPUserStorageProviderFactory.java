package openspp.keycloak.user.storage;

import com.google.auto.service.AutoService;

import lombok.extern.slf4j.Slf4j;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@AutoService(UserStorageProviderFactory.class)
public class OpenSPPUserStorageProviderFactory implements UserStorageProviderFactory<OpenSPPUserStorageProvider> {

    public static final String id = "openspp";
    private static final String PARAMETER_PLACEHOLDER_HELP = " Use '?' as parameter placeholder character (replaced only once). ";
    private static final String DEFAULT_HELP_TEXT = "Select to query all users you must return at least: \"id\". " +
            "            \"username\"," +
            "            \"email\" (optional)," +
            "            \"firstName\" (optional)," +
            "            \"lastName\" (optional). Any other parameter can be mapped by aliases to a realm scope.";
    private static final String PARAMETER_HELP = " The %s is passed as query parameter.";

    private Map<String, ProviderConfig> providerConfigPerInstance = new HashMap<>();

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void close() {
        for (Map.Entry<String, ProviderConfig> pc : providerConfigPerInstance.entrySet()) {
            pc.getValue().dataSourceProvider.close();
        }
    }

    @Override
    public OpenSPPUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        ProviderConfig providerConfig = providerConfigPerInstance.computeIfAbsent(model.getId(),
                s -> configure(model));
        return new OpenSPPUserStorageProvider(session, model, providerConfig.dataSourceProvider,
                providerConfig.queryConfigurations);
    }

    private synchronized ProviderConfig configure(ComponentModel model) {
        log.info("Creating configuration for model: id={} name={}", model.getId(), model.getName());
        ProviderConfig providerConfig = new ProviderConfig();
        String user = model.get("user");
        String password = model.get("password");
        String url = model.get("url");
        JDBC jdbc = JDBC.getByDescription(model.get("jdbc"));
        providerConfig.dataSourceProvider.configure(url, jdbc, user, password, model.getName());
        providerConfig.queryConfigurations = new QueryConfigurations(
                model.get("count"),
                model.get("listAll"),
                model.get("findById"),
                model.get("findByUsername"),
                model.get("findBySearchTerm"),
                model.get("findPasswordHash"),
                model.get("hashingAlgorithm"),
                jdbc);
        return providerConfig;
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel model)
            throws ComponentValidationException {
        try {
            ProviderConfig old = providerConfigPerInstance.put(model.getId(), configure(model));
            if (old != null) {
                old.dataSourceProvider.close();
            }
        } catch (Exception e) {
            throw new ComponentValidationException(e.getMessage(), e);
        }
    }

    @Override
    public String getId() {
        return OpenSPPUserStorageProviderFactory.id;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigurationBuilder pcBuilder = ProviderConfigurationBuilder.create();

        // Database

        pcBuilder.property()
                .name("url")
                .label("JDBC URL")
                .helpText("JDBC Connection String")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("jdbc:postgresql://server-name:server-port/database_name")
                .add();

        pcBuilder.property()
                .name("user")
                .label("JDBC Connection User")
                .helpText("JDBC Connection User")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("user")
                .add();

        pcBuilder.property()
                .name("password")
                .label("JDBC Connection Password")
                .helpText("JDBC Connection Password")
                .type(ProviderConfigProperty.PASSWORD)
                .defaultValue("password")
                .add();

        pcBuilder.property()
                .name("jdbc")
                .label("JDBC")
                .helpText("Relational Database Management System")
                .type(ProviderConfigProperty.LIST_TYPE)
                .options(JDBC.getAllDescriptions())
                .defaultValue(JDBC.POSTGRESQL.getDesc())
                .add();

        // Queries

        pcBuilder.property()
                .name("count")
                .label("User count SQL query")
                .helpText("SQL query returning the total count of users")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("SELECT COUNT(*) FROM users")
                .add();

        pcBuilder.property()
                .name("listAll")
                .label("List All Users SQL query")
                .helpText(DEFAULT_HELP_TEXT)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("SELECT \"id\", \"username\", \"email\", \"firstName\", \"lastName\" FROM users")
                .add();

        pcBuilder.property()
                .name("findById")
                .label("Find user by id SQL query")
                .helpText(DEFAULT_HELP_TEXT + String.format(PARAMETER_HELP, "user id")
                        + PARAMETER_PLACEHOLDER_HELP)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue(
                        "SELECT \"id\", \"username\", \"email\", \"firstName\", \"lastName\" FROM users WHERE \"id\" = ? ")
                .add();

        pcBuilder.property()
                .name("findByUsername")
                .label("Find user by username SQL query")
                .helpText(
                        DEFAULT_HELP_TEXT + String.format(PARAMETER_HELP, "user username")
                                + PARAMETER_PLACEHOLDER_HELP)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue(
                        "SELECT \"id\", \"username\", \"email\", \"firstName\", \"lastName\" FROM users WHERE \"username\" = ? ")
                .add();

        pcBuilder.property()
                .name("findBySearchTerm")
                .label("Find user by search term SQL query")
                .helpText(DEFAULT_HELP_TEXT + String.format(PARAMETER_HELP, "search term")
                        + PARAMETER_PLACEHOLDER_HELP)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue(
                        "SELECT \"id\", \"username\", \"email\", \"firstName\", \"lastName\" FROM users WHERE \"username\" ILIKE (?) or \"email\" ILIKE (?) or \"firstName\" ILIKE (?) or \"lastName\" ILIKE (?)")
                .add();

        pcBuilder.property()
                .name("findPasswordHash")
                .label("Find password hash (blowfish or hash digest hex) SQL query")
                .helpText(
                        DEFAULT_HELP_TEXT + String.format(PARAMETER_HELP, "user username")
                                + PARAMETER_PLACEHOLDER_HELP)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("SELECT password FROM users WHERE \"username\" = ? ")
                .add();

        // Password hashing algorithms
        pcBuilder.property()
                .name("hashingAlgorithm")
                .label("Password hashing algorithm")
                .helpText("Hash type used to match password (md* or sha* uses hex hash digest)")
                .type(ProviderConfigProperty.LIST_TYPE)
                .options("Blowfish (bcrypt)", "MD2", "MD5", "SHA-1", "SHA-256", "SHA3-224", "SHA3-256",
                        "SHA3-384",
                        "SHA3-512", "SHA-384", "SHA-512/224", "SHA-512/256", "SHA-512",
                        "PBKDF2-SHA256", "INSECURE PLAINTEXT")
                .defaultValue("SHA-1")
                .add();

        return pcBuilder.build();
    }

    private static class ProviderConfig {
        private DataSourceProvider dataSourceProvider = new DataSourceProvider();
        private QueryConfigurations queryConfigurations;
    }

}
