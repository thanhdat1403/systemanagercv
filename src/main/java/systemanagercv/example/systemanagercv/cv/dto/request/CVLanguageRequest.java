package systemanagercv.example.systemanagercv.cv.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVLanguageRequest {

    @NotBlank(message = "{cv.language.code.required}")
    @Size(max = 10, message = "{cv.language.code.max}")
    private String languageCode;

    @NotBlank(message = "{cv.language.name.required}")
    @Size(max = 100, message = "{cv.language.name.max}")
    private String languageName;

    @Size(max = 50, message = "{cv.language.proficiency.max}")
    private String proficiency;

    private Integer sortOrder = 0;
}