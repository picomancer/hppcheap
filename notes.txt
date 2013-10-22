
Hi.  I wrote a hppc implementation of heap (priority queue).  It is available at https://github.com/picomancer/hppcheap.

Building it is going to be a pain.  I made two versions of the heap, one that uses a comparator class,
and a faster one using the <= operator.  The operator version probably won't build with hppc's
existing scripts because `ObjectHeap.java` is uncompilable as a result of these operators.  I simply
delete `ObjectHeap.java` in my shell script before compiling, but Maven is so awful and obtuse that
it will probably take me longer to figure out how to do that in Maven than it took me to write the
whole heap implementation!  So you're on your own as far as getting it to build.  Here are a couple
suggestions of how to fix the `ObjectHeap` situation:

- Use Intrinsics class to implement le, ge, lt, gt which writes "(a <= b)" for primitives, and "(((Comparable<Object>) a).compareTo(b))" for Objects
- Alternately you might somehow tell the TemplateProcessor not to generate ObjectHeap.java
- Or convince Maven to delete ObjectHeap.java before compiling it, or exclude it from the compile
- As a last resort, you could do conditional compilation on every instance of the <= operator

This code is relatively untested.  I made a small class to test every permutation of input elements
for heap sizes up to and including 10.  (This caught silly errors like not incrementing the heap size
when elements are added.)  Most of the code isn't covered by the tests that are there.  The whole
thing should probably receive more thorough testing, and ideally it would have 100% test coverage.

The KTypeIndirectHeap hasn't had any testing whatsoever!  It's derived from straightforward
modifications to KTypeHeap.  It compiles, and that's all I can say about it at the moment.

I am just using shell scripts to build and test the code.  It works for me, and it should work for
you on a recent distribution of a vaguely Debian- or Ubuntu-like system, if you run the `apt-get`
command in `install.md` first.

If you are running Windows, you're on your own figuring out how to build the code.  The last time
I made an unsolicited contribution of an ant script to an open source project that used Maven,
it wasn't well received.  So I'm not going to go to the effort of making my build cross-platform
in any way, shape or form, on the assumption you're going to pitch any non-Maven build solution out
on its ear.  So I just wrote the build to be the dirtiest, fastest possible thing to write, that
works for me, on my system.  YMMV.

Things to consider for the future:

- Should iteration over `Heap` be in sorted order, or not?
- Search for methods with `//@Override`.  Should some of them be removed?
- Removal throws UnsupportedOperationException.  Should it be implemented?
- We should be able to use the heap structure to optimize `contains()` a little more
- Maybe stylistic nitpicks with leading underscores, variable names, everything possible public and non-final, etc.
- `clone()` and `toString()` and `from()` aren't implemented.  Maybe they should be, but I don't care about these.
- The class prints a suppressible warning in the constructor.  This should go away when the API stabilizes and there's been more testing.
- Maybe we don't want to remove the warning flag without letting it be deprecated for a while first, to avoid breaking code in the wild that sets `experimental_warning = false`.

Packaging details:

- Should all of this go into sorting package?  Or maybe a new heap package?
- Should comparators go into com.carrotsearch.hppc.comparators to mirror predicates and procedures?

