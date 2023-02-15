package openspp.keycloak.user.storage;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.mindrot.jbcrypt.BCrypt;

import lombok.extern.slf4j.Slf4j;
import openspp.keycloak.user.storage.util.PBKDF2SHA256HashingUtil;
import openspp.keycloak.user.storage.util.Paginator;
import openspp.keycloak.user.storage.util.Paginator.Pageable;

import javax.sql.DataSource;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

@Slf4j
public class UserRepository {

    private DataSourceProvider dataSourceProvider;
    private QueryConfigurations queryConfigurations;

    public UserRepository(DataSourceProvider dataSourceProvider, QueryConfigurations queryConfigurations) {
        this.dataSourceProvider = dataSourceProvider;
        this.queryConfigurations = queryConfigurations;
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
                    query = Paginator.getPagableQuery(query, pageable, queryConfigurations.getJDBCDriver());
                }
                log.info("Query: {} params: {} ", query, Arrays.toString(params));
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

    public List<Map<String, String>> findUsers(String search, Paginator.Pageable pageable) {
        if (search == null || search.isEmpty() || search.equals("*")) {
            return doQuery(queryConfigurations.getListAll(), pageable, this::readMap);
        }
        return doQuery(queryConfigurations.getFindBySearchTerm(), pageable, this::readMap, search);
    }

    public boolean validateCredentials(String username, String password) {
        String hash = Optional
                .ofNullable(doQuery(queryConfigurations.getFindPasswordHash(), this::readString, username))
                .orElse("");
        if (queryConfigurations.isPlainText()) {
            return Objects.equals(password, hash);
        } else if (queryConfigurations.isBlowfish()) {
            return !hash.isEmpty() && BCrypt.checkpw(password, hash);
        } else {
            String hashFunction = queryConfigurations.getHashingAlgorithm();

            if (queryConfigurations.isPBKDF2SHA256()) {
                String[] components = hash.split("\\$");
                return new PBKDF2SHA256HashingUtil(password, components[2], Integer.valueOf(components[1]))
                        .validatePassword(components[3]);
            }

            MessageDigest digest = DigestUtils.getDigest(hashFunction);
            byte[] pwdBytes = StringUtils.getBytesUtf8(password);
            return Objects.equals(Hex.encodeHexString(digest.digest(pwdBytes)), hash);
        }
    }

    public boolean updateCredentials(String username, String password) {
        throw new NotImplementedException("Password update not supported");
    }

    public boolean removeUser() {
        throw new NotImplementedException("Remove user not supported");
    }
}
