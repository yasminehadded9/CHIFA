package com.example.chifaa.models;

/** Un message de conversation (table messages). */
public class ChatMessage {

    public static final String ROLE_USER = "User";
    public static final String ROLE_AI = "AI";

    // Valeurs de topic_tag pour les réponses particulières du compagnon
    public static final String TAG_DISTRESS = "distress";
    public static final String TAG_MEDICAL_LIMIT = "medical-limit";
    public static final String TAG_NO_MATCH = "no-match";

    public String messageId;
    public String sessionId;
    public String role;          // User / AI
    public String text;
    public String timestamp;     // ISO 8601 UTC
    public String contentId;     // fiche utilisée (réponse du compagnon)
    public String contentTitle;  // titre de cette fiche (jointure)
    public String topicTag;
    public String language;      // Darija, French, MSA, Mixed (langue détectée / langue de réponse)
    public boolean distress;
    public Integer feedback;     // 1 à 5, null = pas encore noté

    public boolean isUser() { return ROLE_USER.equals(role); }
}
