
set PROD; # products
set STAGE; # stages

param rate {PROD,STAGE} > 0; # tons per hour in each stage
param avail {STAGE} >= 0; # hours available/week in each stage
param profit {PROD}; # profit per ton

param commit {PROD} >= 0; # lower limit on tons sold in week
param market {PROD} >= 0; # upper limit on tons sold in week

var Make {p in PROD} >= commit[p], <= market[p]; # tons produced

# Objective: total profits from all products
maximize Total_Profit: sum {p in PROD} profit[p] * Make[p];
subject to Time {s in STAGE}:
  sum {p in PROD} (1/rate[p,s]) * Make[p] <= avail[s];


#(b) How would you add to the model to restrict the total weight of all products to be less than a
#new parameter, max_weight? Solve the linear program for a weight limit of 6500 tons, and
#explain how this extra restriction changes the results.

param max_weight;
subject to Total_Weight:
  sum {p in PROD} Make[p] <= max_weight;
