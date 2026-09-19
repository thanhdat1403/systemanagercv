package systemanagercv.example.systemanagercv.cv.dto.response;

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
public class CVLanguageResponse {

    private Long id;

    private String languageCode;

    private String languageName;

    private String proficiency;

    private Integer sortOrder;
}