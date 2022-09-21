package matchers;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import utils.OutputWriter;
import utils.TestClass;
import utils.TestMethod;

import java.util.*;
import java.util.logging.Logger;

public class DuplicateAssertMatcher extends SmellMatcher {

  @Override
  protected void match(TestClass testClass) {
    for (TestMethod testMethod : testClass.getTestMethods()) {
      NodeList childrenMethods = testMethod.getMethodDeclaration().getChildNodes();
      for (int i = 0; i < childrenMethods.getLength(); ++i) {
        Node node = childrenMethods.item(i);
        if(node.getNodeName().equals("block")) {
          Map<String, Integer> allAsserts = new HashMap<>();
          getAllAsserts(node.getChildNodes(), allAsserts);
          for (Map.Entry<String, Integer> entry : allAsserts.entrySet()) {
            if (entry.getValue() > 1) {
              write(testMethod.getTestFilePath(), "Duplicate Assert", testMethod.getMethodName(), new LinkedList<>().toString());
            }
          }
        }
      }
    }
  }

    @Override
    public void write(String filePath, String testSmell, String name, String lines) {
        OutputWriter.getInstance().write(filePath, testSmell, name, lines);
        Logger.getLogger(AssertionRouletteMatcher.class.getName()).info("Found duplicate assert in method \"" + name + "\" in lines " + lines);
    }

  private void getAllAsserts(NodeList nodeList, Map<String, Integer> allAsserts) {
    for (int i = 0; i < nodeList.getLength(); ++i) {
      Node root = nodeList.item(i);
      if(root.getNodeName().equals("block_content")) {
        NodeList childrenBlockContent = root.getChildNodes();
        for(int k = 0; k < childrenBlockContent.getLength(); ++k) {
          Node childBlockContent = childrenBlockContent.item(k);
          if (childBlockContent.getNodeName().equals("expr_stmt")) {
            DocumentTraversal traversal = (DocumentTraversal) childBlockContent.getOwnerDocument();
            TreeWalker iterator = traversal.createTreeWalker(childBlockContent, NodeFilter.SHOW_ALL, null, false);
            Node node = null;
            while ((node = iterator.nextNode()) != null) {
              String textContent = node.getTextContent().trim();
              if (node.getNodeName().equals("expr") && (textContent.startsWith("assert") || textContent.startsWith("Assert"))) {
                if (allAsserts.containsKey(node.getTextContent())) {
                  allAsserts.put(node.getTextContent(), allAsserts.get(node.getTextContent()) + 1);
                } else {
                  allAsserts.put(node.getTextContent(), 1);
                }
              }
            }
          }
        }
      }
    }
  }
}
