package controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import db.DBConnection;

import java.io.FileOutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StatementController {

    private Connection conn = DBConnection.getConnection();

    public String generateStatement(String accountNumber, String fromDate, String toDate, String savePath) {
        try {
            // Account info বের করা
            PreparedStatement ps1 = conn.prepareStatement(
                    "SELECT a.*, c.full_name, c.email, c.phone, c.address, c.nid " +
                            "FROM accounts a JOIN customers c ON a.customer_id = c.id " +
                            "WHERE a.account_number = ?");
            ps1.setString(1, accountNumber);
            ResultSet accRs = ps1.executeQuery();

            if (!accRs.next()) return "Account পাওয়া যায়নি!";

            String customerName = accRs.getString("full_name");
            String email        = accRs.getString("email");
            String phone        = accRs.getString("phone");
            String address      = accRs.getString("address");
            String accountType  = accRs.getString("account_type");
            String status       = accRs.getString("status");
            double balance      = accRs.getDouble("balance");

            // Transactions বের করা
            PreparedStatement ps2 = conn.prepareStatement(
                    "SELECT * FROM transactions WHERE account_number = ? " +
                            "AND DATE(transaction_date) BETWEEN ? AND ? " +
                            "ORDER BY transaction_date ASC");
            ps2.setString(1, accountNumber);
            ps2.setString(2, fromDate);
            ps2.setString(3, toDate);
            ResultSet txRs = ps2.executeQuery();

            // PDF তৈরি করা
            Document document = new Document(PageSize.A4, 40, 40, 60, 40);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(savePath));
            document.open();

            // ── Fonts ──
            Font titleFont    = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD,   new BaseColor(15, 40, 80));
            Font subFont      = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, new BaseColor(100, 120, 160));
            Font headerFont   = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   new BaseColor(255, 255, 255));
            Font normalFont   = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(50, 50, 50));
            Font boldFont     = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   new BaseColor(30, 30, 30));
            Font greenFont    = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   new BaseColor(0, 130, 80));
            Font redFont      = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   new BaseColor(200, 50, 50));
            Font blueFont     = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   new BaseColor(30, 100, 200));
            Font smallFont    = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, new BaseColor(120, 120, 120));

            // ── Header ──
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{2f, 1f});

            // Bank name
            PdfPCell bankCell = new PdfPCell();
            bankCell.setBorder(Rectangle.NO_BORDER);
            bankCell.addElement(new Paragraph("🏦 Bangladesh Bank", titleFont));
            bankCell.addElement(new Paragraph("Management System — Account Statement", subFont));
            headerTable.addCell(bankCell);

            // Date generated
            PdfPCell dateCell = new PdfPCell();
            dateCell.setBorder(Rectangle.NO_BORDER);
            dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            dateCell.addElement(new Paragraph(
                    "Generated: " + new SimpleDateFormat("dd MMM yyyy HH:mm").format(new Date()), smallFont));
            headerTable.addCell(dateCell);
            document.add(headerTable);

            // Divider line
            document.add(new Chunk(new com.itextpdf.text.pdf.draw.LineSeparator(
                    1f, 100f, new BaseColor(15, 40, 80), Element.ALIGN_CENTER, -2)));
            document.add(Chunk.NEWLINE);

            // ── Account Info Box ──
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(10f);
            infoTable.setSpacingAfter(10f);

            // Left: Customer info
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBackgroundColor(new BaseColor(240, 245, 255));
            leftCell.setPadding(10);
            leftCell.setBorderColor(new BaseColor(200, 215, 240));
            leftCell.addElement(new Paragraph("Account Holder", boldFont));
            leftCell.addElement(new Paragraph(customerName, titleFont));
            leftCell.addElement(new Paragraph("Phone: " + (phone != null ? phone : "N/A"), normalFont));
            leftCell.addElement(new Paragraph("Email: " + (email != null ? email : "N/A"), normalFont));
            leftCell.addElement(new Paragraph("Address: " + (address != null ? address : "N/A"), normalFont));
            infoTable.addCell(leftCell);

            // Right: Account info
            PdfPCell rightCell = new PdfPCell();
            rightCell.setBackgroundColor(new BaseColor(15, 40, 80));
            rightCell.setPadding(10);
            rightCell.setBorderColor(new BaseColor(15, 40, 80));

            Font whiteFont  = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.WHITE);
            Font whiteBold  = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD,   BaseColor.WHITE);
            Font whiteSmall = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, new BaseColor(180, 200, 230));

            rightCell.addElement(new Paragraph("Account Number", whiteSmall));
            rightCell.addElement(new Paragraph(accountNumber, whiteBold));
            rightCell.addElement(Chunk.NEWLINE);
            rightCell.addElement(new Paragraph("Account Type: " + accountType, whiteFont));
            rightCell.addElement(new Paragraph("Status: " + status, whiteFont));
            rightCell.addElement(Chunk.NEWLINE);
            rightCell.addElement(new Paragraph("Current Balance", whiteSmall));

            Font balanceFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD,
                    new BaseColor(100, 220, 180));
            rightCell.addElement(new Paragraph(String.format("TK %.2f", balance), balanceFont));
            infoTable.addCell(rightCell);
            document.add(infoTable);

            // Statement period
            Paragraph period = new Paragraph(
                    "Statement Period: " + fromDate + "  to  " + toDate, boldFont);
            period.setAlignment(Element.ALIGN_CENTER);
            period.setSpacingAfter(8f);
            document.add(period);

            // ── Transaction Table ──
            PdfPTable txTable = new PdfPTable(6);
            txTable.setWidthPercentage(100);
            txTable.setWidths(new float[]{0.5f, 1.5f, 1f, 1.2f, 1.2f, 2f});
            txTable.setSpacingBefore(5f);

            // Table header
            String[] headers = {"#", "Date", "Type", "Amount (TK)", "Balance (TK)", "Description"};
            for (String h : headers) {
                PdfPCell hCell = new PdfPCell(new Phrase(h, headerFont));
                hCell.setBackgroundColor(new BaseColor(15, 40, 80));
                hCell.setPadding(7);
                hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                txTable.addCell(hCell);
            }

            // Table rows
            int rowNum = 1;
            double totalDeposit  = 0;
            double totalWithdraw = 0;

            while (txRs.next()) {
                String type        = txRs.getString("type");
                double amount      = txRs.getDouble("amount");
                double balAfter    = txRs.getDouble("balance_after");
                String desc        = txRs.getString("description");
                Timestamp txDate   = txRs.getTimestamp("transaction_date");

                BaseColor rowBg = (rowNum % 2 == 0) ?
                        new BaseColor(245, 248, 255) : BaseColor.WHITE;

                // Row number
                PdfPCell c1 = new PdfPCell(new Phrase(String.valueOf(rowNum), smallFont));
                c1.setBackgroundColor(rowBg);
                c1.setPadding(6);
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                txTable.addCell(c1);

                // Date
                PdfPCell c2 = new PdfPCell(new Phrase(
                        new SimpleDateFormat("dd/MM/yyyy HH:mm").format(txDate), normalFont));
                c2.setBackgroundColor(rowBg);
                c2.setPadding(6);
                txTable.addCell(c2);

                // Type (রঙ সহ)
                Font typeFont = "Deposit".equals(type) ? greenFont :
                        "Withdrawal".equals(type) ? redFont : blueFont;
                PdfPCell c3 = new PdfPCell(new Phrase(type, typeFont));
                c3.setBackgroundColor(rowBg);
                c3.setPadding(6);
                c3.setHorizontalAlignment(Element.ALIGN_CENTER);
                txTable.addCell(c3);

                // Amount
                String prefix = "Deposit".equals(type) ? "+ " :
                        "Withdrawal".equals(type) ? "- " : "";
                Font amtFont = "Deposit".equals(type) ? greenFont :
                        "Withdrawal".equals(type) ? redFont : blueFont;
                PdfPCell c4 = new PdfPCell(new Phrase(
                        prefix + String.format("%.2f", amount), amtFont));
                c4.setBackgroundColor(rowBg);
                c4.setPadding(6);
                c4.setHorizontalAlignment(Element.ALIGN_RIGHT);
                txTable.addCell(c4);

                // Balance after
                PdfPCell c5 = new PdfPCell(new Phrase(
                        String.format("%.2f", balAfter), boldFont));
                c5.setBackgroundColor(rowBg);
                c5.setPadding(6);
                c5.setHorizontalAlignment(Element.ALIGN_RIGHT);
                txTable.addCell(c5);

                // Description
                PdfPCell c6 = new PdfPCell(new Phrase(
                        desc != null ? desc : "", normalFont));
                c6.setBackgroundColor(rowBg);
                c6.setPadding(6);
                txTable.addCell(c6);

                if ("Deposit".equals(type)) totalDeposit += amount;
                else if ("Withdrawal".equals(type)) totalWithdraw += amount;
                rowNum++;
            }

            document.add(txTable);

            // ── Summary ──
            document.add(Chunk.NEWLINE);
            PdfPTable summaryTable = new PdfPTable(3);
            summaryTable.setWidthPercentage(60);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            Font sumHeaderFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);

            String[] sumLabels = {"Total Deposit", "Total Withdrawal", "Net"};
            double net = totalDeposit - totalWithdraw;
            String[] sumValues = {
                    String.format("TK %.2f", totalDeposit),
                    String.format("TK %.2f", totalWithdraw),
                    String.format("TK %.2f", net)
            };
            BaseColor[] sumColors = {
                    new BaseColor(0, 130, 80),
                    new BaseColor(200, 50, 50),
                    net >= 0 ? new BaseColor(30, 100, 200) : new BaseColor(180, 50, 50)
            };

            for (int i = 0; i < 3; i++) {
                PdfPCell lc = new PdfPCell(new Phrase(sumLabels[i], sumHeaderFont));
                lc.setBackgroundColor(sumColors[i]);
                lc.setPadding(8);
                lc.setHorizontalAlignment(Element.ALIGN_CENTER);
                summaryTable.addCell(lc);
            }
            for (int i = 0; i < 3; i++) {
                PdfPCell vc = new PdfPCell(new Phrase(sumValues[i], boldFont));
                vc.setBackgroundColor(new BaseColor(245, 248, 255));
                vc.setPadding(8);
                vc.setHorizontalAlignment(Element.ALIGN_CENTER);
                summaryTable.addCell(vc);
            }
            document.add(summaryTable);

            // Footer
            document.add(Chunk.NEWLINE);
            document.add(new Chunk(new com.itextpdf.text.pdf.draw.LineSeparator(
                    0.5f, 100f, new BaseColor(200, 210, 230), Element.ALIGN_CENTER, -2)));
            Paragraph footer = new Paragraph(
                    "This is a computer-generated statement. Bangladesh Bank Management System © 2026",
                    smallFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(5f);
            document.add(footer);

            document.close();
            return "SUCCESS:" + savePath;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}