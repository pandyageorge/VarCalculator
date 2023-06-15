# tkinter library is used to create the GUI
import tkinter as tk
from tkinter import messagebox
# numpy arrays are used to store inputs
import numpy as np

class VaRCalculatorGUI:
    
#     init method is the constructor that's called when an object of this class is instantiated, creating the GUI
    def __init__(self):
#         create the tkinter GUI window
        self.window = tk.Tk()
        self.window.title("VaR Calculator")
        
#         Debugging box
#         self.logbox = tk.Text(self.window, width=80, height=20)


#         Data entry box. Using the text class of tk because it allows lines, which is needed for portfolio VAR
        self.hist_data_label = tk.Label(self.window, text="Enter historical data for each trade on a new line, separated by a semicolon:",wraplength=300, justify="center")
        self.hist_data_entry = tk.Text(self.window, width=30, height=10)

#         Confidence interval entry box
        self.conf_lvl_label = tk.Label(self.window, text="Enter confidence level (e.g., 0.95 for 95%):",wraplength=300, justify="center")
        self.conf_lvl_entry = tk.Entry(self.window, width=30)

#         Button to choose between trade or portfolio, with the default being the single trade VAR
        self.trade_option_label = tk.Label(self.window, text="Select trade option:")
#     
        self.trade_option = tk.StringVar(self.window) 
        self.trade_option.set("Single Trade")
        self.trade_option_menu = tk.OptionMenu(self.window, self.trade_option, "Single Trade", "Portfolio")
#         Create action button to calculate the VAr, calling the below calculate_var method
        self.calculate_button = tk.Button(self.window, text="Calculate VaR", command=self.calculate_var)

        self.var_label = tk.Label(self.window, text="VaR will be displayed here", width=50,wraplength=300, justify="center")
        
#       Using the pack arrangement of tkinter to automatically arrange elements
        self.hist_data_label.pack()
        self.hist_data_entry.pack()
        self.conf_lvl_label.pack()
        self.conf_lvl_entry.pack()
        self.trade_option_label.pack()
        self.trade_option_menu.pack()
        self.calculate_button.pack()
        self.var_label.pack()
        self.var_label.pack()
        
#         debugging box
#         self.logbox.pack()

    def calculate_var(self):
#         try-except allows for error handling
        try:
#             pull in the data from the text box, split it into lines, split the lines into separate numbers, converts them into floats, and converts this list into a numpy array, along with the confidence level, cast to a float
#             The get method reads the value in the text box from the first character until 1 character from the end, eliminating the extra line that is automatically added at the end
            hist_data = np.array([list(map(float, line.split(';'))) for line in self.hist_data_entry.get('1.0', 'end-1c').split('\n') if line])
            conf_lvl = float(self.conf_lvl_entry.get())

            # Validate the input
            if conf_lvl <= 0 or conf_lvl >= 1:
                self.var_label.config(text="Error: Confidence level must be between 0 and 1 (exclusive).")
                return

            if len(hist_data) == 0:
                self.var_label.config(text="Error: Please enter valid historical data.")
                return
            
            if hist_data.shape[0]>1 and self.trade_option.get() == "Single Trade":
#             messagebox.showerror("Error", "Single Trade-level VAR calculation requires data for one trade in a single line")
                self.var_label.config(text="Error: Single Trade-level calculation requires data for one trade in a single line")
                return

            if hist_data.shape[0]==1 and self.trade_option.get() == "Portfolio":
    #             messagebox.showerror("Error", "Portfolio-level VAR calculation requires data for multiple trades")
                self.var_label.config(text="Error: Portfolio-level calculation requires data for multiple trades")
                return
        
            # Calculate VaR based on whether this is at the single trade or portfolio level, calling the below respective method
            if self.trade_option.get() == "Single Trade":
                var = self.calculate_single_trade_var(hist_data, conf_lvl)
            else:
                var = self.calculate_portfolio_var(hist_data, conf_lvl)
#        display the calculated VAR at the bottom of the box
            self.var_label.config(text="The VaR at " + str(conf_lvl * 100) + "% confidence level is $" + str(round(var,2)))

#           Vestigial debug script
#             self.var_label.config(text=#"len "+str(len(hist_data))+"index "+
#                                   str(int(len(hist_data.flatten()) * (1 - conf_lvl))) +" ndim="+str(hist_data.shape[0])+","+str(hist_data.shape[1])+"The VaR at " + str(conf_lvl * 100) + "% confidence level is $" + str(var))

        except ValueError:
            self.var_label.config(text="Error: Invalid input data or confidence level. Please ensure all data are numeric values.")
        except Exception as e:
            self.var_label.config(text="Error:"+ f"An error occurred: {str(e)}")

#             
    def calculate_single_trade_var(self, hist_data, conf_lvl):
#        Casting to integer below ensures that the index is a whole number. 
#        The var index is based on 1-confidence level times the number of elements, rounded down, according to the VAR definition
       

        hist_data_1d=hist_data.flatten()
        var_index = int(len(hist_data_1d) * (1 - conf_lvl)) 
        return np.sort(hist_data_1d)[var_index]
#         return np.percentile(hist_data,(1-conf_lvl)*100,interpolation='midpoint')

    def calculate_portfolio_var(self, hist_data, conf_lvl):
        
#        Casting to integer below ensures that the index is a whole number. 
#        The var index is based on 1-confidence level times the number of elements, rounded down, according to the VAR definition
       
        var_index = int(len(hist_data[0]) * (1 - conf_lvl))
        
#         debugging box inputs
#         self.logbox.insert("end",(var_index))
#         self.logbox.insert("end",np.sort(np.sum(hist_data, axis=0)))
        return np.sort(np.sum(hist_data, axis=0))[var_index]
        
#         Previous function to sum across all trades and return the percentile of 1-confidence level
#         portfolio_data=np.sum(hist_data,axis=0)
#         return np.percentile(portfolio_data,(1-conf_lvl)*100)
#         Ended up using basic linear percentile without interpolation above, to keep consistency with Java code, 
#         as there are many ways to take percentiles, and this is the simplest

#     Run method 
    def run(self):
        self.window.mainloop()

if __name__ == "__main__":
    VaRCalculatorGUI().run()
