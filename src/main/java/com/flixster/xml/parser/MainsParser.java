package com.flixster.xml.parser;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.flixster.xml.parser.entity.Film;

public class MainsParser extends DefaultHandler
{
    public static final String XML_FILE = "/home/ubuntu/mains243.xml";
    public static final String DIR_NAME = "dirn";
    public static final String FILM = "film";
    public static final String TITLE = "t";
    public static final String YEAR = "year";
    public static final String FILM_ID = "fid";
    public static final String CATEGORY = "cat";
    public static final int BATCH_SIZE = 50;

    private HashMap<String, String[]> genreCodeMap;
    private HashSet<Film> movieSet;
    private HashMap<String, String> genreIdMap;

    private String chars;
    private int counter, genresInMoviesCounter, moviesCounter;
    private Connection dbcon;
    private Film film;
    private int genreIdCounter;
    private PreparedStatement insertMoviesStatement, insertGenresStatement, insertGenresInMoviesStatement;
    
    public MainsParser()
    {
        genreCodeMap = new HashMap<>();
        movieSet = new HashSet<>();
        genreIdMap = new HashMap<>();
        counter = 1;
        genresInMoviesCounter = 0;
        moviesCounter = 0;
        
        genreCodeMap.put("act", new String[] { "Action" });
        genreCodeMap.put("actn", new String[] { "Action" });
        genreCodeMap.put("axtn", new String[] { "Action" });
        genreCodeMap.put("viol", new String[] { "Action" });
        genreCodeMap.put("romt actn", new String[] { "Action", "Comedy" });
        genreCodeMap.put("dram.actn", new String[] { "Action", "Drama" });
        genreCodeMap.put("kinky", new String[] { "Adult" });
        genreCodeMap.put("porn", new String[] { "Adult" });
        genreCodeMap.put("porb", new String[] { "Adult" });
        genreCodeMap.put("adct", new String[] { "Adventure" });
        genreCodeMap.put("adctx", new String[] { "Adventure" });
        genreCodeMap.put("advt", new String[] { "Adventure" });
        genreCodeMap.put("romtadvt", new String[] { "Adventure", "Romance" });
        genreCodeMap.put("allegory", new String[] { "Allegory" });
        genreCodeMap.put("cart", new String[] { "Animation" });
        genreCodeMap.put("anti-dram", new String[] { "Anti-Drama" });
        genreCodeMap.put("art video", new String[] { "Art Video" });
        genreCodeMap.put("avant garde", new String[] { "Avant Garde" });
        genreCodeMap.put("avga", new String[] { "Avant Garde" });
        genreCodeMap.put("bio", new String[] { "Biography" });
        genreCodeMap.put("biob", new String[] { "Biography" });
        genreCodeMap.put("biog", new String[] { "Biography" });
        genreCodeMap.put("biop", new String[] { "Biography" }); 
        genreCodeMap.put("biopp", new String[] { "Biography" }); 
        genreCodeMap.put("biopx", new String[] { "Biography" });
        genreCodeMap.put("comd", new String[] { "Comedy" }); 
        genreCodeMap.put("comd west", new String[] { "Comedy", "West" });
        genreCodeMap.put("comdx", new String[] { "Comedy" }); 
        genreCodeMap.put("cond", new String[] { "Comedy" });
        genreCodeMap.put("comd noir", new String[] { "Comedy", "Crime", "Drama" });
        genreCodeMap.put("noir comd", new String[] { "Comedy", "Crime", "Drama" });
        genreCodeMap.put("noir comd romt", new String[] { "Comedy", "Crime", "Drama", "Romance" });
        genreCodeMap.put("romt comd", new String[] { "Comedy", "Romance" });
        genreCodeMap.put("romt. comd", new String[] { "Comedy", "Romance" });
        genreCodeMap.put("cmr", new String[] { "Crime" });
        genreCodeMap.put("cnr", new String[] { "Crime" });
        genreCodeMap.put("cnrb", new String[] { "Crime" });
        genreCodeMap.put("cnrbb", new String[] { "Crime" });
        genreCodeMap.put("crim", new String[] { "Crime" });
        genreCodeMap.put("noir", new String[] { "Crime", "Drama" });
        genreCodeMap.put("disa", new String[] { "Disaster" });
        genreCodeMap.put("dicu", new String[] { "Documentary" });
        genreCodeMap.put("docu", new String[] { "Documentary" });
        genreCodeMap.put("duco", new String[] { "Documentary" });
        genreCodeMap.put("ducu", new String[] { "Documentary" });
        genreCodeMap.put("docu dram", new String[] { "Documentary", "Drama" });
        genreCodeMap.put("draam", new String[] { "Drama" });
        genreCodeMap.put("dram", new String[] { "Drama" });
        genreCodeMap.put("dram>", new String[] { "Drama" });
        genreCodeMap.put("drama", new String[] { "Drama" });
        genreCodeMap.put("dramd", new String[] { "Drama" });
        genreCodeMap.put("dramn", new String[] { "Drama" });
        genreCodeMap.put("psych dram", new String[] { "Drama", "Psychological" });
        genreCodeMap.put("romt dram", new String[] { "Drama", "Romance" });
        genreCodeMap.put("epic", new String[] { "Epic" });
        genreCodeMap.put("faml", new String[] { "Family" });
        genreCodeMap.put("fant", new String[] { "Fantasy" });
        genreCodeMap.put("fanth*", new String[] { "Fantasy" });
        genreCodeMap.put("romt fant", new String[] { "Fantasy", "Romance" });
        genreCodeMap.put("hist", new String[] { "History" });
        genreCodeMap.put("hor", new String[] { "Horror" });
        genreCodeMap.put("horr", new String[] { "Horror" });
        genreCodeMap.put("tvm", new String[] { "Miniseries - TV" });
        genreCodeMap.put("tvmini", new String[] { "Miniseries - TV" });
        genreCodeMap.put("musc", new String[] { "Musical" });
        genreCodeMap.put("muusc", new String[] { "Musical" });
        genreCodeMap.put("muscl", new String[] { "Musical" });
        genreCodeMap.put("stage musical", new String[] { "Musical" });
        genreCodeMap.put("myst", new String[] { "Mystery" });
        genreCodeMap.put("mystp", new String[] { "Mystery" });
        genreCodeMap.put("natu", new String[] { "Nature" });
        genreCodeMap.put("camp", new String[] { "Now - Camp" });
        genreCodeMap.put("psyc", new String[] { "Psychological" });
        genreCodeMap.put("road", new String[] { "Road" });
        genreCodeMap.put("romt", new String[] { "Romance" });
        genreCodeMap.put("ront", new String[] { "Romance" });
        genreCodeMap.put("romtx", new String[] { "Romance" }); 
        genreCodeMap.put("sati", new String[] { "Satire" });
        genreCodeMap.put("tv", new String[] { "Series - TV" });
        genreCodeMap.put("tvs", new String[] { "Series - TV" });
        genreCodeMap.put("s.f.", new String[] { "Sci-Fi" });
        genreCodeMap.put("scfi", new String[] { "Sci-Fi" });
        genreCodeMap.put("scif", new String[] { "Sci-Fi" });
        genreCodeMap.put("sxfi", new String[] { "Sci-Fi" });
        genreCodeMap.put("surl", new String[] { "Surreal" });
        genreCodeMap.put("surr", new String[] { "Surreal" });
        genreCodeMap.put("surreal", new String[] { "Surreal" });
        genreCodeMap.put("susp", new String[] { "Thriller" });
        genreCodeMap.put("weird", new String[] { "Weird" });
        genreCodeMap.put("west", new String[] { "Western" });
        genreCodeMap.put("west1", new String[] { "Western" });
    }

