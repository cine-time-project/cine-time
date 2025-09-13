package com.cinetime.entity.business;

import com.cinetime.entity.enums.MovieStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min=3, max=100)
    @Column(nullable = false, length = 100)
    private String title;

    @NotNull
    @Size(min = 5, max = 20)
    @Column(nullable = false,unique = true,length = 20)
    private String slug;

    @NotNull
    @Size(min = 3, max = 300)
    private String summary;

    @NotNull
    private Date releaseDate;

    @NotNull
    private Integer duration;

    @Column(nullable = true)
    private Double rating;

    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private LocalDateTime updatedAt;

    @Column(nullable = true)
    private String specialHalls;

    @Column(nullable = true)
    @Size(min = 5, max = 20)
    private String director;

    @NotNull
    @ElementCollection
    @CollectionTable(
            name = "movie_cast",
            joinColumns = @JoinColumn(name = "movie_id")
    )
    @Column(name = "actor", nullable = false)
    private List<String> cast = new ArrayList<>();

    @NotNull
    @ElementCollection
    @CollectionTable(
            name="movie_format",
            joinColumns = @JoinColumn(name="movie_id")
    )
    @Column(name = "format", nullable = false)
    private List<String> formats = new ArrayList<>();

    @NotNull
    private String genre;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(32) default 'COMING_SOON'")
    private MovieStatus status = MovieStatus.COMING_SOON;

    //Burayi chatGPT den yardim alarak yaptim. Eger status de default olarak deger vermek istiyorsak enum kullanmamiz lazimmis
    //MovieStatus adinda bi enum olusturup sirasiyla comingsoon, in theaters, presela degerlerini atadim
    //Enum type ordinal oldugu zaman enum class ina ilk yazdigimiz degeri 0 kabul ederek deger atamasi yapiyor
    //Default status coming 0 oldugu icin enum daki 0 in karsiligi coming soon u verdik.

    @ManyToMany(mappedBy = "movies", fetch = FetchType.LAZY)
    private Set<Cinema> cinemas = new LinkedHashSet<>();


}
