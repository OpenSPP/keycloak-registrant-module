package openspp.keycloak.user.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserAdapter extends AbstractUserAdapterFederatedStorage {

    private final String keycloakId;
    private String username;
    private List<String> nameParts;

    public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, Map<String, String> data) {
        super(session, realm, model);
        this.keycloakId = StorageId.keycloakId(model, data.get("id"));
        this.username = data.get("username");
        try {
            for (Entry<String, String> e : data.entrySet()) {
                Set<String> newValues = new HashSet<>();
                newValues.add(StringUtils.trimToNull(e.getValue()));
                this.setAttribute(e.getKey(), newValues.stream().filter(Objects::nonNull).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), "UserAdapter, username={}", this.username);
        }
    }

    @Override
    public String getId() {
        return keycloakId;
    }

    public String getPartnerId() {
        return getFirstAttribute("partner_id");
    }

    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Parse first name and last name from full name format using by Odoo.
     * 
     * @return List<String>
     */
    private List<String> getNameParts() {
        if (nameParts != null) {
            return nameParts;
        }
        nameParts = new ArrayList<>();
        String fullName = getFirstAttribute("full_name");
        if (fullName != null) {
            String token = null;
            if (fullName.contains(",")) {
                token = ",";
            } else if (fullName.contains(" ")) {
                token = " ";
            }

            if (token != null) {
                nameParts.add(fullName.substring(0, fullName.lastIndexOf(token)).trim());
                nameParts.add(fullName.substring(fullName.lastIndexOf(token)).replace(token, "").trim());
            } else {
                nameParts.add(fullName);
            }
        }
        return nameParts;
    }

    public String getFirstName() {
        String firstName = getFirstAttribute("first_name");
        if (firstName == null || firstName.isEmpty()) {
            List<String> nameParts = getNameParts();
            if (nameParts.size() > 0) {
                return nameParts.get(0);
            }
        }
        return null;
    }

    public String getLastName() {
        String lastName = getFirstAttribute("last_name");
        if (lastName == null || lastName.isEmpty()) {
            List<String> nameParts = getNameParts();
            if (nameParts.size() > 1) {
                return nameParts.get(1);
            }
        }
        return null;
    }

    public String getFullName() {
        String fullName = getFirstAttribute("full_name");
        if (fullName == null) {
            fullName = getFirstName() + " " + getLastName();
        }
        return fullName;
    }

    public String getPhoneNumber() {
        return getFirstAttribute("phone");
    }

    public String getTypeName() {
        return getFirstAttribute("id_type_name");
    }

    public String getTypeValue() {
        return getFirstAttribute("id_type_value");
    }

    public String getKindName() {
        return getFirstAttribute("kind_name");
    }

    public boolean isGroup() {
        return getFirstAttribute("is_group").equals("t");
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

}
