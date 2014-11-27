package org.kidding.orm.test;

import com.sun.javafx.beans.IDProperty;
import org.kidding.orm.entity.POJO;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by iCrany on 14/11/27.
 */
@Table(name="tableTest")
public class POJOTest extends POJO {

    @Id
    private Long id;

    @Id
    private String name;

    private String lastName;

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
