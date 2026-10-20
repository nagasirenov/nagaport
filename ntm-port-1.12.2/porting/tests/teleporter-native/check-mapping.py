from pathlib import Path
import argparse,json,hashlib
parser=argparse.ArgumentParser();parser.add_argument('--source-root',type=Path,required=True);opts=parser.parse_args()
data=json.loads(Path(__file__).with_name('source-map.json').read_text());t=data['before'];assert hashlib.sha256(t.encode()).hexdigest()==data['before_sha256']
for a,b in [('S07PacketRespawn','SPacketRespawn'),('S18PacketEntityTeleport','SPacketEntityTeleport'),('S1DPacketEntityEffect','SPacketEntityEffect'),('ServerConfigurationManager','PlayerList'),('AxisAlignedBB.getBoundingBox(','new AxisAlignedBB('),('spawnParticle("reddust",','spawnParticle(net.minecraft.util.EnumParticleTypes.REDSTONE,'),('player.mcServer','player.server'),('.worldServerForDimension(','.getWorld('),('.getConfigurationManager()','.getPlayerList()'),('player.playerNetServerHandler','player.connection'),('player.worldObj.difficultySetting','player.world.getDifficulty()'),('player.theItemInWorldManager','player.interactionManager'),('.removePlayerEntityDangerously(','.removeEntityDangerously('),('.spawnEntityInWorld(','.spawnEntity('),('man.func_72375_a(','man.preparePlayer('),('entityItem.getEntityItem()','entityItem.getItem()'),('Items.bread','Items.BREAD'),('oldEntity.worldObj','oldEntity.world'),('EntityList.createEntityByName(EntityList.getEntityString(oldEntity), newWorld)','EntityList.createEntityByIDFromName(EntityList.getKey(oldEntity), newWorld)'),('IChunkProvider provider = newWorld.getChunkProvider();','net.minecraft.world.gen.ChunkProviderServer provider = newWorld.getChunkProvider();'),('slime.onSpawnWithEgg(null);','slime.onInitialSpawn(newWorld.getDifficultyForLocation(slime.getPosition()), null);')]:
 assert a in t,a;t=t.replace(a,b)
t=t.replace('MinecraftServer.getServer().getWorld(this.targetDim)','this.world.getMinecraftServer().getWorld(this.targetDim)').replace('MinecraftServer minecraftserver = MinecraftServer.getServer();','MinecraftServer minecraftserver = oldEntity.getServer();')
t=t.replace('this.world.playSoundEffect(', 'this.world.playSound(null, ').replace('"mob.endermen.portal", 1.0F, 1.0F','net.minecraft.init.SoundEvents.ENTITY_ENDERMEN_TELEPORT, net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F')
a=t.index('\t\t\t\t\tEntityTracker entitytracker');b=t.index('\n\t\t\t\t} catch(Exception ex)',a)
t=t[:a]+'''\t\t\t\t\tEntityTrackerEntry entry = com.hbm.util.TrackerUtil.getTrackerEntry((WorldServer)this.world, entity.getEntityId());
					SPacketEntityTeleport packet = new SPacketEntityTeleport(entity);
					// Preserve original floor rotation and leave the tracker counter untouched.
					ReflectionHelper.setPrivateValue(SPacketEntityTeleport.class, packet, (byte)MathHelper.floor(entity.rotationYaw * 256.0F / 360.0F), "yaw", "field_149455_e");
					ReflectionHelper.setPrivateValue(SPacketEntityTeleport.class, packet, (byte)MathHelper.floor(entity.rotationPitch * 256.0F / 360.0F), "pitch", "field_149453_f");
					entry.sendPacketToTrackedPlayers(packet);'''+t[b:]
t=t.replace('entity.copyDataFrom(oldEntity, true);','''// Original copyDataFrom included Dimension in NBT; the native private helper
			// removes it. Retain the original serialized data and portal cooldown/direction.
			entity.readFromNBT(oldEntity.writeToNBT(new NBTTagCompound()));
			entity.timeUntilPortal = oldEntity.timeUntilPortal;
			ReflectionHelper.setPrivateValue(Entity.class, entity, oldEntity.getTeleportDirection(), "teleportDirection", "field_181018_ap");''')
for i in ['net.minecraft.entity.EntityTracker','net.minecraft.util.IntHashMap','net.minecraft.world.chunk.IChunkProvider']:t=t.replace('import '+i+';\n','')
actual=(opts.source_root/data['path']).read_text();assert actual==t;assert hashlib.sha256(actual.encode()).hexdigest()==data['after_sha256'];print('PASS complete exact teleporter source mapping; unrelated original statements retained.')
