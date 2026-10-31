import os

def update_rad_beast():
    path = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\entity\mob\EntityRADBeast.java"
    with open(path, "rb") as f:
        text = f.read().decode("utf-8").replace("\r\n", "\n")

    # Imports
    target_imp = "import net.minecraft.world.World;"
    repl_imp = """import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;"""
    assert target_imp in text, "rad_beast target_imp"
    text = text.replace(target_imp, repl_imp, 1)

    # Fields
    target_field = "private float heightOffset = 0.5F;"
    repl_field = """private static final DataParameter<Integer> TARGET_ID = EntityDataManager.createKey(EntityRADBeast.class, DataSerializers.VARINT);
\tprotected int attackTime;
\tprotected boolean hasAttacked;

\tprivate float heightOffset = 0.5F;"""
    assert target_field in text, "rad_beast target_field"
    text = text.replace(target_field, repl_field, 1)

    # entityInit
    target_init = """\tprotected void entityInit() {
\t\tsuper.entityInit();
\t\tthis.dataWatcher.addObject(16, (int) 0);
\t}"""
    repl_init = """\t@Override
\tprotected void entityInit() {
\t\tsuper.entityInit();
\t\tthis.dataManager.register(TARGET_ID, Integer.valueOf(0));
\t}"""
    assert target_init in text, "rad_beast target_init"
    text = text.replace(target_init, repl_init, 1)

    # Sounds
    target_sounds = """\tprotected String getLivingSound() {
\t\treturn "hbm:item.geiger" + (1 + rand.nextInt(6));
\t}

\tprotected String getHurtSound() {
\t\treturn "mob.blaze.hit";
\t}

\tprotected String getDeathSound() {
\t\treturn "hbm:step.iron";
\t}"""
    repl_sounds = """\t@Override
\tprotected SoundEvent getAmbientSound() {
\t\treturn com.hbm.main.ModSoundEvents.resolve("hbm:item.geiger" + (1 + rand.nextInt(6)));
\t}

\t@Override
\tprotected SoundEvent getHurtSound(DamageSource damageSourceIn) {
\t\treturn com.hbm.main.ModSoundEvents.resolve("mob.blaze.hit");
\t}

\t@Override
\tprotected SoundEvent getDeathSound() {
\t\treturn com.hbm.main.ModSoundEvents.resolve("hbm:step.iron");
\t}"""
    assert target_sounds in text, "rad_beast target_sounds"
    text = text.replace(target_sounds, repl_sounds, 1)

    # onLivingUpdate entityToAttack & dataWatcher
    target_update = """\t\t\tif(this.getEntityToAttack() != null && this.getEntityToAttack().posY + (double) this.getEntityToAttack().getEyeHeight() > this.posY + (double) this.getEyeHeight() + (double) this.heightOffset) {
\t\t\t\tthis.motionY += (0.30000001192092896D - this.motionY) * 0.30000001192092896D;
\t\t\t}

\t\t\tif(this.entityToAttack != null && attackTime < 10) {

\t\t\t\tif(this.dataWatcher.getWatchableObjectInt(16) != entityToAttack.getEntityId())
\t\t\t\t\tthis.dataWatcher.updateObject(16, entityToAttack.getEntityId());
\t\t\t} else {
\t\t\t\tthis.dataWatcher.updateObject(16, 0);
\t\t\t}"""
    repl_update = """\t\t\tif(this.getAttackTarget() != null && this.getAttackTarget().posY + (double) this.getAttackTarget().getEyeHeight() > this.posY + (double) this.getEyeHeight() + (double) this.heightOffset) {
\t\t\t\tthis.motionY += (0.30000001192092896D - this.motionY) * 0.30000001192092896D;
\t\t\t}

\t\t\tif(this.getAttackTarget() != null && attackTime < 10) {

\t\t\t\tif(this.dataManager.get(TARGET_ID).intValue() != this.getAttackTarget().getEntityId())
\t\t\t\t\tthis.dataManager.set(TARGET_ID, Integer.valueOf(this.getAttackTarget().getEntityId()));
\t\t\t} else {
\t\t\t\tthis.dataManager.set(TARGET_ID, Integer.valueOf(0));
\t\t\t}"""
    assert target_update in text, "rad_beast target_update"
    text = text.replace(target_update, repl_update, 1)

    # onLivingUpdate timer decrement & attack
    target_post_update = """\t\tsuper.onLivingUpdate();
\t}"""
    repl_post_update = """\t\tif(this.attackTime > 0) {
\t\t\t--this.attackTime;
\t\t}

\t\tif(!this.world.isRemote && this.getAttackTarget() != null) {
\t\t\tthis.attackEntity(this.getAttackTarget(), (float) this.getDistance(this.getAttackTarget()));
\t\t}

\t\tsuper.onLivingUpdate();
\t}"""
    assert target_post_update in text, "rad_beast target_post_update"
    text = text.replace(target_post_update, repl_post_update, 1)

    # attackEntity
    target_attack = """\t\t\tif(this.attackTime == 0 && getEntityToAttack() != null) {

\t\t\t\tChunkRadiationManager.proxy.incrementRad(this.world, (int) Math.floor(posX), (int) Math.floor(posY), (int) Math.floor(posZ), 100);
\t\t\t\ttarget.attackEntityFrom(ModDamageSource.radiation, 16.0F);
\t\t\t\tthis.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
\t\t\t\tthis.playLivingSound();
\t\t\t\tthis.attackTime = 20;
\t\t\t}"""
    repl_attack = """\t\t\tif(this.attackTime == 0 && this.getAttackTarget() != null) {

\t\t\t\tChunkRadiationManager.proxy.incrementRad(this.world, (int) Math.floor(posX), (int) Math.floor(posY), (int) Math.floor(posZ), 100);
\t\t\t\ttarget.attackEntityFrom(ModDamageSource.radiation, 16.0F);
\t\t\t\tthis.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
\t\t\t\tthis.playAmbientSound();
\t\t\t\tthis.attackTime = 20;
\t\t\t}"""
    assert target_attack in text, "rad_beast target_attack"
    text = text.replace(target_attack, repl_attack, 1)

    # getUnfortunateSoul
    target_soul = """\tpublic Entity getUnfortunateSoul() {

\t\tint id = this.dataWatcher.getWatchableObjectInt(16);
\t\treturn this.world.getEntityByID(id);
\t}"""
    repl_soul = """\tpublic Entity getUnfortunateSoul() {

\t\tint id = this.dataManager.get(TARGET_ID).intValue();
\t\treturn this.world.getEntityByID(id);
\t}"""
    assert target_soul in text, "rad_beast target_soul"
    text = text.replace(target_soul, repl_soul, 1)

    with open(path, "wb") as f:
        f.write(text.replace("\n", "\r\n").encode("utf-8"))
    print("EntityRADBeast updated successfully")


