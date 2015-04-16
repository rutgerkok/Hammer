package nl.rutgerkok.hammer.anvil.material;

import java.util.HashMap;
import java.util.Map;

import nl.rutgerkok.hammer.material.Material;
import nl.rutgerkok.hammer.material.MaterialMap;
import nl.rutgerkok.hammer.util.MaterialNotFoundException;

public class VanillaMaterialMap implements MaterialMap {

    private static final AnvilMaterial[] byId = new AnvilMaterial[256];
    private static final Map<String, AnvilMaterial> byName = new HashMap<>();

    static {
        register("air", 0);
        register("stone", 1);
        register("grass", 2);
        register("dirt", 3);
        register("cobblestone", 4);
        register("planks", 5);
        register("sapling", 6);
        register("bedrock", 7);
        register("flowing_water", 8);
        register("water", 9);
        register("flowing_lava", 10);
        register("lava", 11);
        register("sand", 12);
        register("gravel", 13);
        register("gold_ore", 14);
        register("iron_ore", 15);
        register("coal_ore", 16);
        register("log", 17);
        register("leaves", 18);
        register("sponge", 19);
        register("glass", 20);
        register("lapis_ore", 21);
        register("lapis_block", 22);
        register("dispenser", 23);
        register("sandstone", 24);
        register("noteblock", 25);
        register("bed", 26);
        register("golden_rail", 27);
        register("detector_rail", 28);
        register("sticky_piston", 29);
        register("web", 30);
        register("tallgrass", 31);
        register("deadbush", 32);
        register("piston", 33);
        register("piston_head", 34);
        register("wool", 35);
        register("piston_extension", 36);
        register("yellow_flower", 37);
        register("red_flower", 38);
        register("brown_mushroom", 39);
        register("red_mushroom", 40);
        register("gold_block", 41);
        register("iron_block", 42);
        register("double_stone_slab", 43);
        register("stone_slab", 44);
        register("brick_block", 45);
        register("tnt", 46);
        register("bookshelf", 47);
        register("mossy_cobblestone", 48);
        register("obsidian", 49);
        register("torch", 50);
        register("fire", 51);
        register("mob_spawner", 52);
        register("oak_stairs", 53);
        register("chest", 54);
        register("redstone_wire", 55);
        register("diamond_ore", 56);
        register("diamond_block", 57);
        register("crafting_table", 58);
        register("wheat", 59);
        register("farmland", 60);
        register("furnace", 61);
        register("lit_furnace", 62);
        register("standing_sign", 63);
        register("wooden_door", 64);
        register("ladder", 65);
        register("rail", 66);
        register("stone_stairs", 67);
        register("wall_sign", 68);
        register("lever", 69);
        register("stone_pressure_plate", 70);
        register("iron_door", 71);
        register("wooden_pressure_plate", 72);
        register("redstone_ore", 73);
        register("lit_redstone_ore", 74);
        register("unlit_redstone_torch", 75);
        register("redstone_torch", 76);
        register("stone_button", 77);
        register("snow_layer", 78);
        register("ice", 79);
        register("snow", 80);
        register("cactus", 81);
        register("clay", 82);
        register("reeds", 83);
        register("jukebox", 84);
        register("fence", 85);
        register("pumpkin", 86);
        register("netherrack", 87);
        register("soul_sand", 88);
        register("glowstone", 89);
        register("portal", 90);
        register("lit_pumpkin", 91);
        register("cake", 92);
        register("unpowered_repeater", 93);
        register("powered_repeater", 94);
        register("stained_glass", 95);
        register("trapdoor", 96);
        register("monster_egg", 97);
        register("stonebrick", 98);
        register("brown_mushroom_block", 99);
        register("red_mushroom_block", 100);
        register("iron_bars", 101);
        register("glass_pane", 102);
        register("melon_block", 103);
        register("pumpkin_stem", 104);
        register("melon_stem", 105);
        register("vine", 106);
        register("fence_gate", 107);
        register("brick_stairs", 108);
        register("stone_brick_stairs", 109);
        register("mycelium", 110);
        register("waterlily", 111);
        register("nether_brick", 112);
        register("nether_brick_fence", 113);
        register("nether_brick_stairs", 114);
        register("nether_wart", 115);
        register("enchanting_table", 116);
        register("brewing_stand", 117);
        register("cauldron", 118);
        register("end_portal", 119);
        register("end_portal_frame", 120);
        register("end_stone", 121);
        register("dragon_egg", 122);
        register("redstone_lamp", 123);
        register("lit_redstone_lamp", 124);
        register("double_wooden_slab", 125);
        register("wooden_slab", 126);
        register("cocoa", 127);
        register("sandstone_stairs", 128);
        register("emerald_ore", 129);
        register("ender_chest", 130);
        register("tripwire_hook", 131);
        register("tripwire", 132);
        register("emerald_block", 133);
        register("spruce_stairs", 134);
        register("birch_stairs", 135);
        register("jungle_stairs", 136);
        register("command_block", 137);
        register("beacon", 138);
        register("cobblestone_wall", 139);
        register("flower_pot", 140);
        register("carrots", 141);
        register("potatoes", 142);
        register("wooden_button", 143);
        register("skull", 144);
        register("anvil", 145);
        register("trapped_chest", 146);
        register("light_weighted_pressure_plate", 147);
        register("heavy_weighted_pressure_plate", 148);
        register("unpowered_comparator", 149);
        register("powered_comparator", 150);
        register("daylight_detector", 151);
        register("redstone_block", 152);
        register("quartz_ore", 153);
        register("hopper", 154);
        register("quartz_block", 155);
        register("quartz_stairs", 156);
        register("activator_rail", 157);
        register("dropper", 158);
        register("stained_hardened_clay", 159);
        register("stained_glass_pane", 160);
        register("leaves2", 161);
        register("log2", 162);
        register("acacia_stairs", 163);
        register("dark_oak_stairs", 164);
        register("slime", 165);
        register("barrier", 166);
        register("iron_trapdoor", 167);
        register("prismarine", 168);
        register("sea_lantern", 169);
        register("hay_block", 170);
        register("carpet", 171);
        register("hardened_clay", 172);
        register("coal_block", 173);
        register("packed_ice", 174);
        register("double_plant", 175);
    }

    private static void register(String shortName, int id) {
        AnvilMaterial material = new AnvilMaterial(MINECRAFT_PREFIX + shortName, id);
        byName.put(shortName, material);
        byId[id] = material;
    }

    @Override
    public Material getAir() {
        return byId[0];
    }

    @Override
    public AnvilMaterial getById(int id) throws MaterialNotFoundException {
        if (id < 0 || id > byId.length) {
            throw new MaterialNotFoundException(id);
        }
        AnvilMaterial material = byId[id];
        if (material == null) {
            throw new MaterialNotFoundException(id);
        }
        return material;
    }

    @Override
    public AnvilMaterial getByName(String name) throws MaterialNotFoundException {
        if (name.startsWith(MINECRAFT_PREFIX)) {
            name = name.substring(MINECRAFT_PREFIX.length());
        }
        AnvilMaterial material = byName.get(name.toLowerCase());
        if (material == null) {
            throw new MaterialNotFoundException(name);
        }
        return material;
    }

    @Override
    public AnvilMaterial getByNameOrId(String nameOrId) throws MaterialNotFoundException {
        try {
            return getById(Integer.parseInt(nameOrId));
        } catch (NumberFormatException e) {
            return getByName(nameOrId);
        }
    }

}
