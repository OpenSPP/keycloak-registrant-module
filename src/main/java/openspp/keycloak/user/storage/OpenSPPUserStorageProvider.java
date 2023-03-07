package openspp.keycloak.user.storage;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.cache.CachedUserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;

import lombok.extern.slf4j.Slf4j;
import openspp.keycloak.user.storage.util.Paginator;

@Slf4j
public class OpenSPPUserStorageProvider implements UserStorageProvider,
        UserLookupProvider, UserQueryProvider, CredentialInputUpdater, CredentialInputValidator,
        UserRegistrationProvider {

    private final KeycloakSession session;
    private final ComponentModel model;
    private final UserRepository repository;

    OpenSPPUserStorageProvider(KeycloakSession session, ComponentModel model, DataSourceProvider dataSourceProvider,
            QueryConfigurations queryConfigurations) {
        this.session = session;
        this.model = model;
        this.repository = new UserRepository(dataSourceProvider, queryConfigurations);
    }

    private Stream<UserModel> toUserModelStream(RealmModel realm, List<Map<String, String>> users) {
        return users.stream()
                .map(m -> new UserAdapter(session, realm, model, m));
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        log.info("isValid user credential: userId={}", user.getId());

        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }

        UserCredentialModel cred = (UserCredentialModel) input;

        UserModel dbUser = user;
        // If the cache just got loaded in the last 500 millisec (i.e. probably part of
        // the actual flow), there is no point in reloading the user.)
        if (user instanceof CachedUserModel
                && (System.currentTimeMillis() - ((CachedUserModel) user).getCacheTimestamp()) > 500) {
            dbUser = this.getUserById(realm, user.getId());

            if (dbUser == null) {
                ((CachedUserModel) user).invalidate();
                return false;
            }

            // For now, we'll just invalidate the cache if username or email has changed.
            // Eventually we could check all (or a parametered list of) attributes fetched
            // from the DB.
            if (!java.util.Objects.equals(user.getUsername(), dbUser.getUsername())
                    || !java.util.Objects.equals(user.getEmail(), dbUser.getEmail())) {
                ((CachedUserModel) user).invalidate();
            }
        }
        try {
            return repository.validateCredentials(dbUser.getUsername(), cred.getChallengeResponse());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {

        log.info("updating credential: realm={} user={}", realm.getId(), user.getUsername());

        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }

        UserCredentialModel cred = (UserCredentialModel) input;
        return repository.updateCredentials(user.getUsername(), cred.getChallengeResponse());
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
    }

    @Override
    public void preRemove(RealmModel realm) {
        log.info("pre-remove realm");
    }

    @Override
    public void preRemove(RealmModel realm, GroupModel group) {
        log.info("pre-remove group");
    }

    @Override
    public void preRemove(RealmModel realm, RoleModel role) {
        log.info("pre-remove role");
    }

    @Override
    public void close() {
        log.debug("closing");
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        log.info("lookup user by id: realm={} userId={}", realm.getId(), id);

        String externalId = StorageId.externalId(id);
        Map<String, String> user = repository.findUserById(externalId);

        if (user == null) {
            log.debug("findUserById returned null, skipping creation of UserAdapter, expect login error");
            return null;
        } else {
            return new UserAdapter(session, realm, model, user);
        }
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        log.info("lookup user by username: realm={} username={}", realm.getId(), username);

        return repository.findUserByUsername(username).map(u -> new UserAdapter(session, realm, model, u)).orElse(null);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        log.info("lookup user by username: realm={} email={}", realm.getId(), email);

        return getUserByUsername(realm, email);
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        return repository.getUsersCount();
    }

    @Override
    public int getUsersCount(RealmModel realm, Set<String> groupIds) {
        return repository.getUsersCount();
    }

    @Override
    public int getUsersCount(RealmModel realm, String search) {
        return repository.getUsersCount(search);
    }

    @Override
    public int getUsersCount(RealmModel realm, String search, Set<String> groupIds) {
        return repository.getUsersCount(search);
    }

    @Override
    public int getUsersCount(RealmModel realm, Map<String, String> params) {
        return repository.getUsersCount();
    }

    @Override
    public int getUsersCount(RealmModel realm, Map<String, String> params, Set<String> groupIds) {
        return repository.getUsersCount();
    }

    @Override
    public int getUsersCount(RealmModel realm, boolean includeServiceAccount) {
        return repository.getUsersCount();
    }

    @Override
    public UserModel addUser(RealmModel realm, String username) {
        return null;
    }

    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {
        return false;
    }

    @Override
    public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user) {
        return Stream.empty();
    }

    private Stream<UserModel> internalSearchForUser(RealmModel realm, String search, Paginator.Pageable pageable) {
        return toUserModelStream(realm, repository.findUsers(search, pageable));
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult,
            Integer maxResults) {
        return internalSearchForUser(realm, search, new Paginator.Pageable(firstResult, maxResults));
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult,
            Integer maxResults) {
        return internalSearchForUser(realm, params.values().stream().findFirst().orElse(null),
                new Paginator.Pageable(firstResult, maxResults));
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult,
            Integer maxResults) {
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
        return Stream.empty();
    }
}
