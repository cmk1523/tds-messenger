package com.techdevsolutions.messenger.beans.auditable;

import com.techdevsolutions.common.beans.auditable.Auditable;
import com.techdevsolutions.common.beans.geo.GeoLocation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public class Message extends Auditable implements Serializable {
    @NotBlank
    private String to = "";

    @NotBlank
    private String from = "";

    @NotBlank
    private String text = "";

    @NotNull
    private Optional<String> key = Optional.empty();

    @NotNull
    private Optional<GeoLocation> location = Optional.empty();

    public Message() {
        super();
    }

    @Override
    public String toString() {
        return "Message{" +
                "to='" + to + '\'' +
                ", from='" + from + '\'' +
                ", text='" + text + '\'' +
                ", key=" + key +
                ", location=" + location +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        if (!super.equals(o)) return false;
        Message message = (Message) o;
        return Objects.equals(to, message.to) &&
                Objects.equals(from, message.from) &&
                Objects.equals(text, message.text) &&
                Objects.equals(key, message.key) &&
                Objects.equals(location, message.location);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), to, from, text, key, location);
    }

    public Optional<GeoLocation> getLocation() {
        return location;
    }

    public Message setLocation(Optional<GeoLocation> location) {
        this.location = location;
        return this;
    }

    public String getTo() {
        return to;
    }

    public Message setTo(String to) {
        this.to = to;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public Message setFrom(String from) {
        this.from = from;
        return this;
    }

    public Optional<String> getKey() {
        return key;
    }

    public Message setKey(Optional<String> key) {
        this.key = key;
        return this;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
