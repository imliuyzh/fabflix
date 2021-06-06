package com.flixster.entity;

import com.google.gson.JsonObject;

public class CartItem 
{
    private String itemId;
    private String title;
    private int price;
    private int quantity;

    public CartItem(String itemId, String title, int price)
    {
        this.itemId = itemId;
        this.title = title;
        this.price = price;
        this.quantity = 1;
    }

    public int getQuantity() 
    {
        return quantity;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }

    public String getItemId() 
    {
        return itemId;
    }

    public String getTitle()
    {
        return title;
    }

    public int getPrice()
    {
        return price;
    }

    public JsonObject getJSON()
    {
        JsonObject result = new JsonObject();
        result.addProperty("id", itemId);
        result.addProperty("title", title);
        result.addProperty("price", price);
        result.addProperty("quantity", quantity);
        return result;
    }
}
