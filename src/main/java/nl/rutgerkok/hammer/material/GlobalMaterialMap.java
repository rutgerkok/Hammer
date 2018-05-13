package nl.rutgerkok.hammer.material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import nl.rutgerkok.hammer.util.MaterialNotFoundException;

/**
 * Stores all {@link MaterialData} objects of all worlds.
 *
 * <p>This class is called global, because it is intended to be shared between
 * different worlds. If two worlds share a material with the same name (even if
 * their Minecraft ids are different), this class recognizes them as the same
 * material. This means that you can get a {@link MaterialData} object in one
 * world and use it in another world, even though Minecraft may have assigned
 * completely different block ids (see Podzol for example).
 *
 * <p>This system only holds up as long as two worlds share the same
 * {@link GlobalMaterialMap} instance. Otherwise, the internal Hammer ids (idh)
 * of the materials can be different, and a completely different material may be
 * seen/placed.
 *
 * <p>All public methods on this class are thread-safe.
 */
public final class GlobalMaterialMap {

    private final List<MaterialData> idToInfo = new ArrayList<>();
    private final Lock lock = new ReentrantLock();
    private final Map<MaterialName, MaterialData> nameToInfo = new HashMap<>();

    private final MaterialData air;

    public GlobalMaterialMap() {
        this.air = addMaterial(MaterialName.ofBaseName("minecraft:air"));
    }

    /**
     * Adds a material entry to this material map.
     *
     * <p>
     * A material may have multiple names, like "reeds" and "sugar cane". You
     * will be able to look up the material by any of these names using
     * {@link #getMaterialByName(MaterialName)}.
     *
     * <p>
     * If any of these names is already in use for a material, that material is
     * returned instead. However, any new names for the material supplied to
     * this method are still registered for use with
     * {@link #getMaterialByName(MaterialName)}.
     *
     * @param names
     *            All names of the material.
     * @return The idh of the material.
     */
    public MaterialData addMaterial(Collection<MaterialName> names) {
        try {
            lock.lock();

            // Search for existing entry
            MaterialName firstFoundName = null;
            for (MaterialName name : names) {
                if (firstFoundName == null) {
                    firstFoundName = name;
                }
                MaterialData found = nameToInfo.get(name);
                if (found != null) {
                    // Return existing material instead, but add possible new
                    // aliases first
                    addNameEntries(found, names);
                    return found;
                }
            }

            // Add new entry
            MaterialData newEntry = new MaterialData((char) idToInfo.size(), firstFoundName);
            idToInfo.add(newEntry);
            addNameEntries(newEntry, names);
            return newEntry;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Adds a material entry to this material map.
     *
     * <p>If an entry already exists in this material map, the idh of that entry
     * is returned instead. Otherwise, a new entry is created and then added.
     *
     * @param name
     *            Name of the material.
     * @return The idh of the material.
     */
    public MaterialData addMaterial(MaterialName name) {
        try {
            lock.lock();

            // Search for existing entry
            MaterialData found = nameToInfo.get(name);
            if (found != null) {
                return found;
            }

            // Add new entry
            MaterialData newEntry = new MaterialData((char) idToInfo.size(), name);
            idToInfo.add(newEntry);
            nameToInfo.put(name, newEntry);
            return newEntry;
        } finally {
            lock.unlock();
        }
    }

    private void addNameEntries(MaterialData materialData, Collection<MaterialName> names) {
        for (MaterialName name : names) {
            nameToInfo.put(name, materialData);
        }
    }

    /**
     * Gets the material representing air.
     *
     * @return The material representing air.
     */
    public MaterialData getAir() {
        return air;
    }

    /**
     * Gets the material with the given Hammer id.
     *
     * @param idh
     *            Id of the material.
     * @return The material.
     * @throws MaterialNotFoundException
     *             If no material exists with the given id.
     */
    public MaterialData getMaterialById(int idh) throws MaterialNotFoundException {
        try {
            lock.lock();
            if (idh < 0 || idh >= idToInfo.size()) {
                throw new MaterialNotFoundException(idh);
            }
            MaterialData returnValue = idToInfo.get(idh);
            if (returnValue == null) {
                throw new MaterialNotFoundException(idh);
            }
            return returnValue;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets the material with the given name. The name is case insensitive.
     *
     * @param name
     *            Name of the material.
     * @return The material.
     * @throws MaterialNotFoundException
     *             If no material exists with the given name.
     */
    public MaterialData getMaterialByName(MaterialName name) throws MaterialNotFoundException {
        try {
            lock.lock();
            MaterialData returnValue = nameToInfo.get(name);
            if (returnValue == null) {
                throw new MaterialNotFoundException(name);
            }
            return returnValue;
        } finally {
            lock.unlock();
        }
    }

}
