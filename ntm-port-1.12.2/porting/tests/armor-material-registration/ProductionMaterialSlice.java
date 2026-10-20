import net.minecraft.item.*;import net.minecraft.item.ItemArmor.ArmorMaterial;import net.minecraft.init.SoundEvents;import net.minecraftforge.common.util.EnumHelper;import net.minecraftforge.oredict.OreDictionary;
final class ProductionMaterialSlice {
static final String[] MAIN_NAMES={"aMatSchrab","aMatEuph","aMatHaz","aMatHaz2","aMatHaz3","aMatSteel","aMatAsbestos","aMatTitan","aMatAlloy","aMatPaa","aMatCMB","aMatAus3","aMatSecurity","aMatCobalt","aMatStarmetal","aMatBismuth"};
static class ModItems {
static final Item asbestos_cloth=new Item();
static final Item hazmat_cloth=new Item();
static final Item hazmat_cloth_grey=new Item();
static final Item hazmat_cloth_red=new Item();
static final Item ingot_advanced_alloy=new Item();
static final Item ingot_australium=new Item();
static final Item ingot_cobalt=new Item();
static final Item ingot_combine_steel=new Item();
static final Item ingot_desh=new Item();
static final Item ingot_dineutronium=new Item();
static final Item ingot_schrabidium=new Item();
static final Item ingot_starmetal=new Item();
static final Item ingot_steel=new Item();
static final Item ingot_titanium=new Item();
static final Item ingot_zirconium=new Item();
static final Item plate_armor_ajr=new Item();
static final Item plate_armor_dnt=new Item();
static final Item plate_armor_fau=new Item();
static final Item plate_armor_hev=new Item();
static final Item plate_armor_lunar=new Item();
static final Item plate_armor_titanium=new Item();
static final Item plate_bismuth=new Item();
static final Item plate_copper=new Item();
static final Item plate_iron=new Item();
static final Item plate_kevlar=new Item();
static final Item plate_lead=new Item();
static final Item plate_paa=new Item();
static final Item rag=new Item();
}
static ArmorMaterial aMatSchrab;
static ArmorMaterial aMatEuph;
static ArmorMaterial aMatHaz;
static ArmorMaterial aMatHaz2;
static ArmorMaterial aMatHaz3;
static ArmorMaterial aMatSteel;
static ArmorMaterial aMatAsbestos;
static ArmorMaterial aMatTitan;
static ArmorMaterial aMatAlloy;
static ArmorMaterial aMatPaa;
static ArmorMaterial aMatCMB;
static ArmorMaterial aMatAus3;
static ArmorMaterial aMatSecurity;
static ArmorMaterial aMatCobalt;
static ArmorMaterial aMatStarmetal;
static ArmorMaterial aMatBismuth;
static void createMain(){
{ArmorMaterial aMatSchrab = EnumHelper.addArmorMaterial("HBM_SCHRABIDIUM", "leather", 100, new int[] { 3, 6, 8, 3 }, 50, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);ProductionMaterialSlice.aMatSchrab=aMatSchrab;}
NativeArmorMaterialTest.created("aMatSchrab",aMatSchrab);
{ArmorMaterial aMatEuph = EnumHelper.addArmorMaterial("HBM_EUPHEMIUM", "leather", 15000000, new int[] { 3, 6, 8, 3 }, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);ProductionMaterialSlice.aMatEuph=aMatEuph;}
NativeArmorMaterialTest.created("aMatEuph",aMatEuph);
{ArmorMaterial aMatHaz = EnumHelper.addArmorMaterial("HBM_HAZMAT", "leather", 60, new int[] { 1, 4, 5, 2 }, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);ProductionMaterialSlice.aMatHaz=aMatHaz;}
NativeArmorMaterialTest.created("aMatHaz",aMatHaz);
{ArmorMaterial aMatHaz2 = EnumHelper.addArmorMaterial("HBM_HAZMAT2", "leather", 60, new int[] { 1, 4, 5, 2 }, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);ProductionMaterialSlice.aMatHaz2=aMatHaz2;}
NativeArmorMaterialTest.created("aMatHaz2",aMatHaz2);
{ArmorMaterial aMatHaz3 = EnumHelper.addArmorMaterial("HBM_HAZMAT3", "leather", 60, new int[] { 1, 4, 5, 2 }, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);ProductionMaterialSlice.aMatHaz3=aMatHaz3;}
NativeArmorMaterialTest.created("aMatHaz3",aMatHaz3);
{ArmorMaterial aMatSteel = EnumHelper.addArmorMaterial("HBM_STEEL", "leather", 20, new int[] { 2, 5, 6, 2 }, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);ProductionMaterialSlice.aMatSteel=aMatSteel;}
NativeArmorMaterialTest.created("aMatSteel",aMatSteel);
{ArmorMaterial aMatAsbestos = EnumHelper.addArmorMaterial("HBM_ASBESTOS", "leather", 20, new int[] { 1, 3, 4, 1 }, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);ProductionMaterialSlice.aMatAsbestos=aMatAsbestos;}
NativeArmorMaterialTest.created("aMatAsbestos",aMatAsbestos);
{ArmorMaterial aMatTitan = EnumHelper.addArmorMaterial("HBM_TITANIUM", "leather", 25, new int[] { 3, 6, 8, 3 }, 9, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);ProductionMaterialSlice.aMatTitan=aMatTitan;}
NativeArmorMaterialTest.created("aMatTitan",aMatTitan);
{ArmorMaterial aMatAlloy = EnumHelper.addArmorMaterial("HBM_ALLOY", "leather", 40, new int[] { 3, 6, 8, 3 }, 12, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);ProductionMaterialSlice.aMatAlloy=aMatAlloy;}
NativeArmorMaterialTest.created("aMatAlloy",aMatAlloy);
{ArmorMaterial aMatPaa = EnumHelper.addArmorMaterial("HBM_PAA", "leather", 75, new int[] { 3, 6, 8, 3 }, 25, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);ProductionMaterialSlice.aMatPaa=aMatPaa;}
NativeArmorMaterialTest.created("aMatPaa",aMatPaa);
{ArmorMaterial aMatCMB = EnumHelper.addArmorMaterial("HBM_CMB", "leather", 60, new int[] { 3, 6, 8, 3 }, 50, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);ProductionMaterialSlice.aMatCMB=aMatCMB;}
NativeArmorMaterialTest.created("aMatCMB",aMatCMB);
{ArmorMaterial aMatAus3 = EnumHelper.addArmorMaterial("HBM_AUSIII", "leather", 375, new int[] { 2, 5, 6, 2 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);ProductionMaterialSlice.aMatAus3=aMatAus3;}
NativeArmorMaterialTest.created("aMatAus3",aMatAus3);
{ArmorMaterial aMatSecurity = EnumHelper.addArmorMaterial("HBM_SECURITY", "leather", 100, new int[] { 3, 6, 8, 3 }, 15, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);ProductionMaterialSlice.aMatSecurity=aMatSecurity;}
NativeArmorMaterialTest.created("aMatSecurity",aMatSecurity);
{ArmorMaterial aMatCobalt = EnumHelper.addArmorMaterial("HBM_COBALT", "leather", 70, new int[] { 3, 6, 8, 3 }, 60, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);ProductionMaterialSlice.aMatCobalt=aMatCobalt;}
NativeArmorMaterialTest.created("aMatCobalt",aMatCobalt);
{ArmorMaterial aMatStarmetal = EnumHelper.addArmorMaterial("HBM_STARMETAL", "leather", 150, new int[] { 3, 6, 8, 3 }, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);ProductionMaterialSlice.aMatStarmetal=aMatStarmetal;}
NativeArmorMaterialTest.created("aMatStarmetal",aMatStarmetal);
{ArmorMaterial aMatBismuth = EnumHelper.addArmorMaterial("HBM_BISMUTH", "leather", 100, new int[] { 3, 6, 8, 3 }, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);ProductionMaterialSlice.aMatBismuth=aMatBismuth;}
NativeArmorMaterialTest.created("aMatBismuth",aMatBismuth);
}
static void createArmor(){
ArmorMaterial aMatRags = EnumHelper.addArmorMaterial("HBM_RAGS", "leather", 150, new int[] { 1, 1, 1, 1 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
NativeArmorMaterialTest.created("aMatRags",aMatRags);
aMatRags.repairMaterial = new ItemStack(ModItems.rag, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatRags", ModItems.rag);
ArmorMaterial aMatZirconium = EnumHelper.addArmorMaterial("HBM_ZIRCONIUM", "leather", 1000, new int[] { 1, 3, 5, 2 }, 1000, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
NativeArmorMaterialTest.created("aMatZirconium",aMatZirconium);
aMatZirconium.repairMaterial = new ItemStack(ModItems.ingot_zirconium, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatZirconium", ModItems.ingot_zirconium);
ArmorMaterial aMatDNT = EnumHelper.addArmorMaterial("HBM_DNT_LOLOLOL", "leather", 3, new int[] { 1, 1, 1, 1 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
NativeArmorMaterialTest.created("aMatDNT",aMatDNT);
aMatDNT.repairMaterial = new ItemStack(ModItems.ingot_dineutronium, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatDNT", ModItems.ingot_dineutronium);
ArmorMaterial aMatT51 = EnumHelper.addArmorMaterial("HBM_T51", "leather", 150, new int[] { 3, 6, 8, 3 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
NativeArmorMaterialTest.created("aMatT51",aMatT51);
aMatT51.repairMaterial = new ItemStack(ModItems.plate_armor_titanium, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatT51", ModItems.plate_armor_titanium);
ArmorMaterial aMatDesh = EnumHelper.addArmorMaterial("HBM_DESH", "leather", 150, new int[] { 3, 6, 8, 3 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
NativeArmorMaterialTest.created("aMatDesh",aMatDesh);
aMatDesh.repairMaterial = new ItemStack(ModItems.ingot_desh, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatDesh", ModItems.ingot_desh);
ArmorMaterial aMatDiesel = EnumHelper.addArmorMaterial("HBM_BNUUY", "leather", 150, new int[] { 3, 6, 8, 3 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
NativeArmorMaterialTest.created("aMatDiesel",aMatDiesel);
aMatDiesel.repairMaterial = new ItemStack(ModItems.plate_copper, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatDiesel", ModItems.plate_copper);
ArmorMaterial aMatAJR = EnumHelper.addArmorMaterial("HBM_T45AJR", "leather", 150, new int[] { 3, 6, 8, 3 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
NativeArmorMaterialTest.created("aMatAJR",aMatAJR);
aMatAJR.repairMaterial = new ItemStack(ModItems.plate_armor_ajr, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatAJR", ModItems.plate_armor_ajr);
ArmorMaterial aMatBJ = EnumHelper.addArmorMaterial("HBM_BLACKJACK", "leather", 150, new int[] { 3, 6, 8, 3 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
NativeArmorMaterialTest.created("aMatBJ",aMatBJ);
aMatBJ.repairMaterial = new ItemStack(ModItems.plate_armor_lunar, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatBJ", ModItems.plate_armor_lunar);
ArmorMaterial aMatEnv = EnumHelper.addArmorMaterial("HBM_ENV", "leather", 150, new int[] { 3, 6, 8, 3 }, 10, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
NativeArmorMaterialTest.created("aMatEnv",aMatEnv);
aMatEnv.repairMaterial = new ItemStack(ModItems.plate_armor_hev, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatEnv", ModItems.plate_armor_hev);
ArmorMaterial aMatHEV = EnumHelper.addArmorMaterial("HBM_HEV", "leather", 150, new int[] { 3, 6, 8, 3 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
NativeArmorMaterialTest.created("aMatHEV",aMatHEV);
aMatHEV.repairMaterial = new ItemStack(ModItems.plate_armor_hev, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatHEV", ModItems.plate_armor_hev);
ArmorMaterial aMatFau = EnumHelper.addArmorMaterial("HBM_DIGAMMA", "leather", 150, new int[] { 3, 6, 8, 3 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
NativeArmorMaterialTest.created("aMatFau",aMatFau);
aMatFau.repairMaterial = new ItemStack(ModItems.plate_armor_fau, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatFau", ModItems.plate_armor_fau);
ArmorMaterial aMatDNS = EnumHelper.addArmorMaterial("HBM_DNT_NANO", "leather", 150, new int[] { 3, 6, 8, 3 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
NativeArmorMaterialTest.created("aMatDNS",aMatDNS);
aMatDNS.repairMaterial = new ItemStack(ModItems.plate_armor_dnt, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatDNS", ModItems.plate_armor_dnt);
ArmorMaterial aMatTaurun = EnumHelper.addArmorMaterial("HBM_TRENCH", "leather", 150, new int[] { 3, 6, 8, 3 }, 10, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
NativeArmorMaterialTest.created("aMatTaurun",aMatTaurun);
aMatTaurun.repairMaterial = new ItemStack(ModItems.plate_iron, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatTaurun", ModItems.plate_iron);
ArmorMaterial aMatTrench = EnumHelper.addArmorMaterial("HBM_TRENCH", "leather", 150, new int[] { 3, 6, 8, 3 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
NativeArmorMaterialTest.created("aMatTrench",aMatTrench);
aMatTrench.repairMaterial = new ItemStack(ModItems.plate_iron, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatTrench", ModItems.plate_iron);
}
static void createLiquidator(){
ArmorMaterial aMatLiquidator = EnumHelper.addArmorMaterial("HBM_LIQUIDATOR", "leather", 750, new int[] { 3, 6, 8, 3 }, 10, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
NativeArmorMaterialTest.created("aMatLiquidator",aMatLiquidator);
aMatLiquidator.repairMaterial = new ItemStack(ModItems.plate_lead, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatLiquidator", ModItems.plate_lead);
}
static void repairMain(){
aMatSchrab.repairMaterial = new ItemStack(ModItems.ingot_schrabidium, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatSchrab", ModItems.ingot_schrabidium);
aMatHaz.repairMaterial = new ItemStack(ModItems.hazmat_cloth, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatHaz", ModItems.hazmat_cloth);
aMatHaz2.repairMaterial = new ItemStack(ModItems.hazmat_cloth_red, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatHaz2", ModItems.hazmat_cloth_red);
aMatHaz3.repairMaterial = new ItemStack(ModItems.hazmat_cloth_grey, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatHaz3", ModItems.hazmat_cloth_grey);
aMatTitan.repairMaterial = new ItemStack(ModItems.ingot_titanium, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatTitan", ModItems.ingot_titanium);
aMatSteel.repairMaterial = new ItemStack(ModItems.ingot_steel, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatSteel", ModItems.ingot_steel);
aMatAsbestos.repairMaterial = new ItemStack(ModItems.asbestos_cloth, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatAsbestos", ModItems.asbestos_cloth);
aMatAlloy.repairMaterial = new ItemStack(ModItems.ingot_advanced_alloy, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatAlloy", ModItems.ingot_advanced_alloy);
aMatPaa.repairMaterial = new ItemStack(ModItems.plate_paa, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatPaa", ModItems.plate_paa);
aMatCMB.repairMaterial = new ItemStack(ModItems.ingot_combine_steel, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatCMB", ModItems.ingot_combine_steel);
aMatAus3.repairMaterial = new ItemStack(ModItems.ingot_australium, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatAus3", ModItems.ingot_australium);
aMatSecurity.repairMaterial = new ItemStack(ModItems.plate_kevlar, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatSecurity", ModItems.plate_kevlar);
aMatCobalt.repairMaterial = new ItemStack(ModItems.ingot_cobalt, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatCobalt", ModItems.ingot_cobalt);
aMatStarmetal.repairMaterial = new ItemStack(ModItems.ingot_starmetal, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatStarmetal", ModItems.ingot_starmetal);
aMatBismuth.repairMaterial = new ItemStack(ModItems.plate_bismuth, 1, OreDictionary.WILDCARD_VALUE);
NativeArmorMaterialTest.repaired("aMatBismuth", ModItems.plate_bismuth);
}
}
