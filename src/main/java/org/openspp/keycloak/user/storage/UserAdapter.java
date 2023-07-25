package org.openspp.keycloak.user.storage;

import java.util.ArrayList;
import java.util.HashSet;
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

    public static String PARTNER_ID_ATTRIBUTE = "partner_id";
    public static String FULL_NAME_ATTRIBUTE = "full_name";
    public static String FIRST_NAME_ATTRIBUTE = "first_name";
    public static String LAST_NAME_ATTRIBUTE = "last_name";
    public static String PHONE_ATTRIBUTE = "phone";
    public static String ID_TYPE_NAME_ATTRIBUTE = "id_type_name";
    public static String ID_TYPE_VALUE_ATTRIBUTE = "id_type_value";
    public static String KIND_NAME_ATTRIBUTE = "kind_name";
    public static String IS_GROUP_ATTRIBUTE = "is_group";
    public static String ACTIVE_GROUP_ATTRIBUTE = "active_group";

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
        return getFirstAttribute(PARTNER_ID_ATTRIBUTE);
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
        String fullName = getFirstAttribute(FULL_NAME_ATTRIBUTE);
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
        String firstName = getFirstAttribute(FIRST_NAME_ATTRIBUTE);
        if (firstName == null || firstName.isEmpty()) {
            List<String> nameParts = getNameParts();
            if (nameParts.size() > 0) {
                return nameParts.get(0);
            }
        }
        return null;
    }

    public String getLastName() {
        String lastName = getFirstAttribute(LAST_NAME_ATTRIBUTE);
        if (lastName == null || lastName.isEmpty()) {
            List<String> nameParts = getNameParts();
            if (nameParts.size() > 1) {
                return nameParts.get(1);
            }
        }
        return null;
    }

    public String getFullName() {
        String fullName = getFirstAttribute(FULL_NAME_ATTRIBUTE);
        if (fullName == null) {
            fullName = getFirstName() + " " + getLastName();
        }
        return fullName;
    }

    public String getPhoneNumber() {
        return getFirstAttribute(PHONE_ATTRIBUTE);
    }

    public String getTypeName() {
        return getFirstAttribute(ID_TYPE_NAME_ATTRIBUTE);
    }

    public String getTypeValue() {
        return getFirstAttribute(ID_TYPE_VALUE_ATTRIBUTE);
    }

    public String getKindName() {
        return getFirstAttribute(KIND_NAME_ATTRIBUTE);
    }

    public boolean isGroup() {
        String isGroupAttr = getFirstAttribute(IS_GROUP_ATTRIBUTE);
        return isGroupAttr != null && isGroupAttr.equals("t");
    }

    public String getActiveGroup() {
        return getFirstAttribute(ACTIVE_GROUP_ATTRIBUTE);
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

}
