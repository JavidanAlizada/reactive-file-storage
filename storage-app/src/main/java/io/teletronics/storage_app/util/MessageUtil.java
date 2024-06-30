package io.teletronics.storage_app.util;

import io.teletronics.storage_app.exception.ErrorMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MessageUtil {
    private final MessageSource messageSource;

    @Autowired
    public MessageUtil(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(ErrorMessages errorMessages) {
        return messageSource.getMessage(errorMessages.getMessageKey(), null, LocaleContextHolder.getLocale());
    }
}
