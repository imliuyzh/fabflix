package com.flixster.xml.parser.entity;

import java.util.ArrayList;
import java.util.Objects;

public class Film
{
    private String id, title, year, director;
    private ArrayList<String> genres;

    public Film(String id, String title, String year, String director)
    {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.genres = new ArrayList<>();
    }

    public Film()
    {
        this(null, null, null, null);
    }

    public void setDirector(String director)
    {
        this.director = director;
    }

    public String getDirector()
    {
        return director;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public void addGenre(String genre)
    {
        genres.add(genre);
    }

    public ArrayList<String> getGenres()
    {
        return genres;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setYear(String year)
    {
        this.year = year;
    }

    public String getTitle()
    {
        return title;
    }

    public String getYear()
    {
        return year;
    }

    public String toString()
    {
        return "ID: " + id + " Title: " + title + " Year: " + year + " Director: " + director;
    }
    
    public boolean equals(Object object) 
    {
        if (object == this)
        {
            return true;
        }
        
        if (this.getClass() != object.getClass())
        {
            return false;
        }
        
        Film film = (Film) object;
        return Objects.equals(this.id, film.id) 
            && Objects.equals(this.title, film.title) 
            && Objects.equals(this.year, film.year) 
            && Objects.equals(this.director, film.director) 
            && Objects.equals(this.genres, film.genres);
    }

    public int hashCode()
    {
        return Objects.hash(id, title, year, director, genres);
    }
}
