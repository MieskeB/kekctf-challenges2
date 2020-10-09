package nl.michelbijnen.ctf.challenges.dtos;

import lombok.Data;

@Data
public class ChallengeDto {
    private String id;
    private String title;
    private String description;
    private String category;
    private String fileURL;
    private int points;
    private boolean solved;
}
