DROP DATABASE IF EXISTS moviedb;
CREATE DATABASE moviedb;
USE moviedb;

/*
    id:varchar(10) (primary key)
    title:varchar(100) (required)
    year:integer (required)
    director:varchar(100) (required)
 */
CREATE TABLE movies
(
    id VARCHAR(10),
    title TEXT NOT NULL,
    year INTEGER NOT NULL,
    director VARCHAR(100) NOT NULL,
    FULLTEXT (title),
    CONSTRAINT MoviePrimaryKey PRIMARY KEY (id)
);

/*
    id:varchar(10) (primary key)
    name:varchar(100) (required)
    birthYear:integer
 */
CREATE TABLE stars
(
    id VARCHAR(10),
    name VARCHAR(100) NOT NULL,
    birthYear INTEGER,
    CONSTRAINT StarsPrimaryKey PRIMARY KEY (id)
);

/*
    starId:varchar(10) (referencing stars.id, required)
    movieId:varchar(10) (referencing movies.id, required)
 */
CREATE TABLE stars_in_movies
(
    starId VARCHAR(10) NOT NULL,
    movieId VARCHAR(10) NOT NULL,
    CONSTRAINT StarInMoviesForeignKeyStarId FOREIGN KEY (starId) REFERENCES stars(id),
    CONSTRAINT StarInMoviesForeignKeyMovieId FOREIGN KEY (movieId) REFERENCES movies(id),
    CONSTRAINT StarsInMoviesPrimaryKey PRIMARY KEY (starId, movieId)
);

/*
    id:integer (primary key)
    name:varchar(32) (required)
 */
CREATE TABLE genres
(
    id INTEGER AUTO_INCREMENT,
    name VARCHAR(32) NOT NULL,
    CONSTRAINT GenresPrimaryKey PRIMARY KEY (id)
);

/*
    genreId:integer (referencing genres.id, required)
    movieId:varchar(10) (referencing movies.id, required)
 */
CREATE TABLE genres_in_movies
(
    genreId INTEGER NOT NULL,
    movieId VARCHAR(10) NOT NULL,
    CONSTRAINT GenresInMoviesForeignKeyGenreId FOREIGN KEY (genreId) REFERENCES genres(id),
    CONSTRAINT GenresInMoviesForeignKeyMovieId FOREIGN KEY (movieId) REFERENCES movies(id),
    CONSTRAINT GenresInMoviesPrimaryKey PRIMARY KEY (genreId, movieId)
);

/*
    id:varchar(20), (primary key)
    firstName:varchar(50) (required)
    lastName:varchar(50) (required)
    expiration:date (required)
 */
CREATE TABLE creditcards
(
    id VARCHAR(20),
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    expiration DATE NOT NULL,
    CONSTRAINT CreditCardsPrimaryKey PRIMARY KEY (id)
);

/*
    id:integer (primary key)
    firstName:varchar(50) (required)
    lastName:varchar(50) (required)
    ccId:varchar(20) (referencing creditcards.id, required)
    address:varchar(200) (required)
    email:varchar(50) (required)
    password:varchar(20) (required)
 */
CREATE TABLE customers
(
    id INTEGER AUTO_INCREMENT,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    ccId VARCHAR(20) NOT NULL,
    address VARCHAR(200) NOT NULL,
    email VARCHAR(50) NOT NULL,
    password VARCHAR(20) NOT NULL,
    CONSTRAINT CustomersForeignKeyCcId FOREIGN KEY (ccId) REFERENCES creditcards(id),
    CONSTRAINT CustomersPrimaryKey PRIMARY KEY (id)
);

/*
    id:integer (primary key)
    customerId:integer (referencing customers.id, required)
    movieId:varchar(10) (referencing movies.id, required)
    saleDate:date (required)
 */
CREATE TABLE sales
(
    id INTEGER AUTO_INCREMENT,
    customerId INTEGER NOT NULL,
    movieId VARCHAR(10) NOT NULL,
    saleDate DATE NOT NULL,
    CONSTRAINT SalesForeignKeyCustomerId FOREIGN KEY (customerId) REFERENCES customers(id),
    CONSTRAINT SalesForeignKeyMovieId FOREIGN KEY (movieId) REFERENCES movies(id),
    CONSTRAINT SalesPrimaryKey PRIMARY KEY (id)
);

/*
    movieId:varchar(10) (referencing movies.id, required)
    rating:float (required)
    numVotes:integer (required)
 */
CREATE TABLE ratings
(
    movieId VARCHAR(10),
    rating FLOAT NOT NULL,
    numVotes INTEGER NOT NULL,
    CONSTRAINT RatingsForeignKeyMovieId FOREIGN KEY (movieId) REFERENCES movies(id),
    CONSTRAINT RatingsPrimaryKey PRIMARY KEY (movieId)
);

/*
    email:varchar(50) (required)
    password:varchar(20) (required)
    fullname:varchar(100)
 */
CREATE TABLE employees
(
    email VARCHAR(50),
    password VARCHAR(20) NOT NULL,
    fullname VARCHAR(100),
    CONSTRAINT EmployeesPrimaryKey PRIMARY KEY (email)
);
