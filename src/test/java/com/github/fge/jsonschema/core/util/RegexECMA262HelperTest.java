/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of this file and of both licenses is available at the root of this
 * project or, if you have the jar distribution, in directory META-INF/, under
 * the names LGPL-3.0.txt and ASL-2.0.txt respectively.
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package com.github.fge.jsonschema.core.util;

import com.google.common.collect.ImmutableList;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Iterator;

import static org.testng.Assert.*;

public final class RegexECMA262HelperTest
{
    private static Object rhinoScriptInstance;
    private static Method regexIsValid;
    private static Method regMatch;

    /**
     * Depending on the JDK version the Rhino fallback engine will
     * not be tested. To ensure functionality for Rhino, too, prepare
     * tests for it via reflection.
     */
    static
    {
        try {
            final Class<?> rhinoScriptClass = Class.forName(
                    RegexECMA262Helper.class.getName() + "$RhinoScript");
            final Constructor<?> constructor = rhinoScriptClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            rhinoScriptInstance = constructor.newInstance();
            regexIsValid = rhinoScriptInstance.getClass()
                    .getDeclaredMethod("regexIsValid", String.class);
            regMatch = rhinoScriptInstance.getClass()
                    .getDeclaredMethod("regMatch", String.class, String.class);
        } catch (final Exception e) {
            throw new IllegalStateException("Can't initialize RhinoScript.", e);
        }
    }

    private static boolean rhinoScript(final Method method, final Object... args)
    {
        try {
            return (boolean) method.invoke(rhinoScriptInstance, args);
        } catch (final Exception e) {
            throw new IllegalStateException("Can't invoke method on RhinoScript.");
        }
    }

    @DataProvider
    public Iterator<Object[]> ecma262regexes()
    {
        return ImmutableList.of(
            new Object[] { "[^]", true },
            new Object[] { "(?<=foobar", false },
            new Object[] { "", true },
            new Object[] { "[a-z]+(?!foo)(?=bar)", true }
        ).iterator();
    }

    @Test(
        dataProvider = "ecma262regexes",
        invocationCount = 10,
        threadPoolSize = 4
    )
    @SuppressWarnings("deprecation")
    public void regexesAreCorrectlyAnalyzed(final String regex,
        final boolean valid)
    {
        assertEquals(RegexECMA262Helper.regexIsValid(regex), valid);
        assertEquals(RhinoHelper.regexIsValid(regex), valid);
        assertEquals(rhinoScript(regexIsValid, regex), valid);
    }

    @DataProvider
    public Iterator<Object[]> regexTestCases()
    {
        return ImmutableList.of(
            new Object[] { "[^a-z]", "9am", true },
            new Object[] { "bar\\d+", "foobar19ae", true },
            new Object[] { "^bar\\d+", "bar", false },
            new Object[] { "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$", "QmFzZTY0IFN0cmluZwo=", true }, // Base64 regex match
            new Object[] { "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$", "QmFzZTY0IFN0cmluZwo", false }, // Base64 regex match
            new Object[] { "[a-z]+(?!foo)(?=bar)", "3aaaabar", true }
        ).iterator();
    }

    @Test(
        dataProvider = "regexTestCases",
        invocationCount = 10,
        threadPoolSize = 4
    )
    @SuppressWarnings("deprecation")
    public void regexMatchingIsDoneCorrectly(final String regex,
        final String input, final boolean valid)
    {
        assertEquals(RegexECMA262Helper.regMatch(regex, input), valid);
        assertEquals(RhinoHelper.regMatch(regex, input), valid);
        assertEquals(rhinoScript(regMatch, regex, input), valid);
    }
}