def update_hunter_chopper():
    path = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\entity\mob\EntityHunterChopper.java"
    with open(path, "rb") as f:
        text = f.read().decode("utf-8").replace("\r\n", "\n")

    # isEntityInvulnerable
    text = text.replace("if(this.isEntityInvulnerable() || source instanceof EntityDamageSource",
                        "if(this.isEntityInvulnerable(source) || source instanceof EntityDamageSource")

    # updateEntityActionState -> updateAITasks
    text = text.replace("protected void updateEntityActionState() {",
                        "protected void updateAITasks() {")

    # difficultySetting -> getDifficulty()
    text = text.replace("this.world.difficultySetting == EnumDifficulty.PEACEFUL",
                        "this.world.getDifficulty() == EnumDifficulty.PEACEFUL")
    text = text.replace("this.world.difficultySetting != EnumDifficulty.PEACEFUL",
                        "this.world.getDifficulty() != EnumDifficulty.PEACEFUL")

    # playSoundEffect / playSoundAtEntity
    text = text.replace('this.world.playSoundEffect(this.posX, this.posY, this.posZ, "hbm:misc.nullChopper", 10.0F, 0.5F);',
                        'this.world.playSound(null, this.posX, this.posY, this.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:misc.nullChopper"), net.minecraft.util.SoundCategory.HOSTILE, 10.0F, 0.5F);')
    text = text.replace('this.world.playSoundAtEntity(this, "hbm:weapon.osiprShoot", 10.0F, 1.0F);',
                        'this.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:weapon.osiprShoot"), 10.0F, 1.0F);')
    text = text.replace('this.world.playSoundAtEntity(this, "hbm:entity.chopperCharge", 5.0F, 1.0F);',
                        'this.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:entity.chopperCharge"), 5.0F, 1.0F);')
    text = text.replace('this.world.playSoundAtEntity(this, "hbm:entity.chopperDrop", 15.0F, 1.0F);',
                        'this.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:entity.chopperDrop"), 15.0F, 1.0F);')
    text = text.replace('this.world.playSoundEffect(this.posX, this.posY, this.posZ, "hbm:misc.nullCrashing", 10.0F, 0.5F);',
                        'this.world.playSound(null, this.posX, this.posY, this.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:misc.nullCrashing"), net.minecraft.util.SoundCategory.HOSTILE, 10.0F, 0.5F);')
    text = text.replace('this.world.playSoundAtEntity(this, "hbm:entity.chopperDamage", 10.0F, 1.0F);',
                        'this.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:entity.chopperDamage"), 10.0F, 1.0F);')

    # BoundingBox & collision
    text = text.replace("AxisAlignedBB axisalignedbb = this.getEntityBoundingBox().copy();",
                        "AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();")
    text = text.replace("if(!this.world.getCollidingBoundingBoxes(this, axisalignedbb).isEmpty()) {",
                        "if(!this.world.getCollisionBoxes(this, axisalignedbb).isEmpty()) {")

    # Sound overrides
    old_sounds = """\t@Override
\tprotected String getHurtSound() {
\t\treturn null;
\t}

\t@Override
\tprotected String getDeathSound() {
\t\treturn null;
\t}"""
    new_sounds = """\t@Override
\tprotected net.minecraft.util.SoundEvent getHurtSound(DamageSource damageSourceIn) {
\t\treturn null;
\t}

\t@Override
\tprotected net.minecraft.util.SoundEvent getDeathSound() {
\t\treturn null;
\t}"""
    assert old_sounds in text, "hunter_chopper old_sounds"
    text = text.replace(old_sounds, new_sounds, 1)

    with open(path, "wb") as f:
        f.write(text.replace("\n", "\r\n").encode("utf-8"))
    print("EntityHunterChopper updated successfully")


