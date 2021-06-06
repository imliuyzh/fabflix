package com.flixster.xml.parser.entity;

public class Credit
{
    private String movieId, name;
    
    public Credit(String movieId, String name)
    {
        this.movieId = movieId;
        this.name = name;
    }
    
    public Credit()
    {
        this(null, null);
    }

    public String getMovieId()
    {
        return movieId;
    }

    public String getName()
    {
        return name;
    }
    
    public String toString()
    {
        return "movieId: " + movieId + " name: " + name;
    }

    public void setMovieId(String movieId)
    {
        this.movieId = movieId;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
