package AccessFromBothSides;

// Enum för alla olika kategorier där varje konstant har en kod och textbeskrivning
// Konstruktorn används för att initialisera varje konstant med sina värden.
// Getters metoderna gör det möjligt att hämta värdena

public enum EnumCategories {

    // Konstanterna är objekt av typen EnumCategories som har två parametrar som skickar value och text till konstruktorn
    FILM (11, "Film"),
    GEOGRAPHY (22, "Geography"),
    POLITICS (24, "Politics"),
    SPORTS (21, "Sports"),
    MYTHOLOGY (20, "Mythology"),
    VEHICLES (28, "Vehicles");

    // Deklarerar två privata instansvariabler i konstruktorn.
    // Value representerar ett id eller kod
    // Text representerar en sträng som beskriver kategorin
    private final int value;
    private final String text;

    // En privat konstruktor som tar emot två parametrar (Value & Text) tilldelar dessa till instansvariablerna.
    // Kan inte anropas utanför enum
    EnumCategories(int value, String text) {
        this.value = value;
        this.text = text;
    }

    // En public metod som retunerar värdet på text, används för att hämta kategorins namn i en sträng
    public String getText() {
        return text;
    }
    // En public metod som retunerar värdet av instansvariabeln Value och omvandlas till en sträng
    public String getValue() {
        return Integer.toString(value);
    }
}
