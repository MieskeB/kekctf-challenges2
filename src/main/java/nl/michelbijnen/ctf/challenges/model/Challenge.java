package nl.michelbijnen.ctf.challenges.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.common.aliasing.qual.Unique;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Challenge {
    @Id
    private String id;
    @Unique
    private String title;
    private String description;
    private String fileURL;
    private String flag;
    private int points;
}
