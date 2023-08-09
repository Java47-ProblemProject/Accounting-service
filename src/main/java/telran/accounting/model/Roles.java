package telran.accounting.model;

import java.util.HashSet;
import java.util.Set;

public enum Roles {
    USER,
    MODERATOR,
    ADMINISTRATOR;

    public static Set<Roles> convertFromDto(Set<String> roles) {
        Set<Roles> ERoles = new HashSet<>();
        for (String role : roles) {
            try {
                ERoles.add(Roles.valueOf(role));
            } catch (IllegalArgumentException ignored) {

            }
        }
        return ERoles;
    }
}
