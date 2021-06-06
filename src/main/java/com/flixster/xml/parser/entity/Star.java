package com.flixster.xml.parser.entity;

import java.util.Objects;

public class Star
{
    private String id, name, birthYear;

    public Star(String id, String name, String birthYear)
    {
        this.id = id;
        this.name = name;
        this.birthYear = birthYear;
    }
    
    public Star(String name, String birthYear)
    {
        this(null, name, birthYear);
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public String getBirthYear()
    {
        return birthYear;
    }

    public void setBirthYear(String birthYear)
    {
        this.birthYear = birthYear;
    }
    
    public String toString()
    {
        return "Name: " + name + " Birth Year: " + birthYear;
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
        
        Star star = (Star) o;
        return Objects.equals(id, star.id) && Objects.equals(name, star.name) && Objects.equals(birthYear, star.birthYear);
    }

    public int hashCode()
    {
        return Objects.hash(name, birthYear, id);
    }
}
