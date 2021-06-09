package com.flixster.xml.parser;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.flixster.xml.parser.entity.Star;

public class ActorsParser extends DefaultHandler
{
    public static final String XML_FILE = "actors63.xml";   // Change the file directory here
    public static final String ACTOR = "actor";
    public static final String NAME = "stagename";
    public static final String BIRTH_YEAR = "dob";
    public static final int BATCH_SIZE = 100;

    Connection dbcon;
    private HashSet<Star> starsSet;
    int insertionCounter, inconsistencyCounter;

    String chars, name, birthYear;

    PreparedStatement insertStarStatement;

    public ActorsParser()
    {
        starsSet = new HashSet<>();
        insertionCounter = 0;
        inconsistencyCounter = 1;
    }

    public static void main(String[] argv)
    {
        ActorsParser parser = new ActorsParser();
        try
        {
            parser.parse();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void parse() throws SQLException
    {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try
        {
            dbcon = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?rewriteBatchedStatements=true", "testuser", "122Baws@ICS");
            dbcon.setAutoCommit(false);
            insertStarStatement = dbcon.prepareStatement("INSERT INTO stars VALUES (SUBSTR(UUID(), 1, 10), ?, ?)");
            initStarsCache();
            
            InputSource file = new InputSource(XML_FILE);
            file.setEncoding(StandardCharsets.ISO_8859_1.displayName());
            SAXParser sp = spf.newSAXParser();
            sp.parse(file, this);
            
            insertStarStatement.executeBatch();
            dbcon.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            insertStarStatement.close();
            dbcon.close();
        }
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {}

    public void characters(char[] ch, int start, int length) throws SAXException
    {
        chars = new String(ch, start, length).trim();
    }

    public void endElement(String uri, String localName, String qName) 
    {
        if (qName.equalsIgnoreCase(ACTOR))
        {
            try
            {
                handleInsert();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        else if (qName.equalsIgnoreCase(NAME))
        {
            name = chars.trim();
        }
        else if (qName.equalsIgnoreCase(BIRTH_YEAR))
        {
            try
            {
                Integer.parseInt(chars.trim());
                birthYear = chars.trim();
            }
            catch (Exception e)
            {
                System.out.println(inconsistencyCounter++ + " SCHEMA INCONSISTENCY: INVALID BIRTH YEAR (" + chars + ") WHEN PARSING (" + name + ")");
                birthYear = null;
            }
        }
    }

    private void handleInsert() throws SQLException
    {
        if (name != null && !name.isEmpty())
        {
            Star newStar = new Star(name, birthYear);
            if (!checkStarCache(newStar))
            {
                insertStarStatement.setString(1, name);
                if (birthYear != null)
                {
                    insertStarStatement.setString(2, birthYear);
                }
                else
                {
                    insertStarStatement.setNull(2, java.sql.Types.INTEGER);
                }
                insertStarStatement.addBatch();
                
                addStarToCache(newStar);
                insertionCounter++;
            }
            else
            {
                System.out.println(inconsistencyCounter++ + " SCHEMA INCONSISTENCY: DUPLICATED ENTRY (" + newStar + ")");
            }
        }
        else
        {
            System.out.println(inconsistencyCounter++ + " SCHEMA INCONSISTENCY: STAGENAME CANNOT BE NULL OR EMPTY (Birth Year: " + birthYear + ")");
        }
        
        if (insertionCounter >= BATCH_SIZE)
        {
            insertStarStatement.executeBatch();
        }
    }

    private void initStarsCache() throws SQLException 
    {
        PreparedStatement statement = dbcon.prepareStatement("SELECT * FROM stars");
        ResultSet rs = statement.executeQuery();

        while (rs.next())
        {
            starsSet.add(new Star(rs.getString("name"), rs.getString("birthYear")));
        }
        
        rs.close();
        statement.close();
    }

    private void addStarToCache(Star star)
    {
        starsSet.add(star);
    }

    private boolean checkStarCache(Star star)
    {
        return starsSet.contains(star);
    }
}
