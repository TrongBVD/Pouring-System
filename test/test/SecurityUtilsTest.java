/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.SecurityUtils;

/**
 *
 * @author qtang
 */
public class SecurityUtilsTest {
    @DataProvider(name = "apiKeyCases")
    public Object[][] apiKeyCases() {
        return new Object[][]{
                {"ESP32_SECRET_2026", true},
                {"WRONG_KEY", false},
                {"", false},
                {null, false},
                {"esp32_secret_2026", false}
        };
    }

    @Test(
            dataProvider = "apiKeyCases",
            groups = {"unit", "security"},
            description = "Only the correct ESP32 IoT API key should be accepted"
    )
    public void shouldValidateDeviceApiKeyCorrectly(String apiKey, boolean expectedResult) {
        boolean actualResult = SecurityUtils.isValidDeviceApiKey(apiKey);

        Assert.assertEquals(
                actualResult,
                expectedResult,
                "API key validation result is incorrect for input: " + apiKey
        );
    }

    @Test(
            groups = {"unit", "audit"},
            description = "Same audit text should always produce the same SHA-256 hash"
    )
    public void sameInputShouldProduceSameHash() {
        String auditText =
                "audit_id=1|action=POUR_SESSION_CREATE|object_type=PourSession|object_id=1001";

        String hash1 = SecurityUtils.sha256HexUtf16LE(auditText);
        String hash2 = SecurityUtils.sha256HexUtf16LE(auditText);

        Assert.assertEquals(
                hash1,
                hash2,
                "The same audit text must always produce the same hash"
        );
    }

    @Test(
            groups = {"unit", "audit"},
            description = "Changing audit text should change the SHA-256 hash"
    )
    public void changedInputShouldProduceDifferentHash() {
        String originalAuditText =
                "audit_id=1|action=POUR_SESSION_CREATE|object_type=PourSession|object_id=1001";

        String tamperedAuditText =
                "audit_id=1|action=POUR_SESSION_DELETE|object_type=PourSession|object_id=1001";

        String originalHash = SecurityUtils.sha256HexUtf16LE(originalAuditText);
        String tamperedHash = SecurityUtils.sha256HexUtf16LE(tamperedAuditText);

        Assert.assertNotEquals(
                originalHash,
                tamperedHash,
                "Changing audit content should produce a different hash"
        );
    }

    @Test(
            groups = {"unit", "audit"},
            description = "SHA-256 hash should be represented as 64 lowercase hexadecimal characters"
    )
    public void hashShouldHaveSha256HexLength() {
        String auditText = "SmartWaterAuditDB";

        String hash = SecurityUtils.sha256HexUtf16LE(auditText);

        Assert.assertNotNull(hash, "Hash should not be null for valid input");
        Assert.assertEquals(hash.length(), 64, "SHA-256 hex string should have 64 characters");
        Assert.assertTrue(
                hash.matches("[0-9a-f]{64}"),
                "Hash should contain only lowercase hexadecimal characters"
        );
    }

    @Test(
            groups = {"unit", "audit"},
            description = "Null input should return null instead of crashing"
    )
    public void nullInputShouldReturnNull() {
        String hash = SecurityUtils.sha256HexUtf16LE(null);

        Assert.assertNull(hash, "Null input should return null");
    }
}
