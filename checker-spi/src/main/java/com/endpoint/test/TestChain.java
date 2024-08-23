/*
package com.endpoint.test;

import com.endpoint.rasp.checker.CheckChain;
import com.endpoint.rasp.checker.Checker;
import com.endpoint.rasp.checker.DefaultCheckChain;
import com.endpoint.rasp.checker.GenericChecker;

import java.util.ArrayList;
import java.util.HashSet;

*/
/**
 * @author: feiwoscun
 * @date: 2024/7/30
 * @email: 2825097536@qq.com
 * @description:
 *//*

public class TestChain {

    private final ArrayList<Checker> checkers;
    private Checker checker1 = new GenericChecker(new HashSet<>(),null) {
        @Override
        public boolean isMatch(String method) {
            return true;
        }

        @Override
        public boolean check(Object[] args, String method, CheckChain checkChain) {
            System.out.println("checker1");
            return checkChain.doCheckChain();
        }
    };
    private Checker checker2 = new GenericChecker(new HashSet<>(),null) {
        @Override
        public boolean isMatch(String method) {
            return true;
        }

        @Override
        public boolean check(Object[] args, String method, CheckChain checkChain) {
            System.out.println("checker2");
            return checkChain.doCheckChain();
        }
    };

    public TestChain() {
        ArrayList<Checker> checkers = new ArrayList<>();
        checkers.add(checker1);
        checkers.add(checker2);
        this.checkers = checkers;
    }

    public ArrayList<Checker> getCheckers() {
        return checkers;
    }

    public static void main(String[] args) {
        TestChain testChain = new TestChain();
        CheckChain checkChain = new DefaultCheckChain(null, testChain.getCheckers().toArray(new Checker[0]), null);
        checkChain.doCheckChain();
    }
}
*/
