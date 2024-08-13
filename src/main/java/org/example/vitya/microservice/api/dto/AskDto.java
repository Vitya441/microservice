package org.example.vitya.microservice.api.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

// Чтобы дать ответ при удалении в ProjectController (@DeleteMapping)

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AskDto {

    Boolean answer;

    public static AskDto makeDefault(Boolean answer) {
        return builder()
                .answer(answer)
                .build();

    }
}
