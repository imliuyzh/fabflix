package com.flixster.xml.parser.entity;

import java.util.Objects;

public class StarInMovie 
{
    private String starId, movieId;

    public StarInMovie(String starId, String movieId)
    {
        this.starId = starId;
        this.movieId = movieId;
    }

    public void setMovieId(String movieId) 
    {
        this.movieId = movieId;
    }

    public String getMovieId() 
    {
        return movieId;
    }

    public String getStarId() 
    {
        return starId;
    }

    public void setStarId(String starId) 
    {
        this.starId = starId;
    }

    public boolean equals(Object o) 
    {
        if (this == o) 
        {
            return true;
        }
        
        if (o == null || getClass() != o.getClass()) 
        {   
            return false;
        }
        
        StarInMovie that = (StarInMovie) o;
        return Objects.equals(starId, that.starId) && Objects.equals(movieId, that.movieId);
    }

    public int hashCode() 
    {
        return Objects.hash(starId, movieId);
    }
}
