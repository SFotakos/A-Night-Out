package sfotakos.anightout.common;

import java.io.Serializable;

public class Event implements Serializable {

    private String eventDate;
    private String eventName;
    private String eventEstablishment;

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventEstablishment() {
        return eventEstablishment;
    }

    public void setEventEstablishment(String eventEstablishment) {
        this.eventEstablishment = eventEstablishment;
    }
}
