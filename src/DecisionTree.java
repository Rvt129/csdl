
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.HashMap;

public class DecisionTree {

    // Attributes and Getters -------------------------------------
    private DTNode root;
    private DataFrame training;
    private int num_nodes;

    public DTNode getRoot() {
        return root;
    }

    public DataFrame getTrainingDataFrame() {
        return training;
    }

    public int getNumberNodes() {
        return num_nodes;
    }

    // Constructor -------------------------------------
    DecisionTree() {
        root = null;
        training = null;
        num_nodes = 0;
    }

    // Methods -------------------------------------
    // returns the most common value in a given Series object
    private Object getMostFrequentValue(Series s) {
        HashSet<Object> unique = s.getUniqueValues();
        int highest_freq = -1;
        ArrayList<Object> most_frequent = new ArrayList<>();
        for (Object value : unique) {
            // đếm số lần mỗi giá trị xuất hiện trong danh sách gốc 
            int freq = Collections.frequency(s.getDataList(), value);
            if (freq > highest_freq) {
                highest_freq = freq;
                most_frequent.clear();
                most_frequent.add(value);
            } else if (freq == highest_freq) {
                most_frequent.add(value);
            }
        }
        // breaking ties randomly, by choosing the first element of the shuffled list
        Collections.shuffle(most_frequent);
        return most_frequent.get(0);
    }

    // returns a leaf node whose classification is equal to the
    // most frequent target classification in the DataFrame data
    private DTNode pluralityValue(DataFrame data) {
        Series target = Utility.ModelSelection.getTarget(data);
        Object classification = getMostFrequentValue(target);
        return new DTNode(
                classification,
                Collections.frequency(target.getDataList(), classification)
        );
    }

    // returns the index of the attribute list that indicates the attribute with highest gain of information
    private int getAttributeHighestGain(DataFrame examples, ArrayList<Integer> attributes_id) {
        Series example_target = Utility.ModelSelection.getTarget(examples);
        int highest_gain_att_id = -1;
        double highest_gain = -1;
        for (int id : attributes_id) {
            Series attribute = examples.getColumn(id);
            double gain = Utility.Metrics.getGain(example_target, attribute);
            if (gain > highest_gain) {
                highest_gain = gain;
                highest_gain_att_id = id;
            }
        }
        return highest_gain_att_id;
    }

    private DTNode learnDecisionTree(DataFrame examples, ArrayList<Integer> attributes_id, DataFrame parent_examples, HashMap<String, HashSet<Object>> unique_values_per_attribute) {
        num_nodes++;

        // if examples is empty
        if (examples.getNumberRows() == 0) {
            return pluralityValue(parent_examples);
        }

        // if there are no attributes, only target
        if (attributes_id.size() == 0) {
            return pluralityValue(examples);
        }

        // if all examples have the same target value
        Series example_target = Utility.ModelSelection.getTarget(examples);
        if (example_target.getUniqueValues().size() == 1) {
            return pluralityValue(examples);
        }

        // else
        // (recursive case)
        int highest_gain_att_id = getAttributeHighestGain(examples, attributes_id);
        // new internal tree node where conditional attribute is the attribute which has higest information gain
        DTNode node = new DTNode(
                examples.getColumn(highest_gain_att_id).getName()
        );

        // gathering the attribute's unique values
        HashSet<Object> highest_gain_att_values = unique_values_per_attribute.get(
                examples.getColumn(highest_gain_att_id).getName()
        );

        for (Object value : highest_gain_att_values) {
            // reduced dataset with the examples where "highest info gain attribute" == value
            DataFrame exs = examples.filterBySpecificAttributeValue(highest_gain_att_id, value);

            // get new list of indexes to the remaining attributes
            ArrayList<Integer> new_attributes_id = new ArrayList<>();
            for (int i = 0; i < attributes_id.size() - 1; i++) {
                new_attributes_id.add(i);
            }

            // generate child node associated to "value" in the "highest info gain attribute" condition
            DTNode child = learnDecisionTree(
                    exs,
                    new_attributes_id,
                    examples,
                    unique_values_per_attribute
            );
            node.addChild(value, child);
        }

        return node;
    }

    // Fit method -------------------------------------
    public void fit(DataFrame fitting_data) {
        this.training = fitting_data;
        // fitting_data already was preprocessed
        ArrayList<Integer> attributes_id = new ArrayList<Integer>();
        HashMap<String, HashSet<Object>> unique_values_per_attribute = new HashMap<>();
        for (int i = 0; i < fitting_data.getNumberColumns() - 1; i++) {
            attributes_id.add(i);
            unique_values_per_attribute.put(fitting_data.getColumn(i).getName(),fitting_data.getColumn(i).getUniqueValues());
        }

        root = learnDecisionTree(fitting_data, attributes_id, null, unique_values_per_attribute);
    }

    // Printing -------------------------------------

    private void formatPrint(DTNode cur, String prefix) {
        if (cur.isLeaf()) {
            System.out.println(prefix + "Classification: " + cur.getClassification().toString() + " (count = " + cur.getCount() + ")");
            System.out.println(prefix);
            return;
        }
        System.out.println(prefix + "Attribute: " + cur.getNodeAttribute());
        Set<Object> unique = cur.getChildren().keySet();
        for (Object value : unique) {
            System.out.println(prefix + "|-> Value: " + value);
            formatPrint(cur.getChild(value), prefix + "|\t");
        }
        System.out.println(prefix);
    }

    // public printing methods
    public void printFormated() {
        formatPrint(root, "");
        System.out.println("Number of nodes: " + num_nodes);
    }

}
