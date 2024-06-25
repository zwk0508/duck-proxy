package a;

/**
 * @author zwk
 * @version 1.0
 * @date 2024/6/25 18:09
 */

public class Target {
    public void run() {
        System.out.println("target run");
    }

    public String say(String name) {
        return "target " + name;
    }
}
