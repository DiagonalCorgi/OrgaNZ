package seng302.Utilities.TypeConverters;

import seng302.Utilities.Enums.BloodType;

import picocli.CommandLine;

/**
 * Converter used by PicoCLI options to select bloodtypes from strings
 */
public class BloodTypeConverter implements CommandLine.ITypeConverter<BloodType> {

    /**
     * Convert a string to a BloodType, matches case insensitive
     * @param value String input from user via PicoCLI
     * @return BloodType object
     * @throws CommandLine.TypeConversionException Throws exception if invalid blood type
     */
    @Override
    public BloodType convert(String value) throws CommandLine.TypeConversionException {
        try {
            return BloodType.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new CommandLine.TypeConversionException(value + " is not a valid blood type. Please enter in the form \"A-\"");
        }
    }
}
