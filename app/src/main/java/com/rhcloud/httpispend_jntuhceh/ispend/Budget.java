package com.rhcloud.httpispend_jntuhceh.ispend;

/**
 * Created by Muneer on 22-03-2016.
 */
public class Budget {
    String email, food, entertainment, electronics, fashion, other, total, budgetSetAt, uploadedTime, uploaderMAC;

    public Budget(String email, String food,  String entertainment, String electronics, String fashion, String other, String total) {
        this.electronics = electronics;
        this.email = email;
        this.entertainment = entertainment;
        this.fashion = fashion;
        this.food = food;
        this.other = other;
        this.total = total;
    }

    public Budget(String email, String food,  String entertainment, String electronics, String fashion, String other, String total, String budgetSetAt) {
        this.electronics = electronics;
        this.email = email;
        this.entertainment = entertainment;
        this.fashion = fashion;
        this.food = food;
        this.other = other;
        this.total = total;
        this.budgetSetAt = budgetSetAt;
    }

    public Budget(String email, String food,  String entertainment, String electronics, String fashion, String other, String total, String uploadedTime, String uploaderMAC) {
        this.electronics = electronics;
        this.email = email;
        this.entertainment = entertainment;
        this.fashion = fashion;
        this.food = food;
        this.other = other;
        this.total = total;
        this.uploadedTime = uploadedTime;
        this.uploaderMAC = uploaderMAC;
    }

    public Budget(String email, String food,  String entertainment, String electronics, String fashion, String other, String total, String budgetSetAt, String uploadedTime, String uploaderMAC) {
        this.electronics = electronics;
        this.email = email;
        this.entertainment = entertainment;
        this.fashion = fashion;
        this.food = food;
        this.other = other;
        this.total = total;
        this.budgetSetAt = budgetSetAt;
        this.uploadedTime = uploadedTime;
        this.uploaderMAC = uploaderMAC;
    }
}