def update_pigeon():
    path = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\entity\mob\EntityPigeon.java"
    with open(path, "rb") as f:
        text = f.read().decode("utf-8").replace("\r\n", "\n")

    # Item setter
    text = text.replace("feather.setEntityItemStack(new ItemStack(Items.FEATHER));",
                        "feather.setItem(new ItemStack(Items.FEATHER));")

    # DataParameter imports & fields
    text = text.replace("import net.minecraft.item.ItemStack;",
                        """import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.item.ItemStack;""")

    old_ai = """\t@Override
\tpublic boolean isAIEnabled() {
\t\treturn true;
\t}"""
    new_ai = """\tprivate static final DataParameter<Byte> STATE = EntityDataManager.createKey(EntityPigeon.class, DataSerializers.BYTE);
\tprivate static final DataParameter<Byte> FAT = EntityDataManager.createKey(EntityPigeon.class, DataSerializers.BYTE);

\tpublic boolean isAIEnabled() {
\t\treturn true;
\t}"""
    assert old_ai in text, "pigeon old_ai"
    text = text.replace(old_ai, new_ai, 1)

    # entityInit
    old_init = """\t\tthis.dataWatcher.addObject(12, Byte.valueOf((byte) 0));
\t\tthis.dataWatcher.addObject(13, Byte.valueOf((byte) 0));"""
    new_init = """\t\tthis.dataManager.register(STATE, Byte.valueOf((byte) 0));
\t\tthis.dataManager.register(FAT, Byte.valueOf((byte) 0));"""
    assert old_init in text, "pigeon old_init"
    text = text.replace(old_init, new_init, 1)

    # playStepSound
    old_step = """\t@Override
\tprotected void func_145780_a(int x, int y, int z, Block block) {
\t\tthis.playSound("mob.chicken.step", 0.15F, 1.0F);
\t}"""
    new_step = """\t@Override
\tprotected void playStepSound(net.minecraft.util.math.BlockPos pos, Block blockIn) {
\t\tthis.playSound(net.minecraft.init.SoundEvents.ENTITY_CHICKEN_STEP, 0.15F, 1.0F);
\t}"""
    assert old_step in text, "pigeon old_step"
    text = text.replace(old_step, new_step, 1)

    # Getters / setters
    text = text.replace("return this.dataWatcher.getWatchableObjectByte(12);",
                        "return this.dataManager.get(STATE).byteValue();")
    text = text.replace("this.dataWatcher.updateObject(12, (byte) state);",
                        "this.dataManager.set(STATE, Byte.valueOf((byte) state));")
    text = text.replace("return this.dataWatcher.getWatchableObjectByte(13) == 1;",
                        "return this.dataManager.get(FAT).byteValue() == 1;")
    text = text.replace("this.dataWatcher.updateObject(13, (byte) (fat ? 1 : 0));",
                        "this.dataManager.set(FAT, Byte.valueOf((byte) (fat ? 1 : 0)));")

    # getDeathSound & getHurtSound
    old_death = """\tprotected String getDeathSound() {
\t\treturn null;
\t}"""
    new_death = """\t@Override
\tprotected net.minecraft.util.SoundEvent getDeathSound() {
\t\treturn null;
\t}

\t@Override
\tprotected net.minecraft.util.SoundEvent getHurtSound(DamageSource damageSourceIn) {
\t\treturn null;
\t}"""
    assert old_death in text, "pigeon old_death"
    text = text.replace(old_death, new_death, 1)

    # playAuxSFX -> playEvent
    text = text.replace("this.world.playAuxSFX(2005, x, y - i, z, 0);",
                        "this.world.playEvent(2005, new net.minecraft.util.math.BlockPos(x, y - i, z), 0);")

    # fall & updateFallState
    old_fall = """\t@Override protected void fall(float p_70069_1_) { }
\t@Override protected void updateFallState(double p_70064_1_, boolean p_70064_3_) { }"""
    new_fall = """\t@Override public void fall(float distance, float damageMultiplier) { }
\t@Override protected void updateFallState(double y, boolean onGroundIn, net.minecraft.block.state.IBlockState state, net.minecraft.util.math.BlockPos pos) { }"""
    assert old_fall in text, "pigeon old_fall"
    text = text.replace(old_fall, new_fall, 1)

    with open(path, "wb") as f:
        f.write(text.replace("\n", "\r\n").encode("utf-8"))
    print("EntityPigeon updated successfully")


