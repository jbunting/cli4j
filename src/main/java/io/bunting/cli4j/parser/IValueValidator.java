package io.bunting.cli4j.parser;

import io.bunting.cli4j.parser.ParameterException;

public interface IValueValidator<T> {
  /**
   * Validate the parameter.
   *
   * @param name The name of the parameter (e.g. "-host").
   * @param value The value of the parameter that we need to validate
   *
   * @throws io.bunting.cli4j.parser.ParameterException Thrown if the value of the parameter is invalid.
   */
  void validate(String name, T value) throws ParameterException;

}
