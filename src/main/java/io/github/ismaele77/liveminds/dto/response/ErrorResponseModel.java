package io.github.ismaele77.liveminds.dto.response;

import io.github.ismaele77.liveminds.dto.ErrorModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseModel {
    public String type;
    public List<ErrorModel> errors;
}
