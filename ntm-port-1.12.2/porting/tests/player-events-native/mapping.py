NAMES=['onClickBlock','onTrySleep','onPull','onFoodEaten']
def method(s,name):
 start=s.index('public void '+name+'(');i=s.index('{',start);depth=1;j=i+1
 while depth:
  if s[j]=='{':depth+=1
  elif s[j]=='}':depth-=1
  j+=1
 return s[start:j]
def migrate(s):
 for a,b in [('PlayerInteractEvent event','PlayerInteractEvent.RightClickBlock event'),('PlayerUseItemEvent.Finish event','LivingEntityUseItemEvent.Finish event'),('event.world','event.getWorld()'),('event.x','event.getPos().getX()'),('event.y','event.getPos().getY()'),('event.z','event.getPos().getZ()'),(' && event.action == Action.RIGHT_CLICK_BLOCK',''),('event.action == Action.RIGHT_CLICK_BLOCK && ',''),('world.getBlock(x, y, z)','world.getBlockState(new BlockPos(x, y, z)).getBlock()'),('Block.blockRegistry.getNameForObject(b)','b.getRegistryName() == null ? null : b.getRegistryName().toString()'),('world.func_147480_a(x, y, z, false)','world.destroyBlock(new BlockPos(x, y, z), false)'),('world.getTileEntity(x, y, z)','world.getTileEntity(new BlockPos(x, y, z))'),('entityitem.delayBeforeCanPickup = 1;','entityitem.setPickupDelay(1);'),('world.spawnEntityInWorld(entityitem)','world.spawnEntity(entityitem)'),('event.entityPlayer.getDisplayName()','event.getEntityPlayer().getDisplayNameString()'),('provider.dimensionId','provider.getDimension()'),('event.getWorld().getBlock(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ())','event.getWorld().getBlockState(event.getPos()).getBlock()'),('Blocks.lever','Blocks.LEVER'),('ItemStack stack = event.item;','if(!(event.getEntityLiving() instanceof EntityPlayer)) return;\n\t\tItemStack stack = event.getItem();'),('stack != null && stack.getItem() instanceof ItemFood','!stack.isEmpty() && stack.getItem() instanceof ItemFood'),('event.entityPlayer.attackEntityFrom','event.getEntityLiving().attackEntityFrom')]:s=s.replace(a,b)
 for i in range(4):s=s.replace('sign.signText['+str(i)+']','sign.signText['+str(i)+'].getUnformattedText()')
 return s
