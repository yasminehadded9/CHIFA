package com.example.chifaa;

import static org.junit.Assert.assertEquals;

import com.example.chifaa.utils.AppConstants;

import org.junit.Test;

/** Tests unitaires locaux (exécutés sur l'ordinateur, sans émulateur). */
public class ExampleUnitTest {

    @Test
    public void vingtQuatreGouvernorats() {
        assertEquals(24, AppConstants.GOVERNORATE_CODES.length);
    }

    @Test
    public void onzeEffetsSecondaires() {
        assertEquals(11, AppConstants.SIDE_EFFECTS.length);
    }
}
