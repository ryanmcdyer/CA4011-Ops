set PROD; # cars

param avail; # hours available/week in each stage
param profit {PROD}; # profit per ton

param time {PROD} >= 0; # time to build each car
param orders {PROD} >= 0; # lower limit on cars made

var Make {p in PROD} >= orders[p]; # tons produced

# Objective: total profits from all products
maximize Total_Make: sum {p in PROD} Make[p];
subject to Time:
  sum {p in PROD} time[p] * Make[p] <= avail;