def update_bf_angel():
    path = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\entity\mob\EntityBFAngel.java"
    with open(path, "rb") as f:
        text = f.read().decode("utf-8").replace("\r\n", "\n")

    text = text.replace("protected void updateEntityActionState() {",
                        "protected void updateAITasks() {")
    text = text.replace("this.world.difficultySetting == EnumDifficulty.PEACEFUL",
                        "this.world.getDifficulty() == EnumDifficulty.PEACEFUL")

    text = text.replace('this.world.playSoundAtEntity(this, "hbm:entity.bfatalk", 10.0F, 1.0F);',
                        'this.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:entity.bfatalk"), 10.0F, 1.0F);')
    text = text.replace('this.world.playSoundAtEntity(this, "hbm:entity.chopperDamage", 10.0F, 1.0F);',
                        'this.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:entity.chopperDamage"), 10.0F, 1.0F);')
    text = text.replace('this.playSound("hbm:entity.bfashoot", 5.0F, 1.0F);',
                        'this.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:entity.bfashoot"), 5.0F, 1.0F);')
    text = text.replace('this.playSound("hbm:turret.richard_fire", 5.0F, 1.0F);',
                        'this.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:turret.richard_fire"), 5.0F, 1.0F);')

    text = text.replace("AxisAlignedBB axisalignedbb = this.getEntityBoundingBox().copy();",
                        "AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();")
    text = text.replace("if(!this.world.getCollidingBoundingBoxes(this, axisalignedbb).isEmpty()) {",
                        "if(!this.world.getCollisionBoxes(this, axisalignedbb).isEmpty()) {")

    old_sounds = """\t@Override
\tprotected String getHurtSound() {
\t\treturn "mob.blaze.hit";
\t}

\t@Override
\tprotected String getDeathSound() {
\t\treturn null;
\t}"""
    new_sounds = """\t@Override
\tprotected net.minecraft.util.SoundEvent getHurtSound(DamageSource damageSourceIn) {
\t\treturn com.hbm.main.ModSoundEvents.resolve("mob.blaze.hit");
\t}

\t@Override
\tprotected net.minecraft.util.SoundEvent getDeathSound() {
\t\treturn null;
\t}"""
    assert old_sounds in text, "bf_angel old_sounds"
    text = text.replace(old_sounds, new_sounds, 1)

    with open(path, "wb") as f:
        f.write(text.replace("\n", "\r\n").encode("utf-8"))
    print("EntityBFAngel updated successfully")


