package net.conology.spring.restjsonpath.mongo.ir;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;

public final class MongoAnyOfSelector implements MongoAlternativesSelector, MongoSelector {

    private final List<MongoAllOfSelector> allOfSelectors;

    public MongoAnyOfSelector(List<MongoAllOfSelector> allOfSelectors) {
        this.allOfSelectors = allOfSelectors;
    }

    public void apply(Criteria parentCriteria) {
        var criterias = new ArrayList<Criteria>();
        for (var allOfSelector : allOfSelectors) {
            Criteria criteria = new Criteria();
            allOfSelector.apply(criteria);
            criterias.add(criteria);
        }
        parentCriteria.orOperator(criterias);
    }

    public List<MongoAllOfSelector> getAllOfSelectors() {
        return allOfSelectors;
    }

    @Override
    public Criteria asCriteria() {
        var critera = new Criteria();
        apply(critera);
        return critera;
    }
}
