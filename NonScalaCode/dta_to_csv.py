import pandas as pd

df = pd.read_stata("../Data/*.dta")

df.to_csv("../Data/zip_population_income_2022.csv", index=False)