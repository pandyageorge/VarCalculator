//Import statements for swing, which is what the GUI is based on, 
//awt ActionEvent and ActionListener for button clicks, 
//and java.util.Arrays for the data structure to store the historical data values
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.math.BigDecimal;
import java.math.RoundingMode;

//Define the class containing the code for the app, using inheritance to import JFrame, the class responsible for the GUI window
public class VarCalculatorGUI extends JFrame {

//	Private instance variables to represent each element of the GUI. 
//	Text area allows data input to be multiple lines, in the case of the portfolio calculation
    private JTextArea historicalDataInput;
    private JTextField confidenceLevelInput;
    private JButton calculateButton;
    private JLabel varResult;
    private JComboBox<String> tradeOption;
//    Box for debug output
//    private JTextArea logBox;

//    Constructor used to initialize the GUI when the class is instantiated
    public VarCalculatorGUI() {
        setLayout(new FlowLayout());

        // Initialize the GUI components
        historicalDataInput = new JTextArea(5, 20);
        JScrollPane scrollPane = new JScrollPane(historicalDataInput);
        confidenceLevelInput = new JTextField(20);
        calculateButton = new JButton("Calculate VaR");
        varResult = new JLabel("VaR will be displayed here");
        tradeOption = new JComboBox<>(new String[] {"Single Trade", "Portfolio"});
//        Box for debug output
//        logBox=new JTextArea(5,20);

        // Add the GUI components to the window in the following order
        add(new JLabel("Enter historical data for each trade on a new line:"));
        add(scrollPane);
        add(new JLabel("Enter confidence level (e.g., 0.95 for 95%):"));
        add(confidenceLevelInput);
        add(new JLabel("Select trade option:"));
        add(tradeOption);
        add(calculateButton);
        add(varResult);
//        Box for debug output
//        add(logBox);

        // Add button click event
        calculateButton.addActionListener(new ActionListener() {
//        	Override the default actionPerformed method in the action listener to pull in the input data
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
//                	Read the historical input data one line at a time, 
//                	splitting the comma delimited into their own array elements
                    String[] lines = historicalDataInput.getText().split("\\n");
                    double[][] historicalData = new double[lines.length][];
                    for (int i = 0; i < lines.length; i++) {
                        String[] dataStrings = lines[i].split(";");
                        historicalData[i] = new double[dataStrings.length];
                        for (int j = 0; j < dataStrings.length; j++) {
                            historicalData[i][j] = Double.parseDouble(dataStrings[j]);
                        }
                    }
                    
//                    Read in the confidence level from its field
                    double confidenceLevel = Double.parseDouble(confidenceLevelInput.getText());

                    // Validate the confidence level
                    if (confidenceLevel <= 0 || confidenceLevel >= 1) {
                        varResult.setText("Confidence level must be between 0 and 1 (exclusive).");
                        return;
                    }
                    
//                    Validate that the data entry isn't null or invalid
                    if (historicalData.length == 0) {
                        varResult.setText("Please enter valid historical data.");
                        return;
                    }
                    
//                    Make sure the right calculation option is selected for the given input type, between single trade and portfolio
                    if (tradeOption.getSelectedItem().equals("Single Trade") && historicalData.length>1) {
                        varResult.setText("Error: Single Trade-level VAR calculation requires data for one trade in a single line");
                        return;
                    }
                    else if (tradeOption.getSelectedItem().equals("Portfolio") && historicalData.length==1) {
                        varResult.setText("Error: Portfolio-level VAR calculation requires data for multiple trades");
                        return;
                    }
                    
//                    Calculate the VAR based on the selected option
                    double var;
                    if (tradeOption.getSelectedItem().equals("Single Trade")) {
                        var = calculateVaR(historicalData[0], confidenceLevel);
                    } else {
                        var = calculatePortfolioVaR(historicalData, confidenceLevel);
                    }
//                    Output the result and catch any errors
                    varResult.setText("<html>"+"The VaR at " + (confidenceLevel * 100) + "% confidence level is $" + round(var,2)+"<html>");
                } catch (NumberFormatException ex) {
                    varResult.setText("Invalid input data or confidence level. Please ensure all data are numeric values.");
                } catch (Exception ex) {
                    varResult.setText("An error occurred: " + ex.getMessage());
                }
            }
        });

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 300);
        setVisible(true);
    }
    

//  Method to round the output to two decimal places
  public static double round(double value, int digits) {
      BigDecimal bd = new BigDecimal(Double.toString(value));
      bd = bd.setScale(digits, RoundingMode.HALF_UP);
      return bd.doubleValue();
  }    


    // The method calculates VaR for a single trade
    public double calculateVaR(double[] historicalData, double confidenceLevel) {
//        // Sort the array
        Arrays.sort(historicalData);
//
//        Casting to integer below ensures that the index is a whole number. 
//        The var index is based on 1-confidence level times the number of elements, rounded down, according to the VAR definition
        int varIndex = (int) (historicalData.length * (1 - confidenceLevel));
//
//        // Return the VaR value
        return historicalData[varIndex];
//    	return calculatePercentile(historicalData,confidenceLevel);
    }
    

// The method to calculate Portfolio VaR
//  Casting to integer below ensures that the index is a whole number. 
//  The var index is based on 1-confidence level times the number of elements, rounded down, according to the VAR definition
    public double calculatePortfolioVaR(double[][] historicalData, double confidenceLevel) {
// 		Calculate portfolio values for each point in time, summing down the "columns" 
//    	of the array, until it's flattened into a single line array 
        double[] portfolioValues = new double[historicalData[0].length];
        for (int i = 0; i < historicalData[0].length; i++) {
            for (double[] data : historicalData) {
                portfolioValues[i] += data[i];
            }
            
//        Debug test script    
//        for (int j=0; j<2;j++) {
//            logBox.append(Double.toString(portfolioValues[j]));
//
//        }
           
        }
//       Once it's flattened, calculate it the same as the single stock return
//       Sort the array
        Arrays.sort(portfolioValues);

//      Casting to integer below ensures that the index is a whole number. 
//      The var index is based on 1-confidence level times the number of elements, rounded down, according to the VAR definition
        int varIndex = (int) (portfolioValues.length * (1 - confidenceLevel));

//       Return the VaR value
        return portfolioValues[varIndex];
    }
    // Main method to execute the code
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new VarCalculatorGUI();
            }
        });
    }


}
