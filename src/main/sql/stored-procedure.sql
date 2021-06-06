USE moviedb;
DROP PROCEDURE IF EXISTS add_movie;

DELIMITER $$

CREATE PROCEDURE add_movie (
    IN movieTitle VARCHAR(100), 
    IN movieYear INTEGER, 
    IN movieDirector VARCHAR(100), 
    IN starName VARCHAR(100), 
    IN starBirthYear INTEGER, 
    IN genreName VARCHAR(32)
)
BEGIN
    -- Insert the movie
    SET @movieId := SUBSTR(UUID(), 1, 10);
    INSERT INTO movies VALUES (@movieId, movieTitle, movieYear, movieDirector, FLOOR(1 + RAND() * (10 - 1 + 1)));
    
    -- Insert the star
    IF starBirthYear IS NULL THEN
        SET @starId := (SELECT id FROM stars WHERE UPPER(name) = UPPER(starName) LIMIT 1);
    ELSE
        SET @starId := (SELECT id FROM stars WHERE UPPER(name) = UPPER(starName) AND birthYear = starBirthYear LIMIT 1);
    END IF;
    
    IF @starId IS NULL THEN
        SET @starId := SUBSTR(UUID(), 1, 10);
        IF starBirthYear IS NULL THEN
            INSERT INTO stars (id, name, birthYear) VALUES (@starId, starName, NULL);
        ELSE
            INSERT INTO stars (id, name, birthYear) VALUES (@starId, starName, starBirthYear);
        END IF;
    END IF;
    
    INSERT INTO stars_in_movies VALUES (@starId, @movieId);
    
    -- Insert the genre
    SET @genreId := (SELECT G.id FROM genres G WHERE UPPER(G.name) = UPPER(genreName));
    IF @genreId IS NULL THEN
        INSERT INTO genres (name) VALUES (genreName);
        SET @genreId := (SELECT G.id FROM genres G WHERE G.name = genreName);
    END IF;
    INSERT INTO genres_in_movies VALUES (@genreId, @movieId);
END
$$

DELIMITER ;
