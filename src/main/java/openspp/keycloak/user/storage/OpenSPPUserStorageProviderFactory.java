package openspp.keycloak.user.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import com.google.auto.service.AutoService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@AutoService(UserStorageProviderFactory.class)
public class OpenSPPUserStorageProviderFactory implements UserStorageProviderFactory<OpenSPPUserStorageProvider> {

    public static final String id = "openspp";
    private static final String PARAMETER_PLACEHOLDER_HELP = " Use '?' as parameter placeholder character (replaced only once). ";
    private static final String DEFAULT_HELP_TEXT = "Select to query all view_oidc you must return at least: \"id\". "
            +
            "            \"username\"," +
            "            \"email\" (optional)," +
            "            \"name\" (optional). Any other parameter can be mapped by aliases to a realm scope.";
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
        JDBC jdbc = JDBC.getByDescription(JDBC.POSTGRESQL.getDesc());
        providerConfig.dataSourceProvider.configure(url, jdbc, user, password, model.getName());
        providerConfig.queryConfigurations = new QueryConfigurations(
                model.get("count"),
                model.get("listAll"),
                model.get("findById"),
                model.get("findByUsername"),
                model.get("findByPDSForm"),
                model.get("findBySearchTerm"),
                model.get("findPasswordHash"),
                model.get("findPasswordHashAlt"),
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
                .defaultValue("jdbc:postgresql://172.17.0.1:15432/devel")
                .add();

        pcBuilder.property()
                .name("user")
                .label("JDBC Connection User")
                .helpText("JDBC Connection User")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("odoo")
                .add();

        pcBuilder.property()
                .name("password")
                .label("JDBC Connection Password")
                .helpText("JDBC Connection Password")
                .type(ProviderConfigProperty.PASSWORD)
                .defaultValue("odoopassword")
                .add();

        // Queries

        pcBuilder.property()
                .name("count")
                .label("User count SQL query")
                .helpText("SQL query returning the total count of view_oidc")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("SELECT COUNT(*) FROM view_oidc")
                .add();

        pcBuilder.property()
                .name("listAll")
                .label("List All Users SQL query")
                .helpText(DEFAULT_HELP_TEXT)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("SELECT id, id AS partner_id, username, email, phone, first_name FROM view_oidc")
                .add();

        pcBuilder.property()
                .name("findById")
                .label("Find user by id SQL query")
                .helpText(DEFAULT_HELP_TEXT + String.format(PARAMETER_HELP, "user id")
                        + PARAMETER_PLACEHOLDER_HELP)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue(
                        "SELECT id, id AS partner_id, username, email, phone, first_name FROM view_oidc WHERE \"id\" = ? ")
                .add();

        pcBuilder.property()
                .name("findByUsername")
                .label("Find user by username SQL query")
                .helpText(
                        DEFAULT_HELP_TEXT + String.format(PARAMETER_HELP, "user username")
                                + PARAMETER_PLACEHOLDER_HELP)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue(
                        "SELECT id, id AS partner_id, username, email, phone, first_name FROM view_oidc WHERE \"username\" = ? ")
                .add();

        pcBuilder.property()
                .name("findByPDSForm")
                .label("Find user by PDS form SQL query")
                .helpText(
                        DEFAULT_HELP_TEXT + String.format(PARAMETER_HELP, "PDS, UID, Family numbers")
                                + PARAMETER_PLACEHOLDER_HELP)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue(
                        "SELECT id, id AS partner_id, username, email, phone, first_name, is_group, kind_name, type_name, type_value FROM view_oidc WHERE (\"type_name\" = 'PDS' AND \"type_value\" = ?) OR (\"type_name\" = 'Unified ID' AND \"type_value\" = ? AND \"phone\" = ?) OR \"username\" = ? ")
                .add();

        pcBuilder.property()
                .name("findBySearchTerm")
                .label("Find user by search term SQL query")
                .helpText(DEFAULT_HELP_TEXT + String.format(PARAMETER_HELP, "search term")
                        + PARAMETER_PLACEHOLDER_HELP)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue(
                        "SELECT id, id AS partner_id, username, email, phone, first_name FROM view_oidc WHERE \"username\" ILIKE (?) or \"email\" ILIKE (?) or \"first_name\" ILIKE (?)")
                .add();

        pcBuilder.property()
                .name("findPasswordHash")
                .label("Find password hash PBKDF2 SQL query")
                .helpText(
                        DEFAULT_HELP_TEXT + String.format(PARAMETER_HELP, "user username")
                                + PARAMETER_PLACEHOLDER_HELP)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("SELECT password FROM view_oidc WHERE \"username\" = ? ")
                .add();

        pcBuilder.property()
                .name("findPasswordHashAlt")
                .label("Find password hash PBKDF2 SQL alt query")
                .helpText(
                        DEFAULT_HELP_TEXT + String.format(PARAMETER_HELP, "user alternative attribute")
                                + PARAMETER_PLACEHOLDER_HELP)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("SELECT password FROM view_oidc WHERE \"type_value\" = ? ")
                .add();

        return pcBuilder.build();
    }

    private static class ProviderConfig {
        private DataSourceProvider dataSourceProvider = new DataSourceProvider();
        private QueryConfigurations queryConfigurations;
    }

}
