package org.kidding.orm.test;

import com.sun.javafx.beans.IDProperty;
import org.kidding.orm.entity.POJO;

import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * Created by iCrany on 14/11/27.
 */
@Table(name="tableTest")
public class POJOTest extends POJO {

    @Id
    private Integer id;

    private String name;

    private String lastName;

    private String sex;

    private String address;

    private Timestamp createTime;

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
