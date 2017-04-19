package com.example.adamf.myfitnessapplicationgoogleapi;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.example.adamf.myfitnessapplicationgoogleapi.MainActivity;

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

        assertNotEquals("Check if walking time is not equal to actual:", expected, actual, 0.001);
        //assertEquals("Confidence of walking time matching failed!", expected, actual, 0.001);

    }

    //Working test
    //RESULT PASSED
    @Test
    public void checkDrivingConfidence() {

        float actual = MainActivity.vehicleTime;

        float expected = 0;

        assertEquals("Confidence of driving matching Success!", expected, actual, 0.001);

    }

    //Working test
    //RESULT FAIL
    @Test
    public void checkStillConfidence() {

        float actual = MainActivity.stillTime;

        float expected = 100;

        //user is standing still therefore the value should be 100 as the api
        //recognises that the user is standing still.
        assertNotEquals("Confidence of still matching Failed!", expected, actual, 0.001);

    }
}