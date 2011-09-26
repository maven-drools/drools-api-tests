/*******************************************************************************
 * Copyright (c) 2009-2011 Ansgar Konermann
 *
 * This file is part of the "Maven 3 Drools Support" Package.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package de.lightful.drools.apitests;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.core.util.DroolsStreamUtils;
import org.drools.definition.KnowledgePackage;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Collection;

import static org.fest.assertions.Assertions.assertThat;

@Test
public class SerializationWithDependenciesTest {

  public static final String DECLARED_TYPE_ONE = "package model;\n" +
                                                 "\n" +
                                                 "declare Person\n" +
                                                 "  name: String\n" +
                                                 "end";

  public static final String DECLARED_TYPE_TWO = "package model;\n" +
                                                 "\n" +
                                                 "declare Age\n" +
                                                 "  person: Person\n" +
                                                 "  age: Integer\n" +
                                                 "end";

  public static final String RULE_USING_BOTH_TYPES = "package rules;\n" +
                                                     "\n" +
                                                     "dialect \"mvel\"\n" +
                                                     "\n" +
                                                     "import model.Person;\n" +
                                                     "import model.Age;\n" +
                                                     "\n" +
                                                     "rule \"check minimum age\"\n" +
                                                     "  when\n" +
                                                     "    Age( $person: person, age < 18 )\n" +
                                                     "  then\n" +
                                                     "    insertLogical(\"TOO YOUNG: \" + $person.name )\n" +
                                                     "end";

  public static final String RULE_USING_NONE_OF_THESE_TYPES = "package rules;\n" +
                                                              "\n" +
                                                              "dialect \"mvel\"\n" +
                                                              "\n" +
                                                              "rule \"fire always\"\n" +
                                                              "  when\n" +
                                                              "    //\n" +
                                                              "  then\n" +
                                                              "    insertLogical(\"RULE FIRED. \")\n" +
                                                              "end";

  private KnowledgeBuilder knowledgeBuilder;

  @BeforeMethod
  protected void setUp() throws Exception {
    knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
  }

  @Test
  public void testCanSerializeAndDeSerializeWithDependencies() throws Exception {
    knowledgeBuilder.add(stringResource(DECLARED_TYPE_ONE), ResourceType.DRL);
    knowledgeBuilder.add(stringResource(DECLARED_TYPE_TWO), ResourceType.DRL);
    knowledgeBuilder.add(stringResource(RULE_USING_BOTH_TYPES), ResourceType.DRL);
    final Collection<KnowledgePackage> knowledgePackages = knowledgeBuilder.getKnowledgePackages();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    DroolsStreamUtils.streamOut(outputStream, knowledgePackages);
    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    final Object deserializedObject = DroolsStreamUtils.streamIn(inputStream);

    assertThat(deserializedObject).isNotNull();
    assertThat(deserializedObject).isInstanceOf(Collection.class);
    Collection<Object> deserializedCollection = (Collection<Object>) deserializedObject;
    assertThat(deserializedCollection).hasSize(2);
  }

  @Test
  public void testCanSerializeAndDeSerializeWithoutDependencies() throws Exception {
    knowledgeBuilder.add(stringResource(DECLARED_TYPE_ONE), ResourceType.DRL);
    knowledgeBuilder.add(stringResource(DECLARED_TYPE_TWO), ResourceType.DRL);
    knowledgeBuilder.add(stringResource(RULE_USING_NONE_OF_THESE_TYPES), ResourceType.DRL);
    final Collection<KnowledgePackage> knowledgePackages = knowledgeBuilder.getKnowledgePackages();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    DroolsStreamUtils.streamOut(outputStream, knowledgePackages);
    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    final Object deserializedObject = DroolsStreamUtils.streamIn(inputStream);

    assertThat(deserializedObject).isNotNull();
    assertThat(deserializedObject).isInstanceOf(Collection.class);
    Collection<Object> deserializedCollection = (Collection<Object>) deserializedObject;
    assertThat(deserializedCollection).hasSize(2);
  }

  private Resource stringResource(String droolsSourcecodeSnippet) {
    return ResourceFactory.newReaderResource(new StringReader(droolsSourcecodeSnippet));
  }
}
