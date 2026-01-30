package com.daou.dop.gapps.infrastructure.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.stereotype.Component;

/**
 * JPA AttributeConverter - 암호화/복호화 자동 처리
 */
@Converter
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private final StringEncryptor encryptor;

    public EncryptedStringConverter(StringEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute != null ? encryptor.encrypt(attribute) : null;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData != null ? encryptor.decrypt(dbData) : null;
    }
}
