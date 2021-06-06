package com.flixster.entity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Customer extends User
{
    private String id, ccId;
    private HashMap<String, String> sessionState;

    public Customer(String id, String firstName, String lastName, String ccId, String email)
    {
        super(email, firstName, lastName);
        
        this.id = id;
        this.ccId = ccId;
        
        this.sessionState = new HashMap<>();
        this.sessionState.put("title", "");
        this.sessionState.put("year", "");
        this.sessionState.put("director", "");
        this.sessionState.put("star", "");
        this.sessionState.put("numResults", "25");
        this.sessionState.put("offset", "0");
        this.sessionState.put("selectedGenre", "");
        this.sessionState.put("selectedTitle", "");
        this.sessionState.put("sortBy1", "title");
        this.sessionState.put("sortOrder1", "ASC");
        this.sessionState.put("sortBy2", "rating");
        this.sessionState.put("sortOrder2", "DESC");
    }
    
    public String getID()
    {
        return id;
    }
    
    public String getCreditID()
    {
        return ccId;
    }
    
    public String getSessionProperty(String property)
    {
        return this.sessionState.get(property);
    }
    
    public void setSessionProperty(String property, String value)
    {
        this.sessionState.put(property, value);
    }
    
    public String getSessionStateString() throws UnsupportedEncodingException
    {
        return this.sessionState.entrySet()
            .stream()
            .map((entry) -> {
                try
                {
                    return entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name());
                }
                catch (UnsupportedEncodingException exception) 
                { 
                    return "";
                }
            })
            .collect(Collectors.joining("&"));
    }
}
