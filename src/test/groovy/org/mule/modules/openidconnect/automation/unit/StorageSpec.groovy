package org.mule.modules.openidconnect.automation.unit

import org.mule.api.store.ObjectStore
import org.mule.modules.openidconnect.client.relyingparty.storage.Storage
import spock.lang.Specification


/**
 * Test specification for the Storage
 *
 * @author Moritz Möller, AOE GmbH
 *
 */
class StorageSpec extends Specification {
    def store = Mock(ObjectStore)
    def storage = new Storage(store)
    def key = "storageKey"
    def entry = "entry"


    def "store already consisting data in storage"() {
        when:
        storage.storeData(key, entry)

        then:
        1 * store.contains(key) >> true
        1 * store.remove(key)
        1 * store.store(key, entry)
    }

    def "store new data in storage"() {
        when:
        storage.storeData(key, entry)

        then:
        1 * store.contains(key) >> false
        0 * store.remove(key)
        1 * store.store(key, entry)
    }

    def "read existing data"() {
        setup:
        store.contains(key) >> true
        store.retrieve(key) >> entry

        expect:
        storage.getData(key) == entry
    }

    def "read non existing data"() {
        when:
        storage.getData(key)

        then:
        1 * store.contains(key) >> false
        0 * store.retrieve(key)
        storage.getData(key) == null
    }

    def "check if storage contains existing data"() {
        setup:
        store.contains(key) >> true

        expect:
        storage.containsData(key)
    }

    def "check if storage contains non existing data"() {
        setup:
        store.contains(key) >> false

        expect:
        !storage.containsData(key)
    }

    def "remove existing data"() {
        when:
        storage.removeData(key)

        then:
        1 * store.contains(key) >> true
        1 * store.remove(key)
    }

    def "remove non existing data"() {
        when:
        storage.removeData(key)

        then:
        1 * store.contains(key) >> false
        0 * store.remove(key)
    }
}