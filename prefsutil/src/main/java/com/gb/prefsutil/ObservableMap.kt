package com.gb.prefsutil

class ObservableMap<K, V>(private val map: MutableMap<K, V>, private val changed: (Map<K, V>) -> Unit) :
    MutableMap<K, V> {

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = ObservableSet(map.entries) {
            changed.invoke(map)
        }

    override val keys: MutableSet<K>
        get() = ObservableSet(map.keys) {
            changed.invoke(map)
        }

    override val values: MutableCollection<V>
        get() = ObservableCollection(map.values) {
            changed.invoke(map)
        }

    override fun clear() {
        map.clear()
        changed.invoke(map)
    }

    override fun put(key: K, value: V): V? {
        return map.put(key, value).also {
            changed.invoke(map)
        }
    }

    override fun putAll(from: Map<out K, V>) {
        return map.putAll(from).also {
            changed.invoke(map)
        }
    }

    override fun remove(key: K): V? {
        return map.remove(key).also {
            changed.invoke(map)
        }
    }

    override val size: Int
        get() = map.size

    override fun containsKey(key: K) = map.containsKey(key)

    override fun containsValue(value: V) = map.containsValue(value)

    override fun get(key: K) = map[key]

    override fun isEmpty() = map.isEmpty()

    override fun toString() = map.toString()
}

class ObservableSet<E>(
    private val set: MutableSet<E>,
    private val changed: () -> Unit
) : MutableSet<E> {

    override fun add(element: E): Boolean {
        return set.add(element).also {
            changed.invoke()
        }
    }

    override fun addAll(elements: Collection<E>): Boolean {
        return set.addAll(elements).also {
            changed.invoke()
        }
    }

    override fun clear() {
        return set.clear().also {
            changed.invoke()
        }
    }

    override fun iterator(): MutableIterator<E> {
        return object : MutableIterator<E> {
            private val i = set.iterator()

            override fun hasNext() = i.hasNext()

            override fun next() = i.next()

            override fun remove() {
                i.remove()
                changed.invoke()
            }
        }
    }

    override fun remove(element: E): Boolean {
        return set.remove(element).also {
            changed.invoke()
        }
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        return set.removeAll(elements).also {
            changed.invoke()
        }
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        return set.retainAll(elements).also {
            changed.invoke()
        }
    }

    override val size = set.size

    override fun contains(element: E) = set.contains(element)

    override fun containsAll(elements: Collection<E>) = set.containsAll(elements)

    override fun isEmpty() = set.isEmpty()

    override fun toString() = set.toString()
}

class ObservableCollection<E>(
    private val collection: MutableCollection<E>,
    private val changed: () -> Unit
) : MutableCollection<E> {

    override val size = collection.size

    override fun contains(element: E) = collection.contains(element)

    override fun containsAll(elements: Collection<E>) = collection.containsAll(elements)

    override fun isEmpty() = collection.isEmpty()

    override fun add(element: E): Boolean {
        return collection.add(element).also {
            changed.invoke()
        }
    }

    override fun addAll(elements: Collection<E>): Boolean {
        return collection.addAll(elements).also {
            changed.invoke()
        }
    }

    override fun clear() {
        return collection.clear().also {
            changed.invoke()
        }
    }

    override fun iterator(): MutableIterator<E> {
        return object : MutableIterator<E> {
            private val i = collection.iterator()

            override fun hasNext() = i.hasNext()

            override fun next() = i.next()

            override fun remove() {
                i.remove()
                changed.invoke()
            }
        }
    }

    override fun remove(element: E): Boolean {
        return collection.remove(element).also {
            changed.invoke()
        }
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        return collection.removeAll(elements).also {
            changed.invoke()
        }
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        return collection.retainAll(elements).also {
            changed.invoke()
        }
    }

    override fun toString() = collection.toString()
}