def update_ufo():
    path = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\entity\mob\EntityUFO.java"
    with open(path, "rb") as f:
        text = f.read().decode("utf-8").replace("\r\n", "\n")

    text = text.replace("protected void updateEntityActionState() {",
                        "protected void updateAITasks() {")
    text = text.replace("this.world.difficultySetting == EnumDifficulty.PEACEFUL",
                        "this.world.getDifficulty() == EnumDifficulty.PEACEFUL")

    text = text.replace('this.world.playSoundAtEntity(this, "hbm:entity.ufoBeam", 10.0F, 1.0F);',
                        'this.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:entity.ufoBeam"), 10.0F, 1.0F);')
    text = text.replace('this.world.playSoundAtEntity(this, "hbm:entity.chopperDamage", 10.0F, 1.0F);',
                        'this.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:entity.chopperDamage"), 10.0F, 1.0F);')
    text = text.replace('this.playSound("hbm:weapon.ballsLaser", 5.0F, 1.0F);',
                        'this.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:weapon.ballsLaser"), 5.0F, 1.0F);')
    text = text.replace('this.playSound("hbm:turret.richard_fire", 5.0F, 1.0F);',
                        'this.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:turret.richard_fire"), 5.0F, 1.0F);')

    text = text.replace("AxisAlignedBB axisalignedbb = this.getEntityBoundingBox().copy();",
                        "AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();")
    text = text.replace("if(!this.world.getCollidingBoundingBoxes(this, axisalignedbb).isEmpty()) {",
                        "if(!this.world.getCollisionBoxes(this, axisalignedbb).isEmpty()) {")

    old_sounds = """\t@Override
\tprotected String getHurtSound() {
\t\treturn "mob.blaze.hit";
\t}

\t@Override
\tprotected String getDeathSound() {
\t\treturn null;
\t}"""
    new_sounds = """\t@Override
\tprotected net.minecraft.util.SoundEvent getHurtSound(DamageSource damageSourceIn) {
\t\treturn com.hbm.main.ModSoundEvents.resolve("mob.blaze.hit");
\t}

\t@Override
\tprotected net.minecraft.util.SoundEvent getDeathSound() {
\t\treturn null;
\t}"""
    assert old_sounds in text, "ufo old_sounds"
    text = text.replace(old_sounds, new_sounds, 1)

    with open(path, "wb") as f:
        f.write(text.replace("\n", "\r\n").encode("utf-8"))
    print("EntityUFO updated successfully")


def update_cyber_crab():
    path = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\entity\mob\EntityCyberCrab.java"
    with open(path, "rb") as f:
        text = f.read().decode("utf-8").replace("\r\n", "\n")

    # implements IRangedAttackMob -> setSwingingArms
    text = text.replace("public class EntityCyberCrab extends EntityMob implements IRangedAttackMob, IRadiationImmune, ISuffocationImmune {",
                        """public class EntityCyberCrab extends EntityMob implements IRangedAttackMob, IRadiationImmune, ISuffocationImmune {

\t@Override
\tpublic void setSwingingArms(boolean swingingArms) { }""")

    # setAvoidsWater
    text = text.replace("\t\tthis.getNavigator().setAvoidsWater(true);\n", "")

    # targetTasks EntityAINearestAttackableTarget
    text = text.replace("this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));",
                        "this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));")

    # Remove @Override on isAIEnabled and getMaxSafePointTries
    old_ai = """\t@Override
\tpublic boolean isAIEnabled() {
\t\treturn true;
\t}"""
    new_ai = """\tpublic boolean isAIEnabled() {
\t\treturn true;
\t}"""
    assert old_ai in text, "cyber_crab old_ai"
    text = text.replace(old_ai, new_ai, 1)

    old_tries = """\t@Override
\tpublic int getMaxSafePointTries() {"""
    new_tries = """\tpublic int getMaxSafePointTries() {"""
    assert old_tries in text, "cyber_crab old_tries"
    text = text.replace(old_tries, new_tries, 1)

    # Sounds
    old_sounds = """\t@Override
\tprotected String getHurtSound() {
\t\treturn "hbm:entity.cybercrab";
\t}

\t/**
\t * Returns the sound this mob makes on death.
\t */
\t@Override
\tprotected String getDeathSound() {
\t\treturn "hbm:entity.cybercrab";
\t}"""
    new_sounds = """\t@Override
\tprotected net.minecraft.util.SoundEvent getHurtSound(DamageSource damageSourceIn) {
\t\treturn com.hbm.main.ModSoundEvents.resolve("hbm:entity.cybercrab");
\t}

\t/**
\t * Returns the sound this mob makes on death.
\t */
\t@Override
\tprotected net.minecraft.util.SoundEvent getDeathSound() {
\t\treturn com.hbm.main.ModSoundEvents.resolve("hbm:entity.cybercrab");
\t}"""
    assert old_sounds in text, "cyber_crab old_sounds"
    text = text.replace(old_sounds, new_sounds, 1)

    # playSound
    text = text.replace('this.playSound("hbm:weapon.sawShoot", 1.0F, 2.0F);',
                        'this.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:weapon.sawShoot"), 1.0F, 2.0F);')

    with open(path, "wb") as f:
        f.write(text.replace("\n", "\r\n").encode("utf-8"))
    print("EntityCyberCrab updated successfully")


