package nexusmarket.domain.models;

import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.UserStatus;

/**
 * LogisticsOperator.
 *
 * <p>Represents the user responsible for the physical operation of warehouses
 * and dispatches. Handles the operational lifecycle of {@link Shipment}
 * instances and participates in managing the {@link Inventory} of the
 * warehouses it operates.</p>
 *
 * <p>No additional attributes beyond those inherited from {@link User}; its
 * role is always {@link SystemRole#LOGISTICS_OPERATOR}.</p>
 */
public class LogisticsOperator extends User {

    public LogisticsOperator(String id, String identificationNumber, String fullName, String email, String password,
                             UserStatus status) {
        super(id, identificationNumber, fullName, email, password, SystemRole.LOGISTICS_OPERATOR, status);
    }

    @Override
    public String toString() {
        return "LogisticsOperator{id='" + getId() + "', fullName='" + getFullName() + "', email='" + getEmail()
                + "', status=" + getStatus() + "}";
    }
}
