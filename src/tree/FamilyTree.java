
package tree;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;

public class FamilyTree {

    private static class TreeNode<T> {
        private T data;
        private TreeNode<T> parent;
        private ArrayList<TreeNode<T>> children;

        TreeNode(T data) {
            this.data = data;
            children = new ArrayList<>();
        }

        T getData() {
            return data;
        }

        void addChild(TreeNode<T> childNode) {
            children.add(childNode);
            childNode.parent = this;
        }

        // Searches subtree at this node for a node with the given data.
        TreeNode<T> getNodeWithName(T targetData) {
            if (data.equals(targetData))
                return this;

            for (TreeNode<T> child : children) {
                TreeNode<T> result = child.getNodeWithName(targetData);

                if (result != null)
                    return result;
            }

            return null;
        }

        // Returns ancestors from parent to root.
        ArrayList<TreeNode<T>> collectAncestorsToList() {
            ArrayList<TreeNode<T>> ancestors = new ArrayList<>();

            TreeNode<T> current = parent;

            while (current != null) {
                ancestors.add(current);
                current = current.parent;
            }

            return ancestors;
        }

        public String toString() {
            return toStringWithIndent("");
        }

        private String toStringWithIndent(String indent) {
            String s = indent + data + "\n";
            indent += "  ";

            for (TreeNode<T> childNode : children)
                s += childNode.toStringWithIndent(indent);

            return s;
        }
    }

    private TreeNode<String> root;

    //
    // Displays a file browser so that user can select the family tree file.
    //
    public FamilyTree() throws IOException, TreeException {
        FileNameExtensionFilter filter =
            new FileNameExtensionFilter("Family tree text files", "txt");

        File dirf = new File("data");
        if (!dirf.exists())
            dirf = new File(".");

        JFileChooser chooser = new JFileChooser(dirf);
        chooser.setFileFilter(filter);

        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
            System.exit(1);

        File treeFile = chooser.getSelectedFile();

        // Parse the input file. Create a FileReader that reads treeFile.
        // Create a BufferedReader that reads from the FileReader.
        FileReader fr = new FileReader(treeFile);
        BufferedReader br = new BufferedReader(fr);

        String line;

        while ((line = br.readLine()) != null)
            addLine(line);

        br.close();
    }

    //
    // Line format is "parent:child1,child2 ..."
    // Throws TreeException if line is illegal.
    //
    private void addLine(String line) throws TreeException {

        // Extract parent and array of children.
        int colonIndex = line.indexOf(":");

        if (colonIndex < 0)
            throw new TreeException("Missing colon in line: " + line);

        String parent = line.substring(0, colonIndex);
        String childrenString = line.substring(colonIndex + 1);
        String[] childrenArray = childrenString.split(",");

        // Find parent node. If root is null then the tree is empty and the
        // parent node must be constructed. Otherwise the parent node should be
        // somewhere in the tree.
        TreeNode<String> parentNode;

        if (root == null)
            parentNode = root = new TreeNode<String>(parent);
        else {
            parentNode = root.getNodeWithName(parent);

            if (parentNode == null)
                throw new TreeException("Parent not found: " + parent);
        }

        for (String childName : childrenArray)
            parentNode.addChild(new TreeNode<String>(childName));
    }

    //
    // Returns the deepest node that is an ancestor of both named nodes.
    //
    TreeNode<String> getMostRecentCommonAncestor(
            String name1, String name2) throws TreeException {

        // Get nodes for input names.
        TreeNode<String> node1 = root.getNodeWithName(name1);

        if (node1 == null)
            throw new TreeException("Name not found: " + name1);

        TreeNode<String> node2 = root.getNodeWithName(name2);

        if (node2 == null)
            throw new TreeException("Name not found: " + name2);

        // Get ancestors of node1 and node2.
        ArrayList<TreeNode<String>> ancestorsOf1 =
            node1.collectAncestorsToList();

        ArrayList<TreeNode<String>> ancestorsOf2 =
            node2.collectAncestorsToList();

        // Check members of ancestorsOf1 in order until you find a node
        // that is also an ancestor of 2.
        for (TreeNode<String> n1 : ancestorsOf1) {
            if (ancestorsOf2.contains(n1))
                return n1;
        }

        // No common ancestor.
        return null;
    }

    public String toString() {
        return "Family Tree:\n\n" + root;
    }

    public static void main(String[] args) {
        try {
            FamilyTree tree = new FamilyTree();

            System.out.println("Tree:\n" + tree);

        } catch (IOException x) {
            System.out.println("IO trouble: " + x.getMessage());

        } catch (TreeException x) {
            System.out.println("Input file trouble: " + x.getMessage());
        }
    }
}