def update_plastic_bag():
    path = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\entity\mob\EntityPlasticBag.java"
    with open(path, "rb") as f:
        text = f.read().decode("utf-8").replace("\r\n", "\n")

    text = text.replace("entityitem.delayBeforeCanPickup = 10;",
                        "entityitem.setPickupDelay(10);")

    old_sounds = """\t@Override
\tprotected String getLivingSound() {
\t\treturn null;
\t}

\t@Override
\tprotected String getHurtSound() {
\t\treturn null;
\t}

\t@Override
\tprotected String getDeathSound() {
\t\treturn null;
\t}"""
    new_sounds = """\t@Override
\tprotected net.minecraft.util.SoundEvent getAmbientSound() {
\t\treturn null;
\t}

\t@Override
\tprotected net.minecraft.util.SoundEvent getHurtSound(DamageSource damageSourceIn) {
\t\treturn null;
\t}

\t@Override
\tprotected net.minecraft.util.SoundEvent getDeathSound() {
\t\treturn null;
\t}"""
    assert old_sounds in text, "plastic_bag old_sounds"
    text = text.replace(old_sounds, new_sounds, 1)

    # moveEntityWithHeading -> travel
    old_move = """\t@Override
\tpublic void moveEntityWithHeading(float forward, float strafe) {
\t\tthis.move(net.minecraft.entity.MoverType.SELF, this.motionX, this.motionY, this.motionZ);
\t}"""
    new_move = """\t@Override
\tpublic void travel(float strafe, float vertical, float forward) {
\t\tthis.move(net.minecraft.entity.MoverType.SELF, this.motionX, this.motionY, this.motionZ);
\t}"""
    assert old_move in text, "plastic_bag old_move"
    text = text.replace(old_move, new_move, 1)

    # updateEntityActionState -> updateAITasks & entityAge -> idleTime
    text = text.replace("protected void updateEntityActionState() {",
                        "protected void updateAITasks() {")
    text = text.replace("++this.entityAge;",
                        "++this.idleTime;")
    text = text.replace("if(this.entityAge > 100) {",
                        "if(this.idleTime > 100) {")

    with open(path, "wb") as f:
        f.write(text.replace("\n", "\r\n").encode("utf-8"))
    print("EntityPlasticBag updated successfully")


def update_tankbot():
    path = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\entity\mob\EntityTankbot.java"
    with open(path, "rb") as f:
        text = f.read().decode("utf-8").replace("\r\n", "\n")

    # Imports
    text = text.replace("import net.minecraft.world.World;",
                        """import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;""")

    # DataParameter
    text = text.replace("public class EntityTankbot extends EntityMob {",
                        """public class EntityTankbot extends EntityMob {

\tpublic static final DataParameter<Integer> TARGET_ID = EntityDataManager.createKey(EntityTankbot.class, DataSerializers.VARINT);

\tpublic int getTargetEntityId() {
\t\treturn this.dataManager.get(TARGET_ID).intValue();
\t}""")

    # setAvoidsWater
    text = text.replace("\t\tthis.getNavigator().setAvoidsWater(true);\n", "")

    # targetTasks
    text = text.replace("this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));",
                        "this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));")

    # playSoundAtEntity
    text = text.replace('this.world.playSoundAtEntity(this, "hbm:entity.chopperDamage", 10.0F, 1.0F);',
                        'this.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:entity.chopperDamage"), 10.0F, 1.0F);')

    # findPlayerToAttack
    old_find = """\t@Override
\tprotected Entity findPlayerToAttack() {
\t\tif(this.isPotionActive(net.minecraft.init.MobEffects.BLINDNESS)) return null;

\t\treturn this.world.getClosestVulnerablePlayerToEntity(this, 64D);
\t}"""
    new_find = """\tprotected Entity findPlayerToAttack() {
\t\tif(this.isPotionActive(net.minecraft.init.MobEffects.BLINDNESS)) return null;

\t\treturn this.world.getNearestAttackablePlayer(this, 64.0D, 64.0D);
\t}"""
    assert old_find in text, "tankbot old_find"
    text = text.replace(old_find, new_find, 1)

    # entityInit & setAttackTarget
    old_init = """\tprotected void entityInit() {
\t\tsuper.entityInit();
\t\tthis.dataWatcher.addObject(20, (int) 0);
\t}"""
    new_init = """\t@Override
\tprotected void entityInit() {
\t\tsuper.entityInit();
\t\tthis.dataManager.register(TARGET_ID, Integer.valueOf(0));
\t}"""
    assert old_init in text, "tankbot old_init"
    text = text.replace(old_init, new_init, 1)

    text = text.replace("this.dataWatcher.updateObject(20, entity != null ? entity.getEntityId() : 0);",
                        "this.dataManager.set(TARGET_ID, Integer.valueOf(entity != null ? entity.getEntityId() : 0));")

    # Sound & isAIEnabled
    old_hurt = """    @Override
\tprotected String getHurtSound()
    {
        return "hbm:entity.cybercrab";
    }
\t@Override
\tpublic boolean isAIEnabled() {
\t\treturn true;
\t}"""
    new_hurt = """    @Override
\tprotected net.minecraft.util.SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
        return com.hbm.main.ModSoundEvents.resolve("hbm:entity.cybercrab");
    }
\tpublic boolean isAIEnabled() {
\t\treturn true;
\t}"""
    assert old_hurt in text, "tankbot old_hurt"
    text = text.replace(old_hurt, new_hurt, 1)

    with open(path, "wb") as f:
        f.write(text.replace("\n", "\r\n").encode("utf-8"))
    print("EntityTankbot updated successfully")


