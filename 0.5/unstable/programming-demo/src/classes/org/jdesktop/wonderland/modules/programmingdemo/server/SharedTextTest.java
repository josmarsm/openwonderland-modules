/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.programmingdemo.server;

import org.jdesktop.wonderland.modules.programmingdemo.server.SharedText.AddTransform;
import org.jdesktop.wonderland.modules.programmingdemo.server.SharedText.DeleteTransform;
import org.jdesktop.wonderland.modules.programmingdemo.server.SharedText.OldRevisionException;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * Test class for shared text
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class SharedTextTest {
    private static final MyClientID CLIENTID = new MyClientID();

    public static void main(String args[]) {
        SharedTextTest test = new SharedTextTest();

        try {
            test.doTest1();
            test.doTest2();
            test.doTest3();
            test.doTest4();
            test.doTest5();
            test.doTest6();
            test.doTest7();
            test.doTest8();
            test.doTest9();
            test.doTest10();
            test.doTest11();
            test.doTest12();
            test.doTest13();
            test.doTest14();
            test.doTest15();
            test.doTest16();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void doTest1() throws OldRevisionException, TestFailureException {
        String name = "test1";

        // test 1: simple insert
        SharedText st = new SharedText("abcdefghij");
        st.apply(CLIENTID, 0, new AddTransform(3, "qrs"));

        String expected = "abcqrsdefghij";
        System.out.println(name + ": " + st.getText());
        if (!st.getText().equals(expected)) {
            throw new TestFailureException(name, expected, st.getText());
        }
    }

    public void doTest2() throws OldRevisionException, TestFailureException {
        String name = "test2";

        // test 2: simple delete
        SharedText st = new SharedText("abcdefghij");
        st.apply(CLIENTID, 0, new DeleteTransform(3, 3));

        String expected = "abcghij";
        System.out.println(name + ": " + st.getText());
        if (!st.getText().equals(expected)) {
            throw new TestFailureException(name, expected, st.getText());
        }
    }

    public void doTest3() throws OldRevisionException, TestFailureException {
        String name = "test3";

        // test 3: add then delete before insertion
        SharedText st = new SharedText("abcdefghij");
        st.apply(CLIENTID, 0, new AddTransform(3, "qrs"));
        st.apply(CLIENTID, 0, new DeleteTransform(0, 2));

        String expected = "cqrsdefghij";
        System.out.println(name + ": " + st.getText());
        if (!st.getText().equals(expected)) {
            throw new TestFailureException(name, expected, st.getText());
        }
    }

    public void doTest4() throws OldRevisionException, TestFailureException {
        String name = "test4";

        // test 3: add then delete after insertion
        SharedText st = new SharedText("abcdefghij");
        st.apply(CLIENTID, 0, new AddTransform(3, "qrs"));
        st.apply(CLIENTID, 0, new DeleteTransform(3, 3));

        String expected = "abcqrsghij";
        System.out.println(name + ": " + st.getText());
        if (!st.getText().equals(expected)) {
            throw new TestFailureException(name, expected, st.getText());
        }
    }

    public void doTest5() throws OldRevisionException, TestFailureException {
        String name = "test5";

        // test 3: add then delete containing insertion
        SharedText st = new SharedText("abcdefghij");
        st.apply(CLIENTID, 0, new AddTransform(3, "qrs"));
        st.apply(CLIENTID, 0, new DeleteTransform(1, 4));

        String expected = "aqrsfghij";
        System.out.println(name + ": " + st.getText());
        if (!st.getText().equals(expected)) {
            throw new TestFailureException(name, expected, st.getText());
        }
    }

    public void doTest6() throws OldRevisionException, TestFailureException {
        String name = "test6";

        // test 6: add then add before insertion
        SharedText st = new SharedText("abcdefghij");
        st.apply(CLIENTID, 0, new AddTransform(3, "qrs"));
        st.apply(CLIENTID, 0, new AddTransform(1, "xyz"));

        String expected = "axyzbcqrsdefghij";
        System.out.println(name + ": " + st.getText());
        if (!st.getText().equals(expected)) {
            throw new TestFailureException(name, expected, st.getText());
        }
    }

    public void doTest7() throws OldRevisionException, TestFailureException {
        String name = "test7";

        // test 6: add then add after insertion
        SharedText st = new SharedText("abcdefghij");
        st.apply(CLIENTID, 0, new AddTransform(3, "qrs"));
        st.apply(CLIENTID, 0, new AddTransform(5, "xyz"));

        String expected = "abcqrsdexyzfghij";
        System.out.println(name + ": " + st.getText());
        if (!st.getText().equals(expected)) {
            throw new TestFailureException(name, expected, st.getText());
        }
    }

    public void doTest8() throws OldRevisionException, TestFailureException {
        String name = "test8";

        // test 8: delete then add before deletion
        SharedText st = new SharedText("abcdefghij");
        st.apply(CLIENTID, 0, new DeleteTransform(3, 3));
        st.apply(CLIENTID, 0, new AddTransform(1, "qrs"));

        String expected = "aqrsbcghij";
        System.out.println(name + ": " + st.getText());
        if (!st.getText().equals(expected)) {
            throw new TestFailureException(name, expected, st.getText());
        }
    }

    public void doTest9() throws OldRevisionException, TestFailureException {
        String name = "test9";

        // test 9: delete then add after deletion
        SharedText st = new SharedText("abcdefghij");
        st.apply(CLIENTID, 0, new DeleteTransform(3, 3));
        st.apply(CLIENTID, 0, new AddTransform(6, "qrs"));

        String expected = "abcqrsghij";
        System.out.println(name + ": " + st.getText());
        if (!st.getText().equals(expected)) {
            throw new TestFailureException(name, expected, st.getText());
        }
    }

    public void doTest10() throws OldRevisionException, TestFailureException {
        String name = "test10";

        // test 10: delete then add inside the deletion
        SharedText st = new SharedText("abcdefghij");
        st.apply(CLIENTID, 0, new DeleteTransform(3, 3));
        st.apply(CLIENTID, 0, new AddTransform(4, "qrs"));

        String expected = "abcqrsghij";
        System.out.println(name + ": " + st.getText());
        if (!st.getText().equals(expected)) {
            throw new TestFailureException(name, expected, st.getText());
        }
    }

    public void doTest11() throws OldRevisionException, TestFailureException {
        String name = "test11";

        // test 11: delete then delete before the deletion
        SharedText st = new SharedText("abcdefghij");
        st.apply(CLIENTID, 0, new DeleteTransform(3, 3));
        st.apply(CLIENTID, 0, new DeleteTransform(0, 3));

        String expected = "ghij";
        System.out.println(name + ": " + st.getText());
        if (!st.getText().equals(expected)) {
            throw new TestFailureException(name, expected, st.getText());
        }
    }

    public void doTest12() throws OldRevisionException, TestFailureException {
        String name = "test12";

        // test 12: delete then delete after the deletion
        SharedText st = new SharedText("abcdefghij");
        st.apply(CLIENTID, 0, new DeleteTransform(3, 3));
        st.apply(CLIENTID, 0, new DeleteTransform(6, 3));

        String expected = "abcj";
        System.out.println(name + ": " + st.getText());
        if (!st.getText().equals(expected)) {
            throw new TestFailureException(name, expected, st.getText());
        }
    }

    public void doTest13() throws OldRevisionException, TestFailureException {
        String name = "test13";

        // test 13: delete then delete including the start of the deletion
        SharedText st = new SharedText("abcdefghij");
        st.apply(CLIENTID, 0, new DeleteTransform(3, 3));
        st.apply(CLIENTID, 0, new DeleteTransform(2, 3));

        String expected = "abghij";
        System.out.println(name + ": " + st.getText());
        if (!st.getText().equals(expected)) {
            throw new TestFailureException(name, expected, st.getText());
        }
    }

    public void doTest14() throws OldRevisionException, TestFailureException {
        String name = "test14";

        // test 14: delete then delete including the end of the deletion
        SharedText st = new SharedText("abcdefghij");
        st.apply(CLIENTID, 0, new DeleteTransform(3, 3));
        st.apply(CLIENTID, 0, new DeleteTransform(5, 3));

        String expected = "abcij";
        System.out.println(name + ": " + st.getText());
        if (!st.getText().equals(expected)) {
            throw new TestFailureException(name, expected, st.getText());
        }
    }

    public void doTest15() throws OldRevisionException, TestFailureException {
        String name = "test15";

        // test 15: delete then delete included entirely in our deletion
        SharedText st = new SharedText("abcdefghij");
        st.apply(CLIENTID, 0, new DeleteTransform(3, 5));
        st.apply(CLIENTID, 0, new DeleteTransform(5, 2));

        String expected = "abcij";
        System.out.println(name + ": " + st.getText());
        if (!st.getText().equals(expected)) {
            throw new TestFailureException(name, expected, st.getText());
        }
    }

    public void doTest16() throws OldRevisionException, TestFailureException {
        String name = "test16";

        // test 16: delete then delete that entirely includes the deletion
        SharedText st = new SharedText("abcdefghij");
        st.apply(CLIENTID, 0, new DeleteTransform(3, 3));
        st.apply(CLIENTID, 0, new DeleteTransform(2, 6));

        String expected = "abij";
        System.out.println(name + ": " + st.getText());
        if (!st.getText().equals(expected)) {
            throw new TestFailureException(name, expected, st.getText());
        }
    }

    class TestFailureException extends Exception {
        public TestFailureException(String name, String expected, String actual) {
            super ("Test " + name + " failed. Expected: " + expected +
                   ", actual: " + actual + ".");
        }
    }

    static class MyClientID extends WonderlandClientID {
        @Override
        public boolean equals(Object obj) {
            return false;
        }
    }
}
