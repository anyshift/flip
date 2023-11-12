package com.flip.utils;

import com.flip.domain.dto.LoggedUser;
import org.springframework.security.core.context.SecurityContextHolder;

public class LoggedUserUtils {

    public static LoggedUser getLoggedUser() {
        return (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
