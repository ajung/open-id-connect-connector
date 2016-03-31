package org.mule.modules.openidconnect.client.relyingparty.storage;

import org.mule.api.store.ObjectStore;
import org.mule.api.store.ObjectStoreException;

import java.io.Serializable;

/**
 * Provides an Mule ObjectStore to store, read and remove data in it. Also allows to check availability of data.
 *
 * @author Moritz Möller, AOE GmbH
 *
 */
public class Storage<T extends Serializable>{

    private ObjectStore<T> store;

    public Storage(ObjectStore<T> store) {
        this.store = store;
    }

    /**
     * Stores given data with given id. If data already exists, it will be overwritten
     * @param entryId ID of the storage entry
     * @param storeData Data to be stored
     * @throws ObjectStoreException if data cant be stored
     */
    public void storeData(String entryId, T storeData) throws ObjectStoreException {
        if (store.contains(entryId)) {
            store.remove(entryId);
        }
        store.store(entryId, storeData);
    }

    /**
     * Reads and returns data from store
     * @param entryId ID of the storage entry
     * @return The stored data or null
     * @throws ObjectStoreException if data cant be read
     */
    public T getData(String entryId) throws ObjectStoreException {
        if (store.contains(entryId)){
            return store.retrieve(entryId);
        } else return null;
    }

    /**
     * Checks if data exists in store
     * @param entryId ID of the storage entry
     * @return True if exists, false if not
     * @throws ObjectStoreException if data cant be read
     */
    public boolean containsData(String entryId) throws ObjectStoreException {
        return entryId != null && store.contains(entryId);
    }

    /**
     * Removes data by given id from store
     * @param entryId ID of the storage entry
     * @throws ObjectStoreException if data cant be deleted
     */
    public void removeData(String entryId) throws ObjectStoreException {
        if (store.contains(entryId)){
            store.remove(entryId);
        }
    }
}
