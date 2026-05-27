/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

/**
 *
 * @author qtang
 */
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.SecurityUtils;

public class ParallelSecurityUtilsTest {
     @DataProvider(name = "parallelApiKeyCases", parallel = true)
    public Object[][] parallelApiKeyCases() {
        return new Object[][]{
                {"ESP32_SECRET_2026", true},
                {"WRONG_KEY", false},
                {"", false},
                {null, false},
                {"esp32_secret_2026", false},
                {"ESP32_SECRET_2026 ", false},
                {" ESP32_SECRET_2026", false},
                {"ESP32-SECRET-2026", false}
        };
    }

    @Test(
            dataProvider = "parallelApiKeyCases",
            groups = {"parallel", "security"},
            description = "Run API key validation cases in parallel"
    )
    public void shouldValidateApiKeysInParallel(String apiKey, boolean expectedResult) throws InterruptedException {
        String threadName = Thread.currentThread().getName();

        System.out.println("[PARALLEL DEMO] Thread=" + threadName + ", apiKey=" + apiKey);

        // Small delay so the parallel behavior is easier to see in console output.
        Thread.sleep(500);

        boolean actualResult = SecurityUtils.isValidDeviceApiKey(apiKey);

        Assert.assertEquals(
                actualResult,
                expectedResult,
                "API key validation result is incorrect for input: " + apiKey
        );
    }
}
