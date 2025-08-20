package com.example.studentprocessor.service;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.springframework.stereotype.Service;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class StreamingExcelToCsvService {

    private static final String OUTPUT_DIR = "C:/var/log/applications/API/dataprocessing/";

    public String convertExcelToCsvStreaming(String excelFilePath) throws Exception {
        System.out.println("=== Starting Streaming Excel to CSV Conversion ===");
        System.out.println("Input file: " + excelFilePath);

        // Generate output CSV filename
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String baseFileName = new File(excelFilePath).getName().replace(".xlsx", "");
        String csvFileName = baseFileName + "_processed_" + timestamp + ".csv";
        String csvFilePath = OUTPUT_DIR + csvFileName;

        System.out.println("Output CSV: " + csvFilePath);

        long startTime = System.currentTimeMillis();

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(csvFilePath))) {
            // Write CSV header
            writer.write("studentId,firstName,lastName,DOB,class,score");
            writer.newLine();

            OPCPackage pkg = OPCPackage.open(new File(excelFilePath));
            XSSFReader reader = new XSSFReader(pkg);
            ReadOnlySharedStringsTable sst = new ReadOnlySharedStringsTable(pkg);

            XMLReader parser = fetchSheetParser(sst, writer);

            // Get first sheet and process streaming
            try (InputStream sheet = reader.getSheetsData().next()) {
                InputSource sheetSource = new InputSource(sheet);
                parser.parse(sheetSource);
            }

            pkg.close();
        }

        long endTime = System.currentTimeMillis();
        double processingTime = (endTime - startTime) / 1000.0;

        System.out.println(String.format("Streaming conversion completed in %.2f seconds", processingTime));
        System.out.println("CSV file created: " + csvFileName);

        return csvFileName;
    }

    private XMLReader fetchSheetParser(ReadOnlySharedStringsTable sst, BufferedWriter writer)
            throws SAXException, ParserConfigurationException {

        XMLReader parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        ContentHandler handler = new SheetHandler(sst, writer);
        parser.setContentHandler(handler);
        return parser;
    }

    // Custom handler to stream rows and apply +10 score adjustment
    private static class SheetHandler extends DefaultHandler {
        private final ReadOnlySharedStringsTable sst;
        private final BufferedWriter writer;

        private boolean nextIsString;
        private boolean isInlineString;
        private String lastContents;
        private String cellReference;
        private String[] currentRowData = new String[6]; // For 6 columns
        private int rowCount = 0;
        private boolean isHeaderRow = true;

        public SheetHandler(ReadOnlySharedStringsTable sst, BufferedWriter writer) {
            this.sst = sst;
            this.writer = writer;
        }

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) {
            if (name.equals("c")) { // cell
                cellReference = attributes.getValue("r"); // Get cell reference like "A1", "B1", etc.
                String cellType = attributes.getValue("t");
                nextIsString = cellType != null && cellType.equals("s");
                isInlineString = cellType != null && cellType.equals("inlineStr");
            } else if (name.equals("row")) {
                // Reset row data for new row
                currentRowData = new String[6];
                for (int i = 0; i < 6; i++) {
                    currentRowData[i] = "";
                }
            }
            lastContents = "";
        }

        @Override
        public void endElement(String uri, String localName, String name) {
            if (nextIsString && name.equals("v")) {
                try {
                    int idx = Integer.parseInt(lastContents);
                    lastContents = sst.getItemAt(idx).getString();
                } catch (Exception e) {
                    // Keep original value if shared string lookup fails
                }
            } else if (name.equals("t") && isInlineString) {
                // Handle inline string content - don't modify lastContents here
                // It will be handled in the "c" end element
            }

            if (name.equals("v") || (name.equals("c") && isInlineString)) {
                // Store cell value in appropriate column
                if (cellReference != null) {
                    int columnIndex = getColumnIndex(cellReference);

                    if (columnIndex >= 0 && columnIndex < 6) {
                        // Apply +10 to score column (index 5) for data rows only
                        if (columnIndex == 5 && !isHeaderRow) {
                            try {
                                int score = Integer.parseInt(lastContents.trim());
                                currentRowData[columnIndex] = String.valueOf(score + 10);
                            } catch (NumberFormatException e) {
                                currentRowData[columnIndex] = lastContents;
                            }
                        } else {
                            currentRowData[columnIndex] = lastContents;
                        }
                    }
                }
            } else if (name.equals("row")) {
                // Write complete row to CSV
                if (!isHeaderRow) {
                    try {
                        StringBuilder csvRow = new StringBuilder();
                        for (int i = 0; i < 6; i++) {
                            if (i > 0) csvRow.append(",");
                            csvRow.append("\"").append(currentRowData[i].replace("\"", "\"\"")).append("\"");
                        }
                        writer.write(csvRow.toString());
                        writer.newLine();
                        rowCount++;

                        // Progress logging every 100,000 rows
                        if (rowCount % 100000 == 0) {
                            System.out.println("Processed " + rowCount + " rows...");
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    isHeaderRow = false;
                }
            }

            // Reset for next cell
            if (name.equals("c")) {
                cellReference = null;
                nextIsString = false;
                isInlineString = false;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            lastContents += new String(ch, start, length);
        }

        // Convert cell reference like "A1", "B2", "F5" to column index (0-5)
        private int getColumnIndex(String cellRef) {
            if (cellRef == null || cellRef.length() < 1) return -1;

            char column = cellRef.charAt(0);
            switch (column) {
                case 'A': return 0; // studentId
                case 'B': return 1; // firstName
                case 'C': return 2; // lastName
                case 'D': return 3; // DOB
                case 'E': return 4; // class
                case 'F': return 5; // score
                default: return -1;
            }
        }
    }
}
