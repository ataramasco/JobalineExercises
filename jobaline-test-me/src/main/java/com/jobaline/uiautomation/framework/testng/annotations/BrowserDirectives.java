package com.jobaline.uiautomation.framework.testng.annotations;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to send directives on whether start or not the browser and close or not the browser
 */
@Retention(RUNTIME)
public @interface BrowserDirectives
{

	boolean start() default true;

	boolean close() default true;
}