def update_siege_craft():
    path = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\entity\mob\siege\EntitySiegeCraft.java"
    with open(path, "rb") as f:
        text = f.read().decode("utf-8").replace("\r\n", "\n")

    text = text.replace("if(this.isEntityInvulnerable())",
                        "if(this.isEntityInvulnerable(source))")

    text = text.replace('this.world.playSoundAtEntity(this, "random.break", 5F, 1.0F + rand.nextFloat() * 0.5F);',
                        'this.playSound(net.minecraft.init.SoundEvents.ENTITY_ITEM_BREAK, 5F, 1.0F + rand.nextFloat() * 0.5F);')

    text = text.replace("protected void updateEntityActionState() {",
                        "protected void updateAITasks() {")

    text = text.replace('this.world.playSoundAtEntity(this.target, "hbm:weapon.stingerLockOn", 2F, 0.75F);',
                        'this.target.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:weapon.stingerLockOn"), 2F, 0.75F);')

    text = text.replace("if(this.canAttackClass(e.getClass())) {",
                        "if(e instanceof EntityLivingBase && this.canAttackClass(((EntityLivingBase) e).getClass())) {")

    text = text.replace("laser.setThrowableHeading(copy.x, copy.y, copy.z, 1F, 0.0F);",
                        "laser.shoot(copy.x, copy.y, copy.z, 1F, 0.0F);")

    text = text.replace('this.playSound("hbm:weapon.ballsLaser", 2.0F, 1.0F);',
                        'this.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:weapon.ballsLaser"), 2.0F, 1.0F);')

    old_spawn = """\t@Override
\tpublic IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
\t\tthis.setTier(SiegeTier.tiers[rand.nextInt(SiegeTier.getLength())]);
\t\treturn super.onSpawnWithEgg(data);
\t}"""
    new_spawn = """\t@Override
\tpublic IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance difficulty, IEntityLivingData data) {
\t\tthis.setTier(SiegeTier.tiers[rand.nextInt(SiegeTier.getLength())]);
\t\treturn super.onInitialSpawn(difficulty, data);
\t}"""
    assert old_spawn in text, "siege_craft old_spawn"
    text = text.replace(old_spawn, new_spawn, 1)

    with open(path, "wb") as f:
        f.write(text.replace("\n", "\r\n").encode("utf-8"))
    print("EntitySiegeCraft updated successfully")


