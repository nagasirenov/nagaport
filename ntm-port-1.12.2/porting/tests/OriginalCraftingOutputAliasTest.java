package craftingproof;

/** Executes genuine signed obfuscated 1.7.10 classes; no repackaging or platform doubles. */
public final class OriginalCraftingOutputAliasTest {
    public static void main(String[] args) throws Exception {
        Class<?> item = Class.forName("adb"), stack = Class.forName("add"), tag = Class.forName("dh");
        java.lang.reflect.Constructor<?> stackCtor = stack.getConstructor(item, int.class, int.class);
        for (boolean shaped : new boolean[] {true, false}) {
            Object output = stackCtor.newInstance(item.newInstance(), 2, 3);
            Object nbt = tag.newInstance(); tag.getMethod("a", String.class, String.class).invoke(nbt, "value", "before");
            stack.getField("d").set(output, nbt);
            Object input = stackCtor.newInstance(item.newInstance(), 1, 0), recipe;
            if (shaped) {
                Object array = java.lang.reflect.Array.newInstance(stack, 1); java.lang.reflect.Array.set(array, 0, input);
                recipe = Class.forName("afh").getConstructor(int.class, int.class, array.getClass(), stack).newInstance(1, 1, array, output);
            } else recipe = Class.forName("afi").getConstructor(stack, java.util.List.class).newInstance(output, java.util.Arrays.asList(input));
            Object stored = recipe.getClass().getMethod("b").invoke(recipe);
            if (stored != output) throw new AssertionError("original output reference");
            stack.getField("b").setInt(output, 7); tag.getMethod("a", String.class, String.class).invoke(nbt, "value", "after");
            if (stack.getField("b").getInt(stored) != 7 || !tag.getMethod("j", String.class).invoke(stack.getField("d").get(stored), "value").equals("after")) throw new AssertionError("original mutations");
            Object result = recipe.getClass().getMethod("a", Class.forName("aae")).invoke(recipe, new Object[] {null});
            Object resultNbt = stack.getField("d").get(result);
            if (result == output || resultNbt == nbt || stack.getField("b").getInt(result) != 7 || !tag.getMethod("j", String.class).invoke(resultNbt, "value").equals("after")) throw new AssertionError("original result deep copy");
            stack.getField("b").setInt(result, 1); tag.getMethod("a", String.class, String.class).invoke(resultNbt, "value", "result-only");
            if (stack.getField("b").getInt(output) != 7 || !tag.getMethod("j", String.class).invoke(nbt, "value").equals("after")) throw new AssertionError("original result isolation");
            System.out.println("PASS authentic 1.7 " + (shaped ? "afh shaped" : "afi shapeless") + " output alias/count/NBT mutations and result deep copy");
        }
    }
}
