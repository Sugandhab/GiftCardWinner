package com.ecom.giftcardwinner.reader;

import com.ecom.giftcardwinner.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;

@Component
public class OrderCsvReader extends FlatFileItemReader<Order> {

    private static final Logger log = LoggerFactory.getLogger(OrderCsvReader.class);

    @Value("${order.csv.path}")
    private String csvFilePath;

    @PostConstruct
    public void initializeReader() {
        File file = new File(csvFilePath);

        if (!file.exists()) {
            log.warn("Order CSV file not found at path: {}. Proceeding anyway (strict=false).", csvFilePath);
        }

        setResource(new FileSystemResource(file));
        setLinesToSkip(1); //Header row skip

        DefaultLineMapper<Order> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("userId", "amount");

        BeanWrapperFieldSetMapper<Order> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Order.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        setLineMapper(lineMapper);

        setStrict(false); // no fail if file is not there
        log.info("OrderCsvReader initialized with file path: {}", csvFilePath);
    }
}
