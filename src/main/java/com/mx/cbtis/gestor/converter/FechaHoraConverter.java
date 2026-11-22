package com.mx.cbtis.gestor.converter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = false)
public class FechaHoraConverter
        implements AttributeConverter<String, Timestamp> {

    private static final DateTimeFormatter FORMATO =
            new DateTimeFormatterBuilder()
                    .appendPattern("uuuu-MM-dd HH:mm:ss")
                    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                    .toFormatter();

    @Override
    public Timestamp convertToDatabaseColumn(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        LocalDateTime fechaHora =
                LocalDateTime.parse(valor.trim().replace(' ', 'T'));

        return Timestamp.valueOf(fechaHora);
    }

    @Override
    public String convertToEntityAttribute(Timestamp valor) {
        return valor == null
                ? null
                : valor.toLocalDateTime().format(FORMATO);
    }
}