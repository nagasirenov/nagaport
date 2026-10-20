# Independent DiFurnace positive-fuel depletion correction

This patch changes only callback receiver selection and canonical absence
conversion at the existing fuel-depletion branch. It does not resolve the six
stackSize diagnostics, modify quantities, or activate a nonpositive input policy.

The complete CP79 source reverses byte-exactly using source.json. Inspection of
the entire update method establishes that fuel power checks and increment still
precede the branch; markDirty is still set before decrement; the same fuel stack
is decremented once; callback executes once iff its quantity becomes exactly
zero; processing, pollution, progress, networking and dirty updates are unchanged.
The captured Item getter is pure on the final native ItemStack type. For positive
entry it retains the same receiver the original engine exposed after depletion.
The callback still receives the original depleted stack, not a positive copy.

A native no-container callback returns canonical EMPTY instead of legacy null.
Only that singleton is converted to null when stored in this nullable internal
array. Noncanonical zero/negative callback objects and returned aliases are NOT
normalized. This is a normal positive-fuel transient-zero correction, not the
pending policy for admission of invalid/nonpositive stacks.

run.py extracts the actual changed consumption block and performs only two
explicit test-only quantity adaptations (shrink/getCount). These are valid for
the tested positive-entry counts, and are NOT applied to production. Fifty
native checks cover lava bucket residue, coal/blaze fuel absence, counts1/2/64,
no premature callback, actual depleted callback argument identity, returned
alias identity, noncanonical zero residue preservation, canonical absence and
null-slot no-op. Two reverted-behavior mutation controls both fail as expected.

This is block-level native runtime proof plus whole-source invariance, NOT full
DiFurnace compilation/execution, original-engine callback execution, or gameplay
acceptance. Whole-machine quantity migration remains blocked separately.

Reproduce: python3 porting/tests/difurnace-fuel-receiver/run.py
Uses existing Java8 and /tmp/nagaport-ae2-native-classpath.txt; no downloads.
