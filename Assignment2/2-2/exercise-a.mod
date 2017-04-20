set EXERCISES;


var Do {e in EXERCISES} <= tolerance[p]; # tons produced

minimize Total_Time_Used: sum {e in EXERCISES} Do[p];
subject to Tolerance {e in EXERCISES}:
    Do[e] <= tolerance[e];
subject to Calories_Burned:
    sum {E in EXERCISES}



///////////////////////////////

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

# In each stage: total of hours used by all
# products may not exceed hours available