    public static void main(String[] argv)
    {
        MainsParser parser = new MainsParser();
        try
        {
            parser.parse();
        }
        catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
    }

    public void parse() throws SQLException 
    {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try
        {
            dbcon = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?rewriteBatchedStatements=true", "testuser", "122Baws@ICS");
            insertMoviesStatement = dbcon.prepareStatement("INSERT INTO movies VALUES (?, ?, ?, ?, FLOOR(1 + RAND() * (10 - 1 + 1)))");
            insertGenresStatement = dbcon.prepareStatement("INSERT INTO genres (name) values (?)");
            insertGenresInMoviesStatement = dbcon.prepareStatement("INSERT INTO genres_in_movies VALUES (?, ?)");
            dbcon.setAutoCommit(false);
            initMovieCache();
            initGenreIdCache();

            InputSource file = new InputSource(XML_FILE);
            file.setEncoding(StandardCharsets.ISO_8859_1.displayName());
            SAXParser sp = spf.newSAXParser();
            sp.parse(file, this);
            
            handleBatch();
            dbcon.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            insertMoviesStatement.close();
            insertGenresStatement.close();
            insertGenresInMoviesStatement.close();
            dbcon.close();
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (qName.equalsIgnoreCase(FILM))
        {
            film = new Film();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException
    {
        chars = new String(ch, start, length).trim();
    }

    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (qName.equalsIgnoreCase(FILM))
        {
            handleInsert();
        }
        else if (qName.equalsIgnoreCase(DIR_NAME))
        {
            film.setDirector(chars.trim());
        }
        else if (qName.equalsIgnoreCase(TITLE))
        {
            film.setTitle(chars.trim());
        }
        else if (qName.equalsIgnoreCase(YEAR))
        {
            try
            {
                Integer.parseInt(chars);
                film.setYear(chars);
            }
            catch (Exception e) {}
        }
        else if (qName.equalsIgnoreCase(FILM_ID))
        {
            film.setId(chars.trim());
        }
        else if (qName.equalsIgnoreCase(CATEGORY))
        {
            film.addGenre(chars.trim());
        }
    }

    private void handleInsert()
    {
        try
        {
            if (film.getDirector() != null && !film.getDirector().isEmpty() && !film.getDirector().startsWith("Unknown") && !film.getDirector().startsWith("Unyear"))
            {
                if (film.getYear() != null && !film.getYear().isEmpty())
                {
                    if (film.getId() != null && !film.getId().isEmpty())
                    {
                        insertMovie();
                    }
                    else
                    {
                        System.out.println(counter++ + " SCHEMA INCONSISTENCY: NULL OR EMPTY ID (" + film + ")");
                    }
                }
                else
                {
                    System.out.println(counter++ + " SCHEMA INCONSISTENCY: NULL OR EMPTY YEAR (" + film + ")");
                }
            }
            else
            {
                System.out.println(counter++ + " SCHEMA INCONSISTENCY: EMPTY OR UNKNOWN DIRECTOR (" + film + ")");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("ERR IN PARSING (" + film + ")");
        }
    }

    private void insertMovie() throws SQLException
    {
        if (film != null && film.getTitle() != null && !film.getTitle().isEmpty())
        {
            // If the id does not exist
            if (!checkMovieCache(film.getId()))
            {
                // No duplicate record exists under same year, title, and director
                boolean isDuplicate = movieSet
                    .stream()
                    .anyMatch(movie ->
                        movie.getTitle().equals(film.getTitle())
                            && movie.getYear().equals(film.getYear())
                            && movie.getDirector().equals(film.getDirector())
                );

                if (!isDuplicate)
                {
                    insertMoviesStatement.setString(1, film.getId());
                    insertMoviesStatement.setString(2, film.getTitle());
                    insertMoviesStatement.setString(3, film.getYear());
                    insertMoviesStatement.setString(4, film.getDirector());
                    insertMoviesStatement.addBatch();
                    addToMovieCache(film.getId(), film.getTitle(), film.getYear(), film.getDirector());
                    moviesCounter++;
                    insertMovieGenres();
                } 
                else 
                {
                    System.out.println("DUPLICATE MOVIE: (" + film + ")");
                }
            }
            else
            {
                System.out.println(counter++ + " SCHEMA INCONSISTENCY: DUPLICATE ID (" + film + ")");
            }
        }
        else
        {
            System.out.println(counter++ + " SCHEMA INCONSISTENCY: FILM TITLE CANNOT BE NULL OR EMPTY (" + film + ")");
        }
        
        if (moviesCounter >= BATCH_SIZE)
        {
            handleBatch();
        }
    }

    private void insertMovieGenres() throws SQLException
    {
        HashSet<String> movieGenres = new HashSet<>();
        for (String genre : film.getGenres())
        {
            String[] genreArray = genreCodeMap.get(genre.toLowerCase());
            if (genreArray != null)
            {
                for (String g : genreArray)
                {
                    String genreID = checkGenreCache(g);
                    
                    if (genreID == null) // Insert when the genre doesn't exist then set genre id to newly generated key
                    {
                        genreID = addGenreCache(g);
                        insertGenresStatement.setString(1, g);
                        insertGenresStatement.addBatch();
                    }

                    if (!movieGenres.contains(genreID))
                    {
                        movieGenres.add(genreID);
                        insertGenresInMoviesStatement.setString(1, genreID);
                        insertGenresInMoviesStatement.setString(2, film.getId());
                        insertGenresInMoviesStatement.addBatch();
                        genresInMoviesCounter++;
                    }
                }
            }
            else
            {
                System.out.println(counter++ + " SCHEMA INCONSISTENCY: DISCARD INVALID GENRE (" + genre + ") WHEN PARSING (" + FILM + ")");
            }
        }
        
        if (genresInMoviesCounter >= BATCH_SIZE)
        {
            handleBatch();
        }
    }

    public void handleBatch() throws SQLException 
    {
        //reset to 0 we want to execute whichever hits batch size first.
        genresInMoviesCounter = 0;
        moviesCounter = 0;

        //execute in order for foreign key deps.
        insertMoviesStatement.executeBatch();
        insertGenresStatement.executeBatch();
        insertGenresInMoviesStatement.executeBatch();
    }

    private void initMovieCache() throws SQLException
    {
        PreparedStatement movies = dbcon.prepareStatement("SELECT id, title, year, director FROM movies");
        ResultSet moviesRS = movies.executeQuery();

        while (moviesRS.next())
        {
            movieSet.add(new Film(
                moviesRS.getString("id"),
                moviesRS.getString("title"),
                String.valueOf(moviesRS.getInt("year")),
                moviesRS.getString("director")
            ));
        }
        
        moviesRS.close();
        movies.close();
    }

    private void initGenreIdCache() throws SQLException 
    {
        PreparedStatement genres = dbcon.prepareStatement("SELECT * FROM genres");
        ResultSet genresRS = genres.executeQuery();

        while (genresRS.next())
        {
            genreIdMap.put(genresRS.getString("name"), genresRS.getString("id"));
            genreIdCounter = Integer.parseInt(genresRS.getString("id"));
        }
        
        genresRS.close();
        genres.close();
    }
    
    private boolean checkMovieCache(String id)
    {
        return movieSet
            .stream()
            .anyMatch(movie -> movie.getId().equals(id));
    }

    private void addToMovieCache(String id, String title, String year, String director)
    {
        movieSet.add(new Film(id, title, year, director));
    }

    private String checkGenreCache(String genreName)
    {
        return genreIdMap.get(genreName);
    }

    private String addGenreCache(String genreName)
    {
        genreIdMap.put(genreName, String.valueOf(++genreIdCounter));
        return String.valueOf(genreIdCounter);
    }
}