def update_burrowing_base():
    path = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\entity\mob\EntityBurrowingBase.java"
    with open(path, "rb") as f:
        text = f.read().decode("utf-8").replace("\r\n", "\n")

    text = text.replace("if(this.isEntityInvulnerable() || source == DamageSource.DROWN || source == DamageSource.IN_WALL) {",
                        "if(this.isEntityInvulnerable(source) || source == DamageSource.DROWN || source == DamageSource.IN_WALL) {")

    old_fall = """\t@Override
\tprotected void fall(float dist) { }
\t@Override
\tprotected void updateFallState(double distFallen, boolean onGround) { }"""
    new_fall = """\t@Override
\tpublic void fall(float distance, float damageMultiplier) { }
\t@Override
\tprotected void updateFallState(double y, boolean onGroundIn, net.minecraft.block.state.IBlockState state, net.minecraft.util.math.BlockPos pos) { }"""
    assert old_fall in text, "burrowing old_fall"
    text = text.replace(old_fall, new_fall, 1)

    text = text.replace("protected void updateEntityActionState() { }",
                        "protected void updateAITasks() { }")

    old_move = """\t@Override
\tpublic void moveEntityWithHeading(float strafe, float forward) {

\t\tfloat drag = this.groundDrag;
\t\tfloat dragY = this.groundDragY;

\t\tif(!isEntityInsideOpaqueBlock() && !isInWater() && !handleLavaMovement()) {
\t\t\tdrag = this.airDrag;
\t\t\tdragY = this.airDragY;
\t\t}

\t\t//misnomer, actually just sets the motion, the moving part happens the line after that
\t\tmoveFlying(strafe, forward, 0.02F);
\t\tmove(net.minecraft.entity.MoverType.SELF, this.motionX, this.motionY, this.motionZ);

\t\tthis.motionX *= drag;
\t\tthis.motionY *= dragY;
\t\tthis.motionZ *= drag;
\t}"""
    new_move = """\t@Override
\tpublic void travel(float strafe, float vertical, float forward) {

\t\tfloat drag = this.groundDrag;
\t\tfloat dragY = this.groundDragY;

\t\tif(!isEntityInsideOpaqueBlock() && !isInWater() && !isInLava()) {
\t\t\tdrag = this.airDrag;
\t\t\tdragY = this.airDragY;
\t\t}

\t\t//misnomer, actually just sets the motion, the moving part happens the line after that
\t\tmoveRelative(strafe, vertical, forward, 0.02F);
\t\tmove(net.minecraft.entity.MoverType.SELF, this.motionX, this.motionY, this.motionZ);

\t\tthis.motionX *= drag;
\t\tthis.motionY *= dragY;
\t\tthis.motionZ *= drag;
\t}"""
    assert old_move in text, "burrowing old_move"
    text = text.replace(old_move, new_move, 1)

    old_ai = """\t@Override
\tprotected boolean isAIEnabled() {
\t\treturn true;
\t}"""
    new_ai = """\tprotected boolean isAIEnabled() {
\t\treturn true;
\t}"""
    assert old_ai in text, "burrowing old_ai"
    text = text.replace(old_ai, new_ai, 1)

    with open(path, "wb") as f:
        f.write(text.replace("\n", "\r\n").encode("utf-8"))
    print("EntityBurrowingBase updated successfully")


def update_render_tankbot():
    path = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\render\entity\mob\RenderTankbot.java"
    with open(path, "rb") as f:
        text = f.read().decode("utf-8").replace("\r\n", "\n")

    text = text.replace("\t\tthis.setRenderPassModel(this.mainModel);\n", "")

    old_pass = """\t@Override
\tprotected int shouldRenderPass(EntityLivingBase entity, int pass, float interp) {
\t\tif(pass != 0) {
\t\t\treturn -1;
\t\t}
\t\t\treturn -1;
\t}"""
    new_pass = """\tprotected int shouldRenderPass(EntityLivingBase entity, int pass, float interp) {
\t\tif(pass != 0) {
\t\t\treturn -1;
\t\t}
\t\t\treturn -1;
\t}"""
    assert old_pass in text, "render_tankbot old_pass"
    text = text.replace(old_pass, new_pass, 1)

    text = text.replace("int targetId = tankbot.getDataWatcher().getWatchableObjectInt(20);",
                        "int targetId = tankbot.getTargetEntityId();")

    with open(path, "wb") as f:
        f.write(text.replace("\n", "\r\n").encode("utf-8"))
    print("RenderTankbot updated successfully")


if __name__ == "__main__":
    for fn in [
        update_rad_beast,
        update_hunter_chopper,
        update_pigeon,
        update_bf_angel,
        update_ufo,
        update_cyber_crab,
        update_plastic_bag,
        update_tankbot,
        update_siege_craft,
        update_burrowing_base,
        update_render_tankbot,
    ]:
        try:
            fn()
        except Exception as e:
            print(f"Skipping {fn.__name__}: {e}")
    print("FINISHED RUNNING UPDATES!")
