package com.example.adamf.myfitnessapplicationgoogleapi;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */


public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void addition_isNotCorrect() throws Exception {
        assertEquals("Numbers isn't equals!", 5, 2 + 2);
    }

    //Working Test
    //RESULT PASSED
    @Test
    public void checkWalkingConfidence() {

        float actual = MainActivity.walkingTime;

        float expected = 50;

        //The user is not walking therefore the confidence found should not be equal to the expected
        //value. The test should pass
        assertNotEquals("Check if walking time is not equal to actual:", expected, actual, 0.001);

    }





















    //Working test
    //RESULT PASSED
    @Test
    public void checkDrivingConfidence() {

        float actual = MainActivity.vehicleTime;

        float expected = 0;

        //User is not driving therefor the the expected value should be 0
        //the assertEquals checks if the expected(0) is the same as the actual (0)
        assertEquals("Confidence of driving matching Success!", expected, actual, 0.001);

    }



    //Working test
    //RESULT FAIL
    @Test
    public void checkStillConfidence() throws Exception {

        float actual = MainActivity.stillTime;

        float expected = 100;

        //user is standing still therefore the value should be 100 as the api
        //recognises that the user is standing still.
        assertNotEquals("Confidence of still matching Failed!", expected, actual, 0.001);

    }




























    //Working test
    //RESULT PASS
    @Test
    public void checkStillConfidenceEquals() throws Exception {

        float actual = MainActivity.stillTime;

        float expected = 100;

        //user is standing still therefore the value should be 100 as the api
        //recognises that the user is standing still.
        //checking to see if the expected 100, matches the actual 100
        assertEquals("Confidence of still matching Correct!", expected, actual, 0.001);

    }
}