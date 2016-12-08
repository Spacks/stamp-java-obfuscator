package wtf.pants.stamp.mapping;

import lombok.Getter;
import wtf.pants.stamp.mapping.exceptions.ClassMapNotFoundException;
import wtf.pants.stamp.mapping.obj.ClassMap;
import wtf.pants.stamp.mapping.obj.MethodObj;

import java.util.*;

/**
 * @author Spacks
 */
public class ClassCollector {

    @Getter
    private final List<ClassMap> classes;

    private final List<String> classesNotFound;

    public ClassCollector() {
        this.classes = new ArrayList<>();
        this.classesNotFound = new ArrayList<>();
    }

    /**
     * Adds a class to the collector
     *
     * @param classMap ClassMap instance
     */
    public void addClass(ClassMap classMap) {
        this.classes.add(classMap);
    }

    /**
     * Looks for a ClassMap from all the mapped classes
     *
     * @param className Class name you're looking for
     * @return Returns ClassMap
     * @throws ClassMapNotFoundException Exception thrown if className was not found
     */
    public ClassMap getClassMap(String className) throws ClassMapNotFoundException {
        //TODO: Maybe get rid of this, in the end will it actually make a difference?
        //In cases of large amounts of classes, hopefully will avoid going through many
        if (classesNotFound.contains(className))
            throw new ClassMapNotFoundException();

        Optional<ClassMap> classMap =
                classes.stream().filter(c -> c.getClassName().equals(className)).findFirst();

        if (classMap.isPresent()) {
            return classMap.get();
        } else {
            classesNotFound.add(className);
            throw new ClassMapNotFoundException();
        }
    }

    /**
     * If it has one, this will get the class' parent class
     *
     * @param classMap Mapped class
     * @return Returns parent ClassMap
     * @throws ClassMapNotFoundException Throws ClassMapNotFoundException if the parent is not mapped
     */
    public ClassMap getParent(ClassMap classMap) throws ClassMapNotFoundException {
        final String parentClassName = classMap.getParent();

        Optional<ClassMap> optional = classes.stream()
                .filter(clazz -> parentClassName.equals(clazz.getClassName()))
                .findAny();

        if (optional.isPresent())
            return optional.get();
        else
            throw new ClassMapNotFoundException(classMap.getParent());
    }

    /**
     * Tries to get the class' overridden methods by comparing the child's methods to the parent's
     *
     * @param parentClass Parent class to compare to
     * @param childClass  Child class to compare to
     * @return Returns a list of the overridden methods
     */
    public Map<MethodObj, MethodObj> getOverriddenMethods(ClassMap parentClass, ClassMap childClass) {
        final Map<MethodObj, MethodObj> methods = new HashMap<>();

        parentClass.getMethods().stream()
                .filter(MethodObj::isSafeMethod)
                .forEach(parentMethod -> {
                    for (MethodObj childMethod : childClass.getMethods()) {
                        if (childMethod.isSafeMethod() && childMethod.getMethod().equals(parentMethod.getMethod())) {
                            methods.put(parentMethod, childMethod);
                            break;
                        }
                    }
                });

        return methods;
    }

}