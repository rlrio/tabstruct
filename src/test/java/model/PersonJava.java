package model;

import com.rlrio.annotations.Column;
import com.rlrio.annotations.TemporalFormatter;
import com.rlrio.annotations.DateRange;
import com.rlrio.annotations.Email;
import com.rlrio.annotations.NumberRange;
import com.rlrio.annotations.Url;

import java.net.URL;
import java.time.LocalDate;
import java.util.Objects;

public class PersonJava {
    @Column(name = "Name")
    private String name;

    @Column
    @NumberRange(min = 15)
    private Integer age;

    @Email
    @Column(name = "Email")
    private String email;

    @Url
    @Column
    private URL url;

    @Column(name = "register_date")
    @TemporalFormatter(format = "dd-MM-yyyy")
    @DateRange(from = "2010-12-31")
    private LocalDate registerDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public LocalDate getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(LocalDate registerDate) {
        this.registerDate = registerDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonJava that = (PersonJava) o;
        return Objects.equals(name, that.name) && Objects.equals(age, that.age) && Objects.equals(email, that.email) && Objects.equals(url, that.url) && Objects.equals(registerDate, that.registerDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, email, url, registerDate);
    }

    @Override
    public String toString() {
        return "PersonJava{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", email='" + email + '\'' +
                ", url=" + url +
                ", registerDate=" + registerDate +
                '}';
    }
}
