package org.ifmo.ru.lab4back.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public class PointEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String owner;
    private String x;
    private String y;
    private String r;
    private String hit;
    private long executionTime;
    private String currentTime;

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }

    public String getHit() {
        return hit;
    }

    public void setHit(String hit) {
        this.hit = hit;
    }

    @Override
    public String toString() {
        return "PointEntity{" +
                "id=" + id +
                ", owner='" + owner + '\'' +
                ", x='" + x + '\'' +
                ", y='" + y + '\'' +
                ", r='" + r + '\'' +
                ", hit='" + hit + '\'' +
                ", executionTime=" + executionTime +
                ", currentTime='" + currentTime + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PointEntity that)) return false;
        return id == that.id && executionTime == that.executionTime && Objects.equals(owner, that.owner) && Objects.equals(x, that.x) && Objects.equals(y, that.y) && Objects.equals(r, that.r) && Objects.equals(hit, that.hit) && Objects.equals(currentTime, that.currentTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, owner, x, y, r, hit, executionTime, currentTime);
    }
}
