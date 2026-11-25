package com.example.chifaa.models;

/** Résumé d'une conversation pour l'écran "Historique". */
public class ChatSessionSummary {

    public String sessionId;
    public String startedAt;     // ISO 8601 UTC
    public String preview;       // première question
    public int messageCount;
    public boolean hadDistress;
}
