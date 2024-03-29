/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.collections4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.collections4.functors.ConstantFactory;
import org.apache.commons.collections4.functors.ExceptionFactory;
import org.junit.Test;

/**
 * Tests the org.apache.commons.collections.FactoryUtils class.
 *
 * @since 3.0
 * @version $Id: FactoryUtilsTest.java 1540705 2013-11-11 13:22:32Z ebourg $
 */
public class FactoryUtilsTest extends junit.framework.TestCase {

    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() {
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
    }

    // exceptionFactory
    //------------------------------------------------------------------

    public void testExceptionFactory() {
        assertNotNull(FactoryUtils.exceptionFactory());
        assertSame(FactoryUtils.exceptionFactory(), FactoryUtils.exceptionFactory());
        try {
            FactoryUtils.exceptionFactory().create();
        } catch (final FunctorException ex) {
            try {
                FactoryUtils.exceptionFactory().create();
            } catch (final FunctorException ex2) {
                return;
            }
        }
        fail();
    }

    // nullFactory
    //------------------------------------------------------------------

    public void testNullFactory() {
        final Factory<Object> factory = FactoryUtils.nullFactory();
        assertNotNull(factory);
        final Object created = factory.create();
        assertNull(created);
    }

    // constantFactory
    //------------------------------------------------------------------

    public void testConstantFactoryNull() {
        final Factory<Object> factory = FactoryUtils.constantFactory(null);
        assertNotNull(factory);
        final Object created = factory.create();
        assertNull(created);
    }

    public void testConstantFactoryConstant() {
        final Integer constant = Integer.valueOf(9);
        final Factory<Integer> factory = FactoryUtils.constantFactory(constant);
        assertNotNull(factory);
        final Integer created = factory.create();
        assertSame(constant, created);
    }

    // prototypeFactory
    //------------------------------------------------------------------

    public void testPrototypeFactoryNull() {
        assertSame(ConstantFactory.NULL_INSTANCE, FactoryUtils.prototypeFactory(null));
    }

    public void testPrototypeFactoryPublicCloneMethod() throws Exception {
        final Date proto = new Date();
        final Factory<Date> factory = FactoryUtils.prototypeFactory(proto);
        assertNotNull(factory);
        final Date created = factory.create();
        assertTrue(proto != created);
        assertEquals(proto, created);

        // check serialisation works
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(factory);
        out.close();
        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        in.readObject();
        in.close();
    }

    public void testPrototypeFactoryPublicCopyConstructor() throws Exception {
        final Mock1 proto = new Mock1(6);
        Factory<Object> factory = FactoryUtils.<Object>prototypeFactory(proto);
        assertNotNull(factory);
        final Object created = factory.create();
        assertTrue(proto != created);
        assertEquals(proto, created);

        // check serialisation works
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        try {
            out.writeObject(factory);
        } catch (final NotSerializableException ex) {
            out.close();
        }
        factory = FactoryUtils.<Object>prototypeFactory(new Mock2("S"));
        buffer = new ByteArrayOutputStream();
        out = new ObjectOutputStream(buffer);
        out.writeObject(factory);
        out.close();
        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        in.readObject();
        in.close();
    }

    public void testPrototypeFactoryPublicSerialization() throws Exception {
        final Integer proto = Integer.valueOf(9);
        final Factory<Integer> factory = FactoryUtils.prototypeFactory(proto);
        assertNotNull(factory);
        final Integer created = factory.create();
        assertTrue(proto != created);
        assertEquals(proto, created);

        // check serialisation works
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(factory);
        out.close();
        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        in.readObject();
        in.close();
    }

    public void testPrototypeFactoryPublicSerializationError() {
        final Mock2 proto = new Mock2(new Object());
        final Factory<Object> factory = FactoryUtils.<Object>prototypeFactory(proto);
        assertNotNull(factory);
        try {
            factory.create();
        } catch (final FunctorException ex) {
            assertTrue(ex.getCause() instanceof IOException);
            return;
        }
        fail();
    }

    public void testPrototypeFactoryPublicBad() {
        final Object proto = new Object();
        try {
            FactoryUtils.prototypeFactory(proto);
        } catch (final IllegalArgumentException ex) {
            return;
        }
        fail();
    }

    public static class Mock1 {
        private final int iVal;
        public Mock1(final int val) {
            iVal = val;
        }
        public Mock1(final Mock1 mock) {
            iVal = mock.iVal;
        }
        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Mock1) {
                if (iVal == ((Mock1) obj).iVal) {
                    return true;
                }
            }
            return false;
        }
        @Override
        public int hashCode() { // please Findbugs
            return super.hashCode();
        }
    }

    public static class Mock2 implements Serializable {
        /**
         * Generated serial version ID.
         */
        private static final long serialVersionUID = 4899282162482588924L;
        private final Object iVal;
        public Mock2(final Object val) {
            iVal = val;
        }
        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Mock2) {
                if (iVal == ((Mock2) obj).iVal) {
                    return true;
                }
            }
            return false;
        }
        @Override
        public int hashCode() { // please Findbugs
            return super.hashCode();
        }
    }

    public static class Mock3 {
        private static int cCounter = 0;
        private final int iVal;
        public Mock3() {
            iVal = cCounter++;
        }
        public int getValue() {
            return iVal;
        }
    }

    // instantiateFactory
    //------------------------------------------------------------------

    @Test(expected=IllegalArgumentException.class)
    public void instantiateFactoryNull() {
        FactoryUtils.instantiateFactory(null);
    }

    @Test
    public void instantiateFactorySimple() {
        final Factory<Mock3> factory = FactoryUtils.instantiateFactory(Mock3.class);
        assertNotNull(factory);
        Mock3 created = factory.create();
        assertEquals(0, created.getValue());
        created = factory.create();
        assertEquals(1, created.getValue());
    }

    @Test(expected=IllegalArgumentException.class)
    public void instantiateFactoryMismatch() {
        FactoryUtils.instantiateFactory(Date.class, null, new Object[] {null});
    }

    @Test(expected=IllegalArgumentException.class)
    public void instantiateFactoryNoConstructor() {
        FactoryUtils.instantiateFactory(Date.class, new Class[] {Long.class}, new Object[] {null});
    }

    @Test
    public void instantiateFactoryComplex() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        // 2nd Jan 1970
        final Factory<Date> factory = FactoryUtils.instantiateFactory(Date.class,
            new Class[] {Integer.TYPE, Integer.TYPE, Integer.TYPE},
            new Object[] {Integer.valueOf(70), Integer.valueOf(0), Integer.valueOf(2)});
        assertNotNull(factory);
        final Date created = factory.create();
        // long time of 1 day (== 2nd Jan 1970)
        assertEquals(new Date(1000 * 60 * 60 * 24), created);
    }

    // misc tests
    //------------------------------------------------------------------

    /**
     * Test that all Factory singletones hold singleton pattern in
     * serialization/deserialization process.
     */
    public void testSingletonPatternInSerialization() {
        final Object[] singletones = new Object[] {
                ExceptionFactory.INSTANCE,
        };

        for (final Object original : singletones) {
            TestUtils.assertSameAfterSerialization(
                    "Singletone patern broken for " + original.getClass(),
                    original
            );
        }
    }

}
