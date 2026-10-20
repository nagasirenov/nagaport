import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/** Runs cached, unmodified 1.7.10 Minecraft bytecode, independently of native sound choices. */
public final class LegacyBlockSoundOracle {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        try (URLClassLoader loader = new URLClassLoader(new URL[] {new File(args[0]).toURI().toURL()},
                LegacyBlockSoundOracle.class.getClassLoader())) {
            Class<?> block = Class.forName("aji", true, loader);
            Class<?> type = Class.forName("ajm", true, loader);
            Object stone = block.getField("i").get(null), metal = block.getField("j").get(null);
            for (Object sound : new Object[] {stone, metal}) {
                check(type.getField("a").get(sound).equals("stone"), "Original stone and metal both use stone samples");
                check(type.getMethod("c").invoke(sound).equals(1.0F), "Original volume is 1");
                check(type.getMethod("a").invoke(sound).equals("dig.stone"), "Original break is dig.stone");
                check(type.getMethod("b").invoke(sound).equals("dig.stone"), "Original place is dig.stone");
                check(type.getMethod("e").invoke(sound).equals("step.stone"), "Original step is step.stone");
            }
            check(type.getMethod("d").invoke(stone).equals(1.0F), "Original stone pitch");
            check(type.getMethod("d").invoke(metal).equals(1.5F), "Original metal differs only by pitch");
            System.out.println("PASS legacy sound bytecode oracle: " + checks + " assertions; stone and metal use dig.stone / step.stone, pitches 1.0 / 1.5");
        }
    }
}
