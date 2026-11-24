package com.affariyet.utils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

/** Lecture des paramètres d'URL, indépendante de la forme d'encodage choisie par le navigateur. */
public final class UrlUtils {

    private UrlUtils() {
    }

    /**
     * Valeur DÉCODÉE du paramètre {@code name} de la chaîne de requête, ou {@code null} s'il est absent.
     * Accepte toutes les formes rencontrées : « + » ou « %20 » pour l'espace, « %C3%A9 » ou « é » brut
     * pour un caractère accentué. Le fragment (#...) est ignoré.
     */
    public static String queryParameter(String url, String name) {
        if (url == null) {
            return null;
        }
        int q = url.indexOf('?');
        if (q < 0) {
            return null;
        }
        String query = url.substring(q + 1);
        int hash = query.indexOf('#');
        if (hash >= 0) {
            query = query.substring(0, hash);
        }
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            String key = decode(eq < 0 ? pair : pair.substring(0, eq));
            if (name.equals(key)) {
                return eq < 0 ? "" : decode(pair.substring(eq + 1));
            }
        }
        return null;
    }

    /** Comparaison de deux textes après normalisation Unicode NFC (« é » précomposé = « e » + accent combinant). */
    public static boolean sameText(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return Normalizer.normalize(a, Normalizer.Form.NFC).equals(Normalizer.normalize(b, Normalizer.Form.NFC));
    }

    private static String decode(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return s; // séquence « % » invalide : valeur gardée telle quelle
        }
    }
}
