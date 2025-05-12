package com.ecom.giftcardwinner.reader;

import com.ecom.giftcardwinner.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.item.ExecutionContext;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class OrderCsvReaderTest {

    @TempDir
    Path tempDir;

    private OrderCsvReader reader;

    @BeforeEach
    void setup() {
        reader = new OrderCsvReader();
    }

    @Test
    void testReadSingleOrderFromValidCsv() throws Exception {
        File csv = tempDir.resolve("orders.csv").toFile();
        Files.writeString(csv.toPath(), "userId,amount\n1,45.23");

        injectCsvPath(csv.getAbsolutePath());
        reader.initializeReader();
        reader.open(new ExecutionContext());

        Order order = reader.read();

        assertNotNull(order);
        assertEquals(1L, order.getUserId());
        assertEquals(45.23, order.getAmount(), 0.01);

        reader.close();
    }

    @Test
    void testReturnNullWhenCsvMissing() throws Exception {
        File missingFile = tempDir.resolve("missing.csv").toFile();

        injectCsvPath(missingFile.getAbsolutePath());
        reader.initializeReader();
        reader.open(new ExecutionContext());

        assertNull(reader.read());

        reader.close();
    }

    @Test
    void testReadMultipleOrdersFromCsv() throws Exception {
        File csv = tempDir.resolve("orders.csv").toFile();
        Files.writeString(csv.toPath(), """
            userId,amount
            1,45.23
            2,10.50
            """);

        injectCsvPath(csv.getAbsolutePath());
        reader.initializeReader();
        reader.open(new ExecutionContext());

        Order first = reader.read();
        Order second = reader.read();
        Order third = reader.read(); // should be null

        assertNotNull(first);
        assertEquals(1L, first.getUserId());

        assertNotNull(second);
        assertEquals(2L, second.getUserId());

        assertNull(third);

        reader.close();
    }

    @Test
    void testReturnNullForEmptyCsvFile() throws Exception {
        File csv = tempDir.resolve("empty.csv").toFile();
        Files.writeString(csv.toPath(), "userId,amount\n"); // only header

        injectCsvPath(csv.getAbsolutePath());
        reader.initializeReader();
        reader.open(new ExecutionContext());

        assertNull(reader.read());

        reader.close();
    }

    private void injectCsvPath(String path) {
        try {
            var field = OrderCsvReader.class.getDeclaredField("csvFilePath");
            field.setAccessible(true);
            field.set(reader, path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set csvFilePath", e);
        }
    }
}
