package nl.michelbijnen.ctf.challenges.dtos;

import lombok.Data;

@Data
public class UpdateChallengeDto {
    private String title;
    private String description;
    private int points;
    private String flag;
    private String category;
}
