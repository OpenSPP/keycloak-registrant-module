package org.openspp.keycloak.user.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

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
        JDBC jdbc = JDBC.getByDescription(JDBC.POSTGRESQL.getDesc());
        providerConfig.dataSourceProvider.configure(
            model.get("url"),
            jdbc,
            model.get("user"),
            model.get("password"),
            model.getName(),
            model.get("minIdle", 5),
            model.get("poolSize", 50),
            model.get("connectionTimeout", SECONDS.toMillis(30)),
            model.get("idleTimeout", MINUTES.toMillis(10)),
            model.get("lifeTime", MINUTES.toMillis(30))
        );
        providerConfig.queryConfigurations = new QueryConfigurations(
                Query.getCount(),
                Query.getListAll(),
                Query.getFindById(),
                Query.getFindByUsername(),
                Query.getFindByBeneficiaryForm(),
                Query.getFindBySearchTerm(),
                Query.getFindPasswordHash(),
                Query.getFindPasswordHashAlt(),
                jdbc
        );
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

        pcBuilder.property()
                .name("minIdle")
                .label("Minimum Idle Connections")
                .helpText("The property controls the minimum number of idle connections that HikariCP tries to maintain in the pool, including both idle and in-use connections.")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue(5)
                .add();

        pcBuilder.property()
                .name("poolSize")
                .label("Max Pool Size")
                .helpText("The property controls the maximum number of connections that HikariCP will keep in the pool, including both idle and in-use connections.")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue(50)
                .add();

        pcBuilder.property()
                .name("connectionTimeout")
                .label("Connection Timeout")
                .helpText("The maximum number of milliseconds that a client will wait for a connection from the pool.")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue(SECONDS.toMillis(30))
                .add();

        pcBuilder.property()
                .name("idleTimeout")
                .label("Idle Timeout")
                .helpText("This property controls the maximum amount of time (in milliseconds) that a connection is allowed to sit idle in the pool.")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue(MINUTES.toMillis(10))
                .add();

        pcBuilder.property()
                .name("lifeTime")
                .label("Max Lifetime")
                .helpText("This property controls the maximum lifetime of a connection in the pool.")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue(MINUTES.toMillis(30))
                .add();

        return pcBuilder.build();
    }

    private static class ProviderConfig {
        private DataSourceProvider dataSourceProvider = new DataSourceProvider();
        private QueryConfigurations queryConfigurations;
    }

}
