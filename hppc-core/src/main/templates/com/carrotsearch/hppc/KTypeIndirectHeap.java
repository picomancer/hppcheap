//
// This file (c) 2013 Benjamin Johnson, http://picomancer.com
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.carrotsearch.hppc;

import java.util.Iterator;

import com.carrotsearch.hppc.cursors.KTypeCursor;
import com.carrotsearch.hppc.predicates.KTypePredicate;
import com.carrotsearch.hppc.procedures.KTypeProcedure;

/**
 * A min-heap of <code>KType</code>s, using built-in comparison.
 */

/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeIndirectHeap<KType>
    extends AbstractKTypeCollection<KType> 
    implements Cloneable
{
    /**
     * Experimental!
     */
    public static boolean experimental_warning = true;

    /**
     * Minimum capacity.  Borrowed from HashContainerUtils.
     */
    public final static int MIN_CAPACITY = HashContainerUtils.MIN_CAPACITY;

    /**
     * Default capacity.  Borrowed from HashContainerUtils.
     */
    public final static int DEFAULT_CAPACITY = HashContainerUtils.DEFAULT_CAPACITY;

    /**
     * The actual heap data.
     */
    public KType [] heap;

    /**
     * The number of heap elements in use.
     */
    public int _size;

    /**
     * How to compare integers
     */
    public KTypeComparator comp;

    /**
     * Create a heap with the default capacity of {@value #DEFAULT_CAPACITY}.
`     */
    public KTypeIndirectHeap(KTypeComparator comp)
    {
        this(comp, DEFAULT_CAPACITY);
        return;
    }

    /**
     * Create a heap with the given capacity.
     */
    public KTypeIndirectHeap(KTypeComparator comp, int initialCapacity)
    {
        this.comp = comp;
        initialCapacity = Math.max(initialCapacity, MIN_CAPACITY);

        assert initialCapacity > 0
            : "Initial capacity must be between (0, " + Integer.MAX_VALUE + "].";

        if (IntHeap.experimental_warning)
        {
            System.err.println("hppc:  Heap classes are still considered experimental, use at own risk.  Set "+IntHeap.class+".experimental_warning = false to suppress this message.");
            IntHeap.experimental_warning = false;
        }

        this.allocateBuffers(HashContainerUtils.roundCapacity(initialCapacity));
        return;
    }

    /**
     * Create a heap from elements of another container.  Default load factor is used.
     */
    public KTypeIndirectHeap(KTypeComparator comp, KTypeContainer<KType> container)
    {
        this(comp, (int) (container.size()));
        this.addAll(container);
        return;
    }

    /**
     * {@inheritDoc}
     */
    //@Override
    public boolean add(KType e)
    {
        int n = this._size;

        assert n <= this.heap.length;

        //               0
        //       1               2
        //   3       4       5       6
        // 7   8   9   A   B   C   D   E
        //
        // parent elements of i : (i-1) >> 1
        //
        if (n == this.heap.length)
        {
            this.expand();
            assert n < this.heap.length;
            assert n == this._size;
        }

        // Use the following algorithm to add elements to the heap:
        //  Put new element at the bottom of the heap.
        //  Swap the new element and its parent up the tree, until
        //    the new element has an element heavier than its parent
        //    or the top of the tree is reached.

        this.heap[n] = e;

        while(n > 0)
        {
            // get parent offset
            int p = (n-1) >> 1;

            // if parent is smaller than us, then heap is ok
            KType p_e = this.heap[p];
            if (this.comp.compare(p_e, e) < 0)
                break;

            this.heap[p] = e;
            this.heap[n] = p_e;
            n = p;
        }
        this._size++;
        return true;
    }

    /**
     * Add two elements to the heap.
     *
     * @return Returns the number of elements that were added to the heap (equal
     * to the number of arguments passed).
     */
    public int add(KType e1, KType e2)
    {
        int count = 0;
        count += (this.add(e1) ? 1 : 0);
        count += (this.add(e2) ? 1 : 0);
        return count;
    }

    /**
     * Vararg-signature method for adding elements to this heap.
     * <p><b>This method is handy, but costly if used in tight loops (anonymous 
     * array passing)</b></p>
     * 
     * @return Returns the number of elements that were added to the heap (equal
     * to the number of arguments passed).
     */
    public int add(KType... elements)
    {
        int count = 0;
        for (KType e : elements)
            count += (this.add(e) ? 1 : 0);
        return count;
    }

    /**
     * Adds all elements from a given container to this heap.
     * 
     * @return Returns the number of elements actually added as a result of this
     * call (equal to the number of elements in container).
     */
    public int addAll(KTypeContainer<? extends KType> container)
    {
        return addAll((Iterable<? extends KTypeCursor<? extends KType>>) container);
    }

    /**
     * Adds all elements from a given iterable to this heap.
     * 
     * @return Returns the number of elements actually added as a result of this
     * call (equal to the number of elements in iterable).
     */
    public int addAll(Iterable<? extends KTypeCursor<? extends KType>> iterable)
    {
        int count = 0;
        for (KTypeCursor<? extends KType> cursor : iterable)
            count += (add(cursor.value) ? 1 : 0);
        return count;
    }

    /**
     * Remove and return the topmost element from the heap.
     *
     * @return The smallest element in the heap.
     */
    public KType pop()
    {
        int n = this._size;
        int nm1 = n-1;
        assert n > 0;

        // remove 0th element and pull up the "hole" that remains
        //               0
        //       1               2
        //   3       4       5       6
        // 7   8   9   A   B   C   D   E
        //
        // child elements of i : (i << 1) + 1, (i << 1) + 2

        KType result = this.heap[0];
        this.heap[0] = this.heap[nm1];
        /* #if ($TemplateOptions.KTypeGeneric) */ this.heap[nm1] = null; /* #end */
        this._relax();
        this._size = nm1;
        return result;
    }

    private void _relax()
    {
        // This method fixes a heap which is OK except for possibly the root element.

        int n = this._size;
        int p = 0;
        KType h_p = this.heap[0];

        // grab the top element, then rotate the smaller child into each position.
        relax_loop:
        while(true)
        {
            assert p < n;
            int a, b = (p+p+2);
            KType h_a, h_b;
            if (b >= n)
            {
                if (b == n)
                {
                    // we have one child.  the child should be the smaller of the two elements.
                    a = b-1;
                    h_a = this.heap[a];
                    boolean p_a = (this.comp.compare(h_p, h_a) <= 0);
                    this.heap[p] = (p_a ? h_p : h_a);
                    this.heap[a] = (p_a ? h_a : h_p);
                }
                else
                {
                    // we have zero children.  put down h_p in current position.
                    this.heap[p] = h_p;
                }
                break relax_loop;
            }
            a = b-1;
            h_a = this.heap[a];
            h_b = this.heap[b];

            int d = ((this.comp.compare(h_p, h_a) <= 0) ? 1 : 0)
                  | ((this.comp.compare(h_p, h_b) <= 0) ? 2 : 0)
                  ;
            switch(d)
            {
                case 0:
                    // the new element's heavier than both children; we need the lighter child to rise to the top
                    //   which means another comparison.
                    boolean d2 = (this.comp.compare(h_a, h_b) <= 0);
                    this.heap[p] = (d2 ? h_a : h_b);
                    p   = (d2 ? a : b);
                    break;
                case 1:
                    // the parent element's lighter than a, which means b must rise and we descend along b
                    this.heap[p] = h_b;
                    p = b;
                    break;
                case 2:
                    // the parent element's lighter than b, which means a must rise and we descend along a
                    this.heap[p] = h_a;
                    p = a;
                    break;
                case 3:
                    // the parent element's smaller than both children; it's reached its ending position without descending completely
                    this.heap[p] = h_p;
                    break relax_loop;
            }
        }
        return;
    }

    /**
     * Push the given value, then pop a value.
     *
     * Much more efficient than a call to push() followed by a call to pop().
     */
    public KType push_pop(KType e)
    {
        if (this._size == 0)
            return e;
        KType result = this.heap[0];
        if (this.comp.compare(e, result) <= 0)
            return e;
        this.heap[0] = e;
        this._relax();
        return result;
    }

    /**
     * Pop a value, then push the given value.
     *
     * Much more efficient than a call to pop() followed by a call to push().
     */
    public KType pop_push(KType e)
    {
        assert this._size > 0;
        KType result = this.heap[0];
        this.heap[0] = e;
        this._relax();
        return result;
    }

    /**
     * Return the topmost element in the heap.
     */
    public KType peek()
    {
        assert this._size > 0;
        return this.heap[0];
    }

    /**
     * Check if the heap is empty.
     *
     * You should call this before peek(), pop(), or pop_push()
     */
    public boolean isEmpty()
    {
        return this._size > 0;
    }

    @Override
    public void clear()
    {
        this._size = 0;
        return;
    }

    //@Override
    public int removeAll(KTypeContainer<? extends KType> container)
    {
        throw(new UnsupportedOperationException("illegal operation: removal from heap"));
    }

    @Override
    public int removeAll(KTypePredicate<? super KType> predicate)
    {
        throw(new UnsupportedOperationException("illegal operation: removal from heap"));
    }

    //@Override
    public boolean remove(KType key)
    {
        throw(new UnsupportedOperationException("illegal operation: removal from heap"));
    }

    /**
     * Test for membership.  Arguments smaller than the smallest element in the container are
     * quickly rejected in O(1).  Otherwise the entire array is scanned by brute force until a match is found.
     *
     * This method may be optimized in future releases.
     */
    @Override
    public boolean contains(KType k)
    {
        int n = this._size;
        if (n == 0)
            return false;
        final KType [] h = this.heap;
        if (this.comp.compare(k, h[0]) < 0)
            return false;
        for(int i=0;i<h.length;i++)
        {
            if (this.comp.compare(h[i], k) == 0)
                return true;
        }
        return false;
    }

    @Override
    public int removeAllOccurrences(KType key)
    {
        throw(new UnsupportedOperationException("illegal operation: removal from heap"));
    }

    /**
     * This function currently visits the elements in unsorted order.
     * This behavior may change in future releases.
     */

    @Override
    public <T extends KTypeProcedure<? super KType>> T forEach(T procedure)
    {
        final KType [] h = this.heap;
        int n = this._size;

        for(int i=0;i<n;i++)
            procedure.apply(h[i]);

        return procedure;
    }

    /**
     * This function currently visits the elements in unsorted order.
     * This behavior may change in future releases.
     */

    @Override
    public <T extends KTypePredicate<? super KType>> T forEach(T predicate)
    {
        final KType [] h = this.heap;
        int n = this._size;

        for(int i=0;i<n;i++)
        {
            if (!predicate.apply(h[i]))
                break;
        }

        return predicate;
    }

    @Override
    public int size()
    {
        return this._size;
    }

    /**
     * Iterate over elements in unsorted order.
     * This behavior may change in future releases.
     */
    public class _Iterator
        extends AbstractIterator<KTypeCursor<KType>>
    {
        public KTypeIndirectHeap heap;
        public KTypeCursor<KType> cursor;

        public _Iterator(KTypeIndirectHeap heap)
        {
            this.heap = heap;
            this.cursor = new KTypeCursor<KType>();
            this.cursor.index = -1;
            return;
        }

        @Override
        protected KTypeCursor<KType> fetch()
        {
            int i = this.cursor.index+1;
            if (i >= this.heap._size)
                return this.done();
            this.cursor.index = i;
            /* #if ($TemplateOptions.KTypeGeneric) */
            this.cursor.value = (KType) this.heap.heap[i];
            /* #else */
            this.cursor.value = this.heap.heap[i];
            /* #end */
            return cursor;
        }
    }

    /**
     * Iterate over elements in unsorted order.
     * This behavior may change in future releases.
     */
    @Override
    public Iterator<KTypeCursor<KType>> iterator()
    {
        return new _Iterator(this);
    }

    public void expand()
    {
        this.allocateBuffers(HashContainerUtils.nextCapacity(this.heap.length));
        return;
    }

    public void allocateBuffers(int capacity)
    {
        KType [] new_heap = Intrinsics.newKTypeArray(capacity);
        if (this.heap != null)
            System.arraycopy(this.heap, 0, new_heap, 0, Math.min(this._size, capacity));
        this.heap = new_heap;
        return;
    }

}

