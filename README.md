The <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/collections/index.html" target="_blank">Java Collections Framework</a> provides an easy and simple way to create unmodifiable lists, sets and maps from existing ones, using the <code>Collections</code>' <code>unmodifiable<em>XXX</em>()</code> methods (<a href="http://docs.oracle.com/javase/7/docs/api/java/util/Collections.html" target="_blank">Java Doc</a>).  In this article we will discuss how this works and will demonstrate common pitfalls when using such methods.


All code listed below is available at: <a href="https://github.com/javacreed/modifying-an-unmodifiable-list" target="_blank">https://github.com/javacreed/modifying-an-unmodifiable-list</a>, under the <em>collections</em> project.  Most of the examples will not contain the whole code and may omit fragments which are not relevant to the example being discussed.  The readers can download or view all code from the above link.


<h2>Unmodifiable List</h2>

Consider the following simple example.

<pre>
final List&lt;String&gt; modifiable = new ArrayList&lt;&gt;();
modifiable.add("Java");
modifiable.add("is");

final List&lt;String&gt; unmodifiable = Collections.unmodifiableList(modifiable);
System.out.println("Before modification: " + unmodifiable);

modifiable.add("the");
modifiable.add("best");

System.out.println("After modification: " + unmodifiable);
</pre>


Here we have a list named: <code>modifiable</code>, from which we create an unmodifiable version using the <code>Collections</code>' method <code>unmodifiableList()</code>, named: <code>unmodifiable</code>.  As their names imply, one list is modifiable (we can add and remove elements) while the other is read-only.


When executed, the above code will print the following to the command prompt.


<pre>
Before modification: [Java, is]
After modification: [Java, is, the, best]
</pre>


If this was a surprise or an unexpected answer, then you should continue reading this article to find out why this was produced.


<strong>Why was the list <code>unmodifiable</code> modified?</strong>


Let us first have a look at the <a href="http://docs.oracle.com/javase/7/docs/api/java/util/Collections.html#unmodifiableList(java.util.List)" target="_blank">Java Doc</a> of the <code>Collections</code>' <code>unmodifiableList()</code> method.


<blockquote cite="http://docs.oracle.com/javase/7/docs/api/java/util/Collections.html#unmodifiableList(java.util.List)">


<code>public static &lt;T&gt; List&lt;T&gt; unmodifiableList(List&lt;? extends T&gt; list)</code>


Returns an unmodifiable view of the specified list.  This method allows modules to provide users with "read-only" access to internal lists.  Query operations on the returned list "read through" to the specified list, and attempts to modify the returned list, whether direct or via its iterator, result in an UnsupportedOperationException.


The returned list will be serializable if the specified list is serializable. Similarly, the returned list will implement RandomAccess if the specified list does.


Parameters:
list - the list for which an unmodifiable view is to be returned.

Returns:
an unmodifiable view of the specified list.
</blockquote>


The documentation mentions that the returned object (the unmodifiable list, referred to as <em>returned list</em> in this paragraph) is a read-only view of the given one (referred to in this paragraph as the <em>given list</em>).  It says nothing about the behaviour we just saw.  Well now we know that while we cannot modify the returned list, any changes to the given list are observed by the returned one.


This is very important and very useful too.  We can share a list with another object without the object (receiver) being able to modify it.  But this is not always desirable as we can have weird surprises, especially when it comes to <a href="http://en.wikipedia.org/wiki/Builder_pattern" target="_blank">builders</a> and <a href="http://en.wikipedia.org/wiki/Immutable_object" target="_blank">immutable objects</a>, as discussed in the following section.


<h2>Builders and Immutable objects</h2>


This problem was first observed during code review of an immutable object that makes use of the builder pattern.  The object that was planned to be immutable, was not truly immutable as we could still modify it past its creation.


Consider the following class.


<pre>
package com.javacreed.examples.collections.part2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Person {

  public static class Builder {
    private String name;
    private final List&lt;String&gt; friends = new ArrayList&lt;&gt;();

    public Builder addFriend(final String name) {
      this.friends.add(name);
      return this;
    }

    public Person build() {
      return new Person(this);
    }

    public Builder setName(final String name) {
      this.name = name;
      return this;
    }
  }

  private final String name;
  private final List&lt;String&gt; friends;

  private Person(final Builder builder) {
    this.name = builder.name;
    this.friends = Collections.unmodifiableList(builder.friends);
  }

  public List&lt;String&gt; getFriends() {
    return friends;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return String.format("%s has %d friends", name, friends.size());
  }
}
</pre>


