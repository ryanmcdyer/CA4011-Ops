set PROD; # cars

param avail; # hours available/week in each stage
param min_efficiency;
param profit {PROD}; # profit per ton

param time {PROD} >= 0; # time to build each car
param orders {PROD} >= 0; # lower limit on cars made
param efficiency {PROD}; # efficiency per car

var Make {p in PROD} >= orders[p]; # tons produced

# Objective: total profits from all products
maximize Total_Profit: sum {p in PROD} profit[p] * Make[p];
subject to Time:
  sum {p in PROD} time[p] * Make[p] <= avail;
subject to Efficiency:
  sum {p in PROD} efficiency[p] * Make[p] >= min_efficiency * sum {p0 in PROD} Make[p0];
