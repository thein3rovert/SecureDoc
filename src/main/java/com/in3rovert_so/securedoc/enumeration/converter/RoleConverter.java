package com.in3rovert_so.securedoc.enumeration.converter;

import com.in3rovert_so.securedoc.enumeration.Authority;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Authority, String> {

    /**
     ===================
     * Converts an Authority object to its corresponding string representation for storage in the database.
     *
     * @param  authority  the Authority object to be converted
     * @return            the string representation of the Authority object, or null if the input is null
     ====================
     */
    @Override
    public String convertToDatabaseColumn(Authority authority) {
        if (authority == null) {
            return null;
        }
        return authority.getValue();
    }

        /**
         ====================
         * Converts a string code(value) to its corresponding Authority enum value -> (if(code(value) = value))
         *
         * @param  code  the string code to be converted
         * @return       the Authority enum value corresponding to the code, or null if the input is null
         * @throws IllegalArgumentException if no corresponding Authority enum value is found for the code
        ====================
         */
    @Override
    public Authority convertToEntityAttribute(String code) {
        if(code == null) {
            return null;
        }
        // Create a stream of all the Authority enum values
        return Stream.of(Authority.values())
                // Filter the stream to only include the Authority enum value that matches the code(values) in the database
                .filter(authority -> authority.getValue().equals(code))
                // Get the first matching value. If no value matches, throw an exception
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
