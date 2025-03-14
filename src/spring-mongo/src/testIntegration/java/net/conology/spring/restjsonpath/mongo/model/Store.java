package net.conology.spring.restjsonpath.mongo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Store {
    public String id;
    public String name;
    @JsonProperty("@type")
    public String atType;
    public Instant created;
    public Person owner;
    public List<Person> employees = new ArrayList<>();
    public List<Book> books = new ArrayList<>();
}
