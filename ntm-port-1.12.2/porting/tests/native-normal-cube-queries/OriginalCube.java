/** Test-only callback matrix on the actual original engine Block, not a copied predicate. */
public final class OriginalCube extends aji {
    public boolean full, power;
    public OriginalCube(awt material, boolean full, boolean power) { super(material); this.full=full; this.power=power; }
    @Override public boolean d() { return full; }
    @Override public boolean f() { return power; }
}
