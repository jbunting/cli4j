package io.bunting.cli4j;

import com.beust.jcommander.JCommander;

import java.io.Console;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Document this class
 */
public class Cli4J {
  private final CliAdapter cliAdapter;
  private final List<Class<?>> resources = new ArrayList<>();
  private final Map<String, Mapping> mappings = new HashMap<>();

  public Cli4J(CliAdapter cliAdapter) {
    this.cliAdapter = cliAdapter;
  }

  public Cli4J() {
    this(new ConsoleCliAdapter(System.console()));
  }

  public void addResource(final Class<?> commandClass) {
    this.resources.add(commandClass);
    List<Mapping> resourceMappings = extractMappings(commandClass);
    for (Mapping mapping: resourceMappings) {
      this.mappings.put(mapping.name, mapping);
    }

  }

  private List<Mapping> extractMappings(Class<?> commandClass) {
    final List<Mapping> mappings = new ArrayList<>();
    for (Method method: commandClass.getMethods()) {
      if (method.isAnnotationPresent(Command.class)) {
        Command command = method.getAnnotation(Command.class);
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 2) {
          throw new IllegalStateException("Right now, command methods MUST take two parameters -- a Console and a Args object.");
        }
        Mapping mapping = new Mapping(commandClass, command.name(), method, parameterTypes[1]);
        mappings.add(mapping);
      }
    }
    return mappings;
  }

  public void execute(final String... args) {
    JCommander jCommander = new JCommander();
    Map<String, Object> mappingObjects = new HashMap<>();
    for (Mapping mapping: this.mappings.values()) {
      Object object = mapping.newArgClass();
      jCommander.addCommand(mapping.name, object);
      mappingObjects.put(mapping.name, object);
    }
    jCommander.parse(args);
    String commandName = jCommander.getParsedCommand();
    Mapping mapping = mappings.get(commandName);
    Object argsObject = mappingObjects.get(commandName);
    mapping.invoke(cliAdapter, argsObject);
  }

  static class Mapping {
    private final Class<?> resourceClass;
    private final String name;
    private final Method method;
    private final Class<?> argClass;

    public Mapping(Class<?> resourceClass, String name, Method method, Class<?> argClass) {
      this.resourceClass = resourceClass;
      this.name = name;
      this.method = method;
      this.argClass = argClass;
    }

    public Object newArgClass() {
      try {
        return argClass.newInstance();
      } catch (InstantiationException|IllegalAccessException e) {
        throw new RuntimeException("Failed to instantiate arg class for command " + this.name, e);
      }
    }

    public void invoke(CliAdapter cliAdapter, Object args) {
      try {
        Object resource = resourceClass.newInstance();
        this.method.invoke(resource, cliAdapter, args);
      } catch (InstantiationException | IllegalAccessException e) {
        throw new RuntimeException("Failed to instantiate resource class " + this.resourceClass + " for command " + this.name, e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException("Failed to invoke command " + this.name + " on resource class " + this.resourceClass, e);
      }
    }
  }
}
