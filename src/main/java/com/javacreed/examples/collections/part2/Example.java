/*
 * #%L
 * Modifying an Unmodifiable List
 * $Id:$
 * $HeadURL: https://java-creed-examples.googlecode.com/svn/collections/Modifying%20an%20Unmodifiable%20List/src/main/java/com/javacreed/examples/collections/part2/Example.java $
 * %%
 * Copyright (C) 2012 - 2014 Java Creed
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
/**
 * Copyright 2012-2014 Java Creed.
 *
 * Licensed under the Apache License, Version 2.0 (the "<em>License</em>");
 * you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.javacreed.examples.collections.part2;

public class Example {
  public static void main(final String[] args) {

    final Person.Builder builder = new Person.Builder();
    builder.setName("Albert Attard");
    builder.addFriend("John White");
    builder.addFriend("Mary Vella");

    final Person person = builder.build();
    System.out.println("Before modification: " + person);

    // Adding a new friend after the object was created
    builder.addFriend("Joe Borg");
    System.out.println("After modification: " + person);
  }
}
