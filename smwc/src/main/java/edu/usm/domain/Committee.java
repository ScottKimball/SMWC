package edu.usm.domain;

import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;

@Entity
public class Committee extends Aggregation implements Serializable {

    @ManyToMany(mappedBy = "committees" , cascade = {CascadeType.REFRESH,CascadeType.MERGE} , fetch = FetchType.EAGER)
    @JsonView({Views.GroupDetailsView.class,
            Views.CommitteeList.class})
    private Set<Contact> members;

    @OneToMany(mappedBy="committee", cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private Set<Event> events;

    @Column
    @NotNull
    @JsonView({Views.CommitteeList.class,
            Views.ContactDetails.class,
            Views.ContactCommitteeDetails.class,
            Views.EventList.class,
            Views.GroupListView.class,
            Views.GroupDetailsView.class})
    private String name;

    @Override
    public String getAggregationType() {
        return Aggregation.TYPE_COMMITTEE;
    }

    @Override
    public Set<Contact> getAggregationMembers() {
        return members;
    }

    @Override
    public void setAggregationType(String aggregationType) {
        this.aggregationType = aggregationType;
    }

    @Override
    public void setAggregationMembers(Set<Contact> aggregationMembers) {
        this.members = aggregationMembers;
    }

    public Committee (String id) {
        setId(id);
    }

    public Committee() {
        super();
    }

    public Set<Event> getEvents() {
        return events;
    }

    public void setEvents(Set<Event> events) {
        this.events = events;
    }

    public Set<Contact> getMembers() {
        return members;
    }

    public void setMembers(Set<Contact> members) {
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
