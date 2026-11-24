package com.affariyet;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AppTest {

    @Test
    public void testApplication() {

        String message = "Affariyet Automation";

        Assert.assertEquals(
                message,
                "Affariyet Automation");

        System.out.println("TestNG fonctionne correctement !");
    }
}