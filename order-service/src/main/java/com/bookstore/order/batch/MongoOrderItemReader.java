package com.bookstore.order.batch;

import com.bookstore.order.document.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MongoOrderItemReader extends MongoItemReader<Order> {

    public MongoOrderItemReader(MongoTemplate mongoTemplate, LocalDateTime startDate, LocalDateTime endDate) {
        setTemplate(mongoTemplate);
        setTargetType(Order.class);
        setQuery(buildQuery(startDate, endDate));
        setSort(buildSort());
        setPageSize(100);
        setName("mongoOrderItemReader");
    }

    private String buildQuery(LocalDateTime startDate, LocalDateTime endDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("createdAt").gte(startDate).lte(endDate));
        return query.toString();
    }

    private Map<String, Sort.Direction> buildSort() {
        Map<String, Sort.Direction> sort = new HashMap<>();
        sort.put("createdAt", Sort.Direction.ASC);
        return sort;
    }
}
