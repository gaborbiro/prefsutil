package com.gb.prefsutil

class ObservableList<T>(private val list: MutableList<T>, private val changed: (List<T>) -> Unit) : MutableList<T> {

    override fun iterator(): MutableIterator<T> {
        return object : MutableIterator<T> {
            private val i = list.iterator()

            override fun hasNext() = i.hasNext()

            override fun next() = i.next()

            override fun remove() {
                i.remove()
                changed.invoke(list)
            }
        }
    }

    override fun add(element: T) = list.add(element).also { changed.invoke(list) }

    override fun add(index: Int, element: T) = list.add(index, element).also { changed.invoke(list) }

    override fun addAll(index: Int, elements: Collection<T>) =
        list.addAll(index, elements).also { changed.invoke(list) }

    override fun addAll(elements: Collection<T>) = list.addAll(elements).also { changed.invoke(list) }

    override fun clear() = list.clear().also { changed.invoke(list) }

    override fun listIterator(): MutableListIterator<T> =
        ObservableListIterator(list.listIterator()) { changed.invoke(this) }

    override fun listIterator(index: Int): MutableListIterator<T> =
        ObservableListIterator(list.listIterator(index)) { changed.invoke(this) }

    override fun remove(element: T) = list.remove(element).also { changed.invoke(list) }

    override fun removeAll(elements: Collection<T>) = list.removeAll(elements).also { changed.invoke(list) }

    override fun removeAt(index: Int) = list.removeAt(index).also { changed.invoke(list) }

    override fun retainAll(elements: Collection<T>) = list.retainAll(elements).also { changed.invoke(list) }

    override fun set(index: Int, element: T) = list.set(index, element).also { changed.invoke(list) }

    override val size: Int
        get() = list.size

    override fun contains(element: T) = list.contains(element)

    override fun containsAll(elements: Collection<T>) = list.containsAll(elements)

    override fun get(index: Int) = list[index]

    override fun indexOf(element: T) = list.indexOf(element)

    override fun lastIndexOf(element: T) = list.lastIndexOf(element)

    override fun isEmpty() = list.isEmpty()

    override fun subList(fromIndex: Int, toIndex: Int) = list.subList(fromIndex, toIndex)
}

private class ObservableListIterator<T>(
    private val listIterator: MutableListIterator<T>,
    private val changed: () -> Unit
) : MutableListIterator<T> {

    override fun hasPrevious() = listIterator.hasPrevious()

    override fun nextIndex() = listIterator.nextIndex()

    override fun previous() = listIterator.previous()

    override fun previousIndex() = listIterator.previousIndex()

    override fun add(element: T) = listIterator.add(element).also { changed.invoke() }

    override fun hasNext() = listIterator.hasNext()

    override fun next() = listIterator.next()

    override fun remove() = listIterator.remove().also { changed.invoke() }

    override fun set(element: T) = listIterator.set(element).also { changed.invoke() }
}