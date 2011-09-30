/*******************************************************************************
 * Copyright (c) 2009-2011 Ansgar Konermann <konermann@itikko.net>
 *
 * This file is part of the Maven 3 Drools Support Package.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
