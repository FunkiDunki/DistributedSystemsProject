import pandas as pd

dta_file = "../Data/zip_demographics.dta"
output_file = "../Data/zip_population_income_2022.csv"
columns_to_keep = ["ZCTA20", "TOTPOP", "MEDFAMINC"]
df = pd.read_stata(dta_file, columns=columns_to_keep)


df.to_csv(output_file, index=False)