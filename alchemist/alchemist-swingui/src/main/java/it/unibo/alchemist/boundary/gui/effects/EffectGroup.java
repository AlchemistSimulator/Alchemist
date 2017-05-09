package it.unibo.alchemist.boundary.gui.effects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Models a group of effects. It is a decorator pattern for a List<Effect> collection.
 */
public class EffectGroup implements List<Effect> {
    private final List<Effect> effects = new ArrayList<>();

    @Override
    public int size() {
        return effects.size();
    }

    @Override
    public boolean isEmpty() {
        return effects.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return effects.contains(o);
    }

    @Override
    public Iterator<Effect> iterator() {
        return effects.iterator();
    }

    @Override
    public Object[] toArray() {
        return effects.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return effects.toArray(a);
    }

    @Override
    public boolean add(final Effect e) {
        return effects.add(e);
    }

    @Override
    public boolean remove(final Object o) {
        return effects.remove(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return effects.containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends Effect> c) {
        return effects.addAll(c);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends Effect> c) {
        return effects.addAll(index, c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return effects.removeAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return effects.retainAll(c);
    }

    @Override
    public void clear() {
        effects.clear();
    }

    @Override
    public Effect get(final int index) {
        return effects.get(0);
    }

    @Override
    public Effect set(final int index, final Effect element) {
        return effects.set(index, element);
    }

    @Override
    public void add(final int index, final Effect element) {
        effects.add(index, element);
    }

    @Override
    public Effect remove(final int index) {
        return effects.remove(index);
    }

    @Override
    public int indexOf(final Object o) {
        return effects.indexOf(o);
    }

    @Override
    public int lastIndexOf(final Object o) {
        return effects.lastIndexOf(o);
    }

    @Override
    public ListIterator<Effect> listIterator() {
        return effects.listIterator();
    }

    @Override
    public ListIterator<Effect> listIterator(final int index) {
        return effects.listIterator(index);
    }

    @Override
    public List<Effect> subList(final int fromIndex, final int toIndex) {
        return effects.subList(fromIndex, toIndex);
    }

}
