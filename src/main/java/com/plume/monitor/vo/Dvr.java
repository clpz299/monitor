package com.plume.monitor.vo;

import java.util.Date;

/**
 * @author AC
 */
public class Dvr {
    private Integer num;
    private String ip;
    private Integer port;
    private String username;
    private String pwd;
    private Date ddd;
    private Integer fiel;

    public Dvr() {
        super();
    }

    public Dvr(Integer num, String ip, Integer port, String username, String pwd, Date ddd, Integer fiel) {
        super();
        this.num = num;
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.pwd = pwd;
        this.ddd = ddd;
        this.fiel = fiel;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public Date getDdd() {
        return ddd;
    }

    public void setDdd(Date ddd) {
        this.ddd = ddd;
    }

    public Integer getFiel() {
        return fiel;
    }

    public void setFiel(Integer fiel) {
        this.fiel = fiel;
    }
}
