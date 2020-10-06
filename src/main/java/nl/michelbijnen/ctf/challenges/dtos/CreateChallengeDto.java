package nl.michelbijnen.ctf.challenges.dtos;

import lombok.Data;

@Data
public class CreateChallengeDto {
    private String title;
    private String description;
    private String fileURL;
    private String flag;
    private int points;
}
