package net.tslat.smartbrainlib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods marked with this annotation should only be used internally, or by abstract-subclasses.
 * This is usually done to avoid accidentally overriding methods handled in the super class.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface APIOnly {}
