package openspp.keycloak.user.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.sql.DataSource;

import org.apache.commons.lang3.NotImplementedException;
import org.keycloak.models.KeycloakSession;

import lombok.extern.slf4j.Slf4j;
import openspp.keycloak.user.auth.beneficiary.oidc.BeneficiaryOIDCAuthenticatorForm;
import openspp.keycloak.user.storage.util.PBKDF2HashingUtil;
import openspp.keycloak.user.storage.util.Paginator;
import openspp.keycloak.user.storage.util.Paginator.Pageable;

@Slf4j
public class UserRepository {

    private DataSourceProvider dataSourceProvider;
    private QueryConfigurations queryConfigurations;
    private final KeycloakSession session;

    public UserRepository(KeycloakSession session, DataSourceProvider dataSourceProvider, QueryConfigurations queryConfigurations) {
        this.dataSourceProvider = dataSourceProvider;
        this.queryConfigurations = queryConfigurations;
        this.session = session;
    }

    private <T> T doQuery(String query, Function<ResultSet, T> resultTransformer, Object... params) {
        return this.doQuery(query, null, resultTransformer, params);
    }

    private <T> T doQuery(String query, Pageable pageable, Function<ResultSet, T> resultTransformer, Object... params) {
        Optional<DataSource> dataSourceOpt = dataSourceProvider.getDataSource();
        if (dataSourceOpt.isPresent()) {
            DataSource dataSource = dataSourceOpt.get();
            try (Connection c = dataSource.getConnection()) {
                if (pageable != null) {
                    query = Paginator.getPagableQuery(query, pageable, queryConfigurations.getJDBC());
                }
                log.debug("Query: {} params: {} ", query, Arrays.toString(params));
                try (PreparedStatement statement = c.prepareStatement(query)) {
                    if (params != null) {
                        int parameterCount = statement.getParameterMetaData().getParameterCount();
                        if (params.length == parameterCount) {
                            for (int i = 1; i <= params.length; i++) {
                                statement.setObject(i, params[i - 1]);
                            }
                        } else {
                            // Search query use only first parameter.
                            log.info("Search term with single param {}", Arrays.toString(params));
                            for (int i = 1; i <= parameterCount; i++) {
                                statement.setString(i, String.format("%%%s%%", String.valueOf(params[0])));
                            }
                        }
                    }
                    try (ResultSet rs = statement.executeQuery()) {
                        return resultTransformer.apply(rs);
                    }
                }
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
            return null;
        }
        return null;
    }

    private List<Map<String, String>> readMap(ResultSet rs) {
        try {
            List<Map<String, String>> data = new ArrayList<>();
            Set<String> columnLabels = new HashSet<>();
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                String columnLabel = rs.getMetaData().getColumnLabel(i);
                columnLabels.add(columnLabel);
            }
            while (rs.next()) {
                Map<String, String> result = new HashMap<>();
                for (String col : columnLabels) {
                    result.put(col, rs.getString(col));
                }
                data.add(result);
            }
            return data;
        } catch (Exception e) {
            throw new UserStorageException(e.getMessage(), e);
        }
    }

    private Integer readInt(ResultSet rs) {
        try {
            return rs.next() ? rs.getInt(1) : null;
        } catch (Exception e) {
            throw new UserStorageException(e.getMessage(), e);
        }
    }

    private Boolean readBoolean(ResultSet rs) {
        try {
            return rs.next() ? rs.getBoolean(1) : null;
        } catch (Exception e) {
            throw new UserStorageException(e.getMessage(), e);
        }
    }

    private String readString(ResultSet rs) {
        try {
            return rs.next() ? rs.getString(1) : null;
        } catch (Exception e) {
            throw new UserStorageException(e.getMessage(), e);
        }
    }

    public List<Map<String, String>> getAllUsers() {
        return doQuery(queryConfigurations.getListAll(), this::readMap);
    }

    public int getUsersCount() {
        return this.getUsersCount();
    }

    public int getUsersCount(String search) {
        if (search == null || search.isEmpty()) {
            return Optional.ofNullable(doQuery(queryConfigurations.getCount(), this::readInt)).orElse(0);
        } else {
            String query = String.format("SELECT COUNT(*) FROM (%s) COUNT", queryConfigurations.getFindBySearchTerm());
            return Optional.ofNullable(doQuery(query, this::readInt, search)).orElse(0);
        }
    }

    public Map<String, String> findUserById(String id) {
        return Optional
                .ofNullable(doQuery(queryConfigurations.getFindById(), this::readMap, Integer.parseInt(id)))
                .orElse(Collections.emptyList())
                .stream().findFirst().orElse(null);
    }

    public Optional<Map<String, String>> findUserByUsername(String username) {
        return Optional.ofNullable(doQuery(queryConfigurations.getFindByUsername(), this::readMap, username))
                .orElse(Collections.emptyList())
                .stream().findFirst();
    }

    public List<Map<String, String>> findUsersByBeneficiaryForm(String householdNumber, String uidNumber, String phoneNumber) {
        return doQuery(queryConfigurations.getFindByBeneficiaryForm(), this::readMap, householdNumber, uidNumber, phoneNumber);
    }

    public List<Map<String, String>> findUsers(String search, Paginator.Pageable pageable) {
        if (search == null || search.isEmpty() || search.equals("*")) {
            return doQuery(queryConfigurations.getListAll(), pageable, this::readMap);
        }
        return doQuery(queryConfigurations.getFindBySearchTerm(), pageable, this::readMap, search);
    }

    public boolean validateCredentials(String username, String password) throws Exception {
        String param = username;
        String query = queryConfigurations.getFindPasswordHash();
        String uid = session.getContext().getAuthenticationSession().getAuthNote(BeneficiaryOIDCAuthenticatorForm.FIELD_UID);

        // Use Unified ID number in the findPasswordHashAlt query if we are using beneficiary authenticator.
        if (uid != null && !uid.isEmpty()) {
            param = uid;
            query = queryConfigurations.getFindPasswordHashAlt();
        }
        
        String hash = Optional
                .ofNullable(doQuery(query, this::readString, param))
                .orElse("");
        return PBKDF2HashingUtil.validatePassword(password, hash);
    }

    public boolean updateCredentials(String username, String password) {
        throw new NotImplementedException("Password update not supported");
    }

    public boolean removeUser() {
        throw new NotImplementedException("Remove user not supported");
    }
}
