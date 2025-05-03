package com.codejam.codex.authzen.responses;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
public class AuthzenResponse<T> {
    private static final String SUCCESSFUL   = "successful";
    private static final String UNSUCCESSFUL = "unsuccessful";

    private String status;

    @Setter
    private List<T> results;

    @Setter
    private String message;

    public AuthzenResponse() {
        this.status = SUCCESSFUL;
        this.results = new ArrayList<>();
    }

    public AuthzenResponse(T data) {
        this();
        if (data != null) this.results.add(data);
    }

    public AuthzenResponse(List<T> results, boolean successful, String message) {
        this.status  = successful ? SUCCESSFUL : UNSUCCESSFUL;
        this.results = (results != null ? results : new ArrayList<>());
        this.message = message;
    }

    public static AuthzenResponse failure(String message) {
        AuthzenResponse r = new AuthzenResponse();
        r.status  = UNSUCCESSFUL;
        r.message = message;
        return r;
    }

    public static <T> AuthzenResponse<T> success(T result, String message) {
        AuthzenResponse<T> r = new AuthzenResponse<>();
        r.status  = SUCCESSFUL;
        r.message = message;
        if (result != null) r.results.add(result);
        return r;
    }
}
