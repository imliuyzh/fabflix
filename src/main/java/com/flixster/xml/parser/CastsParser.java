package com.flixster.xml.parser;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashSet;
import java.util.UUID;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.flixster.xml.parser.entity.Credit;
import com.flixster.xml.parser.entity.Film;
import com.flixster.xml.parser.entity.Star;
import com.flixster.xml.parser.entity.StarInMovie;

public class CastsParser extends DefaultHandler
{
    public static final String XML_FILE = "/home/ubuntu/casts124.xml";
    public static final String MOVIE = "m";
    public static final String FILM_ID = "f";
    public static final String ACTOR = "a";

    Connection dbcon;
    HashSet<Film> movieSet;
    HashSet<Star> starSet;
    HashSet<StarInMovie> starInMovieSet;
    PreparedStatement insertStarStatement, insertStarInMovieStatement;

    Credit credit;
    String chars;
    int inconsistencyCounter;

    public CastsParser()
    {
        movieSet = new HashSet<>();
        starSet = new HashSet<>();
        starInMovieSet = new HashSet<>();
        inconsistencyCounter = 1;
    }

    public static void main(String[] argv)
    {
        CastsParser parser = new CastsParser();
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
            dbcon.setAutoCommit(false);
            insertStarStatement = dbcon.prepareStatement("INSERT INTO stars VALUES (?, ?, null)");
            insertStarInMovieStatement = dbcon.prepareStatement("INSERT INTO stars_in_movies VALUES (?, ?)");
            initializeMoviesCache();
            initializeStarsCache();
            initializeStarsInMoviesCache();
            
            InputSource file = new InputSource(XML_FILE);
            file.setEncoding(StandardCharsets.ISO_8859_1.displayName());
            SAXParser sp = spf.newSAXParser();
            sp.parse(file, this);
            
            insertStarStatement.executeBatch();
            insertStarInMovieStatement.executeBatch();
            dbcon.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            insertStarStatement.close();
            insertStarInMovieStatement.close();
            dbcon.close();
        }
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (qName.equalsIgnoreCase(MOVIE))
        {
            credit = new Credit();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException
    {
        chars = new String(ch, start, length).trim();
    }

    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (qName.equalsIgnoreCase(MOVIE))
        {
            try
            {
                handleInsert();
            }
            catch (SQLException throwables)
            {
                throwables.printStackTrace();
            }
        }
        else if (qName.equalsIgnoreCase(FILM_ID))
        {
            credit.setMovieId(chars);
        }
        else if (qName.equalsIgnoreCase(ACTOR))
        {
            credit.setName(chars);
        }
    }

    private void handleInsert() throws SQLException
    {
        if (credit.getMovieId() != null && credit.getName() != null && !credit.getMovieId().isEmpty() && !credit.getName().isEmpty())
        {
            if (checkMovieCacheByFid(credit.getMovieId()))
            {
                Star star = checkStarsCacheByName(credit.getName());
                String starId = null;

                if (star == null)
                {
                    Star newStar = new Star(UUID.randomUUID().toString().substring(0, 10), credit.getName(), null);
                    starId = newStar.getId();

                    insertStarStatement.setString(1, newStar.getId());
                    insertStarStatement.setString(2, newStar.getName());
                    insertStarStatement.addBatch();
                    addStarToStarsCache(newStar);
                }
                else
                {
                    starId = star.getId();
                }
                
                // Check for duplicate star in movie to avoid foreign key constraint violations
                if (!checkStarsInMoviesCache(new StarInMovie(starId, credit.getMovieId())))
                {
                    insertStarInMovieStatement.setString(1, starId);
                    insertStarInMovieStatement.setString(2, credit.getMovieId());
                    insertStarInMovieStatement.addBatch();
                    
                    addStarsInMovieCache(new StarInMovie(starId, credit.getMovieId()));
                }
                else
                {
                    System.out.println(inconsistencyCounter++ + " SCHEMA INCONSISTENCY: DUPLICATED STAR AND MOVIE PAIR (" + credit + ")");
                }
            }
            else
            {
                System.out.println(inconsistencyCounter++ + " SCHEMA INCONSISTENCY: MISSING MOVIE ID (" + credit + ")");
            }
        }
        else
        {
            System.out.println(inconsistencyCounter++ + " SCHEMA INCONSISTENCY: NULL OR EMPTY ID AND NAME (" + credit + ")");
        }
    }

    private void initializeMoviesCache() throws SQLException
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

    private boolean checkMovieCacheByFid(String fid)
    {
        return movieSet
            .stream()
            .anyMatch(f -> f.getId().equals(fid));
    }

    private void initializeStarsCache() throws SQLException
    {
        PreparedStatement statement = dbcon.prepareStatement("SELECT * FROM stars");
        ResultSet rs = statement.executeQuery();

        while (rs.next())
        {
            starSet.add(new Star(rs.getString("id"), rs.getString("name"), rs.getString("birthYear")));
        }
        
        rs.close();
        statement.close();
    }

    private Star checkStarsCacheByName(String name)
    {
        return starSet
            .stream()
            .filter(s -> s.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    private void addStarToStarsCache(Star star)
    {
        starSet.add(star);
    }

    private void initializeStarsInMoviesCache() throws SQLException
    {
        PreparedStatement statement = dbcon.prepareStatement("SELECT * FROM stars_in_movies");
        ResultSet rs = statement.executeQuery();

        while (rs.next())
        {
            starInMovieSet.add(new StarInMovie(rs.getString("starId"), rs.getString("movieId")));
        }
        
        rs.close();
        statement.close();
    }

    private boolean checkStarsInMoviesCache(StarInMovie sim)
    {
        return starInMovieSet.contains(sim);
    }

    private void addStarsInMovieCache(StarInMovie sim)
    {
        starInMovieSet.add(sim);
    }
}
