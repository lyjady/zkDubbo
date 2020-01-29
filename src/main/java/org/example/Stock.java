package org.example;

/**
 * @author LinYongJin
 * @date 2020/1/29 16:52
 */
public class Stock {
    private static int num = 1;

    public boolean reduce() {
        if (num > 0) {
            num--;
            return true;
        }
        return false;
    }

}
