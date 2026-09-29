package sportspulse.models;

public class Competition {
    private String id;
    private String name;
    private String year;
    private String sport;

    public Competition(String id, String name, String year, String sport) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.sport = sport;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getSport() { return sport; }
    public void setSport(String sport) { this.sport = sport; }

    @Override
    public String toString() {
        return "Competition{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", year='" + year + '\'' +
                ", sport='" + sport + '\'' +
                '}';
    }
}
