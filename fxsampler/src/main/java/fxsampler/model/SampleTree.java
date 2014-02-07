package fxsampler.model;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.TreeItem;
import fxsampler.Sample;

public class SampleTree {
    private TreeNode root;
    
    private int count = 0;

    public SampleTree(Sample rootSample) {
        root = new TreeNode(null, null, rootSample);
    }
    
    public TreeNode getRoot() {
        return root;
    }
    
    public Object size() {
        return count;
    }
    
    public void addSample(String[] packages, Sample sample) {
        if (packages.length == 0) {
            root.addSample(sample);
            return;
        }
        
        TreeNode n = root;
        for (String packageName : packages) {
            if (n.containsChild(packageName)) {
                n = n.getChild(packageName);
            } else {
                TreeNode newNode = new TreeNode(packageName);
                n.addNode(newNode);
                n = newNode;
            }
        }
        
        if (n.packageName.equals(packages[packages.length - 1])) {
            n.addSample(sample);
            count++;
        }
    }
    
    @Override public String toString() {
        return root.toString();
    }
    

    public static class TreeNode {
        private final Sample sample;
        private final String packageName;
        
        private final TreeNode parent;
        private List<TreeNode> children;
        
        public TreeNode() {
            this(null, null, null);
        }
        
        public TreeNode(String packageName) {
            this(null, packageName, null);
        }
        
        public TreeNode(TreeNode parent, String packageName, Sample sample) {
            this.children = new ArrayList<>();
            this.sample = sample;
            this.parent = parent;
            this.packageName = packageName;
        }
        
        public boolean containsChild(String packageName) {
            if (packageName == null) return false;
            
            for (TreeNode n : children) {
                if (packageName.equals(n.packageName)) {
                    return true;
                }
            }
            return false;
        }
        
        public TreeNode getChild(String packageName) {
            if (packageName == null) return null;
            
            for (TreeNode n : children) {
                if (packageName.equals(n.packageName)) {
                    return n;
                }
            }
            return null;
        }
        
        public void addSample(Sample sample) {
            children.add(new TreeNode(this, null, sample));
        }
        
        public void addNode(TreeNode n) {
            children.add(n);
        }
        
        public Sample getSample() {
            return sample;
        }
        
        public String getPackageName() {
            return packageName;
        }
        
        public TreeItem<Sample> createTreeItem() {
            TreeItem<Sample> treeItem = null;
            
            if (sample != null) {
                treeItem = new TreeItem<Sample>(sample);
            } else if (packageName != null) {
                treeItem = new TreeItem<Sample>(new EmptySample(packageName));
            }
            
            treeItem.setExpanded(true);
            
            // recursively add in children
            for (TreeNode n : children) {
                treeItem.getChildren().add(n.createTreeItem());
            }
            
            return treeItem;
        }
        
        @Override public String toString() {
            if (sample != null) {
                return " Sample [ sampleName: " + sample.getSampleName() + ", children: " + children + " ]";
            } else {
                return " Sample [ packageName: " + packageName + ", children: " + children + " ]";
            }
        }
    }
}