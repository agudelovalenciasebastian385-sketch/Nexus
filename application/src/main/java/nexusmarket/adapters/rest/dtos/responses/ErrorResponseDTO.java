package nexusmarket.adapters.rest.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard error body of every failed request (SDD/Adapters/Global-exception-handler.md).
 *
 * <p>{@code errors} lists the invalid fields and is present only for validation errors.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {

    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<FieldErrorDTO> errors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldErrorDTO {
        private String field;
        private String message;
    }
}
