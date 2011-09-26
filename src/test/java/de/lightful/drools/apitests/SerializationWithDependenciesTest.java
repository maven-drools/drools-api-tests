package de.lightful.drools.apitests;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.core.util.DroolsStreamUtils;
import org.drools.definition.KnowledgePackage;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
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

  @Test
  public void testCanSerializeAndDeSerialize() throws Exception {
    KnowledgeBuilder knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
    knowledgeBuilder.add(stringResource(DECLARED_TYPE_ONE), ResourceType.DRL);
    knowledgeBuilder.add(stringResource(DECLARED_TYPE_TWO), ResourceType.DRL);
    knowledgeBuilder.add(stringResource(RULE_USING_BOTH_TYPES), ResourceType.DRL);

    final Collection<KnowledgePackage> knowledgePackages = knowledgeBuilder.getKnowledgePackages();

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    DroolsStreamUtils.streamOut(byteArrayOutputStream, knowledgePackages);

    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    final Object deserializedKnowledge = DroolsStreamUtils.streamIn(byteArrayInputStream);
    assertThat(deserializedKnowledge).isNotNull();
    assertThat(deserializedKnowledge).isInstanceOf(Collection.class);
    Collection<Object> deserializedObjects = (Collection<Object>) deserializedKnowledge;
    assertThat(deserializedObjects).hasSize(2);
  }

  private Resource stringResource(String droolsSourcecodeSnippet) {
    return ResourceFactory.newReaderResource(new StringReader(droolsSourcecodeSnippet));
  }
}