Here we have a class that represents a person.  The person has a list of friends (which are of type <code>String</code> to keep this example as simple as possible).  The <code>Person</code> class is expected to be immutable and therefore we cannot modify it once created.  The <code>Person</code> class provides no <strong>public</strong> means by which another class can modify its state.


If observed closely, we will notice that this class is not truly immutable as owe can still modify it, even after it is created.  This can be achieved through the <code>Builder</code> instance used to create the class as shown next.  This class has the same weakness, if you may, as we saw in the previous example.


<pre>
final Person.Builder builder = new Person.Builder();
builder.setName("Albert Attard");
builder.addFriend("John White");
builder.addFriend("Mary Vella");

final Person person = builder.build();
System.out.println("Before modification: " + person);

<span class="comments">// Adding a new friend after the object was created</span>
builder.addFriend("Joe Borg");
System.out.println("After modification: " + person);
</pre>

The above code will print the following to the command prompt.

<pre>
Before modification: Albert Attard has 2 friends
After modification: Albert Attard has 3 friends
</pre>


This should be of no surprise as the <code>Builder</code> class is sharing the list of friends with the <code>Person</code>.  In the following section we will see how we can prevent this behaviour from happening.


<h2>How to prevent modifications to the unmodifiable list?</h2>


The solution to this problem is quite simple and is highlighted in the following code.


<pre>
final List&lt;String&gt; modifiable = new ArrayList&lt;&gt;();
modifiable.add("Java");
modifiable.add("is");

<span class="comments">// Here we are creating a new array list</span>
final List&lt;String&gt; unmodifiable = Collections.unmodifiableList(<span class="highlight">new ArrayList&lt;&gt;(modifiable)</span>);
System.out.println("Before modification: " + unmodifiable);

modifiable.add("the");
modifiable.add("best");

System.out.println("After modification: " + unmodifiable);
</pre>


Note that in this example, we are not passing the <code>modifiable</code> list object, but a new list created from this one.  When we create a list like this, the elements of the given list are simply copied to the one being created.  Therefore, these two lists (the <code>modifiable</code> list and the one just created: <code>new ArrayList&lt;&gt;(modifiable)</code> list) are disconnected and they only share the elements.  Any modifications to each list will not affect the other.


One has to be aware that if we modify any of the elements within any of the lists, then all variables referring to the modified object will observe the modification.  This is not the case here as <code>String</code> is an immutable object and thus cannot be changed once created.  But if the objects contained by these two lists where mutable, then any changes to the objects from one of the list will be observed in the peer element in the other list.


The above code will produce the following result


<pre>
Before modification: [Java, is]
After modification: [Java, is]
</pre>

Now the <code>unmodifiable</code> list was not affected by the changes made to the <code>modifiable</code> lost.  The same technique can be applied to the <code>Person</code> class saw earlier to prevent the <code>Builder</code> instance from accidentally modifying the supposedly immutable object, as shown in the next example.

<pre>
package com.javacreed.examples.collections.part3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Person {

  public static class Builder {
    private String name;
    private final List&lt;String&gt; friends = new ArrayList&lt;&gt;();

    public Builder addFriend(final String name) {
      this.friends.add(name);
      return this;
    }

    public Person build() {
      return new Person(this);
    }

    public Builder setName(final String name) {
      this.name = name;
      return this;
    }
  }

  private final String name;
  private final List&lt;String&gt; friends;

  private Person(final Builder builder) {
    this.name = builder.name;
    this.friends = Collections.unmodifiableList(<span class="highlight">new ArrayList&lt;&gt;(builder.friends)</span>);
  }

  public List&lt;String&gt; getFriends() {
    return friends;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return String.format("%s has %d friends", name, friends.size());
  }
}
</pre>


This concludes our article about how to control whether an unmodifiable list is updated after its creation or not.  Please note that the <code>Collections</code>' methods are working well as these were designed to work.  It allows us to share a list, set or map with another objects without giving them the possibility to modify them, while we can manipulate them as required.  Documentation should state this behaviour clearly as many who mistakenly believe that the returned list will not be affected by changes made to the given list.
