package edu.usm.domain;

import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by andrew on 10/6/15.
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Aggregation extends BasicEntity{

    static final String TYPE_ORGANIZATION = "Organization";
    static final String TYPE_COMMITTEE = "Committee";
    static final String TYPE_EVENT = "Event";

    @Column
    @JsonView({Views.GroupListView.class,
            Views.GroupDetailsView.class})
    protected String aggregationType;

    @ManyToMany(mappedBy = "aggregations" , cascade = {CascadeType.REFRESH, CascadeType.MERGE} , fetch = FetchType.EAGER)
    protected Set<Group> groups = new HashSet<>();

    public Set<Group> getGroups() {
        return groups;
    }
    public void setGroups(Set<Group> groups) {
        this.groups = groups;
    }

    public abstract String getAggregationType();
    public abstract Set<Contact> getAggregationMembers();
    public abstract void setAggregationType(String aggregationType);
    public abstract void setAggregationMembers(Set<Contact> aggregationMembers);

    Aggregation() {
        super();
    }

}