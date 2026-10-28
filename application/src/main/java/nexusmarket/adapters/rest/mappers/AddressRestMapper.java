package nexusmarket.adapters.rest.mappers;

import java.util.List;
import nexusmarket.adapters.rest.dtos.requests.AddressRequestDTO;
import nexusmarket.adapters.rest.dtos.responses.AddressResponseDTO;
import nexusmarket.domain.valueobjects.Address;

/**
 * Mapper between the Address value object and its REST DTOs. The domain validates the values.
 */
public final class AddressRestMapper {

    private AddressRestMapper() {
    }

    public static Address toDomain(AddressRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return new Address(dto.getLine1(), dto.getLine2(), dto.getCity(), dto.getState(), dto.getCountry());
    }

    public static AddressResponseDTO toResponse(Address address) {
        if (address == null) {
            return null;
        }
        AddressResponseDTO dto = new AddressResponseDTO();
        dto.setLine1(address.getLine1());
        dto.setLine2(address.getLine2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setCountry(address.getCountry());
        return dto;
    }

    public static List<AddressResponseDTO> toResponse(List<Address> addresses) {
        return addresses.stream().map(AddressRestMapper::toResponse).toList();
    }
}
