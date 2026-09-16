package systemanagercv.example.systemanagercv.cv.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CvVersionStatus {

    DRAFT("Bản nháp"),

    PENDING_TECH_LEAD("Chờ Tech Lead duyệt"),

    TECH_LEAD_APPROVED("Tech Lead đã duyệt"),

    TECH_LEAD_REJECTED("Tech Lead từ chối"),

    PENDING_HR("Chờ HR duyệt"),

    HR_APPROVED("HR đã duyệt"),

    HR_REJECTED("HR từ chối"),

    OFFICIAL("Chính thức"),

    ARCHIVED("Đã lưu trữ");

    private final String description;
}

/*Workflow:
*                          ┌─────────────────────┐
                         │       DRAFT         │
                         │      Bản nháp       │
                         └──────────┬──────────┘
                                    │
                                    ▼
                  ┌──────────────────────────────┐
                  │     PENDING_TECH_LEAD        │
                  │      Chờ Tech Lead duyệt     │
                  └──────────────┬───────────────┘
                                 │
                    ┌────────────┴─────────────┐
                    │                          │
                    ▼                          ▼
          TECH_LEAD_REJECTED          TECH_LEAD_APPROVED
                    │                          │
                    │                          ▼
                    │                ┌─────────────────┐
                    │                │   PENDING_HR    │
                    │                │    Chờ HR duyệt  │
                    │                └────────┬────────┘
                    │                         │
                    │              ┌──────────┴─────────┐
                    │              │                    │
                    │              ▼                    ▼
                    │        HR_REJECTED           HR_APPROVED
                    │              │                    │
                    │              │                    ▼
                    │              │              ┌──────────┐
                    │              │              │ OFFICIAL │
                    │              │              │ Chính thức│
                    │              │              └────┬─────┘
                    │              │                   │
                    │              │                   ▼
                    │              │              ARCHIVED
                    │              │
                    └──────────────┴──→ DRAFT*/