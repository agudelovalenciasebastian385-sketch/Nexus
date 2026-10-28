package nexusmarket.adapters.rest.mappers;

import nexusmarket.adapters.rest.dtos.responses.LoginResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.UserResponseDTO;
import nexusmarket.domain.models.AuthenticationResult;
import nexusmarket.domain.models.User;

/**
 * Mapper from users of any role and authentication results to REST DTOs.
 * The password hash and the token version are never copied.
 */
public final class UserRestMapper {

    private UserRestMapper() {
    }

    public static UserResponseDTO toResponse(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setIdentificationNumber(user.getIdentificationNumber());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().getCode());
        dto.setStatus(user.getStatus().getCode());
        return dto;
    }

    public static LoginResponseDTO toLoginResponse(AuthenticationResult result, long expiresInSeconds) {
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.setToken(result.getToken());
        dto.setExpiresIn(expiresInSeconds);
        dto.setUser(toResponse(result.getAuthenticatedUser()));
        return dto;
    }
}
