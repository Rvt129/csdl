

public class Main {

    public static void main(String[] args) {

        // create the DataFrame object with the given CSV file
        DataFrame training = new DataFrame();
        training.readCSV("data.csv");

        // processing continuous values to discrete ones
        for (int i = 0; i < training.getNumberColumns()-1; i++)
            Utility.Encoding.discretize(training.getColumn(i));

        // create and train the Decision Tree
        DecisionTree dt = new DecisionTree();
        dt.fit(training);

        
        // print the decision tree
        System.out.println();
        dt.printFormated();
    }
}