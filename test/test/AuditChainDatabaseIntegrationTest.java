/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import dao.ReportDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import model.AuditChainRow;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import service.ChainVerificationService;
import utils.DBContext;

/**
 *
 * @author qtang
 */
public class AuditChainDatabaseIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void checkDatabaseConnection() {
        try ( Connection conn = DBContext.getConnection()) {
            if (conn == null || conn.isClosed()) {
                throw new SkipException("Skipped: Không thể mở kết nối đến Database.");
            }
        } catch (Exception e) {
            throw new SkipException("Skipped: Máy host chưa cài đặt SmartWaterAuditDB hoặc sai cấu hình. Bỏ qua Integration Test. Chi tiết: " + e.getMessage());
        }
    }

    @Test
    public void sqlAuditIntegrityShouldBeValid() {
        String result = new ReportDAO().verifyAuditIntegrity();
        Assert.assertEquals(result, "VALID");
    }

    @Test(
            groups = {"integration", "audit"},
            description = "Real SQL Server audit chain should be valid before tampering"
    )
    public void databaseAuditChainShouldInitiallyBeValid() {
        List<AuditChainRow> chain = new ReportDAO().getFullAuditLogs();

        if (chain.isEmpty()) {
            throw new SkipException("No audit rows found in database. Run an audited action first.");
        }

        new ChainVerificationService().verifyAndAnnotateChain(chain);

        for (int i = 0; i < chain.size(); i++) {
            AuditChainRow row = chain.get(i);

            if (!row.isValid()) {
                AuditChainRow previousRow = i > 0 ? chain.get(i - 1) : null;

                String message
                        = "\nAudit chain was already broken BEFORE tampering."
                        + "\nBroken index: " + i
                        + "\nBroken audit_id: " + row.getAuditId()
                        + "\nReason: " + row.getTamperReason()
                        + "\nAction: " + row.getAction()
                        + "\nObject type: " + row.getObjectType()
                        + "\nObject id: " + row.getObjectId()
                        + "\nPrev hash in broken row: " + row.getPrevHash()
                        + "\nRow hash in broken row: " + row.getRowHash()
                        + "\nChain hash in broken row: " + row.getChainHash()
                        + (previousRow == null
                                ? "\nPrevious row: <none, this should be genesis row>"
                                : "\nPrevious audit_id: " + previousRow.getAuditId()
                                + "\nPrevious chain hash: " + previousRow.getChainHash());

                Assert.fail(message);
            }
        }
    }

    @Test(
            groups = {"integration", "audit"},
            dependsOnMethods = {"databaseAuditChainShouldInitiallyBeValid"},
            description = "Tampering with a real AuditLog row should be detected by Java verification"
    )
    public void databaseAuditChainTamperingShouldBeDetected() throws Exception {
        AuditTarget target = findSafeAuditRowToTamper();

        if (target == null) {
            throw new SkipException("No suitable audit row found to tamper.");
        }

        String tamperedAction = target.originalAction + "_TAMPERED_BY_TESTNG";

        try {
            updateAuditAction(target.auditId, tamperedAction);

            List<AuditChainRow> chainAfterTamper = new ReportDAO().getFullAuditLogs();
            new ChainVerificationService().verifyAndAnnotateChain(chainAfterTamper);

            AuditChainRow tamperedRow = findRowByAuditId(chainAfterTamper, target.auditId);

            Assert.assertNotNull(
                    tamperedRow,
                    "Tampered audit row should still exist in loaded chain"
            );

            Assert.assertFalse(
                    tamperedRow.isValid(),
                    "Tampered row should be detected as invalid"
            );

            Assert.assertEquals(
                    tamperedRow.getTamperReason(),
                    "ROW_HASH_MISMATCH",
                    "Changing AuditLog.action should break the stored row_hash"
            );

        } finally {
            updateAuditAction(target.auditId, target.originalAction);
        }

        List<AuditChainRow> chainAfterRestore = new ReportDAO().getFullAuditLogs();
        new ChainVerificationService().verifyAndAnnotateChain(chainAfterRestore);

        for (AuditChainRow row : chainAfterRestore) {
            Assert.assertTrue(
                    row.isValid(),
                    "Audit chain should be valid again after restoring original action. Broken at audit_id="
                    + row.getAuditId()
                    + ", reason="
                    + row.getTamperReason()
            );
        }
    }

    private AuditTarget findSafeAuditRowToTamper() throws Exception {
        /*
         * Pick the newest row because it is safer.
         * Tampering the newest row usually affects only that row.
         */
        String sql
                = "SELECT TOP 1 audit_id, action "
                + "FROM dbo.AuditLog "
                + "WHERE action IS NOT NULL "
                + "ORDER BY audit_id DESC";

        try ( Connection conn = DBContext.getConnection();  PreparedStatement ps = conn.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                return null;
            }

            AuditTarget target = new AuditTarget();
            target.auditId = rs.getInt("audit_id");
            target.originalAction = rs.getString("action");
            return target;
        }
    }

    private void updateAuditAction(int auditId, String newAction) throws Exception {
        String sql
                = "UPDATE dbo.AuditLog "
                + "SET action = ? "
                + "WHERE audit_id = ?";

        try ( Connection conn = DBContext.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newAction);
            ps.setInt(2, auditId);

            int affectedRows = ps.executeUpdate();

            Assert.assertEquals(
                    affectedRows,
                    1,
                    "Exactly one audit row should be updated"
            );
        }
    }

    private AuditChainRow findRowByAuditId(List<AuditChainRow> chain, int auditId) {
        for (AuditChainRow row : chain) {
            if (row.getAuditId() == auditId) {
                return row;
            }
        }
        return null;
    }

    private static class AuditTarget {

        int auditId;
        String originalAction;
    }
}
